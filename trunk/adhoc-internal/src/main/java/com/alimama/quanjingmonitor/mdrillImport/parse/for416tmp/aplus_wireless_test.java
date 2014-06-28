package com.alimama.quanjingmonitor.mdrillImport.parse.for416tmp;

import java.util.Date;

import org.apache.log4j.Logger;

import com.alimama.mdrillImport.InvalidEntryException;
import com.google.protobuf.ByteString;
import com.taobao.loganalyzer.aplus.Aplus;
public class aplus_wireless_test extends com.alimama.mdrillImport.DataParser{
	private static final long serialVersionUID = 1L;
	public volatile long groupCreateerror=0;

	private static Logger LOG = Logger.getLogger(aplus_wireless_test.class);

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
			
		
			
			if(currurl.indexOf("laiwang")<0&&preurl.indexOf("laiwang")<0)
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
					LOG.info("parseLine_sb_"+ColsDefine.formatDayMin.format(new Date(ts*1000))+" "+currurl+"=="+preurl+"<<<");
				}
			}
			
			String url_id=ColsDefine.getNameNodecode(currurl, "p");
			String pre_id=ColsDefine.getNameNodecode(preurl, "p");
				
			String url_pid=ColsDefine.getNameNodecode(currurl, "tid");
			String pre_pid=ColsDefine.getNameNodecode(preurl, "tid");
				
			if(url_id!=null&&!url_id.isEmpty())
			{
				return new DataIterParse(ts,log,url_id,pre_id,url_pid,pre_pid);
			}
			
			
			if(pre_id!=null&&!pre_id.isEmpty())
			{
				return new DataIterParse(ts,log,url_id,pre_id,url_pid,pre_pid);
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
		String urlid_tid;
		String urlpre_tid;
		public DataIterParse(long ts,Aplus.AplusLog pvlog,String urlid,String urlpre,String url_pid,String pre_pid) {
			this.pvlog = pvlog;
			this.ts=ts;
			this.urlid=urlid;
			this.urlpre=urlpre;
			
			this.urlid_tid=url_pid;
			this.urlpre_tid=pre_pid;
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
					String.valueOf(ColsDefine.formatDay.format(d)),
					String.valueOf(ColsDefine.formatMin.format(d)),
					"wireless",
					"aplus_wireless",
					String.valueOf(this.urlid == null ? this.urlpre	: this.urlid) // media_pid
					, channel
					,String.valueOf(this.urlid == null ? this.urlpre_tid	: this.urlid_tid)
					,""
					,ColsDefine.version
			}	;
		
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

	
