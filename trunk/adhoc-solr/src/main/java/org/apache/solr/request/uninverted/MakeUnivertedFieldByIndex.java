package org.apache.solr.request.uninverted;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.PriorityQueue;
import java.util.zip.CRC32;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.solr.request.mdrill.MdrillUtils;
import org.apache.solr.request.uninverted.UnInvertedFieldUtils.FieldDatatype;
import org.apache.solr.request.uninverted.UnInvertedFieldUtils.MixTermInfo;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.search.BitDocSet;
import org.apache.solr.search.DocIterator;
import org.apache.solr.search.DocSet;
import org.apache.solr.search.DocSetCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 历史的旧方法，通过遍历倒排表来实现，效率不高
 * @author yannian.mu
 *
 */
public class MakeUnivertedFieldByIndex {
	public static Logger log = LoggerFactory.getLogger(MakeUnivertedFieldByIndex.class);

	private static int SKIP_STEP = 32;
	private static int SKIP_MIN = 64;
	private static int MAX_SKIP_COUNT = 102400;
	
	private static long TD_MAX_CMP_COUNT = 64000000l;
	private static int TD_LIMIT_MAX = 10240;
	private static int TD_LIMIT_MIN = 8;
	
	private int[] docs = new int[1000];
	private int[] freqs = new int[1000];
	
	private UnInvertedField uni;

	public MakeUnivertedFieldByIndex(UnInvertedField uni) throws IOException {
		this.uni=uni;
	}
	
