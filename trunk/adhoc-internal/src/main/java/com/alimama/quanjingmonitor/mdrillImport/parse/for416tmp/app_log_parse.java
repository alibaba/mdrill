package com.alimama.quanjingmonitor.mdrillImport.parse.for416tmp;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.alimama.mdrill.json.JSONException;
import com.alimama.mdrill.json.JSONObject;
import com.alimama.mdrillImport.InvalidEntryException;
import com.alimama.quanjingmonitor.mdrillImport.parse.FetchAdid2Pid;


/**
 * usertrack日志
 * @author yannian.mu
 *
 */
public class app_log_parse extends com.alimama.mdrillImport.DataParser{
	private static final long serialVersionUID = 1L;
	public volatile long groupCreateerror=0;

	private static Logger LOG = Logger.getLogger(app_log_parse.class);

	private volatile long lines=0;
	private volatile long lines_sb=0;
	private volatile long lines_sb3=0;

	private static long TS_MAX=3600l*24*31;

	private volatile long laststartts=System.currentTimeMillis()/1000-TS_MAX;
	private volatile long lastendts=System.currentTimeMillis()/1000+TS_MAX;

	private volatile long timediff=System.currentTimeMillis();
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
	
     private String matchGet(String s,Pattern pat,int index)
     {
    	 if(s==null)
    	 {
    		 return null;
    	 }
         Matcher mat = pat.matcher(s);

    	 while (mat.find()) {
	           return mat.group(index);
	        }
    	 return null;
     }
     
     private String get_json_object(String s,String key)
     {
    	 if(s==null)
    	 {
    		 return null;
    	 }
    	 try{
    		 JSONObject obj= new JSONObject(s);
    		 if(obj.has(key))
    		 {
    			 return String.valueOf(obj.get(key));
    		 }
    	 }catch(Throwable e)
    	 {
    		 return null;
    	 }
    	 
    	 return null;
     }
     
     
     
     private static Pattern pat_arg1=Pattern.compile("(.*)(lwfrom|point|_sb)=([^&=,]+)(.*)");
     private static Pattern pat_reserves=Pattern.compile("(.*)(lwfrom|_sb|point)=([^&=,]+)(.*)");
     private static Pattern pat_args=Pattern.compile("(.*)(lwfrom|_sb|point)=([^&=,]+)(.*)");
     private static Pattern pat_last=Pattern.compile("(\\w+)[#]*(.*)");
     
     

     /**
      2014-04-10 00:04:48 0=59.174.28.74,1=5.0.1,2=860623029116421,3=460012710506535,4=Huawei,5=ARMv7 Processor rev 2 (v7l),6=860623029116421,7=HUAWEI G610-U00,8=960*540,9=中国联通,10=Wi-Fi,11=Unknown,12=227200,13=12278902,14=4.1.2,15=tbzhanglihua,16=tbzhanglihua,17=-,18=Unknown,19=Unknown,20=Android,21=4.2.1,22=Android,23=1.3.8,24=KORRWMGMFVKHQQLJLUTIBEED_12278902_1397059405807,25=KORRWMGMFVKHQQLJLUTIBEED,26=-,27=-,28=-,29=_uid=121123981,_cc=227200,_oc=227200,30=2014-04-10 00:03:38,31=1397059418449,32=Page_Webview,33=2001,34=Page_Home,35=Page_Home_Button-home-1-5-1,36=6161,37=action=kpv,list_param=1_72091_h18019_首焦-来往-0409-0410_home-1-5-1,list_type=activity,from=lw,url=http://m.laiwang.com/go/market/laiwang/mingrenmingxing.php?locate=home-1-5-1&actparam=1_72091_h18019_%E9%A6%96%E7%84%A6-%E6%9D%A5%E5%BE%80-0409-0410&lwfrom=20140404152605385&imei=860623029116421&imsi=460012710506535&ttid=227200@taobao_android_4.1.2,dep=16,idx=1955,38=1397059200,39=1397059200,40=1397059488,41=0,
      * @param arg1
      * @param reserves
      * @param args
      * @param key
      * @return
      */
	 private String parseGet(String arg1,String reserves,String args)
	 {
		 String rtn=null;
		 if(rtn==null&&arg1!=null&&arg1.indexOf("lwfrom")>=0)
		 {
			 String lower_decode_arg1=decodeString(String.valueOf(arg1)).toLowerCase();
			 rtn=matchGet(lower_decode_arg1,pat_arg1,3);
		 }
		 
		 if(rtn==null&&reserves!=null&&reserves.indexOf("lwfrom")>=0)
		 {
			 String lower_decode_reserves=decodeString(String.valueOf(reserves)).toLowerCase();
			 rtn=matchGet(lower_decode_reserves,pat_reserves,3);

		 }
		 
		 if(rtn==null&&args!=null&&args.indexOf("lwfrom")>=0)
		 {
			 String lower_decode_args=decodeString(String.valueOf(args)).toLowerCase();
			 rtn=matchGet(lower_decode_args,pat_args,3);
		 }
		 
		 if(rtn==null)
		 {
			 return null;
		 }
		 
		 return matchGet(rtn,pat_last,1);	
	 }
	 
