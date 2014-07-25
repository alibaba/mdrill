package com.alimama.quanjingmonitor.kmeans;

import java.util.HashMap;
import java.util.HashSet;

public class PrintSql {
	/**
	 * 子落 (2014-04-15 10:38:37): 
就是强制  指标必须在一定范围内 才能分到一起的那个 

张壮 (2014-04-15 10:48:54): 
维度：主营类目、B|C、星级
指标：笔单价、财务消耗
子落 (2014-04-15 10:49:41): 
就这几个啊 

张壮 (2014-04-15 10:49:46): 
时间周期：2014-4-2~2014-4-15
张壮 (2014-04-15 10:51:03): 
样本量
1000-15000
1000-1500

	 * @param args
	 */
	public static void main(String[] args) {
		String[] colls_important={"main_cat_name"};
		String[] number_important={"(avg(case when (alipay_direct_num+alipay_indirect_num)=0 then 0.00001 else ((alipay_direct_amt+alipay_indirect_amt)/(alipay_direct_num+alipay_indirect_num)) end))"};
		String[] colls={"bc_sellers","seller_star_name"};
		String[] numbers={"finprice"};
		String[] thedates={"20140415","20140402","20140403","20140404","20140405","20140406","20140407","20140408","20140409","20140410","20140412","20140412","20140414"};//

		makeSql("cust_table", "1=1", "custid", thedates, colls_important, number_important, colls, numbers);
		
//		System.out.println(ajustName("c(ust_1d@#$64fd"));
	}

