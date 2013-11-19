package com.alimama.quanjingmonitor.topology;

import java.io.Serializable;

public class AccessStatVal implements Serializable{
	private static final long serialVersionUID = 1L;
	volatile long cnt=0;
	volatile double sum=0;
	volatile double max=0;
	volatile double min=0;
	
	public AccessStatVal copy()
	{
		AccessStatVal rtn=new AccessStatVal();
		rtn.cnt=this.cnt;
		rtn.sum=this.sum;
		rtn.max=this.max;
		rtn.min=this.min;
		
		return rtn;
	}
	public void add(double rt)
	{
		this.sum+=rt;
		this.cnt++;
		this.max=this.max==0?rt:Math.max(this.max, rt);
		this.min=this.min==0?rt:Math.min(this.max, rt);
	}
	
	public void merge(AccessStatVal as)
	{
		this.sum+=as.sum;
		this.cnt+=as.cnt;
		this.max=this.max==0?as.max:Math.max(this.max, as.max);
		this.min=this.min==0?as.min:Math.min(this.max, as.min);
	}

}
