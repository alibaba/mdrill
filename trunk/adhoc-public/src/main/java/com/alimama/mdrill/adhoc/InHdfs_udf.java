package com.alimama.mdrill.adhoc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.io.Text;


public class InHdfs_udf extends UDF {
    private static HashMap<String, HashSet<String>> match=new HashMap<String, HashSet<String>>();
    public Text evaluate(final Text d, String file) {
        if (d == null) {
            return new Text("-");
        }
        HashSet<String> set=match.get(file);
        if(set==null)
 {
			try {
				set = new HashSet<String>();
				Configuration conf = new Configuration();
				Path p = new Path(file);
				FileSystem fs = p.getFileSystem(conf);
				if(fs.exists(p))
				{
					FSDataInputStream in = fs.open(p);
					BufferedReader br = new BufferedReader(new InputStreamReader(in));
					String s1 = null;
					while ((s1 = br.readLine()) != null) {
						String line = s1.trim();
						if (!line.isEmpty()) {
							set.add(line);
						}
					}
					br.close();
					in.close();
				}
				match.put(file, set);
			} catch (IOException e) {
			}
			

		}
        
        
        
        if(set.contains(d.toString()))
        {
        	return new Text("ok");
        }
        
        return new Text("-");
        
    }
    

}