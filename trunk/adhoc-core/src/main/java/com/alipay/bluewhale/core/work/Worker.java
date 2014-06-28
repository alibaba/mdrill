package com.alipay.bluewhale.core.work;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import backtype.storm.Config;
import backtype.storm.daemon.Shutdownable;
import backtype.storm.generated.StormTopology;
import backtype.storm.serialization.KryoTupleSerializer;
import backtype.storm.task.TopologyContext;
import backtype.storm.utils.Utils;

import com.alipay.bluewhale.core.callback.RunnableCallback;
import com.alipay.bluewhale.core.cluster.Cluster;
import com.alipay.bluewhale.core.cluster.ClusterState;
import com.alipay.bluewhale.core.cluster.Common;
import com.alipay.bluewhale.core.cluster.StormClusterState;
import com.alipay.bluewhale.core.cluster.StormConfig;
import com.alipay.bluewhale.core.daemon.NodePort;
import com.alipay.bluewhale.core.messaging.IConnection;
import com.alipay.bluewhale.core.messaging.IContext;
import com.alipay.bluewhale.core.messaging.MsgLoader;
import com.alipay.bluewhale.core.task.Task;
import com.alipay.bluewhale.core.task.common.TaskShutdownDameon;
import com.alipay.bluewhale.core.utils.AsyncLoopThread;
import com.alipay.bluewhale.core.utils.PathUtils;
import com.alipay.bluewhale.core.utils.StormUtils;
import com.alipay.bluewhale.core.work.context.SystemContextMake;
import com.alipay.bluewhale.core.work.context.UserContextMake;
import com.alipay.bluewhale.core.work.refresh.RefreshActive;
import com.alipay.bluewhale.core.work.refresh.RefreshConnections;
import com.alipay.bluewhale.core.work.refresh.WorkerHeartbeatRunable;
import com.alipay.bluewhale.core.work.transfer.DrainerRunable;
import com.alipay.bluewhale.core.work.transfer.TransferData;
import com.alipay.bluewhale.core.work.transfer.WorkerTransfer;
import com.alipay.bluewhale.core.work.transfer.WorkerVirtualPort;

/**
 * worker启动进程
 * 
 * @author yannian
 * 
 */
public class Worker {

	private static Logger LOG = Logger.getLogger(Worker.class);
	// 系统配置
	@SuppressWarnings("rawtypes")
	private Map conf;
	// 用户配置
	@SuppressWarnings("rawtypes")
	private Map stormConf;

	// 消息队列对象
	private IContext mqContext;
	private String topologyId;
	private String supervisorId;
	private Integer port;
	private String workerId;

	// 当前worker是否是active状态
	private AtomicBoolean active;
	// 从zk中读取的当前storm是否是active状态
	private AtomicBoolean zkActive;

	// zk相关
	private ClusterState zkClusterstate;
	private StormClusterState zkCluster;
	
	
	private static StormClusterState shareCluster=null;
	
	public static StormClusterState getCluster()
	{
	   return  shareCluster;
	}
	
	public static Integer workerport=0;
	
	public static Integer getWorkPort()
	{
	    return workerport;
	}

	// 保存了当前worker到其他worker的链接
	private ConcurrentHashMap<NodePort, IConnection> nodeportSocket;
	// 保存了某个taskid，在那个worker上执行
	private ConcurrentHashMap<Integer, NodePort> taskNodeport;
	// 本地task发出的消息缓冲在这里
	// 当前worker执行的task列表
	private Set<Integer> taskids;
	// 系统TopologyContext的maker，带acker的
	private SystemContextMake systemContext;
	// 用户TopologyContext的maker,不带acker
	private UserContextMake userContext;
	// 保存每个task对应的componentId
	private HashMap<Integer, String> tasksToComponent;
	// 如果系统出粗，则会通过此方法，退出当前进程
	private final WorkerHaltRunable workHalt = new WorkerHaltRunable();

	//private ReentrantReadWriteLock endpoint_socket_lock=new ReentrantReadWriteLock();
	@SuppressWarnings("rawtypes")
	public Worker(Map conf, IContext mq_context, String topology_id,
			String supervisor_id, int port, String worker_id)
			throws Exception {
		
		LOG.info("Launching worker for " + topology_id + " on " + supervisor_id
				+ ":" + port + " with id " + worker_id + " and conf " + conf);

		this.conf = conf;
		this.mqContext = mq_context;
		this.topologyId = topology_id;
		this.supervisorId = supervisor_id;
		this.port = port;
		workerport=port;
		this.workerId = worker_id;

		this.active = new AtomicBoolean(true);
		this.zkActive = new AtomicBoolean();
		
		if (StormConfig.cluster_mode(conf).equals("distributed")) {
			PathUtils.touch(StormConfig.worker_pid_path(conf, worker_id,
					StormUtils.process_pid()));
		}
		
		// 创建与zk的链接
		this.zkClusterstate =Cluster.mk_distributed_cluster_state(conf);
		this.zkCluster = Cluster.mk_storm_cluster_state(zkClusterstate);
		shareCluster=this.zkCluster;

		this.stormConf = StormConfig.read_supervisor_storm_conf(conf, topology_id);

		// 创建zeroMq对象ZMQContext
		if (this.mqContext == null) {
			int zmqThreads = StormUtils.parseInt(stormConf.get(Config.ZMQ_THREADS));
			int linger = StormUtils.parseInt(stormConf.get(Config.ZMQ_LINGER_MILLIS));
			boolean isLocal = stormConf.get(Config.STORM_CLUSTER_MODE).equals("local");
			this.mqContext = MsgLoader.mk_zmq_context(zmqThreads, linger,isLocal);
		}

		this.nodeportSocket = new ConcurrentHashMap<NodePort, IConnection>();
		this.taskNodeport = new ConcurrentHashMap<Integer, NodePort>();
		this.tasksToComponent = Common.topology_task_info(zkCluster, topologyId);
		//当前worker的taskid列表
		this.taskids = WorkCommon.readWorkerTaskids(zkCluster, topologyId,supervisorId, port);
		//从本地读取supervisor目录下序列化的topology文件
		StormTopology topology = StormConfig.read_supervisor_topology(conf,	topology_id);
		// 创建系统topology,添加上acker
		this.systemContext = new SystemContextMake(topology, stormConf,
				topologyId, worker_id, tasksToComponent);
		// 创建无acker的StormTopology
		this.userContext = new UserContextMake(topology, stormConf, topologyId,
				worker_id, tasksToComponent);

	}

