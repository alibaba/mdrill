package org.apache.solr.request.uninverted;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;

public class TermIndex {
//	  public static Logger log = LoggerFactory.getLogger(TermIndex.class);

	  final static int intervalBits = 7;  // decrease to a low number like 2 for testing
	  public final static int intervalMask = 0xffffffff >>> (32-intervalBits);
	  public final static int interval = 1 << intervalBits;

	  final Term fterm; // prototype to be used in term construction w/o String.intern overhead
	  final String prefix;
	 

	  int nTerms;
	  long sizeOfStrings;
	  String field;
	  IndexSearch index;

	public static class IndexSearch {
		String[] index;

		@Override
		public String toString() {
			return "IndexSearch [index=" + Arrays.toString(index) + "]";
		}

		public String get(int i) throws IOException {
			if (index == null || i >= index.length) {
				return "null";
			}
			return index[i];

		}

		public int search(String s) throws IOException {
			return Arrays.binarySearch(index, s);
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

	 public TermNumEnumerator getEnumerator(IndexReader reader, int termNumber) throws IOException {
	    TermNumEnumerator te = new TermNumEnumerator(reader, this);
	    te.skipTo(termNumber);
	    return te;
	  }
	  
	
	

	  /* The first time an enumerator is requested, it should be used
	     with next() to fully traverse all of the terms so the index
	     will be built.
	   */
	  public TermNumEnumerator getEnumerator(IndexReader reader) throws IOException {
	    if (index==null) return new TermNumEnumerator(reader,this, prefix==null?"":prefix, 0) {
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
	    else return new TermNumEnumerator(reader,this,"",0);
	  }

	  public long memSize() {
		  long size=0l;
		  if(index.index!=null)
		  {
			  size+=index.index.length<<3;
		  }
	    return 8+8+8+8+size+sizeOfStrings;
	  }
}