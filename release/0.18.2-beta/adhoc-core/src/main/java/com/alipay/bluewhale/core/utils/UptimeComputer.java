package com.alipay.bluewhale.core.utils;

/**
 * 用于task的zk心跳，返回task目前已经运行了多久
 * @author yannian
 *
 */
public class UptimeComputer {
    int start_time = 0;

    public UptimeComputer() {
        start_time = TimeUtils.current_time_secs();
    }

    public synchronized int uptime() {
        return TimeUtils.time_delta(start_time);
    }
}
