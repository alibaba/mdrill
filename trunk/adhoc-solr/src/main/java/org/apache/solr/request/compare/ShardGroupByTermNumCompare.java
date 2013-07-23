package org.apache.solr.request.compare;

import java.io.Serializable;
import java.util.Comparator;

import org.apache.log4j.Logger;
import org.apache.solr.request.join.HigoJoinSort;
import org.apache.solr.request.mdrill.MdrillPorcessUtils.RefRowStat;

import com.alimama.mdrill.distinct.DistinctCount;


public class ShardGroupByTermNumCompare implements Comparator<ShardGroupByTermNum>,Serializable{
    private static Logger LOG = Logger.getLogger(ShardGroupByTermNumCompare.class);

	private static final long serialVersionUID = 1L;
	private UniqTypeNum.SortType typenum=null;
	private boolean isdesc=true;
	private Integer fl_num=0;
	private compareInterface cmpobj=null;
	
	public ShardGroupByTermNumCompare(String[] groupby,String[] crossFs, String[] distFS,HigoJoinSort[] joinSort,String fl,String type,boolean _isdesc) {
		this.isdesc=_isdesc;
		this.typenum=UniqTypeNum.parseType(type,fl,groupby,joinSort) ;
		this.cmpobj=new CompareIndex();
		LOG.info("####"+this.typenum.toString());

		if(this.typenum.typeEnum.equals(UniqTypeNum.SortTypeEnum.joincolumn))
		{
			this.fl_num=this.typenum.sortFieldNum;
			this.cmpobj=new CompareColumn();
			return ;
		}
		if(this.typenum.typeEnum.equals(UniqTypeNum.SortTypeEnum.index))
		{
			this.fl_num=0;
			this.cmpobj=new CompareIndex();
			return ;
		}
		if(this.typenum.typeEnum.equals(UniqTypeNum.SortTypeEnum.countall))
		{
			this.fl_num=0;
			this.cmpobj=new CompareCountall();
			return ;
		}
		if(this.typenum.typeEnum.equals(UniqTypeNum.SortTypeEnum.count))
		{
			this.fl_num=UniqTypeNum.foundIndex(crossFs, fl);
			this.cmpobj=new CompareCount();
			return ;
		}
		
		
		if(this.typenum.typeEnum.equals(UniqTypeNum.SortTypeEnum.dist))
		{
			this.fl_num=UniqTypeNum.foundIndex(distFS, fl);
			this.cmpobj=new CompareDist();
			return ;
		}
		
		if(this.typenum.typeEnum.equals(UniqTypeNum.SortTypeEnum.sum))
		{
			this.fl_num=UniqTypeNum.foundIndex(crossFs, fl);
			this.cmpobj=new CompareSum();
			return ;
		}
		
		if(this.typenum.typeEnum.equals(UniqTypeNum.SortTypeEnum.max))
		{
			this.fl_num=UniqTypeNum.foundIndex(crossFs, fl);
			this.cmpobj=new CompareMax();
			return ;
		}
		
		if(this.typenum.typeEnum.equals(UniqTypeNum.SortTypeEnum.min))
		{
			this.fl_num=UniqTypeNum.foundIndex(crossFs, fl);
			this.cmpobj=new CompareMin();
			return ;
		}
		
		if(this.typenum.typeEnum.equals(UniqTypeNum.SortTypeEnum.avg))
		{
			this.fl_num=UniqTypeNum.foundIndex(crossFs, fl);
			this.cmpobj=new CompareAvg();
			return ;
		}
		
		if(this.typenum.typeEnum.equals(UniqTypeNum.SortTypeEnum.column))
		{
			this.fl_num=UniqTypeNum.foundIndex(groupby, fl);
			this.cmpobj=new CompareColumn();
			return ;
		}
		

	}

	@Override
	public int compare(ShardGroupByTermNum o1,
			ShardGroupByTermNum o2) {
		int cmp = cmpobj.compare(o1, o2);
		if(this.isdesc)
		{
			return cmp;
		}
		return cmp*-1;
	}
	
	public interface compareInterface
	{
		public int compare(ShardGroupByTermNum o1, ShardGroupByTermNum o2);
	}
	
	public class CompareIndex implements compareInterface
	{
		@Override
		public int compare(ShardGroupByTermNum o1, ShardGroupByTermNum o2) {
			return UniqTypeNum.compare(o1.key.list, o2.key.list);
		}
	}
	
