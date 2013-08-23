package com.alimama.mdrill.ui.service.utils;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.impl.BinaryRequestWriter;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.common.params.FacetParams;
import org.apache.solr.request.join.HigoJoinUtils;

import backtype.storm.utils.Utils;

import com.alimama.mdrill.index.utils.TdateFormat;
import com.alimama.mdrill.json.JSONArray;
import com.alimama.mdrill.json.JSONException;
import com.alimama.mdrill.json.JSONObject;
import com.alimama.mdrill.partion.GetPartions;
import com.alimama.mdrill.utils.EncodeUtils;
import com.alimama.mdrill.utils.HadoopBaseUtils;
import com.alimama.mdrill.utils.IndexUtils;
import com.alimama.mdrill.utils.UniqConfig;

public class WebServiceParams {
	private static Logger LOG = Logger.getLogger(WebServiceParams.class);

	public static int parseStart(String startStr)
	{
		int start=0;

		if(startStr!=null  && !startStr.equals("")){
			start = Integer.parseInt(startStr);
			if(start>10000)
			{
				start=10000;
			}
		}
		return start;
	}
	
	public static int parseRows(String rowsStr)
	{
		int rows = 20;

		if(rowsStr!=null && !rowsStr.equals("")){
			rows = Integer.parseInt(rowsStr);
		}
		
		return rows;
	}
	

	public static class HigoJoinParams{
		public String tablename;
		public String hdfsPath;
		public ArrayList<String> fq;
		public ArrayList<String> groupfq=new ArrayList<String>();
		public String[] fl;
		public String leftkey;
		public String rightkey;
		public String returnPrefix;
		public String sort;
		
	}
	
	public static HigoJoinParams[] parseJoins(String leftjoin,GetPartions.Shards shard) throws JSONException
	{
		if(leftjoin==null||leftjoin.trim().isEmpty())
		{
			return new HigoJoinParams[0];
		}
		JSONArray jsonStr=new JSONArray(leftjoin.trim());
		HigoJoinParams[] rtn=new HigoJoinParams[jsonStr.length()];
		for(int i=0;i<jsonStr.length();i++)
		{
			JSONObject obj=jsonStr.getJSONObject(i);
			HigoJoinParams p=new HigoJoinParams();
			p.tablename=obj.getString("tablename");
			p.hdfsPath=obj.getString("path")+"/part-00000";
			p.fq=WebServiceParams.fqList(obj.getString("fq"), shard,null);
			p.fl=obj.getString("fl").split(",");
			p.leftkey=obj.getString("leftkey");
			p.rightkey=obj.getString("rightkey");
			p.returnPrefix=obj.getString("prefix");
			p.sort=obj.has("sort")?obj.getString("sort"):"";
			rtn[i]=p;
		}
		return rtn;
	}
	
	
	//格式兼容
	public static String query(String queryStr) throws JSONException {
		if (queryStr == null || queryStr.trim().equals("")) {
			queryStr = "[]";
		}
		queryStr = queryStr.trim();
		if (queryStr.startsWith("{") && queryStr.endsWith("}")) {
			JSONArray arr = new JSONArray();
			JSONObject jsonStr = new JSONObject(queryStr.trim());
			Iterator keys = jsonStr.keys();
			while (keys.hasNext()) {
				JSONObject item = new JSONObject();
				String key = (String) keys.next();
				item.put(key, jsonStr.getJSONObject(key));
				arr.put(item);
			}
			queryStr = arr.toString();
		}
		
		JSONArray jsonStr=new JSONArray(queryStr.trim());
		JSONArray rtn=new JSONArray();

		for(int i=0;i<jsonStr.length();i++)
		{
			JSONObject obj=jsonStr.getJSONObject(i);
			if(obj.has("key"))
			{
				JSONObject objnew=new JSONObject();
				JSONObject objself=new JSONObject();
				objself.put("operate", obj.get("operate"));
				objself.put("value", obj.get("value"));
				objnew.put(obj.getString("key"), objself);
				rtn.put(objnew);
			}else{
				rtn.put(obj);
			}
		}
    	
    	return rtn.toString();

	}
	
	public static class SortParam{
		public SortParam(String realSort, String realSortPt, String order,String sortRow,boolean isnum,String cmptype) {
			super();
			this.sortField = realSort;
			this.sortType = realSortPt;
			this.order = order;
			this.sortRow=sortRow;
			this.isStatNum=isnum;
			this.cmptype=cmptype;
		}
		public String cmptype="string";
		public String sortField = null;
		public String sortType = null;
		public String order=null;
		
		public String sortRow=null;
		public boolean isStatNum=true;
	}
	
	public static String sortHive(String sort,String order)
	{
		String sortType = null;
		if(sort!=null && !sort.equals("")){
			if(sort.startsWith("count(")){
				sortType = sort;
			}else if(sort.startsWith("sum(")){
				sortType = sort;
			}else if(sort.startsWith("index(")){
				sortType = sort;
			}else if(sort.startsWith("max(")){
				sortType = sort;
			}else if(sort.startsWith("min(")){
				sortType = sort;
			}else if(sort.startsWith("dist(")){
				String sortField = sort.substring(5,sort.length()-1);
				sortType = "count(distinct("+sortField+"))";
			}else{
			    sortType=sort;
			}
		}
		
		if(sort!=null && !sort.equals("")){
			if(order==null || order.equals("")){
				order = "desc";
			}else{
				if(order.equals("true")){
					order = "desc";
				}else if(order.equals("false")){
					order = "asc";
				}else{
					order = "desc";
				}
			}
		}
		
		if(sortType!=null&&order!=null)
		{
//			��ʱ��ʹ������
//			return "sort by "+sortType+" "+order;
		}
		return "";
	}
	
