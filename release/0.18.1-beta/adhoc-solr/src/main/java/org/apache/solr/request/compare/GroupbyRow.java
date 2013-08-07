package org.apache.solr.request.compare;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.solr.common.util.NamedList;

import com.alimama.mdrill.distinct.DistinctCount;

/**
 * 用于记录groupby的每条记录以及统计值
 * @author yannian.mu
 */
public class GroupbyRow implements Comparable<GroupbyRow>, GroupbyItem{
	public Integer docidkey;//仅仅用于明细
	private String key;
	private long value;
	private ArrayList<ArrayList<Double>> stat=new ArrayList<ArrayList<Double>>();
	public ArrayList<DistinctCount> dist=new ArrayList<DistinctCount>();

	private boolean isFinalResult=false;
	
	private String[] crossFs;
	private String[] distFS;
	
	public void setKey(String key) {
		this.key = key;
	}
	
	public boolean isFinalResult() {
		return isFinalResult;
	}

	public void setFinalResult(boolean isFinalResult) {
		this.isFinalResult = isFinalResult;
	}
	
	public void setCross(String[] crossFs,String[] distFS){
		this.crossFs=crossFs;
		this.distFS=distFS;
		
		if(this.crossFs!=null)
		{
			int diff=this.crossFs.length-stat.size();
			for(int i=diff;i>0;i--)
			{
				stat.add(new ArrayList<Double>(Arrays.asList(this.mkStat())));
			}
		}
		
		if(this.distFS!=null)
		{
			int diff=this.distFS.length-dist.size();
			for(int i=diff;i>0;i--)
			{
				dist.add(new DistinctCount());
			}
		}
		
	}

	
	public GroupbyRow() {
	}
	
	public GroupbyRow(String key, Long value) {
		this.key = key;
		this.value = value;
	}
	
	public GroupbyRow(String key,NamedList nst)
	{
		this.key=key;
		this.value=(Long) nst.get("count");
		this.stat=(ArrayList<ArrayList<Double>>) nst.get("stat");
		ArrayList<byte[]> compress=(ArrayList<byte[]>) nst.get("dist");
		int compresssize=compress.size();
		this.dist=new ArrayList<DistinctCount>(compresssize);
		for(int i=0;i<compresssize;i++)
		{
			this.dist.add(new DistinctCount(compress.get(i)));
		}
	}
	
	public void shardsMerge(GroupbyItem o)
	{
		GroupbyRow instance=(GroupbyRow)o;
		if (this.key == null || this.key.isEmpty()) {
			this.key = instance.key;
		}
		this.value += instance.value;
		this.mergeStat((GroupbyRow)o);
		this.mergeDist((GroupbyRow)o);
	}
	
	private void mergeDist(GroupbyRow o)
	{
		int compresssize=o.dist.size();
		for(int i=0;i<compresssize;i++)
		{
			DistinctCount vv =o.dist.get(i);
			DistinctCount curr=this.dist.get(i);
			curr.merge(vv);
		}
	}
	
	private void mergeStat(GroupbyRow o)
	{
		int statsize=stat.size();
		for(int field=0;field<statsize;field++)
		{
			ArrayList<Double> vv = o.stat.get(field);
			int sz=vv.size();
			for(int i=0;i<sz;i++)
			{
				this.addStat(field, i, vv.get(i));
			}
		}
	}
	
	public boolean isrecordcount(){
		return false;
	}
	
	private Double[] mkStat()
	{
		return new Double[]{0d,0d,0d,0d,0d};
	}
	
	public NamedList toNamedList()
	{
		NamedList rtn=new NamedList();
		rtn.add("count", value);
		if(isFinalResult)
		{
			HashMap<String,ArrayList<Double>> finalResult=new HashMap<String, ArrayList<Double>>();
			for(int field=0;field<this.dist.size();field++)
			{
				DistinctCount vv = this.dist.get(field);
				ArrayList<Double> result=finalResult.get(this.distFS[field]);
				if(result==null)
				{
					result=new ArrayList<Double>(Arrays.asList(this.mkStat()));
					finalResult.put(this.distFS[field], result);
				}
				result.set(0, (double)vv.getValue());
			}
			
			for(int field=0;field<this.stat.size();field++)
			{
				ArrayList<Double> vv = this.stat.get(field);
				ArrayList<Double> result=finalResult.get(this.crossFs[field]);
				if(result==null)
				{
					result=new ArrayList<Double>(Arrays.asList(this.mkStat()));
					finalResult.put(this.crossFs[field], result);
				}
					int size=Math.min(vv.size(), result.size());
					for(int i=0;i<size;i++)
					{
						result.set(i, vv.get(i));
					}
			}
				for(Entry<String,ArrayList<Double>> e:finalResult.entrySet())
				{
					String field = e.getKey();
					ArrayList<Double> vv = e.getValue();
					NamedList stat=new NamedList();
					int sz=vv.size();
					for(int i=0;i<sz;i++)
					{
						String keyname=transKeyName(i);
						if(keyname!=null)
						{
							stat.add(keyname, vv.get(i));
						}
					}
					rtn.add(field, stat);
				}
		}else{
			ArrayList<byte[]> compress=new ArrayList<byte[]>();
			for(int field=0;field<this.dist.size();field++)
			{
				DistinctCount vv =this.dist.get(field) ;
				compress.add(field, vv.toBytes());
			}
			rtn.add("dist",compress);
			rtn.add("stat", this.stat);
			rtn.add("rc", 1);
		}
		
		return rtn;
	}
	
	private String transKeyName(int i)
	{
		String keyname=null;
		switch(i)
		{
			case 0:
			{
				keyname="dist";
				break;
			}
			case 1:
			{
				keyname="sum";
				break;
			}
			case 2:
			{
				keyname="max";
				break;
			}
			case 3:
			{
				keyname="min";
				break;
			}
			case 4:
			{
				keyname="cnt";
				break;
			}
		}
		return keyname;
	}
	

	public String getKey() {
		return key;
	}
	
	public double getStatVal(int field,int index)
	{
		return stat.get(field).get(index);
	}
	

	public void addStat(int field, int type, Double value) {
		
		ArrayList<Double> w = stat.get(field);
		Double lastValue =  w.get(type);
		Double cnt =  w.get(4);
		if(cnt<=0)
		{
			w.set(type,value);
			return ;
		}

		switch(type)
		{
			case 1://sum
			{
				w.set(type, lastValue+value);
				break;
			}
			case 2://max
			{
				w.set(type, Math.max(value, lastValue));
				break;
			}
			case 3://min
			{
				w.set(type, Math.min(value, lastValue));
				break;
			}
			case 4://cnt
			{
				w.set(type, lastValue+value);
				break;
			}
			default:{
				w.set(type,value);
				break;
			}
		}
	}
	
	public void addDistinct(Integer field, DistinctCount value) {
	
		DistinctCount w = dist.get(field);
		w.merge(value);

	}
	
	public Long getValue() {
		return value;
	}


	@Override
	public int compareTo(GroupbyRow o) {
		return Double.compare(this.value, o.value);
	}


}
