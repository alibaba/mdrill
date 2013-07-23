package com.alipay.bluewhale.core.stats.incval;

import java.util.HashMap;
import java.util.Map;

import com.alipay.bluewhale.core.callback.RunnableCallback;
import com.alipay.bluewhale.core.stats.RollingWindow.UpdateParams;
import com.alipay.bluewhale.core.stats.method.StatFunction;

public class IncValUpdater extends RunnableCallback {

	@SuppressWarnings("unchecked")
	@Override
	public <T> Object execute(T... args) {
		Map<Object, Long> curr = null;
		if (args != null && args.length > 0) {
			UpdateParams p = (UpdateParams) args[0];
			if (p.curr != null) {
				curr = (Map<Object, Long>) p.curr;
			} else {
				curr = new HashMap<Object, Long>();
			}
			Object[] incArgs = p.args;

			Long amt = 1l;

			if (incArgs.length > 1) {
				amt = Long.parseLong(String.valueOf(incArgs[1]));
			}
			StatFunction.incr_val(curr, incArgs[0], amt);

		}
		return curr;
	}

}
