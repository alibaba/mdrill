package com.alipay.bluewhale.core.work.refresh;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import backtype.storm.Config;

import com.alipay.bluewhale.core.callback.RunnableCallback;
import com.alipay.bluewhale.core.utils.StormUtils;
import com.alipay.bluewhale.core.work.WorkCommon;

/**
 * worker的心跳发送程序
 * 
 * @author yannian
 * 
 */
public class WorkerHeartbeatRunable extends RunnableCallback {
	private static Logger LOG = Logger.getLogger(WorkCommon.class);
	private AtomicBoolean active;
	@SuppressWarnings("rawtypes")
	private Map conf;
	private String worker_id;
	private Integer port;
	private String topologyId;
	private CopyOnWriteArraySet<Integer> task_ids;
	//private Object lock = new Object();

	@SuppressWarnings("rawtypes")
	public WorkerHeartbeatRunable(Map conf, String worker_id, Integer port,
			String topologyId, CopyOnWriteArraySet<Integer> task_ids,
			AtomicBoolean active) {
		this.conf = conf;
		this.worker_id = worker_id;
		this.port = port;
		this.topologyId = topologyId;
		this.task_ids = task_ids;
		this.active = active;
	}

	@Override
	public void run() {
		//MODIFIED 没有必要synchronized
		//synchronized (lock) {
			LOG.debug("In heartbeat thread");
			if (active.get()) {
				try {
					WorkCommon.doHeartbeat(conf, worker_id, port, topologyId,
							task_ids);
				} catch (IOException e) {
					LOG.error("work_heart_beat_fn fail", e);
				}	
			}else{
				LOG.info("In heartbeat thread active is false");
			}		
		//}
	}

	@Override
	public Object getResult() {
		if (this.active.get()) {
			String key = Config.WORKER_HEARTBEAT_FREQUENCY_SECS;
			return StormUtils.parseInt(conf.get(key));
		}
		return -1;
	}
}
