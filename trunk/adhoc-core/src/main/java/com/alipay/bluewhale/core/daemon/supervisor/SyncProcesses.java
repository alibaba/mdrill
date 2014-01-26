package com.alipay.bluewhale.core.daemon.supervisor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import backtype.storm.Config;
import backtype.storm.utils.LocalState;
import backtype.storm.utils.Time;

import com.alipay.bluewhale.core.cluster.Common;
import com.alipay.bluewhale.core.cluster.StormConfig;
import com.alipay.bluewhale.core.daemon.State;
import com.alipay.bluewhale.core.messaging.ZMQContext;
import com.alipay.bluewhale.core.task.LocalAssignment;
import com.alipay.bluewhale.core.utils.PathUtils;
import com.alipay.bluewhale.core.utils.ProcessSimulator;
import com.alipay.bluewhale.core.utils.StormUtils;
import com.alipay.bluewhale.core.utils.TimeUtils;
import com.alipay.bluewhale.core.work.Worker;
import com.alipay.bluewhale.core.work.WorkerShutdown;
import com.alipay.bluewhale.core.work.refresh.WorkerHeartbeat;

/**
 *  SyncProcesses
 *  (1) 关闭心跳异常(不存在，超时)的worker；
 *  (2) 找出需要启动的worker，启动。
 */
class SyncProcesses extends ShutdownWork {
	private static Logger LOG = Logger.getLogger(SyncProcesses.class);

	private LocalState localState;

	private Map conf;

	private ConcurrentHashMap<String, String> workerThreadPids;

	private String supervisorId;

	private ZMQContext sharedContext;

	// private Supervisor supervisor;

