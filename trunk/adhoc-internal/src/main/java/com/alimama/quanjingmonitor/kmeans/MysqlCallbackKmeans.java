package com.alimama.quanjingmonitor.kmeans;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Date;

import org.apache.hadoop.conf.Configuration;
import org.apache.log4j.Logger;

import com.alimama.mdrill.adhoc.IOffilneDownloadCallBack;
import com.alimama.mdrill.adhoc.MySqlConn;
import com.alimama.mdrill.utils.HadoopBaseUtils;
import com.alimama.web.TableJoin;


public class MysqlCallbackKmeans implements IOffilneDownloadCallBack{
	private static Logger LOG = Logger.getLogger(MysqlCallbackKmeans.class);

	public MysqlCallbackKmeans(MySqlConn m_fpsql) {
		this.m_fpsql = m_fpsql;
	}
	private String memo = "";
	 public void setMemo(String memo) {
			this.memo = memo;
		}
	private boolean isfinish=false;
	private MySqlConn m_fpsql = null;
	private String mailto="yannian.mu@alipay.com";
	private String uuid="";
	public String getUuid() {
		return uuid;
	}
	
	public String cols="";

	public String getCols() {
		return cols;
	}

	public void setCols(String cols) {
		this.cols = cols;
	}

	private String hql=""; 
	private String md5=""; 
	private String[] cmd=new String[0];
	private String[] env=new String[0];
	private StringBuffer stdbuff=new StringBuffer();
	private StringBuffer stderror=new StringBuffer();
	private StringBuffer exception=new StringBuffer();
	private StringBuffer failedMsg=new StringBuffer();
	private int slotcount=0;
	private long resultKb=0l;
	private long rows=0l;
	private String percent="init";
	private String stage="40";
	private StringBuffer hadoopjobId=new StringBuffer();
	private int extval=0;
	private boolean isfail=false;
	private String username="";
	private String storedir="";
	private String jobname="";
	private String params="";
	private java.util.Date starttime=new java.util.Date();
	private java.util.Date endtime=new java.util.Date();
	private java.util.Date lastsynctime=new java.util.Date();;

	@Override
	public void init(String hql, String[] cmd, String[] env)  {
		synchronized (lock) {
		this.hql=hql;
		this.cmd=cmd;
		this.env=env;
		this.starttime=new java.util.Date();
		this.sync();
		}
	}
	
	public String getMailto() {
		return mailto;
	}

	public void setMailto(String mailto) {
		synchronized (lock) {
		this.mailto = mailto;
		}
	}

	@Override
	public void WriteStdOutMsg(String line) {
		synchronized (lock) {
		this.appendCut(this.stdbuff, line, 10000, 2);
		}
	}

	@Override
	public void WriteSTDERRORMsg(String line) {
		synchronized (lock) {
		this.appendCut(this.stderror, line, 10000, 2);
		}
	}
	
	private void appendCut(StringBuffer buff,String line,int num,int step)
	{
		buff.append(line+"\r\n");
		if(buff.length()>(num*step))
		{
			String cutstr=this.cutLast(buff.toString(), num);
			buff.setLength(0);
			buff.append(cutstr);
		}
	}

	@Override
	public void setSlotCount(int slotCount) {
		synchronized (lock) {
		this.slotcount=slotCount;
		}
	}

	@Override
	public void setResultKb(long kb) {
		synchronized (lock) {
		this.resultKb=kb;
		}
		
	}

	@Override
	public void setResultRows(long rows) {
		synchronized (lock) {
		this.rows=rows;
		}
		
	}

	
	double laspparcent=-1;
	
	@Override
	public void setPercent(String percent) {
		synchronized (lock) {
			
			double dpercent=TableJoin.parsePercent(String.valueOf(this.stage),percent,true);
			if(laspparcent<=dpercent)
			{
				laspparcent=dpercent;
				this.percent=percent;
			}

		}
	}
	
	@Override
	public void setStage(String stage) {
		synchronized (lock) {
		this.stage="60";//stage;
		}
	}
	
	

