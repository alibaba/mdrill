package com.alimama.mdrill.solr.realtime;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.CRC32;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.KeepOnlyLastCommitDeletionPolicy;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LinkFSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.cache.Cache;
import org.apache.lucene.util.cache.SimpleLRUCache;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.core.SolrCore;
import org.apache.solr.core.SolrResourceLoader;
import org.apache.solr.core.SolrResourceLoader.PartionKey;
import org.apache.solr.update.DocumentBuilder;


import com.alimama.mdrill.adhoc.TimeCacheMap;
import com.alimama.mdrill.hdfsDirectory.FileSystemDirectory;
import com.alimama.mdrill.utils.HadoopUtil;
import com.alimama.mdrill.utils.UniqConfig;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
public class RealTimeDirectory implements MdrillDirectory,TimeCacheMap.ExpiredCallback<Integer, DirectoryInfo>{
	public static Logger LOG = LoggerFactory.getLogger(RealTimeDirectory.class);

	private ConcurrentHashMap<String, DirectoryInfo> diskDirector=new ConcurrentHashMap<String, DirectoryInfo>();
	private TimeCacheMap<Integer, DirectoryInfo> RamDirector=new TimeCacheMap<Integer, DirectoryInfo>(UniqConfig.RealTimeLocalFlush(), this);
	private ArrayList<SolrInputDocument> doclistBuffer=new ArrayList<SolrInputDocument>(UniqConfig.RealTimeDoclistBuffer());
	private Object doclistBuffer_lock=new Object();
	private TimeCacheMap<Integer, DirectoryInfo> bufferDirector=new TimeCacheMap<Integer, DirectoryInfo>(UniqConfig.RealTimeBufferFlush(), this);
	private TimeCacheMap<Integer, DirectoryInfo> ToDel=new TimeCacheMap<Integer, DirectoryInfo>(UniqConfig.RealTimeDelete(), this);
	private int uniqIndex=0;
	private SolrCore core;
	private PartionKey Partion;

	public void setPartion(PartionKey partion) {
		this.Partion = partion;
	}


	public void setCore(SolrCore core) {
		this.core = core;
	}

	private String baseDir;
	private String hadoopConfDir;
	private String hdfsPath;
	
	private Configuration getConf() {
		Configuration conf = new Configuration();
		HadoopUtil.grabConfiguration(this.hadoopConfDir, conf);
		return conf;
	}
	
    private Thread _cleaner;
    private Thread _doclistthr;


    private boolean ishdfsmode=false;
    private AtomicBoolean isreadOnly=new AtomicBoolean(true);
	public void setIsreadOnly(boolean isreadOnly) {
		this.isreadOnly.set(isreadOnly);
	}


