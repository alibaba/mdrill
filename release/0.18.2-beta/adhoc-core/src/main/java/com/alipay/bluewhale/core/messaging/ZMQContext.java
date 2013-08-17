package com.alipay.bluewhale.core.messaging;

import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

import com.alipay.bluewhale.core.zilch.ZeroMq;
import com.alipay.bluewhale.core.zilch.VirtualPort;

public class ZMQContext implements IContext, IZMQContext {
	private org.zeromq.ZMQ.Context context;
	private Socket socket;
	private int linger_ms;
	private boolean ipc;

	public ZMQContext(org.zeromq.ZMQ.Context _context, int _linger_ms,
			boolean _ipc) {
		context = _context;
		linger_ms = _linger_ms;
		ipc = _ipc;
	}

	@Override
	public IConnection bind(String topologyid, int virtual_port) {
		this.socket = ZeroMq.socket(context, ZeroMq.pull);
		this.socket = VirtualPort.virtual_bind(socket, virtual_port);
		return new ZMQConnection(socket);
	}

	@Override
	public IConnection connect(String storm_id, String host, int port) {
		String url = null;
		if (ipc) {
			url = "ipc://" + port + ".ipc";
		} else {
			url = "tcp://" + host + ":" + port;
		}
		this.socket = ZeroMq.socket(context, ZeroMq.push);
		this.socket = ZeroMq.set_linger(socket, linger_ms);
		this.socket = ZeroMq.connect(socket, url);
		return new ZMQConnection(socket);

	}

	@Override
	public void send_local_task_empty(String storm_id, int virtual_port) {

		Socket pusher = ZeroMq.socket(context, ZeroMq.push);
		pusher = VirtualPort.virtual_connect(pusher, virtual_port);
		ZeroMq.send(pusher, new byte[0]);
		pusher.close();

	};

	@Override
	public void term() {
		context.term();
	}

	@Override
	public Context zmq_context() {
		return context;
	}

}
