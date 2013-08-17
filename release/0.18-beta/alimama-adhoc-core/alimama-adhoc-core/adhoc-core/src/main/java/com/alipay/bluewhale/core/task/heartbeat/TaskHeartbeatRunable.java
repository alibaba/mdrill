package com.alipay.bluewhale.core.task.heartbeat;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import backtype.storm.Config;

import com.alipay.bluewhale.core.callback.RunnableCallback;
import com.alipay.bluewhale.core.cluster.StormClusterState;
import com.alipay.bluewhale.core.stats.BaseTaskStatsRolling;
import com.alipay.bluewhale.core.stats.Stats;
import com.alipay.bluewhale.core.task.StopCheck;
import com.alipay.bluewhale.core.utils.TimeUtils;
import com.alipay.bluewhale.core.utils.UptimeComputer;

/**
 * ÐÄÌøÖ´ÐÐ³ÌÐò
 * 
 * @author yannian
 * 
 */
public class TaskHeartbeatRunable extends RunnableCallback {
    private static Logger LOG = Logger.getLogger(TaskHeartbeatRunable.class);

	private StormClusterState zkCluster;
	private String storm_id;
	private int task_id;
	private UptimeComputer uptime;
	private BaseTaskStatsRolling task_stats;
	private AtomicBoolean active;
	private Map storm_conf;
	private static StopCheck stopcheck=new StopCheck() {
	    @Override
	    public boolean isStop() {
		return false;
	    }
	};
	
	private static Object lock=new Object();

    public static void regieterStopCheck(StopCheck check) {
	synchronized (lock) {
	    LOG.info("higolog regieterStopCheck" + check.getClass().getName());
	    TaskHeartbeatRunable.stopcheck = check;
	}
    }

	public TaskHeartbeatRunable(StormClusterState zkCluster, String _storm_id,
			int _task_id, UptimeComputer _uptime,
			BaseTaskStatsRolling _task_stats, AtomicBoolean _active,
			Map _storm_conf) {
		this.zkCluster = zkCluster;
		this.storm_id = _storm_id;
		this.task_id = _task_id;
		this.uptime = _uptime;
		this.task_stats = _task_stats;
		this.active = _active;
		this.storm_conf = _storm_conf;
	}

	@Override
	public void run() {
	    
	    synchronized (lock) {
	    if(TaskHeartbeatRunable.stopcheck.isStop())
	    {
		LOG.info("stopcheck is true");
		return ;
	    }
	    }
	    
		Integer currtime = TimeUtils.current_time_secs();
		TaskHeartbeat hb = new TaskHeartbeat(currtime, uptime.uptime(),
				Stats.render_stats(task_stats));
		LOG.debug("task hearbeat task_id=" + task_id + ",storm_id=" + storm_id
				+ "=>" + hb.toString());
		zkCluster.task_heartbeat(storm_id, task_id, hb);
	}

	@Override
	public Object getResult() {
		if (active.get()) {
			String key = Config.TASK_HEARTBEAT_FREQUENCY_SECS;
			Object time = storm_conf.get(key);
			if (time != null) {
				return Integer.parseInt(String.valueOf(time));
			} else {
				return 0;
			}
		}

		return -1;
	}

}
