package com.alimama.mdrill.editlog.defined;


import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public class StorageDirectory {

	private FileSystem fs;
	private Path dir;

	public StorageDirectory(FileSystem fs, Path dir) {
		this.fs = fs;
		this.dir = dir;
	}

	public FileSystem getFileSystem() {
		return fs;
	}
	
	public Path getCurrentDir() {
		return dir;
	}
	
	public boolean isrequire()
	{
		return false;
	}

}
