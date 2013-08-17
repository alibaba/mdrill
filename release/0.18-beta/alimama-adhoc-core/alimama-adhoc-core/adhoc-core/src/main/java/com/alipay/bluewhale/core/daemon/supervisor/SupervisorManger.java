package com.alipay.bluewhale.core.daemon.supervisor;

import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import backtype.storm.daemon.Shutdownable;

import com.alipay.bluewhale.core.cluster.DaemonCommon;
import com.alipay.bluewhale.core.cluster.StormClusterState;
import com.alipay.bluewhale.core.cluster.StormConfig;
import com.alipay.bluewhale.core.event.EventManager;
import com.alipay.bluewhale.core.utils.PathUtils;
import com.alipay.bluewhale.core.utils.SmartThread;
/**
 *可关闭supervisor，和所有的worker 
 */
class SupervisorManger extends ShutdownWork implements Shutdownable, SupervisorDaemon, DaemonCommon {
    
    private static Logger LOG = Logger.getLogger(SupervisorManger.class);
    
   // private Supervisor              supervisor;
    
    private Map                     conf;

    private String                  supervisorId;

    private AtomicBoolean        active;

    private Vector<SmartThread>     threads;

    private EventManager            processesEventManager;

    private EventManager            eventManager;

    private StormClusterState       stormClusterState;

    private ConcurrentHashMap<String, String> workerThreadPidsAtom;

    public SupervisorManger(Map conf, String supervisorId,
    		AtomicBoolean active, Vector<SmartThread> threads,
            EventManager processesEventManager, EventManager eventManager,
            StormClusterState stormClusterState,
            ConcurrentHashMap<String, String> workerThreadPidsAtom) {
        this.conf = conf;
        this.supervisorId = supervisorId;
        this.active = active;
        this.threads = threads;
        this.processesEventManager = processesEventManager;
        this.eventManager = eventManager;
        this.stormClusterState = stormClusterState;
        this.workerThreadPidsAtom = workerThreadPidsAtom;
    }

    @Override
    public void shutdown() {
        LOG.info("Shutting down supervisor " + supervisorId);
        
        active.set(false);
        
        int size = threads.size();
        for (int i = 0; i < size; i++) {
            SmartThread thread = threads.elementAt(i);
            thread.interrupt();
            try {
                thread.join();
            } catch (InterruptedException e) {
               LOG.error(e.getMessage(), e);
            }
        }
        eventManager.shutdown();
        processesEventManager.shutdown();
        stormClusterState.disconnect();
    }

    @Override
    public void ShutdownAllWorkers() {
        LOG.info("Begin to shutdown all workers");
        String path = StormConfig.worker_root(conf);
		List<String> myWorkerIds = PathUtils.read_dir_contents(path);

        for (String workerId: myWorkerIds) {
            try {
            shutWorker(conf, supervisorId, workerId, workerThreadPidsAtom);
            }catch (Exception e) {
                String errMsg = "Failed to shutdown supervisorId:"
                        + supervisorId
                        + ",workerId:" + workerId
                        + "workerThreadPidsAtom:" + workerThreadPidsAtom
                        + "\n";
                LOG.error(errMsg, e);
                
            }
        }
    }

    @Override
    public Map getConf() {
        return conf;
    }

    @Override
    public String getId() {
        return supervisorId;
    }

    @Override
    public boolean waiting() {
        if (!active.get()) {
            return true;
        }

        Boolean bThread = true;
        int size = threads.size();
        for (int i = 0; i < size; i++) {
            if (!(Boolean) threads.elementAt(i).isSleeping()) {
                bThread = false;
                return false;
            }
        }
        boolean bManagers = true;
        if (eventManager.waiting() && processesEventManager.waiting()) {
            bManagers = false;
            return false;
        }
        return true;
    }

}
