package org.apache.solr.request.mdrill;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.PriorityQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.lucene.queryParser.ParseException;
import org.apache.solr.common.params.FacetParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.search.DocIterator;
import org.apache.solr.search.DocSet;
import org.apache.solr.search.SolrIndexSearcher;

import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.request.compare.RecordCountDetail;
import org.apache.solr.request.compare.SelectDetailRow;
import org.apache.solr.request.compare.ShardDetailSelectDetailRowCompare;
import org.apache.solr.request.compare.ShardDetailSelectDetailRowStringCompare;
import org.apache.solr.request.compare.UniqTypeNum;
import org.apache.solr.request.join.HigoJoinInvert;
import org.apache.solr.request.join.HigoJoinSort;
import org.apache.solr.request.join.HigoJoinUtils;
import org.apache.solr.request.mdrill.MdrillPorcessUtils.*;
import org.apache.solr.request.uninverted.UnInvertedField;
import org.apache.solr.request.uninverted.UnInvertedFieldUtils;

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
	public MdrillDetail(SolrIndexSearcher _searcher,SolrParams _params,SolrQueryRequest req)
	{
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
		
		this.isdesc=params.getBool(FacetParams.FACET_CROSS_SORT_ISDESC, true);
		this.cmpTermNum=new ShardDetailSelectDetailRowCompare(isdesc);
		this.cmpresult=new ShardDetailSelectDetailRowStringCompare(isdesc);
		this.recordCount = new RecordCountDetail();
		this.recordCount.setFinalResult(false);
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
	private HigoJoinSort[] joinSort={};
	private UniqTypeNum.SelectDetailSort SelectDetailSort=null;

	public PriorityQueue<SelectDetailRow> execute(PriorityQueue<SelectDetailRow> res,String[] fields,DocSet baseDocs) throws IOException, ParseException
	{
		UnvertFields ufs=new UnvertFields(fields, searcher);
		UnvertFields sortufs=new UnvertFields(new String[]{this.sort_fl}, searcher);
		
		this.joinInvert=new HigoJoinInvert[this.joinList.length];
		this.joinSort=new HigoJoinSort[this.joinList.length];

		for(int i=0;i<this.joinList.length;i++)
		{
			this.joinSort[i]=new HigoJoinSort(this.joinList[i], this.req);
			this.joinInvert[i]=new HigoJoinInvert(this.joinList[i], this.searcher);
			this.joinInvert[i].open(this.req);
			baseDocs=this.joinInvert[i].filterRight(baseDocs);
		}
		this.SelectDetailSort=UniqTypeNum.parseSelectDetailType(fields, joinSort);
		if(this.SelectDetailSort!=null)
		{
			this.isNeedSort=true;
		}
		
		
		int groupbySize=ufs.length;
		for(HigoJoinInvert inv:joinInvert)
		{
			groupbySize+=inv.fieldCount();
		}
		
		
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
	
	
	private TermNumToString[] prefetchValues(UnvertFields ufs, PriorityQueue<SelectDetailRow> res,UnvertFields sortufs,boolean isNumberVal) throws IOException
	{
		TermNumToString[] tm=new TermNumToString[ufs.length+1];
		for(int i=0;i<ufs.length;i++)
		 {
			tm[i]=new TermNumToString(ufs,i);
		 }
		tm[ufs.length]=new TermNumToString(sortufs,0);

		boolean usedsort=!isNumberVal&&this.SelectDetailSort==null;


		
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
				for (GroupList group : groups) {
					for (int i = 0; i < fc; i++) {
						inv.addTermNum(group.list[i], i);
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

	public PriorityQueue<SelectDetailRow> transGroupValue(UnvertFields ufs, PriorityQueue<SelectDetailRow> res,UnvertFields sortufs,boolean isNumberVal)
			throws ParseException, IOException {
		TermNumToString[] tm=this.prefetchValues(ufs, res, sortufs, isNumberVal);
		
		PriorityQueue<SelectDetailRow> topItems = new PriorityQueue<SelectDetailRow>(this.limit_offset, Collections.reverseOrder(this.cmpresult));

		boolean usedsort=!isNumberVal&&this.SelectDetailSort==null;

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
							tmpb.sortString=inv.getTermNumValue(group.list[i], i);
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
		this.recordCount.inc(baseDocs.size());
		int doc=-1;
		boolean isNumberVal=false;
		if(this.SelectDetailSort!=null)
		{
			double cmpValue=0;
			HigoJoinInvert inv=this.joinInvert[this.SelectDetailSort.sortIndex];
			while (iter.hasNext()) {
				doc = iter.nextDoc();
				cmpValue=inv.fieldNumTop(doc, this.SelectDetailSort.selfOffset,this.isdesc);
				SelectDetailRow row = SelectDetailRow.INSTANCE(doc,cmpValue);
				MdrillPorcessUtils.put2QueueDetail(row, res, this.limit_offset, this.cmpTermNum);
			}
		}else if(this.isNeedSort&&sortufs.listIndex.length>0)
		{
			double cmpValue=0;
			UnvertFile uf=sortufs.cols[0];

			UnInvertedField cif=uf.uif;
			boolean isnum=cif.dataType.equals(UnInvertedFieldUtils.Datatype.d_double)||cif.dataType.equals(UnInvertedFieldUtils.Datatype.d_long);
			isNumberVal=!cif.isMultiValued&&isnum;
			if(isNumberVal)//sort by num
			{
				while (iter.hasNext()) {
					doc = iter.nextDoc();
					cmpValue  = cif.quickToDouble(doc,uf.filetype, uf.ti);
					SelectDetailRow row = SelectDetailRow.INSTANCE(doc, cmpValue);
					MdrillPorcessUtils.put2QueueDetail(row, res, this.limit_offset, this.cmpTermNum);
				}
			}else{
				while (iter.hasNext()) {
					doc = iter.nextDoc();
					cmpValue =cif.termNum(doc);
					SelectDetailRow row = SelectDetailRow.INSTANCE(doc,cmpValue);
					MdrillPorcessUtils.put2QueueDetail(row, res, this.limit_offset, this.cmpTermNum);
				}
			}
		}else{
			while (iter.hasNext()) {
				doc = iter.nextDoc();
				SelectDetailRow row = SelectDetailRow.INSTANCE(doc,doc);
				MdrillPorcessUtils.put2QueueDetail(row, res, this.limit_offset, this.cmpTermNum);
				if(res.size()>=this.limit_offset)
				{
					break;
				}
			}
		}
		
		return this.transGroupValue(ufs, res,sortufs,isNumberVal);
	}
}
