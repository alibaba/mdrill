package com.alimama.mdrillImport;

import java.io.Serializable;
import java.util.Arrays;

public class BoltStatVal implements Serializable{
	

	private static final long serialVersionUID = 1L;
	public Number[] list;
	public BoltStatVal(Number[] list) {
		this.list = list;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		if (!Arrays.equals(list, other.list))
			return false;
		return true;
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
	}

}
