package com.alimama.mdrill.topology;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;
import org.apache.lucene.store.LinkFSDirectory;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.JettySolrRunner;
import org.apache.solr.core.SolrCore;
import org.apache.solr.core.SolrResourceLoader;
import org.apache.solr.request.join.HigoJoinUtils;

import backtype.storm.task.OutputCollector;

import com.alipay.bluewhale.core.cluster.SolrInfo;
import com.alipay.bluewhale.core.cluster.SolrInfo.ShardCount;
import com.alimama.mdrill.solr.realtime.realtime.RealTimeDirectoryParams;
import com.alimama.mdrill.topology.utils.Interval;
import com.alimama.mdrill.topology.utils.SolrStartJettyExcetionCollection;
import com.alimama.mdrill.utils.HadoopUtil;
import com.alimama.mdrill.utils.IndexUtils;
import com.alipay.bluewhale.core.cluster.ShardsState;
import com.alipay.bluewhale.core.task.StopCheck;
import com.alipay.bluewhale.core.utils.NetWorkUtils;
import com.alipay.bluewhale.core.utils.StormUtils;
import com.alipay.bluewhale.core.work.Worker;

public class SolrStartJetty implements StopCheck,SolrStartInterface{
    private static Logger LOG = Logger.getLogger(SolrStartJetty.class);
    private Configuration conf;
    private String hdfsSolrhome;
    private Integer portbase;
    private String singleStorePath;
    private int taskIndex; 
    private String tablename;
    private Integer taskid;
    private OutputCollector collector;
    private boolean isMergeServer=false;
	private AtomicBoolean neetRestart=new AtomicBoolean(false);
	
	ShardThread EXECUTE =null;

	@Override
	public void setExecute(ShardThread EXECUTE) {
		this.EXECUTE=EXECUTE;
		
	}
	
	private AtomicBoolean initSolrConfig=new AtomicBoolean(false);


    public void setRestart()
    {
    	this.neetRestart.set(true);
    }

    public void setMergeServer(boolean isMergeServer) {
        this.isMergeServer = isMergeServer;
    }
	BoltParams params;

    public SolrStartJetty(BoltParams params,OutputCollector collector,Configuration conf, String solrhome, String diskList, Integer portbase,int taskIndex, String tblName, Integer taskid,Integer partions) throws IOException {
    	this.params=params;
    	this.collector=collector;
		this.conf = conf;
		this.fs = FileSystem.get(this.conf);
		this.lfs = FileSystem.getLocal(this.conf);
		this.hdfsSolrhome = solrhome;
		this.portbase = portbase;
		this.taskIndex=taskIndex;
		this.tablename=tblName;
		this.taskid=taskid;
		this.singleStorePath=IndexUtils.getPath(diskList, taskIndex,0,this.lfs);
		HigoJoinUtils.setLocalStorePath(diskList.split(","));
		RealTimeDirectoryParams.setDiskDirList(taskIndex, diskList);
		SolrCore.setSearchCacheSize(partions);
		SolrCore.setBinglogType(params.binlog);
		LOG.info("higolog init table:"+this.tablename+",port:"+this.portbase+",taskIndex:"+this.taskIndex+",taskId:"+this.taskid+",storepath:"+this.singleStorePath);
		this.init();
		this.validate();
    }
    

    public void setConfigDir(String dir)
    {
    	HadoopUtil.setHdfsConfDir(dir);
    	LinkFSDirectory.setHdfsConfDir(dir);
    	HigoJoinUtils.setHdfsConfDir(dir);
    }
    
    private FileSystem fs  ;
    private FileSystem lfs ;
	
    private Path hdfsSolrpath ;
    private Path localSolrPath ;
    private Path localTablePath ;
    private Path localTmpPath ;
    
    private void init() throws IOException
    {
    	this.hdfsSolrpath = new Path(hdfsSolrhome, "solr");
    	this.localSolrPath =this.getLocalPath("solr");
    	this.localTablePath =this.getLocalPath("tablelist");
    	this.localTmpPath =this.getLocalPath("higotmp");
    	
    }
    
