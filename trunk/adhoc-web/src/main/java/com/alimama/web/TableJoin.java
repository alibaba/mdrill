package com.alimama.web;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Counters;
import org.apache.hadoop.mapreduce.Job;
import org.apache.log4j.Logger;

import backtype.storm.utils.Utils;

import com.alimama.mdrill.adhoc.ExecutorSerives;
import com.alimama.mdrill.adhoc.MySqlConn;
import com.alimama.mdrill.adhoc.MysqlInfo;
import com.alimama.mdrill.index.MakeIndex;
import com.alimama.mdrill.index.MakeIndex.updateStatus;
import com.alimama.mdrill.json.JSONArray;
import com.alimama.mdrill.json.JSONException;
import com.alimama.mdrill.json.JSONObject;
import com.alimama.mdrill.ui.service.MdrillService;
import com.alimama.mdrill.utils.HadoopUtil;
import com.alimama.web.adhoc.Upload;
/**
 * createTable.jsp
 * tableJoinList.jsp
 * upload.jsp
 * download2table.jsp
 * downloadoffline.jsp
 * downloadjoin.jsp
 * deleteDownload.jsp
 * deleteTable.jsp
 * 
 * @author yannian.mu
 *
 */
public class TableJoin {
	private static Logger LOG = Logger.getLogger(TableJoin.class);

	public static void addTxt(HttpServletRequest request, HttpServletResponse response,OutputStreamWriter outStream) throws Exception
	{
		SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
		String day = fmt.format(new Date());
		Map stormconf = Utils.readStormConfig();
		String store = (String) stormconf.get("higo.download.offline.store") + "/" + day + "/upload_" + java.util.UUID.randomUUID().toString();
		
		Configuration conf=getConf(stormconf);
		FileSystem fs=FileSystem.get(conf);
		if(!fs.exists(new Path(store)))
		{
			fs.mkdirs(new Path(store));
		}
		
		HashMap<String,String> params=new HashMap<String, String>();
		Path outpath=new Path(store,String.valueOf(System.currentTimeMillis()));
		FSDataOutputStream out=fs.create(outpath);
	    OutputStreamWriter osw = new OutputStreamWriter(out, "UTF-8");

		Upload up=new Upload();
		up.mergerTo(request, response, "gbk", osw, params);
		osw.close();
		out.close();
		String rtn=  addTxt(params.get("tableName"), store,params.get("callback"));
		
		
		outStream.append(rtn);
	}
	
	
	public static String addDownload2Table(final String tableName,final String uuid,String callback) throws SQLException, JSONException, IOException
	{
		Map stormconf = Utils.readStormConfig();
		String hdpConf = (String) stormconf.get("hadoop.conf.dir");
		String connstr = (String) stormconf.get("higo.download.offline.conn");
		String uname = (String) stormconf.get("higo.download.offline.username");
		String passwd = (String) stormconf.get("higo.download.offline.passwd");
		MySqlConn conn = new MySqlConn(connstr, uname, passwd);
		MysqlInfo info = new MysqlInfo(conn);
		HashMap<String, String> result = info.get(uuid);
		
		String rtn= addTxt(tableName, result.get("storedir"),callback);
		setJoinDownload(tableName, uuid,null);
		DeleteDownload(uuid,null);
		return rtn;
	}
	
	public static void readJoinResult(String uuid, OutputStreamWriter outStream)
	throws SQLException, IOException, JSONException {
		final HashMap<String,String> tableInfo=getTableInfo(uuid);
		JSONObject jsonObj = new JSONObject();

		if(tableInfo==null||tableInfo.isEmpty())
		{
			jsonObj.put("code", "0");
			jsonObj.put("message",  "该表不存在");
			outStream.append(jsonObj.toString());
			return ;
		}
		if(tableInfo.get("status").equals("INDEXING"))
		{
			jsonObj.put("code", "0");
			jsonObj.put("message", "正在创建索引中请稍后");
			outStream.append(jsonObj.toString());
			return ;
		}
		Map stormconf = Utils.readStormConfig();
		
		Configuration conf=getConf(stormconf);
		outStream.write(tableInfo.get("colsShowName").replaceAll("\001", ",")
				.replaceAll("\t", ",")
				+ "\n");
		com.alimama.mdrill.ui.service.AdhocOfflineService.readHiveResult(tableInfo.get("txtStorePath"), outStream, conf);


}
	
	
	public static String getDownloadId(String uuid)
	throws SQLException, IOException, JSONException {
		final HashMap<String,String> tableInfo=getTableInfo(uuid);
		if(tableInfo==null||tableInfo.isEmpty())
		{
			return "adhoc.csv";
		}
		
		String tablename=tableInfo.get("tableShowName");
		
		if(tablename.isEmpty())
		{
			return "adhoc.csv";
		}
		return tablename.replaceAll("[\n|\\/|:|\t|\\.|\\\\|\\?|<|>|\\*|\\?|\\|\"]", "_")+".csv";

}
	
	
    private static final ExecutorService       EXECUTE  = ExecutorSerives.EXECUTE;

