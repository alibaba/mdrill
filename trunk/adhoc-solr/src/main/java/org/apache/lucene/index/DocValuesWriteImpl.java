package org.apache.lucene.index;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.util.IOUtils;
import org.apache.lucene.util.OpenBitSet;
import org.apache.solr.request.BlockBufferPool;
import org.apache.solr.request.BlockBufferPool.BlockArray;
import org.apache.solr.search.BitDocSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DocValuesWriteImpl implements DocValuesWriter{
//	public static Logger log = LoggerFactory.getLogger(DocValuesWriteImpl.class);

	private static int TERM_NULL_NUMBER=-1;
	public IndexOutput outputQuickTis = null;
	public IndexOutput outputQuickTisTxt = null;
	public IndexOutput outputQuickTisVal = null;
	
	private HashMap<Integer, Long> fieldPosTis = new HashMap<Integer, Long>();
	private HashMap<Integer, Long> fieldPosTisTxt = new HashMap<Integer, Long>();
	private HashMap<Integer, Long> fieldPosTisVal = new HashMap<Integer, Long>();

	public int current_alloc_size = BlockBufferPool.interval;
	
	private BlockArray<Integer> doc2Tm = BlockBufferPool.INT_POOL.calloc(current_alloc_size, BlockBufferPool.INT_CREATE, TERM_NULL_NUMBER);
//	BitDocSet bits = new BitDocSet(new OpenBitSet(10000000));

	private int maxdocid=0;
	public void collectDoc(int docid, int termNum) {
		if (this.current_alloc_size <= docid) {
			this.current_alloc_size = docid + BlockBufferPool.interval;
			this.doc2Tm = BlockBufferPool.INT_POOL.reCalloc(this.doc2Tm,this.current_alloc_size, BlockBufferPool.INT_CREATE, TERM_NULL_NUMBER);
		}
		maxdocid=Math.max(maxdocid, docid);
		doc2Tm.set(docid, termNum);
//		bits.add(docid);
	}
	public void collectTmIndex(String termValue) throws IOException {
		outputQuickTisTxt.writeString(termValue);
	}

	public void collectTm(long termValue) throws IOException {
		outputQuickTisVal.writeLong(termValue);
	}

	String fl="";
	public void start(int fieldNumber,String field) {
		this.fl=field;
//		log.info("start:"+this.fl+"@"+fieldNumber);
		this.maxdocid=0;
		outputQuickTis.startBits();
		fieldPosTis.put(fieldNumber, outputQuickTis.getFilePointer());
		fieldPosTisTxt.put(fieldNumber, outputQuickTisTxt.getFilePointer());
		fieldPosTisVal.put(fieldNumber, outputQuickTisVal.getFilePointer());
//		bits = new BitDocSet(new OpenBitSet(10000000));
//		debugindex=0;
	}
	

//	int debugindex=0;
	public void flushFieldDoc(int termNumCount) throws IOException {
		int size=Math.min(doc2Tm.getSize(), (maxdocid+1));
		outputQuickTis.writeInt(size);
		outputQuickTis.writeInt(termNumCount);
		int useBits=useBits(termNumCount);
		outputQuickTis.writeInt(useBits);

		for(int i=0;i<size;i++)
		{
			int tm=doc2Tm.get(i);
			if(tm==TERM_NULL_NUMBER||tm<0||tm>=termNumCount)
			{
//				if(debugindex++<200)
//				{
//				log.info("flushFieldDoc:"+i+","+tm+","+termNumCount+","+bits.exists(i));
//				}
				outputQuickTis.writeBits(termNumCount,useBits);				
			}else{
				outputQuickTis.writeBits(tm,useBits);
			}
		}
		outputQuickTis.flushBits();
		BlockBufferPool.INT_POOL.allset(this.doc2Tm, BlockBufferPool.INT_CREATE, TERM_NULL_NUMBER);

	}
	
	public static int useBits(int tm)
	{
		for(int i=1;i<32;i++)
		{
			if((tm >> i)==0)
			{
				return i;
			}
		}
		return 32;
	}
	

	public void flushPosTo(IndexOutput outputSize) throws IOException {
		outputSize.writeInt(fieldPosTis.size());
		for (Entry<Integer, Long> e : fieldPosTis.entrySet()) {
			outputSize.writeInt(e.getKey());
			outputSize.writeLong(e.getValue());
		}
		
		outputSize.writeInt(fieldPosTisTxt.size());
		for (Entry<Integer, Long> e : fieldPosTisTxt.entrySet()) {
			outputSize.writeInt(e.getKey());
			outputSize.writeLong(e.getValue());
		}
		
		outputSize.writeInt(fieldPosTisVal.size());
		for (Entry<Integer, Long> e : fieldPosTisVal.entrySet()) {
			outputSize.writeInt(e.getKey());
			outputSize.writeLong(e.getValue());
		}
	}
	

	protected void finalize() throws Throwable {
		super.finalize();
		this.free();
	}

	public void free() {
		BlockBufferPool.INT_POOL.recycleByteBlocks(this.doc2Tm);
	}
	@Override
	public void close() throws IOException {
        IOUtils.closeWhileHandlingException(outputQuickTis,outputQuickTisTxt,outputQuickTisVal);
	}
	
}