	private RefreshConnections makeRefreshConnections(Set<Integer> task_ids) {
		//计算 每个taskid，产生的tupple有可能流向那些tasks，
		Set<Integer> outboundTasks = WorkCommon.worker_outbound_tasks(
				tasksToComponent, systemContext, task_ids);
		RefreshConnections refresh_connections = new RefreshConnections(active,
				conf, zkCluster, topologyId, outboundTasks, nodeportSocket,
				mqContext, taskNodeport);
		return refresh_connections;
	}

	public WorkerShutdown execute() throws Exception {

		
		

		// 执行创建虚拟端口对象，worker接收发送过来的tuple,然后根据task_id，分发给本地的相关task(通过zeromo的本地模式)
		WorkerVirtualPort virtual_port = new WorkerVirtualPort(conf,
				supervisorId, topologyId, port, mqContext, taskids);
		Shutdownable 	 virtual_port_shutdown = virtual_port.launch();

		
		

		TopologyContext systemTopology = systemContext.make(null);

		// 刷新链接
		RefreshConnections refreshConn = makeRefreshConnections(taskids);
		refreshConn.run();

		// 刷新zk中的active状态
		RefreshActive refreshZkActive = new RefreshActive(active, conf,
				zkCluster, topologyId, zkActive);
		refreshZkActive.run();

		// 创建心跳线程
		RunnableCallback heartbeat_fn = new WorkerHeartbeatRunable(conf,
				workerId, port, topologyId, new CopyOnWriteArraySet<Integer>(taskids),
				active);
		heartbeat_fn.run();

		// 创建worker发送tuple的缓冲区
		LinkedBlockingQueue<TransferData> transferQueue = new LinkedBlockingQueue<TransferData>();

		
		// 创建消息tuple发送线程，将task存入到transfer_queue中的Tuple发送到目标work
		KryoTupleSerializer serializer = new KryoTupleSerializer(stormConf,systemTopology);
		
		// 创建用来接收task发送消息的队列，task发送的消息，临时存储在这s里
		WorkerTransfer workerTransfer = new WorkerTransfer(serializer, transferQueue);

		// 创建停止进程回调函数
		List<TaskShutdownDameon> shutdowntasks = new ArrayList<TaskShutdownDameon>();
		// 启动task
		if (taskids != null) {
			for (int taskid : taskids) {

				TaskShutdownDameon t = Task.mk_task(conf, stormConf,
						systemContext.make(taskid), userContext.make(taskid),
						topologyId, mqContext, zkClusterstate, zkActive,
						workerTransfer, workHalt);

				shutdowntasks.add(t);
			}
		}

		//FIXME 换成线程池操作
		// worker本身启动四个线程
		AsyncLoopThread refreshconn = new AsyncLoopThread(refreshConn);

		AsyncLoopThread refreshzk = new AsyncLoopThread(refreshZkActive);

		AsyncLoopThread hb = new AsyncLoopThread(heartbeat_fn, false, null,
				Thread.MAX_PRIORITY, true);

		AsyncLoopThread dr = new AsyncLoopThread(new DrainerRunable(transferQueue, nodeportSocket, taskNodeport));

		AsyncLoopThread[] threads = { refreshconn, refreshzk, hb, dr };


		return new WorkerShutdown(shutdowntasks, active, nodeportSocket,
				virtual_port_shutdown, mqContext, threads, zkCluster,
				zkClusterstate);

	}

	/**
	 * 创建 work进程操作
	 * 
	 * @param conf
	 * @param mq_context
	 * @param topology_id
	 * @param supervisor_id
	 * @param port
	 * @param worker_id
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	public static WorkerShutdown mk_worker(Map conf, IContext mq_context,
			String topology_id, String supervisor_id, int port,
			String worker_id) throws Exception {
		Worker w = new Worker(conf, mq_context, topology_id, supervisor_id, port,
				worker_id);
		return w.execute();
	}

	/**
	 * work启动主函数
	 * 
	 * @param args
	 */
	@SuppressWarnings("rawtypes")
	public static void main(String[] args) {
		if (args.length != 4) {
			LOG.error("the length of args is less than 4");
			return;
		}
		String topology_id = args[0];
		String supervisor_id = args[1];
		String port_str = args[2];
		String worker_id = args[3];

		Map conf = Utils.readStormConfig();
		// 如果非分布式模式 则退出
		Common.validate_distribute_mode(conf);
		try {
			WorkerShutdown sd = mk_worker(conf, null, topology_id,
					supervisor_id, Integer.parseInt(port_str), worker_id);
			sd.join();
			LOG.info("WorkerShutdown topology_id=" + topology_id + ",port_str="
					+ port_str);
		}  catch (Throwable e) {
			LOG.error("make worker error" ,e);
			LOG.info("WorkerShutdown topology_id=" + topology_id + ",port_str="
					+ port_str);
			System.exit(0);

		}
	}

}
