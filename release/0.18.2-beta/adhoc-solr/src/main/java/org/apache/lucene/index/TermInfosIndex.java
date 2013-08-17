package org.apache.lucene.index;

import java.io.IOException;
import java.util.HashMap;

import org.apache.lucene.store.IndexInput;
import org.apache.lucene.util.cache.SimpleLRUCache;

public class TermInfosIndex {
//	private int datasize=Long.SIZE/8;
//	private long indexfilelen=0;
//	private long count = -1;
//	private HashLru cache=new HashLru(8);
////	public SimpleLRUCache<Long,TermResult> cache=new SimpleLRUCache<Long,TermResult>(TermInfosReader.DEFAULT_CACHE_SIZE*10);
//	
////	public HashMap<Long,TermResult> cache=new HashMap<Long,TermResult>(TermInfosReader.DEFAULT_CACHE_SIZE*10);
//	private IndexInput index=null;
//	
//	public TermInfosIndex(IndexInput index) {
//		this.index = index;
//		this.indexfilelen=index.length();
//	    this.count=indexfilelen/datasize;
//	}
//
//	private TermResult seekToTerm(long p,SegmentTermEnum enumerator)  throws IOException {
//		long pos=this.enumSeek(enumerator, p);
//	  	if(enumerator.next())
//	  	{
//	  		 return new TermResult(enumerator.term(),pos,enumerator.termInfo());
//	  	}
//	  	return null;
//	   
//	}
//	
//	
//	private TermInfo getTermInfo(TermResult tr,SegmentTermEnum enumerator,long p,boolean onlysetLast) throws IOException
//	{
//		if(onlysetLast)
//		{
//			enumerator.setIntervalLast(p, tr.key, tr.ti);
//			return tr.ti;
//		}
//		if(!tr.isFromCache)
//		{
//			return enumerator.termInfo();
//		}
//		
//		this.termResultSeek(enumerator,tr,p);
//		if(enumerator.next())
//	  	{
//	  		return enumerator.termInfo();
//	  	}else{
//	    	return null;
//	    }
//	}
//	
//	
//
//	  public TermInfo binarySearch(Term term,SegmentTermEnum enumerator) throws IOException {
//	      long low = 0;
//	      long high = count-1;
//		  int interval=enumerator.indexInterval;
//		  int halfinterval=interval;
//	      while (low <= high) {
//	    	  long diff=high-low;
//			    if(diff<=halfinterval)
//			    {
//			    	long lastpos=seekToKeyFrame(low,enumerator,interval,false);
//				  	long minstart=Math.min(lastpos, low);
//				  	int maxScan=(int) (high-minstart);
//				  	return scanToTermResult(term,enumerator,maxScan);
//			    } 
//			    
//	    	  
//			    long mid = (low + high) >>> 1;
//	      	TermResult midTr;
//	      	int cmp;
//				    seekToKeyFrame(mid,enumerator,interval,true);
//				    midTr= getTermResult(mid,enumerator);
//				    if(midTr==null)
//				    {
//				    	return null;
//				    }
//				    cmp= midTr.key.compareTo(term);
//				    if(cmp==0)
//				    {
//					    return getTermInfo(midTr, enumerator, mid,false);
//				    }
//			    
//			    if (cmp < 0)
//			    {
//			      low = mid + 1;
//			    } else {
//			      high = mid - 1;
//			    } 
//			    
//			    
//		  }
//	      return null;
//	    }
//	
//	  private synchronized long seekToKeyFrame(long n,SegmentTermEnum enumerator,int interval,boolean onlysetLast) throws IOException {
//		  long lastInterval= n/interval;
//		  long keyPos=lastInterval*interval;
//		  TermResult last=getTermResult(keyPos,enumerator);
//		  if(last!=null)
//		  {
//			  getTermInfo(last, enumerator, keyPos, onlysetLast);
//		  }
//			  
//		  return keyPos;
//	}
//	
//	private  TermResult getTermResult(long n,SegmentTermEnum enumerator) throws IOException {
//		  TermResult rtn=cache.get(n);
//		  if(rtn!=null)
//		  {
//			  rtn.isFromCache=true;
//			  return rtn;
//		  }
//
//		  rtn= seekToTerm(n,enumerator);
//		  if(rtn==null)
//		  {
//			  return null;
//		  }
//		  cache.put(n, rtn);
//		  rtn.isFromCache=true;
//		  return rtn;
//	}
//	
//	private  TermInfo scanToTermResult(Term sterm,SegmentTermEnum enumerator,int maxScan) throws IOException {
//	   	if(enumerator.scanWithCurrent(sterm,maxScan)>=0)
//	 	{
//	   		return enumerator.termInfo();
//	   	}else{
//	     	return null;
//	     }
//	 }
//	
//	private long enumSeek(SegmentTermEnum enumerator,long p) throws IOException
//	{
//	 	 	long filePos=-1;
//	 	    long prevPosition=p*datasize;
//	 	  	index.seek(prevPosition);
//	 	  	filePos=index.readLong();
//	 	  	enumerator.seek(filePos,p);
//	 	  	return filePos;
//	}
//	
//	private void termResultSeek(SegmentTermEnum enumerator,TermResult tr,long p) throws IOException
//	{
// 	  	enumerator.seek(tr.pos,p);
//	}
//	
//
//	
//	private static class TermResult{
//		public TermInfo ti;
//	  	public Term key;
//	  	public long pos;
//	  	public boolean isFromCache=false;
//	  	public TermResult(Term _key,long _pos,TermInfo _ti)
//	  	{
//	  		key=_key;
//	  		pos=_pos;
//	  		ti=_ti;
//	  	}
//	  }
//	 public static class singleCache{
//		  public SimpleLRUCache<Long,TermResult> cache=new SimpleLRUCache<Long,TermResult>(TermInfosReader.DEFAULT_CACHE_SIZE);
//	  }
//	  
//	  public static class HashLru{
//		  private int hash_size=32;
//		  singleCache[] caches;
//		  public HashLru(int _size)
//		  {
//			  hash_size=_size;
//			  caches=new singleCache[hash_size];
//			  for(int i=0;i<hash_size;i++)
//			  {
//					caches[i]=new singleCache();
//			  }
//		  }
//		  
//		  public void put(Long n,TermResult v)
//		  {
//			  getCache(n).put(n, v);
//		  }
//		  public TermResult get(Long n)
//		  {
//			  return getCache(n).get(n);
//		  }
//		  public boolean containsKey(Long n)
//		  {
//			  return getCache(n).containsKey(n);
//		  }
//		  
//		  public SimpleLRUCache<Long,TermResult> getCache(Long n)
//		  {
//			  int index=(int) (n%hash_size);
//			  return caches[index].cache;
//		  }
//		  public int size()
//		  {
//			  int rtn=0;
//			  for(singleCache c:caches)
//			  {
//				  rtn+=c.cache.size();
//			  }
//			  
//			  return rtn;
//		  }
//	  }
}
