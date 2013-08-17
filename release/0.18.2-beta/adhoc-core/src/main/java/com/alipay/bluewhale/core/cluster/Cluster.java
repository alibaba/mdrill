package com.alipay.bluewhale.core.cluster;

import java.util.Map;

import backtype.storm.utils.Utils;

/**
 * storm与zk有关的路径的定义
 * 
 * @author yannian
 * 
 */
public class Cluster {

	public static String ASSIGNMENTS_ROOT = "assignments";
	public static String TASKS_ROOT = "tasks";
	public static String CODE_ROOT = "code";
	public static String STORMS_ROOT = "storms";
	public static String SUPERVISORS_ROOT = "supervisors";
	public static String HIGO_ROOT = "higo";
	public static String DRPCS_ROOT = "drpcs";

	public static String TASKBEATS_ROOT = "taskbeats";
	public static String TASKERRORS_ROOT = "taskerrors";

	public static String ASSIGNMENTS_SUBTREE;
	public static String TASKS_SUBTREE;
	public static String STORMS_SUBTREE;
	public static String SUPERVISORS_SUBTREE;
	public static String DRPCS_SUBTREE;
	
	public static String HIGO_SUBTREE;

	public static String TASKBEATS_SUBTREE;
	public static String TASKERRORS_SUBTREE;
	static {
		ASSIGNMENTS_SUBTREE = "/" + ASSIGNMENTS_ROOT;
		TASKS_SUBTREE = "/" + TASKS_ROOT;
		STORMS_SUBTREE = "/" + STORMS_ROOT;
		SUPERVISORS_SUBTREE = "/" + SUPERVISORS_ROOT;
		HIGO_SUBTREE = "/" + HIGO_ROOT;
		TASKBEATS_SUBTREE = "/" + TASKBEATS_ROOT;
		TASKERRORS_SUBTREE = "/" + TASKERRORS_ROOT;
		DRPCS_SUBTREE="/"+DRPCS_ROOT;
	}

	public static String drpc_path(String id) {
		return DRPCS_SUBTREE + "/" + id;
	}
	
	public static String supervisor_path(String id) {
		return SUPERVISORS_SUBTREE + "/" + id;
	}
	public static String higo_root()
	{
	    return HIGO_SUBTREE;
	}
	
	public static String higo_table(String id) {
		return HIGO_SUBTREE + "/" + id;
	}
	public static String higo_path(String tablename,Integer id) {
		return higo_table(tablename) + "/" + id;
	}

	public static String assignment_path(String id) {
		return ASSIGNMENTS_SUBTREE + "/" + id;
	}

	public static String storm_path(String id) {
		return STORMS_SUBTREE + "/" + id;
	}

	public static String storm_task_root(String storm_id) {
		return TASKS_SUBTREE + "/" + storm_id;
	}

	public static String task_path(String storm_id, int task_id) {
		return storm_task_root(storm_id) + "/" + task_id;
	}

	public static String taskbeat_storm_root(String storm_id) {
		return TASKBEATS_SUBTREE + "/" + storm_id;
	}

	public static String taskbeat_path(String storm_id, int task_id) {
		return taskbeat_storm_root(storm_id) + "/" + task_id;
	}

	public static String taskerror_storm_root(String storm_id) {
		return TASKERRORS_SUBTREE + "/" + storm_id;
	}

	public static String taskerror_path(String storm_id, int task_id) {
		return taskerror_storm_root(storm_id) + "/" + task_id;
	}

	public static Object maybe_deserialize(byte[] data) {
		if (data == null) {
			return null;
		}
		return Utils.deserialize(data);
	}

	@SuppressWarnings("rawtypes")
	public static StormClusterState mk_storm_cluster_state(
			Map cluster_state_spec) throws Exception {
		return new StormZkClusterState(cluster_state_spec);
	}

	public static StormClusterState mk_storm_cluster_state(
			ClusterState cluster_state_spec) throws Exception {
		return new StormZkClusterState(cluster_state_spec);
	}

	@SuppressWarnings("rawtypes")
	public static ClusterState mk_distributed_cluster_state(Map _conf)
			throws Exception {
		return new DistributedClusterState(_conf);
	}

}
