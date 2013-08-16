package org.apache.solr.request.mdrill;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.PriorityQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.SegmentReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.solr.common.params.FacetParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.handler.component.ShardResponse;
import org.apache.solr.search.DocIterator;
import org.apache.solr.search.DocSet;
import org.apache.solr.search.SolrIndexReader;
import org.apache.solr.search.SolrIndexSearcher;

import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.request.compare.GroupbyItem;
import org.apache.solr.request.compare.RecordCountDetail;
import org.apache.solr.request.compare.SelectDetailRow;
import org.apache.solr.request.compare.ShardDetailSelectDetailRowCompare;
import org.apache.solr.request.compare.ShardDetailSelectDetailRowStringCompare;
import org.apache.solr.request.compare.UniqTypeNum;
import org.apache.solr.request.join.HigoJoinInvert;
import org.apache.solr.request.join.HigoJoinSort;
import org.apache.solr.request.join.HigoJoinUtils;
import org.apache.solr.request.mdrill.FacetComponent.DistribFieldFacet;
import org.apache.solr.request.mdrill.FacetComponent.FacetInfo;
import org.apache.solr.request.mdrill.MdrillPorcessUtils.*;
import org.apache.solr.request.uninverted.UnInvertedField;

import com.alimama.mdrill.utils.EncodeUtils;
import com.alimama.mdrill.utils.UniqConfig;

/**
	查询明细的实现
 * @author yannian.mu
 */
public class MdrillDetail {
	public static Integer MAX_CROSS_ROWS=UniqConfig.defaultCrossMaxLimit();
	private SolrIndexSearcher searcher;
	private SolrParams params;
	private int offset ;
	private RecordCountDetail recordCount ;
	private ShardDetailSelectDetailRowCompare cmpTermNum;
	private ShardDetailSelectDetailRowStringCompare cmpresult;
	private String sort_fl;
	private boolean isNeedSort=true;
	private int limit_offset=0;
	private String[] joinList ;
	private SolrQueryRequest req;
	boolean isdesc;
	private String sort_column_type;
	public MdrillDetail(SolrIndexSearcher _searcher,SolrParams _params,SolrQueryRequest req)
	{
		this.req=req;
		this.searcher=_searcher;
		this.params=_params;
		this.init();
	}
	
	private SegmentReader reader;
	private boolean isSchemaReaderType=false;
	public MdrillDetail(SolrIndexSearcher _searcher,SegmentReader reader,SolrParams _params,SolrQueryRequest req)
	{
		this.isSchemaReaderType=true;
		this.reader=reader;
		this.req=req;
		this.searcher=_searcher;
		this.params=_params;
		this.init();
	}
	
	private void init()
	{
		this.joinList=params.getParams(HigoJoinUtils.getTables());
		if(this.joinList==null)
		{
			this.joinList= new String[0];
		}
		this.offset = params.getInt(FacetParams.FACET_CROSS_OFFSET, 0);
		int limit = params.getInt(FacetParams.FACET_CROSS_LIMIT, 100);
		this.limit_offset=this.offset+limit;
		this.sort_fl=params.get(FacetParams.FACET_CROSS_SORT_FL,null);
		
		if(this.sort_fl==null||this.sort_fl.isEmpty())
		{
			this.sort_fl="higoempty_sort_s";
			this.isNeedSort=false;
		}
		this.sort_column_type=params.get("facet.cross.sort.cp");
		this.isdesc=params.getBool(FacetParams.FACET_CROSS_SORT_ISDESC, true);
		
		this.recordCount = new RecordCountDetail();
		this.recordCount.setFinalResult(false);
	}
	
