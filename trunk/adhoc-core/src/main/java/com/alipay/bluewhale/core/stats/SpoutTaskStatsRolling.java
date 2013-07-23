package com.alipay.bluewhale.core.stats;

import com.alipay.bluewhale.core.stats.RollingWindow.RollingWindowSet;

/**
 * spout的统计对象 rollingwindowset的组合
 * 
 * @author yannian
 * 
 */
public class SpoutTaskStatsRolling extends BaseTaskStatsRolling {
	private static final long serialVersionUID = 3371333738303727073L;	
	private CommonStatsRolling common;
	private RollingWindowSet acked;
	private RollingWindowSet failed;
	private RollingWindowSet complete_latencies;

	public SpoutTaskStatsRolling(CommonStatsRolling common,
			RollingWindowSet acked, RollingWindowSet failed,
			RollingWindowSet complete_latencies) {
		super();
		this.common = common;
		this.acked = acked;
		this.failed = failed;
		this.complete_latencies = complete_latencies;
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

	public RollingWindowSet getComplete_latencies() {
		return complete_latencies;
	}

	public void setComplete_latencies(RollingWindowSet complete_latencies) {
		this.complete_latencies = complete_latencies;
	}

	public String getType() {
		return "spout";
	}
}
