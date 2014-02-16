/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.solr.request.uninverted;

import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.SegmentReader;
import org.apache.lucene.index.SegmentTermDocs;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.FieldInfo.IndexOptions;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrCore;

import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.schema.TrieField;
import org.apache.solr.search.*;
import org.apache.solr.handler.component.StatsValues;
import org.apache.lucene.util.cache.Cache;
import org.apache.lucene.util.cache.SimpleLRUCache;
import org.apache.solr.request.BigReUsedBuffer;
import org.apache.solr.request.BigReUsedBuffer.BlockArray;
import org.apache.solr.request.mdrill.MdrillUtils;
import org.apache.solr.request.uninverted.GrobalCache.ILruMemSizeCache;
import org.apache.solr.request.uninverted.GrobalCache.ILruMemSizeKey;
import org.apache.solr.request.uninverted.UnInvertedFieldTermNumRead.*;
import org.apache.solr.request.uninverted.UnInvertedFieldUtils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.alimama.mdrill.buffer.LuceneUtils;
import com.alimama.mdrill.utils.UniqConfig;

import java.io.IOException;


import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.CRC32;

public class UnInvertedField extends UnInvertedFieldBase {
	 public static Logger log = LoggerFactory.getLogger(UnInvertedField.class);

	public static double MINVALUE_FILL=Math.min(Long.MIN_VALUE,Double.MIN_VALUE);
	public static double MINVALUE=MINVALUE_FILL+10000;
	public static int TNUM_OFFSET = 2;
	public boolean isMultiValued = false;
	public boolean isNullField = false;
	public TermNumReadInterface tnr;
	
	

private void setSingleValue(TermIndex.QuickNumberedTermEnum te,SegmentReader reader,String key,boolean isReadDouble) throws IOException
{
	UnInvertedField.log.info("setSingleValue QuickNumberedTermEnum " + this.field + " field "	+ this.isMultiValued + "@" + key);
	int maxDoc = reader.maxDoc();
	int maxDocOffset = maxDoc + 2;
	this.index = INT_BUFFER.calloc(maxDocOffset, BigReUsedBuffer.INT_CREATE, -1);
	int[] docs = new int[1000];
	int[] freqs = new int[1000];
	if(isReadDouble)
	{
		if (this.dataType == Datatype.d_long|| this.dataType == Datatype.d_string) {
			this.termValueLong =LONG_BUFFER.calloc(maxDocOffset, BigReUsedBuffer.LONG_CREATE, (long)MINVALUE_FILL); 
		}
		if (this.dataType == Datatype.d_double) {
			this.termValueDouble = DOUBLE_BUFFER.calloc(maxDocOffset, BigReUsedBuffer.DOUBLE_CREATE, MINVALUE_FILL);
		}
	}
	
	 FieldInfo fi = reader.getFieldInfo().fieldInfo(this.field);
	   IndexOptions indexOptions = (fi != null) ? fi.indexOptions : IndexOptions.DOCS_AND_FREQS_AND_POSITIONS;
	   boolean currentFieldStoresPayloads = (fi != null) ? fi.storePayloads : false;
	
	this.maxTermNum=0;
	while(te.next()) {

		int termNum = te.getTermNumber();
		termsInverted++;
		SegmentTermDocs td = reader.SegmentTermDocs(1024);
		td.seekDocs(te.getDocPos(), te.getDocCount(), indexOptions,currentFieldStoresPayloads);
		for (;;) {
			int n = td.read(docs, freqs);
			if (n <= 0) {
				break;
			}
			for (int i = 0; i < n; i++) {
				termInstances++;
				this.index.set(docs[i], termNum) ;
			}
		}
		maxTermNum=Math.max(maxTermNum, termNum);
		if(isReadDouble)
		{
			if (dataType == Datatype.d_long) {
				this.termValueLong.set(termNum, te.getVVVlong()) ;
			} else if (dataType == Datatype.d_double) {
				this.termValueDouble.set(termNum, Double.longBitsToDouble(te.getVVVlong()));
			} else if (dataType == Datatype.d_string) {// for dist
				this.termValueLong.set(termNum, te.getVVVlong());
			}
		}
	}

	if (termInstances == 0) {
		INT_BUFFER.free(this.index);
		this.index = null;
	}else{ 
		
		int nullTerm=maxTermNum+1;
		int finalTerm=maxTermNum+2;
		this.setFinalIndex(nullTerm,finalTerm);
		
		if(isReadDouble)
		{
			if (this.dataType == Datatype.d_long|| this.dataType == Datatype.d_string) {
				BlockArray<Long> bigTermValue=this.termValueLong;
				int size=bigTermValue.getSize();
				this.termValueLong =LONG_BUFFER.calloc(finalTerm, BigReUsedBuffer.LONG_CREATE, (long)MINVALUE_FILL); 
				for(int i=0;i<finalTerm&&i<size;i++)
				{
					Long v=bigTermValue.get(i);
					this.termValueLong.set(i, v);
				}
				this.termValueLong.set(nullTerm, (long)MINVALUE_FILL);
				LONG_BUFFER.free(bigTermValue);
				bigTermValue=null;
			}
			if (this.dataType == Datatype.d_double) {
				BlockArray<Double> bigTermValue=this.termValueDouble;
				int size=bigTermValue.getSize();
				this.termValueDouble = DOUBLE_BUFFER.calloc(finalTerm, BigReUsedBuffer.DOUBLE_CREATE, MINVALUE_FILL);
				for(int i=0;i<finalTerm&&i<size;i++)
				{
					Double v=bigTermValue.get(i);
					this.termValueDouble.set(i, v);
				}
				this.termValueDouble.set(nullTerm, MINVALUE_FILL);
				DOUBLE_BUFFER.free(bigTermValue);
				bigTermValue=null;
			}
			
		}
	}
}
	private void setSingleValue( NumberedTermEnum te,IndexReader reader,String key) throws IOException
	{
		UnInvertedField.log.info("setSingleValue " + this.field + " field "	+ this.isMultiValued + "@" + key);
		int maxDoc = reader.maxDoc();
		int maxDocOffset = maxDoc + 2;
		this.index = INT_BUFFER.calloc(maxDocOffset, BigReUsedBuffer.INT_CREATE, -1);
		int[] docs = new int[1000];
		int[] freqs = new int[1000];
		if (this.dataType == Datatype.d_long|| this.dataType == Datatype.d_string) {
			this.termValueLong =LONG_BUFFER.calloc(maxDocOffset, BigReUsedBuffer.LONG_CREATE, (long)MINVALUE_FILL); 
		}
		if (this.dataType == Datatype.d_double) {
			this.termValueDouble = DOUBLE_BUFFER.calloc(maxDocOffset, BigReUsedBuffer.DOUBLE_CREATE, MINVALUE_FILL);
		}
		
		this.maxTermNum=0;
		for (;;) {
			Term t = te.term();
			if (t == null) {
				break;
			}

			int termNum = te.getTermNumber();
			termsInverted++;
			TermDocs td = te.getTermDocs();
			td.seek(te);
			for (;;) {
				int n = td.read(docs, freqs);
				if (n <= 0) {
					break;
				}
				for (int i = 0; i < n; i++) {
					termInstances++;
					this.index.set(docs[i], termNum) ;
				}
			}
			maxTermNum=Math.max(maxTermNum, termNum);
			if (dataType == Datatype.d_long) {
				this.termValueLong.set(termNum, Long.parseLong(ft.indexedToReadable(t.text()))) ;
			} else if (dataType == Datatype.d_double) {
				this.termValueDouble.set(termNum, MdrillUtils.ParseDouble(ft.indexedToReadable(t.text())));
			} else if (dataType == Datatype.d_string) {// for dist
				CRC32 crc32 = new CRC32();
				crc32.update(new String(ft.indexedToReadable(t.text())).getBytes());
				this.termValueLong.set(termNum, crc32.getValue());
			}
			te.next();
		}

		if (termInstances == 0) {
			INT_BUFFER.free(this.index);
			this.index = null;
		}else{ 
			
			int nullTerm=maxTermNum+1;
			int finalTerm=maxTermNum+2;
			this.setFinalIndex(nullTerm,finalTerm);
			
			if (this.dataType == Datatype.d_long|| this.dataType == Datatype.d_string) {
				BlockArray<Long> bigTermValue=this.termValueLong;
				int size=bigTermValue.getSize();
				this.termValueLong =LONG_BUFFER.calloc(finalTerm, BigReUsedBuffer.LONG_CREATE, (long)MINVALUE_FILL); 
				for(int i=0;i<finalTerm&&i<size;i++)
				{
					Long v=bigTermValue.get(i);
					this.termValueLong.set(i, v);
				}
				this.termValueLong.set(nullTerm, (long)MINVALUE_FILL);
				LONG_BUFFER.free(bigTermValue);
				bigTermValue=null;
			}
			if (this.dataType == Datatype.d_double) {
				BlockArray<Double> bigTermValue=this.termValueDouble;
				int size=bigTermValue.getSize();
				this.termValueDouble = DOUBLE_BUFFER.calloc(finalTerm, BigReUsedBuffer.DOUBLE_CREATE, MINVALUE_FILL);
				for(int i=0;i<finalTerm&&i<size;i++)
				{
					Double v=bigTermValue.get(i);
					this.termValueDouble.set(i, v);
				}
				this.termValueDouble.set(nullTerm, MINVALUE_FILL);
				DOUBLE_BUFFER.free(bigTermValue);
				bigTermValue=null;
			}
		}
	}
	
