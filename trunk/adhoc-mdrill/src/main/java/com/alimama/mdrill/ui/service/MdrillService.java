package com.alimama.mdrill.ui.service;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.*;
import org.apache.solr.client.solrj.impl.*;
import org.apache.solr.client.solrj.response.*;

import backtype.storm.utils.Utils;

import com.alipay.bluewhale.core.cluster.SolrInfo.ShardCount;
import com.alimama.mdrill.json.JSONArray;
import com.alimama.mdrill.json.JSONException;
import com.alimama.mdrill.json.JSONObject;
import com.alimama.mdrill.partion.GetPartions;
import com.alimama.mdrill.partion.GetShards;
import com.alimama.mdrill.partion.GetPartions.TablePartion;
import com.alimama.mdrill.partion.GetShards.SolrInfoList;
import com.alimama.mdrill.ui.service.utils.WebServiceParams;
import com.alimama.mdrill.ui.service.utils.WebServiceParams.HigoJoinParams;
import com.alimama.mdrill.ui.service.utils.WebServiceParams.SortParam;
import com.alimama.mdrill.utils.HadoopUtil;
import com.alimama.mdrill.utils.UniqConfig;
import com.alipay.bluewhale.core.cluster.ShardsState;
import com.alipay.bluewhale.core.cluster.SolrInfo;

import org.apache.log4j.Logger;


public class MdrillService {
	private static Logger LOG = Logger.getLogger(MdrillService.class);

	private static String logRequest(String projectName, String callback,
			String startStr, String rowsStr, String queryStr, String dist,
			String fl, String groupby, String sort, String order,String leftjoin) {
		StringBuffer debugBuffer = new StringBuffer();
		debugBuffer.append(projectName).append(",").append(startStr)
				.append(",").append(rowsStr).append(",").append(queryStr)
				.append(",").append(fl).append(",");
		debugBuffer.append(groupby).append(",").append(sort).append(",")
				.append(order).append(",").append(leftjoin).append(",");
		return debugBuffer.toString();
	}



	public static String result(String projectName, String callback,
			String startStr, String rowsStr, String queryStr, String dist,
			String fl, String groupby, String sort, String order,String leftjoin)
			throws Exception {
		long t1=System.currentTimeMillis();
		String logParams = logRequest(projectName, callback, startStr, rowsStr,
				queryStr, dist, fl, groupby, sort, order,leftjoin);
		LOG.info("higorequest:" + logParams);
		TablePartion part = GetPartions.partion(projectName);

		try {
			queryStr = WebServiceParams.query(queryStr);
			queryStr = WebServiceParams.queryadHoc(queryStr,part.parttype);// adhocʱ������
			
			String logParams2 = logRequest(projectName, callback, startStr, rowsStr,
					queryStr, dist, fl, groupby, sort, order,leftjoin);
			LOG.info("query:" + logParams2);

			int start = WebServiceParams.parseStart(startStr);
			int rows = WebServiceParams.parseRows(rowsStr);
			
			SortParam sortType = WebServiceParams.sort(sort, order);

			ArrayList<String> groupbyFields = WebServiceParams.groupFields(groupby);
			ArrayList<String> showFields = WebServiceParams.showFields(fl);
			HashSet<String> commonStatMap = new HashSet<String>();
			HashSet<String> distStatFieldMap = new HashSet<String>();
			WebServiceParams.setCrossStatMap(showFields,commonStatMap,distStatFieldMap);
	
			HashMap<String, String> fieldColumntypeMap = readFieldsFromSchemaXml(part.name);

	 
			String[] cores = GetShards.get(part.name, false, 10000);
			String[] ms = GetShards.get(part.name, true, 10000);

			String[] partionsAll = GetPartions.get(queryStr, part.parttype);

			Arrays.sort(partionsAll);
			LOG.info("partionsAll:" + Arrays.toString(partionsAll));


			GetPartions.Shards shard = GetPartions.getshard(part, partionsAll,cores, ms,  10000, 0);
			HigoJoinParams[] joins=WebServiceParams.parseJoins(leftjoin, shard);
			ArrayList<String> fqList = WebServiceParams.fqList(queryStr, shard,	fieldColumntypeMap);

			String rtn= result(part,callback, fqList, shard, start, rows, sortType,
					groupbyFields, showFields, commonStatMap,
					distStatFieldMap,joins);
			long t2=System.currentTimeMillis();
			LOG.info("timetaken:"+(t2-t1)+",logParams2:"+logParams2);
			return rtn;

		} catch (RuntimeException e) {
			SolrInfoList infolist=GetShards.getSolrInfoList(part.name);
	    	infolist.run();
			long t2=System.currentTimeMillis();
			LOG.error("timetaken:"+(t2-t1)+",logParams:"+logParams);
			LOG.error(logParams, e);
			throw e;
		} catch (Exception e) {
			SolrInfoList infolist=GetShards.getSolrInfoList(part.name);
	    	infolist.run();
			long t2=System.currentTimeMillis();
			LOG.error("timetaken:"+(t2-t1)+",logParams:"+logParams);
			LOG.error(logParams, e);
			throw e;
		}

	}
	
	
	public static String fieldValueList(String projectName, String callback,
			String field, String startStr, String rowsStr, String queryStr)
			throws Exception {
		return result(projectName, callback, "0", "1000", queryStr, "", null,
				field, null, null,null);
	}

