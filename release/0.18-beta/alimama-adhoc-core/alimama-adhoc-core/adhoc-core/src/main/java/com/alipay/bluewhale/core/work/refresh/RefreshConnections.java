package com.alipay.bluewhale.core.work.refresh;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import backtype.storm.Config;

import com.alipay.bluewhale.core.callback.RunnableCallback;
import com.alipay.bluewhale.core.cluster.StormClusterState;
import com.alipay.bluewhale.core.daemon.NodePort;
import com.alipay.bluewhale.core.messaging.IConnection;
import com.alipay.bluewhale.core.messaging.IContext;
import com.alipay.bluewhale.core.task.common.Assignment;

/**
 * 更新当前worker到其他worker的zeromq链接，经常发生在其他worker的分配发生改变，清除与重新创建worker与其他worker的链接
 * 
 * @author yannian
 * 
 */
public class RefreshConnections extends RunnableCallback {
	private static Logger LOG = Logger.getLogger(RefreshConnections.class);
	private AtomicBoolean active;
	@SuppressWarnings("rawtypes")
	private Map conf;
	private StormClusterState zkCluster;
	private String topologyId;
	private Set<Integer> outboundTasks;
	private ConcurrentHashMap<NodePort, IConnection> nodeportSocket;
	private IContext mqContext;
	private ConcurrentHashMap<Integer, NodePort> taskNodeport;

	// private ReentrantReadWriteLock endpoint_socket_lock;

	@SuppressWarnings("rawtypes")
	public RefreshConnections(AtomicBoolean active, Map conf,
			StormClusterState zkCluster, String topologyId,
			Set<Integer> outbound_tasks,
			ConcurrentHashMap<NodePort, IConnection> node_port__socket,
			IContext mq_context,
			ConcurrentHashMap<Integer, NodePort> task__node_port) {
		this.active = active;
		this.conf = conf;
		this.zkCluster = zkCluster;
		this.topologyId = topologyId;
		this.outboundTasks = outbound_tasks;
		this.nodeportSocket = node_port__socket;
		this.mqContext = mq_context;
		this.taskNodeport = task__node_port;
		// this.endpoint_socket_lock = endpoint_socket_lock;
	}

	@Override
	public void run() {
		try {
			//
			//
			// endpoint_socket_lock.writeLock().lock();
			// TODO 是否有必要加锁？局部变量线程安全,taskNodeport本身已经线程安全
			Assignment assignment = zkCluster.assignment_info(topologyId, this);
			Map<Integer, NodePort> my_assignment = new HashMap<Integer, NodePort>();
			Map<Integer, NodePort> taskNodeportAll = null;
			Map<String, String> node = null;
			if (assignment != null) {
				taskNodeportAll = assignment.getTaskToNodeport();
				node = assignment.getNodeHost();
			}

			// 仅保留在outbound_tasks有的task(有后续接收的)
			Set<NodePort> need_connections = new HashSet<NodePort>();
			if (taskNodeportAll != null && outboundTasks != null) {
				for (Entry<Integer, NodePort> mm : taskNodeportAll.entrySet()) {
					int taks_id = mm.getKey();
					if (outboundTasks.contains(taks_id)) {
						my_assignment.put(taks_id, mm.getValue());
						need_connections.add(mm.getValue());
					}
				}
			}
			taskNodeport.putAll(my_assignment);

			// 计算哪那些是need_connections，哪那些是新链接，哪那些是应该删除的链接
			Set<NodePort> current_connections = nodeportSocket.keySet();
			Set<NodePort> new_connections = new HashSet<NodePort>();
			Set<NodePort> remove_connections = new HashSet<NodePort>();
			for (NodePort node_port : need_connections) {
				if (!current_connections.contains(node_port)) {
					new_connections.add(node_port);
				}
			}

			for (NodePort node_port : current_connections) {
				if (!need_connections.contains(node_port)) {
					remove_connections.add(node_port);
				}
			}

			// 创建新的链接，然后将连接对象添加到node_port__socket里
			if (node != null) {
				for (NodePort nodePort : new_connections) {
					String host = node.get(nodePort.getNode());
					int port = nodePort.getPort();
					IConnection conn = mqContext
							.connect(topologyId, host, port);
					nodeportSocket.put(nodePort, conn);
				}
			}
			// 关闭应该关闭的链接
			for (NodePort node_port : remove_connections) {
				nodeportSocket.remove(node_port).close();
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		// finally {
		// endpoint_socket_lock.writeLock().unlock();
		// }

	}

	@Override
	public Object getResult() {
		if (active.get()) {
			return conf.get(Config.TASK_REFRESH_POLL_SECS);
		}
		return -1;
	}

}
