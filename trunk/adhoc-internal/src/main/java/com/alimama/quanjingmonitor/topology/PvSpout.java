package com.alimama.quanjingmonitor.topology;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;


import com.alimama.quanjingmonitor.parser.Armory;
import com.alimama.quanjingmonitor.parser.P4PPVLogParser;
import com.alimama.quanjingmonitor.parser.Armory.ArmoryItemInfo;
import com.alimama.quanjingmonitor.tt.TTReader;
import com.alipay.bluewhale.core.utils.StormUtils;
import com.taobao.loganalyzer.input.p4ppv.parser.P4PPVLog;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichSpout;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;

public class PvSpout implements IRichSpout{
	private static Logger LOG = Logger.getLogger(PvSpout.class);

    private static final long serialVersionUID = 1L;

    private String confPrefix;
    public PvSpout(String confPrefix)
    {
    	this.confPrefix=confPrefix;
    }
    private SpoutOutputCollector collector;
    
    private TTReader reader=null;
    @Override
    public void open(Map conf, TopologyContext context,SpoutOutputCollector collector) {
    	this.timeoutCheck=new TimeOutCheck(20*1000l);
    	this.stat=new PvSpoutStat();
    	this.collector = collector;
    	int readerCount=context.getComponentTasks(context.getThisComponentId()).size();
    	int readerIndex=context.getThisTaskIndex();
    	try {
			this.reader=new TTReader(conf, confPrefix, new P4PPVLogParser(), readerIndex, readerCount);
		} catch (IOException e) {
		}
    }
    
	private HashMap<BoltStatKey, BoltStatVal> nolockbuffer=new HashMap<BoltStatKey, BoltStatVal>(BUFFER_SIZE);
	private HashMap<BoltStatKey, BoltStatVal> nolockbuffer_machine=new HashMap<BoltStatKey, BoltStatVal>(BUFFER_SIZE);
	

	private PvSpoutStat stat=null;
	private TimeOutCheck timeoutCheck=null;
	
	private static  int BUFFER_SIZE=10000;

    private static SimpleDateFormat formatHour = new SimpleDateFormat("HH:mm:ss");
    private static final String LOG_ENCODING = "GBK";

    private static String decodeString(String args) {
		try {
			return new String(java.net.URLDecoder.decode(args, "GBK")
					.getBytes("UTF-8"), "UTF-8");
		} catch (Exception e) {
			return args;
		}
	}


