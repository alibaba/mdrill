package com.alimama.mdrillImport;

import java.io.Serializable;
import java.util.Arrays;

public class BoltStatVal implements Serializable{
	

	private static final long serialVersionUID = 1L;

	public Number[] list;
	public long groupts=0;

	@Override
	public String toString() {
		return "BoltStatVal [list=" + Arrays.toString(list) + ", groupts="
				+ groupts + "]";
	}

	public long getGroupts() {
		return groupts;
	}

	public BoltStatVal(Number[] list,long groupts) {
		this.list = list;
		this.groupts=groupts;
	}
	
	private Number add(Number a,Number b)
	{
		if(a instanceof Integer)
		{
			return a.intValue()+b.intValue();
		}else if(a instanceof Long)
		{
			return a.longValue()+b.longValue();
		}
		else if(a instanceof Double)
		{
			return a.doubleValue()+b.doubleValue();
		}
		else if(a instanceof Float)
		{
			return a.floatValue()+b.floatValue();
		}else{
			return a.floatValue()+b.floatValue();

		}
	}
	
	public void merger(BoltStatVal v)
	{
		for(int i=0;i<this.list.length&&i<v.list.length;i++)
		{
			this.list[i]=this.add(v.list[i],this.list[i]);
		}
		this.groupts=Math.max(this.groupts, v.groupts);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (groupts ^ (groupts >>> 32));
		result = prime * result + Arrays.hashCode(list);
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
		BoltStatVal other = (BoltStatVal) obj;
		if (groupts != other.groupts)
			return false;
		if (!Arrays.equals(list, other.list))
			return false;
		return true;
	}

}
