package org.apache.solr.request.uninverted;

import java.io.IOException;
import java.util.HashSet;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.util.cache.Cache;
import org.apache.lucene.util.cache.SimpleLRUCache;
import org.apache.solr.request.BlockBufferPool;
import org.apache.solr.request.BlockBufferPool.BlockArray;
import org.apache.solr.request.uninverted.RamTermNumValue.TermDoubleValue;
import org.apache.solr.request.uninverted.UnInvertedFieldUtils.Doc2TmInterface;
import org.apache.solr.request.uninverted.UnInvertedFieldUtils.ByteDoc2Tm;
import org.apache.solr.request.uninverted.UnInvertedFieldUtils.IntDoc2Tm;
import org.apache.solr.request.uninverted.UnInvertedFieldUtils.ShortDoc2Tm;
import org.apache.solr.request.uninverted.UnInvertedFieldUtils.CompressType;
import org.apache.solr.schema.FieldType;




import com.alimama.mdrill.utils.UniqConfig;

public class RamDocValue {
//	public static Logger log = LoggerFactory.getLogger(RamDocValue.class);

	public RamTermNumValue tmValue=null;
	public UnInvertedFieldUtils.CompressType indexDatatype = UnInvertedFieldUtils.CompressType.d_int;
	
	public BlockArray<Integer> index = null;
	public BlockArray<Short> indexshort = null;
	public BlockArray<Byte> indexbyte = null;
	
	public RamDocValue(UnInvertedFieldUtils.FieldDatatype fieldDataType)
	{
		this.tmValue=new RamTermNumValue(fieldDataType);
	}

	public DocReaderNull getDocReaderNull()
	{
		DocReaderNull rnt= new DocReaderNull();
		rnt.setUni(this);
		return rnt;
	}
	public DocReaderSingle getDocReader()
	{
		DocReaderSingle rnt= new DocReaderSingle();
		rnt.setUni(this);
		return rnt;
	}
	
	public void startInitValue(int maxDoc, IndexReader reader,boolean isReadDouble) {
		int maxDocOffset = maxDoc + 2;
		this.index = BlockBufferPool.INT_POOL.calloc(maxDocOffset,BlockBufferPool.INT_CREATE, -1);
		this.tmValue.startInitValue(maxDoc, reader, isReadDouble);
	}

	public void endInitValue(boolean isReadDouble, int maxTermNum) {
		this.tmValue.endInitValue(isReadDouble, maxTermNum);
		this.compressDoc2Tm();
	}
	

	private void compressDoc2Tm() {
		int nullTerm=this.tmValue.nullTermNum;
	
		int checkValue=nullTerm+1;
		int size = this.index.getSize();
		
		if (checkValue< Byte.MAX_VALUE) {
			this.indexDatatype = UnInvertedFieldUtils.CompressType.d_byte;
			this.indexbyte = BlockBufferPool.BYTE_POOL.calloc(size,BlockBufferPool.BYTE_CREATE, (byte) nullTerm);
			this.indexbyte.fillByInt(this.index, -1);
			BlockBufferPool.INT_POOL.recycleByteBlocks(this.index);
			this.index = null;
		} else if (checkValue < Short.MAX_VALUE) {
			this.indexDatatype = UnInvertedFieldUtils.CompressType.d_short;
			this.indexshort = BlockBufferPool.SHORT_POOL.calloc(size,BlockBufferPool.SHORT_CREATE, (short) nullTerm);
			this.indexshort.fillByInt(this.index, -1);
			BlockBufferPool.INT_POOL.recycleByteBlocks(this.index);
			this.index = null;
		} else {
			this.indexDatatype = UnInvertedFieldUtils.CompressType.d_int;
			this.index.replace(-1, nullTerm);
		}
		
	}


	public void free() {
		if (this.index != null) {
			BlockBufferPool.INT_POOL.recycleByteBlocks(this.index);
			this.index=null;
		}

		if (this.indexshort != null) {
			BlockBufferPool.SHORT_POOL.recycleByteBlocks(this.indexshort);
			this.indexshort=null;
		}

		if (this.indexbyte != null) {
			BlockBufferPool.BYTE_POOL.recycleByteBlocks(this.indexbyte);
			this.indexbyte=null;
		}

		this.tmValue.free();
	}

