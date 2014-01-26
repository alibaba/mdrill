package com.alimama.mdrillImport;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.apache.solr.common.SolrInputDocument;

import com.alimama.mdrill.adhoc.TimeCacheMap;
import com.alimama.mdrill.ui.service.MdrillService;

public class mdrillCommit implements TimeCacheMap.ExpiredCallback<BoltStatKey,BoltStatVal>{
	private static Logger LOG = Logger.getLogger(mdrillCommit.class);
	private static Timer TIMER_LIST=null;  
	private static Object TIMER_LOCK=new Object();  
    private TimeCacheMap<BoltStatKey, BoltStatVal> group=null;
	private DataParser parse;
	private TimeCacheMap.Update<BoltStatKey, BoltStatVal> update=null;
	private ArrayList<SolrInputDocument> doclist=null;
	private Object doclistLock=new Object();

    private int commitbatch=5000;
    private int buffersize=50000;
	private TimeOutCheck timeoutCheckcommit=null;
    private String confPrefix;

	private int timeout;
	private volatile long lastts[]={0,0};
	private volatile long committs=System.currentTimeMillis();

	public mdrillCommit(DataParser parse,Map conf,String confPrefix)
	{
		synchronized (TIMER_LOCK) {
			if(TIMER_LIST==null)
			{
				TIMER_LIST=new Timer(); 
			}
		}
		
    	this.confPrefix=confPrefix;

		this.parse=parse;
		this.commitbatch=Integer.parseInt(String.valueOf(conf.get(confPrefix+"-commitbatch")));
		this.buffersize=Integer.parseInt(String.valueOf(conf.get(confPrefix+"-commitbuffer")));
		this.timeout=Integer.parseInt(String.valueOf(conf.get(confPrefix+"-timeoutCommit")));
		timeoutCheckcommit=new TimeOutCheck(10*1000l);
		this.group=new TimeCacheMap<BoltStatKey, BoltStatVal>(TIMER_LIST,Math.max(this.timeout, 20), this);
    	this.doclist=new ArrayList<SolrInputDocument>(300);
		this.update=new TimeCacheMap.Update<BoltStatKey, BoltStatVal>() {
    		@Override
    		public synchronized BoltStatVal update(BoltStatKey key, BoltStatVal old,
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
	}
		

	public synchronized void updateAll(HashMap<BoltStatKey, BoltStatVal> buffer,boolean istanxpv)
	{
		this.lastts=this.getMinTs(buffer,istanxpv);
		this.group.updateAll(buffer, this.update);

		group.maybeClean();
		int ramsize=group.size();
		if(ramsize>buffersize)
		{
			LOG.info("fourceTimeout:"+this.toDebugString());
			group.fourceClean();
		}
	}
	
	private long[] getMinTs(HashMap<BoltStatKey, BoltStatVal> buffer,boolean istanxpv)
	{
		long logTs=System.currentTimeMillis();
		long logTsmax=System.currentTimeMillis();

		for(Entry<BoltStatKey, BoltStatVal> e:buffer.entrySet())
		{
			BoltStatKey key=e.getKey();
			BoltStatVal bv=e.getValue();
			
			if(istanxpv)
			{
				if(key.list.length>=3&&"mm_12229823_1573806_11174236".equals(key.list[2]))
				{
					LOG.info("yanniandebuggetMinTs:"+key.toString()+"==="+bv.toString());
				}
			}
			long ts=bv.getGroupts();
			logTs=Math.min(logTs, ts);
			logTsmax=Math.max(logTsmax, ts);

		}
		return new long[]{logTs,logTsmax};
	}
	


    private static SimpleDateFormat formatHour = new SimpleDateFormat("HH:mm:ss");
    
	@Override
	public synchronized void expire(BoltStatKey key, BoltStatVal val) {
		try{
			this.committs=val.getGroupts();
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
		}catch(Throwable e)
		{
			LOG.info("expire "+this.toDebugString(),e);

		}
	}

	@Override
	public synchronized void commit() {
		try{
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
					LOG.info(this.confPrefix+" mdrill request:"+i+"@"+buffer.size()+","+this.toDebugString());
					MdrillService.insertLocal(this.parse.getTableName(), buffer,null);
					break ;
				} catch (Throwable e) {
					LOG.error(this.confPrefix+" insert", e);
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
	    	}
    	}
		}catch(Throwable e)
		{
			LOG.info("commit "+this.toDebugString(),e);
		}
	}


	public String toDebugString() {
		try {
		return "access "+formatHour.format(new Date(lastts[0]))+"@"+formatHour.format(new Date(lastts[1]))+"@"+formatHour.format(new Date(this.committs))+",doclist="+doclist.size()+",group="+group.size();
		} catch (Throwable e) {
		}
		return "";
	}
}
