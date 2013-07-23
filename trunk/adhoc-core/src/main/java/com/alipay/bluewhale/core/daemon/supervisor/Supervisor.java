package com.alipay.bluewhale.core.daemon.supervisor;

import java.io.File;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import backtype.storm.utils.LocalState;
import backtype.storm.utils.Utils;

import com.alipay.bluewhale.core.cluster.Cluster;
import com.alipay.bluewhale.core.cluster.Common;
import com.alipay.bluewhale.core.cluster.StormClusterState;
import com.alipay.bluewhale.core.cluster.StormConfig;
import com.alipay.bluewhale.core.event.EventManager;
import com.alipay.bluewhale.core.event.EventManagerImp;
import com.alipay.bluewhale.core.messaging.ZMQContext;
import com.alipay.bluewhale.core.utils.AsyncLoopThread;
import com.alipay.bluewhale.core.utils.NetWorkUtils;
import com.alipay.bluewhale.core.utils.SmartThread;
import com.alipay.bluewhale.core.utils.TimeUtils;

/**
 * supervisor 主要完成了以下几个工作：
 * 1. 向zk中写入自己的心跳supervisorInfo. 目前没用于监控
 * 2. 启动一个线程，每隔10s执行SynchronizeSupervisor：
 *    (1) 下载要下载的代码、jar包和配置文件；
 *    (2) 删除那些没有被nimbus分配任务的topology资源；
 *    (3) 将nimbus分配到该supervisor的任务写入到/storm-local-dir/supervisor/localstate中，
 *        LocalState可以看作是一个KV database;
 *    (4) 将syncProcesses添加到对应的事件队列中。
 * 3. 启动一个线程，每隔supervisor.monitor.frequency.secs执行SyncProcesses：
 *    (1) 关闭心跳异常(不存在，超时)的worker；
 *    (2) 找出需要启动的worker，启动。
 */

public class Supervisor {

	private static Logger LOG = Logger.getLogger(Supervisor.class);

//	public String generateSupervisorId() {
//		return StormUtils.uuid().toString();// util.clj
//	}

