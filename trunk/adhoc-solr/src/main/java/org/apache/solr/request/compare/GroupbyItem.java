package org.apache.solr.request.compare;

import java.util.ArrayList;




public interface GroupbyItem  {
	public void setCross(String[] crossFs,String[] distFS);
	public void shardsMerge(GroupbyItem o);
	public boolean isrecordcount();
	public ArrayList<Object> toNamedList();
//	public Integer getKeyNum() ;
//	public Long getValue();
//	public boolean isFinalResult() ;
//	public void setFinalResult(boolean isFinalResult);
}
