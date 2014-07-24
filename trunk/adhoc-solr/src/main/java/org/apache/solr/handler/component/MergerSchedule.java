package org.apache.solr.handler.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.solr.common.params.FacetParams;
import org.apache.solr.common.params.MergeShards;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.handler.component.ResponseBuilder.ScheduleInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alimama.mdrill.utils.UniqConfig;

/**
 * 海狗的查询调度
 * @author yannian.mu
 *
 */
public class MergerSchedule {
	protected static Logger log = LoggerFactory.getLogger(MergerSchedule.class);

	
	public static int countSize(HashMap<String, AssginShard> host2AssginShard)
	{
		int size=0;
		for(Entry<String, AssginShard> e:host2AssginShard.entrySet())
    	{
			size+=e.getValue().shards.size();
    	}
		
		return size;
	}
	public static ScheduleInfo schedule(SolrParams params, 
			List<String> lst,
			List<String> mslist, String[] shardpartions) {
	    ScheduleInfo scheduleInfo=new ScheduleInfo();
	    scheduleInfo.partions=shardpartions;
		scheduleInfo.hasSubShards = false;

		boolean isfacet = params.getBool(FacetParams.FACET_CROSS, false);
		Integer maxshards = params.getInt(FacetParams.MERGER_MAX_SHARDS,UniqConfig.getMaxMergerShard());
	    HashMap<String, AssginShard> host2AssginShard=MergerSchedule.assignByHost(lst, mslist);
	    int hostsize=host2AssginShard.size();
	    
	    int size=countSize(host2AssginShard);
	    if(maxshards >= size|| hostsize<=1|| !isfacet)
	    {
	    	if(shardpartions!=null&&shardpartions.length>0)
		    {
		    	//如果只剩下一个hosts的，需要将partions展开
		    	for(Entry<String, AssginShard> e:host2AssginShard.entrySet())
		    	{
		    		e.getValue().expand(shardpartions);
		    	}
		    	scheduleInfo.partions=null;
				log.info("MergerSchedule fill:"+size+"=="+host2AssginShard.toString());
		    }
	    }
	    
	    size=countSize(host2AssginShard);
		if ((maxshards >= size&&hostsize<=1) || !isfacet) {
			
			ArrayList<String> shardlist=new ArrayList<String>();
			for(Entry<String, AssginShard> e:host2AssginShard.entrySet())
	    	{
				shardlist.addAll(e.getValue().shards);
	    	}
			
			scheduleInfo.shards=shardlist.toArray(new String[shardlist.size()]);
			log.info("MergerSchedule shards:"+Arrays.toString(scheduleInfo.shards));
			return scheduleInfo;
		} 


	    ArrayList<AssginShard> result=new ArrayList<AssginShard>();
	    int requestCount=hostsize;
	    if(hostsize>maxshards)
	    {
	    	requestCount=maxshards;
	    }
	    
		for (int i = 0; i < requestCount; i++) {
			result.add(new AssginShard(new ArrayList<String>(),	new ArrayList<String>()));
		}
		int selectindex = 0;
		for (Entry<String, AssginShard> e : host2AssginShard.entrySet()) {
			AssginShard as = e.getValue();
			AssginShard toadd = result.get(selectindex % requestCount);
			toadd.ms.addAll(as.ms);
			toadd.shards.addAll(as.shards);
			selectindex++;
		}
	    
	    if(hostsize<=1)
	    {
	    	scheduleSingleHost(result.get(0), scheduleInfo, maxshards);
	    	return scheduleInfo;
	    }
	    
		int shardcnt = result.size();
		
		scheduleInfo.shards = new String[shardcnt];
		scheduleInfo.subShards = new String[shardcnt];
		scheduleInfo.hasSubShards = true;
		for (int i = 0; i < shardcnt; i++) {
			AssginShard as = result.get(i);
			scheduleInfo.shards[i] = as.randomMerger();
			StringBuffer subShards = new StringBuffer();
			String joinchar = "";
			for (String s : as.shards) {
				subShards.append(joinchar);
				subShards.append(s);
				joinchar = ",";
			}
			scheduleInfo.subShards[i] = subShards.toString();
			log.info("MergerSchedule host:"+scheduleInfo.shards[i] + "=>>"+ scheduleInfo.subShards[i]);
		}
		
		return scheduleInfo;
	    
	}
	
	
	private static class AssginShard{
		public List<String> ms=null;
	
