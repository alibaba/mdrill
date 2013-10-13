package com.etao.adhoc.metric.load;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import backtype.storm.utils.Utils;

import com.etao.adhoc.metric.Metric;
import com.etao.adhoc.metric.MetricService;

public class UpdateService {
	private MetricService metricServer;
	private Map conf;
	private QueryService queryServer;
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public UpdateService(String queryFrom) {
		conf = Utils.readStormConfig("adhoc.yaml");
		metricServer = new MetricService();
		if(queryFrom.equals("hive")){
			queryServer = new HiveQueryService(conf);
		} else if(queryFrom.equals("higo")){
			queryServer = new HigoQueryService(conf);
		} else {
			System.out.println("UpdateService: unknown query type " + queryFrom);
		}
	}
	public void load(String tablename, String thedate) throws IOException {
		Metric metric = queryServer.getMetric(tablename, thedate);
		String queryServiceName = queryServer.getName();
	
		if(metric == null){
			System.out.println(queryServiceName + " RETURN NULL");
		} else {
			metricServer.delete(metric);
			metricServer.insert(metric);
		}
	
	
	}
	public void close() {
		if(queryServer!=null){
			queryServer.close();
		}
		if(metricServer!=null){
			metricServer.close();
		}
	}
	
	public static void usage(){
		System.out.println("(hive|higo) " +
				"startday" +
				"endday");
	}
	public static void main(String[] args) throws IOException {
		UpdateService server = new UpdateService(args[0]);
		server.load(args[1], args[2]);
		server.close();
	}

}
