package org.apache.solr.request.compare;

import java.util.ArrayList;

import com.alimama.mdrill.distinct.DistinctCount.DistinctCountAutoAjuest;


public class GroupbyAgent implements GroupbyItem{
	private final GroupbyItem groupby;
	
	public GroupbyAgent(ArrayList<Object> nst) 
	{
		Integer rc=(Integer) nst.get(1);
		switch(rc)
		{
			case 0:
			{
				groupby=new RecordCount(nst);
				break;
			}
			case 1:
			{
				groupby=new GroupbyRow(nst);
				break;
			}
			case 2:
			{
				groupby=new SelectDetailRow(nst);
				break;
			}
			case 3:
			{
				groupby=new RecordCountDetail(nst);
				break;
			}
			default:{
				groupby=new RecordCount(nst);

			}
		}
	}
	
	public GroupbyItem getRaw()
	{
		return this.groupby;
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
	

	@Override
	public void setCross(String[] crossFs, String[] distFS) {
		groupby.setCross(crossFs, distFS);
	}

	@Override
	public ArrayList<Object> toNamedList() {
		return groupby.toNamedList();
	}

	
}
