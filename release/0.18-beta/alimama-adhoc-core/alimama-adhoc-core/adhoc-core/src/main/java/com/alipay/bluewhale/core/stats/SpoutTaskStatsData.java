package com.alipay.bluewhale.core.stats;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import backtype.storm.generated.SpoutStats;
import backtype.storm.generated.TaskSpecificStats;
import backtype.storm.generated.TaskStats;

/**
 * spout任务的统计结果
 * 
 * @author yannian
 * 
 */
public class SpoutTaskStatsData extends BaseStatsData {

	private static final long serialVersionUID = -2845809856793844845L;
	private CommonStatsData common = new CommonStatsData();
	private HashMap<Integer, Object> acked = new HashMap<Integer, Object>();
	private HashMap<Integer, Object> failed = new HashMap<Integer, Object>();
	private HashMap<Integer, Object> complete_latencies = new HashMap<Integer, Object>();

	public SpoutTaskStatsData(CommonStatsData common,
			HashMap<Integer, Object> acked, HashMap<Integer, Object> failed,
			HashMap<Integer, Object> complete_latencies) {
		this.common = common;
		this.acked = acked;
		this.failed = failed;
		this.complete_latencies = complete_latencies;
	}

	public String getType() {
		return "spout";
	}

	public TaskStats getTaskStats() {
		return new TaskStats(common.get_emitted(), common.get_transferred(),
				getThirftstats());
	}

	public TaskSpecificStats getThirftstats() {
		return TaskSpecificStats.spout(new SpoutStats(get_acked(), get_fail(),
				get_complete_latencies()));
	}

	@SuppressWarnings("unchecked")
	private Map<String, Map<String, Long>> get_acked() {
		Map<String, Map<String, Long>> rtn = new HashMap<String, Map<String, Long>>();
		for (Entry<Integer, Object> times : acked.entrySet()) {
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

	@SuppressWarnings("unchecked")
	private Map<String, Map<String, Long>> get_fail() {
		Map<String, Map<String, Long>> rtn = new HashMap<String, Map<String, Long>>();
		for (Entry<Integer, Object> times : failed.entrySet()) {
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

	@SuppressWarnings("unchecked")
	private Map<String, Map<String, Double>> get_complete_latencies() {
		Map<String, Map<String, Double>> rtn = new HashMap<String, Map<String, Double>>();
		for (Entry<Integer, Object> times : complete_latencies.entrySet()) {
			Map<Object, Double> val = (Map<Object, Double>) times.getValue();
			String key = Stats.parseTimeKey(times.getKey());
			Map<String, Double> stats = new HashMap<String, Double>();
			for (Entry<Object, Double> stat : val.entrySet()) {
				stats.put((String) stat.getKey(), stat.getValue());
			}

			rtn.put(key, stats);
		}
		return rtn;
	}

	public CommonStatsData getCommon() {
		return common;
	}

	public void setCommon(CommonStatsData common) {
		this.common = common;
	}

	public HashMap<Integer, Object> getAcked() {
		return acked;
	}

	public void setAcked(HashMap<Integer, Object> acked) {
		this.acked = acked;
	}

	public HashMap<Integer, Object> getFailed() {
		return failed;
	}

	public void setFailed(HashMap<Integer, Object> failed) {
		this.failed = failed;
	}

	public HashMap<Integer, Object> getComplete_latencies() {
		return complete_latencies;
	}

	public void setComplete_latencies(
			HashMap<Integer, Object> complete_latencies) {
		this.complete_latencies = complete_latencies;
	}

	@Override
	public boolean equals(Object assignment) {
		if (assignment instanceof SpoutTaskStatsData
				&& ((SpoutTaskStatsData) assignment).getCommon().equals(common)
				&& ((SpoutTaskStatsData) assignment).get_acked().equals(acked)
				&& ((SpoutTaskStatsData) assignment).get_fail().equals(failed)
				&& ((SpoutTaskStatsData) assignment).getComplete_latencies().equals(complete_latencies)

		) {
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return common.hashCode() + acked.hashCode() + failed.hashCode()
				+ complete_latencies.hashCode();
	}

	@Override
	public String toString() {
		return "SpoutTaskStatsData [common=" + common + ", acked=" + acked
				+ ", failed=" + failed + ", complete_latencies="
				+ complete_latencies + "]";
	}
	
	
}
