package com.alimama.mdrill.solr.realtime;

//import java.io.IOException;
//import java.util.Map;
//import java.util.zip.CRC32;
//
//import org.apache.hadoop.conf.Configuration;
//import org.apache.hadoop.hbase.HColumnDescriptor;
//import org.apache.hadoop.hbase.HTableDescriptor;
//import org.apache.hadoop.hbase.client.HBaseAdmin;
//import org.apache.hadoop.hbase.io.hfile.Compression.Algorithm;
//import org.apache.hadoop.hbase.regionserver.StoreFile.BloomType;
//
//import com.alimama.mdrill.solr.realtime.MdrillRealtime.MdrillInputDocument;
//import com.etao.hadoop.hbase.queue.client.HQueue;
//import com.etao.hadoop.hbase.queue.client.Message;
//import com.etao.hadoop.hbase.queue.client.MessageID;

public class MdrillRealTimeHbaseImpl {
//	public static class Hbase{
//		private Configuration config;
//		public Configuration getConfig() {
//			return config;
//		}
//
//		private String prefix="mdrill";
//		public Hbase(Configuration config,String prefix) {
//			this.config = config;
//			this.prefix=prefix;
//		}
//		
//		public String getDataTableNme(String tablename)
//		{
//			return this.prefix+"_"+ tablename+"_data";
//		}
//		
//		public String getQueueTableNme(String tablename)
//		{
//			return this.prefix+"_"+ tablename+"_queue";
//		}
//		
//		public void create(String tablename, int shardcount, Map<?, ?> params) throws IOException{
//
//			HBaseAdmin admin = new HBaseAdmin(config);
//
//			String tablenameshard = this.getDataTableNme(tablename);
//			if (!admin.tableExists(tablenameshard)) {
//				HTableDescriptor tableDescripter = new HTableDescriptor(tablenameshard.getBytes());
//				tableDescripter.setValue("group",String.valueOf(params.get("realtime.hbase.group")));
//
//				String familyName2 = "mdrill";
//				HColumnDescriptor columnDescripter2 = new HColumnDescriptor(familyName2.getBytes());
//				columnDescripter2.setBlockCacheEnabled(false);
//				columnDescripter2.setBlocksize(262144);
//				columnDescripter2.setBloomFilterType(BloomType.NONE);
//				columnDescripter2.setCompressionType(Algorithm.GZ);
//				columnDescripter2.setMaxVersions(10);
//				columnDescripter2.setInMemory(false);
//				columnDescripter2.setTimeToLive(Integer.MAX_VALUE);
//				tableDescripter.addFamily(columnDescripter2);
//
//				admin.createTable(tableDescripter);
//			}
//			
//			
//			String tablenamequeue = this.getQueueTableNme(tablename);
//			if (!admin.tableExists(tablenamequeue)) {
//				HTableDescriptor tableDescripter = new HTableDescriptor(tablenamequeue.getBytes());
//				tableDescripter.setValue("group",String.valueOf(params.get("realtime.hbase.group")));
//
//				HColumnDescriptor columnDescripter2 = new HColumnDescriptor(Message.FAMILY);
//				columnDescripter2.setBlockCacheEnabled(false);
//				columnDescripter2.setBlocksize(262144);
//				columnDescripter2.setBloomFilterType(BloomType.NONE);
//				columnDescripter2.setCompressionType(Algorithm.NONE);
//				columnDescripter2.setMaxVersions(10);
//				columnDescripter2.setInMemory(false);
//				columnDescripter2.setTimeToLive(2 * 24 * 60 * 60);
//				tableDescripter.addFamily(columnDescripter2);
//				admin.createTable(tableDescripter);
//			}
//				
//		
//		}
//	}
//	
//  public static class MdrillRealtimeClient implements MdrillRealtime.MdrillRealtimeClient
// {		private Hbase hbase;
// 		private HQueue queue;
//		public MdrillRealtimeClient(Configuration config,String prefix) {
//			this.hbase = new Hbase(config, prefix);;
//		
//		}
//
//		@Override
//		public void open(String tablename, int shardcount, Map<?, ?> params) throws IOException {
//			this.hbase.create(tablename, shardcount, params);
//			this.queue=new HQueue(this.hbase.getConfig(), this.hbase.getQueueTableNme(tablename));
//		}
//
//		@Override
//		public void close() throws IOException {
//			this.queue.close();
//		}
//
//		  
//		public static byte[] long2Bytes(long num) {  
//		    byte[] byteNum = new byte[8];  
//		    for (int ix = 0; ix < 8; ++ix) {  
//		        int offset = 64 - (ix + 1) * 8;  
//		        byteNum[ix] = (byte) ((num >> offset) & 0xff);  
//		    }  
//		    return byteNum;  
//		}  
//		  
//		public static long bytes2Long(byte[] byteNum) {  
//		    long num = 0;  
//		    for (int ix = 0; ix < 8; ++ix) {  
//		        num <<= 8;  
//		        num |= (byteNum[ix] & 0xff);  
//		    }  
//		    return num;  
//		} 
//		@Override
//		public void updateSet(int shard,String partion, long higo_uuid,
//				MdrillInputDocument set, MdrillInputDocument inc) throws IOException {
//			
//			this.add2Queue(shard, partion, higo_uuid, (short)0);
//			
//		}
//		
//		
//		private void add2Queue(int shard,String partion, long higo_uuid,short type) throws IOException
//		{
//			byte[] partinsbytes=partion.getBytes();
//			CRC32 crc32 = new CRC32();
//			crc32.update(partinsbytes);
//			MessageID msgid=new MessageID(crc32.getValue(), System.currentTimeMillis(),type);
//			Message message =new Message((short)shard, msgid, partion.getBytes(), long2Bytes(higo_uuid));
//			this.queue.put((short)shard, message);
//		}
//
//		@Override
//		public void delete(int shard,String partion, long higo_uuid) throws IOException {
//			this.add2Queue(shard, partion, higo_uuid, (short)1);
//
//		}
//
//	}
}
