package com.alimama.mdrill.topology.assignment;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.alimama.mdrill.topology.MdrillTaskAssignment;
import com.alipay.bluewhale.core.task.common.TaskInfo;
import com.alipay.bluewhale.core.utils.StormUtils;

public class TaskNeedAssign {

	public HashSet<Integer> msTask = new HashSet<Integer>();
	public HashMap<Integer,HashSet<Integer>> shardTask = new HashMap<Integer,HashSet<Integer>>();
	public HashMap<Integer,HashSet<Integer>> realtimeTask = new HashMap<Integer,HashSet<Integer>>();

	public HashSet<Integer> otherTask = new HashSet<Integer>();
	
	
	public void setSpecialTask(Set<Integer> reassignIds,MdrillTaskAssignment assen) {
		
		for(int i=0;i<assen.replication;i++)
		{
			shardTask.put(i, new HashSet<Integer>());
			realtimeTask.put(i, new HashSet<Integer>());
		}
		
		for (Integer tid : reassignIds) {
			TaskInfo info = assen.zkCluster.task_info(assen.topologyId, tid);
			if (info == null) {
				this.otherTask.add(tid);
			}
			String commnetId=info.getComponentId();
			Integer group=0;
			String[] cols=commnetId.split("@");
			if(cols.length>=2)
			{
				group=StormUtils.parseInt(cols[1]);
			}
			if (commnetId.indexOf(assen.shard_name) >= 0) {
				this.shardTask.get(group).add(tid);
			} else if (commnetId.indexOf(assen.ms_name) >= 0) {
				this.msTask.add(tid);
			} else if (commnetId.indexOf(assen.realtime_name) >= 0) {
				this.realtimeTask.get(group).add(tid);
			} else {
				this.otherTask.add(tid);
			}
		}
	}

}
