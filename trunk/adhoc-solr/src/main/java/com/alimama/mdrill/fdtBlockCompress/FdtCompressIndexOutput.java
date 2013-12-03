package com.alimama.mdrill.fdtBlockCompress;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.RAMOutputStream;
//FieldsWriter
public class FdtCompressIndexOutput extends IndexOutput {
    private static final Log LOG = LogFactory.getLog(FdtCompressIndexOutput.class);

	private IndexOutput output;
	private RAMOutputStream ramoutput;
	private boolean isNeedFlush=false;
	private int blocksize=1024*512;
	public FdtCompressIndexOutput(IndexOutput output,int blocksize) {
		this.output = output;
		this.blocksize=blocksize;
		this.ramoutput=new RAMOutputStream();
		 isNeedFlush=false;
	}

	@Override
	public void flush() throws IOException {
		this.output.flush();
		this.ramoutput.flush();

	}
	
	private void syncBlock() throws IOException
	{
		if(this.isNeedFlush)
		{
		    ByteArrayOutputStream bos = new ByteArrayOutputStream();
		    GZIPOutputStream gzip = new GZIPOutputStream(bos);
//		    long size=this.ramoutput.getFilePointer();
		    this.ramoutput.writeTo(gzip);	    
		    gzip.finish();
		    gzip.close();
		    byte[] b = bos.toByteArray();
		    bos.close();
		    this.output.writeVInt(b.length);
		    this.output.writeBytes(b, 0,b.length);
		    this.ramoutput=new RAMOutputStream();
		    this.isNeedFlush=false;
//		    LOG.info(b.length+"@"+size+","+this.output.getFilePointer());
		}
		
	}

	@Override
	public void close() throws IOException {
		this.syncBlock();
		this.output.close();
		this.ramoutput.close();
	}

	@Override
	public long getFilePointer() {
		long rtn= (this.output.getFilePointer()<<24)+this.ramoutput.getFilePointer();
		return rtn;
	}

	@Override
	public void seek(long pos) throws IOException {
	      throw new RuntimeException("not allowed");    
	}

	@Override
	public long length() throws IOException {
	      throw new RuntimeException("not allowed");    
	}

	@Override
	public void writeByte(byte b) throws IOException {
		this.ramoutput.writeByte(b);
		this.isNeedFlush=true;
		if(this.ramoutput.getFilePointer()>this.blocksize)
		{
			this.syncBlock();
		}
	}

	@Override
	public void writeBytes(byte[] b, int offset, int length) throws IOException {
		this.ramoutput.writeBytes(b, offset, length);
		this.isNeedFlush=true;
		if(this.ramoutput.getFilePointer()>this.blocksize)
		{
			this.syncBlock();
		}
	}

}
