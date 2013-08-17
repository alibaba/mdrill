package com.alipay.bluewhale.core.callback.impl;

import com.alipay.bluewhale.core.callback.BaseCallback;
import com.alipay.bluewhale.core.cluster.StormStatus;
import com.alipay.bluewhale.core.daemon.StatusType;
/**
 * 将topology的状态设置为active状态
 *
 */
public class ActiveTransitionCallback extends BaseCallback {

	@Override
	public <T> Object execute(T... args) {
		
		return new StormStatus(StatusType.active);
	}

}
