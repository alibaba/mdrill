package org.apache.solr.request.join;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.request.mdrill.MdrillPorcessUtils;
import org.apache.solr.request.mdrill.MdrillPorcessUtils.GroupList;
import org.apache.solr.request.mdrill.MdrillPorcessUtils.UnvertFields;
import org.apache.solr.search.QParser;
import org.apache.solr.search.SolrIndexSearcher;
import org.apache.solr.util.RefCounted;

import com.alimama.mdrill.utils.HadoopUtil;
import com.alimama.mdrill.utils.IndexUtils;

public class HigoJoinUtils {
	private static Logger LOG = Logger.getLogger(HigoJoinUtils.class);

	public static String hadoopConfDir;
	public static void setHdfsConfDir(String dir)
	{
		hadoopConfDir=dir;
	}
	public static Configuration getConf() {
		Configuration conf = new Configuration();
		HadoopUtil.grabConfiguration(hadoopConfDir, conf);
		return conf;
	}
	public static String[] localStoreBase={};
	public static void setLocalStorePath(String[] localStore)
	{
		HigoJoinUtils.localStoreBase=localStore;
	}
	public static String getFq(String tablename) {
		return "join.fq." + tablename;
	}
	
	public static String getTables() {
		return "join.tables";
	}
	
	public static String getPath(String tablename) {
		return "join.path." + tablename;
	}
	
	public static String getFields(String tablename) {
		return "join.fl." + tablename;
	}
	
	public static String getLeftField(String tablename) {
		return "join.leftkey." + tablename;
	}
	
	public static String getRightField(String tablename) {
		return "join.rightkey." + tablename;
	}
	
	public static String getsortField(String tablename) {
		return "join.sort." + tablename;
	}
	
	public static RefCounted<SolrIndexSearcher> getSearch(SolrQueryRequest req,String tablename) throws IOException
	{
		String path=pathToLocal(req, tablename);
		LOG.info("###joinpath###"+path);
		return req.getCore().getSearcherByPath(String.valueOf(path), path, false, false, false);
	}
	
	
	public static interface MakeGroups
	{
		public  ArrayList<GroupList> toGroupsByJoin(int doc,GroupList group,UnvertFields ufs,HigoJoinInvert[] joinInvert) throws IOException;
		
	}
	
	public static class MakeGroupsDefault implements MakeGroups
	{
		ArrayList<GroupList> val=new ArrayList<MdrillPorcessUtils.GroupList>(1);
		@Override
		public ArrayList<GroupList> toGroupsByJoin(int doc, GroupList group,
				UnvertFields ufs, HigoJoinInvert[] joinInvert)throws IOException {
			group.reset();
			for (int i:ufs.listIndex) {
				group.list[i]=ufs.cols[i].uif.termNum(doc);
			}
			 val.set(0, group);
			 return val;
		}
	}
	
	public static class MakeGroupsJoin implements MakeGroups
	{
		LinkedBlockingQueue<GroupList> groupListCache;
		public MakeGroupsJoin(LinkedBlockingQueue<GroupList> groupListCache) {
			this.groupListCache = groupListCache;
		}
		@Override
		public ArrayList<GroupList> toGroupsByJoin(int doc, GroupList group,
				UnvertFields ufs, HigoJoinInvert[] joinInvert) throws IOException {
			group.reset();
			for (int i:ufs.listIndex) {
				group.list[i]=ufs.cols[i].uif.termNum(doc);
			}
			ArrayList<GroupList> tmp=new ArrayList<MdrillPorcessUtils.GroupList>(1);
			ArrayList<GroupList> newgroup=new ArrayList<MdrillPorcessUtils.GroupList>(1);
			newgroup.add(group.copy(this.groupListCache));

			int joinoffset=ufs.length;
			for(HigoJoinInvert inv:joinInvert)
			{
				for(GroupList base:newgroup)
				{
					GroupList[] list=inv.fieldNum(doc,joinoffset,base,groupListCache);
					if(list!=null)
					{
						for(GroupList g:list)
						{
							tmp.add(g);
						}
					}
				}
				newgroup=tmp;
				tmp=new ArrayList<MdrillPorcessUtils.GroupList>();
				joinoffset+=inv.fieldCount();
			}
			
			return newgroup;
		
		}
		
		
	}
	
	
	private static java.util.concurrent.ConcurrentHashMap<String, Long> cleartimes=new ConcurrentHashMap<String, Long>();
	private static  long checkinterval=1000l*600;
	private static  long cleanTimesinterval=1000l*3600*24;
	private static int checkMaxDirCount=64;
	private static void maybeclear(File basedir,File lockbase) throws IOException
	{
		String key=basedir.getAbsolutePath();
		Long expirestimes=cleartimes.contains(key)?cleartimes.get(key)+checkinterval:checkinterval;
		long nowtimes=System.currentTimeMillis();
		if(expirestimes>nowtimes)
		{
			LOG.info("nonclean "+key+","+expirestimes+","+nowtimes);
			return ;
		}
		LOG.info("begin clean "+key+","+expirestimes+","+nowtimes);

		cleartimes.put(key,nowtimes );

		
		File lockPath=new File(lockbase,"higo_clear_lock");
		if(!lockbase.exists())
		{
			lockbase.mkdirs();
		}
		if (!lockPath.exists()) {
			lockPath.createNewFile();
		}
		FileLock flout = null;
		RandomAccessFile out = null;
		FileChannel fcout = null;
		try{
				out = new RandomAccessFile(lockPath, "rw");
				fcout = out.getChannel();
				flout = fcout.lock();

				Configuration conf=getConf();
				FileSystem lfs = FileSystem.getLocal(conf);
				Path basepath=new Path(key);
				if(!lfs.exists(basepath))
				{
					return ;
				}
				FileStatus[] list=lfs.listStatus(basepath);
				if(list==null)
				{
					return ;
				}
				
				long cleantimes=nowtimes-cleanTimesinterval;
				ArrayList<Path> toremove=new ArrayList<Path>();
				ArrayList<cleanPair> toSave=new ArrayList<cleanPair>();
				for(FileStatus s:list)
				{
					if(!s.isDir())
					{
						continue;
					}
					long lasttimes=Math.max(s.getAccessTime(), s.getModificationTime());
					if(lasttimes<cleantimes)
					{
						LOG.info("drop "+s.getAccessTime()+","+s.getModificationTime()+","+cleantimes+","+s.getPath().toString());
						toremove.add(s.getPath());
					}else{
						toSave.add(new cleanPair(s.getPath(),lasttimes));
					}
				}
				
				for(Path p:toremove)
				{
					lfs.delete(p, true);
				}
				
				if(toSave.size()<=checkMaxDirCount)
				{
					return ;
				}
				Collections.sort(toSave,new Comparator<cleanPair>() {
					@Override
					public int compare(cleanPair o1, cleanPair o2) {
						return o2.t.compareTo(o1.t);
					}
				});
				
				for(int i=checkMaxDirCount;i<toSave.size();i++)
				{
					cleanPair p=toSave.get(i);
					lfs.delete(p.p, true);
				}
		
		}finally
		{
			try {
				if (flout != null) {
					flout.release();
				}
				if (fcout != null) {
					fcout.close();
				}
				if (out != null) {
					out.close();
				}
				out = null;
			} catch (Exception e) {
			}
			
			lockPath.delete();

		}
		
	}
	
