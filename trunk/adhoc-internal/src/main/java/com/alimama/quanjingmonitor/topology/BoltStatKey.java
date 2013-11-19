package com.alimama.quanjingmonitor.topology;

import java.io.Serializable;
import java.util.Arrays;


public class BoltStatKey implements Serializable{

	private static final long serialVersionUID = 1L;
	public Object[] list;
	public boolean isPidKey=true;


	public BoltStatKey(int size) {
		list = new Object[size];
	}
	@Override
	public String toString() {
		return "BoltStatKey [list=" + Arrays.toString(list) + ", isPidKey="
				+ isPidKey + "]";
	}



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isPidKey ? 1231 : 1237);
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
		if (isPidKey != other.isPidKey)
			return false;
		if (!Arrays.equals(list, other.list))
			return false;
		return true;
	}



}
