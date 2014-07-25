package com.alimama.mdrill.ui.service;

import java.util.HashMap;
import java.util.Map;

import backtype.storm.utils.Utils;

import com.alimama.mdrill.json.JSONArray;
import com.alimama.mdrill.json.JSONException;
import com.alimama.mdrill.json.JSONObject;
import com.alimama.web.TableList;

public class AdhocUi {
	
	public static String getTableListForUi() throws Exception
	{
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("code", 1);
		jsonObj.put("message", "success");
		
		JSONArray modules = new JSONArray();
		
		
		HashMap<String,JSONObject> tableInfo=new HashMap<String, JSONObject>();
//		{
//			"moduleName": "P4P-推广",
//			"moduleId": "3",
//			"moduleTableName":"rpt_p4padhoc_product",
//			"begin":"2012-10-10",
//            "dateLimit": -30
//		}
		for(String s:TableList.getTablelist())
		{
			JSONObject item=new JSONObject();
			item.put("moduleName", s);
			item.put("moduleId", s);
		}
		Map stormconf = Utils.readStormConfig();
		String uiconfpath=(String) stormconf.get("higo.ui.conf.dir");


		
		return jsonObj.toString();
	}
}
