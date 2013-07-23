package com.alipay.bluewhale.core.utils;

import java.util.ArrayList;

/**
 * 用于task的shuffle类型的grouper,其特点是分配的比较均匀，不会像随机那种，有可能出现不均衡的现象
 * 
 * 假设有100个task，向另外100个task发送信息 刚启动的瞬间  排列在第一个的task倒霉了
 * @author yannian
 *
 */
public class RandomRange {
    private ArrayList<Integer> rr;
    private Integer amt;

    public RandomRange(int amt) {
        this.amt = amt;
        this.rr = rotating_random_range(amt);
    }

    public Integer nextInt() {
        return this.acquire_random_range_id();
    }

    private ArrayList<Integer> rotating_random_range(int amt) {

        ArrayList<Integer> range = new ArrayList<Integer>();
        for (int i = 0; i < amt; i++) {
            range.add(i);
        }

        ArrayList<Integer> rtn = new ArrayList<Integer>();
        for (int i = 0; i < amt; i++) {
            int index = (int) (Math.random() * range.size());
            rtn.add(range.remove(index));
        }

        return rtn;
    }

    private synchronized int acquire_random_range_id() {
        int ret = this.rr.remove(0);
        if (this.rr.size() == 0) {
            this.rr.addAll(rotating_random_range(this.amt));
        }
        return ret;
    }
}
