package com.alimama.mdrill.solr.realtime.mlog;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.alimama.mdrill.solr.realtime.IMessage;

public class MLogSigmentWrite {
	private Configuration conf;
	private FileSystem fs;

	private FSDataOutputStream write;

	private final long startoffset;

	public MLogSigmentWrite(Configuration conf, String filepath, final long offset)
			throws IOException {
		this.conf = conf;
		this.fs = FileSystem.get(this.conf);
		this.write = this.fs.create(new Path(filepath));
		this.startoffset = offset;
	}


	public void append(final IMessage msg) throws IOException {
		synchronized (this) {
			this.write.write(msg.getData());
		}

	}

	public long getPos() throws IOException {
		return this.startoffset + this.write.getPos();
	}

	public long getCurrPos() throws IOException {
		return this.startoffset + this.write.getPos();
	}

	public void sync() throws IOException {
		synchronized (this) {
			this.write.flush();
			this.write.sync();
		}

	}

	public void close() throws IOException {
		synchronized (this) {
			this.write.close();
		}

	}

}
