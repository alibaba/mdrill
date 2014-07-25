package com.alimama.mdrill.ui.service;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.jsp.JspWriter;

public class HeartBeat implements Runnable
{
	public Object lock=new Object();
	JspWriter out;
	public HeartBeat(JspWriter out) {
		super();
		this.out = out;
	}
	AtomicBoolean isstop=new AtomicBoolean(false);
	

	public void setIsstop(boolean isstop) {
			this.thrStop.set(isstop);
	}
	
	AtomicBoolean thrStop=new AtomicBoolean(false);

	public boolean isstop()
	{
		return thrStop.get();
	}
	
	public void stop()
	{
		this.setIsstop(true);
	    while(!this.isstop())
	    {
	    	try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
	    }
	}
	

	@Override
	public void run() {
		
		while(true)
		{
				if(this.thrStop.get())
				{
					thrStop.set(true);
					return ;
				}
				
			try {
				synchronized (this.lock) {
					if(this.out!=null)
					{
						this.out.write(" ");
						this.out.flush();
					}
				}
			} catch (Throwable e) {
			}
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
		
	}
	
}