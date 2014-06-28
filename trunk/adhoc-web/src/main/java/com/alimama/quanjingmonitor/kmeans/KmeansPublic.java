package com.alimama.quanjingmonitor.kmeans;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.Reader;
import org.apache.hadoop.io.Text;

public class KmeansPublic {
	public static PathFilter FILTER=new org.apache.hadoop.fs.PathFilter() {
		public boolean accept(Path path) {
			String name = path.getName();
			return !(name.endsWith(".crc")
					|| name.startsWith(".") || name
					.startsWith("_"));
		}
	};

	public static void configureWithClusterInfo(Configuration conf,
	                                              Path clusterPathStr,
	                                              Collection<Cluster> clusters) throws IOException {
	
	    Path clusterPath = new Path(clusterPathStr, "*");
	    Collection<Path> result = new ArrayList<Path>();
	
	    // get all filtered file names in result list
	    FileSystem fs = clusterPath.getFileSystem(conf);
	    FileStatus[] matches = fs.listStatus(FileUtil.stat2Paths(fs.globStatus(clusterPath, FILTER)),FILTER);
	
	    for (FileStatus match : matches) {
	      result.add(fs.makeQualified(match.getPath()));
	    }
	
	    // iterate through the result path list
	    for (Path path : result) {
	    	SequenceFile.Reader reader= new SequenceFile.Reader(fs, path, conf);
	    	Text key=new Text();
	    	Cluster clu=new Cluster();
	      while (reader.next(key, clu)) {
	          clusters.add(new Cluster(clu));
	      }
	      reader.close();
	    }
	    
	    System.out.println("####clusters.size="+clusters.size());
	  }
}
