package com.alimama.mdrill.partion;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;






import backtype.storm.utils.Utils;

import com.alimama.mdrill.partion.GetPartions.TablePartion;
import com.alipay.bluewhale.core.callback.RunnableCallback;
import com.alipay.bluewhale.core.cluster.Cluster;
import com.alipay.bluewhale.core.cluster.ClusterState;
import com.alipay.bluewhale.core.cluster.ShardsState;
import com.alipay.bluewhale.core.cluster.SolrInfo;
import com.alipay.bluewhale.core.cluster.StormClusterState;
import com.alipay.bluewhale.core.utils.StormUtils;


/**
 * 用户获取某个表的所有shards以及mergers
 * @author yannian.mu
 *
 */
public class GetShards {
	
	private static Timer  EXECUTE =new Timer();

	private static Logger LOG = Logger.getLogger(GetShards.class);

	public static class SolrInfoList extends RunnableCallback{
		private String tableName;
		TimerTask task=null;

		List<SolrInfo> infolist=new ArrayList<SolrInfo>();
		public SolrInfoList(String tableName) {
			this.tableName = tableName;
		}
		


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
				List<Integer> list = zkCluster.higo_base(tableName,this);
				for (Integer id : list) {
					SolrInfo info = zkCluster.higo_info(tableName, id);
					if (info != null )
					{
						newlist.add(info);
					}
				}
				
				long tl=System.currentTimeMillis()-t1;
				LOG.info("getShards timetaken:"+tl);

			} catch (Throwable e) {
				synchronized (lock) {
					if(this.task!=null)
					{
						this.task.cancel();
						this.task=null;
					}
				}

				
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
					EXECUTE.schedule(task, 300000l, 300000l);
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
	
	public static void purge(String tableName)
	{
    	SolrInfoList infolist=getSolrInfoList(tableName);
    	infolist.run();
	}
	
	public static ShardsList[] getCores(Map stormconf,TablePartion part) throws Exception
	{
		ShardsList[] cores = GetShards.get(part.name, false);
		
		for(int i=0;i<10;i++)
		{
			if(cores.length!=StormUtils.parseInt(stormconf.get("higo.shards.count")))
			{
				if(i>5)
				{
					throw new Exception("core.size="+cores.length);
				}
				GetShards.purge(part.name);
				cores = GetShards.get(part.name, false);
				LOG.info("core.size="+cores.length);
				
				Thread.sleep(1000);
			}else{
				break;
			}
		}
		LOG.info("request core.size="+cores.length);
		
		return cores;
	}
	
	public static ShardsList[] getCoresNonCheck(TablePartion part) throws Exception
	{

		ShardsList[] cores = GetShards.get(part.name, false);
		for(int i=0;i<10;i++)
		{
			if(cores==null||cores.length<=0)
			{
				if(i>5)
				{
					throw new Exception("core.size<=0");
				}
				GetShards.purge(part.name);
				cores = GetShards.get(part.name, false);
				LOG.info("core.size="+cores.length);
				Thread.sleep(1000);
			}else{
				break;
			}
		}
		
		return cores;
	
	}

	
	public static ShardsList[] getMergers(String tableName) throws Exception
	{
		return GetShards.get(tableName, true);
	}
	
    private static ShardsList[] get(String tableName,boolean isMs) throws Exception
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
    

    private static StormClusterState higozkCluster;
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
