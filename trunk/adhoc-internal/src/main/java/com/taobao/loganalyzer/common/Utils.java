package com.taobao.loganalyzer.common;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Set;

public class Utils {

	public static String combine(List<String> tokens, String separator) {
		StringBuffer sb = new StringBuffer();
		boolean isFirst = true;
		for (String token : tokens) {
			if (isFirst)
				isFirst = false;
			else
				sb.append(separator);
			sb.append(token);
		}
		return sb.toString();
	}

	public static String combine(Set<String> tokens, String separator) {
		StringBuffer sb = new StringBuffer();
		boolean isFirst = true;
		for (String token : tokens) {
			if (isFirst)
				isFirst = false;
			else
				sb.append(separator);
			sb.append(token);
		}
		return sb.toString();
	}
	public static String combine(String[] tokens, String separator, String nullvalue) {
		if(tokens == null || tokens.length == 0 )	return "" ;
		
		StringBuffer sb = new StringBuffer();
		for (int i=0; i < tokens.length; i++) {
			if (tokens[i] == null) {
				sb.append(nullvalue);
			} else {
				sb.append(tokens[i]) ;
			}
			sb.append(separator) ;
		}		
		return sb.substring(0, sb.length() - separator.length());
	}
	public static String combine(String[] tokens, String separator) {
		StringBuffer sb = new StringBuffer();
		boolean isFirst = true;
		for (String token : tokens) {
			if (isFirst)
				isFirst = false;
			else
				sb.append(separator);
			sb.append(token);
		}
		return sb.toString();
	}

	public static String toDateTime(String timeStamp)
	{
		  try{
			  Long ts = Long.parseLong(timeStamp)*1000; 
			  DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			  return sdf.format(new java.util.Date(ts));
		  }catch(NumberFormatException e){
			  return "";
		  }

	}
	public static String toDateTime(String timeStamp, String fmt)
	{
		  try{
			  Long ts = Long.parseLong(timeStamp)*1000; 
			  DateFormat sdf = new SimpleDateFormat(fmt);
			  return sdf.format(new java.util.Date(ts));
		  }catch(NumberFormatException e){
			  return "";
		  }

	}
	public static boolean isNum(String str) {
		if (str == null)
			return false;
		int i;
		for (i = 0; i < str.length(); i++) {
			char ch = str.charAt(i);
			if (Character.isDigit(ch) == false)
				return false;
		}
		if (i != 0) {
			return true;
		} else
			return false;
	}

}
