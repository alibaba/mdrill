package com.alimama.mdrill.partion;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;


import backtype.storm.utils.Utils;

import com.alimama.mdrill.json.JSONArray;
import com.alimama.mdrill.json.JSONException;
import com.alimama.mdrill.json.JSONObject;
import com.alimama.mdrill.ui.service.utils.WebServiceParams;

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
        	
        //����response��json
        //��ȡ������---------------------------------------------------------
        // loadbalance���֡��������һ��shard��Ϊmergeserver��
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
    
    
    public static String[] get(String queryStr,String parttype) throws JSONException, ParseException
    {
    	HashSet<String> rtn=new HashSet<String>();
    	
    	SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
    	boolean isset=false;

    	Long small=-1l;
    	Long bigger=-1l;
    	JSONArray jsonStr=new JSONArray(queryStr.trim());

    	for(int j=0;j<jsonStr.length();j++)
    	{
    		JSONObject obj=jsonStr.getJSONObject(j);
    		if(obj.has("thedate"))
    		{
    			JSONObject thedate=obj.getJSONObject("thedate");
    			Integer operate=Integer.parseInt(thedate.get("operate").toString());
    			String[] val=WebServiceParams.parseFqValue(thedate.getString("value"), operate).split(",");
    			if(operate==1 ||operate==5||operate==6||operate==7||operate==8||operate==9)//=,range
    			{
    				Long min=Long.MAX_VALUE;
    				Long max=Long.MIN_VALUE;
        		    for(String day:val)
        		    {
        		    	Date d=fmt.parse(day);
        		    	Long time=d.getTime();
        		    	max=Math.max(max, time);
        		    	min=Math.min(min, time);
        		    	isset=true;
        		    }
        		    String[] partions=get(min, max, parttype);
        		    for(String partion:partions)
        		    {
        		    	rtn.add(partion);
        		    }
        		    break;
        		    
    			}
    			
    			if(operate==13||operate==3)//<,<=
    			{
        		    for(String day:val)
        		    {
        		    	Date d=fmt.parse(day);
        		    	Long time=d.getTime()+(operate==3?0:1);
        		    	small=small>0?Math.min(small, time):time;
        		    }
    			}
    			
    			if(operate==14||operate==4)//>,>=
    			{
        		    for(String day:val)
        		    {
        		    	Date d=fmt.parse(day);
        		    	Long time=d.getTime()-(operate==4?0:1);
        		    	bigger=bigger>0?Math.max(bigger, time):time;
        		    }
    			}
    			
    			if(bigger>0&&small>0)
    			{
    				isset=true;
    				 String[] partions=get(small, bigger, parttype);
    	    		    for(String partion:partions)
    	    		    {
    	    		    	rtn.add(partion);
    	    		    }
        		    small=Long.MAX_VALUE;
        			bigger=Long.MIN_VALUE;
    	    		break;
    			}
    		   
    		}
    	}

	


    	if(isset)
    	{
    		String[] rtnarr=new String[rtn.size()];
    		return rtn.toArray(rtnarr);
    	}
	
    	Long step=1000l*3600*24;
    	Long max=0l;
    	Long initDate=(new Date()).getTime();
    	max=initDate;
    	Long min=initDate-step;
    	return get(min, max, parttype);
    }
    
    
    
    public static String[] get(Long min,Long max,String parttype) throws JSONException, ParseException
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
		HashMap<String,HashSet<String>> rtn=Partions.parseDays(list,parttype);
		Set<String> ks=rtn.keySet();
		String[] rtnarr=new String[ks.size()];
		return ks.toArray(rtnarr);
    }
    
    public static HashMap<String,HashSet<String>> dayPartion(Long min,Long max,String parttype) throws JSONException, ParseException
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
		return Partions.parseDays(list,parttype);
    }
}
