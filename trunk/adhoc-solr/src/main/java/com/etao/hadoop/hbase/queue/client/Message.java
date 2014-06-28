package com.etao.hadoop.hbase.queue.client;

//import java.io.DataInput;
//import java.io.DataOutput;
//import java.io.IOException;
//import java.text.DecimalFormat;
//
//import org.apache.hadoop.hbase.util.Bytes;
//import org.apache.hadoop.io.Writable;

public class Message {//implements Writable {
//	private static String PARTITION_ID_PATTERN;
//	static {
//		String maxShort = String.valueOf(Short.MAX_VALUE);
//		StringBuffer buffer = new StringBuffer(maxShort.length());
//		for (int i = 0; i < maxShort.length(); i++) {
//			buffer.append("0");
//		}
//		PARTITION_ID_PATTERN = buffer.toString();
//	}
//	public static int PARTITION_ID_LENGTH = PARTITION_ID_PATTERN.length();
//	public static DecimalFormat partitionIdFormat = new DecimalFormat(
//			PARTITION_ID_PATTERN);
//
//	private static String TIMESTAMP_PATTERN;
//	static {
//		String maxLong = String.valueOf(Long.MAX_VALUE);
//		StringBuffer buffer = new StringBuffer(maxLong.length());
//		for (int i = 0; i < maxLong.length(); i++) {
//			buffer.append("0");
//		}
//		TIMESTAMP_PATTERN = buffer.toString();
//	}
//	public static int TIMESTAMP_LENGTH = TIMESTAMP_PATTERN.length();
//	public static DecimalFormat timestampFormat = new DecimalFormat(TIMESTAMP_PATTERN);
//
//	private static String SEQUENCE_ID_PATTERN;
//	static {
//		String maxShort = String.valueOf(Short.MAX_VALUE);
//		StringBuffer buffer = new StringBuffer(maxShort.length());
//		for (int i = 0; i < maxShort.length(); i++) {
//			buffer.append("0");
//		}
//		SEQUENCE_ID_PATTERN = buffer.toString();
//	}
//	public static int SEQUENCE_ID_LENGTH = SEQUENCE_ID_PATTERN.length();
//	public static DecimalFormat sequenceIdFormat = new DecimalFormat(
//			SEQUENCE_ID_PATTERN);
//
//	
//
//	public static int ROW_LENGTH = PARTITION_ID_LENGTH + TIMESTAMP_LENGTH+TIMESTAMP_LENGTH+ SEQUENCE_ID_LENGTH ;
//
//	public static int PARTITION_ID_OFFSET = 0;
//
//	public static int CRC_OFFSET = PARTITION_ID_LENGTH ;
//
//	public static int TIMESTAMP_OFFSET =  CRC_OFFSET+ TIMESTAMP_LENGTH;;
//
//	public static int SEQUENCE_ID_OFFSET = TIMESTAMP_OFFSET + TIMESTAMP_LENGTH;
//
//	public static byte[] FAMILY = Bytes.toBytes("message");
//
//	private short shard;
//
//	private MessageID id;
//
//	private byte[] topic;
//
//	private byte[] value;
//
//	public Message() {
//	}
//
//
//
//	/*
//	 * only be used in scanner, because message does't has id before put
//	 */
//	public Message(short shard, MessageID id, byte[] topic, byte[] value) {
//		this.shard = shard;
//		this.id = id;
//		this.topic = topic;
//		this.value = value;
//	}
//
//	/**
//	 * Get Message Topic Name
//	 * */
//	public byte[] getTopic() {
//		return topic;
//	}
//
//	/**
//	 * Set Message Topic Name
//	 * */
//	public void setTopic(byte[] topic) {
//		this.topic = topic;
//	}
//
//	public short getPartitionID() {
//		return shard;
//	}
//
//	public void setShard(short partitionID) {
//		this.shard = partitionID;
//	}
//
//	/*
//	 * get message timestamp, which is reset in server
//	 */
//	public MessageID getID() {
//		return id;
//	}
//
//	public void setID(MessageID id) {
//		this.id = id;
//	}
//
//	/**
//	 * Get Message Value
//	 * */
//	public byte[] getValue() {
//		return value;
//	}
//
//	/**
//	 * set Message Value
//	 * */
//	public void setValue(byte[] value) {
//		this.value = value;
//	}
//
//	/**
//	 * create rowkey for message
//	 * */
//	public static byte[] makeKeyValueRow(short partitionID, MessageID messageID)
//			throws IOException {
//		byte[] row = new byte[Message.ROW_LENGTH];
//		long partioncre=messageID.getPartioncrc();
//		long timestamp = messageID.getTimestamp();
//		int sequenceID = messageID.getType();
//		// set partition id
//		byte[] partitionIDBytes = Bytes.toBytes(Message.partitionIdFormat.format(partitionID));
//		System.arraycopy(partitionIDBytes, 0, row, Message.PARTITION_ID_OFFSET,	partitionIDBytes.length);
//
//		// set timestamp
//		byte[] partioncreBytes = Bytes.toBytes(Message.timestampFormat.format(partioncre));
//		System.arraycopy(partioncreBytes, 0, row, Message.CRC_OFFSET,partioncreBytes.length);
//		
//		// set timestamp
//		byte[] timestampBytes = Bytes.toBytes(Message.timestampFormat.format(timestamp));
//		System.arraycopy(timestampBytes, 0, row, Message.TIMESTAMP_OFFSET,timestampBytes.length);
//
//		// set sequence id
//		byte[] sequnceIDBytes = Bytes.toBytes(Message.sequenceIdFormat.format(sequenceID));
//		System.arraycopy(sequnceIDBytes, 0, row, Message.SEQUENCE_ID_OFFSET,Message.SEQUENCE_ID_LENGTH);
//
//		return row;
//	}
//
//	@Override
//	public void readFields(DataInput in) throws IOException {
//		shard = in.readShort();
//		id.readFields(in);
//		topic = Bytes.readByteArray(in);
//		value = Bytes.readByteArray(in);
//	}
//
//	@Override
//	public void write(DataOutput out) throws IOException {
//		out.writeShort(shard);
//		id.write(out);
//		Bytes.writeByteArray(out, topic);
//		Bytes.writeByteArray(out, value);
//	}
//
}
