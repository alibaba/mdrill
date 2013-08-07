package com.alipay.bluewhale.core.daemon.supervisor;

import java.util.concurrent.atomic.AtomicBoolean;

import com.alipay.bluewhale.core.callback.RunnableCallback;
import com.alipay.bluewhale.core.event.EventManager;

/**
 * 将synchronizeSupervisor事件添加到eventManager队列中
 */
class AsyncSynchronizeSupervisor extends RunnableCallback {

    private EventManager                     eventManager;

    private SynchronizeSupervisor synchronizeSupervisor;

    private AtomicBoolean                active;

    private Integer                          result;

    public AsyncSynchronizeSupervisor(EventManager eventManager,
            SynchronizeSupervisor synchronizeSupervisor,
            AtomicBoolean active) {
        this.eventManager = eventManager;
        this.synchronizeSupervisor = synchronizeSupervisor;
        this.active = active;
        this.result = null;
    }

    @Override
    public Object getResult() {
        return result;
    }


    @Override
    public void run() {
        eventManager.add(synchronizeSupervisor);
        if (active.get()) {
            this.result = 10;
        }else{
            this.result = -1;

        }
    }

}
