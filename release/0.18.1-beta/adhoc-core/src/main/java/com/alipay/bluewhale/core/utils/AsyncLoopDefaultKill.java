package com.alipay.bluewhale.core.utils;

import com.alipay.bluewhale.core.callback.RunnableCallback;

public class AsyncLoopDefaultKill extends RunnableCallback {
    
	@Override
	public <T> Object execute(T... args) {
		Exception e = (Exception) args[0];
		StormUtils.halt_process(1, "Async loop died!");
		return e;
	}

    @Override
    public void run() {
        StormUtils.halt_process(1, "Async loop died!");
    }
}
