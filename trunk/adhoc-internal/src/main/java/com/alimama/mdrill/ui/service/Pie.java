package com.alimama.mdrill.ui.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.servlet.jsp.JspWriter;

import com.alimama.mdrill.json.JSONArray;
import com.alimama.mdrill.json.JSONException;
import com.alimama.mdrill.json.JSONObject;
import com.alimama.mdrill.ui.service.utils.WebServiceParams;
import com.alimama.mdrill.ui.service.utils.WebServiceParams.StateField;

public class Pie {
	
	public static ArrayList<String> parseGroup(String fl)
	{
		ArrayList<String> groupbyFields=new ArrayList<String>();
		ArrayList<String> showFields = WebServiceParams.showFields(fl);
		for (String showfield : showFields) {
			StateField showfielda=WebServiceParams.parseStat(showfield);
			if(!showfielda.isstat)
			{
				groupbyFields.add(showfield);
			}
			
		}
		return groupbyFields;
	}
	
	public static String parseStatList(String projectName, String queryStr,	String fl, String groupby,String dimvalue) throws JSONException, SQLException
	{
		JSONObject jsonObj = new JSONObject();
		ArrayList<String> StatFields=new ArrayList<String>();
		ArrayList<String> StatFieldsDisplay=new ArrayList<String>();
		HashMap<String,ArrayList<String>> namelist=AdhocWebServiceParams.parseFieldToNames(fl, groupby, dimvalue, null);
		ArrayList<String> namelistread=namelist.get("namelist");
		ArrayList<String> showFields = namelist.get("field");
		StatFields.add("count(*)");
		StatFieldsDisplay.add("计数(*)");
		for(int i=0;i<showFields.size()&&i<namelistread.size();i++){
			String showfield=showFields.get(i);
			String showfieldDisplay=namelistread.get(i);
			StateField showfielda=WebServiceParams.parseStat(showfield);
			if(showfielda.isstat)
			{
				StatFields.add(showfield);
				StatFieldsDisplay.add(showfieldDisplay);

			}
		}
		jsonObj.put("stats", StatFields);
		jsonObj.put("statsShow", StatFieldsDisplay);

		jsonObj.put("project", projectName);
		return jsonObj.toString();
	}
	
	
	public static String parseGroupList(String projectName, String queryStr,	String fl, String groupby,String dimvalue) throws JSONException, SQLException
	{
		JSONObject jsonObj = new JSONObject();
		ArrayList<String> StatFields=new ArrayList<String>();
		ArrayList<String> StatFieldsDisplay=new ArrayList<String>();
		HashMap<String,ArrayList<String>> namelist=AdhocWebServiceParams.parseFieldToNames(fl, groupby, dimvalue, null);
		ArrayList<String> namelistread=namelist.get("namelist");
		ArrayList<String> showFields = namelist.get("field");
		for(int i=0;i<showFields.size()&&i<namelistread.size();i++){
			String showfield=showFields.get(i);
			String showfieldDisplay=namelistread.get(i);
			StateField showfielda=WebServiceParams.parseStat(showfield);
			if(!showfielda.isstat)
			{
				StatFields.add(showfield);
				StatFieldsDisplay.add(showfieldDisplay);

			}
		}
		jsonObj.put("group", StatFields);
		jsonObj.put("groupShow", StatFieldsDisplay);

		jsonObj.put("project", projectName);
		return jsonObj.toString();
	}
	