	public RealTimeDirectory(File path,String hadoopConfDir,String hdfsPath) throws IOException
	{
		LOG.info("####"+path.getAbsolutePath()+","+hdfsPath);
		this.hadoopConfDir=hadoopConfDir;
		this.baseDir=path.getAbsolutePath();
		this.hdfsPath=hdfsPath;
		
		Configuration conf=this.getConf();
		FileSystem fs=FileSystem.get(conf);
		
		fs.mkdirs(new Path(hdfsPath).getParent());

		
		path.mkdirs();
		File links=new File(path,"indexLinks");
		if(links.exists())
		{
			FileReader freader= new FileReader(links);
		    BufferedReader br = new BufferedReader(freader);
		    String s1 = null;
		    while ((s1 = br.readLine()) != null) {
		    	if(s1.trim().length()>0)
		    	{
		    		if(s1.startsWith("@hdfs@"))
		    		{
		    			Path p=new Path(s1.replaceAll("@hdfs@", ""));
		    			if(!fs.exists(p))
		    			{
		    				continue;
		    			}
		    			
		    			Path p2=new Path(p.getParent(),"sigment/"+p.getName());
		    			if(fs.exists(p2))
		    			{
		    				FileStatus[] sublist=fs.listStatus(p2);
		    				if(sublist==null)
		    				{
		    					continue;
		    				}
		    				for(FileStatus ssss:sublist)
		    				{
			    				FileSystemDirectory d=new FileSystemDirectory(fs, ssss.getPath(), false, conf);
				    			DirectoryInfo info=new DirectoryInfo();
				    			info.d=d;
				    			info.tp=DirectoryInfo.DirTpe.file;
				    			diskDirector.put(s1+"/sigment/"+ssss.getPath().getName(), info);
				    			ishdfsmode=true;
					    		SolrCore.log.info(">>>>>FileSystemDirectory hdfs add links "+ssss.getPath());
		    				}
		    				
		    			}else{

		    			
		    			FileSystemDirectory d=new FileSystemDirectory(fs, p, false, conf);
		    			DirectoryInfo info=new DirectoryInfo();
		    			info.d=d;
		    			info.tp=DirectoryInfo.DirTpe.file;
		    			diskDirector.put(s1, info);
		    			ishdfsmode=true;
			    		SolrCore.log.info(">>>>>FileSystemDirectory readOnlyOpen add links "+s1);
		    			}
		    		}
		    		
		    		File f=new File(s1);
		    		if(!f.exists())
		    		{
		    			continue;
		    		}
	    			FSDirectory d=LinkFSDirectory.open(f);
	    			DirectoryInfo info=new DirectoryInfo();
	    			info.d=d;
	    			info.tp=DirectoryInfo.DirTpe.file;
	    			diskDirector.put(s1, info);
		    		SolrCore.log.info(">>>>>LinkFSDirectory readOnlyOpen add links "+s1);
		    	}
		    }
		    br.close();
		    freader.close();
		}
		
		
		  _cleaner = new Thread(new Runnable() {
	            public void run() {
	            	if(RealTimeDirectory.this.ishdfsmode)
	            	{
	            		return ;
	            	}
				while (true) {
					try {

						Thread.sleep(UniqConfig.RealTimeHdfsFlush() * 1000l);
						RealTimeDirectory.this.syncHdfs();
					} catch (InterruptedException ex) {

					}
				}
	            }
	        });
	        _cleaner.setDaemon(true);
	        _cleaner.start();
	        _doclistthr = new Thread(new Runnable() {
	            public void run() {
	            	if(RealTimeDirectory.this.ishdfsmode)
	            	{
	            		return ;
	            	}
	            	long lasttime=System.currentTimeMillis();
	            	long tslen=UniqConfig.RealTimeDoclistFlush() * 1000l;
				while (true) {
					try {
						
						Thread.sleep(1000l);
						int size=0;
						synchronized (RealTimeDirectory.this.doclistBuffer_lock) {
							size=RealTimeDirectory.this.doclistBuffer.size();
						}
						long nowtime=System.currentTimeMillis();
						if(size==0)
						{
							continue;
						}
						if(size>=UniqConfig.RealTimeDoclistBuffer()|| (lasttime+tslen)<nowtime)
						{
							RealTimeDirectory.this.flushDocList();
							lasttime=System.currentTimeMillis();
							long ts=lasttime-nowtime;
							LOG.info("flushDocList timetake:"+(ts/1000)+RealTimeDirectory.this.extaLog());
						}

					} catch (Throwable ex) {

					}
				}
	            }
	        });
	        _doclistthr.setDaemon(true);
	        _doclistthr.start();
	        
	        
	}
	
	 @Override
	    protected void finalize() throws Throwable {
	        try {
	            _cleaner.interrupt();
	            _doclistthr.interrupt();
	        } 
	        catch(Throwable e){}
	        finally {
	            super.finalize();
	        }
	    }
	private static Integer RAM_KEY=1;
	private Object lock=new Object(); 

	private void purger(){
		this.remakeSearch();
		SolrResourceLoader.SetCacheFlushKey(this.Partion,System.currentTimeMillis());
		
	}
	
	private String mallocPath()
	{
		while(true)
		{
			File rtn= new File(new File(baseDir,"realtime"),String.valueOf(uniqIndex++)+"_"+java.util.UUID.randomUUID().toString());
			if(rtn.exists())
			{
				continue;
			}
			rtn.mkdirs();
			return rtn.getAbsolutePath();
		}
	}
	
