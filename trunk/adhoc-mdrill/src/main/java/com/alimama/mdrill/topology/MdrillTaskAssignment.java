package com.alimama.mdrill.topology;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.alimama.mdrill.topology.MdrillTaskAssignmentBase.*;
import com.alipay.bluewhale.core.cluster.StormClusterState;
import com.alipay.bluewhale.core.custom.IAssignment;
import com.alipay.bluewhale.core.daemon.NodePort;
import com.alipay.bluewhale.core.daemon.supervisor.SupervisorInfo;

public class MdrillTaskAssignment implements IAssignment {
	private static Logger LOG = Logger.getLogger(MdrillTaskAssignment.class);

	public static String MS_PORTS = "higo.merger.ports";
	public static String REALTIME_PORTS = "higo.realtime.ports";
	public static String MS_NAME = "higo.merger.componname";
	public static String SHARD_NAME = "higo.shard.componname";
	public static String REALTIME_NAME = "higo.realtime.componname";

	public String topologyId;
	public StormClusterState zkCluster;
	public String ms_name;
	public String shard_name;
	public String realtime_name;

	MdrillTaskAssignmentBase.PortsType porttype=new MdrillTaskAssignmentBase.PortsType();
	

	private Map<String, SupervisorInfo> supInfos;

	@Override
	public void setup(Map topology_conf, String topologyId,
			StormClusterState zkCluster,
			Map<NodePort, List<Integer>> keepAssigned,
			Map<String, SupervisorInfo> supInfos) {
		LOG.info("higolog HigoTaskAssignment setup");

		this.topologyId = topologyId;
		this.zkCluster = zkCluster;
		porttype.setup(topology_conf);
		
		this.ms_name = String.valueOf(topology_conf.get(MS_NAME));
		this.shard_name = String.valueOf(topology_conf.get(SHARD_NAME));
		this.realtime_name = String.valueOf(topology_conf.get(REALTIME_NAME));
		this.supInfos = supInfos;
	}

	



	@Override
	public List<NodePort> slotsAssignment(List<NodePort> freedSlots,
			int reassign_num, Set<Integer> reassignIds) {
		return freedSlots;
	}

	

	private MdrillTaskAssignmentBase.HostSlots[] getHostSlots(Set<NodePort> shard,Set<NodePort> allSlots,portTypeEnum type) {

		HashMap<String, List<NodePort>> allshard = new HashMap<String, List<NodePort>>();
		for (NodePort p : allSlots) {
			if (this.porttype.isType(type, p.getPort())) {
				SupervisorInfo sinfo = supInfos.get(p.getNode());
				List<NodePort> ports = allshard.get(sinfo.getHostName());
				if (ports == null) {
					ports = new ArrayList<NodePort>();
					allshard.put(sinfo.getHostName(), ports);
				}
				if (shard.contains(p)) {
					ports.add(p);
				}
			}
		}

		
		List<String> shardHosts = new ArrayList<String>();
		shardHosts.addAll(allshard.keySet());
		String[] shardsHostArr = new String[shardHosts.size()];
		shardHosts.toArray(shardsHostArr);
		Arrays.sort(shardsHostArr);

		HostSlots[] allList = new HostSlots[shardsHostArr.length];

		for (int i = 0; i < shardsHostArr.length; i++) {
			String host = shardsHostArr[i];
			allList[i] = new HostSlots(host, allshard.get(host));
		}

		return allList;
	}



	public Map<Integer, NodePort> assignment(HostSlots[] ResourceMap,HashSet<NodePort> resource,HashSet<Integer> jobids,String logtype)
	{
		Map<Integer, NodePort> rtn = new HashMap<Integer, NodePort>();
		if(jobids.size()<=0)
		{
			return rtn;
		}

		int hostsize = ResourceMap.length;
		if(hostsize>0)
		{
			for (Integer tid : jobids) {
				Integer index = tid % hostsize;
				HostSlots hostslot = ResourceMap[index];
				List<NodePort> list = hostslot.ports;
				if (hostslot.index < list.size()) {
					NodePort np = list.get(hostslot.index);
					rtn.put(tid, np);
					LOG.info("higolog assign:" +logtype+"@"+ tid + "==>" + hostslot.host + ","	+ hostslot.index + "," + np.getPort());
					hostslot.index++;
				}
			}
		}else{
			LOG.info("higolog assign:" +logtype+" is empty");

		}
		
		for (Entry<Integer, NodePort> e : rtn.entrySet()) {
			jobids.remove(e.getKey());
			resource.remove(e.getValue());
		}
		
		return rtn;
	}
	
	public Map<Integer, NodePort> randomAssign(HashSet<NodePort> resource,HashSet<Integer> jobids)
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
	
	@Override
	public Map<Integer, NodePort> tasksAssignment(List<NodePort> reassignSlots,
			Set<Integer> reassignIds) {

		LOG.info("higolog reassignIds " + reassignIds.toString());
		if (reassignIds.size() <= 0) {
			return new HashMap<Integer, NodePort>();
		}
		
		Set<NodePort> allSlots = MdrillTaskAssignmentBase.AllSlots(supInfos, zkCluster);


		TaskJobIds jobids=new TaskJobIds();
		jobids.setSpecialTask(reassignIds, this);
		
		Resources resources=new Resources();
		resources.setup(reassignSlots, this.porttype);
	

		HostSlots[] shardResourceMap = getHostSlots(resources.shard,allSlots,portTypeEnum.shard);
		HostSlots[] msResourceMap = getHostSlots(resources.ms,allSlots,portTypeEnum.mergerserver);
		HostSlots[] realtimeResourceMap = getHostSlots(resources.realtime,allSlots,portTypeEnum.realtime);
		
		Map<Integer, NodePort> shardAssign =this.assignment(shardResourceMap, resources.shard, jobids.shardTask,"shard");
		Map<Integer, NodePort> msAssign =this.assignment(msResourceMap, resources.ms, jobids.msTask,"ms");
		Map<Integer, NodePort> realtimeAssign =this.assignment(realtimeResourceMap, resources.realtime, jobids.realtimeTask,"realtime");
		
		Map<Integer, NodePort> shardAssignRandom =this.randomAssign(resources.shard, jobids.shardTask);
		Map<Integer, NodePort> msAssignRandom =this.randomAssign(resources.ms, jobids.msTask);
		Map<Integer, NodePort> realtimeAssignRandom =this.randomAssign(resources.realtime, jobids.realtimeTask);
		
		
		Map<Integer, NodePort> rtn = new HashMap<Integer, NodePort>();
		rtn.putAll(shardAssign);
		rtn.putAll(msAssign);
		rtn.putAll(realtimeAssign);
		rtn.putAll(shardAssignRandom);
		rtn.putAll(msAssignRandom);
		rtn.putAll(realtimeAssignRandom);

		NodePort tmp=null;
		for(Entry<Integer, NodePort> e:rtn.entrySet())
		{
			tmp=e.getValue();
			break;
		}

		// ����acker��spout
		if (tmp == null) {
			tmp = reassignSlots.get(0);
		}

		for (Integer tid : jobids.otherTask) {
			rtn.put(tid, tmp);
		}

		return rtn;
	}

	@Override
	public void cleanup() {

	}

}
