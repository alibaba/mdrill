package com.alipay.bluewhale.core.cluster;

import java.util.List;

import com.alipay.bluewhale.core.callback.RunnableCallback;
import com.alipay.bluewhale.core.daemon.supervisor.SupervisorInfo;
import com.alipay.bluewhale.core.task.common.Assignment;
import com.alipay.bluewhale.core.task.common.TaskInfo;
import com.alipay.bluewhale.core.task.error.TaskError;
import com.alipay.bluewhale.core.task.heartbeat.TaskHeartbeat;

/**
 * ����storm��zk�ϵĽڵ���Ϣ
 */
public interface StormClusterState {
	public List<String> assignments(RunnableCallback callback);
	public Assignment assignment_info(String storm_id,RunnableCallback callback);
	public void set_assignment(String stormId, Assignment info) ;

	public List<String> active_storms();
	public StormBase storm_base(String storm_id,RunnableCallback callback);
	public void activate_storm (String storm_id,StormBase storm_base);
	public void update_storm (String storm_id,StormStatus new_elems);
	public void remove_storm_base (String storm_id);
	public void remove_storm (String storm_id);

	
	public List<Integer> task_ids(String stromId);
	public void set_task(String storm_id ,int task_id,TaskInfo info);
	public TaskInfo task_info(String storm_id,int task_id);
	public List<String> task_storms();

	public void setup_heartbeats (String storm_id);
	public void teardown_heartbeats (String storm_id);
	public List<String> heartbeat_storms ();
	public List<String> heartbeat_tasks (String storm_id);
	public  TaskHeartbeat task_heartbeat(String stormId, int taskId) ;
	public void task_heartbeat(String stormId, int taskId,  TaskHeartbeat info) ;
	public void remove_task_heartbeat (String storm_id,int task_id);

	
	public List<String> supervisors (RunnableCallback callback);
	public SupervisorInfo supervisor_info (String supervisor_id) ;
	public void supervisor_heartbeat (String supervisor_id,SupervisorInfo  info);

	
	public void teardown_task_errors (String storm_id);
	public List<String> task_error_storms ();
	public void report_task_error (String storm_id,int task_id,Throwable error);
	public List<TaskError> task_errors (String storm_id,int task_id);
	
	public void disconnect ();
	
	public List<String> drpcs(RunnableCallback callback);
//	public DrpcInfo drpc_info(String supervisorId);
//	public void drpc_heartbeat(String drpcid, DrpcInfo info);
	void higo_heartbeat(String tablename, Integer task, SolrInfo info);
	List<Integer> higo_ids(String tablename);
	SolrInfo higo_info(String tablename, int taskId);
	void higo_remove(String tablename);
	void higo_remove_task(String tablename, Integer taskId);
	List<String> higo_tableList();
	List<Integer> higo_base(String tablename, RunnableCallback callback);
}