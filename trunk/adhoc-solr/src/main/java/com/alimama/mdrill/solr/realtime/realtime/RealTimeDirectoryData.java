package com.alimama.mdrill.solr.realtime.realtime;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.CRC32;

import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LinkFSDirectory;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.core.SolrCore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alimama.mdrill.adhoc.TimeCacheMap;
import com.alimama.mdrill.solr.realtime.DirectoryInfo;
import com.alimama.mdrill.solr.realtime.RealTimeDirectory;
import com.alimama.mdrill.utils.UniqConfig;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;


import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.KeepOnlyLastCommitDeletionPolicy;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.solr.update.DocumentBuilder;


import com.alimama.mdrill.solr.realtime.realtime.RealTimeDirectoryParams;
import com.alimama.mdrill.solr.realtime.realtime.RealTimeDirectoryStatus;
import com.alimama.mdrill.solr.realtime.realtime.RealTimeDirectoryData;

//review ok 2013-12-28
public class RealTimeDirectoryData {
	public static Logger LOG = LoggerFactory.getLogger(RealTimeDirectoryData.class);
	private ConcurrentHashMap<String, DirectoryInfo> diskDirector=new ConcurrentHashMap<String, DirectoryInfo>();
	private TimeCacheMap<Integer, DirectoryInfo> RamDirector=null;
	private TimeCacheMap<Integer, DirectoryInfo> bufferDirector=null;
	private TimeCacheMap<Integer, DirectoryInfo> ToDel=null;
	private ArrayList<SolrInputDocument> doclistBuffer=new ArrayList<SolrInputDocument>(UniqConfig.RealTimeDoclistBuffer());

	private RealTimeDirectoryParams params;
	private RealTimeDirectoryStatus status;
	private static Integer RAM_KEY=1;

	public RealTimeDirectoryData(RealTimeDirectoryParams params,
			RealTimeDirectoryStatus rstatus,RealTimeDirectory mainthr) {
		this.params = params;
		this.status = rstatus;
		this.RamDirector=new TimeCacheMap<Integer, DirectoryInfo>(RealTimeDirectorUtils.getSlowTimer(),UniqConfig.RealTimeLocalFlush(), mainthr);
		this.bufferDirector=new TimeCacheMap<Integer, DirectoryInfo>(RealTimeDirectorUtils.getQuickTimer(),UniqConfig.RealTimeBufferFlush(), mainthr);
		this.ToDel=new TimeCacheMap<Integer, DirectoryInfo>(RealTimeDirectorUtils.getSlowTimer(),UniqConfig.RealTimeDelete(), mainthr);
	}
	
	public int doclistSize()
	{
		return doclistBuffer.size();
	}
	
	
	public void AddIndex(String path,DirectoryInfo dir)
	{
		diskDirector.put(path, dir);
	}
	
	public void AddDoc(SolrInputDocument doc)
	{
		doclistBuffer.add(doc);
	}

	public List<Directory> copyForSearch() {
		List<Directory> rtn = new ArrayList<Directory>();
		for (Entry<String, DirectoryInfo> e : diskDirector.entrySet()) {
			rtn.add(e.getValue().d);
		}
		DirectoryInfo ram = this.RamDirector.get(RAM_KEY);
		if (ram != null) {
			rtn.add(ram.d);
		}
		LOG.info("####remakeSearch####" + rtn.toString());
		return rtn;
	}
		
