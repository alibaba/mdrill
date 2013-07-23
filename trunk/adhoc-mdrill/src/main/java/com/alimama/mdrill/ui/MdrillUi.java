package com.alimama.mdrill.ui;
import org.mortbay.jetty.Server;  
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.webapp.WebAppContext;
public class MdrillUi { 
	public static void main(String[] args) throws Exception {
		Integer port = Integer.parseInt(args[0]);
		String warpath = args[1];
		String tmpath = args[2];
		
		System.setProperty("java.io.tmpdir", tmpath);
		
		if(args.length>3)
		{
			System.setProperty("higo.table.path", args[3]);
		}
		
	      System.setProperty("org.mortbay.jetty.Request.maxFormContentSize", "12000000");
		

		Server server = new Server();

		org.mortbay.thread.concurrent.ThreadPool threads = new org.mortbay.thread.concurrent.ThreadPool();
		threads.setCorePoolSize(512);
		threads.setMaximumPoolSize(512);
		SelectChannelConnector conn = new SelectChannelConnector();
		conn.setPort(port);
		conn.setLowResourcesConnections(10240);
		conn.setMaxIdleTime(3600000);
		conn.setLowResourceMaxIdleTime(600000);
		server.setThreadPool(threads);
		server.setConnectors(new SelectChannelConnector[] { conn });
		server.setStopAtShutdown(true);

		WebAppContext root = new WebAppContext();
		root.setContextPath("/");
		root.setWar(warpath);
		WebAppContext context = new WebAppContext();
		context.setContextPath("/higo");
		context.setWar(warpath);
		server.addHandler(context);
		server.addHandler(root);
		server.start();
		server.join();
	}
}
