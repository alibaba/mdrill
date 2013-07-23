package com.alipay.bluewhale.core.stats.keyAvg;

import java.util.Map;

import com.alipay.bluewhale.core.callback.RunnableCallback;
import com.alipay.bluewhale.core.stats.method.Pair;
import com.alipay.bluewhale.core.stats.method.StatFunction;

public class KeyAvgExtractor extends RunnableCallback {

	@SuppressWarnings("unchecked")
	@Override
	public <T> Object execute(T... args) {
		Map<Object, Double> result = null;
		if (args != null && args.length > 0) {
			Map<Object, Pair> v = (Map<Object, Pair>) args[0];
			result = StatFunction.extract_key_avg(v);
		}

		return result;
	}
}
