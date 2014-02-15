package com.alimama.mdrill.solr.realtime.realtime;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimerTask;
import java.util.zip.CRC32;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alimama.mdrill.solr.realtime.DirectoryInfo;
import com.alimama.mdrill.solr.realtime.RealTimeDirectory;
import com.alimama.mdrill.utils.IndexUtils;
import com.alimama.mdrill.utils.UniqConfig;


//review ok 2013-12-28
public class RealTimeDirectoryThread {
	public static Logger LOG = LoggerFactory.getLogger(RealTimeDirectoryThread.class);
	final RealTimeDirectoryStatus status;
	final RealTimeDirectoryParams params;
	final RealTimeDirectoryData data;
	final RealTimeDirectoryLock plock;
	final RealTimeDirectory mainThread;

	public RealTimeDirectoryThread(RealTimeDirectoryStatus status,
			RealTimeDirectoryParams params, RealTimeDirectoryData data,
			RealTimeDirectoryLock plock, RealTimeDirectory mainthread) {
		this.status = status;
		this.params = params;
		this.data = data;
		this.plock = plock;
		this.mainThread = mainthread;
	}
	
	ArrayList<TimerTask> taskList=new ArrayList<TimerTask>();

	private void initPurgerThread() {
		
		TimerTask task=new TimerTask() {
			@Override
			public void run() {
				try {
					if (status.needRemakeLinks.get()) {
						status.needRemakeLinks.set(false);
						status.needPurger.set(false);
						mainThread.purger(true);
					}
					if (status.needPurger.get()) {
						status.needPurger.set(false);
						mainThread.purger(false);
					}

				} catch (Throwable ex) {
					LOG.error("_purgerthr", ex);
				}
			}
		};
		
		taskList.add(task);
		
		RealTimeDirectorUtils.getQuickTimer().schedule(task, 300, 300);
	
	}
	
	private void syncHdfs()
	{
		long len=UniqConfig.RealTimeHdfsFlush() * 1000l;
		TimerTask task=new TimerTask() {
			@Override
			public void run() {
				try {
					mainThread.syncHdfs();
				} catch (Throwable ex) {
					LOG.error("syncHdfs",ex);
				}				
			}
			
		};
		taskList.add(task);
		RealTimeDirectorUtils.getSlowTimer().schedule(task, len, len);
	}
	
	
	public static class lastParams{
		public long lasttime = System.currentTimeMillis();;
		public long tslen = UniqConfig.RealTimeDoclistFlush() * 1000l;
		
		public long getTimeout()
		{
			return lasttime + tslen;
		}
	}
	
	private void doclistSync() {
		long len=1000l;
		final lastParams lasttime = new lastParams();
		
		TimerTask task=new TimerTask() {
			@Override
			public void run() {
				int size = 0;
				try {
					synchronized (plock.doclistBuffer_lock) {
						size = data.doclistSize();
					}
					if (size == 0) {
						return ;
					}
					long nowtime = System.currentTimeMillis();
					if (size >= UniqConfig.RealTimeDoclistBuffer()|| (lasttime.getTimeout()) < nowtime) {
						mainThread.flushDocList();
						lasttime.lasttime = System.currentTimeMillis();
						long ts = lasttime.lasttime - nowtime;
						LOG.info("flushDocList timetake:" + (ts / 1000));
					}
				} catch (Throwable ex) {
					LOG.error("flushDocList", ex);
				}
			}
		};
		taskList.add(task);
		RealTimeDirectorUtils.getQuickTimer().schedule(task, len, len);
	}
	
	
    public void start()
    {
    	this.initPurgerThread();	
		this.syncHdfs();
		this.doclistSync();
    }
    
    public void close()
    {
    	for(TimerTask t:taskList){
    		t.cancel();
    	}
    	RealTimeDirectorUtils.getSlowTimer().purge();
    	RealTimeDirectorUtils.getQuickTimer().purge();
    }
	public static SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMddHHmmss");

    public void startSyncFromHdfs(final Runnable callback) throws IOException
	{
    	Configuration conf=params.getConf();
    	
    	CRC32 crc32 = new CRC32();
		crc32.update(String.valueOf(java.util.UUID.randomUUID().toString()).getBytes());
		Long uuid=crc32.getValue();
		String pathname=String.valueOf(fmt.format(new Date())+"_"+status.uniqIndex.incrementAndGet()) + "_"	+ uuid;
    	
    	final Path localHdfsRecover=new Path(new Path(params.getIndexMalloc(pathname.hashCode()),"realtime_hdfs_recover"),pathname);
    	final Path hdfsRealtime=new Path(params.hdfsPath,"realtime");
    	
		final FileSystem fs=FileSystem.get(conf);
		final FileSystem lfs=FileSystem.getLocal(conf);
	
		lfs.delete(localHdfsRecover, true);
		final Path localPath=new Path(localHdfsRecover.getParent(),localHdfsRecover.getName()+"_hdfs");
		final Path localPathtmp=new Path(localHdfsRecover.getParent(),localHdfsRecover.getName()+"_tmp");
		
		TimerTask task=new TimerTask() {
			@Override
			public void run() {
				try {
					IndexUtils.copyToLocal(fs, lfs, hdfsRealtime,localPath,localPathtmp,true);
					synchronized (plock.lock) {
								FileStatus[] filelist=lfs.listStatus(localPath);
								boolean needPurge=false;
								if(filelist!=null)
								{
									for(FileStatus f:filelist)
									{
										if(!f.isDir())
										{
											continue;
										}
										try{
										DirectoryInfo d=new DirectoryInfo();
										File findex=new File(f.getPath().toString());
										d.d=FSDirectory.open(findex);
										d.tp=DirectoryInfo.DirTpe.file;
										data.AddIndex(findex.getAbsolutePath(), d);	
										needPurge=true;
										}catch(Throwable e)
										{
											LOG.error("AddIndex",e);

										}
									}
								}
								
								if(needPurge){
									status.needRemakeLinks.set(true);
								}
					}
					callback.run();
					return ;
				} catch (Throwable ex) {
					LOG.error("syncHdfs",ex);
				}				
			}
		};
		taskList.add(task);
		RealTimeDirectorUtils.getSlowTimer().schedule(task, 0);
	}
}