	public NamedList getBySchemaReader(String[] fields, DocSet base)throws Exception 
	{
		SolrIndexReader reader=this.searcher.getReader();
		IndexReader.InvertParams invparam=new IndexReader.InvertParams();
		invparam._searcher=this.searcher;
		invparam._params=this.params;
		invparam.fields=fields;
		invparam.base=base;
		invparam.req=this.req;
		invparam.isdetail=false;
		IndexReader.InvertResult result=reader.invertScan(this.searcher.getSchema(), invparam);
		ArrayList<NamedList> resultlist=result.getResult();
		if(resultlist.size()==1)
		{
			return resultlist.get(0);
		}
		FacetComponent.FacetInfo fi = new FacetComponent.FacetInfo();
	     fi.parse(params);
         DistribFieldFacet dff = fi.facets.get("solrCorssFields_s");

	     long addtime=0;
	     for (NamedList nl: resultlist) {
	         addtime+=dff.add(nl, dff);
	     }
	     
	     NamedList fieldCounts = new NamedList();
	      GroupbyItem[] counts = dff.getPairSorted(dff.sort_column_type,dff.joinSort,dff.facetFs,dff.crossFs,dff.distFS,dff.sort_fl, dff.sort_type, dff.isdesc,this.limit_offset);
	      if(dff.recordcount!=null)
	      {
	    	  GroupbyItem recordcount=dff.recordcount;
		      fieldCounts.add(recordcount.getKey(), recordcount.toNamedList());
	      }
	      int end = this.limit_offset> counts.length ?counts.length:this.limit_offset;
	      for (int i=this.offset; i<end; i++) {
	        fieldCounts.add(counts[i].getKey(), counts[i].toNamedList());
	      }
		return fieldCounts;
	}
		
	public NamedList getDetail(String[] fields, DocSet base) throws IOException,
			ParseException {
		synchronized (MdrillPorcessUtils.getLock()) {
			PriorityQueue<SelectDetailRow> topItems = new PriorityQueue<SelectDetailRow>(	this.limit_offset, Collections.reverseOrder(cmpTermNum));
			PriorityQueue<SelectDetailRow> rows=this.execute(topItems, fields, base);
			return this.toNameList(rows);	
		}
	}

	private NamedList toNameList(PriorityQueue<SelectDetailRow> topItems) {
		java.util.ArrayList<SelectDetailRow> recommendations = new ArrayList<SelectDetailRow>(topItems.size());
		recommendations.addAll(topItems);
		Collections.sort(recommendations, this.cmpresult);
		Integer index = 0;
		NamedList res = new NamedList();
		res.add(recordCount.getKey(), recordCount.toNamedList());
		for (SelectDetailRow kv : recommendations) {
			if (index >= this.offset) {
				res.add(kv.getKey(), kv.toNamedList());
			}
			index++;
		}
		return res;
	}
	
	private HigoJoinInvert[] joinInvert={};
	private UniqTypeNum.SelectDetailSort SelectDetailSort=null;

	
	