	private void remakeLinkFs() throws IOException
	{
		if(this.ishdfsmode)
		{
			return ;
		}
		File links = new File(baseDir, "indexLinks");
		File links2 = new File(baseDir, "indexLinks_tmp_"+(uniqIndex++)+"_"+System.currentTimeMillis());
		
		File realtime_ts = new File(baseDir, "realtime_ts");
		File realtime_ts2 = new File(baseDir, "realtime_ts_tmp_"+(uniqIndex++)+"_"+System.currentTimeMillis());
		
		FileWriter freader= new FileWriter(links2);
		StringBuffer buffer=new StringBuffer();
		ArrayList<String> toremove=new ArrayList<String>();
		for(Entry<String, DirectoryInfo> e:diskDirector.entrySet())
		{
			File f=new File(e.getKey());
			if(!f.exists())
			{
				toremove.add(e.getKey());
				continue;
			}
			freader.write(e.getKey()+"\r\n");
			buffer.append(e.getKey()+"\r\n");
		}
	
		freader.close();
		
		FileWriter freader2= new FileWriter(realtime_ts2);
		freader2.write(String.valueOf(System.currentTimeMillis()));
		freader2.close();
		
		
		if(links.exists())
		{
			links.delete();
		}
		
		if(realtime_ts.exists())
		{
			realtime_ts.delete();
		}
		
		
		for(String s:toremove)
		{
			diskDirector.remove(s);
		}
		if(toremove.size()>0)
		{
			LOG.info("####toremove####"+toremove.toString()+this.extaLog());

		}
		LOG.info("####remakeLinkFs####"+buffer.toString()+this.extaLog());

		links2.renameTo(links);
		realtime_ts2.renameTo(realtime_ts);

	}
	
	
	private List<Directory> readList=null; 
	private Object readLock=new Object();
	
	private void remakeSearch()
	{
		List<Directory> rtn=new ArrayList<Directory>(); 
	
			for(Entry<String, DirectoryInfo> e:diskDirector.entrySet())
			{
				rtn.add(e.getValue().d);
			}
			
			DirectoryInfo ram=this.RamDirector.get(RAM_KEY);
			if(ram!=null)
			{
				rtn.add(ram.d);
			}
			LOG.info("####remakeSearch####"+rtn.toString()+this.extaLog());	
		
		synchronized (readLock) {
			readList=rtn;
		}

		
	}
	public List<Directory> getForSearch()
	{
		List<Directory> rtn=null;
		synchronized (readLock) {
			rtn=readList;
		}
		
		if(rtn==null)
		{
			synchronized (lock) {
				this.remakeSearch();
			}
		}
		synchronized (readLock) {
			rtn=readList;
		}
		LOG.info("####getForSearch####"+String.valueOf(rtn)+this.extaLog());	
		return rtn;
		
	}
	
    public void commit(){
    	
    }

	@Override
	public void expire(Integer key,DirectoryInfo val) {
		if(this.ishdfsmode)
		{
			return ;
		}
		synchronized (lock) {
			if(val.tp.equals(DirectoryInfo.DirTpe.buffer))
			{
				try {
					this.mergerBuffer(val);
					this.maybeMerger();
				} catch (Throwable e) {
					LOG.error("####expire buffer error ####"+this.extaLog(),e);

				} 
				
				return ;
			}
			
			if(val.tp.equals(DirectoryInfo.DirTpe.ram))
			{
				try {
					this.mergerRam(val);
					this.maybeMerger();
				} catch (Throwable e) {
					LOG.error("####expire ram error ####"+this.extaLog(),e);

				} 
				
				return ;
			}
			
			
			if(val.tp.equals(DirectoryInfo.DirTpe.delete))
			{
				try {
					this.deleteDirector(val);
				} catch (Throwable e) {
					LOG.error("deleteDirector"+this.extaLog(),e);
				} 
				
				return ;
			}
		
		}
	}
	

	public void sync()
	{
		synchronized (lock) {
			flushToDisk();
		}
	}
	
