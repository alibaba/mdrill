package org.apache.solr.request.join;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.apache.lucene.index.SegmentReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.request.join.HigoJoin.IntArr;
import org.apache.solr.request.mdrill.MdrillPorcessUtils;
import org.apache.solr.request.mdrill.MdrillPorcessUtils.GroupList;
import org.apache.solr.request.mdrill.MdrillPorcessUtils.TermNumToString;
import org.apache.solr.request.mdrill.MdrillPorcessUtils.UnvertFields;
import org.apache.solr.request.mdrill.MdrillPorcessUtils.UnvertFile;
import org.apache.solr.request.uninverted.UnInvertedField;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.search.DocSet;
import org.apache.solr.search.SolrIndexSearcher;
import org.apache.solr.util.RefCounted;


public class HigoJoinInvert {
	private static Logger LOG = Logger.getLogger(HigoJoinInvert.class);

	private String tableName;
	private SolrIndexSearcher leftSearcher;

	private LinkedBlockingQueue<GroupList> groupListCache;

	public HigoJoinInvert(String tableName, SolrIndexSearcher leftSearcher) {
		super();
		this.tableName = tableName;
		this.leftSearcher = leftSearcher;
	}
	public HigoJoinInvert(String tableName, SegmentReader reader,String partion,IndexSchema schema) {
		super();
		this.tableName = tableName;
		this.leftSearcher = null;
		this.partion=partion;
		this.schema=schema;
		this.leftreader=reader;
	}
	
	SegmentReader leftreader=null;
	String partion;
	IndexSchema schema;

	private RefCounted<SolrIndexSearcher> search=null;
	private HigoJoinInterface join;
	private DocSet docset;
	private String[] fields;
	private UnvertFields ufsRight;
	private UnInvertedField uifleft;
	private TermNumToString[] tmRigth;
	public void open(SolrQueryRequest req) throws IOException, ParseException
	{
		this.search=HigoJoinUtils.getSearch(req, this.tableName);
		this.fields = req.getParams().getParams(HigoJoinUtils.getFields(this.tableName));
		List<Query> fqlist=HigoJoinUtils.getFilterQuery(req, this.tableName);
		LOG.info("##fqlist.size()##"+fqlist.size());
		this.docset=this.search.get().getDocSet(fqlist);
		LOG.info("##joinright##"+this.docset.size());
		String fieldLeft=req.getParams().get(HigoJoinUtils.getLeftField(this.tableName));
		String fieldRigth=req.getParams().get(HigoJoinUtils.getRightField(this.tableName));
		this.ufsRight=new UnvertFields(fields, this.search.get());

		if(this.leftreader!=null)
		{
			this.join=HigoJoin.getJoin(this.leftreader,this.partion,this.schema, search.get(), fieldLeft, fieldRigth);
			this.uifleft = UnInvertedField.getUnInvertedField(fieldLeft,this.leftreader,this.partion,this.schema);

		}else{
			this.join=HigoJoin.getJoin(leftSearcher, search.get(), fieldLeft, fieldRigth);
			this.uifleft = UnInvertedField.getUnInvertedField(fieldLeft,this.leftSearcher);
		}
		
		this.tmRigth=new TermNumToString[this.ufsRight.length];
		for(int i=0;i<this.ufsRight.length;i++)
		 {
			tmRigth[i]=new TermNumToString(this.ufsRight,i);
		 }

		groupListCache=MdrillPorcessUtils.getGroupListQueue(ufsRight.length);
	}
	
	public LinkedBlockingQueue<GroupList> getGroupListCache() {
		return groupListCache;
	}

	public DocSet filterByRight(DocSet leftDocs)
	{
		return this.join.filterByRight(leftDocs, this.docset);
	}
	
	

	public void setfieldNum(String[] values,int offset,GroupList group)
	{
		UnvertFields ufs=ufsRight;
		for (int i:ufs.listIndex) {
			UnvertFile uf=ufs.cols[i];

			try {
				group.list[i+offset]=uf.uif.getTermNum(uf.ti,values[i+offset],uf.filetype);
			} catch (Throwable e) {
				LOG.error("setfieldNum",e);
				group.list[i+offset]=uf.uif.getNullTm();
			}
		}
	}
	
