package com.alipay.bluewhale.core.custom;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alipay.bluewhale.core.cluster.StormClusterState;
import com.alipay.bluewhale.core.daemon.NodePort;
import com.alipay.bluewhale.core.daemon.supervisor.SupervisorInfo;

/**
 * 用户自定义任务的分配规则
 * 
 * @author yannian
 * @version $Id: IAssignment.java, v 0.1 2012-4-28 下午4:48:35 yannian Exp $
 */
public interface IAssignment {
	public Map<NodePort, List<Integer>> keeperSlots(
			Map<NodePort, List<Integer>> aliveAssigned, int numTaskIds,
			int numWorkers);
    public void setup(Map topology_conf, String topologyId, StormClusterState zkCluster,
                      Map<NodePort, List<Integer>> keepAssigned,Map<String, SupervisorInfo> supInfos);

    public List<NodePort> slotsAssignment(List<NodePort> freedSlots, int reassign_num,Set<Integer> reassignIds);

    public Map<Integer, NodePort> tasksAssignment(List<NodePort> reassignSlots,Set<Integer> reassignIds);
    
    public void cleanup();
}
