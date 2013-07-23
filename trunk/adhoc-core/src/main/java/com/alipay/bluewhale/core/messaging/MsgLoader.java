package com.alipay.bluewhale.core.messaging;

import java.util.Set;

import org.zeromq.ZMQ.Context;

import backtype.storm.daemon.Shutdownable;

import com.alipay.bluewhale.core.callback.RunnableCallback;
import com.alipay.bluewhale.core.zilch.VirtualPort;
import com.alipay.bluewhale.core.zilch.ZeroMq;

public class MsgLoader {

	public static IContext mk_zmq_context(int num_threads, int linger,
			boolean local) {
		Context context = ZeroMq.context(num_threads);
		return new ZMQContext(context, linger, local);
	}

	public static Shutdownable launchVirtualPort(boolean local,
			IContext context, Integer port, boolean daemon,
			RunnableCallback kill_fn, Integer priority, Set<Integer> valid_ports)
			throws InterruptedException {
		String url = "ipc://" + port + ".ipc";
		if (!local) {
			url = "tcp://*:" + port;
		}

		return VirtualPort.launch_virtual_port(context.zmq_context(), url,
				daemon, kill_fn, priority, valid_ports);
	}

}
