package org.apache.lucene.store;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.lucene.util.Constants;
import org.apache.lucene.util.cache.Cache;
import org.apache.lucene.util.cache.SimpleLRUCache;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.index.KeepOnlyLastCommitDeletionPolicy;
import org.apache.lucene.store.Directory;
import org.apache.solr.core.SolrCore;

import com.alimama.mdrill.hdfsDirectory.FileSystemDirectory;
import com.alimama.mdrill.utils.HadoopUtil;
public abstract class LinkFSDirectory extends FSDirectory {
	@Override
	  public String toString() {
	    return directory.toString();
	  }
	public static LinkFSDirectory open(File path) throws IOException {
		return open(path, null);
	}
	
	public static LinkFSDirectory open(File path,boolean isallowmove) throws IOException {
		LinkFSDirectory rtn= open(path, null);
		rtn.setAllowLinks(isallowmove);
		return rtn;
	}

	public static LinkFSDirectory open(File path, LockFactory lockFactory)
			throws IOException {
		if ((Constants.WINDOWS || Constants.SUN_OS || Constants.LINUX)
				&& Constants.JRE_IS_64BIT && MMapDirectory.UNMAP_SUPPORTED) {
			return new LinkMMapDirectory(path, lockFactory);
		} else if (Constants.WINDOWS) {
			return new LinkSimpleFSDirectory(path, lockFactory);
		} else {
			return new LinkNIOFSDirectory(path, lockFactory);
		}
	}
	
	
	public static String hadoopConfDir;
	public static void setHdfsConfDir(String dir)
	{
		hadoopConfDir=dir;
	}
	public static void truncate(FileSystem lfs,Path target) throws IOException
    {
		if (lfs.exists(target)) {
		    lfs.delete(target, true);
		}
		lfs.mkdirs(target);
    }
	
	public static Configuration getConf() {
		Configuration conf = new Configuration();
		HadoopUtil.grabConfiguration(hadoopConfDir, conf);
		return conf;
	}
	private static Cache<String,Object> localLock=Cache.synchronizedCache(new SimpleLRUCache<String,Object>(10240));
	private static Object maplock=new Object();
  
	public static Directory readOnlyOpen(File path, LockFactory lockFactory) throws IOException
	{
		Object lock=new Object();
		synchronized (maplock) {
			String key=path.getAbsolutePath();
			lock=localLock.get(key);
			if(lock==null)
			{
				lock=new Object();
				localLock.put(key, lock);
			}
		}
		
		Configuration conf = getConf();
		
		synchronized (lock) {
			
			
//			System.out.println("LinkFSDirectory readOnlyOpen "+path.getAbsolutePath());
			File links=new File(path,"indexLinks");
			List<Directory> dirlist=new ArrayList<Directory>();
			System.out.println("links file is"+links.getAbsolutePath());
			if(links.exists())
			{
				FileReader freader= new FileReader(links);
			    BufferedReader br = new BufferedReader(freader);
			    String s1 = null;
			    while ((s1 = br.readLine()) != null) {
			    	if(s1.trim().length()>0)
			    	{
			    		if(s1.indexOf("@hdfs@")<0)
			    		{
			    			
			    			FSDirectory d=LinkFSDirectory.open(new File(s1));
			    			dirlist.add(d);
			    		}else{
			    			dirlist.add(new FileSystemDirectory( FileSystem.get(conf), new Path(s1.replaceAll("@hdfs@", "")), false, conf));
			    		}
			    		SolrCore.log.info(">>>>>LinkFSDirectory readOnlyOpen add links "+s1);
			    	}
			    }
			    br.close();
			    freader.close();
			}
			
			
			if(dirlist.size()==1)
			{
				return dirlist.get(0);
			}
			
			
			File workerspace=new File(path,getWorkDir(path.getAbsolutePath()));
			deleteDirectory(workerspace);
			workerspace.mkdirs();
			
			FSDirectory dir= open(workerspace,lockFactory);
			IndexWriter writer=new IndexWriter(dir, null,new KeepOnlyLastCommitDeletionPolicy(), MaxFieldLength.UNLIMITED);
			writer.setMergeFactor(512);
			writer.setUseCompoundFile(false);
			if(dirlist.size()>0)
			{
				Directory[] dirs=new Directory[dirlist.size()];
				writer.addIndexesNoOptimize(dirlist.toArray(dirs));
			}
			writer.close();
			return dir;
		}
	}
	
