package com.alipay.bluewhale.core.cluster;

import java.io.Serializable;

/**
 * storm(topology)的基本信息
 * stormName：topology's name
 * lanchTimeSecs: 发起时间
 * status：状态信息
 */

public class StormBase implements Serializable {

    private static final long serialVersionUID = 1L;
	private String stormName;
	private int lanchTimeSecs;
	private StormStatus status;

	public StormBase(String stormName, int lanchTimeSecs, StormStatus status){
		this.stormName = stormName;
		this.lanchTimeSecs = lanchTimeSecs;
		this.status = status;
	}
	public String getStormName() {
		return stormName;
	}

	public void setStormName(String stormName) {
		this.stormName = stormName;
	}

	public int getLanchTimeSecs() {
		return lanchTimeSecs;
	}

	public void setLanchTimeSecs(int lanchTimeSecs) {
		this.lanchTimeSecs = lanchTimeSecs;
	}

	public StormStatus getStatus() {
		return status;
	}

	public void setStatus(StormStatus status) {
		this.status = status;
	}
	
	@Override
	public boolean equals(Object base){
		if (base instanceof  StormBase && ((StormBase)base).getStormName().equals(stormName) 
				&& ((StormBase)base).getLanchTimeSecs()== lanchTimeSecs
				&& ((StormBase)base).getStatus().equals(status)){
			return true;
		}
		return false;
	}
	
	@Override
	public int  hashCode()
	{
		return this.status.hashCode() + this.lanchTimeSecs + this.stormName.hashCode();
	}

}
