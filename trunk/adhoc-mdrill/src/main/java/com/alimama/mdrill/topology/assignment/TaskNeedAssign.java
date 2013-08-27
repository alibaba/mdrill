package com.alimama.mdrill.topology.assignment;

import java.util.HashSet;
import java.util.Set;

import com.alimama.mdrill.topology.MdrillTaskAssignment;
import com.alipay.bluewhale.core.task.common.TaskInfo;

public class TaskNeedAssign {

	public HashSet<Integer> msTask = new HashSet<Integer>();
	public HashSet<Integer> realtimeTask = new HashSet<Integer>();
	public HashSet<Integer> shardTask = new HashSet<Integer>();
	public HashSet<Integer> otherTask = new HashSet<Integer>();
	
	
	public void setSpecialTask(Set<Integer> reassignIds,MdrillTaskAssignment assen) {
		for (Integer tid : reassignIds) {
			TaskInfo info = assen.zkCluster.task_info(assen.topologyId, tid);
			if (info == null) {
				this.otherTask.add(tid);
			}
			if (info.getComponentId().indexOf(assen.shard_name) >= 0) {
				this.shardTask.add(tid);
			} else if (info.getComponentId().indexOf(assen.ms_name) >= 0) {
				this.msTask.add(tid);
			} else if (info.getComponentId().indexOf(assen.realtime_name) >= 0) {
				this.realtimeTask.add(tid);
			} else {
				this.otherTask.add(tid);
			}
		}
	}

}
