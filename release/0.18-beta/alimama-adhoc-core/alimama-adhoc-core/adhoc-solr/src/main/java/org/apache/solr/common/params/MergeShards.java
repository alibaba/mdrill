package org.apache.solr.common.params;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class MergeShards {
	
	public static String[] get(HashMap<String,ArrayList<String>> map,int numshards)
	{
		HashMap<Integer, StringBuffer> groups=new HashMap<Integer, StringBuffer>();
		int index=0;
		for(Entry<String,ArrayList<String>> e:map.entrySet())
		{
			ArrayList<String> lst=e.getValue();
		  	 String[] sards=lst.toArray(new String[lst.size()]);
		  	for(int i=0;i<sards.length;i++)
			  {
				  Integer g=index%numshards;
				  StringBuffer group=groups.get(g);
				  if(group==null)
				  {
					  group=new StringBuffer();
					  group.append("issubshard");
					  groups.put(g, group);
				  }
				  group.append(",");
				  group.append(sards[i]);
				  index++;
			  }
		}
		
		  String[] ssubshards=new String[groups.size()];
		  index=0;
		  for(StringBuffer s:groups.values())
		  {
			  ssubshards[index]=s.toString();
			  index++;
		  }
		  return ssubshards;
	}
	
	/**
	 * 按照host:port 解析shards
	 */
	public static HashMap<String,ArrayList<String>> getByHostport(List<String> lst)
	{
		HashMap<String,ArrayList<String>> hostposts=new HashMap<String, ArrayList<String>>();
		for(String shard:lst)
		{
	    	String[] shardparams=shard.split("@");
	    	String hostport=shardparams[0];
	    	ArrayList<String> l=hostposts.get(hostport);
	    	if(l==null)
	    	{
	    		l=new ArrayList<String>();
	    		hostposts.put(hostport, l);
	    	}
	    	l.add(shard);
		}
	  return hostposts;
	}
	
	/**
	 * 按照host解析shards
	 */
	public static HashMap<String,ArrayList<String>> getByHost(List<String> lst)
	{
		HashMap<String,ArrayList<String>> hostposts=new HashMap<String, ArrayList<String>>();
		for(String shard:lst)
		{
	    	String[] shardparams=shard.split("@");
	    	String hostport=shardparams[0];
	    	String host=hostport.split(":")[0];
	    	ArrayList<String> l=hostposts.get(host);
	    	if(l==null)
	    	{
	    		l=new ArrayList<String>();
	    		hostposts.put(host, l);
	    	}
	    	l.add(shard);
		}
	  return hostposts;
	}

}
