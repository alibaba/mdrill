package com.alipay.bluewhale.core.cluster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;
import org.apache.zookeeper.Watcher.Event.EventType;

import backtype.storm.utils.Utils;

import com.alipay.bluewhale.core.callback.RunnableCallback;
//import com.alipay.bluewhale.core.contrib.distributeSearch.drpc.DrpcInfo;
import com.alipay.bluewhale.core.daemon.supervisor.SupervisorInfo;
import com.alipay.bluewhale.core.task.common.Assignment;
import com.alipay.bluewhale.core.task.common.TaskInfo;
import com.alipay.bluewhale.core.task.error.TaskError;
import com.alipay.bluewhale.core.task.heartbeat.TaskHeartbeat;
import com.alipay.bluewhale.core.utils.PathUtils;
import com.alipay.bluewhale.core.utils.StormUtils;
import com.alipay.bluewhale.core.utils.TimeUtils;

public class StormZkClusterState implements StormClusterState {
	private static Logger LOG = Logger.getLogger(StormZkClusterState.class);

	private ClusterState cluster_state;

	private ConcurrentHashMap<String, RunnableCallback> assignment_info_callback;
	private AtomicReference<RunnableCallback> supervisors_callback;
	private AtomicReference<RunnableCallback> drpcs_callback;

	private AtomicReference<RunnableCallback> assignments_callback;
	private ConcurrentHashMap<String, RunnableCallback> storm_base_callback;
	private ConcurrentHashMap<String, RunnableCallback> higo_base_callback;

	private UUID state_id;

	private boolean solo;

	public StormZkClusterState(Object cluster_state_spec) throws Exception {

		if (cluster_state_spec instanceof ClusterState) {
			solo = false;
			cluster_state = (ClusterState) cluster_state_spec;
		} else {

			solo = true;
			cluster_state = new DistributedClusterState(
					(Map) cluster_state_spec);
		}

		assignment_info_callback = new ConcurrentHashMap<String, RunnableCallback>();
		supervisors_callback = new AtomicReference<RunnableCallback>(null);
		drpcs_callback = new AtomicReference<RunnableCallback>(null);

		assignments_callback = new AtomicReference<RunnableCallback>(null);
		storm_base_callback = new ConcurrentHashMap<String, RunnableCallback>();
		higo_base_callback= new ConcurrentHashMap<String, RunnableCallback>();

		state_id = cluster_state.register(new ClusterStateCallback() {

			@Override
			public <T> Object execute(T... args) {
				if (args != null && args.length == 2) {
					EventType zkEventTypes = (EventType) args[0];
					String path = (String) args[1];
					List<String> toks = PathUtils.tokenize_path(path);
					int size = toks.size();
					if (size >= 1) {
						String params = null;
						String root = toks.get(0);
						RunnableCallback fn = null;
						if (root.equals(Cluster.ASSIGNMENTS_ROOT)) {
							if (size == 1) {
								//设置null，返回oldvalue
								fn = assignments_callback.getAndSet(null);
							} else {
								params = toks.get(1);
								fn = assignment_info_callback.remove(params);
							}

						} else if (root.equals(Cluster.SUPERVISORS_ROOT)) {
							fn = supervisors_callback.getAndSet(null);
						}else if (root.equals(Cluster.DRPCS_ROOT)) {
								fn = drpcs_callback.getAndSet(null);
						} else if (root.equals(Cluster.STORMS_ROOT) && size > 1) {
							params = toks.get(1);
							fn = storm_base_callback.remove(params);
						} else if (root.equals(Cluster.HIGO_ROOT) && size > 1) {
							params = toks.get(1);
							fn = higo_base_callback.remove(params);
						} else {
							StormUtils.halt_process(30,
									"Unknown callback for subtree " + path);
						}
						
						if (fn != null) {
							//FIXME 何处设置这三个参数？？
							//fn.setArgs(params, zkEventTypes, path);
							fn.run();
						}
					}
				} else {
					LOG.error("the size of param is not 2");
				}

				return null;
			}

		});

		String[] pathlist = StormUtils.mk_arr(Cluster.ASSIGNMENTS_SUBTREE,
				Cluster.TASKS_SUBTREE, Cluster.STORMS_SUBTREE,
				Cluster.SUPERVISORS_SUBTREE, Cluster.TASKBEATS_SUBTREE,
				Cluster.TASKERRORS_SUBTREE);
		for (String path : pathlist) {
			cluster_state.mkdirs(path);
		}

	}

