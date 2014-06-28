package org.apache.solr.request.uninverted;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.SegmentReader;
import org.apache.lucene.util.OpenBitSet;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.handler.component.StatsValues;
import org.apache.solr.request.BlockBufferPool.BlockArray;
import org.apache.solr.request.uninverted.RamDocValue.DocValueReadInterface;
import org.apache.solr.request.uninverted.UnInvertedFieldUtils.FieldDatatype;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.schema.TrieField;
import org.apache.solr.search.BitDocSet;
import org.apache.solr.search.DocSet;
import org.apache.solr.search.SolrIndexSearcher;


public class UnInvertedFieldBase implements GrobalCache.ILruMemSizeCache {
//	public static Logger log = LoggerFactory.getLogger(UnInvertedFieldBase.class);

	public AtomicInteger refCnt = new AtomicInteger(0);
	public UnInvertedFieldUtils.FieldDatatype fieldDataType = UnInvertedFieldUtils.FieldDatatype.d_default;
	public String field;
	public FieldType ft;
	public TermIndex ti;
	public RamDocValue ramDocValue =null;

	//mark history
	public BitDocSet bits;
	
	//read current
	public BitDocSet baseAdvanceDocs = null;

	private boolean isShutDown = false;
	public boolean isShutDown() {
		return isShutDown;
	}


	

	public boolean isMultiValued = false;
	public boolean isNullField = false;
	public DocValueReadInterface tnr;
	
	
	public void init(String field, IndexReader reader,IndexSchema schema) throws IOException
	{
		this.bits = new BitDocSet(new OpenBitSet(reader.maxDoc()));
		this.field = field;
		
		FieldType schemaft=schema.getFieldType(field);
		String prefix=TrieField.getMainValuePrefix(schemaft);
		SchemaField sf = schema.getField(field);
		this.ft = sf.getType();
		this.isMultiValued = this.ft.isMultiValued();	
		this.fieldDataType = UnInvertedFieldUtils.getDataType(this.ft);
		this.ramDocValue=new RamDocValue(this.fieldDataType);
		this.ti = new TermIndex(field, prefix);
	}
	

	public void setTmValueDouble(int tm, double val) {
//		log.info("setTmValueDouble "+tm+","+val);

		this.ramDocValue.tmValue.termValueDouble.set(tm, val);
	}
	
	public void setTmValueLong(int tm, long val) {
//		log.info("setTmValueLong "+tm+","+val);

		this.ramDocValue.tmValue.termValueLong.set(tm, val);
	}

	public BlockArray<Double> getTmValueDouble() {
		return this.ramDocValue.tmValue.termValueDouble;
	}
	
	public BlockArray<Long> getTmValueLong() {
		return this.ramDocValue.tmValue.termValueLong;
	}

	public void markDocTm(int doc, int termNum, boolean isinit)
			throws IOException {

		if (isinit) {
			this.ramDocValue.index.set(doc, termNum);
		} else {
			this.tnr.setTermNum(doc, termNum);
		}
	}
	
	

	public void startRamDocValue(int maxDoc, IndexReader reader,
			boolean isReadDouble) {
		ramDocValue.startInitValue(maxDoc, reader, isReadDouble);
	}

	public void endRamDocValue(boolean isReadDouble, int maxTermNum) {
		this.ramDocValue.endInitValue(isReadDouble, maxTermNum);
	}

	public boolean checkEmpty() throws IOException {
		if (this.field.indexOf("higoempty_") >= 0) {
			this.isNullField = true;
		}
		
		if (this.isNullField) {
			this.tnr = this.ramDocValue.getDocReaderNull();
			return true;
		}
		if(this.isMultiValued)
		{
			throw new IOException("unsupport MultiValued");
		}
		return false;
	}

	public TermNumEnumerator getTi(SolrIndexSearcher searcher)
			throws IOException {
		return ti.getEnumerator(searcher.getReader());
	}

	public TermNumEnumerator getTi(SegmentReader reader) throws IOException {
		return ti.getEnumerator(reader);
	}

	public int getNullTm() {
		return this.ramDocValue.tmValue.nullTermNum;
	}
	
	public Integer termNum(int doc) throws IOException {
		return this.tnr.termNum(doc, this.getNullTm());
	}



	// group by
	public String tNumToString(int tnum, FieldType ft, TermNumEnumerator te,
			String def) throws IOException {
		String rtn= this.tnr.tNumToString(tnum, ft, te, def);
		return rtn;
	}

	// sum,dist
	public double quickToDouble(int doc, FieldType ft, TermNumEnumerator te)
			throws IOException {
		try{
			return this.tnr.quickToDouble(doc, ft, te);
		}catch(Throwable e)
		{
			return RamTermNumValue.TERMNUM_NAN_VALUE;
		}
	}

	public int getTermNum(TermNumEnumerator te, String text, FieldType ft)
			throws IOException {

		//TODO  ----这个地方显得多余，之后考虑注释掉 在试试 ----
		if ((text.startsWith("_") || text.startsWith("null"))&& !this.fieldDataType.equals(FieldDatatype.d_string)) {
			return this.getNullTm();
		}
		if (te.skipTo(ft.toInternal(text))) {
			return te.getTermNumber();
		} else {
			return this.getNullTm();
		}
	}

	@Override
	public String toString() {
		return "{field=" + field + ",memSize="	+ (memSize() * 1.0 / 1024 / 1024) + "mb" + ",nullTermNum="	+ this.getNullTm() 	+ this.ramDocValue.toString() +",bits.size="+this.bits.size()+ "}";
	}

	private void freeMem() {
		if (isShutDown) {
			return;
		}
		isShutDown = true;
		this.ramDocValue.free();
	}

	public void LRUclean() {
		if (this.refCnt.get() == 0) {
			this.freeMem();
		}
	}

	protected void finalize() throws Throwable {
		super.finalize();
		this.freeMem();
	}

	long sz = -1;

	public synchronized long memSize() {
		if (sz > 0) {
			return sz;
		}

		sz = 8 * 8 + 32; // local fields
		if (bits != null)
			sz += bits.memSize();
		if (baseAdvanceDocs != null)
			sz += baseAdvanceDocs.memSize();
		sz += ramDocValue.getmemsize();
		sz += ti.memSize();
		return sz;
	}

	/**
	 * 旧的solr接口，已经废弃
	 */
	public NamedList getCounts(SolrIndexSearcher searcher, DocSet baseDocs,
			int offset, int limit, Integer mincount, boolean missing,
			String sort, String prefix, Boolean returnPair, boolean isRow)
			throws IOException {
		return new NamedList();
	}

	/**
	 * 旧的solr接口，已经废弃
	 */
	public StatsValues getStats(SolrIndexSearcher searcher, DocSet baseDocs,
			String[] facet) throws IOException {
		return null;
	}

}
