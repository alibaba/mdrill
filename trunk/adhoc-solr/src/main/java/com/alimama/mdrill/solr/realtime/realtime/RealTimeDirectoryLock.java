package com.alimama.mdrill.solr.realtime.realtime;


//review ok 2013-12-27
public class RealTimeDirectoryLock {
	public Object lock=new Object(); 
	public Object doclistBuffer_lock=new Object();
	public Object searchLock=new Object();
}