	public PriorityQueue<SelectDetailRow> execute(PriorityQueue<SelectDetailRow> res,String[] fields,DocSet baseDocs) throws IOException, ParseException
	{
		UnvertFields ufs=null;
	
		UnvertFields sortufs=null;
		if(!isSchemaReaderType)
		{
			ufs=new UnvertFields(fields, searcher);
			sortufs=new UnvertFields(new String[]{this.sort_fl}, searcher);
		}else{
			ufs=new UnvertFields(fields, this.reader,this.searcher.getPartionKey(),this.searcher.getSchema());
			sortufs=new UnvertFields(new String[]{this.sort_fl}, this.reader,this.searcher.getPartionKey(),this.searcher.getSchema());
		}
		
		this.joinInvert=new HigoJoinInvert[this.joinList.length];
		HigoJoinSort[] joinSort=new HigoJoinSort[this.joinList.length];

		for(int i=0;i<this.joinList.length;i++)
		{
			joinSort[i]=new HigoJoinSort(this.joinList[i], this.req);
			if(!isSchemaReaderType)
			{
				this.joinInvert[i]=new HigoJoinInvert(this.joinList[i], this.searcher);
			}else{
				this.joinInvert[i]=new HigoJoinInvert(this.joinList[i], this.reader,this.searcher.getPartionKey(),this.searcher.getSchema());
			}
			this.joinInvert[i].open(this.req);
			baseDocs=this.joinInvert[i].filterByRight(baseDocs);
		}
		this.SelectDetailSort=UniqTypeNum.parseSelectDetailType(fields, joinSort);
		this.cmpTermNum=new ShardDetailSelectDetailRowCompare(isdesc);
		if(this.SelectDetailSort!=null)
		{
			this.isNeedSort=true;
			this.cmpresult=new ShardDetailSelectDetailRowStringCompare("string",isdesc);

		}else{
			this.cmpresult=new ShardDetailSelectDetailRowStringCompare(this.sort_column_type,isdesc);
		}
		
		
		int groupbySize=ufs.length;
		for(HigoJoinInvert inv:joinInvert)
		{
			groupbySize+=inv.fieldCount();
		}
		this.nonJoins=this.joinInvert.length<=0;
		
		PriorityQueue<SelectDetailRow> rtn=this.topRows(sortufs,ufs,res, fields, baseDocs);
		
		for(int i=0;i<this.joinList.length;i++)
		{
			this.joinInvert[i].close();
		}
		ufs.free();
		sortufs.free();
		MdrillPorcessUtils.cleanFieldValueCache(groupbySize);
		SelectDetailRow.CLEAN();
		return rtn;
	}
	
	
	boolean nonJoins=true;
	private boolean joincontains(int doc) throws IOException
	{
		if(nonJoins)
		{
			return true;
		}
		
		for(HigoJoinInvert inv:this.joinInvert)
		{
			if(!inv.contains(doc))
			{
				return false;
			}
		}
		return true;
	}
	
	
	private TermNumToString[] prefetchValues(UnvertFields ufs, PriorityQueue<SelectDetailRow> res,UnvertFields sortufs) throws IOException
	{
		TermNumToString[] tm=new TermNumToString[ufs.length+1];
		for(int i=0;i<ufs.length;i++)
		 {
			tm[i]=new TermNumToString(ufs,i);
		 }
		tm[ufs.length]=new TermNumToString(sortufs,0);

		boolean usedsort=isNeedSort&&this.SelectDetailSort==null;


		
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
			
			
			for(HigoJoinInvert inv:joinInvert)
			{
				int fc=inv.fieldCount();
				LinkedBlockingQueue<GroupList> groupListCache=inv.getGroupListCache();
				GroupList base=GroupList.INSTANCE(groupListCache, fc);
				base.reset();
				GroupList[] groups=inv.fieldNum(doc, base);
				if(groups!=null)
				{
				for (GroupList group : groups) {
					for (int i = 0; i < fc; i++) {
						inv.addTermNum(group.list[i], i);
					}
				}
				}
				
				for (GroupList group : groups) {
					groupListCache.add(group);
				}

				groupListCache.add(base);
			}
		}
		
		
		 //fetch by field 
		 for(int i=0;i<tm.length;i++)
		 {
			tm[i].fetchValues();
		 }
		 
		 for(HigoJoinInvert inv:joinInvert)
		 {
			 inv.fetchValues();
		 }
		 
