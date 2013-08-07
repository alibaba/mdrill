package com.alimama.mdrill.topology;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.log4j.Logger;

import com.alimama.mdrill.utils.HadoopUtil;
import com.alipay.bluewhale.core.utils.StormUtils;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;

public class ShardsBolt implements IRichBolt{
    private static Logger LOG = Logger.getLogger(ShardsBolt.class);

    private static final long serialVersionUID = 1L;
    private Configuration conf;
    private String hadoopConfPath;
    private String solrhome;
    
    private Integer portbase; 
    private String storePath;
    private String tablename;
    Integer shards;
    private  OutputCollector collector;
    private Integer partions;
    private String topologyName;
    private boolean isRealTime=false;

    public ShardsBolt( boolean isRealTime,String hadoopConfPath,String solrhome,String tablename,String storePath,Integer portbase,Integer shards,Integer partions,String topologyName)
    {
	this.hadoopConfPath=hadoopConfPath;
	this.solrhome=solrhome;
	this.storePath=storePath;
	this.portbase=portbase;
	this.tablename=tablename;
	this.shards=shards;
	this.partions=partions;
	this.topologyName=topologyName;
	this.isRealTime=isRealTime;
    }
    
    private SolrStartInterface solr=null;

    @Override
    public void prepare(Map stormConf, TopologyContext context,
            OutputCollector collector) {
	this.collector=collector;
	try {
	    this.conf=new Configuration();
	    HadoopUtil.grabConfiguration(this.hadoopConfPath, this.conf);
	    int hashcode=this.tablename.hashCode()%10000;
	    if(hashcode<0)
	    {
		hashcode*=-1;
	    }
	    Integer taskIndex=context.getThisTaskIndex();
	    
	    this.solr=new SolrStart(collector, conf, solrhome, tablename.split(","), storePath, this.portbase, taskIndex, this.topologyName, context.getThisTaskId(), this.partions);
	    this.solr.setConfigDir(this.hadoopConfPath);
	    this.solr.setRealTime(this.isRealTime);
	    this.solr.setMergeServer(!this.isRealTime&&taskIndex>=this.shards);
	    this.solr.start();
        } catch (RuntimeException e1) {
            LOG.error(StormUtils.stringify_error(e1));
            this.collector.reportError(e1);
            this.solr.unregister();
	    throw new RuntimeException(e1);
        }
	catch (Exception e) {
	    LOG.error(StormUtils.stringify_error(e));
            this.collector.reportError(e);
            this.solr.unregister();
	    throw new RuntimeException(e);
        }
    }
    
    @Override
    public void execute(Tuple input) {
	List<Object> data = StormUtils.mk_list((Object) System.currentTimeMillis());
        this.collector.emit(data);
	try {
	    this.solr.heartbeat();
        } catch (RuntimeException e1) {
            LOG.error(StormUtils.stringify_error(e1));
            this.solr.unregister();
            this.collector.reportError(e1);
            throw new RuntimeException(e1);
        }
	catch (Exception e) {
	    LOG.error(StormUtils.stringify_error(e));
            this.solr.unregister();
            this.collector.reportError(e);
            throw new RuntimeException(e);
        }
	
	this.collector.ack(input);
	
    }

    @Override
    public void cleanup() {
	try {
	    this.solr.stop();
        } catch (Exception e) {
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

}
