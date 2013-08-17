package com.alipay.bluewhale.core.callback.impl;

import org.apache.log4j.Logger;

import com.alipay.bluewhale.core.callback.BaseCallback;
import com.alipay.bluewhale.core.cluster.StormStatus;
import com.alipay.bluewhale.core.daemon.NimbusData;
import com.alipay.bluewhale.core.daemon.NimbusUtils;

/**
 * topology 状态为 rebalancing的时候  执行do_rebalance，重新分配topology的任务，并设置集群状态为oldstatus
 *
 */
public class DoRebalanceTransitionCallback extends BaseCallback {

	private static Logger LOG = Logger.getLogger(DoRebalanceTransitionCallback.class);
	
	private NimbusData data;
	private String topologyid;
	private StormStatus oldStatus;
	
	public DoRebalanceTransitionCallback(NimbusData data, String topologyid,
			StormStatus status) {
		this.data=data;
		this.topologyid=topologyid;
		this.oldStatus=status;
	}

	@Override
	public <T> Object execute(T... args) {
		try {
			NimbusUtils.mkAssignments(data, topologyid, true);
		} catch (Exception e) {
			LOG.error("do-rebalance error!", e);
		}
		//FIXME Why oldStatus?
		return oldStatus.getOldStatus();
	}

}
