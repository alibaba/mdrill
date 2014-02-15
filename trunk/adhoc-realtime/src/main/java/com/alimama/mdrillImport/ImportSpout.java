package com.alimama.mdrillImport;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.alipay.bluewhale.core.utils.StormUtils;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichSpout;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;

public class ImportSpout implements IRichSpout{
	private static Logger LOG = Logger.getLogger(ImportSpout.class);

    private static final long serialVersionUID = 1L;
    private SpoutOutputCollector collector;
    private ImportReader reader=null;
    private DataParser parse=null;
    private String confPrefix;
    public ImportSpout(String confPrefix)
    {
    	this.confPrefix=confPrefix;
    }

    int buffersize=5000;
    int intMapsize=500;
    
    @Override
    public void open(Map conf, TopologyContext context,SpoutOutputCollector collector) {
    	try {
			parse=(DataParser) Class.forName(String.valueOf(conf.get(this.confPrefix+"-parse"))).newInstance();
			parse.init(true, conf, context);
		} catch (Throwable e1) {
			LOG.error("DataParser",e1);
		}
		int timeout=Integer.parseInt(String.valueOf(conf.get(confPrefix+"-timeoutSpout")));
		this.buffersize=Integer.parseInt(String.valueOf(conf.get(confPrefix+"-spoutbuffer")));
		
		Object chkIntervel=conf.get(confPrefix+"-spoutIntervel");
		if(chkIntervel!=null)
		{
			this.checkIntervel=Integer.parseInt(String.valueOf(chkIntervel));
		}

    	this.timeoutCheck=new TimeOutCheck(timeout*1000l);
    	this.status=new SpoutStatus();
    	this.collector = collector;
    	int readerCount=context.getComponentTasks(context.getThisComponentId()).size();
    	int readerIndex=context.getThisTaskIndex();
    	this.intMapsize=Math.min(this.buffersize, 1024);
    	nolockbuffer=new HashMap<BoltStatKey, BoltStatVal>(this.intMapsize);

    	try {
			this.reader=new ImportReader(conf, confPrefix, parse, readerIndex, readerCount);
		} catch (IOException e) {
			LOG.error("TTReader",e);
		}
    }
    

	private HashMap<BoltStatKey, BoltStatVal> nolockbuffer=null;
	
	private SpoutStatus status=null;
	private TimeOutCheck timeoutCheck=null;
	
    private static SimpleDateFormat formatHour = new SimpleDateFormat("HH:mm:ss");

    int checkIntervel=1000;

    
	private boolean putdata(DataParser.DataIter log)
	{	
    	long ts=log.getTs();
		BoltStatKey key=new BoltStatKey(log.getGroup());
		BoltStatVal val=new BoltStatVal(log.getSum(),ts);
		
		BoltStatVal statval=nolockbuffer.get(key);
		this.status.ttInput++;
    	if(statval==null)
    	{
    		nolockbuffer.put(key, val);
    		this.status.groupCreate++;
    	}else{
    		statval.merger(val);
    	}
    	
    	if((this.status.ttInput%this.checkIntervel!=0))
    	{
    		return false;
    	}
    	
    	if(!this.timeoutCheck.istimeout()&&nolockbuffer.size()<buffersize)
		{
    		return false;
		}
    	this.timeoutCheck.reset();

		HashMap<BoltStatKey, BoltStatVal> buffer=nolockbuffer;
    	LOG.info(this.confPrefix+"####total:group="+buffer.size()+",ts:"+formatHour.format(new Date(ts))+",status:"+this.status.toString());
    	nolockbuffer=new HashMap<BoltStatKey, BoltStatVal>(this.intMapsize);
    	int batch=1;
		for(Entry<BoltStatKey, BoltStatVal> e:buffer.entrySet())
		{
			batch++;
			BoltStatKey pkey=e.getKey();
			BoltStatVal pvalue=e.getValue();
			
			
    		List<Object> data = StormUtils.mk_list((Object)pkey,pvalue);
	        collector.emit(data, SpoutUtils.uuid());
	        
	        if(batch%50==0)
	        {
	        	this.sleep(10);
	        }
		}
		
		if(this.status.ttInput>100000000)
		{
			this.status.reset();
		}
	    
	    return true;
	}
	
    @Override
    public synchronized void nextTuple()  {
		try {
			List ttdata = this.reader.read();
			if(ttdata==null)
			{
				return ;
			}
			for(Object o:ttdata)
			{
				this.status.ttInput++;
				DataParser.DataIter pv=(DataParser.DataIter)o;
				while(true)
				{
					this.status.groupInput++;
					this.putdata(pv);
					if(!pv.next())
					{
						break;
					}
				}
			}
			
		} catch (Throwable e) {
			this.sleep(100);
		}
    }
    
    private void sleep(int i)
    {
    	try {
			Thread.sleep(i);
		} catch (InterruptedException e1) {
		}
    }

    @Override
    public void close() {
	
    }
        @Override
    public void ack(Object msgId) {
        	this.status.ackCnt++;
    }

    @Override
    public void fail(Object msgId) {
    	this.status.failCnt++;
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