	/**
	 * @param conf
	 * @param localState
	 * @param workerThreadPids
	 * @param supervisorId
	 * @param sharedContext
	 * @param workerThreadPidsReadLock
	 * @param workerThreadPidsWriteLock
	 */
	public SyncProcesses(String supervisorId, Map conf, LocalState localState,
			ConcurrentHashMap<String, String> workerThreadPids,
			ZMQContext sharedContext) {

		this.supervisorId = supervisorId;

		this.conf = conf;

		this.localState = localState;

		this.workerThreadPids = workerThreadPids;

		this.sharedContext = sharedContext;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		/**
		 * Step 1: get assigned tasks from localstat Map<port(type Integer),
		 * LocalAssignment>
		 */
		Map<Integer, LocalAssignment> assignedTasks = null;
		try {
			assignedTasks = (Map<Integer, LocalAssignment>) localState
					.get(Common.LS_LOCAL_ASSIGNMENTS);
		} catch (IOException e) {
			LOG.error(
					"Failed to get Common.LS_LOCAL_ASSIGNMENTS from localState\n",
					e);
		}
		if (assignedTasks == null) {
			assignedTasks = new HashMap<Integer, LocalAssignment>();
		}

		/**
		 * Step 2: get allocated tasks from local_dir/worker/ids/heartbeat
		 * Map<workerid [WorkerHeartbeat, state]>
		 */
		Map<String, StateHeartbeat> allocated = null;
		try {
			allocated = readAllocatedworkers(conf, localState, assignedTasks);
		} catch (IOException e2) {
			LOG.error("readAllocatedworkers" + allocated + " failed");
		}

		/**
		 * Step 3: get which one should be keep and get keeper ports
		 */
		Map<String, StateHeartbeat> keepers = null;
		Set<Integer> keepPorts = null;
		if (allocated != null) {
			keepers = new HashMap<String, StateHeartbeat>();
			keepPorts = new HashSet<Integer>();

			Set<Entry<String, StateHeartbeat>> allocatedSet = allocated
					.entrySet();
			for (Iterator<Entry<String, StateHeartbeat>> it = allocatedSet
					.iterator(); it.hasNext();) {

				Entry<String, StateHeartbeat> entry = it.next();

				String workerid = entry.getKey();
				StateHeartbeat hbstate = entry.getValue();

				if (hbstate.getState().equals(State.valid)) {
					keepers.put(workerid, hbstate);
				}
				if (hbstate.getHeartbeat() != null) {
					keepPorts.add(hbstate.getHeartbeat().getPort());
				}
				// kill those in allocated that are dead or disallowed;
				if (hbstate.getState() != State.valid) {
					StringBuilder sb = new StringBuilder();
					sb.append("Shutting down and clearing state for id ");
					sb.append(workerid);
					sb.append(";State:");
					sb.append(hbstate.getState());
					sb.append(";Heartbeat");
					sb.append(hbstate.getHeartbeat());
					LOG.info(sb);

					try {
						shutWorker(conf, supervisorId, workerid,
								workerThreadPids);
					} catch (IOException e) {
						String errMsg = "Failed to shutdown worker workId:"
								+ workerid + ",supervisorId: " + supervisorId
								+ ",workerThreadPids:" + workerThreadPids;
						LOG.error(errMsg, e);
					}
				}
			}

		}

		/**
		 * Step 4: get reassigned tasks, which is in assignedTasks, but not in
		 * keeperPorts Map<port(type Integer), LocalAssignment>
		 */
		Map<Integer, LocalAssignment> reassignTasks = StormUtils
				.select_keys_pred(keepPorts, assignedTasks);

		/**
		 * Step 5: generate new work ids
		 */
		Map<Integer, String> newWorkerIds = null;
		if (reassignTasks != null) {

			newWorkerIds = new HashMap<Integer, String>();

			Set<Integer> reassignedTaskSet = reassignTasks.keySet();

			for (Integer port : reassignedTaskSet) {

				String newWorkerId = UUID.randomUUID().toString();

				newWorkerIds.put(port, newWorkerId);

				// create new worker Id directory
				// LOCALDIR/workers/newworkid/pids
				String path = StormConfig.worker_pids_root(conf, newWorkerId);
				try {
					PathUtils.local_mkdirs(path);
				} catch (IOException e) {
					LOG.error("Making dirs at " + path + " failed");
				}
			}
		}

		LOG.debug("Syncing processes");
		LOG.debug("Assigned tasks: " + assignedTasks);
		LOG.debug("Allocated: " + allocated);

		/**
		 * Step 6: update localstat's LS_APPROVED_WORKERS Create approvedWorkers
		 * Map<WorkerId, port>
		 */

		Map<String, Integer> lsApprovedWorkers = null;
		try {
			lsApprovedWorkers = (Map<String, Integer>) localState
					.get(Common.LS_APPROVED_WORKERS);

		} catch (IOException e) {
			LOG.error("get Common.LS_APPROVED_WORKERS of localState failed");
		}
		if (lsApprovedWorkers == null) {
			lsApprovedWorkers = new HashMap<String, Integer>();
		}

		Map<String, Integer> approvedWorkers = new HashMap<String, Integer>();

		if (keepers != null && lsApprovedWorkers != null) {
			Set<String> keepersKeySet = keepers.keySet();
			Set<Entry<String, Integer>> lsAWEntrySet = lsApprovedWorkers
					.entrySet();

			for (Iterator<Entry<String, Integer>> it = lsAWEntrySet.iterator(); it
					.hasNext();) {

				Entry<String, Integer> entry = it.next();

				String keepWorkerId = entry.getKey();

				if (keepersKeySet.contains(keepWorkerId)) {

					approvedWorkers.put(keepWorkerId, entry.getValue());
				}
			}
		}

		if (newWorkerIds != null) {
			Set<Entry<Integer, String>> newWorkerIdsEntrySet = newWorkerIds
					.entrySet();
			for (Entry<Integer, String> entry : newWorkerIdsEntrySet) {

				String workerId = entry.getValue();
				Integer port = entry.getKey();

				approvedWorkers.put(workerId, port);
			}
		}

		try {
			localState.put(Common.LS_APPROVED_WORKERS, approvedWorkers);
		} catch (IOException e1) {
			LOG.error("put Common.LS_APPROVED_WORKERS " + approvedWorkers
					+ " of localState failed");
		}

		/**
		 * Step 7: wait for worker launch
		 */
		if (reassignTasks != null) {
			Set<Entry<Integer, LocalAssignment>> reassignTasksEntrySet = reassignTasks
					.entrySet();
			for (Entry<Integer, LocalAssignment> entry : reassignTasksEntrySet) {

				Integer port = entry.getKey();
				LocalAssignment assignment = entry.getValue();

				String workerId = newWorkerIds.get(port);

				StringBuilder sb = new StringBuilder();
				sb.append("Launching worker with assiangment ");
				sb.append(assignment.toString());
				sb.append(" for the supervisor ");
				sb.append(supervisorId);
				sb.append(" on port ");
				sb.append(port);
				sb.append(" with id ");
				sb.append(workerId);
				LOG.info(sb);

				try {
					String clusterMode = StormConfig.cluster_mode(conf);

					if (clusterMode.equals("distributed")) {
						launchWorker(conf, sharedContext,
								assignment.getTopologyId(), supervisorId, port,
								workerId);
					} else if (clusterMode.equals("local")) {
						// in fact, this is no use
						launchWorker(conf, sharedContext,
								assignment.getTopologyId(), supervisorId, port,
								workerId, workerThreadPids);
					}
				} catch (Exception e) {
					String errorMsg = "Failed to launchWorker workerId:"
							+ workerId + ":" + port;
					LOG.error(errorMsg, e);
				}
			}
		}

		/**
		 * FIXME, workerIds should be Set, not Collection, but here simplify the
		 * logic
		 */
		Collection<String> workerIds = newWorkerIds.values();
		try {
			waitForWorkersLaunch(conf, workerIds);
		} catch (IOException e) {
			LOG.error(e + " waitForWorkersLaunch failed");
		} catch (InterruptedException e) {
			LOG.error(e + " waitForWorkersLaunch failed");
		}

	}

