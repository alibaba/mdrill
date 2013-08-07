package com.alipay.bluewhale.core.stats;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * spout与bolt都共用的统计结果对象
 * 
 * @author yannian
 * 
 */
public class CommonStatsData implements Serializable {

	private static final long serialVersionUID = -2811225938044543165L;
	private HashMap<Integer, Object> emitted = new HashMap<Integer, Object>();
	private HashMap<Integer, Object> transferred = new HashMap<Integer, Object>();
	private int rate = 0;

	public String getType() {
		return "common";
	}

	@SuppressWarnings("unchecked")
	public Map<String, Map<String, Long>> get_emitted() {
		Map<String, Map<String, Long>> rtn = new HashMap<String, Map<String, Long>>();
		for (Entry<Integer, Object> times : emitted.entrySet()) {
			Map<Object, Long> val = (Map<Object, Long>) times.getValue();
			String key = Stats.parseTimeKey(times.getKey());
			Map<String, Long> stats = new HashMap<String, Long>();
			for (Entry<Object, Long> stat : val.entrySet()) {
				stats.put(String.valueOf(stat.getKey()), stat.getValue());
			}

			rtn.put(key, stats);
		}
		return rtn;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Map<String, Long>> get_transferred() {
		Map<String, Map<String, Long>> rtn = new HashMap<String, Map<String, Long>>();
		for (Entry<Integer, Object> times : transferred.entrySet()) {
			Map<Object, Long> val = (Map<Object, Long>) times.getValue();
			String key = Stats.parseTimeKey(times.getKey());
			Map<String, Long> stats = new HashMap<String, Long>();
			for (Entry<Object, Long> stat : val.entrySet()) {
				stats.put((String) stat.getKey(), stat.getValue());
			}

			rtn.put(key, stats);
		}
		return rtn;
	}

	public HashMap<Integer, Object> getEmitted() {
		return emitted;
	}

	public void setEmitted(HashMap<Integer, Object> emitted) {
		this.emitted = emitted;
	}

	public HashMap<Integer, Object> getTransferred() {
		return transferred;
	}

	public void setTransferred(HashMap<Integer, Object> transferred) {
		this.transferred = transferred;
	}

	public int getRate() {
		return rate;
	}

	public void setRate(int rate) {
		this.rate = rate;
	}

	@Override
	public boolean equals(Object assignment) {
		if (assignment instanceof CommonStatsData
				&& ((CommonStatsData) assignment).get_emitted().equals(emitted)
				&& ((CommonStatsData) assignment).transferred
						.equals(transferred)
				&& ((CommonStatsData) assignment).getRate()==rate
		) {
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return emitted.hashCode() + transferred.hashCode();
	}

	@Override
	public String toString() {
		return "CommonStatsData [emitted=" + emitted + ", transferred="
				+ transferred + ", rate=" + rate + "]";
	}
	
	

}
