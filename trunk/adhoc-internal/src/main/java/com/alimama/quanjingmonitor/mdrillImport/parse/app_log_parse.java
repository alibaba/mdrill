package com.alimama.quanjingmonitor.mdrillImport.parse;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import com.alimama.mdrill.json.JSONException;
import com.alimama.mdrill.json.JSONObject;
import com.alimama.mdrillImport.InvalidEntryException;


/**
 * usertrack日志
 * 
 * 
 * @author yannian.mu
 *
 */
public class app_log_parse extends com.alimama.mdrillImport.DataParser{
	private static final long serialVersionUID = 1L;
	public volatile long groupCreateerror=0;

	private static Logger LOG = Logger.getLogger(app_log_parse.class);

	private volatile long lines=0;
	private volatile long lines_sb=0;
	private volatile long lines_sb2=0;
	private volatile long lines_sb3=0;

	private static long TS_MAX=3600l*24*31;

	private volatile long laststartts=System.currentTimeMillis()/1000-TS_MAX;
	private volatile long lastendts=System.currentTimeMillis()/1000+TS_MAX;

	private volatile long timediff=System.currentTimeMillis();
	private volatile long timediff2=System.currentTimeMillis();
	private volatile long timediff3=System.currentTimeMillis();

	public static void main(String[] args) {
		System.out.println(111);
	}

	public static JSONObject parseSb(String str) throws JSONException
	{
		String[] split=str.split("[ ]*,[ ]*",-1);
		for(String s:split)
		{
			if(!s.startsWith("_sb="))
			{
				continue;
			}
			String json=decodeString(s.substring(4));
			return new JSONObject(json);
		}
		return new JSONObject();
	}
	
	 private static String decodeString(String args) {
			try {
				return new String(java.net.URLDecoder.decode(args,"UTF-8")	.getBytes("UTF-8"), "UTF-8");
			} catch (Throwable e) {
				try {
					return new String(java.net.URLDecoder.decode(args,"GBK")	.getBytes("UTF-8"), "UTF-8");
				} catch (Throwable e2) {
					return args;
				}
			}
		}
	
	 
	 public String formatRows(String[] clicklog)
	 {
		 StringBuilder b = new StringBuilder();
			for (int i = 0; i < clicklog.length; i++) {
				b.append(i);
				b.append("=");
				b.append(String.valueOf(clicklog[i]));
				b.append(",");
			}
			
			return b.toString();
	 }
	@Override
	public DataIter parseLine(String line) throws InvalidEntryException {
		
		
		try {
			if(line==null)
			{
				return null;
			}
			
			
			String[] clicklog=line.split("\001",-1);
			if(clicklog.length<41)
			{
				return null;
			}
			
						
			if(clicklog[40].isEmpty()||clicklog[40].length()<=5)
			{
				return null;
			}
			
			String app_key = clicklog[13];//应用的标识
			String event_id=clicklog[33];
			String arg1=clicklog[34];
			String args=clicklog[37];
			String reserves =clicklog[29];
			
			boolean match_app_key="12278902".equals(app_key)||"12087020".equals(app_key)||"12500477".equals(app_key);
			if(!match_app_key)
			{
				return null;

			}
			
			boolean match_event_id="21032".equals(event_id)||"2001".equals(event_id)||"30001".equals(event_id);
			if(!match_event_id)
			{
				return null;

			}
			
			boolean match_ad_id=arg1.indexOf("ad_id")>=0||args.indexOf("ad_id")>=0||reserves.indexOf("ad_id")>=0;
			boolean match_refpid=arg1.indexOf("refpid")>=0||args.indexOf("refpid")>=0||reserves.indexOf("refpid")>=0;
			
			if(!(match_ad_id||match_refpid))
			{
				return null;
			}
		
			this.lines++;
			if(this.lines>100000)
			{
				this.laststartts=(System.currentTimeMillis()/1000)-TS_MAX;
				this.lastendts=(System.currentTimeMillis()/1000)+TS_MAX;
				this.lines=0;
			}

			if(clicklog[29].isEmpty()||clicklog[29].length()<5||clicklog[29].indexOf("_sb")<0)
			{
				return null;
			}
			
			long ts = Long.parseLong(clicklog[40]);
			this.lines_sb++;
			if(this.lines_sb>5000)
			{
				this.lines_sb=0;
				long nowts=System.currentTimeMillis();
				if(nowts-timediff>30000)
				{
					timediff=nowts;
					LOG.info("parseLine_sb_"+formatDayMin.format(new Date(ts*1000))+" "+formatRows(clicklog));
				}
				
			}
			

			if(ts<laststartts||ts>lastendts)
			{
				return null;
			}
			
			JSONObject json=parseSb(clicklog[29]);
			String refpid=null;
			String ad_id=null;
			if(match_refpid)
			{
				if(json.has("refpid"))
				{
					refpid=String.valueOf(json.get("refpid"));
				}
			}
			
			
			
			if(match_ad_id)
			{
				if(json.has("ad_id"))
				{
					ad_id=String.valueOf(json.get("ad_id"));
				}
			}
			
			
			
			try {
				if(match_refpid)
				{
					if (refpid == null || refpid.isEmpty() || refpid.length() < 5
							|| refpid.length() > 500) {
						refpid = getName(args, "refpid");
					}
					
					if (refpid == null || refpid.isEmpty() || refpid.length() < 5
							|| refpid.length() > 500) {
						if(arg1.indexOf("refpid")>=0)
						{
							JSONObject jsonstr = new JSONObject(arg1);
							if (jsonstr.has("refpid")) {
								refpid = String.valueOf(jsonstr.get("refpid"));
							}
						}
					}
					
				}
				
				
				if(match_ad_id)
				{
					
					if (ad_id == null || ad_id.isEmpty() || ad_id.length() < 5
							|| ad_id.length() > 500) {
						ad_id = getName(args, "ad_id");
					}
	
					
					if (ad_id == null || ad_id.isEmpty() || ad_id.length() < 5
							|| ad_id.length() > 500) {
						if(arg1.indexOf("ad_id")>=0)
						{
							JSONObject jsonstr = new JSONObject(arg1);
							if (jsonstr.has("ad_id")) {
								ad_id = String.valueOf(jsonstr.get("ad_id"));
							}
						}
					}
				
				}
			}catch(Throwable e)
			 {
				 	this.lines_sb2++;
					if(this.lines_sb2>100)
					{
						this.lines_sb2=0;
						long nowts=System.currentTimeMillis();
						if(nowts-timediff2>30000)
						{
							timediff2=nowts;
							LOG.info("parse error :"+formatDayMin.format(new Date(ts*1000))+" "+formatRows(clicklog),e);
						}
					}
			 }
			
			
			
			if(refpid==null||refpid.isEmpty()||refpid.length()<5||refpid.length()>500)
			{
				if(match_ad_id&&ad_id!=null)
				{
					String strday=formatDay.format(new Date(ts*1000));
					refpid=FetchAdid2Pid.fetch().get(strday+"@"+String.valueOf(ad_id));
				}
			}
			
			
			if(refpid==null||refpid.isEmpty()||refpid.length()<5||refpid.length()>500)
			{
	
				this.lines_sb3++;
				if(this.lines_sb3>100)
				{
					this.lines_sb3=0;
					long nowts=System.currentTimeMillis();
					if(nowts-timediff3>30000)
					{
						timediff3=nowts;
						LOG.info("parse error :"+formatDayMin.format(new Date(ts*1000))+" "+formatRows(clicklog));
					}
				}
				
				return null;
			}
			
			return new DataIterParse(ts,clicklog,refpid,!match_ad_id);
		} catch (Throwable nfe) {
			if(groupCreateerror<100)
			{
				LOG.error("InvalidEntryException:"+line,nfe);
				groupCreateerror++;
			}
			
			throw new InvalidEntryException("Invalid log `" + line + "'\n" , nfe);
		}
	}
	
