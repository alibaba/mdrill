package com.alimama.mdrill.topology.assignment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alimama.mdrill.topology.MdrillTaskAssignment;
import com.alipay.bluewhale.core.utils.StormUtils;

public class FixdIndex2Host {

	public HashMap<Integer,String> shardfixdMap=new HashMap<Integer,String>();
	public HashMap<Integer,String> mergerMap=new HashMap<Integer,String>();
	public HashMap<Integer,String> realtimeMap=new HashMap<Integer,String>();
	
	public HashMap<Integer,Integer> taskId2Index=new HashMap<Integer,Integer>();

	public void setup(Map topology_conf,MdrillTaskAssignment assimeng)
	{
		//fix assignment 
		Integer fixassign = StormUtils.parseInt(topology_conf.get(MdrillTaskAssignment.HIGO_FIX_SHARDS));
		for(int i=1;i<=fixassign;i++)
		{
			String ass=String.valueOf(topology_conf.get(MdrillTaskAssignment.HIGO_FIX_SHARDS+"."+i));
			String[] host_ids=ass.split(":");
			String comp_name=assimeng.shard_name;
			if(host_ids.length>=3)
			{
				comp_name=host_ids[2];
			}
			
			 HashMap<Integer,String> fixMap=this.shardfixdMap;
			 if (comp_name.indexOf(assimeng.shard_name) >= 0) {
				 fixMap=this.shardfixdMap;
				} else if (comp_name.indexOf(assimeng.ms_name) >= 0) {
					fixMap=this.mergerMap;
				} else if (comp_name.indexOf(assimeng.realtime_name) >= 0) {
					fixMap=this.realtimeMap;
				} else {
					fixMap=this.mergerMap;
				}
			
			if(host_ids.length>=2)
			{
				String hostname=host_ids[0].trim();
				String[] ids=host_ids[1].split(",");
				for(String tidIndex:ids)
				{
					fixMap.put(Integer.parseInt(tidIndex), hostname);
				}
			}
			
		}
	}
	
	public void taskId2Index(MdrillTaskAssignment ass,String topologyId)
	{
		Set<Integer> allTaskIds = StormUtils.listToSet(ass.zkCluster.task_ids(topologyId));
		TaskNeedAssign jobids2=new TaskNeedAssign();
		jobids2.setSpecialTask(allTaskIds, ass);
		
	        List<Integer> tasks = new ArrayList<Integer>(jobids2.shardTask);
	        Collections.sort(tasks);
	        for(int i=0; i<tasks.size(); i++) {
	            taskId2Index.put(tasks.get(i), i);
	        }
	        
			Utils.LOG.info("taskId2Index "+taskId2Index.toString());
	}


}