    private Path getLocalTmpPath() {
		 return this.getLocalTmpPath(this.tablename);
	}
    
    public Path getLocalTmpPath(String tablename) {
		 return new Path(this.localTmpPath,tablename);
	}

	public Path getLocalTablePath(String tablename) {
        return new Path(this.localTablePath,tablename);
    }

    private Path getLocalPath(String type) throws IOException
    {
    	Path localpath = new Path(new Path(this.singleStorePath, "higo"), tablename + "/" + this.params.compname+"_"+this.taskIndex);
    	return  new Path(localpath, type);
    }
    
    private void validate() throws IOException
    {
    	try{
			if(!fs.exists(hdfsSolrpath))
			{
			    throw new RuntimeException("can`t not found path "+hdfsSolrpath);
			}
    	}catch(IOException e)
    	{
    		LOG.info("igno hadoop error",e);
    	}
    }
    
	private synchronized boolean sync(boolean isforce)  {
		try {
			this.validate();
			boolean needRestart = false;
			if (!lfs.exists(new Path(localSolrPath, "complete"))) {
				boolean iscopy=IndexUtils.copyToLocal(fs, lfs, hdfsSolrpath, localSolrPath,this.getLocalTmpPath());
				if(iscopy)
				{
					lfs.mkdirs(new Path(localSolrPath, "complete"));
					needRestart = true;
				}
			}
			
			Path confpath=new Path(localSolrPath,"conf");
			if (isforce||!lfs.exists(new Path(confpath, "complete"))) {
				boolean iscopy=IndexUtils.copyToLocal(fs, lfs, new Path(hdfsSolrpath,"conf"),confpath,this.getLocalTmpPath());
				if(iscopy)
				{
					needRestart = true;
					lfs.mkdirs(new Path(confpath, "complete"));
				}
			}
			initSolrConfig.set(true);
			return needRestart;
		} catch (Throwable e) {
			LOG.error(StormUtils.stringify_error(e));
			initSolrConfig.set(true);
			return false;
		}
	}
    
    public void start() throws Exception {
	    statcollect.setStat(ShardsState.INIT);
    	this.zkHeatbeat();
   	
		TimerTask task=new TimerTask() {
			@Override
			public void run() {
				try {
					synchronized (isInit) {
						 if(!isInit.get())
						 {
							 statcollect.setLastTime(System.currentTimeMillis());
						    LOG.info("higolog table begin:"+SolrStartJetty.this.tablename);
					    	rsyncExecute exe=new rsyncExecute(SolrStartJetty.this,true);
					    	EXECUTE.EXECUTE_HDFS.submit(exe);
					    	while(!exe.isfinish()&&!statcollect.isTimeout(1200l*1000))
					    	{
					    		Thread.sleep(1000l);
					    	}
					    	
					    	while(!initSolrConfig.get())
			    	    	{
			    	    		Thread.sleep(1000l);
			    	    	}
					    	
					    	if(statcollect.isTimeout(1200l*1000))
					    	{
								statcollect.setLastTime(System.currentTimeMillis());
					    	}
					    	SolrStartJetty.this.startJetty();
							isInit.set(true);
							LOG.info("higolog table end:" + SolrStartJetty.this.tablename);
							hb();
							return ;
						 }
					}
					

					 if (!hbInterval.heartBeatInterval()) {
							return;
						}
						hb();
				  


					
				} catch (Throwable ex) {
					LOG.error("hb",ex);
					try{
						SolrStartJetty.this.runException(ex);
					}catch (Throwable eee) {}
				}				
			}
			
		};
		taskList.add(task);
		EXECUTE.schedule(task, 100l, 10000l);
    }
    
	ArrayList<TimerTask> taskList=new ArrayList<TimerTask>();

    
    AtomicBoolean isInit=new AtomicBoolean(false);

    hbExecute lasthb=null;
	private synchronized void hb()
	{
		
		if(!initSolrConfig.get())
		{
			return ;
		}
		if(lasthb==null||lasthb.isfinish())
		{
			hbExecute exec=new hbExecute(this);
	    	EXECUTE.EXECUTE.submit(exec);
	    	lasthb=exec;
		}
		
	}


