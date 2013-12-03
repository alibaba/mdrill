package com.alimama.mdrill.fdtBlockCompress;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.RAMDirectory;


//FieldsWriter
public class FdtCompressIndexInput extends IndexInput{
    private static final Log LOG = LogFactory.getLog(FdtCompressIndexInput.class);
    String compressuuid=java.util.UUID.randomUUID().toString();
public static void main(String[] args) throws IOException, InterruptedException {
	long pos=721943728;
	System.out.println(FdtCompressIndexInput.getBlockPos(pos));
	System.out.println(FdtCompressIndexInput.getRamOffset(pos));
	
//	RAMDirectory ramDirectory=new RAMDirectory();
//	int testlen=1024000;
//	long[] testdata=new long[testlen];
//	
//	FdtCompressIndexOutput out=new FdtCompressIndexOutput(ramDirectory.createOutput("test"), 512);
//	for(int i=0;i<testlen;i++)
//	{
//		testdata[i]=out.getFilePointer();
//		out.writeVInt(i);
//		if(i%1000000==0)
//		{
//			System.out.println(i);
//		}
//	}
//	System.out.println("close");
//	out.close();
//	System.out.println("close2");
//
//	FdtCompressIndexInput input=new FdtCompressIndexInput(ramDirectory.openInput("test"));
//	System.out.println("open");
//
//	for(int i=0;i<testlen;i++)
//	{
//		int val=input.readVInt();
//		if(val!=i)
//		{
//			System.out.println("error="+i+","+val);
//		}
//	}
//	
//	System.out.println("open2");
//
//	for(int i=0;i<testlen;i++)
//	{
//		input.seek(testdata[i]);
//		int val=input.readVInt();
//		if(val!=i)
//		{
//			System.out.println("seek error="+i+","+val);
//		}
//		if(i%100000==0)
//		{
//			System.out.println("seek 1 "+i);
//		}
//	}
//	System.out.println("open3");
//
//	for(int i=0;i<testlen;)
//	{
//		input.seek(testdata[i]);
//		int val=input.readVInt();
//		if(val!=i)
//		{
//			System.out.println("seek error="+i+","+val);
//		}
//		System.out.println("seek 100 "+i);
//		i+=100000;
//	}
//	
//	for(int i=0;i<100;i++)
//	{
//		RAMDirectory ramDirectory2=new RAMDirectory();
//
//		FdtCompressIndexOutput out2=new FdtCompressIndexOutput(ramDirectory2.createOutput("test"), 3);
//		
//		int start=(int) (Math.random()*100000);
//		int end=start+(int) (Math.random()*100000);
//		input.seek(testdata[start]);
//		input.writeToPos(out2, testdata[end]);
//		out2.close();
//
//		FdtCompressIndexInput input2=new FdtCompressIndexInput(ramDirectory2.openInput("test"));
//
//		System.out.println(">>."+i+","+start+","+end);
//		for(int j=start;j<end;j++)
//		{
//			int val=input2.readVInt();
//			if(val!=j)
//			{
//				System.out.println("eoror writeToPos "+j);
//			}
//		}
//		
//		input2.close();
//		
//	}
//	
//	for(int i=0;i<100;i++)
//	{
//		RAMDirectory ramDirectory2=new RAMDirectory();
//
//		FdtCompressIndexOutput out2=new FdtCompressIndexOutput(ramDirectory2.createOutput("test"), 3);
//		
//		int start=i;
//		int end=start+1;
//		input.seek(testdata[start]);
//		input.writeToPos(out2, testdata[end]);
//		out2.close();
//
//		FdtCompressIndexInput input2=new FdtCompressIndexInput(ramDirectory2.openInput("test"));
//
//		System.out.println(">>."+i+","+start+","+end);
//		for(int j=start;j<end;j++)
//		{
//			int val=input2.readVInt();
//			if(val!=j)
//			{
//				System.out.println("eoror writeToPos "+j);
//			}
//		}
//		
//		input2.close();
//		
//	}
//	
//	
//	input.close();
//	
}
	private IndexInput input;
	private IndexInput raminput=null;
	private long blockpos=0;
	private long nextpos=0;
	private boolean isClone=false;
	private RAMDirectory ramDirectory=new RAMDirectory();
	
	public Object clone() {
		FdtCompressIndexInput clone = (FdtCompressIndexInput) super.clone();
	    clone.isClone = true;
	    clone.blockpos=blockpos;
	    clone.nextpos=nextpos;
	    clone.input=(IndexInput) input.clone();
	    clone.raminput=null;
	    clone.compressuuid=java.util.UUID.randomUUID().toString();
	    clone.ramDirectory=new RAMDirectory();
	    try {
			clone.seek(this.getFilePointer());
		} catch (IOException e) {
			LOG.error("clone seek",e);
		}
	    return clone;
	}
	
	
	public FdtCompressIndexInput(IndexInput input)
	{
		super("FdtCompressIndexIndexInput");

		this.input=input;
		this.blockpos=input.getFilePointer();
	}
	@Override
	public void close() throws IOException {
		if(this.isClone)
		{
			//TODO nothing
		}
		this.input.close();
		if(this.raminput!=null)
		{
			this.raminput.close();
		}
	}
	
