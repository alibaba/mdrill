package com.alimama.mdrill.partion.thedate;

import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.solr.client.solrj.SolrServerException;

import com.alimama.mdrill.json.JSONException;
import com.alimama.mdrill.partion.PartionListener;
import com.alimama.mdrill.partion.GetPartions.*;
import com.alimama.mdrill.topology.SolrStartJetty;
import com.alipay.bluewhale.core.cluster.SolrInfo;
import com.alipay.bluewhale.core.cluster.SolrInfo.ShardCount;
public class ThedateListener extends PartionListener{
	private HashMap<String, String> dayPartions=new HashMap<String, String>();
	private HashMap<String, ShardCount> daycount = new HashMap<String, ShardCount>();
	private AtomicInteger lastDayIndex = new AtomicInteger(0);

	
	private String smallestPartion=null;
	private HashMap<String, ShardCount> smallest = new HashMap<String, ShardCount>();
	private AtomicInteger lastsmallDayIndex = new AtomicInteger(0);

	private String biggestPartion=null;
	private HashMap<String, ShardCount> biggest = new HashMap<String, ShardCount>();
	private AtomicInteger lastbigDayIndex = new AtomicInteger(0);

	private void requestDaycount(SolrStartJetty solrservice,String tablename) throws MalformedURLException, SolrServerException
	{
		ArrayList<String> toremove=new ArrayList<String>();
		for(String strday:this.daycount.keySet())
		{
			if(!dayPartions.containsKey(strday))
    		{
				toremove.add(strday);
    		}
		}
		
		for(String rmday:toremove)
		{
			this.daycount.remove(rmday);
		}
		
		int loopcount=1;
		if(this.daycount.size()<=1)
		{
			loopcount=7;
		}
		
		
		for(int i=0;i<loopcount;i++)
		{
			if(i>0)
			{
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {}
			}
    		int index = lastDayIndex.incrementAndGet();
    		if (index >= 7) {
    			index = 0;
    			lastDayIndex.set(0);
    			index=lastDayIndex.get();
    		}
    		
    		long dayl=System.currentTimeMillis()-1000l*3600*24*index;
    		SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
    		String strday=fmt.format(new Date(dayl));
    		if(dayPartions.containsKey(strday))
    		{
    			String part=dayPartions.get(strday);
    			if(this.containsTablePartion(part))
    			{
    				long cnt = solrservice.checkSolr(tablename, part,strday);
    				this.daycount.put(strday, new ShardCount(cnt));
    			}
    		}
		}
	}
	
	private void requestSmallest(SolrStartJetty solrservice,String tablename,TablePartion part) throws MalformedURLException, SolrServerException
	{
		if(this.smallestPartion==null)
		{
			return ;
		}
		
		ArrayList<String> toremove=new ArrayList<String>();

		HashSet<String> smallestdays=ThedatePartionsUtils.partion2Days(smallestPartion, part.parttype);
		String[] smallestdaysarr=smallestdays.toArray(new String[smallestdays.size()]);
		Arrays.sort(smallestdaysarr);
		toremove.clear();
		for(String strday:this.smallest.keySet())
		{
			if(!smallestdays.contains(strday))
    		{
				toremove.add(strday);
    		}
		}
		
		for(String rmday:toremove)
		{
			this.smallest.remove(rmday);
		}
		
		
		int smallloopcnt=1;
		if(this.smallest.size()<1)
		{
			smallloopcnt=smallestdaysarr.length;
		}
		
		for(int i=0;i<smallloopcnt;i++)
		{
			if(i>0)
			{
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {}
			}
    		int index = lastsmallDayIndex.incrementAndGet();
    		if (index >= smallestdaysarr.length) {
    			index = 0;
    			lastsmallDayIndex.set(0);
    		}
    		String strday=smallestdaysarr[index];
    		long cnt = solrservice.checkSolr(tablename, smallestPartion,strday);
			this.smallest.put(strday, new ShardCount(cnt));
		}
	}
	
	private void requestBiggest(SolrStartJetty solrservice,String tablename,TablePartion part) throws MalformedURLException, SolrServerException
	{
		if(this.biggestPartion==null)
		{
			return ;
		}
		
		ArrayList<String> toremove=new ArrayList<String>();

		HashSet<String> biggestestdays=ThedatePartionsUtils.partion2Days(biggestPartion, part.parttype);
		String[] biggestestdaysarr=biggestestdays.toArray(new String[biggestestdays.size()]);
		Arrays.sort(biggestestdaysarr);
		toremove.clear();
		for(String strday:this.biggest.keySet())
		{
			if(!biggestestdays.contains(strday))
    		{
				toremove.add(strday);
    		}
		}
		
		for(String rmday:toremove)
		{
			this.biggest.remove(rmday);
		}
		
		
		int smallloopcnt=1;
		if(this.biggest.size()<1)
		{
			smallloopcnt=biggestestdaysarr.length;
		}
		
		for(int i=0;i<smallloopcnt;i++)
		{
			if(i>0)
			{
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {}
			}
    		int index = lastbigDayIndex.incrementAndGet();
    		if (index >= biggestestdaysarr.length) {
    			index = 0;
    			lastbigDayIndex.set(0);
    		}
    		String strday=biggestestdaysarr[index];
    		long cnt = solrservice.checkSolr(tablename, biggestPartion,strday);
			this.biggest.put(strday, new ShardCount(cnt));
		}
	}
		
