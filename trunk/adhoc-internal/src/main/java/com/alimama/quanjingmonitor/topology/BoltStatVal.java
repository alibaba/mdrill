package com.alimama.quanjingmonitor.topology;

import java.io.Serializable;

public class BoltStatVal implements Serializable{
	private static final long serialVersionUID = 1L;
	public volatile long cnt=0;
	public volatile long cntnonclear=0;
	
	public BoltStatVal copy()
	{
		BoltStatVal rtn=new BoltStatVal();
		rtn.cnt=this.cnt;
		rtn.cntnonclear=this.cntnonclear;
		return rtn;
	}

}
