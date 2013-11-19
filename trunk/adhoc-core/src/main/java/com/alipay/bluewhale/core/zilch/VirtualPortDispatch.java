package com.alipay.bluewhale.core.zilch;

import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

import com.alipay.bluewhale.core.callback.RunnableCallback;

/**
 * 消息的分发
 * 
 * @author yannian
 * 
 */
public class VirtualPortDispatch extends RunnableCallback {
	private final static Logger LOG = Logger
			.getLogger(VirtualPortDispatch.class);

	private Context context;
	private Socket socket;
	private Map<Integer, Socket> virtual_mapping;
	private String url;
	private Set<Integer> valid_ports = null;

	public VirtualPortDispatch(Context context, Socket socket,
			Map<Integer, Socket> virtual_mapping, String url,
			Set<Integer> valid_ports) {
		this.context = context;
		this.socket = socket;
		this.virtual_mapping = virtual_mapping;
		this.url = url;
		this.valid_ports = valid_ports;
	}

	@Override
	public void run() {

		if (socket != null) {
			byte[] data = ZeroMq.recv(socket);

			PacketPair packet = VirtualPort.parse_packet(data);
			if (packet.getPort() == -1) {
				LOG.info("Virtual port " + url + " received shutdown notice");
				VirtualPort.close_virtual_sockets(virtual_mapping);
				socket.close();
				socket = null;
			} else {
				if (valid_ports == null || valid_ports.contains(packet.getPort())) {

					Socket virtual_socket = VirtualPort.get_virtual_socket(
							context, virtual_mapping, packet.getPort(), url);
					ZeroMq.send(virtual_socket, packet.getMessage());

				} else {
					//LOG.info("Received invalid message directed at port "
					//		+ packet.getPort() + ". Dropping...");
				}
			}
		}

	}

	@Override
	public Object getResult() {
		return socket == null ? -1 : 0;
	}
}
