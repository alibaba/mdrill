package com.alimama.mdrill.solr.hbaserealtime;

import java.io.IOException;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectoryInfo {
	public static Logger LOG = LoggerFactory.getLogger(DirectoryInfo.class);

	public static enum DirTpe{
		hdfs,file,ram,buffer,delete
	}
	public Directory d;
	public long createtime=System.currentTimeMillis();
	public DirTpe tp=DirTpe.file;
	public long txid=0;
	
	public void UpTxid(long txidp)
	{
		this.txid=Math.max(this.txid, txidp);
	}
	
	public long readTxid() throws IOException
	{
		if(txid>0)
		{
			return txid;
		}
		if(!d.fileExists("txid"))
		{
			return 0l;
		}
		IndexInput in=d.openInput("txid");
		
		long rtn=0l;
		try{
			rtn=Long.parseLong(in.readString());
		}catch (Throwable e) {
			rtn=0;
		}
		in.close();
		return rtn;
	}
	
	public void synctxid() throws IOException
	{
		if(d.fileExists("txid"))
		{
			d.deleteFile("txid");
		}
		IndexOutput out=d.createOutput("txid");
		out.writeString(String.valueOf(this.txid));
		out.close();
	}
	
	
	
	public Long filelength() 
	{
		Long rtn=0l;
		try{
		String[] list=this.d.listAll();
		if(list!=null)
		{
			for(String d:list)
			{
				try{
					rtn+=this.d.fileLength(d);
				}catch(Throwable e)
				{
					LOG.error("getfilelen:"+this.d.toString(),e);

				}
			}
		}
		}catch(Throwable e)
		{
			LOG.error("filelength",e);

		}
		return rtn;
	}
	

}
