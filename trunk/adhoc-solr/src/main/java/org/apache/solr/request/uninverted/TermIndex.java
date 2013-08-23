package org.apache.solr.request.uninverted;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.SegmentReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TermIndex {
	  public static Logger log = LoggerFactory.getLogger(TermIndex.class);

	  final static int intervalBits = 7;  // decrease to a low number like 2 for testing
	  public final static int intervalMask = 0xffffffff >>> (32-intervalBits);
	  public final static int interval = 1 << intervalBits;

	  final Term fterm; // prototype to be used in term construction w/o String.intern overhead
	  final String prefix;
	  String[] index;
	  int nTerms;
	  long sizeOfStrings;
	  String field;

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
	  
	  Directory cachedir=null;
	  String key=null;
	 public void setCacheDir(Directory dir,String key)
	  {
		  cachedir=dir;
		  this.key=key;
	  }
	  
	  void setCache(IndexReader reader,Directory dir,String key) throws IOException {
		  String filename=this.field + ".index" + UnInvertedFieldBase.CacheVersion + "."+key;
			if (dir.fileExists(filename)) {
				IndexInput input =dir.openInput(filename);
				nTerms=input.readInt();
				sizeOfStrings=input.readLong();
				index=new String[input.readInt()];
				for(int i=0;i<index.length;i++)
				{
					index[i]=input.readString();
				}
				input.close();
			}else{
				NumberedTermEnum te=this.getEnumerator(reader);
				for (;;) {
					Term t = te.term();
					if (t == null)
						break;
					te.next();
				}
				 te.close();
			}
	  }
	  
	  public static class QuickNumberedTermEnum{
	      ArrayList<String> lst;
	      TermIndex ti;

		IndexInput quicktisInput;
		int termCount=0;
		int termNum=0;
		  public QuickNumberedTermEnum(TermIndex ti,IndexInput quicktisInput, long pos,int cnt) throws IOException {
			  this.ti=ti;
				this.quicktisInput = quicktisInput;
//				log.info("##QuickNumberedTermEnum##"+pos+"@"+cnt);
				this.quicktisInput.seek(pos);
				this.termCount=cnt;
				this.termNum=0;
	            this.lst = new ArrayList<String>();

			}
		  
		  long freqPos;
		  String text="";
		  long vvvlong=0;
		  long lastfreqPointer=0;
		  int doccount=0;
		  public boolean next() throws IOException
		  {
			  if(termNum>=this.termCount)
			  {
				  return false;
			  }
			  if((termNum & intervalMask)==0)
			  {
				  text=this.quicktisInput.readString();
				  this.ti.sizeOfStrings += text.length() << 1;
		          lst.add(text);
			  }
			  
			  vvvlong=this.quicktisInput.readVVVLong();
			  doccount=this.quicktisInput.readVInt();
			  long pos=this.quicktisInput.readVLong();
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
			  this.ti.index = lst!=null ? lst.toArray(new String[lst.size()]) : new String[0];
		  }
	  }
	  
	  public QuickNumberedTermEnum getEnumerator(SegmentReader reader,IndexInput quicktisInput, long pos,int cnt) throws IOException {
		  return new QuickNumberedTermEnum(this,(IndexInput)quicktisInput.clone(), pos, cnt);
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
	        index = lst!=null ? lst.toArray(new String[lst.size()]) : new String[0];
	        String filename=field + ".index" + UnInvertedFieldBase.CacheVersion + "."+key;
	        if(cachedir!=null)
	        {
	        	if(cachedir.fileExists(filename))
	        	{
	        		cachedir.deleteFile(filename);
	        	}
		        IndexOutput out=cachedir.createOutput(filename);
		        out.writeInt(nTerms);
		        out.writeLong(sizeOfStrings);
		        out.writeInt(index.length);
		        for(int i=0;i<index.length;i++)
		        {
		        	out.writeString(index[i]);
		        }
		        out.close();
	        }
	        
	      }
	    };
	    else return new NumberedTermEnum(reader,this,"",0);
	  }

	  public long memSize() {
	    return 8+8+8+8+(index.length<<3)+sizeOfStrings;
	  }
}