	public void fetchCount(SolrStartJetty solrservice,String tablename,TablePartion part) throws MalformedURLException, SolrServerException
	{
		super.fetchCount(solrservice, tablename, part);
		
		if(ThedatePartionsUtils.canUsedThedate(part.parttype))
		{
			this.requestDaycount(solrservice,tablename);
			this.requestSmallest(solrservice,tablename,part);
			this.requestBiggest(solrservice, tablename, part);
		}
	}
	
	public void syncClearPartions()
	{
		super.syncClearPartions();
		smallestPartion=null;
		biggestPartion=null;
	
	}
	public void addPartionStat(String partion)
	{
		super.addPartionStat(partion);
		if(smallestPartion==null||partion.compareTo(smallestPartion)<=0)
		{
			smallestPartion=partion;
		}
		if(biggestPartion==null||partion.compareTo(biggestPartion)>=0)
		{
			biggestPartion=partion;
		}
	}
	
	public void syncClearStat()
	{
		super.syncClearStat();
		long now=System.currentTimeMillis();
		dayPartions.clear();
		try {

			 HashMap<String, HashSet<String>> w=dayPartion(now-1000l*3600*24*15, now+1000l*3600*24*1, this.getPartionType());
			 for(Entry<String, HashSet<String>> e:w.entrySet())
			 {
				 String part=e.getKey();
				 for(String day:e.getValue())
				 {
					 dayPartions.put(day, part);
				 }
			 }
		} catch (Exception e1) {
		}
	}
	
	 private HashMap<String,HashSet<String>> dayPartion(Long min,Long max,String parttype) throws JSONException, ParseException
	    {
			Long step=1000l*3600*24;
			Long start=min;
			HashSet<String> list=new HashSet<String>();
			SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
			while(start<=max)
			{
			    list.add(fmt.format(new Date(start)));
			    start+=step;
			}
			return ThedatePartionsUtils.parseDays(list,parttype);
	    }
	
	public HashMap<String, ShardCount> getSmallestDaycount() {
		return this.topMap(this.smallest, false, 2);
	}
	
	public HashMap<String, ShardCount> getBiggestDaycount() {
		HashMap<String, ShardCount> daystat=new HashMap<String, SolrInfo.ShardCount>();
		daystat.putAll(this.daycount);
		daystat.putAll(this.biggest);
		return this.topMap(daystat, true, 5);
	}
	public HashMap<String, ShardCount> getPartioncount() {
		return super.getPartioncount();
	}
	
	public HashMap<String, ShardCount> getExtaCount(){
		HashMap<String, ShardCount> daystat=new HashMap<String, SolrInfo.ShardCount>();
		daystat.putAll(this.getSmallestDaycount());
		daystat.putAll(this.getBiggestDaycount());
		return daystat;
	}

	public HashMap<String, ShardCount> topMap(HashMap<String, ShardCount> map,boolean isdesc,int num)
	{
		ArrayList<shardCountDayCmp> list=new ArrayList<shardCountDayCmp>();
		for(Entry<String, ShardCount> e:map.entrySet())
		{
			ShardCount sc=e.getValue();
			if(sc.cnt>0)
			{
				list.add(new shardCountDayCmp(e.getKey(), sc,isdesc));
			}
		}
		
		Collections.sort(list);
		
		HashMap<String, ShardCount> daystat=new HashMap<String, SolrInfo.ShardCount>();
		for(int i=0;i<num&&i<list.size();i++)
		{
			shardCountDayCmp cmp=list.get(i);
			daystat.put(cmp.day, cmp.shardCount);
		}
		
		return daystat;
	}
	
	private static class shardCountDayCmp implements Comparable<shardCountDayCmp>{
		public String day;
		public ShardCount shardCount;
		public boolean isdesc=false;
		public shardCountDayCmp(String day, ShardCount shardCount,boolean isdesc) {
			super();
			this.day = day;
			this.shardCount = shardCount;
			this.isdesc=isdesc;
		}
		@Override
		public int compareTo(shardCountDayCmp o) {
			int rtn= this.day.compareTo(o.day);
			if(isdesc)
			{
				rtn*=-1;
			}
			return rtn;
		}
	}
}
