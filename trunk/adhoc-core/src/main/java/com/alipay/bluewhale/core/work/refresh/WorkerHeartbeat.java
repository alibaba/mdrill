package com.alipay.bluewhale.core.work.refresh;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * worker 发送心跳给supervisor的数据结构
 * @author yannian
 *
 */
public class WorkerHeartbeat implements Serializable{

    private static final long serialVersionUID = 1L;
	private int timeSecs;
	private String topologyId;
	private Set<Integer> taskIds;
	private Integer port;
	
	public WorkerHeartbeat(int timeSecs, String topologyId,
		Set<Integer> taskIds, Integer port){

		this.timeSecs = timeSecs;
		this.topologyId = topologyId;
		this.taskIds = new HashSet<Integer>(taskIds);
		this.port = port;

	}

	public int getTimeSecs() {
		return timeSecs;
	}

	public void setTimeSecs(int timeSecs) {
		this.timeSecs = timeSecs;
	}

	public String getTopologyId() {
		return topologyId;
	}

	public void setTopologyId(String topologyId) {
		this.topologyId = topologyId;
	}

	public Set<Integer> getTaskIds() {
		return taskIds;
	}

	public void setTaskIds(Set<Integer> taskIds) {
		this.taskIds = new HashSet<Integer>(taskIds);
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}
	public String toString(){
		return "topologyId:"+topologyId+", timeSecs:"+timeSecs+", port:"+port+", taskIds:"+taskIds.toString();
	}
}
