package com.alimama.mdrill.partion;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;





import backtype.storm.utils.Utils;

import com.alipay.bluewhale.core.callback.RunnableCallback;
import com.alipay.bluewhale.core.cluster.Cluster;
import com.alipay.bluewhale.core.cluster.ClusterState;
import com.alipay.bluewhale.core.cluster.ShardsState;
import com.alipay.bluewhale.core.cluster.SolrInfo;
import com.alipay.bluewhale.core.cluster.StormClusterState;


public class GetShards {
	
	private static Timer  EXECUTE =new Timer();

	private static Logger LOG = Logger.getLogger(GetShards.class);

	public static class SolrInfoList extends RunnableCallback{
		private String tableName;
		public SolrInfoList(String tableName) {
			super();
			this.tableName = tableName;
		}
		
		TimerTask task=null;


		List<SolrInfo> infolist=new ArrayList<SolrInfo>();
		Object lock=new Object();
		@Override
		public <T> Object execute(T... args) {
			this.run();
			return null;
		}
		
		
		public List<SolrInfo> getlist()
		{
			synchronized (lock) {
				List<SolrInfo> rtn=new ArrayList<SolrInfo>();
				rtn.addAll(this.infolist);
				return rtn;
			}
		}
		public void run() {
			ArrayList<SolrInfo> newlist=new ArrayList<SolrInfo>();

			try {
				long t1=System.currentTimeMillis();
				LOG.info("sync from zookeeper "+tableName);
				StormClusterState zkCluster = getCluster();
				List<Integer> list = zkCluster.higo_ids(tableName);
				for (Integer id : list) {
					SolrInfo info = zkCluster.higo_info(tableName, id);
					if (info != null )
					{
						newlist.add(info);
					}
				}
				
				zkCluster.higo_base(tableName, this);
				long tl=System.currentTimeMillis()-t1;
				LOG.info("getShards timetaken:"+tl);

			} catch (Exception e) {
				
				return ;
			}
			synchronized (lock) {
				infolist.clear();
				infolist.addAll(newlist);
				if(this.task==null)
				{
					this.task=new TimerTask() {
						@Override
						public void run() {
							SolrInfoList.this.run();
						}
					};
					EXECUTE.schedule(task, 60000l, 60000l);
				}
			}
			
			
			
			
		}
	};
	
	private static ConcurrentHashMap<String, SolrInfoList> infolistmap=new ConcurrentHashMap<String, GetShards.SolrInfoList>();

	public static SolrInfoList getSolrInfoList(String tableName)
	{
		SolrInfoList infolist=infolistmap.get(tableName);
    	if(infolist==null)
    	{
    		infolist=new SolrInfoList(tableName);
    		infolist.run();
    		infolistmap.putIfAbsent(tableName, infolist);
			LOG.info("SolrInfoList init:"+tableName);
    	}
    	return infolist;
	}
	
	public static class ShardsList{
		public ArrayList<String> list=new ArrayList<String>();
		public int random=(int) (Math.random()*1000);
		
		public String randomGet()
		{
			int index=random%list.size();
			random++;
			return list.get(index);
		}
		
		public boolean containsIp(String ip)
		{
			for(String s:list)
			{
				if(s.indexOf(ip)>=0)
				{
					return true;
				}
			}
			return false;
		}
	}
	
    public static ShardsList[] get(String tableName,boolean isMs) throws Exception
 {
    	SolrInfoList infolist=getSolrInfoList(tableName);
    	
    	long nowtime=System.currentTimeMillis();
    	HashMap<Integer,ShardsList> replication=new HashMap<Integer, ShardsList>();
		for (SolrInfo info : infolist.getlist()) {
			if (info != null && info.isMergeServer == isMs)// &&info.stat==ShardsState.SERVICE
			{
				if (isMs && !info.stat.equals(ShardsState.SERVICE)) {
					continue;
				}
				
				if((info.times+1000l*3600)<nowtime)
				{
					continue;
				}
				
				ShardsList solrlist=replication.get(info.taskIndex);
				if(solrlist==null)
				{
					solrlist=new ShardsList();
					replication.put(info.taskIndex, solrlist);
				}
				solrlist.list.add(new String(info.localip + ":" + info.port));
			}
		}
		
		
		ShardsList[] rtn=new ShardsList[replication.size()];
		
		int index=0;
		for(ShardsList s:replication.values())
		{
			rtn[index++]=s;
		}
		
		return rtn;


	}
    
    
//    public static String printSolr(String tablename) throws Exception {
//
//	String urlMain = "";
//	String urlShards = "";
//
//	Random rdm = new Random();
//	String[] cores = GetShards.get(tablename,false,100000);
//	if (cores != null && cores.length > 0) {
//	    int count = 0;
//	    int r = rdm.nextInt(cores.length);
//	    for (String c : cores) {
//		if (count == r)
//		{
//		    urlMain = "http://" + c + "/solr";
//		}
//	
//		urlShards += c + "/solr,";
//		count++;
//	    }
//	}
//
//	return urlMain+"/select/?q=*:*&start=0&rows=1&indent=on&shards="+urlShards;
//
//    }
    public static StormClusterState higozkCluster;
    public static StormClusterState getCluster() throws Exception
    {
	if(higozkCluster==null)
	{
        	Map stormconf = Utils.readStormConfig();
        	ClusterState zkClusterstate =Cluster.mk_distributed_cluster_state(stormconf);
        	higozkCluster = Cluster.mk_storm_cluster_state(zkClusterstate);
	}
	return higozkCluster;
    }
}