	/**
	 * 启动一个supervisor
	 * 
	 * @param conf： 配置信息，default.yaml storm.yaml
	 * @param sharedContext： 目前调用时参数为 null
	 * @return SupervisorManger: 可关闭supervisor，和所有的worker     
	 */
	public SupervisorManger mkSupervisor(Map conf, ZMQContext sharedContext)
			throws Exception {

		LOG.info("Starting Supervisor with conf " + conf);
		/**
		 * Step 1: cleanup all files in supervisor_tmp_dir
		 */
		//清空/storm-local-dir/supervisor/tmp 下的所有文件
		String path = StormConfig.supervisorTmpDir(conf);
		FileUtils.cleanDirectory(new File(path));

		AtomicBoolean active = new AtomicBoolean(true);

		int startTimeStamp = TimeUtils.current_time_secs();
		
		//Step 2, get hostname 
		//获得主机名
		String myHostName = NetWorkUtils.local_hostname();

		ConcurrentHashMap<String, String> workerThreadPids = new ConcurrentHashMap<String, String>();

		/*
		 * Step 3: mk_storm_cluster_state register supervisor stats in zookeeper
		 * 3.1 connection zk 
		 * 3.2 register watcher of zk 
		 * 3.3 register all kinds of callbacks 
		 * 3.4 create znode
		 */
		//可操作zookeeper的实例
		StormClusterState stormClusterState = Cluster
				.mk_storm_cluster_state(conf);

		/*
		 * Step 4, create LocalStat LocalStat is one KV database 
		 * 4.1 create LocalState instance 
		 * 4.2 get or put supervisorId
		 */
		//将supervisorid写入本地
		LocalState localState = StormConfig.supervisorState(conf);

		String supervisorId = (String) localState.get(Common.LS_ID);
		if (supervisorId == null) {
			supervisorId = UUID.randomUUID().toString();
			localState.put(Common.LS_ID, supervisorId);
		}

		/*
		 * Step 5, create event manager EventManger create one thread to handle
		 * event get one event from queue, then execute it
		 */
		/*
		 * EventManager 创建对象的时候，会启动一个单独的线程会循环从事件队列中取出一个时间，并执行；
		 * eventManager处理SynchronizeSupervisor，processesEventManager处理SyncProcesses。
		*/
		
		EventManager eventManager = new EventManagerImp(false);
		EventManager processesEventManager = new EventManagerImp(false);

		//Step 6, create syncProcess
		 
		//SyncProcesses：关闭心跳异常(不存在，超时)的worker；找出需要启动的worker，启动。
		SyncProcesses syncProcesses = new SyncProcesses(supervisorId, conf,
				localState, workerThreadPids, sharedContext);

		
		//Step 7, create synchronizeSupervisor

		/*    SynchronizeSupervisor
		 *    (1) 下载要下载的代码、jar包和配置文件；
                 *    (2) 删除那些没有被nimbus分配任务的topology资源；
                 *    (3) 将nimbus分配到该supervisor的任务写入到/storm-local-dir/supervisor/localstate中，
                 *        LocalState可以看作是一个KV database;
                 *    (4) 将syncProcesses添加到对应的事件队列中。
		 */
		SynchronizeSupervisor synchronizeSupervisor = new SynchronizeSupervisor(
				supervisorId, conf, eventManager, processesEventManager,
				stormClusterState, localState, syncProcesses);

		
		//Step 8: create HeartBeat
		
		//向zk中写入一次supervisor的心跳信息
		Heartbeat hb = new Heartbeat(conf, stormClusterState, supervisorId,
				myHostName, startTimeStamp);
		hb.run();

		//Step 9: start the threads

		Vector<SmartThread> threads = new Vector<SmartThread>();

		
		//Step 9.1 start heartbeat thread
		
		//启动一个线程，每隔supervisor.heartbeat.frequency.secs写入一次supervisor心跳信息
		AsyncHeartbeat asyncHB = new AsyncHeartbeat(conf, hb, active);
		AsyncLoopThread heartbeat = new AsyncLoopThread(asyncHB, false, null,
				Thread.MAX_PRIORITY, true);
		threads.add(heartbeat);
		
		//Step 9.2 start sync Supervisor thread

		//启动一个线程，每隔10妙执行SynchronizeSupervisor
		AsyncSynchronizeSupervisor syncSupervisor = new AsyncSynchronizeSupervisor(
				eventManager, synchronizeSupervisor, active);
		AsyncLoopThread syncsThread = new AsyncLoopThread(syncSupervisor);
		threads.add(syncsThread);


		// Step 9.3 start sync process thread

		//启动一个线程，每隔supervisor.monitor.frequency.secs妙执行SyncProcesses
		AsyncSyncProcesses syncProcess = new AsyncSyncProcesses(conf,
				processesEventManager, syncProcesses, active);
		AsyncLoopThread syncpThread = new AsyncLoopThread(syncProcess, false,
				null, Thread.MAX_PRIORITY, true);
		threads.add(syncpThread);

		LOG.info("Starting supervisor with id " + supervisorId + " at host "
				+ myHostName);

		//SupervisorManger： 可关闭supervisor，和所有的worker
		return new SupervisorManger(conf, supervisorId, active, threads,
				processesEventManager, eventManager, stormClusterState,
				workerThreadPids);
	}

	/**
	 * 关闭supervisor 
	 * @param supervisor
	 */
	public void killSupervisor(SupervisorManger supervisor) {
		supervisor.shutdown();
	}

	/**
	 * 启动supervisor
	 */
	public void run() {

		SupervisorManger supervisorManager = null;
		try {
			Map conf = Utils.readStormConfig();

			Common.validate_distribute_mode(conf);

			supervisorManager = mkSupervisor(conf, null);

		} catch (Exception e) {
			LOG.error("Failed to start supervisor\n", e);
			System.exit(1);
		}

		try {
                        
			Thread.currentThread().join();

		} catch (InterruptedException e) {
			LOG.info("Begin to shutdown supervisor");

			supervisorManager.ShutdownAllWorkers();

			supervisorManager.shutdown();

			LOG.info("Successfully shutdown supervisor");

		}

	}

	/**
	 * supervisor daemon enter entrance
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		Supervisor instance = new Supervisor();

		instance.run();

	}

}