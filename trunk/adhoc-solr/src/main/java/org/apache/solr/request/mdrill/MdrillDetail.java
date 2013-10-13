package org.apache.solr.request.mdrill;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.lucene.index.SegmentReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.search.DocIterator;
import org.apache.solr.search.DocSet;
import org.apache.solr.search.SolrIndexSearcher;

import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.request.compare.ColumnKey;
import org.apache.solr.request.compare.RecordCountDetail;
import org.apache.solr.request.compare.SelectDetailRow;
import org.apache.solr.request.join.HigoJoinInvert;
import org.apache.solr.request.mdrill.MdrillUtils.*;

import com.alimama.mdrill.utils.EncodeUtils;
import com.alimama.mdrill.utils.UniqConfig;

/**
	查询明细的实现
 * @author yannian.mu
 */
public class MdrillDetail {
	public static Integer MAX_CROSS_ROWS=UniqConfig.defaultCrossMaxLimit();
	private SolrIndexSearcher searcher;
	private RecordCountDetail recordCount ;
	private SolrQueryRequest req;
	private MdrillParseDetail parse;
	private SegmentReader reader;
	private MdrillParseDetail.fetchContaioner container=null;
	public MdrillDetail(SolrIndexSearcher _searcher,SegmentReader reader,SolrParams _params,SolrQueryRequest req)
	{
		this.reader=reader;
		this.req=req;
		this.searcher=_searcher;
		this.parse=new MdrillParseDetail(_params);
		this.recordCount = new RecordCountDetail();
	}
	

	public NamedList get(String[] fields, DocSet baseDocs) throws IOException,
			ParseException {
		this.container=this.parse.createContainer(fields, baseDocs, this.reader, this.searcher, this.req);
		DocIterator iter = baseDocs.iterator();
		int doc=-1;
		if(this.container.isUseJoinSort())
		{
			int cmpValue=0;
			while (iter.hasNext()) {
				doc = iter.nextDoc();
				if(!this.container.containsInJoins(doc))
				{
					continue;
				}
				cmpValue=this.container.getJoinCompareValue(doc);
				SelectDetailRow row = SelectDetailRow.INSTANCE(doc,cmpValue);
				QueuePutUtils.put2QueueDetail(row, this.container.res, this.parse.limit_offset, this.container.cmpTermNum);
				this.recordCount.inc(1);
			}
		}else if(this.container.isColumnSort())
		{
			int cmpValue=0;
			
				while (iter.hasNext()) {
					doc = iter.nextDoc();
					if(!this.container.containsInJoins(doc))
					{
						continue;
					}
					cmpValue =this.container.getColumnCompareValue(doc);
					SelectDetailRow row = SelectDetailRow.INSTANCE(doc,cmpValue);
					QueuePutUtils.put2QueueDetail(row, this.container.res, this.parse.limit_offset, this.container.cmpTermNum);
					this.recordCount.inc(1);
				}
		}else{
			while (iter.hasNext()) {
				doc = iter.nextDoc();
				if(!this.container.containsInJoins(doc))
				{
					continue;
				}
				SelectDetailRow row = SelectDetailRow.INSTANCE(doc,doc);
				QueuePutUtils.put2QueueDetail(row, this.container.res, this.parse.limit_offset, this.container.cmpTermNum);
				this.recordCount.inc(1);
				if(this.container.res.size()>=this.parse.limit_offset)
				{
					break;
				}
			}
			while (iter.hasNext()) {
				doc = iter.nextDoc();
				if(!this.container.containsInJoins(doc))
				{
					continue;
				}
				this.recordCount.inc(1);
			}
		}
		
		PriorityQueue<SelectDetailRow> topItems=this.transGroupValue(this.container.ufs, this.container.res,this.container.sortufs);
				
		 this.container.free();
		
		return this.toNameList(topItems);	
	}

	
	
	private TermNumToString[] prefetchValues(UnvertFields ufs, PriorityQueue<SelectDetailRow> res,UnvertFields sortufs) throws IOException
	{
		TermNumToString[] tm=new TermNumToString[ufs.length+1];
		for(int i=0;i<ufs.length;i++)
		 {
			tm[i]=new TermNumToString(ufs,i);
		 }
		tm[ufs.length]=new TermNumToString(sortufs,0);//for sort
		boolean usedsort=this.container.isOnlyColumnSort();

		for (SelectDetailRow row : res) {
			int doc = row.docid;
			for (int i = 0; i < ufs.length; i++) {
				UnvertFile uf=ufs.cols[i];
				if (uf!= null) {
					Integer termNum = uf.uif.termNum(doc);
					tm[i].addTermNum(termNum);
				} 
			}
			
			if(usedsort)
			{
				int ternnum=(int)row.value;
				tm[ufs.length].addTermNum(ternnum);
			}
			
			
			for(HigoJoinInvert inv:this.container.joinInvert)
			{
				int fc=inv.fieldCount();
				LinkedBlockingQueue<GroupListCache.GroupList> groupListCache=inv.getGroupListCache();
				GroupListCache.GroupList base=GroupListCache.GroupList.INSTANCE(groupListCache, fc);
				base.reset();
				if(inv.fieldNum(doc, base))
				{
					for (int i = 0; i < fc; i++) {
						inv.addTermNum(base.list[i], i);
					}
				}

				groupListCache.add(base);
			}
		}
		
		
		 //fetch by field 
		 for(int i=0;i<tm.length;i++)
		 {
			tm[i].fetchValues();
		 }
		 
		 for(HigoJoinInvert inv:this.container.joinInvert)
		 {
			 inv.fetchValues();
		 }
		 
		 return tm;
	}

