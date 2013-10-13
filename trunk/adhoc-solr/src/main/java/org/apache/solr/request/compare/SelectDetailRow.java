package org.apache.solr.request.compare;


import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.log4j.Logger;
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
	private ColumnKey key;//col1@col2@col3
	public void setKey(ColumnKey key) {
		this.key = key;
	}

	public int value;//compare field value
	public String colVal=null;//compare field value
	

	
	public SelectDetailRow(ArrayList<Object> nst)
	{
		this.key=new ColumnKey((ArrayList<Object>)nst.get(0));
		this.docid= (Integer) nst.get(2);
		this.colVal=(String) nst.get(3);
		this.value=(Integer) nst.get(4);
	}
	
	public void ToCrcSet(Map<Long,String> cache)
	{
		this.key.ToCrcSet(cache);
	}
	
	public void shardsMerge(GroupbyItem o)
	{
		throw new RuntimeException("not support");
	}
	

	public boolean isrecordcount(){
		return false;
	}
	
	public ArrayList<Object> toNamedList()
	{
		
		ArrayList<Object> rtn=new ArrayList<Object>();
		rtn.add(0, this.key.toNamedList());//"key"
		rtn.add(1, 2);//"rc"
		rtn.add(2, this.docid);//"rc"
		rtn.add(3, colVal==null?"":colVal);//"rc"
		rtn.add(4,this.value);//"count"

		return rtn;
	}
	

	public ColumnKey getKey() {
		return key;
	}
	
	public int getCompareValue()
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

	public static SelectDetailRow INSTANCE(int docid, int value)
	{
		SelectDetailRow rtn=instanceCache.poll();
		if(rtn==null)
		{
			rtn=new SelectDetailRow();
		}
		
		rtn.docid=docid;
		rtn.value=value;
		rtn.key=null;
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
