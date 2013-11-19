package com.alimama.mdrill.buffer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;


public class RepeatCompress {

	public static class RepeatCompressRtn{
		public int[] bytes;
		public int index;
	}
	
	public static void main(String[] args) {
		long t1=System.currentTimeMillis();
		int[] test=new int[10240];
		for(int j=0;j<test.length;j++)
		{
			test[j]=j/100;

		}

		for(int i=0;i<1;i++)
		{
			
			RepeatCompressRtn compress2=compress(test, test.length);

			int[] compress=new int[compress2.index];
			for(int k=0;k<compress.length;k++)
			{
				compress[k]=compress2.bytes[k];
			}
			
//			System.out.println(Arrays.toString(test));
//			System.out.println(Arrays.toString(compress));
			int[] result=decompress(compress);

			System.out.println(Arrays.equals(result, test)+","+compress.length+","+test.length+","+compress.length*100d/test.length);
		}
		System.out.println(System.currentTimeMillis()-t1);
		
		
//		long t1=System.currentTimeMillis();
//
//		for(int k=0;k<10;k++)
//			{
//		int[] test=new int[10240];
//		for(int j=0;j<test.length;j++)
//		{
//			test[j]=(int) (100*Math.random());
//
//		}
//		RepeatCompressRtn rtn=groupNumEncode(test, test.length);
//		int[] compress=new int[rtn.index];
//		for(int i=0;i<compress.length;i++)
//		{
//			compress[i]=rtn.bytes[i];
//		}
//
//
//		int[] result=groupNumDecode(compress);
//			}
//		
//		System.out.println(System.currentTimeMillis()-t1);


	}
	
	public static class groupCount implements Comparable<groupCount>{
		int cnt;
		@Override
		public String toString() {
			return "[cnt=" + cnt + ", val=" + val + "]";
		}
		int val;
		@Override
		public int compareTo(groupCount o) {
			return (this.cnt<o.cnt ? 1 : (this.cnt==o.cnt ? 0 : -1));
		}
	}
	
	private static RepeatCompressRtn groupNumEncode(final int[] inBlock, int blockSize)
	{
		HashMap<Integer,groupCount> groupCount=new HashMap<Integer,groupCount>(blockSize);
		 for(int i=0;i<blockSize;i++)
		  {
			  int val=inBlock[i];
			  groupCount g=groupCount.get(val);
			  if(g==null)
			  {
				  g=new groupCount();
				  g.cnt=0;
				  g.val=val;
				  groupCount.put(val,g);
			  }
			  g.cnt++;
		  }
		 
		 ArrayList<groupCount> sortlist=new ArrayList<RepeatCompress.groupCount>();
		 sortlist.addAll(groupCount.values());
		 Collections.sort(sortlist );
		 
			HashMap<Integer,Integer> repeat=new HashMap<Integer,Integer>(blockSize);

		 int[] groupNum=new int[blockSize<<1];
			int groupNumIndex=1;
			for(groupCount g:sortlist)
			{
				  int groupNumval=groupNumIndex++;
				  repeat.put(g.val, groupNumval);
				  groupNum[groupNumval]=g.val;
			}
			
			

		
		RepeatCompressRtn rtn=new RepeatCompressRtn();
		rtn.bytes=new int[blockSize<<2];
		rtn.index=0;
		 for(int i=0;i<blockSize;i++)
		  {
			  int val=inBlock[i];
			  Integer g=repeat.get(val);
			  rtn.bytes[rtn.index++]=g;
		  }
		 
		 
		 for(int i=1;i<groupNumIndex;i++)
		 {
			  rtn.bytes[rtn.index++]=groupNum[i];
		 }
		  rtn.bytes[rtn.index++]=groupNumIndex-1;

		  return rtn;
	}
	
	private static int[] groupNumDecode(final int[] inBlock)
	{
		int lastNum=inBlock.length-1;
		int[] groupNum=new int[inBlock[lastNum]+1];

		int start=lastNum-inBlock[lastNum];
		for(int i=start;i<lastNum;i++)
		{
			groupNum[i-start+1]=inBlock[i];

		}
		

		int[] rtn=new int[start];

		for(int i=0;i<start;i++)
		{
			rtn[i]=groupNum[inBlock[i]];
		}
		return rtn;
	}
	
	
	  public static RepeatCompressRtn compress(final int[] inBlock, int blockSize)
	  {
		  RepeatCompressRtn group=groupNumEncode(inBlock, blockSize);
//		  RepeatCompressRtn group=new RepeatCompressRtn();
//		  group.bytes=inBlock;
//		  group.index=blockSize;
		  
		  RepeatCompressRtn rtn=new RepeatCompressRtn();
		  rtn.bytes=new int[group.index<<2];
		  rtn.index=0;
		  int last=Integer.MIN_VALUE;
		  int cnt=0;
		  for(int i=0;i<group.index;i++)
		  {
			  int val=group.bytes[i];
			  if(last!=val)
			  {
				  rtn.bytes[rtn.index++]=val<<1;
//				  System.out.println(i+"@"+val+"@"+Arrays.toString(rtn.bytes));

				  last=val;
				  cnt=1;
			  }else{
				  cnt++;
				  if(cnt==2)
				  {
//					  System.out.println(i+"@"+"22>"+val+"@"+Arrays.toString(rtn.bytes));
					  rtn.bytes[rtn.index-1]=(val<<1)+1;
					  rtn.bytes[rtn.index++]=2;
//					  System.out.println(i+"@"+"44>"+val+"@"+Arrays.toString(rtn.bytes));

				  }else{
//					  System.out.println(i+"@"+"33"+val+"@"+Arrays.toString(rtn.bytes));

					  rtn.bytes[rtn.index-1]++;
				  }
			  }
		  }
		  rtn.bytes[rtn.index++]=group.index;
		  return rtn;
	  }
	  
	  public static int[] decompress(final int[] compress)
	  {
		  return decompress(compress,compress.length);
	  }
	  
	  public static int[] decompress(final int[] compress,int len)
	  {
		  int groupsize=len-1;
		  int[] rtn=new int[compress[groupsize]];
		  int index=0;
		  for(int i=0;i<groupsize;)
		  {
			  int val=compress[i++];
			  int num=val>>1;
		  	  int type=val-(num<<1);
			  int cnt=1;
			  if(type==1)
			  {
				  cnt=compress[i++];
			  }
			  for(int j=0;j<cnt;j++)
			  {
				  rtn[index++]=num; 
			  }
		  }
		  
		  return groupNumDecode(rtn);
	  }

}
