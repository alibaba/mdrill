package org.apache.solr.request.join;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import org.apache.lucene.index.SegmentReader;
import org.apache.lucene.util.cache.Cache;
import org.apache.solr.request.uninverted.GrobalCache;
import org.apache.solr.request.uninverted.GrobalCache.ILruMemSizeCache;
import org.apache.solr.request.uninverted.GrobalCache.ILruMemSizeKey;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.search.SolrIndexSearcher;

import com.alimama.mdrill.buffer.LuceneUtils;

public class HigoJoin  {
	public static Object lock=new Object();
	public static HigoJoinInterface getJoin(SegmentReader reader,String partion,IndexSchema schema,
			SolrIndexSearcher readerright, String fieldLeft, String fieldRigth)
			throws IOException {

		Cache<ILruMemSizeKey, ILruMemSizeCache> cache = GrobalCache.fieldValueCache;
		StringBuffer key = new StringBuffer();
		key.append("sig@");
		key.append(partion);
		key.append("@");
		key.append(fieldLeft);
		key.append("@");
		key.append(LuceneUtils.crcKey(reader));
		key.append("@");
		key.append(readerright.getPartionKey());
		key.append("@");
		key.append(fieldRigth);
		key.append("@");
		key.append(LuceneUtils.crcKey(readerright.getReader()));
		String cachekey = key.toString();
		GrobalCache.StringKey fvkey = new GrobalCache.StringKey(cachekey);
		HigoJoinInterface uif = (HigoJoinInterface) cache.get(fvkey);
		if (uif == null) {
			synchronized (lock) {
				uif = (HigoJoinInterface) cache.get(fvkey);
				if (uif == null) {
					
					FieldType ftleft=schema.getFieldType(fieldLeft);
					FieldType ftright =readerright.getSchema().getFieldType(fieldRigth);
					if(ftleft.isMultiValued()||ftright.isMultiValued())
					{
						throw new IOException("multi value");
					}else{
						uif = new HigoJoinSingleValues(reader,partion,schema,readerright,fieldLeft, fieldRigth);
					}
					cache.put(fvkey, uif);
				}
			}
		}
		return uif;
	}
	

	private static int INT_SIZE = Integer.SIZE / 8;

	public static class IntArr {
		public int[] list = new int[0];

		
		public static IntArr parse(Integer d) {
			IntArr rtn = new IntArr();
			rtn.list = new int[1];
			rtn.list[0]=d;

			return rtn;
		}

		public static IntArr parse(ArrayList<Integer> d) {
			IntArr rtn = new IntArr();
			rtn.list = new int[d.size()];
			int index = 0;
			for (Integer v : d) {
				rtn.list[index] = v;
				index++;
			}
			return rtn;
		}
		
		public static IntArr parse(HashSet<Integer> d) {
			IntArr rtn = new IntArr();
			rtn.list = new int[d.size()];
			int index = 0;
			for (Integer v : d) {
				rtn.list[index] = v;
				index++;
			}
			return rtn;
		}

		public long memsize() {
			return 8 + 8 + list.length * INT_SIZE;
		}
	}

	public static class JoinPariArr {
		public JoinPair[] list = new JoinPair[0];

		public long memsize() {
			long memsize = 8 + 8;
			for (JoinPair p : this.list) {
				memsize += p.memsize();
			}
			return memsize;
		}

		public static JoinPariArr parse(ArrayList<JoinPair> d) {
			JoinPariArr rtn = new JoinPariArr();
			rtn.list = new JoinPair[d.size()];
			int index = 0;
			for (JoinPair v : d) {
				rtn.list[index] = v;
				index++;
			}
			return rtn;
		}
	}
	
	

	static class JoinPair {
		int termNum;
		int[] left;

		public long memsize() {
			return (left.length + 2) * INT_SIZE + 16;
		}

		@Override
		public String toString() {
			return "JoinPair [termNum=" + termNum + ", left="
					+ Arrays.toString(left) + "]";
		}

	}

	static class JoinTermNum {
		public JoinTermNum(Integer docId, Integer termNum) {
			super();
			this.leftId = docId;
			this.termNum = termNum;
		}

		int leftId = -1;
		int termNum = -1;

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + leftId;
			result = prime * result + termNum;
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
			JoinTermNum other = (JoinTermNum) obj;
			if (leftId != other.leftId)
				return false;
			if (termNum != other.termNum)
				return false;
			return true;
		}

		public long memsize() {
			return 8 + INT_SIZE * 2;
		}

	}
	
	

}
