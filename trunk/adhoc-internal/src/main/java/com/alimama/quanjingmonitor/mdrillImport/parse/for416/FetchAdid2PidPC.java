package com.alimama.quanjingmonitor.mdrillImport.parse.for416;

import java.util.Date;
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

/**

CREATE TABLE ods_quanjing_416_pc_pid (
   putin_date string,
   pid string,
   url string,
   media string,
   thedate string
 )

 * @author yannian.mu
 *
 */
public class FetchAdid2PidPC {
	private static Logger LOG = Logger.getLogger(FetchAdid2PidPC.class);
	
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
			if(FetchAdid2PidPC.times.incrementAndGet()<1000)
			{
				return FetchAdid2PidPC.lastCache.get();
			}
			FetchAdid2PidPC.times.set(0);

			Long nowts=System.currentTimeMillis();
			if(nowts-FetchAdid2PidPC.ts.get()<FetchAdid2PidPC.maxdiff)
			{
				return FetchAdid2PidPC.lastCache.get();
			}
		}
		
		Long nowts=System.currentTimeMillis();
		synchronized (FetchAdid2PidPC.lock) {
			if(isinit.get())
			{				
				if(nowts-FetchAdid2PidPC.ts.get()<FetchAdid2PidPC.maxdiff)
				{
					return FetchAdid2PidPC.lastCache.get();
				}
			}
			
			for(int j=0;j<100;j++)
				{
				HashMap<String, String> rtn=new HashMap<String, String>();
				try {
					String strday=ColsDefine.formatDay.format(new Date(System.currentTimeMillis()-1000l*3600*24));
					String fqstr="[{\"thedate\":{\"operate\":1,\"value\":[\""+strday+"\"]}}]";
					String jsonstr=MdrillService.result("ods_quanjing_416_pc_pid", null, "0", "10000", fqstr, null, "putin_date,pid,url,count(*)", "putin_date,pid,url", "count(*)", "desc", null, null);
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
							String delivey_date= String.valueOf(item.get("putin_date"));
							String url_lwfrom=ColsDefine.getName(String.valueOf(item.get("url")), "lwfrom");
							String adid = String.valueOf(url_lwfrom);
							String pid = String.valueOf(item.get("pid"));
							rtn.put(delivey_date+"@"+adid, pid);
						} catch (Exception e) {
							LOG.info("fetch parse error", e);
						}
					}
					
					FetchAdid2PidPC.lastCache.set(rtn);
					FetchAdid2PidPC.ts.set(System.currentTimeMillis());
					isinit.set(true);
					LOG.info("fetch_result:"+FetchAdid2PidPC.lastCache.toString());

					return rtn;
				} catch (Exception e) {
					LOG.info("fetch error",e);
					sleep();
				}
			}
		}

		return FetchAdid2PidPC.lastCache.get();
	}
	
	private static void sleep()
	{
		try {
			Thread.sleep(30000);
		} catch (InterruptedException e) {
		}
	}
}
