package com.alipay.bluewhale.core.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.ConcurrentHashMap;


import org.apache.log4j.Logger;

import backtype.storm.task.TopologyContext;

import com.alipay.bluewhale.core.callback.RunnableCallback;
import com.alipay.bluewhale.core.cluster.ClusterState;
import com.alipay.bluewhale.core.cluster.Common;
import com.alipay.bluewhale.core.cluster.StormClusterState;
import com.alipay.bluewhale.core.cluster.StormConfig;
import com.alipay.bluewhale.core.cluster.StormZkClusterState;
import com.alipay.bluewhale.core.messaging.IConnection;
import com.alipay.bluewhale.core.messaging.IContext;
import com.alipay.bluewhale.core.stats.BaseTaskStatsRolling;
import com.alipay.bluewhale.core.task.common.TaskShutdownDameon;
import com.alipay.bluewhale.core.task.common.TasksCommon;
import com.alipay.bluewhale.core.task.error.ITaskReportErr;
import com.alipay.bluewhale.core.task.error.TaskReportError;
import com.alipay.bluewhale.core.task.error.TaskReportErrorAndDie;
import com.alipay.bluewhale.core.task.group.MkGrouper;
import com.alipay.bluewhale.core.task.heartbeat.TaskHeartbeatRunable;
import com.alipay.bluewhale.core.task.transfer.TaskSendTargets;
import com.alipay.bluewhale.core.task.transfer.UnanchoredSend;
import com.alipay.bluewhale.core.utils.AsyncLoopThread;
import com.alipay.bluewhale.core.utils.EvenSampler;
import com.alipay.bluewhale.core.utils.StormUtils;
import com.alipay.bluewhale.core.utils.UptimeComputer;
import com.alipay.bluewhale.core.work.WorkerHaltRunable;
import com.alipay.bluewhale.core.work.transfer.WorkerTransfer;

/**
 * task的启动
 * 
 * @author yannian
 * 
 */
public class Task {

	private final static Logger LOG = Logger.getLogger(Task.class);

	@SuppressWarnings("rawtypes")
	private Map stormConf;
	private TopologyContext topologyContext;
	private TopologyContext userContext;
	private String topologyid;
	private IContext mqContext;

	private AtomicBoolean stormActive;
	private WorkerTransfer workerTransfer;
	private WorkerHaltRunable workHalt;

	private Integer taskid;
	private String componentid;
	private AtomicBoolean active = new AtomicBoolean(true);
	// 创建运行时间计数器
	private UptimeComputer uptime = new UptimeComputer();

	private StormClusterState zkCluster;
	private Object taskObj;
	private BaseTaskStatsRolling taskStats;
	
	
	@SuppressWarnings("rawtypes")
	public Task(Map conf, Map stormConf, TopologyContext topologyContext,
			TopologyContext userContext, String topologyid, IContext mqContext,
			ClusterState clusterState, AtomicBoolean stormActive,
			WorkerTransfer workerTransfer, WorkerHaltRunable workHalt)
			throws Exception {
		this.topologyContext = topologyContext;
		this.userContext = userContext;
		this.topologyid = topologyid;
		this.mqContext = mqContext;
		this.stormActive = stormActive;
		this.workerTransfer = workerTransfer;
		this.workHalt = workHalt;

		this.taskid = topologyContext.getThisTaskId();
		
		this.componentid = topologyContext.getThisComponentId();
		this.stormConf = TasksCommon.component_conf(stormConf, topologyContext,
				componentid);
		LOG.info("Loading task " + componentid + ":" + taskid);
		this.zkCluster = new StormZkClusterState(clusterState);

		// 获取task_obj对象,其实就是根据Component_id获取队形的SpoutSpec、bolt、StateSpoutSpec
		this.taskObj = TasksCommon.get_task_object(
				topologyContext.getRawTopology(), componentid);
		// 创建task统计的对象-参见stats类
		int samplerate = StormConfig.sampling_rate(stormConf);
		this.taskStats = TasksCommon.mk_task_stats(taskObj, samplerate);
	}

