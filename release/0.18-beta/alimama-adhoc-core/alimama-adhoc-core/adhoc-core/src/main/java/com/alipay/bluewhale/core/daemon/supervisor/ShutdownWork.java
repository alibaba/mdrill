package com.alipay.bluewhale.core.daemon.supervisor;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.alipay.bluewhale.core.callback.RunnableCallback;
import com.alipay.bluewhale.core.cluster.StormConfig;
import com.alipay.bluewhale.core.utils.PathUtils;
import com.alipay.bluewhale.core.utils.ProcessSimulator;
import com.alipay.bluewhale.core.utils.StormUtils;

public class ShutdownWork extends RunnableCallback{

	private static Logger LOG = Logger.getLogger(ShutdownWork.class);
	
	/**
	 * shutdown the spec worker of the supervisor. and clean the local dir of
	 * workers
	 * 
	 * 
	 * @param conf
	 * @param supervisorId
	 * @param workerId
	 * @param workerThreadPidsAtom
	 * @param workerThreadPidsAtomReadLock
	 */
	public void shutWorker(Map conf, String supervisorId, String workerId,
			ConcurrentHashMap<String, String> workerThreadPids)
			throws IOException {

		LOG.info("Begin to shut down " + supervisorId + ":" + workerId);

		// STORM-LOCAL-DIR/workers/workerId/pids
		String workerPidPath = StormConfig.worker_pids_root(conf, workerId);

		List<String> pids = PathUtils.read_dir_contents(workerPidPath);

		String threadPid = workerThreadPids.get(workerId);

		if (threadPid != null) {
			ProcessSimulator.killProcess(threadPid);
		}

		for (String pid : pids) {

			StormUtils.ensure_process_killed(Integer.parseInt(pid));
			PathUtils.rmpath(StormConfig.worker_pid_path(conf, workerId, pid));

		}

		tryCleanupWorker(conf, workerId);

		LOG.info("Successfully shut down " + supervisorId + ":" + workerId);
	}

	/**
	 * clean the directory , subdirectories of STORM-LOCAL-DIR/workers/workerId
	 * 
	 * 
	 * @param conf
	 * @param workerId
	 * @throws IOException
	 */
	public void tryCleanupWorker(Map conf, String workerId) throws IOException {
		try {
			// delete heartbeat dir LOCAL_DIR/workers/workid/heartbeats
			PathUtils.rmr(StormConfig.worker_heartbeats_root(conf, workerId));
			// delete pid dir, LOCAL_DIR/workers/workerid/pids
			PathUtils.rmpath(StormConfig.worker_pids_root(conf, workerId));
			// delete workerid dir, LOCAL_DIR/worker/workerid
			PathUtils.rmpath(StormConfig.worker_root(conf, workerId));
		} catch (RuntimeException e) {
			LOG.warn(e + "Failed to cleanup worker " + workerId
					+ ". Will retry later");
		}
	}
}
