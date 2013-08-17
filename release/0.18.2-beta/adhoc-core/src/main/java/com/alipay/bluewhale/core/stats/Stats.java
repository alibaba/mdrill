package com.alipay.bluewhale.core.stats;

import java.util.HashMap;

import com.alipay.bluewhale.core.callback.RunnableCallback;
import com.alipay.bluewhale.core.stats.RollingWindow.RollingWindowSet;
import com.alipay.bluewhale.core.stats.RollingWindow.RollingWindowSetStat;
import com.alipay.bluewhale.core.stats.incval.IncValExtractor;
import com.alipay.bluewhale.core.stats.incval.IncValMerger;
import com.alipay.bluewhale.core.stats.incval.IncValUpdater;
import com.alipay.bluewhale.core.stats.keyAvg.KeyAvgExtractor;
import com.alipay.bluewhale.core.stats.keyAvg.KeyAvgMerge;
import com.alipay.bluewhale.core.stats.keyAvg.KeyAvgUpdater;
import com.alipay.bluewhale.core.utils.StormUtils;

import backtype.storm.generated.GlobalStreamId;
import backtype.storm.generated.TaskStats;

/**
 * storm的task统计 storm 对tuple数量，处理时间，按照不同时间段的统计
 * 
 * @author yannian
 * 
 */
public class Stats {

	/**
	 * 简单累加计数统计
	 * 
	 * @param num_buckets
	 * @param bucket_sizes
	 * @return
	 */
	public static RollingWindowSet keyed_counter_rolling_window_set(
			int num_buckets, Integer[] bucket_sizes) {
		RunnableCallback updater = new IncValUpdater();
		RunnableCallback merger = new IncValMerger();

		RunnableCallback extractor = new IncValExtractor();
		return RollingWindowSetStat.rolling_window_set(updater, merger,
				extractor, num_buckets, bucket_sizes);
	}

	/**
	 * 平均值统计
	 * 
	 * @param num_buckets
	 * @param bucket_sizes
	 * @return
	 */
	public static RollingWindowSet keyed_avg_rolling_window_set(
			int num_buckets, Integer[] bucket_sizes) {
		RunnableCallback updater = new KeyAvgUpdater();

		RunnableCallback merger = new KeyAvgMerge();

		RunnableCallback extractor = new KeyAvgExtractor();

		return RollingWindowSetStat.rolling_window_set(updater, merger,
				extractor, num_buckets, bucket_sizes);
	}

	public static Integer NUM_STAT_BUCKETS = 20;
	public static Integer[] STAT_BUCKETS = { 30, 540, 4320 };

	private static CommonStatsRolling mk_common_stats(Integer rate) {
		CommonStatsRolling rtn = new CommonStatsRolling();
		rtn.emitted = keyed_counter_rolling_window_set(NUM_STAT_BUCKETS,
				STAT_BUCKETS);
		rtn.transferred = keyed_counter_rolling_window_set(NUM_STAT_BUCKETS,
				STAT_BUCKETS);
		rtn.rate = rate;
		return rtn;
	}

	/**
	 * 创建bolt的统计对象
	 * 
	 * @param rate
	 * @return
	 */
	public static BoltTaskStatsRolling mk_bolt_stats(Integer rate) {

		CommonStatsRolling common = mk_common_stats(rate);
		RollingWindowSet acked = keyed_counter_rolling_window_set(
				NUM_STAT_BUCKETS, STAT_BUCKETS);
		RollingWindowSet failed = keyed_counter_rolling_window_set(
				NUM_STAT_BUCKETS, STAT_BUCKETS);
		RollingWindowSet process_latencies = keyed_avg_rolling_window_set(
				NUM_STAT_BUCKETS, STAT_BUCKETS);
		BoltTaskStatsRolling rtn = new BoltTaskStatsRolling(common, acked,
				failed, process_latencies);
		return rtn;
	}

