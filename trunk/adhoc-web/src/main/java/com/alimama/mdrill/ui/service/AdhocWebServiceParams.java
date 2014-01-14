package com.alimama.mdrill.ui.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

import com.alimama.mdrill.json.JSONArray;
import com.alimama.mdrill.json.JSONException;
import com.alimama.mdrill.json.JSONObject;
import com.alimama.mdrill.partion.GetPartions;
import com.alimama.mdrill.ui.service.utils.WebServiceParams;
import com.alimama.mdrill.ui.service.utils.WebServiceParams.StateField;
import com.alimama.web.TableJoin;

public class AdhocWebServiceParams {
	public static class HigoAdhocJoinParams{
		public String tablename;
		public String hdfsPath;
		public String txtPath;
		public ArrayList<String> fq;
		public ArrayList<String> groupfq=new ArrayList<String>();
		public String[] fl;
		public String leftkey;
		public String rightkey;
		public String returnPrefix;
		public String sort;
		
		public String createSql="";
		public String DropSql="";
		public String addData="";
		public String frQuer="";
//		public Stri
		
	}
	
	
	public static HashMap<String,ArrayList<String>> parseFieldToNames(String fl, String groupby,  String params,String leftjoin) throws JSONException, SQLException
	{

		HashMap<String,ArrayList<String>> fieldToName=new HashMap<String, ArrayList<String>>();
		ArrayList<String> groupFields = WebServiceParams.groupFields(groupby);
		ArrayList<String> showFields = WebServiceParams.showHiveFields(fl);
		HigoAdhocJoinParams[] joins=AdhocWebServiceParams.parseJoinsHive(leftjoin, null);
		String daycols=AdhocWebServiceParams.parseDayCols("dt",groupFields,showFields);
		ArrayList<String> fieldlist=new ArrayList<String>();
		if(joins.length<=0)
		{
			for (String field : groupFields) {
				fieldlist.add(field);
			}
			for (String field : showFields) {
				if(!groupFields.contains(field))
				{
					fieldlist.add(field);
				}
			}

		}else
		{
			for (String field : groupFields) {
				fieldlist.add(field);
			}
			for (String field : showFields) {
				
				StateField showfield=WebServiceParams.parseStat(field);
				
				if(!groupFields.contains(showfield.realField))
				{
					if(!showfield.isstat)
					{
						fieldlist.add(field);
					}
				}
			}
			
			for(int i=0;i<joins.length;i++)
			{
				HigoAdhocJoinParams jp=joins[i];
				for (String field : jp.fl) {
					fieldlist.add(jp.tablename+"."+field);

				}
			}
			for (String field : showFields) {
				
				StateField showfield=WebServiceParams.parseStat(field);
				
				if(!groupFields.contains(showfield.realField))
				{
					if(showfield.isstat)
					{
						fieldlist.add(field);
					}
				}
			}
		}
		
		ArrayList<String> fieldNamelist=new ArrayList<String>();

		String[] pcols = params == null ? new String[0] : new String(daycols
				+ params.replaceAll("维度指标：", "").replaceAll("。.*$", ""))
				.split(",");
		for (String s : pcols) {
			if (!AdhocOfflineService.isStatFields(s)) {
				fieldNamelist.add(s);
			}
		}
		for (String s : pcols) {
			if (AdhocOfflineService.isStatFields(s)) {
				fieldNamelist.add(s);
			} 
		}
		
		fieldToName.put("field", fieldlist);
		fieldToName.put("namelist", fieldNamelist);
		return fieldToName;
	}
	
	
	public static void parseColsNoJoins(StringBuffer cols,ArrayList<String> groupFields,ArrayList<String> showFields,HashMap<String,String> colMapforStatFilter,AtomicInteger nameindex)
	{
		String join = "";

		for (String field : groupFields) {

			cols.append(join);
			cols.append(field);
			join = ",";
		}
		for (String field : showFields) {
			StateField showfield=WebServiceParams.parseStat(field);

			if(!groupFields.contains(field))
			{
				cols.append(join);
				cols.append(field);
				
				if(showfield.isstat)
				{
					String alias="tmp_"+nameindex.incrementAndGet();
					cols.append(" as "+alias);
					colMapforStatFilter.put(field, alias);
				}
				join = ",";
			}
		}
	}
	
	public static boolean hasStatFiled(ArrayList<String> showFields)
	{
		for (String field : showFields) {
			StateField showfield=WebServiceParams.parseStat(field);
			if(showfield.isstat)
			{
				return true;
			}
		}
		return false;
	}
	
