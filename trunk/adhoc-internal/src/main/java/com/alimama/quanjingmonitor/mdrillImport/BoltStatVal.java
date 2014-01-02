package com.alimama.quanjingmonitor.mdrillImport;

import java.io.Serializable;
import java.util.Arrays;

public class BoltStatVal implements Serializable{
	private static final long serialVersionUID = 1L;
	public double[] list;
	public BoltStatVal(double[] list) {
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
	public void merger(BoltStatVal v)
	{
		for(int i=0;i<this.list.length&&i<v.list.length;i++)
		{
			this.list[i]+=v.list[i];
		}
	}

}
