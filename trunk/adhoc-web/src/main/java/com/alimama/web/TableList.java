package com.alimama.web;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.alimama.mdrill.partion.GetShards;
import com.alipay.bluewhale.core.cluster.ShardsState;
import com.alipay.bluewhale.core.cluster.SolrInfo;
import com.alipay.bluewhale.core.cluster.StormClusterState;
import com.alipay.bluewhale.core.cluster.SolrInfo.ShardCount;


public class TableList {
	public static String[] getTablelist() throws Exception
	{
		StormClusterState stat=GetShards.getCluster();
		List<String> list=stat.higo_tableList();
		String[] rtn=new String[list.size()];
		return list.toArray(rtn);
	}
	
	
	public static String[] getTableShards(String tableName)throws Exception
	{
		StormClusterState zkCluster = GetShards.getCluster();
		List<Integer> list=zkCluster.higo_ids(tableName);
		List<String> listrtntotal=new ArrayList<String>();
		List<String> listrtn=new ArrayList<String>();
		listrtntotal.add("进程数:"+list.size()+"<br>");
		Long recordCount=0l;
	    HashMap<String,Long> partionCount = new HashMap<String, Long>();
	    HashMap<String,Long> dayCount = new HashMap<String, Long>();
	    HashMap<String,Long> dayAmt = new HashMap<String, Long>();

		for(Integer id:list)
		{
		    SolrInfo info=zkCluster.higo_info(tableName, id);
		    if(info!=null)
		    {
		    	if(info.stat==ShardsState.SERVICE)
		    	{
		    		for(Entry<String, ShardCount> e:info.recorecount.entrySet())
		    		{
		    			Long sc=partionCount.get(e.getKey());
		    			if(sc==null)
		    			{
		    				sc=0l;
		    			}
		    			sc+=e.getValue().cnt;
		    			recordCount+=e.getValue().cnt;
		    			partionCount.put(e.getKey(), sc);
		    		}
		    		
		    		for(Entry<String, ShardCount> e:info.daycount.entrySet())
		    		{
		    			Long sc=dayCount.get(e.getKey()+"@"+info.replicationindex);
		    			long cnt=e.getValue().cnt;
		    			if(sc==null)
		    			{
		    				sc=0l;
		    			}
		    			sc+=cnt;
		    			dayCount.put(e.getKey()+"@"+info.replicationindex, sc);
		    			
		    			
		    			Long amt=dayAmt.get(e.getKey()+"@"+info.replicationindex);
		    			if(amt==null)
		    			{
		    				amt=0l;
		    			}
		    			if(cnt>1000)
		    			{
		    				amt+=1;
		    			}
		    			dayAmt.put(e.getKey()+"@"+info.replicationindex, amt);

		    		}
		    	}
		    	
		    	StringBuffer buff=new StringBuffer();
		    	buff.append("<tr style=\"background:#FFFFCC; font:bold\">");
		    	buff.append("<td>"+info.isMergeServer+"</td>");
		    	buff.append("<td>"+info.taskIndex+"@"+info.replicationindex+"@"+info.replication+"</td>");
		    	buff.append("<td>"+info.hostname+"</td>");
		    	buff.append("<td>"+info.localip + ":"+info.port+"</td>");
		    	buff.append("<td>"+info.workport+"</td>");
		    	buff.append("<td>"+info.processId+"</td>");
		    	buff.append("<td>"+info.stat+"</td>");
		    	buff.append("<td>"+info.memInfo+"</td>");
		    	
		    	buff.append("<td>"+info.hdfsfolder+"</td>");
		    	
		    	buff.append("</tr>");
		    	
		    	
		    	SimpleDateFormat fmt=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		    	String yyyymmmddd=fmt.format(new Date(info.times));
		    	String yyyymmmdddstart=fmt.format(new Date(info.startTimes));
		    	String yyyymmmdddhb=fmt.format(new Date(info.solrhbTimes));
			    	
		    	buff.append("<tr>");
		    	buff.append("<td colspan=10>"+"solr探测时间:"+yyyymmmdddhb+"&nbsp;&nbsp;&nbsp;&nbsp;"+"最后一次心跳时间:"+yyyymmmddd+"&nbsp;&nbsp;&nbsp;&nbsp;"+"启动时间:"+yyyymmmdddstart+"</td>");
		    	buff.append("</tr>");
		    	

		    	
		    	if(!info.isMergeServer)
		    	{
			    	if(info.recorecount.size()>0)
			    	{
			    		buff.append("<tr>");
				    	buff.append("<td>分区心跳：</td>");
				    	buff.append("<td colspan=9>"+info.recorecount.toString()+"</td>");
				    	buff.append("</tr>");
			    	}
			    	if(info.daycount.size()>0)
			    	{
			    		buff.append("<tr>");
				    	buff.append("<td>额外监控：</td>");
				    	buff.append("<td colspan=9>"+info.daycount.toString()+"</td>");
				    	buff.append("</tr>");
			    	}
		    		
		    		buff.append("<tr>");
			    	buff.append("<td>本地硬盘路径：</td>");
			    	buff.append("<td colspan=9>"+info.localpath+"</td>");
			    	buff.append("</tr>");
			    	buff.append("<tr>");
			    	buff.append("<td>hdfs存储路径：</td>");
			    	buff.append("<td colspan=9>"+info.hdfsPath+"</td>");
			    	buff.append("</tr>");
			    	
			    	 
		    	}
		    	
		    	
		    	listrtn.add(buff.toString());
		    }
		}
		
    	StringBuffer buff=new StringBuffer();
    	buff.append("<tr>");
    	buff.append("<td>merger server</td>");
    	buff.append("<td>replication</td>");
    	buff.append("<td>域名</td>");
    	buff.append("<td>solr地址</td>");
    	buff.append("<td>蓝鲸端口</td>");
    	buff.append("<td>worker进程ID</td>");
    	buff.append("<td>健康状态</td>");
    	buff.append("<td>内存</td>");
    	buff.append("<td>hdfs分区</td>");
    	buff.append("</tr>");

		
		listrtntotal.add("总记录数="+recordCount+"<br>");
		listrtntotal.add("分区记录数："+"<br>");
		List<String> tmp=new ArrayList<String>();

		for(Entry<String,Long> e:partionCount.entrySet())
		{
			tmp.add(""+e.getKey()+"="+e.getValue()+"<br>");
		}
		Collections.sort(tmp);

		listrtntotal.addAll(tmp);
		listrtntotal.add("起始分区每天记录数："+"<br>");
		tmp=new ArrayList<String>();
		for(Entry<String,Long> e:dayCount.entrySet())
		{
			tmp.add(" day:"+e.getKey()+"="+e.getValue()+"<br>");
		}
		Collections.sort(tmp);

		listrtntotal.addAll(tmp);
		listrtntotal.add("起始分区每天有效shard数："+"<br>");
		tmp=new ArrayList<String>();
		for(Entry<String,Long> e:dayAmt.entrySet())
		{
			tmp.add(e.getKey()+"="+e.getValue()+"<br>");
		}
		Collections.sort(tmp);

		listrtntotal.addAll(tmp);
		Collections.sort(listrtn);
		
		List<String> finalrtnlist=new ArrayList<String>();
		finalrtnlist.addAll(listrtntotal);
		finalrtnlist.add("<hr><table border=1 >");
		finalrtnlist.add(buff.toString());
		finalrtnlist.addAll(listrtn);
		finalrtnlist.add("</table>");

		
		String[] rtn=new String[finalrtnlist.size()];

		return finalrtnlist.toArray(rtn);
	}
	
	
	
}
