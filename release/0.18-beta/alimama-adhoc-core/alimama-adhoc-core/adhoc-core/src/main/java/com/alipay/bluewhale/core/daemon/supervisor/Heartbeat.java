package com.alipay.bluewhale.core.daemon.supervisor;

import java.util.List;
import java.util.Map;

import backtype.storm.Config;

import com.alipay.bluewhale.core.cluster.StormClusterState;
import com.alipay.bluewhale.core.utils.TimeUtils;
/**
 *supervisorµÄÐÄÌø
 */
class Heartbeat implements Runnable {

	private Map conf;

	private StormClusterState stormClusterState;

	private String supervisorId;

	private String myHostName;

	private int startTime;

	/**
	 * @param conf
	 * @param stormClusterState
	 * @param supervisorId
	 * @param myHostName
	 */
	public Heartbeat(Map conf, StormClusterState stormClusterState,
			String supervisorId, String myHostName, int startTimeStamp) {
		this.stormClusterState = stormClusterState;
		this.supervisorId = supervisorId;
		this.conf = conf;
		this.myHostName = myHostName;
		this.startTime = startTimeStamp;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {

		SupervisorInfo sInfo = new SupervisorInfo(
				TimeUtils.current_time_secs(), myHostName,
				(List<Integer>) conf.get(Config.SUPERVISOR_SLOTS_PORTS),
				(int) (TimeUtils.current_time_secs() - startTime));

		stormClusterState.supervisor_heartbeat(supervisorId, sInfo);
	}

}
