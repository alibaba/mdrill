package com.alipay.bluewhale.core.work.context;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import backtype.storm.generated.StormTopology;
import backtype.storm.task.TopologyContext;

import com.alipay.bluewhale.core.cluster.Common;
import com.alipay.bluewhale.core.cluster.StormConfig;

/**
 * 创建系统TopologyContext,带acker bolt的
 * 
 * @author yannian
 * 
 */
public class SystemContextMake {
	private static Logger LOG = Logger.getLogger(SystemContextMake.class);

	private StormTopology topology;
	private Map stormConf;
	private String topologyId;
	private String workerId;
	private HashMap<Integer, String> tasksToComponent;

	public SystemContextMake(StormTopology topology, Map storm_conf,
			String topologyId, String worker_id,
			HashMap<Integer, String> tasksToComponent) {
		this.topology = topology;
		this.stormConf = storm_conf;
		this.topologyId = topologyId;
		this.workerId = worker_id;
		this.tasksToComponent = tasksToComponent;
	}

	public TopologyContext make(Integer task_id) {
		TopologyContext rtn = null;
		try {
			StormTopology systopology = Common.system_topology(stormConf,topology);
			String distroot = StormConfig.supervisor_stormdist_root(stormConf,topologyId);
			String resourcePath = StormConfig.supervisor_storm_resources_path(distroot);
			String workpid = StormConfig.worker_pids_root(stormConf, workerId);
			rtn = new TopologyContext(systopology, tasksToComponent, topologyId,resourcePath, workpid, task_id);
		} catch (Exception e) {
			LOG.error("SystemContextMake make", e);
		}
		return rtn;
	}

}
