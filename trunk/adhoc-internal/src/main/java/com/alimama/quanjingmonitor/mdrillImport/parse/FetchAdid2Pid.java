package com.alimama.quanjingmonitor.mdrillImport.parse;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;

import com.alimama.mdrill.json.JSONArray;
import com.alimama.mdrill.json.JSONObject;
import com.alimama.mdrill.ui.service.MdrillService;

public class FetchAdid2Pid {
	private static Logger LOG = Logger.getLogger(FetchAdid2Pid.class);
	
	private static AtomicReference<HashMap<String, String>> lastCache=new AtomicReference<HashMap<String,String>>(new HashMap<String,String>());
	private static AtomicLong ts=new AtomicLong(System.currentTimeMillis());
	private static long maxdiff=1000l*1800;
	private static AtomicInteger times=new AtomicInteger(0);
	private static AtomicBoolean isinit=new AtomicBoolean(false);
	
	private static Object lock=new Object();

	public static Map<String,String> fetch()
	{
		if(isinit.get())
		{
			if(FetchAdid2Pid.times.incrementAndGet()<1000)
			{
				return FetchAdid2Pid.lastCache.get();
			}
			FetchAdid2Pid.times.set(0);

			Long nowts=System.currentTimeMillis();
			if(nowts-FetchAdid2Pid.ts.get()<FetchAdid2Pid.maxdiff)
			{
				return FetchAdid2Pid.lastCache.get();
			}
		}
		
		Long nowts=System.currentTimeMillis();
		synchronized (FetchAdid2Pid.lock) {
			if(isinit.get())
			{				
				if(nowts-FetchAdid2Pid.ts.get()<FetchAdid2Pid.maxdiff)
				{
					return FetchAdid2Pid.lastCache.get();
				}
			}
			
			for(int j=0;j<100;j++)
				{
				HashMap<String, String> rtn=new HashMap<String, String>();
				try {
					String fqstr="[{\"thedate\":{\"operate\":1,\"value\":[\"20140228\"]}}]";
					String jsonstr=MdrillService.result("dim_adpmp_bd_activity_d", null, "0", "10000", fqstr, null, "delivey_date,adid,pid,count(*)", "delivey_date,adid,pid", "count(*)", "desc", null, null);
					LOG.info("fetch "+j+"@"+jsonstr);
					JSONObject jsonObj = new JSONObject(jsonstr);
					if(!jsonObj.getString("code").equals("1"))
					{
						sleep();
						continue;
					}
		
					JSONArray list=jsonObj.getJSONObject("data").getJSONArray("docs");

					if(list.length()==0)
					{
						sleep();
						continue;
					}
					
					for(int i=0;i<list.length();i++)
					{
						try {
							JSONObject item = list.getJSONObject(i);
							String delivey_date= String.valueOf(item.get("delivey_date"));
							String adid = String.valueOf(item.get("adid"));
							String pid = String.valueOf(item.get("pid"));
							rtn.put(delivey_date+"@"+adid, pid);
						} catch (Exception e) {
							LOG.info("fetch parse error", e);
						}
					}
					
					FetchAdid2Pid.lastCache.set(rtn);
					FetchAdid2Pid.ts.set(System.currentTimeMillis());
					isinit.set(true);
					LOG.info("fetch_result:"+FetchAdid2Pid.lastCache.toString());

					return rtn;
				} catch (Exception e) {
					LOG.info("fetch error",e);
					sleep();
				}
			}
		}

		return FetchAdid2Pid.lastCache.get();
	}
	
	private static void sleep()
	{
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
		}
	}
}
