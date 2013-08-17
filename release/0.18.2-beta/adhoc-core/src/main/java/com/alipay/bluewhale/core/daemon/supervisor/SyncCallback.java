package com.alipay.bluewhale.core.daemon.supervisor;

import com.alipay.bluewhale.core.callback.RunnableCallback;
import com.alipay.bluewhale.core.event.EventManager;

class SyncCallback extends RunnableCallback {

    private EventManager eventManager;

    private RunnableCallback cb;

    /**
     * @param cb
     * @param eventManager
     */
    public SyncCallback(RunnableCallback cb, EventManager eventManager) {
        this.eventManager = eventManager;
        this.cb = cb;
    }

    @Override
    public void run() {
        eventManager.add(cb);
    }

}