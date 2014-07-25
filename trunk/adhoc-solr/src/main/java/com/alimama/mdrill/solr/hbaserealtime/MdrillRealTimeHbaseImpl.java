package com.alimama.mdrill.solr.hbaserealtime;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.Append;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Increment;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.io.hfile.Compression.Algorithm;
import org.apache.hadoop.hbase.regionserver.StoreFile.BloomType;
import org.apache.hadoop.hbase.util.Bytes;

import com.alimama.mdrill.solr.hbaserealtime.MdrillRealtime.MdrillInputDocument;
import com.alimama.mdrill.solr.hbaserealtime.queue.client.MdrillQueue;
import com.alimama.mdrill.solr.hbaserealtime.queue.client.Message;
import com.alimama.mdrill.solr.hbaserealtime.queue.client.MessageID;

public class MdrillRealTimeHbaseImpl {
	public static byte[] DATA_FAMILY = Bytes.toBytes("mdrilldata");

	public static class Hbase{
		private Configuration config;
		public Configuration getConfig() {
			return config;
		}

		private String prefix="mdrill";
		public Hbase(Configuration config,String prefix) {
			this.config = config;
			this.prefix=prefix;
		}
		
		public String getDataTableNme(String tablename)
		{
			return this.prefix+"_"+ tablename+"_data";
		}
		
		public String getQueueTableNme(String tablename)
		{
			return this.prefix+"_"+ tablename+"_queue";
		}

		public void create(String tablename, int shardcount, Map<?, ?> params) throws IOException{

			HBaseAdmin admin = new HBaseAdmin(config);
			String tablenameshard = this.getDataTableNme(tablename);
			
			
			if (!admin.tableExists(tablenameshard)) {
				HTableDescriptor tableDescripter = new HTableDescriptor(tablenameshard.getBytes());
				tableDescripter.setValue("group",String.valueOf(params.get("realtime.hbase.group")));

				HColumnDescriptor columnDescripter2 = new HColumnDescriptor(DATA_FAMILY);
				columnDescripter2.setBlockCacheEnabled(false);
				columnDescripter2.setBlocksize(262144);
				columnDescripter2.setBloomFilterType(BloomType.NONE);
				columnDescripter2.setCompressionType(Algorithm.GZ);
				columnDescripter2.setMaxVersions(10);
				columnDescripter2.setInMemory(false);
				columnDescripter2.setTimeToLive(Integer.MAX_VALUE);
				tableDescripter.addFamily(columnDescripter2);

				admin.createTable(tableDescripter);
			}
			
			
			String tablenamequeue = this.getQueueTableNme(tablename);
			if (!admin.tableExists(tablenamequeue)) {
				HTableDescriptor tableDescripter = new HTableDescriptor(tablenamequeue.getBytes());
				tableDescripter.setValue("group",String.valueOf(params.get("realtime.hbase.group")));

				HColumnDescriptor columnDescripter2 = new HColumnDescriptor(MdrillQueue.FAMILY);
				columnDescripter2.setBlockCacheEnabled(false);
				columnDescripter2.setBlocksize(262144);
				columnDescripter2.setBloomFilterType(BloomType.NONE);
				columnDescripter2.setCompressionType(Algorithm.NONE);
				columnDescripter2.setMaxVersions(10);
				columnDescripter2.setInMemory(false);
				columnDescripter2.setTimeToLive(2 * 24 * 60 * 60);
				tableDescripter.addFamily(columnDescripter2);
				admin.createTable(tableDescripter);
			}
				
		
		}
	}
	
  public static class MdrillRealtimeClient implements MdrillRealtime.MdrillRealtimeClient
 {		private Hbase hbase;
 		private MdrillQueue queue;
 		private HTable table;

		public MdrillRealtimeClient(Configuration config,String prefix) {
			this.hbase = new Hbase(config, prefix);
		
		}

		@Override
		public void open(String tablename, short shardcount, Map<?, ?> params) throws IOException {
			this.hbase.create(tablename, shardcount, params);
			this.queue=new MdrillQueue(this.hbase.getConfig(), this.hbase.getQueueTableNme(tablename));
			this.table=new HTable(this.hbase.getConfig(), this.hbase.getDataTableNme(tablename));
			
		}

		@Override
		public void close() throws IOException {
			this.queue.close();
			this.table.close();
		}

		  
		public static byte[] long2Bytes(long num) {  
		    byte[] byteNum = new byte[8];  
		    for (int ix = 0; ix < 8; ++ix) {  
		        int offset = 64 - (ix + 1) * 8;  
		        byteNum[ix] = (byte) ((num >> offset) & 0xff);  
		    }  
		    return byteNum;  
		}  
		  
		public static long bytes2Long(byte[] byteNum) {  
		    long num = 0;  
		    for (int ix = 0; ix < 8; ++ix) {  
		        num <<= 8;  
		        num |= (byteNum[ix] & 0xff);  
		    }  
		    return num;  
		} 
		
		public byte[] toBytes(Object o)
		{
			if(o instanceof String)
			{
				return Bytes.toBytes((String)o);
			}
			if(o instanceof Integer)
			{
				return Bytes.toBytes((Integer)o);
			}
			if(o instanceof Long)
			{
				return Bytes.toBytes((Long)o);
			}
			
			if(o instanceof Boolean)
			{
				return Bytes.toBytes((Boolean)o);
			}
			
			if(o instanceof Double)
			{
				return Bytes.toBytes((Double)o);
			}
			
			if(o instanceof Float)
			{
				return Bytes.toBytes((Float)o);
			}
			
			if(o instanceof Byte)
			{
				return Bytes.toBytes((Byte)o);
			}
			
			return Bytes.toBytes(String.valueOf(o));
		}
		@Override
		public void updateSet(short shard,String partion, long higo_uuid,
				MdrillInputDocument update, MdrillInputDocument inc, MdrillInputDocument del) throws IOException {
			if(update!=null)
			{
				Append append=new Append(Bytes.toBytes(higo_uuid));

				for(Entry<String, Object> e:update.getMap().entrySet())
				{
					append.add(MdrillRealTimeHbaseImpl.DATA_FAMILY, Bytes.toBytes(e.getKey()),toBytes(e.getValue()));
				}
				table.append(append);
			}
			
			if(del!=null)
			{
				Delete delete=new Delete(Bytes.toBytes(higo_uuid));
				for(Entry<String, Object> e:del.getMap().entrySet())
				{
					delete.deleteColumn(MdrillRealTimeHbaseImpl.DATA_FAMILY,  Bytes.toBytes(e.getKey()));
				}
				
				table.delete(delete);
			}
			
		
			
			if(inc!=null)
			{
				Increment incment=new Increment();
				for(Entry<String, Object> e:inc.getMap().entrySet())
				{
					incment.addColumn(MdrillRealTimeHbaseImpl.DATA_FAMILY,  Bytes.toBytes(e.getKey()), Long.parseLong(String.valueOf(e.getValue())));
				}
				table.increment(incment);
			}
			
			this.queue.put(Message.INSTANCE(shard, partion, long2Bytes(higo_uuid), this.hbase.getConfig()));
		}

		
		
	}
}
