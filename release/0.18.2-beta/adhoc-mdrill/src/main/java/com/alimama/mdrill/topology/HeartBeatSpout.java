package com.alimama.mdrill.topology;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.alipay.bluewhale.core.utils.StormUtils;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichSpout;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;

public class HeartBeatSpout implements IRichSpout{
    private static final long serialVersionUID = 1L;

    private SpoutOutputCollector collector;
    
    @Override
    public void open(Map conf, TopologyContext context,
            SpoutOutputCollector collector) {
	this.collector = collector;
    }

    @Override
    public void nextTuple()  {
	List<Object> data = StormUtils.mk_list((Object) System.currentTimeMillis());
        collector.emit(data, UUID.randomUUID().toString());
        try {
	    Thread.sleep(10000);
        } catch (InterruptedException e) {
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
        declarer.declare(new Fields("timestamp"));
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        Map<String, Object> rtnmap = new HashMap<String, Object>();
        return rtnmap;
    }

}
