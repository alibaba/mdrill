package com.alimama.mdrill.solr.hbaserealtime.queue.client;

//import java.io.DataInput;
//import java.io.DataOutput;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
//import org.apache.hadoop.hbase.util.Bytes;
//import org.apache.hadoop.io.Writable;

public class Scan{// implements Writable {
//	private MessageID startMessageID;
//	private MessageID stopMessageID;
//	private List<byte[]> topics;
//	private int caching;
//
//	public Scan(long crcpartion) {
//		startMessageID = new MessageID(crcpartion,Long.MIN_VALUE,Short.MIN_VALUE);
//		stopMessageID = new MessageID(crcpartion,Long.MAX_VALUE,Short.MAX_VALUE);
//		topics = new ArrayList<byte[]>();
//		caching = -1;
//	}
//
//	public Scan(long crcpartion,long startTime, long stopTime) {
//		this(new MessageID(crcpartion,startTime,Short.MIN_VALUE), new MessageID(crcpartion,stopTime,Short.MAX_VALUE));
//	}
//
//	public Scan(MessageID startMessageID, MessageID stopMessageID) {
//		this.startMessageID = startMessageID;
//		this.stopMessageID = stopMessageID;
//		topics = new ArrayList<byte[]>();
//		caching = -1;
//	}
//
//	public Scan(Scan other) {
//		this.startMessageID = other.startMessageID;
//		this.stopMessageID = other.stopMessageID;
//		this.topics = new ArrayList<byte[]>(other.getTopics().size());
//		for (byte[] topic : other.getTopics()) {
//			byte[] newTopic = new byte[topic.length];
//			System.arraycopy(topic, 0, newTopic, 0, topic.length);
//			this.topics.add(newTopic);
//		}
//		this.caching = other.caching;
//	}
//
//	public void setStartMessageID(MessageID startMessageID) {
//		this.startMessageID = startMessageID;
//	}
//
//	public MessageID getStartMessageID() {
//		return startMessageID;
//	}
//
//	public void setStopMessageID(MessageID stopMessageID) {
//		this.stopMessageID = stopMessageID;
//	}
//
//	public MessageID getStopMessageID() {
//		return stopMessageID;
//	}
//
//	public void addTopic(byte[] topicName) {
//		this.topics.add(topicName);
//	}
//
//	public List<byte[]> getTopics() {
//		return topics;
//	}
//
//	public void setCaching(int caching) {
//		this.caching = caching;
//	}
//
//	public int getCaching() {
//		return this.caching;
//	}
//
//	@Override
//	public void readFields(DataInput input) throws IOException {
//		int topicCount = input.readInt();
//		topics.clear();
//		for (int i = 0; i < topicCount; i++) {
//			topics.add(Bytes.readByteArray(input));
//		}
//		startMessageID.readFields(input);
//		stopMessageID.readFields(input);
//		caching = input.readInt();
//	}
//
//	@Override
//	public void write(DataOutput output) throws IOException {
//		output.writeInt(topics.size());
//		for (byte[] topicName : topics) {
//			Bytes.writeByteArray(output, topicName);
//		}
//		startMessageID.write(output);
//		stopMessageID.write(output);
//		output.writeInt(caching);
//	}
//
//	@Override
//	public String toString() {
//		return "{startMessageID=" + startMessageID + ",stopMessageID="
//				+ stopMessageID + ", caching=" + caching + "}";
//	}
}
