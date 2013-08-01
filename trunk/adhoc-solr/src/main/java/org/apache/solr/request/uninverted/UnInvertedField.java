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

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.SolrException;
import org.apache.solr.core.SolrCore;

import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.schema.TrieField;
import org.apache.solr.search.*;
import org.apache.solr.handler.component.StatsValues;
import org.apache.lucene.store.LinkFSDirectory;
import org.apache.lucene.util.cache.Cache;
import org.apache.lucene.util.cache.SimpleLRUCache;
import org.apache.solr.request.BigReUsedBuffer;
import org.apache.solr.request.BigReUsedBuffer.BlockArray;
import org.apache.solr.request.mdrill.MdrillPorcessUtils;
import org.apache.solr.request.uninverted.GrobalCache.ILruMemSizeCache;
import org.apache.solr.request.uninverted.GrobalCache.ILruMemSizeKey;
import org.apache.solr.request.uninverted.UnInvertedFieldTermNumRead.*;
import org.apache.solr.request.uninverted.UnInvertedFieldUtils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.alimama.mdrill.buffer.LuceneUtils;
import com.alimama.mdrill.utils.UniqConfig;

import java.io.IOException;

import java.util.zip.CRC32;

public class UnInvertedField extends UnInvertedFieldBase implements GrobalCache.ILruMemSizeCache{
	  public static Logger log = LoggerFactory.getLogger(UnInvertedField.class);

	public static double MINVALUE_FILL=Math.min(Long.MIN_VALUE,Double.MIN_VALUE);
	public static double MINVALUE=MINVALUE_FILL+10000;
	public static int TNUM_OFFSET = 2;
	public boolean isMultiValued = false;
	public boolean isNullField = false;
	public TermNumReadInterface tnr;
	
	public UnInvertedField(String field, SolrIndexSearcher searcher,
			IndexReader reader) throws IOException {
		uninvert(field, searcher, reader);
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
				this.termValueDouble.set(termNum, MdrillPorcessUtils.ParseDouble(ft.indexedToReadable(t.text())));
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
	
	private void setMultyValue(NumberedTermEnum te, IndexReader reader,String key) throws IOException
	{
		UnInvertedField.log.info("setMultyValue " + this.field + " field "	+ this.isMultiValued + "@" + key);
		int maxDoc = reader.maxDoc();
		int maxDocOffset = maxDoc + 2;
		this.index = INT_BUFFER.calloc(maxDocOffset,BigReUsedBuffer.INT_CREATE, -1);
		int[] docs = new int[1000];
		int[] freqs = new int[1000];
		final byte[][] bytes = new byte[maxDoc][];
		this.maxTermNum = 0;
		byte[] tempArr = new byte[12];
		final int[] lastTerm = new int[maxDoc];
		for (;;) {
			Term t = te.term();
			if (t == null) {
				break;
			}

			int termNum = te.getTermNumber();
			maxTermNum = Math.max(maxTermNum, termNum);
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
					int doc = docs[i];
					int delta = termNum - lastTerm[doc] + TNUM_OFFSET;
					lastTerm[doc] = termNum;
					int val = index.get(doc);
					if ((val & 0xff) == 1) {
						int pos = val >>> 8;
						int ilen = UnInvertedFieldUtils.vIntSize(delta);
						byte[] arr = bytes[doc];
						int newend = pos + ilen;
						if (newend > arr.length) {
							int newLen = (newend + 3) & 0xfffffffc; // 4 byte
																	// alignment
							byte[] newarr = new byte[newLen];
							System.arraycopy(arr, 0, newarr, 0, pos);
							arr = newarr;
							bytes[doc] = newarr;
						}
						pos = UnInvertedFieldUtils.writeInt(delta, arr, pos);
						index.set(doc, (pos << 8) | 1); // update pointer to end
														// index in byte[]
					} else {
						int ipos;
						if (val == 0) {
							ipos = 0;
						} else if ((val & 0x0000ff80) == 0) {
							ipos = 1;
						} else if ((val & 0x00ff8000) == 0) {
							ipos = 2;
						} else if ((val & 0xff800000) == 0) {
							ipos = 3;
						} else {
							ipos = 4;
						}

						int endPos = UnInvertedFieldUtils.writeInt(delta,
								tempArr, ipos);
						if (endPos <= 4) {
							// value will fit in the integer... move bytes back
							for (int j = ipos; j < endPos; j++) {
								val |= (tempArr[j] & 0xff) << (j << 3);
							}
							index.set(doc, val);
						} else {
							// value won't fit... move integer into byte[]
							for (int j = 0; j < ipos; j++) {
								tempArr[j] = (byte) val;
								val >>>= 8;
							}
							// point at the end index in the byte[]
							index.set(doc, (endPos << 8) | 1);
							bytes[doc] = tempArr;
							tempArr = new byte[12];
						}
					}
				}
			}
			te.next();
		}

