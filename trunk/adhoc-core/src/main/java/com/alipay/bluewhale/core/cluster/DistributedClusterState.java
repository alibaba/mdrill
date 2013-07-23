package com.alipay.bluewhale.core.cluster;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;

import backtype.storm.Config;

import com.alipay.bluewhale.core.utils.PathUtils;
import com.alipay.bluewhale.core.zk.WatcherCallBack;
import com.alipay.bluewhale.core.zk.Zookeeper;
import com.netflix.curator.framework.CuratorFramework;

public class DistributedClusterState implements ClusterState {

	private static Logger LOG = Logger.getLogger(DistributedClusterState.class);

	private Zookeeper zkobj = null;

	private ConcurrentHashMap<UUID, ClusterStateCallback> callbacks = new ConcurrentHashMap<UUID, ClusterStateCallback>();
	private Map conf;
	private AtomicBoolean active;
	private CuratorFramework zk;
	private WatcherCallBack watcher;

	public DistributedClusterState(Map _conf) throws Exception {
		conf = _conf;
		zkobj = new Zookeeper();
		CuratorFramework _zk = mkZk();
		String path = String.valueOf(conf.get(Config.STORM_ZOOKEEPER_ROOT));
		zkobj.mkdirs(_zk, path);
		_zk.close();

		active = new AtomicBoolean(true);

		watcher = new WatcherCallBack() {
			@SuppressWarnings("unchecked")
			@Override
			public void execute(KeeperState state, EventType type, String path) {
				if (active.get()) {
					if (!(state.equals(KeeperState.SyncConnected))) {
						LOG.warn("Received event " + state + ":" + type + ":"
								+ path + " with disconnected Zookeeper.");
					}

					if (!type.equals(EventType.None)) {
						for (Entry<UUID, ClusterStateCallback> e : callbacks
								.entrySet()) {
							ClusterStateCallback fn = e.getValue();
							fn.execute(type, path);
						}
					}
				}
			}
		};
		zk = null;
		zk = mkZk(watcher);

	}

	@SuppressWarnings("unchecked")
	private CuratorFramework mkZk() throws IOException {
		return zkobj.mkClient(conf,
				(List<String>) conf.get(Config.STORM_ZOOKEEPER_SERVERS),
				conf.get(Config.STORM_ZOOKEEPER_PORT), "");
	}

	@SuppressWarnings("unchecked")
	private CuratorFramework mkZk(WatcherCallBack watcher)
			throws NumberFormatException, IOException {
		return zkobj.mkClient(conf,
				(List<String>) conf.get(Config.STORM_ZOOKEEPER_SERVERS),
				conf.get(Config.STORM_ZOOKEEPER_PORT),
				String.valueOf(conf.get(Config.STORM_ZOOKEEPER_ROOT)), watcher);
	}

	@Override
	public void close() {
		this.active.set(false);
		zk.close();
	}

	@Override
	public void delete_node(String path) {
		try {
			zkobj.deletereRcursive(zk, path);
		} catch (Exception e) {
			LOG.error("delete_node", e);
		}
	}

	@Override
	public List<String> get_children(String path, boolean watch) {
		List<String> list = null;
		try {
			list = zkobj.getChildren(zk, path, watch);
		} catch (Exception e) {
			LOG.error("get_children", e);
		}
		return list;
	}

	@Override
	public byte[] get_data(String path, boolean watch) {
		byte[] bytes = null;
		try {
			bytes = zkobj.getData(zk, path, watch);
		} catch (Exception e) {
			LOG.error("get_data", e);
		}

		return bytes;
	}

	@Override
	public void mkdirs(String path) {
		try {
			zkobj.mkdirs(zk, path);
		} catch (Exception e) {
			LOG.error("mkdirs", e);
		}

	}

	@Override
	public UUID register(ClusterStateCallback callback) {
		UUID id = UUID.randomUUID();
		this.callbacks.put(id, callback);
		return id;
	}

	@Override
	public void set_data(String path, byte[] data) {
		try {
			if (zkobj.exists(zk, path, false)) {
				zkobj.setData(zk, path, data);
			} else {
				zkobj.mkdirs(zk, PathUtils.parent_path(path));
				zkobj.createNode(zk, path, data, CreateMode.PERSISTENT);
			}

		} catch (Exception e) {
			LOG.error("set_data", e);
		}
	}

	@Override
	public void set_ephemeral_node(String path, byte[] data) {
		try {
			zkobj.mkdirs(zk, PathUtils.parent_path(path));
			if (zkobj.exists(zk, path, false)) {
				zkobj.setData(zk, path, data);
			} else {
				zkobj.createNode(zk, path, data, CreateMode.EPHEMERAL);
			}
		} catch (Exception e) {
			LOG.error("set_ephemeral_node", e);
		}
	}

	@Override
	public ClusterStateCallback unregister(UUID id) {
		return this.callbacks.remove(id);
	}
}
