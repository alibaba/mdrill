package com.alimama.mdrill.topology;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ShardThread {
	//执行表的心跳
	public ThreadPoolExecutor EXECUTE =new ThreadPoolExecutor(2, 7,1800l, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
	//用户控制hdfs的下载并发
	public ExecutorService EXECUTE_HDFS = new ThreadPoolExecutor(1,5,3600*6l, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
	
	public Timer[] TIMER_LIST={new Timer(),new Timer(),new Timer(),new Timer()};  

	private  AtomicInteger index=new AtomicInteger(0);
    public void schedule(TimerTask task, long delay, long period) {
    	int pos=index.incrementAndGet();
    	if(pos>10000000)
    	{
    		index.set(0);
    	}
    	int i=pos%TIMER_LIST.length;
    	TIMER_LIST[i].purge();
    	TIMER_LIST[i].schedule(task, delay, period);
    }
    
    
    public int purge() {
    	int rtn=0;
    	for(Timer t:TIMER_LIST)
    	{
    		rtn+=t.purge();
    	}
    	return rtn;
    }
    



}
