package com.alimama.mdrill.solr.realtime.realtime;

import java.io.IOException;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.CRC32;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.cache.Cache;
import org.apache.lucene.util.cache.SimpleLRUCache;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alimama.mdrill.hdfsDirectory.FileSystemDirectory;
import com.alimama.mdrill.solr.realtime.DirectoryInfo;


//review ok 2013-12-27
public class RealTimeDirectorUtils {
	public static Logger LOG = LoggerFactory.getLogger(RealTimeDirectorUtils.class);

	public static Timer timerslow = new Timer();
	public static Timer timerquick = new Timer();


	private static Cache<Long, Object> termsCache = Cache.synchronizedCache(new SimpleLRUCache<Long, Object>(102400));
	
	private static AtomicLong debuglines=new AtomicLong(0); 
	
	

	public static void deleteDirector(DirectoryInfo m1,Configuration conf) throws IOException
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
			FileSystem fs=FileSystem.get(conf);
			fs.delete(fd.directory,true);
		}
	}
	public static synchronized boolean maybeReplication(SolrInputDocument doc)
	{
		if(debuglines.incrementAndGet()%10000==0)
		{
			LOG.info("addDoc "+doc.toString()+",termsCache.size="+termsCache.size());
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
//						LOG.info("replication uuid :"+String.valueOf(uuid)+",doc="+doc.toString()+",termsCache.size="+termsCache.size());
						termsCache.put(uuidl, new Object());
						return true;
					}
					termsCache.put(uuidl, new Object());
				}
			}catch(Throwable e)
			{
				LOG.info("uuid is null:doc="+doc.toString()+",termsCache.size="+termsCache.size(),e);
			}
		}else{
			LOG.info("uuid is null:doc="+doc.toString()+",termsCache.size="+termsCache.size());
		}
		
		return false;
	}

	public static Long uuid()
	{
		CRC32 crc32 = new CRC32();
		crc32.update(String.valueOf(java.util.UUID.randomUUID().toString()).getBytes());
		return crc32.getValue();
	}
}
