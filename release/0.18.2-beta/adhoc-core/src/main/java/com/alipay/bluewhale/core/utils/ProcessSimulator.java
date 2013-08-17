package com.alipay.bluewhale.core.utils;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.alipay.bluewhale.core.daemon.supervisor.Supervisor;
import com.alipay.bluewhale.core.work.WorkerShutdown;

public class ProcessSimulator {
    private static Logger LOG = Logger.getLogger(Supervisor.class);

    protected static Object lock = new Object();

    /**
     * skip old function name: pid-counter
     */

    protected static ConcurrentHashMap<String, WorkerShutdown> processMap = new ConcurrentHashMap<String, WorkerShutdown>();

    /**
     * Register process handler old function name: register-process
     * 
     * @param pid
     * @param shutdownable
     */
    public static void registerProcess(String pid, WorkerShutdown shutdownable) {
        processMap.put(pid, shutdownable);
    }

    /**
     * Get process handle old function name: process-handle
     * 
     * @param pid
     * @return
     */
    protected static WorkerShutdown getProcessHandle(String pid) {
        return processMap.get(pid);
    }

    /**
     * Get all process handles old function name:all-processes
     * 
     * @return
     */
    protected static Collection<WorkerShutdown> GetAllProcessHandles() {
        return processMap.values();
    }

    /**
     * Kill pid handle old function name: KillProcess
     * 
     * @param pid
     */
    public static void killProcess(String pid) {
        synchronized (lock) {
            LOG.info("Killing process " + pid);

            WorkerShutdown shutdownHandle = getProcessHandle(pid);

            processMap.remove(pid);

            if (shutdownHandle != null) {
                shutdownHandle.shutdown();
            }
        }
    }

    /**
     * kill all handle old function name: kill-all-processes
     */
    public static void killAllProcesses() {
        Set<String> pids = processMap.keySet();
        for (String pid : pids) {
            killProcess(pid);
        }

        LOG.info("Successfully kill all processes");
    }
}
