package com.alipay.bluewhale.core.stats.keyAvg;

import java.util.HashMap;
import java.util.Map;

import com.alipay.bluewhale.core.callback.RunnableCallback;
import com.alipay.bluewhale.core.stats.RollingWindow.UpdateParams;
import com.alipay.bluewhale.core.stats.method.Pair;
import com.alipay.bluewhale.core.stats.method.StatFunction;

public class KeyAvgUpdater extends RunnableCallback {

	@SuppressWarnings("unchecked")
	@Override
	public <T> Object execute(T... args) {
		Map<Object, Pair> curr = null;
		if (args != null && args.length > 0) {
			UpdateParams p = (UpdateParams) args[0];
			if (p.curr != null) {
				curr = (Map<Object, Pair>) p.curr;
			} else {
				curr = new HashMap<Object, Pair>();
			}
			Object[] keyAvgArgs = p.args;

			Long amt = 1l;
			if (keyAvgArgs.length > 1) {
				amt = Long.parseLong(String.valueOf(keyAvgArgs[1]));
			}
			StatFunction.update_keyed_avg(curr, keyAvgArgs[0], amt);
		}
		return curr;
	}
}
