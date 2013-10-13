package org.apache.solr.request.compare;


import java.util.ArrayList;


/**
 * groupby 分页的总记录数实现
 * @author yannian.mu
 */
public class RecordCountDetail implements GroupbyItem{
	private long count=0l;
	public RecordCountDetail() {}
	public RecordCountDetail(ArrayList<Object> nst)
	{
		this.count=(Long) nst.get(0);
	}
	
	public void shardsMerge(GroupbyItem g)
	{	
		RecordCountDetail o=(RecordCountDetail)g;
		count+=o.count;
	}
		
	public void inc()
	{
		this.count++;
	}
	
	public void inc(int num)
	{
		this.count+=num;
	}
	
	
	public ArrayList<Object> toNamedList()
	{
		ArrayList<Object> rtn=new ArrayList<Object>();
		rtn.add(0,this.count);//"key"
		rtn.add(1, 3);//顺序不能乱改
		return rtn;
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
