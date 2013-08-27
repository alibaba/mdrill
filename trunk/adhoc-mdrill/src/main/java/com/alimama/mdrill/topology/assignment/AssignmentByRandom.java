package com.alimama.mdrill.topology.assignment;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.alipay.bluewhale.core.daemon.NodePort;

public class AssignmentByRandom {
	private static Logger LOG = Logger.getLogger(AssignmentByRandom.class);


	public Map<Integer, NodePort> assign(FreeResources resources,TaskNeedAssign jobids)
	{
		Map<Integer, NodePort> shardAssignRandom =this.randomAssign(resources.shard, jobids.shardTask);
		Map<Integer, NodePort> msAssignRandom =this.randomAssign(resources.ms, jobids.msTask);
		Map<Integer, NodePort> realtimeAssignRandom =this.randomAssign(resources.realtime, jobids.realtimeTask);
		
		Map<Integer, NodePort> rtn = new HashMap<Integer, NodePort>();
		rtn.putAll(shardAssignRandom);
		rtn.putAll(msAssignRandom);
		rtn.putAll(realtimeAssignRandom);
		
		return rtn;
	}

	
	
	private Map<Integer, NodePort> randomAssign(HashSet<NodePort> resource,HashSet<Integer> jobids)
	{
		Map<Integer, NodePort> rtn = new HashMap<Integer, NodePort>();
		if(jobids.size()<=0)
		{
			return rtn;
		}
		Integer index = 0;
		NodePort[] nodes=new NodePort[resource.size()];
		resource.toArray(nodes);
		for (Integer tid : jobids) {
			if(index>=nodes.length)
			{
				break;
			}
			NodePort np = nodes[index];
			rtn.put(tid, np);
			LOG.info("higolog random assign:" + tid + "==>" + np.getPort());
			index++;
		}
		
		for (Entry<Integer, NodePort> e : rtn.entrySet()) {
			jobids.remove(e.getKey());
			resource.remove(e.getValue());
		}
		
		return rtn;
		
	}
}
