package com.alimama.mdrill.solr.realtime.mlog;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.alimama.mdrill.solr.realtime.IMessage;

public class MLogSigmentReader {
	private Configuration conf;
	private FileSystem fs;
	private final long startoffset;
	FSDataInputStream reader;
	public MLogSigmentReader(Configuration conf, String filepath, final long offset)
			throws IOException {
		this.conf = conf;
		this.fs = FileSystem.get(this.conf);
		this.reader = this.fs.open(new Path(filepath));
		this.startoffset = offset;
	}
	
	public void read(final IMessage msg) throws IOException
	{
		byte[] d=this.readByte();
		msg.setData(d);
	}
	
	public byte[] readByte() throws IOException
	{
		synchronized (this) {
			int size=reader.readInt();
			byte[] d=new byte[size];
			reader.read(d);
			return d;
		}
	}
	
	public long getPos() throws IOException {
		return this.startoffset + this.reader.getPos();
	}

	public long getCurrPos() throws IOException {
		return this.startoffset +this.reader.getPos();
	}
	
	
	public void close() throws IOException {
		synchronized (this) {
			this.reader.close();
		}

	}
}