	public static class DataIterParse implements DataIter{
		private String[] pvlog=null;
		String refpid=null;
		boolean iswareless;

		long ts;
		public DataIterParse(long ts,String[] pvlog,String refpid,boolean iswareless) {
			this.pvlog = pvlog;
			this.ts=ts;
			this.refpid=refpid;
			this.iswareless=iswareless;
		}

		@Override
		public boolean next() {
			return false;
		}


		@Override
		public Number[] getSum() {
			return new Number[] { 0, 0, 0, 0, 0, 1, 0, 0 };
		}
		
		@Override
		public long getTs() {
			 return (ts/10)*10000;
		}
		

	    //wdm_v3_user_track
		@Override
		public Object[] getGroup() {
			long ts300 = (this.ts / 300) * 300000;
			Date d = new Date(ts300);
			String channel = String.valueOf(pvlog[20]).toLowerCase();
			if (channel.indexOf("android") >= 0) {
				channel = "android";
			} else if (channel.indexOf("iphone") >= 0||channel.indexOf("ios") >= 0) {
				channel = "ios";
			} else {
				channel = "other";
			}
			return new String[] { 
					String.valueOf(formatDay.format(d)),
					String.valueOf(formatMin.format(d)),
					"wireless",
					"app_log_parse", 
					(this.iswareless?"wireless":"pc"),
					String.valueOf(this.refpid) // media_pid,需要解析 reserves
					, channel // channel
					,DebugVersion.version
			};

		}
	}
	
	

    
    
    public static String getNameNodecode(String url,String keyname)
	{
    	try{
			String[] tem = url.split("\\?", 2);
			String params=tem[0];
			if (tem.length >= 2){
				params=tem[1];
			}
		
			for (String s: params.split("&", -1)) {
			    String[] tem1 = s.split("=", -1);
			    String key = decodeString(tem1[0]);
				if(key.equals(keyname))
				{
					String value = (tem1.length < 2
							? "" : decodeString(tem1[1]));
					return value;
				}
			}
    	}catch(Throwable e){}
		return null;
	 }
    
    public static String getName(String url,String keyname)
	{
    	try{
			String[] tem = decodeString(url).split("\\?", 2);
			String params=tem[0];
			if (tem.length >= 2){
				params=tem[1];
			}
		
			for (String s: params.split("&", -1)) {
			    String[] tem1 = s.split("=", -1);
			    String key = decodeString(tem1[0]);
				if(key.equals(keyname))
				{
					String value = (tem1.length < 2
							? "" : decodeString(tem1[1]));
					return value;
				}
			}
    	}catch(Throwable e){}
		return null;
	 }
	

	
	private static String[] colname={
		"thedate"
		,"miniute_5"
		,"source"
		,"sub_source"
		,"media_name"
		,"media_pid"
		,"channel"
		,"o2o"
};

	
	private static String[] colSumName={
		"pv_2"
		,"click_1"
		,"click_2"
		,"promise_click"
		,"pc_2_wap"
		,"weakup"
		,"backup_1"
		,"backup_2"
};
	
	
	@Override
	public String[] getSumName() {
		return colSumName;

	}

	@Override
	public String getTableName() {
		return "rpt_adpmp_3_8_online";
	}
	
    private static SimpleDateFormat formatDayMin = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static SimpleDateFormat formatDay = new SimpleDateFormat("yyyyMMdd");
    private static SimpleDateFormat formatMin = new SimpleDateFormat("HHmm");


	@Override
	public String[] getGroupName() {
		return colname;
	}
}

	
