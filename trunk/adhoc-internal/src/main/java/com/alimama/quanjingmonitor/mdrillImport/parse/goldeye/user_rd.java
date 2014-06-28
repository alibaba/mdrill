package com.alimama.quanjingmonitor.mdrillImport.parse.goldeye;

import java.util.Date;

import org.apache.log4j.Logger;

import com.alimama.mdrillImport.InvalidEntryException;

/**
 * 2. user_rd：一跳点击日志；
   /group/tbads/logdata/user_rd/$date
   对应天表：s_ods_log_cps_combine_source
   对应小时表：s_ods_log_cps_combine_source_hour
   PID  ----第5个字段
   BUCKETID    ----target目标URL中的sbid（同一跳PV中的abtag）

 * @author yannian.mu
 *
 */
public class user_rd extends com.alimama.mdrillImport.DataParser{
	private static final long serialVersionUID = 1L;
	public volatile long groupCreateerror=0;

	private static Logger LOG = Logger.getLogger(user_rd.class);

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
			
			
			if(clicklog[4].isEmpty()||clicklog[1].isEmpty()||clicklog[1].length()<=5||clicklog[4].length()>100)
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
		public Number[] getSum() {			return new Number[] { 0, 1, 0, 0, 0, 0 , 0 ,0};
}
		
		@Override
		public long getTs() {
			 long ts = Long.parseLong(pvlog[1]);
			 return (ts/10)*10000;
		}
		

	    
		@Override
		public Object[] getGroup() {
			 long ts = Long.parseLong(pvlog[1]);
			 long ts300=(ts/300)*300000;
			 Date d= new Date(ts300);
			 
			 String bucketId = "";
			String seaBucketid = "";
			try{
			String tagFields[] = ColsDefine.getName(pvlog[11], "sbid").split(";",3);
			if ( tagFields.length >= 2 ){
				bucketId = tagFields[0];
				seaBucketid =tagFields[1];
			}
			}catch(Throwable e){
				
			}
			
		 

			 
			return  new String[] {
					String.valueOf(ColsDefine.formatDay.format(d)),
					String.valueOf(ColsDefine.formatMin.format(d)),
					String.valueOf(String.valueOf(pvlog[9]).indexOf("mmp4ptest")>=0?"1":""),
					String.valueOf(""),
					String.valueOf(pvlog[4]),
					String.valueOf(bucketId), "user_rd", "", ColsDefine.version,
					seaBucketid,"" };
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
