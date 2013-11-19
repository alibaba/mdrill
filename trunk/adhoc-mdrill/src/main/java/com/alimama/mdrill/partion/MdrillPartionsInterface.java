package com.alimama.mdrill.partion;

import java.util.HashMap;
import java.util.HashSet;

import org.apache.hadoop.fs.FileSystem;
import org.apache.solr.common.SolrInputDocument;




public interface MdrillPartionsInterface {
	public void setPartionType(String parttype);
	public String[] SqlPartions(String queryStr) throws Exception;
	public String SqlFilter(String queryStr) throws Exception;
	
	public String InsertPartion(SolrInputDocument doc)  throws Exception;
	
	public HashSet<String> getNameList(FileSystem fs,String inputBase,String startPoint,int dayDelay, int maxRunDays) throws Exception;
	public HashMap<String,HashSet<String>> indexPartions(HashSet<String> namelist,String startday,int dayDelay, int maxRunDays) throws Exception;
	public HashMap<String,String> indexVertify(HashMap<String,HashSet<String>> partions,int shards,String startday,int dayDelay, int maxRunDays) throws Exception;
	
	
	public StatListenerInterface getStatObj() throws Exception ;
}
