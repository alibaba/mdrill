package com.alipay.bluewhale.core.daemon.supervisor;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.thrift7.TException;

import backtype.storm.utils.LocalState;
import backtype.storm.utils.Utils;

import com.alipay.bluewhale.core.callback.RunnableCallback;
import com.alipay.bluewhale.core.cluster.Common;
import com.alipay.bluewhale.core.cluster.StormClusterState;
import com.alipay.bluewhale.core.cluster.StormConfig;
import com.alipay.bluewhale.core.daemon.NodePort;
import com.alipay.bluewhale.core.event.EventManager;
import com.alipay.bluewhale.core.task.LocalAssignment;
import com.alipay.bluewhale.core.task.common.Assignment;
import com.alipay.bluewhale.core.utils.PathUtils;
import com.alipay.bluewhale.core.utils.StormUtils;

/**
 * supervisor的SynchronizeSupervisor操作
 * (1) 下载要下载的代码、jar包和配置文件；
 * (2) 删除那些没有被nimbus分配任务的topology资源；
 * (3) 将nimbus分配到该supervisor的任务写入到/storm-local-dir/supervisor/localstate中，
 *     LocalState可以看作是一个KV database;
 * (4) 将syncProcesses添加到对应的事件队列中
 */
class SynchronizeSupervisor extends RunnableCallback {

	private static final Logger LOG = Logger
			.getLogger(SynchronizeSupervisor.class);

	// private Supervisor supervisor;

	private String supervisorId;

	private EventManager eventManager;

	private EventManager processesEventManager;

	private StormClusterState stormClusterState;

	private LocalState localState;

	private Map conf;

	private SyncProcesses syncProcesses;

	/**
	 * @param conf
	 * @param eventManager
	 * @param processesEventManager
	 * @param stormClusterState
	 * @param supervisorId
	 * @param localState
	 * @param syncProcesses
	 */
	public SynchronizeSupervisor(String supervisorId, Map conf,
			EventManager eventManager, EventManager processesEventManager,
			StormClusterState stormClusterState, LocalState localState,
			SyncProcesses syncProcesses) {
		this.syncProcesses = syncProcesses;
		this.eventManager = eventManager;
		this.processesEventManager = processesEventManager;
		this.stormClusterState = stormClusterState;
		this.conf = conf;
		this.supervisorId = supervisorId;
		this.localState = localState;

	}

	@Override
	public void run() {
		/**
		 * Step 1: create SyncCallback
		 */
		RunnableCallback syncCallback = new SyncCallback(this, eventManager);

		/**
		 * Step 2: create storm-code map stormid to :master-code-dir
		 */
		Map<String, String> stormCodeMap = readStormCodeLocations(
				stormClusterState, syncCallback);
		//Set<String> assignedStormIds = null;
		List<String> downloadedTopologyIds = null;
		if (stormCodeMap != null) {

		     //assignedStormIds = stormCodeMap.keySet();

			/**
			 * Step 3: get topologyIds from
			 * STORM-LOCAL-DIR/supervisor/stormdist/
			 */
			try {
				downloadedTopologyIds = readDownloadedTopologyIds(conf);
			} catch (IOException e) {
				String errMsg = "Failed to get downloaded topologyids\n";
				LOG.error(errMsg, e);
			}

			/**
			 * Step 4: get <port,LocalAssignments> from ZK local node's
			 * assignment
			 */
			Map<Integer, LocalAssignment> newAssignment = readAssignments(
					stormClusterState, supervisorId, syncCallback);

			LOG.debug("Synchronizing supervisor");
			LOG.debug("Storm code map: " + stormCodeMap);
			LOG.debug("Downloaded storm ids: " + downloadedTopologyIds);
			LOG.debug("New assignment: " + newAssignment);

			// Step 5: download code from ZK

			Set<Entry<String, String>> entryStormCodeSet = stormCodeMap
					.entrySet();
			for (Entry<String, String> entry : entryStormCodeSet) {

				String stormId = entry.getKey();
				String masterCodeDir = entry.getValue();

				if (!downloadedTopologyIds.contains(stormId)) {

					LOG.info("Downloading code for storm id " + stormId
							+ " from " + masterCodeDir);

					try {
						downloadStormCode(conf, stormId, masterCodeDir);
					} catch (IOException e) {
						LOG.error(e + " downloadStormCode failed " + "stormId:"
								+ stormId + "masterCodeDir:" + masterCodeDir);

					} catch (TException e) {
						LOG.error(e + " downloadStormCode failed " + "stormId:"
								+ stormId + "masterCodeDir:" + masterCodeDir);
					}
					LOG.info("Finished downloading code for storm id "
							+ stormId + " from " + masterCodeDir);
				}

			}

			/**
			 * Step 5: remove any downloaded code that's no longer assigned or
			 * active
			 */
			for (Iterator<String> it = downloadedTopologyIds.iterator(); it.hasNext();) {
				String topologyId = it.next();

				if (!stormCodeMap.containsKey(topologyId)) {

					LOG.info("Removing code for storm id " + topologyId);

					String path = null;
					try {
						path = StormConfig.supervisor_stormdist_root(conf,
								topologyId);
						PathUtils.rmr(path);
					} catch (IOException e) {
						String errMsg = "rmr the path:" + path + "failed\n";
						LOG.error(errMsg, e);
					}
				}
			}

			LOG.debug("Writing new assignment " + newAssignment);
			try {
				localState.put(Common.LS_LOCAL_ASSIGNMENTS, newAssignment);
			} catch (IOException e) {
				LOG.error("put LS_LOCAL_ASSIGNMENTS " + newAssignment
						+ " of localState failed");
			}
		}

		processesEventManager.add(syncProcesses);

	}

