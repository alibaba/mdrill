package com.alipay.bluewhale.core.zilch;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

import backtype.storm.daemon.Shutdownable;

import com.alipay.bluewhale.core.callback.RunnableCallback;
import com.alipay.bluewhale.core.utils.AsyncLoopThread;

/**
 * zeromq 虚拟端口的实现，用于worker接收消息，然后向本地task分发消息这个逻辑中 在这里会创建一个线程，不断的接收消息并分发
 * 
 * @author yannian
 * 
 */
public class VirtualPort {

	private static Logger LOG = Logger.getLogger(VirtualPort.class);

	public static byte[] mk_packet(Integer virtual_port, byte[] message) {
		ByteBuffer buff = ByteBuffer.allocate((Integer.SIZE / 8)
				+ message.length);
		buff.putInt(virtual_port);
		buff.put(message);
		byte[] rtn = buff.array();
		return rtn;
	}

	public static PacketPair parse_packet(byte[] packet) {
		ByteBuffer buff = ByteBuffer.wrap(packet);
		Integer port = buff.getInt();
		byte[] message = new byte[buff.array().length - (Integer.SIZE / 8)];
		buff.get(message);
		PacketPair pair = new PacketPair(port,message);
		return pair;
	}

	public static String virtual_url(int port) {
		return "inproc://" + port;
	}

	public static Socket get_virtual_socket(Context context,
			Map<Integer, Socket> mapping_atom, Integer port, String debugurl) {
		synchronized (mapping_atom) {
			if (!mapping_atom.containsKey(port)) {
				for(int i=0;i<62;i++) {
					try {
						LOG.info("Connecting to virtual port " + port
								+ ",bind=" + debugurl);

						Socket socket = ZeroMq.socket(context, ZeroMq.push);
						String vurl = virtual_url(port);
						socket = ZeroMq.connect(socket, vurl);
						mapping_atom.put(port, socket);
						break;
					} catch (org.zeromq.ZMQException e) {
						LOG.info("Connecting error "+i,e);
						if(i>=60)
						{
							throw e;
						}
						try {
							Thread.sleep(1000l*10);
						} catch (InterruptedException e1) {
						}					}
				}
			}

			return mapping_atom.get(port);
		}
	}

	public static Socket get_virtual_socket(Context context,
			Map<Integer, Socket> mapping_atom, Integer port) {
		return get_virtual_socket(context, mapping_atom, port, "");
	}

	public static void close_virtual_sockets(Map<Integer, Socket> mapping_atom) {
		synchronized (mapping_atom) {
			for (Entry<Integer, Socket> entry : mapping_atom.entrySet()) {
				entry.getValue().close();
			}
			mapping_atom.clear();
		}
	}

	public static Socket virtual_send(Socket socket, Integer virtual_port,
			byte[] message, int flags) {
		return ZeroMq.send(socket, mk_packet(virtual_port, message), flags);
	}

	public static Socket virtual_send(Socket socket, int virtual_port,
			byte[] message) {
		return virtual_send(socket, virtual_port, message, ZMQ.NOBLOCK);
	}

	public static Shutdownable launch_virtual_port(Context context, String url,
			boolean daemon, RunnableCallback kill_fn, Integer priority,
			Integer[] valid_ports) {
		HashSet<Integer> sets = new HashSet<Integer>();
		for (Integer i : valid_ports) {
			sets.add(i);
		}
		return launch_virtual_port(context, url, daemon, kill_fn, priority,
				sets);
	}

	public static Shutdownable launch_virtual_port(Context context, String url,
			boolean daemon, RunnableCallback kill_fn, Integer priority,
			Set<Integer> valid_ports) {

		Socket socket = ZeroMq.socket(context, ZeroMq.pull);
		for(int i=0;i<605;i++)
		{
			try{
			ZeroMq.bind(socket, url);
			LOG.info("bind:"+url);
			break;
			}catch(org.zeromq.ZMQException e)
			{
				LOG.info("zeromqBind error"+i,e);
				if(i>=600)
				{
					throw e;
				}
				try {
					Thread.sleep(1000l*3);
				} catch (InterruptedException e1) {
				}
			}
		}
		Map<Integer, Socket> virtual_mapping = new HashMap<Integer, ZMQ.Socket>();

		RunnableCallback loop_fn = new VirtualPortDispatch(context, socket,
				virtual_mapping, url, valid_ports);

		AsyncLoopThread vthread = new AsyncLoopThread(loop_fn, daemon, kill_fn,
				priority, true);

		return new VirtualPortShutdown(context, vthread, url);
	}

	public static Socket virtual_bind(Socket socket, int virtual_port) {
		return ZeroMq.bind(socket, virtual_url(virtual_port));
	}

	public static Socket virtual_connect(Socket socket, Integer virtual_port) {
		return ZeroMq.connect(socket, virtual_url(virtual_port));
	}
}
