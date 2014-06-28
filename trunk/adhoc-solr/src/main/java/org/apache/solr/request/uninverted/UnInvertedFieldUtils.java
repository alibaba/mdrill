package org.apache.solr.request.uninverted;

import java.io.IOException;
import java.util.Comparator;
import java.util.concurrent.ExecutorCompletionService;

import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.util.cache.Cache;
import org.apache.lucene.util.cache.SimpleLRUCache;
import org.apache.solr.request.BlockBufferPool.BlockArray;
import org.apache.solr.request.uninverted.GrobalCache.ILruMemSizeKey;
import org.apache.solr.schema.DoubleField;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.LongField;
import org.apache.solr.schema.StrField;
import org.apache.solr.schema.TrieDoubleField;
import org.apache.solr.schema.TrieLongField;

public class UnInvertedFieldUtils {
	public static enum FieldDatatype{
		d_default,
		d_string,
		d_long,
		d_double
	}
	
	public static enum CompressType{
		d_byte,
		d_short,
		d_int,
	}
	
	public static Comparator<MixTermInfo>  TD_CMP=new Comparator<MixTermInfo>() {
		@Override
		public int compare(MixTermInfo o1, MixTermInfo o2) {
			int t1=o1.getCount()/1024;
			int t2=o2.getCount()/1024;
			if(t1 == t2)
			{
				int tt1=o1.getTermNum();
				int tt2=o2.getTermNum();
				return  tt1 == tt2 ? 0 : tt1 > tt2 ? 1 : -1;
			}
			
			return t1 < t2 ? 1 : -1;
		}
	};

	public static Comparator<MixTermInfo>  TD_CMP_TM=new Comparator<MixTermInfo>() {
		@Override
		public int compare(MixTermInfo o1, MixTermInfo o2) {
			int tt1=o1.getTermNum();
			int tt2=o2.getTermNum();
			return  tt1 == tt2 ? 0 : tt1 > tt2 ? 1 : -1;
		}
	};
	


	
	public static interface Doc2TmInterface{
		public Integer get(int i);
		public void set(int i,Number v);
		public Integer getsize();
	}
	
	public static class IntDoc2Tm implements Doc2TmInterface{
		BlockArray<Integer> arr;
		public IntDoc2Tm(BlockArray<Integer> arr) {
			this.arr = arr;
		}
		public Integer get(int i){
			return this.arr.get(i);
		}
		public Integer getsize() {
			return this.arr.getSize();
		}
		@Override
		public void set(int i, Number v) {
			this.arr.set(i, v.intValue());
		}
	}
	
	public static class ByteDoc2Tm implements Doc2TmInterface{
		BlockArray<Byte> arr;
		public ByteDoc2Tm(BlockArray<Byte> arr) {
			this.arr = arr;
		}
		public Integer get(int i){
			return (int)this.arr.get(i);
		}
		public Integer getsize() {
			return this.arr.getSize();
		}
		@Override
		public void set(int i, Number v) {
			this.arr.set(i, v.byteValue());
		}
	}
	
	public static class ShortDoc2Tm implements Doc2TmInterface{
		BlockArray<Short> arr;
		public ShortDoc2Tm(BlockArray<Short> arr) {
			this.arr = arr;
		}
		public Integer get(int i){
			return (int)this.arr.get(i);
		}
		public Integer getsize() {
			return this.arr.getSize();
		}
		@Override
		public void set(int i, Number v) {
			this.arr.set(i, v.shortValue());
		}
	}
	
	public static int compressTypeToInt(CompressType dt)
	{
		if(dt==CompressType.d_byte)
		{
			return 1;
		}
		if(dt==CompressType.d_short)
		{
			return 2;
		}
		return 0;
	}
	

	public static CompressType intToCompressType(int d) {
		switch (d) {
		case 1: {
			return CompressType.d_byte;
		}
		case 2: {
			return CompressType.d_short;
		}
		default: {
			return CompressType.d_int;
		}
		}

	}
	
	public static int fieldDataTypeToInt(FieldDatatype dt)
	{
		if(dt==FieldDatatype.d_string)
		{
			return 1;
		}
		if(dt==FieldDatatype.d_long)
		{
			return 2;
		}
		if(dt==FieldDatatype.d_double)
		{
			return 3;
		}
		return 0;
	}
	
