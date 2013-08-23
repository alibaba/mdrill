package com.alimama.mdrill.ui.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;

import backtype.storm.utils.Utils;


import com.alimama.mdrill.adhoc.MD5;
import com.alimama.mdrill.adhoc.MySqlConn;
import com.alimama.mdrill.adhoc.MysqlCallback;
import com.alimama.mdrill.adhoc.MysqlInfo;
import com.alimama.mdrill.adhoc.OfflineDownload;
import com.alimama.mdrill.json.JSONObject;
import com.alimama.mdrill.partion.GetPartions;
import com.alimama.mdrill.partion.GetShards;
import com.alimama.mdrill.partion.GetPartions.*;
import com.alimama.mdrill.ui.service.AdhocWebServiceParams.HigoAdhocJoinParams;
import com.alimama.mdrill.ui.service.MdrillService;
import com.alimama.mdrill.ui.service.partions.AdhocHivePartions;
import com.alimama.mdrill.ui.service.utils.WebServiceParams;
import com.alimama.mdrill.utils.HadoopBaseUtils;

public class AdhocOfflineService {
	private static Logger LOG = Logger.getLogger(AdhocOfflineService.class);

	public static void readHiveResult(String uuid, OutputStreamWriter outStream)
			throws SQLException, IOException {
		Map stormconf = Utils.readStormConfig();
		String hdpConf = (String) stormconf.get("hadoop.conf.dir");
		String connstr = (String) stormconf.get("higo.download.offline.conn");
		String uname = (String) stormconf.get("higo.download.offline.username");
		String passwd = (String) stormconf.get("higo.download.offline.passwd");
		MySqlConn conn = new MySqlConn(connstr, uname, passwd);
		Configuration conf = new Configuration();
		HadoopBaseUtils.grabConfiguration(hdpConf, conf);
		MysqlInfo info = new MysqlInfo(conn);
		HashMap<String, String> result = info.get(uuid);
		String exehql = "";
		if (result != null && result.containsKey("storedir")) {
			exehql = result.get("_exehql");
		}
		if (result != null && result.containsKey("storedir")
				&& result.containsKey("cols")) {
			outStream.write(result.get("cols").replaceAll("\001", ",")
					.replaceAll("\t", ",")
					+ "\n");
			AdhocOfflineService.readHiveResult(result.get("storedir"), outStream, conf);
		}
	
		// outStream.write(exehql.replaceAll("\001", ",").replaceAll("\t",
		// "\t")+"\n");
	
	}
	
	public static String readHiveResultID(String uuid) throws SQLException,
			IOException {
		Map stormconf = Utils.readStormConfig();
		String hdpConf = (String) stormconf.get("hadoop.conf.dir");
		String connstr = (String) stormconf.get("higo.download.offline.conn");
		String uname = (String) stormconf.get("higo.download.offline.username");
		String passwd = (String) stormconf.get("higo.download.offline.passwd");
		MySqlConn conn = new MySqlConn(connstr, uname, passwd);
		Configuration conf = new Configuration();
		HadoopBaseUtils.grabConfiguration(hdpConf, conf);
		MysqlInfo info = new MysqlInfo(conn);
		HashMap<String, String> result = info.get(uuid);
		if (result != null && result.containsKey("jobname")
				&& result.containsKey("cols")) {

			String tablename = result.get("jobname");
			if(tablename.isEmpty())
			{
				return "adhoc.csv";
			}
			return tablename.replaceAll(
					"[\n|\\/|:|\t|\\.|\\\\|\\?|<|>|\\*|\\?|\\|\"]", "_")
					+ ".csv";

		}
		return "adhoc.csv";

	}

	public static void readHiveResult(String path,
			OutputStreamWriter outStream, Configuration conf)
			throws IOException {
		FileSystem fs = FileSystem.get(conf);
		Path dir = new Path(path);
		if (!fs.exists(dir)) {
			throw new IOException("can not found path:" + path);
		}
		FileStatus[] filelist = fs.listStatus(dir);
	
		Long bytesRead = 0l;
		long maxsize = 1024l * 1024 * 1024 * 10;
	
		for (FileStatus f : filelist) {
			if (!f.isDir() && !f.getPath().getName().startsWith("_")) {
				FSDataInputStream in = fs.open(f.getPath());
				BufferedReader bf=new BufferedReader(new InputStreamReader(in)); 
				String line;
				while ((line = bf.readLine()) != null) {
					bytesRead += line.getBytes().length;
					String towrite=line.replaceAll("\001", ",").replaceAll("\t", ",");
					if(!towrite.isEmpty())
					{
					outStream.write(towrite);
					outStream.write("\r\n");
					}
					if (bytesRead >= maxsize) {
						bf.close();
						in.close();
						return;
					}
				}
				bf.close();
				in.close();
			}
		}
		return;
	}