	public static SortParam sort(String sort,String order,HashMap<String, String> fieldColumntypeMap)
	{
		String sortField = null;
		String sortType = null;
		String cmptype="string";
		boolean isStat=false;
		if(sort!=null && !sort.equals("")){
			if(sort.startsWith("count(")){
				sortField = sort.substring(6,sort.length()-1);
				sortType = "count";
				cmptype="tdouble";
				isStat=true;
			}else if(sort.startsWith("sum(")){
				sortField = sort.substring(4,sort.length()-1);
				sortType = "sum";
				cmptype="tdouble";
				isStat=true;
			}else if(sort.startsWith("index(")){
				sortField = sort.substring(6,sort.length()-1);
				sortType = "index";
				cmptype="tdouble";
				isStat=false;
			}
			else if(sort.startsWith("average(")){
				sortField = sort.substring(8,sort.length()-1);
				sortType = "avg";
				cmptype="tdouble";
				isStat=true;
			}
			else if(sort.startsWith("max(")){
				sortField = sort.substring(4,sort.length()-1);
				sortType = "max";
				cmptype="tdouble";
				isStat=true;
			}else if(sort.startsWith("min(")){
				sortField = sort.substring(4,sort.length()-1);
				sortType = "min";
				cmptype="tdouble";
				isStat=true;
			}else if(sort.startsWith("dist(")){
				sortField = sort.substring(5,sort.length()-1);
				sortType = "dist";
				cmptype="tdouble";
				isStat=true;
			}else{
			    sortField=sort;
			    sortType="column";
			    cmptype="string";
			    if(fieldColumntypeMap.containsKey(sortField))
			    {
			    	cmptype=fieldColumntypeMap.get(sortField);
			    }
			    isStat=false;
			}
		}
		

		if(order==null || order.equals("")){
			order = "true";
		}else{
			if(order.equals("desc")){
				order = "true";
			}else if(order.equals("asc")){
				order = "false";
			}else{
				order = "true";
			}
		}
	
		if(sortField==null)
		{
			return new SortParam(null, sortType, order,sort,false,cmptype);

		}
		return new SortParam(sortField.equals("*")?"higoempty_count_l":sortField, sortType, order,sort,isStat,cmptype);
	}
	
	
	public static CommonsHttpSolrServer makeServer(GetPartions.Shards shard) throws MalformedURLException
	{
		CommonsHttpSolrServer server = new CommonsHttpSolrServer(shard.urlMain);
		server.setConnectionManagerTimeout(60000000l);
		server.setSoTimeout(60000000);
	    server.setConnectionTimeout(100000);
	    server.setDefaultMaxConnectionsPerHost(100);
	    server.setMaxTotalConnections(100);
	    server.setFollowRedirects(false);
	    server.setAllowCompression(true);
	    server.setMaxRetries(1);
		server.setRequestWriter(new BinaryRequestWriter());
		return server;
	}
	
	public static SolrQuery makeSolrQuery(GetPartions.Shards shard)
	{
		SolrQuery query = new SolrQuery();
		query.setParam("shards", shard.urlShards);
		query.setParam(FacetParams.MERGER_MAX_SHARDS, String.valueOf(UniqConfig.getMaxMergerShard()));
		query.setParam(FacetParams.MERGER_SERVERS, shard.urlMSs);
		return query;
	}
	
	public static ArrayList<String> groupFields(String groupby)
	{
		ArrayList<String> groupFields = new ArrayList<String>();
		if(groupby!=null){
			String[] values = groupby.split(",");
			for(String s : values){
				if(s!=null && s.length() > 0){
					groupFields.add(s);
				}
			}
		}
		return groupFields;
	}
	
	public static ArrayList<String> showFields(String fl)
	{
		ArrayList<String> showFields = new ArrayList<String>();
		if(fl!=null){
			String[] values = fl.split(",");
			for(String s : values){
				if(s!=null && s.length() > 0){
					showFields.add(s);
				}
			}
		}
		return showFields;
	}
	
	public static ArrayList<String> showHiveFields(String fl)
	{
		ArrayList<String> showFields = new ArrayList<String>();
		if(fl!=null){
			String[] values = fl.split(",");
			for(String s : values){
				if(s!=null && s.length() > 0){
					showFields.add(s.trim().replaceAll("average\\(", "avg\\("));
				}
			}
		}
		return showFields;
	}
	
	
	public static ArrayList<String> distFields(String dist)
	{
		ArrayList<String> distFields = new ArrayList<String>();
		if(dist!=null){
			String[] values = dist.split(",");
			for(String s : values){
				if(s!=null && s.length() > 0){
					distFields.add(s);
				}
			}
		}
		return distFields;
	}
	
	public static String uploadInHdfs(String[] listStrs,String key,GetPartions.Shards shard)
	{
			Date now = new Date();
			String upFile = "grep_"+now.getTime();
			String upFolder = "/group/taobao/external/p4p/p4padhoc/tmp/selectin/safedir/"+upFile;
			String fqStr = "{!inhdfs f="+key+"}"+upFolder;
		
			try{
				
				Map stormconf = Utils.readStormConfig();
				String hdpConf=(String) stormconf.get("hadoop.conf.dir");
				Configuration conf=new Configuration();
				HadoopBaseUtils.grabConfiguration(hdpConf, conf);	
				  FileSystem fs = FileSystem.get(conf);
				  IndexUtils.truncate(fs, new Path(upFolder));
				 FSDataOutputStream output= fs.create( new Path(upFolder),true);		
				for(String s : listStrs){
					if(s!=null && !s.equals(""))
						output.write(new String(s+"\n").getBytes());
				}								
				output.close();
			}catch(Exception e){
				e.printStackTrace();
			}
			
			return fqStr;
	}
	
	
	
	public static String uploadInHdfsForHive(String[] listStrs,String key,GetPartions.Shards shard)
	{
			Date now = new Date();
			String upFile = "grep_"+now.getTime();
			String upFolder = "/group/taobao/external/p4p/p4padhoc/tmp/selectin/safedir/"+upFile;
			String fqStr = "inhdfs_udf("+key+",'"+upFolder+"')<>'-'";
			
			try{
				Map stormconf = Utils.readStormConfig();
				String hdpConf=(String) stormconf.get("hadoop.conf.dir");
				Configuration conf=new Configuration();
				HadoopBaseUtils.grabConfiguration(hdpConf, conf);	
				  FileSystem fs = FileSystem.get(conf);
				  IndexUtils.truncate(fs, new Path(upFolder));
				 FSDataOutputStream output= fs.create( new Path(upFolder),true);		
				for(String s : listStrs){
					if(s!=null && !s.equals(""))
						output.write(new String(s+"\n").getBytes());
				}								
				output.close();
				
			}catch(Exception e){
				e.printStackTrace();
			}
			
			return fqStr;
		
	}
	
//	if(ft.equals("tlong")||ft.equals("tdouble")||ft.equals("tint")||ft.equals("tfloat"))