	private static Configuration getConf(Map stormconf) {
		String hadoopConfDir = (String) stormconf.get("hadoop.conf.dir");
		String opts = (String) stormconf.get("hadoop.java.opts");
		Configuration conf = new Configuration();
		conf.set("mapred.child.java.opts", opts);
		HadoopUtil.grabConfiguration(hadoopConfDir, conf);
		return conf;
	}

	public static String basePath = null;

	public static void setBasePath(String basePath) {
		MdrillService.basePath = basePath;
	}

	static HashMap<String, HashMap<String, String>> cache = new HashMap<String, HashMap<String, String>>();

	
	public static String getBasePath()
	{
		Map stormconf = Utils.readStormConfig();
		basePath=(String) stormconf.get("higo.table.path");
		if (basePath == null || basePath.isEmpty()) {
			basePath = System.getenv("higo.table.path");
		}
		if (basePath == null || basePath.isEmpty()) {
			basePath = System.getProperty("higo.table.path",
					"/group/taobao/external/p4p/p4padhoc/tablelist");
		}
		
		if (basePath == null || basePath.isEmpty()) {
			basePath = "/group/taobao/external/p4p/p4padhoc/tablelist";
		}
		
		return basePath;
	}
	public static synchronized HashMap<String, String> readFieldsFromSchemaXml(
			String tablename) throws Exception {

		if (tablename.equals("rpt_p4padhoc_auction")) {
			tablename = "rpt_hitfake_auctionall_d";
		}

		HashMap<String, String> datatype = cache.get(tablename);
		if (datatype != null) {
			return datatype;
		}
		datatype = new HashMap<String, String>();

		Map stormconf = Utils.readStormConfig();
		Configuration conf = getConf(stormconf);
		FileSystem fs = FileSystem.get(conf);
		String regex = "<field\\s+name=\"([^\"]*?)\"\\s+type=\"([^\"]*?)\"\\s+indexed=\"([^\"]*?)\"\\s+stored=\"([^\"]*?)\"\\s*/>";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher("");
		BufferedReader br = null;
		try {
			

			FSDataInputStream in = fs.open(new Path(getBasePath(), tablename
					+ "/solr/conf/schema.xml"));
			br = new BufferedReader(new InputStreamReader(in));
			String temp = null;
			while ((temp = br.readLine()) != null) {
				matcher.reset(temp);
				if (matcher.find()) {
					datatype.put(matcher.group(1), matcher.group(2));
				}
			}
			in.close();
		}catch(Exception e){
		} finally {
			if (br != null) {
				br.close();
			}
		}
		LOG.info(tablename + ">>" + datatype.toString());

		cache.put(tablename, datatype);
		return datatype;

	}

