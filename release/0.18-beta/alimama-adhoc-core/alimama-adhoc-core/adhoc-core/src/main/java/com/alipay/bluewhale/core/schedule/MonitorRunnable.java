package com.alipay.bluewhale.core.schedule;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.alipay.bluewhale.core.cluster.StormClusterState;
import com.alipay.bluewhale.core.cluster.StormConfig;
import com.alipay.bluewhale.core.daemon.NimbusData;
import com.alipay.bluewhale.core.daemon.StatusTransition;
import com.alipay.bluewhale.core.daemon.StatusType;
import com.alipay.bluewhale.core.daemon.supervisor.Supervisor;
import com.alipay.bluewhale.core.utils.PathUtils;
import com.esotericsoftware.minlog.Log;

public class MonitorRunnable implements Runnable {
	private static Logger LOG = Logger.getLogger(Supervisor.class);

	private NimbusData data;

	public MonitorRunnable(NimbusData data) {
		this.data = data;
	}

	@Override
	public void run() {
		StormClusterState clusterState = data.getStormClusterState();

		List<String> active_topologys = clusterState.active_storms();
		if (active_topologys != null){
		    for (String topologyid : active_topologys) {
			StatusTransition.transition(data, topologyid, false, StatusType.monitor);
		    }
		}


		// 线程安全操作，与submitTopology和transition状态时
		synchronized (data.getSubmitLock()) {
			Set<String> to_cleanup_ids = do_cleanup(clusterState,active_topologys);
			if (to_cleanup_ids != null && to_cleanup_ids.size() > 0) {
				for (String storm_id : to_cleanup_ids) {
				    LOG.info("Cleaning up " + storm_id);
					clusterState.teardown_heartbeats(storm_id);
					clusterState.teardown_task_errors(storm_id);
					// 获取目录 /nimbus/stormdist/topologyid
					String master_stormdist_root = StormConfig
							.masterStormdistRoot(data.getConf(), storm_id);
					try {
						// 强制删除topologyid对应的目录
						PathUtils.rmr(master_stormdist_root);
					} catch (IOException e) {
					    LOG.error("强制删除目录" + master_stormdist_root + "出错!", e);
					}
					// (swap! (:task-heartbeats-cache nimbus) dissoc id))
					data.getTaskHeartbeatsCache().remove(storm_id);

				}
			}
		}
	    
	}

	/**
	 * 获取需要清理的 storm id列表
	 * @param clusterState
	 * @return
	 */
	private Set<String> do_cleanup(StormClusterState clusterState,List<String> active_topologys) {
		
		List<String> heartbeat_ids =clusterState.heartbeat_storms();
		List<String> error_ids = clusterState.task_error_storms();

		String master_stormdist_root = StormConfig.masterStormdistRoot(data.getConf());
		// 获取/nimbus/stormdist路径下面文件名称集合(topology id集合)
		List<String> code_ids = PathUtils.read_dir_contents(master_stormdist_root);

//		Set<String> assigned_ids = StormUtils.listToSet(clusterState.active_storms());//这次差需
		Set<String> to_cleanup_ids = new HashSet<String>();
		if (heartbeat_ids != null){
		    to_cleanup_ids.addAll(heartbeat_ids);
		}
		if (error_ids != null){
		    to_cleanup_ids.addAll(error_ids);
		}
		if (code_ids != null){
		    to_cleanup_ids.addAll(code_ids);
		}
                if (active_topologys != null){
		    to_cleanup_ids.removeAll(active_topologys);
                }
		return to_cleanup_ids;
	}

}