	public void extendFree()
	{
		try{
		if(this.cachete!=null)
		{
			
			this.cachete.close();
			
		}
		}catch(Throwable e)
		{
			log.info("extendFree",e);
		}
		super.extendFree();

	}
	
	TermIndex.QuickNumberedTermEnum cachete=null;
	private void setSegmentReader(SegmentReader reader)
	{
		if(this.cachete!=null)
		{
			try{
			this.cachete.resetQuickTis(reader.getQuickTis());
			}catch(Throwable e)
			{
				log.info("setSegmentReader",e);
			}
		}
	}
	private void uninvert(String field,SegmentReader reader,IndexSchema schema,boolean isreadDouble) throws IOException {
		UnInvertedField.log.info("####UnInverted#### SegmentReader begin");
		this.field = field;
		if(this.checkEmpty())
		{
			return ;
		}
		
		long startTime = System.currentTimeMillis();

		FieldType schemaft=schema.getFieldType(field);
		String prefix=TrieField.getMainValuePrefix(schemaft);
		SchemaField sf = schema.getField(field);
		this.ft = sf.getType();
		this.isMultiValued = ft.isMultiValued();
		if(this.isMultiValued)
		{
			throw new IOException("unsupport MultiValued");
		}
		this.dataType = UnInvertedFieldUtils.getDataType(this.ft);
		this.tnr = new TermNumReadSingle();

		String key = LuceneUtils.crcKey(reader);
		
		this.dataType = UnInvertedFieldUtils.getDataType(this.ft);

		this.ti = new TermIndex(field, prefix);

		boolean isbyQuick=false;
		try{
			
			if(reader.isSupportQuick())
			{
				Long pos=reader.getQuickPos(this.field);
				Integer cnt=reader.getQuickCount(this.field);
				
				if(pos!=null&&cnt!=null)
				{
					Long posval=reader.getQuickDoublePos(this.field);
					TermIndex.QuickNumberedTermEnum te=ti.getEnumerator(reader,reader.getQuickTis(),pos,cnt,posval,isreadDouble);
					this.setSingleValue(te,reader, key,isreadDouble);
					numTermsInField = te.getTermNumber();
					te.TermIndexTrans();
					cachete=te;
					isbyQuick=true;
				}
				
				
			}
		}catch(Throwable e)
		{
			log.error("readFail",e);
		}

		
		if(!isbyQuick){
			log.info("#####isbyQuick false");

			NumberedTermEnum te = ti.getEnumerator(reader);
			this.setSingleValue(te, reader, key);
			numTermsInField = te.getTermNumber();
			te.close();
		}

		
		total_time = (int) (System.currentTimeMillis() - startTime);
		this.tnr.setUni(this);
		UnInvertedField.log.info("####UnInverted#### Create "+this.toString() +" " + this.isMultiValued + "@" + key+ ",dataType=" + dataType+",");
		
	}
		