	private static String[] filterListStr(String ft,String[] listStrs)
	{
		ArrayList<String> fq=new ArrayList<String>();
		if(ft.equals("tlong"))
		{
    		for(String s:listStrs)
    		{
    			try{
    				Long.parseLong(s);
    				fq.add(s);
    			}catch(NumberFormatException e){}
    		}
    		String[] rtn=new String[fq.size()];
    		return fq.toArray(rtn);
		}
		
		if(ft.equals("tdouble"))
		{
    		for(String s:listStrs)
    		{
    			try{
    				Double.parseDouble(s);
    				fq.add(s);
    			}catch(NumberFormatException e){}
    		}
    		String[] rtn=new String[fq.size()];
    		return fq.toArray(rtn);
		}
		
		if(ft.equals("tint"))
		{
    		for(String s:listStrs)
    		{
    			try{
    				Integer.parseInt(s);
    				fq.add(s);
    			}catch(NumberFormatException e){}
    		}
    		String[] rtn=new String[fq.size()];
    		return fq.toArray(rtn);
		}
		
		if(ft.equals("tfloat"))
		{
    		for(String s:listStrs)
    		{
    			try{
    				Float.parseFloat(s);
    				fq.add(s);
    			}catch(NumberFormatException e){}
    		}
    		String[] rtn=new String[fq.size()];
    		return fq.toArray(rtn);
		}
		
		
		return listStrs;
	}

	
	
public static OperateType parseOperateType(int operate)
 {
		switch (operate) {
			case 1: {
				return OperateType.eq;
			}
			case 32: {
				return OperateType.like;
			}
			case 33: {
				return OperateType.notlike;
			}
			case 2: {
				return OperateType.neq;
			}
			case 3: {
				return OperateType.gt;
			}
			case 4: {
				return OperateType.lg;
			}
			case 13: {
				return OperateType.gteq;
			}
			case 14: {
				return OperateType.lgeq;
			}
			case 30: {
				return OperateType.gteq;
			}
			case 31: {
				return OperateType.lgeq;
			}
			case 5:
			case 6:
			case 7:
			case 8: {
				boolean isNotIn = (operate == 7 || operate == 8);
				if (isNotIn) {
					return OperateType.notin;
				}
				return OperateType.in;
			}
			case 9: {
				return OperateType.range;
			}
			case 1000: {
				return OperateType.other;
			}
		}
		return OperateType.other;
	}
	private static String parseFqOperate(int operate,String key,String value2,GetPartions.Shards shard,HashMap<String, String> fieldColumntypeMap)
	{
		String ft="string";
		if(fieldColumntypeMap!=null&&fieldColumntypeMap.containsKey(key))
		{
			ft=fieldColumntypeMap.get(key);
		}
		
		boolean isdate=false;
		if(ft.equals("tdate")||ft.equals("date"))
		{
			isdate=true;
		}
		switch(operate){
		case 1:
		{
			if(isdate)
			{
				return key+":"+TdateFormat.transformSolrMetacharactor(TdateFormat.ensureTdateForSearch(value2));
			}
			return key+":"+TdateFormat.transformSolrMetacharactor(value2);
		}
		case 32:
		{
			if(isdate)
			{
				return key+":*"+TdateFormat.transformSolrMetacharactor(TdateFormat.ensureTdateForSearch(value2))+"*";
			}
			return key+":*"+TdateFormat.transformSolrMetacharactor(value2)+"*";
		}
		case 33:
		{
			if(isdate)
			{
				return "-"+key+":*"+TdateFormat.transformSolrMetacharactor(TdateFormat.ensureTdateForSearch(value2))+"*";
			}
			return "-"+key+":*"+TdateFormat.transformSolrMetacharactor(value2)+"*";
		}
		case 2:
		{
			if(isdate)
			{
				return "-"+key+":"+TdateFormat.transformSolrMetacharactor(TdateFormat.ensureTdateForSearch(value2));
			}
			return "-"+key+":"+TdateFormat.transformSolrMetacharactor(value2);
		}
		case 3:
		{
			if(isdate)
			{
				return key+":["+TdateFormat.ensureTdateForSearch(value2)+" TO 2098-09-09T00:00:00Z]";
			}
			return key+":["+value2+" TO *]";
		}
		case 4:
		{
			if(isdate)
			{
				return key+":[* TO "+TdateFormat.ensureTdateForSearch(value2)+"]";
			}
			return key+":[* TO "+value2+"]";
		}
		case 13:
		{
			if(isdate)
			{
				return key+":{"+TdateFormat.ensureTdateForSearch(value2)+" TO 2098-09-09T00:00:00Z}";
			}
			return key+":{"+value2+" TO *}";
		}
		case 14:
		{
			if(isdate)
			{
				return key+":{* TO "+TdateFormat.ensureTdateForSearch(value2)+"}";
			}
			return key+":{* TO "+value2+"}";
		}
		case 30:
		{
			if(isdate)
			{
				return key+":{"+TdateFormat.ensureTdateForSearch(value2)+" TO 2098-09-09T00:00:00Z}";
			}
			return key+":{"+value2+" TO *}";
		}
		case 31:
		{
			if(isdate)
			{
				return key+":{* TO "+TdateFormat.ensureTdateForSearch(value2)+"}";
			}
			return key+":{* TO "+value2+"}";
		}
		case 5:
		case 6:
		case 7:
		case 8:
		{
			boolean isNotIn=(operate==7 || operate==8);
			String[] listStrs = filterListStr(ft,value2.split(","));
			if(listStrs.length > 20&&!isNotIn){
				return uploadInHdfs(listStrs, key, shard);
			}else{
				StringBuffer sb = new StringBuffer();
				if(isNotIn){
					sb.append("-");
				}
                sb.append(key);
                sb.append(":(");
                for(String v : listStrs){
                    sb.append(TdateFormat.transformSolrMetacharactorNoLike(v));
                    sb.append(" ");
                }
                sb.append(")");
                return sb.toString();
			}
		}
		case 9:
		{
			String[] listStrs = value2.split(",");
			if(listStrs.length==2 && listStrs[0] != null && listStrs[1] != null){
				if(listStrs[0].equals(listStrs[1])){
					if(isdate)
					{
						return key+":"+TdateFormat.transformSolrMetacharactor(TdateFormat.ensureTdateForSearch(listStrs[0]));
					}
					return key+":"+TdateFormat.transformSolrMetacharactor(listStrs[0]);
				}else{
					if(isdate)
					{
						return key+":["+TdateFormat.ensureTdateForSearch(listStrs[0])+" TO "+ TdateFormat.ensureTdateForSearch(listStrs[1]) +"]";
					}
					return key+":["+listStrs[0]+" TO "+ listStrs[1] +"]";
				}
			}
			break;
		}
		case 1000:
		{
			return value2;
		}
	}
		return null;
	}
	
