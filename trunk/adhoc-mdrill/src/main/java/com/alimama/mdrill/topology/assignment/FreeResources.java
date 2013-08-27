package com.alimama.mdrill.topology.assignment;

import java.util.HashSet;
import java.util.List;

import com.alipay.bluewhale.core.daemon.NodePort;

public class FreeResources {

	HashSet<NodePort> shard = new HashSet<NodePort>();
	HashSet<NodePort> ms = new HashSet<NodePort>();
	HashSet<NodePort> realtime = new HashSet<NodePort>();
	
	public void setup(List<NodePort> reassignSlots,SupervisorPortType porttype)
	{
		for (NodePort p : reassignSlots) {
			if (porttype.isMergerPort(p.getPort())) {
				ms.add(p);
			}else if (porttype.isRealTimePort(p.getPort())) {
				realtime.add(p);
			} else {
				shard.add(p);
			}
		}
	}

}