	public void makeInit(BitDocSet baseAdvanceDocs,String field, IndexSchema schema,IndexReader reader) throws IOException
	{
		uni.init(field, reader, schema);
		uni.baseAdvanceDocs=UnInvertedField.ajustBase(48,baseAdvanceDocs, reader);

		if(this.uni.checkEmpty())
		{
			return ;
		}
		
		log.info(" makeInit  begin " + this.uni.field + " field " +",baseAdvanceDocs="+(this.uni.baseAdvanceDocs==null?"null":this.uni.baseAdvanceDocs.size())+"@"+(baseAdvanceDocs==null?"null":baseAdvanceDocs.size()));
			
		TermNumEnumerator te = uni.ti.getEnumerator(reader);

		int maxDoc = reader.maxDoc();

		this.uni.startRamDocValue(maxDoc, reader, true);
		
		int limitsize=this.getLimitSize();

		PriorityQueue<MixTermInfo> termDocslist=new PriorityQueue<MixTermInfo>(limitsize,Collections.reverseOrder(UnInvertedFieldUtils.TD_CMP));

		TermDocs tdreader=reader.termDocs(1024);
		int maxTermNum=0;
		for (;;) {
			Term t = te.term();
			if (t == null) {
				break;
			}

			int termNum = te.getTermNumber();
			
			if(termNum%100000==0)
			{
				log.info("termsInverted " +termNum+"@"+ this.uni.field + ",limitsize=" + limitsize);
			}
			
			int df=te.docFreq();
			
			if(!this.isFinish())
			{
				TermDocs td = te.getTermDocs();
				if(df<=SKIP_MIN||this.uni.baseAdvanceDocs==null)
				{
					td.seek(te);
					this.set_Doc2TermNum_NonSkip(td, docs, freqs, termNum,true,maxDoc);
				} else {

					MixTermInfo cl = new MixTermInfo(df, termNum, tdreader,	new Term(t.field(), t.text()));
					if (termDocslist.size() < limitsize) {
						termDocslist.add(cl);
					} else {
						MixTermInfo peek = termDocslist.peek();
						if (UnInvertedFieldUtils.TD_CMP.compare(peek, cl) > 0 && cl.getCount() / (peek.getCount() + 1) > 1.5) {
							termDocslist.add(cl);
							MixTermInfo cl_old = termDocslist.poll();
							this.set_Doc2TermNum_NonSkip(cl_old.getTd(), docs,freqs, cl_old.getTermNum(), true, maxDoc);
						} else {
							td.seek(te);
							this.set_Doc2TermNum_NonSkip(td, docs, freqs,termNum, true, maxDoc);
						}
					}
				}
			}
			
			maxTermNum=Math.max(maxTermNum, termNum);

			this.setTermNumValue(t, termNum);
			te.next();
		}
		
		this.PriorityQueue_skip_set(termDocslist, maxDoc,true);


		this.uni.endRamDocValue(true,maxTermNum);

		tdreader.close();
		te.close();

		this.uni.tnr = this.uni.ramDocValue.getDocReader();
	
		this.setTdIndex_NULL();
	}
	
	
	public void setTdIndex_NULL() throws IOException {
		if (this.uni.baseAdvanceDocs == null) {
			return;
		}

		log.info("setTdIndex_NULL :"+this.uni.baseAdvanceDocs.size());
		DocIterator iter = this.uni.baseAdvanceDocs.iterator();
		while (iter.hasNext()) {
			int doc = iter.nextDoc();
			this.uni.bits.add(doc);
			this.uni.markDocTm(doc, this.uni.getNullTm(),false);
		}
		
		this.uni.baseAdvanceDocs=null;
	}
	
	
	public void addDoclist(BitDocSet baseAdvanceDocs,String field, 
			IndexReader reader) throws IOException {
		
		if (uni.checkEmpty()) {
			return ;
		}

		BitDocSet tmp=null;
		if(baseAdvanceDocs!=null)
		{
			tmp=(BitDocSet) baseAdvanceDocs.andNot(this.uni.bits);

			if(tmp!=null&&tmp.size()<=0)
			{
				return ;
			}
		}
	
		this.uni.baseAdvanceDocs=UnInvertedField.ajustBase(48,tmp, reader);
		TermNumEnumerator te = uni.ti.getEnumerator(reader);

		log.info("addDoclist start " + this.uni.field +",baseAdvanceDocs="+(this.uni.baseAdvanceDocs==null?"null":this.uni.baseAdvanceDocs.size())+"@"+(baseAdvanceDocs==null?"null":baseAdvanceDocs.size()));

		int maxDoc=reader.maxDoc();
		int limitsize=this.getLimitSize();

		PriorityQueue<MixTermInfo> termDocslist=new PriorityQueue<MixTermInfo>(limitsize,Collections.reverseOrder(UnInvertedFieldUtils.TD_CMP));
		TermDocs tdreader=reader.termDocs(1024);

		for (;;) {
			Term t = te.term();
			if (t == null) {
				break;
			}

			if (this.isFinish()) {
				break;
			}

			int termNum = te.getTermNumber();

			if (termNum%10000==0) {
				log.info("termsInverted " +termNum+"@"+ this.uni.field + ",limitsize=" + limitsize);
			}
			TermDocs td = te.getTermDocs();

			int df = te.docFreq();
			if (df <= SKIP_MIN || this.uni.baseAdvanceDocs == null) {
				td.seek(te);
				this.set_Doc2TermNum_NonSkip(td, docs, freqs, termNum, false, maxDoc);
			} else {
				MixTermInfo cl = new MixTermInfo(df, termNum, tdreader,	new Term(t.field(), t.text()));
				if (termDocslist.size() < limitsize) {
					termDocslist.add(cl);
				} else {
					MixTermInfo peek = termDocslist.peek();
					if (UnInvertedFieldUtils.TD_CMP.compare(peek, cl) > 0 && cl.getCount() / (peek.getCount() + 1) > 1.5) {
						termDocslist.add(cl);
						MixTermInfo cl_old = termDocslist.poll();
						this.set_Doc2TermNum_NonSkip(cl_old.getTd(), docs,	freqs, cl_old.getTermNum(), false, maxDoc);
					} else {
						td.seek(te);
						this.set_Doc2TermNum_NonSkip(td, docs, freqs, termNum, false, maxDoc);
					}
				}
			}
			te.next();
		}
		
		this.PriorityQueue_skip_set(termDocslist, maxDoc,false);
		
		te.close();
		this.setTdIndex_NULL();
	}
	
	private boolean isFinish()
	{
		if(this.uni.baseAdvanceDocs==null)
		{
			return false;
		}
		
		return this.uni.baseAdvanceDocs.size()<=0;
	}
	
	private void cleanBase(DocSetCollector collect)
	{
		DocSet docsit=collect.getDocSet();
		DocIterator toremove = docsit.iterator();
		while (toremove.hasNext()) {
			int doc = toremove.nextDoc();
			this.uni.baseAdvanceDocs.clear(doc);
		}
		
		

	}

	private int getLimitSize()
	{
		int limitsize=TD_LIMIT_MAX;
		if(this.uni.baseAdvanceDocs!=null)
		{
			limitsize=(int) Math.min(limitsize, TD_MAX_CMP_COUNT/(1+this.uni.baseAdvanceDocs.size()));
		}
		limitsize=Math.max(limitsize, TD_LIMIT_MIN);
		return limitsize;
	}
	
