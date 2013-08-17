package com.alipay.bluewhale.core.utils;

import org.apache.log4j.Logger;


import com.alipay.bluewhale.core.callback.RunnableCallback;

/**
 * AsyncLoopThread 线程实际的执行者
 * @author yannian
 *
 */
public class AsyncLoopRunnable implements Runnable {
    private static Logger LOG = Logger.getLogger(AsyncLoopRunnable.class);

    private RunnableCallback fn;
    private RunnableCallback killfn;

    public AsyncLoopRunnable(RunnableCallback fn, RunnableCallback killfn) {
        this.fn = fn;
        this.killfn = killfn;
    }

    private boolean needSleep(Object rtn) {
        if (rtn != null) {
            long sleepTime = Long.parseLong(String.valueOf(rtn));
            if (sleepTime > 0) {
                try {
                    StormUtils.sleep_secs(sleepTime);
                } catch (InterruptedException e) {
                    LOG.error("need sleep", e);
                }
            }
            if (sleepTime < 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void run() {
    
        try {
            while (true) {
                Exception e = null;

                try {
                	if(fn==null){
                		LOG.info("fn==null,"+fn.getClass());
                		continue;
                	}
                	
                    fn.run();

                    e = fn.error();

                } catch (Exception ex) {
                    e = ex;
                }
                if (e != null) {

                    throw e;
                }
                Object rtn = fn.getResult();
                if (this.needSleep(rtn)) {
                    return;
                }

            }
        } catch (InterruptedException e) {
            LOG.info("Async loop interrupted!");
        } catch (Exception e) {
            if (e.getCause() instanceof InterruptedException) {
                LOG.info("Async loop interrupted!");
            } else {
                LOG.error("Async loop died!", e);
                killfn.execute(e);
            }
        }

    }

}
