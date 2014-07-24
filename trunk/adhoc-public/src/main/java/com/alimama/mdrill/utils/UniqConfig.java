package com.alimama.mdrill.utils;

public class UniqConfig {
	
	static Runtime myRun = Runtime.getRuntime();	
	static long maxfieldvaluemem=(long) ((Math.max(myRun.maxMemory(), myRun.totalMemory()))*0.3);
	static long maxfieldvaluemem_fq=(long) ((Math.max(myRun.maxMemory(), myRun.totalMemory()))*0.05);

	public static long getFieldValueMemSize()
	{
		return maxfieldvaluemem;
	}
	
	public static long getFieldValueMemSizefq()
	{
		return maxfieldvaluemem_fq;
	}
	
	public static int RealTimeBufferFlush()
	{
		return 30;
	}
	
	public static int RealTimeLocalFlush()
	{
		return 1200;
	}
	
	public static int RealTimeDoclistFlush()
	{
		return 30;
	}
	public static int RealTimeDoclistBuffer()
	{
		return 5000;
	}
	
	public static int RealTimeMergerOneTimelen()
	{
		return 3600*1;
	}
	

	public static int RealTimeHdfsFlush()
	{
		return 3600*4;
	}
	
	
	public static int MaybeRepCheckCacheSize()
	{
		return 10240;
	}
	
	public static long logRollIntervel()
	{
		return 1000l;
	}
	
	public static long logRollTimelen()
	{
		return 1000l*300;
	}
	
	public static int RealTimeDelete()
	{
		return 3600*8;
	}
	
	public static int RealTimeMaxIndexCount()
	{
		return 10;
	}
	
	public static int RealTimeMergeFactor()
	{
		return 6;
	}
	
	
	static long buffersize=1024l*1024*10;
	public static long RealTimeBufferSize()
	{
		return buffersize;
	}
	
	static long ramsize=1024l*1024*100;
	public static long RealTimeRamSize()
	{
		return ramsize;
	}
		
	public static int maxHbTablesParal()
	{
		return 2;
	}

	public static int getFacetThreads()
	{
		return 8;
	}
	public static int getUnivertedFieldThreads()
	{
		return 6;
	}
	
	static int[]  threadCountMax={256,128,64,64,64,64,64,64,64,64,64,64,64,64};
	static int[]  threadCountMin={16,8,4,2,1};

	public static int getMergerRequestMaxDepth()
	{
		return threadCountMax.length;
	}
	
	public static int getMergerRequestThreadsMax(int depth)
	{
		if(depth<threadCountMax.length)
		{
			return threadCountMax[depth];
		}
		return 2;
	}
	
	public static int getMergerRequestThreadsMin(int depth)
	{
		if(depth<threadCountMin.length)
		{
			return threadCountMin[depth];
		}
		return 0;
	}
	
	
	public static int getTermCacheSizeIndex()
	{
		return 1024;
	}
	
	public static int getCrcCacheTimeoutSecs()
	{
		return 1200;
	}
	
	public static int getTermCacheSize()
	{
		return 10240;
	}
	
	public static Integer getMaxMergerShard()
	{
		return 64;
	}
	
	public static Integer fqCacheSize()
	{
		return 16;
	}
	
	
	public static Integer ShardMaxGroups()
	{
		return 102400;
	}
	
	public static Integer shardMergerCount()
	{
		return 64;
	}
		
	public static String GroupJoinString()
	{
		return "@";
	}
	
	public static String GroupJoinTagString()
	{
		return "m";
	}
	
	public static Integer defaultCrossMaxLimit()
	{
		return 10010;
	}
	
	public static Integer DistinctCountSize()
	{
		return 10000*100;
	}
	
	public static int getBlockBufferSize()
	{
		return 100;
	}
	
	public static int getBlockBufferPoolSize()
	{
		return 1024;
	}
	
}
