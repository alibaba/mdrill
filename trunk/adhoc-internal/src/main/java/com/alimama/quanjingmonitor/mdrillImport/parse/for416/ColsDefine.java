package com.alimama.quanjingmonitor.mdrillImport.parse.for416;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ColsDefine {
	 public static SimpleDateFormat formatDay = new SimpleDateFormat("yyyyMMdd");
	 public static SimpleDateFormat formatDayHHMM = new SimpleDateFormat("yyyyMMddHHmm");

	 public static SimpleDateFormat formatMin = new SimpleDateFormat("HHmm");
	 public static SimpleDateFormat formatDayMin = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	 
	 static long  step=1000l*600;
	 public static String version=formatDayHHMM.format(new Date((System.currentTimeMillis()/step)*step));
	 
	 /**
	  
	  create table rpt_416_online(
	  	thedate string,
	  	miniute_5 string,
	  	source string,
	  	source_tt string,
	  	media_pid string,
	  	channel string,
	  	tid string,
	  	tp1 string,
	  	tp2 string,
	  	pv_2 tlong,
	  	click_1 tlong,
	  	click_2 tlong,
	  	promise_click tlong,
	  	pc_2_wap tlong,
	  	weakup tlong,
	  	backup_1 tlong,
	  	backup_2 tlong,
	  	backup_3 tlong
	  )
	  */
	 
	public static String tablename="rpt_416_online";
	public static String[] colname={
		"thedate"
		,"miniute_5"
		,"source"
		,"source_tt"
		,"media_pid"
		,"channel"
		,"tid"
		,"tp1"
		,"tp2"
};

	public static String[] colSumName={
		"pv_2" //pc or huodong
		,"click_1"
		,"click_2"  //pc or huodong
		,"promise_click"
		,"pc_2_wap"
		,"weakup"
		,"backup_1"//zhadui pv2
		,"backup_2"//zhadui click2
		,"backup_3" //saoma
};
	
	
	 public static String decodeString(String args) {
			try {
				return new String(java.net.URLDecoder.decode(args,"UTF-8")	.getBytes("UTF-8"), "UTF-8");
			} catch (Throwable e) {
				try {
					return new String(java.net.URLDecoder.decode(args,"GBK")	.getBytes("UTF-8"), "UTF-8");
				} catch (Throwable e2) {
					return args;
				}
			}
		}
	    
	    
	    public static String getNameNodecode(String url,String keyname)
		{
	    	try{
				String[] tem = url.split("\\?", 2);
				String params=tem[0];
				if (tem.length >= 2){
					params=tem[1];
				}
			
				for (String s: params.split("&", -1)) {
				    String[] tem1 = s.split("=", -1);
				    String key = decodeString(tem1[0]);
					if(key.equals(keyname))
					{
						String value = (tem1.length < 2
								? "" : decodeString(tem1[1]));
						return value;
					}
				}
	    	}catch(Throwable e){}
			return null;
		 }
	    
	    public static String getName(String url,String keyname)
		{
	    	try{
				String[] tem = decodeString(url).split("\\?", 2);
				String params=tem[0];
				if (tem.length >= 2){
					params=tem[1];
				}
			
				for (String s: params.split("&", -1)) {
				    String[] tem1 = s.split("=", -1);
				    String key = decodeString(tem1[0]);
					if(key.equals(keyname))
					{
						String value = (tem1.length < 2
								? "" : decodeString(tem1[1]));
						return value;
					}
				}
	    	}catch(Throwable e){}
			return null;
		 }
}
