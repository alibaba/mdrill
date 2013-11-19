package com.alimama.mdrill.ui.service;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;

import javax.servlet.jsp.JspWriter;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.solr.client.solrj.*;
import org.apache.solr.client.solrj.impl.*;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.response.*;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.request.compare.GroupbyRow;

import backtype.storm.utils.Utils;

import com.alipay.bluewhale.core.cluster.SolrInfo.ShardCount;
import com.alimama.mdrill.json.JSONArray;
import com.alimama.mdrill.json.JSONException;
import com.alimama.mdrill.json.JSONObject;
import com.alimama.mdrill.partion.GetPartions;
import com.alimama.mdrill.partion.GetShards;
import com.alimama.mdrill.partion.GetShards.ShardsList;
import com.alimama.mdrill.partion.MdrillPartions;
import com.alimama.mdrill.partion.GetPartions.TablePartion;
import com.alimama.mdrill.partion.GetShards.SolrInfoList;
import com.alimama.mdrill.partion.MdrillPartionsInterface;
import com.alimama.mdrill.ui.service.utils.WebServiceParams;
import com.alimama.mdrill.ui.service.utils.WebServiceParams.HigoJoinParams;
import com.alimama.mdrill.ui.service.utils.WebServiceParams.SortParam;
import com.alimama.mdrill.utils.HadoopUtil;
import com.alimama.mdrill.utils.UniqConfig;
import com.alipay.bluewhale.core.cluster.ShardsState;
import com.alipay.bluewhale.core.cluster.SolrInfo;
import com.alipay.bluewhale.core.utils.StormUtils;

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
	
	public static class HeartBeat implements Runnable
	{
		public Object lock=new Object();
		JspWriter out;
		public HeartBeat(JspWriter out) {
			super();
			this.out = out;
		}
		AtomicBoolean isstop=new AtomicBoolean(false);
		

		public void setIsstop(boolean isstop) {
				this.thrStop.set(isstop);
		}
		
		AtomicBoolean thrStop=new AtomicBoolean(false);

		public boolean isstop()
		{
			return thrStop.get();
		}
		
		public void stop()
		{
			this.setIsstop(true);
		    while(!this.isstop())
		    {
		    	try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
		    }
		}
		

		@Override
		public void run() {
			
			while(true)
			{
					if(this.thrStop.get())
					{
						thrStop.set(true);
						return ;
					}
					
				try {
					synchronized (this.lock) {
						if(this.out!=null)
						{
							this.out.write(" ");
							this.out.flush();
						}
					}
				} catch (Throwable e) {
				}
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}
			
		}
		
	}


	private static AtomicLong InsertIndex=new AtomicLong(0); 
	
	
	public static Long uuid()
	{
		CRC32 crc32 = new CRC32();
		crc32.update(String.valueOf(java.util.UUID.randomUUID().toString()).getBytes());
		return crc32.getValue();
	}
	
	public static enum FlushType{
		buffer,sync,syncHdfs
	}
	public synchronized static String insert(String projectName,Collection<SolrInputDocument> docs,FlushType tp)
	throws Exception {
		return insert(projectName, docs,null, tp);
	}
	
	public synchronized static String insert(String projectName,Collection<SolrInputDocument> docs,ShardsList[] coresfortest,FlushType tp)
			throws Exception {
		
		
		
		TablePartion part = GetPartions.partion(projectName);
		MdrillPartionsInterface drillpart=MdrillPartions.INSTANCE(part.parttype);
		
		ShardsList[] cores = coresfortest;

		if (cores == null) {
			
			cores = GetShards.get(part.name, false);
			
			Map stormconf = Utils.readStormConfig();
			for(int i=0;i<10;i++)
			{
				if(cores.length!=StormUtils.parseInt(stormconf.get("higo.shards.count")))
				{
					if(i>5)
					{
						if(cores.length<=(StormUtils.parseInt(stormconf.get("higo.shards.count"))/2))
						{
							throw new Exception("core.size="+cores.length);
						}else{
							break;
						}
					}
					SolrInfoList infolist=GetShards.getSolrInfoList(part.name);
			    	infolist.run();
					cores = GetShards.get(part.name, false);
					LOG.info("core.size="+cores.length);
					
					Thread.sleep(1000);
				}else{
					break;
				}
			}
		}
		for(SolrInputDocument doc:docs)
		{
			doc.addField("mdrill_uuid", uuid());
			if(!doc.containsKey("mdrillPartion"))
			{
				String partion=drillpart.InsertPartion(doc);
				doc.setField("mdrillPartion", partion);
			}
				
			if(tp!=null)
			{
				if(tp.equals(FlushType.sync))
				{
					doc.setField("mdrillCmd", "sync");
				}else if(tp.equals(FlushType.syncHdfs))
				{
					doc.setField("mdrillCmd", "syncHdfs");
				}else{
					doc.setField("mdrillCmd", "add");
				}
			}
		}
		
		long index = InsertIndex.getAndIncrement();
		if (index > 100000000) {
			index = 0l;
			InsertIndex.set(index);
		}

		JSONObject rtn = new JSONObject();
	
		if(tp.equals(FlushType.buffer))
		{
			ShardsList write = cores[(int) (index % cores.length)];
	
			for (String s : write.list) {
				String url = "http://" + s + "/solr/" + projectName;
				CommonsHttpSolrServer server = new CommonsHttpSolrServer(url);
				server.setConnectionManagerTimeout(60000l);
				server.setSoTimeout(60000);
				server.setConnectionTimeout(10000);
				server.setDefaultMaxConnectionsPerHost(100);
				server.setMaxTotalConnections(100);
				server.setFollowRedirects(false);
				server.setAllowCompression(false);
				server.setMaxRetries(4);
				server.setRequestWriter(new BinaryRequestWriter());
				UpdateRequest req = new UpdateRequest();
				req.add(docs);
				UpdateResponse rsp = req.process(server);
				rtn.put(s, rsp.toString());
			}

		}else if(tp.equals(FlushType.sync)||tp.equals(FlushType.syncHdfs))
		{
			for(ShardsList write:cores)
			{
				for (String s : write.list) {
					String url = "http://" + s + "/solr/" + projectName;
					CommonsHttpSolrServer server = new CommonsHttpSolrServer(url);
					server.setConnectionManagerTimeout(60000l);
					server.setSoTimeout(60000);
					server.setConnectionTimeout(10000);
					server.setDefaultMaxConnectionsPerHost(100);
					server.setMaxTotalConnections(100);
					server.setFollowRedirects(false);
					server.setAllowCompression(false);
					server.setMaxRetries(4);
					server.setRequestWriter(new BinaryRequestWriter());
					UpdateRequest req = new UpdateRequest();
					req.add(docs);
					UpdateResponse rsp = req.process(server);
					rtn.put(s, rsp.toString());
				}
			}
		}
		rtn.put("code","1");
		rtn.put("message","success");
		return rtn.toString();


	}
	public static String insert(String projectName,String list,ShardsList[] coresfortest)
			throws Exception {
		JSONArray jsonStr=new JSONArray(list.trim());
	    Collection<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();

		for(int i=0;i<jsonStr.length();i++)
		{
			SolrInputDocument doc=new SolrInputDocument();
			JSONObject obj=jsonStr.getJSONObject(i);
			Iterator keys = obj.keys();
			while (keys.hasNext()) {
				String key = (String) keys.next();
				doc.addField(key, obj.get(key));
			}
			docs.add(doc);
		}
		
		return insert(projectName, docs, coresfortest,FlushType.buffer);

	}

	public static String result(String projectName, String callback,
			String startStr, String rowsStr, String queryStr, String dist,
			String fl, String groupby, String sort, String order,String leftjoin,JspWriter out)
			throws Exception {
		long t1=System.currentTimeMillis();
		String logParams = logRequest(projectName, callback, startStr, rowsStr,
				queryStr, dist, fl, groupby, sort, order,leftjoin);
		LOG.info("higorequest:" + logParams);
		TablePartion part = GetPartions.partion(projectName);
		HeartBeat hb=new HeartBeat(out);
		new Thread(hb).start();
		try {
			queryStr = WebServiceParams.query(queryStr);
			
			String logParams2 = logRequest(projectName, callback, startStr, rowsStr,
					queryStr, dist, fl, groupby, sort, order,leftjoin);
			LOG.info("query:" + logParams2);

			int start = WebServiceParams.parseStart(startStr);
			int rows = WebServiceParams.parseRows(rowsStr);
			

			ArrayList<String> groupbyFields = WebServiceParams.groupFields(groupby);
			ArrayList<String> showFields = WebServiceParams.showFields(fl);
			HashSet<String> commonStatMap = new HashSet<String>();
			HashSet<String> distStatFieldMap = new HashSet<String>();
			WebServiceParams.setCrossStatMap(showFields,commonStatMap,distStatFieldMap);
	
			Map stormconf = Utils.readStormConfig();
			String mode=String.valueOf(stormconf.get("higo.mode."+part.name));

			LinkedHashMap<String, String> fieldColumntypeMap = readFieldsFromSchemaXml(part.name);

			SortParam sortType = WebServiceParams.sort(sort, order,fieldColumntypeMap,groupbyFields);

			ShardsList[] cores = GetShards.get(part.name, false);
			
			for(int i=0;i<10;i++)
			{
				if(cores.length!=StormUtils.parseInt(stormconf.get("higo.shards.count")))
				{
					if(i>5)
					{
						throw new Exception("core.size="+cores.length);
					}
					SolrInfoList infolist=GetShards.getSolrInfoList(part.name);
			    	infolist.run();
					cores = GetShards.get(part.name, false);
					LOG.info("core.size="+cores.length);
					
					Thread.sleep(1000);
				}else{
					break;
				}
			}
			
			ShardsList[] ms = GetShards.get(part.name, true);

			MdrillPartionsInterface drillpart=MdrillPartions.INSTANCE(part.parttype);
			String[] partionsAll = drillpart.SqlPartions(queryStr);
			queryStr=drillpart.SqlFilter(queryStr);

			Arrays.sort(partionsAll);
			LOG.info("partionsAll:" + Arrays.toString(partionsAll));


			GetPartions.Shards shard = GetPartions.getshard(part, partionsAll,cores, ms);
			HigoJoinParams[] joins=WebServiceParams.parseJoins(leftjoin, shard);
			ArrayList<String> fqList = WebServiceParams.fqList(queryStr, shard,	fieldColumntypeMap);

			String rtn= result(part,callback, fqList, shard, start, rows, sortType,
					groupbyFields, showFields, commonStatMap,
					distStatFieldMap,joins,mode);
			long t2=System.currentTimeMillis();
			long timetaken=t2-t1;
			LOG.info("timetaken:"+(timetaken)+",logParams2:"+logParams2);
			if(timetaken>1000l*100)
			{
				LOG.info("longrequest:"+(timetaken)+",logParams2:"+logParams2+"@"+rtn);
			}
			 hb.setIsstop(true);
			    while(!hb.isstop())
			    {
			    	try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
					}
			    }
			    
			    synchronized (hb.lock) {
			    	if(out!=null)
			    	{
			    		out.write(rtn);
			    	}
				}
			    
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
		finally{
			 hb.setIsstop(true);
		}

	}
	
	
	public static String fieldValueList(String projectName, String callback,
			String field, String startStr, String rowsStr, String queryStr)
			throws Exception {
		return result(projectName, callback, "0", "1000", queryStr, "", null,
				field, null, null,null,null);
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

	static HashMap<String, LinkedHashMap<String, String>> cache = new HashMap<String, LinkedHashMap<String, String>>();

	
	public static String getBasePath()
	{
		Map stormconf = Utils.readStormConfig();
		basePath=(String) stormconf.get("higo.table.path");
		if (basePath == null || basePath.isEmpty()) {
			basePath = System.getenv("higo.table.path");
		}
		if (basePath == null || basePath.isEmpty()) {
			basePath = System.getProperty("higo.table.path",
					"/group/tbdp-etao-adhoc/p4padhoc/tablelist");
		}
		
		if (basePath == null || basePath.isEmpty()) {
			basePath = "/group/tbdp-etao-adhoc/p4padhoc/tablelist";
		}
		
		return basePath;
	}
	public static synchronized LinkedHashMap<String, String> readFieldsFromSchemaXml(
			String tablename) throws Exception {

		if (tablename.equals("rpt_p4padhoc_auction")) {
			tablename = "rpt_hitfake_auctionall_d";
		}

		LinkedHashMap<String, String> datatype = cache.get(tablename);
		if (datatype != null) {
			return datatype;
		}
		datatype = new LinkedHashMap<String, String>();

		Map stormconf = Utils.readStormConfig();
		Configuration conf = getConf(stormconf);
		FileSystem fs = FileSystem.get(conf);
		String regex = "<field\\s+name=\"([^\"]*?)\"\\s+type=\"([^\"]*?)\"\\s+indexed=\"([^\"]*?)\"\\s+stored=\"([^\"]*?)\"\\s*.*/>";
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
			HashSet<String> distStatFieldMap,HigoJoinParams[] joins,String mode) throws Exception {
		CommonsHttpSolrServer server = WebServiceParams.makeServer(shard);
		SolrQuery query = WebServiceParams.makeSolrQuery(shard);
		JSONObject jsonObj = new JSONObject();
		
		if(groupbyFields.size()==0&&joins.length>0)
		{
			if(commonStatMap.size() > 0
					|| distStatFieldMap.size() > 0)
			{
				groupbyFields.add("higoempty_groupby_forjoin_l");
			}
			showFields.add("higoempty_groupby_forjoin_l");
		}
		try {
			if (groupbyFields.size() > 0) {
				int minstart = start;
				int maxEend = rows;
				if (sortType.isStatNum) {
					minstart = start - 100;
					if (minstart < 0) {
						minstart = 0;
					}
					maxEend = Math.min(rows + 100,UniqConfig.defaultCrossMaxLimit());
				}
				WebServiceParams.setGroupByQuery(query, fqList, groupbyFields,
						minstart, maxEend, distStatFieldMap, commonStatMap, sortType,joins,null);
				LOG.info("queryinfo:"+shard.urlMain + "/select/?" + query.toString());
				
				QueryResponse qr = WebServiceParams.fetchGroupCrcQr(query, server);
				
//				jsonObj.put("__timedebug", qr.getTimetaken().toString());
				LinkedHashMap<String,GroupbyRow> groupValueCache=WebServiceParams.setGroupByResult(query,jsonObj, qr, groupbyFields, showFields,joins,null);
				
				if(jsonObj.getLong("total")>(UniqConfig.defaultCrossMaxLimit()-10)&&groupValueCache.size()<=UniqConfig.defaultCrossMaxLimit()&&jsonObj.getString("code").equals("1"))
				{
					try{
					query = WebServiceParams.makeSolrQuery(shard);
					WebServiceParams.setGroupByQuery(query, fqList, groupbyFields,
							0, Math.min(groupValueCache.size()*10,UniqConfig.defaultCrossMaxLimit()), distStatFieldMap, commonStatMap, sortType,joins,groupValueCache);
					LOG.info("queryinfo_pre:"+shard.urlMain + "/select/?" + query.toString());
					QueryResponse qr2 = server.query(query, SolrRequest.METHOD.POST);
//					jsonObj.put("__timedebug_qr2", qr2.getTimetaken().toString());
					WebServiceParams.setGroupByResult(query,jsonObj, qr2, groupbyFields, showFields,joins,groupValueCache);
					}catch(Exception e2)
					{
						LOG.error("queryinfo_pre_exception",e2);

					}
				}
				
				
				if(jsonObj.getString("code").equals("1")&&sortType.isStatNum)
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
					if(sortType.isStatNum&&iscontains&&jsonObj.getLong("total")>(UniqConfig.defaultCrossMaxLimit()-10))
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
//				jsonObj.put("__timedebug", qr.getTimetaken().toString());

				WebServiceParams.setGroupByResult(query,jsonObj, qr, groupbyFields,showFields,joins,null);
			} else {
				WebServiceParams.setDetailByQuery(
						query, fqList, showFields,
						start, rows, sortType,joins,mode.indexOf("@fdt@")>=0);
				LOG.info("queryinfo:"+shard.urlMain + "/select/?" + query.toString());
				
				QueryResponse qr = WebServiceParams.fetchDetailCrcQr(query, server);
//				jsonObj.put("__timedebug", qr.getTimetaken().toString());

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
	public static String notice(String projectName, String callback,String startStr,String rowsStr) throws JSONException {
	
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

			    			if(cnt>0)
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
				//if(e.getValue()>=maxamt)
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


			JSONArray jsonArray=new JSONArray();

			int total=0;
			if(fcsize>0)
			{
				int start = WebServiceParams.parseStart(startStr);
				int rows = WebServiceParams.parseRows(rowsStr);
				SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
				Date d1=fmt.parse(String.valueOf(maxValue));
				Date d2=fmt.parse(String.valueOf(minValue));
				
				total = ((int) (d1.getTime() / 1000) - (int) (d2.getTime() / 1000)) / 3600 / 24+1;  
				
				for(int i=start;i<(start+rows)&&i<total;i++)
				{
					jsonArray.put(fmt.format(new Date(d1.getTime()-i*1000l*3600*24)));
				}
			}
		
			
			JSONObject jsonObj = new JSONObject();
			JSONObject jo = new JSONObject();
			jo.put("min", minValue);
			jo.put("max", maxValue);
			jo.put("total", total);
			jo.put("list", jsonArray);
			jsonObj.put("data", jo);
			jsonObj.put("code", "1");
			jsonObj.put("message", "success");
			jsonObj.put("fcsize", fcsize);
			return callback + "(" + jsonObj.toString() + ")";
		} catch (Exception e) {
			throw new JSONException(e);
		}

	}
	
	
	public static final int daysBetween(Date early, Date late) {
	    
        java.util.Calendar calst = java.util.Calendar.getInstance();  
        java.util.Calendar caled = java.util.Calendar.getInstance();  
        calst.setTime(early);  
         caled.setTime(late);  
         //设置时间为0时  
         calst.set(java.util.Calendar.HOUR_OF_DAY, 0);  
         calst.set(java.util.Calendar.MINUTE, 0);  
         calst.set(java.util.Calendar.SECOND, 0);  
         caled.set(java.util.Calendar.HOUR_OF_DAY, 0);  
         caled.set(java.util.Calendar.MINUTE, 0);  
         caled.set(java.util.Calendar.SECOND, 0);  
        //得到两个日期相差的天数  
         int days = ((int) (caled.getTime().getTime() / 1000) - (int) (calst  
                .getTime().getTime() / 1000)) / 3600 / 24;  
        
        return days;  
   }


}
