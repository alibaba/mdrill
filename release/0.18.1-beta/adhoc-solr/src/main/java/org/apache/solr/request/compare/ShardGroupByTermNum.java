package org.apache.solr.request.compare;



import org.apache.solr.request.mdrill.MdrillPorcessUtils.GroupList;
import org.apache.solr.request.mdrill.MdrillPorcessUtils.RefRow;
import org.apache.solr.request.mdrill.MdrillPorcessUtils.RefRowStat;



public class ShardGroupByTermNum implements Comparable<ShardGroupByTermNum>{
	public GroupList key;
	public RefRow statVal;

	public ShardGroupByTermNum(GroupList key, RefRow value) {
		this.key = key;
		this.statVal = value;
	}

	public RefRowStat gerRowStat(Integer fieldIndex)
	{
		RefRowStat stat=statVal.stat[fieldIndex];
		if (stat == null) {
			return null;
		}
		
		if(stat.issetup)
		{
			return stat;
		}
		return null;
	}

	@Override
	public int compareTo(ShardGroupByTermNum o) {
		return Double.compare(key.list.length, o.key.list.length);
	}
}
