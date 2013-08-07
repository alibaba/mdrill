package com.alipay.bluewhale.core.callback.impl;

import org.apache.log4j.Logger;

import com.alipay.bluewhale.core.callback.BaseCallback;
import com.alipay.bluewhale.core.daemon.NimbusData;
/**
 * 当期状态为killed状态， zk中remove topology方法
 *
 */
public class RemoveTransitionCallback extends BaseCallback {

	private static Logger LOG = Logger.getLogger(KillTransitionCallback.class);

	private NimbusData data;
	private String topologyid;

	public RemoveTransitionCallback(NimbusData data, String topologyid) {
		this.data = data;
		this.topologyid = topologyid;
	}

	@Override
	public <T> Object execute(T... args) {
		LOG.info("Killing topology: " + topologyid);
		data.getStormClusterState().remove_storm(topologyid);
		return null;
	}

}
