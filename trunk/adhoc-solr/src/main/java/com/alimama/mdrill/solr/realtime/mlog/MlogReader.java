package com.alimama.mdrill.solr.realtime.mlog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.alimama.mdrill.solr.realtime.IMessage;

public class MlogReader {
	private Configuration conf;
	private FileSystem fs;
	private long minStart=0;
	private long maxEnd=0;
	
	private long currStart=0;
	private int currIndex=0;
	private long currEnd=0;
	private MLogSigmentReader currReader=null;
	private String filepath;
	private ArrayList<Long> list=new ArrayList<Long>();
	
	public MlogReader(Configuration conf,String filepath) throws IOException {
		this.conf = conf;
		this.fs = FileSystem.get(this.conf);
		this.minStart = 0;
		this.filepath=filepath;
		this.init();
	}
	
	public long getMaxEnd()
	{
		return this.maxEnd;
	}
	
	public boolean read(final IMessage msg) 
	{
		while (true) {
			try {
				if (minStart >= maxEnd) {
					return false;
				}

				if (this.currReader == null) {
					this.currReader = new MLogSigmentReader(conf, filepath,
							currStart);
					this.currReader=null;
				}

				if (this.currReader.getPos() >= this.currEnd) {
					if (!this.nextIndex()) {
						return false;
					}
				}

				this.currReader.read(msg);
				return true;

			} catch (IOException e) {
				try {
					this.nextIndex();
				} catch (IOException e1) {
				}
			}
		}
	}
	
	public long getPos() throws IOException {
		if(this.currReader!=null)
		{
			return this.currReader.getPos();
		}
		return this.minStart;
	}
	
	public void close() throws IOException
	{
		if(this.currReader!=null)
		{
			 this.currReader.close();
		}
	}

		
	
	private boolean nextIndex() throws IOException
	{
		if(this.currReader!=null)
		{
			this.currReader.close();
			this.currReader=null;
		}
		this.currIndex++;
		if(this.currIndex>=this.list.size())
		{
			return false;
		}
		this.setCurrIndex(this.currIndex);
		this.currReader=new MLogSigmentReader(conf, filepath, currStart);
		return true;
	}
	
	private void setList()
	{
		try {
			FileStatus[] flist=this.fs.listStatus(new Path(this.filepath));
			if(flist!=null)
			{
				for(FileStatus s:flist)
				{
					String name=s.getPath().getName();
					if(MLogUtil.isSigment(name))
					{
						this.list.add(MLogUtil.parseSigment(name));
					}
				}
			}
		} catch (IOException e) {
			
		}
		Collections.sort(this.list);
	}
	
	private void init()
	{
		this.minStart=0;
		this.maxEnd=0;
		this.currStart=0;
		this.currEnd=0;
		this.currIndex=0;
		
		this.setList();
		
		if(this.list.size()>0)
		{
			this.minStart=this.list.get(0);
			this.setMaxEnd();
			this.setCurrIndex(0);
		}
		
	}
	
	
	private void setMaxEnd()
	{
		long endfile=this.list.get(this.list.size()-1);
		String strendfile=this.filepath+"/"+MLogUtil.parseFile(endfile);
		
		try {
			MLogSigmentReader sigread=new MLogSigmentReader(this.conf, strendfile, endfile);
			while(true)
			{
				try {
					sigread.readByte();
				} catch (IOException e) {
					break;
				}
			}
			this.maxEnd=sigread.getPos();
			sigread.close();
		} catch (IOException e) {
			
		}
	}
	
	private void setCurrIndex(int index)
	{
		this.currStart=this.list.get(index);
		this.currIndex=index;
		if(this.list.size()>(index+1))
		{
			this.currEnd=this.list.get(index+1);
		}else{
			this.currEnd=this.maxEnd;
		}
	}

}