	private void set_Doc2TermNum_NonSkip(TermDocs td,int[] docs,int[] freqs,int termNum,boolean isinit,int maxDoc) throws IOException
	{
		if(this.uni.baseAdvanceDocs==null)
		{
			for (;;) {
				int n = td.read(docs, freqs);
				if (n <= 0) {
					break;
				}
				for (int i = 0; i < n; i++) {
					int docid=docs[i];
					this.uni.bits.add(docid);
					this.uni.markDocTm(docid, termNum, isinit);
				}
			}
			return ;
		}
		
		DocSetCollector collect=new DocSetCollector(10240, maxDoc);
		for (;;) {
			int n = td.read(docs, freqs);
			if (n <= 0) {
				break;
			}
			for (int i = 0; i < n; i++) {
				int docid=docs[i];
				collect.collect(docid);
				this.uni.bits.add(docid);
				this.uni.markDocTm(docid, termNum, isinit);
			}
		}

		this.cleanBase(collect);

	}

	private int set_Doc2TermNum_Skip(TermDocs td,int[] docs,int[] freqs,int termNum,boolean isinit,int maxDoc) throws IOException
	{
		int skipcount=0;
			
		DocSetCollector collect=new DocSetCollector(10240, maxDoc);
		DocIterator iter = this.uni.baseAdvanceDocs.iterator();
		int doc=-1;
		int baseDoc=-1;
		
		while (iter.hasNext()) {
			doc = iter.nextDoc();
			if(doc<baseDoc)
			{
				continue;
			}
			
			
			if(doc>baseDoc)
			{
				int diff=doc-baseDoc;
				if(diff>=SKIP_STEP)
				{
					if(baseDoc>=0)
					{
						skipcount++;
					}
					baseDoc=UnInvertedFieldUtils.advance(td, doc);
					if(UnInvertedFieldUtils.NO_MORE_DOCS==baseDoc)
					{
						break;
					}
				}else{
					boolean is_no_more_docs=false;
					for(int i=0;i<=SKIP_STEP;i++)
					{
						if(td.next())
						{
							baseDoc=td.doc();
							if(baseDoc>=doc)
							{
								break;
							}
							collect.collect(baseDoc);
							this.uni.bits.add(baseDoc);
							this.uni.markDocTm(baseDoc, termNum, isinit);
							
						}else{
							is_no_more_docs=true;
							break;
						}
					}
					if(is_no_more_docs)
					{
						break;
					}
				}
			}
			
			if(baseDoc==doc)
			{
				this.uni.bits.add(doc);
				collect.collect(doc);
				this.uni.markDocTm(doc, termNum, isinit);
			}
		}
		
		
		this.cleanBase(collect);
		
		return skipcount;
	}
	
	private int PriorityQueue_skip_set(PriorityQueue<MixTermInfo> termDocslist,int maxDoc,boolean isinit) throws IOException
	{
		int skipcount=0;
		ArrayList<MixTermInfo> sorted=new ArrayList<MixTermInfo>(termDocslist.size());
		sorted.addAll(termDocslist);
		Collections.sort(sorted, UnInvertedFieldUtils.TD_CMP_TM);
		for(MixTermInfo cl:sorted)
		{
			if(this.isFinish())
			{
				break;
			}

			if(skipcount>MAX_SKIP_COUNT)
			{
				this.set_Doc2TermNum_NonSkip(cl.getTd(), docs, freqs,cl.getTermNum(),isinit,maxDoc);
			}else{
				skipcount+=this.set_Doc2TermNum_Skip(cl.getTd(), docs, freqs, cl.getTermNum(),isinit,maxDoc);
			}
		
		}
		
		return skipcount;
	}
	
	private void setTermNumValue(Term t,int termNum)
	{
		if (this.uni.fieldDataType == FieldDatatype.d_long) {
			this.uni.setTmValueLong(termNum,Long.parseLong(this.uni.ft.indexedToReadable(t.text()))) ;
		} else if (this.uni.fieldDataType == FieldDatatype.d_double) {
			this.uni.setTmValueDouble(termNum, MdrillUtils.ParseDouble(this.uni.ft.indexedToReadable(t.text())));
		} else if (this.uni.fieldDataType == FieldDatatype.d_string) {// for dist
			CRC32 crc32 = new CRC32();
			crc32.update(new String(this.uni.ft.indexedToReadable(t.text())).getBytes());
			this.uni.setTmValueLong(termNum, crc32.getValue());
		}
	}

}