	public ConcurrentHashMap<String, DirectoryInfo>  copyForSyncHdfs()
	{
		ConcurrentHashMap<String, DirectoryInfo> diskDirector_hdfs=new ConcurrentHashMap<String, DirectoryInfo>();
		for(Entry<String, DirectoryInfo> e:diskDirector.entrySet())
		{
			diskDirector_hdfs.put(e.getKey(), e.getValue());
		}
		return diskDirector_hdfs;
	}
	
		
	public String  remakeIndexLinksFile() throws IOException
	{
		File linksTmp = new File(params.baseDir, "indexLinks_tmp_"+(status.uniqIndex.incrementAndGet())+"_"+System.currentTimeMillis());
		File realtime_tsTmp = new File(params.baseDir, "realtime_ts_tmp_"+(status.uniqIndex.incrementAndGet())+"_"+System.currentTimeMillis());
		
		OutputStreamWriter fwriter= new OutputStreamWriter(new FileOutputStream(linksTmp));
		ArrayList<String> toremove=new ArrayList<String>();
		for(Entry<String, DirectoryInfo> e:diskDirector.entrySet())
		{
			File f=new File(e.getKey());
			if(!f.exists())
			{
				toremove.add(e.getKey());
				continue;
			}
			fwriter.write(e.getKey()+"\r\n");
		}
	
		fwriter.close();
		
		
		OutputStreamWriter fwriterTs= new OutputStreamWriter(new FileOutputStream(realtime_tsTmp));
		String tstag=String.valueOf(System.currentTimeMillis());
		fwriterTs.write(tstag);
		fwriterTs.close();
		
		File links = new File(params.baseDir, "indexLinks");
		if(links.exists())
		{
			links.delete();
		}
		
		File realtime_ts = new File(params.baseDir, "realtime_ts");
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
			LOG.info("####toremove####"+toremove.toString());
		}