	@Override
	public Assignment assignment_info(String stormId, RunnableCallback callback) {
		if (callback != null) {
			assignment_info_callback.put(stormId, callback);
		}

		String assgnmentPath = Cluster.assignment_path(stormId);

		byte[] znodeData = cluster_state.get_data(assgnmentPath,
				callback != null);

		Object data = Cluster.maybe_deserialize(znodeData);

		if (data == null) {
			return null;
		}
		return (Assignment) data;
	}

	@Override
	public List<String> assignments(RunnableCallback callback) {
		if (callback != null) {
			assignments_callback.set(callback);
		}
		return cluster_state.get_children(Cluster.ASSIGNMENTS_SUBTREE,
				callback != null);
	}

	@Override
	public void set_assignment(String stormId, Assignment info) {
		cluster_state.set_data(Cluster.assignment_path(stormId),
				Utils.serialize(info));
	}

	@Override
	public void activate_storm(String stormId, StormBase stormBase) {
		String stormPath = Cluster.storm_path(stormId);

		byte[] stormBaseData = Utils.serialize(stormBase);

		cluster_state.set_data(stormPath, stormBaseData);
	}

	@Override
	public List<String> active_storms() {
		return cluster_state.get_children(Cluster.STORMS_SUBTREE, false);
	}

	@Override
	public List<String> heartbeat_storms() {
		return cluster_state.get_children(Cluster.TASKBEATS_SUBTREE, false);
	}

	@Override
	public List<String> heartbeat_tasks(String stormId) {
		String taskbeatPath = Cluster.taskbeat_storm_root(stormId);

		return cluster_state.get_children(taskbeatPath, false);
	}

	@Override
	public void remove_storm(String stormId) {
		cluster_state.delete_node(Cluster.storm_task_root(stormId));
		cluster_state.delete_node(Cluster.assignment_path(stormId));
		this.remove_storm_base(stormId);
	}

	@Override
	public void remove_storm_base(String stormId) {
		cluster_state.delete_node(Cluster.storm_path(stormId));
	}

	@Override
	public void remove_task_heartbeat(String stormId, int taskId) {
		String taskbeatPath = Cluster.taskbeat_path(stormId, taskId);

		cluster_state.delete_node(taskbeatPath);
	}

	@Override
	public void report_task_error(String stormId, int taskId, Throwable error) {

		// add by ourself
		LOG.warn(StormUtils.stringify_error(error));

		String path = Cluster.taskerror_path(stormId, taskId);
		cluster_state.mkdirs(path);

		List<Integer> children = new ArrayList<Integer>();

		for (String str : cluster_state.get_children(path, false)) {
			children.add(Integer.parseInt(str));
		}

		Collections.sort(children);

		while (children.size() >= 10) {
			cluster_state.delete_node(path + "/" + children.remove(0));
		}

		String timestampPath = path + "/" + TimeUtils.current_time_secs();
		byte[] errorData = new String(StormUtils.stringify_error(error))
				.getBytes();

		cluster_state.set_data(timestampPath, errorData);

	}

	@Override
	public void set_task(String stormId, int taskId, TaskInfo info) {
		String taskPath = Cluster.task_path(stormId, taskId);

		byte[] taskData = Utils.serialize(info);

		cluster_state.set_data(taskPath, taskData);
	}

	@Override
	public void setup_heartbeats(String stormId) {
		String taskbeatPath = Cluster.taskbeat_storm_root(stormId);

		cluster_state.mkdirs(taskbeatPath);
	}