	public static void parseColsWithJoins(StringBuffer cols,StringBuffer cols_inner,HigoAdhocJoinParams[] joins,HashMap<String,String> colMap,HashMap<String,String> colMapforStatFilter,ArrayList<String> groupFields,ArrayList<String> showFields,AtomicInteger nameindex)
	{
		//----inner begin-----
		String join = "";

		for (String field : groupFields) {
			if(colMap.containsKey(field))
			{
				continue;
			}
			cols_inner.append(join);
			cols_inner.append(field);
			String alias="tmp_"+nameindex.incrementAndGet();
			cols_inner.append(" as "+alias);
			colMap.put(field, alias);
			join = ",";
		}
		for (String field : showFields) {
			StateField showfield=WebServiceParams.parseStat(field);
			if(!groupFields.contains(showfield.realField))
			{
				if(colMap.containsKey(showfield.realField))
				{
					continue;
				}
				cols_inner.append(join);
				cols_inner.append(showfield.realField);
				String alias="tmp_"+nameindex.incrementAndGet();
				cols_inner.append(" as "+alias);
				colMap.put(showfield.realField, alias);
				join = ",";
			}
		}
		
		for(int i=0;i<joins.length;i++)
		{
			HigoAdhocJoinParams jp=joins[i];
			if(!groupFields.contains(jp.leftkey)&&!showFields.contains(jp.leftkey))
			{
				if(colMap.containsKey(jp.leftkey))
				{
					continue;
				}
				cols_inner.append(join);
				cols_inner.append(jp.leftkey);
				String alias="tmp_"+nameindex.incrementAndGet();
				cols_inner.append(" as "+alias);
				colMap.put(jp.leftkey, alias);
				join = ",";
			}
		}
		
		
		//----inner end-----
		
		
		
		
		
		join = "";
		for (String field : groupFields) {
			cols.append(join);
			cols.append("jl1.");
			cols.append(colMap.get(field));
			String alias="tmp_"+nameindex.incrementAndGet();
			cols.append(" as "+alias);
			join = ",";
		}
		for (String field : showFields) {
			
			StateField showfield=WebServiceParams.parseStat(field);
			
			if(!groupFields.contains(showfield.realField))
			{
				if(!showfield.isstat)
				{
					cols.append(join);

					cols.append("jl1.");
					cols.append(colMap.get(showfield.realField));
					join = ",";

				}
			}
		}
		
		for(int i=0;i<joins.length;i++)
		{
			HigoAdhocJoinParams jp=joins[i];
			for (String field : jp.fl) {
				cols.append(join);
				cols.append("jr"+i+".");
				cols.append(field);
				String alias="tmp_"+nameindex.incrementAndGet();
				cols.append(" as "+alias);
				join = ",";
			}
		}
		for (String field : showFields) {
			
			StateField showfield=WebServiceParams.parseStat(field);
			
			if(!groupFields.contains(showfield.realField))
			{
				if(showfield.isstat)
				{
					cols.append(join);

					cols.append(showfield.type);
					cols.append("(");
					cols.append("jl1.");
					cols.append(colMap.get(showfield.realField));
					cols.append(")");
					
					String alias="tmp_"+nameindex.incrementAndGet();
					cols.append(" as "+alias);
					colMapforStatFilter.put(field, alias);
					join = ",";

				}
			}
		}
		
		
	}
	
	public static String parseDayCols(String hpart,ArrayList<String> groupFields,ArrayList<String> showFields)
	{
		String daycols="";
		for (String field : groupFields) {
			if(field.equals("thedate"))
			{
				daycols="日期,";
			}
			if(field.equals(hpart))
			{
				daycols="日期,";
			}
		}
		for (String field : showFields) {
			if(field.equals("thedate"))
			{
				daycols="日期,";
			}
			if(field.equals(hpart))
			{
				daycols="日期,";
			}
		}
		return daycols;
	}
	
	public  static StringBuffer makeWhere(ArrayList<String> fqList)
	{
		StringBuffer sqlWhere = new StringBuffer();
		String join = " where ";
		for (String fq : fqList) {
			sqlWhere.append(join);
			sqlWhere.append(fq);
			join = " and ";
		}
		
		return sqlWhere;
	}
	public static HigoAdhocJoinParams[] parseJoinsHive(String leftjoin,GetPartions.Shards shard) throws JSONException, SQLException
	{
		if(leftjoin==null||leftjoin.trim().isEmpty())
		{
			return new HigoAdhocJoinParams[0];
		}
		JSONArray jsonStr=new JSONArray(leftjoin.trim());
		HigoAdhocJoinParams[] rtn=new HigoAdhocJoinParams[jsonStr.length()];
		for(int i=0;i<jsonStr.length();i++)
		{
			JSONObject obj=jsonStr.getJSONObject(i);
			HigoAdhocJoinParams p=new HigoAdhocJoinParams();
			p.tablename=obj.getString("tablename");
			
			HashMap<String, String> map=TableJoin.getTableInfo(p.tablename);
			if(map!=null)
			{
				p.txtPath=map.get("txtStorePath");
			}
			p.hdfsPath=obj.getString("path")+"/part-00000";
			if(shard!=null)
			{
				p.fq=WebServiceParams.fqListHive(false,"dt",obj.getString("fq"), shard,false,null,null,null,null);
			}
			p.fl=obj.getString("fl").split(",");
			p.leftkey=obj.getString("leftkey");
			p.rightkey=obj.getString("rightkey");
			p.returnPrefix=obj.getString("prefix");
			p.sort=obj.has("sort")?obj.getString("sort"):"";
			
			String tmptblname=("tmp_"+p.tablename+"_"+System.currentTimeMillis()).replaceAll("-", "_");
			p.createSql=AdhocHiveTmpTable.createTmpTable(tmptblname, map.get("colsName").split(","), map.get("splitString"));
			p.DropSql=AdhocHiveTmpTable.dropTable(tmptblname);
			p.addData=AdhocHiveTmpTable.addData(tmptblname, map.get("txtStorePath"));
			
			StringBuffer sqlWhere = new StringBuffer();
			String join = " where ";
			for (String fq : p.fq) {
				sqlWhere.append(join);
				sqlWhere.append(fq);
				join = " and ";
			}
			HashSet<String> allfl=new HashSet<String>();
			for(String s:obj.getString("fl").split(","))
			{
				allfl.add(s.trim());
			}
			allfl.add(p.rightkey);
			
			StringBuffer buff=new StringBuffer();
			String joinchar="";
			for(String fl:allfl)
			{
				buff.append(joinchar);
				buff.append(fl);
				joinchar=",";
			}
			
			p.frQuer="select "+ buff.toString()+" from "+tmptblname +" "+sqlWhere.toString();
			rtn[i]=p;
		}
		return rtn;
	}
}
