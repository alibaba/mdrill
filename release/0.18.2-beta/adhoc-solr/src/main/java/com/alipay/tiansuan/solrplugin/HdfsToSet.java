package com.alipay.tiansuan.solrplugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.solr.schema.DateField;

import backtype.storm.utils.Utils;

import com.alimama.mdrill.utils.HadoopUtil;



public class HdfsToSet <A>{
    public Configuration getConf() {
        return HadoopUtil.getConf(Utils.readStormConfig());
    }
    
    public Set<A> toset(String file,TransType<A> trans) throws IOException
    {
	Set<A> inlist = new HashSet<A>();
	Configuration conf=this.getConf();
	Path p=new Path(file);
	
	FileSystem fs = p.getFileSystem(conf);
	FSDataInputStream in = fs.open(p);
	BufferedReader br = new BufferedReader(new InputStreamReader(in));
	String s1 = null;
	while ((s1 = br.readLine()) != null) {
	    String line=s1.trim();
	    if(!line.isEmpty())
	    {
		inlist.add(trans.trans(line));
	    }
	}
	br.close();
	in.close();
	
	return inlist;
    }
    
    

    public static interface TransType<A>{
	public A trans(String str);
	public String transBack(A a);
    }
    
    public static class TransLong implements TransType<Long>
    {
	
        public Long trans(String str) {
	    return Long.parseLong(str);
        }

	
        public String transBack(Long a) {
	    return String.valueOf(a);
        }
    }
    
    
    public static class TransFloat implements TransType<Float>
    {
	
        public Float trans(String str) {
	    return Float.parseFloat(str);
        }

	
        public String transBack(Float a) {
	    return String.valueOf(a);
        }
    }
    
    
    public static class TransDouble implements TransType<Double>
    {
	
        public Double trans(String str) {
	    return Double.parseDouble(str);
        }

	
        public String transBack(Double a) {
	    return String.valueOf(a);
        }
    }
    
    public static class TransDate implements TransType<Long>
    {
    	DateField dateField = new DateField();
	
        public Long trans(String str) {
	    return dateField.parseMath(null, str).getTime();
        }

	
        public String transBack(Long a) {
		Date d=new Date();
		d.setTime(a);
	    return dateField.toInternal(d);
        }
    }
    
    
    public static class TransInt implements TransType<Integer>
    {
	
        public Integer trans(String str) {
	    return Integer.parseInt(str);
        }

	
        public String transBack(Integer a) {
	    return String.valueOf(a);
        }
    }
    
    
    public static class TransString implements TransType<String>
    {
	
        public String trans(String str) {
	    return str;
        }

	
        public String transBack(String a) {
	    return a;
        }
    }

}
