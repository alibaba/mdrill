package com.alimama.mdrill.topology;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.alimama.mdrill.topology.assignment.*;
import com.alipay.bluewhale.core.cluster.StormClusterState;
import com.alipay.bluewhale.core.custom.IAssignment;
import com.alipay.bluewhale.core.daemon.NodePort;
import com.alipay.bluewhale.core.daemon.supervisor.SupervisorInfo;
import com.alipay.bluewhale.core.utils.StormUtils;

public class MdrillTaskAssignment implements IAssignment {
	private static Logger LOG = Logger.getLogger(MdrillTaskAssignment.class);

	public static String MS_PORTS = "higo.merger.ports";
	public static String REALTIME_PORTS = "higo.realtime.ports";
	public static String MS_NAME = "higo.merger.componname";
	public static String SHARD_NAME = "higo.shard.componname";
	public static String REALTIME_NAME = "higo.realtime.componname";
	public static String HIGO_FIX_SHARDS = "higo.fixed.shards.assigns";
	public static String SHARD_REPLICATION = "higo.assign.shards.replication";


	public String topologyId;
	public StormClusterState zkCluster;
	public String ms_name;
	public String shard_name;
	public String realtime_name;
	public Integer replication=1;

	public SupervisorPortType porttype=new SupervisorPortType();
	
	public Map<String, SupervisorInfo> supInfos;

	public Map topology_conf;
	@Override
	public void setup(Map topology_conf, String topologyId,
			StormClusterState zkCluster,
			Map<NodePort, List<Integer>> keepAssigned,
			Map<String, SupervisorInfo> supInfos) {
		LOG.info("higolog HigoTaskAssignment setup");
		
		this.topology_conf=topology_conf;
		this.ms_name = String.valueOf(topology_conf.get(MS_NAME));
		this.shard_name = String.valueOf(topology_conf.get(SHARD_NAME));
		this.realtime_name = String.valueOf(topology_conf.get(REALTIME_NAME));
		this.replication = StormUtils.parseInt(topology_conf.containsKey(SHARD_REPLICATION)?topology_conf.get(SHARD_REPLICATION):1);

		this.supInfos = supInfos;
		this.topologyId = topologyId;
		this.zkCluster = zkCluster;
		
		this.porttype.setup(topology_conf);
	}

	
	public Map<NodePort, List<Integer>> keeperSlots(
			Map<NodePort, List<Integer>> aliveAssigned, int numTaskIds,
			int numWorkers)
	{
		Map<NodePort, List<Integer>> rtn=new HashMap<NodePort, List<Integer>>();
		rtn.putAll(aliveAssigned);
		return rtn;
	}

	@Override
	public List<NodePort> slotsAssignment(List<NodePort> freedSlots,
			int reassign_num, Set<Integer> reassignIds) {
		return freedSlots;
	}

	
	@Override
	public Map<Integer, NodePort> tasksAssignment(List<NodePort> reassignSlots,
			Set<Integer> reassignIds) {

		if (reassignIds.size() <= 0) {
			return new HashMap<Integer, NodePort>();
		}
		
		
		FreeResources freeResource=new FreeResources();
		freeResource.setup(reassignSlots, this.porttype);
	
		TaskNeedAssign taskNeedassign=new TaskNeedAssign();
		taskNeedassign.setSpecialTask(reassignIds, this);
		
		Map<Integer, NodePort> rtn = new HashMap<Integer, NodePort>();

		AssignmentByHost assignByHost=new AssignmentByHost(this);
		rtn.putAll(assignByHost.assignment(freeResource,taskNeedassign));

		AssignmentByRandom assignByRandom=new AssignmentByRandom();
		rtn.putAll(assignByRandom.assign(freeResource, taskNeedassign));

		NodePort tmp=null;
		for(Entry<Integer, NodePort> e:rtn.entrySet())
		{
			tmp=e.getValue();
			break;
		}

		if (tmp == null) {
			tmp = reassignSlots.get(0);
		}

		for (Integer tid : taskNeedassign.otherTask) {
			rtn.put(tid, tmp);
		}

		return rtn;
	}

	@Override
	public void cleanup() {

	}

}
