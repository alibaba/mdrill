package com.alimama.quanjingmonitor.mdrillImport;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.solr.common.SolrInputDocument;

import com.alimama.mdrill.adhoc.TimeCacheMap;
import com.alimama.mdrill.ui.service.MdrillService;
import com.alimama.quanjingmonitor.topology.TimeOutCheck;

public class mdrillCommit implements TimeCacheMap.ExpiredCallback<BoltStatKey,BoltStatVal>{
	private static Logger LOG = Logger.getLogger(mdrillCommit.class);

    private TimeCacheMap<BoltStatKey, BoltStatVal> group=null;
	private DataParser parse;
	private TimeCacheMap.Timeout<BoltStatKey, BoltStatVal> clean=null;
	private TimeCacheMap.Update<BoltStatKey, BoltStatVal> update=null;
	private ArrayList<SolrInputDocument> doclist=null;
	private Object doclistLock=new Object();

    private int commitbatch=5000;
    private int buffersize=50000;
	private TimeOutCheck timeoutCheck=null;
	private TimeOutCheck timeoutCheckcommit=null;

	int timeout;
	public mdrillCommit(DataParser parse,Map conf,String confPrefix)
	{
		this.parse=parse;
		this.commitbatch=Integer.parseInt(String.valueOf(conf.get(confPrefix+"-commitbatch")));
		this.buffersize=Integer.parseInt(String.valueOf(conf.get(confPrefix+"-commitbuffer")));
		this.timeout=Integer.parseInt(String.valueOf(conf.get(confPrefix+"-timeoutBolt")));
		int timeoutCache=Integer.parseInt(String.valueOf(conf.get(confPrefix+"-timeoutCommit")));
		timeoutCheck=new TimeOutCheck(this.timeout*1000l);
		timeoutCheckcommit=new TimeOutCheck(timeoutCache*1000l);
		this.group=new TimeCacheMap<BoltStatKey, BoltStatVal>(120, this);
    	this.doclist=new ArrayList<SolrInputDocument>(300);
		this.update=new TimeCacheMap.Update<BoltStatKey, BoltStatVal>() {
    		@Override
    		public BoltStatVal update(BoltStatKey key, BoltStatVal old,
    				BoltStatVal newval) {
    			if(old==null)
    			{
    				return newval;
    			}
    			BoltStatVal rtn=old;
    			rtn.merger(newval);
    			return rtn;
    		}
    	};
    	
    	this.clean=new TimeCacheMap.Timeout<BoltStatKey, BoltStatVal>() {
			@Override
			public boolean timeout(BoltStatKey key, BoltStatVal val) {
				return mdrillCommit.this.lasttimeout>key.getGroupts();
			}
    		
    	};
	}
	
	volatile long lasttimeout=0l;
	
	long localMergerDelay=60*1000;
	long lastts=0;

	public void updateAll(HashMap<BoltStatKey, BoltStatVal> buffer,long logTs)
	{
		this.group.updateAll(buffer, this.update);
		this.maybeupdateAll(logTs);
	}
	
	
	public void maybeupdateAll(long logTs)
	{
		if(timeoutCheck.istimeout(logTs))
		{
			this.lasttimeout=logTs-this.timeout;
			group.fourceTimeout(this.clean,this.update);
			timeoutCheck.reset();
		}
		
		int ramsize=group.size();
		if(ramsize>buffersize)
		{
			group.fourceTimeout();
		}
		
		this.lastts=logTs;

		
	}

    private static SimpleDateFormat formatHour = new SimpleDateFormat("HH:mm:ss");
    
	@Override
	public void expire(BoltStatKey key, BoltStatVal val) {
		
	 	SolrInputDocument doc=new SolrInputDocument();

		String[] groupnames=parse.getGroupName();
		for(int i=0;i<groupnames.length&&i<key.list.length;i++)
		{
		    doc.addField(groupnames[i], key.list[i]);
		}
		
		String[] statNames=parse.getSumName();

		for(int i=0;i<statNames.length&&i<val.list.length;i++)
		{
		    doc.addField(statNames[i], val.list[i]);
		}
	
	    boolean needCommit=false;

    	synchronized (doclistLock) {
   		 	doclist.add(doc);
   		 	needCommit=doclist.size()>this.commitbatch;
		}
    	
    	if(needCommit||timeoutCheckcommit.istimeout())
    	{
    		timeoutCheckcommit.reset();
		 	this.commit();
    	}
	}

	@Override
	public void commit() {
		ArrayList<SolrInputDocument> buffer=null;
		synchronized (doclistLock) {
	 		buffer=doclist;
		 	doclist=new ArrayList<SolrInputDocument>(300);
		}

    	if(buffer!=null&&buffer.size()>0)
    	{
	    	for(int i=0;i<100;i++)
	    	{
	    		try {
					LOG.info("mdrill request:"+buffer.size());
					MdrillService.insertLocal(this.parse.getTableName(), buffer,null);
					break ;
				} catch (Throwable e) {
					LOG.error("insert", e);
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
	    	}
    	}
	}


	public String toDebugString() {
		return "access "+formatHour.format(new Date(lastts))+",doclist="+doclist.size()+",group="+group.size();
	}
}
