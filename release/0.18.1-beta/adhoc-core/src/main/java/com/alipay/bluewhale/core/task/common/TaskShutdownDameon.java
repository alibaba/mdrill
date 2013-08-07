package com.alipay.bluewhale.core.task.common;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import backtype.storm.spout.ISpout;
import backtype.storm.task.IBolt;

import com.alipay.bluewhale.core.cluster.StormClusterState;
import com.alipay.bluewhale.core.messaging.IConnection;
import com.alipay.bluewhale.core.messaging.IContext;
import com.alipay.bluewhale.core.utils.AsyncLoopThread;

/**
 * 用此类来关闭一个task线程
 * 
 * @author yannian
 * 
 */
public class TaskShutdownDameon implements ShutdownableDameon {
	private static Logger LOG = Logger.getLogger(TaskShutdownDameon.class);

	private AtomicBoolean active;
	private String storm_id;
	private Integer task_id;
	private IContext mq_context;
	private AsyncLoopThread[] all_threads;
	private StormClusterState zkCluster;
	private IConnection puller;
	private Object task_obj;
	private AsyncLoopThread heartbeat_thread;

	public TaskShutdownDameon(AtomicBoolean active, String storm_id,
			Integer task_id, IContext mq_context,
			AsyncLoopThread[] all_threads, StormClusterState zkCluster,
			IConnection puller, Object task_obj,
			AsyncLoopThread heartbeat_thread) {
		this.active = active;
		this.storm_id = storm_id;
		this.task_id = task_id;
		this.mq_context = mq_context;
		this.all_threads = all_threads;
		this.zkCluster = zkCluster;
		this.puller = puller;
		this.task_obj = task_obj;
		this.heartbeat_thread = heartbeat_thread;

	}

	@Override
	public void shutdown() {
		LOG.info("Shutting down task " + storm_id + ":" + task_id);
		active.set(Boolean.FALSE);
		mq_context.send_local_task_empty(storm_id, task_id);
		for (AsyncLoopThread thr : all_threads) {
			thr.interrupt();
			try {
				thr.join();
			} catch (InterruptedException e) {
			}
		}
		zkCluster.remove_task_heartbeat(storm_id, task_id);
		zkCluster.disconnect();
		puller.close();
		closeComponent(task_obj);

		LOG.info("Shut down task " + storm_id + ":" + task_id);

	}

	public void join() throws InterruptedException {
		for (AsyncLoopThread t : all_threads) {
			t.join();
		}
	}

	private void closeComponent(Object _task_obj) {
		if (_task_obj instanceof IBolt) {
			((IBolt) _task_obj).cleanup();
		}

		if (_task_obj instanceof ISpout) {
			((ISpout) _task_obj).close();
		}
	}

	@Override
	public boolean waiting() {
		return heartbeat_thread.isSleeping();
	}

}
