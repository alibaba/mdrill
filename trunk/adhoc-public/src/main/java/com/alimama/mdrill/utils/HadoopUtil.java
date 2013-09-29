package com.alimama.mdrill.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;







public class HadoopUtil {
	
	public static String hadoopConfDir;
	public static void setHdfsConfDir(String dir)
	{
		hadoopConfDir=dir;
	}
	
		public static long duSize(FileSystem fs,Path dataPath) throws IOException{
			long size = 0L;
			org.apache.hadoop.fs.FileStatus[] fileStatus = new FileStatus[0];
			try {
				if(fs.exists(dataPath))
				{
					fileStatus = fs.listStatus(dataPath);
				}
			} catch (IOException e) {
			}
			if(fileStatus != null){
				for(FileStatus file : fileStatus){
					String pathname=file.getPath().getName();
					if(pathname.startsWith(".")||pathname.startsWith("_"))
					{
						continue;
					}
					if(file.isDir())
					{
						size+=duSize(fs,file.getPath());
					}else{
						size += file.getLen();
					}
				}
			}
			return size;
		}
		
	
		
		public static void cleanHistoryFile(FileSystem fs,Path dataPath) throws IOException
		{
				long cleantimes=System.currentTimeMillis()-1000l*3600*24*7;
				if(!fs.exists(dataPath))
				{
					return ;
				}
				FileStatus[] list=fs.listStatus(dataPath);
				if(list==null)
				{
					return ;
				}
				
				ArrayList<Path> toremove=new ArrayList<Path>();
				for(FileStatus s:list)
				{
					
					long lasttimes=Math.max(s.getAccessTime(), s.getModificationTime());
					if(lasttimes<cleantimes)
					{
						toremove.add(s.getPath());
					}
				}
				
				for(Path p:toremove)
				{
					System.out.println("clean :"+p.toString());
					fs.delete(p, true);
				}
		}
	
	public static Configuration getConf(Map stormconf) {
		Configuration conf = new Configuration();
		String hadoopConfDir=(String) stormconf.get("hadoop.conf.dir");
		if(HadoopUtil.hadoopConfDir!=null&&HadoopUtil.hadoopConfDir.length()>0)
		{
			hadoopConfDir=HadoopUtil.hadoopConfDir;
		}

		return grabConfiguration(hadoopConfDir, conf);
	}
	
    
    public static Configuration grabConfiguration(String hadoopConfDir, Configuration conf){
		return HadoopBaseUtils.grabConfiguration(hadoopConfDir, conf);
	}
}
