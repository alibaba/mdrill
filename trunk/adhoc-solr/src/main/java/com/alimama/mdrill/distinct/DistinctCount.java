package com.alimama.mdrill.distinct;

import gnu.trove.set.hash.TIntHashSet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.WeakHashMap;
import java.util.zip.CRC32;

import org.apache.hadoop.io.Writable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DistinctCount implements Writable {
	
	public static Logger LOG = LoggerFactory.getLogger(DistinctCount.class);
	private TIntHashSet uniq = DistinctCount.createmap();
	private Integer maxUniqSize = 10000;
	private Integer currentTimes = 1;
	private Integer TimesStep = 2;
	
	private DistinctCountAutoAjuest autoAjust=null;
	
	public void setAutoAjust(DistinctCountAutoAjuest autoAjust) {
		this.autoAjust = autoAjust;
	}

	private static TIntHashSet createmap()
	{
		return new TIntHashSet(10,0.75f,0);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		this.maxUniqSize = in.readInt();
		this.currentTimes = in.readInt();
		this.TimesStep = in.readInt();
		this.uniq.clear();
		int usize = in.readInt();
		for (int i = 0; i < usize; i++) {
			this.uniq.add(in.readInt());
		}
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeInt(this.maxUniqSize);
		out.writeInt(this.currentTimes);
		out.writeInt(this.TimesStep);
		out.writeInt(this.uniq.size());
		for (Integer uin : this.uniq.toArray()) {
			out.writeInt(uin);
		}
	}

	public DistinctCount(byte[] zipdata) {
		if (zipdata.length <= 0) {
			return;
		}
		try {
			ByteArrayInputStream bis2 = new ByteArrayInputStream(zipdata);
			DataInputStream in2 = new DataInputStream(bis2);
			this.readFields(in2);
			in2.close();
			bis2.close();
		} catch (Exception e) {
		}
	}

	public byte[] toBytes() {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DataOutputStream dout = new DataOutputStream(bos);

			this.write(dout);

			byte[] data = bos.toByteArray();
			bos.close();
			return data;

		} catch (IOException e) {
			e.printStackTrace();
		}

		return new byte[0];

	}

	public DistinctCount() {

	}

	public void set(String item) {
		CRC32 crc32 = new CRC32();
		crc32.update(new String(item).getBytes());
		long crcvalue = crc32.getValue();
		this.add((int) crcvalue);
	}
	
	public void set(double item) {
		CRC32 crc32 = new CRC32();
		crc32.update(ByteUtil.getBytes(item));
		long crcvalue = crc32.getValue();
		this.add((int) crcvalue);
	}
	
	public void set(int item) {
		CRC32 crc32 = new CRC32();
		crc32.update(ByteUtil.getBytes(item));
		long crcvalue = crc32.getValue();
		this.add((int) crcvalue);
	}

	public Long getValue() {
		return (long) this.uniq.size() * currentTimes;
	}
	
	int last_increateTime=1;
	public int getIncreateTimes(boolean fromcache)
	{
		if(fromcache)
		{
			return this.last_increateTime;
		}
		int increateTime=1;
		int times=this.currentTimes;
		while(times>1)
		{
			times=times/TimesStep;
			increateTime++;
		}
		
		this.last_increateTime=increateTime;
		return increateTime;

	}

	private void add(Integer crc) {
		if (this.isallow(crc)) {
			boolean isadd = this.uniq.add(crc);
			if(!isadd)
			{
				return ;
			}
			if(autoAjust!=null)
			{
				if (this.uniq.size() > getMaxUniqSize()) {
					autoAjust.ajust();
				}
			}
			
			if (this.uniq.size() > getMaxUniqSize()) {
				int newtimes = currentTimes * TimesStep;
				this.reFilter(newtimes);
			}
		}
	}
	
	public void reAjuest()
	{
		while (this.uniq.size() > getMaxUniqSize()) {
			int newtimes = currentTimes * TimesStep;
			this.reFilter(newtimes);
		}
	}

	private boolean isallow(long crc) {
		if (crc % this.currentTimes == 0) {
			return true;
		}
		return false;
	}

	private void reFilter(int times) {
		if (this.currentTimes == times) {
			return;
		}

		this.currentTimes = times;
		TIntHashSet data = DistinctCount.createmap();

		for (Integer ucrc : this.uniq.toArray()) {
			if (this.isallow(ucrc)) {
				data.add(ucrc);
			}
		}
		this.uniq = data;
	}

	public Integer getMaxUniqSize() {
		return maxUniqSize;
	}

	public void setMaxUniqSize(Integer maxUniqSize) {
		this.maxUniqSize = maxUniqSize;
	}

	public void merge(DistinctCount dc) {
		int newtimes = Math.max(dc.currentTimes, this.currentTimes);
		this.reFilter(newtimes);
		dc.reFilter(newtimes);
		for (Integer ucrc : dc.uniq.toArray()) {
			this.add(ucrc);
		}
	}
	
	
	
	public static class DistinctCountAutoAjuest{
		private WeakHashMap<Object,DistinctCount> ajust=new WeakHashMap<Object,DistinctCount>();
		int size=100000;
		public DistinctCountAutoAjuest(int size)
		{
			this.size=size;
		}
		
		public DistinctCount create(Object key)
		{
			DistinctCount dist=new DistinctCount();
			ajust.put(key, dist);
			this.autoAjust();
			dist.setAutoAjust(this);
			return dist;
		}
		
		
		public DistinctCount put(Object key,DistinctCount dist)
		{
			if(dist==null)
			{
				return null;
			}
			ajust.put(key, dist);
			this.autoAjust();
			dist.setAutoAjust(this);
			return dist;
		}
		
		public DistinctCount remove(Object key)
		{
			if(key==null)
			{
				return null;
			}
			DistinctCount rtn= ajust.remove(key);
			this.autoAjust();
			
			return rtn;

		}
		
		public void ajust()
		{
			int zjustsize=Math.max(ajust.size(), 1);
			int persize=size/zjustsize;
			if(persize<20)
			{
				persize=20;
			}
//			LOG.info("autoAjust ajust:"+persize+",size:"+size+",zjustsize:"+zjustsize+",last_persize:"+this.last_persize);

			this.last_persize=persize;
			this._ajust(zjustsize, persize);

		}
		
		private void _ajust(int zjustsize,int persize)
		{

			ArrayList<DistinctCount> list=new ArrayList<DistinctCount>(this.size+1);
			list.addAll(this.ajust.values());
			
			long totalsize=0l;
			for(DistinctCount d:list)
			{
				int t=Math.max(d.getIncreateTimes(false), 1);
				totalsize+=t;
			}
			
			double pre_uniqsize=Math.max((totalsize*1.0/zjustsize), 1d);
			double pre_uniqsize_max=pre_uniqsize*3;
			double pre_uniqsize_min=pre_uniqsize/2;

			totalsize=0l;
			for(DistinctCount d:list)
			{
				int t=d.getIncreateTimes(true);
				if(t>pre_uniqsize_max)
				{
					t=(int) pre_uniqsize_max;
				}else if(t<pre_uniqsize_min)
				{
					t=(int) pre_uniqsize_min;
				}
				t=Math.max(t, 1);
				totalsize+=t;
			}
			
			pre_uniqsize=Math.max((totalsize*1.0/zjustsize), 1);

			int allowSize=0;
			try{
			for(DistinctCount d:list)
			{

				int t=d.getIncreateTimes(true);
				if(t>pre_uniqsize_max)
				{
					t=(int) pre_uniqsize_max;
				}else if(t<pre_uniqsize_min)
				{
					t=(int)pre_uniqsize_min;
				}
				t=Math.max(t, 1);

				double times=t/pre_uniqsize;

				int uniqsize=(int)(persize*times);
				if(uniqsize<20)
				{
					uniqsize=20;
				}
				if(uniqsize>this.size)
				{
					uniqsize=this.size;
				}
				allowSize+=uniqsize;
				int lastuniqsize=d.getMaxUniqSize();
				d.setMaxUniqSize(uniqsize);
				if(lastuniqsize>uniqsize)
				{
					d.reAjuest();
				}
			
			}
			}catch(Throwable e){}
			
			LOG.info("autoAjust _ajust:"+persize+",size:"+size+",zjustsize:"+zjustsize+",avg:"+pre_uniqsize+",max:"+pre_uniqsize_max+",min:"+pre_uniqsize_min+",allowSize:"+allowSize);

		}
		
		int last_persize=0;
		private void autoAjust()
		{
			int zjustsize=Math.max(ajust.size(), 1);
			
			int persize=size/zjustsize;
			
			int diff=Math.abs((zjustsize*this.last_persize)-size);
			if(diff<102400)
			{
				return ;
			}
			
			
			
			if(persize<20)
			{
				persize=20;
			}
			
			if(persize==this.last_persize)
			{
				return ;
			}
			
//			LOG.info("autoAjust persize:"+persize+",size:"+size+",zjustsize:"+zjustsize+",last_persize:"+this.last_persize+",diff:"+diff);
			this.last_persize=persize;
			
			this._ajust(zjustsize, persize);
			
		}
	}

}
