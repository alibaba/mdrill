package com.alipay.bluewhale.core.messaging;

import org.zeromq.ZMQ.Socket;

import com.alipay.bluewhale.core.zilch.ZeroMq;
import com.alipay.bluewhale.core.zilch.VirtualPort;

public class ZMQConnection implements IConnection {
	private Socket socket;

	public ZMQConnection(Socket _socket) {
		socket = _socket;
	}

	@Override
	public byte[] recv() {
		return ZeroMq.recv(socket);
	}

	@Override
	public void send(int task, byte[] message) {

		VirtualPort.virtual_send(socket, task, message);
	}

	@Override
	public void close() {
		socket.close();

	}

}
