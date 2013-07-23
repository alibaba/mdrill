package org.apache.solr.request.join;

import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.request.compare.UniqTypeNum;

public class HigoJoinSort {
	private String tableName;
	String[] fields;
	int index=-1;
	String sort;
	public HigoJoinSort(String tableName,SolrQueryRequest req) {
		this.tableName = tableName;
		this.fields=req.getParams().getParams(HigoJoinUtils.getFields(this.tableName));
		String sort=req.getParams().get(HigoJoinUtils.getsortField(this.tableName));
		this.index=UniqTypeNum.foundIndex(this.fields, sort, -1);
	}
	
	public HigoJoinSort(String tableName,SolrParams params) {
		this.tableName = tableName;
		this.fields=params.getParams(HigoJoinUtils.getFields(this.tableName));
		this.sort=params.get(HigoJoinUtils.getsortField(this.tableName));
		this.index=UniqTypeNum.foundIndex(this.fields, sort, -1);
	}
	
	public int getoffset()
	{
		return this.fields.length;
	}
	
	public int getIndex()
	{
		return this.index;
	}
	
	public String getSortField()
	{
		return this.sort;
	}

}
