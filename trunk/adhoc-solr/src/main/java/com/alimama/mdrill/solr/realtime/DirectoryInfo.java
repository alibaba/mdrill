package com.alimama.mdrill.solr.realtime;

import org.apache.lucene.store.Directory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectoryInfo {
	public static Logger LOG = LoggerFactory.getLogger(DirectoryInfo.class);

	public static enum DirTpe{
		file,ram,buffer,delete
	}
	public Directory d;
	public long createtime=System.currentTimeMillis();
	public DirTpe tp=DirTpe.file;
	
	
	public Long filelength() 
	{
		Long rtn=0l;
		try{
		String[] list=this.d.listAll();
		if(list!=null)
		{
			for(String d:list)
			{
				rtn+=this.d.fileLength(d);
			}
		}
		}catch(Throwable e)
		{
			LOG.error("filelength",e);

		}
		return rtn;
	}
	

}
