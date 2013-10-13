package org.apache.solr.request.compare;

import java.util.ArrayList;
import java.util.Map;
import java.util.zip.CRC32;

public class ColumnKey {
	private boolean iscrc=false;
	public boolean isIscrc() {
		return iscrc;
	}


	public long getCrc() {
		return crc;
	}


	public ArrayList<Object> getSort() {
		return sort;
	}

	private long crc=0;
	private ArrayList<Object> sort;
	private String key;
	public String getKey() {
		return key;
	}


	
	public void ToCrcSet(MergerGroupByGroupbyRowCompare cmp,Map<Long,String> cache)
	{
		this.sort=cmp.getCmpobj().getCompareValue(this);
		this.crc= this.getkeyCrc();
		cache.put(this.crc, this.key);
		this.key=null;
		this.iscrc=true;
	}
	
	public void ToCrcSet(Map<Long,String> cache)
	{
		this.sort=new ArrayList<Object>();
		this.crc= this.getkeyCrc();
		cache.put(this.crc, this.key);
		this.key=null;
		this.iscrc=true;
	}
	
	
	public long getkeyCrc()
	{
		CRC32 crc32 = new CRC32();
		crc32.update(String.valueOf(this.key).getBytes());
		return crc32.getValue();
	}
	public ArrayList<Object> toNamedList()
	{
		if(iscrc)
		{
			ArrayList<Object> rtn=new ArrayList<Object>();
			rtn.add(0,crc);
			rtn.add(1,sort);
			return rtn;
		}

		ArrayList<Object> rtn=new ArrayList<Object>();
		rtn.add(0,key);
		return rtn;
	}
	
	public ColumnKey(ArrayList<Object> d)
	{
		if(d.size()>1)
		{
			this.iscrc=true;
			this.crc=(Long) d.get(0);
			this.sort=(ArrayList<Object>) d.get(1);
		}else{
			this.iscrc=false;
			this.key=(String) d.get(0);
		}
	}
	
	
	public ColumnKey(String key) {
		super();
		this.key = key;
		this.iscrc=false;
	}
	
	
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (crc ^ (crc >>> 32));
		result = prime * result + (iscrc ? 1231 : 1237);
		result = prime * result + ((key == null) ? 0 : key.hashCode());
//		result = prime * result + ((sort == null) ? 0 : sort.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ColumnKey other = (ColumnKey) obj;
		if (crc != other.crc)
			return false;
		if (iscrc != other.iscrc)
			return false;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
//		if (sort == null) {
//			if (other.sort != null)
//				return false;
//		} else if (!sort.equals(other.sort))
//			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "ColumnKey [iscrc=" + iscrc + ", crc=" + crc + ", sort=" + sort
				+ ", key=" + key + "]";
	}

}
