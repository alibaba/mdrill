package com.alimama.mdrill.ui.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import backtype.storm.utils.Utils;

import com.alimama.mdrill.adhoc.MySqlConn;
import com.alimama.mdrill.json.JSONArray;
import com.alimama.mdrill.json.JSONException;
import com.alimama.mdrill.json.JSONObject;

public class UserJson {
	private static Logger LOG = Logger.getLogger(UserJson.class);

	private static MySqlConn getConn()
	{
		SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
		Map stormconf = Utils.readStormConfig();
		String connstr = (String) stormconf.get("higo.download.offline.conn");
		String uname = (String) stormconf.get("higo.download.offline.username");
		String passwd = (String) stormconf.get("higo.download.offline.passwd");
		MySqlConn m_fpsql = new MySqlConn(connstr, uname, passwd);
		
		return m_fpsql;
	}
	
	public static void addJsonList(String json,String user) throws Exception
	{
		JSONObject jsonObj = new JSONObject(json);
		JSONArray list=jsonObj.getJSONObject("data").getJSONArray("users");
		for(int i=0;i<list.length();i++)
		{
			JSONObject obj=list.getJSONObject(i);
			addjson(obj.toString(),user);
		}
	}
	
	public static String getJson() throws JSONException, SQLException
	{
		return getJson(null,false);
	}
	public static String getJson(String daystart,boolean extamsg) throws JSONException, SQLException
	{
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("code", "1");
		MySqlConn m_fpsql = getConn();
		
		String strSql = "select userid,email,cname,role,permission,'-' as queryday,opuser,optime from users_json order by role desc,userid";
		if(daystart!=null&&!daystart.isEmpty())
		{
			strSql = " select userid,email,cname,role,permission,opuser,optime from users_json where (cname not in (select nick from query_analyser.day_user_pv  where queryday>'"+daystart+"')) order by role desc,userid";
		}
		
		if(extamsg)
		{
			strSql = " select a.userid as userid,a.email as email ,a.cname as cname,a.role as role,a.permission as permission,a.opuser as opuser,a.optime as optime, b.queryday as queryday " +
					"from ( " +
					""+strSql+") " +
			"  a " +
			" left join (select nick,max(queryday) as queryday from query_analyser.day_user_pv group by nick ) b" +
			" on a.cname=b.nick order by role desc,queryday desc" ;
			
		}

		jsonObj.put("_exehql", strSql);

		Connection conn = m_fpsql.getConn();
		Statement stmt = conn.createStatement();
		try {
			ResultSet res = stmt.executeQuery(strSql);
			JSONArray userlist=new JSONArray();
			while (res.next()) {
				JSONObject item = new JSONObject();
				item.put("userid", String.valueOf(res.getString("userid")));
				item.put("email", String.valueOf(res.getString("email")));
				item.put("queryday", String.valueOf(res.getString("queryday")));

				
				item.put("cname", String.valueOf(res.getString("cname")));

				item.put("role", Integer.parseInt(res.getString("role")));
				item.put("permission", new JSONArray(res.getString("permission")));
				item.put("opuser", String.valueOf(res.getString("opuser")));
				item.put("optime", String.valueOf(res.getString("optime")));


				userlist.put(item);
			 }
			m_fpsql.close();
			jsonObj.put("code", "1");
			jsonObj.put("message", "success");
			JSONObject dddd = new JSONObject();
			dddd.put("users", userlist);
			jsonObj.put("data", dddd);
		} catch (Exception e) {
			jsonObj.put("message", e.toString());
			jsonObj.put("code", "0");
		}finally{
			m_fpsql.close();
		}
		
		return jsonObj.toString();
	}
	
	public static void addjson(String json,String user) throws Exception
	{
		JSONObject jsonObj = new JSONObject(json);
		String userid=jsonObj.getString("userid");
		HashMap<String,String> val=new HashMap<String, String>();
		val.put("userid", jsonObj.getString("userid"));
		val.put("email", jsonObj.getString("email"));
		val.put("cname", jsonObj.getString("cname"));
		val.put("role", String.valueOf(jsonObj.getInt("role")));
		val.put("permission", jsonObj.getJSONArray("permission").toString());
		add(userid, val,user);
	}

	
	
