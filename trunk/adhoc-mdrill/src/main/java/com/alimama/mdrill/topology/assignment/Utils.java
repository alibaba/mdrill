package com.alimama.mdrill.topology.assignment;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.alipay.bluewhale.core.cluster.StormClusterState;
import com.alipay.bluewhale.core.daemon.NodePort;
import com.alipay.bluewhale.core.daemon.supervisor.SupervisorInfo;

public class Utils {
	static Logger LOG = Logger.getLogger(Utils.class);

	public static Set<NodePort> AllSlots(Map<String, SupervisorInfo> supervisorInfos,StormClusterState stormClusterState) {

		LOG.info("higolog supervisorInfos:" + supervisorInfos.toString());

		Map<String, List<Integer>> allSlots = new HashMap<String, List<Integer>>();
		for (Entry<String, SupervisorInfo> entry : supervisorInfos.entrySet()) {

			String supervisorid = entry.getKey();
			SupervisorInfo info = stormClusterState
					.supervisor_info(supervisorid);
			allSlots.put(entry.getKey(), info.getWorkPorts());
		}

		Set<NodePort> rtn = new HashSet<NodePort>();
		for (Entry<String, List<Integer>> entry : allSlots.entrySet()) {
			String supervisorid = entry.getKey();
			List<Integer> s = entry.getValue();
			for (Integer port : s) {
				NodePort nodeport = new NodePort(supervisorid, port);
				rtn.add(nodeport);
			}
		}
		return rtn;
	}
	
	
	public static class HostSlots {
		public String host;
		public Integer index = 0;
		public boolean fixd = false;
		@Override
		public String toString() {
			return "HostSlots [host=" + host + ", ports=" + ports + "]";
		}

		public List<NodePort> ports;

		public HostSlots(String host, List<NodePort> ports) {
			super();
			this.host = host;
			this.ports = ports;
		}
	}
}