    private SolrStartJettyExcetionCollection errorCollect=new SolrStartJettyExcetionCollection();
    private SolrStartJettyStat statcollect=new SolrStartJettyStat();

    
	public boolean isService()
	{
		return statcollect.getStat().equals(ShardsState.SERVICE);
	}
    
    private void runException(Throwable er)
    {
    	LOG.error(StormUtils.stringify_error(er));
		statcollect.setStat(ShardsState.INITFAIL);
		this.zkHeatbeat();
		errorCollect.setException(er);
    }
    
    public void stop() throws Exception {
    	for(TimerTask t:taskList){
    		t.cancel();
    	}
    	EXECUTE.purge();
    	
    	
		statcollect.setStat(ShardsState.UINIT);
	    this.zkHeatbeat();
	    this.stopJetty();
    }
        
    public Boolean isTimeout()
    {
		return isInit.get()&&statcollect.isTimeout(1000l*60*120);
    }
    
    private Interval hbInterval=new Interval();;
    
    public void checkError()
    {
		errorCollect.checkException();
    }
    public void heartbeat() throws Exception {
		LOG.info("higolog heartbeat called:" + this.tablename);

    	boolean needhb=false;
		if (!neetRestart.get()) {
		    if (!statcollect.getStat().equals(ShardsState.SERVICE)) {
		    	this.startJetty();
		    }else{
		    	needhb=true;
		    }
		}else{
			statcollect.setLastTime(System.currentTimeMillis());

		    neetRestart.set(false);
		    this.stopJetty();

		    rsyncExecute exe=new rsyncExecute(this,false);
		    EXECUTE.EXECUTE_HDFS.submit(exe);
	    	while(!exe.isfinish()&&!statcollect.isTimeout(600l*1000))
	    	{
	    		Thread.sleep(1000l);
	    	}
	    	if(statcollect.isTimeout(600l*1000))
	    	{
				statcollect.setLastTime(System.currentTimeMillis());
	    	}
		    this.startJetty();
		}
		this.statcollect.setLastTime(System.currentTimeMillis());
		if(needhb)
		{
			this.zkHeatbeat();
		}
        }
    

    private JettySolrRunner jetty=null;
    private Object jettyObj=new Object();
    private Integer getTryPort()
    {
    	return this.portbase + taskIndex;
    }
    
    private int bindPort=0;

    public Integer getBindPort() {
		int rtn = 0;
		synchronized (this.jettyObj) {
		    if (this.jetty != null) {
			rtn = this.jetty.getLocalPort();
		    }
		}
	
		if (rtn > 0) {
		    return rtn;
		}
		if (this.bindPort <= 0) {
		    return this.getTryPort();
		}
		return this.bindPort;
    }
    
    private void startJetty() throws Exception {
        statcollect.setStat(ShardsState.STARTSOLR);
    	String solrHome=this.localSolrPath.toUri().getPath();
    	SolrResourceLoader.SetSolrHome(solrHome);
    	SolrResourceLoader.SetSchemaHome(this.localTablePath.toUri().getPath());
    	LOG.info("higolog startJetty start:"+this.tablename+",solrhome:"+solrHome);
    	this.bindPort=NetWorkUtils.available_port(this.getTryPort());
    	int tryCont=3;
    	for (int i = 0; i <= tryCont; i++) {
    	    try {
        		synchronized (this.jettyObj) {
        		this.jetty = null;
        		this.jetty = new JettySolrRunner("/solr", this.bindPort);
        		LOG.info("higolog startJetty create:" + this.tablename + ",solrhome:" + solrHome + ",port:" + this.bindPort);
        		jetty.start();
        		this.bindPort = jetty.getLocalPort();
        		}
        		LOG.info("higolog startJetty started:" + this.tablename + ",solrhome:" + solrHome + ",port:" + this.bindPort);
        		statcollect.setStat(ShardsState.SERVICE);
        		this.zkHeatbeat();
        		break;
    	    } catch (java.net.BindException e) {
        		LOG.error(StormUtils.stringify_error(e));
        		if(i>=tryCont)
        		{
        		    throw e; 
        		}
        		this.bindPort = NetWorkUtils.available_port();
    	    }
    	}
	
    }
    
