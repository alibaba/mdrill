package org.apache.solr.request.uninverted;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.apache.solr.core.SolrCore;
import org.apache.solr.request.BigReUsedBuffer;
import org.apache.solr.request.BigReUsedBuffer.BlockArray;
import org.apache.solr.schema.FieldType;
import org.apache.solr.search.SolrIndexSearcher;

import com.alimama.mdrill.buffer.LuceneUtils;

public class UnInvertedFieldBase {

	public static BigReUsedBuffer<Integer> INT_BUFFER=new BigReUsedBuffer<Integer>();
	public static BigReUsedBuffer<Short> SHORT_BUFFER=new BigReUsedBuffer<Short>();
	public static BigReUsedBuffer<Byte> BYTE_BUFFER=new BigReUsedBuffer<Byte>();
	public static BigReUsedBuffer<Long> LONG_BUFFER=new BigReUsedBuffer<Long>();
	public static BigReUsedBuffer<Double> DOUBLE_BUFFER=new BigReUsedBuffer<Double>();
	public static int CacheVersion=31;
	
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
	public void free()
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
	
	protected void finalize() throws Throwable
     {
		super.finalize();
        this.free();
     }
	  
	  
	  public boolean readUninvertCache(IndexReader reader,SolrIndexSearcher searcher,long startTime) throws IOException
	 {
		  String key=LuceneUtils.crcKey(reader);
			Directory cachedir =searcher.getFieldcacheDir();
			ti.setCacheDir(cachedir,key);
			String filename=field + ".fv"+CacheVersion+"."+key;
			if (cachedir.fileExists(filename)) {
				try {
					IndexInput input = cachedir.openInput(filename, 10240);
					this.nullTermNum=input.readInt();
					this.maxTermNum=input.readInt();
					this.numTermsInField = input.readInt();
					this.termsInverted = input.readInt();
					this.termInstances = input.readLong();
					int index_len = input.readInt();
					if (index_len <= 0) {
						index = null;
					} else {
						index = INT_BUFFER.calloc(index_len, BigReUsedBuffer.INT_CREATE, -1);
						for (int i = 0; i < index_len; i++) {
							index.set(i, input.readInt());
						}
					}
					index_len = input.readInt();
					if (index_len <= 0) {
						indexshort = null;
					} else {
						indexshort = SHORT_BUFFER.calloc(index_len, BigReUsedBuffer.SHORT_CREATE, (short)-1);
						for (int i = 0; i < index_len; i++) {
							indexshort.set(i, (short)input.readInt());
						}
					}
					index_len = input.readInt();
					if (index_len <= 0) {
						indexbyte = null;
					} else {
						indexbyte = BYTE_BUFFER.calloc(index_len, BigReUsedBuffer.BYTE_CREATE,(byte) -1);
						for (int i = 0; i < index_len; i++) {
							indexbyte.set(i, (byte)input.readInt());
						}
					}
					this.indexDatatype=UnInvertedFieldUtils.IntToTypeIndex(input.readInt());
					this.dataType=UnInvertedFieldUtils.IntToDatatype(input.readInt());
					
					index_len = input.readInt();
					if (index_len <= 0) {
						termValueLong = null;
					} else {
						termValueLong = LONG_BUFFER.calloc(index_len, BigReUsedBuffer.LONG_CREATE, 0l);
						for (int i = 0; i < index_len; i++) {
							termValueLong.set(i, input.readLong());
						}
					}
					
					index_len = input.readInt();
					if (index_len <= 0) {
						termValueDouble = null;
					} else {
						termValueDouble = DOUBLE_BUFFER.calloc(index_len, BigReUsedBuffer.DOUBLE_CREATE, 0d);
						for (int i = 0; i < index_len; i++) {
							termValueDouble.set(i, Double.longBitsToDouble(input.readLong()));
						}
					}
					

					int tnums_len = input.readInt();
					if (tnums_len <= 0) {
						tnums = null;
					} else {
						tnums = new byte[tnums_len][];
						for (int i = 0; i < tnums.length; i++) {
							int bytelen = input.readInt();
							if (bytelen > 0) {
								tnums[i] = new byte[bytelen];
								input.readBytes(tnums[i], 0, tnums[i].length);
							} else {
								tnums[i] = new byte[0];
							}
						}
					}

					input.close();
					long endTime = System.currentTimeMillis();
					total_time = (int) (endTime - startTime);
					ti.setCache(reader, cachedir,key);
					endTime = System.currentTimeMillis();
					total_time = (int) (endTime - startTime);
					SolrCore.log.info("facet read success from field '" + field+ "' time=" + total_time);
					return true;

				} catch (Exception e) {
					cachedir.deleteFile(filename);
					SolrCore.log.info("facet read fail from file '" + field + "'",e);
				}
			}
			return false;
		}
	  

