package com.alipay.bluewhale.core.stats.incval;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.alipay.bluewhale.core.callback.RunnableCallback;

public class IncValMerger extends RunnableCallback {

	@SuppressWarnings("unchecked")
	@Override
	public <T> Object execute(T... args) {
		Map<Object, Long> result=null;
		if(args!=null&&args.length>0){
			List<Map<Object, Long>> list = (List<Map<Object, Long>>) args[0];
			result = new HashMap<Object, Long>();
			for (Map<Object, Long> each : list) {
				for (Entry<Object, Long> e : each.entrySet()) {
					Object key = e.getKey();
					Long val = e.getValue();
					if (result.containsKey(key)) {
						val += result.get(key);
					}
					result.put(key, val);
				}
			}
		}
		return result;
	}
}