		public List<String> shards=null;
		public AssginShard(List<String> ms, List<String> shards) {
			super();
			this.ms = ms;
			this.shards = shards;
		}
		
		public void expand(String[] partions)
		{
			List<String> tmp=new ArrayList<String>(shards.size()*partions.length);
			
			for(String s:this.shards)
			{
				for(String p:partions)
				{
					if(!p.isEmpty())
					{
						tmp.add(s.replaceAll("_mdrillshard_", p));
					}
				}
			}
			this.shards=tmp;
		}
		
		int index=-1;
		public String randomMerger()
		{
			if(this.index<0)
			{
				this.index=(int) (Math.random() * 100000);
			}
			
			Integer pos = (this.index++ % this.ms.size());
			return this.ms.get(pos);
		}
		
		@Override
		public String toString() {
			return "AssginShard [ms=" + ms + ", shards=" + shards + "]";
		}

	}
	
	private static HashMap<String,AssginShard> assignByHost(List<String> lst,
			List<String> mslist)
	{
		HashMap<String,ArrayList<String>> listByHost=MergeShards.getByHost(lst);
	    HashMap<String,ArrayList<String>> mslistByHost=MergeShards.getByHost(mslist);
		HashMap<String,AssginShard> assign=new HashMap<String,AssginShard>();
		
		//处理本机有merger server的情形
	    ArrayList<String> allmsHost=new ArrayList<String>();
	    for(Entry<String,ArrayList<String>> e:mslistByHost.entrySet())
	    {
	    	String host=e.getKey();
	    	allmsHost.add(host);
	    	ArrayList<String> list=listByHost.remove(host);
	    	if(list==null||list.size()<=0)
	    	{
	    		continue;
	    	}
	    	
	    	AssginShard assignlist=assign.get(host);
	    	if(assignlist==null)
	    	{
		    	ArrayList<String> localMergerList=e.getValue();
	    		assignlist=new AssginShard(localMergerList, new ArrayList<String>());
		    	assign.put(host, assignlist);
	    	}
	    	assignlist.shards.addAll(list);
	    }
	    
	    Collections.sort(allmsHost);
	    int mshostlength=allmsHost.size();
	    
	    //处理本机没有merger server的情形
	    for(Entry<String,ArrayList<String>> e:listByHost.entrySet())
	    {
	    	for(String s:e.getValue())
	    	{
	    		int hashcode=absHashcode(s.hashCode());
	    		int index=hashcode%mshostlength;
	    		String host=allmsHost.get(index);
	    		
	    		
	    		AssginShard assignlist=assign.get(host);
		    	if(assignlist==null)
		    	{
			    	List<String> val=mslistByHost.get(host);
		    		assignlist=new AssginShard(val, new ArrayList<String>());
			    	assign.put(host, assignlist);
		    	}
		    	assignlist.shards.add(s);
	    	}
	    }
	    
	    return assign;
	}
	
	
	
	private static int absHashcode(int hashcode )
	{
		if(hashcode<0)
		{
			hashcode*=-1;
		}
		
		return hashcode;
	}
	

	private static void scheduleSingleHost(AssginShard assign0,
			ScheduleInfo scheduleInfo, Integer maxshards) {
		int numshards = (assign0.shards.size() / maxshards) + 1;
		if (numshards > maxshards) {
			numshards = maxshards;
		}
		String[] ssubshards = MergeShards.split(assign0.shards, numshards);
		scheduleInfo.shards = new String[ssubshards.length];
		scheduleInfo.subShards = new String[ssubshards.length];
		scheduleInfo.hasSubShards = true;
		for (int i = 0; i < ssubshards.length; i++) {
			scheduleInfo.shards[i] = assign0.randomMerger();
			scheduleInfo.subShards[i] = ssubshards[i];
			log.info("shedule by single:" + scheduleInfo.shards[i] + "=>>"	+ scheduleInfo.subShards[i]);
		}

		log.info("MergerSchedule scheduleSingleHost:"+ scheduleInfo.shards.length + ">>" + numshards);
	}
	
}
