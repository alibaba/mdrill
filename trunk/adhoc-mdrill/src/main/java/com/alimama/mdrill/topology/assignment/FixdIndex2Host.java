package com.alimama.mdrill.topology.assignment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alimama.mdrill.topology.MdrillTaskAssignment;
import com.alipay.bluewhale.core.utils.StormUtils;

public class FixdIndex2Host {

	public HashMap<Integer,HashMap<Integer,String>> shardfixdMap=new HashMap<Integer,HashMap<Integer,String>>();
	public HashMap<Integer,String> mergerMap=new HashMap<Integer,String>();
	public HashMap<Integer,HashMap<Integer,String>> realtimeMap=new HashMap<Integer,HashMap<Integer,String>>();
	
	public HashMap<Integer,HashMap<Integer,Integer>> taskId2Index=new HashMap<Integer,HashMap<Integer,Integer>>();

	public void setup(Map topology_conf,MdrillTaskAssignment assimeng)
	{
		for(int i=0;i<assimeng.replication;i++)
		{
			shardfixdMap.put(i,new HashMap<Integer,String>());
			realtimeMap.put(i, new HashMap<Integer,String>());
		}
		//fix assignment 
		Integer fixassign = StormUtils.parseInt(topology_conf.get(MdrillTaskAssignment.HIGO_FIX_SHARDS));
		for(int i=1;i<=fixassign;i++)
		{
			String ass=String.valueOf(topology_conf.get(MdrillTaskAssignment.HIGO_FIX_SHARDS+"."+i));
			String[] host_ids=ass.split(":");
			String comp_name=assimeng.shard_name;
			if(host_ids.length>=4)
			{
				comp_name=host_ids[3];
			}
			
			Integer group=StormUtils.parseInt(host_ids[0]);
			
			 HashMap<Integer,String> fixMap=null;
			 if (comp_name.indexOf(assimeng.shard_name) >= 0) {
				 fixMap=this.shardfixdMap.get(group);
				} else if (comp_name.indexOf(assimeng.ms_name) >= 0) {
					fixMap=this.mergerMap;
				} else if (comp_name.indexOf(assimeng.realtime_name) >= 0) {
					fixMap=this.realtimeMap.get(group);
				} else {
					fixMap=this.mergerMap;
				}
			
			if(host_ids.length>=3)
			{
				String hostname=host_ids[1].trim();
				String[] ids=host_ids[2].split(",");
				for(String tidIndex:ids)
				{
					fixMap.put(Integer.parseInt(tidIndex), hostname);
				}
			}
			
		}
	}
	
	public void taskId2Index(MdrillTaskAssignment ass,String topologyId)
	{
		for(int i=0;i<ass.replication;i++)
		{
			taskId2Index.put(i,new HashMap<Integer,Integer>());
		}
		Set<Integer> allTaskIds = StormUtils.listToSet(ass.zkCluster.task_ids(topologyId));
		TaskNeedAssign jobids2=new TaskNeedAssign();
		jobids2.setSpecialTask(allTaskIds, ass);
		
		for(int i=0;i<ass.replication;i++)
		{
		
			
		        List<Integer> tasks = new ArrayList<Integer>(jobids2.shardTask.get(i));
		        Collections.sort(tasks);
		        for(int j=0; j<tasks.size(); j++) {
		            taskId2Index.get(i).put(tasks.get(j), j);
		        }
		        
				Utils.LOG.info("taskId2Index "+taskId2Index.toString());
		}
	}


}
