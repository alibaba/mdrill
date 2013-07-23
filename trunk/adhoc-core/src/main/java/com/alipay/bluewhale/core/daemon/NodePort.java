package com.alipay.bluewhale.core.daemon;

import java.io.Serializable;
/**
 * 标识supervisorid和端口号
 */
public class NodePort implements Serializable{

    private static final long serialVersionUID = 1L;
	private String node;
	private Integer port;
	public NodePort(String node, Integer port){
		this.node = node;
		this.port = port;	
	}
	public String getNode() {
		return node;
	}
	public void setNode(String node) {
		this.node = node;
	}
	public Integer getPort() {
		return port;
	}
	public void setPort(Integer port) {
		this.port = port;
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof NodePort && ((NodePort) obj).getNode().equals(node)
				&& ((NodePort) obj).getPort().equals(port)) {
			return true;
		}

		return false;
	}
	@Override
	public int  hashCode()
	{
		return this.node.hashCode()+this.port.hashCode();
	}
	@Override
	public String  toString()
	{
		return node+":"+port;
	}
}