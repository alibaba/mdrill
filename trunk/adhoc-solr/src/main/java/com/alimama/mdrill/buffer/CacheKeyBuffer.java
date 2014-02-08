package com.alimama.mdrill.buffer;

import java.util.Arrays;

import org.apache.solr.core.SolrResourceLoader.PartionKey;


public class CacheKeyBuffer {
	private String[] filelist;
	private  String uniq;
	private long ts;
	private PartionKey p;
	public CacheKeyBuffer(String[] filelist, String uniq, long ts, PartionKey p) {
		super();
		this.filelist = filelist;
		this.uniq = uniq;
		this.ts = ts;
		this.p = p;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(filelist);
		result = prime * result + ((p == null) ? 0 : p.hashCode());
		result = prime * result + (int) (ts ^ (ts >>> 32));
		result = prime * result + ((uniq == null) ? 0 : uniq.hashCode());
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
		CacheKeyBuffer other = (CacheKeyBuffer) obj;
		if (!Arrays.equals(filelist, other.filelist))
			return false;
		if (p == null) {
			if (other.p != null)
				return false;
		} else if (!p.equals(other.p))
			return false;
		if (ts != other.ts)
			return false;
		if (uniq == null) {
			if (other.uniq != null)
				return false;
		} else if (!uniq.equals(other.uniq))
			return false;
		return true;
	}
	

	@Override
	public String toString() {
		return "CacheKeyBuffer [filelist=" + Arrays.toString(filelist)
				+ ", uniq=" + uniq + ", ts=" + ts + ", p=" + p + "]";
	}

}
