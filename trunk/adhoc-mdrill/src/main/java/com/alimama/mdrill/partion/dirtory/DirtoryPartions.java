package com.alimama.mdrill.partion.dirtory;

import java.util.HashMap;
import java.util.HashSet;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.alimama.mdrill.json.JSONArray;
import com.alimama.mdrill.json.JSONObject;
import com.alimama.mdrill.partion.MdrillPartions;
import com.alimama.mdrill.partion.MdrillPartionsInterface;
import com.alimama.mdrill.partion.PartionListener;
import com.alimama.mdrill.partion.StatListenerInterface;
import com.alimama.mdrill.ui.service.utils.OperateType;
import com.alimama.mdrill.ui.service.utils.WebServiceParams;

public class DirtoryPartions implements MdrillPartionsInterface{

	private String parttype="dir@dir"; 
	private String fieldname="dir";
	public void setPartionType(String parttype)
	{
		this.parttype=parttype;
		String[] params=this.parttype.split("@");
		if(params.length>1)
		{
			this.fieldname=params[1];
		}
	}

	@Override
	public String[] SqlPartions(String queryStr) throws Exception {
		HashSet<String> rtn = new HashSet<String>();
		JSONArray jsonStr = new JSONArray(queryStr.trim());

		for (int j = 0; j < jsonStr.length(); j++) {
			JSONObject obj = jsonStr.getJSONObject(j);
			if (obj.has(this.fieldname)) {
				JSONObject thedate = obj.getJSONObject(this.fieldname);
				Integer operate = Integer.parseInt(thedate.get("operate").toString());
				OperateType optype=WebServiceParams.parseOperateType(operate);
				String[] val = WebServiceParams.parseFqValue(thedate.getString("value"), operate).split(",");
				if (optype.equals(OperateType.eq)||optype.equals(OperateType.in))
				{
					for (String dir : val) {
						rtn.add(dir);
					}

				}
			}
		}

		String[] rtnarr=new String[rtn.size()];
		return rtn.toArray(rtnarr);

	}

	@Override
	public String SqlFilter(String queryStr) throws Exception {
		JSONArray rtn = new JSONArray(queryStr.trim());
		JSONArray jsonStr = new JSONArray(queryStr.trim());

		for (int j = 0; j < jsonStr.length(); j++) {
			JSONObject obj = jsonStr.getJSONObject(j);
			if (!obj.has(this.fieldname)) {
				rtn.put(obj);
			}
		}
		return rtn.toString();
	}

	@Override
	public HashSet<String> getNameList(FileSystem fs, String inputBase,
			String startPoint, int dayDelay, int maxRunDays) throws Exception {
		HashSet<String> rtn = new HashSet<String>();
		FileStatus[] list = fs.listStatus(new Path(inputBase));
		if (list != null) {
			for (FileStatus f : list) {
				String name = f.getPath().getName().trim();
				if (name != null&&!name.isEmpty()) {
					rtn.add(name);
				}
			}
		}

		return rtn;
	}

	@Override
	public HashMap<String, HashSet<String>> indexPartions(
			HashSet<String> namelist, String startday, int dayDelay,
			int maxRunDays) throws Exception {
		HashMap<String, HashSet<String>> rtn=new HashMap<String, HashSet<String>>();
		for(String s:namelist)
		{
			HashSet<String> list=new HashSet<String>();
			list.add(s);
			rtn.put(s, list);
		}
		return rtn;
	}

	@Override
	public HashMap<String, String> indexVertify(
			HashMap<String, HashSet<String>> partions, int shards,
			String startday, int dayDelay, int maxRunDays) throws Exception {
		HashMap<String, String> rtn=new HashMap<String, String>();
		for(String key:partions.keySet())
		{
			String partionvertify = "partionV"+MdrillPartions.PARTION_VERSION+"@001@"+key+"@" + shards ;
			rtn.put(key, partionvertify);
		}
		
		return rtn;
	}

	@Override
	public StatListenerInterface getStatObj() throws Exception {
		StatListenerInterface rtn=new PartionListener();
		rtn.setPartionType(this.parttype);
		rtn.init();
		return rtn;
	}

}