	private void uninvert(String field,
			SolrIndexSearcher searcher, IndexReader reader) throws IOException {
		UnInvertedField.log.info("####UnInverted#### begin");

		this.field = field;
		if(this.checkEmpty())
		{
			return ;
		}
				
		long startTime = System.currentTimeMillis();

		FieldType schemaft=searcher.getSchema().getFieldType(field);
		String prefix=TrieField.getMainValuePrefix(schemaft);
		this.ti = new TermIndex(field, prefix);
		SchemaField sf = searcher.getSchema().getField(field);
		this.ft = sf.getType();
		this.isMultiValued = ft.isMultiValued();
		
		if(this.isMultiValued)
		{
			throw new IOException("unsupport MultiValued");
		}
		
		this.dataType = UnInvertedFieldUtils.getDataType(this.ft);
		this.tnr = new TermNumReadSingle();

		String key = LuceneUtils.crcKey(reader);
//		boolean isUsedCache =!LinkFSDirectory.isRealTime() &&searcher.getFieldcacheDir() != null;
//		if (isUsedCache) {
//			if (this.readUninvertCache(reader, searcher, startTime)) {
//				this.tnr.setUni(this);
//				this.dataType = UnInvertedFieldUtils.getDataType(this.ft);
//				total_time = (int) (System.currentTimeMillis() - startTime);
//				UnInvertedField.log.info("####UnInverted#### Load "+this.toString() +" " + this.isMultiValued + "@" + key	+ ",dataType=" + dataType+",");
//				return;
//			}else{
//				this.setdefault();
//			}
//		}
		
		this.dataType = UnInvertedFieldUtils.getDataType(this.ft);

		
		NumberedTermEnum te = ti.getEnumerator(reader);
		this.setSingleValue(te, reader, key);

		numTermsInField = te.getTermNumber();
		te.close();

//		if (isUsedCache) {
//			this.writeUnvertCache(reader, searcher);
//		}

		total_time = (int) (System.currentTimeMillis() - startTime);
		this.tnr.setUni(this);
		UnInvertedField.log.info("####UnInverted#### Create "+this.toString() +" " + this.isMultiValued + "@" + key+ ",dataType=" + dataType+",");
	}
  
