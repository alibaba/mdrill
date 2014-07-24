package com.alimama.mdrill.adhoc;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.alimama.mdrill.utils.HadoopBaseUtils;

public class HiveExecute implements Runnable {
	private static StringMatchGet MATCH_GET=new StringMatchGet();
	private IHiveExecuteCallBack callback;
	private String hql = null;
	private String hiveBin = "/home/hive/hive/bin/hive";
	String[] cmd;
	String [] env;
	
	private String confdir=	System.getenv("HADOOP_CONF_DIR");

	public void setConfdir(String confdir) {
		this.confdir = confdir;
	}

	public void setCallback(IHiveExecuteCallBack callback) {
		this.callback = callback;
	}

	public void setHql(String hql) {
		this.hql = hql;
	}
	
    private String findContainingJar(Class<?> myClass) {
        ClassLoader loader = myClass.getClassLoader();
        String class_file = myClass.getName().replaceAll("\\.", "/") + ".class";
        try {
            for (Enumeration<URL> itr = loader.getResources(class_file); itr.hasMoreElements();) {
                URL url = itr.nextElement();
                if ("jar".equals(url.getProtocol())) {
                    String toReturn = url.getPath();
                    if (toReturn.startsWith("file:")) {
                        toReturn = toReturn.substring("file:".length());
                    }
                    toReturn = URLDecoder.decode(toReturn, "UTF-8");
                    return toReturn.replaceAll("!.*$", "");
                }
            }
        } catch (IOException e) {}
        return null;
    }
    
    public static String stringify_error(Throwable error) {
        StringWriter result = new StringWriter();
        PrintWriter printer = new PrintWriter(result);
        error.printStackTrace(printer);
        return result.toString();
    }
    
    public void init()
    {
    	String addjar="add jar "+findContainingJar(this.getClass())+";";
    	String addudf="create temporary function inhdfs_udf as 'com.alimama.mdrill.adhoc.InHdfs_udf'; create temporary function transhigo_udf as 'com.alimama.mdrill.adhoc.TransHigo_udf'; ;";

    	String executeHql = addjar+addudf+hql;
		String[] execmd = { hiveBin, "-e", executeHql };
		this.cmd=execmd;
		String casspath = findContainingJar(HiveExecute.class);
		
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
		map.put("HADOOP_CLASSPATH", casspath);
		map.put("LANG", "zh_CN.UTF-8");
		this.env = new String[map.size()];
		int index=0;
		for(Entry<String, String> e:map.entrySet())
		{
			env[index]=e.getKey()+"="+e.getValue();
			index++;
		}
		this.callback.setConfdir(this.confdir);
		this.callback.init(executeHql,cmd,env);

    }
    
    String storedir;
	public void setStoreDir(String store) {
		this.storedir=store;
	}
	
    private Runnable Processer=null;
    


	public void setProcesser(Runnable processer) {
		Processer = processer;
	}

	public void run() {
		
		Configuration conf=new Configuration();
 		HadoopBaseUtils.grabConfiguration(confdir, conf);
		try {
			FileSystem fs = FileSystem.get(conf);
	    	fs.mkdirs(new Path(this.storedir).getParent());
		} catch (IOException e) {}
		
		this.callback.setPercent("started");
		this.callback.sync();
		   Process process = null;
	        try {
	            process = Runtime.getRuntime().exec(cmd, env,new File(hiveBin).getParentFile());
	        } catch (Throwable e) {
	        	this.callback.addException(stringify_error(e));
	        }

	        if(process==null)
	        {
	            this.callback.setFailed("process==null");
	            this.callback.finish();
	            return ;
	        }
	        try {
	            GetBytesThread errorThread = new GetBytesThread(process.getErrorStream(), true,this.callback);
	            GetBytesThread inputThread = new GetBytesThread(process.getInputStream(), false,this.callback);
	            errorThread.start();
	            inputThread.start();
	            process.waitFor();
	            errorThread.join();
	            inputThread.join();
	           int  exitValue = process.exitValue();
	           
	           if(Processer!=null)
	           {
	        	   Processer.run();
	           }
	           this.callback.setExitValue(exitValue);
	        } catch (Throwable e) {
		         this.callback.setExitValue(9);
	            callback.setFailed(stringify_error(e));
	        }finally{
	        	
	        	this.callback.finish();
	        }
	}
	
