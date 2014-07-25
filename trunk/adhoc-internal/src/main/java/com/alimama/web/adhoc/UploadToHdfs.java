package com.alimama.web.adhoc;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.alimama.mdrill.utils.HadoopUtil;

import backtype.storm.utils.Utils;

public class UploadToHdfs {
	public static HashMap<String,String> upload(HttpServletRequest request, HttpServletResponse response,OutputStreamWriter outStream) throws IOException
	{
		SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
		String day = fmt.format(new Date());
		Map stormconf = Utils.readStormConfig();
		String hdpConf = (String) stormconf.get("hadoop.conf.dir");
		String connstr = (String) stormconf.get("higo.download.offline.conn");
		String uname = (String) stormconf.get("higo.download.offline.username");
		String passwd = (String) stormconf.get("higo.download.offline.passwd");
		String store = (String) stormconf.get("higo.download.offline.store")
				+ "/" + day + "/upload_" + java.util.UUID.randomUUID().toString();
		
		Configuration conf=getConf(stormconf);
		FileSystem fs=FileSystem.get(conf);
		
		if(!fs.exists(new Path(store)))
		{
			fs.mkdirs(new Path(store));
		}
		
		HashMap<String,String> params=new HashMap<String, String>();
		FSDataOutputStream out=fs.create(new Path(store,String.valueOf(System.currentTimeMillis())));
	    OutputStreamWriter osw = new OutputStreamWriter(out, "UTF-8");

		Upload up=new Upload();
		up.mergerTo(request, response, "gbk", osw, params);
		osw.close();

		out.close();
		
		outStream.append(store);
		outStream.append("<br>\r\n");
		for(Entry<String, String> e:params.entrySet())
		{
			outStream.append(e.getKey());
			outStream.append("=");
			outStream.append(e.getValue());
			outStream.append("<br>\r\n");
		}
		
		return params;
	}
	
	private static Configuration getConf(Map stormconf) {
		String hadoopConfDir = (String) stormconf.get("hadoop.conf.dir");
		String opts = (String) stormconf.get("hadoop.java.opts");
		Configuration conf = new Configuration();
		conf.set("mapred.child.java.opts", opts);
		HadoopUtil.grabConfiguration(hadoopConfDir, conf);
		return conf;
	}
}
