package com.alipay.bluewhale.core.zilch;

/**
 * virtualport 发送的消息
 * 
 * @author yannian
 * 
 */
public class PacketPair {
	private int port;
	private byte[] message;

	public PacketPair(int port, byte[] message) {
		this.port = port;
		this.message = message;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public byte[] getMessage() {
		return message;
	}

	public void setMessage(byte[] message) {
		this.message = message;
	}

}