	public static InputStreamReader makeStream(InputStream input) {
		try {
			return new InputStreamReader(input, "utf8");
		} catch (UnsupportedEncodingException e) {
			return new InputStreamReader(input);
		}
	}
	
	private class GetBytesThread extends Thread {
        BufferedReader reader = null;
        boolean        isERR  = false;
    	private IHiveExecuteCallBack callback;
    	int slotCount=0;

		GetBytesThread(InputStream input, boolean isERR,IHiveExecuteCallBack callback) {
			this.callback=callback;
			this.reader = new BufferedReader(makeStream(input));
			this.isERR = isERR;
			setDaemon(true);
		}

        public void run() {
            String s = null;
            try {
                Integer index=0;

                while ((s = reader.readLine()) != null&&index<100000) {
                    if (isERR) {
                    	this.callback.WriteSTDERRORMsg(s);
                        this.updateDao(s);
                        this.callback.maybeSync();
                    } else {
                    	this.callback.WriteStdOutMsg(s);
                    }
                    index++;
                }
            } catch (IOException e) {
            }
        }
        
        private void updateDao(String line) {
            String result = MATCH_GET.evaluate(line,".*Stage-.*number.*of.*mappers[^\\d]*(\\d+)[^\\d]*number.*of.*reducers", 1, "Integer");
            if (!result.equals("-")) {
                this.slotCount += Integer.parseInt(result);
                this.callback.setSlotCount(this.slotCount);
            }
            
            
            result = MATCH_GET.evaluate(line,".*Stage-.*number.*of.*mappers.*number.*of.*reducers[^\\d]*(\\d+)[^\\d]*", 1, "Integer");
            if (!result.equals("-")) {
                this.slotCount += Integer.parseInt(result);
                this.callback.setSlotCount(this.slotCount);
            }
            
            
            result = MATCH_GET.evaluate(line,".*Moving.*data.*to:(.*$)", 1, "String");
            if (!result.equals("-")) {
            	String path=result.trim();
            	Configuration conf=new Configuration();
            	HadoopBaseUtils.grabConfiguration(confdir, conf);
            	long sizekb=0;
				try {
					sizekb = HadoopBaseUtils.size(path, conf)/1024;
				} catch (IOException e) {
				}
            	this.callback.setResultKb( sizekb);
            }

            result = MATCH_GET.evaluate(line,"[^\\d]*(\\d+)[^\\d].*Rows.*loaded.*to.*", 1, "Integer");
            if (!result.equals("-")) {
            	Long rows=Long.parseLong(result);
            	this.callback.setResultRows(rows);
            	
            }
            
           

            result = MATCH_GET.evaluate(line, ".*(Stage.*map.*reduce.*$)", 1, "String");
            if (!result.equals("-")) {
                this.callback.setPercent(result);
            }
            
            result = MATCH_GET.evaluate(line,".*Launching.*Job.*out.*of(.*$)", 1, "String");
            if (!result.equals("-")) {
            	this.callback.setStage(result.trim());
            }
            
            result = MATCH_GET.evaluate(line,".*Total.*MapReduce.*jobs.*=(.*$)", 1, "String");
            if (!result.equals("-")) {
            	this.callback.setStage(result.trim());
            }
            
            
            
            
            
            result = MATCH_GET.evaluate(line, ".*Starting.*Job.*=.*(job_\\d+_\\d+),.*Tracking.*URL.*", 1, "String");
            if (!result.equals("-")) {
               this.callback.addJobId(result);
            }
            
            if (line.toLowerCase().indexOf("exception") > 0) {
            	this.callback.addException(line);
            }
        }
    }
}
