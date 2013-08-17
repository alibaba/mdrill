package org.apache.solr.request.join;

import org.apache.solr.request.join.HigoJoin.IntArr;
import org.apache.solr.request.uninverted.GrobalCache;
import org.apache.solr.search.DocSet;

public interface HigoJoinInterface extends GrobalCache.ILruMemSizeCache{
	public DocSet filterByRight(DocSet leftDocs,DocSet rightDocs);
	public IntArr getRight(int leftDocid,int termNum);


}
