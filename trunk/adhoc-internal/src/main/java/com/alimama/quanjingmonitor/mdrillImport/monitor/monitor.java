package com.alimama.quanjingmonitor.mdrillImport.monitor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import com.alimama.mdrill.jdbc.MdrillQueryResultSet;
import com.alimama.mdrill.utils.TryLockFile;

public class monitor {
	

	public static void main(String[] args) throws SQLException, ClassNotFoundException {

		String path=args[1];
		String lockname=args[2];
		
		String stormhome = System.getProperty("storm.home");
		if (stormhome == null) {
			stormhome=".";
		}
		
		String lockPathBase=stormhome+"/lock";
		File file = new File(lockPathBase);
		file.mkdirs();
		TryLockFile flock=new TryLockFile(lockPathBase+"/"+lockname);
		
		flock.trylock();
		
		execute(path);
		
		flock.unlock();
	}
	
	
	public static class PidInfo{
		public PidInfo(String pid, String[] mailto, String[] wangwang,
				double overrate) {
			super();
			this.pid = pid;
			this.mailto = mailto;
			this.wangwang = wangwang;
			this.overrate = overrate;
		}
		public String pid;
		public String[] mailto;
		public String[] wangwang;
		public double overrate=0.5;
	}
	
	
	public static class PidStat{
		
		public String arrayToString(String[] d)
		{
			StringBuffer buff=new StringBuffer();
			String join ="";
			for(Object s:d)
			{
				buff.append(join).append(s);
				join=",";
			}
			
			return buff.toString();
		}
		
		public String arrayToString(double[] d)
		{
			StringBuffer buff=new StringBuffer();
			String join ="";
			for(double s:d)
			{
				buff.append(join).append((long)s);
				join=",";
			}
			
			return buff.toString();
		}
		public String printIsOver(double overrate,double[] d)
		{
			boolean isover=this.isover(overrate, d);
			if(isover)
			{
				return "<font color='red'>"+arrayToString(d)+"</font>";
			}
			return arrayToString(d);
		}
		public String print(PidInfo info) {
			return "<tr><td>"+start + "</td><td>" + end + "</td><td>" + info.pid+ "</td><td>" + info.overrate
					+ "</td><td>" + arrayToString(dayList) + "</td>" +
							"<td>"+printIsOver(info.overrate,this.p4pprice) + "</td>" +
							"<td>"+printIsOver(info.overrate,this.p4pclick) + "</td>" +
							"<td>"+printIsOver(info.overrate,this.p4ppv) + "</td><td>"+printIsOver(info.overrate,this.p4pctr) + "</td></tr>";
		}
		
		public String printlog(PidInfo info) {
			return start + "\t" + end + "\t" + info.pid+ "\t" + info.overrate
					+ "\t" + arrayToString(dayList) + "\t"
					+ arrayToString(p4pprice)+"\t"+isover(info.overrate,this.p4pprice) + "\t"
					+ arrayToString(p4pclick)+"\t"+isover(info.overrate,this.p4pclick) + "\t"
					+ arrayToString(aclick)+"\t"+isover(info.overrate,this.aclick) + "\t" 
					+ arrayToString(apv)+"\t"+isover(info.overrate,this.apv)
					+ "\t" + arrayToString(p4ppv)+"\t"+isover(info.overrate,this.p4ppv)+"\t"+isover(info.overrate,this.p4pctr) + "";
		}
		String start;
		String end;
		String[] dayList;
		double[] p4pprice;
		double[] p4pclick;
		double[] aclick;
		double[] apv;
		double[] p4ppv;
		
		double[] p4pctr;
		
		public void makectr()
		{
			this.p4pctr=new double[this.dayList.length];
			
			for(int i=0;i<this.dayList.length;i++)
			{
				this.p4pctr[i]=0;
				if(this.p4ppv[i]>0&&this.p4ppv[i]>this.p4pclick[i])
				{
					this.p4pctr[i]=this.p4pclick[i]*100/this.p4ppv[i];
				}
			}
		}
		
