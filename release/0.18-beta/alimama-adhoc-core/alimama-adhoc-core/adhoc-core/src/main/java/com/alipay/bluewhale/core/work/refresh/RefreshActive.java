package com.alipay.bluewhale.core.work.refresh;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import backtype.storm.Config;

import com.alipay.bluewhale.core.callback.RunnableCallback;
import com.alipay.bluewhale.core.cluster.StormBase;
import com.alipay.bluewhale.core.cluster.StormClusterState;
import com.alipay.bluewhale.core.daemon.StatusType;

/**
 * 从zk中读取当前storm的当前状态是否为active，并更新到zkActive中
 * 
 * @author yannian
 * 
 */
public class RefreshActive extends RunnableCallback {
	private AtomicBoolean active;
	@SuppressWarnings("rawtypes")
	private Map conf;
	private StormClusterState zkCluster;
	private String topologyId;
	private AtomicBoolean zkActive;

	// private Object lock = new Object();

	@SuppressWarnings("rawtypes")
	public RefreshActive(AtomicBoolean active, Map conf,
			StormClusterState zkCluster, String topologyId,
			AtomicBoolean zkActive) {
		this.active = active;
		this.conf = conf;
		this.zkCluster = zkCluster;
		this.topologyId = topologyId;
		this.zkActive = zkActive;

	}

	@Override
	public void run() {
		// zkActive已经是线程安全
		// synchronized (lock) {
		StormBase base = zkCluster.storm_base(topologyId, this);
		if (base == null) {
			zkActive.set(false);
		} else {
			StatusType type = base.getStatus().getStatusType();
			boolean isActive = type.equals(StatusType.active);
			zkActive.set(isActive);
		}

		// }

	}

	@Override
	public Object getResult() {
		if (active.get()) {
			String key = Config.TASK_REFRESH_POLL_SECS;
			return conf.get(key);
		}
		return -1;
	}
}
