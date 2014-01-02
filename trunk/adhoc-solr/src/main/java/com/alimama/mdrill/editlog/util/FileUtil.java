package com.alimama.mdrill.editlog.util;

import java.io.File;
import java.io.IOException;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public class FileUtil {

	  public static File[] listFiles(File dir) throws IOException {
	    File[] files = dir.listFiles();
	    if(files == null) {
	      throw new IOException("Invalid directory or I/O error occurred for dir: "
	                + dir.toString());
	    }
	    return files;
	  }  
	  
	  
	  
	  public static FileStatus[] listFiles(FileSystem fs,Path dir) throws IOException {
		  FileStatus[] files= fs.listStatus(dir);
		    if(files == null) {
		      throw new IOException("Invalid directory or I/O error occurred for dir: "
		                + dir.toString());
		    }
		    return files;
		  } 
}