	private boolean checkEmpty()
	{
		if (this.field.indexOf("higoempty_") >= 0) {
			this.isNullField = true;
		}
		if (this.isNullField) {
			this.tnr = new TermNumReadNull();
			this.tnr.setUni(this);
			return true;
		}
		return false;
	}
  public NumberedTermEnum getTi(SolrIndexSearcher searcher) throws IOException
  {
	  return ti.getEnumerator(searcher.getReader());
  }
  
  public NumberedTermEnum getTi(SegmentReader reader) throws IOException
  {
	  return ti.getEnumerator(reader);
  }
  
  public int getNullTm() 
  {
	  return this.nullTermNum;
  }
  //group by
  public Integer termNum(int doc) throws IOException
  {
	  return this.tnr.termNum(doc, this.nullTermNum);
  }
  
  //group by
  public String tNumToString(int tnum,FieldType ft,NumberedTermEnum te,String def) throws IOException
  {
	  return this.tnr.tNumToString(tnum, ft,te,def);
  }
  
  //sum,dist
  public double quickToDouble(int tnum,FieldType ft,NumberedTermEnum te) throws IOException
  {
	  return this.tnr.quickToDouble(tnum, ft,te);
  }
	
	private Cache<Integer, String> termsCache = Cache.synchronizedCache(new SimpleLRUCache<Integer, String>(UniqConfig.getTermCacheSize()));

	public int getTermNum(NumberedTermEnum te, String text,FieldType ft) throws IOException
	{
		
		if((text.startsWith("_")||text.startsWith("null"))&&!this.dataType.equals(Datatype.d_string))
		{
			return this.nullTermNum;
		}
		if(te.skipTo(ft.toInternal(text)))
		{
			return te.getTermNumber();
		}else{
			return this.nullTermNum;
		}
	}

	public String getTermText(NumberedTermEnum te, int termNum)
			throws IOException {
		String termText = termsCache.get(termNum);
		if (termText != null) {
			return termText;
		}

		if(!te.skipTo(termNum))
		{
			return null;
		}
		Term t = te.term();
		if (t == null) {
			return null;
		}
		termText = t.text();
		if (termText == null) {
			return null;
		}
		termsCache.put(termNum, termText);
		return termText;
	}
	
  @Override
  	public String toString() {
	  StringBuffer membuffer=new StringBuffer();
	    if (index != null)
	    {
	    	membuffer.append(",").append("index="+index.getMemSize()+"@"+index.getSize());
	    }
	    if (indexshort != null)
	    {
	    	membuffer.append(",").append("indexshort="+indexshort.getMemSize()+"@"+indexshort.getSize());
	    }
	    
	    if (indexbyte != null)
	    {
	    	membuffer.append(",").append("indexbyte="+indexbyte.getMemSize()+"@"+indexbyte.getSize());
	    }
	    
	    if (termValueDouble != null)
	    {
	    	membuffer.append(",").append("termValueDouble="+termValueDouble.getMemSize()+"@"+termValueDouble.getSize());
	    }
	    
	    if (termValueLong != null)
	    {
	    	membuffer.append(",").append("termValueLong="+termValueLong.getMemSize()+"@"+termValueLong.getSize());
	    }
	    
    return "{field=" + field
            + ",memSize="+(memSize()*1.0/1024/1024)+"mb"
            + ",time="+total_time
            + ",nTerms="+numTermsInField
            + ",nullTermNum="+nullTermNum
            + ",maxTermNum="+maxTermNum
            + ",termInstances="+termInstances+membuffer.toString()
            + "}";
  }
  
  	public static UnInvertedField getUnInvertedField(String field, SolrIndexSearcher searcher) throws IOException {
	  return getUnInvertedField(field, searcher.getReader());
  	}

