package com.alipay.bluewhale.core.stats;

import com.alipay.bluewhale.core.stats.RollingWindow.RollingWindowSet;

/**
 * bolt的统计对象 rollingwindowset的组合
 * 
 * @author yannian
 * 
 */
public class BoltTaskStatsRolling extends BaseTaskStatsRolling {

	private static final long serialVersionUID = -4995632339230845499L;
	private CommonStatsRolling common;
	private RollingWindowSet acked;
	private RollingWindowSet failed;
	private RollingWindowSet process_latencies;

	public BoltTaskStatsRolling(CommonStatsRolling common,
			RollingWindowSet acked, RollingWindowSet failed,
			RollingWindowSet process_latencies) {
		this.common = common;
		this.acked = acked;
		this.failed = failed;
		this.process_latencies = process_latencies;
	}

	public CommonStatsRolling getCommon() {
		return common;
	}

	public void setCommon(CommonStatsRolling common) {
		this.common = common;
	}

	public RollingWindowSet getAcked() {
		return acked;
	}

	public void setAcked(RollingWindowSet acked) {
		this.acked = acked;
	}

	public RollingWindowSet getFailed() {
		return failed;
	}

	public void setFailed(RollingWindowSet failed) {
		this.failed = failed;
	}

	public RollingWindowSet getProcess_latencies() {
		return process_latencies;
	}

	public void setProcess_latencies(RollingWindowSet process_latencies) {
		this.process_latencies = process_latencies;
	}

	public String getType() {
		return "bolt";
	}
}
