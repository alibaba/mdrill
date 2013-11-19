package org.apache.solr.request.uninverted;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.SegmentReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermInfosReader;
import org.apache.lucene.util.cache.Cache;
import org.apache.lucene.util.cache.SimpleLRUCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alimama.mdrill.utils.UniqConfig;

public class TermIndex {
	  public static Logger log = LoggerFactory.getLogger(TermIndex.class);

	  final static int intervalBits = 7;  // decrease to a low number like 2 for testing
	  public final static int intervalMask = 0xffffffff >>> (32-intervalBits);
	  public final static int interval = 1 << intervalBits;

	  final Term fterm; // prototype to be used in term construction w/o String.intern overhead
	  final String prefix;
	 

	  int nTerms;
	  long sizeOfStrings;
	  String field;
	  IndexSearch index;

public static class IndexSearch
{
	 String[] index;
	 long[] indexl;
     TermInfosReader.QuickInput quicktisInput=null;
     
     public String get(int i) throws IOException
     {
    	 if(quicktisInput==null)
    	 {
    		 if(index==null||i>=index.length)
    		 {
    			 return "null";
    		 }
    		 return index[i];
    	 }
    	 
    	 return getl(i);
     }
     
 	private Cache<Integer, String> termsCache = Cache.synchronizedCache(new SimpleLRUCache<Integer, String>(UniqConfig.getTermCacheSizeIndex()));

		private String getl(int i) throws IOException {
			long pos = 0;
			try {
				String termText = termsCache.get(i);
				if (termText != null) {
					return termText;
				}
				pos = indexl[i];
				quicktisInput.quicktisInputTxt.seek(pos);
				termText = quicktisInput.quicktisInputTxt.readString();

				termsCache.put(i, termText);

				return termText;
			} catch (Throwable e) {
				log.error(i + "@pos:" + pos, e);
				return "null";
			}

		}
     
     public int search(String s) throws IOException
     {
    	 if(quicktisInput==null)
    	 {
    		 return Arrays.binarySearch(index,s);
    	 }
    	 
    	 return binarySearch0(0, indexl.length, s);
     }
     
     private int binarySearch0(int fromIndex, int toIndex,
		     Object key) throws IOException {
		int low = fromIndex;
		int high = toIndex - 1;
		
		while (low <= high) {
		int mid = (low + high) >>> 1;
		Comparable midVal = (Comparable)this.getl(mid);
		int cmp = midVal.compareTo(key);
		
		if (cmp < 0)
		low = mid + 1;
		else if (cmp > 0)
		high = mid - 1;
		else
		return mid; // key found
		}
		return -(low + 1);  // key not found.
		}
}
	  public TermIndex(String field) {
	    this(field, null);
	  }

	 public TermIndex(String _field, String prefix) {
		  this.field=_field;
	    this.fterm = new Term(_field, "");
	    this.prefix = prefix;
	  }

	  public Term createTerm(String termVal) {
	    return fterm.createTerm(termVal);
	  }

	 public NumberedTermEnum getEnumerator(IndexReader reader, int termNumber) throws IOException {
	    NumberedTermEnum te = new NumberedTermEnum(reader, this);
	    te.skipTo(termNumber);
	    return te;
	  }
	  
	  
	  public static class QuickNumberedTermEnum{
	      ArrayList<String> lst;
	      ArrayList<Long> pos;
	      TermIndex ti;

	      TermInfosReader.QuickInput quicktisInput;
		int termCount=0;
		int termNum=0;
		boolean isNonText=false;
		boolean isReadDouble=true;
		  public QuickNumberedTermEnum(TermIndex ti,TermInfosReader.QuickInput quicktisInput, long pos,int cnt,Long pos2,boolean isReadDouble) throws IOException {
			  this.isReadDouble=isReadDouble;
			  this.ti=ti;
			  this.quicktisInput = quicktisInput.singleThr();
			  this.quicktisInput.quicktisInput.seek(pos);
			 
			  this.termCount=cnt;
			  this.termNum=0;
	          this.lst = new ArrayList<String>();
	          this.pos=new ArrayList<Long>();
	          this.isNonText=this.quicktisInput.isQuickTxtMode.get();
	          
	          if(this.isNonText&&this.isReadDouble)
			  {
				  this.quicktisInput.quicktisInputVal.seek(pos2);
			  }

			}
		  
