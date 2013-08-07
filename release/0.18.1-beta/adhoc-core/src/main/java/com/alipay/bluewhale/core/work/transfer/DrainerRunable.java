package com.alipay.bluewhale.core.work.transfer;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import com.alipay.bluewhale.core.callback.RunnableCallback;
import com.alipay.bluewhale.core.daemon.NodePort;
import com.alipay.bluewhale.core.messaging.IConnection;

/**
 * 用于将transfer_queue缓冲的消息，发送给目标worker
 * 
 * 之前一直比较担心->worker发送的消息是在一个单线程中发送，如果某一条线程发中过程中出现问题被阻塞，整体就会被阻塞住
 * 对zeromq的测试发现，这里使用的是异步的方式 第一步直接connect一个tcp的端口，实现没有任何接收端绑定，然后发送消息，程序没有阻塞
 * 然后sleep 一秒，接收端开始bind 依然能正常接收到消息
 * 
 * @author yannian
 * 
 */
public class DrainerRunable extends RunnableCallback {
	private final static Logger LOG = Logger.getLogger(DrainerRunable.class);

	private LinkedBlockingQueue<TransferData> transferQueue;
	private ConcurrentHashMap<NodePort, IConnection> nodeportSocket;
	private ConcurrentHashMap<Integer, NodePort> taskNodeport;
	
	public DrainerRunable(LinkedBlockingQueue<TransferData> transfer_queue,
			ConcurrentHashMap<NodePort, IConnection> node_port__socket,
			ConcurrentHashMap<Integer, NodePort> task__node_port) {
		this.transferQueue = transfer_queue;
		this.nodeportSocket = node_port__socket;
		this.taskNodeport = task__node_port;
	}

	@Override
	public void run() {
			try {
				TransferData felem = transferQueue.take();
				if (felem != null) {
					ArrayList<TransferData> drainer = new ArrayList<TransferData>();
					drainer.add(felem);
					transferQueue.drainTo(drainer);
					for (TransferData o : drainer) {
						int taskId = o.getTaskid();
						byte[] tuple = o.getData();

						NodePort nodePort = taskNodeport.get(taskId);
						if (nodePort == null) {
							String errormsg = "can`t not found IConnection";
							LOG.warn("DrainerRunable warn", new Exception(
									errormsg));
							continue;
						}
						IConnection conn = nodeportSocket.get(nodePort);
						if (conn == null) {
							String errormsg = "can`t not found nodePort";
							LOG.warn("DrainerRunable warn", new Exception(
									errormsg));
							continue;
						}

						conn.send(taskId, tuple);
					}
					drainer.clear();
				}
			} catch (Exception e) {
				LOG.error("DrainerRunable send error", e);
			}
	}

	@Override
	public Object getResult() {
		return 0;
	}

}
