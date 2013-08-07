package org.apache.solr.request.compare;


import org.apache.solr.common.util.NamedList;

/**
 * groupby 分页的总记录数实现
 * @author yannian.mu
 */
public class RecordCountDetail implements GroupbyItem{
	private long count=0l;
	private boolean isFinalResult=false;
	public RecordCountDetail() {}
	public RecordCountDetail(String key,NamedList nst)
	{
		this.count=(Long) nst.get("recordcount");
	}
	
	public void shardsMerge(GroupbyItem g)
	{	
		RecordCountDetail o=(RecordCountDetail)g;
		count+=o.count;
	}
	
	public boolean isFinalResult() {
		return isFinalResult;
	}

	public void setFinalResult(boolean isFinalResult) {
		this.isFinalResult = isFinalResult;
	}
	
	public void inc()
	{
		this.count++;
	}
	
	public void inc(int num)
	{
		this.count+=num;
	}
	
	
	public NamedList toNamedList()
	{
		NamedList rtn=new NamedList();
		rtn.add("recordcount", this.count);
		rtn.add("count", Integer.MAX_VALUE);
		if(!isFinalResult)
		{
			rtn.add("rc", 3);
		}
		return rtn;
	}
	

	public String getKey() {
		return "recordcount";
	}
	
	public boolean isrecordcount(){
		return true;
	}



	
	public Long getValue() {
		return this.count;
	}
	@Override
	public void setCross(String[] crossFs, String[] distFS) {
		
	}
	
	


}
