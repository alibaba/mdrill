package org.apache.solr.request.uninverted;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.apache.lucene.util.cache.Cache;
import org.apache.lucene.util.cache.SimpleMapCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alimama.mdrill.utils.UniqConfig;

public class GrobalCache {
	  private static long MAX_MEM_CACHE_SIZE = UniqConfig.getFieldValueMemSize();
	  private static long MAX_MEM_CACHE_SIZE_FQ = UniqConfig.getFieldValueMemSizefq();
	  public static Logger log = LoggerFactory.getLogger(GrobalCache.class);

	  
	  public static interface ILruMemSizeCache{
		  public long memSize();
		  public void LRUclean();
	  }
	  
	  public static interface ILruMemSizeKey{
		  
	  }
	  
		public static class StringKey implements GrobalCache.ILruMemSizeKey{
			private String str;

			@Override
			public String toString() {
				return str;
			}

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result + ((str == null) ? 0 : str.hashCode());
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
				StringKey other = (StringKey) obj;
				if (str == null) {
					if (other.str != null)
						return false;
				} else if (!str.equals(other.str))
					return false;
				return true;
			}

			public StringKey(String str) {
				super();
				this.str = str;
			}
		}
		
		
		private Cache<ILruMemSizeKey,ILruMemSizeCache> fieldValueCache_instance=Cache.synchronizedCache((new SimpleMapCache<ILruMemSizeKey, ILruMemSizeCache>(new LinkedHashMap<ILruMemSizeKey, ILruMemSizeCache>(1000,0.75f,true) {
			private static final long serialVersionUID = 1L;
			private long totalUsedMemsize=0l;
			public ILruMemSizeCache put(ILruMemSizeKey key, ILruMemSizeCache value){
				ILruMemSizeCache old=this.remove(key);
				if(old!=null)
				{
					this.totalUsedMemsize-=old.memSize();
					old.LRUclean();
				}
				
				long newmemsize = value.memSize();
				boolean isprint=newmemsize>1024*1024;

				if ((newmemsize + this.totalUsedMemsize) >= MAX_MEM_CACHE_SIZE) {
					long removesize = 0l;
					ArrayList<ILruMemSizeKey> toremove = new ArrayList<ILruMemSizeKey>();
					for (Entry<ILruMemSizeKey, ILruMemSizeCache> e : this.entrySet()) {
						removesize += e.getValue().memSize();
						toremove.add(e.getKey());
						if (removesize >= newmemsize) {
							break;
						}
					}
					this.totalUsedMemsize -= removesize;
					int printcnt=0;
					for (ILruMemSizeKey rm : toremove) {
						ILruMemSizeCache urm = this.remove(rm);
						if (urm!=null) {
							if(isprint&&printcnt++<10)
							{
								log.info("field value cache lru removed key "+rm.toString());
							}
							urm.LRUclean();
						}
					}
					
					if(this.size()==0)
					{
						this.totalUsedMemsize=0l;
					}
				}
				if(isprint)
				{
					log.info("####fieldvaluecache####"+(this.totalUsedMemsize/1024/1024)+"@"+(MAX_MEM_CACHE_SIZE/1024/1024)+"mb,size="+this.size()+",mem="+(newmemsize/1024/1024)+"mb,key "+key+"");
				}
				this.totalUsedMemsize+=newmemsize;
				return super.put(key, value);
			}
		})));
		
		
		private Cache<ILruMemSizeKey,ILruMemSizeCache> fieldValueCacheFq_instance=Cache.synchronizedCache((new SimpleMapCache<ILruMemSizeKey, ILruMemSizeCache>(new LinkedHashMap<ILruMemSizeKey, ILruMemSizeCache>(1000,0.75f,true) {
			private static final long serialVersionUID = 1L;
			private long totalUsedMemsize=0l;
			public ILruMemSizeCache put(ILruMemSizeKey key, ILruMemSizeCache value){
				ILruMemSizeCache old=this.remove(key);
				if(old!=null)
				{
					this.totalUsedMemsize-=old.memSize();
					old.LRUclean();
				}
				
				long newmemsize = value.memSize();
				boolean isprint=newmemsize>1024*1024;

				if ((newmemsize + this.totalUsedMemsize) >= MAX_MEM_CACHE_SIZE_FQ) {
					long removesize = 0l;
					ArrayList<ILruMemSizeKey> toremove = new ArrayList<ILruMemSizeKey>();
					for (Entry<ILruMemSizeKey, ILruMemSizeCache> e : this.entrySet()) {
						removesize += e.getValue().memSize();
						toremove.add(e.getKey());
						if (removesize >= newmemsize) {
							break;
						}
					}
					this.totalUsedMemsize -= removesize;
					int printcnt=0;
					for (ILruMemSizeKey rm : toremove) {
						ILruMemSizeCache urm = this.remove(rm);
						if (urm!=null) {
							if(isprint&&printcnt++<10)
							{
								log.info("field value cache FQ lru removed key "+rm.toString());
							}
							urm.LRUclean();
						}
					}
					
					if(this.size()==0)
					{
						this.totalUsedMemsize=0l;
					}
				}
				if(isprint)
				{
					log.info("####fieldvaluecache FQ####"+(this.totalUsedMemsize/1024/1024)+"@"+(MAX_MEM_CACHE_SIZE_FQ/1024/1024)+"mb,size="+this.size()+",mem="+(newmemsize/1024/1024)+"mb,key "+key+"");
				}
				this.totalUsedMemsize+=newmemsize;
				return super.put(key, value);
			}
		})));

		public static GrobalCache gcache= new GrobalCache();
	  public static Cache<ILruMemSizeKey,ILruMemSizeCache> fieldValueCache=gcache.fieldValueCache_instance;
	  public static Cache<ILruMemSizeKey,ILruMemSizeCache> fieldValueCache_fq=gcache.fieldValueCacheFq_instance;

}