	public class CompareCountall implements compareInterface
	{
		CompareIndex index=new CompareIndex();
		@Override
		public int compare(ShardGroupByTermNum o1, ShardGroupByTermNum o2) {
			int cmp= UniqTypeNum.compare(o1.statVal.val, o2.statVal.val);
			if(cmp==0)
			{
				cmp=index.compare(o1, o2);
			}
			return cmp;
		}
	}
	public class CompareCount implements compareInterface
	{
		CompareIndex index=new CompareIndex();
		@Override
		public int compare(ShardGroupByTermNum o1, ShardGroupByTermNum o2) {
			
			int cmp= UniqTypeNum.compare(getCompareValue(o1,fl_num),getCompareValue(o2,fl_num));
			if(cmp==0)
			{
				cmp=index.compare(o1, o2);
			}
			return cmp;
		}
		
		public long getCompareValue(ShardGroupByTermNum g, Integer field) {
			RefRowStat s = g.gerRowStat(field);
			if (s != null) {
				return s.cnt;
			}
			return 0;
		}
	}
	
	public class CompareDist implements compareInterface
	{
		CompareIndex index=new CompareIndex();
		@Override
		public int compare(ShardGroupByTermNum o1, ShardGroupByTermNum o2) {
			
			int cmp= UniqTypeNum.compare(getCompareValue(o1,fl_num),getCompareValue(o2,fl_num));
			if(cmp==0)
			{
				cmp=index.compare(o1, o2);
			}
			return cmp;
		}
		
		public long getCompareValue(ShardGroupByTermNum g, Integer field) {
			DistinctCount dst=g.statVal.dist[field];
			if(dst!=null)
			{
				return dst.getValue();
			}else{
				return 0l;
			}
		}
	}
	
	public class CompareSum implements compareInterface
	{
		CompareIndex index=new CompareIndex();
		@Override
		public int compare(ShardGroupByTermNum o1, ShardGroupByTermNum o2) {
			
			int cmp= UniqTypeNum.compare(getCompareValue(o1,fl_num),getCompareValue(o2,fl_num));
			if(cmp==0)
			{
				cmp=index.compare(o1, o2);
			}
			return cmp;
		}
		
		public double getCompareValue(ShardGroupByTermNum g, Integer field) {
			RefRowStat s = g.gerRowStat(field);
			if (s != null) {
				return s.sum;
			}
			return 0;
		}
	}
	
	public class CompareMax implements compareInterface
	{
		CompareIndex index=new CompareIndex();
		@Override
		public int compare(ShardGroupByTermNum o1, ShardGroupByTermNum o2) {
			
			int cmp= UniqTypeNum.compare(getCompareValue(o1,fl_num),getCompareValue(o2,fl_num));
			if(cmp==0)
			{
				cmp=index.compare(o1, o2);
			}
			return cmp;
		}
		
		public double getCompareValue(ShardGroupByTermNum g, Integer field) {
			RefRowStat s = g.gerRowStat(field);
			if (s != null) {
				return s.max;
			}
			return 0;
		}
	}
	
	public class CompareMin implements compareInterface
	{
		CompareIndex index=new CompareIndex();
		@Override
		public int compare(ShardGroupByTermNum o1, ShardGroupByTermNum o2) {
			
			int cmp= UniqTypeNum.compare(getCompareValue(o1,fl_num),getCompareValue(o2,fl_num));
			if(cmp==0)
			{
				cmp=index.compare(o1, o2);
			}
			return cmp;
		}
		
		public double getCompareValue(ShardGroupByTermNum g, Integer field) {
			RefRowStat s = g.gerRowStat(field);
			if (s != null) {
				return s.min;
			}
			return 0;
		}
	}
	
	public class CompareAvg implements compareInterface
	{
		CompareIndex index=new CompareIndex();
		@Override
		public int compare(ShardGroupByTermNum o1, ShardGroupByTermNum o2) {
			
			int cmp= UniqTypeNum.compare(getCompareValue(o1,fl_num),getCompareValue(o2,fl_num));
			if(cmp==0)
			{
				cmp=index.compare(o1, o2);
			}
			return cmp;
		}
		
		public double getCompareValue(ShardGroupByTermNum g,Integer field) {
			RefRowStat s=g.gerRowStat(field);
			if(s!=null)
			{
				if(s.cnt>0)
				{
					return s.sum/s.cnt;
				}
			}
			return 0;
		}
	}
	
	public class CompareColumn implements compareInterface
	{
		CompareIndex index=new CompareIndex();
		@Override
		public int compare(ShardGroupByTermNum o1, ShardGroupByTermNum o2) {
			
			int cmp= UniqTypeNum.compare(o1.key.list[fl_num],o2.key.list[fl_num]);
			if(cmp==0)
			{
				cmp=index.compare(o1, o2);
			}
			return cmp;
		}
	}
	
}
