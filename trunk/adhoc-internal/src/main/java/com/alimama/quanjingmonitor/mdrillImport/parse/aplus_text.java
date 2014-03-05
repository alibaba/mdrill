package com.alimama.quanjingmonitor.mdrillImport.parse;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;

import com.alimama.mdrillImport.InvalidEntryException;
public class aplus_text extends com.alimama.mdrillImport.DataParser{
	private static final long serialVersionUID = 1L;
	public volatile long groupCreateerror=0;

	/**
	 * create stream table overwrite aplus_target_table (
0  version string,
1 ip string,
2 time string,
3 url string,
4 user_agent string,
5 linezing_session string,
6 cna string,
7 adid string,
8 amid string,
9 cmid string,
10 pmid string,
11 uid string,
12 sid string,
13 pre string,
14 cache_ string,
15 scr string,
16 nick string,
17 at_autype string,
18 bbid string,
19 at_isb string,
20 at_mall_pro_re string,
21 at_mall_re string,
22 at_shoptype string,
23 b2c_auction string,
24 b2c_brand string,
25 b2c_orid string,
26 at_type string,
27 category string,
28 marketinfo string,
29 atp_isdpp string,
30 at_bucketid string,
31 at_insid string,
32 at_jporid string,
33 upi_bi string,
34 rpi_bi string,
35 wm_pageid string,
36 wm_prototypeid string,
37 wm_sid string,
38 spm_cnt string,
39 title string,
40 url_type string,
41 ref_type string,
42 ref_shopid string,
43 parse_ip string,
44 parse_time string,
45 logkey string,
46 gmkey string,
47 gokey string,
48 logtype string,
49 atp_sid string,
50 userid string,
51 isbeta string,
52 spm_url string,
53 spm_pre string 
 ) with (
      input.type='tt',
      galaxy.semantic.source.timetunnel.logname='aplus_text',
      galaxy.semantic.source.timetunnel.accesskey='accesskey',
      galaxy.semantic.source.timetunnel.subid='subid',
      galaxy.semantic.source.timetunnel.checkpoint.name='ds_rt_pv_10',
      galaxy.semantic.source.timetunnel.start.time='2013-10-23 00:00:00',
      galaxy.semantic.source.max.batch.size=100,
      galaxy.semantic.source.input.field.delimiter='\u0001',
      galaxy.semantic.source.debug=false
);
galaxy.semantic.source.parser.classpath=com.alibaba.galaxy.semantic.source.component.DefaultParser

	 */
	private static Logger LOG = Logger.getLogger(aplus_text.class);

	private volatile long lines=0;
	private volatile long lines_sb=0;
	private volatile long lines_sb2=0;


	private static long TS_MAX=3600l*24*31;

	private volatile long laststartts=System.currentTimeMillis()/1000-TS_MAX;
	private volatile long lastendts=System.currentTimeMillis()/1000+TS_MAX;
	private volatile long timediff=System.currentTimeMillis();
	