	public PriorityQueue<SelectDetailRow> transGroupValue(UnvertFields ufs, PriorityQueue<SelectDetailRow> res,UnvertFields sortufs)throws ParseException, IOException {
		TermNumToString[] tm=this.prefetchValues(ufs, res, sortufs);
		
		PriorityQueue<SelectDetailRow> topItems = new PriorityQueue<SelectDetailRow>(this.parse.limit_offset, Collections.reverseOrder(this.container.cmpresult));

		boolean usedsort=this.container.isOnlyColumnSort();

		for (SelectDetailRow row : res) {
			int doc = row.docid;
			String sortString=null;
			if(usedsort)
			{
				sortString=tm[ufs.length].getTermValueWithNull((int)row.value);
			}
			SortGroupVal buff = new SortGroupVal();
			buff.groupbuff.append("-");
			buff.groupbuff.append(UniqConfig.GroupJoinString());
			buff.groupbuff.append("-");
			for (int i = 0; i < ufs.length; i++) {
				buff.groupbuff.append(UniqConfig.GroupJoinString());
				UnvertFile uf=ufs.cols[i];
				if (uf != null) {
					Integer termNum = uf.uif.termNum(doc);
					buff.groupbuff.append(EncodeUtils.encode(tm[i].getTermValue(termNum)));
				} else {
					buff.groupbuff.append(EncodeUtils.encode("-"));
				}
			}
			buff.sortString=sortString;
			this.setGroupJoin(buff, ufs.length,doc);
			SelectDetailRow newrow = SelectDetailRow.INSTANCE(doc, row.getCompareValue());
			newrow.setKey(new ColumnKey(buff.groupbuff.toString()));
			newrow.colVal=buff.sortString;
			QueuePutUtils.put2QueueDetail(newrow, topItems, this.parse.limit_offset, this.container.cmpresult);
		}

		return topItems;
	}
	
	
	public void setGroupJoin(SortGroupVal buff,int offset,int doc) throws IOException
	{
		for(HigoJoinInvert inv:this.container.joinInvert)
		{
			int fc=inv.fieldCount();
			LinkedBlockingQueue<GroupListCache.GroupList> groupListCache=inv.getGroupListCache();
			GroupListCache.GroupList base=GroupListCache.GroupList.INSTANCE(groupListCache, fc);
			base.reset();
			if(inv.fieldNum(doc, base))
			{

				for (int i = 0; i < fc; i++) {
					buff.groupbuff.append(UniqConfig.GroupJoinString());
					buff.groupbuff.append(EncodeUtils.encode(inv.getTermNumValue(base.list[i], i)));
					if(this.container.SelectDetailSort!=null&&(offset+i)==this.container.SelectDetailSort.offset)
					{
						buff.sortString=inv.getTermNumValue(base.list[this.container.SelectDetailSort.selfOffset], i);
					}
				}
			}
			groupListCache.add(base);
			offset+=fc;
		}
	}
	
	

	private NamedList toNameList(PriorityQueue<SelectDetailRow> topItems) {
		java.util.ArrayList<SelectDetailRow> recommendations = new ArrayList<SelectDetailRow>(topItems.size());
		recommendations.addAll(topItems);
		Collections.sort(recommendations, this.container.cmpresult);
		Integer index = 0;
		NamedList res = new NamedList();
		res.add("count", recordCount.toNamedList());
		
		
		ConcurrentHashMap<Long,String> cache=null;

		boolean issetCrc=this.parse.crcOutputSet!=null;
		if(issetCrc)
		{
			synchronized (MdrillUtils.CRC_CACHE_SIZE) {
				cache=MdrillUtils.CRC_CACHE_SIZE.get(this.parse.crcOutputSet);
				if(cache==null)
				{
					cache=new ConcurrentHashMap<Long,String>();
					MdrillUtils.CRC_CACHE_SIZE.put(this.parse.crcOutputSet, cache);
				}
			}
			
		}
		
		ArrayList<Object> list=new ArrayList<Object>();

		
		for (SelectDetailRow kv : recommendations) {
			if (index >= this.parse.offset) {
				if(issetCrc)
				{
					kv.ToCrcSet(cache);
				}
				list.add(kv.toNamedList());
			}
			index++;
		}
		res.add("list", list);
		return res;
	}
	
	public static class SortGroupVal{
		public StringBuffer groupbuff=new StringBuffer();
		public String sortString;
	}
	
}
