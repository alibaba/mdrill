package org.apache.solr.request.compare;

import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.solr.request.join.HigoJoinSort;

import com.alimama.mdrill.utils.EncodeUtils;

public class UniqTypeNum {
    private static Logger LOG = Logger.getLogger(UniqTypeNum.class);

	static HashMap<String,SortType> typeMap=new HashMap<String, SortType>();
	static{
		typeMap.put("index",  new SortType(0,SortTypeEnum.index));
		typeMap.put("countall", new SortType(1,SortTypeEnum.countall));
		typeMap.put("count", new SortType(2,SortTypeEnum.count));
		typeMap.put("dist", new SortType(3,SortTypeEnum.dist));
		typeMap.put("sum", new SortType(4,SortTypeEnum.sum));
		typeMap.put("max", new SortType(5,SortTypeEnum.max));
		typeMap.put("min", new SortType(6,SortTypeEnum.min));
		typeMap.put("avg", new SortType(7,SortTypeEnum.avg));
		typeMap.put("column",new SortType(8,SortTypeEnum.column));
		typeMap.put("joincolumn",new SortType(9,SortTypeEnum.joincolumn));
	}
	
	
	
	public static enum SortTypeEnum{
		none,index,countall,count,dist,sum,max,min,avg,column,joincolumn
	}
	public static SortType parseType(String type,String fl,String[] groupby,HigoJoinSort[] joinSort)
	{
		int start=groupby.length;
		for(HigoJoinSort s:joinSort)
		{
			if(s.getIndex()>=0)
			{
				start+=s.getIndex();
				SortType rtn=typeMap.get("joincolumn");	
				rtn.sortFieldNum=start;
				return rtn;
			}
			start+=s.getoffset();
		}
		SortType rtn=typeMap.get(type);	
		if(rtn==null)
		{
			return typeMap.get("index");
		}
		
		if(rtn.typeEnum.equals(SortTypeEnum.count)&&fl.indexOf("higoempty_")>=0)
		{
			return typeMap.get("countall");
		}
		
		
		return rtn;
	}
	
	public static class SelectDetailSort{
		public int sortIndex;
		public int offset;
		public int selfOffset;
		public String field;
		public SelectDetailSort(int sortIndex, int offset, int selfOffset,
				String field) {
			this.sortIndex = sortIndex;
			this.offset = offset;
			this.selfOffset = selfOffset;
			this.field = field;
		}
	}
	public static SelectDetailSort parseSelectDetailType(String[] groupby,HigoJoinSort[] joinSort)
	{
		int start=groupby.length;
		for(int i=0;i<joinSort.length;i++)
		{
			HigoJoinSort s=joinSort[i];
			if(s.getIndex()>=0)
			{
				start+=s.getIndex();
				return new SelectDetailSort(i,start, s.getIndex(), s.getSortField());
			}
			start+=s.getoffset();
		}
		
		return null;
	}
	
	public static Integer foundIndex(String[] list,String fl)
	{
		int rtn= foundIndex(list, fl, 0);
		LOG.info("####"+Arrays.toString(list)+"@"+fl+"@"+rtn);
		return rtn;
	}
	
	public static Integer foundIndex(String[] list,String fl,int def)
	{
		if(fl==null)
		{
			return def;
		}
		if(list!=null)
		{
			for(int i=0;i<list.length;i++)
			{
				if(fl.equals(list[i])){
					return i;
				}
			}
		}
		return def;
	}
	
	public static class SortType{
		public int sortFieldNum=-1;
		@Override
		public String toString() {
			return "SortType [sortFieldNum=" + sortFieldNum + ", typeNum="
					+ typeNum + ", typeEnum=" + typeEnum + "]";
		}
		public int typeNum;
		public SortTypeEnum typeEnum;
		public SortType(int typeNum, SortTypeEnum typeEnum) {
			this.typeNum = typeNum;
			this.typeEnum = typeEnum;
		}
	}
	
	public static int compare(double value1, double value2) {
		return value1 > value2 ? -1 : value1 < value2 ? 1 : 0;
    }
	 public static int compare(long value1, long value2) {
			return value1 > value2 ? -1 : value1 < value2 ? 1 : 0;
	    }
	 public static int compare(int value1, int value2) {
			return value1 > value2 ? -1 : value1 < value2 ? 1 : 0;
	    }

	public static int compare(int[] a, int[] a2) {
		int len = a.length;
		int cmp = compare(len, a2.length);
		if (cmp != 0) {
			return cmp;
		}

		for (int i = 0; i < len; i++) {
			cmp = compare(a[i], a2[i]);
			if (cmp != 0) {
				return cmp;
			}
		}

		return cmp;
	}
	 public static int compare(String value1, String value2) {
		 return value2.compareTo(value1);
	 }
	 
	 public static int compareStrNum(String value1, String value2) {
		 return compare(Double.parseDouble(filterUnNumber(value1)), Double.parseDouble(filterUnNumber(value2)));
	 }
	    public static String filterUnNumber(String str) {
	        String regEx = "[^0-9]";
	        Pattern p = Pattern.compile(regEx);
	        Matcher m = p.matcher(str);
	        String rtn= m.replaceAll("").trim();
	        if(rtn.isEmpty())
	        {
	        	return "0";
	        }
	        return rtn;

	    }
public static void main(String[] args) {

}
	
	 public static int compareDecode(String value1, String value2) {
			return compare(EncodeUtils.decode(value1),EncodeUtils.decode(value2));
	    }
	 
	 public static int compareDecodeNum(String value1, String value2) {
			return compareStrNum(EncodeUtils.decode(value1),EncodeUtils.decode(value2));
	    }
	
	public static int compareDecode(String[] a, String[] a2) {
		int len = a.length;
		int cmp = compare(len, a2.length);
		if (cmp != 0) {
			return cmp;
		}

		for (int i = 0; i < len; i++) {
			cmp = compareDecode(a[i], a2[i]);
			if (cmp != 0) {
				return cmp;
			}
		}

		return cmp;
	}
}
