package com.alimama.mdrill.partion;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.solr.client.solrj.SolrServerException;

import com.alimama.mdrill.topology.SolrStartJetty;
import com.alipay.bluewhale.core.cluster.SolrInfo;
import com.alipay.bluewhale.core.cluster.SolrInfo.ShardCount;

public class PartionListener  implements StatListenerInterface{

	@Override
	public void init() {
	}

	private String parttype;
	@Override
	public void setPartionType(String parttype) {
		this.parttype=parttype;
	}
	
	public String getPartionType()
	{
		return this.parttype;
	}

	
	
	
	private ArrayList<String> tablePartions = new ArrayList<String>();
	private HashSet<String> tablePartionsSet = new HashSet<String>();
	private HashMap<String, ShardCount> recorecount = new HashMap<String, ShardCount>();
	private AtomicInteger lastPartionIndex = new AtomicInteger(0);

	public boolean containsTablePartion(String partion)
	{
		return this.tablePartionsSet.contains(partion);
	}
	
	private void requestPartions(SolrStartJetty solrservice,String tablename) throws MalformedURLException, SolrServerException
	{
		String partion = "";
		int len = tablePartions.size();
		if (len > 0) {
			int index = lastPartionIndex.incrementAndGet();
			if (index >= len) {
				index = 0;
				lastPartionIndex.set(index);
			}
			partion = tablePartions.get(index);
		}
		if (partion != null) {
			long cnt = solrservice.checkSolr(tablename, partion);
			this.recorecount.put(partion, new ShardCount(cnt));
	
		}
	}
	
	@Override
	public void fetchCount(SolrStartJetty solrservice, String tablename,
			GetPartions.TablePartion part) throws MalformedURLException,
			SolrServerException {
		this.requestPartions(solrservice,tablename);
	}
	

	@Override
	public void syncClearPartions() {
		tablePartions.clear();
		tablePartionsSet.clear();
	}
	
	@Override
	public void syncClearStat() {
		HashSet<String> toclear = new HashSet<String>();
		for(String k:recorecount.keySet())
		{
			if(!this.containsTablePartion(k))
			{
				toclear.add(k);
			}
		}
		
		for(String k:toclear)
		{
			this.recorecount.remove(k);
		}

	}

	@Override
	public void addPartionStat(String partion) {
		tablePartions.add(partion);
		tablePartionsSet.add(partion);
	}

	

	

	@Override
	public HashMap<String, ShardCount> getPartioncount() {
		return recorecount;
	}

	@Override
	public HashMap<String, ShardCount> getExtaCount() {
		return new HashMap<String, SolrInfo.ShardCount>();
	}

}
