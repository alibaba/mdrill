package com.alimama.mdrill.topology.utils;

public class Interval {
	 private Long hbInterval=System.currentTimeMillis();
	    public synchronized boolean heartBeatInterval()
	    {
			long nowtime = System.currentTimeMillis();
			if (nowtime - hbInterval < 1000l * 360) {
			    return false;
			}
			hbInterval=nowtime;
			return true;
	    }
	    
}
