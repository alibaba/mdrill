package com.alipay.bluewhale.core.task.common;

import java.io.Serializable;

/**
 * zk上/storm-zk-root/tasks/{topologyid}/{taskid}下存储的任务信息
 * componentId： 任务所属的componentId
 */
public class TaskInfo implements Serializable{

    private static final long serialVersionUID = 1L;
    private String componentId;


    public TaskInfo(String componentId) {
	this.componentId = componentId;
    }

    public String getComponentId() {
	return componentId;
    }

    public void setComponentId(String componentId) {
	this.componentId = componentId;
    }

    @Override
    public boolean equals(Object assignment) {
	if (assignment instanceof TaskInfo
		&& ((TaskInfo) assignment).getComponentId().equals(
			getComponentId())) {
	    return true;
	}
	return false;
    }

    @Override
    public int hashCode() {
	return this.getComponentId().hashCode();
    }
    

    @Override
    public String toString() {
	return "TaskInfo [componentId=" + componentId + "]";
    }

}
