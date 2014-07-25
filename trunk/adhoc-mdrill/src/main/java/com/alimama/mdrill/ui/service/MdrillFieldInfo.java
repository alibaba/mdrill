package com.alimama.mdrill.ui.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.alimama.mdrill.utils.HadoopUtil;

import backtype.storm.utils.Utils;

public class MdrillFieldInfo {

	public static String basePath = null;

	

	static HashMap<String,FieldsInfo> cache = new HashMap<String,FieldsInfo>();

	
	private static Configuration getConf(Map stormconf) {
		String hadoopConfDir = (String) stormconf.get("hadoop.conf.dir");
		String opts = (String) stormconf.get("hadoop.java.opts");
		Configuration conf = new Configuration();
		conf.set("mapred.child.java.opts", opts);
		HadoopUtil.grabConfiguration(hadoopConfDir, conf);
		return conf;
	}
	

	


	public static String getBasePath(Map stormconf)
	{
		basePath=(String) stormconf.get("higo.table.path");
		if (basePath == null || basePath.isEmpty()) {
			basePath = System.getenv("higo.table.path");
		}
		if (basePath == null || basePath.isEmpty()) {
			basePath = System.getProperty("higo.table.path",
					"/group/tbdp-etao-adhoc/p4padhoc/tablelist");
		}
		
		if (basePath == null || basePath.isEmpty()) {
			basePath = "/group/tbdp-etao-adhoc/p4padhoc/tablelist";
		}
		
		return basePath;
	}
	
	
	public static class FieldsInfo{
		public LinkedHashMap<String, String> info=new LinkedHashMap<String, String>();
		private long ts=System.currentTimeMillis();
		
		public boolean istimeout()
		{
			return  (System.currentTimeMillis()-ts)>1000l*3600;
		}
	}
	
	public static synchronized LinkedHashMap<String, String> readFieldsFromSchemaXml(Map stormconf,
			String tablename) throws Exception {

		if (tablename.equals("rpt_p4padhoc_auction")) {
			tablename = "rpt_hitfake_auctionall_d";
		}

		FieldsInfo datatype = cache.get(tablename);
		if (datatype != null&&(!datatype.istimeout())) {
			return datatype.info;
		}
		datatype = new FieldsInfo();

		Configuration conf = getConf(stormconf);
		FileSystem fs = FileSystem.get(conf);
		String regex = "<field\\s+name=\"([^\"]*?)\"\\s+type=\"([^\"]*?)\"\\s+indexed=\"([^\"]*?)\"\\s+stored=\"([^\"]*?)\"\\s*.*/>";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher("");
		BufferedReader br = null;
		try {
			

			FSDataInputStream in = fs.open(new Path(getBasePath(stormconf), tablename
					+ "/solr/conf/schema.xml"));
			br = new BufferedReader(new InputStreamReader(in));
			String temp = null;
			while ((temp = br.readLine()) != null) {
				matcher.reset(temp);
				if (matcher.find()) {
					datatype.info.put(matcher.group(1), matcher.group(2));
				}
			}
			in.close();
		}catch(Exception e){
		} finally {
			if (br != null) {
				br.close();
			}
		}

		cache.put(tablename, datatype);
		return datatype.info;

	}
	
}
