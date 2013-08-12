package com.alimama.mdrill.partion.thedate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

public class ThedatePartionsUtils {

	public static HashMap<String,HashSet<String>> parseDays(HashSet<String> list,String type) throws ParseException
	{
		HashMap<String,HashSet<String>> rtn=new HashMap<String, HashSet<String>>();
		for(String day:list)
		{
			String partion=ThedatePartionsUtils.parseDay(day,type);
			HashSet<String> partlist=rtn.get(partion);
			if(partlist==null)
			{
				partlist=new HashSet<String>();
				rtn.put(partion, partlist);
			}
			
			partlist.add(day);
		}
		return rtn;
	}
	
	
	  
    public static boolean canUsedThedate(String parttype)
	{
		return parttype.equals("day")||parttype.equals("default")||parttype.equals("month");
	}
    
    public static HashSet<String> partion2Days(String str,String type) 
    {
    	HashSet<String> rtn=new HashSet<String>();
    	if(type.equals("day"))
		{
    		rtn.add(str);
    		
    		return rtn;
		}
    	
    	if(type.equals("month"))
		{
    		for(int i=1;i<=31;i++)
    		{
    			String strday=str+(i<10?"0":"")+String.valueOf(i);
    			try{
    			if(parseDay(strday,type).equals(str))
    			{
    				rtn.add(strday);
    			}
    			}catch(Exception e){}
    		}
    		return rtn;
		}
    	
    	for(int i=1;i<=31;i++)
		{
			String strday=str.substring(0, 6)+(i<10?"0":"")+String.valueOf(i);
			try{
			if(parseDay(strday,type).equals(str))
			{
				rtn.add(strday);
			}
			}catch(Exception e){}
		}
		return rtn;
    	
    	
    }
    

      
//    /** 
//     * 得到指定月的天数 
//     * */  
//    public static int getMonthLastDay(Date d)  
//    {  
//        Calendar a = Calendar.getInstance();  
//        a.setTime(d);
//        a.set(Calendar.DATE, 1);//把日期设置为当月第一天  
//        a.roll(Calendar.DATE, -1);//日期回滚一天，也就是最后一天  
//        int maxDate = a.get(Calendar.DATE);  
//        return maxDate;  
//    } 
	
	public static String parseDay(String str,String type) throws ParseException
	{
		if(type.equals("single"))
		{
			return "single";
		}
		if(type.equals("day"))
		{
			return str;
		}
		
		SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
		Date d=fmt.parse(str);
		SimpleDateFormat fmtmonth = new SimpleDateFormat("yyyyMM");
		
		if(type.equals("month"))
		{
			return fmtmonth.format(d);
		}
		
		SimpleDateFormat fmtrtn = new SimpleDateFormat("dd");
		Integer rtn=Integer.parseInt(fmtrtn.format(d));
		String xun="1";
		if(rtn>10)
		{
			xun="2";
		}
		if(rtn>20)
		{
			xun="3";
		}

		return fmtmonth.format(d)+xun;
	}
}
