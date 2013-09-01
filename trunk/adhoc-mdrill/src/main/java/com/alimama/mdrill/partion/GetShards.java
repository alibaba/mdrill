package com.alimama.mdrill.partion;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;


import backtype.storm.utils.Utils;

import com.alipay.bluewhale.core.callback.RunnableCallback;
import com.alipay.bluewhale.core.cluster.Cluster;
import com.alipay.bluewhale.core.cluster.ClusterState;
import com.alipay.bluewhale.core.cluster.ShardsState;
import com.alipay.bluewhale.core.cluster.SolrInfo;
import com.alipay.bluewhale.core.cluster.StormClusterState;


public class GetShards {
	private static Logger LOG = Logger.getLogger(GetShards.class);

	public static class SolrInfoList extends RunnableCallback{
		private String tableName;
		public SolrInfoList(String tableName) {
			super();
			this.tableName = tableName;
		}

		List<SolrInfo> infolist=new ArrayList<SolrInfo>();
		private long time=System.currentTimeMillis();
		Object lock=new Object();
		@Override
		public <T> Object execute(T... args) {
			this.run();
			return null;
		}
		
		public void maybeRefresh()
		{
			long t2=System.currentTimeMillis();
			if((t2-time)>=1000l*600)
			{
				this.run();
				time=System.currentTimeMillis();
			}
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
			synchronized (lock) {
				try {
					LOG.info("sync from zookeeper "+tableName);
					StormClusterState zkCluster = getCluster();
					List<Integer> list = zkCluster.higo_ids(tableName);
					ArrayList<SolrInfo> newlist=new ArrayList<SolrInfo>();
					for (Integer id : list) {
						SolrInfo info = zkCluster.higo_info(tableName, id);
						if (info != null )
						{
							newlist.add(info);
						}
					}
					infolist.clear();
					infolist.addAll(newlist);
					zkCluster.higo_base(tableName, this);

				} catch (Exception e) {}
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
    	}
    	infolist.maybeRefresh();
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
				
				if((info.times+1000l*600)<nowtime)
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
