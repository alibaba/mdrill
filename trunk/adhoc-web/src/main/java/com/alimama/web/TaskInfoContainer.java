package com.alimama.web;

import java.util.List;

import com.alipay.bluewhale.core.daemon.NodePort;
import com.alipay.bluewhale.core.task.common.TaskInfo;
import com.alipay.bluewhale.core.task.error.TaskError;
import com.alipay.bluewhale.core.task.heartbeat.TaskHeartbeat;

public class TaskInfoContainer {
	private Integer taskId;
	private NodePort np;

	private String hostname;
	private Integer starttime;

	private TaskInfo taskInfo;
	private TaskHeartbeat hb;
	private List<TaskError> taskerrors;
	
	public NodePort getNp() {
		return np;
	}
	public void setNp(NodePort np) {
		this.np = np;
	}
	public String getHostname() {
		return hostname;
	}
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	public Integer getStarttime() {
		return starttime;
	}
	public void setStarttime(Integer starttime) {
		this.starttime = starttime;
	}
	public List<TaskError> getTaskerrors() {
		return taskerrors;
	}
	public void setTaskerrors(List<TaskError> taskerrors) {
		this.taskerrors = taskerrors;
	}
	public TaskInfo getTaskInfo() {
		return taskInfo;
	}
	public void setTaskInfo(TaskInfo taskInfo) {
		this.taskInfo = taskInfo;
	}
	public TaskHeartbeat getHb() {
		return hb;
	}
	public void setHb(TaskHeartbeat hb) {
		this.hb = hb;
	}
	
	public Integer getTaskId() {
		return taskId;
	}
	public void setTaskId(Integer taskId) {
		this.taskId = taskId;
	}
	
	@Override
	public String toString() {
		return "TaskInfoContainer <br> " +
				"hostname:" +hostname+"<br>"+
				"NodePort:" +np.toString()+"<br>"+
				"taskId:" +this.taskId+"<br>"+
				"starttime:" +this.starttime+"<br>"+
				"taskInfo:" + taskInfo + "<br>" +
				"hb:" + hb + "<br>" +
				"taskerrors:" + taskerrors + "<br>";
	}
	
}
