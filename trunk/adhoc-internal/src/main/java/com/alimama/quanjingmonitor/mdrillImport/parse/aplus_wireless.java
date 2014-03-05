package com.alimama.quanjingmonitor.mdrillImport.parse;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import com.alimama.mdrillImport.InvalidEntryException;
import com.google.protobuf.ByteString;
import com.taobao.loganalyzer.aplus.Aplus;
public class aplus_wireless extends com.alimama.mdrillImport.DataParser{
	private static final long serialVersionUID = 1L;
	public volatile long groupCreateerror=0;

	private static Logger LOG = Logger.getLogger(aplus_wireless.class);

	private volatile long lines=0;
	private volatile long lines_sb=0;

	private static long TS_MAX=3600l*24*31;

	private volatile long laststartts=System.currentTimeMillis()/1000-TS_MAX;
	private volatile long lastendts=System.currentTimeMillis()/1000+TS_MAX;
	
	private volatile long timediff=System.currentTimeMillis();
	@Override
	public DataIter parseObject(Object line) throws InvalidEntryException {
		try {
			if(line==null)
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
			
			byte[] d=(byte[]) line;
			
			Aplus.AplusLog log = Aplus.AplusLog.parseFrom(d);
			if(log==null)
			{
				return null;
			}
			
			long ts=log.getTime();
			com.google.protobuf.ByteString currurlbyte=log.getUrl();
			com.google.protobuf.ByteString preurlbyte=log.getPre();
			String currurl=String.valueOf(currurlbyte==null?"":currurlbyte.toStringUtf8());
			String preurl=String.valueOf(preurlbyte==null?"":preurlbyte.toStringUtf8());
			
			
			if(ts<laststartts||ts>lastendts)
			{
				return null;
			}
			
		
			
			if(currurl.indexOf("h5.m.taobao.com")<0&&preurl.indexOf("h5.m.taobao.com")<0)
			{
				return null;
			}
			
			
			this.lines_sb++;
			if(this.lines_sb>5000)
			{
				this.lines_sb=0;
				long nowts=System.currentTimeMillis();
				if(nowts-timediff>30000)
				{
					timediff=nowts;
					LOG.info("parseLine_sb_"+formatDayMin.format(new Date(ts*1000))+" "+currurl+"=="+preurl+"<<<");
				}
			}
			
			String url_id=getNameNodecode(currurl, "refpid");
			String pre_id=getNameNodecode(preurl, "refpid");
			String actname=String.valueOf(getNameNodecode(currurl, "actname"));
			String pre_actname=String.valueOf(getNameNodecode(preurl, "actname"));
				
				
			if(url_id!=null&&!url_id.isEmpty())
			{
				if(currurl.indexOf("http://h5.m.taobao.com/38/o2o/home.html")>=0&&actname.equals("o2o38"))
				{
					return new DataIterParse(ts,log,url_id,pre_id);
				}
				
				if(currurl.indexOf("http://h5.m.taobao.com/channel/act/38/main.html")>=0&&actname.equals("shop38"))
				{
					return new DataIterParse(ts,log,url_id,pre_id);
				}
				
				if(currurl.indexOf("http://h5.m.taobao.com/38/wish38.html?sprefer=pca217")>=0&&actname.equals("wish38"))
				{
					return new DataIterParse(ts,log,url_id,pre_id);
				}
				if(currurl.indexOf("http://h5.m.taobao.com/other/shaoma.html")>=0&&actname.equals("alimm_ggk"))
				{
					return new DataIterParse(ts,log,url_id,pre_id);
				}
				if(currurl.indexOf("http://h5.m.taobao.com/channel/act/wetao/paimai/mrli.html")>=0&&actname.equals("auction38"))
				{
					return new DataIterParse(ts,log,url_id,pre_id);
				}
				if(currurl.indexOf("http://h5.m.taobao.com/38/recharge.html")>=0&&actname.equals("recharge38"))
				{
					return new DataIterParse(ts,log,url_id,pre_id);
				}
			}
			
			
			if(pre_id!=null&&!pre_id.isEmpty())
			{
				if(preurl.indexOf("http://h5.m.taobao.com/38/o2o/home.html")>=0&&pre_actname.equals("o2o38"))
				{
					return new DataIterParse(ts,log,url_id,pre_id);
				}
				
				if(preurl.indexOf("http://h5.m.taobao.com/channel/act/38/main.html")>=0&&pre_actname.equals("shop38"))
				{
					return new DataIterParse(ts,log,url_id,pre_id);
				}
				
				if(preurl.indexOf("http://h5.m.taobao.com/38/wish38.html?sprefer=pca217")>=0&&pre_actname.equals("wish38"))
				{
					return new DataIterParse(ts,log,url_id,pre_id);
				}
				if(preurl.indexOf("http://h5.m.taobao.com/other/shaoma.html")>=0&&pre_actname.equals("alimm_ggk"))
				{
					return new DataIterParse(ts,log,url_id,pre_id);
				}
				if(preurl.indexOf("http://h5.m.taobao.com/channel/act/wetao/paimai/mrli.html")>=0&&pre_actname.equals("auction38"))
				{
					return new DataIterParse(ts,log,url_id,pre_id);
				}
				if(preurl.indexOf("http://h5.m.taobao.com/38/recharge.html")>=0&&pre_actname.equals("recharge38"))
				{
					return new DataIterParse(ts,log,url_id,pre_id);
				}
			}
			
			
		
			return null;
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
		private Aplus.AplusLog pvlog=null;
		long ts;
		String urlid;
		String urlpre;
		public DataIterParse(long ts,Aplus.AplusLog pvlog,String urlid,String urlpre) {
			this.pvlog = pvlog;
			this.ts=ts;
			this.urlid=urlid;
			this.urlpre=urlpre;
		}

		@Override
		public boolean next() {
			return false;
		}

		@Override
		public Number[] getSum() {
			return  new Number[]{
				this.urlid!=null?1:0
				,0
				,this.urlpre!=null?1:0
				,0
				,0
				,0
				,0
				,0
		};
		}
		
		@Override
		public long getTs() {
			 return (ts/10)*10000;
		}
		

	    
		@Override
		public Object[] getGroup() {
			 long ts300=(this.ts/300)*300000;
			 Date d= new Date(ts300);
			 
			 ByteString agent=pvlog.getUserAgent();

			 String channel = String.valueOf(agent==null?"":agent.toStringUtf8()).toLowerCase();
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
					"aplus_wireless",
					"wireless",
					String.valueOf(this.urlid == null ? this.urlpre	: this.urlid) // media_pid
					, channel
					,DebugVersion.version// String.valueOf(actname)
			}	;
		
	}
	}
	

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
	
	@Override
	public String[] getSumName() {
		return colSumName;

	}

	@Override
	public String getTableName() {
		return "rpt_adpmp_3_8_online";
	}
	

    private static SimpleDateFormat formatDay = new SimpleDateFormat("yyyyMMdd");
    private static SimpleDateFormat formatMin = new SimpleDateFormat("HHmm");
    private static SimpleDateFormat formatDayMin = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


	@Override
	public String[] getGroupName() {
		return colname;
	}
}

	
