package com.alimama.mdrill.index;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Partitioner;

public class IntPartion extends Partitioner<IntWritable, Text>{

	@Override
	public int getPartition(IntWritable ir, Text arg1, int c) {
		int i=ir.get();
		if(i<0)
		{
			return (i*-1)%c;
		}
		return i%c;
	}

}