	/**
	 * wait for all workers of the supervisor launch
	 * 
	 * @param conf
	 * @param workerIds
	 * @throws InterruptedException
	 * @throws IOException
	 * @pdOid 52b11418-7474-446d-bff5-0ecd68f4954f
	 */
	public void waitForWorkersLaunch(Map conf, Collection<String> workerIds)
			throws IOException, InterruptedException {

		int startTime = TimeUtils.current_time_secs();

		for (Iterator<String> iter = workerIds.iterator(); iter.hasNext();) {
			String workerId = iter.next();

			waitForWorkerLaunch(conf, workerId, startTime);
		}
	}

	/**
	 * wait for worker launch if the time is not > *
	 * SUPERVISOR_WORKER_START_TIMEOUT_SECS, otherwise info failed
	 * 
	 * @param conf
	 * @param workerId
	 * @param startTime
	 * @throws IOException
	 * @throws InterruptedException
	 * @pdOid f0a6ab43-8cd3-44e1-8fd3-015a2ec51c6a
	 */
	public void waitForWorkerLaunch(Map conf, String workerId, int startTime)
			throws IOException, InterruptedException {

		LocalState ls = StormConfig.worker_state(conf, workerId);

		while (true) {

			WorkerHeartbeat whb = (WorkerHeartbeat) ls
					.get(Common.LS_WORKER_HEARTBEAT);
			if (whb == null
					&& ((TimeUtils.current_time_secs() - startTime) < (Integer) conf
							.get(Config.SUPERVISOR_WORKER_START_TIMEOUT_SECS))) {
				LOG.info(workerId + "still hasn't started");
				Time.sleep(500);
			} else {
				// whb is valid or timeout
				break;
			}
		}

		WorkerHeartbeat whb = (WorkerHeartbeat) ls
				.get(Common.LS_WORKER_HEARTBEAT);
		if (whb == null) {
			LOG.info("Worker " + workerId + "failed to start");
		}
	}

	/**
	 * get localstat approved workerId's map
	 * 
	 * @return Map<workerid [workerheart, state]> [workerheart, state] is also a
	 *         map, key is "workheartbeat" and "state"
	 * @param conf
	 * @param localState
	 * @param assignedTasks
	 * @throws IOException
	 * @pdOid 11c9bebb-d082-4c51-b323-dd3d5522a649
	 */
	@SuppressWarnings("unchecked")
	public Map<String, StateHeartbeat> readAllocatedworkers(Map conf,
			LocalState localState, Map<Integer, LocalAssignment> assignedTasks)
			throws IOException {

		Map<String, StateHeartbeat> workeridHbstate = null;

		int now = TimeUtils.current_time_secs();

		/**
		 * Get approved workerIds from local_dir/supervisor/localstat
		 * Map<WorkerId, port>
		 */
		Map<String, Integer> approvedIds = (Map<String, Integer>) localState
				.get(Common.LS_APPROVED_WORKERS);

		/**
		 * Get Map<workerId, WorkerHeartbeat> from
		 * local_dir/worker/ids/heartbeat
		 */
		Map<String, WorkerHeartbeat> idToHeartbeat = readWorkerHeartbeats(conf);

		if (idToHeartbeat != null) {
			workeridHbstate = new HashMap<String, StateHeartbeat>();
			Set<Map.Entry<String, WorkerHeartbeat>> entrySet = idToHeartbeat
					.entrySet();
			for (Iterator<Map.Entry<String, WorkerHeartbeat>> it = entrySet
					.iterator(); it.hasNext();) {

				Map.Entry<String, WorkerHeartbeat> entry = it.next();

				String workerid = entry.getKey().toString();

				WorkerHeartbeat whb = entry.getValue();

				State state = null;

				if (whb == null) {

					state = State.notStarted;

				} else if (approvedIds == null
						|| approvedIds.containsKey(workerid) == false
						|| matchesAssignment(whb, assignedTasks) == false) {

					// workerId isn't approved or
					// isn't assigned task
					state = State.disallowed;

				} else if ((now - whb.getTimeSecs()) > (Integer) conf
						.get(Config.SUPERVISOR_WORKER_TIMEOUT_SECS)) {//

					state = State.timedOut;
				} else {
					state = State.valid;
				}

				LOG.debug("Worker:" + workerid + " state:" + state
						+ " WorkerHeartbeat: " + whb
						+ " at supervisor time-secs " + now);

				workeridHbstate.put(workerid, new StateHeartbeat(state, whb));
			}
		}

		return workeridHbstate;
	}

