package com.alimama.mdrill.ui.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

import backtype.storm.utils.Utils;




import com.alimama.mdrill.adhoc.MD5;
import com.alimama.mdrill.adhoc.MySqlConn;
import com.alimama.mdrill.adhoc.MysqlCallback;
import com.alimama.mdrill.adhoc.MysqlInfo;
import com.alimama.mdrill.adhoc.OfflineDownload;
import com.alimama.mdrill.adhoc.TimeCacheMap;
import com.alimama.mdrill.json.JSONArray;
import com.alimama.mdrill.json.JSONException;
import com.alimama.mdrill.json.JSONObject;
import com.alimama.mdrill.partion.GetPartions;
import com.alimama.mdrill.partion.GetShards;
import com.alimama.mdrill.partion.GetShards.ShardsList;
import com.alimama.mdrill.partion.GetPartions.*;
import com.alimama.mdrill.ui.service.AdhocWebServiceParams.HigoAdhocJoinParams;
import com.alimama.mdrill.ui.service.MdrillService;
import com.alimama.mdrill.ui.service.partions.AdhocHivePartions;
import com.alimama.mdrill.ui.service.partions.AdhocHivePartions.KmeansQueryParse;
import com.alimama.mdrill.ui.service.utils.WebServiceParams;
import com.alimama.mdrill.utils.HadoopBaseUtils;
import com.alimama.mdrill.utils.IndexUtils;
import com.alimama.quanjingmonitor.kmeans.KMeansDriver;
import com.alimama.quanjingmonitor.kmeans.MysqlCallbackKmeans;
import com.alimama.quanjingmonitor.kmeans.PrintSql;

public class AdhocOfflineService {
	private static Logger LOG = Logger.getLogger(AdhocOfflineService.class);

