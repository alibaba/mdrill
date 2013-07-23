package org.apache.solr.request.mdrill;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.WeakHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.apache.solr.request.compare.GroupbyRow;
import org.apache.solr.request.compare.SelectDetailRow;
import org.apache.solr.request.compare.ShardDetailSelectDetailRowCompare;
import org.apache.solr.request.compare.ShardDetailSelectDetailRowStringCompare;
import org.apache.solr.request.compare.ShardGroupByGroupbyRowCompare;
import org.apache.solr.request.uninverted.NumberedTermEnum;
import org.apache.solr.request.uninverted.UnInvertedField;
import org.apache.solr.schema.FieldType;
import org.apache.solr.search.SolrIndexSearcher;

import com.alimama.mdrill.distinct.DistinctCount;
import com.alimama.mdrill.utils.UniqConfig;

public class MdrillPorcessUtils {
    private static Logger LOG = Logger.getLogger(MdrillPorcessUtils.class);

	private static WeakHashMap<Integer, LinkedBlockingQueue<GroupList>> fieldValueCache = new WeakHashMap<Integer, LinkedBlockingQueue<GroupList>>();
	public static synchronized LinkedBlockingQueue<GroupList>  getGroupListQueue(int size)
	{
		LinkedBlockingQueue<GroupList> rtn=fieldValueCache.get(size);
		if(rtn==null)
		{
			rtn=new LinkedBlockingQueue<GroupList>();
			fieldValueCache.put(size, rtn);
		}
		
		return rtn;
	}
	
	public static void cleanFieldValueCache(int size)
	{
		LinkedBlockingQueue<GroupList> rtn=fieldValueCache.get(size);
		if(rtn==null)
		{
			return ;
		}
		LOG.info("fieldValueCache.size="+rtn.size()+",size="+size);

		int sz=Math.min(UniqConfig.ShardMaxGroups(), 640000);
		if(rtn.size()>sz)
		{
			int left=rtn.size()-sz+1;
			for(int i=0;i<left;i++)
			{
				rtn.poll();
			}
		}
	}
	
	public static class TermNumToString{
		private static Object lock=new Object();
		HashSet<Integer> tmlist=new HashSet<Integer>();
		HashMap<Integer,String> tmMap=new HashMap<Integer,String>();
		UnvertFields ufs=null;
		int index=0;
		public TermNumToString(UnvertFields ufs,int index) {
			this.ufs = ufs;
			this.index=index;
		}
		
		public void addTermNum(Integer termNum)
		{
			tmlist.add(termNum);
		}
		
		public void fetchValues() throws IOException
		{
			int size=tmlist.size();
			if(size<=0)
			{
				return ;
			}
			Integer[] list=new Integer[size];
			tmlist.toArray(list);
			Arrays.sort(list);
			synchronized (TermNumToString.lock) {
				for(Integer i:list)
				{
					UnvertFile uf=ufs.cols[this.index];
					if (uf != null) {
					 String fieldvalue=uf.uif.tNumToString(i, uf.filetype, uf.ti,"null");
					 tmMap.put(i, fieldvalue);
					}
				}
			}
		}
		
		public String getTermValue(Integer termNum)
		{
			return String.valueOf(tmMap.get(termNum));
		}
		
		public String getTermValueWithNull(Integer termNum)
		{
			return tmMap.get(termNum);
		}
	}
	private static Object[] LOCK={new Object(),new Object()};
	  private static AtomicInteger lockIndex=new AtomicInteger(0);
	  
	  public static Object getLock()
	  {
		  int index=lockIndex.incrementAndGet();
		  if(index>=LOCK.length)
		  {
			  index=0; 
			  lockIndex.set(index);
		  }
		  return LOCK[index];
	  }
	  	  
	  public static void put2Queue(GroupbyRow mrow,PriorityQueue<GroupbyRow> res,int limit_offset,ShardGroupByGroupbyRowCompare cmp)
		{
			if (res.size() < limit_offset) {
				res.add(mrow);
			} else if (cmp.compare(res.peek(), mrow) > 0) {
				res.add(mrow);
				res.poll();
			}
		}
	  
	  
	  public static void put2QueueDetail(SelectDetailRow mrow,PriorityQueue<SelectDetailRow> res,int limit_offset,ShardDetailSelectDetailRowCompare cmp)
		{
			if (res.size() < limit_offset) {
				res.add(mrow);
			} else if (cmp.compare(res.peek(), mrow) > 0) {
				res.add(mrow);
				SelectDetailRow.FREE(res.poll());
			}else{
				SelectDetailRow.FREE(mrow);
			}
		}
	  
