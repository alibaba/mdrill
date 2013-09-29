package org.apache.solr.request.join;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.apache.lucene.index.SegmentReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.request.mdrill.GroupListCache;
import org.apache.solr.request.mdrill.MdrillUtils.TermNumToString;
import org.apache.solr.request.mdrill.MdrillUtils.UnvertFields;
import org.apache.solr.request.mdrill.MdrillUtils.UnvertFile;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.search.DocSet;
import org.apache.solr.search.SolrIndexSearcher;
import org.apache.solr.util.RefCounted;

import com.alimama.mdrill.utils.TryLockFile;


public class HigoJoinInvert {
	private static Logger LOG = Logger.getLogger(HigoJoinInvert.class);

	private String tableName;

	private LinkedBlockingQueue<GroupListCache.GroupList> groupListCache;

	
	public HigoJoinInvert(String tableName, SegmentReader reader,String partion,IndexSchema schema) {
		super();
		this.tableName = tableName;
		this.partion=partion;
		this.schema=schema;
		this.leftreader=reader;
	}
	
	private SegmentReader leftreader=null;
	private String partion;
	private IndexSchema schema;

	private RefCounted<SolrIndexSearcher> search=null;
	private HigoJoinInterface join;
	private DocSet docset;
	private String[] fields;
	private UnvertFields ufsRight;
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
	
			this.join=HigoJoin.getJoin(this.leftreader,this.partion,this.schema, search.get(), fieldLeft, fieldRigth);

			
			this.tmRigth=new TermNumToString[this.ufsRight.length];
			for(int i=0;i<this.ufsRight.length;i++)
			 {
				tmRigth[i]=new TermNumToString(this.ufsRight,i);
			 }
			groupListCache=GroupListCache.getGroupListQueue(ufsRight.length);
		
	}
	
	public LinkedBlockingQueue<GroupListCache.GroupList> getGroupListCache() {
		return groupListCache;
	}

	public DocSet filterByRight(DocSet leftDocs)
	{
		return this.join.filterByRight(leftDocs, this.docset);
	}
	
	public void setfieldNum(String[] values,int offset,GroupListCache.GroupList group)
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
//		int termNum=this.uifleft.termNum(doc);
		Integer doclist=this.join.getRight(doc, -1);
		if(doclist==null)
		{
			return false;
		}
		
		if(this.docset.exists(doclist))
		{
			return true;
		}
		
		return false;
	}
	
	public Integer fieldNumForSort(int doc,int offset) throws IOException
	{
//		int termNum=this.uifleft.termNum(doc);
		Integer doclist=this.join.getRight(doc, -1);
		if(doclist==null)
		{
			return ufsRight.cols[offset].uif.getNullTm();
		}
		
		if(!this.docset.exists(doclist))
		{
			return ufsRight.cols[offset].uif.getNullTm();
		}
		UnvertFile uf=ufsRight.cols[offset];
		return uf.uif.termNum(doclist);
	}
	
	
	
	public boolean fieldNum(int doc,int offset,GroupListCache.GroupList base,LinkedBlockingQueue<GroupListCache.GroupList> cache) throws IOException
	{
		Integer doclist=this.join.getRight(doc, -1);
		if(doclist==null)
		{
			return false;
		}
		
		if(!this.docset.exists(doclist))
		{
			return false;

		}
	
		int rightdocid=doclist;
		for (int j:ufsRight.listIndex) {
			UnvertFile uf=ufsRight.cols[j];
			base.list[j+offset]=uf.uif.termNum(rightdocid);
		}
		return true;
	
	
	}
	
	public boolean fieldNum(int doc,GroupListCache.GroupList base) throws IOException
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
		
		GroupListCache.cleanFieldValueCache(this.fieldCount());
		if(this.ufsRight!=null)
		{
			this.ufsRight.free();
			this.ufsRight=null;
		}
		if(search!=null)
		{
			search.decref();
			search=null;
		}
	}
}
