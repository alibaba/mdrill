package com.alipay.bluewhale.core.callback.impl;

import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.alipay.bluewhale.core.callback.BaseCallback;
import com.alipay.bluewhale.core.cluster.StormStatus;
import com.alipay.bluewhale.core.daemon.NimbusData;
import com.alipay.bluewhale.core.daemon.StatusType;
import com.alipay.bluewhale.core.schedule.DelayEventRunnable;

/**
 * 延迟 oldStatus.getKillTimeSecs()秒收，执行当前状态的StatusTransition.transition remove操作
 *
 */
public class DelayRemoveTransitionCallback extends BaseCallback {

	private static Logger LOG = Logger.getLogger(DelayRemoveTransitionCallback.class);
	
	private NimbusData data;
	private String topologyid;
	private StormStatus oldStatus;
	
	public DelayRemoveTransitionCallback(NimbusData data, String topologyid,
			StormStatus status) {
		this.data=data;
		this.topologyid=topologyid;
		this.oldStatus=status;
	}

	@Override
	public <T> Object execute(T... args) {
		
		LOG.info("Delaying event " + oldStatus.getStatusType().getStatus() + " for " + oldStatus.getKillTimeSecs() + " secs for " + topologyid);
		
		data.getScheduExec().schedule(new DelayEventRunnable(data,topologyid,StatusType.remove), oldStatus.getKillTimeSecs(), TimeUnit.SECONDS);
		
		return null;
	}

}