	@Override
	public void addJobId(String jobid) {
		synchronized (lock) {
		this.appendCut(this.hadoopjobId, jobid+",", 10000, 2);
		}

	}

	@Override
	public void setExitValue(int val) {
		synchronized (lock) {
		this.extval=val;
		if(this.extval!=0)
		{
			isfail=true;
		}
		}
		
	}

	@Override
	public void addException(String msg) {
		synchronized (lock) {
		this.appendCut(this.exception, msg, 10000, 2);
		}
	}

	@Override
	public void setFailed(String failmsg) {
		synchronized (lock) {
		this.appendCut(this.failedMsg, failmsg, 10000, 2);
		this.isfail=true;
		}
	}

	@Override
	public void maybeSync() {
		synchronized (lock) {
		Date now=new java.util.Date();;
		if(now.getTime()-this.lastsynctime.getTime()>10000)
		{
			this.lastsynctime=now;
			this.sync();
		}
		}
	}

	
	public String  getMD5(byte[] bytes) { 
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
    
     
  
	@Override
	public void finish() {
		
		LOG.info("abtest call finish begin");
		this.setPercent("Stage-"+this.stage+" map = 100.0%, reduce = 100.0%");
		
		synchronized (lock) {
		this.isfinish=true;
		this.endtime=new java.util.Date();
		this.sync();
		}
		
		LOG.info("abtest call finish end");

		
		String[] cols=this.mailto.split(";");
		
    	long sizekb=resultKb;
    	if(sizekb<=0)
    	{
			try {
				Configuration conf=new Configuration();
		    	HadoopBaseUtils.grabConfiguration(this.confdir, conf);
				sizekb = HadoopBaseUtils.size(this.storedir, conf)/1024;
			} catch (IOException e) {
			}
    	}
    		
		StringBuffer debug=new StringBuffer();
		if(cols.length>1)
		{
			for(String s:cols[1].split(","))
			{
				if(s.trim().isEmpty())
				{
					continue;
				}
				try {
					
					StringBuffer contentw=new StringBuffer();
					if(!this.isfail)
					{
						contentw.append(" <br>");
						contentw.append("亲爱的"+s+"，你在ad hoc的任务已经完成 <br>");
						contentw.append("任务名称："+this.jobname+"<br>");
						DecimalFormat df =new DecimalFormat("0.00");
					      double f = sizekb*1.0/1024;
//						contentw.append("结果大小："+(sizekb<=1?"小于1KB":df.format(f)+"MB")+"<br>\r\n");
//						String strurl="http://adhoc.etao.com:9999/downloadoffline.jsp?uuid="+this.getUuid();
						contentw.append("请进入邮箱查看<br>");

					}else{
						contentw.append(" <br>");
						contentw.append("Sorry，任务出错，请与管理员联系 <br>");
						contentw.append("任务名称："+this.jobname+"<br>");
						contentw.append("任务ID："+this.uuid+"<br>");
					}
					String subject= URLEncoder.encode(this.isfail?"adhoc任务执行失败":"adhoc任务执行成功" ,"utf8");
					String msg = URLEncoder.encode(contentw.toString(),"utf8");
					String wname=URLEncoder.encode(s ,"utf8");
					
					StringBuffer urlFormat = new StringBuffer();;
					urlFormat.append("receiver="+wname);
					urlFormat.append("&subtitle="+subject);
					urlFormat.append("&message="+msg);
					urlFormat.append("&method="+URLEncoder.encode("wang-alert" ,"utf8"));
					urlFormat.append("&username="+URLEncoder.encode("yannian.mu" ,"utf8"));
					urlFormat.append("&password="+this.getMD5((new String("yannian.mu@1106"+s)).getBytes("utf8")));
					urlFormat.append("&charset=utf8");

					
					debug.append(urlFormat.toString()+"\r\n");
					Request(urlFormat);
					
				} catch (Exception e) {
				}
			}
		}
		
		
		StringBuffer content=new StringBuffer();
		content.append("<table width=\"800\" border=\"1\" cellspacing=\"0\" cellpadding=\"1\" style=\"font-family:微软雅黑,宋体;font-size:12px;\">");
		if(!this.isfail)
		{
			content.append("<tr><td>任务名称："+this.jobname+"</tr></td>\r\n");
			content.append("<tr><td>数据记录："+this.rows+"条</tr></td>\r\n");
			
			DecimalFormat df =new DecimalFormat("0.00");
		      double f = sizekb*1.0/1024;
//			content.append("<tr><td>结果大小：<span style=\"color: #FF0000;font-weight: bold;\">"+(sizekb<=1?"小于1KB":df.format(f)+"MB")+"</span></tr></td>\r\n");
			content.append("<tr><td>执行时间："+((this.endtime.getTime()-this.starttime.getTime())/1000)+"秒</tr></td>\r\n");
			content.append("<tr><td>查询参数：<br>&nbsp;&nbsp;&nbsp;&nbsp;"+this.params.replaceAll("。", "<br>&nbsp;&nbsp;&nbsp;&nbsp;")+"</tr></td>\r\n");
			content.append("<tr><td>" +
					"" +
					"" +
					"" +
					"下载地址：<a href=\"http://quanjing.alimama.com:9999/downloadabtest.jsp?uuid="+this.getUuid()+"\">http://quanjing.alimama.com:9999/downloadabtest.jsp?uuid="+this.getUuid()+"</a>" +
					"</tr></td>\r\n");
			content.append("<tr><td>存储地址："+this.storedir+"</tr></td>\r\n");

		}else{
			content.append("<tr><td>任务名称："+this.jobname+"</tr></td>\r\n");
			content.append("<tr><td>任务ID："+this.uuid+"</tr></td>\r\n");
			content.append("<tr><td>hadoop任务ID："+this.hadoopjobId+"</tr></td>\r\n");
		}
		content.append("</table>");
		
		
		try{
		StringBuffer urlFormat = new StringBuffer();;
		urlFormat.append("receiver="+URLEncoder.encode(cols[0] ,"utf8"));
		urlFormat.append("&subtitle="+URLEncoder.encode(this.isfail?"【adhoc任务执行失败】- "+this.jobname:"【adhoc任务执行成功】- "+this.jobname ,"utf8"));
		urlFormat.append("&message="+URLEncoder.encode(content.toString() ,"utf8"));
		urlFormat.append("&method="+URLEncoder.encode("mail" ,"utf8"));
		urlFormat.append("&username="+URLEncoder.encode("yannian.mu" ,"utf8"));
		urlFormat.append("&password="+this.getMD5((new String("yannian.mu@1106"+cols[0])).getBytes("utf8")));
		urlFormat.append("&charset=utf8");
		
		
		debug.append(urlFormat.toString()+"\r\n");
		
		Request(urlFormat);

		} catch (Exception e) {
		}
		
		LOG.info(debug.toString());
	}
	
	public void Request(StringBuffer urlFormat ) throws IOException
	{

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

	@Override
	public boolean isfinished() {
		return this.isfinish;
	}
	
	
	@Override
	public void setUserName(String username) {
		synchronized (lock) {
		this.username=username;
		}
	}

	@Override
	public void setStoreDir(String store) {
		synchronized (lock) {
		this.storedir=store;
		}
		
	}

	@Override
	public void setName(String name) {
		synchronized (lock) {
		this.jobname=name;
		}
		
	}

	@Override
	public void setDisplayParams(String params) {
		synchronized (lock) {
		this.params=params;
		}
	}
	
	
	public void sync() {
		this.endtime=new java.util.Date();
		try {
			if (this.uuid.isEmpty()) {
				this.insert();
			} else {
				this.update();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 
	create table adhoc_download (
	  `id` bigint(20) NOT NULL AUTO_INCREMENT,
	  `isfinish` tinyint(4) NOT NULL DEFAULT '0' COMMENT '',
	  `cols` text  NOT NULL COMMENT '',
	  `mailto` text  NOT NULL COMMENT '',
	  `hql` text  NOT NULL COMMENT '',
	  `cmd` text  NOT NULL COMMENT '',
	  `env` text  NOT NULL COMMENT '',
	  `stdmsg` text  NOT NULL COMMENT '',
	  `errmsg` text  NOT NULL COMMENT '',
	  `exceptionmsg` text  NOT NULL COMMENT '',
	  `failmsg` text  NOT NULL COMMENT '',
	  `slotcount` bigint(20) default 0 COMMENT '',
	  `resultkb` bigint(20) default 0 COMMENT '',
	  `rows` bigint(20) default 0 COMMENT '',
	  `percent` text  NOT NULL COMMENT '',
	  `hadoopjobid` text  NOT NULL COMMENT '',
	  `extval` tinyint(4) NOT NULL DEFAULT '0' COMMENT '',
	  `isfail` tinyint(4) NOT NULL DEFAULT '0' COMMENT '',
	  `username` char(240) NOT NULL   COMMENT '',
	  `storedir` text NOT NULL  COMMENT '',
	  `jobname` char(240) NOT NULL DEFAULT '0' COMMENT '',
	  `params` text NOT NULL  COMMENT '',
	   `starttime` datetime default '1983-11-07 12:12:12'  COMMENT '',
	   `endtime` datetime default '1983-11-07 12:12:12'  COMMENT '',
	   `md5` char(240) NOT NULL   COMMENT '',
	   `stage` text  NOT NULL COMMENT '',
	   `uuid` char(240) NOT NULL DEFAULT '0' COMMENT '',
	  PRIMARY KEY (`id`),
  KEY `frontsearch` (`uuid`),
  INDEX `username` (`username`),
  INDEX `jobname` (`jobname`)
  ) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 ;
	 * @author yannian.mu
	 *
	 */
	private String insert() throws SQLException
	{
		this.uuid=java.util.UUID.randomUUID().toString();
		Connection conn = m_fpsql.getConn();
		String strSql = "insert into adhoc_download " +
				"(isfinish,cols,mailto,hql,cmd,env,stdmsg,errmsg,exceptionmsg,failmsg,slotcount,resultkb,rows" +
				",percent,hadoopjobid,extval,isfail,username,storedir,jobname,params,starttime,endtime,stage,md5,uuid,memo)" +
				"values" +
				"(?,?,?,?,?,?,?,?,?,?,?,?,?" +
				",?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		PreparedStatement m_fps = conn.prepareStatement(strSql);
		try {
			int index=1;
			m_fps.setInt(index++, this.isfinish?0:1);
			m_fps.setString(index++, cutLast(this.cols,1000));
			m_fps.setString(index++, cutLast(this.mailto,1000));
			m_fps.setString(index++, cutLast(this.hql,5000));
			m_fps.setString(index++, cutLast(Arrays.toString(this.cmd),5000));
			m_fps.setString(index++, cutLast(Arrays.toString(this.env),5000));
			m_fps.setString(index++,cutLast(stdbuff.toString(),5000));
			m_fps.setString(index++,cutLast(stderror.toString(),5000));
			m_fps.setString(index++,cutLast(exception.toString(),5000));
			m_fps.setString(index++,cutLast(failedMsg.toString(),5000));
			m_fps.setInt(index++, slotcount);
			m_fps.setLong(index++, resultKb);
			m_fps.setLong(index++, rows);
			m_fps.setString(index++, this.percent);
			m_fps.setString(index++, cutLast(this.hadoopjobId.toString(),1000));
			m_fps.setInt(index++, extval);
			m_fps.setInt(index++, isfail?1:0);
			m_fps.setString(index++, this.username);
			m_fps.setString(index++, this.storedir);
			m_fps.setString(index++, this.jobname);
			m_fps.setString(index++, this.params);
			m_fps.setTimestamp(index++, new java.sql.Timestamp(starttime.getTime()));
			m_fps.setTimestamp(index++, new java.sql.Timestamp(endtime.getTime()));
			m_fps.setString(index++, this.stage);
			m_fps.setString(index++, this.md5);
			m_fps.setString(index++, this.uuid);
			m_fps.setString(index++, this.memo);
			m_fps.executeUpdate();
			String fullstrSql=m_fps.toString();
			return fullstrSql;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("err:====>"+m_fps.toString());
			
		}finally{
			m_fps.close();
			m_fpsql.close();
		}
		return "";
	}
	
	
	private String cutLast(String str,int num)
	{
		int cutnum=Math.min(str.length(), num);
		return str.substring(str.length()-cutnum);
	}
	
	
	private String update() throws SQLException
	{
		Connection conn = m_fpsql.getConn();
		String strSql = "update adhoc_download set " +
				"isfinish=?,cols=?,mailto=?,hql=?,cmd=?,env=?,stdmsg=?,errmsg=?," +
				"exceptionmsg=?,failmsg=?,slotcount=?,resultkb=?,rows=?,percent=?," +
				"hadoopjobid=?,extval=?,isfail=?,username=?,storedir=?,jobname=?,params=?,starttime=?,endtime=?,stage=?,md5=? where uuid=? ";
		System.out.println(strSql);
		PreparedStatement m_fps = conn.prepareStatement(strSql);
		try {
			int index=1;
			m_fps.setInt(index++, this.isfinish?0:1);
			m_fps.setString(index++, cutLast(this.cols,1000));
			m_fps.setString(index++, cutLast(this.mailto,1000));
			m_fps.setString(index++, cutLast(this.hql,5000));
			m_fps.setString(index++, cutLast(Arrays.toString(this.cmd),5000));
			m_fps.setString(index++, cutLast(Arrays.toString(this.env),5000));
			m_fps.setString(index++,cutLast(stdbuff.toString(),5000));
			m_fps.setString(index++,cutLast(stderror.toString(),5000));
			m_fps.setString(index++,cutLast(exception.toString(),5000));
			m_fps.setString(index++,cutLast(failedMsg.toString(),5000));
			m_fps.setInt(index++, slotcount);
			m_fps.setLong(index++, resultKb);
			m_fps.setLong(index++, rows);
			m_fps.setString(index++, this.percent);
			m_fps.setString(index++, cutLast(this.hadoopjobId.toString(),1000));
			m_fps.setInt(index++, extval);
			m_fps.setInt(index++, isfail?1:0);
			m_fps.setString(index++, this.username);
			m_fps.setString(index++, this.storedir);
			m_fps.setString(index++, this.jobname);
			m_fps.setString(index++, this.params);
			m_fps.setTimestamp(index++, new java.sql.Timestamp(starttime.getTime()));
			m_fps.setTimestamp(index++, new java.sql.Timestamp(endtime.getTime()));
			m_fps.setString(index++, this.stage);
			m_fps.setString(index++, this.md5);
			m_fps.setString(index++, this.uuid);

			m_fps.executeUpdate();
			String fullstrSql=m_fps.toString();
			return fullstrSql;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("err:====>"+m_fps.toString());
		}finally{
			m_fps.close();
			m_fpsql.close();
		}
		return "";
	}

	@Override
	public void setSqlMd5(String md5) {
		synchronized (lock) {
		this.md5=md5;
		}
	}
	
	
	Object lock=new Object();
	
	@Override
	public String toString() {
		return "MysqlCallback [isfinish=" + isfinish + ", m_fpsql=" + m_fpsql
				+ ", mailto=" + mailto + ", uuid=" + uuid + ", hql=" + hql
				+ ", md5=" + md5 + ", cmd=" + Arrays.toString(cmd) + ", env="
				+ Arrays.toString(env) + ", stdbuff=" + stdbuff + ", stderror="
				+ stderror + ", exception=" + exception + ", failedMsg="
				+ failedMsg + ", slotcount=" + slotcount + ", resultKb="
				+ resultKb + ", rows=" + rows + ", percent=" + percent
				+ ", hadoopjobId=" + hadoopjobId + ", extval=" + extval
				+ ", isfail=" + isfail + ", username=" + username
				+ ", storedir=" + storedir + ", jobname=" + jobname
				+ ", params=" + params + ", starttime=" + starttime
				+ ", endtime=" + endtime + ", lastsynctime=" + lastsynctime
				+ "]";
	}

	String confdir="";
	@Override
	public void setConfdir(String line) {
		this.confdir=line;
	}

}
