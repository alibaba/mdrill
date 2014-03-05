package com.alimama.mdrill.solr.realtime.realtime;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

//review ok 2013-12-27
public class RealTimeDirectoryStatus {
	public AtomicInteger uniqIndex=new AtomicInteger(0);
	public AtomicBoolean isInit=new AtomicBoolean(false);;
	public AtomicBoolean allowsynchdfs=new AtomicBoolean(false);;
	public AtomicBoolean needPurger=new AtomicBoolean(false);
	public AtomicBoolean needRemakeLinks=new AtomicBoolean(false);
	public AtomicLong lastAddDocumentTime=new AtomicLong(System.currentTimeMillis());

}
