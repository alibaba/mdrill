package com.alipay.bluewhale.core.work;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import backtype.storm.Config;
import backtype.storm.generated.Grouping;
import backtype.storm.task.TopologyContext;
import backtype.storm.utils.LocalState;

import com.alipay.bluewhale.core.cluster.Common;
import com.alipay.bluewhale.core.cluster.StormClusterState;
import com.alipay.bluewhale.core.cluster.StormConfig;
import com.alipay.bluewhale.core.daemon.NodePort;
import com.alipay.bluewhale.core.task.common.Assignment;
import com.alipay.bluewhale.core.utils.TimeUtils;
import com.alipay.bluewhale.core.work.context.SystemContextMake;
import com.alipay.bluewhale.core.work.refresh.WorkerHeartbeat;

/**
 * worker utils
 * 
 * @author yannian
 * 
 */
public class WorkCommon {
	private static Logger LOG = Logger.getLogger(WorkCommon.class);

	/**
	 * 是否使用zmq来分发消息
	 */
	@SuppressWarnings("rawtypes")
	public static boolean localModeZmq(Map conf) {
		String clusterMode = StormConfig.cluster_mode(conf);
		Boolean isDistribute = clusterMode.equals("distributed");
		String key = Config.STORM_LOCAL_MODE_ZMQ;
		if (isDistribute || conf.get(key).equals(Boolean.TRUE)) {
			return true;
		}
		return false;
	}

	/**
	 * 从zk中读取当前work分配的task
	 */
	public static java.util.Set<Integer> readWorkerTaskids(StormClusterState zkCluster, String topologyid,String supervisorId, int port) {
		Set<Integer> tasks = null;
		Assignment assignmentInfo = zkCluster.assignment_info(topologyid, null);
		if (assignmentInfo != null) {
			tasks = new HashSet<Integer>();
			Map<Integer, NodePort> taskToNodeport = assignmentInfo.getTaskToNodeport();
			for (Entry<Integer, NodePort> taskNode : taskToNodeport.entrySet()) {
				NodePort loc = taskNode.getValue();
				if (loc != null && loc.getNode().equals(supervisorId)&& loc.getPort() == port) {
					tasks.add(taskNode.getKey());
				}
			}
			LOG.info("readWorkerTaskids topologyid=" + topologyid + ",port="
					+ port + ",supervisorId=" + supervisorId + ",tasks.size="
					+ tasks.size() + "," + assignmentInfo.toString());

		}
		return tasks;
	}

	/**
	 * work的心跳，仅仅更新本地目录
	 * 
	 * @throws IOException
	 */

	@SuppressWarnings("rawtypes")
	public static void doHeartbeat(Map conf, String worker_id, int port,
			String storm_id, Set<Integer> task_ids) throws IOException {

		int currtime = TimeUtils.current_time_secs();
		WorkerHeartbeat hb = new WorkerHeartbeat(currtime, storm_id, task_ids,
				port);

		LOG.debug("Doing heartbeat:" + worker_id + ",port:" + port + ",hb"
				+ hb.toString());

		LocalState state = StormConfig.worker_state(conf, worker_id);
		state.put(Common.LS_WORKER_HEARTBEAT, hb);

	}

	/**
	 * 计算 每个taskid，产生的tupple有可能流向那些tasks，将来创建链接使用
	 * 
	 * @param tasks_component
	 * @param mk_topology_context
	 * @param task_ids
	 */
	public static Set<Integer> worker_outbound_tasks(
			HashMap<Integer, String> tasks_component,
			SystemContextMake mk_topology_context, Set<Integer> task_ids) {

		Set<Integer> rtn = null;
		if (task_ids != null) {
			rtn= new HashSet<Integer>();
			for (Integer taskid : task_ids) {
				TopologyContext context = mk_topology_context.make(taskid);
				if(context!=null){
					// <StreamId,<ComponentId,Grouping>>
					Map<String, Map<String, Grouping>> targets = context.getThisTargets();
					for (Map<String, Grouping> e : targets.values()) {
						for (String componentId : e.keySet()) {
							List<Integer> tasks= context.getComponentTasks(componentId);
							rtn.addAll(tasks);
						}
					}	
				}
			}	
		}
		return rtn;
	}
}