	public void syncHdfs()
	{
		if(!needFlushDfs.get()||isreadOnly.get())
		{
			return ;
		}
		needFlushDfs.set(false);
		synchronized (lock) {
			flushToDisk();
			try {
				saveToHdfs_makelist();
			} catch (Throwable e) {
				LOG.error("syncHdfs"+this.extaLog(),e);
			}
		}
		
		
		try {
			saveToHdfs();
		} catch (Throwable e) {
			LOG.error("syncHdfs"+this.extaLog(),e);
		}
		
	}
	
	private ConcurrentHashMap<String, DirectoryInfo> diskDirector_hdfs=new ConcurrentHashMap<String, DirectoryInfo>();
	private Object diskDirector_hdfs_lock =new Object();

	private void saveToHdfs_makelist() throws IOException
	{
		synchronized (diskDirector_hdfs_lock) {
			remakeLinkFs();
			diskDirector_hdfs=new ConcurrentHashMap<String, DirectoryInfo>(diskDirector);
		}
	}
	
	private void saveToHdfs() throws IOException
	{
		if(this.ishdfsmode)
		{
			return ;
		}
		ConcurrentHashMap<String, DirectoryInfo> buffer=null;
		synchronized (diskDirector_hdfs_lock) {
			buffer=diskDirector_hdfs;
			diskDirector_hdfs=new ConcurrentHashMap<String, DirectoryInfo>(diskDirector);	
		}
	
		Configuration conf=this.getConf();
		FileSystem fs=FileSystem.get(conf);
		Path tmpDir=new Path(hdfsPath,"realtime_tmp");
		if(fs.exists(tmpDir))
		{
			fs.delete(tmpDir, true);
		}
		fs.mkdirs(tmpDir);
		
		Path tslocal=new Path(baseDir, "realtime_ts");
		Path tsdst=new Path(tmpDir,"realtime_ts");
		LOG.info("####copyFromLocalFile####"+tslocal.toString()+","+tsdst.toString());
		fs.copyFromLocalFile(tslocal,tsdst);
		for(Entry<String, DirectoryInfo> e:buffer.entrySet())
		{
			Path local=new Path(e.getKey());
			Path dst=new Path(tmpDir,local.getName());
			LOG.info("####copyFromLocalFile####"+local.toString()+","+dst.toString());
			fs.copyFromLocalFile(local,dst);
		}
		
		Path realtimefinal=new Path(hdfsPath,"realtime");
		if(fs.exists(realtimefinal))
		{
			fs.delete(realtimefinal, true);
		}
		
		fs.rename(tmpDir, realtimefinal);
		
		LOG.info("####saveToHdfs####"+realtimefinal.toString()+this.extaLog());
	}

	
	private static Cache<Long, Object> termsCache = Cache.synchronizedCache(new SimpleLRUCache<Long, Object>(102400));
	
	private static AtomicLong debuglines=new AtomicLong(0); 
	private static synchronized boolean maybeReplication(SolrInputDocument doc,RealTimeDirectory oo)
	{
		if("420434_1006".equals(doc.getFieldValue("pid")))
		{
			LOG.info("addDoc_debug222222 "+doc.toString()+",termsCache.size="+termsCache.size()+oo.extaLog());
		}
		if(debuglines.incrementAndGet()%10000==0)
		{
			LOG.info("addDoc "+doc.toString()+",termsCache.size="+termsCache.size()+oo.extaLog());
			if(debuglines.get()>100000000)
			{
				debuglines.set(0);
			}
		}
		Object uuid=doc.getFieldValue("mdrill_uuid");
		if(uuid!=null)
		{
			try{
				Long uuidl=(Long)uuid;
				synchronized (termsCache) {
					if(termsCache.containsKey(uuidl))
					{
						LOG.info("replication uuid :"+String.valueOf(uuid)+",doc="+doc.toString()+",termsCache.size="+termsCache.size()+oo.extaLog());
						termsCache.put(uuidl, new Object());
						return true;
					}
					termsCache.put(uuidl, new Object());
				}
			}catch(Throwable e)
			{
				LOG.info("uuid is null:doc="+doc.toString()+",termsCache.size="+termsCache.size()+oo.extaLog(),e);
			}
		}else{
			LOG.info("uuid is null:doc="+doc.toString()+",termsCache.size="+termsCache.size()+oo.extaLog());
		}
		
		return false;
	}

