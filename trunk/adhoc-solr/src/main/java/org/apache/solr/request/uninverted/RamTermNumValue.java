package org.apache.solr.request.uninverted;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.solr.request.BlockBufferPool;
import org.apache.solr.request.BlockBufferPool.BlockArray;
import org.apache.solr.schema.FieldType;

public class RamTermNumValue {

	private static long MIN_VALUE=Integer.MIN_VALUE;
	public static long TERMNUM_NAN_VALUE =MIN_VALUE+100;
	public static long TERMNUM_NAN_VALUE_FORCMP=TERMNUM_NAN_VALUE+10;
	public static long EMPTY_FOR_MARK = MIN_VALUE+2;
	public static long EMPTY_FOR_MARK_FORCMP =EMPTY_FOR_MARK+10;

	public BlockArray<Double> termValueDouble = null;
	public BlockArray<Long> termValueLong = null;
	public UnInvertedFieldUtils.FieldDatatype fieldDataType = null;
	public int nullTermNum = -1;

	public RamTermNumValue(UnInvertedFieldUtils.FieldDatatype fieldDataType)
	{
		this.fieldDataType=fieldDataType;
	}

	public void startInitValue(int maxDoc, IndexReader reader,boolean isReadDouble) {
		if (!isReadDouble) {
			return ;
		}
		
		int maxDocOffset = maxDoc + 2;
		if(this.fieldDataType.equals(UnInvertedFieldUtils.FieldDatatype.d_double))
		{
			this.termValueDouble = BlockBufferPool.DOUBLE_POOL.calloc(maxDocOffset, BlockBufferPool.DOUBLE_CREATE,(double)RamTermNumValue.TERMNUM_NAN_VALUE);
		}else{
			this.termValueLong = BlockBufferPool.LONG_POOL.calloc(maxDocOffset, BlockBufferPool.LONG_CREATE,RamTermNumValue.TERMNUM_NAN_VALUE);
		}
	}
	
	public void endInitValue(boolean isReadDouble, int maxTermNum) {
		this.nullTermNum = maxTermNum + 1;

		if (!isReadDouble) {
			return ;
		}
		
		int fillSize=nullTermNum+1;

		
		if(this.fieldDataType.equals(UnInvertedFieldUtils.FieldDatatype.d_double))
		{	
			this.termValueDouble = BlockBufferPool.DOUBLE_POOL.reCalloc(this.termValueDouble,fillSize, BlockBufferPool.DOUBLE_CREATE, (double)RamTermNumValue.TERMNUM_NAN_VALUE);
			this.termValueDouble.set(this.nullTermNum, (double)RamTermNumValue.TERMNUM_NAN_VALUE);
			
		}else{
			this.termValueLong = BlockBufferPool.LONG_POOL.reCalloc(this.termValueLong,fillSize,BlockBufferPool.LONG_CREATE,	RamTermNumValue.TERMNUM_NAN_VALUE);
			this.termValueLong.set(this.nullTermNum, (long)RamTermNumValue.TERMNUM_NAN_VALUE);
		}
	
	}
	
	
	public void free() {
		if (this.termValueDouble != null) {
			BlockBufferPool.DOUBLE_POOL.recycleByteBlocks(this.termValueDouble);
			this.termValueDouble=null;
		}
		
		if (this.termValueLong != null) {
			BlockBufferPool.LONG_POOL.recycleByteBlocks(this.termValueLong);
			this.termValueLong=null;
		}
	}
	
	
	
	public int getmemsize() {
		int sz = 0;
		if (termValueDouble != null)
			sz += termValueDouble.getMemSize() * 8;
		
		if (termValueLong != null)
			sz += termValueLong.getMemSize() * 8;
		return sz;
	}
	

	
	
	public String toString() {
		StringBuffer membuffer = new StringBuffer();


		if (termValueDouble != null) {
			membuffer.append(",").append(
					"termValueDouble=" + termValueDouble.getMemSize() + "@"
							+ termValueDouble.getSize());
		}
		
		
		if (termValueLong != null) {
			membuffer.append(",").append(
					"termValueLong=" + termValueLong.getMemSize() + "@"
							+ termValueLong.getSize());
		}

		return membuffer.toString();
	}
	
	
	
	public TermDoubleValue getTermDoubleValue()
	{
		if (fieldDataType.equals(UnInvertedFieldUtils.FieldDatatype.d_long)) {
			return new TermDoubleValue_long(this);
		}

		if (fieldDataType.equals(UnInvertedFieldUtils.FieldDatatype.d_string)) {
			return new TermDoubleValue_long(this);
		}

		if (fieldDataType.equals(UnInvertedFieldUtils.FieldDatatype.d_double)) {
			return new TermDoubleValue_double(this);
		}

		return new TermDoubleValue_nan();
	}
	
	

	private static class TermDoubleValue_long extends TermDoubleValue {
		private BlockArray<Long> termValueLong;
		private RamTermNumValue tv;


		public TermDoubleValue_long( RamTermNumValue tv) {
			this.termValueLong = tv.termValueLong;
			this.tv=tv;

		}

		public double tm(int tm, FieldType ft, TermNumEnumerator te) {
			if(tm >= tv.nullTermNum)
			{
				return TERMNUM_NAN_VALUE;
			}
			return this.termValueLong.get(tm);
		}
	}

	private static class TermDoubleValue_double extends TermDoubleValue {
		private BlockArray<Double> termValueDouble;
		private RamTermNumValue tv;

		public TermDoubleValue_double(RamTermNumValue tv) {
			this.termValueDouble = tv.termValueDouble;
			this.tv=tv;
		}

		public double tm(int tm, FieldType ft, TermNumEnumerator te) {
			if(tm >= tv.nullTermNum)
			{
				return TERMNUM_NAN_VALUE;
			}
			return this.termValueDouble.get(tm);
		}
	}

	private static class TermDoubleValue_nan extends TermDoubleValue {

		public double tm(int tm, FieldType ft, TermNumEnumerator te)
				throws IOException {
			throw new RuntimeException("TermDoubleValue_nan");
//			return TERMNUM_NAN_VALUE;
		}
	}
	
	
	public abstract static class TermDoubleValue {
		public abstract double tm(int tm, FieldType ft, TermNumEnumerator te)
				throws IOException;
	}


}
