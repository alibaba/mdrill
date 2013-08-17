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

	public static void schedule(SolrParams params, 
			List<String> lst,
			List<String> mslist, 
			ResponseBuilder rb) {

		boolean isfacet = params.getBool(FacetParams.FACET_CROSS, false);

		int size = lst.size();
		Integer maxshards = params.getInt(FacetParams.MERGER_MAX_SHARDS,UniqConfig.getMaxMergerShard());
	    HashMap<String, AssginShard> host2AssginShard=assignByHost(lst, mslist);
	    int hostsize=host2AssginShard.size();
		if ((maxshards >= size&&hostsize<=1) || !isfacet) {
			rb.lockType=ResponseBuilder.RequestLockType.shards;

			rb.shards = lst.toArray(new String[lst.size()]);
			log.info("MergerSchedule shards:"+Arrays.toString(rb.shards));
			return ;
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
			rb.lockType=ResponseBuilder.RequestLockType.singlehosts;
	    	scheduleSingleHost(result.get(0), rb, maxshards);
	    	return ;
	    }
	    
		int shardcnt = result.size();
		
		rb.lockType=ResponseBuilder.RequestLockType.multy;
		rb.shards = new String[shardcnt];
		rb.subShards = new String[shardcnt];
		rb.issubshard = true;
		for (int i = 0; i < shardcnt; i++) {
			AssginShard as = result.get(i);
			rb.shards[i] = as.randomMerger();
			StringBuffer subShards = new StringBuffer();
			String joinchar = "";
			for (String s : as.shards) {
				subShards.append(joinchar);
				subShards.append(s);
				joinchar = ",";
			}
			rb.subShards[i] = subShards.toString();
			log.info("MergerSchedule host:"+rb.shards[i] + "=>>"+ rb.subShards[i]);
		}
	    
	}
	
	
	private static class AssginShard{
		public List<String> ms=null;
		public List<String> shards=null;
		public AssginShard(List<String> ms, List<String> shards) {
			super();
			this.ms = ms;
			this.shards = shards;
		}
		
		public String randomMerger()
		{
			Integer index = (((int) (Math.random() * 100000)) % this.ms.size());
			return this.ms.get(index);
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
	

	private static void scheduleSingleHost(AssginShard assign0,ResponseBuilder rb,Integer maxshards)
	{
		int numshards = (assign0.shards.size() / maxshards) + 1;
		if (numshards > maxshards) {
			numshards = maxshards;
		}
      	  
      	HashMap<String,ArrayList<String>> listByShard=MergeShards.getByHostport(assign0.shards);
        HashMap<String,ArrayList<String>> mslistByShard=MergeShards.getByHostport(assign0.ms);
    	  String[] ssubshards=MergeShards.get(listByShard, numshards);
       	  String[] msShards=MergeShards.get(mslistByShard, numshards);
       	  rb.shards=new String[ssubshards.length];
       	  rb.subShards=new String[ssubshards.length];
       	  rb.issubshard=true;
       	  for(int i=0;i<ssubshards.length;i++)
       	  {
             	StringBuffer subShards=new StringBuffer();
         		String[] cols=ssubshards[i].split(",");
         		Integer index=(((int)(Math.random()*100000))%msShards.length);
         		String[] msServer=msShards[index].split(",");
         		int rindex=(int) (Math.random()*(msServer.length-1))+1;
         		rb.shards[i]=msServer[rindex];
         		
         		String joinchar="";
         		for(int j=1;j<cols.length;j++)
         		{
         			subShards.append(joinchar);
         			subShards.append(cols[j]);
         			joinchar=",";
         		}
         		rb.subShards[i]=subShards.toString();
         		log.info("shedule by single:"+rb.shards[i]+"=>>"+rb.subShards[i]);
       	  }
       	  
		  log.info("MergerSchedule scheduleSingleHost:"+rb.shards.length+">>"+numshards);

    
	}
	
}
