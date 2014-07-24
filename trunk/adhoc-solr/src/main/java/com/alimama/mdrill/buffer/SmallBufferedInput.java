package com.alimama.mdrill.buffer;

import java.io.IOException;

import org.apache.lucene.store.BufferedIndexInput;
import org.apache.lucene.store.IndexInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SmallBufferedInput extends BufferedIndexInput {
	public static Logger log = LoggerFactory
			.getLogger(SmallBufferedInput.class);
	private IndexInput in;
	private boolean isOpen;
	private boolean isClone;

	public SmallBufferedInput(IndexInput input, int buffersize) {
		super("SmallBufferedInput", buffersize);
//		log.info("SmallBufferedInput:" + input.getClass().getName() + ","
//				+ buffersize);
		this.in = input;
		this.isOpen = true;
	}

	@Override
	protected void readInternal(byte[] b, int offset, int length)
			throws IOException {
		long position = getFilePointer();
		synchronized (this.in) {
			if(position!=this.in.getFilePointer())
			{
				this.in.seek(position);
			}
			this.in.readBytes(b, offset, length);
		}
	}

	@Override
	protected void seekInternal(long pos) throws IOException {

	}

	public Object clone() {
		SmallBufferedInput clone = (SmallBufferedInput) super.clone();
		clone.isClone = true;
		return clone;
	}

	@Override
	public void close() throws IOException {
		if (!isClone) {
			if (isOpen) {
				in.close();
				isOpen = false;
			} else {
				throw new IOException("Index file " + " already closed");
			}
		}

	}

	@Override
	public long length() {
		return in.length();
	}

}
