package org.apache.solr.request.mdrill;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.util.OpenBitSet;


public class FdtMdrillCollector implements IndexSearcher.MdrillCollector{
	OpenBitSet bits;
	int limit;
	public OpenBitSet getBits() {
		return bits;
	}

	public FdtMdrillCollector( int limit ,int maxDoc) {
		this.limit=limit;
		bits = new OpenBitSet(maxDoc);
	}
	int base;
	int cnt=0;
	@Override
	public void setNextReader(IndexReader reader, int docBase)
			throws IOException {
		this.base=docBase;
	}
	
	@Override
	public boolean isstop() throws IOException {
		return cnt>this.limit;
	}
	
	@Override
	public void collect(int docid) throws IOException {
		cnt++;
		bits.fastSet(docid+this.base);
	}


}