	private static String result(TablePartion part,String callback, ArrayList<String> fqList,
			GetPartions.Shards shard, int start, int rows, final SortParam sortType,
			ArrayList<String> groupbyFields, ArrayList<String> showFields,
			HashSet<String> commonStatMap,
			HashSet<String> distStatFieldMap,HigoJoinParams[] joins) throws Exception {
		CommonsHttpSolrServer server = WebServiceParams.makeServer(shard);
		SolrQuery query = WebServiceParams.makeSolrQuery(shard);
		JSONObject jsonObj = new JSONObject();
		try {
			if (groupbyFields.size() > 0) {
				int minstart = start;
				int maxEend = rows;
				if (sortType.isnum) {
					minstart = start - 100;
					if (minstart < 0) {
						minstart = 0;
					}
					maxEend = Math.min(rows + 100,UniqConfig.defaultCrossMaxLimit());
				}
				WebServiceParams.setGroupByQuery(query, fqList, groupbyFields,
						minstart, maxEend, distStatFieldMap, commonStatMap, sortType,joins,null);
				LOG.info("queryinfo:"+shard.urlMain + "/select/?" + query.toString());
				QueryResponse qr = server.query(query, SolrRequest.METHOD.POST);
				LinkedHashMap<String,Count> groupValueCache=WebServiceParams.setGroupByResult(jsonObj, qr, groupbyFields, showFields,joins,null);
				
				if(jsonObj.getLong("total")>(UniqConfig.defaultCrossMaxLimit()-10)&&groupValueCache.size()<=UniqConfig.defaultCrossMaxLimit()&&jsonObj.getString("code").equals("1"))
				{
					try{
					query = WebServiceParams.makeSolrQuery(shard);
					WebServiceParams.setGroupByQuery(query, fqList, groupbyFields,
							0, Math.min(groupValueCache.size()*10,UniqConfig.defaultCrossMaxLimit()), distStatFieldMap, commonStatMap, sortType,joins,groupValueCache);
					LOG.info("queryinfo_pre:"+shard.urlMain + "/select/?" + query.toString());
					QueryResponse qr2 = server.query(query, SolrRequest.METHOD.POST);
					WebServiceParams.setGroupByResult(jsonObj, qr2, groupbyFields, showFields,joins,groupValueCache);
					}catch(Exception e2)
					{
						LOG.error("queryinfo_pre_exception",e2);

					}
				}
				
				
				if(jsonObj.getString("code").equals("1")&&sortType.isnum)
				{
					JSONArray jsonArray=jsonObj.getJSONObject("data").getJSONArray("docs");
					ArrayList<JSONObject> results=new ArrayList<JSONObject>();
					boolean iscontains=true;
					for(int i=0;i<jsonArray.length();i++)
					{
						JSONObject obj=jsonArray.getJSONObject(i);
						if(!obj.has(sortType.sortRow))
						{
							iscontains=false;
						}
						results.add(obj);
					}
					final boolean isdesc=sortType.order.toLowerCase().equals("true");
					if(sortType.isnum&&iscontains&&jsonObj.getLong("total")>(UniqConfig.defaultCrossMaxLimit()-10))
					{
						Collections.sort(results, new Comparator<JSONObject>() {
							@Override
							public int compare(JSONObject o1, JSONObject o2) {
								int rtn=0;
								try {
									rtn = Double.compare(ParseDouble(o1.get(sortType.sortRow)), ParseDouble(o2.get(sortType.sortRow)));
								} catch (JSONException e) {
								}
								if(isdesc)
								{
									rtn*=-1;
								}
								return rtn;
							}
						}
						);
					}
					
					JSONArray rtnarr=new JSONArray();
					int actualstart=start-minstart;
					int actualend=rows+actualstart;
					for(int i=actualstart;i<results.size()&&i<actualend;i++)
					{
						rtnarr.put(results.get(i));
					}
					
					jsonObj.getJSONObject("data").put("docs", rtnarr);
				}
				

			} else if (commonStatMap.size() > 0
					|| distStatFieldMap.size() > 0) {
				ArrayList<String> groupFieldsEmpty = WebServiceParams
						.groupFields("higoempty_groupby_l");
				WebServiceParams.setGroupByQuery(query, fqList,
						groupFieldsEmpty, start, rows, distStatFieldMap,
						commonStatMap, sortType,joins,null);
				LOG.info("queryinfo:"+shard.urlMain + "/select/?" + query.toString());
				QueryResponse qr = server.query(query, SolrRequest.METHOD.POST);
				WebServiceParams.setGroupByResult(jsonObj, qr, groupbyFields,showFields,joins,null);
			} else {
				WebServiceParams.setDetailByQuery(
						query, fqList, showFields,
						start, rows, sortType,joins);
				LOG.info("queryinfo:"+shard.urlMain + "/select/?" + query.toString());
				QueryResponse qr = server.query(query, SolrRequest.METHOD.POST);
				WebServiceParams.setDetailResult(jsonObj, qr, showFields,joins);
			}

		} catch (Exception e) {
			SolrInfoList infolist=GetShards.getSolrInfoList(part.name);
	    	infolist.run();
			LOG.error("higocall,exceptin", e);
			jsonObj.put("code", "0");
			jsonObj.put("message", WebServiceParams.errorToString(e));
		}

		jsonObj.put("____debugurl",
				shard.urlMain + "/select/?" + query.toString());

		if (callback != null && callback.length() > 0) {
			return callback + "(" + jsonObj.toString() + ")";
		} else {
			return jsonObj.toString();
		}
	}

	
	public static Double ParseDouble(Object s)
	{
		try{
		return Double.parseDouble(String.valueOf(s));
		}catch(Throwable e)
		{
			return (double) s.hashCode();
		}
	}
	public static String notice(String projectName, String callback,
			String groupby) throws JSONException {
		try {
		    HashMap<String,Long> dayAmt = new HashMap<String, Long>();
			SolrInfoList infolist=GetShards.getSolrInfoList(projectName);
			for (SolrInfo info : infolist.getlist()) {
				if(info!=null)
			    {
			    	if(info.stat==ShardsState.SERVICE&&!info.isMergeServer)
			    	{
			    		for(Entry<String, ShardCount> e:info.daycount.entrySet())
			    		{
			    			Long amt=dayAmt.get(e.getKey());
			    			if(amt==null)
			    			{
			    				amt=0l;
			    			}
			    			long cnt=e.getValue().cnt;

			    			if(cnt>1000)
			    			{
			    				amt+=1;
			    			}
			    			dayAmt.put(e.getKey(), amt);
			    		}
			    	}
			    }
			}
			
			long maxamt=0;
			for(Entry<String,Long> e:dayAmt.entrySet())
			{
				maxamt=Math.max(e.getValue(),maxamt);
			}
			
			ArrayList<String> matchlist=new ArrayList<String>(); 
			for(Entry<String,Long> e:dayAmt.entrySet())
			{
				if(e.getValue()>=maxamt)
				{
					matchlist.add(e.getKey());
				}
			}
			
			int maxValue = 0;
			int minValue = Integer.MAX_VALUE;
			int fcsize = 0;
			for (String s:matchlist) {
				fcsize++;
				int value = Integer.parseInt(String.valueOf(s));
				if (value > maxValue) {
					maxValue = value;
				}
				if (value < minValue) {
					minValue = value;
				}
			}
			JSONObject jsonObj = new JSONObject();
			JSONObject jo = new JSONObject();
			jo.put("min", minValue);
			jo.put("max", maxValue);
			jsonObj.put("data", jo);
			jsonObj.put("code", "1");
			jsonObj.put("message", "success");
			jsonObj.put("total", fcsize);
			return callback + "(" + jsonObj.toString() + ")";
		} catch (Exception e) {
			throw new JSONException(e);
		}

	}


}
