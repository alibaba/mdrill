package org.apache.lucene.index;


import java.io.IOException;

import org.apache.lucene.store.IndexInput;
import org.apache.lucene.util.BytesRef;


/**
 * This stores a monotonically increasing set of <Term, TermInfo> pairs in an
 * index segment. Pairs are accessed either by Term or by ordinal position the
 * set. The Terms and TermInfo are actually serialized and stored into a byte
 * array and pointers to the position of each are stored in a int array.
 */
class TermInfosReaderIndexQuick implements TermInfoReaderIndexInterface{
	TermBuffer buff=new TermBuffer();
	private static long HEAD_SIZE=(Integer.SIZE*4+Long.SIZE*1)/8;
	private static long ITEM_SIZE=(Integer.SIZE*2+Long.SIZE*4)/8;
	  public static class TiiInfo{
		  int docFreq;
		  long freqPointer;
		  long proxPointer;
		  int skipOffset;
		  long tispos;
		  long qtiipos;
		  Term tm;
	  }
  private TiiInfo[] termInfoArr;
  private int totalIndexInterval;
  private final int skipInterval;
  IndexInput tiiInput;
  IndexInput quickTii;
  FieldInfos fieldInfos;
  private final int indexSize;

  TermInfosReaderIndexQuick( IndexInput tiiInput,IndexInput quickTii,  FieldInfos fieldInfos,long tiifilesize, int indexDivisor, long tiiFileLength, int totalIndexInterval) throws IOException {
	 this.tiiInput=tiiInput;
	 this.quickTii=quickTii;
	 this.fieldInfos=fieldInfos;
    this.totalIndexInterval = totalIndexInterval;
 
    
    int FORMAT_CURRENT=tiiInput.readInt();
    
    if (FORMAT_CURRENT > TermInfosWriter.FORMAT_VERSION_UTF8_LENGTH_IN_BYTES) {
    	buff.setPreUTF8Strings();
    }

    long QUICK_TII=tiiInput.readLong();
    int indexInterval=tiiInput.readInt();
    skipInterval=tiiInput.readInt();
    int maxSkipLevels=tiiInput.readInt();
    termInfoArr = new TiiInfo[(int) tiifilesize];
    
    indexSize = 1 + ((int) tiifilesize - 1) / indexDivisor;

  }


  public void seekEnum(SegmentTermEnum enumerator, int indexOffset) throws IOException {
	  IndexInput tii= (IndexInput)this.tiiInput.clone();
	  IndexInput qtii=(IndexInput)this.quickTii.clone();
		TiiInfo ti=this.getTiiInfo(indexOffset, tii, qtii);


    // read the term
    Term term = ti.tm;

    // read the terminfo
    TermInfo termInfo = new TermInfo();
    termInfo.docFreq = ti.docFreq;
    if (termInfo.docFreq >= skipInterval) {
      termInfo.skipOffset = ti.skipOffset;
    } else {
      termInfo.skipOffset = 0;
    }
    termInfo.freqPointer = ti.freqPointer;
    termInfo.proxPointer =ti.proxPointer;

    long pointer =ti.tispos;

    // perform the seek
    enumerator.seek(pointer, ((long) indexOffset * totalIndexInterval) - 1, term, termInfo);
  }

  /**
   * Binary search for the given term.
   * 
   * @param term
   *          the term to locate.
   * @throws IOException 
   */
  public int getIndexOffset(Term term, BytesRef termBytesRef) throws IOException {
	  IndexInput tii= (IndexInput)this.tiiInput.clone();
	  IndexInput qtii=(IndexInput)this.quickTii.clone();
    int lo = 0;
    int hi = termInfoArr.length-1;
    while (hi >= lo) {
      int mid = (lo + hi) >>> 1;
      int delta = compareTo(term, termBytesRef, mid,tii, qtii,new BytesRef());
      if (delta < 0)
        hi = mid - 1;
      else if (delta > 0)
        lo = mid + 1;
      else
        return mid;
    }
    return hi;
  }

  

  public int length() {
    return indexSize;
  }


  public int compareTo(Term term, BytesRef termBytesRef, int termIndex) throws IOException {
    return compareTo(term, termBytesRef, termIndex,(IndexInput)this.tiiInput.clone(),(IndexInput)this.quickTii.clone(),new BytesRef());
  }


  private int compareTo(Term term, BytesRef termBytesRef, int termIndex, IndexInput tiiInput, IndexInput quickTii, BytesRef reuse) throws IOException {
		 TiiInfo ti=this.getTiiInfo(termIndex, tiiInput, quickTii);
    return term.compareTo(ti.tm);
  }
//  output.writeInt(ti.docFreq);                       // write doc freq
//  output.writeLong(ti.freqPointer); // write pointers
//  output.writeLong(ti.proxPointer);
//  output.writeInt(ti.skipOffset);
//  output.writeLong(other.output.getFilePointer());
//  output.writeLong(this.outputQuickTii.getFilePointer());
//  this.writeTermTii(fieldNumber, termBytes, termBytesLength);
  private TiiInfo getTiiInfo(int termIndex,   IndexInput tiiInput, IndexInput quickTii) throws IOException {
	  TiiInfo ti=null;
	  synchronized (this.termInfoArr) {
		  ti=this.termInfoArr[termIndex];
		  if(ti==null)
		  {
			  long pos=TermInfosReaderIndexQuick.HEAD_SIZE+termIndex*TermInfosReaderIndexQuick.ITEM_SIZE;
			    tiiInput.seek(pos);
	
			    ti=new TiiInfo();
			    ti.docFreq=tiiInput.readInt();
			    ti.freqPointer=tiiInput.readLong();
			    ti.proxPointer=tiiInput.readLong();
			    ti.skipOffset=tiiInput.readInt();
			    ti.tispos=tiiInput.readLong();
			    ti.qtiipos=tiiInput.readLong();
			    quickTii.seek(ti.qtiipos);
			    buff.readTiiQuick(quickTii, fieldInfos);
			    ti.tm=buff.toTerm();
			    this.termInfoArr[termIndex]=ti;

	  }
	}
   
    return ti;
  }
}
