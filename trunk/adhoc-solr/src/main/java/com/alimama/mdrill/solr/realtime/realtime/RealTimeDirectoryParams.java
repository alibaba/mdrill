package com.alimama.mdrill.solr.realtime.realtime;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.solr.core.SolrCore;
import org.apache.solr.core.SolrResourceLoader.PartionKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alimama.mdrill.utils.HadoopUtil;
import com.alimama.mdrill.utils.IndexUtils;

//review ok 2013-12-27
public class RealTimeDirectoryParams {
	public static Logger LOG = LoggerFactory.getLogger(RealTimeDirectoryParams.class);

	private static String CHK_UNQ_ID=java.util.UUID.randomUUID().toString();
	public static String diskDirList=null;
	public static String getDiskDirList() {
		return diskDirList;
	}


	public static void setDiskDirList(int taskIndex,String diskDirList) {
		synchronized (diskMallocLock) {
			RealTimeDirectoryParams.diskDirList = diskDirList;
			RealTimeDirectoryParams.taskIndex = taskIndex;
		}

	}


	public static int taskIndex=0;
	private static Object diskMallocLock=new Object();
	
	public SolrCore core;
	public PartionKey Partion;
	public String baseDir;
	public String hadoopConfDir;
	public String hdfsPath;
	public Configuration getConf() {
		Configuration conf = new Configuration();
		HadoopUtil.grabConfiguration(this.hadoopConfDir, conf);
		return conf;
	}
	
	
	public String getIndexMalloc(int hashcode)
	{
		synchronized (diskMallocLock) {
			if(diskDirList!=null)
			{
				try {
					
				  	Configuration conf=this.getConf();
					FileSystem lfs=FileSystem.getLocal(conf);
					
				String partionDisk = IndexUtils.getPath(RealTimeDirectoryParams.diskDirList, taskIndex,hashcode,lfs);
				String tablename=this.Partion==null?"default":this.Partion.tablename;
				String partionName=this.Partion==null?"default":this.Partion.partion;
				Path localPartionStorePath = new Path(new Path(partionDisk, "higo"),tablename + "/" + partionName + "/" + taskIndex );
				lfs.mkdirs(localPartionStorePath);
				return localPartionStorePath.toString();
				} catch (IOException e) {
					LOG.error("getIndexMalloc",e);
				}
			}
		}
		
		return this.baseDir;
	}
	public String getLogStr()
	{
		StringBuffer buff=new StringBuffer();
		if(Partion!=null)
		{
		buff.append(Partion.tablename);
		buff.append("@");
		buff.append(Partion.partion);
		}else{
			buff.append(baseDir);
		}
		return buff.toString();
	}
	
	public boolean checkSyncHdfs() throws IOException
	{
		File links = new File(this.baseDir, "process_check");
		if(links.exists())
		{
			BufferedReader reader=new BufferedReader(new FileReader(links));
			String line;
			while((line=reader.readLine())!=null){
				if(line.indexOf(CHK_UNQ_ID)>=0)
				{
					return true;
				}
			}
			reader.close();
		}
		
		return false;
	}
	
	public void markSyncHdfs() throws IOException
	{
		File links = new File(this.baseDir, "process_check");
		FileWriter freader2= new FileWriter(links);
		freader2.write(String.valueOf(CHK_UNQ_ID));
		freader2.close();
	}
	
	
	 public boolean isUseHdfsIndex() throws IOException
		{
	    	Configuration conf=this.getConf();
	    	final Path hdfsRealtime=new Path(this.hdfsPath,"realtime");
			final FileSystem fs=FileSystem.get(conf);
			final FileSystem lfs=FileSystem.getLocal(conf);
			if(IndexUtils.readReadTimeTs(lfs, new Path(this.baseDir))>=IndexUtils.readReadTimeTs(fs, hdfsRealtime))
			{
				return false;
			}
			
			return true;
		}
	
}