	/**
	 * check whether the workerheartbeat is allowed in the assignedTasks
	 * 
	 * @param whb
	 *            : WorkerHeartbeat
	 * @param assignedTasks
	 * @return boolean if true, the assignments(LS-LOCAL-ASSIGNMENTS) is match
	 *         with workerheart if fasle, is not matched
	 */
	public boolean matchesAssignment(WorkerHeartbeat whb,
			Map<Integer, LocalAssignment> assignedTasks) {

		boolean isMatch = true;
		LocalAssignment localAssignment = assignedTasks.get(whb.getPort());

		if (localAssignment == null) {
			isMatch = false;
		} else if (!whb.getTopologyId().equals(localAssignment.getTopologyId())) {
			// topology id not equal
			LOG.info("topology id not equal whb=" + whb.getTopologyId()
					+ ",localAssignment=" + localAssignment.getTopologyId());
			isMatch = false;
		} else if (!(whb.getTaskIds().equals(localAssignment.getTaskIds()))) {
			// task-id isn't equal
			LOG.info("task-id isn't equal whb=" + whb.getTaskIds()
					+ ",localAssignment=" + localAssignment.getTaskIds());
			isMatch = false;
		}

		return isMatch;
	}

	/**
	 * get all workers heartbeats of the supervisor
	 * 
	 * @param conf
	 * @return Map<workerId, WorkerHeartbeat>
	 * @throws IOException
	 * @throws IOException
	 */
	public Map<String, WorkerHeartbeat> readWorkerHeartbeats(Map conf)
			throws IOException {

		Map<String, WorkerHeartbeat> workerHeartbeats = null;

		// get the path: STORM-LOCAL-DIR/workers
		String path = StormConfig.worker_root(conf);

		List<String> workerIds = PathUtils.read_dir_contents(path);

		if (workerIds != null) {
			workerHeartbeats = new HashMap<String, WorkerHeartbeat>();

			for (String workerId : workerIds) {

				WorkerHeartbeat whb = readWorkerHeartbeat(conf, workerId);
				// this place whb can be null
				workerHeartbeats.put(workerId, whb);
			}
		}
		return workerHeartbeats;
	}

	/**
	 * get worker heartbeat by workerid
	 * 
	 * @param conf
	 * @param workerId
	 * @returns WorkerHeartbeat
	 * @throws IOException
	 */
	public WorkerHeartbeat readWorkerHeartbeat(Map conf, String workerId)
			throws IOException {

		LocalState ls = StormConfig.worker_state(conf, workerId);

		return (WorkerHeartbeat) ls.get(Common.LS_WORKER_HEARTBEAT);
	}

	/**
	 * launch a worker in local mode
	 * 
	 * @param conf
	 * @param sharedcontext
	 * @param stormId
	 * @param supervisorId
	 * @param port
	 * @param workerId
	 * @param workerThreadPidsAtom
	 * @param workerThreadPidsAtomWriteLock
	 * @pdOid 405f44c7-bc1b-4e16-85cc-b59352b6ff5d
	 */
	@Deprecated
	public void launchWorker(Map conf, ZMQContext sharedcontext,
			String stormId, String supervisorId, Integer port, String workerId,
			ConcurrentHashMap<String, String> workerThreadPidsAtom)
			throws Exception {

		String pid = UUID.randomUUID().toString();

		WorkerShutdown worker = Worker.mk_worker(conf, sharedcontext, stormId,
				supervisorId, port, workerId);

		ProcessSimulator.registerProcess(pid, worker);

		workerThreadPidsAtom.put(workerId, pid);

	}

