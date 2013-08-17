package com.alipay.bluewhale.core.stats.RollingWindow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.alipay.bluewhale.core.callback.RunnableCallback;
import com.alipay.bluewhale.core.utils.TimeUtils;

public class RollingWindowStat {
	public static Integer curr_time_bucket(Integer time_secs,
			Integer bucket_size_secs) {
		return (Integer) (bucket_size_secs * (time_secs / bucket_size_secs));
	}

	public static RollingWindow rolling_window(RunnableCallback updater,
			RunnableCallback merger, RunnableCallback extractor,
			Integer bucket_size_secs, Integer num_buckets) {
		RollingWindow rtn = new RollingWindow();
		rtn.updater = updater;
		rtn.merger = merger;
		rtn.extractor = extractor;
		rtn.bucket_size_secs = bucket_size_secs;
		rtn.buckets = new HashMap<Integer, Object>();
		rtn.num_buckets = num_buckets;

		return rtn;
	}

	public static RollingWindow update_rolling_window(RollingWindow rw,
			Integer time_secs, Object[] args) {
		synchronized (rw) {
			Integer time_bucket = curr_time_bucket(time_secs,
					rw.bucket_size_secs);
			Map<Integer, Object> buckets = rw.buckets;
			Object curr = buckets.get(time_bucket);
			UpdateParams p = new UpdateParams();
			p.args = args;
			p.curr = curr;
			curr = rw.updater.execute(p);

			buckets.put(time_bucket, curr);
			return rw;
		}
	}

	public static Object value_rolling_window(RollingWindow rw) {
		synchronized (rw) {
			List<Object> values = new ArrayList<Object>();
			for (Entry<Integer, Object> entry : rw.buckets.entrySet()) {
				values.add(entry.getValue());
			}
			Object result = rw.merger.execute(values);
			return rw.extractor.execute(result);
		}

	}

	public static RollingWindow cleanup_rolling_window(int cutoff,
			RollingWindow rw) {
		synchronized (rw) {
			Map<Integer, Object> buckets = rw.buckets;
			List<Integer> toremove = new ArrayList<Integer>();
			for (Entry<Integer, Object> entry : rw.buckets.entrySet()) {
				Integer key = entry.getKey();
				if (key < cutoff) {
					toremove.add(key);
				}
			}

			for (Integer i : toremove) {
				buckets.remove(i);
			}
			rw.buckets = buckets;
			return rw;
		}
	}

	public static RollingWindow cleanup_rolling_window(RollingWindow rw) {
		int cutoff = TimeUtils.current_time_secs() - rolling_window_size(rw);
		return cleanup_rolling_window(cutoff, rw);
	}

	public static int rolling_window_size(RollingWindow rw) {
		return rw.bucket_size_secs * rw.num_buckets;
	}
}
