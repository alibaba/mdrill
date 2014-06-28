package com.alimama.mdrill.topology;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.log4j.Logger;

import com.alimama.mdrill.utils.HadoopUtil;
import com.alipay.bluewhale.core.utils.StormUtils;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichSpout;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;

public class MdrillSeriviceSpout implements IRichSpout{
    private static Logger LOG = Logger.getLogger(MdrillSeriviceSpout.class);

    private static final long serialVersionUID = 1L;
    private Configuration conf;
    private String hadoopConfPath;
    private String solrhome;
    
    private Integer portbase; 
    private String storePath;
    private String tablename;
    private Integer shards;
    private SpoutOutputCollector collector;
    private Integer partions;
    private String topologyName;
    private BoltParams params;
    public MdrillSeriviceSpout(BoltParams params,String hadoopConfPath,String solrhome,String tablename,String storePath,Integer portbase,Integer shards,Integer partions,String topologyName)
    {
	    this.params=params;
		this.hadoopConfPath=hadoopConfPath;
		this.solrhome=solrhome;
		this.storePath=storePath;
		this.portbase=portbase;
		this.tablename=tablename;
		this.shards=shards;
		this.partions=partions;
		this.topologyName=topologyName;
    }
    
    private SolrStartInterface solr=null;
    ShardThread EXECUTE =null;

    
    public void open(Map stormConf, TopologyContext context,
            SpoutOutputCollector collector) {
		this.collector = collector;
		this.EXECUTE = new ShardThread();
		try {
			this.conf = new Configuration();
			HadoopUtil.grabConfiguration(this.hadoopConfPath, this.conf);
			Integer taskIndex = context.getThisTaskIndex();
			this.solr = new SolrStart(this.params, collector, conf, solrhome,
					tablename.split(","), storePath, this.portbase, taskIndex,
					this.topologyName, context.getThisTaskId(), this.partions);
			this.solr.setExecute(this.EXECUTE);
			this.solr.setConfigDir(this.hadoopConfPath);
			this.solr.setConf(stormConf);
			this.solr.setMergeServer(taskIndex >= this.shards);
			this.solr.start();
		} catch (Throwable e1) {
			LOG.error(StormUtils.stringify_error(e1));
			this.solr.unregister();
			throw new RuntimeException(e1);
		}

	}
    
	@Override
    public void nextTuple()  {
		List<Object> data = StormUtils.mk_list((Object) System.currentTimeMillis());
		this.collector.emit(data);
		try {
			this.solr.checkError();
		} catch (Throwable e1) {
			LOG.error(StormUtils.stringify_error(e1));
			this.solr.unregister();
			throw new RuntimeException(e1);
		}

		try {
			Thread.sleep(1000l*60);
		} catch (InterruptedException e) {
		};

	}

	@Override
	public void close() {
		try {
			this.solr.stop();
		} catch (Throwable e) {
			LOG.error(StormUtils.stringify_error(e));
			this.solr.unregister();
			throw new RuntimeException(e);
		}
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

	@Override
	public void ack(Object msgId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fail(Object msgId) {
		// TODO Auto-generated method stub
		
	}

}
