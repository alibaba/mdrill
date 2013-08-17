package com.alipay.bluewhale.core.cluster;

import java.io.Serializable;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import com.alipay.bluewhale.core.work.Worker;


public class SolrInfo implements Serializable{
	public static class ShardCount implements Serializable{
		private static final long serialVersionUID = 1L;
		public Long cnt;
		public ShardCount(Long cnt) {
			super();
			this.cnt = cnt;
		}
		Long times=(new Date()).getTime();
		@Override
		public String toString() {
			SimpleDateFormat fmt=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String yyyymmmddd=fmt.format(new Date(times));
			return "{cnt:" + cnt + ", times:" + yyyymmmddd + "}<br>";
		}
	}
	
    private static final long serialVersionUID = 1L;
    public String localip;
    public String localpath;
    public String hdfsPath;
    public String hdfsfolder;
    public Integer port;
    public Long times;
    public Long startTimes;
    public Long solrhbTimes;
    public ShardsState stat;

    public Integer workport;
    
    public String hostname;

    public HashMap<String,ShardCount> recorecount = new HashMap<String, ShardCount>();
    public HashMap<String,ShardCount> daycount = new HashMap<String, ShardCount>();

    public String memInfo=MemInfo.getInfo(1024*1024);
    public Boolean isMergeServer=false;
    public Boolean isRealTime=false;
    
    public String processId=pid();
   
    private static String pid()
    {
    	  String processName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
    	   
          String processID = processName.substring(0,processName.indexOf('@'));
     
          return processID;
    }

    public SolrInfo(boolean isRealTime, String localpath, String hdfsPath,
            String hdfsfolder, Integer port, ShardsState stat,HashMap<String,ShardCount> count,HashMap<String,ShardCount> daycount,Long starttimes,Long solrhbTimes,Boolean isMergeServer) throws UnknownHostException {
	super();
	this.localip = java.net.InetAddress.getLocalHost().getHostAddress();;
	this.localpath = localpath;
	this.hdfsPath = hdfsPath;
	this.hdfsfolder = hdfsfolder;
	this.port = port;
	this.times = System.currentTimeMillis();
	this.stat=stat;
	this.workport=Worker.getWorkPort();
	this.recorecount=count;
	this.daycount=daycount;
	this.startTimes=starttimes;
	this.hostname=String.valueOf(java.net.InetAddress.getLocalHost().getHostName());
	this.solrhbTimes=solrhbTimes;
	this.memInfo=MemInfo.getInfo(1024*1024);
	this.isMergeServer=isMergeServer;
	this.isRealTime=isRealTime;
    }
    
    @Override
    public String toString() {
	SimpleDateFormat fmt=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	String yyyymmmddd=fmt.format(new Date(times));
	String yyyymmmdddstart=fmt.format(new Date(startTimes));
	String yyyymmmdddhb=fmt.format(new Date(solrhbTimes));
	if(this.isMergeServer||this.isRealTime)
	{
		return this.isMergeServer+" "+this.isRealTime+" ["+this.hostname +"][" + localip + ":"+port+ "]["+this.stat+"] " +" ["+memInfo+"] " +" ["+yyyymmmdddhb+ "][" + yyyymmmddd + "][" + yyyymmmdddstart + "]  "+this.workport  +"<hr>";
	}
	
	
	String recorecountStr="";
	if(this.recorecount.size()>0)
	{
		recorecountStr=" <br> "+this.recorecount.toString();
	}
	String daycountStr="";
	if(this.daycount.size()>0)
	{
		daycountStr=" <br> "+this.daycount.toString();
	}
	
	
	
	
	return this.isMergeServer+" "+this.isRealTime+" ["+this.hostname +"][" + localip + ":"+port+ "]["+this.stat+"] " + hdfsfolder+" ["+memInfo+"] " +" ["+yyyymmmdddhb+ "][" + yyyymmmddd + "][" + yyyymmmdddstart + "]  "+this.workport +recorecountStr +daycountStr+"<br> [" + localpath+"]<br>["+this.hdfsPath+"]<hr>";
	
    }

}
