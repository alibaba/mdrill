package com.etao.hadoop.hbase.queue.client;

//import java.io.IOException;
//import java.util.ArrayList;
//
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//import org.apache.hadoop.conf.Configuration;
//import org.apache.hadoop.hbase.HBaseConfiguration;
//import org.apache.hadoop.hbase.KeyValue;
//import org.apache.hadoop.hbase.client.ClientScanner;
//import org.apache.hadoop.hbase.client.Result;
//import org.apache.hadoop.hbase.util.Bytes;

public class ShardScanner {
//	static final Log LOG = LogFactory.getLog(ShardScanner.class);
//	private ClientScanner clientScanner;
//
//	private short partitionID;
//	
//
//	public ShardScanner(byte[] queueName, short shard, Scan scan)
//			throws IOException {
//		this(HBaseConfiguration.create(), queueName, shard, scan);
//	}
//
//	public ShardScanner(Configuration conf, byte[] queueName,
//			short partitionID, Scan scan) throws IOException {
//		this.partitionID = partitionID;
//		org.apache.hadoop.hbase.client.Scan tableScan = new org.apache.hadoop.hbase.client.Scan();
//		tableScan.setStartRow(Message.makeKeyValueRow(partitionID,
//				scan.getStartMessageID()));
//		tableScan.setStopRow(Message.makeKeyValueRow(partitionID,
//				scan.getStopMessageID()));
//		tableScan.setTimeRange(scan.getStartMessageID().getTimestamp(), scan
//				.getStopMessageID().getTimestamp());
//		for (byte[] topicName : scan.getTopics()) {
//			tableScan.addColumn(Message.FAMILY, topicName);
//		}
//		if (scan.getCaching() > 0) {
//			tableScan.setCaching(scan.getCaching());
//		} else {
//			tableScan.setCaching(conf.getInt(
//					"hbase.queue.client.scanner.caching", 10));
//		}
//
//		clientScanner = new ClientScanner(conf, tableScan, queueName);
//	}
//
//	/*
//	 * get message from scanner
//	 */
//	public Message next() throws IOException {
//		Result result = clientScanner.next();
//		if (null == result || result.isEmpty()) {
//			return null;
//		}
//		KeyValue kv = result.list().get(0);
//
//		byte[] crcBytes = new byte[Message.TIMESTAMP_LENGTH];
//		byte[] timestampBytes = new byte[Message.TIMESTAMP_LENGTH];
//		byte[] sequenceIDBytes = new byte[Message.SEQUENCE_ID_LENGTH];
//		System.arraycopy(kv.getBuffer(), kv.getRowOffset()
//				+ Message.CRC_OFFSET, crcBytes, 0,
//				Message.TIMESTAMP_LENGTH);
//		
//		System.arraycopy(kv.getBuffer(), kv.getRowOffset()
//				+ Message.TIMESTAMP_OFFSET, timestampBytes, 0,
//				Message.TIMESTAMP_LENGTH);
//		System.arraycopy(kv.getBuffer(), kv.getRowOffset()
//				+ Message.SEQUENCE_ID_OFFSET, sequenceIDBytes, 0,
//				Message.SEQUENCE_ID_LENGTH);
//		long crcstamp = Long.valueOf(Bytes.toString(crcBytes));
//
//		long timestamp = Long.valueOf(Bytes.toString(timestampBytes));
//		short sequenceID = Short.valueOf(Bytes.toString(sequenceIDBytes));
//		MessageID id = new MessageID(crcstamp,timestamp, sequenceID);
//		byte[] topic = kv.getQualifier();
//		byte[] value = kv.getValue();
//		Message message = new Message(partitionID, id, topic, value);
//		return message;
//	}
//
//	/*
//	 * get multi messages from scanner
//	 */
//	public Message[] next(int nbMessages) throws IOException {
//		ArrayList<Message> messageSets = new ArrayList<Message>(nbMessages);
//		for (int i = 0; i < nbMessages; i++) {
//			Message next = next();
//			if (next != null) {
//				messageSets.add(next);
//			} else {
//				break;
//			}
//		}
//		return messageSets.toArray(new Message[messageSets.size()]);
//	}
//
//	/*
//	 * close scanner
//	 */
//	public void close() {
//		clientScanner.close();
//	}
//
}
