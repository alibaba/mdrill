package com.alimama.mdrill.topology;

import com.alipay.bluewhale.core.cluster.ShardsState;

public class SolrStartJettyStat {
	long setuptime = System.currentTimeMillis();

	long lasttime = System.currentTimeMillis();
	private Object statlock = new Object();
	private ShardsState stat = ShardsState.UINIT;

	public void setStat(ShardsState s) {
		synchronized (statlock) {
			this.stat = s;
		}
	}

	public ShardsState getStat() {
		synchronized (statlock) {
			return this.stat;
		}
	}

	public Long getLastTime() {
		synchronized (statlock) {
			return this.lasttime;
		}
	}
	
	public Boolean isTimeout(Long timespan) {
		long nowtime = System.currentTimeMillis();
		if (nowtime - this.getLastTime() > timespan) {
			return true;
		}
		return false;
	}
	
	public Long getSetupTime() {
		synchronized (statlock) {
			return this.setuptime;
		}
	}

	public void setLastTime(Long t) {
		synchronized (statlock) {
			this.lasttime = t;
		}
	}
}
