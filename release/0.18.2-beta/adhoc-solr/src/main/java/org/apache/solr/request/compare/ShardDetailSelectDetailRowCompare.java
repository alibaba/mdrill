package org.apache.solr.request.compare;

import java.io.Serializable;
import java.util.Comparator;

import org.apache.log4j.Logger;

public class ShardDetailSelectDetailRowCompare implements Comparator<SelectDetailRow>,Serializable{
	private static Logger LOG = Logger.getLogger(ShardDetailSelectDetailRowCompare.class);
	private static final long serialVersionUID = 1L;
	private boolean isdesc=true;
	private compareInterface cmpobj=null;
	
	public ShardDetailSelectDetailRowCompare(boolean _isdesc) {
		this.isdesc=_isdesc;
		this.cmpobj=new CompareColumn();
	}

	@Override
	public int compare(SelectDetailRow o1,
			SelectDetailRow o2) {
		int cmp = cmpobj.compare(o1, o2);
		if(this.isdesc)
		{
			return cmp;
		}
		return cmp*-1;
	}
	
	public interface compareInterface
	{
		public int compare(SelectDetailRow o1, SelectDetailRow o2);
	}
	
	public class CompareIndex implements compareInterface
	{
		@Override
		public int compare(SelectDetailRow o1, SelectDetailRow o2) {
			return UniqTypeNum.compare(o1.docid, o2.docid);
		}
	}
	
	public class CompareColumn implements compareInterface
	{
		CompareIndex index=new CompareIndex();
		@Override
		public int compare(SelectDetailRow o1, SelectDetailRow o2) {

			int cmp= UniqTypeNum.compare(o1.getCompareValue(),o2.getCompareValue());
			if(cmp==0)
			{
				cmp=index.compare(o1, o2);
			}
			return cmp;
		}
	}
	
}
