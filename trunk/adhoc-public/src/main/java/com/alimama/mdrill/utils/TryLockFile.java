package com.alimama.mdrill.utils;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TryLockFile {
	  public static Logger log = LoggerFactory.getLogger(TryLockFile.class);

	String localpath = null;
	FileLock flout = null;
	RandomAccessFile out = null;
	FileChannel fcout = null;
	public TryLockFile(String stopath) {
		super();
		this.localpath = stopath;
	}
	
	public void trylock()
	{
		try{
			File file = new File(localpath);
			if (!file.exists()) {
				file.createNewFile();
			}
			out = new RandomAccessFile(file, "rw");
			fcout = out.getChannel();
			
			for(int i=0;i<10000;i++)
			{
				try
				{
					flout = fcout.lock();
					break;
				}catch(OverlappingFileLockException e){
					Thread.sleep(300);
				}
			}
		}catch(Throwable e)
		{
			log.error("trylock",e);
		}
	}
	
	protected void finalize() throws Throwable
    {
		super.finalize();
       this.unlock();
    }
	
	public void unlock()
	{
		try {
			if (flout != null) {
				flout.release();
				flout=null;
			}
			if (fcout != null) {
				fcout.close();
				fcout=null;
			}
			if (out != null) {
				out.close();
				out = null;
			}
		} catch (Exception e) {
			log.error("unlock",e);
		}
	}
	
}
