package com.alipay.bluewhale.core.callback.impl;

import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.alipay.bluewhale.core.callback.BaseCallback;
import com.alipay.bluewhale.core.cluster.StormStatus;
import com.alipay.bluewhale.core.daemon.NimbusData;
import com.alipay.bluewhale.core.daemon.StatusType;
import com.alipay.bluewhale.core.schedule.DelayEventRunnable;

/**
 * 延迟 oldStatus.getDelaySecs()秒后，执行当前状态的StatusTransition.transition do_rebalance操作
 *
 */
public class DelayRebalanceTransitionCallback extends BaseCallback {

	private static Logger LOG = Logger.getLogger(DelayRebalanceTransitionCallback.class);
	
	private NimbusData data;
	private String topologyid;
	private StormStatus oldStatus;
	
	public DelayRebalanceTransitionCallback(NimbusData data, String topologyid,
			StormStatus status) {
		this.data=data;
		this.topologyid=topologyid;
		this.oldStatus=status;
	}

	@Override
	public <T> Object execute(T... args) {
		
        LOG.info("Delaying event " + oldStatus.getStatusType().getStatus() + " for " + oldStatus.getDelaySecs() + " secs for " + topologyid);
		
		data.getScheduExec().schedule(new DelayEventRunnable(data,topologyid,StatusType.do_rebalance), oldStatus.getDelaySecs(), TimeUnit.SECONDS);

		return null;
	}

}
