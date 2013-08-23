package org.apache.solr.request.join;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.apache.lucene.index.SegmentReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.solr.request.join.HigoJoin.*;
import org.apache.solr.request.uninverted.NumberedTermEnum;
import org.apache.solr.request.uninverted.TermIndex;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.TrieField;
import org.apache.solr.search.BitDocSet;
import org.apache.solr.search.DocIterator;
import org.apache.solr.search.DocSet;
import org.apache.solr.search.SolrIndexSearcher;


public class HigoJoinMultyValues implements HigoJoinInterface{
	private static Logger LOG = Logger.getLogger(HigoJoinMultyValues.class);
	private SolrIndexSearcher readerleft;
	private SolrIndexSearcher readerright;
	private String fieldLeft;
	private String fieldRigth;
	public HigoJoinMultyValues(SolrIndexSearcher readerleft,
			SolrIndexSearcher readerright, String fieldLeft, String fieldRigth) throws IOException {
		this.readerleft = readerleft;
		this.readerright = readerright;
		this.fieldLeft = fieldLeft;
		this.fieldRigth = fieldRigth;
		this.makejoin();
	}
	

	public HigoJoinMultyValues(SegmentReader reader,String partion,IndexSchema schema,
			SolrIndexSearcher readerright, String fieldLeft, String fieldRigth) throws IOException {
		this.readerleft = null;
		this.partion=partion;
		this.schema=schema;
		this.leftreader=reader;
		this.readerright = readerright;
		this.fieldLeft = fieldLeft;
		this.fieldRigth = fieldRigth;
		this.makejoin();
	}
	

SegmentReader leftreader=null;
String partion;
IndexSchema schema;
	
	long memsize = -1;

	@Override
	public synchronized long memSize() {
		if (memsize >= 0) {
			return memsize;
		}

		memsize = 64;
		for (Entry<Integer, JoinPariArr> e : join.entrySet()) {
			memsize += 16;
			memsize += e.getValue().memsize();

		}

		for (Entry<JoinTermNum, IntArr> e : joinRevert.entrySet()) {
			memsize += e.getKey().memsize();
			memsize += e.getValue().memsize();

		}

		return memsize;
	}

		@Override
		public void LRUclean() {
			
		}
	  

	 //right,left
	private HashMap<Integer,JoinPariArr> join=new HashMap<Integer,JoinPariArr>();
	private HashMap<Integer,ArrayList<JoinPair>> join_tmp=new HashMap<Integer,ArrayList<JoinPair>>();
	//left,right
	private HashMap<JoinTermNum,IntArr> joinRevert=new HashMap<JoinTermNum,IntArr>();
	
	public DocSet filterByRight(DocSet leftDocs,DocSet rightDocs)
	{
		BitDocSet docset=new BitDocSet();
		DocIterator iter = rightDocs.iterator();
		while (iter.hasNext()) {
			int doc = iter.nextDoc();
			JoinPariArr list=join.get(doc);
			if(list==null)
			{
				continue;
			}
			for(JoinPair jp:list.list)
			{
				for (int i:jp.left) {
					docset.add(i);
				}
			}
		}
		
		return leftDocs.intersection(docset);
	}
	
	public IntArr getRight(int leftDocid,int termNum)
	{
		return joinRevert.get(new JoinTermNum(leftDocid, termNum));
	}

	
	private void makejoin() throws IOException
	{
		IndexSchema schema=null;
		if(this.leftreader!=null)
		{
			schema=this.schema;
		}else{
			schema=readerleft.getSchema();
		}
		
		FieldType ftleft=schema.getFieldType(fieldLeft);
		
		String prefixLeft=TrieField.getMainValuePrefix(ftleft);
		TermIndex tiLeft = new TermIndex(fieldLeft, prefixLeft);
		tiLeft.setCacheDir(null, String.valueOf(System.currentTimeMillis()));
		NumberedTermEnum teLeft =null;
		if(this.leftreader!=null)
		{
			teLeft=tiLeft.getEnumerator(this.leftreader);
		}else{
			teLeft=tiLeft.getEnumerator(readerleft.getReader());
		}	
		FieldType ftright =readerright.getSchema().getFieldType(fieldRigth);
		String prefixRight=TrieField.getMainValuePrefix(ftright);
		TermIndex tiRight = new TermIndex(fieldRigth, prefixRight);
		tiRight.setCacheDir(null,String.valueOf( System.currentTimeMillis()));
		NumberedTermEnum teRight = tiRight.getEnumerator(readerright.getReader());
		
		int[] docs = new int[1000];
		int[] freqs = new int[1000];
		
		
		int debugline=0;
		
		for (;;) {
			Term tleft= teLeft.term();
			Term tRight=teRight.term();
			
			
			if (tleft == null||tRight==null) {
				LOG.info("###termbreak###"+String.valueOf(tleft)+">>>>"+String.valueOf(tRight)+","+fieldLeft+","+fieldRigth);

				break;
			}
			
			String tvleft=ftleft.indexedToReadable(tleft.text());
			String tvRight=ftright.indexedToReadable(tRight.text());


			if(tvleft.equals(tvRight))
			{
				if(debugline++<10)
				{
					LOG.info("###termok###"+String.valueOf(tvleft)+">>>>"+String.valueOf(tvRight)+","+fieldLeft+","+fieldRigth);

				}

				JoinPair jp=new JoinPair();
				jp.termNum = teLeft.getTermNumber();

				ArrayList<Integer> jpleft=new ArrayList<Integer>();
				TermDocs tdleft = teLeft.getTermDocs();
				tdleft.seek(teLeft);
				for (;;) {
					int n = tdleft.read(docs, freqs);
					if (n <= 0) {
						break;
					}
					for (int i = 0; i < n; i++) {
						jpleft.add(docs[i]);
					}
				}
				
				jp.left=new int[jpleft.size()];
				int index=0;
				for(Integer d:jpleft)
				{
					jp.left[index]=d;
					index++;
				}
				
				
				ArrayList<Integer> RightList=new ArrayList<Integer>();
				TermDocs tdRight = teRight.getTermDocs();
				tdRight.seek(teRight);
				boolean isset=false;

				for (;;) {
					int n = tdRight.read(docs, freqs);
					if (n <= 0) {
						break;
					}
					for (int i = 0; i < n; i++) {
						if(isset)
						{
							continue;
						}
						int docid=docs[i];
						RightList.add(docid);
						ArrayList<JoinPair> list=join_tmp.get(docid);
						if(list==null)
						{
							list=new ArrayList<JoinPair>();
							join_tmp.put(docid, list);
						}
						list.add(jp);
						isset=true;

					}
				}
				
				for(Integer leftid:jp.left)
				{
					JoinTermNum tm=new JoinTermNum(leftid, jp.termNum);
					joinRevert.put(tm, IntArr.parse(RightList));
				}
				
				teLeft.next();
				teRight.next();
			}else if(tvleft.compareTo(tvRight)>0)
			{
				teRight.next();
			}else{
				teLeft.next();
			}
		}
		
		teLeft.close();
		teRight.close();
		
		for(Entry<Integer,ArrayList<JoinPair>> e:this.join_tmp.entrySet())
		{
			this.join.put(e.getKey(), JoinPariArr.parse(e.getValue()));
		}
		this.join_tmp.clear();
		
		LOG.info("###join###"+join.size()+","+joinRevert.size());
	}
	
	



	

	
}
