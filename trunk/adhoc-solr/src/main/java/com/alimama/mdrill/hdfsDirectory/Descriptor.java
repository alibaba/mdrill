package com.alimama.mdrill.hdfsDirectory;

import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.alimama.mdrill.adhoc.TimeCacheMapLru;


// shared by clones
public class Descriptor {
	    private static final Log logger = LogFactory.getLog(Descriptor.class);
		private static TimeCacheMapLru.ExpiredCallback<String, Descriptor> callback=new TimeCacheMapLru.ExpiredCallback<String, Descriptor>() {
			@Override
			public void expire(String key, Descriptor val) {
				synchronized (val) {
					try {
						val.close();
					} catch (IOException e) {
						logger.error("expire",e);
					}
				}
			}

			@Override
			public void commit() {
				
			}
		
		};
		private static TimeCacheMapLru<String, Descriptor> RamDirector=new TimeCacheMapLru<String, Descriptor>(1024,600,3,callback );


	    private String uuid=java.util.UUID.randomUUID().toString();
	    private  FSDataInputStream in;
	    private long position; // cache of in.getPos()
	    private Path file;
	    
	    private FileSystem fs;
	    private int ioFileBufferSize;
	    long index=0;

	    private long tlSum=0;
		private long tlCount=0;
	    public Descriptor(FileSystem fs,Path _file, int ioFileBufferSize)
		    throws IOException {
	    	this.position=0l;
	    	this.file=_file;
	    	this.fs=fs;
	    	this.ioFileBufferSize=ioFileBufferSize;
	    }
	    
	    public void Stat(long tl,long tl2,int len)
	    {
	    	tlSum+=tl;
	  	    tlCount+=1;
	  	  tlSum+=tl;
		    tlCount+=1;
		    if(tl2>100||tlCount%1000==0)
		    {
		    	logger.info("readInternal "+this.file.getName()+" timetaken="+tl+"@"+tl2+",tlSum="+tlSum+",tlCount="+tlCount+",len="+len);
		    	if(tlSum>10000000)
			    {
			    	tlCount=0;
			    	tlSum=0;
			    }
		    }
	    }
	    
	    public FSDataInputStream Stream() throws IOException
	    {
	    	if(this.in==null)
	    	{
	    		long t1=System.currentTimeMillis();
	    		this.in = fs.open(file, ioFileBufferSize);
	    		if(this.position>0)
	    		{
	    			this.in.seek(this.position);
	    		}
	    		RamDirector.put(this.uuid, this);
	    		long t2=System.currentTimeMillis();
	    		long tl=t2-t1;
	    		if(tl>100)
	    		{
	    			logger.info("fs.open "+file.getName()+" timetaken "+tl);
	    		}

	    	}
	    	
	    	if(index++>10)
	    	{
	    		RamDirector.put(this.uuid, this);
	    		index=0;
	    	}
			
			return this.in;

	    }
	    
	    public long Positon()
	    {
	    	return this.position;
	    }
	    
	    public void setPositon(long pos)
	    {
	    	this.position=pos;
	    }
	    
	    public void addPositon(long pos)
	    {
	    	this.position+=pos;
	    }
	    
	    public void close() throws IOException
	    {
	    	if(this.in!=null)
	    	{
	    		this.in.close();
	    		
	        	logger.info("close "+this.file.getName()+" tlSum="+tlSum+",tlCount="+tlCount);
	        	tlCount=0;
		    	tlSum=0;
	    		this.in=null;
	    	}
	    }
	    
	}