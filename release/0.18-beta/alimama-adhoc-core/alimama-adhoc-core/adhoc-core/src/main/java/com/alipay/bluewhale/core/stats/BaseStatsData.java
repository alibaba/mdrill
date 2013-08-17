package com.alipay.bluewhale.core.stats;

import java.io.Serializable;

import backtype.storm.generated.TaskSpecificStats;
import backtype.storm.generated.TaskStats;

/**
 * 统计的基类
 * 
 * @author yannian
 * 
 */
public abstract class BaseStatsData implements Serializable {
    private static final long serialVersionUID = 1L;

    public abstract String getType();

    public abstract TaskStats getTaskStats();

    public abstract TaskSpecificStats getThirftstats();

}