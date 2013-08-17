package com.alimama.mdrill.index;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;


public class JobIndexParse {
	public JobIndexParse(FileSystem fs) {
		this.fs = fs;
	}

	private FileSystem fs;
	
	public void writeStr(Path file, String contents) throws IOException {
		if (fs.exists(file)) {
			fs.delete(file, true);
		}

		FSDataOutputStream write = fs.create(file);
		write.write(contents.getBytes());
		write.close();
	}
	
	
	public Set<String> readPartion(Path dir) throws IOException {
		HashSet<String> rtn = new HashSet<String>();
		if (fs.exists(dir)) {
			FileStatus[] list = fs.listStatus(dir);
			for (FileStatus d : list) {
				String dirname = d.getPath().getName();
				if (!d.isDir() || dirname.startsWith("_")
						|| dirname.startsWith(".") || dirname.equals("index")) {
					continue;
				}
				Path p = d.getPath();
				rtn.add(p.getName());
			}
		}
		return rtn;
	}

	public String readFirstLineStr(Path file) {
		StringBuffer buff = new StringBuffer();
		try {
			if (fs.exists(file)) {
				FSDataInputStream r = fs.open(file);
				BufferedReader in = new BufferedReader(new InputStreamReader(r,
						"UTF-8"));
				buff.append(in.readLine());
				in.close();
				r.close();
			}
		} catch (IOException e) {
		}
		return buff.toString();
	}

	public Path distribute(String output) {
		return new Path(output + "_DistributedCache");
	}

	public Path smallIndex(String output) {
		return new Path(output + "_smallIndex");
	}

}