	public static String add(String userid,Map<String,String> val,String user) throws Exception
	{
		del(userid,user);
		return create(userid, val,user);
	}
	
	public static String del(String userid,String user) throws Exception
	{
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("code", "0");
		MySqlConn m_fpsql = getConn();

		
		Connection conn = m_fpsql.getConn();
		String strSql = "delete from users_json  where userid=? ";
		PreparedStatement m_fps = conn.prepareStatement(strSql);
		try {
			int index=1;
			m_fps.setString(index++, userid);
			m_fps.executeUpdate();
			jsonObj.put("code", "1");
		} catch (Exception e) {
			jsonObj.put("message", e.toString());
			jsonObj.put("code", "0");
		}finally{
			m_fps.close();
			m_fpsql.close();
		}
		
		return jsonObj.toString();
	}
	
	
	public static String create(String userid,Map<String,String> val,String user) throws Exception
	{
		MySqlConn m_fpsql = getConn();
		val.put("opuser", String.valueOf(user));
		val.put("optime", getNowTime());
		JSONObject jsonObj = new JSONObject();

		StringBuffer sqlbuffer=new StringBuffer();
		StringBuffer sqlbuffer2=new StringBuffer();

		String joinchar="";
		String[] indexval=new String[val.size()];
		int index=0;
		for(Entry<String, String> e:val.entrySet())
		{
			sqlbuffer.append(joinchar).append(e.getKey());
			sqlbuffer2.append(joinchar).append("?");

			joinchar=",";
			indexval[index]=e.getValue();
			index++;
		}
		
		Connection conn = m_fpsql.getConn();
		String strSql = "insert into users_json " +
				"("+sqlbuffer+")" +
				"values" +
				"("+sqlbuffer2+")";
		PreparedStatement m_fps = conn.prepareStatement(strSql);
		try {
			for(int i=0;i<indexval.length;i++)
			{
				m_fps.setString(i+1, indexval[i]);

			}
			m_fps.executeUpdate();
			jsonObj.put("code", "1");

			jsonObj.put("____debug", m_fps.toString());
		} catch (Exception e) {
			jsonObj.put("____debug2", m_fps.toString());
			jsonObj.put("____debugerror", e.toString());
			jsonObj.put("code", "0");
		}finally{
			m_fps.close();
			m_fpsql.close();
		}
		return jsonObj.toString();
	}

	private static String getNowTime()
	{
		SimpleDateFormat fmt=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		return fmt.format(new java.util.Date());

	}
	public String update(String userid,Map<String,String> val,String user) throws SQLException
	{
		MySqlConn m_fpsql = getConn();
		val.put("opuser", String.valueOf(user));
		val.put("optime", getNowTime());
		StringBuffer sqlbuffer=new StringBuffer();
		String joinchar="";
		String[] indexval=new String[val.size()];
		int index=0;
		for(Entry<String, String> e:val.entrySet())
		{
			sqlbuffer.append(joinchar).append(e.getKey()+"=?");
			joinchar=",";
			indexval[index]=e.getValue();
			index++;
		}
		
		Connection conn = m_fpsql.getConn();
		String strSql = "update users_json set " +
				" "+sqlbuffer+" where userid=? ";
		System.out.println(strSql);
		PreparedStatement m_fps = conn.prepareStatement(strSql);
		try {
			for(int i=0;i<indexval.length;i++)
			{
				m_fps.setString(i+1, indexval[i]);

			}
			m_fps.setString(indexval.length+1, userid);

			m_fps.executeUpdate();
			String fullstrSql=m_fps.toString();
			return fullstrSql;
		} catch (Exception e) {
			LOG.error("updatePercent"+m_fps.toString(),e);
		}finally{
			m_fps.close();
			m_fpsql.close();
		}
		return "";
	}
}
