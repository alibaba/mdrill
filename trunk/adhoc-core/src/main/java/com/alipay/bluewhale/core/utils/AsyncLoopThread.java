package com.alipay.bluewhale.core.utils;

import java.lang.Thread.UncaughtExceptionHandler;

import org.apache.log4j.Logger;

import backtype.storm.utils.Time;

import com.alipay.bluewhale.core.callback.RunnableCallback;
/**
 * 创建一个后台线程，不断的执行ArgsRunable afn的run方法
 * @author yannian
 *
 */
public class AsyncLoopThread implements SmartThread {
    private Thread thread;
    private static Logger LOG = Logger.getLogger(AsyncLoopThread.class);

    public AsyncLoopThread(RunnableCallback afn) {
        this.init(afn, false, Thread.NORM_PRIORITY, true);
    }

    public AsyncLoopThread(RunnableCallback afn, boolean daemon, int priority,
            boolean start) {
        this.init(afn, daemon, priority, start);
    }

    public AsyncLoopThread(RunnableCallback afn, boolean daemon,
            RunnableCallback kill_fn, int priority, boolean start) {
        this.init(afn, daemon, kill_fn, priority, start);
    }

    public void init(RunnableCallback afn, boolean daemon, int priority,
            boolean start) {
        RunnableCallback kill_fn = new AsyncLoopDefaultKill();
        this.init(afn, daemon, kill_fn, priority, start);
    }

    /**
     * 
     * @param afn
     * @param daemon
     * @param kill_fn
     *            (Exception e)
     * @param priority
     * @param args_fn
     * @param start
     */
    private void init(RunnableCallback afn, boolean daemon, RunnableCallback kill_fn,
            int priority, boolean start) {
        Runnable runable = new AsyncLoopRunnable(afn, kill_fn);
        thread = new Thread(runable);
        thread.setDaemon(daemon);
        thread.setPriority(priority);
        thread.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                LOG.error("uncaughtException", e);
            }
        });
        if (start) {
            thread.start();
        }
    }

    @Override
    public void start() {
        thread.start();
    }

    @Override
    public void join() throws InterruptedException {
        thread.join();
    }

    // for test
    public void join(int times) throws InterruptedException {
        thread.join(times);
    }

    @Override
    public void interrupt() {
        thread.interrupt();
    }

    @Override
    public Boolean isSleeping() {
        return Time.isThreadWaiting(thread);
    }
}
