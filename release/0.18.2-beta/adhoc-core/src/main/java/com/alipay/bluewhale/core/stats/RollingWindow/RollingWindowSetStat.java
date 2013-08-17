package com.alipay.bluewhale.core.stats.RollingWindow;

import java.util.HashMap;

import com.alipay.bluewhale.core.callback.RunnableCallback;
import com.alipay.bluewhale.core.utils.TimeUtils;

public class RollingWindowSetStat {

	public static RollingWindowSet rolling_window_set(RunnableCallback updater,
			RunnableCallback merger, RunnableCallback extractor,
			Integer num_buckets, Integer[] bucket_size) {
		RollingWindowSet rtn = new RollingWindowSet();
		rtn.updater = updater;
		rtn.extractor = extractor;
		rtn.windows = new RollingWindow[bucket_size.length];
		for (int i = 0; i < bucket_size.length; i++) {
			rtn.windows[i] = RollingWindowStat.rolling_window(updater, merger,
					extractor, bucket_size[i], num_buckets);
		}
		rtn.all_time = null;
		return rtn;
	}

	public static void update_rolling_window_set(RollingWindowSet rws,
			Object[] args) {
		synchronized (rws) {
			int now = TimeUtils.current_time_secs();
			for (int i = 0; i < rws.windows.length; i++) {
				rws.windows[i] = RollingWindowStat.update_rolling_window(
						rws.windows[i], now, args);
			}

			UpdateParams p = new UpdateParams();
			p.args = args;
			p.curr = rws.all_time;
			rws.all_time = rws.updater.execute(p);
		}
	}

	public static RollingWindowSet cleanup_rolling_window_set(
			RollingWindowSet rws) {
		synchronized (rws) {
			for (int i = 0; i < rws.windows.length; i++) {
				rws.windows[i] = RollingWindowStat
						.cleanup_rolling_window(rws.windows[i]);
			}
			return rws;
		}
	}

	public static HashMap<Integer, Object> value_rolling_window_set(
			RollingWindowSet rws) {
		HashMap<Integer, Object> rtn = new HashMap<Integer, Object>();
		synchronized (rws) {
			for (int i = 0; i < rws.windows.length; i++) {
				int size = RollingWindowStat
						.rolling_window_size(rws.windows[i]);
				Object obj = RollingWindowStat
						.value_rolling_window(rws.windows[i]);
				rtn.put(size, obj);
			}

			Object result = rws.extractor.execute(rws.all_time);

			rtn.put(0, result);
			return rtn;
		}
	}
}
