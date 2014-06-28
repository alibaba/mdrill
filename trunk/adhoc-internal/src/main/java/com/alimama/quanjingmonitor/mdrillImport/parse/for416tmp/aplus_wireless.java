package com.alimama.quanjingmonitor.mdrillImport.parse.for416tmp;

import java.util.Date;
import java.util.concurrent.LinkedBlockingDeque;

import org.apache.log4j.Logger;

import com.alimama.mdrillImport.InvalidEntryException;
import com.taobao.loganalyzer.aplus.Aplus;

public class aplus_wireless extends com.alimama.mdrillImport.DataParser {
	private static final long serialVersionUID = 1L;
	public volatile long groupCreateerror = 0;

	private static Logger LOG = Logger.getLogger(aplus_wireless.class);

	private volatile long lines = 0;
	private volatile long lines_sb = 0;
	private volatile long lines_sb2 = 0;

	private static long TS_MAX = 3600l * 24 * 31;

	private volatile long laststartts = System.currentTimeMillis() / 1000 - TS_MAX;
	private volatile long lastendts = System.currentTimeMillis() / 1000	+ TS_MAX;
	private volatile long timediff = System.currentTimeMillis();
	private volatile long timediff2 = System.currentTimeMillis();

	public static String formatRows(Aplus.AplusLog log) {
		StringBuilder b = new StringBuilder();
		b.append(log.toString());

		return b.toString();
	}

	@Override
	public DataIter parseObject(Object line) throws InvalidEntryException {
		try {
			if (line == null) {
				return null;
			}

			this.lines++;
			if (this.lines > 100000) {
				this.laststartts = (System.currentTimeMillis() / 1000) - TS_MAX;
				this.lastendts = (System.currentTimeMillis() / 1000) + TS_MAX;
				this.lines = 0;
			}

			byte[] d=(byte[]) line;
			
			Aplus.AplusLog log = Aplus.AplusLog.parseFrom(d);
			if(log==null)
			{
				return null;
			}

			long ts = log.getTime();

			
			com.google.protobuf.ByteString currurlbyte=log.getUrl();
			com.google.protobuf.ByteString preurlbyte=log.getPre();
			String currurl=String.valueOf(currurlbyte==null?"":currurlbyte.toStringUtf8());
			String preurl=String.valueOf(preurlbyte==null?"":preurlbyte.toStringUtf8());
			this.lines_sb++;
			if (this.lines_sb > 5000) {
				this.lines_sb = 0;
				long nowts = System.currentTimeMillis();
				if (nowts - timediff > 30000) {
					timediff = nowts;
					LOG.info("parseLine_sb_"+ ColsDefine.formatDayMin.format(new Date(ts * 1000)) + " "	+ " "+currurl+"=="+preurl+"<<<");

				}
			}

			if (ts < laststartts || ts > lastendts) {
				return null;
			}
			//--宋智给的格式--
			// 0 version string,
			// 1 ip string,
			// 2 time string,
			// 3 url string,
			// 4 user_agent string,
			// 5 linezing_session string,
			// 6 cna string,
			// 7 adid string,
			// 8 amid string,
			// 9 cmid string,
			// 10 pmid string,
			// 11 uid string,
			// 12 sid string,
			// 13 pre string,
			// 14 cache_ string,
			// 15 scr string,
			// 16 nick string,
			// 17 at_autype string,
			// 18 bbid string,
			// 19 at_isb string,
			// 20 at_mall_pro_re string,
			// 21 at_mall_re string,
			// 22 at_shoptype string,
			// 23 b2c_auction string,
			// 24 b2c_brand string,
			// 25 b2c_orid string,
			// 26 at_type string,
			// 27 category string,
			// 28 marketinfo string,
			// 29 atp_isdpp string,
			// 30 at_bucketid string,
			// 31 at_insid string,
			// 32 at_jporid string,
			// 33 upi_bi string,
			// 34 rpi_bi string,
			// 35 wm_pageid string,
			// 36 wm_prototypeid string,
			// 37 wm_sid string,
			// 38 spm_cnt string,
			// 39 title string,
			// 40 url_type string,
			// 41 ref_type string,
			// 42 ref_shopid string,
			// 43 parse_ip string,
			// 44 parse_time string,
			// 45 logkey string,
			// 46 gmkey string,
			// 47 gokey string,
			// 48 logtype string,
			// 49 atp_sid string,
			// 50 userid string,
			// 51 isbeta string,
			// 52 spm_url string,
			// 53 spm_pre string

//			String url_pre_lwfrom = ColsDefine.getName(log[13], "lwfrom");
			
//			if ((url_lwfrom == null || url_lwfrom.isEmpty())) {
//				return null;
//			}

			DataIterParse rtn = new DataIterParse(ts, log);
			if (rtn.getPrint()!=null) {
				this.lines_sb2++;
				if (this.lines_sb2 > 5000) {
					this.lines_sb2 = 0;
					long nowts = System.currentTimeMillis();
					if (nowts - timediff2 > 30000) {
						timediff2 = nowts;
						LOG.info("parseLine_text_"
								+ ColsDefine.formatDayMin.format(new Date(
										ts * 1000)) + " " + " "+rtn.getPrint());
					}
				}

				return null;
			}

			return rtn;
		} catch (Throwable nfe) {
			if (groupCreateerror < 100) {
				LOG.error("InvalidEntryException:" + line, nfe);
				groupCreateerror++;
			}
			throw new InvalidEntryException("Invalid log `" + line + "'\n", nfe);
		}
	}

	public static class DataIterParse implements DataIter {
		private long ts;

		private boolean ispv2 = false;
		private boolean isclick_2 = false;
		private String pid = null;

