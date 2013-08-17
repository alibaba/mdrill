package com.alipay.bluewhale.core.daemon;

import com.alipay.bluewhale.core.callback.RunnableCallback;


/**
 * zk的目录发生改变的时候会回调这里，用以检测或者重新分配tasks
 *
 */
public class TransitionZkCallback extends RunnableCallback {

	//FIXME 暂时实现接口ArgsRunnable，暂时放在com.alipay.bluewhale.core.daemon包下
	
	private NimbusData data;
	private String topologyid;
	
	public TransitionZkCallback(NimbusData data, String topologyid) {
		this.data=data;
		this.topologyid=topologyid;
	}

	@Override
	public void run() {
		StatusTransition.transition(data, topologyid, false, StatusType.monitor);
	}
}
