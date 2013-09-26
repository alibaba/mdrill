package org.apache.solr.request.join;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.apache.lucene.index.SegmentReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.solr.request.join.HigoJoin.IntArr;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.TrieField;
import org.apache.solr.search.BitDocSet;
import org.apache.solr.search.DocIterator;
import org.apache.solr.search.DocSet;
import org.apache.solr.search.SolrIndexSearcher;


public class HigoJoinSingleValues implements HigoJoinInterface{
	private static Logger LOG = Logger.getLogger(HigoJoinSingleValues.class);
	private SolrIndexSearcher readerleft;
	private SolrIndexSearcher readerright;
	private String fieldLeft;
	private String fieldRigth;
	public HigoJoinSingleValues(SolrIndexSearcher readerleft,
			SolrIndexSearcher readerright, String fieldLeft, String fieldRigth) throws IOException {
		this.readerleft = readerleft;
		this.readerright = readerright;
		this.fieldLeft = fieldLeft;
		this.fieldRigth = fieldRigth;
		this.makejoin();
	}
	
	public HigoJoinSingleValues(SegmentReader reader,String partion,IndexSchema schema,
			SolrIndexSearcher readerright, String fieldLeft, String fieldRigth) throws IOException {
		this.readerleft = null;
		this.schema=schema;
		this.leftreader=reader;
		this.readerright = readerright;
		this.fieldLeft = fieldLeft;
		this.fieldRigth = fieldRigth;
		this.makejoin();
	}
	

	private SegmentReader leftreader=null;
	private IndexSchema schema;
	
	long memsize = -1;

	@Override
	public synchronized long memSize() {
		if (memsize >= 0) {
			return memsize;
		}

		memsize = 256;
		memsize+=join.length*4;
		memsize+=joinRevert.length*8;
		
		for(int[] list:this.join){
			if(list!=null)
			{
				memsize+=list.length*8;
			}
		}
		return memsize;
	}

		@Override
		public void LRUclean() {
			
		}
	  
		
	int[][] join=null;
	private int[] joinRevert=null;
	public DocSet filterByRight(DocSet leftDocs,DocSet rightDocs)
	{
		BitDocSet docset=new BitDocSet();
		DocIterator iter = rightDocs.iterator();
		while (iter.hasNext()) {
			int doc = iter.nextDoc();
			int[] list=join[doc];
			if(list==null)
			{
				continue;
			}
			for(int jp:list)
			{
				docset.add(jp);
			}
		}
		
		return leftDocs.intersection(docset);
	}
	
	
	public Integer getRight(int leftDocid,int termNum)
	{
		int rtn=joinRevert[leftDocid];
		if(rtn<0)
		{
			return null;
		}
		return rtn;
	}

	
	
	private IntArr getListArr(TermEnum teLeft,TermDocs tdleft,int[] docs,int[] freqs,int limit) throws IOException
	{

		IntArr LeftArr=new IntArr();

		ArrayList<Integer> jpleft=new ArrayList<Integer>();
		tdleft.seek(teLeft);
		int index=0;
		for (;;) {
			int n = tdleft.read(docs, freqs);
			if (n <= 0) {
				break;
			}
			for (int i = 0; i < n; i++) {
				if(index<limit)
				{
					index++;
					jpleft.add(docs[i]);
				}else{
					break;
				}
				
			}
		}
		
		LeftArr.list=new int[jpleft.size()];
		index=0;
		for(Integer d:jpleft)
		{
			LeftArr.list[index]=d;
			index++;
		}
		
		return LeftArr;
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
		Term tiLeft=new Term(fieldLeft, prefixLeft==null?"":prefixLeft);
		TermEnum teLeft = null;
		TermDocs tdleft=null;
		if(this.leftreader!=null)
		{
			this.joinRevert=new int[this.leftreader.maxDoc()+1];
			tdleft=this.leftreader.termDocs();
			teLeft=this.leftreader.terms(tiLeft);
		}else{
			this.joinRevert=new int[readerleft.getReader().maxDoc()+1];
			teLeft=readerleft.getReader().terms(tiLeft);
			tdleft=readerleft.getReader().termDocs();
		}
		
		for(int i=0;i<this.joinRevert.length;i++)
		{
			this.joinRevert[i]=-1;
		}
		

		
		FieldType ftright =readerright.getSchema().getFieldType(fieldRigth);
		String prefixRight=TrieField.getMainValuePrefix(ftright);
		Term tiRight=new Term(fieldRigth, prefixRight==null?"":prefixRight);
		
		TermEnum teRight = readerright.getReader().terms(tiRight.createTerm(prefixRight==null?"":prefixRight));
		TermDocs tdRight=readerright.getReader().termDocs();
		this.join=new int[readerright.getReader().maxDoc()+1][];
		for(int i=0;i<this.join.length;i++)
		{
			this.join[i]=null;
		}
		
		int[] docs = new int[1000];
		int[] freqs = new int[1000];
		
		
		int debugline=0;
		
		HashMap<Integer,HashSet<Integer>> join_tmp=new HashMap<Integer,HashSet<Integer>>();
//		HashMap<Integer,Integer> joinRevert_tmp=new HashMap<Integer,Integer>();
		for (;;) {
			Term tleft= teLeft.term();
			Term tRight=teRight.term();
			
			
			if (tleft == null||tRight==null) {
				LOG.info("###termbreak###"+String.valueOf(tleft)+">>>>"+String.valueOf(tRight)+","+fieldLeft+","+fieldRigth);
				break;
			}
			if((!tleft.field().equals(fieldLeft))||(!tRight.field().equals(fieldRigth)))
			{
				LOG.info("###termbreak fieldchange###"+String.valueOf(tleft)+">>>>"+String.valueOf(tRight)+","+fieldLeft+","+fieldRigth);
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

				if(tvleft!=null&&!tvleft.trim().isEmpty())
				{
					IntArr LeftArr=this.getListArr(teLeft, tdleft, docs, freqs,Integer.MAX_VALUE);
					IntArr RightArr=this.getListArr(teRight, tdRight, docs, freqs,1);
	
					for (Integer docid:RightArr.list) {
						HashSet<Integer> list=join_tmp.get(docid);
						if(list==null)
						{
							list=new HashSet<Integer>();
							join_tmp.put(docid, list);
						}
						for(int jid:LeftArr.list)
						{
	//						joinRevert_tmp.put(jid, docid);
							this.joinRevert[jid]=docid;
							list.add(jid);
						}
					}
				}else{
					LOG.info("###empty###"+String.valueOf(tvleft)+">>>>"+String.valueOf(tvRight)+","+fieldLeft+","+fieldRigth);
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
				
		for(Entry<Integer, HashSet<Integer>> e:join_tmp.entrySet())
		{
			this.join[e.getKey()]=IntArr.parse(e.getValue()).list;
		}
		join_tmp=null;
		
		LOG.info("###join###"+join.length+","+joinRevert.length);
	}
		
}