    private synchronized void zkHeatbeat() {
		try {
		    String hdfsforder = (this.isMergeServer) ? "mergerServer" : IndexUtils.getHdfsForder(taskIndex);
		    Integer bindport = this.getBindPort();
		    Long hbtime = statcollect.getLastTime();
		    SolrInfo info = new SolrInfo(this.params.replication,this.params.replicationindex,this.taskIndex,false,localSolrPath.toString(),
			    "solrservice".toString(), hdfsforder, bindport,
			    statcollect.getStat(), new HashMap<String, ShardCount>(), new HashMap<String, ShardCount>(), this.statcollect.getSetupTime(),
			     hbtime, this.isMergeServer);
		    LOG.info("higolog zkHeatbeat " + this.tablename + ",info:"  + info.toShortString());
		    Worker.getCluster().higo_heartbeat(this.tablename, this.taskid, info);
		} catch (Exception e) {
		    LOG.error(StormUtils.stringify_error(e));
		    throw new RuntimeException(e);
		}
    }
    
    
    public long checkSolr(String tablename,String newestPartion) throws MalformedURLException,
	    SolrServerException {
    	return this.checkSolr(tablename, newestPartion,"");
    }
    
    public long checkSolr(String tablename,String newestPartion,String day) throws MalformedURLException,
    SolrServerException {
    synchronized (this.jettyObj) {
        if(!statcollect.getStat().equals(ShardsState.SERVICE))
        {
        	return 0l;
        }
        if (this.jetty == null) {
        	return 0l;
    	}
        
        String context = "/solr/" + tablename;
        if (tablename.isEmpty()) {
        	context = "/solr";
        }
        long rtn= this.jetty.checkSolrRecord(context,newestPartion,day);
        LOG.info("higolog checkSolr " + context+","+newestPartion+","+day+",result="+rtn);
        return rtn;
    }
    }

    public void unregister() {
    	Worker.getCluster().higo_remove_task(this.tablename, this.taskid);
}

    private void stopJetty() throws Exception {
	synchronized (this.jettyObj) {
	    if (this.jetty != null) {
	    	LOG.info("higolog stopSolr:" + this.tablename);
	    	this.jetty.stop();
	    }
	}
		statcollect.setStat(ShardsState.SOLRSTOP);
		this.zkHeatbeat();
    }
    
    
    public boolean isStop() {
		Boolean rtn = this.isTimeout();
		if (rtn) {
		    this.collector.reportError(new RuntimeException("timeout:" + this.tablename));
		}
		return rtn;
    }

	@Override
	public void setConf(Map stormConf) {
		
	}

    public static class rsyncExecute implements Runnable{
    	private SolrStartJetty obj;
		private boolean issync=false;
		private AtomicBoolean isfinish=new AtomicBoolean(false);
		private boolean result=false;
    	public rsyncExecute(SolrStartJetty obj, boolean issync) {
			this.obj = obj;
			this.issync = issync;
		}


		@Override
		public void run() {
			this.result=this.obj.sync(this.issync);
			isfinish.set(true);
		}
		
		public boolean isfinish()
		{
			return this.isfinish.get();
		}
		
		public boolean result()
		{
			return this.result;
		}
    }
    
    public static class hbExecute implements Runnable{
    	private SolrStartJetty obj;
		private AtomicBoolean isfinish=new AtomicBoolean(false);
    	public hbExecute(SolrStartJetty obj) {
			this.obj = obj;
		}

		@Override
		public void run() {
			try {
				this.obj.heartbeat();
			} catch (Exception e) {
				this.obj.errorCollect.setException(e);
			}
			isfinish.set(true);
		}
		
		public boolean isfinish()
		{
			return this.isfinish.get();
		}
		    	
    }
}