	public static Long uuid()
	{
		CRC32 crc32 = new CRC32();
		crc32.update(String.valueOf(java.util.UUID.randomUUID().toString()).getBytes());
		return crc32.getValue();
	}
	long uuid=uuid();
	private AtomicLong total_add_nofilter=new AtomicLong(0); 
	private AtomicLong total_add=new AtomicLong(0); 
	private AtomicBoolean needFlushDfs=new AtomicBoolean(false);

	public synchronized void addDocument(SolrInputDocument doc) throws CorruptIndexException, LockObtainFailedException, IOException
	{
		if(this.ishdfsmode)
		{
			return ;
		}
		total_add_nofilter.incrementAndGet();
		if(maybeReplication(doc,this))
		{
			return ;
		}
		total_add.incrementAndGet();
		
		needFlushDfs.set(true);
		synchronized (doclistBuffer_lock) {
			doclistBuffer.add(doc);
		}

	}
	
	private void flushDocList() throws CorruptIndexException, LockObtainFailedException, IOException
	{
		synchronized (lock) {
			synchronized (doclistBuffer_lock) {
				int size=doclistBuffer.size();
				if(size<=0)
				{
					return ;
				}
			}
			ArrayList<SolrInputDocument> flush=null;
			synchronized (doclistBuffer_lock) {
				flush=doclistBuffer;
				doclistBuffer=new ArrayList<SolrInputDocument>(UniqConfig.RealTimeDoclistBuffer());
			}

			DirectoryInfo buffer=bufferDirector.get(RAM_KEY);
			if(buffer==null)
			{
				buffer=new DirectoryInfo();
				buffer.d=new RAMDirectory();
				buffer.tp=DirectoryInfo.DirTpe.buffer; 
				LOG.info("####create buffer####"+this.extaLog());
				bufferDirector.put(RAM_KEY, buffer);
			}
			
			buffer.d.setSchema(this.core.getSchema());
			IndexWriter writer=new IndexWriter(buffer.d, null,new KeepOnlyLastCommitDeletionPolicy(), MaxFieldLength.UNLIMITED);
//			writer.setMergeScheduler(new SerialMergeScheduler());
			writer.setMergeFactor(10);
			writer.setUseCompoundFile(false);
			
			ArrayList<Document> doclist=new ArrayList<Document>(flush.size());
			for(SolrInputDocument sdoc:flush)
			{
				if("420434_1006".equals(sdoc.getFieldValue("pid")))
				{
					LOG.info("addDoc_debug33333 "+sdoc.toString()+",termsCache.size="+termsCache.size()+this.extaLog());
				}
				sdoc.remove("mdrillPartion");
				sdoc.remove("mdrillCmd");
				sdoc.remove("mdrill_uuid");
				Document lucenedoc= DocumentBuilder.toDocument(sdoc,core.getSchema());
				doclist.add(lucenedoc);
			}
			writer.addDocuments(doclist);
			writer.close();		
			this.maybeMerger();
		}
	}
	
	/**
	 * 将内存中的数据flush到硬盘上
	 */
	private void flushToDisk()
	{
		DirectoryInfo buffer=bufferDirector.get(RAM_KEY);
		if(buffer!=null)
		{
			try {
				this.mergerBuffer(null);
			} catch (Throwable e) {
				LOG.error("mayBeMerger_buffer"+this.extaLog(),e);
			}
		}
		
		
		
		DirectoryInfo ram=this.RamDirector.get(RAM_KEY);
		if(ram!=null)
		{
			try {
				this.mergerRam(null);
			} catch (Throwable e) {
				LOG.error("mayBeMerger_ram"+this.extaLog(),e);
			}
		}
		
	}

	long debugIndex=0;
	long bufferdely=UniqConfig.RealTimeBufferFlush()*1000l;
	long ramdelay=UniqConfig.RealTimeLocalFlush()*1000l;
	

