package com.alimama.quanjingmonitor.mdrillImport.parse;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import com.alimama.mdrillImport.InvalidEntryException;
import com.taobao.loganalyzer.input.tanxpv.parser.TanxPVLog;
import com.taobao.loganalyzer.input.tanxpv.parser.TanxPVLogParser;

public class tanx_pv extends com.alimama.mdrillImport.DataParser{
	private static final long serialVersionUID = 1L;
	public volatile long groupCreateerror=0;

	private static Logger LOG = Logger.getLogger(tanx_pv.class);

	private volatile long lines=0;
	private static long TS_MAX=3600l*24*31;

	private volatile long laststartts=System.currentTimeMillis()/1000-TS_MAX;
	private volatile long lastendts=System.currentTimeMillis()/1000+TS_MAX;
	


	public DataIter parseLine(String line) throws InvalidEntryException {
		
		try {
			TanxPVLog pvlog = TanxPVLogParser.parse(line);
			if (pvlog == null) {
				return null;
			}
			
			String pid=pvlog.getPid();
			String strts=pvlog.getTimestamp();
			if(pid==null||strts==null||pid.length()>50)
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
			
			long ts = Long.parseLong(strts);
			if(ts<laststartts||ts>lastendts)
			{
				return null;
			}
			
			
			DataIterParse rtn= new DataIterParse(pvlog,pid,ts);
			
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
		private TanxPVLog pvlog=null;
		private String pid;
		private long ts;
		public DataIterParse(TanxPVLog pvlog,String pid,long ts) {
			this.pvlog = pvlog;
			this.pid=pid;
			this.ts=ts;
		}

		@Override
		public boolean next() {
			return false;
		}


		
		@Override
		public Number[] getSum() {
			return new Number[]{1};
		}
		
		@Override
		public long getTs() {
			 return (ts/10)*10000;
		}
		

	    
		@Override
		public Object[] getGroup() {
			 Date d= new Date((ts/300)*300000);
			return new String[]{
					String.valueOf(formatDay.format(d))
					,String.valueOf(formatMin.format(d))
					,String.valueOf(this.pid)
					,String.valueOf(pvlog.getProductType())
			};
		}
		
	}

	private static String[] colname={
			"thedate"
			,"miniute_5"
			,"pid"
			,"producttype"
	};

	@Override
	public String[] getGroupName() {
		return colname;
	}

	
	private static String[] sumName={"records"};
	@Override
	public String[] getSumName() {
		return sumName;

	}

	private static String tablename="tanx_pv";
	@Override
	public String getTableName() {
		return tablename;
	}
	
    private static SimpleDateFormat formatDay = new SimpleDateFormat("yyyyMMdd");
    private static SimpleDateFormat formatMin = new SimpleDateFormat("HHmm");

}
