package com.etao.adhoc.metric.load;

import com.etao.adhoc.metric.Metric;

public interface QueryService {
	Metric getMetric(String tablename, String thedate);
	String getName();
	void close();
}
