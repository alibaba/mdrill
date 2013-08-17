package com.alimama.mdrill.topology;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.log4j.Logger;

import backtype.storm.task.OutputCollector;

import com.alipay.bluewhale.core.task.StopCheck;
import com.alipay.bluewhale.core.task.heartbeat.TaskHeartbeatRunable;

public class SolrStart implements Runnable, StopCheck, SolrStartInterface {
	private static Logger LOG = Logger.getLogger(SolrStart.class);
	private SolrStartJetty jetty;
	private SolrStartTable[] tables;

	public SolrStart(OutputCollector collector, Configuration conf,
			String solrhomeBase, String[] tableNames, String diskList,
			Integer portbase, int taskIndex, String topologyName,
			Integer taskid, Integer partions) throws IOException {
		jetty = new SolrStartJetty(collector, conf, solrhomeBase + "/"
				+ tableNames[0], diskList, portbase, taskIndex, topologyName,
				taskid, partions);
		tables = new SolrStartTable[tableNames.length];
		for (int i = 0; i < tableNames.length; i++) {
			LOG.info("create table " + tableNames[i]);
			tables[i] = new SolrStartTable(collector, conf, solrhomeBase + "/"
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
    public void setRealTime(boolean isRealTime) {
		jetty.setRealTime(isRealTime);
		for (SolrStartTable table : tables) {
			table.setRealTime(isRealTime);
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
//		MemInfo.gc();
		jetty.heartbeat();
		for (SolrStartTable table : tables) {
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
		if (jetty.isStop()) {
			return true;
		}
		for (SolrStartTable table : tables) {
			if (table.isStop()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void run() {
		jetty.run();
		for (SolrStartTable table : tables) {
			table.run();
		}
	}

}
