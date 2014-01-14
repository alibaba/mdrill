package com.alimama.quanjingmonitor.mdrillImport.parse;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import com.alimama.mdrillImport.InvalidEntryException;

public class tanx_click_rt extends com.alimama.mdrillImport.DataParser{
	private static final long serialVersionUID = 1L;
	public volatile long groupCreateerror=0;

	private static Logger LOG = Logger.getLogger(tanx_click_rt.class);

	private volatile long lines=0;
	private static long TS_MAX=3600l*24*31;

	private volatile long laststartts=System.currentTimeMillis()/1000-TS_MAX;
	private volatile long lastendts=System.currentTimeMillis()/1000+TS_MAX;
	@Override
	public DataIter parseLine(String line) throws InvalidEntryException {
		
		try {
			if(line==null)
			{
				return null;
			}
			String[] clicklog=line.split("\001",-1);
			if(clicklog.length<5)
			{
				return null;
			}
			
			
			
			if(clicklog[4].isEmpty()||clicklog[1].isEmpty()||clicklog[1].length()<=5||clicklog[4].length()>50)
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
			
			long ts = Long.parseLong(clicklog[1]);
			if(ts<laststartts||ts>lastendts)
			{
				return null;
			}
			
			
			DataIterParse rtn= new DataIterParse(clicklog);
			
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
		private String[] pvlog=null;

		public DataIterParse(String[] pvlog) {
			this.pvlog = pvlog;
		}

		@Override
		public boolean next() {
			return false;
		}


		@Override
		public Number[] getSum() {
			Number[] rtn=new Number[1];
			rtn[0]=1;
			return rtn;
		}
		
		@Override
		public long getTs() {
			 long ts = Long.parseLong(pvlog[1]);
			 return (ts/300)*300000;
		}
		

	    
		@Override
		public Object[] getGroup() {
			 long ts = Long.parseLong(pvlog[1]);
			 long ts300=(ts/300)*300000;
			 Date d= new Date(ts300);
			 
			String[] rtn=new String[4];
			rtn[0]=String.valueOf(formatDay.format(d));
			rtn[1]=String.valueOf(formatMin.format(d));
			rtn[2]=String.valueOf(pvlog[4]);
			rtn[3]=String.valueOf("tanx_rd_log");
			return rtn;
		}
		
	}
	

	

	@Override
	public String[] getGroupName() {
		String[] rtn=new String[4];
		rtn[0]="thedate";
		rtn[1]="miniute_5";
		rtn[2]="pid";
		rtn[3]="logtype";
		return rtn;
	}

	

	@Override
	public String[] getSumName() {
		String[] rtn=new String[1];
		rtn[0]="records";
		return rtn;

	}

	@Override
	public String getTableName() {
		return "tanx_click";
	}
	
    private static SimpleDateFormat formatDay = new SimpleDateFormat("yyyyMMdd");
    private static SimpleDateFormat formatMin = new SimpleDateFormat("HHmm");

}
