package com.alimama.quanjingmonitor.mdrillImport.parse.for416tmp;

import java.util.Date;
import java.util.concurrent.LinkedBlockingDeque;

import org.apache.log4j.Logger;

import com.alimama.mdrillImport.InvalidEntryException;
import com.alimama.quanjingmonitor.mdrillImport.parse.for416tmp.aplus_text.DataIterParse.pid_type;
import com.taobao.loganalyzer.aplus.Aplus;

public class aplus_hjlj extends com.alimama.mdrillImport.DataParser {
	private static final long serialVersionUID = 1L;
	public volatile long groupCreateerror = 0;

	private static Logger LOG = Logger.getLogger(aplus_hjlj.class);

	private volatile long lines = 0;
	private volatile long lines_sb = 0;
	private volatile long lines_sb2 = 0;

	private static long TS_MAX = 3600l * 24 * 31;

	private volatile long laststartts = System.currentTimeMillis() / 1000 - TS_MAX;
	private volatile long lastendts = System.currentTimeMillis() / 1000	+ TS_MAX;
	private volatile long timediff = System.currentTimeMillis();
	private volatile long timediff2 = System.currentTimeMillis();


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

			if (ts < laststartts || ts > lastendts) {
				return null;
			}
		
			DataIterParse rtn = new DataIterParse(ts, log);
			if (!rtn.isvalidate()) {
				this.lines_sb2++;
				if (this.lines_sb2 > 5000) {
					this.lines_sb2 = 0;
					long nowts = System.currentTimeMillis();
					if (nowts - timediff2 > 30000) {
						timediff2 = nowts;
						LOG.info("parseLine_hjlj_"
								+ ColsDefine.formatDayMin.format(new Date(
										ts * 1000)) );
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



		java.util.concurrent.LinkedBlockingDeque<pid_type> querylist = new LinkedBlockingDeque<aplus_hjlj.DataIterParse.pid_type>();

		public static class pid_type {
			public String pid = "";
			public String tid = "";
			public String matchUrl="";
			public boolean isAplusMode=false;

		}

		pid_type curr = null;


		private String fetch(String type,String lwfrom, String strday) {
			if (lwfrom == null || lwfrom.isEmpty()) {
				return null;
			}
			if(type.equals("pc"))
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
		
		public String matchType(String gmkey,String logkey)
		{
			if(logkey.indexOf("laiwang.1.1")>=0&&gmkey.indexOf("outer419")>=0)
			{
				return "pc";
			}
			
			if(gmkey.indexOf("419")<0)
			{
				return null;
			}
			
			if(gmkey.indexOf("h5down")>=0)
			{
				return "wireless_huodong";
			}
			
			if(gmkey.indexOf("419_cat")>=0)
			{
				return "wireless_huodong";
			}

			if(gmkey.indexOf("419_rank")>=0)
			{
				return "wireless_huodong";
			}
			
			if(gmkey.indexOf("419_rank_all")>=0)
			{
				return "wireless_huodong";
			}
			
			if(gmkey.indexOf("419_rank_catid")>=0)
			{
				return "wireless_huodong";
			}
			
			if(gmkey.indexOf("419_rule")>=0)
			{
				return "wireless_huodong";
			}
			
			if(gmkey.indexOf("outer419_lwfrom")>=0)
			{
				return "wireless_huodong";
			}
			
			if(gmkey.indexOf("launch419_lwfrom")>=0)
			{
				return "wireless_zhadui";
			}
			
			return null;
		}
		
		public String toUtf8String(com.google.protobuf.ByteString b)
		{
			if(b==null)
				
			{
				return "";
			}
			
			return b.toStringUtf8();
		}
		
		
		
		public void parse_aplustext(long ts, Aplus.AplusLog log,String currurl)
		{

			String isMathchUrl = matchType(currurl);
			
					
			if(isMathchUrl!=null&&!"pc".equals(isMathchUrl))
			{
					String pid=ColsDefine.getName(currurl, "refpid");
					if(pid != null){
						String tid=ColsDefine.getName(currurl, "tid");;
						pid_type p = new pid_type();
						p.isAplusMode=true;
						p.pid = pid;
						p.tid = tid;
						p.matchUrl=isMathchUrl;
						querylist.add(p);
					}
			}
		}
		
		public void parse_hjlj(long ts, Aplus.AplusLog log,String currurl)
		{
			String logkey=toUtf8String(log.getLogkey());//pvlog[45];
			boolean boolisMathchUrl = logkey.indexOf("laiwang.1.1")>=0;//&&logtype.equals("2")
			
			if(!boolisMathchUrl)
			{
				return ;
			}
			
			String gmkey=toUtf8String(log.getGmkey());//pvlog[46];

			String matchurl_type = matchType(gmkey,logkey);
			
			if(matchurl_type==null)
			{
				return ;
			}
			
			if("pc".equals(matchurl_type))
			{
				String strday = ColsDefine.formatDay.format(new Date(ts * 1000));

				String url_lwfrom = ColsDefine.getName(currurl, "lwfrom");
				String pid = this.fetch("pc",url_lwfrom, strday);
				
				if(pid!=null)
				{
					pid_type p = new pid_type();
					p.pid = pid;
					p.tid = "";
					p.matchUrl=matchurl_type;
					p.isAplusMode=false;
					querylist.add(p);
				}
			
				
			}else{

				String pid=ColsDefine.getName(currurl, "refpid");
				if(pid!=null){
					String tid=ColsDefine.getName(currurl, "tid");
					pid_type p = new pid_type();
					p.pid = pid;
					p.tid = tid;
					p.matchUrl=matchurl_type;
					p.isAplusMode=false;
					querylist.add(p);
				}
			}

			
		}

		public DataIterParse(long ts, Aplus.AplusLog log) {
			this.ts = ts;
			
			String currurl=toUtf8String(log.getUrl());

			this.parse_aplustext(ts, log,currurl);
			this.parse_hjlj(ts, log,currurl);
	
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
			if(this.curr.isAplusMode)
			{

				int type1=(this.curr.matchUrl.equals("wireless_zhadui"))?1:0;
				int type2=(!this.curr.matchUrl.equals("wireless_zhadui"))?1:0;
				return new Number[] { 
									type1
										, 0
										,0
										, 0
										, 0
										, 0
										, type2
										, 0
										, 0 
								};

			
			}else{
			
			
				int type1=(this.curr.matchUrl.equals("wireless_zhadui"))?1:0;
				int type2=(!this.curr.matchUrl.equals("wireless_zhadui"))?1:0;
				return new Number[] { 
									0
										, type1
										,0
										, 0
										, 0
										, 0
										, 0
										, type2
										, 0 
								};
			}

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
					this.curr.matchUrl,
					"aplus_hjlj",
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

	
