package com.alimama.mdrill.solr.hbaserealtime;

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

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LinkFSDirectory;
import org.apache.lucene.store.LockObtainFailedException;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.core.SolrCore;
import org.apache.solr.core.SolrResourceLoader.PartionKey;


import com.alimama.mdrill.hdfsDirectory.FileSystemDirectory;
import com.alimama.mdrill.utils.HadoopUtil;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
public class ReadOnlyDirectory implements MdrillDirectory{
	public static Logger LOG = LoggerFactory.getLogger(ReadOnlyDirectory.class);

	private ConcurrentHashMap<String, DirectoryInfo> diskDirector=new ConcurrentHashMap<String, DirectoryInfo>();
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
	
    private boolean ishdfsmode=false;
    private AtomicBoolean isreadOnly=new AtomicBoolean(true);
	public void setIsreadOnly(boolean isreadOnly) {
		this.isreadOnly.set(isreadOnly);
	}

	File path;
	public ReadOnlyDirectory(File path,String hadoopConfDir,String hdfsPath) throws IOException
	{
		this.path=path;
		LOG.info("####"+path.getAbsolutePath()+","+hdfsPath);
		this.hadoopConfDir=hadoopConfDir;
		this.baseDir=path.getAbsolutePath();
		this.hdfsPath=hdfsPath;
		
		
	}
	
	
	private void fresh() throws IOException
	{
		diskDirector.clear();
			
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
		    			Configuration conf=this.getConf();
		    			FileSystem fs=FileSystem.get(conf);
		    			
		    			
		    			Configuration conf_timeout=this.getConf();
		    			conf_timeout.setInt("dfs.socket.timeout", 5000);
		    			FileSystem fstimeout=FileSystem.get(conf_timeout);
		    			
//		    			fs.mkdirs(new Path(hdfsPath).getParent());
		    			Path p=new Path(s1.replaceAll("@hdfs@", ""));
		    			Path p2=new Path(p.getParent(),"sigment/"+p.getName());

		    			if(!fs.exists(p)&&!fs.exists(p2))
		    			{
		    				continue;
		    			}
		    			
		    			if(fs.exists(p2))
		    			{
		    				FileStatus[] sublist=fs.listStatus(p2);
		    				if(sublist==null)
		    				{
		    					continue;
		    				}
		    				for(FileStatus ssss:sublist)
		    				{
			    				FileSystemDirectory d=new FileSystemDirectory(fstimeout, ssss.getPath(), false, conf_timeout);
			    				d.setUsedBlockBuffer(true);
				    			DirectoryInfo info=new DirectoryInfo();
				    			info.d=d;
				    			info.tp=DirectoryInfo.DirTpe.file;
				    			diskDirector.put(s1+"/sigment/"+ssss.getPath().getName(), info);
				    			ishdfsmode=true;
					    		SolrCore.log.info(">>>>>FileSystemDirectory hdfs add links "+ssss.getPath());
		    				}
		    				
		    			}else{

		    			
		    			FileSystemDirectory d=new FileSystemDirectory(fs, p, false, conf);
		    			d.setUsedBlockBuffer(true);
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
	}

	
	public synchronized List<Directory> getForSearch()
	{
		try {
			this.fresh();
		} catch (Throwable e1) {
			LOG.error("getForSearch",e1);
		}
		List<Directory> rtn=new ArrayList<Directory>(); 
		
		for(Entry<String, DirectoryInfo> e:diskDirector.entrySet())
		{
			rtn.add(e.getValue().d);
		}
		
		LOG.info("####remakeSearch readonly ####"+rtn.toString());		
		return rtn;
		
	}


	@Override
	public void addDocument(SolrInputDocument doc)
			throws CorruptIndexException, LockObtainFailedException,
			IOException {
		
	}


	@Override
	public void syncLocal() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void syncHdfs() {
		// TODO Auto-generated method stub
		
	}
	
	
	
}