		 return tm;
	}

	public PriorityQueue<SelectDetailRow> transGroupValue(UnvertFields ufs, PriorityQueue<SelectDetailRow> res,UnvertFields sortufs)
			throws ParseException, IOException {
		TermNumToString[] tm=this.prefetchValues(ufs, res, sortufs);
		
		PriorityQueue<SelectDetailRow> topItems = new PriorityQueue<SelectDetailRow>(this.limit_offset, Collections.reverseOrder(this.cmpresult));

		boolean usedsort=isNeedSort&&this.SelectDetailSort==null;

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

			ArrayList<SortGroupVal> newgroup=this.setGroupJoin(buff, ufs.length,doc);
			for(SortGroupVal g:newgroup)
			{
				SelectDetailRow newrow = SelectDetailRow.INSTANCE(doc, row.getCompareValue());
				newrow.setKey(g.groupbuff.toString());
				newrow.colVal=g.sortString;
				MdrillPorcessUtils.put2QueueDetail(newrow, topItems, this.limit_offset, this.cmpresult);
			}
		}

		return topItems;
		
	}
	
	
	public ArrayList<SortGroupVal> setGroupJoin(SortGroupVal buff,int offset,int doc) throws IOException
	{
		ArrayList<SortGroupVal> tmp=new ArrayList<SortGroupVal>(1);
		ArrayList<SortGroupVal> newgroup=new ArrayList<SortGroupVal>(1);
		newgroup.add(buff);
		for(HigoJoinInvert inv:joinInvert)
		{
			int fc=inv.fieldCount();
			LinkedBlockingQueue<GroupList> groupListCache=inv.getGroupListCache();
			GroupList base=GroupList.INSTANCE(groupListCache, fc);
			base.reset();
			GroupList[] groups=inv.fieldNum(doc, base);
			groupListCache.add(base);
			for(SortGroupVal gbuff:newgroup)
			{
				for (GroupList group : groups) {
					SortGroupVal tmpb=new SortGroupVal();
					tmpb.sortString=gbuff.sortString;
					tmpb.groupbuff.append(gbuff.groupbuff);
					for (int i = 0; i < fc; i++) {
						tmpb.groupbuff.append(UniqConfig.GroupJoinString());
						tmpb.groupbuff.append(EncodeUtils.encode(inv.getTermNumValue(group.list[i], i)));
						if(this.SelectDetailSort!=null&&(offset+i)==this.SelectDetailSort.offset)
						{
							tmpb.sortString=inv.getTermNumValue(group.list[this.SelectDetailSort.selfOffset], i);
						}
					}
					tmp.add(tmpb);
				}
			}
			for (GroupList group : groups) {
				groupListCache.add(group);
			}
			
			newgroup=tmp;
			tmp=new ArrayList<SortGroupVal>();
			offset+=fc;
		}
		
		return newgroup;
	}
	
	public static class SortGroupVal{
		public StringBuffer groupbuff=new StringBuffer();
		public String sortString;
	}
	
	public PriorityQueue<SelectDetailRow> topRows(UnvertFields sortufs,UnvertFields ufs,PriorityQueue<SelectDetailRow> res,String[] fields,DocSet baseDocs) throws IOException, ParseException
	{
		DocIterator iter = baseDocs.iterator();
		int doc=-1;
		if(this.SelectDetailSort!=null)
		{
			int cmpValue=0;
			HigoJoinInvert inv=this.joinInvert[this.SelectDetailSort.sortIndex];
			while (iter.hasNext()) {
				doc = iter.nextDoc();
				if(!this.joincontains(doc))
				{
					continue;
				}
				cmpValue=inv.fieldNumTop(doc, this.SelectDetailSort.selfOffset,this.isdesc);
				SelectDetailRow row = SelectDetailRow.INSTANCE(doc,cmpValue);
				MdrillPorcessUtils.put2QueueDetail(row, res, this.limit_offset, this.cmpTermNum);
				this.recordCount.inc(1);
			}
		}else if(this.isNeedSort&&sortufs.listIndex.length>0)
		{
			int cmpValue=0;
			UnvertFile uf=sortufs.cols[0];

			UnInvertedField cif=uf.uif;
				while (iter.hasNext()) {
					doc = iter.nextDoc();
					if(!this.joincontains(doc))
					{
						continue;
					}
					cmpValue =cif.termNum(doc);
					SelectDetailRow row = SelectDetailRow.INSTANCE(doc,cmpValue);
					MdrillPorcessUtils.put2QueueDetail(row, res, this.limit_offset, this.cmpTermNum);
					this.recordCount.inc(1);
				}
//			}
		}else{
			while (iter.hasNext()) {
				doc = iter.nextDoc();
				if(!this.joincontains(doc))
				{
					continue;
				}
				SelectDetailRow row = SelectDetailRow.INSTANCE(doc,doc);
				MdrillPorcessUtils.put2QueueDetail(row, res, this.limit_offset, this.cmpTermNum);
				this.recordCount.inc(1);
				if(res.size()>=this.limit_offset)
				{
					break;
				}
			}
			while (iter.hasNext()) {
				doc = iter.nextDoc();
				if(!this.joincontains(doc))
				{
					continue;
				}
				this.recordCount.inc(1);
			}
		}
		
		return this.transGroupValue(ufs, res,sortufs);
	}
}
