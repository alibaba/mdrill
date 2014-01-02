package com.alimama.mdrill.topology;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.solr.common.SolrInputDocument;

import com.alimama.mdrill.partion.GetShards;
import com.alimama.mdrill.ui.service.MdrillService;

public class Test {
public static void main(String[] args) throws Exception {
	GetShards.ShardsList slist=new GetShards.ShardsList();
	 slist.list.add("127.0.0.1:1210");
	 GetShards.ShardsList[] params={slist};
	 
	 int end=Integer.parseInt(args[0]);
	 for(int i=0;i<end;i++)
	 {
		 Collection<SolrInputDocument> docs=new ArrayList<SolrInputDocument>();
		 for(int k=0;k<100;k++)
		 {
			 SolrInputDocument doc1=new SolrInputDocument();
			 SolrInputDocument doc2=new SolrInputDocument();
			 docs.add(doc1);
			 docs.add(doc2);
			 
			 doc2.addField("mdrillPartion", "201309");
			 doc2.addField("mdrillCmd", "add");
			 doc2.addField("thedate", "20130901");
			 
			 doc1.addField("mdrillPartion", "201308");
			 doc1.addField("mdrillCmd", "add");
			 doc1.addField("thedate", "20130801");
			 for(int j=0;j<10;j++)
			 {
				 doc1.addField("user_id_"+j+"_s", String.valueOf(((int)(Math.random()*10000))));
				 doc2.addField("user_id_"+j+"_s", String.valueOf(((int)(Math.random()*10000))));//java.util.UUID.randomUUID().toString()
			 }
		 }
		 String result=MdrillService.insert("rpt_p4padhoc_cust", docs, false,params,MdrillService.FlushType.buffer);
		 System.out.println(i+"@"+result);
		
		 
		 
		 
	 }
}
}
