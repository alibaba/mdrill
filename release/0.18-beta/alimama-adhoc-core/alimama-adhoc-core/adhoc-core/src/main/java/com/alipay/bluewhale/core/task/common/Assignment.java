package com.alipay.bluewhale.core.task.common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.alipay.bluewhale.core.daemon.NodePort;
import com.alipay.bluewhale.core.utils.StormUtils;
/**
 * zk上/storm-zk-root/assignments/{topologyid}下存储的任务信息
 * nodeHost： {supervisorid: hostname}
 * taskStartTimeSecs: 存储taskid和任务的起始时间
 * masterCodeDir：code的路径
 * taskToNodeport： 每个任务所对于的supervisor和port
 */
public class Assignment implements Serializable {
    private static final long serialVersionUID = 1L;
    private Map<String, String> nodeHost;
    private Map<Integer, Integer> taskStartTimeSecs;
    private String masterCodeDir;
    private Map<Integer, NodePort> taskToNodeport;

    @Override
    public String toString() {
        StringBuffer buff=new StringBuffer();
        buff.append("{");
        buff.append("nodeHost:"+StormUtils.to_json(nodeHost));
        buff.append(",");
        buff.append("taskToNodeport:"+StormUtils.to_json(taskToNodeport));
        buff.append(",");
        buff.append("taskStartTimeSecs:"+StormUtils.to_json(taskStartTimeSecs));
        buff.append(",");
        buff.append("masterCodeDir:\""+masterCodeDir+"\"");
        buff.append("}");
        return buff.toString();
    }
    public Assignment(String masterCodeDir,
	    Map<Integer, NodePort> taskToNodeport,
	    Map<String, String> nodeHost,
	    Map<Integer, Integer> taskStartTimeSecs) {
	this.taskToNodeport = taskToNodeport;
	this.nodeHost = nodeHost;
	this.taskStartTimeSecs = taskStartTimeSecs;
	this.masterCodeDir = masterCodeDir;
    }

    public Map<String, String> getNodeHost() {
	return nodeHost;
    }

    public void setNodePorts(Map<String, String> nodeHost) {
	this.nodeHost = nodeHost;
    }

    public Map<Integer, Integer> getTaskStartTimeSecs() {
	return taskStartTimeSecs;
    }

    public void setTaskStartTimeSecs(Map<Integer, Integer> taskStartTimeSecs) {
	this.taskStartTimeSecs = taskStartTimeSecs;
    }

    public String getMasterCodeDir() {
	return masterCodeDir;
    }

    public void setMasterCodeDir(String masterCodeDir) {
	this.masterCodeDir = masterCodeDir;
    }

    public Map<Integer, NodePort> getTaskToNodeport() {
	return taskToNodeport;
    }

    public void setTaskToNodeport(Map<Integer, NodePort> taskToNodeport) {
	this.taskToNodeport = taskToNodeport;
    }

    /**
     * find taskToNodeport for every supervisorId (node)
     * 
     * @param supervisorId
     * @return Map<Integer, NodePort>
     */
    public Map<Integer, NodePort> getTaskToPortbyNode(String supervisorId) {

	Map<Integer, NodePort> taskToPortbyNode = new HashMap<Integer, NodePort>();
	if (taskToNodeport == null) {
	    return null;
	}
	for (Entry<Integer, NodePort> entry : taskToNodeport.entrySet()) {
	    String node = entry.getValue().getNode();
	    if (node.equals(supervisorId)) {
		taskToPortbyNode.put(entry.getKey(), entry.getValue());
	    }
	}
	return taskToPortbyNode;
    }

    @Override
    public boolean equals(Object assignment) {
	if (assignment instanceof Assignment
		&& ((Assignment) assignment).getNodeHost().equals(nodeHost)
		&& ((Assignment) assignment).getTaskStartTimeSecs().equals(
			taskStartTimeSecs)
		&& ((Assignment) assignment).getMasterCodeDir().equals(
			masterCodeDir)
		&& ((Assignment) assignment).getTaskToNodeport().equals(
			taskToNodeport)) {
	    return true;
	}
	return false;
    }

    @Override
    public int hashCode() {
	return this.nodeHost.hashCode() + this.taskStartTimeSecs.hashCode()
		+ this.masterCodeDir.hashCode()
		+ this.taskToNodeport.hashCode();
    }
}