package com.alipay.bluewhale.core.event;

import org.apache.log4j.Logger;

import com.alipay.bluewhale.core.callback.RunnableCallback;
import com.alipay.bluewhale.core.utils.StormUtils;

public class EventManagerImpExecute implements Runnable {
    private static Logger LOG = Logger.getLogger(EventManagerImpExecute.class);

    public EventManagerImpExecute(EventManagerImp manager) {
        this.manager = manager;
    }

    EventManagerImp manager;
    Exception       error = null;

    @Override
    public void run() {
        try {
            while (manager.isRunning()) {
                RunnableCallback r = null;
                try {
                    r = manager.take();
                } catch (InterruptedException e) {
                    LOG.info("Failed to get ArgsRunable from EventManager queue");
                }

                if (r == null) {
                    continue;
                }

                r.run();
                Exception e = r.error();
                if (e != null) {
                    throw e;
                }
                manager.proccessinc();

            }
            
        } catch (InterruptedException e) {
            error = e;
            LOG.error("Event Manager interrupted", e);
        } catch (Exception e) {
            LOG.error("Error when processing event ",e);
            StormUtils.halt_process(20, "Error when processing an event");
        }
    }

}
