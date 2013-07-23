package com.alimama.mdrill.topology;

import org.apache.lucene.store.LinkFSDirectory;
import org.apache.solr.client.solrj.embedded.JettySolrRunner;
import org.apache.solr.core.SolrCore;
import org.apache.solr.core.SolrResourceLoader;

import com.alimama.mdrill.utils.HadoopUtil;

public class SolrStartSingle {
	public static void main(String[] args) throws Exception {
		LinkFSDirectory.setRealTime(true);
		SolrCore.setSearchCacheSize(32);
		String hdfsconf="/home/taobao/config";
		HadoopUtil.setHdfsConfDir(hdfsconf);
    	LinkFSDirectory.setHdfsConfDir(hdfsconf);
    	
    	SolrResourceLoader.SetSolrHome(args[0]);
    	SolrResourceLoader.SetSchemaHome(args[1]);
    	
    	JettySolrRunner jetty = new JettySolrRunner("/solr", Integer.parseInt(args[2]));
		jetty.start();
		while(true)
		{
			Thread.sleep(1000);
		}
	}
}