		linksTmp.renameTo(links);
		realtime_tsTmp.renameTo(realtime_ts);
		return tstag;
	}
	
	public long getMaxTxidFromLocal() throws IOException
	{
		long savedTxid=0l;
		for(Entry<String, DirectoryInfo> info:diskDirector.entrySet())
		{
			savedTxid=Math.max(savedTxid, info.getValue().readTxid());
		}
		
		return savedTxid;
	}
	
	
	private int maybeDelayClear(DirectoryInfo m1)
	{
		if(m1==null)
		{
			return -1;
		}
		if(m1.tp.equals(DirectoryInfo.DirTpe.file)||m1.d instanceof FSDirectory)
		{
			m1.tp=DirectoryInfo.DirTpe.delete;
			int key=status.uniqIndex.incrementAndGet();
			ToDel.put(key, m1);
			return key;
		}
		
		return -1;
	}
	
	public void mergerBuffer(DirectoryInfo expire)
			throws CorruptIndexException, IOException {
		LOG.info("####mergerBuffer####");
		DirectoryInfo m1 = expire;
		if (m1 == null) {
			m1 = this.bufferDirector.remove(RAM_KEY);
		}
		
		if (m1 == null) {
			return;
		}
		
		try {
			DirectoryInfo d = null;

			DirectoryInfo m2 = RamDirector.get(RAM_KEY);
			if (m2 == null) {
				d = m1;
				d.UpTxid(m1.readTxid());
				d.tp = DirectoryInfo.DirTpe.ram;
			} else {
				d = new DirectoryInfo();
				d.createtime = m2.createtime;
				d.d = new RAMDirectory();
				d.tp = DirectoryInfo.DirTpe.ram;
				ArrayList<DirectoryInfo> merger=new ArrayList<DirectoryInfo>();
				merger.add(m1);
				merger.add(m2);
				this.merger(merger, d);
			}

			RamDirector.put(RAM_KEY, d);
		} catch (Throwable e) {
			LOG.error("####mergerBuffer_error####", e);
			this.bufferDirector.put(RAM_KEY, m1);

		} finally {
			this.status.needPurger.set(true);
		}

	}
	
	public static SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMddHHmmss");

	private String mallocPath() {
		while (true) {
			
			CRC32 crc32 = new CRC32();
			crc32.update(String.valueOf(java.util.UUID.randomUUID().toString()).getBytes());
			Long uuid=crc32.getValue();
			String pathname=String.valueOf(fmt.format(new Date())+"_"+status.uniqIndex.incrementAndGet()) + "_"	+ uuid;
			File rtn = new File(new File(params.getIndexMalloc(pathname.hashCode()), "realtime"),pathname);
			if (rtn.exists()) {
				continue;
			}
			rtn.mkdirs();
			return rtn.getAbsolutePath();
		}
	}

	public void mergerRam(DirectoryInfo expire) throws CorruptIndexException, IOException
	{
		LOG.info("####mergerRam####");
		DirectoryInfo m1 = expire;
		if (m1 == null) {
			m1 = RamDirector.remove(RAM_KEY);
		}
		if (m1 == null) {
			return;
		}
		try{
			
			ArrayList<Pair> pairlist=new ArrayList<Pair>();
			ArrayList<DirectoryInfo> dinfolist=new ArrayList<DirectoryInfo>();
		
			if(this.diskDirector.size()>UniqConfig.RealTimeMaxIndexCount())
			{
				ArrayList<Pair> list=this.getSortList();
				for(int i=0;i<list.size()&&i<UniqConfig.RealTimeMergeFactor();i++)
				{
					Pair p=list.get(i);
					pairlist.add(p);
					dinfolist.add(p.value);
				}
			}
			
			DirectoryInfo d=new DirectoryInfo();
			String path=this.mallocPath();
			d.d=LinkFSDirectory.open(new File(path),true);
			d.tp=DirectoryInfo.DirTpe.file;
			dinfolist.add(m1);
			this.merger(dinfolist, d);
			
			StringBuffer lastkey=new StringBuffer();
			if(pairlist.size()>0)
			{
				for(Pair lastPair:pairlist)
				{
					this.maybeDelayClear(lastPair.value);
					diskDirector.remove(lastPair.key);
					lastkey.append(String.valueOf(lastPair.key)).append(",");
				}
			}
			diskDirector.put(path, d);
			LOG.info("####mergerRam####put =>"+String.valueOf(path) +",remove:"+lastkey.toString());

		}catch(Throwable e)
		{
			LOG.error("####mergerRam_error####",e);
			this.RamDirector.put(RAM_KEY,m1);

		}finally{
			this.status.needRemakeLinks.set(true);
		}
	}
	

	public void maybeMerger()
	{
		DirectoryInfo buffer=bufferDirector.get(RAM_KEY);
		if(buffer!=null)
		{
			if(buffer.filelength()>=UniqConfig.RealTimeBufferSize()||(buffer.createtime+bufferdely)<System.currentTimeMillis())
			{
				try {
					this.mergerBuffer(null);
				} catch (Throwable e) {
					LOG.error("mayBeMerger_buffer",e);
				}
			}
		}
		
		
		DirectoryInfo ram=this.RamDirector.get(RAM_KEY);
		if(ram!=null)
		{
			if((debugIndex++)%10==1)
			{
				LOG.info("####ramsize####"+ram.filelength()+","+UniqConfig.RealTimeRamSize()+","+debugIndex);
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
	
		
	public static class Pair implements Comparable<Pair>{
		public String key;
		Long filesize=null;
		public DirectoryInfo value;

		public Pair(String key, DirectoryInfo value) {
			this.key = key;
			this.value = value;
		}
		@Override
		public int compareTo(Pair o) {
			return this.filelength().compareTo(o.filelength());
		}
		
		
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
	
	

	/**
	 * 将内存中的数据flush到硬盘上
	 */
	public void flushToDisk()
	{
		DirectoryInfo buffer=bufferDirector.get(RAM_KEY);
		if(buffer!=null)
		{
			try {
				this.mergerBuffer(null);
			} catch (Throwable e) {
				LOG.error("mayBeMerger_buffer",e);
			}
		}
		
		
		
		DirectoryInfo ram=this.RamDirector.get(RAM_KEY);
		if(ram!=null)
		{
			try {
				this.mergerRam(null);
			} catch (Throwable e) {
				LOG.error("mayBeMerger_ram",e);
			}
		}
	}

	long debugIndex=0;
	long bufferdely=UniqConfig.RealTimeBufferFlush()*1000l;
	long ramdelay=UniqConfig.RealTimeLocalFlush()*1000l;
	

	private void merger(ArrayList<DirectoryInfo> mlist,DirectoryInfo to) throws CorruptIndexException, IOException
	{
		try{
			long txid=0;
			to.d.setSchema(params.core.getSchema());
			IndexWriter writer=new IndexWriter(to.d, null,new KeepOnlyLastCommitDeletionPolicy(), MaxFieldLength.UNLIMITED);
			writer.setMergeFactor(64);
			writer.setUseCompoundFile(false);
			
			for(DirectoryInfo tomr:mlist)
			{
				if(tomr!=null){
					tomr.d.setSchema(params.core.getSchema());
					txid=Math.max(txid, tomr.readTxid());
					writer.addIndexesNoOptimize(tomr.d);
				}
			}
			writer.optimize();
			writer.close();
			
			to.UpTxid(txid);
			to.synctxid();
		}catch(Throwable e)
		{
			LOG.error("####merger####",e);
			throw new IOException(e);
		}
	}
	
	public ArrayList<SolrInputDocument> popDoclist() {
		int size = doclistBuffer.size();
		if (size <= 0) {
			return null;
		}

		ArrayList<SolrInputDocument> flush = doclistBuffer;
		doclistBuffer = new ArrayList<SolrInputDocument>(UniqConfig.RealTimeDoclistBuffer());
		return flush;
	}
	
	public void flushDocList(ArrayList<SolrInputDocument> flush)
			throws CorruptIndexException, LockObtainFailedException,
			IOException {
		DirectoryInfo buffer = bufferDirector.get(RAM_KEY);
		if (buffer == null) {
			buffer = new DirectoryInfo();
			buffer.d = new RAMDirectory();
			buffer.tp = DirectoryInfo.DirTpe.buffer;
			LOG.info("####create buffer####");
			bufferDirector.put(RAM_KEY, buffer);
		}

		buffer.d.setSchema(params.core.getSchema());
		IndexWriter writer = new IndexWriter(buffer.d, null,
				new KeepOnlyLastCommitDeletionPolicy(),
				MaxFieldLength.UNLIMITED);
		writer.setMergeFactor(10);
		writer.setUseCompoundFile(false);

		ArrayList<Document> doclist = new ArrayList<Document>(flush.size());
		long txid = 0;
		for (SolrInputDocument sdoc : flush) {
			txid = Math.max(txid, sdoc.getTxid());
			sdoc.remove("mdrillPartion");
			sdoc.remove("mdrillCmd");
			sdoc.remove("mdrill_uuid");
			
			
			Document lucenedoc = DocumentBuilder.toDocument(sdoc,
					params.core.getSchema());
			doclist.add(lucenedoc);
		}
		writer.addDocuments(doclist);
		writer.close();
		buffer.UpTxid(txid);

		this.maybeMerger();
	}

	public void initDiskDirector(boolean isUsedHdfs) throws IOException {
		File links=new File(this.params.baseDir,"indexLinks");
		if(isUsedHdfs)
		{
			if(links.exists())
			{
				links.delete();
			}
			return ;
		}
		if (links.exists()) {
			FileReader freader = new FileReader(links);
			BufferedReader br = new BufferedReader(freader);
			String s1 = null;
			while ((s1 = br.readLine()) != null) {
				if (s1.trim().length() > 0) {
					File f = new File(s1);
					if (!f.exists()) {
						continue;
					}
					FSDirectory d = LinkFSDirectory.open(f,true);
					DirectoryInfo info = new DirectoryInfo();
					info.d = d;
					info.tp = DirectoryInfo.DirTpe.file;
					this.diskDirector.put(s1, info);
					SolrCore.log
							.info(">>>>> add links "
									+ s1);
				}
			}
			br.close();
			freader.close();
		}

	}

}