	 private boolean isempty(String refpid)
	 {
		 return refpid==null||refpid.isEmpty()||refpid.length()<5||refpid.length()>500;
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
			
//			boolean match_app_key="12278902".equals(app_key)||"12087020".equals(app_key)||"12500477".equals(app_key);
//			if(!match_app_key)
//			{
//				return null;
//			}
//			
//			boolean match_event_id="21032".equals(event_id)||"2001".equals(event_id)||"30001".equals(event_id);
//			if(!match_event_id)
//			{
//				return null;
//
//			}
			
			boolean match_lwfrom=arg1.indexOf("lwfrom")>=0||args.indexOf("lwfrom")>=0||reserves.indexOf("lwfrom")>=0;
			boolean match_refpid=arg1.indexOf("refpid")>=0||args.indexOf("refpid")>=0||reserves.indexOf("refpid")>=0;
			
			if(!(match_lwfrom||match_refpid))
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

			long ts = Long.parseLong(clicklog[40]);
			this.lines_sb++;
			if(this.lines_sb>5000)
			{
				this.lines_sb=0;
				long nowts=System.currentTimeMillis();
				if(nowts-timediff>30000)
				{
					timediff=nowts;
					LOG.info("parseLine_sb_"+ColsDefine.formatDayMin.format(new Date(ts*1000))+" "+formatRows(clicklog));
				}
			}

			if(ts<laststartts||ts>lastendts)
			{
				return null;
			}
				
			String refpid=null;//parseGet(arg1, reserves, args, "refpid");;
			String lwfrom=parseGet(arg1, reserves, args);;
			
			if(refpid==null&&lwfrom!=null)
			{
				String strday=ColsDefine.formatDay.format(new Date(ts*1000));
				refpid=FetchAdid2PidWireLess.fetch().get(strday+"@"+String.valueOf(lwfrom));
			}
			
			if(isempty(refpid))
			{
				this.lines_sb3++;
				if(this.lines_sb3>100)
				{
					this.lines_sb3=0;
					long nowts=System.currentTimeMillis();
					if(nowts-timediff3>30000)
					{
						timediff3=nowts;
						LOG.info("parse error :"+ColsDefine.formatDayMin.format(new Date(ts*1000))+" "+formatRows(clicklog));
					}
				}
				
				return null;
			}
			return new DataIterParse(ts,clicklog,refpid);
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

		long ts;
		public DataIterParse(long ts,String[] pvlog,String refpid) {
			this.pvlog = pvlog;
			this.ts=ts;
			this.refpid=refpid;
		}

		@Override
		public boolean next() {
			return false;
		}


		@Override
		public Number[] getSum() {
			return new Number[] { 0, 0, 0, 0, 0, 1, 0, 0,0 };
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
					String.valueOf(ColsDefine.formatDay.format(d)),
					String.valueOf(ColsDefine.formatMin.format(d)),
					"wireless",
					"app_log_parse", 
					String.valueOf(this.refpid)
					, channel // channel
					,""
					,""
					,ColsDefine.version
			};

		}
	}

	@Override
	public String[] getSumName() {
		return ColsDefine.colSumName;

	}

	@Override
	public String getTableName() {
		return ColsDefine.tablename;
	}
	

   
	@Override
	public String[] getGroupName() {
		return ColsDefine.colname;
	}
}

	
