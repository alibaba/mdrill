package com.alimama.mdrill.solr.realtime;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;



public class BinLog {
    private final static Logger log = Logger.getLogger(BinLog.class);

	private MessageStore worker;
	private AtomicLong pos=new AtomicLong(0);

	public BinLog(String workerpath) throws IOException {
		this(workerpath, 0);
	}
	public BinLog(String workerpath,long pos) throws IOException {
		 FileUtils.forceMkdir(new File(workerpath));
		this.pos.set(pos);
		this.worker = new MessageStore(workerpath);
	}
	
	
	public BinLog(BinLog binlog,long pos) throws IOException {
		this.pos.set(pos);
		this.worker = binlog.worker;
	}
	
	public MessageStore getStore()
	{
		return this.worker;
	}
	
	
	
	

	
	public long getpos()
	{
		return pos.get();
	}

	
	public int read(final IMessage bf) throws IOException {
		final long len = this.worker.getMaxOffset();
		AtomicLong pos = this.pos;
		while (pos.get() < len) {
			try {
				int size = this.worker.read(bf, pos.get());
				pos.addAndGet(size);
				return size;
			} catch (final ArrayIndexOutOfBoundsException e) {
				pos.set(this.worker.getNearestOffset(pos.get()));
			}
			 catch (EOFException e) {
				 log.warn("read eof "+pos.get(), e);
				long nextpos= this.worker.skipToNext(pos.get());
				if(nextpos>=0)
				{
					pos.set(nextpos);
				}else{
					return -1;
				}
			}

		}
		return -1;
	}

	public long append(final IMessage req) throws IOException {
		return this.worker.append(req);
	}

	public void close() throws IOException {
		this.worker.close();
	}

	public void flush() throws IOException {
		this.worker.flush();
	}

}