	public Doc2TmInterface getDocTmReader() {
		if (this.indexDatatype.equals(CompressType.d_byte)) {
			return new ByteDoc2Tm(this.indexbyte);
		}

		if (this.indexDatatype.equals(CompressType.d_short)) {
			return new ShortDoc2Tm(this.indexshort);
		}

		return new IntDoc2Tm(this.index);
	}
	
	private TermDoubleValue getTermDoubleValue()
	{
		return this.tmValue.getTermDoubleValue();
	}


	public static class DocReaderSingle implements
			DocValueReadInterface {
		private RamDocValue uni;
		private TermDoubleValue termNumberValue;
		private Doc2TmInterface doc2tm = null;

		public void setUni(RamDocValue uni) {
			this.uni = uni;
			this.termNumberValue = uni.getTermDoubleValue();
			this.doc2tm = uni.getDocTmReader();
		}

		@Override
		public double quickToDouble(int doc, FieldType ft, TermNumEnumerator te)
				throws IOException {
			int tnum = this.doc2tm.get(doc);
			if (tnum >= this.uni.tmValue.nullTermNum) {
				return  RamTermNumValue.TERMNUM_NAN_VALUE;
			}
			return this.termNumberValue.tm(tnum, ft, te);
		}

		public Integer termNum(int doc, Integer def) throws IOException {
			int tnum = this.doc2tm.get(doc);
			if (tnum >= this.uni.tmValue.nullTermNum) {
				return def;
			}
			return tnum;
		}

		private Cache<Integer, String> termsCache = Cache.synchronizedCache(new SimpleLRUCache<Integer, String>(UniqConfig.getTermCacheSize()));

		private String getTermText(TermNumEnumerator te, int termNum)
				throws IOException {
			String termText = termsCache.get(termNum);
			if (termText != null) {
				return termText;
			}

			if (!te.skipTo(termNum)) {

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

		public String tNumToString(int tnum, FieldType ft,
				TermNumEnumerator te, String def) throws IOException {
			if (tnum >= this.uni.tmValue.nullTermNum) {
				return def;
			}
			String termText = this.getTermText(te, tnum);
			if (termText != null) {
				return ft.indexedToReadable(termText);
			}
			return def;
		}

		@Override
		public void setTermNum(int doc, int tm) throws IOException {
			this.doc2tm.set(doc, tm);
		}

	}

	public static interface DocValueReadInterface {
		public void setUni(RamDocValue uni);

		public double quickToDouble(int doc, FieldType ft, TermNumEnumerator te)
				throws IOException;

		public Integer termNum(int doc, Integer def) throws IOException;

		public void setTermNum(int doc, int tm) throws IOException;

		public String tNumToString(int tnum, FieldType ft,
				TermNumEnumerator te, String def) throws IOException;
	}

	public static class DocReaderNull implements DocValueReadInterface {
		HashSet<Integer> rtn = new HashSet<Integer>();

		public void setUni(RamDocValue uni) {
		}

		@Override
		public double quickToDouble(int doc, FieldType ft, TermNumEnumerator te)
				throws IOException {
			return RamTermNumValue.TERMNUM_NAN_VALUE;
		}

		public Integer termNum(int doc, Integer def) throws IOException {
			return def;
		}

		@Override
		public String tNumToString(int tnum, FieldType ft,
				TermNumEnumerator te, String def) throws IOException {
			return def;
		}

		@Override
		public void setTermNum(int doc, int tm) throws IOException {

		}
	}
	
	public int getmemsize() {
		int sz = 0;
		if (index != null)
			sz += index.getMemSize() * 4;
		if (indexshort != null)
			sz += indexshort.getMemSize() * 2;
		if (indexbyte != null)
			sz += indexbyte.getMemSize() * 1;
		
		sz += this.tmValue.getmemsize();
		return sz;
	}
	
	public String toString() {
		StringBuffer membuffer = new StringBuffer();
		if (index != null) {
			membuffer.append(",").append(
					"index=" + index.getMemSize() + "@" + index.getSize());
		}
		if (indexshort != null) {
			membuffer.append(",").append(
					"indexshort=" + indexshort.getMemSize() + "@"
							+ indexshort.getSize());
		}

		if (indexbyte != null) {
			membuffer.append(",").append(
					"indexbyte=" + indexbyte.getMemSize() + "@"
							+ indexbyte.getSize());
		}
		membuffer.append(",").append(this.tmValue.toString());


		return membuffer.toString();
	}

}
