package com.alipay.bluewhale.core.stats.method;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


public class StatFunction {
    public static void incr_val(Map<Object, Long> map, Object key, long amt) {
	long value = 0l;
	if (map.containsKey(key)) {
	    value = map.get(key);
	}
	value += amt;
	map.put(key, value);
    }

    public static void incr_val(Map<Object, Long> map, Object key) {
	incr_val(map, key, 1);
    }

    public static synchronized Pair update_avg(Pair curr, long val) {
	curr.first += val;
	curr.second++;
	return curr;
    }

    public static Pair merge_avg(List<Pair> avg) {
	Pair rtn = new Pair();
	for (Pair p : avg) {
	    rtn.first += p.first;
	    rtn.second += p.second;
	}
	return rtn;
    }

    public static double extract_avg(Pair p) {
	if(p.second==0)
	{
	    return 0d;
	}
	return (p.first * 1.0) / p.second;
    }

    public static void update_keyed_avg(Map<Object, Pair> map, Object key,long val) {
	Pair p = map.get(key);
	if (p == null) {
	    p = new Pair();
	}
	update_avg(p, val);
	map.put(key, p);
    }

    public static Pair merge_keyed_avg(List<Pair> avg) {
	return merge_avg(avg);
    }

    public static Map<Object, Double> extract_key_avg(Map<Object, Pair> map) {
	Map<Object, Double> rtn = new HashMap<Object, Double>();
	if(map!=null)
	{
        	for (Entry<Object, Pair> e : map.entrySet()) {
        	    rtn.put(e.getKey(), extract_avg(e.getValue()));
        	}
	}
	return rtn;
    }

    public static Map<Object, Long> counter_extract(Map<Object, Long> v) {
	if (v == null) {
	    return new HashMap<Object, Long>();
	}
	return v;
    }

}