	private String extaLog()
	{
		return "";
//		return "["+this.Partion.partion+"@"+this.Partion.tablename+",add:"+total_add.get()+"@"+total_add_nofilter.get()+",uuid:"+this.uuid+"]";
	}
	private void maybeMerger()
	{
		DirectoryInfo buffer=bufferDirector.get(RAM_KEY);
		if(buffer!=null)
		{
			if(buffer.filelength()>=UniqConfig.RealTimeBufferSize()||(buffer.createtime+bufferdely)<System.currentTimeMillis())
			{
				try {
					this.mergerBuffer(null);
				} catch (Throwable e) {
					LOG.error("mayBeMerger_buffer"+this.extaLog(),e);
				}
			}
		}
		
		
		DirectoryInfo ram=this.RamDirector.get(RAM_KEY);
		if(ram!=null)
		{
			if((debugIndex++)%10==1)
			{
				LOG.info("####ramsize####"+ram.filelength()+","+UniqConfig.RealTimeRamSize()+","+debugIndex+this.extaLog());
				if(debugIndex>100000000)
				{
					debugIndex=0;
				}
			}

			if(ram.filelength()>=UniqConfig.RealTimeRamSize()||(ram.createtime+ramdelay)<System.currentTimeMillis())
			{
				try {
					this.mergerRam(null);
				} catch (Throwable e) {
					LOG.error("mayBeMerger_ram",e);
				}
			}
		}
		
		
		try {
			this.mergerDisk();
		} catch (Throwable e) {
			LOG.error("mayBeMerger_disk",e);
		}

	}
		
	
	long localMergerDelay=UniqConfig.RealTimeLocalMergerInterval()*1000l;
	long lasttime=System.currentTimeMillis();
	private void mergerDisk() throws IOException
	{
		long time=System.currentTimeMillis();
		if((lasttime+localMergerDelay)>=time)
		{
			return ;
		}
		lasttime=time;
		while(true)
		{
			if(this.diskDirector.size()<=UniqConfig.RealTimeLocalMergerFactory())
			{
				return ;
			}
			LOG.info("####mergerDisk####"+this.extaLog());

			ArrayList<Pair> list=this.getSortList();
			
			DirectoryInfo d=new DirectoryInfo();
			String path=this.mallocPath();
			d.d=FSDirectory.open(new File(path));
			d.tp=DirectoryInfo.DirTpe.file;
	
			Pair p1=list.get(0);
			Pair p2=list.get(1);
			this.merger(p1.value, p2.value, d);
			diskDirector.put(path, d);
			this.maybeDelayClear(diskDirector.remove(p1.key));
			this.maybeDelayClear(diskDirector.remove(p2.key));
			this.remakeLinkFs();
			this.purger();
		}
	}
	
	private ArrayList<Pair> getSortList()
	{
		ArrayList<Pair> list=new ArrayList<Pair>();
		for(Entry<String, DirectoryInfo> e:this.diskDirector.entrySet())
		{
			list.add(new Pair(e.getKey(), e.getValue()));
		}
		
		Collections.sort(list);
		return list;
	}
	
	private void mergerRam(DirectoryInfo expire) throws CorruptIndexException, IOException
	{
		LOG.info("####mergerRam####"+this.extaLog());
		DirectoryInfo m1=expire;
			if(m1==null)
			{
				m1=RamDirector.remove(RAM_KEY);
			}
		if(m1==null)
		{
			return ;
		}
		try{
		DirectoryInfo dinfo=null;
		Pair lastPair=null;
		if(this.diskDirector.size()>UniqConfig.RealTimeRam2localFactory())
		{
			ArrayList<Pair> list=this.getSortList();
			lastPair=list.get(0);
			dinfo=lastPair.value;
		}
		
		DirectoryInfo d=new DirectoryInfo();
		String path=this.mallocPath();
		d.d=FSDirectory.open(new File(path));
		d.tp=DirectoryInfo.DirTpe.file;
		this.merger(m1, dinfo, d);
		
		String lastkey="";
		if(lastPair!=null)
		{
			this.maybeDelayClear(diskDirector.remove(lastPair.key));
			lastkey=String.valueOf(lastPair.key);
		}
		

		diskDirector.put(path, d);
		LOG.info("####mergerRam####put =>"+String.valueOf(path) +",remove:"+String.valueOf(lastkey));

		}catch(Throwable e)
		{
			LOG.error("####mergerRam_error####"+this.extaLog(),e);
			this.RamDirector.put(RAM_KEY,m1);

		}finally{
			this.remakeLinkFs();
			this.purger();
		}

		
	}
		
