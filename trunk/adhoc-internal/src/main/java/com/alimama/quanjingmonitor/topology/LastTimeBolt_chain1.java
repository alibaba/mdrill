package com.alimama.quanjingmonitor.topology;


import java.text.SimpleDateFormat;
import java.util.HashMap;

import com.alimama.mdrill.adhoc.TimeCacheMap;

public class LastTimeBolt_chain1 implements TimeCacheMap.ExpiredCallback<BoltStatKey,BoltStatVal>{
	private TimeCacheMap<BoltStatKey, BoltStatVal> bufferMap=null;
	
	private TimeCacheMap.Timeout<BoltStatKey, BoltStatVal> clean=null;
	private TimeCacheMap.Update<BoltStatKey, BoltStatVal> update=null;

	private volatile long  lasttimeout=0l;
	
	private LastTimeBolt_chain2_pid chain_pid;
	private LastTimeBolt_chain2_host chain_host;
	public LastTimeBolt_chain1(SumReduceBolt bolt,LastTimeBolt_chain2_pid chain_pid,LastTimeBolt_chain2_host chain_host)
	{
		this.chain_pid=chain_pid;
		this.chain_host=chain_host;
		this.bufferMap=new TimeCacheMap<BoltStatKey, BoltStatVal>(60, this);
		this.update=new TimeCacheMap.Update<BoltStatKey, BoltStatVal>() {
    		@Override
    		public synchronized BoltStatVal update(BoltStatKey key, BoltStatVal old,
    				BoltStatVal newval) {
    			if(old==null)
    			{
    				return newval.copy();
    			}
    			BoltStatVal rtn=old.copy();
    			rtn.cnt+=newval.cnt;
    			rtn.cntnonclear+=newval.cntnonclear;
    			return rtn;
    		}
    	};
    	
    	this.clean=new TimeCacheMap.Timeout<BoltStatKey, BoltStatVal>() {
			@Override
			public synchronized boolean timeout(BoltStatKey key, BoltStatVal val) {
				return LastTimeBolt_chain1.this.lasttimeout>(Long)key.list[0];
			}
    	};
	}
	

	@Override
	public synchronized void expire(BoltStatKey key, BoltStatVal val) {
		if(key.isPidKey)
		{
	    	this.chain_pid.update(key, val);
		}else{
	    	this.chain_host.update(key,val );

		}
	}
	@Override
	public void commit() {

		
	}
	private TimeOutCheck timeoutCheck=new TimeOutCheck(20*1000l);

	private long localMergerDelay=20*1000;
	private long lastts=0;
	public  void updateAll(HashMap<BoltStatKey, BoltStatVal> buffer,long logTs)
	{
		this.bufferMap.updateAll(buffer, this.update);
		if(timeoutCheck.istimeout())
		{
			this.lasttimeout=((System.currentTimeMillis()-localMergerDelay)/60000)*60000;
			bufferMap.fourceTimeout(this.clean,this.update);
			timeoutCheck.reset();
		}
		int ramsize=bufferMap.size();
		if(ramsize>50000)
		{
			bufferMap.fourceTimeout();
		}
		
		
		this.chain_host.maybeupdateAll(logTs);
		this.chain_pid.maybeupdateAll(logTs);
		this.lastts=logTs;
	}


    private static SimpleDateFormat formatHour = new SimpleDateFormat("HH:mm:ss");

	public String toDebugString() {
		return "LastTimeBolt_chain1 "+formatHour.format(lastts)+","+bufferMap.size()+","+this.chain_host.toDebugString()+","+this.chain_pid.toDebugString();
	}
	
}