	public static String parseFqAlias(String key,HashMap<String,String> colMap,HashMap<String,String> colMap2,String tblname)
	{
		String rtn=key;
		if(tblname!=null&&colMap2!=null&&colMap2.containsKey(key))
		{
			rtn=tblname+"."+colMap2.get(key);
		}else if(tblname!=null&&colMap!=null&&colMap.containsKey(key))
		{
			rtn=tblname+"."+colMap.get(key);
		}
		return rtn;
	}

	
	
	
	private static String parseFqOperateHive(String part,int operate,String key,String value2,GetPartions.Shards shard,boolean isPartionByPt,HashMap<String, String> filetypeMap,HashMap<String,String> colMap,HashMap<String,String> colMap2,String tblname)
	{
		
		
		String ft="string";
		if(filetypeMap!=null&&filetypeMap.containsKey(key))
		{
			ft=filetypeMap.get(key);
		}
		
		key=parseFqAlias(key, colMap, colMap2, tblname);
		
		
		
		boolean isdate=false;
		if(ft.equals("tdate")||ft.equals("date"))
		{
			isdate=true;
		}
		
		
		boolean isNumber=false;
		if(ft.equals("tlong")||ft.equals("tdouble")||ft.equals("tint")||ft.equals("tfloat"))
		{
			isNumber=true;
		}
		if (key.equals("thedate")) {
			String add = "";
			if (isPartionByPt) {
				part = "pt";
				add = "000000";
			}

			switch (operate) {
			case 1: {
				return "("+part + "='" + value2 + add + "' )";//and "+key+"='"+value2+"'
			}
			case 2: {
				return "("+part + "<>'" + value2 + add + "' )";//and "+key+"<>'"+value2+"'"+"
			}
			case 3: {
				return "("+part + ">=" + value2 + add+" )";//and "+key+">="+value2+"
			}
			case 4: {
				return "("+part + "<=" + value2 + add+" )";//and "+key+"<="+value2+"
			}
			case 13: {
				return "("+part + ">" + value2 + add+" )";//and "+key+">"+value2+"
			}
			case 14: {
				return "("+part + "<" + value2 + add+" )";//and "+key+"<"+value2+"
			}
			case 30: {
				return "("+part + ">" + value2 + add+" )";//and "+key+">"+value2+"
			}
			case 31: {
				return "("+part + "<" + value2 + add+" )";//and "+key+"<"+value2+"
			}
			case 5:
			case 6:
			case 7:
			case 8:
			{
				boolean isNotIn=(operate==7 || operate==8);
				String[] listStrs = value2.split(",");
				StringBuffer sb = new StringBuffer();
				sb.append("(");
				String joinchar="";
	            for(String v : listStrs){
	            	sb.append(joinchar);
	                if(isNotIn){
	                	sb.append(part+"<>"+"'"+v + add+"'");
					}else{
	                	sb.append(part+"="+"'"+v + add+"'");
					}
	                joinchar=" or ";
	            }
	            sb.append(")");
	            return sb.toString();
			}
			case 9: {
				String[] listStrs = value2.split(",");
				if (listStrs.length == 2 && listStrs[0] != null
						&& listStrs[1] != null) {
					if (listStrs[0].equals(listStrs[1])) {
						return "("+part + "=" + listStrs[0]+ add+" )";//and "+key+"="+listStrs[0]+"
					} else {
						return "("+
									"(" + part + ">=" + listStrs[0] + add + " and "	+ part + "<=" + listStrs[1] + add + " )"//
//								+" and "+
//									"("+key+">="+listStrs[0]+" )"//and "+ key+"<="+listStrs[1] +" 
								+")";
					}
				}
			}
			}
		}
		
		String usequot="";
		
		if(isdate)
		{
			usequot="'";
		}
		
		switch(operate){
		case 1:
		{
			if(value2.indexOf("*")>=0)
			{
				return "transhigo_udf("+key+",'"+ft+"')"+" like '"+value2.replaceAll("\\*", "%")+"'";
			}
			if(isdate)
			{
				return "transhigo_udf("+key+",'"+ft+"')"+"='"+TdateFormat.ensureTdateForSearch(value2)+"'";
			}
			if(isNumber)
			{
				return ""+key+""+"="+value2+"";
			}
			return "transhigo_udf("+key+",'"+ft+"')"+"='"+value2+"'";
		}
		case 32:
		{
			return "transhigo_udf("+key+",'"+ft+"')"+" like '%"+value2.replaceAll("\\*", "%")+"%'";
		}
		case 33:
		{
			return "transhigo_udf("+key+",'"+ft+"')"+" not like '%"+value2.replaceAll("\\*", "%")+"%'";
		}
		case 2:
		{
			if(isdate)
			{
				return "transhigo_udf("+key+",'"+ft+"')"+"<>'"+TdateFormat.ensureTdateForSearch(value2)+"'";
			}
			
			if(isNumber)
			{
				return ""+key+""+"<>"+value2+"";
			}
			return "transhigo_udf("+key+",'"+ft+"')"+"<>'"+value2+"'";
		}
		case 3:
		{
			if(isdate)
			{
				return "transhigo_udf("+key+",'"+ft+"')"+">='"+TdateFormat.ensureTdateForSearch(value2)+"' and transhigo_udf("+key+",'"+ft+"')"+"<'2098-09-09T00:00:00Z'";
			}
			return "transhigo_udf("+key+",'"+ft+"')"+">="+value2;
		}
		case 4:
		{
			if(isdate)
			{
				return "transhigo_udf("+key+",'"+ft+"')"+"<='"+TdateFormat.ensureTdateForSearch(value2)+"' and transhigo_udf("+key+",'"+ft+"')"+"<'2098-09-09T00:00:00Z'";
			}
			return "transhigo_udf("+key+",'"+ft+"')"+"<="+value2;
		}
		case 13:
		{
			if(isdate)
			{
				return "transhigo_udf("+key+",'"+ft+"')"+">'"+TdateFormat.ensureTdateForSearch(value2)+"' and transhigo_udf("+key+",'"+ft+"')"+"<'2098-09-09T00:00:00Z'";
			}
			return "transhigo_udf("+key+",'"+ft+"')"+">"+value2;
		}
		case 14:
		{
			if(isdate)
			{
				return "transhigo_udf("+key+",'"+ft+"')"+"<'"+TdateFormat.ensureTdateForSearch(value2)+"' and transhigo_udf("+key+",'"+ft+"')"+"<'2098-09-09T00:00:00Z'";
			}
			return "transhigo_udf("+key+",'"+ft+"')"+"<"+value2;
		}
		
		case 30:
		{
			if(isdate)
			{
				return "transhigo_udf("+key+",'"+ft+"')"+">'"+TdateFormat.ensureTdateForSearch(value2)+"' and transhigo_udf("+key+",'"+ft+"')"+"<'2098-09-09T00:00:00Z'";
			}
			return "transhigo_udf("+key+",'"+ft+"')"+">"+value2;
		}
		case 31:
		{
			if(isdate)
			{
				return "transhigo_udf("+key+",'"+ft+"')"+"<'"+TdateFormat.ensureTdateForSearch(value2)+"' and transhigo_udf("+key+",'"+ft+"')"+"<'2098-09-09T00:00:00Z'";
			}
			return "transhigo_udf("+key+",'"+ft+"')"+"<"+value2;
		}
		case 5:
		case 6:
		case 7:
		case 8:
		{
			boolean isNotIn=(operate==7 || operate==8);
			String[] listStrs = value2.split(",");
			if(listStrs.length > 10&&!isNotIn){
				return uploadInHdfsForHive(listStrs, key, shard);
			}else{
			StringBuffer sb = new StringBuffer();
			sb.append("(");
			String joinchar="";
            for(String v : listStrs){
            	sb.append(joinchar);
                if(isNotIn){
                	sb.append("transhigo_udf("+key+",'"+ft+"')"+"<>"+"'"+v+"'");
				}else{
					sb.append("transhigo_udf("+key+",'"+ft+"')"+"="+"'"+v+"'");
				}
                joinchar=" or ";
            }
            sb.append(")");
            return sb.toString();
			}
		}
		case 9:
		{
			String[] listStrs = value2.split(",");
			if(listStrs.length==2 && listStrs[0] != null && listStrs[1] != null){
				if(listStrs[0].equals(listStrs[1])){
					if(isdate)
					{
						return "transhigo_udf("+key+",'"+ft+"')"+"="+usequot+""+TdateFormat.ensureTdateForSearch(listStrs[0])+usequot;
					}
					
					if(isNumber)
					{
						return ""+key+""+"="+listStrs[0]+"";
					}
					return "transhigo_udf("+key+",'"+ft+"')"+"="+usequot+""+listStrs[0]+usequot;
				}else{
					if(isdate)
					{
						return "("+"transhigo_udf("+key+",'"+ft+"')"+">="+usequot+TdateFormat.ensureTdateForSearch(listStrs[0])+usequot+" and "+ "transhigo_udf("+key+",'"+ft+"')"+"<="+usequot+TdateFormat.ensureTdateForSearch(listStrs[1]) +usequot+" )";
					}
					return "("+"transhigo_udf("+key+",'"+ft+"')"+">="+usequot+listStrs[0]+usequot+" and "+ "transhigo_udf("+key+",'"+ft+"')"+"<="+usequot+listStrs[1]+usequot+" )";
				}
			}
			break;
		}
	}
		return null;
	}
	