		public void writeUnvertCache(IndexReader reader,SolrIndexSearcher searcher)
				throws IOException {
			Directory dir = searcher.getFieldcacheDir();
			String filename=field + ".fv"+CacheVersion+"."+LuceneUtils.crcKey(reader);
			try {
				IndexOutput output = dir.createOutput(filename);
				output.writeInt(this.nullTermNum);
				output.writeInt(this.maxTermNum);
				output.writeInt(this.numTermsInField);
				output.writeInt(this.termsInverted);
				output.writeLong(this.termInstances);
				
				if (index == null) {
					output.writeInt(0);
				} else {
					output.writeInt(index.getSize());
					for (int i = 0; i < index.getSize(); i++) {
						output.writeInt(index.get(i));
					}
				}
				
				if (indexshort == null) {
					output.writeInt(0);
				} else {
					output.writeInt(indexshort.getSize());
					for (int i = 0; i < indexshort.getSize(); i++) {
						output.writeInt(indexshort.get(i));
					}
				}
				
				if (indexbyte == null) {
					output.writeInt(0);
				} else {
					output.writeInt(indexbyte.getSize());
					for (int i = 0; i < indexbyte.getSize(); i++) {
						output.writeInt(indexbyte.get(i));
					}
				}
				
				output.writeInt(UnInvertedFieldUtils.TypeIndexToInt(this.indexDatatype));
				output.writeInt(UnInvertedFieldUtils.DatatypeToInt(this.dataType));
				if (termValueLong == null) {
					output.writeInt(0);
				} else {
					output.writeInt(termValueLong.getSize());
					for (int i = 0; i < termValueLong.getSize(); i++) {
						output.writeLong(termValueLong.get(i));
					}
				}
				
				
				if (termValueDouble == null) {
					output.writeInt(0);
				} else {
					output.writeInt(termValueDouble.getSize());
					for (int i = 0; i < termValueDouble.getSize(); i++) {
						output.writeLong(Double.doubleToLongBits(termValueDouble.get(i)));
					}
				}
				
				if (tnums == null) {
					output.writeInt(0);
				} else {
					output.writeInt(tnums.length);
					for (int i = 0; i < tnums.length; i++) {
						byte[] vuff = tnums[i];
						if (vuff == null||vuff.length==0) {
							output.writeInt(0);
						} else {
							output.writeInt(vuff.length);
							output.writeBytes(vuff, vuff.length);
						}
					}
				}

				output.close();
				SolrCore.log.info("facet write success to field'" + field);

			} catch (Exception e) {
				dir.deleteFile(filename);
				SolrCore.log.info("facet write fail from file '" + field,e);
			}
		}
		
		
		long sz=-1;
		  public synchronized long memSize() {
			  if(sz>=0)
			  {
				  return sz;
			  }
			    sz = 8*8 + 32; // local fields
			    if (index != null) sz += index.getMemSize() * 4;
			    if (indexshort != null) sz += indexshort.getMemSize() * 2;
			    if (indexbyte != null) sz += indexbyte.getMemSize() * 1;
			    if (termValueDouble != null) sz += termValueDouble.getMemSize() * 8;
			    if (termValueLong != null) sz += termValueLong.getMemSize() * 8;
			    if (tnums!=null) {
			      for (byte[] arr : tnums)
			        if (arr != null) sz += arr.length;
			    }
			    sz += ti.memSize();
			    return sz;
			  }
		  
}
