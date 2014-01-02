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

import com.alimama.mdrill.utils.HadoopUtil;
import com.alimama.mdrill.utils.IndexUtils;

//review ok 2013-12-27
public class RealTimeDirectoryParams {
	private static String CHK_UNQ_ID=java.util.UUID.randomUUID().toString();

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