	private static class cleanPair{
		public Path p;
		public Long t;
		public cleanPair(Path p, long t) {
			super();
			this.p = p;
			this.t = t;
		}
	}
	private static synchronized String pathToLocal(SolrQueryRequest req,String tablename) throws IOException
	{
		int hashcode=tablename.hashCode();
		hashcode=hashcode<0?hashcode*-1:hashcode;
		int len= HigoJoinUtils.localStoreBase.length;
		String choosepase= HigoJoinUtils.localStoreBase[hashcode%len];
		
		File basedir=new File(choosepase,"higojoin_work");
		File tmpbase=new File(choosepase,"higojoin_tmp");
		File lockbase=new File(choosepase,"higojoin_lock");
		
		File workPath=new File(basedir,tablename);
		File tmp=new File(tmpbase,tablename);
		File lockPath=new File(lockbase,tablename);
		
		File completePath=new File(workPath,"complete");
		
		maybeclear(basedir,lockbase);

		
		FileLock flout = null;
		RandomAccessFile out = null;
		FileChannel fcout = null;
		try {
			if(completePath.exists())
			{
				return workPath.getAbsolutePath();
			}
			
			if(!lockbase.exists())
			{
				lockbase.mkdirs();
			}
			if(!tmpbase.exists())
			{
				tmpbase.mkdirs();
			}
			if(!basedir.exists())
			{
				basedir.mkdirs();
			}
			
			
			if (!lockPath.exists()) {
				lockPath.createNewFile();
			}

			out = new RandomAccessFile(lockPath, "rw");
			fcout = out.getChannel();
			flout = fcout.lock();
		
			if(!completePath.exists())
			{
				Configuration conf=getConf();
				FileSystem fs = FileSystem.get(conf);
				FileSystem lfs = FileSystem.getLocal(conf);
				String hdfsstorepath=req.getParams().get(getPath(tablename));
				
				String rtnpath=workPath.getAbsolutePath();
				Path storepath=new Path(rtnpath,"store");
				boolean iscopy=IndexUtils.copyToLocal(fs, lfs, new Path(hdfsstorepath),storepath,new Path(tmp.getAbsolutePath()));
				
				Path indexlinks=new Path(rtnpath,"indexLinks");
				if(iscopy)
				{
					if(lfs.exists(indexlinks))
					{
						lfs.delete(indexlinks,true);
					}
					FSDataOutputStream outlinks = lfs.create(indexlinks);
					outlinks.write((new String(storepath.toString()	+ "\r\n")).getBytes());
					outlinks.close();
					completePath.mkdirs();
				}
			}
		}finally{
			
			try {
				if (flout != null) {
					flout.release();
				}
				if (fcout != null) {
					fcout.close();
				}
				if (out != null) {
					out.close();
				}
				out = null;
			} catch (Exception e) {
			}
			lockPath.delete();

		}
		
		return workPath.getAbsolutePath();
		
	}

	public static List<Query> getFilterQuery(SolrQueryRequest req,
			String tablename) throws ParseException {
		List<Query> filters = new ArrayList<Query>();
		QParser all = QParser.getParser("*:*", null, req);
		filters.add(all.getQuery());
		String[] fqs = req.getParams()
				.getParams(HigoJoinUtils.getFq(tablename));
		if (fqs != null && fqs.length != 0) {
			for (String fq : fqs) {
				if (fq != null && fq.trim().length() != 0) {
					QParser fqp = QParser.getParser(fq, null, req);
					filters.add(fqp.getQuery());
				}
			}
		}
		
		return filters;
	}
	      
	 
}
