package com.alipay.bluewhale.core.work.context;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import backtype.storm.generated.StormTopology;
import backtype.storm.task.TopologyContext;

import com.alipay.bluewhale.core.cluster.StormConfig;

/**
 * 创建用户所使用的TopologyContext 里面不含有acker bolt
 * 
 * @author yannian
 * 
 */
public class UserContextMake {
	private static Logger LOG = Logger.getLogger(UserContextMake.class);

	private StormTopology topology;
	private Map storm_conf;
	private String topologyId;
	private String worker_id;
	private HashMap<Integer, String> tasksToComponent;

	public UserContextMake(StormTopology topology, Map storm_conf,
			String topologyId, String worker_id,
			HashMap<Integer, String> tasks_component) {
		this.topology = topology;
		this.storm_conf = storm_conf;
		this.topologyId = topologyId;
		this.worker_id = worker_id;
		this.tasksToComponent = tasks_component;
	}

	public TopologyContext make(Integer task_id) {
		TopologyContext rtn=null;
		try {
			String distroot = StormConfig.supervisor_stormdist_root(storm_conf,topologyId);
			String resourcePath = StormConfig.supervisor_storm_resources_path(distroot);
			String workpid = StormConfig.worker_pids_root(storm_conf, worker_id);		
			rtn = new TopologyContext(topology, tasksToComponent, topologyId,
					resourcePath, workpid, task_id);
		} catch (IOException e) {
			LOG.error("UserContextMake make", e);
		}
		return rtn;

	}

}
