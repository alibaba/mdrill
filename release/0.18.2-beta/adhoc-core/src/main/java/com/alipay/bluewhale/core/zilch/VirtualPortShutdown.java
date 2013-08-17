package com.alipay.bluewhale.core.zilch;

import org.apache.log4j.Logger;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

import backtype.storm.daemon.Shutdownable;

import com.alipay.bluewhale.core.utils.AsyncLoopThread;
/**
 * 关闭打开的虚拟端口线程
 * @author yannian
 *
 */
public class VirtualPortShutdown implements Shutdownable{
    private static Logger LOG = Logger.getLogger(VirtualPortShutdown.class);

    public VirtualPortShutdown(Context context,
	    AsyncLoopThread vthread, String url) {
	this.context = context;
	this.vthread = vthread;
	this.url = url;
    }

    Context context;
    Socket kill_socket;
    AsyncLoopThread vthread;
    String url;


    @Override
    public void shutdown() {
	Socket kill_socket = ZeroMq.socket(context, ZeroMq.push);
	ZeroMq.connect(kill_socket, url);
	VirtualPort.virtual_send(kill_socket, -1, new byte[0]);

	kill_socket.close();

	LOG.info("Waiting for virtual port at url " + url + " to die");

	try {
	    vthread.join();
	} catch (InterruptedException e) {

	}

	LOG.info("Shutdown virtual port at url: " + url);
    }
}
