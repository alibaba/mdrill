package com.alipay.bluewhale.core.work;

import com.alipay.bluewhale.core.callback.RunnableCallback;
import com.alipay.bluewhale.core.utils.StormUtils;

public class WorkerHaltRunable extends RunnableCallback {

	@Override
	public void run() {
		StormUtils.halt_process(1, "Task died");
	}

}
