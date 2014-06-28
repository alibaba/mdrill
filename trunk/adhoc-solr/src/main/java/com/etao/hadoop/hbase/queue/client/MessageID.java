package com.etao.hadoop.hbase.queue.client;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.apache.hadoop.io.WritableComparable;

public class MessageID implements WritableComparable<MessageID> {
	private long partioncrc;


	private long timestamp;
	private short type;

	public MessageID(long partioncrc,long timestamp, short type) {
		this.partioncrc=partioncrc;
		this.timestamp = timestamp;
		this.type = type;
	}

	public MessageID(MessageID other) {
		this.partioncrc=other.partioncrc;
		this.timestamp = other.timestamp;
		this.type = other.type;
	}

	public long getPartioncrc() {
		return partioncrc;
	}
	public long getTimestamp() {
		return this.timestamp;
	}

	public short getType() {
		return this.type;
	}



	@Override
	public int compareTo(MessageID other) {
		if (this.partioncrc != other.partioncrc) {
			return this.partioncrc > other.partioncrc ? 1 : -1;
		}
				
		if (this.timestamp != other.getTimestamp()) {
			return this.timestamp > other.getTimestamp() ? 1 : -1;
		} 


		if (this.type != other.type) {

			return this.type > other.getType() ? 1 : -1;
		} else {
			return 0;
		}
	
		
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		this.partioncrc=in.readLong();
		this.timestamp = in.readLong();
		this.type = in.readShort();
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeLong(this.partioncrc);
		out.writeLong(this.timestamp);
		out.writeShort(this.type);
	}

}
