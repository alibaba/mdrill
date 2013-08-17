package com.alipay.tiansuan.solrplugin;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SelectIn {
	public static void main(String[] args) throws IOException {
		
		//一定要为单个shards
		String urlstr = "http://172.24.195.155:51119/update/userimport?istmp=true&filepath=/group/taobao/external/p4p/p4padhoc/tmp/selectin/"+1;
		URL url = new URL(urlstr);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		String BOUNDARY = "---------7d4a6d158c9"; // 定义数据分隔线
		byte[] end_data = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();
		conn.setRequestProperty("charset", "utf-8");
		conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
		conn.setRequestMethod("POST");

		conn.setDoOutput(true);
		conn.setDoInput(true);
		conn.setUseCaches(false);
		DataOutputStream out = new DataOutputStream(conn.getOutputStream());

		StringBuilder sb = new StringBuilder();
		sb.append("--");
		sb.append(BOUNDARY);
		sb.append("\r\n");
		sb.append("Content-Disposition: form-data;name=\"file"+1+"\";filename=\"1\"\r\n");
		sb.append("Content-Type:application/octet-stream\r\n\r\n");
		byte[] data = sb.toString().getBytes();
		out.write(data);
		
		//多个查询在这里写入
		for(int i=0;i<args.length;i++)
		{
			out.write(new String(args[i]+"\n").getBytes());
		}
		
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
		System.out.println(buff.toString());
		
		
		//将来查询的时候fq这样写fq={!inhdfs%20f=user_id}/group/taobao/external/p4p/p4padhoc/tmp/selectin/1

	}
}
