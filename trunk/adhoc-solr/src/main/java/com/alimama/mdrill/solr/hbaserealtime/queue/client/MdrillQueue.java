package com.alimama.mdrill.solr.hbaserealtime.queue.client;

import java.io.IOException;
import java.util.zip.CRC32;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;

public class MdrillQueue {
	
	public static byte[] FAMILY = Bytes.toBytes("mdrillQueueFamily");
	public static byte[] FAMILYColumn = Bytes.toBytes("mdrillQueueCol");

	static final Log LOG = LogFactory.getLog(MdrillQueue.class);
	private byte[] queueName;
	private HTable table;

	public MdrillQueue(Configuration conf, final String queueName)
			throws IOException {
		this(conf, Bytes.toBytes(queueName));
	}

	public MdrillQueue(Configuration conf, final byte[] queueName) throws IOException {
		Configuration realConf = HBaseConfiguration.create();
		HBaseConfiguration.merge(realConf, conf);
		this.queueName = queueName;
		table = new HTable(realConf, queueName);
	}



	public void put(Message message) throws IOException {
		Put put = new Put(MessageID.toBytes(message.getId()));
		put.add(FAMILY, FAMILYColumn, message.getValue());
		table.put(put);
	}

	


	public ShardScanner getShardScanner(short shard,String partion, byte[] last) throws IOException {
		CRC32 crc32 = new CRC32();
		crc32.update(partion.getBytes());
		return new ShardScanner(table.getConfiguration(), queueName, shard, crc32.getValue(),last);
	}

	public void close() throws IOException {
		table.close();
	}


}
