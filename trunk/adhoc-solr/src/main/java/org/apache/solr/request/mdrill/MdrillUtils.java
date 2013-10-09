package org.apache.solr.request.mdrill;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.apache.lucene.index.SegmentReader;
import org.apache.solr.request.uninverted.NumberedTermEnum;
import org.apache.solr.request.uninverted.UnInvertedField;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.search.SolrIndexSearcher;

import com.alimama.mdrill.distinct.DistinctCount;

public class MdrillUtils {
    static Logger LOG = Logger.getLogger(MdrillUtils.class);

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
	  
	public static class UnvertFile{
		  public UnInvertedField uif;
			public NumberedTermEnum ti;
			public FieldType filetype;
	  }
	
	public static class UnvertFields {
		public UnvertFile[] cols;
		public int length = 0;
		public Integer[] listIndex;

		public UnvertFields(String[] fields, SolrIndexSearcher searcher)
				throws IOException {
			if (fields == null) {
				fields = new String[0];
			}
			this.length = fields.length;
			this.cols = new UnvertFile[this.length];

			ArrayList<Integer> index = new ArrayList<Integer>();
			for (int i = 0; i < this.length; i++) {
				if (fields[i].indexOf("higoempty_") >= 0) {
					cols[i] = null;
				} else {
					UnvertFile uf = new UnvertFile();

					uf.uif = UnInvertedField.getUnInvertedField(fields[i],searcher);
					uf.ti = uf.uif.getTi(searcher);
					uf.filetype = searcher.getSchema().getFieldType(fields[i]);
					cols[i] = uf;

					index.add(i);
				}
			}

			listIndex = new Integer[index.size()];
			listIndex = index.toArray(listIndex);
		}

		public UnvertFields(String[] fields, SegmentReader reader,
				String partion, IndexSchema schema,boolean isreadDouble) throws IOException {
			if (fields == null) {
				fields = new String[0];
			}
			this.length = fields.length;
			this.cols = new UnvertFile[this.length];

			ArrayList<Integer> index = new ArrayList<Integer>();
			for (int i = 0; i < this.length; i++) {
				if (fields[i].indexOf("higoempty_") >= 0) {
					cols[i] = null;
				} else {
					UnvertFile uf = new UnvertFile();

					uf.uif = UnInvertedField.getUnInvertedField(fields[i],
							reader, partion, schema,isreadDouble);
					uf.ti = uf.uif.getTi(reader);
					uf.filetype = schema.getFieldType(fields[i]);
					cols[i] = uf;

					index.add(i);
				}
			}

			listIndex = new Integer[index.size()];
			listIndex = index.toArray(listIndex);
		}

		public void free() {
			for (int i = 0; i < cols.length; i++) {
				if (cols[i] != null) {
					cols[i].uif.refCnt.decrementAndGet();
				}
			}
		}

	}
}
