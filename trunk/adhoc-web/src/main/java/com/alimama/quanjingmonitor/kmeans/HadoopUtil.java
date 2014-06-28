package com.alimama.quanjingmonitor.kmeans;

import java.io.IOException;
import java.util.Arrays;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;




public class HadoopUtil {
//	  
//	  
//	  private HadoopUtil() { }
//
//	  public static void delete(Configuration conf, Iterable<Path> paths) throws IOException {
//	    if (conf == null) {
//	      conf = new Configuration();
//	    }
//	    for (Path path : paths) {
//	      FileSystem fs = path.getFileSystem(conf);
//	      if (fs.exists(path)) {
////	        log.info("Deleting "+path.toString());
//	        fs.delete(path, true);
//	      }
//	    }
//	  }
//	  
//	  public static void delete(Configuration conf, Path... paths) throws IOException {
//	    delete(conf, Arrays.asList(paths));
//	  }
//
//

}