	private TaskSendTargets makeSendTargets() {
		String taskName = TasksCommon.get_readable_name(topologyContext);
		EvenSampler statSample = StormConfig.mk_stats_sampler(stormConf);
		//// 获取当前task的每个stream应该流向那些commponID,以及他们是如何分组的
		// <Stream_id,<component,Grouping>>
		Map<String, Map<String, MkGrouper>> streamComponentGrouper = TasksCommon
				.outbound_components(topologyContext);
		Map<Integer, String> task2Component = topologyContext
				.getTaskToComponent();
		Map<String, List<Integer>> component2Tasks = StormUtils
				.reverse_map(task2Component);
		return new TaskSendTargets(stormConf, taskName, streamComponentGrouper,
				topologyContext, statSample, component2Tasks, taskStats);
	}

	private RunnableCallback mkExecutor(IConnection puller,
			TaskSendTargets sendTargets) {
		// 创建出错上报回调函，实际上是调用storm_cluster.report-task-error
		ITaskReportErr reportError = new TaskReportError(zkCluster, topologyid,
				taskid);
		// 创建出错，并退出对象-实际上是调用上步的report_error，并进行halt_process
		TaskReportErrorAndDie reportErrorDie = new TaskReportErrorAndDie(
				reportError, workHalt);
		return TasksCommon.mk_executors(taskObj, workerTransfer, stormConf,
				puller, sendTargets, stormActive, topologyContext, userContext,
				taskStats, reportErrorDie);
	}

	public TaskShutdownDameon execute() throws Exception {

		IConnection puller = mqContext.bind(topologyid, taskid);

		// 创建心跳线程
		TaskHeartbeatRunable hb = new TaskHeartbeatRunable(zkCluster, topologyid,
				taskid, uptime, taskStats, active, stormConf);
		AsyncLoopThread heartbeat_thread = new AsyncLoopThread(hb, true,
				Thread.MAX_PRIORITY, true);

		// 调用tuple发送函数，向系统stream发送startup消息
		List<Object> msg = new ArrayList<Object>();
		msg.add("startup");

		// 创建task接收对象
		TaskSendTargets sendTargets = makeSendTargets();
		UnanchoredSend.send(topologyContext, sendTargets, workerTransfer,
				Common.SYSTEM_STREAM_ID, msg);

		// 创建线程，从zeroMq中读取tuple,交给spout或bolt进行处理，然后发送给worker
		RunnableCallback componsementExecutor = mkExecutor(puller, sendTargets);
		AsyncLoopThread executor_threads = new AsyncLoopThread(
				componsementExecutor);
		AsyncLoopThread[] all_threads = { executor_threads, heartbeat_thread };

		LOG.info("Finished loading task " + componentid + ":" + taskid);

		return getShutdown(all_threads, heartbeat_thread, puller);
	}

	public TaskShutdownDameon getShutdown(AsyncLoopThread[] all_threads,
			AsyncLoopThread heartbeat_thread, IConnection puller) {
		TaskShutdownDameon shutdown = new TaskShutdownDameon(active, topologyid,
				taskid, mqContext, all_threads, zkCluster, puller, taskObj,
				heartbeat_thread);
		return shutdown;
	}

	@SuppressWarnings("rawtypes")
	public static TaskShutdownDameon mk_task(Map conf, Map stormConf,
			TopologyContext topologyContext, TopologyContext userContext,
			String stormId, IContext mqContext, ClusterState clusterState,
			AtomicBoolean stormActive, WorkerTransfer workerTransfer,
			WorkerHaltRunable workHalt) throws Exception {

		Task t = new Task(conf, stormConf, topologyContext, userContext,
				stormId, mqContext, clusterState, stormActive, workerTransfer,
				workHalt);

		return t.execute();
	}

}
