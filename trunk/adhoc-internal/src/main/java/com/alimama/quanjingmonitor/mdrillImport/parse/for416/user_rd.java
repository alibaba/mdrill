package com.alimama.quanjingmonitor.mdrillImport.parse.for416;

import java.util.Date;

import org.apache.log4j.Logger;

import com.alimama.mdrillImport.InvalidEntryException;

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
			
//			2.1     1397048787              e8c48f82dd846b6b4e1d493c914891d2        mm_31791272_3084562_21540303    1       182.118.21.1180       laiwang http://redirect.simba.taobao.com/rd?w=laiwang&f=http%3A%2F%2Fm.laiwang.com%2Fmarket%2Flaiwang%2Fevent-square.php%3Feventid%3D3346308%26lwfrom%3D20140409235959005&p=mm_31791272_3084562_21540303&k=9e1261a0d26a4bed http://m.laiwang.com/market/laiwang/event-square.php?eventid=3346308&lwfrom=20140409235959005       0                                       20140409
//				2.1     1397049262              af005c4080cd028b9815ff0db0a7a8cf        mm_31791272_3084562_21538328    1       182.118.21.1200       laiwang http://redirect.simba.taobao.com/rd?w=laiwang&f=http%3A%2F%2Fm.laiwang.com%2Fmarket%2Flaiwang%2Fevent-square.php%3Feventid%3D3563080%26lwfrom%3D20140409235959008&p=mm_31791272_3084562_21538328&k=9e1261a0d26a4bed http://m.laiwang.com/market/laiwang/event-square.php?eventid=3563080&lwfrom=20140409235959008       0                                       20140409
			
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
			return  new Number[]{
				0
				,1
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
			 long ts = Long.parseLong(pvlog[1]);
			 return (ts/10)*10000;
		}
		

	    
		@Override
		public Object[] getGroup() {
			 long ts = Long.parseLong(pvlog[1]);
			 long ts300=(ts/300)*300000;
			 Date d= new Date(ts300);
			 
			
			return new String[] { 
					String.valueOf(ColsDefine.formatDay.format(d)),
					String.valueOf(ColsDefine.formatMin.format(d)),
					"pc",
					"user_rd", 
					String.valueOf(pvlog[4]) 
					, "pc"
					,String.valueOf(ColsDefine.getName(pvlog[11], "tid"))//target
					,""
					,ColsDefine.version
			};

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
