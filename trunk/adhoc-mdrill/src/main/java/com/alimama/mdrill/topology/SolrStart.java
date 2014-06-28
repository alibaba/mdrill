package com.alimama.mdrill.topology;


import java.util.ArrayList;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.log4j.Logger;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.OutputCollector;

import com.alimama.mdrill.solr.realtime.ShardPartion;
import com.alimama.mdrill.utils.IndexUtils;
import com.alipay.bluewhale.core.task.StopCheck;
import com.alipay.bluewhale.core.task.heartbeat.TaskHeartbeatRunable;

public class SolrStart implements StopCheck, SolrStartInterface {
	private static Logger LOG = Logger.getLogger(SolrStart.class);
	private SolrStartJetty jetty;
	private SolrStartTable[] tables;
	private BoltParams params;
	
	
	public SolrStart(BoltParams params,SpoutOutputCollector collector, Configuration conf,
			String solrhomeBase, String[] tableNames, String diskList,
			Integer portbase, int taskIndex, String topologyName,
			Integer taskid, Integer partions) throws Exception {
		this.params=params;
		
		ShardPartion.base=solrhomeBase;
		ShardPartion.taskIndex=taskIndex;
		ShardPartion.index=IndexUtils.getHdfsForder(taskIndex);
		jetty = new SolrStartJetty(this.params,collector, conf, solrhomeBase + "/"
				+ tableNames[0], diskList, portbase, taskIndex, topologyName,
				taskid, partions);
		tables = new SolrStartTable[tableNames.length];
		for (int i = 0; i < tableNames.length; i++) {
			LOG.info("create table " + tableNames[i]);
			tables[i] = new SolrStartTable(this.params,collector, conf, solrhomeBase + "/"
					+ tableNames[i], diskList, taskIndex, tableNames[i],
					taskid, jetty);
		}
	}

	@Override
	public void setMergeServer(boolean isMergeServer) {
		jetty.setMergeServer(isMergeServer);
		for (SolrStartTable table : tables) {
			table.setMergeServer(isMergeServer);
		}

	}
	


	@Override
	public void setConfigDir(String dir) {
		jetty.setConfigDir(dir);
		for (SolrStartTable table : tables) {
			table.setConfigDir(dir);
		}
	}

	@Override
	public void start() throws Exception {
		jetty.start();
		for (SolrStartTable table : tables) {
			table.start();
		}
		TaskHeartbeatRunable.regieterStopCheck(this);

	}

	@Override
	public void stop() throws Exception {
		jetty.stop();
		for (SolrStartTable table : tables) {
			table.stop();
		}
	}

	@Override
	public Boolean isTimeout() {
		if (jetty.isTimeout()) {
			return true;
		}
		for (SolrStartTable table : tables) {
			if (table.isTimeout()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void heartbeat() throws Exception {
		this.jetty.heartbeat();
		for (final SolrStartTable table : tables) {
			table.heartbeat();
		}
	}

	@Override
	public void unregister() {
		jetty.unregister();
		for (SolrStartTable table : tables) {
			table.unregister();
		}
	}

	@Override
	public boolean isStop() {
		if(this.isTimeout())
		{
			return true;
		}
		
		for (SolrStartTable table : tables) {
			if (!table.isStop()) {
				return false;
			}
		}
		for (SolrStartTable table : tables) {
			table.reportError("timeout");
		}

		return true;
	}

	@Override
	public void setConf(Map stormConf) {
		jetty.setConf(stormConf);
		for (SolrStartTable table : tables) {
			table.setConf(stormConf);
		}
	}

	@Override
	public void setExecute(ShardThread EXECUTE) {
		jetty.setExecute(EXECUTE);
		for (SolrStartTable table : tables) {
			table.setExecute(EXECUTE);
		}
	}

	
	public void checkError(){
		jetty.checkError();
		for (SolrStartTable table : tables) {
			table.checkError();
		}
	}
	

}