		private String prepid = null;

		java.util.concurrent.LinkedBlockingDeque<pid_type> querylist = new LinkedBlockingDeque<aplus_wireless.DataIterParse.pid_type>();

		public static class pid_type {
			public String pid = "";
			public String tid = "";
			public String matchUrl="";
			public boolean isshaoma;
		}

		pid_type curr = null;


		private String fetch(boolean isshaoma,String type,String lwfrom, String strday) {
			if (lwfrom == null || lwfrom.isEmpty()) {
				return null;
			}
			if(isshaoma||type.equals("pc"))
			{
				return  FetchAdid2PidPC.fetch().get(strday+"@"+String.valueOf(lwfrom));
			}
			return  FetchAdid2PidWireLess.fetch().get(strday+"@"+String.valueOf(lwfrom));
		}
		
		public String matchType(String url)
		{
			if(url.indexOf("http://m.laiwang.com/market/laiwang/event-square.php")>=0)
			{
				return "pc";
			}
			
			if(url.indexOf("http://h5.m.taobao.com/laiwang/act/419/index.html")>=0)
			{
				return "wireless_huodong";
			}

			if(url.indexOf("http://m.laiwang.com/go/laiwang/419/index.php")>=0)
			{
				return "wireless_huodong";
			}
			
			if(url.indexOf("http://www.laiwang.com/event/share.htm")>=0)
			{
				return "wireless_zhadui";
			}

			if(url.indexOf("http://www.laiwang.com/event/feed.htm")>=0)
			{
				return "wireless_zhadui";
			}
			
			return null;
		}
		
		String strlog=null;
		public String getPrint()
		{
			return this.strlog;
		}

		public String toUtf8String(com.google.protobuf.ByteString b)
		{
			if(b==null)
				
			{
				return "";
			}
			
			return b.toStringUtf8();
		}
		public DataIterParse(long ts, Aplus.AplusLog log) {
			this.ts = ts;
			String logtype=toUtf8String(log.getLogtype());
			String Url =toUtf8String(log.getUrl());
//			String pre = ColsDefine.decodeString(pvlog[13]);

			String strday = ColsDefine.formatDay.format(new Date(ts * 1000));
			
			String url_lwfrom = ColsDefine.getName(Url, "lwfrom");

			StringBuffer buff=new StringBuffer();

		
			String[] url_lwfrom_split=String.valueOf(url_lwfrom).split("_");
			boolean isshaoma=url_lwfrom_split.length>2&&!url_lwfrom_split[2].isEmpty();
			if(isshaoma)
			{
				String shaomaPid=this.fetch(true,"pc",url_lwfrom_split[0], strday);
				if(shaomaPid!=null)
				{
					pid_type p = new pid_type();
					p.isshaoma=true;
					p.pid = shaomaPid;
					p.matchUrl="pc";
					querylist.add(p);
				}
			}
			
			
			String isMathchUrl = matchType(Url);
			
			buff.append( Url).append("@####@");
			buff.append( logtype).append("@####@");
			buff.append( url_lwfrom).append("@####@");
			buff.append( isMathchUrl).append("@####@");


			
			boolean needprint=Url.indexOf("mm_26632292_4292703_14516725")>=0||Url.indexOf("mm_26632286_4290367_14490604")>=0;

			needprint=needprint||Url.indexOf("h5.m.taobao.com/laiwang/act/419/index.html")>=0;
			needprint=needprint||Url.indexOf("m.laiwang.com/go/laiwang/419/index.php")>=0;
			needprint=needprint||Url.indexOf("www.laiwang.com/event/share.htm")>=0;
			needprint=needprint||Url.indexOf("www.laiwang.com/event/feed.htm")>=0;
			
			if(isMathchUrl!=null)
			{
				String tid="";
					if("pc".equals(isMathchUrl))
					{
						this.pid = this.fetch(false,"pc",String.valueOf(url_lwfrom), strday);
					}else{
						this.pid=ColsDefine.getName(Url, "refpid");
						tid=ColsDefine.getName(Url, "tid");
						if(this.pid==null)
						{
							needprint=true;
						}
					}
					
					if(this.pid != null){
						pid_type p = new pid_type();
						p.isshaoma=false;
						p.pid = this.pid;
						p.tid = tid;
						p.matchUrl=isMathchUrl;
						querylist.add(p);
					}
			}
			
			if(needprint)
			{
				this.strlog=buff.toString();
			}

			this.curr = querylist.poll();

		}

		public boolean isvalidate() {
			return this.curr != null;
		}

		@Override
		public boolean next() {
			this.curr = querylist.poll();
			return isvalidate();
		}

		@Override
		public Number[] getSum() {
			int type1=(!this.curr.isshaoma&&this.curr.matchUrl.equals("wireless_zhadui"))?1:0;
			int type2=(!this.curr.isshaoma&&!this.curr.matchUrl.equals("wireless_zhadui"))?1:0;
			return new Number[] { 
								type1
									, 0
									,0
									, 0
									, 0
									, 0
									, type2
									, 0
									, this.curr.isshaoma?1:0 
							};

		}

		@Override
		public long getTs() {
			return (ts / 10) * 10000;
		}

		@Override
		public Object[] getGroup() {
			long ts300 = (this.ts / 300) * 300000;
			Date d = new Date(ts300);

			return new String[] {
					String.valueOf(ColsDefine.formatDay.format(d)),
					String.valueOf(ColsDefine.formatMin.format(d)), 
					(this.curr.isshaoma||this.curr.matchUrl.equals("pc"))?"pc":"wireless",
					"aplus_wireless",
					this.curr.pid, 
					"", this.curr.tid, "",ColsDefine.version };
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

	
