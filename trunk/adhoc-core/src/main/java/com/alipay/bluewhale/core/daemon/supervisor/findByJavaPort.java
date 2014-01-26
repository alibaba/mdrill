package com.alipay.bluewhale.core.daemon.supervisor;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

public class findByJavaPort {
	private static Logger LOG = Logger.getLogger(findByJavaPort.class);

	public static void main(String[] args) {
		String s="tcp        0      0 0.0.0.0:51117               0.0.0.0:*                   LISTEN      1267/java           ";
		System.out.println(findByJavaPort.fetch(s, 51118));
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
	
	
	public static ArrayList<String> findProcess(int port)
	{
		String[] execmd = { "/bin/sh", "-c" ,"netstat -npl"};
		String[] env = getEnv();
		Process process = null;
		try {

			process = Runtime.getRuntime().exec(execmd, env);
		} catch (Throwable e) {
			LOG.error("armory Runtime:", e);
		}
		

		ArrayList<String> list=new ArrayList<String>();
		try {
			if (process != null) {
				GetBytesThreadError thr2 = new GetBytesThreadError(process.getErrorStream(), list,port);
				GetBytesThread thr = new GetBytesThread(process.getInputStream(), list,port );
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
		
		return list;
	}
	
	

	private static class GetBytesThreadError extends Thread {
        BufferedReader reader = null;
        GetBytesThreadError(InputStream input, ArrayList<String> ramoryInfo,int port) {

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
	
	private static String fetch(String s,int port)
	{
		String matchport=":"+String.valueOf(port);
		if(s.indexOf("java")<0||s.indexOf(matchport)<0)
    	{
        	return null;
    	}
		
		String[] cols=s.split("[ ]+",-1);
    	if(cols.length<7)
    	{
    		return null;
    	}
    	
    	if(cols[3].indexOf(matchport)<0||cols[6].indexOf("java")<0)
    	{
    		return null;
    	}
    	
    	String[] pidpart=cols[6].split("/");
    	if(pidpart.length!=2)
    	{
    		return null;
    	}
    	String pid=pidpart[0];
    	try{
    	int ipid=Integer.parseInt(pid);
    	
    	return pid;
    	}catch(Throwable e)
    	{
    		return null;
    	}
    	
	}

	private static class GetBytesThread extends Thread {
        BufferedReader reader = null;
        ArrayList<String> list;
        int port;
		GetBytesThread(InputStream input, ArrayList<String> ramoryInfo,int port) {
			this.port=port;
			this.list=ramoryInfo;
			this.reader = new BufferedReader(new InputStreamReader(input));
		}

        public void run() {
            String s = null;
            try {
                Integer index=0;
                while ((s = reader.readLine()) != null&&index<100000) {
                	index++;
                	String pid=fetch(s,port);
                	if(pid!=null)
                	{
                		list.add(pid);
                	}
                }
                this.reader.close();
            } catch (Throwable e) {
                LOG.error("armory readLine:",e);
            }
        }
        
    }
}
