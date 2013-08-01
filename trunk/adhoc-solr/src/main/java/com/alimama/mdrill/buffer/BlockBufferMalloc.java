package com.alimama.mdrill.buffer;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.solr.core.SolrResourceLoader;
import org.apache.solr.request.uninverted.GrobalCache;


public class BlockBufferMalloc {
//	public static class SimpleLRUCache extends SimpleMapCache<block, blockData> {
//		  private final static float LOADFACTOR = 0.75f;
//		  public SimpleLRUCache(final int cacheSize) {
//		    super(new LinkedHashMap<block, blockData>((int) Math.ceil(cacheSize / LOADFACTOR) + 1, LOADFACTOR, true) {
//		  	      @Override
//		  	      protected boolean removeEldestEntry(Map.Entry<block, blockData> eldest) {
//		  	    	  
//		  	        boolean isrtn= size() > cacheSize;
//		  	        if(isrtn)
//		  	        {
//		  	        	blockData freed=eldest.getValue();
//		  		    	BlockBufferMalloc.freeData( freed);
//		  	        }
//		  	        return isrtn;
//		  	      }
//		  	    });
//		  }
//
//		}
	
	public static LinkedBlockingQueue<blockData> free = new LinkedBlockingQueue<blockData>();
	public static AtomicLong mallocTimes = new AtomicLong(0l);
	public static AtomicLong reusedTimes = new AtomicLong(0l);

	public static blockData malloc(int asize) {
			blockData rtn=free.poll();
			if(rtn==null)
			{
				rtn=new blockData(new byte[BlockBufferInput.BLOCK_SIZE],asize);
				mallocTimes.incrementAndGet();
			}else{
				reusedTimes.incrementAndGet();
			}
			rtn.setSize(asize);

			return rtn;
	}

	public static void freeData(blockData d) {
				if (d != null) {
					boolean allowadd=d.allowFree.get()==0&&free.size() < 100;
					if(allowadd)
					{
						d.updateLasttime();
						free.add(d);
					}
				}
	}
	public static class block implements GrobalCache.ILruMemSizeKey{
		private String key;
		private long index;
		private long flushkey=0;
		public block(String key, long pos) {
			super();
			this.key = key;
			this.index = pos;
			this.flushkey=SolrResourceLoader.getCacheFlushKey();
		}
		

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (flushkey ^ (flushkey >>> 32));
			result = prime * result + (int) (index ^ (index >>> 32));
			result = prime * result + ((key == null) ? 0 : key.hashCode());
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
			block other = (block) obj;
			if (flushkey != other.flushkey)
				return false;
			if (index != other.index)
				return false;
			if (key == null) {
				if (other.key != null)
					return false;
			} else if (!key.equals(other.key))
				return false;
			return true;
		}
		@Override
		public String toString() {
			return "block [key=" + key + ", index=" + index + ", flushkey="
					+ flushkey + "]";
		}
		
		
	}
	

	public static class blockData implements GrobalCache.ILruMemSizeCache{
		byte[] buff;
		
		int size;
		
		@Override
		public String toString() {
			return "blockData [buff=" + buff.length+ ", size="
					+ size + ", allowFree=" + allowFree + ", lasttime="
					+ lasttime + "]";
		}

		public void setSize(int size) {
			this.size = size;
		}

		public blockData(byte[] buff,int size) {
			super();
			this.buff = buff;
			this.size=size;
		}
		
		public AtomicInteger allowFree=new AtomicInteger(0);
		long lasttime=System.currentTimeMillis();
		public long getLasttime() {
			return lasttime;
		}

		public void updateLasttime() {
			this.lasttime = System.currentTimeMillis();
		}

		@Override
		public long memSize() {
			if(buff==null)
			{
				return Integer.SIZE/8;
			}
			return buff.length+Integer.SIZE/8;
		}

		@Override
		public void LRUclean() {

			BlockBufferMalloc.freeData( this); 
		}
		
	}
}