	@Override
	public StormBase storm_base(String stormId, RunnableCallback callback) {
		if (callback != null) {
			storm_base_callback.put(stormId, callback);
		}
		Object data = Cluster.maybe_deserialize(cluster_state.get_data(
				Cluster.storm_path(stormId), callback != null));
		if (data == null) {
			return null;
		}
		return (StormBase) data;
	}

	@Override
	public void supervisor_heartbeat(String supervisorId, SupervisorInfo info) {

		String supervisorPath = Cluster.supervisor_path(supervisorId);

		byte[] infoData = Utils.serialize(info);

		cluster_state.set_ephemeral_node(supervisorPath, infoData);
	}
	

	@Override
	public SupervisorInfo supervisor_info(String supervisorId) {
		String supervisorPath = Cluster.supervisor_path(supervisorId);

		byte[] znodeData = cluster_state.get_data(supervisorPath, false);

		Object data = Cluster.maybe_deserialize(znodeData);
		if (data == null) {
			return null;
		}
		return (SupervisorInfo) data;

	}

	@Override
	public List<String> supervisors(RunnableCallback callback) {
		if (callback != null) {
			supervisors_callback.set(callback);
		}
		return cluster_state.get_children(Cluster.SUPERVISORS_SUBTREE,
				callback != null);
	}
	
	
	
//	@Override
//	public void drpc_heartbeat(String drpcid, DrpcInfo info) {
//
//		String drpcPath = Cluster.drpc_path(drpcid);
//
//		byte[] infoData = Utils.serialize(info);
//
//		cluster_state.set_ephemeral_node(drpcPath, infoData);
//	}
//
//	@Override
//	public DrpcInfo drpc_info(String drpcid) {
//		String drpcpath = Cluster.drpc_path(drpcid);
//
//		byte[] znodeData = cluster_state.get_data(drpcpath, false);
//
//		Object data = Cluster.maybe_deserialize(znodeData);
//		if (data == null) {
//			return null;
//		}
//		return (DrpcInfo) data;
//
//	}

	@Override
	public List<String> drpcs(RunnableCallback callback) {
		if (callback != null) {
			drpcs_callback.set(callback);
		}
		return cluster_state.get_children(Cluster.DRPCS_SUBTREE,callback != null);
	}

	@Override
	public List<String> task_error_storms() {
		return cluster_state.get_children(Cluster.TASKERRORS_SUBTREE, false);
	}

	@Override
	public List<TaskError> task_errors(String stormId, int taskId) {
		String path = Cluster.taskerror_path(stormId, taskId);
		cluster_state.mkdirs(path);

		List<String> children = cluster_state.get_children(path, false);
		List<TaskError> errors = new ArrayList<TaskError>();

		for (String str : children) {
			byte[] v = cluster_state.get_data(path + "/" + str, false);
			if (v != null) {
				TaskError error = new TaskError(new String(v),
						Integer.parseInt(str));
				errors.add(error);
			}
		}

		Collections.sort(errors, new Comparator<TaskError>() {

			@Override
			public int compare(TaskError o1, TaskError o2) {
				if (o1.getTimSecs() > o2.getTimSecs()) {
					return 1;
				}
				if (o1.getTimSecs() < o2.getTimSecs()) {
					return -1;
				}
				return 0;
			}
		});

		return errors;

	}

	@Override
	public TaskHeartbeat task_heartbeat(String stormId, int taskId) {
		String taskbeatPath = Cluster.taskbeat_path(stormId, taskId);

		byte[] znodeData = cluster_state.get_data(taskbeatPath, false);

		Object data = Cluster.maybe_deserialize(znodeData);
		if (data == null) {
			return null;
		}
		return (TaskHeartbeat) data;
	}

	@Override
	public void task_heartbeat(String stormId, int taskId, TaskHeartbeat info) {
		String taskPath = Cluster.taskbeat_path(stormId, taskId);

		byte[] taskData = Utils.serialize(info);

		cluster_state.set_data(taskPath, taskData);
	}
	