	public static String addTxt(final String tableName,final String store,final String callback) throws JSONException, SQLException, IOException 
	{
		JSONObject jsonObj = new JSONObject();
		final HashMap<String,String> tableInfo=getTableInfo(tableName);
		if(tableInfo==null||tableInfo.isEmpty())
		{
			jsonObj.put("code", "0");
			jsonObj.put("message",  "该表不存在");
			if (callback != null && callback.length() > 0) {
				return callback + "(" + jsonObj.toString() + ")";
			} else {
				return jsonObj.toString();
			}
		}
		if(tableInfo.get("status").equals("INDEXING"))
		{
			jsonObj.put("code", "0");
			jsonObj.put("message", "正在创建索引中请稍后");
			if (callback != null && callback.length() > 0) {
				return callback + "(" + jsonObj.toString() + ")";
			} else {
				return jsonObj.toString();
			}
		}
		final Map stormconf = Utils.readStormConfig();
		final Configuration conf=getConf(stormconf);
		final FileSystem fs=FileSystem.get(conf);
		long size=HadoopUtil.duSize(fs, new Path(store));
		if(size>500l*1024*1024)
		{
			jsonObj.put("code", "0");
			jsonObj.put("message", "上传的文件不能超过500m");
			if (callback != null && callback.length() > 0) {
				return callback + "(" + jsonObj.toString() + ")";
			} else {
				return jsonObj.toString();
			}
		}

		
		TableJoin.updatePercent(tableName, "Stage-1 map = 0%,  reduce = 0%", "INDEXING")	;

		jsonObj.put("code", "1");

		EXECUTE.submit(new Runnable() {
			@Override
			public void run() {
				try{
				SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
				String day = fmt.format(new Date());
				
				if(!fs.exists(new Path(store)))
				{
					fs.mkdirs(new Path(store));
				}
				
				
				Path txtpath=new Path(tableInfo.get("txtStorePath"));
				if(!fs.exists(txtpath))
				{
					fs.mkdirs(txtpath);
				}
				
				for( FileStatus outpath:fs.listStatus(new Path(store)))
				{
					if(!outpath.isDir())
					{
						fs.rename(outpath.getPath(), new Path(txtpath,outpath.getPath().getName()+"_"+System.currentTimeMillis()));
					}
				}
				fs.delete(new Path(store),true);
				
				Path basepath=new Path(MdrillService.getBasePath());
				FileStatus[] tablelist=fs.listStatus(basepath);
				String solrHome=tablelist[0].getPath().toString();

				for(FileStatus tbl:tablelist){
					if(tbl.isDir()&&fs.exists(new Path(tbl.getPath(),"solr")))
					{
						solrHome=tbl.getPath().toString();
					}
				}
				
				HashSet<String> inputs=new HashSet<String>();
				inputs.add(txtpath.getName());
				

				TableJoin.updateKb(tableName, HadoopUtil.duSize(fs, txtpath));
				                          
				String strjoins="";
				if(tableInfo!=null&&tableInfo.get("joins")!=null)
				{
					for(String s:tableInfo.get("joins").split(","))
					{
						String[] cols=s.split(":");
						if(cols.length>=3)
						{
							strjoins=cols[2];
						}
					}
				}
				
				String index = (String) stormconf.get("higo.download.offline.store")+ "/" + day + "/tmp_" + java.util.UUID.randomUUID().toString();
				MakeIndex.make(fs, solrHome, conf, "txt", txtpath.getParent().toString(), inputs, "*", tableInfo.get("indexStorePath"), new Path(index), 1, tableInfo.get("splitString"), false, tableInfo.get("colsName"),new updateStatus() {

					@Override
					public void update(int statge, Job job) {
						try {
							TableJoin.LOG.info("update "+tableName+ ",stage:"+statge+",map:"+job.mapProgress()+",reduce:"+job.reduceProgress()+ ":INDEXING");
							String percent="Stage-"+statge+" map = "+(job.mapProgress()*100)+"%,  reduce = "+(job.reduceProgress()*100)+"%";
							TableJoin.updatePercent(tableName, percent, "INDEXING")	;
						} catch (Exception e) {
							TableJoin.LOG.error("updatePercent",e);
						}				
					}

					@Override
					public void finish() {
						try {
							TableJoin.LOG.info("update "+tableName+ ",INDEX");
							TableJoin.updatePercent(tableName, "Stage-2 map = 100%,  reduce = 100%", "INDEX")	;
						} catch (Exception e) {
							TableJoin.LOG.error("updatePercent",e);
						}
					}

					@Override
					public boolean dump(Job job) {
						  Counters counters;
						try {
							counters = job.getCounters();
				            long wsize=counters.findCounter("higo", "dumpcount").getValue();
				            if(wsize>0)
				            {
				            	try {
									TableJoin.LOG.info("update "+tableName+ ",dump");
									TableJoin.updatePercent(tableName, "Stage-2 map = 100%,  reduce = 100%", "DUMP")	;
								} catch (Exception e) {
									TableJoin.LOG.error("updatePercent",e);
								}
				            	return true;
				            }
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						return false;
						
					}
				},strjoins);
			}catch(Exception e){
				TableJoin.LOG.error("make index",e);
				try {
					TableJoin.updatePercent(tableName, "Stage-2 map = 100%,  reduce = 100%", "FAIL")	;
				} catch (Exception e2) {
					TableJoin.LOG.error("updatePercent",e2);
				}
			}
				
			}
		});
		
		if (callback != null && callback.length() > 0) {
			return callback + "(" + jsonObj.toString() + ")";
		} else {
			return jsonObj.toString();
		}
	}
	
	private static Configuration getConf(Map stormconf) {
		String hadoopConfDir = (String) stormconf.get("hadoop.conf.dir");
		String opts = (String) stormconf.get("hadoop.java.opts");
		Configuration conf = new Configuration();
		conf.set("mapred.child.java.opts", opts);
		HadoopUtil.grabConfiguration(hadoopConfDir, conf);
		return conf;
	}

	public static String  updatePercent(String uuid,String percent,String status) throws SQLException
	{
		MySqlConn m_fpsql = getConn();

		Connection conn = m_fpsql.getConn();
		String strSql = "update adhoc_joins set " +
				"percent=?,status=?,lastuptime=? where tableName=? ";
		System.out.println(strSql);
		PreparedStatement m_fps = conn.prepareStatement(strSql);
		try {
			int index=1;
			m_fps.setString(index++, percent);
			m_fps.setString(index++, status);
			m_fps.setTimestamp(index++, new java.sql.Timestamp(System.currentTimeMillis()));
			m_fps.setString(index++, uuid);

			m_fps.executeUpdate();
			String fullstrSql=m_fps.toString();
			return fullstrSql;
		} catch (Exception e) {
			TableJoin.LOG.error("updatePercent"+m_fps.toString(),e);
		}finally{
			m_fps.close();
			m_fpsql.close();
		}
		return "";
	}
	
	public static String  setJoinDownload(String tablename,String uuid,String callback) throws SQLException, JSONException
	{
		JSONObject jsonObj = new JSONObject();

		MySqlConn m_fpsql = getConn();

		Connection conn = m_fpsql.getConn();
		String strSql = "update adhoc_joins set download_uuid=?,lastuptime=? where tableName=? ";
		PreparedStatement m_fps = conn.prepareStatement(strSql);
		try {
			int index=1;
			m_fps.setString(index++, uuid);
			m_fps.setTimestamp(index++, new java.sql.Timestamp(System.currentTimeMillis()));
			m_fps.setString(index++, tablename);

			m_fps.executeUpdate();
			String fullstrSql=m_fps.toString();
			jsonObj.put("code", "1");

			return fullstrSql;
		} catch (Exception e) {
			jsonObj.put("code", "0");

			TableJoin.LOG.error("updatePercent"+m_fps.toString(),e);
		}finally{
			m_fps.close();
			m_fpsql.close();
		}
		
		if (callback != null && callback.length() > 0) {
			return callback + "(" + jsonObj.toString() + ")";
		} else {
			return jsonObj.toString();
		}
	}
	
	public static String  setJoinDelete(String uuid,String callback) throws SQLException, JSONException
	{
		JSONObject jsonObj = new JSONObject();

		MySqlConn m_fpsql = getConn();

		Connection conn = m_fpsql.getConn();
		String strSql = "update adhoc_joins set status='DEL',lastuptime=? where tableName=? ";
		PreparedStatement m_fps = conn.prepareStatement(strSql);
		try {
			int index=1;
			m_fps.setTimestamp(index++, new java.sql.Timestamp(System.currentTimeMillis()));
			m_fps.setString(index++, uuid);

			m_fps.executeUpdate();
			String fullstrSql=m_fps.toString();
			jsonObj.put("code", "1");
			jsonObj.put("__debug", fullstrSql);
		} catch (Exception e) {
			TableJoin.LOG.error("updatePercent"+m_fps.toString(),e);
			jsonObj.put("__debugerror", m_fps.toString());
			jsonObj.put("code", "0");
		}finally{
			m_fps.close();
			m_fpsql.close();
		}
		if (callback != null && callback.length() > 0) {
			return callback + "(" + jsonObj.toString() + ")";
		} else {
			return jsonObj.toString();
		}
	}
	
	public static String  DeleteDownload(String uuid,String callback) throws SQLException, JSONException
	{
		JSONObject jsonObj = new JSONObject();

		MySqlConn m_fpsql = getConn();

		Connection conn = m_fpsql.getConn();
		String strSql = "update adhoc_download set status='DEL',endtime=? where uuid=? ";
		PreparedStatement m_fps = conn.prepareStatement(strSql);
		try {
			int index=1;
			m_fps.setTimestamp(index++, new java.sql.Timestamp(System.currentTimeMillis()));
			m_fps.setString(index++, uuid);

			m_fps.executeUpdate();
			String fullstrSql=m_fps.toString();
			jsonObj.put("code", "1");
			jsonObj.put("__debug", fullstrSql);
		} catch (Exception e) {
			jsonObj.put("code", "0");
			TableJoin.LOG.error("updatePercent"+m_fps.toString(),e);
			jsonObj.put("__debugerror", m_fps.toString());
		}finally{
			m_fps.close();
			m_fpsql.close();
		}
		if (callback != null && callback.length() > 0) {
			return callback + "(" + jsonObj.toString() + ")";
		} else {
			return jsonObj.toString();
		}
	}
	
	public static String  updateKb(String uuid,long sz) throws SQLException
	{
		MySqlConn m_fpsql = getConn();

		Connection conn = m_fpsql.getConn();
		String strSql = "update adhoc_joins set " +
				"resultkb=? where tableName=? ";
		System.out.println(strSql);
		PreparedStatement m_fps = conn.prepareStatement(strSql);
		try {
			int index=1;
			m_fps.setInt(index++, (int)sz/1024);
			m_fps.setString(index++, uuid);

			m_fps.executeUpdate();
			String fullstrSql=m_fps.toString();
			return fullstrSql;
		} catch (Exception e) {
			TableJoin.LOG.error("updatePercent"+m_fps.toString(),e);
		}finally{
			m_fps.close();
			m_fpsql.close();
		}
		return "";
	}
	
	private static MySqlConn getConn()
	{
		SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
		String day = fmt.format(new Date());
		Map stormconf = Utils.readStormConfig();
		String connstr = (String) stormconf.get("higo.download.offline.conn");
		String uname = (String) stormconf.get("higo.download.offline.username");
		String passwd = (String) stormconf.get("higo.download.offline.passwd");
		MySqlConn m_fpsql = new MySqlConn(connstr, uname, passwd);
		
		return m_fpsql;
	}
	
	
	private static Pattern  pat=null;
	public static double parsePercent(String stage,String percent,boolean issuccess)
	{
		Integer totalstage=Integer.parseInt(stage);
		if(pat==null)
		{
			pat= Pattern.compile(".*Stage.*[^\\d]*(\\d+)[^\\d]map[^\\d]*(\\d+)[^\\d].*reduce[^\\d]*(\\d+)[^\\d].*");
		}
		
		if(!percent.startsWith("Stage"))
		{
			return 0d;
		}
		
		Integer currStage=0;
		double map=0;
		double reduce=0;
		
		Matcher mat = pat.matcher(percent);
        while (mat.find()) {
        	currStage=Integer.parseInt(mat.group(1));
        	map=Double.parseDouble(mat.group(2));
        	reduce=Double.parseDouble(mat.group(3));
        }
        
        if(currStage<1)
        {
        	currStage=1;
        }
        
        if(totalstage>0)
        {
        	double result= (100d*(currStage-1)+map*0.5d+reduce*0.5d)/totalstage;
        	return result;
        }
		return 100d;
	}
	
	
	public static String getUserTables(String username,int start,int rows,int type,String callback,String moduleName) throws SQLException, JSONException
	{
		String joins="";
		if(moduleName!=null&&moduleName.length()>0)
		{
			joins=" and joins like '%"+moduleName+":%' ";
		}
		SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
		Map stormconf = Utils.readStormConfig();
		String connstr = (String) stormconf.get("higo.download.offline.conn");
		String uname = (String) stormconf.get("higo.download.offline.username");
		String passwd = (String) stormconf.get("higo.download.offline.passwd");
		MySqlConn m_fpsql = new MySqlConn(connstr, uname, passwd);

		Connection conn = m_fpsql.getConn();
		Statement stmt = conn.createStatement();
		
		String strsqlJoin="select '1' as source " +
				",tableShowName as tableShowName" +
				",tableName as tableName" +
				",colsShowName as colsShowName" +
				",colsName as colsName" +
				",colsType as colsType" +
				",splitString as splitString" +
				",txtStorePath as txtStorePath" +
				",indexStorePath as indexStorePath" +
				",'0' as extval" +
				",'0' as isfinish" +
				",status as status" +
				",username as username" +
				",createtime as createtime" +
				",lastuptime as lastuptime" +
				",joins as joins" +
				",'2' as stage" +
				",percent as percent" +
				",resultkb as resultkb" +
				",memo as memo" +
				" from adhoc_joins where username='"+username.replaceAll("'", "")+"' and status<>'DEL'  and (copy_uuid is null or copy_uuid='')  "+joins+" ";
		
		
		StringBuffer bufferSql=new StringBuffer();
		if(type==0)//个人中心列表
		{
			
			String strsqlJoin2="select '3' as source " +
			",tableShowName as tableShowName" +
			",tableName as tableName" +
			",colsShowName as colsShowName" +
			",colsName as colsName" +
			",colsType as colsType" +
			",splitString as splitString" +
			",txtStorePath as txtStorePath" +
			",indexStorePath as indexStorePath" +
			",'0' as extval" +
			",'0' as isfinish" +
			",status as status" +
			",username as username" +
			",createtime as createtime" +
			",lastuptime as lastuptime" +
			",joins as joins" +
			",'2' as stage" +
			",percent as percent" +
			",resultkb as resultkb" +
			",memo as memo" +
			" from adhoc_joins where username='"+username.replaceAll("'", "")+"' and status<>'DEL'  and (copy_uuid is not null and copy_uuid<>''  and status='INDEX' )  "+joins+" ";
	
	
			
			String strsqlDownload="select '2' as source " +
			",jobname as tableShowName" +
			",uuid as tableName" +
			",cols as colsShowName" +
			",'empty' as colsName" +
			",'empty' as colsType" +
			",'default' as splitString" +
			",storedir as txtStorePath" +
			",'empty' as indexStorePath" +
			",extval as extval" +
			",isfinish as isfinish" +
			",'INDEX' as status" +
			",username as username" +
			",starttime as createtime" +
			",endtime as lastuptime" +
			",'' as joins" +
			",stage as stage" +
			",percent as percent" +
			",resultkb as resultkb" +
			",'' as memo" +
			" from adhoc_download where username='"+username.replaceAll("'", "")+"' and status<>'DEL'  ";
			bufferSql.append("select source,tableShowName,tableName,colsShowName" +
					",colsName,colsType,splitString,txtStorePath,indexStorePath,extval,status,username," +
					"createtime,lastuptime,joins,stage,percent,resultkb,memo");
			bufferSql.append(" from ("+strsqlJoin+"  union "+strsqlDownload+" union "+strsqlJoin2+") tmp order by tmp.createtime desc limit "+start+","+rows+" ");
		}
		if(type==1)//for join
		{
			bufferSql.append(strsqlJoin+" and status='INDEX' and resultkb<=512000 order by createtime desc limit "+start+","+rows+" ");
		}
		
		
		
		String sql=bufferSql.toString();
		TableJoin.LOG.info("getUserTables:"+sql);
		ResultSet res = stmt.executeQuery(sql);
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("code", "1");
		jsonObj.put("_exehql", sql);
		JSONArray jsonArray = new JSONArray();

		while (res.next()) {
			JSONObject item = new JSONObject();

			item.put("source", res.getString("source"));
			item.put("tableShowName", res.getString("tableShowName"));//展示名称
			item.put("tableName", res.getString("tableName"));//uuid
			item.put("colsShowName", res.getString("colsShowName").replaceAll("\\(", "_").replaceAll("\\)", "_").replaceAll(" ", "").replaceAll("_,", ",").replaceAll(",$", ""));
			item.put("colsName", res.getString("colsName"));
			item.put("colsType", res.getString("colsType"));
			item.put("splitString", res.getString("splitString"));
			item.put("txtStorePath", res.getString("txtStorePath"));
			item.put("indexStorePath", res.getString("indexStorePath"));
			item.put("extval", res.getString("extval"));
			item.put("status", res.getString("status"));
			item.put("username", res.getString("username"));
			item.put("createtime", res.getString("createtime"));
			item.put("lastuptime", res.getString("lastuptime"));
			item.put("joins", res.getString("joins"));
			item.put("stage", res.getString("stage"));
			item.put("percent", res.getString("percent"));
			item.put("resultkb", parseInt(res.getString("resultkb"))>0?res.getString("resultkb"):"<1");
			item.put("memo", String.valueOf(res.getString("memo")));
			boolean isoversize=parseInt(res.getString("resultkb"))>512000;

			boolean issuccess=res.getString("status").equals("INDEX")&&res.getString("extval").equals("0");
			boolean iserror=res.getString("status").equals("FAIL")||!res.getString("extval").equals("0");
			double percent=parsePercent(res.getString("stage"),res.getString("percent"),issuccess);
			
			
			boolean isallowEdit=!res.getString("status").equals("INDEXING")&&res.getString("source").equals("1");
			item.put("allowCreate",String.valueOf(res.getString("source").equals("2")&&issuccess&&percent>=100&&!isoversize));//是否允许将离线下载转换为个人表
			item.put("allowUpload",String.valueOf(isallowEdit&&res.getString("status").equals("init")));//上传
			item.put("allowDownload",String.valueOf((issuccess||res.getString("status").equals("DUMP"))&&percent>=100));//下载
			item.put("allowJoin",String.valueOf(res.getString("source").equals("1")&&issuccess&&percent>=100&&!isoversize));//join
			item.put("allowSend",String.valueOf(issuccess&&percent>=100));//推送
			item.put("isError", iserror);
			String uuidshow=iserror?("<br>"+res.getString("tableName")):"";
			item.put("proccess", ((res.getString("status").equals("DUMP")||iserror)?"0":String.valueOf(percent))+"%");

			item.put("msg", String.valueOf(iserror?"服务器异常":isoversize?"数据文件超过500M	":res.getString("status").equals("DUMP")?"设置关联关系的字段有重复值":issuccess&&percent>=100?"成功":res.getString("status").equals("init")?"等待上传数据":"处理中...")+uuidshow);

			
			jsonArray.put(item);
			
		    }
		HashMap<String,String> cnt=getUserTablesCount(username, type,moduleName);

		JSONObject data = new JSONObject();
		data.put("list",jsonArray);
		data.put("total",cnt.get("cnt"));
		jsonObj.put("data",data);
		jsonObj.put("total_debug", new JSONObject(cnt));
		m_fpsql.close();
		
	
		if (callback != null && callback.length() > 0) {
			return callback + "(" + jsonObj.toString() + ")";
		} else {
			return jsonObj.toString();
		}	}
	
	public static Integer parseInt(String str)
	{
		try{
		return Integer.parseInt(str);
		}catch(NumberFormatException e)
		{
			return 0;
		}
	}
	
	public static HashMap<String,String> getUserTablesCount(String username,int type,String moduleName) throws SQLException
	{
		String joins="";
		if(moduleName!=null&&moduleName.length()>0)
		{
			joins=" and joins like '%"+moduleName+":%' ";
		}
		SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
		Map stormconf = Utils.readStormConfig();
		String connstr = (String) stormconf.get("higo.download.offline.conn");
		String uname = (String) stormconf.get("higo.download.offline.username");
		String passwd = (String) stormconf.get("higo.download.offline.passwd");
		MySqlConn m_fpsql = new MySqlConn(connstr, uname, passwd);

		Connection conn = m_fpsql.getConn();
		Statement stmt = conn.createStatement();
		
		String strsqlJoin="select count(*) as cnt" +
				" from adhoc_joins where username='"+username.replaceAll("'", "")+"' and status<>'DEL' and (copy_uuid is null or copy_uuid='') "+joins+" " ;
		
		StringBuffer bufferSql=new StringBuffer();
		if(type==0)
		{
			String strsqlJoin2="select count(*) as cnt" +
			" from adhoc_joins where username='"+username.replaceAll("'", "")+"' and status<>'DEL' and (copy_uuid is not null and copy_uuid<>'')   "+joins+"  " ;
			
			String strsqlDownload="select count(*) as cnt" +
			" from adhoc_download where username='"+username.replaceAll("'", "")+"' and status<>'DEL' ";
	
			bufferSql.append("select sum(cnt) as cnt");
			bufferSql.append(" from ("+strsqlJoin+" union "+strsqlDownload+" union "+strsqlJoin2+") tmp  limit 10 ");
		}if(type==1)//for join
		{
			bufferSql.append(strsqlJoin+" and status='INDEX' and resultkb<=512000  order by createtime desc limit 10 ");
		}
		
		
		
		
		String sql=bufferSql.toString();
		ResultSet res = stmt.executeQuery(sql);
		HashMap<String,String> rtn=new HashMap<String,String>();
		rtn.put("_exehql", sql);

		while (res.next()) {
			rtn.put("cnt", String.valueOf(res.getInt("cnt")));
		 }
		m_fpsql.close();
		return rtn;
	}
	
	
	public static HashMap<String,String>  getTableInfo(String uuid) throws SQLException
	{
		SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
		String day = fmt.format(new Date());
		Map stormconf = Utils.readStormConfig();
		String connstr = (String) stormconf.get("higo.download.offline.conn");
		String uname = (String) stormconf.get("higo.download.offline.username");
		String passwd = (String) stormconf.get("higo.download.offline.passwd");

		MySqlConn m_fpsql = new MySqlConn(connstr, uname, passwd);

		Connection conn = m_fpsql.getConn();
		Statement stmt = conn.createStatement();

		String sql="select tableShowName,tableName,colsShowName,colsName,colsType,splitString,txtStorePath,indexStorePath,status,username,createtime,lastuptime,joins,percent,resultkb,download_uuid,copy_uuid,memo from adhoc_joins where tableName='"+uuid.replaceAll("'", "")+"' limit 10";
		ResultSet res = stmt.executeQuery(sql);
		HashMap<String,String> rtn=new HashMap<String,String>();
		rtn.put("_exehql", sql);

		boolean issetup=false;
		while (res.next()) {
			rtn.put("tableShowName", res.getString("tableShowName"));
			rtn.put("tableName", res.getString("tableName"));
			rtn.put("colsShowName", res.getString("colsShowName"));
			rtn.put("colsName", res.getString("colsName"));
			rtn.put("colsType", res.getString("colsType"));
			rtn.put("splitString", res.getString("splitString"));
			rtn.put("memo", String.valueOf(res.getString("memo")));

			rtn.put("txtStorePath", res.getString("txtStorePath"));
			rtn.put("indexStorePath", res.getString("indexStorePath"));
			rtn.put("status", res.getString("status"));
			rtn.put("username", res.getString("username"));
			rtn.put("createtime", res.getString("createtime"));
			rtn.put("lastuptime", res.getString("lastuptime"));
			rtn.put("joins", res.getString("joins"));
			rtn.put("percent", res.getString("percent"));
			rtn.put("resultkb", res.getString("resultkb"));
			rtn.put("download_uuid", res.getString("download_uuid"));
			rtn.put("copy_uuid", res.getString("copy_uuid"));
			issetup=true;
		    }
		m_fpsql.close();
		
	
		if(!issetup)
		{
			return null;
		}
		return rtn;
	}
	

//	CREATE TABLE `adhoc_joins` (
//	  `id` bigint(20) NOT NULL auto_increment,
//	  `tableShowName` char(240) NOT NULL,	
//	  `tableName` char(240) NOT NULL,	
//	  `colsShowName` text NOT NULL,	
//	  `colsName` text NOT NULL,
//	  `colsType` text NOT NULL,
//	  `splitString` char(240) NOT NULL,
//	  `txtStorePath` text  NOT NULL,
//	  `indexStorePath` text  NOT NULL,
//	  `status` char(240) NOT NULL,
//	  `username` char(240) NOT NULL,
//	  `createtime` datetime default '1983-11-07 12:12:12',
//	  `lastuptime` datetime default '1983-11-07 12:12:12',
//	   `joins` text NOT NULL,
//	  `percent` text NOT NULL,
//	`percent` text NOT NULL,
//	 `resultkb` bigint(20) default '0',
//	 `download_uuid` char(240) default '',
//	 `copy_uuid` char(240) default '',
//	  PRIMARY KEY  (`id`),
//	  KEY `username` (`username`),
//	 KEY `tableName` (`tableName`)
//
//	) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 

	
	
	
	public static String copyto(String uuid,String mailto,String callback,String memo) throws Exception
	{
		JSONObject jsonObj = new JSONObject();
		HashMap<String,String> tableInfo=getTableInfo(uuid);
		
		if(tableInfo==null)
		{
			//TODO
			Map stormconf = Utils.readStormConfig();
			String hdpConf = (String) stormconf.get("hadoop.conf.dir");
			String connstr = (String) stormconf.get("higo.download.offline.conn");
			String uname = (String) stormconf.get("higo.download.offline.username");
			String passwd = (String) stormconf.get("higo.download.offline.passwd");
			MySqlConn conn = new MySqlConn(connstr, uname, passwd);
			MysqlInfo info = new MysqlInfo(conn);
			HashMap<String, String> result2 = info.get(uuid);
			if(result2!=null)
			{
				tableInfo=new HashMap<String, String>();
				tableInfo.put("tableShowName", result2.get("jobname"));
				tableInfo.put("tableName", result2.get("uuid"));
				tableInfo.put("colsShowName", result2.get("cols"));
				tableInfo.put("colsName", "");
				tableInfo.put("colsType", "");
				tableInfo.put("splitString", "default");
				tableInfo.put("memo", String.valueOf(result2.get("memo")));

				tableInfo.put("txtStorePath", result2.get("storedir"));
				tableInfo.put("indexStorePath", "");
				tableInfo.put("status", "INDEX");
				tableInfo.put("username", result2.get("username"));
				tableInfo.put("createtime", result2.get("starttime"));
				tableInfo.put("lastuptime", result2.get("endtime"));
				tableInfo.put("joins", "");
				tableInfo.put("percent", "Stage-2 map = 100%,  reduce = 100%");
				tableInfo.put("resultkb", result2.get("resultkb"));
				tableInfo.put("download_uuid", "");
				tableInfo.put("copy_uuid", uuid);
			}
		}
		
		tableInfo.put("memo", String.valueOf(memo));//tableInfo.get("username")+":"+
		if(tableInfo==null||tableInfo.isEmpty())
		{
			jsonObj.put("code", "0");
			jsonObj.put("message",  "该表不存在");
		}else
		if(tableInfo.get("status").equals("INDEXING"))
		{
			jsonObj.put("code", "0");
			jsonObj.put("message", "正在创建索引中请稍后");
			return jsonObj.toString();
		}else
		{
		JSONArray list = new JSONArray();

		for(String username:mailto.split(","))
		{
			JSONObject result=createByMap(tableInfo, uuid,username);
			list.put(result);
		}
		jsonObj.put("code", "1");
		jsonObj.put("data", list);
	}
		if (callback != null && callback.length() > 0) {
			return callback + "(" + jsonObj.toString() + ")";
		} else {
			return jsonObj.toString();
		}	}
	
	public static JSONObject createByMap(final HashMap<String,String> tableInfo,String copyuuid,String username) throws Exception
	{
		JSONObject jsonObj = new JSONObject();

		SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
		String day = fmt.format(new Date());
		Map stormconf = Utils.readStormConfig();
		String hdpConf = (String) stormconf.get("hadoop.conf.dir");
		String connstr = (String) stormconf.get("higo.download.offline.conn");
		String uname = (String) stormconf.get("higo.download.offline.username");
		String passwd = (String) stormconf.get("higo.download.offline.passwd");
		String store = (String) stormconf.get("higo.download.offline.store")
				+ "/" + day + "/" + java.util.UUID.randomUUID().toString();
		MySqlConn m_fpsql = new MySqlConn(connstr, uname, passwd);
		
		String tableName=java.util.UUID.randomUUID().toString();
		Connection conn = m_fpsql.getConn();
//		tableShowName,tableName,colsShowName,colsName,colsType,splitString,txtStorePath,indexStorePath,status,username,createtime,lastuptime,joins,percent,resultkb,download_uuid,copy_uuid
		String strSql = "insert into adhoc_joins " +
				"(tableShowName,tableName,colsShowName,colsName,colsType,splitString,txtStorePath,indexStorePath,status,username,createtime,lastuptime,joins,percent,resultkb,download_uuid,copy_uuid,memo)" +
				"values" +
				"(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		PreparedStatement m_fps = conn.prepareStatement(strSql);
		try {
			int index=1;
			m_fps.setString(index++, tableInfo.get("tableShowName"));
			m_fps.setString(index++, tableName);
			m_fps.setString(index++, tableInfo.get("colsShowName"));
			m_fps.setString(index++, tableInfo.get("colsName"));
			m_fps.setString(index++, tableInfo.get("colsType"));
			m_fps.setString(index++, tableInfo.get("splitString"));
			m_fps.setString(index++, tableInfo.get("txtStorePath"));
			m_fps.setString(index++, tableInfo.get("indexStorePath"));
			m_fps.setString(index++, tableInfo.get("status"));
			m_fps.setString(index++, username);
			long nowtims=System.currentTimeMillis();
			m_fps.setTimestamp(index++, new java.sql.Timestamp(nowtims));
			m_fps.setTimestamp(index++, new java.sql.Timestamp(nowtims));
			
			m_fps.setString(index++, tableInfo.get("joins"));
			m_fps.setString(index++, tableInfo.get("percent"));
			m_fps.setInt(index++, Integer.parseInt(tableInfo.get("resultkb")));
			m_fps.setString(index++, tableInfo.get("download_uuid"));
			m_fps.setString(index++, copyuuid);
			m_fps.setString(index++, tableInfo.get("memo"));
			m_fps.executeUpdate();
			jsonObj.put("code", "1");
			jsonObj.put("tableid", tableName);
			jsonObj.put("____debug", m_fps.toString());
		} catch (Exception e) {
			jsonObj.put("____debug2", m_fps.toString());
			jsonObj.put("____debugerror", e.toString());
			jsonObj.put("code", "0");
		}finally{
			m_fps.close();
			m_fpsql.close();
		}
		
		return jsonObj;

	}
	
//	public String get(String key) throws UnsupportedEncodingException{
//		String keyPath = ZKUtil.joinZNode(PROJECT_NODE, key);
//		HashMap<String,String> rtn=new HashMap<String,String>();
//		rtn.put("code", "0");
//
//		try {
//			byte[] d=ZKUtil.getData(zkw, keyPath);
//			if(d!=null)
//			{
//				rtn.put("data", new String(d,"utf8"));
//				rtn.put("code", "1");
//			}else{
//				rtn.put("message", "nodata");
//				rtn.put("data", "");
//				rtn.put("code", "1");
//			}
//			
//		} catch (KeeperException e) {
//			rtn.put("message", this.stringify_error(e));
//		}
//		return new JSONObject(rtn).toString();
//	}
	public static String get(String key) throws SQLException, JSONException
	{
		Map stormconf = Utils.readStormConfig();
		String connstr = (String) stormconf.get("higo.download.offline.conn");
		String uname = (String) stormconf.get("higo.download.offline.username");
		String passwd = (String) stormconf.get("higo.download.offline.passwd");

		MySqlConn m_fpsql = new MySqlConn(connstr, uname, passwd);

		Connection conn = m_fpsql.getConn();
		Statement stmt = conn.createStatement();

		String sql="select tab_key,tab_value from tab_infos where tab_key='"+key+"' limit 10";
		ResultSet res = stmt.executeQuery(sql);
		JSONObject jsonObj = new JSONObject();

		HashMap<String,String> rtn=new HashMap<String,String>();
		jsonObj.put("code", "0");

		boolean issetup=false;
		while (res.next()) {
			rtn.put("tab_key", res.getString("tab_key"));
			rtn.put("tab_value", res.getString("tab_value"));
			issetup=true;
		    }
		m_fpsql.close();
		
		if(issetup)
 {
			jsonObj.put("data", rtn.get("tab_key"));
			jsonObj.put("code", "1");
		} else {
			jsonObj.put("message", "nodata");
			jsonObj.put("data", "");
			jsonObj.put("code", "1");
		}
		return jsonObj.toString();
	}
	
	public String set(String key,String value) throws Exception{
		HashMap<String,String> rtn=new HashMap<String,String>();
		rtn.put("code", "0");

		try {
			JSONObject last=new JSONObject(get(key));
			if(last.has("message")&&last.getString("message").equals("nodate"))
			{
				String result=insert(key,value);
				return result;
			}else{
				update(key, value);
				rtn.put("lastdata",last.getString("data"));
			}
			rtn.put("code", "1");

		} catch (Exception e) {
			rtn.put("message", e.toString());
		}
		return new JSONObject(rtn).toString();
	}
	
	public static String insert(String key,String value) throws Exception
	{
		JSONObject jsonObj = new JSONObject();

		SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
		String day = fmt.format(new Date());
		Map stormconf = Utils.readStormConfig();
		String connstr = (String) stormconf.get("higo.download.offline.conn");
		String uname = (String) stormconf.get("higo.download.offline.username");
		String passwd = (String) stormconf.get("higo.download.offline.passwd");
	
		MySqlConn m_fpsql = new MySqlConn(connstr, uname, passwd);
		
		Connection conn = m_fpsql.getConn();
		String strSql = "insert into tab_infos " +
				"(tab_key,tab_value)" +
				"values" +
				"(?,?)";
		PreparedStatement m_fps = conn.prepareStatement(strSql);
		try {
			int index=1;
			m_fps.setString(index++, key);
			m_fps.setString(index++, value);
			m_fps.executeUpdate();
			jsonObj.put("code", "1");
			JSONObject daa = new JSONObject();
			jsonObj.put("data", daa);
			jsonObj.put("____debug", m_fps.toString());
		} catch (Exception e) {
			jsonObj.put("____debug2", m_fps.toString());
			jsonObj.put("message", e.toString());
			jsonObj.put("code", "0");
		}finally{
			m_fps.close();
			m_fpsql.close();
		}
		
		return jsonObj.toString();

	}
	
//	public String del(String key) throws UnsupportedEncodingException{
//		String keyPath = ZKUtil.joinZNode(PROJECT_NODE, key);
//		HashMap<String,String> rtn=new HashMap<String,String>();
//		rtn.put("code", "0");
//		try {
//			ZKUtil.deleteNodeRecursively(zkw, keyPath);
//			rtn.put("code", "1");
//		} catch (KeeperException e) {
//			rtn.put("message", this.stringify_error(e));
//		}
//		return new JSONObject(rtn).toString();
//	}
	
	public static String del(String key) throws Exception
	{
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("code", "0");
		SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
		String day = fmt.format(new Date());
		Map stormconf = Utils.readStormConfig();
		String connstr = (String) stormconf.get("higo.download.offline.conn");
		String uname = (String) stormconf.get("higo.download.offline.username");
		String passwd = (String) stormconf.get("higo.download.offline.passwd");
	
		MySqlConn m_fpsql = new MySqlConn(connstr, uname, passwd);
		
		Connection conn = m_fpsql.getConn();
		String strSql = "delete from tab_infos  where tab_key=? ";
		PreparedStatement m_fps = conn.prepareStatement(strSql);
		try {
			int index=1;
			m_fps.setString(index++, key);
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
	
	public static String update(String key,String value) throws Exception
	{
		JSONObject jsonObj = new JSONObject();

		SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
		String day = fmt.format(new Date());
		Map stormconf = Utils.readStormConfig();
		String connstr = (String) stormconf.get("higo.download.offline.conn");
		String uname = (String) stormconf.get("higo.download.offline.username");
		String passwd = (String) stormconf.get("higo.download.offline.passwd");
	
		MySqlConn m_fpsql = new MySqlConn(connstr, uname, passwd);
		
		Connection conn = m_fpsql.getConn();
		String strSql = "update tab_infos set tab_value=? where tab_key=? ";
		PreparedStatement m_fps = conn.prepareStatement(strSql);
		try {
			int index=1;
			m_fps.setString(index++, value);
			m_fps.setString(index++, key);
			m_fps.executeUpdate();
			jsonObj.put("code", "1");
			JSONObject daa = new JSONObject();
			jsonObj.put("data", daa);
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

	
	public static String create(String tableShowName,String colsShowName,String splitString,String username,String joins,String callback,String memo) throws Exception
	{
//		public static HashMap<String,String> getUserTablesCount(String username,int type) throws SQLException

		HashMap<String,String> tablecount=getUserTablesCount(username, 1,"");
	
		
		JSONObject jsonObj = new JSONObject();
		if(tablecount!=null&&tablecount.containsKey("cnt")&&Integer.parseInt(tablecount.get("cnt"))>=20)
		{
			jsonObj.put("code", "0");
			jsonObj.put("message", "你已经创建了"+tablecount.get("cnt")+"个个人表了，单用户最多创建20个个人表");
			if (callback != null && callback.length() > 0) {
				return callback + "(" + jsonObj.toString() + ")";
			} else {
				return jsonObj.toString();
			}
		}

		SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
		String day = fmt.format(new Date());
		Map stormconf = Utils.readStormConfig();
		String hdpConf = (String) stormconf.get("hadoop.conf.dir");
		String connstr = (String) stormconf.get("higo.download.offline.conn");
		String uname = (String) stormconf.get("higo.download.offline.username");
		String passwd = (String) stormconf.get("higo.download.offline.passwd");
		String store = (String) stormconf.get("higo.download.offline.store")
				+ "/" + day + "/" + java.util.UUID.randomUUID().toString();
		MySqlConn m_fpsql = new MySqlConn(connstr, uname, passwd);
		
		String tableName=java.util.UUID.randomUUID().toString();
		Connection conn = m_fpsql.getConn();
		String strSql = "insert into adhoc_joins " +
				"(tableShowName,tableName,colsShowName,colsName,colsType,splitString,txtStorePath,indexStorePath,status,username,createtime,lastuptime,joins,percent,memo)" +
				"values" +
				"(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		PreparedStatement m_fps = conn.prepareStatement(strSql);
		try {
			int index=1;
			m_fps.setString(index++, tableShowName);
			m_fps.setString(index++, tableName);
			m_fps.setString(index++, colsShowName);
			StringBuffer colsName=new StringBuffer();
			StringBuffer colsType=new StringBuffer();
			String[] cols=colsShowName.split(",");
			String join="";
			HashMap<String,String> colsNames=new HashMap<String, String>();
			for(int i=0;i<cols.length;i++)
			{
				String colname="cols_"+i+"_s";
				colsName.append(join);
				colsName.append(colname);
				colsType.append(join);
				colsType.append("string");
				colsNames.put(cols[i],colname);
				join=",";
			}
			m_fps.setString(index++, colsName.toString());
			m_fps.setString(index++, colsType.toString());
			m_fps.setString(index++, MakeIndex.parseSplit(splitString));
			m_fps.setString(index++, store+"/txt");
			m_fps.setString(index++, store+"/index");
			m_fps.setString(index++, "init");
			m_fps.setString(index++, username);
		
			long nowtims=System.currentTimeMillis();
			m_fps.setTimestamp(index++, new java.sql.Timestamp(nowtims));
			m_fps.setTimestamp(index++, new java.sql.Timestamp(nowtims));
			
			StringBuffer joinbuff=new StringBuffer();
			String joinchar="";
			for(String joindesc:joins.split(","))
			{
				String[] arr=joindesc.split(":");
				joinbuff.append(joinchar);
				joinbuff.append(arr[0]);
				joinbuff.append(":");
				joinbuff.append(arr[1]);
				joinbuff.append(":");
				joinbuff.append(colsNames.get(arr[2]));
				joinchar=",";
			}
			m_fps.setString(index++,joinbuff.toString());
			m_fps.setString(index++,"");
			m_fps.setString(index++,String.valueOf(memo));
			m_fps.executeUpdate();
			jsonObj.put("code", "1");
			JSONObject daa = new JSONObject();

			daa.put("tableid", tableName);
			jsonObj.put("data", daa);
			jsonObj.put("____debug", m_fps.toString());
		} catch (Exception e) {
			jsonObj.put("____debug2", m_fps.toString());
			jsonObj.put("____debugerror", e.toString());
			jsonObj.put("code", "0");
		}finally{
			m_fps.close();
			m_fpsql.close();
		}
		
		if (callback != null && callback.length() > 0) {
			return callback + "(" + jsonObj.toString() + ")";
		} else {
			return jsonObj.toString();
		}
	}
	
	
}