		public boolean isover(PidInfo info)
		{
			if(isover(info.overrate,this.p4pprice))
			{
				return true;
			}
			
			if(isover(info.overrate,this.p4pclick))
			{
				return true;
			}
			
			if(isover(info.overrate,this.p4pctr))
			{
				return true;
			}
//			
//			if(isover(info.overrate,this.aclick))
//			{
//				return true;
//			}
//			
//			if(isover(info.overrate,this.apv))
//			{
//				return true;
//			}
			
			if(isover(info.overrate,this.p4ppv))
			{
				return true;
			}
			
			return false;
		}
		public boolean isover(double overrate,double[] d)
		{
			double base=d[0];
			boolean isset=false;
			for(int i=1;i<d.length;i++)
			{
				if(d[i]<=5)
				{
					continue;
				}
				isset=true;
				double diff=Math.abs(base-d[i])/(0.001+d[i]);
				double diff2=Math.abs(base-d[i])/(0.001+base);

				if(diff<overrate&&diff2<overrate)
				{
					return false;
				}
			}
			
			return isset;
			
		}
	}
	
	 public static SimpleDateFormat formatDay = new SimpleDateFormat("yyyyMMdd");

	 public static SimpleDateFormat formatMin = new SimpleDateFormat("HHmm");
	 
	 public static SimpleDateFormat formatDayHHMM = new SimpleDateFormat("yyyyMMdd_HHmm");

	
	public static HashMap<String,PidStat> request(long ts,HashMap<String,PidInfo> pidinfo,ArrayList<String> pidlist) throws SQLException, ClassNotFoundException
	{
		Class.forName("com.alimama.mdrill.jdbc.MdrillDriver");
		
		HashMap<String,PidStat> rtn=new HashMap<String, PidStat>();
		
		long delaymin=1800;
		String[] dayList={
				formatDay.format(new Date(ts-1000l*delaymin))
				,formatDay.format(new Date(ts-1000l*delaymin-1000l*3600*24*1))
				,formatDay.format(new Date(ts-1000l*delaymin-1000l*3600*24*3))
				,formatDay.format(new Date(ts-1000l*delaymin-1000l*3600*24*7))
				};
		
		HashMap<String,Integer> dayIndex=new HashMap<String, Integer>();
		for(int i=0;i<dayList.length;i++)
		{
			dayIndex.put(dayList[i], i);
		}
		
		String start=formatMin.format(new Date(ts-1000l*delaymin-1000l*3600));
		String end=formatMin.format(new Date(ts-1000l*delaymin));

		StringBuffer pidliststr=new StringBuffer();
		String join="";
		for(String pid:pidlist)
		{
			pidliststr.append(join).append("'"+pid+"'");
			join=",";
		}
		
		String sql="select " +
				" thedate,pid,sum(p4pprice) as p4pprice,sum(p4pclick) as p4pclick,sum(aclick) as aclick,sum(apv) as apv,sum(p4ppv) as p4ppv " +
				" from rpt_quanjing_p4p_k2_realtime where thedate in ('"+dayList[0]+"','"+dayList[1]+"','"+dayList[2]+"','"+dayList[3]+"') " +
				" and pid in ("+pidliststr+") and miniute_5>='"+start+"' and miniute_5<='"+end+"' group by thedate,pid limit 0,8000 ";

		Connection con = DriverManager.getConnection("jdbc:mdrill://adhoc7.kgb.cm6:9999", "", "");

		Statement stmt = con.createStatement();
		

			MdrillQueryResultSet res = null;
		
			res = (MdrillQueryResultSet) stmt.executeQuery(sql);
	
			while (res.next()) {
				String thedate=res.getString("thedate");
				String pid=res.getString("pid");
				int index=dayIndex.get(thedate);
				
				PidStat stat=rtn.get(pid);
				if(stat==null)
				{
					stat=new PidStat();
					stat.start=start;
					stat.end=end;
					stat.dayList=dayList;
					stat.p4pprice=new double[dayList.length];
					stat.p4pclick=new double[dayList.length];
					stat.apv=new double[dayList.length];
					stat.p4ppv=new double[dayList.length];
					stat.aclick=new double[dayList.length];
					rtn.put(pid, stat);
				}
				
				stat.p4pprice[index]=Double.parseDouble(res.getString("p4pprice"));
				stat.p4pclick[index]=Double.parseDouble(res.getString("p4pclick"));
				stat.apv[index]=Double.parseDouble(res.getString("apv"));
				stat.p4ppv[index]=Double.parseDouble(res.getString("p4ppv"));
				stat.aclick[index]=Double.parseDouble(res.getString("p4pprice"));
			}
			con.close();
			
			return rtn;
	}
	
