package com.alipay.bluewhale.core.stats;

import java.io.Serializable;

public abstract class BaseTaskStatsRolling implements Serializable {

	private static final long serialVersionUID = 4277428394466722940L;

	public abstract String getType();
}
