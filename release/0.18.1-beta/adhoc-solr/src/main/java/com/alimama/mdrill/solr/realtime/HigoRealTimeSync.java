package com.alimama.mdrill.solr.realtime;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.apache.lucene.document.Document;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.core.SolrCore;
import org.apache.solr.update.DocumentBuilder;
import org.apache.solr.update.SolrIndexWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HigoRealTimeSync implements Runnable{
	  public static Logger log = LoggerFactory.getLogger(HigoRealTimeSync.class);

	private SolrCore core;
	private String day;
	
	private BinLog writelog;
	
	public HigoRealTimeSync(BinLog writelog,SolrCore core, String day) {
		this.core = core;
		this.day = day;
		this.writelog=writelog;
	}
	
AtomicBoolean isstop=new AtomicBoolean(false);
	
	public boolean isIsstop() {
		return isstop.get();
	}

	public void setIsstop(boolean isstop) {
		this.isstop.set(isstop);
	}
	
	
	private String basePath;
	private BinLog checkpointposwrite=null;
	private BinLog checkpointpos=null;
	private BinLog binlog=null;
	private int checkpointindex=0;
	private void init() throws IOException
	{
		this.basePath=core.getRealTimeDir(this.day)+"/realtime";
		checkpointposwrite=new BinLog(basePath+"/checkpointpos", 0);
		checkpointposwrite.getStore().setMaxSegmentsCount(3);
		checkpointposwrite.getStore().setMaxSegmentSize(1024);
		checkpointpos=new BinLog(basePath+"/checkpointpos", 0);
		HashMap<String,String>  params=this.readPos(checkpointpos);
		this.checkpointindex=Integer.parseInt(params.get("index"));

		this.loadCheckpoint(this.checkpointindex);

		binlog=new BinLog(this.writelog,Long.parseLong(params.get("pos")));
		
		checkpointpos.close();
		checkpointpos=null;
	}
	private static ReentrantReadWriteLock  rwlock=new ReentrantReadWriteLock();
	public static ReentrantReadWriteLock getLock()
	{
		return rwlock;
	}
	private void loadCheckpoint(long index) throws IOException
	{
		if(index<0)
		{
			return ;
		}
		
		File p=new File(basePath+"/checkpointpos_"+index);
		if(!p.exists())
		{
			return ;
		}
		

		FSDirectory fsdir = FSDirectory.open(p);
		Directory ram = core.getDirectoryFactory().open(core.getIndexDir(this.day));
		String[] list = ram.listAll();
		for (String tdel : list) {
			ram.deleteFile(tdel);
		}
		for (String fname : fsdir.listAll()) {
			fsdir.copy(ram, fname, fname);
		}
		core.getSearcher(day, false, true,false);

	}
	private void writeCheckpointPos(BinLog checkpointwrite,Long pos,int index) throws IOException
	{
		HashMap<String,String> data=new HashMap<String,String>();
		data.put("pos", String.valueOf(pos));
		data.put("index", String.valueOf(pos));
		HashMapMessage posdata=new HashMapMessage();
		posdata.setMap(data);
		checkpointwrite.append(posdata);
		checkpointwrite.flush();
	}
	
	
	private HashMap<String,String> readPos(BinLog checkpointpos) throws IOException
	{
		HashMapMessage posmap=null;
		HashMapMessage map=new HashMapMessage();
		while(true)
		{
			int res=checkpointpos.read(map);
			if(res>=0)
			{
				posmap=map;
			}
			else{
				break;
			}
		}
		
		if(posmap==null)
		{
			HashMap<String,String> data=new HashMap<String,String>();
			data.put("pos", String.valueOf(0l));
			data.put("index", String.valueOf(-1));
			posmap=new HashMapMessage();
			posmap.setMap(data);
		}
		
		return posmap.getMap();
	}
	
	
	
	private void writeCheckpoint() throws IOException
	{
		this.checkpointindex++;
		if(this.checkpointindex>10)
		{
			this.checkpointindex=0;
		}
	    FSDirectory fsdir=FSDirectory.open(new File(basePath+"/checkpointpos_"+this.checkpointindex));
	    String[] list=fsdir.listAll();
	    for(String tdel:list)
	    {
	    	fsdir.deleteFile(tdel);
	    }
	    
	    Directory d = core.getDirectoryFactory().open(core.getIndexDir(this.day));
	    for(String fname:d.listAll())
	    {
	    	d.copy(fsdir, fname, fname);
	    }
	}
	
	@Override
	public void run() {
		try {
			log.info("init start");
			this.init();
			
			long t1=System.currentTimeMillis();
			boolean needflush=false;
			
			log.info("init end");
			while(!this.isIsstop())
			{
				
				ArrayList<Document> doclist=new ArrayList<Document>();
				SolrInputDocumentMessage map=new SolrInputDocumentMessage();
				for(int i=0;i<1000;i++)
				{
					int res=binlog.read(map);
					if(res>=0)
					{
					    SolrInputDocument doc =map.getMap();
					    Document lucenedoc= DocumentBuilder.toDocument(doc,core.getSchema());
					    doclist.add(lucenedoc);
					}
					else{
						break;
					}
				}
				if(doclist.size()>0&&!this.isIsstop())
				{
					WriteLock wlock=HigoRealTimeSync.getLock().writeLock();
					wlock.lock();
					try{
						log.info("addDocuments "+doclist.size());
						SolrIndexWriter indexwriter= new SolrIndexWriter(this.day,core.getIndexDir(this.day), core.getDirectoryFactory(), false, core.getSchema(), core.getSolrConfig().mainIndexConfig, core.getDeletionPolicy());
						indexwriter.addDocuments(doclist);
						indexwriter.close();
					    needflush=true;
						log.info("flushcache "+doclist.size());
						core.getSearcher(day, true, true,false);
						
					}finally{
						wlock.unlock();
					}
					
				}else{
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {}
				}
				
			    if(needflush&&!this.isIsstop())
			    {
					long t2=System.currentTimeMillis();
					if(t2-t1>1000l*1800)
					{
						log.info("writeCheckpoint "+doclist.size());

				    	t1=t2;
				    	needflush=false;
				    	this.writeCheckpoint();
				    	this.writeCheckpointPos(checkpointposwrite, binlog.getpos(),this.checkpointindex);
					}
			    }
			}
				
		} catch (IOException e) {}
		finally{
			this.close();
		}
	}
	
	private void close()
	{
		this.setIsstop(true);
		try {
			if(checkpointposwrite!=null)
			{
				checkpointposwrite.close();
			}
			
			if(checkpointpos!=null)
			{
				checkpointpos.close();
			}
			
			
			if(binlog!=null)
			{
				binlog.close();
			}
			
		} catch (IOException e) {
		}
	}
	
}
