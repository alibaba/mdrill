package com.alipay.bluewhale.core.event;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.alipay.bluewhale.core.callback.RunnableCallback;

import backtype.storm.utils.Time;

/**
 * 事件管理，管理一个事件队列，启动一个线程，每次都队列去取出事件执行。
 */
public class EventManagerImp implements EventManager {
	AtomicInteger added = new AtomicInteger();
	AtomicInteger processed = new AtomicInteger();
	AtomicBoolean isrunning = new AtomicBoolean(true);
    Thread runningThread;
    LinkedBlockingQueue<RunnableCallback> queue     = new LinkedBlockingQueue<RunnableCallback>();

    public EventManagerImp(boolean _daemon) {
        
        Runnable runner = new EventManagerImpExecute(this);
        
        runningThread = new Thread(runner);
        runningThread.setDaemon(_daemon);
        runningThread.start();
    }

    public boolean isRunning() {
        return isrunning.get();
    }

    public RunnableCallback take() throws InterruptedException {
        RunnableCallback event = queue.take();
        return event;
    }

    public void proccessinc() {
        processed.incrementAndGet();
    }

    @Override
    public void add(RunnableCallback event_fn) {
        if (!this.isRunning()) {
            throw new RuntimeException(
                    "Cannot add events to a shutdown event manager");
        }
        added.incrementAndGet();
        queue.add(event_fn);
    }

    @Override
    public boolean waiting() {
        return Time.isThreadWaiting(runningThread)
                || (processed.get() == added.get());
    }

    @Override
    public void shutdown() {
        isrunning.set(false);
        runningThread.interrupt();
        try {
            runningThread.join();
        } catch (InterruptedException e) { }

    }
}
