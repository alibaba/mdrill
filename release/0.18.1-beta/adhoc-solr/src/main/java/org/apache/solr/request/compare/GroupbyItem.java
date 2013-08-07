package org.apache.solr.request.compare;



import org.apache.solr.common.util.NamedList;

public interface GroupbyItem  {
	public void setCross(String[] crossFs,String[] distFS);
	public void shardsMerge(GroupbyItem o);
	public boolean isrecordcount();
	public NamedList toNamedList();
	public String getKey() ;
//	public Integer getKeyNum() ;
//	public Long getValue();
	public boolean isFinalResult() ;
	public void setFinalResult(boolean isFinalResult);
}
