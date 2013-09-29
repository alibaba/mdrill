package org.apache.solr.request.join;

import java.io.File;
import java.io.IOException;

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
import org.apache.solr.core.SolrResourceLoader;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.request.mdrill.GroupListCache;

import org.apache.solr.request.mdrill.MdrillUtils.UnvertFields;
import org.apache.solr.search.QParser;
import org.apache.solr.search.SolrIndexSearcher;
import org.apache.solr.util.RefCounted;

import com.alimama.mdrill.utils.HadoopUtil;
import com.alimama.mdrill.utils.IndexUtils;
import com.alimama.mdrill.utils.TryLockFile;

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
		String path=null;
		TryLockFile lock=null;
		try{
			lock=new TryLockFile(HigoJoinUtils.pathForLock(req, tablename));
			lock.trylock();
			path=pathToLocal(req, tablename);
		}finally{
			if(lock!=null)
			{
				lock.unlock();
			}
		}
		LOG.info("###joinpath###"+path);
		return req.getCore().getSearcherByPath(null,"join@"+SolrResourceLoader.getCacheFlushKey(null)+"@"+String.valueOf(path), path, false, false, false);
	}
	
	public static interface MakeGroups
	{
		public  boolean toGroupsByJoin(int doc,GroupListCache.GroupList group,UnvertFields ufs,HigoJoinInvert[] joinInvert) throws IOException;
	}
	
	public static class MakeGroupsDefault implements MakeGroups
	{
		@Override
		public boolean toGroupsByJoin(int doc, GroupListCache.GroupList group,
				UnvertFields ufs, HigoJoinInvert[] joinInvert)throws IOException {
			group.reset();
			for (int i:ufs.listIndex) {
				group.list[i]=ufs.cols[i].uif.termNum(doc);
			}
			return true;
		}
	}
	
	public static class MakeGroupsJoin implements MakeGroups
	{
		LinkedBlockingQueue<GroupListCache.GroupList> groupListCache;
		public MakeGroupsJoin(LinkedBlockingQueue<GroupListCache.GroupList> groupListCache) {
			this.groupListCache = groupListCache;
		}
		@Override
		public boolean toGroupsByJoin(int doc, GroupListCache.GroupList group,
				UnvertFields ufs, HigoJoinInvert[] joinInvert) throws IOException {
			group.reset();
			for (int i:ufs.listIndex) {
				group.list[i]=ufs.cols[i].uif.termNum(doc);
			}

			int joinoffset=ufs.length;
			for(HigoJoinInvert inv:joinInvert)
			{

				boolean rtn=inv.fieldNum(doc,joinoffset,group,groupListCache);
				if(!rtn)
				{
					return false;
				}
				joinoffset+=inv.fieldCount();
			}
			
			return true;
			
		}
		
		
	}
	
	
	private static java.util.concurrent.ConcurrentHashMap<String, Long> cleartimes=new ConcurrentHashMap<String, Long>();
	private static  long checkinterval=1000l*600;
	private static  long cleanTimesinterval=1000l*3600*24;
	private static int checkMaxDirCount=64;
	private static void maybeclear(File basedir) throws IOException
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
	
	public static String pathForLock(SolrQueryRequest req,String tablename) throws IOException
	{
		int hashcode=tablename.hashCode();
		hashcode=hashcode<0?hashcode*-1:hashcode;
		int len= HigoJoinUtils.localStoreBase.length;
		String choosepase= HigoJoinUtils.localStoreBase[hashcode%len];
		
		File lockbase=new File(choosepase,"higojoin_lock_pathForLock");
		
		if(!lockbase.exists())
		{
			lockbase.mkdirs();
		}
		
		return new File(lockbase.getAbsolutePath(),String.valueOf(hashcode%100)).getAbsolutePath();
	
	}

	
	private static String pathToLocal(SolrQueryRequest req,String tablename) throws IOException
 {
		int hashcode = tablename.hashCode();
		hashcode = hashcode < 0 ? hashcode * -1 : hashcode;
		int len = HigoJoinUtils.localStoreBase.length;
		String choosepase = HigoJoinUtils.localStoreBase[hashcode % len];

		File basedir = new File(choosepase, "higojoin_work");
		File tmpbase = new File(choosepase, "higojoin_tmp");

		File workPath = new File(basedir, tablename);
		File tmp = new File(tmpbase, tablename);

		File completePath = new File(workPath, "complete");

		maybeclear(basedir);
		if (completePath.exists()) {
			return workPath.getAbsolutePath();
		}

		if (!tmpbase.exists()) {
			tmpbase.mkdirs();
		}
		if (!basedir.exists()) {
			basedir.mkdirs();
		}

		if (!completePath.exists()) {
			Configuration conf = getConf();
			FileSystem fs = FileSystem.get(conf);
			FileSystem lfs = FileSystem.getLocal(conf);
			String hdfsstorepath = req.getParams().get(getPath(tablename));

			String rtnpath = workPath.getAbsolutePath();
			Path storepath = new Path(rtnpath, "store");
			boolean iscopy = IndexUtils.copyToLocal(fs, lfs, new Path(
					hdfsstorepath), storepath, new Path(tmp.getAbsolutePath()),
					true);

			Path indexlinks = new Path(rtnpath, "indexLinks");
			if (iscopy) {
				if (lfs.exists(indexlinks)) {
					lfs.delete(indexlinks, true);
				}
				FSDataOutputStream outlinks = lfs.create(indexlinks);
				outlinks.write((new String(storepath.toString() + "\r\n"))
						.getBytes());
				outlinks.close();
				completePath.mkdirs();
			}
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
