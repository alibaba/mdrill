package com.alipay.bluewhale.core.event;

import com.alipay.bluewhale.core.callback.RunnableCallback;


public interface EventManager {
	public void add(RunnableCallback event_fn);
	public boolean waiting();
	public void shutdown();
}
