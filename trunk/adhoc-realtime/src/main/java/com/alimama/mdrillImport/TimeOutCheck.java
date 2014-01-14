package com.alimama.mdrillImport;

public class TimeOutCheck {
	private long lasttime=System.currentTimeMillis();
	private long localMergerDelay=20*1000l;
	public TimeOutCheck(long localMergerDelay) {
		this.localMergerDelay = localMergerDelay;
	}

	public boolean istimeout()
	{
		long time=System.currentTimeMillis();
    	if((lasttime+localMergerDelay)<=time)
		{
			return true ;
		}
    	
    	return false;
	}
	
	long lastts=0;
	boolean isChangeTs=false;
	public boolean istimeout(long ts)
	{
		if(this.isChangeTs)
		{
			long time=System.currentTimeMillis();
	    	if((lasttime+localMergerDelay)<=time)
			{
				this.isChangeTs=false;
				return true ;
			}
	    	
	    	return false;
		}
		
		if(ts>this.lastts)
		{
			this.lastts=ts;
			this.isChangeTs=true;
			this.reset();
		}
		
		return false;
	}
	
	public void reset()
	{
		lasttime=System.currentTimeMillis();
	}

}
