package com.alipay.bluewhale.core.work;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import backtype.storm.daemon.Shutdownable;

import com.alipay.bluewhale.core.cluster.ClusterState;
import com.alipay.bluewhale.core.cluster.StormClusterState;
import com.alipay.bluewhale.core.daemon.NodePort;
import com.alipay.bluewhale.core.messaging.IConnection;
import com.alipay.bluewhale.core.messaging.IContext;
import com.alipay.bluewhale.core.task.common.ShutdownableDameon;
import com.alipay.bluewhale.core.task.common.TaskShutdownDameon;
import com.alipay.bluewhale.core.utils.AsyncLoopThread;

/**
 * 用于关闭启动的worker
 * 
 * @author yannian
 * 
 */
public class WorkerShutdown implements ShutdownableDameon {
	private static Logger LOG = Logger.getLogger(WorkerShutdown.class);

	private List<TaskShutdownDameon> shutdowntasks;
	private AtomicBoolean active;
	private ConcurrentHashMap<NodePort, IConnection> nodeportSocket;
	private Shutdownable virtualPortShutdown;
	private IContext mq_context;
	private AsyncLoopThread[] threads;
	private StormClusterState zkCluster;
	private ClusterState cluster_state;

	public WorkerShutdown(List<TaskShutdownDameon> _shutdowntasks,
			AtomicBoolean _active,
			ConcurrentHashMap<NodePort, IConnection> _node_port__socket,
			Shutdownable _virtual_port_shutdown, IContext _mq_context,
			AsyncLoopThread[] _threads, StormClusterState _storm_cluster_state,
			ClusterState _cluster_state

	) {
		this.shutdowntasks = _shutdowntasks;
		this.active = _active;
		this.nodeportSocket = _node_port__socket;
		this.virtualPortShutdown = _virtual_port_shutdown;
		this.mq_context = _mq_context;
		this.threads = _threads;
		this.zkCluster = _storm_cluster_state;
		this.cluster_state = _cluster_state;
	}

	@Override
	public void shutdown() {
		active.set(false);

		// 关闭tasks线程
		for (ShutdownableDameon task : shutdowntasks) {
			task.shutdown();
		}

		// 关闭发送tuple的socket链接
		for (NodePort k : nodeportSocket.keySet()) {
			IConnection value = nodeportSocket.get(k);
			value.close();
		}

		virtualPortShutdown.shutdown();
		mq_context.term();

		// 关闭worker启动的三个线程 心跳 刷新链接，发送tuple
		for (AsyncLoopThread t : threads) {
			t.interrupt();
			try {
				t.join();
			} catch (InterruptedException e) {
				LOG.error("join thread", e);
			}
		}

		// 关闭与zk的链接
		zkCluster.disconnect();
		cluster_state.close();

	}

	public void join() throws InterruptedException {
		for (TaskShutdownDameon task : shutdowntasks) {
			task.join();
		}
		for (AsyncLoopThread t : threads) {
			t.join();
		}

	}

	public boolean waiting() {
		Boolean isExistsWait = false;
		for (ShutdownableDameon task : shutdowntasks) {
			if (task.waiting()) {
				isExistsWait = true;
				break;
			}
		}
		for (AsyncLoopThread thr : threads) {
			if (thr.isSleeping()) {
				isExistsWait = true;
				break;
			}
		}
		return isExistsWait;
	}

}
