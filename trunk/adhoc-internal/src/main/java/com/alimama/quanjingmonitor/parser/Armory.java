package com.alimama.quanjingmonitor.parser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.apache.lucene.util.cache.Cache;
import org.apache.lucene.util.cache.SimpleLRUCache;




public class Armory {
	private static Logger LOG = Logger.getLogger(Armory.class);

	
	public static class ArmoryInfo{
		public ArrayList<ArmoryItemInfo> info=new ArrayList<ArmoryItemInfo>();
		@Override
		public String toString() {
			return "ArmoryInfo [info=" + info + "]";
		}
		public long ts=System.currentTimeMillis();
	}
	
	
	public static void main(String[] args) {
		Armory.getInfo(args[0]);
	}
	
	public static String[] getEnv(){

		HashMap<String,String> map=new HashMap<String, String>();
		Map<String, String> sysenv=System.getenv();
		if(sysenv!=null)
		{
			for(Entry<String, String> e:System.getenv().entrySet())
			{
				String ekey=e.getKey();
				String evalue=e.getValue();
				if(ekey.length()>1&&evalue.length()>1)
				{
					map.put(ekey, evalue);
				}
			}
		}
		
		String[] env = new String[map.size()];
		int index=0;
		for(Entry<String, String> e:map.entrySet())
		{
			env[index]=e.getKey()+"="+e.getValue();
			index++;
		}
		
		return env;
	}
	
    private static HashSet<String> SELECT_KEY=new HashSet<String>();
    static{
    	SELECT_KEY.add("nodename");
    	SELECT_KEY.add("dns_ip");
    	SELECT_KEY.add("nodegroup");
    	SELECT_KEY.add("product_name");
    	SELECT_KEY.add("site");
    }
    
    
    public static class ArmoryItemInfo{
    	public String nodename;

		@Override
		public String toString() {
			return "ArmoryItemInfo [nodename=" + nodename + ", dns_ip="
					+ dns_ip + ", nodegroup=" + nodegroup + ", product_name="
					+ product_name + ", site=" + site + ", groupName="
					+ groupName + "]";
		}

		public String dns_ip;
    	public String nodegroup;
    	public String product_name;
    	public String site;
    	public String groupName;
    	
    	public static ArmoryItemInfo INSTANCE(HashMap<String,String> d)
    	{
    		ArmoryItemInfo rtn=new ArmoryItemInfo();
    		rtn.nodename=d.get("nodename");
    		rtn.dns_ip=d.get("dns_ip");
    		rtn.nodegroup=d.get("nodegroup");
    		rtn.product_name=d.get("product_name");
    		rtn.site=d.get("site");
    		
//    		String[] cols=String.valueOf(rtn.nodegroup).split("_");
    		rtn.groupName=rtn.nodegroup;
    		return rtn;
    	}
    	
    }
	private static ConcurrentHashMap<String, ArmoryInfo> termsCache = new ConcurrentHashMap<String, ArmoryInfo>();

	
	private static class GetBytesThreadError extends Thread {
        BufferedReader reader = null;
        GetBytesThreadError(InputStream input, ArmoryInfo ramoryInfo,String name) {

			this.reader = new BufferedReader(new InputStreamReader(input));
		}

        public void run() {

            String s = null;
            try {
                Integer index=0;
                while ((s = reader.readLine()) != null&&index<100000) {
                	index++;
                }
                this.reader.close();
            } catch (Throwable e) {
                LOG.error("armory readLine:",e);

            }
        
        }
        
    }

	private static class GetBytesThread extends Thread {
        BufferedReader reader = null;
        ArmoryInfo ramoryInfo;
        String name="";
		GetBytesThread(InputStream input, ArmoryInfo ramoryInfo,String name) {

			this.name=name;
			this.ramoryInfo=ramoryInfo;
			this.reader = new BufferedReader(new InputStreamReader(input));
		}

        public void run() {

            String s = null;
            try {
                Integer index=0;
                HashMap<String,String> info=new HashMap<String, String>();
                boolean skip=false;
                while ((s = reader.readLine()) != null&&index<100000) {

                	index++;
                	
                	if(s.indexOf("---------")>=0)
                	{
                		if(!skip&&info.size()>0)
                		{
                			ramoryInfo.info.add(ArmoryItemInfo.INSTANCE(info));
                		}
                		info=new HashMap<String, String>();
                		skip=false;
                		continue;
                	}
                	
                	String[] cols=s.split("[ ]+",-1);
                	if(cols.length>=2&&!cols[0].isEmpty()&&!cols[1].isEmpty())
                	{
                		if(cols[0].equals("nodename")&&!cols[1].startsWith(name+"."))
                		{
                    		info=new HashMap<String, String>();
                    		skip=true;
                		}
                		if(!skip&&SELECT_KEY.contains(cols[0]))
                		{
                			info.put(cols[0], cols[1]);
                		}
                	}
                	
                }
                
                this.reader.close();
                if(!skip&&info.size()>0)
        		{
        			ramoryInfo.info.add(ArmoryItemInfo.INSTANCE(info));
        		}
            } catch (Throwable e) {
                LOG.error("armory readLine:",e);

            }
        
        }
        
    }
	
	private static long DELAYTIME=1000l*3600*24*7+(long)(Math.random()*1000l*3600);
	public static ArmoryInfo getInfo(String name)
	{
		ArmoryInfo rtn=termsCache.get(name);
		
		long ts= System.currentTimeMillis()-DELAYTIME;
		if(rtn!=null&&rtn.ts>ts)
		{
			return rtn;
		}

		synchronized (termsCache) {
			
			rtn=termsCache.get(name);
			if(rtn!=null&&rtn.ts>ts)
			{
				return rtn;
			}
			 rtn=new ArmoryInfo();

			String[] execmd = { "/bin/sh", "-c" ,"/home/taobao/armory/armory.sh "+name+""};
			String[] env = getEnv();
			Process process = null;
			try {

				process = Runtime.getRuntime().exec(execmd, env);
			} catch (Throwable e) {
				LOG.error("armory Runtime:", e);
			}
			try {
				if (process != null) {
					GetBytesThreadError thr2 = new GetBytesThreadError(process.getErrorStream(), rtn, name);
					GetBytesThread thr = new GetBytesThread(process.getInputStream(), rtn, name);
					thr2.start();
					thr.start();
					process.waitFor();

					thr2.join();
					thr.join();

					int exitValue = process.exitValue();

				}

			} catch (Throwable e) {
				LOG.error("armory Runtime:", e);
			}
			LOG.info("armory:"+rtn.toString()+","+termsCache.size()+",name="+name);

			if(termsCache.size()>100000)
			{
				termsCache.clear();
			}
			termsCache.put(name, rtn);

		}
        
        return rtn;
        
	}

}
