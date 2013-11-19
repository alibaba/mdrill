package com.alimama.mdrill.utils;

public class UniqConfig {
	
	static Runtime myRun = Runtime.getRuntime();	
	static long maxfieldvaluemem=(long) ((Math.max(myRun.maxMemory(), myRun.totalMemory()))*0.35);
	public static long getFieldValueMemSize()
	{
		return maxfieldvaluemem;
	}
	
	public static int RealTimeBufferFlush()
	{
		return 30;
	}
	
	
	public static int RealTimeLocalMergerInterval()
	{
		return 3600;
	}
	public static int RealTimeLocalFlush()
	{
		return 300;
	}
	
	
	public static int RealTimeDoclistFlush()
	{
		return 10;
	}
	public static int RealTimeDoclistBuffer()
	{
		return 1024;
	}
	

	public static int RealTimeHdfsFlush()
	{
		return 3600*6;
	}
	
	public static int RealTimeDelete()
	{
		return 3600;
	}
	
	public static int RealTimeRam2localFactory()
	{
		return 6;
	}
	
	public static int RealTimeLocalMergerFactory()
	{
		return 3;
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
	public static int getMergerRequestThreads()
	{
		return 25;
	}
	
	public static int getUnivertedFieldThreads()
	{
		return 4;
	}
	public static int getMergerRequestMaxDepth()
	{
		return 6;
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
		return 12;
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
	
	
}
