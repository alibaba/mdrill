package org.apache.solr.request.compare;


import java.util.concurrent.LinkedBlockingQueue;
import org.apache.log4j.Logger;
import org.apache.solr.common.util.NamedList;

import com.alimama.mdrill.utils.UniqConfig;



/**
 * 查询明细的基本数据结构
 * @author yannian.mu
 */
public class SelectDetailRow implements Comparable<SelectDetailRow>, GroupbyItem{
    private static Logger LOG = Logger.getLogger(SelectDetailRow.class);

	private SelectDetailRow()
	{
		
	}
	
	public int docid=0;//forCompare
	private String key;//col1@col2@col3
	public double value;//compare field value
	public String colVal=null;//compare field value
	private boolean isFinalResult=false;
	
	
	public void setKey(String key) {
		this.key = key;
	}
	
	public boolean isFinalResult() {
		return isFinalResult;
	}

	public void setFinalResult(boolean isFinalResult) {
		this.isFinalResult = isFinalResult;
	}
	
	
	public SelectDetailRow(String key,NamedList nst)
	{
		this.key=key;
		this.value=(Double) nst.get("count");
		this.docid= (Integer) nst.get("docid");
		this.colVal=(String) nst.get("sortStr");
		

	}
	
	public void shardsMerge(GroupbyItem o)
	{
		throw new RuntimeException("not support");
	}
	

	public boolean isrecordcount(){
		return false;
	}
	
	public NamedList toNamedList()
	{
		NamedList rtn=new NamedList();
		rtn.add("count", value);
		if(isFinalResult)
		{
			NamedList stat=new NamedList();
			stat.add("sum", 0d);
			stat.add("max", 0d);
			stat.add("min", 0d);
			stat.add("dist", 0d);
			rtn.add("higo_empty_s", stat);
		}else{
			rtn.add("rc", 2);
			rtn.add("docid", this.docid);
			if(colVal!=null)
			{
				rtn.add("sortStr", colVal);
			}
		}
		return rtn;
	}
	

	public String getKey() {
		return key;
	}
	
	public double getCompareValue()
	{
		return this.value;
	}
	
	public Long getValue() {
		return (long)this.value;
	}

	@Override
	public void setCross(String[] crossFs, String[] distFS) {
		
	}
	
	private static LinkedBlockingQueue<SelectDetailRow> instanceCache=new LinkedBlockingQueue<SelectDetailRow>();

	public static SelectDetailRow INSTANCE(int docid, double value)
	{
		SelectDetailRow rtn=instanceCache.poll();
		if(rtn==null)
		{
			rtn=new SelectDetailRow();
		}
		
		rtn.docid=docid;
		rtn.value=value;
		rtn.key=null;
		rtn.isFinalResult=false;
		rtn.colVal=null;
		
		return rtn;
	}
	
	public static void CLEAN()
	{
		LOG.info("instanceCache.size="+instanceCache);
		int sz=Math.min(UniqConfig.ShardMaxGroups(), 640000);

		if(instanceCache.size()>sz)
		{
			int left=instanceCache.size()-sz+1;
			for(int i=0;i<left;i++)
			{
				instanceCache.poll();
			}
		}
	}
	
	public static void FREE(SelectDetailRow o)
	{
		instanceCache.add(o);
	}

	@Override
	public int compareTo(SelectDetailRow o) {
		return Double.compare(this.docid, o.docid);

	}

}
