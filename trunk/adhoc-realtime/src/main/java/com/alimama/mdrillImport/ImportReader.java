package com.alimama.mdrillImport;

import java.io.*;
import java.util.*;
import java.nio.charset.Charset;

import org.apache.commons.logging.*;



public class ImportReader
{
	
	
    private static final Log LOG = LogFactory.getLog(ImportReader.class.getName());

    private final static int NEW_ENTRIES_COUNT = 20;


    final Stat stat;

     RawDataReader rawDataReader;

    final Parser parser;


    static final Charset charset;
    static {
    	charset = Charset.forName("UTF-8");
    }
    
    public static abstract class RawDataReader
    {
		public  abstract void init(Map config, String confPrefix,
				     int readerIndex, int readerCount)
		    throws IOException;
	
		public abstract List<Object> read()
		    throws IOException;
	
		public abstract void close()
		    throws IOException;
    }

    public ImportReader(Map conf, String confPrefix, Parser parser,
		    int readerIndex, int readerCount)
	throws IOException
    {
		stat = new Stat();
		try {
			rawDataReader=(RawDataReader) Class.forName(String.valueOf(conf.get(confPrefix+"-reader"))).newInstance();
			rawDataReader.init(conf, confPrefix, readerIndex, readerCount);
		} catch (Throwable e1) {
			LOG.error("RawDataReader",e1);
		}
	    this.parser = parser;
    }


	public synchronized List read() throws IOException {
		List<Object> rawData = rawDataReader.read();
		List entries = new ArrayList(NEW_ENTRIES_COUNT);
		if (rawData != null&&rawData.size()>0) {
			for (Object str : rawData) {

				try {
					stat.printlog(str);
					Object e = parser.parse(str);
					if(e!=null)
					{
						stat.valid++;
						entries.add(e);
					}else{
						stat.invalid++;
						stat.debugError(str);
					}
				} catch (InvalidEntryException iee) {
					stat.invalid++;
					stat.debugError(str);

				}
			}
		}else{
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
		return entries;
	}




    public static class Stat
 {
		public long size;

		public long valid;

		public long invalid;
		public long debuglines = 0;
		long debugts = System.currentTimeMillis() / 300000;

		public Stat() {
			size = 0;
			valid = 0;
			invalid = 0;
		}

		public long print = 0;
		long printts = System.currentTimeMillis() / 300000;

		public void printlog(Object s) {
			
		}
		public void printlog(String s) {
			print++;

			if (print < 20) {
				if (!s.isEmpty() && s.length() < 500) {
					LOG.info("loginfo " + s);
				} 
			}

			if (print % 10000 == 0) {
				long nowts = System.currentTimeMillis() / 300000;
				if (nowts != printts) {
					printts = nowts;
					print = 0;
				}

			}
		}
		public void debugError(Object s) {
			
		}

			

		public void debugError(String s) {
			debuglines++;

			if (debuglines < 20) {
				if (!s.isEmpty() && s.length() < 500) {
					LOG.error("wronglog:" + s);
				} 
			}

			if (debuglines % 10000 == 0) {
				long nowts = System.currentTimeMillis() / 300000;
				if (nowts != debugts) {
					debugts = nowts;
					debuglines = 0;
				}

			}

		}

		public Stat(Stat stat) {
			size = stat.size;
			valid = stat.valid;
			invalid = stat.invalid;
		}
	}
}


