package com.alipay.bluewhale.core.daemon;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import backtype.storm.utils.TimeCacheMap;

import com.alipay.bluewhale.core.cluster.Cluster;
import com.alipay.bluewhale.core.cluster.StormClusterState;
import com.alipay.bluewhale.core.task.TkHbCacheTime;
import com.alipay.bluewhale.core.utils.TimeUtils;

/**
 *  nimbus中所使用的数据，均存放在此
 *
 */
public class NimbusData {
	private Map<Object,Object> conf;
	private StormClusterState stormClusterState;
	// private final Object submitLock;
	private ConcurrentHashMap<String, Map<Integer, Map<TkHbCacheTime, Integer>>> taskHeartbeatsCache;
	//TODO value既存在Channel类型又存在 BufferFileInputStream类型
	private TimeCacheMap<Object,Object> downloaders;
	private TimeCacheMap<Object,Object> uploaders;
	private int startTime = TimeUtils.current_time_secs();
	//private StormTimer timer;
	private final ScheduledExecutorService scheduExec;
	private AtomicInteger submittedCount;
	
	private Object submitLock = new Object();

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public NimbusData(Map conf, TimeCacheMap<Object,Object> downloaders,
			TimeCacheMap<Object,Object> uploaders) throws Exception {
		this.conf = conf;
		this.stormClusterState = Cluster.mk_storm_cluster_state(conf);
		this.taskHeartbeatsCache = new ConcurrentHashMap<String, Map<Integer, Map<TkHbCacheTime, Integer>>>();
		this.downloaders = downloaders;
		this.uploaders = uploaders;
		//this.timer = timer;
		this.startTime = TimeUtils.current_time_secs();
		this.submittedCount = new AtomicInteger(0);
		this.scheduExec = Executors.newScheduledThreadPool(6);

	}
	
	/**
	 * for test
	 */
	public NimbusData(){
	    scheduExec = Executors.newScheduledThreadPool(6);
	}

	public int uptime() {
		return  (TimeUtils.current_time_secs() - startTime);
	}

	public Map<Object,Object> getConf() {
		return conf;
	}

	public void setConf(Map<Object,Object> conf) {
		this.conf = conf;
	}

	public StormClusterState getStormClusterState() {
		return stormClusterState;
	}

	public void setStormClusterState(StormClusterState stormClusterState) {
		this.stormClusterState = stormClusterState;
	}

	public ConcurrentHashMap<String, Map<Integer, Map<TkHbCacheTime, Integer>>> getTaskHeartbeatsCache() {
		return taskHeartbeatsCache;
	}

	public void setTaskHeartbeatsCache(ConcurrentHashMap<String, Map<Integer, Map<TkHbCacheTime, Integer>>> taskHeartbeatsCache) {
		this.taskHeartbeatsCache = taskHeartbeatsCache;
	}

	public TimeCacheMap<Object,Object> getDownloaders() {
		return downloaders;
	}

	public void setDownloaders(TimeCacheMap<Object,Object> downloaders) {
		this.downloaders = downloaders;
	}

	public TimeCacheMap<Object,Object> getUploaders() {
		return uploaders;
	}

	public void setUploaders(TimeCacheMap<Object,Object> uploaders) {
		this.uploaders = uploaders;
	}

	public int getStartTime() {
		return startTime;
	}

	public void setStartTime(int startTime) {
		this.startTime = startTime;
	}

	public AtomicInteger getSubmittedCount() {
		return submittedCount;
	}

	public void setSubmittedCount(AtomicInteger submittedCount) {
		this.submittedCount = submittedCount;
	}

	public Object getSubmitLock() {
		return submitLock;
	}
		
	public ScheduledExecutorService getScheduExec() {
	    return scheduExec;
	}

	//for test
	public void setSubmitLock(Object obj) {
		this.submitLock = obj;
	}

}