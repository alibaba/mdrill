package com.alimama.quanjingmonitor.mdrillImport.parse.goldeye;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.alimama.mdrillImport.InvalidEntryException;
import com.taobao.loganalyzer.input.p4ppv.parser.P4PPVLog;

public class p4p_wt_itempv extends com.alimama.mdrillImport.DataParser{
	private static final long serialVersionUID = 1L;
	public volatile long groupCreateerror=0;

	private static Logger LOG = Logger.getLogger(p4p_wt_itempv.class);

	private volatile long lines=0;
	private static long TS_MAX=3600l*24*31;

	private volatile long laststartts=(System.currentTimeMillis()/1000)-TS_MAX;
	private volatile long lastendts=(System.currentTimeMillis()/1000)+TS_MAX;
	@Override
	public DataIter parseLine(String line) throws InvalidEntryException {
		
		try {
			P4PPVLog p4ppvlog = com.taobao.loganalyzer.input.p4ppv.parser.P4PPVLogParser.parse(line);
			if (p4ppvlog == null) {
				return null;
			}
			
			if(p4ppvlog.getP4PPID()==null||p4ppvlog.getTimestamp()==null)
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
			
			long ts = Long.parseLong(p4ppvlog.getTimestamp());
			if(ts<laststartts||ts>lastendts)
			{
				return null;
			}
			
			DataIterParse rtn= new DataIterParse(p4ppvlog);
			
			return rtn;
		} catch (Throwable nfe) {
			if(groupCreateerror<10)
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
		public long getTs() {
			 long ts = Long.parseLong(p4ppvlog.getTimestamp());
			 return (ts/10)*10000;
		}
		

	    
		@Override
		public Object[] getGroup() {
			 long ts = Long.parseLong(p4ppvlog.getTimestamp());
			 long ts300=(ts/300)*300000;
			 Date d= new Date(ts300);
			 
			 String bucketId = "";
			String seaBucketid = "";
			try{
				String tagFields[] = p4ppvlog.getAbTag().split(";",3);
				if ( tagFields.length >= 2 ){
					bucketId = tagFields[0];
					seaBucketid =tagFields[1];
				}
			} catch (Throwable e) {
			}
			
		    List<P4PPVLog.ADSection> adList = p4ppvlog.getAdList();
		    
		    String productType="";
		    if(adList!=null)
		    {
			    for (P4PPVLog.ADSection adsec: adList) {
				    productType= adsec.getProductType();
				    break;
				}
		    }

			 String pvtype=String.valueOf(p4ppvlog.getAdzoneCategory());
			return  new String[] {
					String.valueOf(ColsDefine.formatDay.format(d)),
					String.valueOf(ColsDefine.formatMin.format(d)),
					String.valueOf((pvtype.isEmpty()||pvtype.equals("0")||pvtype.equals("2")||pvtype.equals("1"))?"1":pvtype),
					String.valueOf(productType),
					String.valueOf(p4ppvlog.getP4PPID()),
					String.valueOf(bucketId), "p4p_wt_itempv", "", ColsDefine.version,
					seaBucketid,"" };
		}
		
		@Override
		public Number[] getSum() {
			return new Number[] { 1, 0, 0, 0, 0, 0 , 0,0 };
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
