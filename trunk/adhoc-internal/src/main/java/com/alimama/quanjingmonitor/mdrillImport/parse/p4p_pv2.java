package com.alimama.quanjingmonitor.mdrillImport.parse;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import com.alimama.quanjingmonitor.parser.InvalidEntryException;
import com.taobao.loganalyzer.input.p4ppv.parser.P4PPVLog;

public class p4p_pv2 extends com.alimama.quanjingmonitor.mdrillImport.DataParser{
	private static final long serialVersionUID = 1L;
	public volatile long groupCreateerror=0;

	private static Logger LOG = Logger.getLogger(p4p_pv2.class);

	@Override
	public DataIter parseLine(String line) throws InvalidEntryException {
		
		try {
			P4PPVLog p4ppvlog = com.taobao.loganalyzer.input.p4ppv.parser.P4PPVLogParser.parse(line);
			if (p4ppvlog == null) {
				return null;
			}
			
			if(p4ppvlog.getP4PPID()==null)
			{
				return null;
			}
			DataIterParse rtn= new DataIterParse(p4ppvlog);
			
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
		private P4PPVLog p4ppvlog=null;

		public DataIterParse(P4PPVLog p4ppvlog) {
			this.p4ppvlog = p4ppvlog;
		}

		@Override
		public boolean next() {
			return false;
		}


		@Override
		public double[] getSum() {
			double[] rtn=new double[1];
			rtn[0]=1;
			return rtn;
		}
		
		@Override
		public long getTs() {
			 long ts = Long.parseLong(p4ppvlog.getTimestamp());
			 return (ts/300)*300000;
		}
		

	    
		@Override
		public String[] getGroup() {
			 long ts = Long.parseLong(p4ppvlog.getTimestamp());
			 long ts300=(ts/300)*300000;
			 Date d= new Date(ts300);
			 
			String[] rtn=new String[5];
			rtn[0]=String.valueOf(formatDay.format(d));
			rtn[1]=String.valueOf(formatMin.format(d));
			rtn[2]=String.valueOf(formatHour.format(d));
			rtn[3]=String.valueOf(p4ppvlog.getP4PPID());
			rtn[4]=String.valueOf(getName(p4ppvlog.getQueryOriginURL(),"keyword"));
			return rtn;
		}
		
	}
	

	

	@Override
	public String[] getGroupName() {
		String[] rtn=new String[5];
		rtn[0]="thedate";
		rtn[1]="miniute_5";
		rtn[2]="hour";
		rtn[3]="pid";
		rtn[4]="keyword";
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
		return "p4p_pv2";
	}
    private static SimpleDateFormat formatDay = new SimpleDateFormat("yyyyMMdd");
    private static SimpleDateFormat formatMin = new SimpleDateFormat("HHmm");
    private static SimpleDateFormat formatHour = new SimpleDateFormat("HH");
    private static final String LOG_ENCODING = "GBK";

    private static String decodeString(String args) {
		try {
			return new String(java.net.URLDecoder.decode(args,LOG_ENCODING)	.getBytes("UTF-8"), "UTF-8");
		} catch (Exception e) {
			return args;
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
			    try {
				String key = URLDecoder.decode(tem1[0], LOG_ENCODING);
				if(key.equals(keyname))
				{
					String value = (tem1.length < 2
							? "" : URLDecoder.decode(tem1[1], LOG_ENCODING));
					return value;
				}
			    } catch (UnsupportedEncodingException uee) {
			    }
			}
    	}catch(Throwable e){}
		return null;
	 }

}