	public static ArrayList<String> fqList(String queryStr,GetPartions.Shards shard,HashMap<String, String> fieldColumntypeMap) throws JSONException
	{
		queryStr=query(queryStr);
		ArrayList<String> fqList = new ArrayList<String>();
		JSONArray jsonStr=new JSONArray(queryStr.trim());
		for(int j=0;j<jsonStr.length();j++)
		{
			JSONObject obj=jsonStr.getJSONObject(j);
			
			if(obj.has("subQuery"))
			{
				String filterType="AND";
				if(obj.has("filter"))
				{
					filterType=obj.getString("filter").toUpperCase();
				}
				ArrayList<String> sublist=fqList(obj.getJSONArray("list").toString(), shard, fieldColumntypeMap);
				if(sublist.size()==1)
				{
					fqList.add(sublist.get(0));
				}
				StringBuffer buff=new StringBuffer();
				buff.append("(");
				String joinchar="";
				for(String fq:sublist)
				{
					buff.append(joinchar);
					buff.append(fq);
					joinchar=" "+filterType+" ";
				}
				
				buff.append(")");
				fqList.add(buff.toString());

			}else{
			
        			for  (Iterator iter = obj.keys(); iter.hasNext();) { 
        		    	String field = (String)iter.next();
        		    	JSONObject jsonStr2= obj.getJSONObject(field);
        		    	int operate = Integer.parseInt(jsonStr2.getString("operate"));
        		    	String valueList=parseFqValue(jsonStr2.getString("value"),operate);
        				String fq = parseFqOperate(operate, field, valueList, shard, fieldColumntypeMap);
        				if (fq != null) {
        					fqList.add(fq);
        				}
        			}
			
			}
		
		}
				
		return fqList;
	}
	
	public static String parseFqValue(String valueList,int operate) throws JSONException
	{
		if (operate < 1000) {
			// for adhoc
			if (valueList.startsWith("[") && valueList.endsWith("]")) {
				JSONArray arr=new JSONArray(valueList);
		        int len = arr.length();
		        StringBuffer sb = new StringBuffer();
		        for (int i = 0; i < len; i += 1) {
		            if (i > 0) {
		                sb.append(",");
		            }
		            sb.append(arr.get(i));
		        }
		        valueList= sb.toString();
			}
			
			if(operate < 9&&operate>4)
			{
				valueList = valueList.replaceAll(" ", ",").replaceAll("\t", ",").replaceAll("\r", ",").replaceAll("\n", ",");
			}
		}
		
		return valueList;
	}
	
	
	public static ArrayList<String> fqListHive(String part,String queryStr,GetPartions.Shards shard,boolean isPartionByPt,HashMap<String, String> filetypeMap,HashMap<String,String> colMap,HashMap<String,String> colMap2,String tblname) throws JSONException
	{
		ArrayList<String> fqList = new ArrayList<String>();
		if(queryStr==null||queryStr.isEmpty()||queryStr.equals("*:*")){
			return fqList;
		}
		
		queryStr=WebServiceParams.query(queryStr);
		
		JSONArray jsonStr=new JSONArray(queryStr.trim());
		for(int j=0;j<jsonStr.length();j++)
		{
			JSONObject obj=jsonStr.getJSONObject(j);
			if(obj.has("subQuery"))
			{
				String filterType="AND";
				if(obj.has("filter"))
				{
					filterType=obj.getString("filter");
				}
				ArrayList<String> sublist=fqListHive(part,obj.getJSONArray("list").toString(), shard,isPartionByPt, filetypeMap,colMap,colMap2,tblname);
				if(sublist.size()==1)
				{
					fqList.add(sublist.get(0));
				}
				StringBuffer buff=new StringBuffer();
				buff.append("(");
				String joinchar="";
				for(String fq:sublist)
				{
					buff.append(joinchar);
					buff.append(fq);
					joinchar=" "+filterType+" ";
				}
				
				buff.append(")");
				
				fqList.add(buff.toString());
			}else{
			
    			for  (Iterator iter = obj.keys(); iter.hasNext();) { 
    		    	String key = (String)iter.next();
    		    	JSONObject jsonStr2= obj.getJSONObject(key);
    		    	
    		    	int operate = Integer.parseInt(jsonStr2.getString("operate"));
    		    	String valueList=jsonStr2.getString("value");
    
        			if (operate < 1000) {
        				// for adhoc
        				if (valueList.startsWith("[") && valueList.endsWith("]")) {
        					JSONArray arr=new JSONArray(valueList);
    				        int len = arr.length();
    				        StringBuffer sb = new StringBuffer();
    				        for (int i = 0; i < len; i += 1) {
    				            if (i > 0) {
    				                sb.append(",");
    				            }
    				            sb.append(arr.get(i));
    				        }
    				        valueList= sb.toString();
        				}
        				if(operate < 9&&operate>4)
        				{
        					valueList = valueList.replaceAll(" ", ",").replaceAll("\r", ",").replaceAll("\t", ",").replaceAll("\n", ",");
        				}
        				valueList = valueList.replaceAll("\'", "\\\'");
        			}
        			
        			String fq=parseFqOperateHive(part,operate, key, valueList,shard,isPartionByPt,filetypeMap,colMap,colMap2,tblname);
        			if(fq!=null)
        			{
        				fqList.add(fq);
        			}
        		
    			} 
			
			}
		}
	
		
		return fqList;
	}
	
	
	public static String errorToString(Throwable e)
	{
		String errorStr = null;
		try {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			errorStr = sw.toString();
		} catch (Exception e2) {
			errorStr = "bad getErrorInfoFromException";
		}
		return errorStr;
	}
	
