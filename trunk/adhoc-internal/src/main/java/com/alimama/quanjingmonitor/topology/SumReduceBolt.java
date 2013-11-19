package com.alimama.quanjingmonitor.topology;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;

/**
create table quanjingmointor_pid(
 	thedate string ,
 	hour string ,
 	miniute tdate ,
 	miniute5 tdate ,
 	logtype string ,
 	pid string,
 	namemodle string,
 	groupName string,
 	datanum_a tlong,
 	datanum_b tlong
 )
 
 
 
 create table quanjingmointor_host(
 	thedate string ,
 	hour string ,
 	miniute tdate ,
 	logtype string ,
 	groupName string,
 	nodename string,
 	dns_ip string,
 	nodegroup string,
 	product_name string,
 	site string,
 	datanum_a tlong,
 	datanum_b tlong
 )
 
 
 
 http://110.75.67.137:9999/sql.jsp?connstr=jdbc%3Amdrill%3A%2F%2Fpreadhoc.kgb.cm6%3A9999&sql=select+miniute%2Clogtype%2Ccount%28*%29%2Csum%28datanum%29+from+quanjingmointor+where+thedate%3C%3D%2720131025%27+and+thedate%3E%3D%2720131020%27+group+by+miniute%2Clogtype+order+by+miniute+desc++limit+0%2C1000&go=%E6%8F%90%E4%BA%A4%E6%9F%A5%E8%AF%A2

select miniute,logtype,count(*),sum(datanum_a) from quanjingmointor_pid where thedate='20131028'  group by miniute,logtype order by miniute desc  limit 0,100

select thedate,hour,miniute,logtype,pid,process,nodename,dns_ip,nodegroup,product_name,site,count(*),sum(datanum),sum(datanum_b),sum(datanum_c) from quanjingmointor where thedate='20131025'  group by thedate,hour,miniute,logtype,pid,process,nodename,dns_ip,nodegroup,product_name,site order by miniute desc  limit 0,10


 */
public class SumReduceBolt implements IRichBolt {
	private static Logger LOG = Logger.getLogger(SumReduceBolt.class);
    private static final long serialVersionUID = 1L;
    
    public String type="";
    public SumReduceBolt(String type) {
		this.type = type;
	}

	private OutputCollector collector=null;
	private volatile long index=0;
	
	private HashMap<BoltStatKey, BoltStatVal> nolockbuffer=null;

	private LastTimeBolt_chain1 lasttimeUp_chain1=null;//new LastTimeBolt();
	
	private TimeOutCheck timeoutCheck=null;

    @Override
    public void prepare(Map stormConf, TopologyContext context,
            OutputCollector collector) {
    	this.timeoutCheck=new TimeOutCheck(60*1000l);
    	this.lasttimeUp_chain1=new LastTimeBolt_chain1(this, new LastTimeBolt_chain2_pid(this),new LastTimeBolt_chain2_host(this));
    	this.collector=collector;
    	this.nolockbuffer=new HashMap<BoltStatKey, BoltStatVal>(10000);
    }
    
	private static int BUFFER_SIZE=10000;

    @Override
    public synchronized void execute(Tuple input) {
    	
    	BoltStatKey key=(BoltStatKey) input.getValue(0);
    	BoltStatVal bv=(BoltStatVal)input.getValue(1);

    	BoltStatVal statval=nolockbuffer.get(key);
    	if(statval==null)
    	{
    		statval=new BoltStatVal();
    		nolockbuffer.put(key, statval);
    	}
    	
    	statval.cnt+=bv.cnt;
    	statval.cntnonclear+=bv.cntnonclear;
    	this.index++;
    	
    	if((this.index%1000!=0))
    	{
        	this.collector.ack(input);
    		return;
    	}
    	
		boolean isNotOvertime=!this.timeoutCheck.istimeout();
    	if(isNotOvertime&&nolockbuffer.size()<BUFFER_SIZE)
		{
    		this.collector.ack(input);
    		return;
		}
		this.timeoutCheck.reset();

		HashMap<BoltStatKey, BoltStatVal> buffer=nolockbuffer;
		nolockbuffer=new HashMap<BoltStatKey, BoltStatVal>(BUFFER_SIZE);
		this.lasttimeUp_chain1.updateAll(buffer,(Long)key.list[0]);
		
		LOG.info("bolt total="+this.index+",buffersize="+buffer.size()+","+this.lasttimeUp_chain1.toDebugString());
		this.index=0;

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