	public static void readAbtestResult(String uuid, OutputStreamWriter outStream,String match,boolean onlycustid)
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
			AdhocOfflineService.readAbtestResult(result.get("storedir"), outStream, conf,match,onlycustid);
		}
	
		// outStream.write(exehql.replaceAll("\001", ",").replaceAll("\t",
		// "\t")+"\n");
	
	}
	

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
		
		
		
		public static void readAbtestInfo(String varstr,String uuid, OutputStreamWriter outStream)
		throws SQLException, IOException, JSONException {
	
	JSONObject jsonObjresult = readAbtestInfo(uuid);

	
	if(varstr!=null&&varstr.length()>0)
	{
		outStream.write(""+varstr+"("+jsonObjresult.toString()+");");
	}else{
		outStream.write(jsonObjresult.toString());
	}

}
		
		
		public static JSONObject readAbtestInfo(String uuid)
				throws SQLException, IOException, JSONException {
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

			Path A;
			Path B;
			JSONObject jsonObj = new JSONObject();
			if (result != null && result.containsKey("storedir")
					&& result.containsKey("cols")) {
	
				
				Path base=makeAB(result.get("storedir"), conf);
				A=new Path(base,"A");
				B=new Path(base,"B");

				jsonObj.put("A", A.toString());
				jsonObj.put("B", B.toString());
			}else{
				jsonObj.put("A","");
				jsonObj.put("B", "");
			}
			
			
			
			jsonObj.put("params", result);
			
			JSONObject jsonObjresult = new JSONObject();
			jsonObjresult.put("code", 1);
			jsonObjresult.put("data", jsonObj);

			
			return jsonObjresult;

		}
		
		public static Path makeAB(String path,Configuration conf) throws IOException
		{
			Path base=new Path(new Path(path).getParent(),"AB");
			try {
				AdhocOfflineService.readAbtestResult(path,new Path(base,"A"),new Path(base,"B"), conf);
				return base;
			} catch (Throwable e) {
				FileSystem fs = FileSystem.get(conf);
				if(fs.exists(base))
				{
					fs.delete(base, true);
				}
				throw new IOException(e);
			}
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
	
	static TimeCacheMap<String, Object> lockMap=new TimeCacheMap<String, Object>(1200);
	public static void readAbtestResult(String path,
			Path A,Path B, Configuration conf)
			throws IOException {
		
		String key=new Path(String.valueOf(path)).toString();
		Object lockObj = null;
		synchronized (lockMap) {
			lockObj = lockMap.get(key);
			if (lockObj == null) {
				lockObj = new Object();
				lockMap.put(key, lockObj);
				;
			}
		}
		
		synchronized (lockObj) {
		
		conf.setInt("dfs.replication", 8);
		FileSystem fs = FileSystem.get(conf);
		Path dir = new Path(path);
		if (!fs.exists(dir)) {
			throw new IOException("can not found path:" + path);
		}
		
		if (fs.exists( A)&&fs.exists(B)&&fs.getFileStatus(A).getLen()>0&&fs.getFileStatus(B).getLen()>0) {
			return ;
		}
		
		
		  IndexUtils.truncate(fs, A);
		  IndexUtils.truncate(fs,B);
		 FSDataOutputStream outputA= fs.create(A,true);		
		 FSDataOutputStream outputB= fs.create(B,true);		
				
		
		FileStatus[] filelist = fs.listStatus(dir);
	
		Long bytesRead = 0l;
		long maxsize = 1024l * 1024 * 1024 * 100;
		
	
		for (FileStatus f : filelist) {
			if (!f.isDir() && !f.getPath().getName().startsWith("_")) {
				FSDataInputStream in = fs.open(f.getPath());
				BufferedReader bf=new BufferedReader(new InputStreamReader(in)); 
				String line;
				while ((line = bf.readLine()) != null) {
					bytesRead += line.getBytes().length;
					String towrite=line;
					towrite=line.replaceAll(",", "_").replaceAll("\001", ",").replaceAll("\t", ",").replaceAll("\"", "");
					if(!towrite.isEmpty()&&towrite.indexOf("rep0")>=0)
					{

						String[] cols=towrite.split(",");
						for(String s:cols)
						{
							if(s.indexOf("@abtest@")>=0)
							{
								String[] sss=s.split("@abtest@");
								if(sss.length>1)
								{
									
									outputA.writeUTF(sss[1]+"\r\n");
								break;
								}

							}
						}
					
					}
					
					if(!towrite.isEmpty()&&towrite.indexOf("rep1")>=0)
					{

						String[] cols=towrite.split(",");
						for(String s:cols)
						{
							if(s.indexOf("@abtest@")>=0)
							{
								String[] sss=s.split("@abtest@");
								if(sss.length>1)
								{
									outputB.writeUTF(sss[1]+"\r\n");
								break;
								}

							}
						}
					
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
		
		 outputA.close();
		 outputB.close();
		 

		return;
		
		}
	}

	public static void readAbtestResult(String path,
			Writer outStream, Configuration conf,String match,boolean onlycustid)
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
					String towrite=line;
					towrite=line.replaceAll(",", "_").replaceAll("\001", ",").replaceAll("\t", ",").replaceAll("\"", "");
					if(!towrite.isEmpty()&&towrite.indexOf(match)>=0)
					{
						
						String label="A";
						if(towrite.indexOf("rep0")>=0)
						{
							label="A";
						}
						if(towrite.indexOf("rep1")>=0)
						{
							label="B";
						}
						
						if(onlycustid)
						{
							String[] cols=towrite.split(",");
							for(String s:cols)
							{
								if(s.indexOf("@abtest@")>=0)
								{
									String[] sss=s.split("@abtest@");
									if(sss.length>1)
									{
									outStream.write(sss[1]+","+label);
									outStream.write("\r\n");
									break;
									}

								}
							}
						}else{
							outStream.write(towrite);
							outStream.write("\r\n");	
						}
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
					String towrite=line;
					if(line.indexOf("\001")>=0)
					{
						towrite=line.replaceAll(",", "_").replaceAll("\001", ",").replaceAll("\t", "_").replaceAll("\"", "");
					}else if(line.indexOf("\t")>=0)
					{
						towrite=line.replaceAll(",", "_").replaceAll("\001", "_").replaceAll("\t", ",").replaceAll("\"", "");
					}
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
	
	public static hivePartion getPartion(Map stormconf,String projectName)
	{
		hivePartion rtn=new hivePartion();
		

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
		if (projectName.equals("r_rpt_nz_adhoc_member")) {
			hpart="ds";
		}
		if (projectName.equals("st_tanx_x_core_gateway")) {
			hpart="ds";
		}
		if (projectName.equals("r_rpt_tanx_amif_adhoc_adzone")) {
			hpart="ds";
		}
		if (projectName.equals("r_rpt_tanx_amif_zx_adzone")) {
			hpart="ds";
		}

		String mode=String.valueOf(stormconf.get("higo.mode."+projectName));
		if(mode.indexOf("@hivepart:")>=0)
		{
			Pattern mapiPattern = Pattern.compile("@hivepart:([^@]+)@");
			Matcher mat = mapiPattern.matcher(mode);
			if (mat.find()) {
				hpart = mat.group(1);
			}
		}
		
		rtn.isPartionByPt=isPartionByPt;
		rtn.hpart=hpart;
		rtn.projectName=projectName;
		return rtn;
	}
	
	private static String[] Json2Array(String j) throws JSONException
	{
    	JSONArray jsonStr=new JSONArray(j.trim());
    	ArrayList<String> rtn=new ArrayList<String>();
    	for(int i=0;i<jsonStr.length();i++)
    	{
    		rtn.add(jsonStr.getString(i));
    	}
    	
    	return rtn.toArray(new String[rtn.size()]);

	}
	
	
	public static void main(String[] args) throws Exception {
		
		String querStr="[{\"thedate\":{\"operate\":9,\"value\":[\"20140707\",\"20140707\"]}},{\"ismatch\":{\"operate\":\"1\",\"value\":\"搜索\"}}]";
		KmeansQueryParse kmeansData = AdhocHivePartions.getKmeansDays(querStr);
		System.out.println(kmeansData.queryStr);

		ArrayList<String> fqList = WebServiceParams.fqListHive(false,"dt",querStr,false, new HashMap<String, String>(),null,null,null);
		
		StringBuffer sqlWhere =AdhocWebServiceParams.makeWhere(fqList,new ArrayList<String>(),"userid");

		
	}
	public static String kmeans(String projectName, String jsoncallback, String queryStr,final int count,final int rep,String idcols
			,String colls_important_json,String number_important_json,String colls_json,String numbers_json,
			
			String mailto, String username, String jobname, String params,String memo) throws Exception
	{
		
		String[] excludeIds=memo.split(",");
		ArrayList<String> exlist=new ArrayList<String>();
		for(String uuid:excludeIds)
		{
			String uuuuid=uuid.trim().replaceAll("\t", "");
			if(uuuuid.isEmpty())
			{
				continue;
			}
			JSONObject info=readAbtestInfo(uuuuid);
			LOG.info(uuid+">>"+info.toString());
			if(String.valueOf(info.get("code")).equals("1"))
			{
				exlist.add(String.valueOf(info.getJSONObject("data").get("A")));
				exlist.add(String.valueOf(info.getJSONObject("data").get("B")));
				
			}
		}
		
		
		String[] number_important=Json2Array(number_important_json);
		String[] colls_important=Json2Array(colls_important_json);
		String[] colls=Json2Array(colls_json);
		String[] numbers=Json2Array(numbers_json);
		
		long t1 = System.currentTimeMillis();
	
		Map stormconf = Utils.readStormConfig();
		
		hivePartion hp=getPartion(stormconf, projectName);
		boolean isPartionByPt=hp.isPartionByPt;
		String hpart=hp.hpart;
		projectName=hp.projectName;

		String mode=String.valueOf(stormconf.get("higo.mode."+projectName));

		queryStr = WebServiceParams.query(queryStr);


		TablePartion part = GetPartions.partion(projectName);
		KmeansQueryParse kmeansData = AdhocHivePartions.getKmeansDays(queryStr);
		
		LinkedHashMap<String, String> filetypeMap = MdrillFieldInfo.readFieldsFromSchemaXml(stormconf,part.name);
		ArrayList<String> fqList = WebServiceParams.fqListHive(false,hpart,kmeansData.queryStr,isPartionByPt, filetypeMap,null,null,null);
		
		StringBuffer sqlWhere =AdhocWebServiceParams.makeWhere(fqList,exlist,idcols);
		LOG.info("queryStr:"+queryStr+">>>>kmeansData.queryStr:"+kmeansData.queryStr+","+sqlWhere.toString()+","+fqList.toString());

	
//		String hql = "select custid from " + projectName + " " + sqlWhere.toString() + " " ;
		final String[] sqlparams=PrintSql.makeSql(projectName, sqlWhere.toString(), idcols, kmeansData.getSortDays(), colls_important, number_important, colls, numbers);;
		String hql=sqlparams[0];
		
		String md5 = MD5.getMD5(hql);
	
		SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
		String day = fmt.format(new Date());
	
		final String hdpConf = (String) stormconf.get("hadoop.conf.dir");
		String connstr = (String) stormconf.get("higo.download.offline.conn");
		String uname = (String) stormconf.get("higo.download.offline.username");
		String passwd = (String) stormconf.get("higo.download.offline.passwd");
		final String store = (String) stormconf.get("higo.download.offline.store")+ "/" + day + "/" + java.util.UUID.randomUUID().toString();
		MySqlConn conn = new MySqlConn(connstr, uname, passwd);
		MysqlInfo info = new MysqlInfo(conn);
		if (username == null || username.length() <= 0) {
			username = "default";
		}
	
		StringBuffer sqlbuff = new StringBuffer();
		sqlbuff.append(";");
		int size = info.getUser(username, true, sqlbuff).size();
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("sqlbuff", sqlbuff.toString());
		jsonObj.put("size", size);

		final String storepath=store+"/abtest/cluster_abtest";
		if (size < 5) {
			final MysqlCallbackKmeans callback = new MysqlCallbackKmeans(conn);
			
			Runnable process=new Runnable() {
				
				@Override
				public void run() {
					Configuration conf=new Configuration();
			 		HadoopBaseUtils.grabConfiguration(hdpConf, conf);
					try {
						ToolRunner.run(conf, new KMeansDriver(callback), new String[]{
							""
							,store+"/hive"
							,store+"/abtest"
							,"20"
							,"1000" //k
							,"0.00001" //delta
							,String.valueOf(count)
							,String.valueOf(rep)
							,String.valueOf(100)
							,sqlparams[1]
							,String.valueOf(548576)
						});
						
						makeAB(storepath, conf);

					} catch (Exception e) {
						throw new RuntimeException(e);
					}
					
				}
			};
			callback.setCols("");
			OfflineDownload download = new OfflineDownload();
			download.setOffline(callback);
			if (mailto == null || mailto.length() <= 0) {
				mailto = "yannian.mu@alipay.com";
			}
			
			download.setMailto(mailto);
			download.setHql(" INSERT OVERWRITE DIRECTORY '" + store+"/hive" + "' " + hql+"  ");
			download.setUseName(username);
			if (jobname == null || jobname.length() <= 0) {
				jobname = day + "_" + md5;
			}
			download.setJobName(jobname);
			download.setMemo(String.valueOf(memo));
			download.setDisplayParams(params);
			download.setStoreDir(storepath);
			download.setConfdir(hdpConf);
			download.setSqlMd5(md5);
			download.setProcesser(process);
			download.run();
	
			long t2 = System.currentTimeMillis();
			jsonObj.put("code", "1");
	
			jsonObj.put("message","数据下载中...完成后将会通过<b style=\"color:red\">旺旺</b>和<b style=\"color:red\">邮件</b>通知");
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

	
	public static class hivePartion{
		boolean isPartionByPt = false;
		String hpart="dt";
		String projectName;


	}
	
	private Object part;
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
	
		Map stormconf = Utils.readStormConfig();
		
		hivePartion hp=getPartion(stormconf, projectName);
		boolean isPartionByPt=hp.isPartionByPt;
		String hpart=hp.hpart;
		projectName=hp.projectName;

		String mode=String.valueOf(stormconf.get("higo.mode."+projectName));

		queryStr = WebServiceParams.query(queryStr);

		
		boolean isnothedate=mode.indexOf("@nothedate@")>=0;
		if(isnothedate)
		{
			if(fl!=null)
			{
			fl=fl.replaceAll("thedate", hpart);
			}
			if(groupby!=null)
			{
				groupby=groupby.replaceAll("thedate", hpart);
			}
			if(queryStr!=null)
			{
				queryStr=queryStr.replaceAll("thedate", hpart);
			}
		}

		
		if (projectName.equals("fact_wirelesspv_clickcosteffect1_app_adhoc_d_1")) {
			projectName="fact_wirelesspv_clickcosteffect1_app_adhoc_d";
			JSONArray jsonStr=new JSONArray(queryStr.trim());
			JSONObject obj=new JSONObject();
			obj.put("key", "app_type");
			obj.put("operate", "1");
			obj.put("value", "1");
			jsonStr.put(obj);
			queryStr = WebServiceParams.query(jsonStr.toString());
		}
		if (projectName.equals("fact_wirelesspv_clickcosteffect1_app_adhoc_d_2")) {
			projectName="fact_wirelesspv_clickcosteffect1_app_adhoc_d";
			JSONArray jsonStr=new JSONArray(queryStr.trim());
			JSONObject obj=new JSONObject();
			obj.put("key", "app_type");
			obj.put("operate", "1");
			obj.put("value", "2");
			jsonStr.put(obj);
			queryStr = WebServiceParams.query(jsonStr.toString());
		}
		if (projectName.equals("fact_wirelesspv_clickcosteffect1_app_adhoc_d_3")) {
			projectName="fact_wirelesspv_clickcosteffect1_app_adhoc_d";
			JSONArray jsonStr=new JSONArray(queryStr.trim());
			JSONObject obj=new JSONObject();
			obj.put("key", "app_type");
			obj.put("operate", "1");
			obj.put("value", "3");
			jsonStr.put(obj);
			queryStr = WebServiceParams.query(jsonStr.toString());
		}
		
		if (projectName.equals("fact_wirelesspv_clickcosteffect1_app_adhoc_d_4")) {
			projectName="fact_wirelesspv_clickcosteffect1_app_adhoc_d";
			JSONArray jsonStr=new JSONArray(queryStr.trim());
			JSONObject obj=new JSONObject();
			obj.put("key", "app_type");
			obj.put("operate", "1");
			obj.put("value", "4");
			jsonStr.put(obj);
			queryStr = WebServiceParams.query(jsonStr.toString());
		}
		
		
	
	
		TablePartion part = GetPartions.partion(projectName);
		ShardsList[] cores = GetShards.getCoresNonCheck(part);
		ShardsList[] ms = GetShards.getMergers(part.name);
		String[] partionsAll = AdhocHivePartions.get(queryStr, part.parttype);
		GetPartions.Shards shard = GetPartions.getshard(part, partionsAll,cores, ms);
		
		
		
		LinkedHashMap<String, String> filetypeMap = MdrillFieldInfo.readFieldsFromSchemaXml(stormconf,part.name);
		ArrayList<String> fqList = WebServiceParams.fqListHive(false,hpart,queryStr,
				isPartionByPt, filetypeMap,null,null,null);
		
		
		
		
		StringBuffer sqlWhere =AdhocWebServiceParams.makeWhere(fqList,new ArrayList<String>(),"userid");
	
	
		
		
		ArrayList<String> groupFields = WebServiceParams.groupFields(groupby);
		ArrayList<String> showFields = WebServiceParams.showHiveFields(fl);
		HigoAdhocJoinParams[] joins=AdhocWebServiceParams.parseJoinsHive(leftjoin, shard);
		String daycols=AdhocWebServiceParams.parseDayCols(hpart,groupFields,showFields);
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
				buffer.append(" join ("+jp.frQuer+") jr"+i+" on (jl1."+colMap.get(jp.leftkey)+" is not null and trim(jl1."+colMap.get(jp.leftkey)+")==trim(jr"+i+"."+jp.rightkey+")) ");
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
						join=",";
					}
				}
			}
			
			hql+=" "+sqlGroup.toString();

			
		}
		
		
		
		
		 ArrayList<String> fq2list=WebServiceParams.fqListHive(true,hpart,fq2, 
					isPartionByPt, filetypeMap,colMap,colMapforStatFilter,"fq2");
		 if(fq2list.size()>0||orderby2!=null)
		 {
				StringBuffer buffer=new StringBuffer();
				buffer.append("select * from ");
				buffer.append("("+hql+") fq2");
				
				if(fq2list.size()>0)
				{
					String join2 = " where ";
					for (String fq : fq2list) {
						buffer.append(join2);
						buffer.append(fq);
						join2 = " and ";
					}
				}
				
				 if(orderby2!=null)
				 {
					 buffer.append(" order by "+WebServiceParams.parseFqAlias(orderby2, colMap, colMapforStatFilter, "fq2")+" "+desc2);
				 }
				hql=buffer.toString();
		 }
		 
		

		 
		
		 
		 if(limit >1000000)
		 {
			 limit=1000000;
		 }
		 if(limit>0)
		 {
			 hql=hql+" limit "+limit;
 
		 }
		 
		 hql=hql.replaceAll("dist\\((.*)\\)", "count(distinct($1))");
		
	
		String md5 = MD5.getMD5(hql);
	
		SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
		String day = fmt.format(new Date());
	
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
