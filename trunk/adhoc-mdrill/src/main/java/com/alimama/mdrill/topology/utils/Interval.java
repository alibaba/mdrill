package com.alimama.mdrill.topology.utils;

public class Interval {
	 private Long hbInterval=System.currentTimeMillis();
	    public synchronized boolean heartBeatInterval()
	    {
	    	return heartBeatInterval(false);
	    }
	    public synchronized boolean heartBeatInterval(boolean ishdfsmode)
	    {
	    	long timelen=ishdfsmode?1000l * 600:1000l * 360;
			long nowtime = System.currentTimeMillis();
			if (nowtime - hbInterval < timelen) {
			    return false;
			}
			hbInterval=nowtime;
			return true;
	    }
	    
	    
}
