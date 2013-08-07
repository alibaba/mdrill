package com.alipay.bluewhale.core.cluster;

import java.util.List;
import java.util.UUID;
/**
 * zk 操做的初级封装接口
 * @author yannian
 *
 */
public interface ClusterState {
	public void set_ephemeral_node(String path,byte[] data);
	public void delete_node(String path);
	public void set_data(String path,byte[] data);
	public byte[] get_data(String path,boolean watch);
	public  List<String>  get_children(String path,boolean watch);
	public void mkdirs(String path);
	public void close();
	public UUID register(ClusterStateCallback callback);
	public ClusterStateCallback unregister(UUID id);
}