	public static void Request(StringBuffer urlFormat ) throws IOException
	{
System.out.println(urlFormat.toString());
        URL u = new URL("http://mon.alibaba-inc.com/noticenter/send.do");
        HttpURLConnection con = (HttpURLConnection) u.openConnection();
        con.setRequestMethod("POST");
        con.setDoOutput(true);
        con.setDoInput(true);
        con.setUseCaches(false);
        con.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
        OutputStreamWriter osw = new OutputStreamWriter(con.getOutputStream(),"UTF-8");
        osw.write(urlFormat.toString());
        osw.flush();
        osw.close();
        
        InputStreamReader urlStream = new InputStreamReader(con.getInputStream(), "utf-8");
        BufferedReader in = new BufferedReader(urlStream);
        int bufferlen = 1;
        char[] buffer = new char[bufferlen];
        int readBytes = 0;

        while (true) {
                readBytes = in.read(buffer, 0, bufferlen);
                if (readBytes < 0) {
                        break;
                }
          }
        in.close();
        urlStream.close();
        con.disconnect();
	}
	
	public static void mointorBatch(HashMap<String,StringBuffer> mailTo,HashMap<String,StringBuffer> wangwangTo,long ts,HashMap<String,PidInfo> pidinfo,ArrayList<String> pidlist) throws SQLException, ClassNotFoundException
	{
		HashMap<String,PidStat> requestData=request(ts, pidinfo, pidlist);
		for(Entry<String,PidStat> e:requestData.entrySet())
		{
			String pid=e.getKey();
			PidInfo info=pidinfo.get(pid);
			PidStat stat=e.getValue();
			stat.makectr();

			String loginfo="can`t found pid info "+pid;
			if(info!=null)
			{
				loginfo=stat.printlog(info);
				if(stat.isover(info))
				{
					for(String mail:info.mailto)
					{
						StringBuffer buffer=mailTo.get(mail);
						if(buffer==null)
						{
							buffer=new StringBuffer();
							mailTo.put(mail, buffer);
						}
						
						buffer.append(stat.print(info)).append("\r\n");
					}
					
					for(String wangwang:info.wangwang)
					{
						StringBuffer buffer=wangwangTo.get(wangwang);
						if(buffer==null)
						{
							buffer=new StringBuffer();
							wangwangTo.put(wangwang, buffer);
						}
						
						buffer.append(pid).append("\r\n");
					}
				}
			}
			System.out.println(loginfo);
		}

	}
	
