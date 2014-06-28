package com.alimama.mdrill.solr.realtime;

import java.util.Map;

import org.apache.solr.common.SolrInputDocument;


public class MdrillRealtime {
	
	public static interface MdrillRealtimeClient {
	
		public void open(String tablename,int shardcount,Map<?,?> params) throws Exception;
		public void close()  throws Exception;

		/**
		 * 有则更新无则添加,每次调用都会产生一个递增的Oplog，将来在MdrillRealtimeOpLogs中可以获取
		 * inc 适用于计数的更新，要保证计数的原子操作,每次调用都会产生一个递增的Oplog，将来在MdrillRealtimeOpLogs中可以获取
		 */
		public void updateSet(int shard,String partion,long higo_uuid,MdrillInputDocument set,MdrillInputDocument inc)  throws Exception;
		
		/**
			每次调用都会产生一个递增的Oplog，将来在MdrillRealtimeOpLogs中可以获取
		 */
		public void delete(int shard,String partion,long higo_uuid) throws Exception;
		
	}
	

	
	public static interface MdrillRealtimeOpLogs{
	
		public void open(String tablename,int shardcount,int shard,String partion,Map<?,?> params) throws Exception;
		
		public void close() throws Exception;

		/**
		 * 清理掉历史的操作日志
		 * @param pos
		 */
		public void cleanbefore(long pos) throws Exception;
		
		/**
		 * 获取下一个大于pos位置的document, 如果服务器出现异常，数据丢了，可以从指定消费的位置继续读取
		 * @param pos
		 * @return
		 */
		public Oplog getNext(long pos) throws Exception;
		
		/**
		 * 获取插入的值
		 */
		public MdrillDocument get(long higo_uuid);
	}
	
	public static interface Oplog{
		//单个shard内递增，他在单个shard内不能重复很关键
		public long getPos() ;
		//对应MdrillRealtimeClient中的全局唯一的id,根据这个ID，可以通过client获取到SolrInputDocument
		public long getHigoUuid();
		//如果是删除操作，该项值返回true
		public boolean isIsdelete(); 
	}
	
	public static interface MdrillInputDocument{
		public SolrInputDocument getDoc();
	}
	
	public static interface MdrillDocument
	{
		public long getPos() ;
		public MdrillInputDocument getDoc();
	}
}
