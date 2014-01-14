package com.alimama.mdrillImport;

import java.io.Serializable;
import java.util.Arrays;

public class BoltStatKey implements Serializable{

	private static final long serialVersionUID = 1L;
	public Object[] list;
	
	public long groupts=0;
	
	public BoltStatKey(Object[] list, long groupts) {
		super();
		this.list = list;
		this.groupts = groupts;
	}

	public Object[] getList() {
		return list;
	}
	public void setList(Object[] list) {
		this.list = list;
	}
	public long getGroupts() {
		return groupts;
	}
	public void setGroupts(long groupts) {
		this.groupts = groupts;
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
		BoltStatKey other = (BoltStatKey) obj;
		if (groupts != other.groupts)
			return false;
		if (!Arrays.equals(list, other.list))
			return false;
		return true;
	}
	





}
