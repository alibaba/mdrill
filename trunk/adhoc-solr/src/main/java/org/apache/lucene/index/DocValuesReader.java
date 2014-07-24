package org.apache.lucene.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.lucene.store.IndexInput;
import org.apache.solr.request.uninverted.RamTermNumValue;
import org.apache.solr.request.uninverted.TermIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alimama.mdrill.buffer.SmallBufferedInput;

public class DocValuesReader {
	public static Logger log = LoggerFactory.getLogger(DocValuesReader.class);

	public SmallBufferedInput quicktisInput=null;
	public SmallBufferedInput quicktisInputTxt=null;
	public SmallBufferedInput quicktisInputVal=null;

	private ConcurrentHashMap<Integer, Long> fieldPosTis = new ConcurrentHashMap<Integer, Long>();
	private ConcurrentHashMap<Integer, Long> fieldPosTisTxt = new ConcurrentHashMap<Integer, Long>();
	private ConcurrentHashMap<Integer, Long> fieldPosTisVal = new ConcurrentHashMap<Integer, Long>();
	
	@Override
	public String toString() {
		return "DocValuesReader [tispos=" + tispos + ", tisposTxt=" + tisposTxt
				+ ", tisposVal=" + tisposVal + ", maxsize=" + maxsize
				+ ", maxtm=" + maxtm + ", useBits=" + useBits + ", lst="
				+ lst.size()+ ", sizeOfStrings=" + sizeOfStrings + "]";//+"@"+String.valueOf(lst.subList(0, Math.min(lst.size(), 10)))
	}

	private long tispos=-1;
	private long tisposTxt=-1;
	private long tisposVal=-1;
	
	private int maxsize=0;
	public int maxtm=0;
	private int useBits=0;
	
	public ArrayList<String> lst=new ArrayList<String>();
	public long sizeOfStrings=0;
		
	public void readPosForm(IndexInput sizebuff) throws IOException {

		synchronized (lock) {
			
		
		int size;
		size = sizebuff.readInt();
		for (int i = 0; i < size; i++) {
			fieldPosTis.put(sizebuff.readInt(), sizebuff.readLong());
		}
		
		size = sizebuff.readInt();
		for (int i = 0; i < size; i++) {
			fieldPosTisTxt.put(sizebuff.readInt(), sizebuff.readLong());
		}
		
		size = sizebuff.readInt();
		for (int i = 0; i < size; i++) {
			fieldPosTisVal.put(sizebuff.readInt(), sizebuff.readLong());
		}
		
//		log.info("readPosForm:fieldPosTis:"+fieldPosTis.toString());
//		log.info("readPosForm:fieldPosTisTxt:"+fieldPosTisTxt.toString());
//		log.info("readPosForm:fieldPosTisVal:"+fieldPosTisVal.toString());
		}
	}
	
	
	public Object getLock()
	{
		return this.lock;
	}
	public void seekTo(int fieldNumber,boolean readText) throws IOException {

		this.tisposTxt=fieldPosTisTxt.get(fieldNumber);
		this.tisposVal=fieldPosTisVal.get(fieldNumber);
		
		long pos=fieldPosTis.get(fieldNumber);
		this.quicktisInput.seek(pos);
		this.maxsize=this.quicktisInput.readInt();
		this.maxtm=this.quicktisInput.readInt();
		this.useBits=this.quicktisInput.readInt();;
		this.tispos=this.quicktisInput.getFilePointer();
		
		this.lst=new ArrayList<String>();
		this.sizeOfStrings=0;
		if(readText)
		{
			this.quicktisInputTxt.seek(this.tisposTxt);

			for (int i = 0; i < this.maxtm; i++)
			{
				if ((i & TermIndex.intervalMask) == 0) {
					String text = this.quicktisInputTxt.readString();
					this.sizeOfStrings += text.length() << 1;
					lst.add(text);
				}
			}
		}
		
		log.info(fieldNumber+">"+this.toString());
		
		
	}
	
	public int readTm(int docid) throws IOException
	{
		if(docid>=this.maxsize)
		{
			return this.maxtm+1;
		}
		long startbits=1l*docid*useBits;
		long startpos=startbits/8;
		int offsetbits=(int) (startbits-(startpos*8));
		
		long pos=this.tispos+startpos;
		this.quicktisInput.seek(pos);
		int rtn= this.quicktisInput.readbits(offsetbits,useBits);
		
		if(rtn<0||rtn>this.maxtm)
		{
			return this.maxtm+1;
		}
		return rtn;
	}
	
	
	public long readTmValue(int tm,boolean islongbits) throws IOException
	{

		if(tm>=this.maxtm)
		{
			if(islongbits)
			{
				return Double.doubleToLongBits(RamTermNumValue.TERMNUM_NAN_VALUE);
			}else{
				return (long) RamTermNumValue.TERMNUM_NAN_VALUE;
			}
		}
		
		long pos=this.tisposVal+1l*tm*8;
		this.quicktisInputVal.seek(pos);
		long rtn= this.quicktisInputVal.readLong();

		return rtn;
	}
	
	  
		protected void finalize() throws Throwable
	    {
			super.finalize();
			try{
				this.close();
			}catch(Throwable e)
			{
				
			}
	    }
		
		  Object lock=new Object();
		  boolean isclone=false;
	  public void close() throws IOException
	  {
		  synchronized (lock) {
			  if(quicktisInput!=null)
			  {
				  quicktisInput.close();
				  quicktisInput=null;
			  }
			  if(quicktisInputTxt!=null)
			  {
				  quicktisInputTxt.close();
				  quicktisInputTxt=null;
			  }
			  if(quicktisInputVal!=null)
			  {
				  quicktisInputVal.close();
				  quicktisInputVal=null;
			  }
		  }
	  }
	  
	  
	  public Object clone() throws CloneNotSupportedException{
		  synchronized (lock) {
			  DocValuesReader rtn = new DocValuesReader();
			  rtn.quicktisInput= (SmallBufferedInput)quicktisInput.clone();
			  rtn.quicktisInputTxt=(SmallBufferedInput)quicktisInputTxt.clone();
			  rtn.quicktisInputVal=(SmallBufferedInput)quicktisInputVal.clone();
			  rtn.fieldPosTis=new ConcurrentHashMap<Integer, Long>(this.fieldPosTis);
			  rtn.fieldPosTisTxt=new ConcurrentHashMap<Integer, Long>(this.fieldPosTisTxt);
			  rtn.fieldPosTisVal=new ConcurrentHashMap<Integer, Long>(this.fieldPosTisVal);
			  rtn.isclone=true;
			  return rtn;
		  }
	  }



}
