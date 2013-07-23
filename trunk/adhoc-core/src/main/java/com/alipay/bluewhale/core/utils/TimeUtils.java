package com.alipay.bluewhale.core.utils;

import backtype.storm.utils.Time;
/**
 * 与时间相关的操作封装
 * @author yannian
 *
 */
public class TimeUtils {

    public static int current_time_secs() {
        return (int) (Time.currentTimeMillis() / 1000);
    }

    public static int time_delta(int time_secs) {
        return current_time_secs() - time_secs;
    }

    public static long time_delta_ms(long time_ms) {
        return System.currentTimeMillis() - time_ms;
    }
}