	private UnInvertedField(String field, SolrIndexSearcher searcher,
			IndexReader reader) throws IOException {
			uninvert(field, searcher, reader);
	}
	
	private UnInvertedField(String field, SegmentReader reader,IndexSchema schema,boolean isreadDouble) throws IOException {
		uninvert(field, reader,schema,isreadDouble);
	}
  
	
	
	public static UnInvertedField getUnInvertedField(final String field,
			SolrIndexReader reader) throws IOException {
		final Cache<ILruMemSizeKey, ILruMemSizeCache> cache = GrobalCache.fieldValueCache;
		final SolrIndexSearcher searcher = reader.getSearcher();
		final ILruMemSizeKey key = new GrobalCache.StringKey(searcher.getPartionKey() + "@@" + field + "@@"	+ LuceneUtils.crcKey(reader));
		UnInvertedField uif = (UnInvertedField)cache.get(key);
		if (uif == null) {
			Object lockobj=LOCK.get(key);
			if(lockobj==null)
			{
				lockobj=new Object();
				LOCK.put(key, lockobj);
			}
			
			final Object lockthr=lockobj;
			
			
			ExecutorCompletionService<UnivertPool> serv=new ExecutorCompletionService<UnivertPool>(pool);
			Callable<UnivertPool> task = new Callable<UnivertPool>() {
			      public UnivertPool call() throws Exception {
						UnivertPool rtnuif=new  UnivertPool();
					try{
						synchronized (lockthr) {
							rtnuif.uni =  (UnInvertedField)cache.get(key);
							if (rtnuif.uni == null) {
								rtnuif.uni = new UnInvertedField(field, searcher,searcher.getReader());
								cache.put(key, rtnuif.uni);
							}
						}
					}catch(IOException e){
						rtnuif.e=e;
					}finally{
						rtnuif.isfinish.set(true);
					}
					return rtnuif;

				}
			};
			serv.submit(task);
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
		
		}
		uif.refCnt.incrementAndGet();
		return uif;
	}
	
	private static ThreadPoolExecutor pool=new ThreadPoolExecutor(Math.max(UniqConfig.getUnivertedFieldThreads()/2, 1), UniqConfig.getUnivertedFieldThreads(),
            100L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>());
	
	public static class UnivertPool{
		AtomicBoolean isfinish=new AtomicBoolean(false);
		UnInvertedField uni;
		IOException e=null;
	}

	private static Cache<ILruMemSizeKey, Object> LOCK = Cache.synchronizedCache(new SimpleLRUCache<ILruMemSizeKey, Object>(256));

	
	public static UnInvertedField getUnInvertedField(final String field,final SegmentReader reader,String partion,final IndexSchema schema,final boolean isreadDouble) throws IOException
	{
		final Cache<ILruMemSizeKey, ILruMemSizeCache> cache = GrobalCache.fieldValueCache;
		final ILruMemSizeKey key = new GrobalCache.StringKey("seg@"+String.valueOf(isreadDouble) + "@" + field + "@"	+reader.getStringCacheKey()+"@"+reader.getSegmentName());
		UnInvertedField uif = (UnInvertedField)cache.get(key);
		if (uif == null) {
			Object lockobj=LOCK.get(key);
			if(lockobj==null)
			{
				lockobj=new Object();
				LOCK.put(key, lockobj);
			}
			
			final Object lockthr=lockobj;
			
			ExecutorCompletionService<UnivertPool> serv=new ExecutorCompletionService<UnivertPool>(pool);
			Callable<UnivertPool> task = new Callable<UnivertPool>() {
			      public UnivertPool call() throws Exception {
					UnivertPool rtnuif=new  UnivertPool();
					try{
						synchronized (lockthr) {
							rtnuif.uni =  (UnInvertedField)cache.get(key);
							if (rtnuif.uni == null) {
								rtnuif.uni = new UnInvertedField(field, reader,schema,isreadDouble);
								cache.put(key, rtnuif.uni);
							}
						}
					}catch(IOException e){
						rtnuif.e=e;
					}finally{
						rtnuif.isfinish.set(true);
					}
					return rtnuif;
				}
			};
			serv.submit(task);
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
		   
		}
		uif.setSegmentReader(reader);
		uif.refCnt.incrementAndGet();
		return uif;
	}
	
	public NamedList getCounts(SolrIndexSearcher searcher, DocSet baseDocs,
			int offset, int limit, Integer mincount, boolean missing,
			String sort, String prefix, Boolean returnPair, boolean isRow)
			throws IOException {
		return new NamedList();
	}

	public StatsValues getStats(SolrIndexSearcher searcher, DocSet baseDocs,
			String[] facet) throws IOException {
		return null;
	}
}



