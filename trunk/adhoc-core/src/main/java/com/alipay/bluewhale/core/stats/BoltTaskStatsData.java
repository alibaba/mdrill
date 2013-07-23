package com.alipay.bluewhale.core.stats;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import backtype.storm.generated.BoltStats;
import backtype.storm.generated.GlobalStreamId;
import backtype.storm.generated.TaskSpecificStats;
import backtype.storm.generated.TaskStats;

/**
 * bolt任务的统计结果
 * 
 * @author yannian
 * 
 */
public class BoltTaskStatsData extends BaseStatsData{

	private static final long serialVersionUID = -6250304684824361807L;
	private CommonStatsData common = new CommonStatsData();
	private HashMap<Integer, Object> acked = new HashMap<Integer, Object>();
	private HashMap<Integer, Object> failed = new HashMap<Integer, Object>();
	private HashMap<Integer, Object> process_latencies = new HashMap<Integer, Object>();

	public String getType() {
		return "bolt";
	}

	public BoltTaskStatsData(CommonStatsData common,
			HashMap<Integer, Object> acked, HashMap<Integer, Object> failed,
			HashMap<Integer, Object> process_latencies) {
		super();
		this.common = common;
		this.acked = acked;
		this.failed = failed;
		this.process_latencies = process_latencies;
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

	public HashMap<Integer, Object> getProcess_latencies() {
		return process_latencies;
	}

	public void setProcess_latencies(HashMap<Integer, Object> process_latencies) {
		this.process_latencies = process_latencies;
	}

	public TaskStats getTaskStats() {
		return new TaskStats(common.get_emitted(), common.get_transferred(),
				getThirftstats());
	}

	public TaskSpecificStats getThirftstats() {
		return TaskSpecificStats.bolt(new BoltStats(get_acked(), get_fail(),
				get_process_latencies()));
	}

	@SuppressWarnings("unchecked")
	private Map<String, Map<GlobalStreamId, Long>> get_acked() {
		Map<String, Map<GlobalStreamId, Long>> rtn = new HashMap<String, Map<GlobalStreamId, Long>>();
		for (Entry<Integer, Object> times : acked.entrySet()) {
			Map<Object, Long> val = (Map<Object, Long>) times.getValue();
			String key = Stats.parseTimeKey(times.getKey());
			Map<GlobalStreamId, Long> stats = new HashMap<GlobalStreamId, Long>();
			for (Entry<Object, Long> stat : val.entrySet()) {
				stats.put((GlobalStreamId) stat.getKey(), stat.getValue());
			}

			rtn.put(key, stats);
		}
		return rtn;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Map<GlobalStreamId, Long>> get_fail() {
		Map<String, Map<GlobalStreamId, Long>> rtn = new HashMap<String, Map<GlobalStreamId, Long>>();
		for (Entry<Integer, Object> times : failed.entrySet()) {
			Map<Object, Long> val = (Map<Object, Long>) times.getValue();
			String key = Stats.parseTimeKey(times.getKey());
			Map<GlobalStreamId, Long> stats = new HashMap<GlobalStreamId, Long>();
			for (Entry<Object, Long> stat : val.entrySet()) {
				stats.put((GlobalStreamId) stat.getKey(), stat.getValue());
			}

			rtn.put(key, stats);
		}
		return rtn;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Map<GlobalStreamId, Double>> get_process_latencies() {
		Map<String, Map<GlobalStreamId, Double>> rtn = new HashMap<String, Map<GlobalStreamId, Double>>();
		for (Entry<Integer, Object> times : process_latencies.entrySet()) {
			Map<Object, Double> val = (Map<Object, Double>) times.getValue();
			String key = Stats.parseTimeKey(times.getKey());
			Map<GlobalStreamId, Double> stats = new HashMap<GlobalStreamId, Double>();
			for (Entry<Object, Double> stat : val.entrySet()) {
				stats.put((GlobalStreamId) stat.getKey(), stat.getValue());
			}

			rtn.put(key, stats);
		}
		return rtn;
	}

	@Override
	public boolean equals(Object assignment) {
		if (assignment instanceof BoltTaskStatsData

				&& ((BoltTaskStatsData) assignment).getCommon().equals(common)
				&& ((BoltTaskStatsData) assignment).getAcked().equals(acked)
				&& ((BoltTaskStatsData) assignment).getFailed().equals(failed)
				&& ((BoltTaskStatsData) assignment).getProcess_latencies()
						.equals(process_latencies)

		) {
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return common.hashCode() + acked.hashCode() + failed.hashCode()
				+ process_latencies.hashCode();
	}

	@Override
	public String toString() {
		return "BoltTaskStatsData [common=" + common + ", acked=" + acked
				+ ", failed=" + failed + ", process_latencies="
				+ process_latencies + "]";
	}
	
	
}
