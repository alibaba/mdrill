package com.alimama.quanjingmonitor.parser;


import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

import com.alimama.mdrillImport.InvalidEntryException;
import com.alimama.mdrillImport.Parser;
import com.taobao.loganalyzer.input.p4ppv.parser.*;



public class AccessLogParser
    implements Parser
{

	
	public static void main(String[] args) throws InvalidEntryException {
		AccessLogParser p=new AccessLogParser();
		System.out.println(p.parse("114.97.229.243 0.016  16279 - [30/Oct/2013:18:11:04 +0800] \"GET http://tmatch.simba.taobao.com/?name=tbuad&count=5&q2cused=0&p4p=__p4p_sidebar__%2C__p4p_bottom__&keyword=artka%B9%D9%B7%BD%C6%EC%BD%A2%B5%EA&pid=420434_1006%2C420435_1006&sort=&ip=114.97.229.243&offset=13&rct=8&propertyid=&sbid=5&o=j&t=1383128141192\" 200 5264 \"http://s.taobao.com/search?spm=a230r.1.4.3.wmVJxl&q=artka%B9%D9%B7%BD%C6%EC%BD%A2%B5%EA&rs=up&rsclick=3&tab=all&promote=0&bcoffset=1&s=40\" "));
	}
    public AccessLogParser()
    {
    }
    
    private static final String LOG_ENCODING = "GBK";

    public static Map<String,String> getName(String url)
	{
    	HashMap<String,String> rtn=new HashMap<String, String>();
    	try{
			String[] tem = url.split("\\?", 2);
			String params=tem[0];
			if (tem.length >= 2){
				params=tem[1];
			}
		
			for (String s: params.split("&", -1)) {
			    String[] tem1 = s.split("=", -1);
			    try {
				String key = URLDecoder.decode(tem1[0], LOG_ENCODING);
					String value = (tem1.length < 2
							? "" : URLDecoder.decode(tem1[1], LOG_ENCODING));
					rtn.put(key, value);
			    } catch (UnsupportedEncodingException uee) {
			    }
			}
			
    	}catch(Throwable e){}
	
		return rtn;
	 }
    
   private static  SimpleDateFormat formatter = new SimpleDateFormat("[dd/MMM/yyyy:hh:mm:ss", Locale.ENGLISH);

   
   public  static class  AccesLog{
	   public Double rt;
	   @Override
	public String toString() {
		return "AccesLog [rt=" + rt + ", ts=" + ts + ", pid=" + pid + ", name="
				+ name + "]";
	}
	   public Long ts;
	   public String pid;
	   public String name;
   }
   
	public Object parse(Object raw) throws InvalidEntryException {
		try {
			if (raw == null) {
				throw new InvalidEntryException("Invalid log `" + String.valueOf(raw) + "'");
			}
			String[] cols=((String)raw).split("[ ]+",-1);
			
			if (cols.length<7) {
				throw new InvalidEntryException("Invalid log `" + raw + "'");
			}
			
			AccesLog rtn=new AccesLog();
			
			int indexadd=0;
			if(cols[3].indexOf("[")<0)
			{
				indexadd=1;
			}
			rtn.rt=Double.parseDouble(cols[1+indexadd]);

			
			rtn.ts=formatter.parse(cols[3+indexadd]).getTime();
			String url=cols[6+indexadd].replaceAll("\"+", "");
			Map<String,String> params=getName(url);
			rtn.pid=params.get("pid");
			rtn.name=params.get("name");
			return rtn;
		} catch (Throwable nfe) {
			nfe.printStackTrace();
			throw new InvalidEntryException("Invalid log `" + raw + "'\n" + nfe);
		}
		
	}
	}
