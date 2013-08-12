package com.alimama.mdrill.partion;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;


import backtype.storm.utils.Utils;


public class GetPartions {
    public static class Shards{
	public String urlMain="";
	public String urlShards="";
	public String urlMSs="";
	public String randomShard = "";
    }
    
    
    public static class TablePartion{
    	public String name;
    	public String parttype;
    	public TablePartion(String name, String parttype) {
			super();
			this.name = name;
			this.parttype = parttype;
		}
    	
    	
    	
    }
  
    
    public static TablePartion partion(String pname)
    {
		Map stormconf = Utils.readStormConfig();
		String partiontype = (String) stormconf.get("higo.partion.type");
		String tabletype = (String) stormconf.get("higo.partion.type."+pname);
		if(tabletype!=null&&!tabletype.isEmpty())
		{
			partiontype=tabletype;
		}
    	String parttype=partiontype;
    	String projectName=pname;
    	return new TablePartion(projectName,parttype);
    }
    public static Shards getshard(TablePartion pname,String[] partionsAll,String[] cores,String[] tmp,int len,int pos) throws Exception
    {
	String projectName=pname.name;
	ArrayList<String> partions=new ArrayList<String>();
	int start=pos*len;
	for(int i=start;i<(start+len)&&i<partionsAll.length;i++)
	{
		partions.add(partionsAll[i]);
	}
	
	if(partions.size()<=0)
	{
		return null;
	}
	
	
	Shards rtn=new Shards();
        	
        Random rdm = new Random();
        String[] ms =null;
        if(tmp!=null&&tmp.length>0)
        {
            int mslen=Math.max(tmp.length, 10);
            ms=new String[mslen];
            for(int i=0;i<mslen;i++)
            {
        	Integer index=i%tmp.length;
        	ms[i]=tmp[index];
            }
        }
        
        if(ms==null||ms.length<=0)
        {
            ms=cores;
        }
        if (cores != null && cores.length > 0) {
            int count = 0;
            for (int i=0;i< cores.length;i++) {
            	String c=cores[i];
        	for(String part:partions)
        	{
        	    rtn.urlShards += c + "/solr/"+projectName+"@"+part+"@"+i+",";
        	}
        	count++;
            }
        }
        
        if (ms != null && ms.length > 0) {
            int count = 0;
            int r = rdm.nextInt(ms.length);
            int r2 = rdm.nextInt(cores.length);
            for (String c : ms) {
        	if (count == r2)
        	{
        	    rtn.randomShard = c + "/solr/"+projectName;
        	}
        	if (count == r)
        	{
        	    rtn.urlMain = "http://" + c + "/solr/"+projectName;
        	}
    	    rtn.urlMSs += c + "/solr/"+projectName+",";

        	count++;
            }
        }
        return rtn;
    }
    

   
}
