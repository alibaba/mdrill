package com.etao.adhoc.metric.load;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.etao.adhoc.common.util.YamlUtils;
import com.etao.adhoc.metric.Metric;
import com.etao.adhoc.metric.MetricService;

public class UpdateService {
	private MetricService metricServer;
	private Map conf;
	private QueryService queryServer;
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public UpdateService(String queryFrom) {
		try {
			conf = YamlUtils.getConfigFromYamlFile("adhoc-metric.yaml");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println("[" + sdf.format(new Date(System.currentTimeMillis())) + "] "
					+ "Error when open YAML File");
			e.printStackTrace();
		}
		metricServer = new MetricService();
		if(queryFrom.equals("hive")){
			queryServer = new HiveQueryService(conf);
		} else if(queryFrom.equals("higo")){
			queryServer = new HigoQueryService(conf);
		} else {
			System.out.println("UpdateService: unknown query type " + queryFrom);
		}
	}
	public void load(String tablename, String thedate) {
		Metric metric = queryServer.getMetric(tablename, thedate);
		String queryServiceName = queryServer.getName();
		int retry = 3; //higo有时会返回空，重试
		if(!queryServiceName.equals("HIGO")){
			retry = 1;  //如果是查询hive，返回空就不用重试了
		}
		
		Set<Metric> history = new HashSet<Metric>();//因为higo有时数值会偏小，所以要重新试几次
		
		
		boolean getResult = false;
		
		for(int i = 0; i < retry; i++){
			if(metric == null){
				System.out.println(queryServiceName + " RETURN NULL");
			} else {
				if(!queryServiceName.equals("HIGO") ||  history.contains(metric)){
					System.out.println("GET METRIC FROM " + queryServiceName +": " + metric);
					metricServer.delete(metric);
					metricServer.insert(metric);
					getResult = true;
					break;
				} else {
					try {
						TimeUnit.SECONDS.sleep(10);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					history.add(metric);
				}
			}
			metric = queryServer.getMetric(tablename, thedate);
		}
		if( history.size() > 1){
			System.out.println("DIFFERENT RESULTS FROM HIGO :");
			for(Metric m : history){
				System.out.println(m);
			}
		}
		if(!getResult){
			System.out.println("GET METRIC FROM " + queryServiceName + "FAILED");
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
	public static void main(String[] args) {
		UpdateService server = new UpdateService(args[0]);
		server.load(args[1], args[2]);
		server.close();
	}

}
