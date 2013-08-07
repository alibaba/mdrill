package org.apache.solr.request.compare;

import org.apache.solr.common.util.NamedList;

public class GroupbyAgent implements GroupbyItem{
	private final GroupbyItem groupby;
	
	public GroupbyAgent(String key,NamedList nst) 
	{
		Integer rc=(Integer) nst.get("rc");
		switch(rc)
		{
			case 0:
			{
				groupby=new RecordCount(key, nst);
				break;
			}
			case 1:
			{
				groupby=new GroupbyRow(key, nst);
				break;
			}
			case 2:
			{
				groupby=new SelectDetailRow(key, nst);
				break;
			}
			case 3:
			{
				groupby=new RecordCountDetail(key, nst);
				break;
			}
			default:{
				groupby=new RecordCount(key, nst);

			}
		}
	}
	
	public GroupbyItem getRaw()
	{
		return this.groupby;
	}
	
	public boolean isFinalResult() {
		return groupby.isFinalResult();
	}

	public void setFinalResult(boolean isFinalResult) {
		groupby.setFinalResult(isFinalResult);
	}
	
	public void shardsMerge(GroupbyItem o)
	{
		GroupbyItem mg=o;
		if(o instanceof GroupbyAgent)
		{
			mg=((GroupbyAgent) o).groupby;
		}
		
		groupby.shardsMerge(mg);
	}
	
	public boolean isrecordcount(){
		return groupby.isrecordcount();
	}
	
	public NamedList toNamedList()
	{
		return groupby.toNamedList();
	}

	public String getKey() {
		return groupby.getKey();
	}
	


	@Override
	public void setCross(String[] crossFs, String[] distFS) {
		groupby.setCross(crossFs, distFS);
	}

	
}
