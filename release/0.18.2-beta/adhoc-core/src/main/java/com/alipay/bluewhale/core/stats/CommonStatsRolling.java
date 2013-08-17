package com.alipay.bluewhale.core.stats;

import java.io.Serializable;

import com.alipay.bluewhale.core.stats.RollingWindow.RollingWindowSet;
/**
 * spout与bolt共用的统计对象 rollingwindowset的组合
 * @author yannian
 *
 */
public class CommonStatsRolling  implements Serializable{

    private static final long serialVersionUID = 1L;
	public RollingWindowSet emitted;
	public RollingWindowSet transferred;
	public Integer rate;
}
