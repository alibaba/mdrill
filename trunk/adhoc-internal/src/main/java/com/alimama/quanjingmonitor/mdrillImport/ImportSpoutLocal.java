package com.alimama.quanjingmonitor.mdrillImport;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.alimama.quanjingmonitor.topology.SpoutUtils;
import com.alimama.quanjingmonitor.topology.TimeOutCheck;
import com.alimama.quanjingmonitor.tt.TTReader;
import com.alipay.bluewhale.core.utils.StormUtils;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichSpout;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;

public class ImportSpoutLocal implements IRichSpout{
	private static Logger LOG = Logger.getLogger(ImportSpoutLocal.class);

    private static final long serialVersionUID = 1L;
    private ImportReader reader=null;
    private DataParser parse=null;
    private String confPrefix;
	private mdrillCommit commit=null;//new LastTimeBolt();

    public ImportSpoutLocal(String confPrefix)
    {
    	this.confPrefix=confPrefix;
    }
	private HashMap<BoltStatKey, BoltStatVal> nolockbuffer=null;

    int buffersize=5000;
    @Override
    public void open(Map conf, TopologyContext context,SpoutOutputCollector collector) {
    	try {
			parse=(DataParser) Class.forName(String.valueOf(conf.get(this.confPrefix+"-parse"))).newInstance();
		} catch (Throwable e1) {
			LOG.error("DataParser",e1);
		}
    	this.commit=new mdrillCommit(parse,conf,this.confPrefix);

		int timeout=Integer.parseInt(String.valueOf(conf.get(confPrefix+"-timeoutSpout")));
		this.buffersize=Integer.parseInt(String.valueOf(conf.get(confPrefix+"-spoutbuffer")));

    	this.timeoutCheck=new TimeOutCheck(timeout*1000l);
    	this.status=new SpoutStatus();
    	int readerCount=context.getComponentTasks(context.getThisComponentId()).size();
    	int readerIndex=context.getThisTaskIndex();
    	nolockbuffer=new HashMap<BoltStatKey, BoltStatVal>(this.buffersize);
    	try {
			this.reader=new ImportReader(conf, confPrefix, parse, readerIndex, readerCount);
		} catch (IOException e) {
			LOG.error("TTReader",e);
		}
    }
    

	
	private SpoutStatus status=null;
	private TimeOutCheck timeoutCheck=null;
	
    private static SimpleDateFormat formatHour = new SimpleDateFormat("HH:mm:ss");

	private synchronized boolean putdata(DataParser.DataIter log)
	{	
    	long ts=log.getTs();
		BoltStatKey key=new BoltStatKey(log.getGroup(),ts);
		BoltStatVal val=new BoltStatVal(log.getSum());
		
		BoltStatVal statval=nolockbuffer.get(key);
		this.status.ttInput++;
    	if(statval==null)
    	{
    		nolockbuffer.put(key, val);
    		this.status.groupCreate++;
    	}else{
    		statval.merger(val);
    	}
    	
    	if((this.status.ttInput%1000!=0))
    	{
    		return false;
    	}
    	
    	if(!this.timeoutCheck.istimeout()&&nolockbuffer.size()<buffersize&&nolockbuffer.size()<buffersize)
		{
    		return false;
		}
    	this.timeoutCheck.reset();

		HashMap<BoltStatKey, BoltStatVal> buffer=nolockbuffer;
    	LOG.info("####total:group="+buffer.size()+",ts:"+formatHour.format(new Date(ts))+",status:"+this.status.toString());
    	nolockbuffer=new HashMap<BoltStatKey, BoltStatVal>(buffersize);

		this.commit.updateAll(buffer,key.getGroupts());
		if(this.status.ttInput>100000000)
		{
			this.status.reset();
		}
	    
	    return true;
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
					this.status.ttInput++;
					DataParser.DataIter pv=(DataParser.DataIter)o;
					while(true)
					{
						this.status.groupInput++;
						if(this.putdata(pv))
						{
							isover=true;
						}
						if(!pv.next())
						{
							break;
						}
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
