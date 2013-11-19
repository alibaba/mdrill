package com.alimama.mdrill.solr.realtime;

import org.apache.hadoop.fs.Path;

public class ShardPartion {

	public static  String base;
	public static int taskIndex;
	public  static  String index;
		
	public static Path getHdfsRealtimePath(String tablename,String partion)
	{
		Path rtn=new Path(base+"/"+tablename+"/index/"+partion,index);
		return rtn;
	}

}
