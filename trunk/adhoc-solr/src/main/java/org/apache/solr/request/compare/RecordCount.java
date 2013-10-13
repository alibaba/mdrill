package org.apache.solr.request.compare;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.zip.CRC32;

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
	public RecordCount(ArrayList<Object> nst)
	{
		String oversize=(String) nst.get(0);
		this.isoversize="yes".equals(oversize);
		this.maxUniqSize=(Integer) nst.get(2);
		this.uniq.addAll((Collection<? extends ArrayList<Integer>>) nst.get(3));
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

	
	
	public ArrayList<Object> toNamedList()
	{
		ArrayList<Object> rtn=new ArrayList<Object>();
		rtn.add(0, this.isoversize?"yes":"no");//"key"
		rtn.add(1, 0);//顺序不能乱改
		rtn.add(2, maxUniqSize);
		rtn.add(3, this.uniq);
		
		return rtn;
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