	@Override
	public void higo_heartbeat(String tablename,Integer task, SolrInfo info) {

		String tablePath = Cluster.higo_path(tablename,task);

		byte[] infoData = Utils.serialize(info);

		cluster_state.set_ephemeral_node(tablePath, infoData);
	}
	
	@Override
	public void higo_base(String tablename, RunnableCallback callback) {
		if (callback != null) {
			higo_base_callback.put(tablename, callback);
		}
	}
	
	@Override
	public List<Integer> higo_ids(String tablename) {

		String tablePath = Cluster.higo_table(tablename);

		List<String> list = cluster_state.get_children(tablePath, false);

		List<Integer> rtn = new ArrayList<Integer>();
		if(list!=null)
		{
			for (String str : list) {
				rtn.add(Integer.parseInt(str));
			}
		}
		return rtn;
	}
	
	@Override
	public List<String> higo_tableList() {

		String tablePath = Cluster.higo_root();

		List<String> list = cluster_state.get_children(tablePath, false);

		List<String> rtn = new ArrayList<String>();
		if(list!=null)
		{
        		for (String str : list) {
        			rtn.add(str);
        		}
		}
		return rtn;
	}
	
	@Override
	public void higo_remove(String tablename) {
		String tablePath = Cluster.higo_table(tablename);
		cluster_state.delete_node(tablePath);
	}
	
	@Override
	public void higo_remove_task(String tablename,Integer taskId) {
		String tablePath = Cluster.higo_path(tablename, taskId);
		cluster_state.delete_node(tablePath);
	}

	@Override
	public SolrInfo higo_info(String tablename, int taskId) {

		String tablePath = Cluster.higo_path(tablename, taskId);

		byte[] znodeData = cluster_state.get_data(tablePath, false);

		Object data = Cluster.maybe_deserialize(znodeData);
		if (data == null) {
			return null;
		}
		return (SolrInfo) data;
	}
	

	@Override
	public List<Integer> task_ids(String stromId) {

		String stormTaskPath = Cluster.storm_task_root(stromId);

		List<String> list = cluster_state.get_children(stormTaskPath, false);

		List<Integer> rtn = new ArrayList<Integer>();
		for (String str : list) {
			rtn.add(Integer.parseInt(str));
		}
		return rtn;
	}

	@Override
	public TaskInfo task_info(String stormId, int taskId) {

		String taskPath = Cluster.task_path(stormId, taskId);

		byte[] znodeData = cluster_state.get_data(taskPath, false);

		Object data = Cluster.maybe_deserialize(znodeData);
		if (data == null) {
			return null;
		}
		return (TaskInfo) data;
	}

	@Override
	public List<String> task_storms() {
		return cluster_state.get_children(Cluster.TASKS_SUBTREE, false);
	}

	@Override
	public void teardown_heartbeats(String stormId) {
		try {
			String taskbeatPath = Cluster.taskbeat_storm_root(stormId);

			cluster_state.delete_node(taskbeatPath);
		} catch (Exception e) {
			LOG.error("Could not teardown heartbeats for " + stormId, e);
		}

	}

	@Override
	public void teardown_task_errors(String stormId) {
		try {
			String taskerrPath = Cluster.taskerror_storm_root(stormId);
			cluster_state.delete_node(taskerrPath);
		} catch (Exception e) {
			LOG.error("Could not teardown errors for " + stormId, e);
		}
	}

	@Override
	public void update_storm(String stormId, StormStatus newElems) {
		/**
		 * FIXME, not sure where the old exist error or not The raw code
		 * (set-data cluster-state (storm-path storm-id) (-> (storm-base this
		 * storm-id nil) (merge new-elems) Utils/serialize)))
		 */

		StormBase base = this.storm_base(stormId, null);

		if (base != null) {
			base.setStatus(newElems);
			cluster_state.set_data(Cluster.storm_path(stormId),
					Utils.serialize(base));
		}

	}

	@Override
	public void disconnect() {
		cluster_state.unregister(state_id);
		if (solo == true) {
			cluster_state.close();
		}
	}

}