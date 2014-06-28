package com.alimama.quanjingmonitor.mdrillImport.parse.goldeye;

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
	  
create table rpt_quanjing_p4p_k2_realtime
(

   thedate string,
    miniute_5 string,
    pvtype string comment '流量类型',
    producttype string comment '产品类型',
    pid string,
    p4ptag string,
    ttname string,
    backup1 string,
    backup2 string,
    searchtag string,
    apv bigint comment '一跳PV',
    aclick bigint comment '一跳点击',
    p4pclick bigint comment '二跳点击',
    p4pprice bigint comment '二跳消耗',
    num1 bigint ,
     num2 bigint ,
     num3 tdouble ,
     p4ppv bigint,
     p4ppid string,
     flag string
)

	  */
	 
	public static String tablename="rpt_quanjing_p4p_k2_realtime";
	public static String[] colname={
		"thedate"
		,"miniute_5"
		,"pvtype"
		,"producttype"
		,"pid"
		,"p4ptag"
		,"ttname"
		,"backup1"
		,"backup2"
		,"searchtag"
		,"p4ppid"
		
		
};
	
	public static int colname_len=colname.length;


	public static String[] colSumName={
		"apv" 
		,"aclick"
		,"p4pclick" 
		,"p4pprice"
		,"num1"
		,"num2"
		,"num3"
		,"p4ppv"
};
	public static int colSumName_len=colSumName.length;

	
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
