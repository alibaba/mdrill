package com.alimama.mdrill.index;

import org.apache.hadoop.mapreduce.Partitioner;

import com.alimama.mdrill.index.utils.DocumentMap;
import com.alimama.mdrill.index.utils.PairWriteable;

public class PairPartion extends Partitioner<PairWriteable, DocumentMap>{

	@Override
	public int getPartition(PairWriteable ir, DocumentMap arg1, int c) {
		int i=ir.isNum()?ir.getIndex():ir.getUniq().toString().hashCode();
		if(i<0)
		{
			return (i*-1)%c;
		}
		return i%c;
	}

}
