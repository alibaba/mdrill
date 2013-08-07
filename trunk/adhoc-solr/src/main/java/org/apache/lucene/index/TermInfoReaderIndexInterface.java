package org.apache.lucene.index;

import java.io.IOException;

import org.apache.lucene.util.BytesRef;

public interface TermInfoReaderIndexInterface {
	  void seekEnum(SegmentTermEnum enumerator, int indexOffset) throws IOException ;
	  int getIndexOffset(Term term, BytesRef termBytesRef) throws IOException ;
	  int length() ;
	  int compareTo(Term term, BytesRef termBytesRef, int termIndex) throws IOException ;

}
