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
import org.apache.lucene.index.SegmentReader;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.search.*;
import org.apache.lucene.util.OpenBitSet;
import org.apache.lucene.util.cache.Cache;
import org.apache.solr.request.uninverted.GrobalCache.ILruMemSizeCache;
import org.apache.solr.request.uninverted.GrobalCache.ILruMemSizeKey;
import org.apache.solr.request.uninverted.UnInvertedFieldUtils.*;

import com.alimama.mdrill.buffer.LuceneUtils;
import com.alimama.mdrill.utils.UniqConfig;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public  class UnInvertedField extends UnInvertedFieldBase {
	public static Logger log = LoggerFactory.getLogger(UnInvertedField.class);

	public static ThreadPoolExecutor SUBMIT_POOL = new ThreadPoolExecutor(UniqConfig.getUnivertedFieldThreads(),UniqConfig.getUnivertedFieldThreads(), 100L, TimeUnit.SECONDS,	new LinkedBlockingQueue<Runnable>());
	
  	public static UnInvertedField getUnInvertedField(DocSet baseAdvanceDocs,String field, SolrIndexSearcher searcher) throws IOException {
	  return getUnInvertedField(baseAdvanceDocs,field, searcher.getReader());
  	}

	public static BitDocSet ajustBase(int times,BitDocSet baseAdvanceDocs,IndexReader reader)
	{
		try {
			if(baseAdvanceDocs==null)
			{
				return null;
			}
			int maxdoc=reader.maxDoc();
			int oversize=maxdoc/times;
			int size=baseAdvanceDocs.size();
			int maxinterval=reader.getMaxInterval();
			log.info("ajustBase "+maxinterval+",baseAdvanceDocs="+size+"@"+oversize+"@"+maxdoc+","+reader.getClass().getCanonicalName());
			if(size>=oversize||maxinterval>256)
			{
				return null;
			}
		} catch (Exception e) {
			return null;
		}
		
		return baseAdvanceDocs;
	}
	
	
	public static BitDocSet cloneBitset(DocSet baseAdvanceDocs,IndexReader reader)
	{
		if(baseAdvanceDocs instanceof BitDocSet)
		{
			BitDocSet rtn= (BitDocSet)baseAdvanceDocs;
		    OpenBitSet newbits = (OpenBitSet)(rtn.getBits().clone());
		    return new BitDocSet(newbits,rtn.size());
		}
		
	    OpenBitSet bs = new OpenBitSet(reader.maxDoc());
	    DocIterator iter = baseAdvanceDocs.iterator();
	    int pos=0;
		while (iter.hasNext()) {
			bs.fastSet(iter.nextDoc());
			pos++;
		}
		
		return new BitDocSet(bs,pos);
	}
  	
	public static UnInvertedField getUnInvertedField(final DocSet baseAdvanceDocs,final String field,final SegmentReader reader,String partion,final IndexSchema schema,final boolean isreadDouble) throws IOException
	{
		final ILruMemSizeKey key = new GrobalCache.StringKey("seg@"+String.valueOf(isreadDouble) + "@" + field + "@"+reader.getStringCacheKey()+"@"+reader.getSegmentName());
		ExecutorCompletionService<UnivertPool> submit=new ExecutorCompletionService<UnivertPool>(SUBMIT_POOL);
		final long t0=System.currentTimeMillis();

		Callable<UnivertPool> task = new Callable<UnivertPool>() {
		      public UnivertPool call() throws Exception {
				UnivertPool rtnuif=new  UnivertPool();
				try{
					long t1=System.currentTimeMillis();
					Cache<ILruMemSizeKey, ILruMemSizeCache> cache = GrobalCache.fieldValueCache;
					final Object lockthr=UnInvertedFieldUtils.getLock(key);
					synchronized (lockthr) {
						rtnuif.uni =  (UnInvertedField)cache.get(key);
						BitDocSet clonebitset=cloneBitset(baseAdvanceDocs,reader);
						long t2=System.currentTimeMillis();

						if (rtnuif.uni == null||rtnuif.uni.isShutDown()) {
							rtnuif.uni = new UnInvertedField();
							boolean issucecess=MakeUnivertedFieldBySigment.makeInit(rtnuif.uni,clonebitset,field, reader,schema,isreadDouble);
							if(!issucecess)
							{
								MakeUnivertedFieldByIndex forjoin=new MakeUnivertedFieldByIndex(rtnuif.uni);
								forjoin.makeInit(clonebitset, field, schema, reader);
							}
							cache.put(key, rtnuif.uni);
						}else{
							boolean issucecess=MakeUnivertedFieldBySigment.addDoclist(rtnuif.uni,clonebitset,field, reader,schema,isreadDouble);
							if(!issucecess)
							{
								MakeUnivertedFieldByIndex forjoin=new MakeUnivertedFieldByIndex(rtnuif.uni);
								forjoin.addDoclist(clonebitset, field, reader);
							}	
						}
						long t3=System.currentTimeMillis();

						log.info("####timetaken####:"+(t3-t2)+"@"+(t2-t1)+"@"+(t1-t0)+","+String.valueOf(rtnuif.uni));
					}
				}catch(IOException e){
					rtnuif.e=e;
				}
				return rtnuif;
			}
		};
		submit.submit(task);
		
		UnInvertedField uif=UnInvertedFieldUtils.takeUnf(submit);
		uif.refCnt.incrementAndGet();
		long t4=System.currentTimeMillis();
		log.info("####timetaken all####:"+(t4-t0)+","+String.valueOf(uif));
		return uif;
	}
	

	public static UnInvertedField getUnInvertedField(final DocSet baseAdvanceDocs,final String field,
			SolrIndexReader reader) throws IOException {
		final SolrIndexSearcher searcher = reader.getSearcher();
		final ILruMemSizeKey key = new GrobalCache.StringKey(searcher.getPartionKey() + "@@" + field + "@@"	+ LuceneUtils.crcKey(reader));
		final long t0=System.currentTimeMillis();

		ExecutorCompletionService<UnivertPool> submit=new ExecutorCompletionService<UnivertPool>(SUBMIT_POOL);
		Callable<UnivertPool> task = new Callable<UnivertPool>() {
		      public UnivertPool call() throws Exception {
				UnivertPool rtnuif=new  UnivertPool();
				try{
					
					final Cache<ILruMemSizeKey, ILruMemSizeCache> cache = GrobalCache.fieldValueCache;
					final Object lockthr=UnInvertedFieldUtils.getLock(key);
					synchronized (lockthr) {
						long t1=System.currentTimeMillis();

						rtnuif.uni =  (UnInvertedField)cache.get(key);
						SolrIndexReader reader=searcher.getReader();
						BitDocSet clonebitset=cloneBitset(baseAdvanceDocs,reader);

						if (rtnuif.uni == null||rtnuif.uni.isShutDown()) {
								rtnuif.uni = new UnInvertedField();
								MakeUnivertedFieldByIndex forjoin=new MakeUnivertedFieldByIndex(rtnuif.uni);
								forjoin.makeInit(clonebitset, field, searcher.getSchema(), reader);
								cache.put(key, rtnuif.uni);
							}else{
								MakeUnivertedFieldByIndex forjoin=new MakeUnivertedFieldByIndex(rtnuif.uni);
								forjoin.addDoclist(clonebitset, field, reader);
							}
						long t2=System.currentTimeMillis();

						log.info("timetaken by index:"+(t2-t1)+"@"+(t1-t0)+","+String.valueOf(rtnuif.uni));
						}
					
				}catch(IOException e){
					rtnuif.e=e;
				}
				
				return rtnuif;

			}
		};
		submit.submit(task);
		
		UnInvertedField uif=UnInvertedFieldUtils.takeUnf(submit);
		uif.refCnt.incrementAndGet();
		return uif;
	}
	
}
