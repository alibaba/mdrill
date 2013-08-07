package com.alimama.mdrill.solr.realtime.mlog;

public class MLogUtil {
	public static String MLOG_EXT_NAME=".mlog";
	public static boolean isSigment(String name)
	{
		return name.endsWith(MLOG_EXT_NAME);
	}
	public static long parseSigment(String name)
	{
		int rtn=name.indexOf(MLOG_EXT_NAME);
		return Long.parseLong(name.substring(0,rtn));
	}
	
	public static String parseFile(long sigment)
	{
		return sigment+MLOG_EXT_NAME;
	}
}
