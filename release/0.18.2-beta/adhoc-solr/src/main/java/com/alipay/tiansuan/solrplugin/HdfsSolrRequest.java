package com.alipay.tiansuan.solrplugin;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HdfsSolrRequest {
    private HttpURLConnection conn;
    private OutputStream out;
    private String BOUNDARY = "---------7d4a6d158c9"; // 定义数据分隔线
    private byte[] end_data = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();// 定义最后数据分隔线

   public HdfsSolrRequest(String solr,String hdfs) throws IOException
   {
       String urlstr = solr+"/update/userimport?isdel=false&hdfsfile="+java.net.URLEncoder.encode(hdfs, "utf8")+"";
       this.init(urlstr);
   }
   
   public HdfsSolrRequest(String solr,Boolean isdel) throws IOException
   {
       String urlstr = solr+"/update/userimport?isdel="+Boolean.toString(isdel);
       this.init(urlstr);

   }
   
   private void init(String urlstr) throws IOException
   {

	URL url = new URL(urlstr);
	conn = (HttpURLConnection) url.openConnection();
	conn.setRequestProperty("charset", "utf-8");
	conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
	conn.setRequestMethod("POST");

	conn.setDoOutput(true);
	conn.setDoInput(true);
	conn.setUseCaches(false);
	
	
	out = new DataOutputStream(conn.getOutputStream());

	StringBuilder sb = new StringBuilder();

	sb.append("--");

	sb.append(BOUNDARY);

	sb.append("\r\n");

	sb.append("Content-Disposition: form-data;name=\"file"+1+"\";filename=\"1\"\r\n");

	sb.append("Content-Type:application/octet-stream\r\n\r\n");

	byte[] data = sb.toString().getBytes();
	out.write(data);
   }
   
   public void append(String userid) throws IOException
   {
	out.write(new String(userid+"\n").getBytes());

   }
   
   public String complete() throws IOException
   {
       out.write(end_data);
       out.flush();
	out.close();
	BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	String line = null;
	StringBuffer buff=new StringBuffer();
	while ((line = reader.readLine()) != null) {
	    buff.append(line);
	}
	reader.close();
	return buff.toString();
   }
}
