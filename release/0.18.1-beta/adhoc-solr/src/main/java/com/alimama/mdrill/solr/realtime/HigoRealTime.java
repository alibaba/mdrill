package com.alimama.mdrill.solr.realtime;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.store.RAMDirectory;
import org.apache.solr.core.SolrCore;

public class HigoRealTime {
	private SolrCore core;
	private String day;
	public HigoRealTime(SolrCore core, String day) {
		this.core = core;
		this.day = day;
	}

	private HigoRealTimeBinlog logwriter;
	private HigoRealTimeSync sync;
	private Thread sync_thr;
	
	public void start() throws IOException
	{
		this.logwriter=new HigoRealTimeBinlog(core, day);
		this.sync=new HigoRealTimeSync(this.logwriter.getbinlog(),core, day);
		this.sync_thr=new Thread(sync);
		this.sync_thr.setPriority(Thread.MAX_PRIORITY);
		this.sync_thr.start();
	}
	
	public HigoRealTimeBinlog getBinlog() {
		return logwriter;
	}
	
	
    public static List<String> tokenize_path(String path) {
        String[] toks = path.split("/");
        java.util.ArrayList<String> rtn = new ArrayList<String>();
        for (String str : toks) {
            if (!str.isEmpty()) {
                rtn.add(str);
            }
        }
        return rtn;
    }

    public static String toks_to_path(List<String> toks) {
        StringBuffer buff = new StringBuffer();
        buff.append("/");
        int size = toks.size();
        for (int i = 0; i < size; i++) {
            buff.append(toks.get(i));
            if (i < (size - 1)) {
                buff.append("/");
            }

        }
        return buff.toString();
    }

    public static String normalize_path(String path) {
        String rtn = toks_to_path(tokenize_path(path));
        return rtn;
    }
	
	public static ConcurrentHashMap<String, HigoRealTime> instance=new ConcurrentHashMap<String, HigoRealTime>();
	private static long t1=System.currentTimeMillis();
	public static HigoRealTime INSTANCE(SolrCore core, String day) throws IOException
	{
		HigoRealTime rtn=null;
		String key=core.getIndexDir(day);

		boolean isneedclean=false;
		synchronized (instance) {
			if(!instance.contains(key))
			{
				isneedclean=true;
			}
		}
		
		long t2=System.currentTimeMillis();
		if(isneedclean||t2-t1>1000l*3600)
		{
			try {
				cleanhistory(core);
			} catch (IOException e) {}
			t1=t2;
		}
		
		synchronized (instance) {
			rtn=instance.get(key);
			if(rtn==null||rtn.isstop())
			{
				if(rtn!=null)
				{
					rtn.stop();
				}
				rtn=new HigoRealTime(core, day);
				rtn.start();
				instance.put(key, rtn);
			}
		}
		return rtn;
	}
	
	public void stop()
	{
		this.logwriter.setIsstop(true);
		this.logwriter.close();
		this.sync.setIsstop(true);
	}
	
	public boolean isstop()
	{
		return this.logwriter.isIsstop()||this.sync.isIsstop();
	}
	
	public static final String yyyymmdd_regex = "\\d{8}";
	public static final Pattern yyyymmdd_pattern = Pattern.compile(yyyymmdd_regex);
	public static final Matcher yyyymmdd_matcher = yyyymmdd_pattern.matcher("");
	public static ConcurrentHashMap<String,RAMDirectory> ramDirectory=new ConcurrentHashMap<String,RAMDirectory>();
	
	

	private static void DROP(SolrCore core, String day)
	{
		synchronized (instance) {
			String key=core.getIndexDir(day);
			String key2=HigoRealTime.normalize_path((new File(key)).getAbsolutePath());
			ramDirectory.remove(key2);
			HigoRealTime rtn=instance.remove(key);
			if(rtn==null)
			{
				return;
			}
			rtn.stop();
			rtn=null;
		}
	}
	private static void cleanhistory(SolrCore core) throws IOException
	{
		SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
		HashSet<String> allowlist=new HashSet<String>();
		Date today=new Date();
		allowlist.add(fmt.format(new Date(today.getTime()+1000l*3600*24)));
		for(int i=0;i<3;i++)
		{
			allowlist.add(fmt.format(new Date(today.getTime()-1000l*3600*24*i)));
		}
		
		ArrayList<HigoRealTime> droplist=new ArrayList<HigoRealTime>();
		for(HigoRealTime r:instance.values())
		{
			if(!allowlist.contains(r.day))
			{
				droplist.add(r);
			}
		}
		
		for(HigoRealTime r:droplist)
		{
			DROP(r.core, r.day);
		}
		
		
		ArrayList<String> cleanDirlist=new ArrayList<String>();
		for(Entry<String,RAMDirectory> e:ramDirectory.entrySet())
		{
			String name=new File(e.getKey()).getName();
			yyyymmdd_matcher.reset(name);
			if(yyyymmdd_matcher.find()&&!allowlist.contains(name))
			{
				cleanDirlist.add(e.getKey());
			}
		}
		for(String p:cleanDirlist)
		{
			ramDirectory.remove(p);
		}

		
		for(int i=0;i<7;i++)
		{
			allowlist.add(fmt.format(new Date(today.getTime()-1000l*3600*24*i)));
		}
		
		File dir=new File(core.getDataDir());
		if(dir.exists())
		{
			for(File f:dir.listFiles())
			{
				String name=f.getName();
				yyyymmdd_matcher.reset(name);
				if(f.isDirectory()&&yyyymmdd_matcher.find())
				{
					if(!allowlist.contains(name))
					{
						 FileUtils.forceDelete(f);
					}
				}
			}
		}
	}
}
