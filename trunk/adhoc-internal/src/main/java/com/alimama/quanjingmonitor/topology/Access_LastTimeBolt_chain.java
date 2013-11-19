package com.alimama.quanjingmonitor.topology;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.apache.solr.common.SolrInputDocument;

import com.alimama.mdrill.adhoc.TimeCacheMap;
import com.alimama.mdrill.ui.service.MdrillService;

public class Access_LastTimeBolt_chain implements TimeCacheMap.ExpiredCallback<BoltStatKey,AccessStatVal>{
	private static Logger LOG = Logger.getLogger(Access_LastTimeBolt_chain.class);

    private TimeCacheMap<BoltStatKey, AccessStatVal> RamDirector_access=null;
	private AccessReduceBolt bolt;
	private TimeCacheMap.Timeout<BoltStatKey, AccessStatVal> clean=null;
	private TimeCacheMap.Update<BoltStatKey, AccessStatVal> update=null;
	private ArrayList<SolrInputDocument> doclistBuffer=null;
	private Object doclistLock=new Object();

	public Access_LastTimeBolt_chain(AccessReduceBolt bolt)
	{
		this.bolt=bolt;
		this.RamDirector_access=new TimeCacheMap<BoltStatKey, AccessStatVal>(600, this);
    	this.doclistBuffer=new ArrayList<SolrInputDocument>(300);

		this.update=new TimeCacheMap.Update<BoltStatKey, AccessStatVal>() {
    		@Override
    		public AccessStatVal update(BoltStatKey key, AccessStatVal old,
    				AccessStatVal newval) {
    			if(old==null)
    			{
    				return newval.copy();
    			}
    			AccessStatVal rtn=old.copy();

    			rtn.merge(newval);

    			return rtn;
    		}
    	};
    	
    	this.clean=new TimeCacheMap.Timeout<BoltStatKey, AccessStatVal>() {
			@Override
			public boolean timeout(BoltStatKey key, AccessStatVal val) {
				return Access_LastTimeBolt_chain.this.lasttimeout>(Long)key.list[0];
			}
    		
    	};
	}
	
	volatile long lasttimeout=0l;
	
	
	private TimeOutCheck timeoutCheck=new TimeOutCheck(60*1000l);
	long localMergerDelay=60*1000;


	long lastts=0;

	public void updateAll(HashMap<BoltStatKey, AccessStatVal> buffer,long logTs)
	{
		this.RamDirector_access.updateAll(buffer, this.update);
		this.maybeupdateAll(logTs);
	}
	
	
	public void maybeupdateAll(long logTs)
	{
		if(timeoutCheck.istimeout(logTs))
		{
			this.lasttimeout=logTs;
			RamDirector_access.fourceTimeout(this.clean,this.update);
			timeoutCheck.reset();
		}
		
		int ramsize=RamDirector_access.size();
		if(ramsize>50000)
		{
			RamDirector_access.fourceTimeout();
		}
		
		this.lastts=logTs;

		
	}

    private static SimpleDateFormat formatDay = new SimpleDateFormat("yyyyMMdd");
    private static SimpleDateFormat formatPartion = new SimpleDateFormat("yyyyMMdd");
    private static SimpleDateFormat formatDay2 = new SimpleDateFormat("yyyy-MM-dd");
    private static SimpleDateFormat formatHour = new SimpleDateFormat("HH:mm:ss");
    private static SimpleDateFormat formatHourStr = new SimpleDateFormat("yyyyMMddHH");
    
	@Override
	public void expire(BoltStatKey key, AccessStatVal val) {
		Date d=new Date(60000+(Long) key.list[0]);
	 	SolrInputDocument doc=new SolrInputDocument();
	    doc.addField("thedate", formatDay.format(d));
	    doc.addField("mdrillPartion", formatPartion.format(d));
	    doc.addField("mdrillCmd", "add");
	    doc.addField("higo_uuid", 0);
	    doc.addField("miniute", formatDay2.format(d)+"T"+formatHour.format(d)+"Z");
	    doc.addField("hour", formatHourStr.format(d));

	    doc.addField("pid", key.list[1]);
	    doc.addField("namemodle",  key.list[2]);
	    doc.addField("rtcnt", val.cnt);
	    doc.addField("rtsum", val.sum);
	    if(val.cnt>0)
	    {
	    	doc.addField("rtavg", val.sum/val.cnt);
	    }else{
	    	doc.addField("rtavg", 0d);
	    }
	    doc.addField("rtmax", val.max);
	    doc.addField("rtmin", val.min);
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
	public void commit() {
		ArrayList<SolrInputDocument> buffer=null;
		synchronized (doclistLock) {
	 		buffer=doclistBuffer;
		 	doclistBuffer=new ArrayList<SolrInputDocument>(300);
		}

    	if(buffer!=null&&buffer.size()>0)
    	{
	    	try {
				LOG.info("debug=>request:"+buffer.size());
				MdrillService.insert("quanjingmointor_access", buffer,null);
			} catch (Throwable e) {
				LOG.error("insert", e);
			}
    	}
	}


	public String toDebugString() {
		return "access "+formatHour.format(lastts)+","+doclistBuffer.size()+","+RamDirector_access.size();
	}
}
