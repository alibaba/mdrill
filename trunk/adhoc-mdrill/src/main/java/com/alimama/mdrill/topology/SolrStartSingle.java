package com.alimama.mdrill.topology;

import org.apache.lucene.store.LinkFSDirectory;
import org.apache.solr.client.solrj.embedded.JettySolrRunner;
import org.apache.solr.core.SolrCore;
import org.apache.solr.core.SolrResourceLoader;
import org.apache.solr.request.join.HigoJoinUtils;

import com.alimama.mdrill.solr.realtime.ShardPartion;
import com.alimama.mdrill.utils.HadoopUtil;
import com.alimama.mdrill.utils.IndexUtils;

/**
 * ./bluewhale jar ../lib/adhoc-solr-0.18-beta.jar  com.alimama.mdrill.topology.SolrStartSingle
 * 
 * 
curl -A "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)" \
-d "shards=127.0.0.1%3A1210%2Fsolr%2Frpt_p4padhoc_cust%40201309%4059%2C&maxshards=8&mergeservers=127.0.0.1%3A1210%2Fsolr%2Frpt_p4padhoc_cust%2C&start=0&rows=0&fq=thedate%3A%5B20130901+TO+20130919%5D&q=*%3A*&facet=true&facet.sort=index&facet.cross.isdetail=true&facet.cross=true&facet.cross.join=%40&facet.cross.offset=0&facet.cross.limit=20&facet.field=thedate&facet.cross.sort.desc=true&facet.cross.sort.fl=higoempty_count_l&facet.cross.sort.cp=string" \
http://127.0.0.1:1210/solr/rpt_p4padhoc_cust/select/


http://10.246.42.76:1210/solr/rpt_p4padhoc_cust/select/?shards=10.246.42.76%3A1210%2Fsolr%2Frpt_p4padhoc_cust%40201309%4059%2C&maxshards=8&mergeservers=10.246.42.76%3A1210%2Fsolr%2Frpt_p4padhoc_cust%2C&start=0&rows=0&fq=thedate%3A%5B20130901+TO+20130919%5D&q=*%3A*&facet=true&facet.sort=index&facet.cross=true&facet.cross.join=%40&facet.cross.offset=0&facet.cross.limit=20&facet.field=thedate&facet.c&facet.cross.sort.desc=true&facet.cross.sort.fl=higoempty_count_l&facet.cross.sort.cp=string

http://10.246.42.76:1210/solr/rpt_p4padhoc_cust/select/?shards=10.246.42.76%3A1210%2Fsolr%2Frpt_p4padhoc_cust%40201309%4059%2C10.246.42.76%3A1210%2Fsolr%2Frpt_p4padhoc_cust%40201308%4059%2C&maxshards=8&mergeservers=10.246.42.76%3A1210%2Fsolr%2Frpt_p4padhoc_cust%2C&start=0&rows=0&fq=thedate%3A%5B20130701+TO+20130919%5D&q=*%3A*&facet=true&facet.sort=index&facet.cross=true&facet.cross.join=%40&facet.cross.offset=0&facet.cross.limit=20&facet.field=thedate&facet.c&facet.cross.sort.desc=true&facet.cross.sort.fl=higoempty_count_l&facet.cross.sort.cp=string

http://10.246.42.76:1210/solr/rpt_p4padhoc_cust/select/?shards=10.246.42.76%3A1210%2Fsolr%2Frpt_p4padhoc_cust%40201309%4059%2C10.246.42.76%3A1210%2Fsolr%2Frpt_p4padhoc_cust%40201308%4059%2C&maxshards=8&mergeservers=10.246.42.76%3A1210%2Fsolr%2Frpt_p4padhoc_cust%2C&start=0&rows=0&fq=thedate%3A%5B20130701+TO+20130919%5D&q=*%3A*&facet=true&facet.sort=index&facet.cross=true&facet.cross.join=%40&facet.cross.offset=0&facet.cross.limit=20&facet.field=thedate&facet.field=user_id_0_s&facet.c&facet.cross.sort.desc=true&facet.cross.sort.fl=higoempty_count_l&facet.cross.sort.cp=string


/group/tbads/p4pdata/hive_data/rpt/rpt_seller_all_crm_d/dt=20131018
 * @author yannian.mu
 *
 */
public class SolrStartSingle {
	public static void main(String[] args) throws Exception {
		LinkFSDirectory.setRealTime(true);
		SolrCore.setSearchCacheSize(32);
		String hdfsconf="/home/taobao/config";
		HadoopUtil.setHdfsConfDir(hdfsconf);
    	LinkFSDirectory.setHdfsConfDir(hdfsconf);
    	HigoJoinUtils.setHdfsConfDir(hdfsconf);
    	
    	
    	ShardPartion.base="/group/tbdp-etao-adhoc/p4padhoc/tabletest";
		ShardPartion.taskIndex=1;
		ShardPartion.index=IndexUtils.getHdfsForder(1);
    	
    	SolrResourceLoader.SetSolrHome("/home/taobao/solr");
    	SolrResourceLoader.SetSchemaHome("/home/taobao/solr/schema");
    	
    	JettySolrRunner jetty = new JettySolrRunner("/solr", 1210);
		jetty.start();
		while(true)
		{
			Thread.sleep(1000);
		}
	}
}