	/**
	 * launch a worker in distributed mode
	 * 
	 * @param conf
	 * @param sharedcontext
	 * @param topologyId
	 * @param supervisorId
	 * @param port
	 * @param workerId
	 * @throws IOException
	 * @pdOid 6ea369dd-5ce2-4212-864b-1f8b2ed94abb
	 */
	public void launchWorker(Map conf, ZMQContext sharedcontext,
			String topologyId, String supervisorId, Integer port,
			String workerId) throws IOException {

		// STORM-LOCAL-DIR/supervisor/stormdist/topologyId
		String stormroot = StormConfig.supervisor_stormdist_root(conf,
				topologyId);

		// STORM-LOCAL-DIR/supervisor/stormdist/topologyId/stormjar.jar
		String stormjar = StormConfig.supervisor_stormjar_path(stormroot);

		// get supervisor conf
		Map stormConf = StormConfig
				.read_supervisor_storm_conf(conf, topologyId);

		// get classpath
		// String[] param = new String[1];
		// param[0] = stormjar;
		// String classpath = StormUtils.add_to_classpath(
		// StormUtils.current_classpath(), param);
		String[] classpath = (new String(StormUtils.current_classpath() + ":" + stormjar)).split(":");
		
		String execute=(String) stormConf.get("worker.classpath.exclude");
		
		ArrayList<String> finalclasspath=new ArrayList<String>();
		for(String s:classpath)
		{
			if(execute==null||!s.matches(execute))
			{
				finalclasspath.add(s);
			}
		}
		
		StringBuffer classpathBuffer=new StringBuffer();
		String joinchar="";
		for(String s:finalclasspath)
		{
			classpathBuffer.append(joinchar);
			classpathBuffer.append(s);
			joinchar=":";
		}
		// get child process parameter

		String childopts = "";
		
		if (conf.get(Config.WORKER_CHILDOPTS) != null) {
			childopts = ""+conf.get(Config.WORKER_CHILDOPTS);
		} 
		if (conf.get(Config.WORKER_CHILDOPTS+"."+port) != null) {
			childopts = ""+conf.get(Config.WORKER_CHILDOPTS+"."+port);
		}
		
		if (stormConf.get(Config.TOPOLOGY_WORKER_CHILDOPTS) != null) {
			childopts = " " + stormConf.get(Config.TOPOLOGY_WORKER_CHILDOPTS);
		}
		
		if (stormConf.get(Config.TOPOLOGY_WORKER_CHILDOPTS+"."+port) != null) {
			childopts = " " + stormConf.get(Config.TOPOLOGY_WORKER_CHILDOPTS+"."+port);
		}
		
		String stormhome = System.getProperty("storm.home");
		if (stormhome == null) {
			stormhome=".";
		}
		// TODO ???哪里出来的 %ID%
		childopts = childopts.replace("%ID%", port.toString());
		childopts = childopts.replaceAll("%port%", port.toString());
		childopts = childopts.replaceAll("%storm.home%", stormhome);

		String logFileName = "worker-" + port + ".log";

		StringBuilder commandSB = new StringBuilder();
		// FIXME 复杂化了，此处拼出字符串，执行又按照空格分拆。
		commandSB.append("java -server ");
		commandSB.append(childopts);

		commandSB.append(" -Djava.library.path=");
		commandSB.append((String) conf.get(Config.JAVA_LIBRARY_PATH));

		commandSB.append(" -Dlogfile.name=");
		commandSB.append(logFileName);

		
		commandSB.append(" -Dstorm.home=");
		commandSB.append(stormhome);

		commandSB.append(" -Dlog4j.configuration=storm.log.properties");

		commandSB.append(" -cp ");
		commandSB.append(classpathBuffer.toString());

		commandSB.append(" com.alipay.bluewhale.core.work.Worker ");
		commandSB.append(topologyId);

		commandSB.append(" ");
		commandSB.append(supervisorId);

		commandSB.append(" ");
		commandSB.append(port);

		commandSB.append(" ");
		commandSB.append(workerId);

		LOG.info("Launching worker with command: " + commandSB);

		Map<String, String> environment = new HashMap<String, String>();
		environment.put("LD_LIBRARY_PATH",
				(String) conf.get(Config.JAVA_LIBRARY_PATH));
		
		try{
		ArrayList<String> killlist=findByJavaPort.findProcess(port);
		if(killlist!=null)
		{
			
			for(int i=0;i<3;i++)
			{
				for(String pid:killlist)
				{
					StormUtils.ensure_process_killed(Integer.parseInt(pid));
				}
				Thread.sleep(300);
			}
		}
		
		}catch(Throwable e)
		{
			LOG.error("killlist",e);
		}
		

		StormUtils.launch_work_process(commandSB.toString(), environment);
	}

}
