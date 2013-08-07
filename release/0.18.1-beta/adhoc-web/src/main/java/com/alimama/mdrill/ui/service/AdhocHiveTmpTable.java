package com.alimama.mdrill.ui.service;


public class AdhocHiveTmpTable {
	public static  String createTmpTable(String tablename,String[] cols,String split)
	{
		StringBuffer buffer=new StringBuffer();
		buffer.append("CREATE EXTERNAL TABLE IF NOT EXISTS "+tablename);
		buffer.append(" (");
		String joinchar="";
		for(String s:cols)
		{
			buffer.append(joinchar);
			buffer.append(s+" String ");
			joinchar=",";
		}
		
		buffer.append(")");
		buffer.append(" PARTITIONED BY (ppp string) ");
		buffer.append(" ROW FORMAT DELIMITED ");
		buffer.append(" FIELDS TERMINATED BY '"+parseSplit(split)+"' ");
		buffer.append(" LINES TERMINATED BY '\\n' ");
		buffer.append(" STORED AS TextFile ");
		return buffer.toString();
	}
	
	private static String parseSplit(String split)
	{
		if(split.equals("\001"))
		{
			return "\\001";
		}
		
		
		return split;
	}
	
	
	public static String addData(String tablename,String storepath)
	{
		StringBuffer buffer=new StringBuffer();
		buffer.append("ALTER TABLE "+tablename+" ADD IF NOT EXISTS PARTITION (ppp='"+System.currentTimeMillis()+"') ");
		buffer.append(" LOCATION '"+storepath+"'");
		return buffer.toString();
	}
	
	
	public static String dropTable(String tablename)
	{
		StringBuffer buffer=new StringBuffer();
		buffer.append("drop TABLE "+tablename+" ");
		return buffer.toString();
	}
	
	
}