	  public static void put2QueueDetail(SelectDetailRow mrow,PriorityQueue<SelectDetailRow> res,int limit_offset,ShardDetailSelectDetailRowStringCompare cmp)
		{
			if (res.size() < limit_offset) {
				res.add(mrow);
			} else if (cmp.compare(res.peek(), mrow) > 0) {
				res.add(mrow);
				SelectDetailRow.FREE(res.poll());
			}else{
				SelectDetailRow.FREE(mrow);
			}
		}
		
		public static double ParseDouble(Object facetCount) {
			try {
				return Double.parseDouble(String.valueOf(facetCount));
			} catch (NumberFormatException e) {
				return 0;
			}
		}
	  
	  
	public static class RefRow{
			public boolean delayPut=false;
			public long val=0l;
			public RefRowStat[] stat;
			public DistinctCount[] dist;
		}
	  
	public static class RefRowStat{
		  public double max=0d;
		  public double min=0d;
		  public double sum=0d;
		  public long cnt=0l;
		  public boolean issetup = false;
		  public void update(Double key)
		  {
			  if(key<=UnInvertedField.MINVALUE)
			  {
				  return ;
			  }
			  this.cnt++;
    		  if(this.issetup)
    		  {
    			  this.sum += key;
    			  this.max = Math.max(this.max, key);
    			  this.min = Math.min(this.min, key);
    		  }else{
    			  this.sum=key;
    			  this.max=key;
    			  this.min=key;
        		  this.issetup=true;
    		  }
		  }
	  }
	  
	public static class GroupList {
		public int[] list;
		private GroupList(int size) {
			list = new int[size];
		}
		

		public void reset() {
			for (int i = 0; i < list.length; i++) {
				list[i] = -1;
			}
		}
		
		
		public static GroupList INSTANCE(LinkedBlockingQueue<GroupList> free,int size)
		{
			GroupList rtn=free.poll();
			if(rtn==null)
			{
				rtn=new GroupList(size);
			}else{
				if(rtn.list.length!=size)
				{
					rtn=new GroupList(size);
				}
			}
			return rtn;
		}
		
		public GroupList copy(LinkedBlockingQueue<GroupList> free)
		{
			GroupList rtn=INSTANCE(free,this.list.length);
			for (int i = 0; i < this.list.length; i++) {
				rtn.list[i] = this.list[i];
			}
			return rtn;
		}
		
		@Override
		public int hashCode() {
			int result = 1;
	        for (int element : this.list)
	        {
	            result = 31 * result + element;
	        }
	        return result;
		}

		@Override
		public boolean equals(Object obj) {
			GroupList other = (GroupList) obj;
			for (int i = 0; i < this.list.length; i++)
			{
				if (this.list[i] != other.list[i]) {
					return false;
				}
			}
			return true;
		}

	} 
	  
	public static class UnvertFile{
		  public UnInvertedField uif;
			public NumberedTermEnum ti;
			public FieldType filetype;
	  }
	
	public static class UnvertFields {
		public UnvertFile[] cols;
		public int length=0;
		public Integer[] listIndex;

		public UnvertFields(String[] fields, SolrIndexSearcher searcher)
				throws IOException {
			if (fields == null) {
				fields = new String[0];
			}
			this.length=fields.length;
			this.cols=new UnvertFile[this.length];

			ArrayList<Integer> index=new ArrayList<Integer>();
			for (int i = 0; i < this.length; i++) {
				 if(fields[i].indexOf("higoempty_")>=0)
				   {
					 cols[i]=null;
				    }else{
				    	UnvertFile uf=  new UnvertFile();

				    	uf.uif = UnInvertedField.getUnInvertedField(fields[i], searcher);
				    	uf.ti = uf.uif.getTi(searcher);
				    	uf.filetype = searcher.getSchema().getFieldType(fields[i]);
				    	cols[i]=uf;

						index.add(i);
				    }
			}
			
			listIndex=new Integer[index.size()];
			listIndex=index.toArray(listIndex);
		}
		
		public void free()
		{
			for(int i=0;i<cols.length;i++)
			{
				if(cols[i]!=null)
				{
					cols[i].uif.refCnt.decrementAndGet();
				}
			}
		}

	}
}