	private void mergerBuffer(DirectoryInfo expire) throws CorruptIndexException, IOException
	{
		LOG.info("####mergerBuffer####"+this.extaLog());
		DirectoryInfo m1=expire;
			if(m1==null)
			{
				m1=this.bufferDirector.remove(RAM_KEY);
			}
		if(m1==null)
		{
			return ;
		}
		try{
		DirectoryInfo d=null;
		
		DirectoryInfo m2=RamDirector.get(RAM_KEY);
		if(m2==null)
		{
			d=m1;
			d.tp=DirectoryInfo.DirTpe.ram;
		}else{
			d=new DirectoryInfo();
			d.createtime=m2.createtime;
			d.d=new RAMDirectory();
			d.tp=DirectoryInfo.DirTpe.ram;
			this.merger(m1, m2, d);
		}
		
		RamDirector.put(RAM_KEY, d);
		}catch(Throwable e)
		{
			LOG.error("####mergerBuffer_error####"+this.extaLog(),e);
			this.bufferDirector.put(RAM_KEY,m1);

		}finally{
			this.purger();
		}
		
		
	}

	
	private void merger(DirectoryInfo m1,DirectoryInfo m2,DirectoryInfo to) throws CorruptIndexException, IOException
	{
		try{
		to.d.setSchema(this.core.getSchema());
		IndexWriter writer=new IndexWriter(to.d, null,new KeepOnlyLastCommitDeletionPolicy(), MaxFieldLength.UNLIMITED);
//		writer.setMergeScheduler(new SerialMergeScheduler());
		writer.setMergeFactor(512);
		writer.setUseCompoundFile(false);
		
		if(m1!=null){
			m1.d.setSchema(this.core.getSchema());
			writer.addIndexesNoOptimize(m1.d);
		}
		if(m2!=null){
			m2.d.setSchema(this.core.getSchema());
			writer.addIndexesNoOptimize(m2.d);
		}
		writer.optimize();
		writer.close();
		}catch(Throwable e)
		{
			LOG.error("####merger####",e);

			throw new IOException(e);
		}
		
	}
	
	private void maybeDelayClear(DirectoryInfo m1)
	{
		if(m1==null)
		{
			return ;
		}
		if(m1.tp.equals(DirectoryInfo.DirTpe.file)||m1.d instanceof FSDirectory)
		{
			m1.tp=DirectoryInfo.DirTpe.delete;
			ToDel.put(uniqIndex++, m1);
		}
	}
	

	public static class Pair implements Comparable<Pair>{
		public String key;
		public Pair(String key, DirectoryInfo value) {
			super();
			this.key = key;
			this.value = value;
		}
		public DirectoryInfo value;
		@Override
		public int compareTo(Pair o) {
			return this.filelength().compareTo(o.filelength());
		}
		
		
		Long filesize=null;
		public Long filelength() 
		{
			if(filesize!=null)
			{
				return filesize;
			}
			Long rtn=this.value.filelength();
			filesize=rtn;
			return rtn;
		}
		
	}
	
	private void deleteDirector(DirectoryInfo m1) throws IOException
	{
		String[] list=m1.d.listAll();
		if(list!=null)
		{
			for(String s:list)
			{
				m1.d.deleteFile(s);
			}
		}
		if(m1.d instanceof FSDirectory)
		{
			FSDirectory fd=(FSDirectory)m1.d;
            FileUtils.forceDelete(fd.getDirectory());
		}
		
		if(m1.d instanceof FileSystemDirectory)
		{
			FileSystemDirectory fd=(FileSystemDirectory)m1.d;
			Configuration conf=this.getConf();
			FileSystem fs=FileSystem.get(conf);
			fs.delete(fd.directory,true);
		}
	}
	
	
}
