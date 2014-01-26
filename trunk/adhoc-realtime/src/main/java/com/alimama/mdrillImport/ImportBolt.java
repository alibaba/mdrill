package com.alimama.mdrillImport;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;


import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;

public class ImportBolt implements IRichBolt {
	private static Logger LOG = Logger.getLogger(ImportBolt.class);
    private static final long serialVersionUID = 1L;
    private String confPrefix;
    boolean istanxpv=false;

    public ImportBolt(String confPrefix)
    {
    	this.confPrefix=confPrefix;
    	istanxpv=this.confPrefix.indexOf("tanx_pv")>=0;

    }

	private OutputCollector collector=null;
	private HashMap<BoltStatKey, BoltStatVal> nolockbuffer=null;

	private mdrillCommit commit=null;//new LastTimeBolt();
	
	private TimeOutCheck timeoutCheck=null;
	
    private DataParser parse=null;
	private BoltStatus status=null;
    int buffersize=5000;
    int intMapsize=500;


    @Override
    public void prepare(Map conf, TopologyContext context,
            OutputCollector collector) {
    	try {
			parse=(DataParser) Class.forName(String.valueOf(conf.get(this.confPrefix+"-parse"))).newInstance();
			parse.init(true, conf, context);
		} catch (Throwable e1) {
			LOG.error(this.confPrefix+" DataParser",e1);
		}
    	
		int timeout=Integer.parseInt(String.valueOf(conf.get(confPrefix+"-timeoutBolt")));
		this.buffersize=Integer.parseInt(String.valueOf(conf.get(confPrefix+"-boltbuffer")));
    	this.intMapsize=Math.min(this.buffersize, 1024);

    	this.status=new BoltStatus();

    	this.timeoutCheck=new TimeOutCheck(timeout*1000l);

    	this.commit=new mdrillCommit(parse,conf,this.confPrefix);
    	
    	this.collector=collector;
    	this.nolockbuffer=new HashMap<BoltStatKey, BoltStatVal>(this.intMapsize);
    }
    
	
    @Override
    public synchronized void execute(Tuple input) {
    	this.status.InputCount++;
    	BoltStatKey key=(BoltStatKey) input.getValue(0);
    	BoltStatVal bv=(BoltStatVal)input.getValue(1);
    	
    	
    	if(istanxpv)
		{
			if(key.list.length>=3&&"mm_12229823_1573806_11174236".equals(key.list[2]))
			{
				LOG.info("yanniandebugbolt:"+key.toString()+"==="+bv.toString());
			}
		}

    	BoltStatVal statval=nolockbuffer.get(key);
    	if(statval==null)
    	{
    		this.status.groupCreate++;
    		nolockbuffer.put(key, bv);
    	}else{
    		statval.merger(bv);
    	}
    	
    	if((this.status.InputCount%1000!=0))
    	{
        	this.collector.ack(input);

    		return;
    	}
		boolean isNotOvertime = !this.timeoutCheck.istimeout();
		if (isNotOvertime && nolockbuffer.size() < buffersize) {
	    	this.collector.ack(input);

			return;
		}
    	
	    	HashMap<BoltStatKey, BoltStatVal> buffer=nolockbuffer;
			nolockbuffer=new HashMap<BoltStatKey, BoltStatVal>(this.intMapsize);
			this.commit.updateAll(buffer,istanxpv);
		
		LOG.info(this.confPrefix+" bolt flush:groupsize="+buffer.size()+","+this.status.toString()+","+this.commit.toDebugString());
		this.timeoutCheck.reset();
    	this.collector.ack(input);

    }
    

    @Override
    public void cleanup() {
    	
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
    	
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return new HashMap<String, Object>();
    }

}
