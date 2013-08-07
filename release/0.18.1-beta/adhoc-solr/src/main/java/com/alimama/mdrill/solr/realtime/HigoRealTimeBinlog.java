package com.alimama.mdrill.solr.realtime;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.core.SolrCore;

public class HigoRealTimeBinlog {
	private SolrCore core;
	private String day;
	
	
	AtomicBoolean isstop=new AtomicBoolean(false);
	
	public boolean isIsstop() {
		return isstop.get();
	}

	public void setIsstop(boolean isstop) {
		this.isstop.set(isstop);
	}

	public HigoRealTimeBinlog(SolrCore core, String day) throws IOException {
		this.core = core;
		this.day = day;
		this.init();
	}

	private BinLog binlog=null;
	private void init() throws IOException
	{
		String basePath=core.getRealTimeDir(this.day)+"/realtime";
		binlog=new BinLog(basePath+"/binlog",0l);
	
	}
	
	public BinLog getbinlog()
	{
		return binlog;
	}
	
	private SolrInputDocumentMessage map=new SolrInputDocumentMessage();
	private long t1=System.currentTimeMillis();

	boolean needflush=false;

	public synchronized void write(SolrInputDocument data) throws IOException
	{
		if(data!=null)
		{
			map.setMap(data);
			binlog.append(map);
			needflush=true;
			
		}else{
			try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {}
		}
		
		if(needflush&&!this.isIsstop())
		{
			long t2=System.currentTimeMillis();
			if(t2-t1>1000l)
			{
				binlog.flush();
				needflush=false;
				t1=t2;
			}
		}
	}
	

	
	public void close()
	{
		this.setIsstop(true);
		try {
			if(binlog!=null)
			{
				binlog.close();
			}
		} catch (IOException e) {}
	}
	
}
