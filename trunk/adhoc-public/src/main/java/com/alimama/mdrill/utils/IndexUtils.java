package com.alimama.mdrill.utils;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.CRC32;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;



public class IndexUtils {
    private static final String ENCODE_NAME = "UTF-8";
    private static Logger LOG = Logger.getLogger(IndexUtils.class);


  
    public static String getPath(String storePath,int taskIndex,int offset,FileSystem lfs)
    {
		CRC32 crc32 = new CRC32();
		 crc32.update(String.valueOf(taskIndex).getBytes());
		 long crcvalue = crc32.getValue();
		 if(crcvalue<0)
			{
			 crcvalue*=-1;
			}
	if(offset<0)
	{
	    offset*=-1;
	}
	String[] pathlist=storePath.split(",");
	for(int i=0;i<pathlist.length;i++)
	{
        	Integer index=(int) ((offset+crcvalue+i)%pathlist.length);
        	String stopath=  pathlist[index].trim();
        	if(diskCheck(stopath,lfs))
        	{
            		return stopath;
        	}

	}
	Integer index=(int) ((offset+crcvalue)%pathlist.length);
	String stopath=  pathlist[index].trim();
	return stopath;
    }
    
	private static synchronized boolean  diskCheck(String stopath, FileSystem lfs) {
		Path localpath = new Path(new Path(stopath, "higo"), "diskcheck");
		TryLockFile lck=new TryLockFile((new File(localpath.toString(), "lock")).getAbsolutePath());
		try {
			try {
				if (lfs.exists(localpath)) {
					lfs.delete(localpath, true);
				}
				lfs.mkdirs(localpath);
				lck.trylock();

				File vfile = new File(localpath.toString(), "vertify");

				Long vertify = System.currentTimeMillis();

				DataOutputStream dos = new DataOutputStream(new FileOutputStream(vfile));
				dos.writeLong(vertify);
				dos.close();

				DataInputStream dis = new DataInputStream(new FileInputStream(
						vfile));
				Long readvertify = dis.readLong();
				dis.close();

				if (vertify.equals(readvertify)) {
					return true;
				}

			} catch (IOException e) {
				return false;
			}

			return false;

		} catch (Exception e) {
			return false;
		} finally {
			lck.unlock();
		}
	}

    public static String getHdfsForder(int index) {
	StringBuilder cmd = new StringBuilder();
	cmd.append("part-00").append(index > 99 ? "" : "0")
	        .append(index > 9 ? index : "0" + index);
	return cmd.toString();
    }
    
    public static void main(String[] args) throws IOException {

    	FileSystem fs;
    	FileSystem lfs;
    	Configuration conf=new Configuration();
    	HadoopUtil.grabConfiguration("/home/taobao/config", conf);
    	fs = FileSystem.get(conf);
		lfs = FileSystem.getLocal(conf);
		
		IndexUtils.copyToLocal(fs, lfs, new Path(args[0]),new Path(args[1]),new Path(args[2]),true);

	}
    
    public static boolean copyToLocal(FileSystem fs, FileSystem lfs, Path src,
			Path target, Path tmpparent) throws IOException {
    	return copyToLocal(fs, lfs, src, target, tmpparent,false);
    }
    private static AtomicInteger tmpIndex=new AtomicInteger(0);
	public static boolean copyToLocal(FileSystem fs, FileSystem lfs, Path src,
			Path target, Path tmpparent,boolean checkzip) throws IOException {
		int index=tmpIndex.incrementAndGet();
		if(index>=128)
		{
			tmpIndex.set(0);
			index=tmpIndex.incrementAndGet();
		}
		Path tmp=new Path(tmpparent,String.valueOf(index));
		
		LOG.info("higolog copyToLocal begin " + src.toString() + ","+ target.toString()+","+tmp.toString());
		try {
			truncate(lfs, tmp);
			boolean isziped=false;
			if(checkzip&&!fs.getFileStatus(src).isDir())
			{
				try{
				ZipUtils.unZip(fs, src.toString(), lfs, tmp.toString());
				isziped=true;
				}catch(Throwable e)
				{
					LOG.error("higolog copyToLocal err " + src.toString() + ","+ target.toString()+","+tmp.toString(),e);

					isziped=false;
				}
			}
			if(!isziped)
			{
				fs.copyToLocalFile(src, tmp);
			}
			if (!lfs.exists(tmp)) {
				LOG.info("higolog copyToLocal non exists");
				return false;
			}
		} catch (IOException e) {
			LOG.error("higolog copyToLocal error", e);
			return false;
		}

		
		truncate(lfs, target);
		LOG.info("higolog copyToLocal rename  " + src.toString() + ","+ target.toString()+","+tmp.toString());
		lfs.rename(tmp, target);
		return true;
	}
    
    public static void truncate(FileSystem lfs,Path target) throws IOException
    {
		LOG.info("truncate "+target.toString());
		if (lfs.exists(target)) {
		    lfs.delete(target, true);
		}
		lfs.mkdirs(target.getParent());
    }
    public static String readVertify(FileSystem fs, Path file) throws IOException {
		Path vertify = new Path(file, "vertify");
		StringBuffer rtn = new StringBuffer();
	
		if (fs.exists(vertify)) {
		    Integer bytesRead = 0;
		    int size = 10240;
		    int maxsize = 1024 * 1024;
		    byte[] buff = new byte[size];
		    FSDataInputStream in = fs.open(vertify);
	
		    while (true) {
			int num = in.read(buff, 0, size);
			if (num < 0) {
			    break;
			}
			bytesRead += num;
			rtn.append(new String(buff, 0, num, ENCODE_NAME));
			if (bytesRead >= maxsize) {
			    break;
			}
		    }
		    in.close();
	
		}
		return rtn.toString().trim();
    }
    
    public static class Vertify{
	public Path path;
	public String vertify;
	public Vertify(Path path, String vertify) {
	    super();
	    this.path = path;
	    this.vertify = vertify;
        }
	public Path getPath() {
            return path;
        }
	public String getVertify() {
            return vertify;
        }
    }
    
    public static HashMap<String,Vertify> readVertifyList(FileSystem fs, Path dir) throws IOException
    {
	HashMap<String,Vertify> rtn=new HashMap<String, Vertify>();
	if(fs.exists(dir))
	{
	    FileStatus[] list=fs.listStatus(dir);
	    for(FileStatus d:list)
	    {
		String dirname=d.getPath().getName();
		if(!d.isDir()||dirname.startsWith("_")||dirname.startsWith(".")||dirname.equals("index")||dirname.equals("spellchecker")||dirname.equals("default"))
		{
		    continue;
		}
		Path p=d.getPath();
		Vertify ver=new Vertify(p, IndexUtils.readVertify(fs, p));
		rtn.put(p.getName(), ver);
	    }
	}
	
	return rtn;
    }
    
}