	/**
	 * 创建spout的统计对象
	 * 
	 * @param rate
	 * @return
	 */
	public static SpoutTaskStatsRolling mk_spout_stats(Integer rate) {

		CommonStatsRolling common = mk_common_stats(rate);
		RollingWindowSet acked = keyed_counter_rolling_window_set(
				NUM_STAT_BUCKETS, STAT_BUCKETS);
		RollingWindowSet failed = keyed_counter_rolling_window_set(
				NUM_STAT_BUCKETS, STAT_BUCKETS);
		RollingWindowSet complete_latencies = keyed_avg_rolling_window_set(
				NUM_STAT_BUCKETS, STAT_BUCKETS);
		return new SpoutTaskStatsRolling(common, acked, failed,
				complete_latencies);
	}

	/**
	 * 更新common的统计
	 * 
	 * @param common
	 * @param path
	 * @param args
	 */
	private static void update_task_stat(CommonStatsRolling common,
			String[] path, Object... args) {
		if (path[0].equals("emitted")) {
			RollingWindowSetStat
					.update_rolling_window_set(common.emitted, args);
		}
		if (path[0].equals("transferred")) {
			RollingWindowSetStat.update_rolling_window_set(common.transferred,
					args);
		}
	}

	/**
	 * 更新bolt的统计
	 * 
	 * @param boltspec
	 * @param path
	 * @param args
	 */
	private static void update_task_stat(BoltTaskStatsRolling boltspec,
			String[] path, Object... args) {
		if (path[0].equals("common")) {
			update_task_stat(boltspec.getCommon(), StormUtils.mk_arr(path[1]),
					args);
		}
		if (path[0].equals("acked")) {
			RollingWindowSetStat.update_rolling_window_set(boltspec.getAcked(),
					args);
		}
		if (path[0].equals("failed")) {
			RollingWindowSetStat.update_rolling_window_set(
					boltspec.getFailed(), args);
		}
		if (path[0].equals("process_latencies")) {
			RollingWindowSetStat.update_rolling_window_set(
					boltspec.getProcess_latencies(), args);
		}
	}

	/**
	 * 更新spout的统计
	 * 
	 * @param spec
	 * @param path
	 * @param args
	 */
	private static void update_task_stat(SpoutTaskStatsRolling spec,
			String[] path, Object... args) {
		if (path[0].equals("common")) {
			update_task_stat(spec.getCommon(), StormUtils.mk_arr(path[1]), args);
		}
		if (path[0].equals("acked")) {
			RollingWindowSetStat.update_rolling_window_set(spec.getAcked(),
					args);
		}
		if (path[0].equals("failed")) {
			RollingWindowSetStat.update_rolling_window_set(spec.getFailed(),
					args);
		}
		if (path[0].equals("complete_latencies")) {
			RollingWindowSetStat.update_rolling_window_set(
					spec.getComplete_latencies(), args);
		}
	}

	public static Integer stats_rate(SpoutTaskStatsRolling spec) {

		return spec.getCommon().rate;
	}

	public static Integer stats_rate(BoltTaskStatsRolling boltspec) {
		return boltspec.getCommon().rate;
	}

	public static void emitted_tuple(BaseTaskStatsRolling stats, String stream) {
		if (stats.getType().equals("bolt")) {
			emitted_tuple((BoltTaskStatsRolling) stats, stream);
		}
		if (stats.getType().equals("spout")) {
			emitted_tuple((SpoutTaskStatsRolling) stats, stream);
		}
	}

	private static void emitted_tuple(SpoutTaskStatsRolling stats, String stream) {
		update_task_stat(stats, StormUtils.mk_arr("common", "emitted"), stream,
				stats_rate(stats));
	}

	private static void emitted_tuple(BoltTaskStatsRolling stats, String stream) {
		update_task_stat(stats, StormUtils.mk_arr("common", "emitted"), stream,
				stats_rate(stats));
	}

