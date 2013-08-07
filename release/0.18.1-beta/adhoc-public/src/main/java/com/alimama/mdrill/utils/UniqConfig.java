package com.alimama.mdrill.utils;

public class UniqConfig {
	
	static Runtime myRun = Runtime.getRuntime();	
	static long maxfieldvaluemem=(long) ((Math.max(myRun.maxMemory(), myRun.totalMemory()))*0.35);
	public static long getFieldValueMemSize()
	{
		return maxfieldvaluemem;
	}
		
	public static int getMergerRequestThreads()
	{
		return 15;
	}
	public static int getMergerRequestMaxDepth()
	{
		return 6;
	}
	
	public static int getTermCacheSize()
	{
		return 10240;
	}
	
	public static Integer getMaxMergerShard()
	{
		return 8;
	}
	
	public static Integer fqCacheSize()
	{
		return 32;
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
	
	public static Integer defaultCrossMaxLimit()
	{
		return 10010;
	}
	
	
}
