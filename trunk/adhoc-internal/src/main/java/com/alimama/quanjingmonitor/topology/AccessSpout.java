package com.alimama.quanjingmonitor.topology;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;


import com.alimama.quanjingmonitor.parser.AccessLogParser;
import com.alimama.quanjingmonitor.tt.TTReader;
import com.alipay.bluewhale.core.utils.StormUtils;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichSpout;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;

public class AccessSpout implements IRichSpout{
	private static Logger LOG = Logger.getLogger(AccessSpout.class);

    private static final long serialVersionUID = 1L;

    private String confPrefix;
    public AccessSpout(String confPrefix)
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
			this.reader=new TTReader(conf, confPrefix, new AccessLogParser(), readerIndex, readerCount);
		} catch (IOException e) {
		}
    }
    
    private static SimpleDateFormat formatHour = new SimpleDateFormat("HH:mm:ss");

	private HashMap<BoltStatKey, AccessStatVal> nolockbuffer=new HashMap<BoltStatKey, AccessStatVal>(BUFFER_SIZE);
	private PvSpoutStat stat=null;
	private TimeOutCheck timeoutCheck=null;
	
	private static  int BUFFER_SIZE=10000;



	private boolean putdata(AccessLogParser.AccesLog pv)
	{
		boolean isover=false;
		
		String pidlist=pv.pid;
    	if(pidlist==null||pidlist.isEmpty()||pv.name==null||pv.ts<1||pv.rt<=0)
    	{
    		this.stat.pidnull++;
    		return isover;
    	}
    	
    	
	    long ts = pv.ts;
	    long ts_300=(ts/60000)*60000;

	    String[] pids=pidlist.split(",",-1);
	    for(String pid:pids)
	    {
	    	if(pid.isEmpty())
	    	{
	    		continue;
	    	}
	    	this.stat.lastsize++;
	    	BoltStatKey key=new BoltStatKey(3);
	    	key.list[0]=ts_300;
	    	key.list[1]=pid;
	    	key.list[2]=pv.name;
	    	
		    	AccessStatVal statval=nolockbuffer.get(key);
		    	if(statval==null)
		    	{
		    		statval=new AccessStatVal();
		    		nolockbuffer.put(key, statval);
		    	}
		    	statval.add(pv.rt);
	    }
	    
    	this.stat.index++;
    	if((this.stat.index%1000!=0))
    	{
    		return isover;
    	}
    	

	    	if(!this.timeoutCheck.istimeout()&&nolockbuffer.size()<BUFFER_SIZE)
			{
	    		return isover;
			}
			this.timeoutCheck.reset();

    	
		isover=true;

		
		HashMap<BoltStatKey, AccessStatVal> buffer=null;
			buffer=nolockbuffer;
			LOG.info("####total:buffer.size="+buffer.size()+",ts:"+formatHour.format(new Date(ts))+",stat:"+this.stat.toString());
			nolockbuffer=new HashMap<BoltStatKey, AccessStatVal>(BUFFER_SIZE);
		this.stat.lastsize=0;
		for(Entry<BoltStatKey, AccessStatVal> e:buffer.entrySet())
		{
			BoltStatKey pkey=e.getKey();
			long result=pkey.list[0].hashCode();
			result=result*31+pkey.list[1].hashCode();
			result=result*31+pkey.list[2].hashCode();
    		List<Object> data = StormUtils.mk_list((Object)pkey,e.getValue(),result);
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
					AccessLogParser.AccesLog pv=(AccessLogParser.AccesLog)o;
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
        declarer.declare(new Fields("key","value","hashkey"));
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        Map<String, Object> rtnmap = new HashMap<String, Object>();
        return rtnmap;
    }

}
