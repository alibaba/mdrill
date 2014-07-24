package org.apache.solr.request.uninverted;

import java.io.IOException;

import org.apache.lucene.index.DocValuesReader;
import org.apache.lucene.index.SegmentReader;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.search.BitDocSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MakeUnivertedFieldBySigment{
	public static Logger log = LoggerFactory.getLogger(MakeUnivertedFieldBySigment.class);
	
	static boolean isforbiden=false;
	public static boolean  makeInit(UnInvertedField uni,BitDocSet baseAdvanceDocs,String field, SegmentReader reader,IndexSchema schema,boolean isreadDouble) throws IOException, CloneNotSupportedException
	{
		long t1=System.currentTimeMillis();

		int fieldNumber=reader.getFieldNum(field);
		if(isforbiden||!reader.isSupportQuick()||fieldNumber<0)
		{
			return false;
		}
		
		
		uni.init( field, reader, schema);
		long t2=System.currentTimeMillis();

		uni.baseAdvanceDocs=UnInvertedField.ajustBase(4,baseAdvanceDocs, reader);
		long t3=System.currentTimeMillis();

		if(uni.checkEmpty())
		{
			return true;
		}
		
		log.info("makeInit start:" + uni.field + " ,this.baseAdvanceDocs="+(uni.baseAdvanceDocs==null?0:uni.baseAdvanceDocs.size()));
		int maxDoc = reader.maxDoc();
		uni.startRamDocValue(maxDoc, reader, isreadDouble);
		long t4=System.currentTimeMillis();

		DocValuesReader docvalues=reader.getDocValues();
		int maxtm=RamDocValueFill.Fill(uni,reader.maxDoc(), true, uni.ti, docvalues, fieldNumber, isreadDouble, uni.baseAdvanceDocs);
		long t5=System.currentTimeMillis();
	
		uni.endRamDocValue(isreadDouble,maxtm);
		uni.tnr = uni.ramDocValue.getDocReader();
		uni.baseAdvanceDocs=null;
		long t6=System.currentTimeMillis();
		log.info("####makeInit end####:" +uni.field+","+(t6-t5)+"@"+(t5-t4)+"@"+(t4-t3)+"@"+(t3-t2)+"@"+(t2-t1));
		return true;	
	}
	

	public static boolean addDoclist(UnInvertedField uni, BitDocSet docs,
			String field, SegmentReader reader, IndexSchema schema,
			boolean isreadDouble) throws IOException, CloneNotSupportedException {
		int fieldNumber = reader.getFieldNum(field);
		if (isforbiden||!reader.isSupportQuick() || fieldNumber < 0) {
			return false;
		}

		if (uni.checkEmpty()) {
			return true;
		}

		BitDocSet tmp = null;
		if (docs != null) {
			tmp = (BitDocSet) docs.andNot(uni.bits);
			if (tmp != null && tmp.size() <= 0) {
				return true;
			}
		}

		log.info("addDoclist start" + uni.field + " ,this.baseAdvanceDocs="+(uni.baseAdvanceDocs==null?0:uni.baseAdvanceDocs.size()));

		uni.baseAdvanceDocs = UnInvertedField.ajustBase(4,tmp, reader);

		DocValuesReader docvalues = reader.getDocValues();
		RamDocValueFill.Fill(uni, reader.maxDoc(),false, uni.ti, docvalues, fieldNumber, isreadDouble,uni.baseAdvanceDocs);
		uni.baseAdvanceDocs=null;

		return true;
	}
}
