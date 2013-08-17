package org.apache.solr.request;

import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.lucene.util.OpenBitSet;
import org.apache.solr.core.SolrCore;
import org.apache.solr.search.BitDocSet;

/**
 * 压缩的bitset
 * @author yannian.mu
 *
 */
public class MapBitSets {
	
	public static class LongBit{
		Long bit;
		public LongBit(Long b)
		{
			this.bit=b;
		}
	}
	boolean isover=false;
	static int oversize=1024;
	protected HashMap<Integer, LongBit> bitMaps=new HashMap<Integer, LongBit>();
	private OpenBitSet bitset;
	long numBits=0;
	public MapBitSets(long _numBits,boolean compress)
	{
		this.numBits=_numBits;
		if(compress)
		{
			this.bitset=null;
		}else{
			this.bitset=null;//new OpenBitSet(_numBits);
		}
	}
	
	
	public void fastSet(int index) {
		if (isover) {
			return;
		}
		if(this.bitset!=null)
		{
			this.bitset.fastSet(index);
			return ;
		}
		
		int wordNum = index >> 6;
		int bit = index & 0x3f;
		long bitmask = 1L << bit;
		LongBit bv = bitMaps.get(wordNum);
		if (bv == null) {
			bv = new LongBit(0l);
			bitMaps.put(wordNum, bv);
			if (bitMaps.size() > oversize) {
				isover = true;
				bitMaps.clear();
				bitMaps=null;

				return;
			}
		}
		bv.bit |= bitmask;

	}
	
	public void compress()
	{
		if(this.bitset==null)
		{
			return ;
		}
		long[] list=this.bitset.getBits();
		int count=0;
		for(int i=0;i<list.length;i++)
		{
			long bit=list[i];
			if(bit!=0)
			{
				bitMaps.put(i, new LongBit(bit));
			}
			count++;
			if(count>oversize)
			{
				isover = true;
				break;
			}
		}
		this.bitset=null;
		if(isover)
		{
			this.bitMaps.clear();
			this.bitMaps=null;
		}
	}
	
	public BitDocSet toOpenBit()
	{

		if (isover||bitMaps.size()<=0) {
			return null;
		}
		
		long startTime = System.currentTimeMillis();

		int numwords=OpenBitSet.bits2words(numBits);
		 long[] bits=new long[numwords];
		 for(Entry<Integer, LongBit> e:this.bitMaps.entrySet())
		 {
			 bits[e.getKey()]=e.getValue().bit;
		 }
		 
		 BitDocSet rtn= new BitDocSet(new OpenBitSet(bits, numwords));
		 
		 long endTime = System.currentTimeMillis();
		    int ttime = (int)(endTime-startTime);
		    if(ttime>1)
		    {
		    	SolrCore.log.info("UnInverted toOpenBit "+ttime);
		    }
		    return rtn;
	}
	
	
}
