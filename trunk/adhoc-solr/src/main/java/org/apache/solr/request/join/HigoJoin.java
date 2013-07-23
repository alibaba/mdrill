package org.apache.solr.request.join;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.util.cache.Cache;
import org.apache.lucene.util.cache.SimpleLRUCache;
import org.apache.solr.request.uninverted.NumberedTermEnum;
import org.apache.solr.request.uninverted.TermIndex;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.TrieField;
import org.apache.solr.search.BitDocSet;
import org.apache.solr.search.DocIterator;
import org.apache.solr.search.DocSet;
import org.apache.solr.search.SolrIndexSearcher;

import com.alimama.mdrill.buffer.LuceneUtils;
import com.alimama.mdrill.utils.UniqConfig;

public class HigoJoin {
	private static Logger LOG = Logger.getLogger(HigoJoin.class);
	private SolrIndexSearcher readerleft;
	private SolrIndexSearcher readerright;
	private String fieldLeft;
	private String fieldRigth;
	public HigoJoin(SolrIndexSearcher readerleft,
			SolrIndexSearcher readerright, String fieldLeft, String fieldRigth) throws IOException {
		this.readerleft = readerleft;
		this.readerright = readerright;
		this.fieldLeft = fieldLeft;
		this.fieldRigth = fieldRigth;
		this.makejoin();
	}
	
	 public static HigoJoin getJoin(SolrIndexSearcher readerleft,
				SolrIndexSearcher readerright, String fieldLeft, String fieldRigth) throws IOException {
		
			Cache<String,HigoJoin> cache = HigoJoin.fieldValueCache;
			StringBuffer key=new StringBuffer();
			key.append(readerleft.getPartionKey());
			key.append("@");
			key.append(fieldLeft);
			key.append("@");
			key.append(LuceneUtils.crcKey(readerleft.getReader()));
			key.append("@");
			key.append(readerright.getPartionKey());
			key.append("@");
			key.append(fieldRigth);
			key.append("@");
			key.append(LuceneUtils.crcKey(readerright.getReader()));
			String cachekey=key.toString();
			HigoJoin uif = cache.get(cachekey);
			if (uif == null) {
				synchronized (cache) {
					uif =  cache.get(cachekey);
					if (uif == null) {
						uif = new HigoJoin(readerleft, readerright,fieldLeft,fieldRigth);
						cache.put(cachekey, uif);
					}
				}
			}
			return uif;
		}
	  
	 private static Cache<String,HigoJoin> fieldValueCache=Cache.synchronizedCache(new SimpleLRUCache<String, HigoJoin>(UniqConfig.getJoinSize()));

	 //right,left
	private HashMap<Integer,ArrayList<JoinPair>> join=new HashMap<Integer,ArrayList<JoinPair>>();
	//left,right
	private HashMap<JoinTermNum,ArrayList<Integer>> joinRevert=new HashMap<JoinTermNum,ArrayList<Integer>>();
	
	public DocSet filterRight(DocSet leftDocs,DocSet rightDocs)
	{
		BitDocSet docset=new BitDocSet();
		DocIterator iter = rightDocs.iterator();
		while (iter.hasNext()) {
			int doc = iter.nextDoc();
			ArrayList<JoinPair> list=join.get(doc);
			if(list==null)
			{
				continue;
			}
			for(JoinPair jp:list)
			{
				for (int i:jp.left) {
					docset.add(i);
				}
			}
		}
		
		return leftDocs.intersection(docset);
	}
	
	public ArrayList<Integer> getRight(int leftDocid,int termNum)
	{
		return joinRevert.get(new JoinTermNum(leftDocid, termNum));
	}

	
	private void makejoin() throws IOException
	{
		
		FieldType ftleft=readerleft.getSchema().getFieldType(fieldLeft);
		
		String prefixLeft=TrieField.getMainValuePrefix(ftleft);
		TermIndex tiLeft = new TermIndex(fieldLeft, prefixLeft);
		NumberedTermEnum teLeft = tiLeft.getEnumerator(readerleft.getReader());
		
		FieldType ftright =readerright.getSchema().getFieldType(fieldRigth);
		String prefixRight=TrieField.getMainValuePrefix(ftright);
		TermIndex tiRight = new TermIndex(fieldRigth, prefixRight);
		
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

				TermDocs tdleft = teLeft.getTermDocs();
				tdleft.seek(teLeft);
				for (;;) {
					int n = tdleft.read(docs, freqs);
					if (n <= 0) {
						break;
					}
					for (int i = 0; i < n; i++) {
						jp.left.add(docs[i]);
					}
				}
				
				
				ArrayList<Integer> RightList=new ArrayList<Integer>();
				TermDocs tdRight = teRight.getTermDocs();
				tdRight.seek(teRight);
				for (;;) {
					int n = tdRight.read(docs, freqs);
					if (n <= 0) {
						break;
					}
					for (int i = 0; i < n; i++) {
						int docid=docs[i];
						RightList.add(docid);
						ArrayList<JoinPair> list=join.get(docid);
						if(list==null)
						{
							list=new ArrayList<HigoJoin.JoinPair>();
							join.put(docid, list);
						}
						list.add(jp);
					}
				}
				
				for(Integer leftid:jp.left)
				{
					JoinTermNum tm=new JoinTermNum(leftid, jp.termNum);
					joinRevert.put(tm, RightList);
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
		
		LOG.info("###join###"+join.size()+","+joinRevert.size());
	}
	
	

	private static class JoinPair{
		Integer termNum;
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((left == null) ? 0 : left.hashCode());
			result = prime * result
					+ ((termNum == null) ? 0 : termNum.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			JoinPair other = (JoinPair) obj;
			if (left == null) {
				if (other.left != null)
					return false;
			} else if (!left.equals(other.left))
				return false;
			if (termNum == null) {
				if (other.termNum != null)
					return false;
			} else if (!termNum.equals(other.termNum))
				return false;
			return true;
		}
		ArrayList<Integer> left=new ArrayList<Integer>();
	}
	
	private static class JoinTermNum{
		public JoinTermNum(Integer docId, Integer termNum) {
			super();
			this.leftId = docId;
			this.termNum = termNum;
		}
		Integer leftId;
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((leftId == null) ? 0 : leftId.hashCode());
			result = prime * result
					+ ((termNum == null) ? 0 : termNum.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			JoinTermNum other = (JoinTermNum) obj;
			if (leftId == null) {
				if (other.leftId != null)
					return false;
			} else if (!leftId.equals(other.leftId))
				return false;
			if (termNum == null) {
				if (other.termNum != null)
					return false;
			} else if (!termNum.equals(other.termNum))
				return false;
			return true;
		}
		Integer termNum;
		
	}

	
}
