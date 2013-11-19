package com.alimama.quanjingmonitor.topology;

public class PvSpoutStat {

	@Override
	public String toString() {
		return "PvSpoutStat [index=" + index + ", record=" + record
				+ ", pidnull=" + pidnull + ", processnull=" + processnull
				+ ", interfacenull=" + interfacenull + ", lastsize=" + lastsize
				+ ", groupnameempty=" + groupnameEmpty
				+ ", lastsize_group_norep=" + lastsize_group_norep
				+ ", lastsize_host_norep=" + lastsize_host_norep + "]";
	}

	public volatile long index=0;
	public volatile long record=0;
	public volatile long pidnull=0;
	public volatile long processnull=0;

	public volatile long interfacenull=0;
	public volatile int lastsize=0;
	public volatile int groupnameEmpty=0;
	public volatile int lastsize_group_norep=0;
	public volatile int lastsize_host_norep=0;

	public void reset()
	{
		index=0;

		record=0;

		pidnull=0;

		processnull=0;

		interfacenull=0;
		lastsize_group_norep=0;
		lastsize_host_norep=0;
		lastsize=0;
		groupnameEmpty=0;

	}
}
