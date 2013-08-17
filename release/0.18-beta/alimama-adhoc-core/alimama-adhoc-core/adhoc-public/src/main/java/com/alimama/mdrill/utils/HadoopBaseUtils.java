package com.alimama.mdrill.utils;

import java.io.File;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;



public class HadoopBaseUtils {
    public static long size(String dataPath,Configuration conf) throws IOException {
    	FileSystem fs=FileSystem.get(conf);
        long size = 0L;
        if (dataPath == null || dataPath.trim().isEmpty()) {
            return size;
        }
        Path p = new Path(dataPath);

        FileStatus[] fileStatus = new FileStatus[0];
        try {
            if (!fs.exists(p)) {
                return size;
            }
            fileStatus = fs.listStatus(new Path(dataPath));
        } catch (IOException e) {
        }
        if (fileStatus != null) {
            for (FileStatus file : fileStatus) {
                size += file.getLen();
            }
        } else {
        }
        return size;
    }
    public static Configuration grabConfiguration(String hadoopConfDir, Configuration conf){
		boolean oldVersionHadoop = new File(hadoopConfDir, "hadoop-default.xml").exists() || new File(hadoopConfDir, "hadoop-site.xml").exists();
		if(oldVersionHadoop){
			conf.addResource(new Path(hadoopConfDir, "hadoop-default.xml"));
		    conf.addResource(new Path(hadoopConfDir, "hadoop-site.xml"));
		}else{
			conf.addResource(new Path(hadoopConfDir, "mapred-site.xml"));
		    conf.addResource(new Path(hadoopConfDir, "hdfs-site.xml"));
		    conf.addResource(new Path(hadoopConfDir, "core-site.xml"));
		}
		return conf;
	}
}
