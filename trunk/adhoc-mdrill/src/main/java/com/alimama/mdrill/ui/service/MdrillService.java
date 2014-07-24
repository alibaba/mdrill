package com.alimama.mdrill.ui.service;


import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.CRC32;

import javax.servlet.jsp.JspWriter;

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
import com.alimama.mdrill.ui.service.MdrillRequest.StartLimit;
import com.alimama.mdrill.ui.service.utils.WebServiceParams;
import com.alimama.mdrill.ui.service.utils.WebServiceParams.HigoJoinParams;
import com.alimama.mdrill.utils.UniqConfig;
import com.alipay.bluewhale.core.cluster.ShardsState;
import com.alipay.bluewhale.core.cluster.SolrInfo;

import org.apache.log4j.Logger;


public class MdrillService {
	private static Logger LOG = Logger.getLogger(MdrillService.class);

	private static AtomicLong InsertIndex=new AtomicLong(0); 
	
	
	public static enum FlushType{
		buffer,sync,syncHdfs
	}
	public synchronized static String insert(String projectName,Collection<SolrInputDocument> docs,FlushType tp)
	throws Exception {
		return insert(projectName, docs,false,null, tp);
	}
	
	public synchronized static String insertLocal(String projectName,Collection<SolrInputDocument> docs,FlushType tp)
	throws Exception {
		return insert(projectName, docs,true,null, tp);
	}
	
