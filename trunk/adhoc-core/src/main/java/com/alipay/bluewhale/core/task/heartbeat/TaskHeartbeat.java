package com.alipay.bluewhale.core.task.heartbeat;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.alipay.bluewhale.core.stats.BaseStatsData;

/**
 * task向zk发送的心跳
 * timeSecs: 写入心跳的时间
 * uptimeSecs：任务运行的时间
 * stats： 任务的状态：emitted、transfered、complete latency/process latency、acked、failed等
 * @author yannian
 *
 */
public class TaskHeartbeat implements Serializable{
	/**
     * 
     */
    private static final long serialVersionUID = 1L;
	private Integer timeSecs;
	private Integer uptimeSecs;
	private BaseStatsData stats; // BoltTaskStats or SpoutTaskStats

	public TaskHeartbeat(int timeSecs, int uptimeSecs, BaseStatsData stats ){
		this.timeSecs = timeSecs;
		this.uptimeSecs = uptimeSecs;
		this.stats = stats;
	}
	public int getTimeSecs() {
		return timeSecs;
	}
	
	
	@Override
	    public String toString() {
	    SimpleDateFormat fmt=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String yyyymmmddd=fmt.format(new Date(1000l*timeSecs));
	        StringBuffer buff=new StringBuffer();
	        buff.append("{");
	        buff.append("timeSecs:\""+yyyymmmddd+"\"");
	        buff.append(",");
	        buff.append("uptimeSecs:\""+uptimeSecs+"\"");
	        buff.append(",");
	        buff.append("nodeHost:"+stats.getTaskStats().toString());
	        buff.append("}");
	        return buff.toString();
	    }
	

	public void setTimeSecs(int timeSecs) {
		this.timeSecs = timeSecs;
	}

	public int getUptimeSecs() {
		return uptimeSecs;
	}

	public void setUptimeSecs(int uptimeSecs) {
		this.uptimeSecs = uptimeSecs;
	}

	public BaseStatsData getStats() {
		return stats;
	}

	public void setStats(BaseStatsData stats) {
		this.stats = stats;
	}
	
	@Override
	public boolean equals(Object hb){
		if (hb instanceof  TaskHeartbeat 
		&& ((TaskHeartbeat)hb).timeSecs.equals(timeSecs)
		&& ((TaskHeartbeat)hb).uptimeSecs.equals(uptimeSecs)
		&& ((TaskHeartbeat)hb).stats.equals(stats)
		){
			return true;
		}
		return false;
	}
	
	@Override
	public int  hashCode()
	{
		return timeSecs.hashCode()+uptimeSecs.hashCode()+ stats.hashCode();
	}
}