	/**
	 * download code ; two cluster mode: local and distributed
	 * 
	 * @param conf
	 * @param stormId
	 * @param masterCodeDir
	 * @param clusterMode
	 * @throws IOException
	 */
	private void downloadStormCode(Map conf, String stormId,
			String masterCodeDir) throws IOException, TException {
		String clusterMode = StormConfig.cluster_mode(conf);

		if (clusterMode.endsWith("local")) {
			downloadLocalStormCode(conf, stormId, masterCodeDir);

		} else if (clusterMode.endsWith("distributed")) {
			downloadDistributeStormCode(conf, stormId, masterCodeDir);
		}
	}

	private void downloadLocalStormCode(Map conf, String stormId,
			String masterCodeDir) throws IOException, TException {

		// STORM-LOCAL-DIR/supervisor/stormdist/storm-id
		String stormroot = StormConfig.supervisor_stormdist_root(conf, stormId);

		FileUtils.copyDirectory(new File(masterCodeDir), new File(stormroot));

		ClassLoader classloader = Thread.currentThread()
				.getContextClassLoader();

		String resourcesJar = resourcesJar();

		URL url = classloader.getResource(StormConfig.RESOURCES_SUBDIR);

		String targetDir = stormroot + '/' + StormConfig.RESOURCES_SUBDIR;

		if (resourcesJar != null) {

			LOG.info("Extracting resources from jar at " + resourcesJar
					+ " to " + targetDir);

//			StormUtils.extract_dir_from_jar(resourcesJar,
//					StormConfig.RESOURCES_SUBDIR, stormroot);// extract dir
			// from jar;;
			// util.clj
		} else if (url != null) {

			LOG.info("Copying resources at " + url.toString() + " to "
					+ targetDir);

			FileUtils.copyDirectory(new File(url.getFile()), (new File(
					targetDir)));

		}
	}

	private void downloadDistributeStormCode(Map conf, String stormId,
			String masterCodeDir) throws IOException, TException {

		// STORM_LOCAL_DIR/supervisor/tmp/(UUID)
		String tmproot = StormConfig.supervisorTmpDir(conf) + "/"
				+ UUID.randomUUID().toString();
		FileUtils.forceMkdir(new File(tmproot));

		// STORM_LOCAL_DIR/supervisor/stormdist/stormId
		String stormroot = StormConfig.supervisor_stormdist_root(conf, stormId);

		// masterCodeDir/stormjar.jar
		String masterStormjarPath = StormConfig
				.masterStormjarPath(masterCodeDir);
		// tmproot/stormjar.jar
		String localFileJarTmp = StormConfig.supervisor_stormjar_path(tmproot);
		// load stormjar.jar
		Utils.downloadFromMaster(conf, masterStormjarPath, localFileJarTmp);// load
		// storm.jar

		// masterCodeDir/stormcode.ser
		String masterStormcodePath = StormConfig
				.masterStormcodePath(masterCodeDir);
		// tmproot/stormcode.ser
		String localFileCodeTmp = StormConfig
				.supervisor_stormcode_path(tmproot);
		// load stormcode.ser
		Utils.downloadFromMaster(conf, masterStormcodePath, localFileCodeTmp);

		// masterCodeDir/stormconf.ser
		String masterStormConfPath = StormConfig
				.masterStormconfPath(masterCodeDir);
		// tmproot/stormconf.ser
		String localFileConfTmp = StormConfig
				.supervisor_sotrmconf_path(tmproot);
		// load conf
		Utils.downloadFromMaster(conf, masterStormConfPath, localFileConfTmp);

		// extract dir from jar
//		StormUtils.extract_dir_from_jar(localFileJarTmp,
//				StormConfig.RESOURCES_SUBDIR, tmproot);

		FileUtils.moveDirectory(new File(tmproot), new File(stormroot));

	}

	private String resourcesJar() {

		String path = System.getProperty("java.class.path");
		if (path == null) {
			return null;
		}

		String[] paths = path.split(File.pathSeparator);

		List<String> jarPaths = new ArrayList<String>();
		for (String s : paths) {
			if (s.endsWith(".jar")) {
				jarPaths.add(s);
			}
		}

		/**
		 * FIXME, this place seems exist problem
		 */
		List<String> rtn = new ArrayList<String>();
		int size = jarPaths.size();
		for (int i = 0; i < size; i++) {
			if (StormUtils.zipContainsDir(jarPaths.get(i),
					StormConfig.RESOURCES_SUBDIR)) {
				rtn.add(jarPaths.get(i));
			}
		}

		if (rtn.size() == 0)
			return null;

		return rtn.get(0);
	}