	private static String localip=null;
	public synchronized static String insert(String projectName,Collection<SolrInputDocument> docs,boolean islocal,ShardsList[] coresfortest,FlushType tp)
			throws Exception {
		if(localip==null)
		{
			localip = java.net.InetAddress.getLocalHost().getHostAddress();
		}
		
		TablePartion part = GetPartions.partion(projectName);
		MdrillPartionsInterface drillpart=MdrillPartions.INSTANCE(part.parttype);
		
		ShardsList[] cores = coresfortest;

		if (cores == null) {
			cores = GetShards.getCoresNonCheck(part);
		}

		for(SolrInputDocument doc:docs)
		{
			
			doc.setField("mdrill_uuid", MdrillFunction.uuid());
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
	
		if(tp==null||tp.equals(FlushType.buffer))
		{
			ShardsList write =null;
			if(islocal)
			{
				ArrayList<ShardsList> list=new ArrayList<GetShards.ShardsList>(cores.length);
				for(ShardsList shard:cores)
				{
					if(shard.containsIp(localip))
					{
						list.add(shard);
					}
				}
				if(list.size()>0)
				{
					write =list.get((int) (index %list.size()));
				}
			}
			if(write==null)
			{
				write = cores[(int) (index % cores.length)];
			}
	
			for (String s : write.list) {
				String url = "http://" + s + "/solr/" + projectName;
				try{
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
				}catch(Throwable e)
				{
					LOG.error("insert error "+url,e);
					throw new Exception(e);
				}
			}

		}else if(tp.equals(FlushType.sync)||tp.equals(FlushType.syncHdfs))
		{
			for(ShardsList write:cores)
			{
				for (String s : write.list) {
					String url = "http://" + s + "/solr/" + projectName;
					try{
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
					}catch(Throwable e)
					{
						LOG.error("insert error "+url,e);
						throw new Exception(e);					}
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
		
		return insert(projectName, docs,false, coresfortest,FlushType.buffer);

	}
	
	public static String getBasePath()
	{
		Map stormconf = Utils.readStormConfig();
		return MdrillFieldInfo.getBasePath(stormconf);
	}
	
	public static LinkedHashMap<String, String> readFieldsFromSchemaXml(String tablename) throws Exception
	{
		Map stormconf = Utils.readStormConfig();
		return  MdrillFieldInfo.readFieldsFromSchemaXml(stormconf, tablename);

	}

	public static String result(String projectName, String callback,
			String startStr, String rowsStr, String queryStr, String dist,
			String fl, String groupby, String sort, String order,String leftjoin,JspWriter out)
			throws Exception {
		long t1=System.currentTimeMillis();
		String logParams = MdrillRequestLog.logRequest(projectName, callback, startStr, rowsStr,queryStr, dist, fl, groupby, sort, order,leftjoin);
		HeartBeat hb=new HeartBeat(out);
		new Thread(hb).start();
				
		TablePartion part = GetPartions.partion(projectName);

		try {
			Map stormconf = Utils.readStormConfig();
			
			MdrillTableConfig tblconfig=new MdrillTableConfig(part, stormconf);
			MdrillTableCoreInfo coreinfo=new MdrillTableCoreInfo(part, stormconf);
			MdrillRequest req=new MdrillRequest(tblconfig,part, stormconf, projectName, startStr, rowsStr, queryStr, dist, fl, groupby, sort, order, leftjoin);
			
			GetPartions.Shards shard = GetPartions.getshard(part, req.partionsAll,coreinfo.cores, coreinfo.ms);
			HigoJoinParams[] joins=req.parseJoins(coreinfo,shard);
			ArrayList<String> fqList =req.parseFq(tblconfig, shard);
		
			
			JSONObject jsonObj = new JSONObject();

			long t2=System.currentTimeMillis();
			
			String rtn= request(part,tblconfig,coreinfo,req,callback, fqList, shard,joins,jsonObj);
			long t3=System.currentTimeMillis();
			long timetaken=t3-t1;
			jsonObj.put("___timetaken", timetaken+"="+(t2-t1)+"+"+(t3-t2));
			logParams=MdrillRequestLog.logRequest(projectName, callback, startStr, rowsStr,req.queryStr, dist, fl, groupby, sort, order,leftjoin);
			LOG.info("timetaken:"+(timetaken)+",logParams2:"+MdrillRequestLog.cutString(logParams));
			if(timetaken>1000l*100)
			{
				LOG.info("longrequest:"+(timetaken)+",logParams2:"+MdrillRequestLog.cutString(logParams)+"@"+MdrillRequestLog.cutString(rtn));
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

		} catch (Throwable e) {
			GetShards.purge(part.name);
			long t2=System.currentTimeMillis();
			LOG.error("timetaken:"+(t2-t1)+",logParams:"+MdrillRequestLog.cutString(logParams));
			LOG.error(MdrillRequestLog.cutString(logParams), e);
			throw new Exception(e);
		}
		finally{
			 hb.setIsstop(true);
		}

	}
	
	


	private static String request(TablePartion part,MdrillTableConfig tblconfig,	MdrillTableCoreInfo coreinfo,
	MdrillRequest req,String callback, ArrayList<String> fqList,
			GetPartions.Shards shard,HigoJoinParams[] joins,JSONObject jsonObj) throws Exception {
		CommonsHttpSolrServer server = WebServiceParams.makeServer(shard);
		

		if(req.groupbyFields.size()==0&&joins.length>0)
		{
			if(req.commonStatMap.size() > 0 || req.distStatFieldMap.size() > 0)
			{
				req.groupbyFields.add("higoempty_groupby_forjoin_l");
			}
			req.showFields.add("higoempty_groupby_forjoin_l");
		}
		
		if(req.commonStatMap.size() > 0	|| req.distStatFieldMap.size() > 0)
		{
			if(req.groupbyFields.size()==0)
			{
				req.groupbyFields.add("higoempty_groupby_l");
				req.showFields.add("higoempty_groupby_l");
			}
		}
		
		
		try {
			if (req.groupbyFields.size() > 0) {
				resultGroupBy(fqList, shard, req, joins, jsonObj, server);
			}  else {
				SolrQuery query = WebServiceParams.makeSolrQuery(shard);
				WebServiceParams.setDetailByQuery(query, fqList,req,joins);
				LOG.info("queryinfo:"+shard.urlMain + "/select/?" + MdrillRequestLog.cutString(query.toString()));
				QueryResponse qr = WebServiceParams.fetchDetailCrcQr(query, server,jsonObj,"__timedebug");
				WebServiceParams.setDetailResult(jsonObj, qr, req.showFields,joins);
			}

		} catch (Throwable e) {
			GetShards.purge(part.name);
			LOG.error("higocall,exceptin", e);
			jsonObj.put("code", "0");
			jsonObj.put("message", WebServiceParams.errorToString(e));
		}

		if (callback != null && callback.length() > 0) {
			return callback + "(" + jsonObj.toString() + ")";
		} else {
			return jsonObj.toString();
		}
	}
	

		
	private static void resultGroupBy(ArrayList<String> fqList,
			GetPartions.Shards shard, final MdrillRequest req ,HigoJoinParams[] joins,JSONObject jsonObj,CommonsHttpSolrServer server) throws SolrServerException, JSONException
	{
	
		StartLimit slimit=req.getReqStartEnd();
		SolrQuery query = WebServiceParams.makeSolrQuery(shard);
		WebServiceParams.setGroupByQuery(query, fqList, slimit.start, slimit.rows,req,joins,null);
		query.set("mdrill.isRestat", false);
		LOG.info("queryinfo:"+shard.urlMain + "/select/?" + MdrillRequestLog.cutString(query.toString()));

		QueryResponse qr = WebServiceParams.fetchGroupCrcQr(query, server,jsonObj,"__timedebug");
		
		LinkedHashMap<String,GroupbyRow> groupValueCache=WebServiceParams.setGroupByResult(query,jsonObj, qr, req.groupbyFields, req.showFields,joins,null);
		
		boolean isUseRefetch=jsonObj.getLong("total")>(UniqConfig.defaultCrossMaxLimit()-10)&&groupValueCache.size()<=UniqConfig.defaultCrossMaxLimit()&&jsonObj.getString("code").equals("1");
		if((req.distStatFieldMap.size()>0)||isUseRefetch)
		{
			try{
				query = WebServiceParams.makeSolrQuery(shard);
				int rows= Math.min(groupValueCache.size()*10,UniqConfig.defaultCrossMaxLimit());
				WebServiceParams.setGroupByQuery(query, fqList, 0,rows, req,joins,groupValueCache);
				LOG.info("queryinfo_pre:"+shard.urlMain + "/select/?" + MdrillRequestLog.cutString(query.toString()));
				
				query.set("mdrill.isRestat", true);
				QueryResponse qr2 = server.query(query, SolrRequest.METHOD.POST);
				jsonObj.put("__timedebug_qr2", qr2.getTimetaken(4).toString());
				WebServiceParams.setGroupByResult(query,jsonObj, qr2, req.groupbyFields, req.showFields,joins,groupValueCache);
			}catch(Exception e2)
			{
				LOG.error("queryinfo_pre_exception",e2);
			}
		}
		
		
		if(jsonObj.getString("code").equals("1")&&req.sortType.isStatNum)
		{
			JSONArray jsonArray=jsonObj.getJSONObject("data").getJSONArray("docs");
			ArrayList<JSONObject> results=new ArrayList<JSONObject>();
			boolean iscontains=true;
			for(int i=0;i<jsonArray.length();i++)
			{
				JSONObject obj=jsonArray.getJSONObject(i);
				if(!obj.has(req.sortType.sortRow))
				{
					iscontains=false;
				}
				results.add(obj);
			}
			final boolean isdesc=req.sortType.order.toLowerCase().equals("true");
			boolean needsort=jsonObj.getLong("total")>(UniqConfig.defaultCrossMaxLimit()-10);
			if(iscontains&&needsort)
			{
				Collections.sort(results, new Comparator<JSONObject>() {
					@Override
					public int compare(JSONObject o1, JSONObject o2) {
						int rtn=0;
						try {
							rtn = Double.compare(ParseDouble(o1.get(req.sortType.sortRow)), ParseDouble(o2.get(req.sortType.sortRow)));
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
			int actualstart=req.start-slimit.start;
			int actualend=req.rows+actualstart;
			for(int i=actualstart;i<results.size()&&i<actualend;i++)
			{
				rtnarr.put(results.get(i));
			}
			
			jsonObj.getJSONObject("data").put("docs", rtnarr);
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
	
	
	public static String fieldValueList(String projectName, String callback,
			String field, String startStr, String rowsStr, String queryStr)
			throws Throwable {
		return result(projectName, callback, "0", "1000", queryStr, "", null,
				field, null, null,null,null);
	}


}
