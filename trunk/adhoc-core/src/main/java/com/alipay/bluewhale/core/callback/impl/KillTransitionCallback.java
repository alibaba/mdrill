package com.alipay.bluewhale.core.callback.impl;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import backtype.storm.Config;

import com.alipay.bluewhale.core.callback.BaseCallback;
import com.alipay.bluewhale.core.cluster.StormStatus;
import com.alipay.bluewhale.core.daemon.NimbusData;
import com.alipay.bluewhale.core.daemon.NimbusUtils;
import com.alipay.bluewhale.core.daemon.StatusType;
import com.alipay.bluewhale.core.schedule.DelayEventRunnable;

/**
 * 将topology状态设置为killed，延迟后执行remove动作
 *
 */
public class KillTransitionCallback extends BaseCallback {

	private static Logger LOG = Logger.getLogger(KillTransitionCallback.class);
	
	private NimbusData data;
	private String topologyid;
	private StormStatus oldStatus;
	public KillTransitionCallback(NimbusData data, String topologyid, StormStatus status) {
		this.data=data;
		this.topologyid=topologyid;
		this.oldStatus=status;
	}

	@Override
	public <T> Object execute(T... args) {
		int delaySecs;
		if(args==null||args.length==0){
			Map<?,?> map=NimbusUtils.readStormConf(data.getConf(), topologyid);
			delaySecs=(Integer)map.get(Config.TOPOLOGY_MESSAGE_TIMEOUT_SECS);
		}else{
			delaySecs=(Integer) args[0];
		}
		LOG.info("Delaying event " + StatusType.remove.getStatus() + " for " + delaySecs + " secs for " + topologyid);
		
		data.getScheduExec().schedule(new DelayEventRunnable(data,topologyid,StatusType.remove), delaySecs, TimeUnit.SECONDS);
		
		return new StormStatus(delaySecs, StatusType.killed, oldStatus);
	}


}
