package com.etao.adhoc.metric.load;

import java.io.IOException;

import com.etao.adhoc.metric.Metric;

public interface QueryService {
	Metric getMetric(String tablename, String thedate)throws IOException;
	String getName();
	void close();
}
