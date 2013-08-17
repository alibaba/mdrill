package org.apache.solr.request.compare;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.zip.CRC32;

import org.apache.solr.common.util.NamedList;

/**
 * groupby 分页的总记录数实现
 * @author yannian.mu
 */
public class RecordCount implements GroupbyItem{
	private HashSet<ArrayList<Integer>> uniq=new HashSet<ArrayList<Integer>>();
	private boolean isFinalResult=false;
	private Integer maxUniqSize=Integer.MAX_VALUE;
	private boolean isoversize=false;
	public RecordCount() {}
	public RecordCount(String key,NamedList nst)
	{
		String oversize=(String) nst.get("isoversize");
		this.isoversize="yes".equals(oversize);
		this.maxUniqSize=(Integer) nst.get("maxUniqSize");
		this.uniq.addAll((Collection<? extends ArrayList<Integer>>) nst.get("dist"));
	}
	
	public void shardsMerge(GroupbyItem g)
	{	
		RecordCount o=(RecordCount)g;
		if(o.isoversize)
		{
			this.isoversize=true;
		}
		
		if(this.maxUniqSize>o.maxUniqSize)
		{
			this.maxUniqSize=o.maxUniqSize;
		}
		
		this.mergeUniq(o);
	}
	
	private void mergeUniq(RecordCount o)
	{
		if(!this.isoversize)
		{
			int index=0;
			for(ArrayList<Integer> s:o.uniq)
			{
				this.uniq.add(s);
				if(index%100==0&&this.uniq.size()>this.maxUniqSize)
				{
					this.isoversize=true;
					break;
				}
				index++;
			}
		}
		
		if(this.isoversize)
		{
			this.isoversize=true;
			this.uniq.clear();
		}
	}
	
	public boolean isFinalResult() {
		return isFinalResult;
	}

	public void setFinalResult(boolean isFinalResult) {
		this.isFinalResult = isFinalResult;
	}
	
	public Integer getMaxUniqSize() {
		return maxUniqSize;
	}

	public void setMaxUniqSize(Integer maxUniqSize) {
		this.maxUniqSize = maxUniqSize;
	}
	
	public void setCrcRecord(String newparentGroup)
	{
		int len = newparentGroup.length();
		CRC32 crc32 = new CRC32();
		crc32.update(new String(newparentGroup).getBytes());
		int crcvalue = (int) crc32.getValue();
		int hashCode = newparentGroup.hashCode();
		
		ArrayList<Integer> key=new ArrayList<Integer>(3);
		key.add(crcvalue);
		key.add(len);
		key.add(hashCode);
		this.adduniq(key);
	}
	
	private void adduniq(ArrayList<Integer> key)
	{
		if(this.isoversize)
		{
			return ;
		}
		
		this.uniq.add(key);
		if(this.uniq.size()>getMaxUniqSize())
		{
			this.uniq.clear();
			this.isoversize=true;
		}
	}
	
	
	public void setIsoversize(boolean isoversize) {
		this.isoversize = isoversize;
	}

	private NamedList toNamelistForMerge(NamedList rtn)
	{
		rtn.add("isoversize", this.isoversize?"yes":"no");
		rtn.add("maxUniqSize", maxUniqSize);
		rtn.add("dist", this.uniq);
		rtn.add("rc", 0);
		return rtn;
	}
	
	public NamedList toNamelistForResult(NamedList rtn)
	{
		rtn.add("recordcount", this.getValue());	
		rtn.add("count", Integer.MAX_VALUE);	
		return rtn;
	}
	
	public NamedList toNamedList()
	{
		NamedList rtn=new NamedList();
		if(!isFinalResult)
		{
			return toNamelistForMerge(rtn);
		}
		return toNamelistForResult(rtn);
	}
	

	public String getKey() {
		return "recordcount";
	}
	
	public boolean isrecordcount(){
		return true;
	}


	
	public Long getValue() {
		return (long) (this.isoversize?this.maxUniqSize:this.uniq.size());
	}
	@Override
	public void setCross(String[] crossFs, String[] distFS) {
		
	}
	
	

}