	private static Pattern solrTimeTrans=Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}Z", Pattern.CASE_INSENSITIVE);
	public static String transDate(String dt)
	{
		if(!solrTimeTrans.matcher(dt).matches())
		{
			return dt;
		}
		
		
		return dt.replaceAll("2099-09-09T00:00:00Z", "-").replaceAll("T00:00:00Z", "").replaceAll("T", " ").replaceAll("Z", "");
	}
	
	
	public static HashMap<String, String[]> fillStatFields(Count row )
	{
		HashMap<String, String[]> statFieldAll =new HashMap<String, String[]>();
		ArrayList<String> ext = row.getExtList();
	   	if(ext!=null){
	   		for(String extStr : ext){
	   			String[] extValues = extStr.split(",");
	   			if(extValues.length>=4){
	   				String field=extValues[0];
	   				String[] mapValue = statFieldAll.get(extValues[0]);
	   				if(mapValue==null){
	   				 mapValue=new String[6];
	   				statFieldAll.put(field, mapValue);
	   				}
	   				          
	   				boolean isCountNull=false;
	   				if(field.indexOf("higoempty_")>=0)
	   				{
	   					isCountNull=true;
	   				}
	   				mapValue[0] = String.valueOf(isCountNull?row.getCount():0);
	   				mapValue[1] = extValues[1];
	   				mapValue[2] = extValues[2];
	   				mapValue[3] = extValues[3];
	   				double tempValue = Double.parseDouble(extValues[1]);
	   				if(row.getCount()!=0)
	   				{
	   					mapValue[4] = ""+(tempValue/row.getCount());
	   				}
	   				else
	   				{
	   					mapValue[4] = "0";
	   				}
	   				if(extValues.length>=5)
	   				{
	   				    mapValue[5] = extValues[4];//dist
	   				}else{
	   				    mapValue[5] = "0";//dist
	   				}
	   				
	   				if(extValues.length>=6)
	   				{
		   				double cnt = Double.parseDouble(extValues[5]);
		   				if(cnt>0)
		   				{
		   					mapValue[4] = ""+(tempValue/cnt);
		   				}
		   				if(!isCountNull)
		   				{
		   					mapValue[0] = String.valueOf((long)cnt);
		   				}
		   				
	   				}
	   			}
	   		}
	   	}
	   	
	   	return statFieldAll;
	}
	
	public static LinkedHashMap<String,Count> setGroupByResult(JSONObject jsonObj,QueryResponse qr,
		ArrayList<String> groupFields,
		ArrayList<String> showFields,
		HigoJoinParams[] joins,
		LinkedHashMap<String,Count> groupValueCacheLast
	) throws JSONException
	{
		LinkedHashMap<String,Count> groupValueCache=new LinkedHashMap<String,Count>();

			FacetField ff = qr.getFacetField("solrCorssFields_s");
			long totalRecord = ff.getTotal();
				
			if(groupValueCacheLast==null)
			{
				jsonObj.put("code", "1"); 
				jsonObj.put("message", "success"); 
				jsonObj.put("total", totalRecord);
			}
			
			JSONObject jsonObj2 = new JSONObject(); 
			JSONArray jsonArray = new JSONArray();
			JSONArray jsonArray_debug = new JSONArray();

			List<Count> facetCounts = ff.getValues();
			

			int fcsize = 0;
			if(facetCounts != null){
				fcsize = facetCounts.size();
			}
			if(groupValueCacheLast==null)
			{
				for(int i=0;i<fcsize;i++){
				   	Count row = facetCounts.get(i);
					String groupValues = row.getName();
				   	groupValueCache.put(groupValues, row);
				   	JSONObject jo = new JSONObject();
				   	setGroup(jo, groupFields, joins, groupValues);
					setStat(jo, showFields, row);
					jsonArray.add(i, jo);
				}
			}else{
				for(int i=0;i<fcsize;i++){
				   	Count row = facetCounts.get(i);
					String groupValues = row.getName();
				   	groupValueCache.put(groupValues, row);
				}
				
				int index=0;
				for(Entry<String,Count> e:groupValueCacheLast.entrySet())
				{
			   		JSONObject jo = new JSONObject();
			   		JSONObject jo_debug = new JSONObject();

			   		jo_debug.put("__higo_gruss__", "false");
					String groupValues = e.getKey();
					Count row = groupValueCache.get(groupValues);
					if(row==null)
					{
						row=e.getValue();
						jo_debug.put("__higo_gruss__", "true");
					}
					//set group by result
				   	setGroup(jo, groupFields, joins, groupValues);
				   	//set stat result
					setStat(jo, showFields, row);
					
					//set group by result
				   	setGroup(jo_debug, groupFields, joins, groupValues);
				   	//set stat result
					setStat(jo_debug, showFields, row);
					jsonArray.add(index, jo);
					jsonArray_debug.add(index, jo_debug);
					index++;
				}
			}
			
			jsonObj2.put("docs", jsonArray);
			
			jsonObj2.put("docs_debug", jsonArray_debug);
			
			jsonObj.put("data", jsonObj2);
			return groupValueCache;
	}
	
	private static void setGroup(JSONObject jo,ArrayList<String> groupFields,HigoJoinParams[] joins,String groupValues) throws JSONException
	{
		String[] values = EncodeUtils.decode(groupValues.split(UniqConfig.GroupJoinString()));
	   	
	   	for(int j =0;j<values.length&&j<groupFields.size();j++){
	   		if(groupFields.get(j).equals("higoempty_groupby_forjoin_l"))
	   		{
	   			continue;
	   		}
	   		if(values[j]==null || values[j].equals(""))
	   		{
		   	   	jo.put(groupFields.get(j), " ");
	   		}
		   	else
		   	{
		   		jo.put(groupFields.get(j), transDate(values[j]));
		   	}
	   	}
	   	
	    int joinoffset=groupFields.size();
		for(HigoJoinParams inv:joins)
		{
			for(int j=0;j<inv.fl.length;j++)
			{
				int pos=j+joinoffset;
				if(pos<values.length)
				{
					jo.put(inv.returnPrefix+"@"+inv.fl[j], transDate(values[pos]));
				}else{
					jo.put(inv.returnPrefix+"@"+inv.fl[j], " ");
				}
			}
			joinoffset+=inv.fl.length;
		}
	}
	
	
	public static class StateField{
		public boolean isstat;
		public String realField;
		public String type;
	}
	public static StateField parseStat(String showfield)
	{
		StateField rtn=new StateField();
		rtn.realField=showfield;
		rtn.isstat=false;
		if(showfield.startsWith("count(")){
			rtn.realField = showfield.substring(6,showfield.length()-1);
			rtn.isstat=true;
			rtn.type="count";
		}
		if(showfield.startsWith("sum(")){
			rtn.realField = showfield.substring(4,showfield.length()-1);
			rtn.isstat=true;
			rtn.type="sum";
		}
		if(showfield.startsWith("max(")){
			rtn.realField = showfield.substring(4,showfield.length()-1);
			rtn.isstat=true;
			rtn.type="max";
		}
		if(showfield.startsWith("min(")){
			rtn.realField = showfield.substring(4,showfield.length()-1);
			rtn.isstat=true;
			rtn.type="min";
		}
		if(showfield.startsWith("average(")){
			rtn.realField = showfield.substring(8,showfield.length()-1);
			rtn.isstat=true;
			rtn.type="avg";
		}
		if(showfield.startsWith("avg(")){
			rtn.realField = showfield.substring(4,showfield.length()-1);
			rtn.isstat=true;
			rtn.type="avg";
		}
		if(showfield.startsWith("dist(")){
			rtn.realField = showfield.substring(5,showfield.length()-1);
			rtn.isstat=true;
			rtn.type="dist";
		}
		
		return rtn;
	}
	
	public static void setStat(JSONObject jo,ArrayList<String> showFields,	Count row) throws JSONException
	{
		HashMap<String, String[]> statFieldAll =fillStatFields(row);
		for(String showfield : showFields){
			if(showfield.equals("higoempty_groupby_forjoin_l"))
			{
				continue;
			}
			if(jo.has(showfield))
			{
				continue;
			}
			String realField=null;
			Integer index=-1;
			if(showfield.startsWith("count(")){
				realField = showfield.substring(6,showfield.length()-1);
				index=0;
			}
			if(showfield.startsWith("sum(")){
				realField = showfield.substring(4,showfield.length()-1);
				index=1;
			}
			if(showfield.startsWith("max(")){
				realField = showfield.substring(4,showfield.length()-1);
				index=2;
			}
			if(showfield.startsWith("min(")){
				realField = showfield.substring(4,showfield.length()-1);
				index=3;
			}
			if(showfield.startsWith("average(")){
				realField = showfield.substring(8,showfield.length()-1);
				index=4;
			}
			if(showfield.startsWith("dist(")){
				realField = showfield.substring(5,showfield.length()-1);
				index=5;
			}
			String[] mapValue = statFieldAll.get(realField.equals("*")?"higoempty_count_l":realField);
			if(mapValue!=null&&mapValue.length>index&&index>=0)
			{
				jo.put(showfield, mapValue[index]);
			}else{
				jo.put(showfield, " ");
			}
	   	}
	}
	
	
	
	public static void setDetailResult(JSONObject jsonObj,QueryResponse qr,
			ArrayList<String> showFields,
			HigoJoinParams[] joins		
	) throws JSONException
		{
				FacetField ff = qr.getFacetField("solrCorssFields_s");
				long totalRecord = ff.getTotal();
					
				jsonObj.put("code", "1"); 
				jsonObj.put("message", "success"); 
				
				JSONObject jsonObj2 = new JSONObject(); 
				JSONArray jsonArray = new JSONArray();

				List<Count> facetCounts = ff.getValues();
				
				int fcsize = 0;
				if(facetCounts != null){
					fcsize = facetCounts.size();
				}
				jsonObj.put("total", totalRecord);

				for(int i=0;i<fcsize;i++){
				   	Count row = facetCounts.get(i);
				   	
				   	JSONObject jo = new JSONObject();
				   	String groupValues = row.getName();
				   	String[] values =  EncodeUtils.decode(groupValues.split(UniqConfig.GroupJoinString()));
				   	int valuesoffset=2;
				   	for(int j =0;j<(values.length-valuesoffset)&&j<showFields.size();j++){
				   		if(showFields.get(j).equals("higoempty_groupby_forjoin_l"))
				   		{
				   			continue;
				   		}
				   		if(values[j+valuesoffset]==null || values[j+valuesoffset].equals(""))
				   		{
					   	   	jo.put(showFields.get(j), " ");
				   		}
					   	else
					   	{
					   		jo.put(showFields.get(j), transDate(values[j+valuesoffset]));
					   	}
				   	}
				   	int joinoffset=showFields.size()+valuesoffset;
					for(HigoJoinParams inv:joins)
					{
						for(int j=0;j<inv.fl.length;j++)
						{
							int pos=j+joinoffset;
							if(pos<values.length)
							{
								jo.put(inv.returnPrefix+"@"+inv.fl[j], transDate(values[pos]));
							}else{
								jo.put(inv.returnPrefix+"@"+inv.fl[j], " ");
							}
						}
						joinoffset+=inv.fl.length;
					}
				   	
				   	jsonArray.add(i, jo);
				}
				jsonObj2.put("docs", jsonArray);
				jsonObj.put("data", jsonObj2);
				
		}
	
	public static void setGroupByQuery(SolrQuery query, ArrayList<String> fqList,ArrayList<String> groupFields,int start,int rows,HashSet<String> realDistFieldMap,HashSet<String> realFieldMap,SortParam sortType,HigoJoinParams[] joins,HashMap<String,Count> groupValueCache)
	{
		query.setParam("start","0");
		query.setParam("rows", "0");
		for(String s : fqList){
			query.addFilterQuery(s);
		}
		query.setQuery("*:*");
		query.setFacet(true);
		query.setFacetSort("index");
		query.setParam("facet.cross", "true");
		query.setParam("facet.cross.join", UniqConfig.GroupJoinString());
		query.setParam("facet.cross.offset", ""+start);
		query.setParam("facet.cross.limit", ""+rows);
		
		if(realFieldMap.size() > 0)
		{
			query.setParam(FacetParams.FACET_CROSS_FL,realFieldMap.toArray(new String[realFieldMap.size()]));
		}
		
		if(realDistFieldMap.size() > 0)
		{
			query.setParam(FacetParams.FACET_CROSSDIST_FL,realDistFieldMap.toArray(new String[realDistFieldMap.size()]));
		}

		for(String gf : groupFields){
			query.addFacetField(gf);
		}
		
		if(groupValueCache!=null)
		{
			for(String g:groupValueCache.keySet())
			{
				query.add(FacetParams.FACET_CROSS_FL_PRE_GROUPS,g);
			}
		}
		
		for(HigoJoinParams p:joins)
		{
			query.add(HigoJoinUtils.getTables(), p.tablename);
			query.add(HigoJoinUtils.getPath(p.tablename),p.hdfsPath);
			query.add(HigoJoinUtils.getLeftField(p.tablename),p.leftkey);
			query.add(HigoJoinUtils.getRightField(p.tablename),p.rightkey);
			for(String fl:p.fl)
			{
				query.add(HigoJoinUtils.getFields(p.tablename),fl);
			}
			for(String fq:p.fq)
			{
				query.add(HigoJoinUtils.getFq(p.tablename),fq);
			}
			query.add(HigoJoinUtils.getsortField(p.tablename),p.sort);

			
		}
		
		
		if(sortType.sortField!=null && !sortType.sortField.equals("")){
			query.setParam("facet.cross.sort.desc", sortType.order);
			query.setParam("facet.cross.sort.fl", sortType.sortField);
			query.setParam("facet.cross.sort.tp", sortType.sortType);
			query.setParam("facet.cross.sort.cp", sortType.cmptype);
		}else
		{
			query.setParam("facet.cross.sort.desc", sortType.order);
			query.setParam("facet.cross.sort.fl", "higoempty_sort_s");
			query.setParam("facet.cross.sort.tp", "index");
			query.setParam("facet.cross.sort.cp", "string");

		}
	}
	
	
	
	public static void setDetailByQuery(SolrQuery query, ArrayList<String> fqList,
			ArrayList<String> showFields,int start,int rows,SortParam sortType,HigoJoinParams[] joins
	)
	{
		query.setParam("start","0");
		query.setParam("rows", "0");
		for(String s : fqList){
			query.addFilterQuery(s);
		}
		query.setQuery("*:*");
		query.setFacet(true);
		query.setFacetSort("index");
		query.setParam(FacetParams.FACET_CROSS_DETAIL, "true");
		query.setParam("facet.cross", "true");
		query.setParam("facet.cross.join", UniqConfig.GroupJoinString());
		query.setParam("facet.cross.offset", ""+start);
		query.setParam("facet.cross.limit", ""+rows);
		
		for(HigoJoinParams p:joins)
		{
			query.add(HigoJoinUtils.getTables(), p.tablename);
			query.add(HigoJoinUtils.getPath(p.tablename),p.hdfsPath);
			query.add(HigoJoinUtils.getLeftField(p.tablename),p.leftkey);
			query.add(HigoJoinUtils.getRightField(p.tablename),p.rightkey);
			for(String fl:p.fl)
			{
				query.add(HigoJoinUtils.getFields(p.tablename),fl);
			}
			for(String fq:p.fq)
			{
				query.add(HigoJoinUtils.getFq(p.tablename),fq);
			}
			query.add(HigoJoinUtils.getsortField(p.tablename),p.sort);

			
		}
		
		for(String gf : showFields){
			query.addFacetField(gf);
		}
		
		if(sortType.sortField!=null && !sortType.sortField.equals("")){
			query.setParam("facet.cross.sort.desc", sortType.order);
			query.setParam("facet.cross.sort.fl", sortType.sortField);
			query.setParam("facet.cross.sort.cp", sortType.cmptype);

//			query.setParam("facet.cross.sort.tp", sortType.);
		}else{
			query.setParam("facet.cross.sort.desc", sortType.order);
			query.setParam("facet.cross.sort.fl", "higoempty_count_s");
			query.setParam("facet.cross.sort.cp", "tdouble");

//			query.setParam("facet.cross.sort.tp", "index");
		}
	}
	
	public static void setCrossStatMap(ArrayList<String> showFields,HashSet<String> realFieldMap,HashSet<String> realDistFieldMap)
	{
		for (String showfield : showFields) {
			if (showfield.startsWith("count(")) {
				String realField = showfield.substring(6, showfield.length() - 1);
				realFieldMap.add(realField.equals("*")?"higoempty_count_l":realField);
			}
			if (showfield.startsWith("sum(")) {
				String realField = showfield.substring(4, showfield.length() - 1);
				realFieldMap.add(realField);
			}
			if (showfield.startsWith("max(")) {
				String realField = showfield.substring(4, showfield.length() - 1);
				realFieldMap.add(realField);
			}
			if (showfield.startsWith("min(")) {
				String realField = showfield.substring(4, showfield.length() - 1);
				realFieldMap.add(realField);
			}
			if (showfield.startsWith("average(")) {
				String realField = showfield.substring(8, showfield.length() - 1);
				realFieldMap.add(realField);
			}
			if (showfield.startsWith("dist(")) {
				String realField = showfield.substring(5, showfield.length() - 1);
				realDistFieldMap.add(realField);
			}
		}
	
	}
	
//	public static SolrQuery makeSelectStatQuery(GetPartions.Shards shard,String realField,ArrayList<String> fqList )
//	{
//		
//		SolrQuery q3 = WebServiceParams.makeSolrQuery(shard);
//		q3.setParam("start","0");
//		q3.setParam("rows", "0");
//		q3.addSortField(realField, ORDER.desc);
//		
//		for(String s : fqList){
//			q3.addFilterQuery(s);
//		}
//		q3.setQuery("*:*");
//		return q3;
//	}
	
	
	public static void setSelectQuery(SolrQuery query,int start,int rows,String fl,ArrayList<String> fqList,SortParam sortType )
	{
		query.setParam("start",""+start);
		query.setParam("rows", ""+rows);
		query.setParam("fl", fl);
		for(String s : fqList){
			query.addFilterQuery(s);
		}
		if(sortType!=null&&sortType.sortField!=null&&sortType.order!=null&&sortType.sortType.equals("value"))
		{
		    query.addSortField(sortType.sortField,sortType.order.toLowerCase().equals("true")?ORDER.desc:ORDER.asc);
		}
		
		query.setQuery("*:*");
	}
}