	private void resetBlock(long pos) throws IOException
	{
		if(this.blockpos==pos&&this.raminput!=null)
		{
			return ;
		}
		if(this.raminput!=null)
		{
			this.raminput.close();
		}
		
		this.input.seek(pos);
		this.blockpos=pos;
		int bytelen=this.input.readVInt();
		byte[] compressdata=new byte[bytelen];
		this.input.readBytes(compressdata, 0, bytelen);
		this.nextpos=this.input.getFilePointer();
//		LOG.info("resetBlock "+bytelen+","+pos+","+this.nextpos+","+this.input.getClass().getName());

		ramDirectory=new RAMDirectory();
		IndexOutput output=ramDirectory.createOutput("r");

		ByteArrayInputStream bis = new ByteArrayInputStream(compressdata);
		GZIPInputStream gzip = new GZIPInputStream(bis);
		byte[] buff = new byte[1024];  
        int rc = 0;  
        while ((rc = gzip.read(buff, 0, buff.length)) > 0) {  
        	output.writeBytes(buff, 0, rc);  
        }  
        gzip.close();
        bis.close();
        output.close();
        this.raminput=ramDirectory.openInput("r");
        
	}

	@Override
	public long getFilePointer() {
		if(this.raminput==null){
			try {
				this.resetBlock(this.blockpos);
			} catch (IOException e) {
			}
		}
		return (this.blockpos<<24)+this.raminput.getFilePointer();
	}

	@Override
	public void seek(long pos) throws IOException {
		long blockpos=getBlockPos(pos);
		long ramoff=getRamOffset(pos);
//		LOG.info("seek "+pos+","+this.blockpos+","+blockpos+","+ramoff+","+this.input.getClass().getName()+","+this.compressuuid);

		this.resetBlock(blockpos);
		this.raminput.seek(ramoff);//24个1

	}

	private static long getBlockPos(long pos)
	{
		return (pos>>24);
	}
	
	
	private static long getRamOffset(long pos)
	{
		return pos&16777215;
	}
	@Override
	public long length() {
	      throw new RuntimeException("not allowed");    
	}

	@Override
	public byte readByte() throws IOException {
		byte[] rtn=new byte[1];
		this.readBytes(rtn, 0, 1);
		return rtn[0];
	}

	@Override
	public void readBytes(byte[] b, int offset, int len) throws IOException {
		if(this.raminput==null)
		{
			this.resetBlock(this.blockpos);
		}
		int posoffset=0;
		while(posoffset<len)
		{
			long pos=this.raminput.getFilePointer();
			long size=this.raminput.length();
			long left=size-pos;
			int need=len-posoffset;
			int readSize=(int) Math.min(left, need);
			if(readSize>0)
			{
				this.raminput.readBytes(b,offset+ posoffset, readSize);
				posoffset+=readSize;
			}
			if(need>left)
			{
				this.resetBlock(this.nextpos);
			}
		}
	}
	
	 public void writeToPos(IndexOutput output, long end) throws IOException {
		 if(end==-1)
		 {
			 while(true)
			 {
				 long pos=this.raminput.getFilePointer();
				long size=this.raminput.length();
				int left=(int)(size-pos);
				 byte[] buffer=new byte[left];
				 this.readBytes(buffer, 0, left);
				 output.writeBytes(buffer, 0, left);
				 if(this.nextpos>=this.input.length())
				 {
					 return ;
				 }
				 this.resetBlock(this.nextpos);
			 }
		 }else{
			 long endblock=getBlockPos(end);
			 long ramoffset=getRamOffset(end);
			 while(endblock>this.blockpos)
			 {
				 long pos=this.raminput.getFilePointer();
				long size=this.raminput.length();
				int left=(int)(size-pos);
				 byte[] buffer=new byte[left];
				 this.readBytes(buffer, 0, left);
				 output.writeBytes(buffer, 0, left);
				 this.resetBlock(this.nextpos);
			 }
			 
			 if(endblock==this.blockpos)
			 {

				 long pos=this.raminput.getFilePointer();
				 int left=(int)(ramoffset-pos);
				 byte[] buffer=new byte[left];
				 this.readBytes(buffer, 0, left);
				 output.writeBytes(buffer, 0, left);
			 }
		 }
	 } 

}
