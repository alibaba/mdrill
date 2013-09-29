package org.apache.solr.request.uninverted;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.solr.request.BigReUsedBuffer;
import org.apache.solr.request.BigReUsedBuffer.BlockArray;
import org.apache.solr.schema.FieldType;

public class UnInvertedFieldBase {

	public static BigReUsedBuffer<Integer> INT_BUFFER=new BigReUsedBuffer<Integer>();
	public static BigReUsedBuffer<Short> SHORT_BUFFER=new BigReUsedBuffer<Short>();
	public static BigReUsedBuffer<Byte> BYTE_BUFFER=new BigReUsedBuffer<Byte>();
	public static BigReUsedBuffer<Long> LONG_BUFFER=new BigReUsedBuffer<Long>();
	public static BigReUsedBuffer<Double> DOUBLE_BUFFER=new BigReUsedBuffer<Double>();
	public static int CacheVersion=34;
	
	public AtomicInteger refCnt=new AtomicInteger(0);
	
	public String field;
	public int numTermsInField;
	public int termsInverted; // number of unique terms that were un-inverted
	public long termInstances; // total number of references to term numbers
	public  TermIndex ti;
	public int total_time; // total time to uninvert the field

	public UnInvertedFieldUtils.TypeIndex indexDatatype=UnInvertedFieldUtils.TypeIndex.d_int;
	public BlockArray<Integer> index=null;
	public BlockArray<Short> indexshort=null;
	public BlockArray<Byte> indexbyte=null;
	public BlockArray<Long> termValueLong=null;
	public BlockArray<Double> termValueDouble=null;
	
	int nullTermNum=-1;
	public Integer maxTermNum=Integer.MAX_VALUE;
  
	public FieldType ft;
	byte[][] tnums =null;
	public UnInvertedFieldUtils.Datatype dataType=UnInvertedFieldUtils.Datatype.d_default;

	
	public void setdefault()
	{
		this.index=null;
		this.indexshort=null;
		this.indexbyte=null;
		this.termValueLong=null;
		this.termValueDouble=null;
		this.tnums = null;
	}
	
	
	  private boolean isShutDown = false;
	  public void setFinalIndex(int nullTerm,int finalTerm)
		{
			this.nullTermNum=nullTerm;
			int size=this.index.getSize();
			if(finalTerm<Byte.MAX_VALUE)
			{
				this.indexDatatype=UnInvertedFieldUtils.TypeIndex.d_byte;
				this.indexbyte= BYTE_BUFFER.calloc(size, BigReUsedBuffer.BYTE_CREATE, (byte) nullTerm);
				for(int i=0;i<size;i++)
				{
					int t=this.index.get(i);
					if(t>=0)
					{
						this.indexbyte.set(i, (byte)t);
					}
				}
				INT_BUFFER.free(this.index);
				this.index=null;
			}else if(finalTerm<Short.MAX_VALUE)
			{
				this.indexDatatype=UnInvertedFieldUtils.TypeIndex.d_short;
				this.indexshort= SHORT_BUFFER.calloc(size, BigReUsedBuffer.SHORT_CREATE, (short) nullTerm);
				for(int i=0;i<size;i++)
				{
					int t=this.index.get(i);
					if(t>=0)
					{
						this.indexshort.set(i, (short)t);
					}
				}
				INT_BUFFER.free(this.index);
				this.index=null;
			}else{
				this.indexDatatype=UnInvertedFieldUtils.TypeIndex.d_int;;
				for(int i=0;i<size;i++)
				{
					int t=this.index.get(i);
					if(t<0)
					{
						this.index.set(i,nullTerm);
					}
				}
			}
		}
	private void free()
	{
		 if(isShutDown){
			 return ;
		 }
		isShutDown=true;

		if(this.index!=null)
		{
			INT_BUFFER.free(this.index);
		}
		
		if(this.indexshort!=null)
		{
			SHORT_BUFFER.free(this.indexshort);
		}
		
		if(this.indexbyte!=null)
		{
			BYTE_BUFFER.free(this.indexbyte);
		}
		
		if(this.termValueLong!=null)
		{
			LONG_BUFFER.free(this.termValueLong);
		}
		if(this.termValueDouble!=null)
		{
			DOUBLE_BUFFER.free(this.termValueDouble);
		}
	}
	
	 public void LRUclean()
	  {
		  if (this.refCnt.get() == 0) {
				this.free();
			}
	  }
	
	protected void finalize() throws Throwable
     {
		super.finalize();
        this.free();
     }
	  
		
	long sz = -1;

	public synchronized long memSize() {
		if (sz >= 0) {
			return sz;
		}
		sz = 8 * 8 + 32; // local fields
		if (index != null)
			sz += index.getMemSize() * 4;
		if (indexshort != null)
			sz += indexshort.getMemSize() * 2;
		if (indexbyte != null)
			sz += indexbyte.getMemSize() * 1;
		if (termValueDouble != null)
			sz += termValueDouble.getMemSize() * 8;
		if (termValueLong != null)
			sz += termValueLong.getMemSize() * 8;
		if (tnums != null) {
			for (byte[] arr : tnums)
				if (arr != null)
					sz += arr.length;
		}
		sz += ti.memSize();
		return sz;
	}
		  
}