	/**
	 * get mastercodedir for every storm(topology)
	 * 
	 * @param stormClusterState
	 * @param callback
	 * @returns Map: <stormid, master-code-dir> from zookeeper
	 */
	private Map<String, String> readStormCodeLocations(
			StormClusterState stormClusterState, RunnableCallback callback) {

		Map<String, String> rtn = null;

		/**
		 * set callback to StormZkClusterState.assignments_callback return zk's
		 * /assignments children get all storm-ids
		 */
		List<String> topologyids = stormClusterState.assignments(callback);

		if (topologyids != null) {
			rtn = new HashMap<String, String>();
			for (String topologyid : topologyids) {
				Assignment assignmenInfo = stormClusterState.assignment_info(
						topologyid, callback);

				rtn.put(topologyid, assignmenInfo.getMasterCodeDir());
			}
		}
		return rtn;
	}

	/**
	 * a port must be assigned one topology
	 * 
	 * @param stormClusterState
	 * @param supervisorId
	 * @param callback
	 * @returns map: {port,LocalAssignment}
	 */
	private Map<Integer, LocalAssignment> readAssignments(
			StormClusterState stormClusterState, String supervisorId,
			RunnableCallback callback) {

		Map<Integer, LocalAssignment> portLA = null;

		/**
		 * set callback to StormZkClusterState.assignments_callback return zk's
		 * /assignments children get all storm-ids
		 */
		List<String> topologyIds = stormClusterState.assignments(callback);
		if (topologyIds != null) {
			portLA = new HashMap<Integer, LocalAssignment>();
			for (String topologyId : topologyIds) {
				// FIXME for循环多次与zk交互，是否可以一次
				// get local node's <port, LocalAssignment> from ZK
				Map<Integer, LocalAssignment> portTasks = readMyTasks(
						stormClusterState, topologyId, supervisorId, callback);
				if (portTasks == null) {
					continue;
				}
				// a port must be assigned one storm
				Set<Entry<Integer, LocalAssignment>> entrySet = portTasks
						.entrySet();
				for (Entry<Integer, LocalAssignment> entry : entrySet) {

					Integer port = entry.getKey();

					LocalAssignment la = entry.getValue();

					if (!portLA.containsKey(port)) {
						portLA.put(port, la);
					} else {
						throw new RuntimeException(
								"Should not have multiple topologys assigned to one port");
					}
				}
			}
		}
		return portLA;
	}

	/**
	 * get topologyids form supervisor local dir
	 * 
	 * @param conf
	 * @throws IOException
	 * @returns Set<String>: stormids
	 */

	@SuppressWarnings("rawtypes")
	private List<String> readDownloadedTopologyIds(Map conf) throws IOException {

		// get the path: STORM-LOCAL-DIR/supervisor/stormdist/
		String path = StormConfig.supervisor_stormdist_root(conf);

		List<String> topologyids = PathUtils.read_dir_contents(path);

		return topologyids;
	}

	/**
	 * read assignment_info from zk , and get local node's tasks
	 * 
	 * @param stormClusterState
	 * @param topologyId
	 * @param supervisorId
	 * @param callback
	 * @return Map: {port, LocalAssignment}
	 */
	private Map<Integer, LocalAssignment> readMyTasks(
			StormClusterState stormClusterState, String topologyId,
			String supervisorId, RunnableCallback callback) {

		Map<Integer, LocalAssignment> portTasks = null;

		Assignment assignmenInfo = stormClusterState.assignment_info(
				topologyId, callback);

		if (assignmenInfo != null) {
			Map<Integer, NodePort> taskToNodeport = assignmenInfo
					.getTaskToNodeport();

			if (taskToNodeport != null) {

				portTasks = new HashMap<Integer, LocalAssignment>();

				Set<Entry<Integer, NodePort>> entrySet = taskToNodeport
						.entrySet();

				for (Iterator<Entry<Integer, NodePort>> it = entrySet
						.iterator(); it.hasNext();) {

					Entry<Integer, NodePort> entry = it.next();

					Integer taskId = entry.getKey();

					NodePort nodePort = entry.getValue();

					int port = nodePort.getPort();

					String node = nodePort.getNode();

					if (!node.equals(supervisorId)) {
						// not localhost
						continue;
					}

					if (portTasks.containsKey(port)) {

						LocalAssignment la = portTasks.get(port);

						Set<Integer> taskIds = la.getTaskIds();

						taskIds.add(taskId);

					} else {

						Set<Integer> taskIds = new HashSet<Integer>();

						taskIds.add(taskId);

						LocalAssignment la = new LocalAssignment(topologyId,
								taskIds);

						portTasks.put(port, la);
					}
				}
			}

		}

		return portTasks;
	}
}
