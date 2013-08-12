package com.alimama.mdrill.partion;

import java.net.MalformedURLException;
import java.util.HashMap;

import org.apache.solr.client.solrj.SolrServerException;

import com.alimama.mdrill.partion.GetPartions.TablePartion;
import com.alimama.mdrill.topology.SolrStartJetty;
import com.alipay.bluewhale.core.cluster.SolrInfo.ShardCount;

public interface StatListenerInterface {
	public void init();
	public void setPartionType(String parttype);
	public void syncClearPartions();
	public void addPartionStat(String partion);
	public void syncClearStat();
	public void fetchCount(SolrStartJetty solrservice,String tablename,TablePartion part) throws MalformedURLException, SolrServerException;
	public HashMap<String, ShardCount> getPartioncount() ;
	public HashMap<String, ShardCount> getExtaCount() ;
}
