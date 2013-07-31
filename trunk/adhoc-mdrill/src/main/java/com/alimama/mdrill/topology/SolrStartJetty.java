package com.alimama.mdrill.topology;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
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
import com.alimama.mdrill.topology.utils.Interval;
import com.alimama.mdrill.topology.utils.SolrStartJettyExcetionCollection;
import com.alimama.mdrill.utils.HadoopUtil;
import com.alimama.mdrill.utils.IndexUtils;
import com.alipay.bluewhale.core.cluster.ShardsState;
import com.alipay.bluewhale.core.task.StopCheck;
import com.alipay.bluewhale.core.utils.NetWorkUtils;
import com.alipay.bluewhale.core.utils.StormUtils;
import com.alipay.bluewhale.core.work.Worker;

public class SolrStartJetty implements Runnable,StopCheck,SolrStartInterface{
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

    private boolean isRealTime=false;

    public void setRealTime(boolean isRealTime) {
		this.isRealTime = isRealTime;
		LinkFSDirectory.setRealTime(isRealTime);
	}

    public void setRestart()
    {
    	this.neetRestart.set(true);
    }

    public void setMergeServer(boolean isMergeServer) {
        this.isMergeServer = isMergeServer;
    }
    
    public SolrStartJetty(OutputCollector collector,Configuration conf, String solrhome, String diskList, Integer portbase,int taskIndex, String tblName, Integer taskid,Integer partions) throws IOException {
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
		SolrCore.setSearchCacheSize(partions);
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
    	Path localpath = new Path(new Path(this.singleStorePath, "higo"), tablename + "/" + taskid+"_"+this.taskIndex);
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
    
	private boolean sync(boolean isforce) throws IOException {
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
			return needRestart;
		} catch (Exception e) {
			LOG.error(StormUtils.stringify_error(e));
			return false;
		}
	}
    
    public void start() throws Exception {
    	statcollect.setLastTime(System.currentTimeMillis());
    	statcollect.setStat(ShardsState.INIT);
    	this.zkHeatbeat();
    	LOG.info("higolog table begin:"+this.tablename);
    	new Thread(this).start();
    }
    
    
    AtomicBoolean isInit=new AtomicBoolean(false);
    public void run() {
    	synchronized (this.getThrLockObj()) {
    	    try {
    			this.sync(true);
    			this.startJetty();
    			isInit.set(true);
    			LOG.info("higolog table end:" + this.tablename);
    	    } catch (RuntimeException er) {
    			this.runException(er);
    	    } catch (Exception e) {
    			this.runException(e);
    	    }
    	}
        }
    
    private Object thrlock=new Object();
    public Object getThrLockObj()
    {
    	return thrlock;
    }
    private SolrStartJettyExcetionCollection errorCollect=new SolrStartJettyExcetionCollection();
    private SolrStartJettyStat statcollect=new SolrStartJettyStat();

    
	public boolean isService()
	{
		return statcollect.getStat().equals(ShardsState.SERVICE);
	}
    
    private void runException(Exception er)
    {
    	LOG.error(StormUtils.stringify_error(er));
		statcollect.setStat(ShardsState.INITFAIL);
		this.zkHeatbeat();
		errorCollect.setException(er);
    }
    
    public void stop() throws Exception {
	synchronized (this.getThrLockObj()) {
		statcollect.setStat(ShardsState.UINIT);
	    this.zkHeatbeat();
	    this.stopJetty();
	}
    }
        
    public Boolean isTimeout()
    {
		return isInit.get()&&statcollect.isTimeout(1000l*60*20);
    }
    
    private Interval hbInterval=new Interval();;
    
    private boolean checkInitFinish() throws Exception
    {
    	if(isInit.get())
    	{
    		return true;
    	}
    	
		long nowtime = System.currentTimeMillis();
		if (nowtime - statcollect.getSetupTime() < 1000l * 60 * 20) {
		    return false;
		}
		
		throw new Exception("init timeout");
    }

    public void heartbeat() throws Exception {
		LOG.info("higolog heartbeat called:" + this.tablename);
		errorCollect.checkException();
		
		if(!hbInterval.heartBeatInterval())
		{
		    return ;
		}
		
		if(!this.checkInitFinish())
		{
		    return ;
		}
		
//		if(!MemInfo.hasMem())
//		{
//		    throw new Exception("no enough mem:"+MemInfo.getInfo(1024));
//		}
		
		synchronized (this.getThrLockObj()) {
		    LOG.info("higolog heartbeat:" + this.tablename);
		    try {
		    	this.heartbeatExecute();
		    } catch (IOException e) {
		    	LOG.error(StormUtils.stringify_error(e));
		    	errorCollect.setException(e);
		    }
		}
    }
    
    private void heartbeatExecute() throws Exception {
    	boolean needhb=false;
		if (!neetRestart.get()) {
		    if (!statcollect.getStat().equals(ShardsState.SERVICE)) {
		    	this.startJetty();
		    }else{
		    	needhb=true;
		    }
		}else{
		    neetRestart.set(false);
		    this.stopJetty();
		    this.sync(false);
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
		    SolrInfo info = new SolrInfo(this.isRealTime,localSolrPath.toString(),
			    "solrservice".toString(), hdfsforder, bindport,
			    statcollect.getStat(), new HashMap<String, ShardCount>(), new HashMap<String, ShardCount>(), this.statcollect.getSetupTime(),
			     hbtime, this.isMergeServer);
		    LOG.info("higolog zkHeatbeat " + this.tablename + ",info:"  + info.toString());
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
        LOG.info("higolog checkSolr " + context+","+newestPartion);
        return this.jetty.checkSolrRecord(context,newestPartion,day);
    }
    }

    public void unregister() {
	synchronized (this.getThrLockObj()) {
	    Worker.getCluster().higo_remove_task(this.tablename, this.taskid);
	}
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
}
