package org.apache.solr.request.uninverted;

import org.apache.solr.request.BigReUsedBuffer;
import org.apache.solr.request.BigReUsedBuffer.BlockArray;
import org.apache.solr.request.uninverted.UnInvertedFieldTermNumRead.*;
import org.apache.solr.schema.DoubleField;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.LongField;
import org.apache.solr.schema.StrField;
import org.apache.solr.schema.TrieDoubleField;
import org.apache.solr.schema.TrieLongField;

public class UnInvertedFieldUtils {
	public static enum Datatype{
		d_default,
		d_string,
		d_long,
		d_double
	}
	
	public static enum TypeIndex{
		d_byte,
		d_short,
		d_int,
	}
	

	public static BlockArrayReadInt getBlockArrayRead(UnInvertedField uni)
	{
		if(uni.indexDatatype.equals(TypeIndex.d_byte))
		{
			return new ByteRead(uni.indexbyte);
		}
		
		if(uni.indexDatatype.equals(TypeIndex.d_short))
		{
			return new ShortRead(uni.indexshort);
		}
		
		return new IntRead(uni.index);
	}
	
	public static interface BlockArrayReadInt{
		public Integer get(int i);
		public Integer getsize();
	}
	
	public static class IntRead implements BlockArrayReadInt{
		BlockArray<Integer> arr;
		public IntRead(BlockArray<Integer> arr) {
			this.arr = arr;
		}
		public Integer get(int i){
			return this.arr.get(i);
		}
		public Integer getsize() {
			return this.arr.getSize();
		}
	}
	
	public static class ByteRead implements BlockArrayReadInt{
		BlockArray<Byte> arr;
		public ByteRead(BlockArray<Byte> arr) {
			this.arr = arr;
		}
		public Integer get(int i){
			return (int)this.arr.get(i);
		}
		public Integer getsize() {
			return this.arr.getSize();
		}
	}
	
	public static class ShortRead implements BlockArrayReadInt{
		BlockArray<Short> arr;
		public ShortRead(BlockArray<Short> arr) {
			this.arr = arr;
		}
		public Integer get(int i){
			return (int)this.arr.get(i);
		}
		public Integer getsize() {
			return this.arr.getSize();
		}
	}
	
	public static int TypeIndexToInt(TypeIndex dt)
	{
		if(dt==TypeIndex.d_byte)
		{
			return 1;
		}
		if(dt==TypeIndex.d_short)
		{
			return 2;
		}
		return 0;
	}
	

	public static TypeIndex IntToTypeIndex(int d) {
		switch (d) {
		case 1: {
			return TypeIndex.d_byte;
		}
		case 2: {
			return TypeIndex.d_short;
		}
		default: {
			return TypeIndex.d_int;
		}
		}

	}
	
	public static int DatatypeToInt(Datatype dt)
	{
		if(dt==Datatype.d_string)
		{
			return 1;
		}
		if(dt==Datatype.d_long)
		{
			return 2;
		}
		if(dt==Datatype.d_double)
		{
			return 3;
		}
		return 0;
	}
	
	public static Datatype IntToDatatype(int d) {
		switch (d) {
		case 1: {
			return Datatype.d_string;
		}
		case 2: {
			return Datatype.d_long;
		}
		case 3: {
			return Datatype.d_double;
		}
		default: {
			return Datatype.d_default;
		}
		}

	}
	
	public static Datatype getDataType(FieldType ft)
	{
		Datatype dataType=Datatype.d_default;
		if(ft instanceof StrField)
	    {
	    	dataType=Datatype.d_string;
	    }else if(ft instanceof LongField||ft instanceof TrieLongField)
	    {
	    	dataType=Datatype.d_long;
	    }else if(ft instanceof DoubleField||ft instanceof TrieDoubleField)
	    {
	    	dataType=Datatype.d_double;
	    }else{
	    	dataType=Datatype.d_default;
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
	  
	  
	  public static TermNumReadInterface getReadInterface(boolean isMultiValued)
	  {
		  if(isMultiValued)
		    {
		       return new TermNumReadMulty();

		    }else{
		        return new TermNumReadSingle();
		    }
	  }
	  
}