	private static HashMap<String, Integer> indexmap=new HashMap<String, Integer>();
	
	public static synchronized String getWorkDir(String path)
	{
		Integer index=indexmap.get(path);
		if(index==null)
		{
			index=0;
		}
		index++;
		if(index>100)
		{
			index=0;
		}
		indexmap.put(path, index);
		return "workerspace_"+index;
	}
	
	public static Directory readOnlyOpen(File path) throws IOException
	{
		return readOnlyOpen(path,null);
	}
	
	
	private static boolean deleteDirectory(File path) {  
	    if( path.exists() ) {  
	      File[] files = path.listFiles();  
	      for(int i=0; i<files.length; i++) {  
	         if(files[i].isDirectory()) {  
	           deleteDirectory(files[i]);  
	         }  
	         else {  
	           files[i].delete();  
	         }  
	      }  
	    }  
	    return( path.delete() );  
	  }  

	public HashMap<String, File> links = new HashMap<String, File>();
	
	//index on hdfs
	public HashMap<String, Path> hdfsLinks = new HashMap<String, Path>();

	private boolean isAllowMove = true;

	public boolean isAllowMove() {
		return isAllowMove;
	}

	public void setAllowLinks(boolean isAllowMove) {
		this.isAllowMove = isAllowMove;
	}


	public LinkFSDirectory(File path, LockFactory lockFactory)
			throws IOException {
		super(path, lockFactory);
	}

	public void addLinks(String name, File p) {
		this.links.put(name, p);
	}
	
	public void addhdfsLinks(String name, Path p) {
		this.hdfsLinks.put(name, p);
	}

	public void copy(Directory to, String src, String dest) throws IOException {
		if (to instanceof LinkFSDirectory) {
			LinkFSDirectory fsto = (LinkFSDirectory) to;
			if (fsto.isAllowMove()) {
				File discfile = new File(fsto.directory, dest);
				File srcfile = new File(this.directory, src);
				fsto.addLinks(discfile.getName(), srcfile);
				return;
			}
		}
		super.copy(to, src, dest);
	}
		
	  public String[] list() throws IOException {
		  return this.listAll();
	  }

	public String[] listAll() throws IOException {
		String[] slist = super.listAll();
		ArrayList<String> rtn = new ArrayList<String>();
		for (int i = 0; i < slist.length; i++) {
			rtn.add(slist[i]);
		}

		for (java.util.Map.Entry<String, File> e : this.links.entrySet()) {
			rtn.add(e.getKey());
		}
		
		for (java.util.Map.Entry<String, Path> e : this.hdfsLinks.entrySet()) {
			rtn.add(e.getKey());
		}
		
		String[] result = new String[rtn.size()];
		return rtn.toArray(result);
	}
	
	  public boolean fileExists(String name) {
			return super.fileExists(name)||this.links.containsKey(name)||this.hdfsLinks.containsKey(name);
	}
	  
	  public long fileModified(String name){
		  
		  if(this.hdfsLinks.containsKey(name))
		  {
			  Configuration conf = getConf();
			  Path p=this.hdfsLinks.get(name);
			  
			  try {
				FileSystemDirectory dir=new FileSystemDirectory(FileSystem.get(conf), p.getParent(), false, conf);
				return dir.fileModified(p.getName());
			} catch (IOException e) {
				SolrCore.log.error("hdfs",e);
			}
		  }
		  
		  if(!this.links.containsKey(name))
		  {
			  return super.fileModified(name);
		  }
		  
		  
		  
		  return this.links.get(name).lastModified();
	  }
	  
	  public  void touchFile(String name)
	  {
		  
		  if(this.hdfsLinks.containsKey(name))
		  {
			  Configuration conf = getConf();
			  Path p=this.hdfsLinks.get(name);
			  
			  try {
				FileSystemDirectory dir=new FileSystemDirectory(FileSystem.get(conf), p.getParent(), false, conf);
				dir.touchFile(p.getName());
			} catch (IOException e) {
				SolrCore.log.error("hdfs",e);
			}
		  }
		  if(!this.links.containsKey(name))
		  {
			  super.touchFile(name);
			  return ;
		  }
		  
		  
		 
		  
		  File file = this.links.get(name);
		  file.setLastModified(System.currentTimeMillis());
	  }
	  
	  
	  
