package com.alipay.bluewhale.core.daemon.supervisor;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 写入到zk的supervisor的信息
 * timeSecs：启动时间
 * hostName：主机名
 * workPorts：所有的端口号
 * uptimeSecs:运行时间
 */
public class SupervisorInfo implements Serializable{

    private static final long serialVersionUID = 1L;
	private Integer timeSecs;
	private String hostName;
	private List<Integer> workPorts;
	private Integer uptimeSecs;
	public SupervisorInfo(int timeSecs, String hostName, List<Integer> workPorts, int uptimeSecs){
		this.timeSecs = timeSecs;
		this.hostName = hostName;
		this.workPorts = workPorts;
		this.uptimeSecs = uptimeSecs;
	}
	public int getTimeSecs() {
		return timeSecs;
	}
	public void setTimeSecs(int timeSecs) {
		this.timeSecs = timeSecs;
	}
	public String getHostName() {
		return hostName;
	}
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	public List<Integer> getWorkPorts() {
		return workPorts;
	}
	public void setWorkPorts(List<Integer> workPorts) {
		this.workPorts = workPorts;
	}
	public int getUptimeSecs() {
		return uptimeSecs;
	}
	public void setUptimeSecs(int uptimeSecs) {
		this.uptimeSecs = uptimeSecs;
	}
	
	@Override
	public boolean equals(Object hb){
		if (hb instanceof  SupervisorInfo 
				
				&& ((SupervisorInfo)hb).timeSecs.equals(timeSecs)
				&& ((SupervisorInfo)hb).hostName.equals(hostName)
				&& ((SupervisorInfo)hb).workPorts.equals(workPorts)
				&& ((SupervisorInfo)hb).uptimeSecs.equals(uptimeSecs)
		){
			return true;
		}
		return false;
	}
	
	@Override
	public int  hashCode()
	{
		return timeSecs.hashCode()+uptimeSecs.hashCode()+ hostName.hashCode()+workPorts.hashCode();
	}
	@Override
	public String toString(){
	    SimpleDateFormat fmt=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String yyyymmmddd=fmt.format(new Date(1000l*timeSecs));
	    return "timeSecs:"+yyyymmmddd+", "
		+ "hostName:"+hostName+", "
		+ "workPorts:"+workPorts+", "
		+ "uptimeSecs:"+uptimeSecs;
	}
	
}
