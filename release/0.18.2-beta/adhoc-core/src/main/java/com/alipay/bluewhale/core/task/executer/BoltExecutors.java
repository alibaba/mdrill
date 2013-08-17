package com.alipay.bluewhale.core.task.executer;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import backtype.storm.Config;
import backtype.storm.serialization.KryoTupleDeserializer;
import backtype.storm.task.IBolt;
import backtype.storm.task.IOutputCollector;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.tuple.Tuple;
import backtype.storm.utils.TimeCacheMap;

import com.alipay.bluewhale.core.callback.RunnableCallback;
import com.alipay.bluewhale.core.cluster.StormConfig;
import com.alipay.bluewhale.core.messaging.IConnection;
import com.alipay.bluewhale.core.stats.BaseTaskStatsRolling;
import com.alipay.bluewhale.core.task.error.ITaskReportErr;
import com.alipay.bluewhale.core.task.transfer.TaskSendTargets;
import com.alipay.bluewhale.core.utils.EvenSampler;
import com.alipay.bluewhale.core.utils.StormUtils;
import com.alipay.bluewhale.core.work.transfer.WorkerTransfer;

/**
 * 
 * bolt 处理器
 * 
 * chages
 * 
 * 我把mk-executors IBolt 中的 tuple-start-times (ConcurrentHashMap.) pending-acks
 * (ConcurrentHashMap.) 类型更换成了 TimeCacheMap类型
 * 
 * 有些时候，没准在bolt里会有一些bug,他们忘记调用，ack或者fail.这个时候spout的pending就会超时，他就会重发这个tuple.
 * 但是在pending-acks和tuple-start-time里就不会被清理掉，这样就会导致ConcurrentHashMap
 * 变得越来越大，这就是我修改的原因 目前我使用storm_conf.get(Config.TOPOLOGY_MESSAGE_TIMEOUT_SECS)
 * 这个配置,来设置超时的时间
 * 
 * 
 * @author yannian
 * 
 */
public class BoltExecutors extends RunnableCallback {
	private static Logger LOG = Logger.getLogger(BoltExecutors.class);
	private IConnection puller;
	private AtomicBoolean zkActive;
	private Integer task_id;
	private IBolt bolt;
	private KryoTupleDeserializer deserializer;
	private TimeCacheMap<Tuple, Long> tuple_start_times;
	private EvenSampler sampler;
	private Exception errorReport = null;

	public BoltExecutors(IBolt _bolt, WorkerTransfer _transfer_fn,
			Map storm_conf, IConnection _puller, TaskSendTargets _send_fn,
			AtomicBoolean _storm_active_atom,
			TopologyContext _topology_context, TopologyContext _user_context,
			BaseTaskStatsRolling _task_stats, ITaskReportErr _report_error) {
		this.bolt = _bolt;
		this.puller = _puller;
		this.zkActive = _storm_active_atom;
		this.task_id = _topology_context.getThisTaskId();
		this.sampler = StormConfig.mk_stats_sampler(storm_conf);

		String component_id = _topology_context.getThisComponentId();

		String timeoutkey = Config.TOPOLOGY_MESSAGE_TIMEOUT_SECS;
		int message_timeout_secs = StormUtils.parseInt(storm_conf
				.get(timeoutkey));

		// 原先是 tuple-start-times (ConcurrentHashMap.)
		// 主要是防止 在bolt中，如果业务逻辑有bug，在某一条件下忘记调用ack或fail，就会导致这俩map越来越大，直到内存不够用

		this.tuple_start_times = new TimeCacheMap<Tuple, Long>(
				message_timeout_secs);

		IOutputCollector output_collector = new BoltCollector(
				message_timeout_secs, _report_error, _send_fn, _transfer_fn,
				_topology_context, task_id, tuple_start_times, _task_stats);
		LOG.info("Preparing bolt " + component_id + ":" + task_id);
		bolt.prepare(storm_conf, _user_context, new OutputCollector(
				output_collector));
		LOG.info("Prepared bolt " + component_id + ":" + task_id);
		deserializer = new KryoTupleDeserializer(storm_conf, _topology_context);

	}

	@Override
	public void run() {

		byte[] ser_msg = puller.recv();

		if (ser_msg != null && ser_msg.length > 0) {
			LOG.debug("Processing message");
			Tuple tuple = deserializer.deserialize(ser_msg);

			if (sampler.getResult()) {
				tuple_start_times.put(tuple, System.currentTimeMillis());
			}

			try {
				bolt.execute(tuple);
			} catch (RuntimeException e) {
				errorReport = e;
				LOG.error("bolt execute error ", e);
			} catch (Exception e) {
				errorReport = e;
				LOG.error("bolt execute error ", e);
			}

		}

	}

	@Override
	public Object getResult() {
		if (this.IsActive()) {
			return 0;
		} else {
			return -1;
		}
	}

	public boolean IsActive() {
		return zkActive.get();
	}

	@Override
	public Exception error() {
		return errorReport;
	}
}
