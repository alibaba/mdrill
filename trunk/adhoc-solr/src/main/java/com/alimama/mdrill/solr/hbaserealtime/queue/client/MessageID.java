package com.alimama.mdrill.solr.hbaserealtime.queue.client;

import java.text.DecimalFormat;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.util.Bytes;


/**
 * 同一个shard的同一个分区 必须在同一个进程内生成，否则这个messageId不会保证全局递增唯一
 * 之所以没有考虑借助外部生成全局唯一ID，主要考虑的是性能因数，大家可以通过改动INSTANCE的方法自己实现
 * @author yannian.mu
 *
 */
public class MessageID implements java.lang.Comparable<MessageID> {

	private static long CURR_TS=-1;
	private static short CURR_SUBINDEX=-1;
	public static synchronized MessageID INSTANCE(short shardIndex, long partioncrc,Configuration realConf)
	{
		long ts=System.currentTimeMillis();
		if(ts!=CURR_TS)
		{
			CURR_TS=ts;
			CURR_SUBINDEX=0;
		}
		
		return new MessageID(shardIndex, partioncrc,CURR_TS, CURR_SUBINDEX++);
	}
	
	public static String MAKE_PARTERN(String partion)
	{
		StringBuffer buffer = new StringBuffer(partion.length());
		for (int i = 0; i < partion.length(); i++) {
			buffer.append("0");
		}
		return buffer.toString();
	
	}
	
	private static String SHORT_PATTERN=MAKE_PARTERN(String.valueOf(Short.MAX_VALUE));
	private static String lONG_PATTERN=MAKE_PARTERN(String.valueOf(Long.MAX_VALUE));
	
	public static DecimalFormat SHORT_Format = new DecimalFormat(SHORT_PATTERN);
	public static DecimalFormat LONG_Format = new DecimalFormat(lONG_PATTERN);

	
	private short shard;
	private long partion;
	private long index;
	private short subIndex;
	
	
	public static byte[] toBytes(MessageID msgid)
	{
		StringBuffer buff=new StringBuffer();
		buff.append(SHORT_Format.format(msgid.getShardIndex()));
		buff.append(LONG_Format.format(msgid.getPartioncrc()));
		buff.append(LONG_Format.format(msgid.getIndex()));
		buff.append(SHORT_Format.format(msgid.getSubIndex()));
		return  Bytes.toBytes(buff.toString());
	}
	



	public MessageID(short shardIndex, long partioncrc, long index,	short subindex) {
		this.shard = shardIndex;
		this.partion = partioncrc;
		this.index = index;
		this.subIndex = subindex;
	}

	public long getPartioncrc() {
		return partion;
	}

	public short getShardIndex() {
		return shard;
	}

	public long getIndex() {
		return index;
	}

	public short getSubIndex() {
		return subIndex;
	}

	public int compareTo(MessageID other) {

		if (this.shard != other.shard) {
			return this.shard > other.shard ? 1 : -1;
		}
		if (this.partion != other.partion) {
			return this.partion > other.partion ? 1 : -1;
		}

		if (this.index != other.index) {
			return this.index > other.index ? 1 : -1;
		}

		if (this.subIndex != other.subIndex) {

			return this.subIndex > other.subIndex ? 1 : -1;
		} else {
			return 0;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (index ^ (index >>> 32));
		result = prime * result + (int) (partion ^ (partion >>> 32));
		result = prime * result + shard;
		result = prime * result + subIndex;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MessageID other = (MessageID) obj;
		if (index != other.index)
			return false;
		if (partion != other.partion)
			return false;
		if (shard != other.shard)
			return false;
		if (subIndex != other.subIndex)
			return false;
		return true;
	}

}
