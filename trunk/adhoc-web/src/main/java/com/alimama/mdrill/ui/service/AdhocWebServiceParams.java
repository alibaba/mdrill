package com.alimama.mdrill.ui.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import com.alimama.mdrill.json.JSONArray;
import com.alimama.mdrill.json.JSONException;
import com.alimama.mdrill.json.JSONObject;
import com.alimama.mdrill.partion.GetPartions;
import com.alimama.mdrill.ui.service.utils.WebServiceParams;
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
			p.fq=WebServiceParams.fqListHive("dt",obj.getString("fq"), shard,false,null,null,null);
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
			p.frQuer="select "+ obj.getString("fl")+" from "+tmptblname +" "+sqlWhere.toString();
			rtn[i]=p;
		}
		return rtn;
	}
}