	public static FieldDatatype intToFieldDataType(int d) {
		switch (d) {
		case 1: {
			return FieldDatatype.d_string;
		}
		case 2: {
			return FieldDatatype.d_long;
		}
		case 3: {
			return FieldDatatype.d_double;
		}
		default: {
			return FieldDatatype.d_default;
		}
		}

	}
	
	public static FieldDatatype getDataType(FieldType ft)
	{
		FieldDatatype dataType=FieldDatatype.d_default;
		if(ft instanceof StrField)
	    {
	    	dataType=FieldDatatype.d_string;
	    }else if(ft instanceof LongField||ft instanceof TrieLongField)
	    {
	    	dataType=FieldDatatype.d_long;
	    }else if(ft instanceof DoubleField||ft instanceof TrieDoubleField)
	    {
	    	dataType=FieldDatatype.d_double;
	    }else{
	    	dataType=FieldDatatype.d_default;
	    }
		return dataType;
	}
	
	
	  /** Number of bytes to represent an unsigned int as a vint. */
	  public static int vIntSize(int x) {
	    if ((x & (0xffffffff << (7*1))) == 0 ) {
	      return 1;
	    }
	    if ((x & (0xffffffff << (7*2))) == 0 ) {
	      return 2;
	    }
	    if ((x & (0xffffffff << (7*3))) == 0 ) {
	      return 3;
	    }
	    if ((x & (0xffffffff << (7*4))) == 0 ) {
	      return 4;
	    }
	    return 5;
	  }
	  


	  // todo: if we know the size of the vInt already, we could do
	  // a single switch on the size
	  public static int writeInt(int x, byte[] arr, int pos) {
	    int a;
	    a = (x >>> (7*4));
	    if (a != 0) {
	      arr[pos++] = (byte)(a | 0x80);
	    }
	    a = (x >>> (7*3));
	    if (a != 0) {
	      arr[pos++] = (byte)(a | 0x80);
	    }
	    a = (x >>> (7*2));
	    if (a != 0) {
	      arr[pos++] = (byte)(a | 0x80);
	    }
	    a = (x >>> (7*1));
	    if (a != 0) {
	      arr[pos++] = (byte)(a | 0x80);
	    }
	    arr[pos++] = (byte)(x & 0x7f);
	    return pos;
	  }
	  
	
	  public static class UnivertPool{
			public UnInvertedField uni;
			public IOException e=null;
		}

		private static Cache<ILruMemSizeKey, Object> LOCK = Cache.synchronizedCache(new SimpleLRUCache<ILruMemSizeKey, Object>(1024));

		
		
		public static synchronized Object getLock(final ILruMemSizeKey key)
		{
			Object lockobj=LOCK.get(key);
			if(lockobj==null)
			{
				lockobj=new Object();
				LOCK.put(key, lockobj);
			}
			
			return lockobj;
		}
		
		public static UnInvertedField takeUnf(ExecutorCompletionService<UnivertPool> serv) throws IOException
		{
			UnInvertedField uif=null;
			try {
				UnivertPool rtnuif = serv.take().get();
				 if(rtnuif.e!=null)
				    {
				    	throw rtnuif.e;
				    }
				    uif=rtnuif.uni;
			} catch (Throwable e) {
				throw new IOException(e);
			}
			
			return uif;
		} 
		
		
		public static final int NO_MORE_DOCS = Integer.MAX_VALUE;
		
		public static int advance(TermDocs termDocs, int target) throws IOException {
			try {
				int doc = NO_MORE_DOCS;
				boolean result = termDocs.skipTo(target);
				if (result) {
					doc = termDocs.doc();
				}
				return doc;
			} catch (Throwable e) {
				UnInvertedField.log.error("advance " + target, e);
				return NO_MORE_DOCS;
			}
		}
		
		
		  

		public static class MixTermInfo {
			private int count = 0;
			private Term tm = null;
			private int termNum = 0;
			private TermDocs td;

			public MixTermInfo(int count, int termNum, TermDocs td, Term tm) {
				this.count = count;
				this.termNum = termNum;
				this.td = td;
				this.tm = tm;
			}

			public int getTermNum() {
				return termNum;
			}

			public int getCount() {
				return count;
			}

			public TermDocs getTd() throws IOException {
				this.td.seek(tm);
				return this.td;
			
			}

		}
}
