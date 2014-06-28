package com.etao.hadoop.hbase.queue.client;

//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//import org.apache.hadoop.conf.Configuration;
//import org.apache.hadoop.hbase.HBaseConfiguration;
//import org.apache.hadoop.hbase.client.HTable;
//import org.apache.hadoop.hbase.client.Put;
//import org.apache.hadoop.hbase.util.Bytes;

public class HQueue {
//	static final Log LOG = LogFactory.getLog(HQueue.class);
//
//	private byte[] queueName;
//	private HTable table;
//
//
//	public HQueue(final String queueName) throws IOException {
//		this(Bytes.toBytes(queueName));
//	}
//
//	public HQueue(final byte[] queueName) throws IOException {
//		this(HBaseConfiguration.create(), queueName);
//	}
//
//	public HQueue(Configuration conf, final String queueName)
//			throws IOException {
//		this(conf, Bytes.toBytes(queueName));
//	}
//
//	public HQueue(Configuration conf, final byte[] queueName) throws IOException {
//		Configuration realConf = HBaseConfiguration.create();
//		HBaseConfiguration.merge(realConf, conf);
//		this.queueName = queueName;
//
//		// merge hqueue autoflush param into htable's orignal one
//		String autoFlushQueue = realConf.get("hbase.queue.client.write.autoflush");
//		if (null != autoFlushQueue && !autoFlushQueue.trim().isEmpty()) {
//			realConf.setBoolean("hbase.client.write.autoflush", realConf.getBoolean("hbase.queue.client.write.autoflush", true));
//		}
//
//		table = new HTable(realConf, queueName);
//		// comment the code below to prevent from stopping commitsFlusher thread
//		// table.setAutoFlush(conf.getBoolean("hbase.queue.client.write.autoflush", true));
//
//	}
//
//	public Configuration getConfiguration() {
//		return table.getConfiguration();
//	}
//
//	/*
//	 * Get Queue Name
//	 */
//	public String getQueueName() {
//		return Bytes.toString(table.getTableName());
//	}
//
//
//
//	
//	private void put(Message message) throws IOException {
//		byte[] row = Message.makeKeyValueRow(message.getPartitionID(), message.getID());
//		Put put = new Put(row);
//		put.add(Message.FAMILY, message.getTopic(), message.getValue());
//		table.put(put);
//	}
//
//	/*
//	 * Put message list, random partition id
//	 */
//	private void put(List<Message> messages) throws IOException {
//		List<Put> puts = new ArrayList<Put>(messages.size());
//		
//		for (Message message : messages) {
//			byte[] row = Message.makeKeyValueRow(message.getPartitionID(), message.getID());
//			Put put = new Put(row);
//			put.add(Message.FAMILY, message.getTopic(), message.getValue());
//			puts.add(put);
//		}
//		table.put(puts);
//	}
//
//	/*
//	 * Put message, validate partition id first
//	 */
//	public void put(short shard, Message message) throws IOException {
//		message.setShard(shard);
//		put(message);
//	}
//
//	/*
//	 * Put message list, validate partition id first
//	 */
//	public void put(short shard, List<Message> messages) throws IOException {
//		for (Message message : messages) {
//			message.setShard(shard);
//		}
//		put(messages);
//	}
//
//
//	public ShardScanner getShardScanner(short shard, Scan scan) throws IOException {
//		return new ShardScanner(table.getConfiguration(), queueName, shard, scan);
//	}
//
//	public void close() throws IOException {
//		table.close();
//	}
//
//	public void setAutoFlush(boolean autoFlush) {
//		table.setAutoFlush(autoFlush);
//	}
}
