package com.alimama.quanjingmonitor.topology;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.solr.common.SolrInputDocument;

import com.alimama.mdrill.adhoc.TimeCacheMap;
import com.alimama.mdrill.ui.service.MdrillService;

public class LastTimeBolt_chain2_pid implements TimeCacheMap.ExpiredCallback<BoltStatKey,BoltStatVal>{
	private static Logger LOG = Logger.getLogger(LastTimeBolt_chain2_pid.class);

    private TimeCacheMap<BoltStatKey, BoltStatVal> bufferMap=null;
	private SumReduceBolt bolt;
	private TimeCacheMap.Timeout<BoltStatKey, BoltStatVal> clean=null;
	private TimeCacheMap.Update<BoltStatKey, BoltStatVal> update=null;
	private ArrayList<SolrInputDocument> doclistBuffer=null;
	private Object doclistLock=new Object();

	
	public LastTimeBolt_chain2_pid(SumReduceBolt bolt)
	{
		this.bolt=bolt;
		this.bufferMap=new TimeCacheMap<BoltStatKey, BoltStatVal>(600, this);
    	this.doclistBuffer=new ArrayList<SolrInputDocument>(300);

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
				return LastTimeBolt_chain2_pid.this.lasttimeout>(Long)key.list[0];
			}
    	};
	}
	
	volatile long lasttimeout=0l;
	
	
	private TimeOutCheck timeoutCheck=new TimeOutCheck(60*1000l);
	long localMergerDelay=60*1000;
	
	public void update(BoltStatKey k, BoltStatVal val)
	{
		bufferMap.update(k, val, this.update);
	}
	
	public void maybeupdateAll(long logTs)
	{
		if(timeoutCheck.istimeout(logTs))
		{
			this.lasttimeout=logTs;
			bufferMap.fourceTimeout(this.clean,this.update);
			timeoutCheck.reset();
		}
		
		int ramsize=bufferMap.size();
		if(ramsize>50000)
		{
			bufferMap.fourceTimeout();
		}
	}

    private static SimpleDateFormat formatDay = new SimpleDateFormat("yyyyMMdd");
    private static SimpleDateFormat formatPartion = new SimpleDateFormat("yyyyMMdd");
    private static SimpleDateFormat formatDay2 = new SimpleDateFormat("yyyy-MM-dd");
    private static SimpleDateFormat formatHour = new SimpleDateFormat("HH:mm:ss");
    private static SimpleDateFormat formatHourStr = new SimpleDateFormat("yyyyMMddHH");
    
	@Override
	public synchronized void expire(BoltStatKey key, BoltStatVal val) {
		long ts=60000+(Long) key.list[0];
		long ts5=300000+((Long) key.list[0]/300000)*300000;
		Date d=new Date(ts);
		Date d5=new Date(ts5);

	 	SolrInputDocument doc=new SolrInputDocument();
	    doc.addField("thedate", formatDay.format(d));
	    doc.addField("mdrillPartion", formatPartion.format(d));
	    doc.addField("mdrillCmd", "add");
	    doc.addField("higo_uuid", 0);
	    doc.addField("miniute", formatDay2.format(d)+"T"+formatHour.format(d)+"Z");
	    doc.addField("miniute5", formatDay2.format(d5)+"T"+formatHour.format(d5)+"Z");
	    doc.addField("hour", formatHourStr.format(d));

	    doc.addField("logtype", this.bolt.type);
	    doc.addField("pid", key.list[1]);
	    doc.addField("groupName", key.list[2]);
	    doc.addField("namemodle",  key.list[3]);
	    doc.addField("datanum_a", val.cnt);
	    doc.addField("datanum_b", val.cntnonclear);
	    
	    boolean needCommit=false;
    	synchronized (doclistLock) {
   		 	doclistBuffer.add(doc);
   		 	needCommit=doclistBuffer.size()>200;
		}
    	
    	if(needCommit)
    	{
		 	this.commit();
    	}
	}

	@Override
	public synchronized void commit() {
		ArrayList<SolrInputDocument> buffer=null;
		synchronized (doclistLock) {
	 		buffer=doclistBuffer;
		 	doclistBuffer=new ArrayList<SolrInputDocument>(300);
		}

    	if(buffer!=null&&buffer.size()>0)
    	{
	    	try {
				LOG.info("debug=>request:"+buffer.size());
				MdrillService.insert("quanjingmointor_pid", buffer,null);
			} catch (Throwable e) {
				LOG.error("insert", e);
			}
    	}
	}

	
	public String toDebugString() {
		synchronized (doclistLock) {
			return "pid "+doclistBuffer.size()+","+bufferMap.size();
		}
	}
}
