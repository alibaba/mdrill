package com.alimama.mdrill.ui.service.partions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.alimama.mdrill.json.JSONArray;
import com.alimama.mdrill.json.JSONException;
import com.alimama.mdrill.json.JSONObject;
import com.alimama.mdrill.partion.thedate.ThedatePartionsUtils;
import com.alimama.mdrill.ui.service.utils.WebServiceParams;

public class AdhocHivePartions {
	public static class KmeansQueryParse{
		public String[] days;
		public String queryStr;
		public String[] getSortDays()
		{
			Arrays.sort(days);
			String[] rtn=new String[days.length];
			for(int i=0;i<rtn.length;i++)
			{
				rtn[i]=days[days.length-(i+1)];
			}
			return rtn;
		}
	}
	public static KmeansQueryParse getKmeansDays(String queryStr) throws Exception
    {
    	HashSet<String> rtn=new HashSet<String>();
    	
    	SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
    	boolean isset=false;

    	Long small=-1l;
    	Long bigger=-1l;
    	JSONArray jsonStr=new JSONArray(queryStr.trim());
    	JSONArray jsonStr_new=new JSONArray();

    	for(int j=0;j<jsonStr.length();j++)
    	{
    		JSONObject obj=jsonStr.getJSONObject(j);
    		if(!obj.has("thedate"))
    		{
    			jsonStr_new.put(obj);
    			continue;
    		}
    		
    		

		
    	}
    	
    	for(int j=0;j<jsonStr.length();j++)
    	{
    		JSONObject obj=jsonStr.getJSONObject(j);
    		if(!obj.has("thedate"))
    		{
    			continue;
    		}
    		
    		

			JSONObject thedate=obj.getJSONObject("thedate");
			Integer operate=Integer.parseInt(thedate.get("operate").toString());
			String[] val=WebServiceParams.parseFqValue(thedate.getString("value"), operate).split(",");
			if(operate==1 ||operate==5||operate==6||operate==7||operate==8||operate==9)//=,range
			{
				Long min=Long.MAX_VALUE;
				Long max=Long.MIN_VALUE;
    		    for(String day:val)
    		    {
    		    	Date d=fmt.parse(day.replaceAll("-", ""));
    		    	Long time=d.getTime();
    		    	max=Math.max(max, time);
    		    	min=Math.min(min, time);
    		    	isset=true;
    		    }
    		    String[] days=getKmeans(min, max);
    		    for(String day:days)
    		    {
    		    	rtn.add(day);
    		    }
    		    break;
    		    
			}
			
			if(operate==13||operate==3)//<,<=
			{
    		    for(String day:val)
    		    {
    		    	Date d=fmt.parse(day.replaceAll("-", ""));
    		    	Long time=d.getTime()+(operate==3?0:1);
    		    	small=small>0?Math.min(small, time):time;
    		    }
			}
			
			if(operate==14||operate==4)//>,>=
			{
    		    for(String day:val)
    		    {
    		    	Date d=fmt.parse(day.replaceAll("-", ""));
    		    	Long time=d.getTime()-(operate==4?0:1);
    		    	bigger=bigger>0?Math.max(bigger, time):time;
    		    }
			}
			
			if(bigger>0&&small>0)
			{
				isset=true;
				 String[] days=getKmeans(small, bigger);
	    		    for(String day:days)
	    		    {
	    		    	rtn.add(day);
	    		    }
    		    small=Long.MAX_VALUE;
    			bigger=Long.MIN_VALUE;
	    		break;
			}
		   
		
    	}

	

    	if(!isset)
    	{
    		throw new Exception("no thedate");
    	}
	
    	String[] rtnarr=new String[rtn.size()];
    	
    	KmeansQueryParse kmeans=new KmeansQueryParse();
    	kmeans.days=rtn.toArray(rtnarr);
    	kmeans.queryStr=jsonStr_new.toString();
    	return kmeans;
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
	        		    	Date d=fmt.parse(day.replaceAll("-", ""));
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
	        		    	Date d=fmt.parse(day.replaceAll("-", ""));
	        		    	Long time=d.getTime()+(operate==3?0:1);
	        		    	small=small>0?Math.min(small, time):time;
	        		    }
	    			}
	    			
	    			if(operate==14||operate==4)//>,>=
	    			{
	        		    for(String day:val)
	        		    {
	        		    	Date d=fmt.parse(day.replaceAll("-", ""));
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
	    
	    
	 private static String[] getKmeans(Long min,Long max) throws JSONException, ParseException
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
			
			String[] rtnarr=new String[list.size()];
			return list.toArray(rtnarr);
	    }
	 
	    
	    private static String[] get(Long min,Long max,String parttype) throws JSONException, ParseException
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
			HashMap<String,HashSet<String>> rtn=ThedatePartionsUtils.parseDays(list,parttype);
			Set<String> ks=rtn.keySet();
			String[] rtnarr=new String[ks.size()];
			return ks.toArray(rtnarr);
	    }
}
