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
 * inactive 与 active 状态下执行rebalance动作，将当前topology状态设置为rebalancing,并延迟delaySecs秒收执行 rebalancing的do_rebalance方法
 *
 */
public class RebalanceTransitionCallback extends BaseCallback {

	private static Logger LOG = Logger.getLogger(RebalanceTransitionCallback.class);
	
	private NimbusData data;
	private String topologyid;
	private StormStatus oldStatus;
	
	public RebalanceTransitionCallback(NimbusData data, String topologyid,StormStatus status) {
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
		LOG.info("Delaying event " + StatusType.do_rebalance.getStatus() + " for " + delaySecs + " secs for " + topologyid);
		
		data.getScheduExec().schedule(new DelayEventRunnable(data,topologyid,StatusType.do_rebalance), delaySecs, TimeUnit.SECONDS);
		
		return new StormStatus(delaySecs, StatusType.rebalancing, oldStatus);
	}


}
