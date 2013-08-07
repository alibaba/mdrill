package com.alipay.bluewhale.core.schedule;

import com.alipay.bluewhale.core.daemon.NimbusData;
import com.alipay.bluewhale.core.daemon.StatusTransition;
import com.alipay.bluewhale.core.daemon.StatusType;

public class DelayEventRunnable implements Runnable {

	private NimbusData data;
	private String topologyid;
	private StatusType status;
	
	public DelayEventRunnable(NimbusData data, String topologyid,StatusType status) {
		this.data=data;
		this.topologyid=topologyid;
		this.status=status;
	}
	
	@Override
	public void run() {
		StatusTransition.transition(data, topologyid, false, status);
	}

}
