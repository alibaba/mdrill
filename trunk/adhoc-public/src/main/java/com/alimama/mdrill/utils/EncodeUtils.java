package com.alimama.mdrill.utils;

import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;

public class EncodeUtils {
    private static Logger LOG = Logger.getLogger(EncodeUtils.class);
    

	public static String encode(String s)
	{
		if(s.indexOf(UniqConfig.GroupJoinString())<0&&!s.endsWith(UniqConfig.GroupJoinTagString()))
		{
			return s;
		}
		
		try {
			return java.net.URLEncoder.encode(s, "utf8")+UniqConfig.GroupJoinTagString();
		} catch (UnsupportedEncodingException e) {
			LOG.error("eocode for "+s,e);
			return s.replaceAll(UniqConfig.GroupJoinString(), "");
		}
	}
	
	public static String decode(String s)
	{
		if(!s.endsWith(UniqConfig.GroupJoinTagString()))
		{
			return s;
		}
		try {
			return java.net.URLDecoder.decode(s.replaceAll(UniqConfig.GroupJoinTagString()+"$", ""), "utf8");
		} catch (UnsupportedEncodingException e) {
			LOG.error("decode for "+s,e);
			return s;
		}

	}
	
	public static String[] decode(String[] s)
	{
		if(s==null)
		{
			return s;
		}
		String[] rtn=new String[s.length];
		for(int i=0;i<s.length;i++)
		{
			rtn[i]=decode(s[i]);
		}
		
		return rtn;
	}
}
