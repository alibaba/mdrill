package com.alipay.bluewhale.core.cluster;

public class MemInfo {

    public static String getInfo(Integer div)
    {
	StringBuffer buff=new StringBuffer();
	Runtime myRun = Runtime.getRuntime();		
	buff.append("max:"+ (myRun.maxMemory()/div));
	buff.append(",total:"+ (myRun.totalMemory()/div));
	buff.append(",free:"+ (myRun.freeMemory()/div));
	return buff.toString();
    }
    
    
//    public static void gc() {
//        Runtime.getRuntime().gc();
//     }
//    
//    public static boolean hasMem()
//    {
//		Runtime myRun = Runtime.getRuntime();		
//		return myRun.freeMemory()>1024*1024*100;
//    }


}
