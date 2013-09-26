package org.apache.solr.request.uninverted;

import java.io.IOException;

import org.apache.solr.request.BigReUsedBuffer.BlockArray;
import org.apache.solr.request.mdrill.MdrillUtils;
import org.apache.solr.request.uninverted.UnInvertedFieldUtils.BlockArrayReadInt;

import org.apache.solr.schema.FieldType;



public class UnInvertedFieldQuickDouble {
	
	public abstract static class  termDoubleValue
	{
		public abstract double doc(int doc, FieldType ft, NumberedTermEnum te)throws IOException;
		public static termDoubleValue INSTANCE(UnInvertedField uni)
		{
			if(uni.dataType.equals(UnInvertedFieldUtils.Datatype.d_long))
			{
				return new termDoubleValue_indexl(uni);
			}
			
			if(uni.dataType.equals(UnInvertedFieldUtils.Datatype.d_string))
			{
				return new termDoubleValue_indexl(uni);
			}
			
			if(uni.dataType.equals(UnInvertedFieldUtils.Datatype.d_double))
			{
				return new termDoubleValue_indexd(uni);
			}
			
			return new termDoubleValue_default(uni);
		}
	}
	

	
	public static class termDoubleValue_indexl extends termDoubleValue
	{
		UnInvertedField uni;
		BlockArrayReadInt tm;
		BlockArray<Long> termValueLong;
		public termDoubleValue_indexl(UnInvertedField uni){
			this.uni=uni;
			this.tm=UnInvertedFieldUtils.getBlockArrayRead(this.uni);
			this.termValueLong=this.uni.termValueLong;
		}
		
		public double doc(int doc, FieldType ft, NumberedTermEnum te)
		{
			return this.termValueLong.get(tm.get(doc));
		}
	}
	
	

	public static class termDoubleValue_indexd extends termDoubleValue
	{
		UnInvertedField uni;
		BlockArrayReadInt tm;
		BlockArray<Double> termValueDouble;

		public termDoubleValue_indexd(UnInvertedField uni){
			this.uni=uni;
			this.tm=UnInvertedFieldUtils.getBlockArrayRead(this.uni);
			this.termValueDouble=this.uni.termValueDouble;
		}
		
		public double doc(int doc, FieldType ft, NumberedTermEnum te)
		{
			return this.termValueDouble.get(tm.get(doc));
		}
	}
	
	public static class termDoubleValue_default extends termDoubleValue
	{
		UnInvertedField uni;
		BlockArrayReadInt tm;

		public termDoubleValue_default(UnInvertedField uni){
			this.uni=uni;
			this.tm=UnInvertedFieldUtils.getBlockArrayRead(this.uni);
		}
		
		public double doc(int doc, FieldType ft, NumberedTermEnum te) throws IOException
		{
			int termNum=this.tm.get(doc);
			if(termNum<0||termNum>this.uni.maxTermNum)
			{
				return 0d;
			}
			String termText = this.uni.getTermText(te, termNum);
			if (termText != null) {
				return MdrillUtils.ParseDouble(ft.indexedToReadable(termText));
			}
			return 0d;
		}
	}
}
