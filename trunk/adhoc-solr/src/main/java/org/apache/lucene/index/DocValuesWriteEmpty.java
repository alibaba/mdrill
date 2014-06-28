package org.apache.lucene.index;

import java.io.IOException;

import org.apache.lucene.store.IndexOutput;

public class DocValuesWriteEmpty implements DocValuesWriter{

	public void collectDoc(int docid, int termNum) {}
	public void collectTmIndex(String termValue) throws IOException {}

	public void collectTm(long termValue) throws IOException {}

	public void start(int fieldNumber,String field) {}

	public void flushFieldDoc(int termNum)throws IOException {
		
	}

	public void flushPosTo(IndexOutput outputSize) throws IOException {}


	public void free() {
	}
	@Override
	public void close()  throws IOException  {
		
	}

}
