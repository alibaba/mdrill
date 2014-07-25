package com.alimama.mdrill.solr.hbaserealtime.queue.client;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.ClientScanner;
import org.apache.hadoop.hbase.client.Result;

public class ShardScanner {
	static final Log LOG = LogFactory.getLog(ShardScanner.class);
	private ClientScanner clientScanner;

	public ShardScanner(Configuration conf, byte[] queueName,
			short shard,long partion, byte[] last) throws IOException {
		
		org.apache.hadoop.hbase.client.Scan tableScan = new org.apache.hadoop.hbase.client.Scan();
		if(last==null)
		{
			tableScan.setStartRow(MessageID.toBytes(new MessageID(shard, partion, Integer.MIN_VALUE, Short.MIN_VALUE)));

		}else{
			tableScan.setStartRow(last);
		}
		tableScan.setStopRow(MessageID.toBytes(new MessageID(shard, partion, Integer.MAX_VALUE, Short.MAX_VALUE)));
		tableScan.setMaxResultSize(10240);
		tableScan.addColumn(MdrillQueue.FAMILY, MdrillQueue.FAMILYColumn);
		tableScan.setBatch(10240);
		tableScan.setCaching(1024);
		clientScanner = new ClientScanner(conf, tableScan, queueName);
	}

	public KeyValue next() throws IOException {
		Result result = clientScanner.next();
		if (null == result || result.isEmpty()) {
			return null;
		}
		return result.list().get(0);
	}

	public void close() {
		clientScanner.close();
	}

}