		  long freqPos;
		  String text="";
		  long textpos=0;
		  long vvvlong=0;
		  long lastfreqPointer=0;
		  long lasttxtPointer=0;
		  int doccount=0;
		  public boolean next() throws IOException
		  {
			  if(termNum>=this.termCount)
			  {
				  return false;
			  }
			  if(this.isNonText)
			  {
				  if((termNum & intervalMask)==0)
				  {
					  textpos=this.quicktisInput.quicktisInput.readVLong();
					  long tpos=lasttxtPointer+textpos;
					  this.lasttxtPointer=tpos;
					  pos.add(tpos);
				  }
				  
				  if(this.isReadDouble)
				  {
					  vvvlong=this.quicktisInput.quicktisInputVal.readVVVLong();
				  }else{
					  vvvlong=0l; 
				  }
				  
				  
				  
				  long pos=this.quicktisInput.quicktisInput.readVLong();
				  doccount=1;
				  if((pos&1)==0)
				  {
					  doccount=this.quicktisInput.quicktisInput.readVInt();
				  }
				  freqPos=(pos>>1)+lastfreqPointer;
				  this.lastfreqPointer=freqPos;
				  termNumRtn=termNum;
				  termNum++;
				  return true;
			  }
			  
			  if((termNum & intervalMask)==0)
			  {
				  text=this.quicktisInput.quicktisInput.readString();
				  this.ti.sizeOfStrings += text.length() << 1;
		          lst.add(text);
			  }
			  
			  vvvlong=this.quicktisInput.quicktisInput.readVVVLong();
			  doccount=this.quicktisInput.quicktisInput.readVInt();
			  long pos=this.quicktisInput.quicktisInput.readVLong();
			  freqPos=pos+lastfreqPointer;
			  this.lastfreqPointer=freqPos;
			  termNumRtn=termNum;
			  termNum++;
			  return true;
		  }
		  
		  int termNumRtn=-1;
		  
		  public int getTermNumber()
		  {
			  return this.termNumRtn;
		  }
		  public long getVVVlong()
		  {
			  return this.vvvlong;
		  }
		  
		  
		  public long getDocPos()
		  {
			  return freqPos;
		  }
		  
		  public int getDocCount()
		  {
			  return this.doccount;
		  }
		  
		  public void close()
		  {
			  this.ti.nTerms=termNumRtn;
			  this.ti.index=new IndexSearch();
			  if(this.isNonText)
			  {
				  this.ti.index.index=null;
				  if(pos==null)
				  {
					  this.ti.index.indexl=new long[0];
				  }else{
					  this.ti.index.indexl=new long[pos.size()];
					  for(int i=0;i<this.pos.size();i++)
					  {
						  this.ti.index.indexl[i]=this.pos.get(i); 
					  }
				  }
				  
				  this.ti.index.quicktisInput=this.quicktisInput;
				 

			  }else{
				  this.ti.index.indexl=null;
				  this.ti.index.index = lst!=null ? lst.toArray(new String[lst.size()]) : new String[0];
			  }
		  }
	  }
	  
	  public QuickNumberedTermEnum getEnumerator(SegmentReader reader,TermInfosReader.QuickInput quicktisInput, long pos,int cnt,Long pos2,boolean isReadDouble) throws IOException {
		  return new QuickNumberedTermEnum(this,quicktisInput, pos, cnt,pos2,isReadDouble);
	  }

		  
	  

	  /* The first time an enumerator is requested, it should be used
	     with next() to fully traverse all of the terms so the index
	     will be built.
	   */
	  public NumberedTermEnum getEnumerator(IndexReader reader) throws IOException {
	    if (index==null) return new NumberedTermEnum(reader,this, prefix==null?"":prefix, 0) {
	      ArrayList<String> lst;

	      @Override
	      protected boolean setTerm() {
	        boolean b = super.setTerm();
	        if (b && (pos & intervalMask)==0) {
	          String text = term().text();
	          sizeOfStrings += text.length() << 1;
	          if (lst==null) {
	            lst = new ArrayList<String>();
	          }
	          lst.add(text);
	        }
	        return b;
	      }

	      @Override
	      public boolean skipTo(Term target) throws IOException {
	        throw new UnsupportedOperationException();
	      }

	      @Override
	      public boolean skipTo(int termNumber) throws IOException {
	        throw new UnsupportedOperationException();
	      }

	      @Override
	      public void close() throws IOException {
	        nTerms=pos;
	        super.close();
	        index=new IndexSearch();
	        index.index = lst!=null ? lst.toArray(new String[lst.size()]) : new String[0];	        
	      }
	    };
	    else return new NumberedTermEnum(reader,this,"",0);
	  }

	  public long memSize() {
		  long size=0l;
		  if(index.index!=null)
		  {
			  size+=index.index.length<<3;
		  }
		  if(index.indexl!=null)
		  {
			  size+=index.indexl.length*(Long.SIZE/8);
		  }
	    return 8+8+8+8+size+sizeOfStrings;
	  }
}