    public static String getName(String url)
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
				if(key.equals("name"))
				{
					String value = (tem1.length < 2
							? "" : URLDecoder.decode(tem1[1], LOG_ENCODING));
					return value;
				}
			    } catch (UnsupportedEncodingException uee) {
			    }
			}
			
			if(tem.length<2)
			{
				return null;
			}
	
			String p=tem[0];
			int lastindex= p.lastIndexOf("/");
			if(lastindex>=0&&(lastindex+1)<p.length()&&p.indexOf("&")<0&&p.indexOf("=")<0)
			{
				String namestr= p.substring(lastindex+1);
				if(!namestr.isEmpty()&&namestr.indexOf("?")<0&&namestr.indexOf("/")<0)
				{
					return namestr;
				}
			}
    	}catch(Throwable e){}
	
		return null;
	 }
    
	private synchronized boolean putdata(P4PPVLog pv)
	{
		boolean isover=false;
		String namemodle=getName(pv.getQueryOriginURL());
		if(namemodle==null||namemodle.isEmpty()||namemodle.length()>50)
		{
			this.stat.interfacenull++;
			if(this.stat.interfacenull%10000==0)
			{
	    		LOG.info("interfacenull:"+pv.getQueryOriginURL());
			}
			return isover;
		}
		
		String pid=pv.getP4PPID();
    	if(pid==null||pid.isEmpty())
    	{
    		this.stat.pidnull++;
    		return isover;
    	}
	    String[] processList=String.valueOf(pv.getProcessPath()).split("-");
	    int onlyFirstAdd=1;
	    int onlyFirstAddhost=1;

	    long ts = Long.parseLong(pv.getTimestamp());
	    long ts_300=(ts/60)*60000;

	    HashSet<BoltStatKey> noreplication=new HashSet<BoltStatKey>(32);
	    HashSet<BoltStatKey> noreplicationMachine=new HashSet<BoltStatKey>(32);
	    for(String process:processList)
	    {   if(process==null||process.isEmpty())
		    {
	    		this.stat.processnull++;
		    	continue;
		    }
	    
	    	Armory.ArmoryInfo ainfo=Armory.getInfo(String.valueOf(process));

	    	for(ArmoryItemInfo item:ainfo.info)
		    {
	    		if(item.groupName==null||item.groupName.isEmpty())
	    		{
	    			this.stat.groupnameEmpty++;
	    			continue;
	    		}
		    	this.stat.lastsize++;
		    	
		    	BoltStatKey key=new BoltStatKey(4);
		    	key.isPidKey=true;
		    	key.list[0]=ts_300;
		    	key.list[1]=pid;
		    	key.list[2]=item.groupName;
		    	key.list[3]=namemodle;
		    	
		    	
		    	BoltStatKey host_key=new BoltStatKey(7);
		    	host_key.isPidKey=false;
		    	host_key.list[0]=ts_300;//ts
		    	host_key.list[1]=item.groupName;//pid
		    	host_key.list[2]=item.nodename;
		    	host_key.list[3]=item.dns_ip;
		    	host_key.list[4]=item.nodegroup;
		    	host_key.list[5]=item.product_name;
		    	host_key.list[6]=item.site;

	    		if(!noreplication.contains(key))
	    		{
	    			this.stat.lastsize_group_norep++;
	    			noreplication.add(key);
			    			
    				BoltStatVal statval=nolockbuffer.get(key);
			    	if(statval==null)
			    	{
			    		statval=new BoltStatVal();
			    		nolockbuffer.put(key, statval);
			    	}
			    	
				    statval.cnt+=onlyFirstAdd;
				    statval.cntnonclear+=1;
			    	onlyFirstAdd=0;
	    		}

		    	
		    	
		    	if(!noreplicationMachine.contains(host_key))
		    	{
			    	this.stat.lastsize_host_norep++;
		    		noreplicationMachine.add(host_key);
				    	BoltStatVal host_val=nolockbuffer_machine.get(host_key);
				    	if(host_val==null)
				    	{
				    		host_val=new BoltStatVal();
				    		nolockbuffer_machine.put(host_key, host_val);
				    	}
				    	
				    	host_val.cnt+=onlyFirstAddhost;
				    	host_val.cntnonclear+=1;
				    	onlyFirstAddhost=0;
		    	}
	    		
		    }
	    }
	    
	    
	    this.stat.index++;

    	if((this.stat.index%1000!=0))
    	{
    		return isover;
    	}
    	
	    	if(!this.timeoutCheck.istimeout()&&nolockbuffer.size()<BUFFER_SIZE&&nolockbuffer_machine.size()<BUFFER_SIZE)
			{
	    		return isover;
			}
    		this.timeoutCheck.reset();
		isover=true;

		HashMap<BoltStatKey, BoltStatVal> buffer=nolockbuffer;
		HashMap<BoltStatKey, BoltStatVal> buffer_machine=nolockbuffer_machine;
    	LOG.info("####total:buffer.size="+buffer.size()+"@"+buffer_machine.size()+",ts:"+formatHour.format(new Date(ts*1000))+",stat:"+this.stat.toString());
    	nolockbuffer=new HashMap<BoltStatKey, BoltStatVal>(BUFFER_SIZE);
    	nolockbuffer_machine=new HashMap<BoltStatKey, BoltStatVal>(BUFFER_SIZE);
		
		for(Entry<BoltStatKey, BoltStatVal> e:buffer.entrySet())
		{
			BoltStatKey pkey=e.getKey();
    		List<Object> data = StormUtils.mk_list((Object)pkey,e.getValue());
	        collector.emit(data, SpoutUtils.uuid());
		}
		
		for(Entry<BoltStatKey, BoltStatVal> e:buffer_machine.entrySet())
		{
			BoltStatKey pkey=e.getKey();
    		List<Object> data = StormUtils.mk_list((Object)pkey,e.getValue());
	        collector.emit(data, SpoutUtils.uuid());
		}
		
		this.stat.reset();
	    
	    return isover;
	}
	
    @Override
    public void nextTuple()  {
		try {
			for(int i=0;i<100;i++)
			{
				List ttdata = this.reader.read();
				if(ttdata==null)
				{
					return ;
				}
				boolean isover=false;
				for(Object o:ttdata)
				{
					this.stat.record++;
					P4PPVLog pv=(P4PPVLog)o;
					if(this.putdata(pv))
					{
						isover=true;
					}
				}
				if(isover)
				{
					return ;
				}
			}
		} catch (Throwable e) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {
			}
		}
    }

    @Override
    public void close() {
	
    }
        @Override
    public void ack(Object msgId) {
	
    }

    @Override
    public void fail(Object msgId) {
	
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("key","value"));
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        Map<String, Object> rtnmap = new HashMap<String, Object>();
        return rtnmap;
    }

}
