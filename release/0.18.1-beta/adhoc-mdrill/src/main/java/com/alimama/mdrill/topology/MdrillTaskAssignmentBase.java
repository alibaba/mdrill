package com.alimama.mdrill.topology;

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
import com.alipay.bluewhale.core.task.common.TaskInfo;

public class MdrillTaskAssignmentBase {
	private static Logger LOG = Logger.getLogger(MdrillTaskAssignmentBase.class);

	public static Set<NodePort> AllSlots(
			Map<String, SupervisorInfo> supervisorInfos,
			StormClusterState stormClusterState) {

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
	
	
	public static class PortsType{
		private HashSet<Integer> msPorts = new HashSet<Integer>();
		private HashSet<Integer> rPorts = new HashSet<Integer>();
		public void setup(Map topology_conf)
		{
			String[] ports = String.valueOf(topology_conf.get(MdrillTaskAssignment.MS_PORTS)).trim().split(",");
			for (String p : ports) {
				this.msPorts.add(Integer.parseInt(p.trim()));
			}
			
			String[] rports = String.valueOf(topology_conf.get(MdrillTaskAssignment.REALTIME_PORTS)).trim().split(",");
			for (String p : rports) {
				this.rPorts.add(Integer.parseInt(p.trim()));
			}
		}
		
		private boolean isMergerPort(int p)
		{
			return msPorts.contains(p);
		}
		
		private boolean isRealTimePort(int p)
		{
			return rPorts.contains(p);
		}
		
		public boolean isType(portTypeEnum t,int p)
		{
			if(t.equals(portTypeEnum.mergerserver))
			{
				return isMergerPort(p);
			}else if(t.equals(portTypeEnum.realtime))
			{
				return isRealTimePort(p);
			}

			return !isRealTimePort(p)&&!isMergerPort(p);
		}
	}
	
	public static enum portTypeEnum{
		mergerserver,shard,realtime
	}
	
	public static class HostSlots {
		public String host;
		public Integer index = 0;

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
	
	public static class Resources{
		HashSet<NodePort> shard = new HashSet<NodePort>();
		HashSet<NodePort> ms = new HashSet<NodePort>();
		HashSet<NodePort> realtime = new HashSet<NodePort>();
		
		public void setup(List<NodePort> reassignSlots,PortsType porttype)
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
	
	public static class TaskJobIds{
		public HashSet<Integer> msTask = new HashSet<Integer>();
		public HashSet<Integer> realtimeTask = new HashSet<Integer>();
		public HashSet<Integer> shardTask = new HashSet<Integer>();
		public HashSet<Integer> otherTask = new HashSet<Integer>();
		
		/**
		 * ��������shards,merger server��ֿ�
		 * 
		 * @param reassignIds
		 */
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
}
