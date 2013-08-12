package com.alimama.mdrill.partion.single;

import java.util.HashMap;
import java.util.HashSet;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.alimama.mdrill.partion.MdrillPartions;
import com.alimama.mdrill.partion.MdrillPartionsInterface;
import com.alimama.mdrill.partion.PartionListener;
import com.alimama.mdrill.partion.StatListenerInterface;

public class SinglePartions implements MdrillPartionsInterface{

	private String parttype="single"; 
	public void setPartionType(String parttype)
	{
		this.parttype=parttype;
	}

	@Override
	public String[] SqlPartions(String queryStr) throws Exception {
		 String[] rtn=new String[]{"single"};
		return rtn;

	}

	@Override
	public String SqlFilter(String queryStr) throws Exception {
		return queryStr;
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
		rtn.put("single", namelist);
		return rtn;
	}

	@Override
	public HashMap<String, String> indexVertify(
			HashMap<String, HashSet<String>> partions, int shards,
			String startday, int dayDelay, int maxRunDays) throws Exception {
		HashMap<String, String> rtn=new HashMap<String, String>();
		for(String key:partions.keySet())
		{
			String partionvertify = "partionV"+MdrillPartions.PARTION_VERSION+"@001@"+key+"@" + shards + "@"+ java.util.UUID.randomUUID().toString();
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
