package com.alipay.bluewhale.core.daemon.supervisor;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import backtype.storm.Config;

import com.alipay.bluewhale.core.callback.RunnableCallback;
import com.alipay.bluewhale.core.event.EventManager;

/**
 * 将syncProcesses事件添加到processesEventManager队列中
 */
class AsyncSyncProcesses extends RunnableCallback {
    
    private Map                      conf;

    private EventManager             processesEventManager;

    private SyncProcesses            syncProcesses;

    private AtomicBoolean         active;

    private Integer                  result;

    /**
     * @param conf
     * @param processesEventManager
     * @param syncProcesses
     * @param active
     * @param activeReadLock
     */
    public AsyncSyncProcesses(Map conf, EventManager processesEventManager,
            SyncProcesses syncProcesses, AtomicBoolean active) {
        this.processesEventManager = processesEventManager;
        this.syncProcesses = syncProcesses;
        this.active = active;
        this.conf = conf;
        this.result = null;
    }

    @Override
    public Object getResult() {
        return result;
    }


    @Override
    public void run() {
        processesEventManager.add(syncProcesses);
        if (active.get()) {
            this.result = (Integer) conf
                    .get(Config.SUPERVISOR_MONITOR_FREQUENCY_SECS);
        }else{
            this.result = -1;
        }
    }

}