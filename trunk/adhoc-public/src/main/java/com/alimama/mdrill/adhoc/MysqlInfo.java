package com.alimama.mdrill.adhoc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;



public class MysqlInfo {
	
	public MysqlInfo(MySqlConn m_fpsql) {
		this.m_fpsql = m_fpsql;
	}
	
	private MySqlConn m_fpsql = null;

	public HashMap<String,String> get(String uuid) throws SQLException
	{
		Connection conn = m_fpsql.getConn();
		Statement stmt = conn.createStatement();

		String sql="select isfinish,cols,mailto,hql,cmd,env,stdmsg,errmsg,exceptionmsg,failmsg,slotcount,resultkb,rows" +
				",percent,hadoopjobid,extval,isfail,username,storedir,jobname,params,starttime,endtime,stage,md5,uuid from adhoc_download where uuid='"+uuid.replaceAll("'", "")+"' limit 10";
		ResultSet res = stmt.executeQuery(sql);
		HashMap<String,String> rtn=new HashMap<String,String>();
		rtn.put("_exehql", sql);

		while (res.next()) {
			rtn.put("cols", res.getString("cols"));
			rtn.put("storedir", res.getString("storedir"));
			rtn.put("jobname", res.getString("jobname"));
		    }
		m_fpsql.close();
		
		return rtn;
	}
	
	
	public ArrayList<HashMap<String,String>> getUser(String username,boolean onlyfinish,StringBuffer sqlbuff) throws SQLException
	{
		Connection conn = m_fpsql.getConn();
		Statement stmt = conn.createStatement();

		long d=new Date().getTime()-1000l*3600*6;
		
		String finish="";
		if(onlyfinish)
		{
			finish=" and isfinish=1  and UNIX_TIMESTAMP(starttime)>"+(d/1000);
		}
		
		String sql="select isfinish,cols,mailto,hql,cmd,env,stdmsg,errmsg,exceptionmsg,failmsg,slotcount,resultkb,rows" +
				",percent,hadoopjobid,extval,isfail,username,storedir,jobname,params,starttime,endtime,stage,md5,uuid from adhoc_download where username='"+username.replaceAll("'", "")+"' "+finish;
		sqlbuff.append(sql);
		ResultSet res = stmt.executeQuery(sql);
		ArrayList<HashMap<String,String>> rtn=new ArrayList<HashMap<String,String>>();
		while (res.next()) {
			HashMap<String,String> map=new HashMap<String,String>();
			map.put("cols", res.getString("cols"));
			map.put("storedir", res.getString("storedir"));
			map.put("isfinish", String.valueOf(res.getInt("isfinish")));
			
			rtn.add(map);
		    }
		m_fpsql.close();
		
		return rtn;
	}
	
	
	
	public ArrayList<HashMap<String,String>> getUserJobname(String username,String jobname,StringBuffer sqlbuff) throws SQLException
	{
		Connection conn = m_fpsql.getConn();
		Statement stmt = conn.createStatement();
		String sql="select isfinish,cols,mailto,hql,cmd,env,stdmsg,errmsg,exceptionmsg,failmsg,slotcount,resultkb,rows" +
				",percent,hadoopjobid,extval,isfail,username,storedir,jobname,params,starttime,endtime,stage,md5,uuid from adhoc_download where username='"+username.replaceAll("'", "")+"' and jobname='"+username.replaceAll("'", "")+"'";
		sqlbuff.append(sql);
		ResultSet res = stmt.executeQuery(sql);
		ArrayList<HashMap<String,String>> rtn=new ArrayList<HashMap<String,String>>();
		while (res.next()) {
			HashMap<String,String> map=new HashMap<String,String>();
			map.put("cols", res.getString("cols"));
			map.put("storedir", res.getString("storedir"));
			map.put("isfinish", String.valueOf(res.getInt("isfinish")));
			
			rtn.add(map);
		    }
		m_fpsql.close();
		
		return rtn;
	}
	
	

}
