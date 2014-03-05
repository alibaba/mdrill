package com.alimama.mdrill.distinct;

import java.io.IOException;

public class DistinctCountExample {

	public static void main(String[] args) throws IOException {
		run(1);
		run(10);
		run(100);
		run(1000);
		run(10000);
		run(100000);
		run(1000000);
		run(10000000);
//		run(100000000);

	}
	
	public static void run(int uniq) throws IOException
	{
		for(int j=1;j<10;j++)
		{
			DistinctCount dc=new DistinctCount();
			dc.setMaxUniqSize(10000);
			for(int i=0;i<uniq*j;i++)
			{
				dc.set("test_bit_"+i);
			}

			long uv=dc.getValue();
			byte[] data=dc.toBytes();
			DistinctCount newdc=new DistinctCount(data);
			long newuv=newdc.getValue();

			double rate=(double) (Math.abs(uv-(uniq*j))*100d/(uniq*j));
			System.out.println(""+uv+","+(uniq*j)+","+rate+","+newuv+","+data.length);
		}
	}
	
	

}
