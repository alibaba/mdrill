package com.alimama.mdrill.topology.assignment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.alipay.bluewhale.core.daemon.NodePort;
import com.alimama.mdrill.topology.MdrillTaskAssignment;
import com.alimama.mdrill.topology.assignment.Utils.*;

public class AssignmentByHost {
	private static Logger LOG = Logger.getLogger(AssignmentByHost.class);
	private FixdIndex2Host index2host=new FixdIndex2Host();
	private MdrillTaskAssignment params;
	public AssignmentByHost(MdrillTaskAssignment params) {
		this.params = params;
		index2host.setup(params.topology_conf, params);
		this.index2host.taskId2Index(params, params.topologyId);

		LOG.info("fixassgin:" +this.index2host.shardfixdMap.toString());
	}

	public Map<Integer, NodePort> assignment(FreeResources freeResource,TaskNeedAssign jobids)
	{
		HostFreePorts hostFreePorts=new HostFreePorts(params);
		hostFreePorts.parse(freeResource);
		Map<Integer, NodePort> rtn = new HashMap<Integer, NodePort>();
		rtn.putAll(assignment( hostFreePorts.shard,freeResource.shard,jobids.shardTask, this.index2host.shardfixdMap));
		rtn.putAll(assignment( hostFreePorts.ms,freeResource.ms,jobids.msTask, this.index2host.mergerMap));
		rtn.putAll(assignment( hostFreePorts.realtime,freeResource.realtime,jobids.realtimeTask, this.index2host.realtimeMap));

		return rtn;
	}
	
	
	private Map<Integer, NodePort> assignment(HostSlots[] hostFreeResource,HashSet<NodePort> freeResource,HashSet<Integer> jobids,HashMap<Integer,String> fixassgin)
	{
		Map<Integer, NodePort> rtn = new HashMap<Integer, NodePort>();
		if(jobids.size()<=0)
		{
			return rtn;
		}
		
		HashMap<String,HostSlots> hostSlotsMap=new HashMap<String, HostSlots>();
			for(HostSlots e:hostFreeResource)
			{
				hostSlotsMap.put(e.host, e);
			}
			
			for(Entry<Integer,String> e:fixassgin.entrySet())
			{
				String host=e.getValue();
				HostSlots hs=hostSlotsMap.get(host);
				if(hs!=null)
				{
					hs.fixd=true;
				}
			}
		
		

		int hostsize = hostFreeResource.length;
		if(hostsize>0)
		{
			for(Integer tid:jobids)
			{
				Integer tindex=this.index2host.taskId2Index.get(tid);
				if(tindex==null||!fixassgin.containsKey(tindex))
				{
					continue;
				}
				String host=fixassgin.get(tindex);
				HostSlots hostslot=hostSlotsMap.get(host);
				if(hostslot==null)
				{
					continue;
				}
				hostslot.fixd=true;
				List<NodePort> list = hostslot.ports;
				if (hostslot.index < list.size()) {
					NodePort np = list.get(hostslot.index);
					rtn.put(tid, np);
					LOG.info("higolog assign:" + tid + "==>" + hostslot.host + ","	+ hostslot.index + "," + np.getPort());
					hostslot.index++;
				}
			}
			
			ArrayList<HostSlots> hslist=new ArrayList<HostSlots>();
			for(HostSlots hs:hostFreeResource)
			{
				if(hs.fixd)
				{
					for(int i=hs.index;i<hs.ports.size();i++)
					{
						freeResource.remove(hs.ports.get(i));
					}
					hs.index=hs.ports.size();
				}else{
					hslist.add(hs);
				}
			}
			
			if(hslist.size()>0)
			{
				for (Integer tid : jobids) {
					Integer tindex=this.index2host.taskId2Index.get(tid);
	
					if(tindex!=null&&fixassgin.containsKey(tindex))
					{
						continue;
					}
					
					Integer index = tid % hslist.size();
					HostSlots hostslot = hslist.get(index);
					List<NodePort> list = hostslot.ports;
					if (hostslot.index < list.size()) {
						NodePort np = list.get(hostslot.index);
						rtn.put(tid, np);
						LOG.info("higolog assign:" + tid + "==>" + hostslot.host + ","	+ hostslot.index + "," + np.getPort());
						hostslot.index++;
					}
				}
			}
		}else{
			LOG.info("higolog assign: is empty");

		}
		
		for (Entry<Integer, NodePort> e : rtn.entrySet()) {
			jobids.remove(e.getKey());
			freeResource.remove(e.getValue());
		}
		
		return rtn;
	}
	



}