	public static String ajustName(String cols)
	{
		String rtn=cols.replaceAll("[^A-Za-z0-9_]*", "");
		
		if(rtn.length()>20)
		{
			return rtn.substring(0, 20);
		}
		
		return rtn;
	}
	public static String[] makeSql(String tablename,String sqlwhere,String idcols,String[] thedates_a,String[] colls_important,String[] number_important,String[] colls,String[] numbers)
	{
		
		String[] thedates=thedates_a;
		if(thedates.length==1)
		{
			thedates=new String[]{thedates_a[0],thedates_a[0]};
		}
		
		if(sqlwhere.trim().isEmpty())
		{
			sqlwhere="1=1";
		}
		
		sqlwhere=sqlwhere.replaceAll("^[ |\t]+where", " ");
		
		HashMap<String, String> alias_name =new HashMap<String, String>();
		 HashSet<String> groupbyrepeat=new HashSet<String>();
		 

		int index=0;
		
		StringBuffer sqlselectbuff=new StringBuffer();
		StringBuffer groupBybuf=new StringBuffer();
		
		StringBuffer sqlBaseFilterbuff=new StringBuffer();

		alias_name.put(idcols, "k_"+ajustName(idcols)+"_"+index++);
		
		sqlselectbuff.append(" ").append(idcols).append(" as ").append(alias_name.get(idcols));
		groupBybuf.append(" ").append(idcols);

		
		sqlBaseFilterbuff.append(""+idcols+"<>'' and "+idcols+" is not null");

		for(String s:colls_important)
		{
			if(s.equals(idcols))
			{
				continue;
			}
			
			alias_name.put(s, "hs_"+ajustName(s)+"_"+index++);
			sqlselectbuff.append(" ,").append(s).append(" as ").append(alias_name.get(s));
			if(!groupbyrepeat.contains(s))
			{
				groupbyrepeat.add(s);
			groupBybuf.append(",").append(s);
			}

			sqlBaseFilterbuff.append(" and "+s+" is not null");
		}
		
		for(String s:number_important)
		{
			alias_name.put(s, "hn_"+ajustName(s)+"_"+index++);
			
			boolean isaddavg=true;
			if(s.trim().toLowerCase().startsWith("sum")||s.trim().toLowerCase().startsWith("max")||s.trim().toLowerCase().startsWith("min")||s.trim().toLowerCase().startsWith("avg")||s.trim().toLowerCase().startsWith("average")||s.trim().toLowerCase().startsWith("skip"))
			{
				isaddavg=false;
			}
			
			
			if(isaddavg)
			{
				sqlselectbuff.append(" ,").append("avg("+s+")").append(" as ").append(alias_name.get(s));

			}else{
				sqlselectbuff.append(" ,").append(s.replaceAll("average\\(", "avg(").replaceAll("skip\\(", "(")).append(" as ").append(alias_name.get(s));

			}
		}
		
		for(String s:colls)
		{
			if(s.equals(idcols))
			{
				continue;
			}
			
			alias_name.put(s, "ls_"+ajustName(s)+"_"+index++);
			sqlselectbuff.append(" ,").append(s).append(" as ").append(alias_name.get(s));
			
			if(!groupbyrepeat.contains(s))
			{
				groupbyrepeat.add(s);
				groupBybuf.append(",").append(s);
			}
			sqlBaseFilterbuff.append(" and "+s+" is not null");


		}
		
		for(String s:numbers)
		{
			alias_name.put(s, "ln_"+ajustName(s)+"_"+index++);
			
			boolean isaddavg=true;
			if(s.trim().toLowerCase().startsWith("sum")||s.trim().toLowerCase().startsWith("max")||s.trim().toLowerCase().startsWith("min")||s.trim().toLowerCase().startsWith("avg")||s.trim().toLowerCase().startsWith("average")||s.trim().toLowerCase().startsWith("skip"))
			{
				isaddavg=false;
			}
			
			if(isaddavg)
			{
				sqlselectbuff.append(" ,").append("avg("+s+")").append(" as ").append(alias_name.get(s));

			}else{
				sqlselectbuff.append(" ,").append(s.replaceAll("average\\(", "avg(").replaceAll("skip\\(", "(")).append(" as ").append(alias_name.get(s));
			}

		}
		
		
		String sqlselect=sqlselectbuff.toString();
		
		String groupBy=groupBybuf.toString();
		
		String sqlBaseFilter="("+sqlBaseFilterbuff.toString()+") ";
	
		StringBuffer buff=new StringBuffer();
		
		index=0;
		buff.append("select tbl_0."+alias_name.get(idcols)+" as col_"+index+" ");
		index++;
		
		StringBuffer[] bufferIndex={new StringBuffer(),new StringBuffer(),new StringBuffer(),new StringBuffer()};
		for(int i=0;i<1;i++)
		{
			for(String s:colls_important)
			{
				if(s.equals(idcols))
				{
					continue;
				}
				
				buff.append(",tbl_"+i+"."+alias_name.get(s)+" as col_"+index);
				bufferIndex[0].append(index).append(",");
				index++;
			}
		}
		for(int i=0;i<1;i++)
		{
			for(String s:number_important)
			{
				buff.append(",tbl_"+i+"."+alias_name.get(s)+" as col_"+index);
				bufferIndex[1].append(index).append(",");
				index++;
			}
		}
		for(int i=0;i<1;i++)
		{
			for(String s:colls)
			{
				if(s.equals(idcols))
				{
					continue;
				}
				
				buff.append(",tbl_"+i+"."+alias_name.get(s)+" as col_"+index);
				bufferIndex[2].append(index).append(",");
				index++;
			}
		}
		for(int i=1;i<thedates.length;i++)
		{
			for(String s:number_important)
			{
				buff.append(",tbl_"+i+"."+alias_name.get(s)+" as col_"+index);
				bufferIndex[3].append(index).append(",");
				index++;
			}
		}
		for(int i=0;i<thedates.length;i++)
		{
			for(String s:numbers)
			{
				buff.append(",tbl_"+i+"."+alias_name.get(s)+" as col_"+index);
				bufferIndex[3].append(index).append(",");
				index++;
			}
		}
		
		buff.append(" from (select "+sqlselect+" from "+tablename+" where dt='"+thedates[0]+"' and "+sqlBaseFilter+" and ("+sqlwhere+") group by "+groupBy+") tbl_0 ");

		for(int i=1;i<thedates.length;i++)
		{
			buff.append(" left outer join (select "+sqlselect+" from "+tablename+" where dt='"+thedates[i]+"' and "+sqlBaseFilter+" and ("+sqlwhere+")  group by "+groupBy+") tbl_"+i+" on (tbl_"+i+"."+alias_name.get(idcols)+"=tbl_0."+alias_name.get(idcols)+")");
		}
		
		System.out.println(buff.toString());
		
		StringBuffer kmeansarams=new StringBuffer();
		kmeansarams.append(bufferIndex[0].toString().replaceAll(",$", "")).append(";");
		kmeansarams.append(bufferIndex[1].toString().replaceAll(",$", "")).append(";");
		kmeansarams.append(bufferIndex[2].toString().replaceAll(",$", "")).append(";");
		kmeansarams.append(bufferIndex[3].toString().replaceAll(",$", ""));
		
		System.out.println(kmeansarams.toString());
		
		return new String[]{buff.toString(),kmeansarams.toString()};
		
		
	
	}
}