	  public void deleteFile(String name) throws IOException
	  {
		  
		  this.hdfsLinks.remove(name);
		  this.links.remove(name);
		  
		  super.deleteFile(name);

//		  if(this.hdfsLinks.containsKey(name))
//		  {
//			  Configuration conf = getConf();
//			  Path p=this.hdfsLinks.get(name);
//			  
//			  try {
//				FileSystemDirectory dir=new FileSystemDirectory(FileSystem.get(conf), p.getParent(), false, conf);
//				dir.deleteFile(p.getName());
//			} catch (IOException e) {
//				SolrCore.log.error("hdfs",e);
//			}
//		  }
//		  
//		  if(!this.links.containsKey(name))
//		  {
//			  super.deleteFile(name);
//		  }
//		  
//		 
//		  
//		  File file = this.links.get(name);
//		  if(file!=null&&file.exists())
//		  {
//			  file.delete();
//		  }
	  }
	  
	public long fileLength(String name) throws IOException {
		
		 if(this.hdfsLinks.containsKey(name))
		  {
			  Configuration conf = getConf();
			  Path p=this.hdfsLinks.get(name);
			  
			  try {
				FileSystemDirectory dir=new FileSystemDirectory(FileSystem.get(conf), p.getParent(), false, conf);
				return dir.fileLength(p.getName());
			} catch (IOException e) {
				SolrCore.log.error("hdfs",e);
			}
		  }
		
		if (!this.links.containsKey(name)) {
			return super.fileLength(name);
		}
		
		

		File file = this.links.get(name);
		final long len = file.length();
		if (len == 0 && !file.exists()) {
			throw new FileNotFoundException(name);
		} else {
			return len;
		}
	}
	
	  public IndexOutput createOutput(String name) throws IOException
	  {
		  if(this.hdfsLinks.containsKey(name))
		  {
			  Configuration conf = getConf();
			  Path p=this.hdfsLinks.get(name);
			  
			  try {
				FileSystemDirectory dir=new FileSystemDirectory(FileSystem.get(conf), p.getParent(), false, conf);
				return dir.createOutput(p.getName());
			} catch (IOException e) {
				SolrCore.log.error("hdfs",e);
			}
		  }
		  
		  if (!this.links.containsKey(name)) {
				return super.createOutput(name);
			}
			File file = this.links.get(name);

		  return new LinkFSIndexOutput(file);
	  }
	  
	  
	  public void sync(Collection<String> names) throws IOException {
		    Set<String> toSync = new HashSet<String>(names);
		    Set<String> toSyncHdfs = new HashSet<String>(names);

			 Configuration conf = getConf();
			 FileSystemDirectory dir=null;
		    for (String name : names)
		    {
				 if (this.links.containsKey(name)) {
						File file = this.links.get(name);
						this.fsync(file);
				 }else if (this.hdfsLinks.containsKey(name)) {
						Path p = this.hdfsLinks.get(name);
						if(dir==null)
						{
							try {
								dir=new FileSystemDirectory(FileSystem.get(conf), p.getParent(), false, conf);
							} catch (IOException e) {
								SolrCore.log.error("hdfs",e);
							}
						}
						toSyncHdfs.add(name);

				 }else{
					 toSync.add(name);
				 }
		    }
		    
		    if(dir!=null)
		    {
		    	dir.sync(toSyncHdfs);
		    }
		    super.sync(toSync);
		  }
	  
	
	  
	  public void close(){
		  super.close();
	  }
	  
	  
	protected static class LinkFSIndexOutput extends BufferedIndexOutput {
		private final RandomAccessFile file;
		private volatile boolean isOpen;

		public LinkFSIndexOutput(File f) throws IOException {
			file = new RandomAccessFile(f, "rw");
			isOpen = true;
		}

		@Override
		public void flushBuffer(byte[] b, int offset, int size)
				throws IOException {
			file.write(b, offset, size);
		}

		@Override
		public void close() throws IOException {
			if (isOpen) {
				boolean success = false;
				try {
					super.close();
					success = true;
				} finally {
					isOpen = false;
					if (!success) {
						try {
							file.close();
						} catch (Throwable t) {
						}
					} else {
						file.close();
					}
				}
			}
		}

		@Override
		public void seek(long pos) {
			throw new RuntimeException("not allowed");
		}

		@Override
		public long length() throws IOException {
			return file.length();
		}

		@Override
		public void setLength(long length) throws IOException {
			file.setLength(length);
		}
	}
}