	private volatile long timediff2=System.currentTimeMillis();

	
	
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
	public   DataIter parseLine(String line) throws InvalidEntryException
	{
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
			
			String[] log =line.split("\001",-1);
			if(log==null||log.length<54)
			{
				return null;
			}
			
			
			
			
			long ts = Long.parseLong(log[2]);
			
			this.lines_sb++;
			if(this.lines_sb>5000)
			{
				this.lines_sb=0;
				long nowts=System.currentTimeMillis();
				if(nowts-timediff>30000)
				{
					timediff=nowts;
				LOG.info("parseLine_sb_"+formatDayMin.format(new Date(ts*1000))+" "+formatRows(log));
				}
			}
			
			
			if(ts<laststartts||ts>lastendts)
			{
				return null;
			}
			
			
			
//			0  version string,
//			1 ip string,
//			2 time string,
//			3 url string,
//			4 user_agent string,
//			5 linezing_session string,
//			6 cna string,
//			7 adid string,
//			8 amid string,
//			9 cmid string,
//			10 pmid string,
//			11 uid string,
//			12 sid string,
//			13 pre string,
//			14 cache_ string,
//			15 scr string,
//			16 nick string,
//			17 at_autype string,
//			18 bbid string,
//			19 at_isb string,
//			20 at_mall_pro_re string,
//			21 at_mall_re string,
//			22 at_shoptype string,
//			23 b2c_auction string,
//			24 b2c_brand string,
//			25 b2c_orid string,
//			26 at_type string,
//			27 category string,
//			28 marketinfo string,
//			29 atp_isdpp string,
//			30 at_bucketid string,
//			31 at_insid string,
//			32 at_jporid string,
//			33 upi_bi string,
//			34 rpi_bi string,
//			35 wm_pageid string,
//			36 wm_prototypeid string,
//			37 wm_sid string,
//			38 spm_cnt string,
//			39 title string,
//			40 url_type string,
//			41 ref_type string,
//			42 ref_shopid string,
//			43 parse_ip string,
//			44 parse_time string,
//			45 logkey string,
//			46 gmkey string,
//			47 gokey string,
//			48 logtype string,
//			49 atp_sid string,
//			50 userid string,
//			51 isbeta string,
//			52 spm_url string,
//			53 spm_pre string 
			
			
			String ad_id=log[7];
			String url_ad_id=getName(log[3], "ad_id");
						
			if((ad_id==null||ad_id.isEmpty())&&(url_ad_id==null||url_ad_id.isEmpty()))
			{
				return null;
			}
			
			
			DataIterParse rtn= new DataIterParse(ts,log,ad_id,url_ad_id);
			if(!rtn.isvalidate())
			{
				this.lines_sb2++;
				if(this.lines_sb2>5000)
				{
					this.lines_sb2=0;
					long nowts=System.currentTimeMillis();
					if(nowts-timediff2>30000)
					{
						timediff2=nowts;
						LOG.info("parseLine_sb2_"+formatDayMin.format(new Date(ts*1000))+" "+formatRows(log));
					}
				}
				
				return null;
			}
			
			return rtn;
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
		private long ts;
		private String ad_id;
		private String Url="";
		private String logkey="";
		
		private boolean ispv2=false;
		private boolean isclick_1=false;
		private boolean isclick_2=false;
		private String url_ad_id;
		private String pid=null;
		private String pid_Url=null;

		private Map<String, String> map=FetchAdid2Pid.fetch();
		private String fetch(String adid,String strday)
		{
			if(adid==null||adid.isEmpty())
			{
				return null;
			}
			String ad_id_cut=adid.substring(0, Math.min(10,adid.length()));
			return  map.get(strday+"@"+String.valueOf(ad_id_cut));
		}
		public DataIterParse(long ts,String[] pvlog,String ad_id,String url_ad_id) {
			this.ts=ts;
			this.ad_id=ad_id;
			this.url_ad_id=url_ad_id;
			this.Url=decodeString(pvlog[3]);
			this.logkey=pvlog[45];
			String strday=formatDay.format(new Date(ts*1000));

			this.pid=this.fetch(this.ad_id, strday);
			this.pid_Url=this.fetch(this.url_ad_id, strday);
			

			this.ispv2=this.pid!=null&&(!this.logkey.equals("/"))&&this.url_ad_id.length()>10&&this.ad_id.startsWith("10");
			this.isclick_2=this.pid!=null&&(!this.logkey.equals("/"))&&this.ad_id.startsWith("10");
			this.isclick_1=this.pid_Url!=null&&this.logkey.equals("/")&&this.url_ad_id.length()>10&&this.Url.indexOf("ju.mmstat.com")>=0;//from url
			
		}
		
		
		public boolean isvalidate()
		{
			return this.ispv2||this.isclick_2||this.isclick_1;
		}

		@Override
		public boolean next() {
			if(this.ispv2||this.isclick_2)
			{
				this.ispv2=false;
				this.isclick_2=false;

				if(this.isclick_1)
				{
					return true;
				}
			}
			
			
			return false;
		}

		@Override
		public Number[] getSum() {
			

			 if(this.ispv2||this.isclick_2)
			 {
				 return  new Number[]{
							this.ispv2?1:0
						,0
						,this.isclick_2?1:0
						,0
						,0
						,0
						,0
						,0
				};
			 }


			 return  new Number[]{
					0
					,this.isclick_1?1:0
					,0
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
				
			 if(this.ispv2||this.isclick_2)
			 {
				 return new String[] {
							String.valueOf(formatDay.format(d)),
							String.valueOf(formatMin.format(d)),
							"pc",
							"aplus_text",
							"pc",
							this.pid
							, ""
							,DebugVersion.version// String.valueOf(actname)
					}	;
			 }


			 return new String[] {
						String.valueOf(formatDay.format(d)),
						String.valueOf(formatMin.format(d)),
						"pc",
						"aplus_text",
						"pc",
						this.pid_Url
						, ""
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

	