	public static void transferred_tuples(BaseTaskStatsRolling stats,
			String stream, Integer amt) {
		if (stats.getType().equals("bolt")) {
			transferred_tuples((BoltTaskStatsRolling) stats, stream, amt);
		}
		if (stats.getType().equals("spout")) {
			transferred_tuples((SpoutTaskStatsRolling) stats, stream, amt);
		}
	}

	public static void transferred_tuples(SpoutTaskStatsRolling stats,
			String stream, Integer amt) {
		update_task_stat(stats, StormUtils.mk_arr("common", "transferred"),
				stream, stats_rate(stats) * amt);
	}

	public static void transferred_tuples(BoltTaskStatsRolling stats,
			String stream, Integer amt) {
		update_task_stat(stats, StormUtils.mk_arr("common", "transferred"),
				stream, stats_rate(stats) * amt);
	}

	public static void bolt_acked_tuple(BoltTaskStatsRolling stats,
			String component, String stream, Long latency_ms) {
		GlobalStreamId key = new GlobalStreamId(component, stream);
		update_task_stat(stats, StormUtils.mk_arr("acked"), key,
				stats_rate(stats));
		update_task_stat(stats, StormUtils.mk_arr("process_latencies"), key,
				latency_ms);
	}

	public static void bolt_failed_tuple(BoltTaskStatsRolling stats,
			String component, String stream, Long latency_ms) {
		GlobalStreamId key = new GlobalStreamId(component, stream);
		update_task_stat(stats, StormUtils.mk_arr("failed"), key,
				stats_rate(stats));
	}

	public static void spout_acked_tuple(SpoutTaskStatsRolling stats,
			String stream, Long latency_ms) {
		update_task_stat(stats, StormUtils.mk_arr("acked"), stream,
				stats_rate(stats));
		update_task_stat(stats, StormUtils.mk_arr("complete_latencies"),
				stream, latency_ms);
	}

	public static void spout_failed_tuple(SpoutTaskStatsRolling stats,
			String stream, Long latency_ms) {
		update_task_stat(stats, StormUtils.mk_arr("failed"), stream,
				stats_rate(stats));
	}

	private static HashMap<Integer, Object> get_path_value_stats(
			CommonStatsRolling common, String[] path) {

		if (path[0].equals("emitted")) {
			return RollingWindowSetStat
					.value_rolling_window_set(common.emitted);
		}

		if (path[0].equals("transferred")) {
			return RollingWindowSetStat
					.value_rolling_window_set(common.transferred);
		}

		return null;
	}

	private static HashMap<Integer, Object> get_path_value_stats(
			BoltTaskStatsRolling spec, String[] path) {

		if (path[0].equals("common")) {
			return get_path_value_stats(spec.getCommon(),
					StormUtils.mk_arr(path[1]));
		}
		if (path[0].equals("acked")) {
			return RollingWindowSetStat.value_rolling_window_set(spec
					.getAcked());
		}
		if (path[0].equals("failed")) {
			return RollingWindowSetStat.value_rolling_window_set(spec
					.getFailed());
		}
		if (path[0].equals("process_latencies")) {
			return RollingWindowSetStat.value_rolling_window_set(spec
					.getProcess_latencies());
		}

		return null;
	}

	private static HashMap<Integer, Object> get_path_value_stats(
			SpoutTaskStatsRolling spec, String[] path) {
		HashMap<Integer, Object> map=null;
		if (path[0].equals("common")) {
			map=get_path_value_stats(spec.getCommon(),
					StormUtils.mk_arr(path[1]));
		}else if (path[0].equals("acked")) {
			map=RollingWindowSetStat.value_rolling_window_set(spec
					.getAcked());
		}else if (path[0].equals("failed")) {
			map=RollingWindowSetStat.value_rolling_window_set(spec
					.getFailed());
		}else if (path[0].equals("complete_latencies")) {
			map=RollingWindowSetStat.value_rolling_window_set(spec
					.getComplete_latencies());
		}
		return map;
	}

	public static String parseTimeKey(Integer key) {
		if (key == 0) {
			return "all-time";
		} else {
			return String.valueOf(key);
		}
	}

