package com.alimama.mdrill.solr.hbaserealtime.queue.client;


import java.util.zip.CRC32;

import org.apache.hadoop.conf.Configuration;

public class Message {
	private byte[] value;
	private MessageID id;

	public static Message INSTANCE(short shard,String partion,byte[] value,Configuration realConf)
	{
		CRC32 crc32 = new CRC32();
		crc32.update(partion.getBytes());
		MessageID id=MessageID.INSTANCE(shard, crc32.getValue(), realConf);
		return new Message( id, value);
	}

	private Message(MessageID id, byte[] value) {
		this.id = id;
		this.value = value;
	}

	public byte[] getValue() {
		return value;
	}

	public MessageID getId() {
		return id;
	}

	

}
