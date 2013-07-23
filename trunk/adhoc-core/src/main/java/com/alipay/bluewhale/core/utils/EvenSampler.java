package com.alipay.bluewhale.core.utils;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
/**
 * 抽样器的实现，特点是在freq频率内肯定会被执行一次，抽样的比较均匀
 * 目前用于task里的tuple统计
 * @author yannian
 *
 */
public class EvenSampler {
    private volatile int freq;
    private AtomicInteger i=new AtomicInteger(-1);
    private volatile int target;
    private Random r = new Random();

    public EvenSampler(int freq) {
        this.freq = freq;
        this.target = r.nextInt(freq);
    }

    public boolean getResult() {
    	i.incrementAndGet();
        if (i.get() >= freq) {
            target = r.nextInt(freq);
            i.set(0);
        }
        if (i.get()==target) {
            return true;
        }
        return false;
    }
}