	public static BaseStatsData render_stats(BaseTaskStatsRolling stats) {
		
		BaseStatsData stat=null;
		if (stats.getType().equals("bolt")) {
			stat= value_bolt_stats((BoltTaskStatsRolling) stats);
		}

		if (stats.getType().equals("spout")) {
			stat= value_spout_stats((SpoutTaskStatsRolling) stats);
		}

		return stat;
	}

	private static CommonStatsData value_common_stats(CommonStatsRolling stats) {
		cleanup_common_stats(stats);
		CommonStatsData rtn = new CommonStatsData();
		rtn.setEmitted(get_path_value_stats(stats, StormUtils.mk_arr("emitted")));
		rtn.setTransferred(get_path_value_stats(stats,
				StormUtils.mk_arr("transferred")));
		rtn.setRate(stats.rate);
		return rtn;
	}

	private static SpoutTaskStatsData value_spout_stats(
			SpoutTaskStatsRolling stats) {
		cleanup_spout_stats(stats, true);
		CommonStatsData common = value_common_stats(stats.getCommon());
		HashMap<Integer, Object> acked = get_path_value_stats(stats,
				StormUtils.mk_arr("acked"));
		HashMap<Integer, Object> failed = get_path_value_stats(stats,
				StormUtils.mk_arr("failed"));
		HashMap<Integer, Object> complete_latencies = get_path_value_stats(
				stats, StormUtils.mk_arr("complete_latencies"));
		return new SpoutTaskStatsData(common, acked, failed, complete_latencies);
	}

	private static BoltTaskStatsData value_bolt_stats(BoltTaskStatsRolling stats) {
		cleanup_bolt_stats(stats, true);

		CommonStatsData common = value_common_stats(stats.getCommon());
		HashMap<Integer, Object> acked = get_path_value_stats(stats,
				StormUtils.mk_arr("acked"));
		HashMap<Integer, Object> failed = get_path_value_stats(stats,
				StormUtils.mk_arr("failed"));
		HashMap<Integer, Object> process_latencies = get_path_value_stats(
				stats, StormUtils.mk_arr("process_latencies"));
		return new BoltTaskStatsData(common, acked, failed, process_latencies);

	}

	private static void cleanup_rolling_stat(RollingWindowSet rws) {
		RollingWindowSetStat.cleanup_rolling_window_set(rws);
	}

	private static void cleanup_common_stats(CommonStatsRolling stats) {
		cleanup_rolling_stat(stats.emitted);
		cleanup_rolling_stat(stats.transferred);
	}

	public static void cleanup_bolt_stats(BoltTaskStatsRolling stats,
			boolean skipcommon) {
		if (!skipcommon) {
			cleanup_common_stats(stats.getCommon());
		}
		cleanup_rolling_stat(stats.getAcked());
		cleanup_rolling_stat(stats.getFailed());
		cleanup_rolling_stat(stats.getProcess_latencies());
	}

	public static void cleanup_spout_stats(SpoutTaskStatsRolling stats,
			boolean skipcommon) {
		if (!skipcommon) {
			cleanup_common_stats(stats.getCommon());
		}
		cleanup_rolling_stat(stats.getAcked());
		cleanup_rolling_stat(stats.getFailed());
		cleanup_rolling_stat(stats.getComplete_latencies());
	}

	// private static TaskSpecificStats
	// thriftify_specific_stats(SpoutTaskStatsRolling stats)
	// {
	// return value_spout_stats(stats).getThirftstats();
	// }
	//
	// private static TaskSpecificStats
	// thriftify_specific_stats(BoltTaskStatsRolling stats)
	// {
	// return value_bolt_stats(stats).getThirftstats();
	// }

	public static TaskStats thriftify_task_stats(SpoutTaskStatsRolling stats) {
		return value_spout_stats(stats).getTaskStats();
	}

	public static TaskStats thriftify_task_stats(BoltTaskStatsRolling stats) {
		return value_bolt_stats(stats).getTaskStats();
	}
}
