package com.alimama.mdrill.solr.realtime;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.core.SolrCore;
import org.apache.solr.core.SolrResourceLoader;
import org.apache.solr.core.SolrResourceLoader.PartionKey;


import com.alimama.mdrill.adhoc.TimeCacheMap;
import com.alimama.mdrill.editlog.AddOp;
import com.alimama.mdrill.editlog.FSEditLog;
import com.alimama.mdrill.editlog.defined.StorageDirectory;
import com.alimama.mdrill.editlog.read.EditLogInputStream;
import com.alimama.mdrill.hdfsDirectory.FileSystemDirectory;
import com.alimama.mdrill.solr.realtime.DirectoryInfo.DirTpe;
import com.alimama.mdrill.solr.realtime.realtime.RealTimeDirectorUtils;
import com.alimama.mdrill.solr.realtime.realtime.RealTimeDirectoryLock;
import com.alimama.mdrill.solr.realtime.realtime.RealTimeDirectoryParams;
import com.alimama.mdrill.solr.realtime.realtime.RealTimeDirectoryStatus;
import com.alimama.mdrill.solr.realtime.realtime.RealTimeDirectoryThread;
import com.alimama.mdrill.solr.realtime.realtime.RealTimeDirectoryData;
import com.alimama.mdrill.utils.UniqConfig;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class RealTimeDirectory implements MdrillDirectory,TimeCacheMap.ExpiredCallback<Integer, DirectoryInfo>{
	
	public static Logger LOG = LoggerFactory.getLogger(RealTimeDirectory.class);
	private RealTimeDirectoryData data;
	private RealTimeDirectoryParams params=new RealTimeDirectoryParams();
	private RealTimeDirectoryStatus status=new RealTimeDirectoryStatus();
	private RealTimeDirectoryThread thread;
	private FSEditLog editlog;
	private final RealTimeDirectoryLock rlock=new RealTimeDirectoryLock();
	private AtomicBoolean needFlushDfs=new AtomicBoolean(false);
	private AtomicBoolean needRollLogs=new AtomicBoolean(false);
	
	public RealTimeDirectory(File path,String hadoopConfDir,String hdfsPath,SolrCore core,PartionKey partion) throws IOException
	{
		
		LOG.info("##RealTimeDirectory init ##"+path.getAbsolutePath()+","+hdfsPath);
		this.params.hadoopConfDir=hadoopConfDir;
		this.params.baseDir=path.getAbsolutePath();
		this.params.hdfsPath=hdfsPath;
		this.params.Partion = partion;
		this.params.core = core;

		path.mkdirs();
		status.isInit.set(this.params.checkSyncHdfs());
		LOG.info("## RealTimeDirectory isinit##"+path.getAbsolutePath()+","+hdfsPath+",this.isInit="+String.valueOf(status.isInit));

		Configuration conf=params.getConf();
		FileSystem fs=FileSystem.get(conf);
		if(!status.isInit.get())
		{
			fs.mkdirs(new Path(hdfsPath).getParent());
		}
		
		boolean isUsedHdfs=false;
		if(!status.isInit.get())
		{
			isUsedHdfs=params.isUseHdfsIndex();
		}
		data=new RealTimeDirectoryData(params, status, this);
		data.initDiskDirector(isUsedHdfs);
		this.recoverFromEditlog(conf,isUsedHdfs);
		thread=new RealTimeDirectoryThread(status,params,data,rlock,this);
		thread.start();
		if(isUsedHdfs)
		{
			status.allowsynchdfs.set(false);
			thread.startSyncFromHdfs(new Runnable() {
				@Override
				public void run() {
					status.allowsynchdfs.set(true);
				}
			});
		}else{
			status.allowsynchdfs.set(true);
		}
		
		if(!status.isInit.get())
		{
			this.params.markSyncHdfs();
		}
	}
	
	public void setPartion(PartionKey partion) {
		params.Partion = partion;
	}
	
	
	public void setCore(SolrCore core) {
		params.core = core;
	}

	public void purger(boolean remakeLinks) throws IOException
	{	
		List<Directory> copyDir=new ArrayList<Directory>();; 
		synchronized (rlock.lock) {
			if(remakeLinks)
			{
				this.data.remakeIndexLinksFile();
			}
			copyDir=this.data.copyForSearch();
		}
		synchronized (rlock.searchLock) {
			readList=copyDir;
		}
		SolrResourceLoader.SetCacheFlushKey(params.Partion,System.currentTimeMillis());
	}
	
	
	private List<Directory> readList=null; 
	
	public List<Directory> getForSearch()
	{
		List<Directory> rtn=null;
		synchronized (rlock.searchLock) {
			rtn=readList;
		}
		
		if(rtn==null)
		{
			List<Directory> copyDir=new ArrayList<Directory>();; 
			synchronized (rlock.lock) {
				copyDir=this.data.copyForSearch();
			}
			synchronized (rlock.searchLock) {
				readList=copyDir;
			}
		}
		
		synchronized (rlock.searchLock) {
			rtn=readList;
		}
		
		LOG.info("####getForSearch####"+String.valueOf(rtn));	
		return rtn;
	}
	
    public void commit(){
    	
    }

	@Override
	public void expire(Integer key, DirectoryInfo val) {
		if (val.tp.equals(DirectoryInfo.DirTpe.buffer)) {
			try {
				synchronized (rlock.lock) {
					this.data.mergerBuffer(val);
					this.data.maybeMerger();
				}
			} catch (Throwable e) {
				LOG.error("####expire buffer error ####", e);
			}
			return;
		}

		if (val.tp.equals(DirectoryInfo.DirTpe.ram)) {
			try {
				synchronized (rlock.lock) {
					this.data.mergerRam(val);
					this.data.maybeMerger();
				}
			} catch (Throwable e) {
				LOG.error("####expire ram error ####", e);
			}
			return;
		}

		if (val.tp.equals(DirectoryInfo.DirTpe.delete)) {
			try {
				RealTimeDirectorUtils.deleteDirector(val,this.params.getConf());
			} catch (Throwable e) {
				LOG.error("deleteDirector", e);
			}
			return;
		}
	}
	
	public void syncLocal()
	{
		synchronized (rlock.lock) {
			this.data.flushToDisk();
		}
	}
	
	public synchronized void syncHdfs()
	{
		if(!needFlushDfs.get())
		{
			return ;
		}
		long t1=System.currentTimeMillis();
		while(!this.status.allowsynchdfs.get())
		{
			try {
				Thread.sleep(10000);
				LOG.error("syncHdfs wait");
			} catch (InterruptedException e) {
			}
			
			long t2=System.currentTimeMillis();
			if(t2-t1>1000l*3600*3)
			{
				LOG.error("syncHdfs wait timeout");
				break;
			}
		}
		
		needFlushDfs.set(false);
		
		String tsstr="";
		ConcurrentHashMap<String, DirectoryInfo> diskDirector_hdfs=new ConcurrentHashMap<String, DirectoryInfo>();
		synchronized (rlock.lock) {
			this.data.flushToDisk();
			try {
				tsstr=data.remakeIndexLinksFile();
				diskDirector_hdfs=this.data.copyForSyncHdfs();;
			} catch (Throwable e) {
				LOG.error("syncHdfs",e);
			}
		}
		
		
		try {
			saveToHdfs(diskDirector_hdfs,tsstr);
		} catch (Throwable e) {
			LOG.error("syncHdfs",e);
		}
		
	}
		
	private void saveToHdfs(ConcurrentHashMap<String, DirectoryInfo> buffer,String tsstr) throws IOException
	{
		Configuration conf=this.params.getConf();
		FileSystem fs=FileSystem.get(conf);
		Path tmpDir=new Path(params.hdfsPath,"realtime_tmp");
		if(fs.exists(tmpDir))
		{
			fs.delete(tmpDir, true);
		}
		fs.mkdirs(tmpDir);
		
		Path tsdst=new Path(tmpDir,"realtime_ts");
		
		OutputStreamWriter fwriterTs= new OutputStreamWriter(fs.create(tsdst));
		fwriterTs.write(tsstr);
		fwriterTs.close();
		LOG.info("####sync realtime_ts ####"+tsstr+","+tsdst.toString());
		
		long txid=0;
		for(Entry<String, DirectoryInfo> e:buffer.entrySet())
		{
			e.getValue().synctxid();
			txid=Math.max(txid, e.getValue().readTxid());
			Path local=new Path(e.getKey());
			Path dst=new Path(tmpDir,local.getName());
			LOG.info("####copyFromLocalFile####"+local.toString()+","+dst.toString());
			fs.copyFromLocalFile(local,dst);
		}
		
		Path realtimefinal=new Path(params.hdfsPath,"realtime");
		if(fs.exists(realtimefinal))
		{
			fs.delete(realtimefinal, true);
		}
		
		fs.rename(tmpDir, realtimefinal);
		
		if(txid>0)
		{
			synchronized (editlog) {
				editlog.purgeLogsOlderThan(txid-1);
			}
		}
		LOG.info("####saveToHdfs####"+realtimefinal.toString()+",purgeLogsOlderThan txid="+txid);
	}

	public synchronized void addDocument(SolrInputDocument doc) throws CorruptIndexException, LockObtainFailedException, IOException
	{
		this.addDocument(doc,true);
	}

	long editlogtime=System.currentTimeMillis();
	
	AtomicInteger addIntervel=new AtomicInteger(0);
	
	public synchronized void addDocument(SolrInputDocument doc,boolean writelog) throws CorruptIndexException, LockObtainFailedException, IOException
	{
		if("mm_12229823_1573806_11174236".equals(doc.getFieldValue("pid")))
		{
			LOG.info("####addDocument 1 :"+doc+","+String.valueOf(writelog));
		}
		
		if(RealTimeDirectorUtils.maybeReplication(doc))
		{
			return ;
		}
		
		if("mm_12229823_1573806_11174236".equals(doc.getFieldValue("pid")))
		{
			LOG.info("####addDocument 2 :"+doc+","+String.valueOf(writelog));
		}
	
		if(writelog)
		{
			try {
				AddOp op=new AddOp();
				op.setDoc(doc);
				synchronized (editlog) {
					if(!needRollLogs.get())
					{
						editlog.openForWrite();
						needRollLogs.set(true);
					}
					editlog.logEdit(op);
					long lasttid=editlog.getLastWrittenTxId();
					doc.setTxid(lasttid);
					if(lasttid%UniqConfig.logRollIntervel()==0)
					{
						long currenttime=System.currentTimeMillis();
						if(currenttime-editlogtime>=UniqConfig.logRollTimelen())
						{
							editlogtime=currenttime;
							editlog.rollEditLog();
						}
					}
				}
			} catch (Exception e) {
				LOG.error("editlog_"+this.params.Partion.toString(),e);
				editlog.openForWrite();
			}
		}
		
		needFlushDfs.set(true);
		synchronized (rlock.doclistBuffer_lock) {
			this.data.AddDoc(doc);
		}
		
		if(addIntervel.incrementAndGet()>5000)
		{
			addIntervel.set(0);
			int size=0;
			synchronized (rlock.doclistBuffer_lock) {
				size=this.data.doclistSize();
			}
			if(size>20000)
			{
				this.flushDocList();
			}
		}
	}
	
	public void flushDocList() throws CorruptIndexException,
			LockObtainFailedException, IOException {
		ArrayList<SolrInputDocument> flush = null;
		synchronized (this.rlock.doclistBuffer_lock) {
			flush = this.data.popDoclist();
			addIntervel.set(0);
		}
		synchronized (this.rlock.lock) {
			if (flush != null) {
				this.data.flushDocList(flush);
			}
		}
	}
	
	
    private void recoverFromEditlog(Configuration conf,boolean isUsedHdfs) throws IOException
    {
    	long t1=System.currentTimeMillis();
		List<StorageDirectory> editsDirs = new ArrayList<StorageDirectory>();
		if("hdfs".equals(SolrCore.getBinglogType()))
		{
			editsDirs.add(new StorageDirectory(FileSystem.get(conf), new Path(params.hdfsPath, "editlogs_v9")));
		}else{
			editsDirs.add(new StorageDirectory(FileSystem.getLocal(conf), new Path(params.baseDir, "editlogs_v9")));
		}
		LOG.info("recoverFromEditlog begin:"+this.params.getLogStr());
		editlog = new FSEditLog(conf, editsDirs);
		editlog.initJournalsForWrite();
		editlog.recoverUnclosedStreams();
    	long savedTxid=this.data.getMaxTxidFromLocal();
    	if(isUsedHdfs)
		{
    		Path realtimefinal=new Path(this.params.hdfsPath,"realtime");
    		FileSystem fs=FileSystem.get(conf);
    		FileStatus[] list=fs.listStatus(realtimefinal);
    		if(list!=null)
    		{
    			for(FileStatus s:list)
    			{
    				try{
    				DirectoryInfo info=new DirectoryInfo();
    				info.tp=DirTpe.hdfs;
    				info.d=new FileSystemDirectory(fs, s.getPath(), false, conf);
    				savedTxid=Math.max(savedTxid, info.readTxid());
    				}catch(Throwable e){
    					LOG.error("recoverFromEditlog error",e);
    				}
    			}
    		}
		}
		LOG.info("recoverFromEditlog savedTxid:"+savedTxid+","+this.params.getLogStr());

		long lines=0;
		long allrecord=0;
		  Collection<EditLogInputStream> streams=new ArrayList<EditLogInputStream>();
		  editlog.selectInputStreams(streams, savedTxid+1, true, true);
		  long lasttxid=savedTxid;
		  if(streams!=null&&streams.size()>0)
		  {
				for(EditLogInputStream stream:streams)
				{
					while(true)
					{
						AddOp op=null;
						try {
							op = (AddOp) stream.readOp();
							if (op == null) {
					    		LOG.error("readOp end");

								break;
							}
							if(op.getDoc()==null)
							{
					    		LOG.error("readOp doc null");

								continue;
							}
						} catch (Throwable e) {
							LOG.error("readOp", e);
							break;
						}
						
						SolrInputDocument doc=op.getDoc();
						doc.setTxid(op.getTransactionId());
						if(lines<100000)
						{
							lines++;
							if(lines%500==0)
							{
								LOG.info("##recover##"+doc.toString()+",savedTxid="+savedTxid+":"+this.params.getLogStr());
							}
						}
						allrecord++;
						if(allrecord%1000==0)
						{
							this.flushDocList();
						}
			
						this.addDocument(doc,false);
					}
					
					lasttxid=Math.max(stream.getLastTxId(), lasttxid);
				}
				FSEditLog.closeAllStreams(streams);
		  }

		  editlog.setNextTxId(lasttxid+1);
		
		long t2=System.currentTimeMillis();
		long timetaken=t2-t1;
		LOG.info("##recovercount##count="+allrecord+",savedTxid="+savedTxid+",getLastTxId="+lasttxid+",timetaken="+timetaken+","+this.params.getLogStr());
    }
    
	
	
	public void close() {
		try {
			this.thread.close();
			if(this.needRollLogs.get())
			{
				this.editlog.endCurrentLogSegment(true);
			}
		} catch (Throwable e) {
			LOG.error("close",e);
		} 
	}

}
