package com.alimama.mdrill.topology;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;
import org.apache.lucene.store.LinkFSDirectory;
import org.apache.solr.core.SolrResourceLoader;
import org.apache.solr.core.SolrResourceLoader.PartionKey;

import backtype.storm.task.OutputCollector;

import com.alimama.mdrill.partion.GetPartions;
import com.alimama.mdrill.partion.GetPartions.TablePartion;
import com.alimama.mdrill.partion.MdrillPartions;
import com.alimama.mdrill.partion.MdrillPartionsInterface;
import com.alimama.mdrill.partion.StatListenerInterface;
import com.alimama.mdrill.topology.utils.Interval;
import com.alimama.mdrill.topology.utils.SolrStartJettyExcetionCollection;
import com.alimama.mdrill.utils.IndexUtils;
import com.alimama.mdrill.utils.IndexUtils.Vertify;
import com.alipay.bluewhale.core.cluster.SolrInfo.ShardCount;
import com.alipay.bluewhale.core.cluster.ShardsState;
import com.alipay.bluewhale.core.cluster.SolrInfo;
import com.alipay.bluewhale.core.task.StopCheck;
import com.alipay.bluewhale.core.utils.StormUtils;
import com.alipay.bluewhale.core.work.Worker;


public class SolrStartTable implements Runnable, StopCheck, SolrStartInterface {
	private static Logger LOG = Logger.getLogger(SolrStartTable.class);
	private Configuration conf;
	private String solrhome;
	private String diskDirList;
	private int taskIndex;
	private String tablename;
	private Integer taskid;
	private OutputCollector collector;
	private SolrStartJetty solrservice;
	private boolean isMergeServer = false;
	public ExecutorService EXECUTE = new ThreadPoolExecutor(1, 1,
            3600*6, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>());