	public static String parseStat(String projectName, String queryStr,	String fl, String groupby,String dimvalue) throws JSONException, SQLException
	{
		JSONObject jsonObj = new JSONObject();
		ArrayList<String> groupbyFields=new ArrayList<String>();
		ArrayList<String> showFields = WebServiceParams.showFields(fl);
		jsonObj.put("pie_stat", "count(*)");
		for (String showfield : showFields) {
			StateField showfielda=WebServiceParams.parseStat(showfield);
			if(showfielda.isstat)
			{
				jsonObj.put("pie_stat", showfield);

			}else{
				groupbyFields.add(showfield);
			}
			
		}
		jsonObj.put("fieldToName", AdhocWebServiceParams.parseFieldToNames(fl, groupby, dimvalue, null));
		jsonObj.put("pie_dimvalue", dimvalue);
		jsonObj.put("pie_groupby",groupbyFields);
		jsonObj.put("project", projectName);

		jsonObj.put("q", queryStr);
		jsonObj.put("sort", String.valueOf(jsonObj.getString("pie_stat")));
		jsonObj.put("order", String.valueOf("desc"));
		
		return jsonObj.toString();
	}
	public static String result(String projectName, 
			String startStr, String rowsStr, String queryStr, 
			String fl, String groupby, String sort, String order,JspWriter out)
			throws Throwable
			{
			return result(projectName, startStr, rowsStr, queryStr, fl, groupby, sort, order, "Y", out);
			}
		
	public static String result(String projectName, 
			String startStr, String rowsStr, String queryStr, 
			String fl, String groupby, String sort, String order,String showOther,JspWriter out)
			throws Throwable {
		   HeartBeat hb=new HeartBeat(out);
			new Thread(hb).start();
			
			String rtn= MdrillService.result(projectName, null, startStr,rowsStr, queryStr, null, fl, groupby, sort, order, null, null);

			  HashMap<String,Object> rtna=new HashMap<String, Object>();

			JSONObject jsonObj = new JSONObject(rtn);
			if(!jsonObj.getString("code").equals("1"))
			{
				rtna.put("code",0);
				rtna.put("msg","服务器异常，请稍后再试");
				hb.stop();
				return new JSONObject(rtna).toString();
			}
			JSONArray list=jsonObj.getJSONObject("data").getJSONArray("docs");
			ArrayList<Object> data=new ArrayList< Object>();
			ArrayList<String> showFields = WebServiceParams.showFields(fl);
			String flkey=groupby;
			String flvalue="";
			for (String showfield : showFields) {
					StateField showfielda=WebServiceParams.parseStat(showfield);
					if(showfielda.isstat)
					{
						flvalue=showfield;
					}
					
			}
			JSONArray filterother=new JSONArray(WebServiceParams.query(queryStr));
			JSONArray filterother2=new JSONArray();

			for(int i=0;i<list.length();i++)
			{
				JSONObject item=list.getJSONObject(i);
				JSONObject newitem=new JSONObject();
				newitem.put("label", String.valueOf(item.get(flkey)));
				newitem.put("data", Double.parseDouble(item.getString(flvalue)));
				data.add(newitem);

				JSONObject newitemfilter=new JSONObject();
				newitemfilter.put("key", flkey);
				newitemfilter.put("operate", "2");
				newitemfilter.put("value",  String.valueOf(item.get(flkey)));
				filterother.put(newitemfilter);
				filterother2.put(newitemfilter);
				

			}
			
			if("Y".equals(showOther))
			{
				String rtnother= MdrillService.result(projectName, null, startStr,rowsStr, filterother.toString(), null, flvalue, null, sort, order, null, null);
				JSONObject jsonObjother = new JSONObject(rtnother);
				if(jsonObjother.getString("code").equals("1"))
				{
					JSONArray listother=jsonObjother.getJSONObject("data").getJSONArray("docs");
					for(int i=0;i<listother.length();i++)
					{
						JSONObject item=listother.getJSONObject(i);
						Double d=Double.parseDouble(item.getString(flvalue));
						if(d>0)
						{
						JSONObject newitem=new JSONObject();
						newitem.put("label", "其他");
						newitem.put("data",d );
						data.add(newitem);
						}
					}
				}
			}
			
			hb.stop();
			
		
		    rtna.put("code", 1);
			rtna.put("其他", filterother2);

			  rtna.put("data", data);

			  String rtnstr= new JSONObject(rtna).toString();
			  out.write(rtnstr);
			  return rtnstr;

	}
}
