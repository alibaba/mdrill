package com.alipay.bluewhale.core.cluster;

import java.io.Serializable;

import com.alipay.bluewhale.core.daemon.StatusType;


 /**
  * topology的状态
  * type: topology包含四种类型：active、inactive、killed or rebalancing
  * killTimeSecs： 当不是killed状态的时候，为-1，无作用，killed状态的时候，表示延迟多长时间执行kill操作
  * delaySecs：表示rebalancing状态的时候，延迟多长时间执行rebalance操作
  * oldStatus: 存储上一个topology的状态，比如说：rebalance的时候，会执行一些操作，但是最后还是会设置topology的状态为上一个状态
  */
public class StormStatus  implements Serializable{

   
    private static final long serialVersionUID = 1L;
    private StatusType type;
    private int killTimeSecs;
    private int delaySecs;
    private StormStatus oldStatus = null;

    public StormStatus(int killTimeSecs, StatusType type, StormStatus oldStatus) {
	this.type = type;
	this.killTimeSecs = killTimeSecs;
	this.oldStatus = oldStatus;
    }

    public StormStatus(int killTimeSecs, StatusType type) {
	this.type = type;
	this.killTimeSecs = killTimeSecs;
    }

    public StormStatus(StatusType type, int delaySecs, StormStatus oldStatus) {
	this.type = type;
	this.delaySecs = delaySecs;
	this.oldStatus = oldStatus;
    }

    public StormStatus(StatusType type) {
	this.type = type;
	this.killTimeSecs = -1;
	this.delaySecs = -1;
    }

    public StatusType getStatusType() {
	return type;
    }

    public void setStatusType(StatusType type) {
	this.type = type;
    }

    public Integer getKillTimeSecs() {
	return killTimeSecs;
    }

    public void setKillTimeSecs(int killTimeSecs) {
	this.killTimeSecs = killTimeSecs;
    }

    public Integer getDelaySecs() {
	return delaySecs;
    }

    public void setDelaySecs(int delaySecs) {
	this.delaySecs = delaySecs;
    }

    public StormStatus getOldStatus() {
	return oldStatus;
    }

    public void setOldStatus(StormStatus oldStatus) {
	this.oldStatus = oldStatus;
    }

    @Override
    public boolean equals(Object base) {
	if (base instanceof StormStatus
		&& ((StormStatus) base).getStatusType().equals(getStatusType())
		&& ((StormStatus) base).getKillTimeSecs() == getKillTimeSecs()
		&& ((StormStatus) base).getDelaySecs().equals(getDelaySecs())) {
	    return true;
	}
	return false;
    }

    @Override
    public int hashCode() {
	return this.getStatusType().hashCode() + this.getKillTimeSecs().hashCode()
		+ this.getDelaySecs().hashCode();
    }

}