	public static void execute(String filePath) throws SQLException, ClassNotFoundException
	{
		HashMap<String,StringBuffer> mailTo=new HashMap<String,StringBuffer>();
		HashMap<String,StringBuffer> wangwangTo=new HashMap<String,StringBuffer>();
		long ts=System.currentTimeMillis();
		HashMap<String,PidInfo> pidinfo=getPidInfo(filePath);
		ArrayList<String> pidlist=new ArrayList<String>();
		for(Entry<String,PidInfo> e:pidinfo.entrySet())
		{
			pidlist.add(e.getKey());
			if(pidlist.size()>49)
			{
				mointorBatch(mailTo,wangwangTo,ts,pidinfo, pidlist);
				pidlist.clear();
			}
		}
		
		if(pidlist.size()>0)
		{
			mointorBatch(mailTo,wangwangTo,ts,pidinfo, pidlist);
			pidlist.clear();
		}
		
		String day_ts=formatDayHHMM.format(new Date(ts));
		for(Entry<String,StringBuffer> e:wangwangTo.entrySet())
		{
			
			try {
				
				StringBuffer contentw=new StringBuffer();

				contentw.append(" <br>");
				contentw.append(e.getValue().toString());
				String content=contentw.toString();
				if(content.length()>150)
				{
					content=content.substring(0,150)+"...";
				}
				
				String subject= URLEncoder.encode("【全景监控】异常的refpid报警["+day_ts+"]" ,"utf8");
				String msg = URLEncoder.encode(content,"utf8");
				String wname=URLEncoder.encode(e.getKey() ,"utf8");
				
				StringBuffer urlFormat = new StringBuffer();;
				urlFormat.append("receiver="+wname);
				urlFormat.append("&subtitle="+subject);
				urlFormat.append("&message="+msg);
				urlFormat.append("&method="+URLEncoder.encode("wang-alert" ,"utf8"));
				urlFormat.append("&username="+URLEncoder.encode("yannian.mu" ,"utf8"));
				urlFormat.append("&password="+getMD5((new String("yannian.mu@1106"+e.getKey())).getBytes("utf8")));
				urlFormat.append("&charset=utf8");

				
				Request(urlFormat);
				
			} catch (Exception e2) {
			}
		}
		
		
		for(Entry<String,StringBuffer> e:mailTo.entrySet())
		{
			
			try {
				StringBuffer contentw=new StringBuffer();

				contentw.append(" 全景监控访问地址：http://quanjing.alimama.com:9999/quanjing/goldeye_realtime.jsp <br>");
				contentw.append("<table width='100%' border='1' cellspacing='0' cellpadding='0'>");
				contentw.append("<tr><td>开始</td><td>结束</td><td>pid</td><td>报警阈值</td><td>日期</td><td>p4p消耗</td><td>p4p点击</td><td>p4pPV</td><td>p4p点击率%</td></tr>");
				contentw.append(e.getValue().toString());
				contentw.append("</table>");
				StringBuffer urlFormat = new StringBuffer();;
				urlFormat.append("receiver="+URLEncoder.encode(e.getKey() ,"utf8"));
				urlFormat.append("&subtitle="+URLEncoder.encode("【全景监控】您有异常的refpid需要处理["+day_ts+"]" ,"utf8"));
				urlFormat.append("&message="+URLEncoder.encode(contentw.toString() ,"utf8"));
				urlFormat.append("&method="+URLEncoder.encode("mail" ,"utf8"));
				urlFormat.append("&username="+URLEncoder.encode("yannian.mu" ,"utf8"));
				urlFormat.append("&password="+getMD5((new String("yannian.mu@1106"+e.getKey())).getBytes("utf8")));
				urlFormat.append("&charset=utf8");
				
				
				
				Request(urlFormat);
				
				
			} catch (Exception e2) {
			}
		}
		

	}
	
	public static String  getMD5(byte[] bytes) { 
        char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' }; 
        char str[] = new char[16 * 2]; 
        try { 
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5"); 
            md.update(bytes); 
            byte tmp[] = md.digest(); 
            int k = 0; 
            for (int i = 0; i < 16; i++) { 
                byte byte0 = tmp[i]; 
                str[k++] = hexDigits[byte0 >>> 4 & 0xf]; 
                str[k++] = hexDigits[byte0 & 0xf]; 
            } 
        } catch (Exception e) { 
        } 
        return new String(str); 
    } ;
	
	public static HashMap<String,PidInfo> getPidInfo(String filePath)
	{
		HashMap<String,PidInfo> pidinfo=new HashMap<String, PidInfo>();
		
		File file = new File(filePath);
		if (file.isFile() && file.exists()) {
			try {
				InputStreamReader read = new InputStreamReader(	new FileInputStream(file), "UTF-8");
				BufferedReader reader = new BufferedReader(read);
				String line;
				try {
					while ((line = reader.readLine()) != null) {
						String[] cols=line.split("\t");
						if(cols.length<4)
						{
							continue;
						}
						
						String pid=cols[0];
						String[] mailto=cols[1].split(",");
						String[] wangwang=cols[2].split(",");
						double overrate=Double.parseDouble(cols[3]);
						PidInfo info=new PidInfo(pid, mailto, wangwang, overrate);
						pidinfo.put(pid, info);
					
					}
					reader.close();
					read.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		return pidinfo;
		}

}
