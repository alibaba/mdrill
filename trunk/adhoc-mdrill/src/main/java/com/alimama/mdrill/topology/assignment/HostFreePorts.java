package com.alimama.mdrill.topology.assignment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;


import com.alipay.bluewhale.core.daemon.NodePort;
import com.alipay.bluewhale.core.daemon.supervisor.SupervisorInfo;
import com.alimama.mdrill.topology.MdrillTaskAssignment;
import com.alimama.mdrill.topology.assignment.Utils.*;

public class HostFreePorts {
	HostSlots[] ms;
	HostSlots[] shard;
	HostSlots[] realtime;
	private MdrillTaskAssignment params;

	
	public HostFreePorts(MdrillTaskAssignment params) {
		super();
		this.params = params;
	}
	
	public void parse(FreeResources freeResource)
	{
		Set<NodePort> allSlots = Utils.AllSlots(params.supInfos, params.zkCluster);

		this.shard=this.getHostSlots(freeResource.shard, allSlots, PortTypeEnum.shard);
		this.ms=this.getHostSlots(freeResource.ms, allSlots, PortTypeEnum.mergerserver);
		this.realtime=this.getHostSlots(freeResource.realtime, allSlots, PortTypeEnum.realtime);
	}


	private HostSlots[] getHostSlots(Set<NodePort> shard,Set<NodePort> allSlots,PortTypeEnum type) {

		HashMap<String, List<NodePort>> allshard = new HashMap<String, List<NodePort>>();
		for (NodePort p : allSlots) {
			if (params.porttype.isType(type, p.getPort())) {
				SupervisorInfo sinfo = params.supInfos.get(p.getNode());
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
}