	public static String offline(String projectName, String jsoncallback, String queryStr, 
			String fl, String groupby, 
			String mailto, String username, String jobname, String params,String leftjoin,String fq2,String limit2,String orderby2,String desc2)
			throws Exception {
		return offline(projectName, jsoncallback, queryStr, fl, groupby, mailto, username, jobname, params, leftjoin, fq2, limit2, orderby2, desc2,"");
	}
	public static String offline(String projectName, String jsoncallback, String queryStr, 
			String fl, String groupby, 
			String mailto, String username, String jobname, String params,String leftjoin,String fq2,String limit2,String orderby2,String desc2,String memo)
			throws Exception {
		int limit=0;
		if(limit2!=null)
		{
			limit=Integer.parseInt(limit2);
		}
		long t1 = System.currentTimeMillis();
	
		boolean isPartionByPt = false;
		if (projectName.equals("rpt_hitfake_auctionall_d")) {
			projectName = "rpt_p4padhoc_auction";
		}
		String hpart="dt";
		if (projectName.equals("rpt_b2bad_hoc_memb_sum_d")) {
			isPartionByPt = true;
			hpart="pt";
		}
		if (projectName.equals("r_rpt_tanx_adzone_total")) {
			hpart="ds";
		}
		
		queryStr = WebServiceParams.query(queryStr);
	
	
		TablePartion part = GetPartions.partion(projectName);
		String[] cores = GetShards.get(part.name, false, 10000);
		String[] ms = GetShards.get(part.name, true, 10000);
		String[] partionsAll = AdhocHivePartions.get(queryStr, part.parttype);
		GetPartions.Shards shard = GetPartions.getshard(part, partionsAll,
				cores, ms, 10000, 0);
		
		
		
		HashMap<String, String> filetypeMap = MdrillService.readFieldsFromSchemaXml(part.name);
		ArrayList<String> fqList = WebServiceParams.fqListHive(hpart,queryStr, shard,
				isPartionByPt, filetypeMap,null,null,null);
		StringBuffer sqlWhere =AdhocWebServiceParams.makeWhere(fqList);
	
	
		
		
		ArrayList<String> groupFields = WebServiceParams.groupFields(groupby);
		ArrayList<String> showFields = WebServiceParams.showHiveFields(fl);
		HigoAdhocJoinParams[] joins=AdhocWebServiceParams.parseJoinsHive(leftjoin, shard);
		String daycols=AdhocWebServiceParams.parseDayCols(groupFields,showFields);
		HashMap<String,String> colMap=new HashMap<String, String>();
		HashMap<String,String> colMapforStatFilter=new HashMap<String, String>();

		StringBuffer cols_inner = new StringBuffer();
		StringBuffer cols = new StringBuffer();
		String join = "";
		AtomicInteger nameindex=new AtomicInteger(0);
		String hql="";
		if(joins.length<=0)
		{
			AdhocWebServiceParams.parseColsNoJoins(cols, groupFields, showFields,colMapforStatFilter, nameindex);
			hql = "select " + cols.toString() + " from " + projectName + " "
			+ sqlWhere.toString() + " " ;
			
			StringBuffer sqlGroup = new StringBuffer();
			join = " group by ";
			for (String field : groupFields) {
				sqlGroup.append(join);
				sqlGroup.append(field);
				join = ",";
			}
			hql+=" "+sqlGroup.toString();

		}else
		{
			AdhocWebServiceParams.parseColsWithJoins(cols,cols_inner, joins, colMap,colMapforStatFilter, groupFields, showFields, nameindex);
			
			StringBuffer buffer=new StringBuffer();
			buffer.append("select ");
			buffer.append(cols.toString());
			buffer.append(" from ");
			
			buffer.append(" (select "+cols_inner.toString()+"  from " + projectName + " "	+ sqlWhere.toString() + " ) jl1 ");
			
			for(int i=0;i<joins.length;i++)
			{
				HigoAdhocJoinParams jp=joins[i];
				buffer.append(" join ("+jp.frQuer+") jr"+i+" on jl1."+colMap.get(jp.leftkey)+"==jr"+i+"."+jp.rightkey+" ");
			}
			
			hql=buffer.toString();
			
			StringBuffer sqlGroup = new StringBuffer();
			join = " group by ";
			for (String field : groupFields) {
				sqlGroup.append(join);
				sqlGroup.append("jl1."+colMap.get(field));
				join = ",";
			}
			
			if(AdhocWebServiceParams.hasStatFiled(showFields))
			{
				for(int i=0;i<joins.length;i++)
				{
					HigoAdhocJoinParams jp=joins[i];
					for(String s:jp.fl)
					{
						sqlGroup.append(join);
						sqlGroup.append("jr"+i+"."+s);
					}
				}
			}
			
			hql+=" "+sqlGroup.toString();

			
		}
		
		
		
		
		 ArrayList<String> fq2list=WebServiceParams.fqListHive(hpart,fq2, shard,
					isPartionByPt, filetypeMap,colMap,colMapforStatFilter,"fq2");
		 if(fq2list.size()>0)
		 {
				StringBuffer buffer=new StringBuffer();
				buffer.append("select * from ");
				buffer.append("("+hql+") fq2");
				String join2 = " where ";
				for (String fq : fq2list) {
					buffer.append(join2);
					buffer.append(fq);
					join2 = " and ";
				}
				hql=buffer.toString();
		 }
		 
		

		 
		 if(orderby2!=null)
		 {
			 hql=hql+" order by "+WebServiceParams.parseFqAlias(orderby2, colMap, colMapforStatFilter, "fq2")+" "+desc2;
		 }
		 
		 if(limit >1000000)
		 {
			 limit=1000000;
		 }
		 if(limit>0)
		 {
			 hql=hql+" limit "+limit;
 
		 }
		
	
		String md5 = MD5.getMD5(hql);
	
		SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
		String day = fmt.format(new Date());
	
		Map stormconf = Utils.readStormConfig();
		String hdpConf = (String) stormconf.get("hadoop.conf.dir");
		String connstr = (String) stormconf.get("higo.download.offline.conn");
		String uname = (String) stormconf.get("higo.download.offline.username");
		String passwd = (String) stormconf.get("higo.download.offline.passwd");
		String store = (String) stormconf.get("higo.download.offline.store")
				+ "/" + day + "/" + java.util.UUID.randomUUID().toString();
		MySqlConn conn = new MySqlConn(connstr, uname, passwd);
		MysqlInfo info = new MysqlInfo(conn);
		if (username == null || username.length() <= 0) {
			username = "default";
		}
	
		StringBuffer sqlbuff = new StringBuffer();
		// int jobsize=info.getUserJobname(username, jobname, sqlbuff).size();
		sqlbuff.append(";");
		int size = info.getUser(username, true, sqlbuff).size();
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("sqlbuff", sqlbuff.toString());
		jsonObj.put("size", size);
		// if(jobsize>0)
		// {
		// jsonObj.put("code", "0");
		// jsonObj.put("message", "之前已经有叫"+jobname+"的任务,请换个名字");
		// }else
		//
		if (size < 5) {
			MysqlCallback callback = new MysqlCallback(conn);
	
			String[] pcols = params == null ? new String[0] : new String(daycols
					+ params.replaceAll("维度指标：", "").replaceAll("。.*$", ""))
					.split(",");
			StringBuffer liststat = new StringBuffer();
			StringBuffer listgroup = new StringBuffer();
			for (String s : pcols) {
				if (AdhocOfflineService.isStatFields(s)) {
					liststat.append(s);
					liststat.append(",");
				} else {
					listgroup.append(s);
					listgroup.append(",");
				}
			}
	
			callback.setCols((params == null || params.isEmpty()) ? cols
					.toString() : listgroup.toString() + liststat.toString());
			OfflineDownload download = new OfflineDownload();
			download.setOffline(callback);
			if (mailto == null || mailto.length() <= 0) {
				mailto = "yannian.mu@alipay.com";
			}
			
			StringBuffer setSql=new StringBuffer();
			StringBuffer cleanSql=new StringBuffer();
			for(int i=0;i<joins.length;i++)
			{
				HigoAdhocJoinParams jp=joins[i];
				
				setSql.append(jp.createSql);
				setSql.append(";");
				setSql.append(jp.addData);
				setSql.append(";");

				cleanSql.append(";");
				cleanSql.append(jp.DropSql);
			}
	
			download.setMailto(mailto);
			download.setHql(setSql.toString()+" INSERT OVERWRITE DIRECTORY '" + store + "' " + hql+"  "+cleanSql.toString());
	
			download.setUseName(username);
			if (jobname == null || jobname.length() <= 0) {
				jobname = day + "_" + md5;
			}
			download.setJobName(jobname);
			download.setMemo(String.valueOf(memo));
			download.setDisplayParams((params == null || params.isEmpty()) ? hql
					: params);
			download.setStoreDir(store);
			download.setConfdir(hdpConf);
			download.setSqlMd5(md5);
			download.run();
	
			long t2 = System.currentTimeMillis();
			jsonObj.put("code", "1");
	
			jsonObj.put("message",
					"数据下载中...完成后将会通过<b style=\"color:red\">旺旺</b>和<b style=\"color:red\">邮件</b>通知");
			jsonObj.put("uuid", callback.getUuid());
			jsonObj.put("debug", callback.toString());
			jsonObj.put("timedebug", String.valueOf(t2 - t1));
	
		} else {
			jsonObj.put("code", "0");
			jsonObj.put("message", "还有" + size + "个任务没有完成数据下载，请稍后提交");
		}
	
		if (jsoncallback != null && jsoncallback.length() > 0) {
			return jsoncallback + "(" + jsonObj.toString() + ")";
		} else {
			return jsonObj.toString();
		}
	
	}

	public static boolean isStatFields(String field)
	{
		if(field.indexOf("求和(")>=0)
		{
			return true;
		}
		if(field.indexOf("count(")>=0)
		{
			return true;
		}
		if(field.indexOf("平均值(")>=0)
		{
			return true;
		}
		
		if(field.indexOf("最大值(")>=0)
		{
			return true;
		}
		
		if(field.indexOf("最小值(")>=0)
		{
			return true;
		}
		
		if(field.indexOf("计数(")>=0)
		{
			return true;
		}
		
		if(field.indexOf("dist(")>=0)
		{
			return true;
		}
		return false;
	}
}