	public boolean contains(int doc) throws IOException
	{
		int termNum=this.uifleft.termNum(doc);
		IntArr doclist=this.join.getRight(doc, termNum);
		if(doclist==null)
		{
			return false;
		}
		
		
		for(Integer docr:doclist.list)
		{
			if(this.docset.exists(docr))
			{
				return true;
			}
		}
		
		return false;
	}
	
	public Integer fieldNumTop(int doc,int offset,boolean isdesc) throws IOException
	{
		Integer rtn=isdesc?Integer.MIN_VALUE:Integer.MAX_VALUE;
		int termNum=this.uifleft.termNum(doc);
		IntArr doclist=this.join.getRight(doc, termNum);
		if(doclist==null)
		{
			return ufsRight.cols[offset].uif.getNullTm();
		}
		
		
		ArrayList<Integer> filter=new ArrayList<Integer>(doclist.list.length);
		for(Integer docr:doclist.list)
		{
			if(this.docset.exists(docr))
			{
				filter.add(docr);
			}
		}
		
		int size=filter.size();
		if(size==0)
		{
			return ufsRight.cols[offset].uif.getNullTm();
		}
		
		boolean isset=false;
		
		if(isdesc)
		{
			for(int i=0;i<size;i++)
			{
				UnvertFile uf=ufsRight.cols[offset];
				isset=true;
				int rightdocid=filter.get(i);
				rtn=Math.max(uf.uif.termNum(rightdocid), rtn);
			}
		}else{
			for(int i=0;i<size;i++)
			{
				UnvertFile uf=ufsRight.cols[offset];
				isset=true;
				int rightdocid=filter.get(i);
				rtn=Math.min(uf.uif.termNum(rightdocid), rtn);
			}
		}
		
		return isset?rtn:ufsRight.cols[offset].uif.getNullTm();
	}
	
	
	
	public GroupList[] fieldNum(int doc,int offset,GroupList base,LinkedBlockingQueue<GroupList> cache) throws IOException
	{
		int termNum=this.uifleft.termNum(doc);
		IntArr doclist=this.join.getRight(doc, termNum);
		if(doclist==null)
		{
			return null;
		}
		
		
		ArrayList<Integer> filter=new ArrayList<Integer>(doclist.list.length);
		for(Integer docr:doclist.list)
		{
			if(this.docset.exists(docr))
			{
				filter.add(docr);
			}
		}
		
		int size=filter.size();
		if(size==0)
		{
			return null;
		}
		GroupList[] group = new GroupList[size];
		
		for(int i=0;i<size;i++)
		{
			int rightdocid=filter.get(i);
			group[i]=base.copy(cache);
			for (int j:ufsRight.listIndex) {
				UnvertFile uf=ufsRight.cols[j];

				group[i].list[j+offset]=uf.uif.termNum(rightdocid);
			}
		}
		
		return group;
	}
	
//	private GroupList nullGroup(GroupList base,int offset,LinkedBlockingQueue<GroupList> cache)
//	{
//		GroupList rtn=base.copy(cache);
//		for (int j:ufsRight.listIndex) {
//			UnvertFile uf=ufsRight.cols[j];
//
//			rtn.list[j+offset]=uf.uif.getNullTm();
//		}
//		
//		return rtn;
//	}
//	
	public GroupList[] fieldNum(int doc,GroupList base) throws IOException
	{
		return this.fieldNum(doc, 0, base,this.groupListCache);
	}
	
	public int fieldCount()
	{
		return ufsRight.length;
	}
	
	
	public void addTermNum(int termNum,int i) throws IOException
	{
		this.tmRigth[i].addTermNum(termNum);
	}
	
	public void fetchValues() throws IOException
	{
		 for(int i=0;i<tmRigth.length;i++)
		 {
			 tmRigth[i].fetchValues();
		 }
	}
	
	public String getTermNumValue(int termNum,int i)
	{
		return tmRigth[i].getTermValue(termNum);
	}

	
	public void close()
	{
		
		MdrillPorcessUtils.cleanFieldValueCache(this.fieldCount());
		if(this.ufsRight!=null)
		{
			this.ufsRight.free();
			this.ufsRight=null;
		}
		if(this.uifleft!=null)
		{
			this.uifleft.free();
			this.uifleft=null;
		}
		if(search!=null)
		{
			search.decref();
			search=null;
		}
	}
}