		if (termInstances == 0) {
			INT_BUFFER.free(this.index);
			this.index = null;
			tnums = null;
		} else {
			tnums = new byte[256][];
			for (int pass = 0; pass < 256; pass++) {
				byte[] target = tnums[pass];
				int pos = 0; // end in target;
				if (target != null) {
					pos = target.length;
				} else {
					target = new byte[4096];
				}

				for (int docbase = pass << 16; docbase < maxDoc; docbase += (1 << 24)) {
					int lim = Math.min(docbase + (1 << 16), maxDoc);
					for (int doc = docbase; doc < lim; doc++) {
						int val = index.get(doc);
						if ((val & 0xff) == 1) {
							int len = val >>> 8;
							index.set(doc, (pos << 8) | 1); // change index to
															// point to start of
															// array
							if ((pos & 0xff000000) != 0) {
								// we only have 24 bits for the array index
								throw new SolrException(
										SolrException.ErrorCode.BAD_REQUEST,
										"Too many values for UnInvertedField faceting on field "
												+ field);
							}
							byte[] arr = bytes[doc];
							bytes[doc] = null; // IMPORTANT: allow GC to avoid
												// OOM
							if (target.length <= pos + len) {
								int newlen = target.length;
								while (newlen <= pos + len)
									newlen <<= 1; // doubling strategy
								byte[] newtarget = new byte[newlen];
								System.arraycopy(target, 0, newtarget, 0, pos);
								target = newtarget;
							}
							System.arraycopy(arr, 0, target, pos, len);
							pos += len + 1; // skip single byte at end and leave
											// it 0 for terminator
						}
					}
				}

				// shrink array
				if (pos < target.length) {
					byte[] newtarget = new byte[pos];
					System.arraycopy(target, 0, newtarget, 0, pos);
					target = newtarget;
					if (target.length > (1 << 24) * .9) {
						SolrCore.log
								.warn("Approaching too many values for UnInvertedField faceting on field '"
										+ field
										+ "' : bucket size="
										+ target.length);
					}
				}

				tnums[pass] = target;

				if ((pass << 16) > maxDoc)
					break;
			}

			int finalTerm = maxTermNum + 2;
			this.setFinalIndex(-1, finalTerm);
		}
	}
	
	private synchronized void uninvert(String field,
			SolrIndexSearcher searcher, IndexReader reader) throws IOException {
		SolrCore.log.info("####UnInverted#### begin");

		this.field = field;
		if (this.field.indexOf("higoempty_") >= 0) {
			this.isNullField = true;
		}
		if (this.isNullField) {
			this.tnr = new TermNumReadNull();
			this.tnr.setUni(this);
			return;
		}
				
		long startTime = System.currentTimeMillis();

		FieldType schemaft=searcher.getSchema().getFieldType(field);
		String prefix=TrieField.getMainValuePrefix(schemaft);
		this.ti = new TermIndex(field, prefix);
		SchemaField sf = searcher.getSchema().getField(field);
		this.ft = sf.getType();
		this.isMultiValued = ft.isMultiValued();
		this.dataType = UnInvertedFieldUtils.getDataType(this.ft);
		this.tnr = UnInvertedFieldUtils.getReadInterface(this.isMultiValued);

		String key = LuceneUtils.crcKey(reader);
		boolean isUsedCache =!LinkFSDirectory.isRealTime() &&searcher.getFieldcacheDir() != null;
		if (isUsedCache) {
			if (this.readUninvertCache(reader, searcher, startTime)) {
				this.tnr.setUni(this);
				this.dataType = UnInvertedFieldUtils.getDataType(this.ft);
				total_time = (int) (System.currentTimeMillis() - startTime);
				SolrCore.log.info("####UnInverted#### Load "+this.toString() +" " + this.isMultiValued + "@" + key	+ ",dataType=" + dataType+",");
				return;
			}else{
				this.setdefault();
			}
		}
		
		this.dataType = UnInvertedFieldUtils.getDataType(this.ft);

		
		NumberedTermEnum te = ti.getEnumerator(reader);
		
		if (!this.isMultiValued) {
			this.setSingleValue(te, reader, key);
		} else {
			this.setMultyValue(te, reader, key);
		}

		numTermsInField = te.getTermNumber();
		te.close();

		if (isUsedCache) {
			this.writeUnvertCache(reader, searcher);
		}

		total_time = (int) (System.currentTimeMillis() - startTime);
		this.tnr.setUni(this);
		SolrCore.log.info("####UnInverted#### Create "+this.toString() +" " + this.isMultiValued + "@" + key+ ",dataType=" + dataType+",");
	}
  

  public NumberedTermEnum getTi(SolrIndexSearcher searcher) throws IOException
  {
	  return ti.getEnumerator(searcher.getReader());
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
  
  
  
  public void LRUclean()
  {
	  if (this.refCnt.get() == 0) {
			this.free();
		}
  }

  public static UnInvertedField getUnInvertedField(String field, SolrIndexSearcher searcher) throws IOException {
	  return getUnInvertedField(field, searcher.getReader());
  }

	public static UnInvertedField getUnInvertedField(String field,
			SolrIndexReader reader) throws IOException {
		Cache<ILruMemSizeKey, ILruMemSizeCache> cache = GrobalCache.fieldValueCache;
		SolrIndexSearcher searcher = reader.getSearcher();
		ILruMemSizeKey key = new GrobalCache.StringKey(searcher.getPartionKey() + "@@" + field + "@@"	+ LuceneUtils.crcKey(reader));
		UnInvertedField uif = (UnInvertedField)cache.get(key);
		if (uif == null) {
			synchronized (cache) {
				uif =  (UnInvertedField)cache.get(key);
				if (uif == null) {
					uif = new UnInvertedField(field, searcher,searcher.getReader());
					cache.put(key, uif);
				}
			}
		}
		uif.refCnt.incrementAndGet();
		return uif;
	}
	

	  public NamedList getCounts(SolrIndexSearcher searcher, DocSet baseDocs, int offset, int limit, Integer mincount, boolean missing, String sort, String prefix,Boolean returnPair,boolean isRow) throws IOException {
		  return new NamedList();
	  }
	  


		public StatsValues getStats(SolrIndexSearcher searcher, DocSet baseDocs,
				String[] facet) throws IOException {
			return null;
		}
}