	 public static class rsyncExecute implements Runnable{
	    	private SolrStartTable obj;
			private boolean issync=false;
			private AtomicBoolean isfinish=new AtomicBoolean(false);
			private boolean result=false;
	    	public rsyncExecute(SolrStartTable obj, boolean issync) {
				super();
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
	
	private boolean isRealTime=false;
	private AtomicInteger copy2indexFailTimes=new AtomicInteger(0);

	private TablePartion part;
	private StatListenerInterface partstat;
	private MdrillPartionsInterface mdrillpartion;
    public void setRealTime(boolean isRealTime) {
		this.isRealTime = isRealTime;
		LinkFSDirectory.setRealTime(isRealTime);

	}
	public void setMergeServer(boolean isMergeServer) {
		this.isMergeServer = isMergeServer;
	}
	BoltParams params;

	public SolrStartTable(	BoltParams params,OutputCollector collector, Configuration conf,
			String solrhome, String diskList, int taskIndex, String tblName,
			Integer taskid, SolrStartJetty solrservice) throws Exception {
		this.params=params;
		this.collector = collector;
		this.conf = conf;
		this.fs = FileSystem.get(this.conf);
		this.lfs = FileSystem.getLocal(this.conf);
		this.solrhome = solrhome;
		this.taskIndex = taskIndex;
		this.tablename = tblName;
		
		this.part=GetPartions.partion(this.tablename);
		this.mdrillpartion=MdrillPartions.INSTANCE(this.part.parttype);
		this.partstat=this.mdrillpartion.getStatObj();
		this.taskid = taskid;
		this.solrservice = solrservice;
		this.diskDirList = diskList;
		LOG.info("higolog init table:" + this.tablename + ",taskIndex:"
				+ this.taskIndex + ",taskId:" + this.taskid);
		this.init();
		this.validate();
	}

	public void setConfigDir(String dir) {
		LinkFSDirectory.setHdfsConfDir(dir);
	}
	
	private void resetFileSystem() throws IOException
	{
		this.fs = FileSystem.get(this.conf);
		this.lfs = FileSystem.getLocal(this.conf);
	}

	private FileSystem fs;
	private FileSystem lfs;

	private Path hdfsSolrpath;
	private Path hdfsIndexpath;

	private Path localSolrPath;
	private Path localIndexPath;
    private Path localTmpPath ;


	private void init() throws IOException {
		this.hdfsSolrpath = new Path(solrhome, "solr");
		this.hdfsIndexpath = new Path(solrhome, "index");
		Path tablepath = this.solrservice.getLocalTablePath(this.tablename);
		this.localSolrPath = new Path(tablepath, "solr");
		this.localIndexPath = new Path(localSolrPath, "./data");
		this.localTmpPath = new Path(this.solrservice.getLocalTmpPath(this.tablename), "./higotmp");
		
	}

	private void validate() throws IOException {
		try {
			if (!fs.exists(hdfsSolrpath)) {
				throw new RuntimeException("can`t not found path "
						+ hdfsSolrpath);
			}
		} catch (IOException e) {
			LOG.error("igno hadoo error", e);
		}
	}

	private boolean syncSolr(boolean isforce) throws IOException {
		boolean ischange = false;
		if (!lfs.exists(new Path(localSolrPath, "complete"))) {
			boolean iscopy=IndexUtils.copyToLocal(fs, lfs, hdfsSolrpath, localSolrPath,this.localTmpPath);
			if(iscopy)
			{
				ischange = true;
				lfs.mkdirs(new Path(localSolrPath, "complete"));
			}
		}

		Path confpath=new Path(localSolrPath,"conf");
		if (isforce||!lfs.exists(new Path(confpath, "complete"))) {
			boolean iscopy=IndexUtils.copyToLocal(fs, lfs, new Path(hdfsSolrpath,"conf"),confpath,this.localTmpPath);
			if(iscopy)
			{
				ischange = true;
				lfs.mkdirs(new Path(confpath, "complete"));
			}
		}
		return ischange;
	}

	private void syncPartion(String key, Path hdfspartion) throws IOException {
		Path localPartionWork = new Path(localIndexPath, key);
		String partionDisk = IndexUtils.getPath(this.diskDirList, taskIndex,key.hashCode(), this.lfs);
		Path localPartionStorePath = new Path(new Path(partionDisk, "higo"),tablename + "/" + this.params.compname + "_" + this.taskIndex + "/" + key);

		boolean iscopy = false;
		if (!this.isMergeServer) {
			Path hdfsPartionShardPath = new Path(hdfspartion,IndexUtils.getHdfsForder(taskIndex));
			iscopy=IndexUtils.copyToLocal(fs, lfs, hdfsPartionShardPath,localPartionStorePath,new Path(partionDisk, "higotmp/"+tablename + "/" + this.params.compname + "_" + this.taskIndex ),true);
			if(!iscopy)
			{
				copy2indexFailTimes.incrementAndGet();
			}
		}
		
		Path indexlinks=new Path(localPartionWork,"indexLinks");
		if(iscopy||!lfs.exists(indexlinks))
		{
			IndexUtils.truncate(lfs, localPartionWork);
			FSDataOutputStream outlinks = lfs.create(indexlinks);
			if (!this.isMergeServer && iscopy) {
				outlinks.write((new String(localPartionStorePath.toString()	+ "\r\n")).getBytes());
			}
			outlinks.close();
		}
		if(iscopy)
		{
			boolean issuccess=IndexUtils.copyToLocal(fs, lfs, new Path(hdfspartion, "vertify"),new Path(localPartionWork, "vertify"),this.localTmpPath);
			if(!issuccess)
			{
				copy2indexFailTimes.incrementAndGet();
			}
		}
		statcollect.setLastTime(System.currentTimeMillis());
	}

	private void dropPartion(Path localpath) throws IOException {
		LOG.info("higolog loadIndex delete:" + localpath.toString());
		File links = new File(localpath.toUri().getPath(), "indexLinks");
		if (links.exists()) {
			FileReader freader = new FileReader(links);
			BufferedReader br = new BufferedReader(freader);
			String s1 = null;
			while ((s1 = br.readLine()) != null) {
				if (s1.trim().length() > 0) {
					Path indexpath = new Path(s1);
					if (lfs.exists(indexpath)) {
						lfs.delete(new Path(s1), true);
					}
				}
			}
			br.close();
			freader.close();
		}
		if (lfs.exists(localpath)) {
			lfs.delete(localpath, true);
		}
	}

	
	
	
	private boolean syncIndex() throws IOException {
		if(this.isRealTime)
		{
			return false;
		}
		
		if(this.isMergeServer)
		{
			return false;
		}
		boolean ischange = false;
		HashMap<String, Vertify> hdfsVertify = IndexUtils.readVertifyList(fs,hdfsIndexpath);// partions
		HashMap<String, Vertify> localVertify = IndexUtils.readVertifyList(lfs,	localIndexPath);// ./data

		this.partstat.syncClearPartions();

		for (Entry<String, Vertify> e : hdfsVertify.entrySet()) {
			String partion = e.getKey();
			this.partstat.addPartionStat(partion);

			
			Vertify hdfsValue = e.getValue();
			Vertify localValue = localVertify.remove(partion);
			if (localValue == null	|| !localValue.getVertify().equals(hdfsValue.getVertify())) {
				this.syncPartion(partion, hdfsValue.getPath());
				SolrResourceLoader.SetCacheFlushKey(new PartionKey(this.tablename, partion),System.currentTimeMillis());
			}
		}
		this.partstat.syncClearStat();

		HashSet<String> skiplist=new HashSet<String>();
		skiplist.add("default");
		skiplist.add("");


		for (Entry<String, Vertify> e : localVertify.entrySet()) {
			if(skiplist.contains(e.getKey()))
			{
				continue;
			}
			this.dropPartion(e.getValue().getPath());
			PartionKey p=new PartionKey(this.tablename, e.getKey());
			SolrResourceLoader.SetCacheFlushKey(p,System.currentTimeMillis());
			SolrResourceLoader.DropCacheFlushKey(p);
		}
		LOG.info("higolog loadIndex finish:" + this.tablename + ",ischange"	+ ischange );
		return false;
	}
	
	

	private synchronized boolean sync( boolean isforce) {
		try {
			this.resetFileSystem();
			this.validate();
			LOG.info("higolog loadSolr start:" + this.tablename);
			boolean ischangeSolr = this.syncSolr(isforce);
			LOG.info("higolog loadIndex start:" + this.tablename);
			boolean ischangeIndex = this.syncIndex();
			boolean ischange = ischangeIndex || ischangeSolr;
			return ischange;
		} catch (Throwable e) {
			LOG.error(StormUtils.stringify_error(e));
			return false;
		}
	}

    AtomicBoolean isInit=new AtomicBoolean(false);
	public void start() throws Exception {
		statcollect.setLastTime(System.currentTimeMillis());
		statcollect.setStat(ShardsState.INIT);
		this.zkHeatbeat();
		LOG.info("higolog table begin:" + this.tablename);
		new Thread(this).start();
	}
	
	

	public void run() {
		synchronized (this.getThrLockObj()) {
			try {
				rsyncExecute exe=new rsyncExecute(this,true);
    	    	EXECUTE.submit(exe);
    	    	while(!exe.isfinish()&&!statcollect.isTimeout(1200l*1000))
    	    	{
    	    		Thread.sleep(1000l);
    	    	}
    	    	
    	    	if(statcollect.isTimeout(1200l*1000))
    	    	{
    				statcollect.setLastTime(System.currentTimeMillis());
    	    	}
				
				this.startService();
    			isInit.set(true);
				LOG.info("higolog table end:" + this.tablename);
			} catch (RuntimeException er) {
				this.runException(er);
			}
			catch (Exception e) {
				this.runException(e);
			}
		}
	}
	
	private void runException(Exception e)
	{
		LOG.error(StormUtils.stringify_error(e));
		statcollect.setStat(ShardsState.INITFAIL);
		this.zkHeatbeat();
		errorCollect.setException(e);
	}

	private Object thrlock = new Object();
	public Object getThrLockObj() {
		return thrlock;
	}

	private SolrStartJettyExcetionCollection errorCollect=new SolrStartJettyExcetionCollection();
	private SolrStartJettyStat statcollect=new SolrStartJettyStat();

	public void stop() throws Exception {
		synchronized (this.getThrLockObj()) {
			statcollect.setStat(ShardsState.UINIT);
			this.zkHeatbeat();
			this.stopService();
		}
	}

	public Boolean isTimeout() {
		Long timespan = 1000l * 60 * 30;
		ShardsState stat = statcollect.getStat();
		if (stat.equals(ShardsState.SERVICE)) {
			timespan = 1000l * 60 * 20;
		}
		return isInit.get()&&statcollect.isTimeout(timespan);
	}

	private Interval hbInterval=new Interval();;

	 private boolean checkInitFinish() throws Exception
    {
    	if(isInit.get())
    	{
    		return true;
    	}
    	
		long nowtime = System.currentTimeMillis();
		if (nowtime - statcollect.getSetupTime() < 1000l * 60 * 120) {
		    return false;
		}
		
		throw new Exception("init timeout");
    }

	public void heartbeat() throws Exception {
		errorCollect.checkException();
		if (!hbInterval.heartBeatInterval()) {
			return;
		}

		if (!this.checkInitFinish()) {
			return;
		}
		
		if(copy2indexFailTimes.get()>10)
		{
			errorCollect.setException(new Exception("copy2indexFailTimes>10"));
			return ;
		}

		synchronized (this.getThrLockObj()) {
			LOG.info("higolog heartbeat:" + this.tablename+",copy2indexFailTimes="+copy2indexFailTimes.get());
			try {
				this.heartbeatExecute();
			} catch (IOException e) {
				LOG.error(StormUtils.stringify_error(e));
				errorCollect.setException(e);
			}
		}
	}

	private void heartbeatExecute() throws Exception {
		
		rsyncExecute exe=new rsyncExecute(this,false);
    	EXECUTE.submit(exe);
    	while(!exe.isfinish()&&!statcollect.isTimeout(900l*1000))
    	{
    		Thread.sleep(1000l);
    	}
    	if(statcollect.isTimeout(900l*1000))
    	{
			statcollect.setLastTime(System.currentTimeMillis());
    	}
		
		boolean needRestart = exe.isfinish()?exe.result():false;
		if (!needRestart) {
			this.checkSolr();
			if (!statcollect.getStat().equals(ShardsState.SERVICE)) {
				this.startService();
			}
		} else {
			this.stopService();

			exe=new rsyncExecute(this,false);
	    	EXECUTE.submit(exe);
	    	while(!statcollect.isTimeout(900l*1000))
	    	{
	    		Thread.sleep(1000l);
	    	}
	    	if(statcollect.isTimeout(900l*1000))
	    	{
				statcollect.setLastTime(System.currentTimeMillis());
	    	}
			this.startService();
		}
	}

	public Integer getBindPort() {
		return this.solrservice.getBindPort();
	}

	private void startService() throws Exception {
		LOG.info("higolog startService");
		SolrResourceLoader.SetCacheFlushKey(null,System.currentTimeMillis());
		statcollect.setStat(ShardsState.SERVICE);
		this.checkSolr();
		this.zkHeatbeat();
		this.solrservice.setRestart();
	}
	

	
	

	private synchronized void zkHeatbeat() {
		try {
			String hdfsforder = (this.isMergeServer) ? "mergerServer"
					: IndexUtils.getHdfsForder(taskIndex);
			Integer bindport = this.getBindPort();
			Long hbtime = statcollect.getLastTime();
			
			HashMap<String, ShardCount> daystat=this.partstat.getExtaCount();
			SolrInfo info = new SolrInfo(this.params.replication,this.params.replicationindex,this.taskIndex,this.isRealTime,localSolrPath.toString(),
					hdfsIndexpath.toString(), hdfsforder, bindport, 
					statcollect.getStat(), this.partstat.getPartioncount(),daystat, statcollect.getSetupTime(),
					hbtime, this.isMergeServer);
			LOG.info("higolog zkHeatbeat " + this.tablename + ",info:"
					+ info.toString());
			Worker.getCluster().higo_heartbeat(this.tablename, this.taskid,
					info);
		} catch (Exception e) {
			LOG.error(StormUtils.stringify_error(e));
			errorCollect.setException(e);
		}
	}

	
	
	
	
	
	
	
	private void checkSolr() {
		if (!this.solrservice.isService()) {
			return;
		}

		try {
			this.partstat.fetchCount(this.solrservice,this.tablename,this.part);
			statcollect.setLastTime(System.currentTimeMillis());
			this.zkHeatbeat();
		} catch (Exception e) {
			LOG.error(StormUtils.stringify_error(e));
			statcollect.setStat(ShardsState.SOLRDIE);
			this.zkHeatbeat();
			errorCollect.setException(e);
		}
	}

	public void unregister() {
		synchronized (this.getThrLockObj()) {
			Worker.getCluster().higo_remove_task(this.tablename, this.taskid);
		}
	}

	private void stopService() throws Exception {
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
