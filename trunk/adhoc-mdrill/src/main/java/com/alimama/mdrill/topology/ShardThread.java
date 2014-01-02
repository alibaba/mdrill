package com.alimama.mdrill.topology;

import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ShardThread {
	//执行表的心跳
	public ThreadPoolExecutor EXECUTE =new ThreadPoolExecutor(2, 5,1800l, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
	//用户控制hdfs的下载并发
	public ExecutorService EXECUTE_HDFS = new ThreadPoolExecutor(1,5,3600*6l, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
	public Timer TIMER = new Timer();

}
