package org.apache.solr.request.compare;

import java.io.Serializable;
import java.util.Comparator;

import org.apache.solr.request.compare.ShardGroupByGroupbyRowCompare.CompareColumn;
import org.apache.solr.request.compare.ShardGroupByGroupbyRowCompare.CompareColumnNum;


public class ShardDetailSelectDetailRowStringCompare implements Comparator<SelectDetailRow>,Serializable{

	private static final long serialVersionUID = 1L;
	private boolean isdesc=true;
	private compareInterface cmpobj=null;
	
	public ShardDetailSelectDetailRowStringCompare(String columntype,boolean _isdesc) {
		this.isdesc=_isdesc;
		if(columntype.equals("string"))
		{
			this.cmpobj=new CompareColumn();
		}else{
			this.cmpobj=new CompareColumnNum();
		}
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
			int cmp=0;
			if(o1.colVal!=null&&o2.colVal!=null)
			{
				cmp= UniqTypeNum.compare(o1.colVal,o2.colVal);//字符串类型的比较
			}else
			{
				cmp= UniqTypeNum.compare(o1.getCompareValue(),o2.getCompareValue());//数值型比较
			}
			if(cmp==0)
			{
				cmp=index.compare(o1, o2);
			}
			return cmp;
		}
	}
	
	
	public class CompareColumnNum implements compareInterface
	{
		CompareIndex index=new CompareIndex();
		@Override
		public int compare(SelectDetailRow o1, SelectDetailRow o2) {
			int cmp=0;
			if(o1.colVal!=null&&o2.colVal!=null)
			{
				cmp= UniqTypeNum.compareStrNum(o1.colVal,o2.colVal);//字符串类型的比较
			}else
			{
				cmp= UniqTypeNum.compare(o1.getCompareValue(),o2.getCompareValue());//数值型比较
			}
			if(cmp==0)
			{
				cmp=index.compare(o1, o2);
			}
			return cmp;
		}
	}
	
}
