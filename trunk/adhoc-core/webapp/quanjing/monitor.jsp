<%@ page contentType="text/javascript; charset=utf-8" %>
<%@ page import="java.lang.*" %>
<%@ page import="java.io.*" %>
<%@ page import="java.net.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.util.regex.*" %>
<%@ page import="java.text.*" %>
<%@ page import="java.util.Map.*" %>
<%@ page import="javax.servlet.*" %>
<%@ page import="java.text.*" %>


<%!

public String  getMD5(byte[] bytes) { 
        char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' }; 
        char str[] = new char[16 * 2]; 
        try { 
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5"); 
            md.update(bytes); 
            byte tmp[] = md.digest(); 
            int k = 0; 
            for (int i = 0; i < 16; i++) { 
                byte byte0 = tmp[i]; 
                str[k++] = hexDigits[byte0 >>> 4 & 0xf]; 
                str[k++] = hexDigits[byte0 & 0xf]; 
            } 
        } catch (Exception e) { 
        } 
        return new String(str); 
    } ;
    
     
  
  public String  getMD5(String value) { 
        String result = ""; 
        try { 
            result = getMD5(value.getBytes("UTF-8")); 
        } catch (Exception e) { 
        } 
        return result; 
    } ;
	
 
%>

<%
	response.setHeader("Access-Control-Allow-Credentials", "true");
	response.setHeader("Access-Control-Allow-Origin","http://localhost,http://adhoc.etao.com:9999");
  request.setCharacterEncoding("utf-8");
	
  
	

		String url = "http://api.m.alibaba-inc.com/monitor/api/monitorDataQuery.rdo";
		String[] paramsfetch = { "scope","scopeType" ,"monitorType","itemName","hisType","tag","primaryKey","dataType","aggrType","time"," _username","_key"};
			SimpleDateFormat formatDay = new SimpleDateFormat("yyyyMMdd");
    SimpleDateFormat formatDay2 = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat formatDay3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		String thedate=formatDay2.format(formatDay.parse(request.getParameter("thedate")));
		HashMap<String,String> defaultParams=new HashMap<String, String>();
		defaultParams.put(" _username", "quanjingjiankong");
		String strtoday=formatDay.format(new Date());
		
		
		defaultParams.put("_key", getMD5("quanjingjiankong"+strtoday+"/3TzEbObO2MW6VQdlmHmvA=="));
		defaultParams.put("scopeType", "3");
		defaultParams.put("monitorType", "3");
		defaultParams.put("itemName", "system");
		defaultParams.put("hisType", "2");
		defaultParams.put("dataType", "detail");
		defaultParams.put("aggrType", "AVG");
		defaultParams.put("tag", "load5_avg,load15_avg,load1_avg,cpu_avg,cpu_max,cpu_min,mem_avg");
		if(!thedate.equals(formatDay2.format(new Date())))
		{
    	defaultParams.put("time", thedate+" 00:00:00,"+thedate+" 23:59:59");
    }else{
    	    	defaultParams.put("time", thedate+" 00:00:00,"+formatDay3.format(new Date()));
    }
		
		String paramsvalue = null;
		StringBuffer sb = new StringBuffer();
		String joinchar = "";
		for (String p : paramsfetch) {
			 paramsvalue=request.getParameter(p);
			 if(paramsvalue==null)
			 {
			 		paramsvalue=defaultParams.get(p);
			 }
			if (paramsvalue != null) {
				sb.append(joinchar);
				sb.append(p);
				sb.append("=");
        sb.append(paramsvalue);//java.net.URLEncoder.encode(, "utf-8")
				joinchar = "&";
			}
		}

		URL u = new URL(url);
		HttpURLConnection con = (HttpURLConnection) u.openConnection();
		con.setRequestMethod("POST");
		con.setDoOutput(true);
		con.setDoInput(true);
		con.setUseCaches(false);
		con.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
		OutputStreamWriter osw = new OutputStreamWriter(con.getOutputStream(),"UTF-8");
		osw.write(sb.toString());
		osw.flush();
		osw.close();

		 out.clear();
		 out = pageContext.pushBody();
		OutputStreamWriter outStream = null;

		 outStream = new OutputStreamWriter(response.getOutputStream(),"utf-8");
		InputStreamReader urlStream = new InputStreamReader(con.getInputStream(), "utf-8");
		BufferedReader in = new BufferedReader(urlStream);
		int bufferlen = 1;
		char[] buffer = new char[bufferlen];
		int readBytes = 0;


		while (true) {
			readBytes = in.read(buffer, 0, bufferlen);
			if (readBytes < 0) {
				break;
			}
			outStream.write(buffer,0,readBytes);
			//for adhoc每秒的心跳
			if(bufferlen==1&&buffer[0]!=' '&&buffer[0]!='\n'&&buffer[0]!='\r')
			{
				bufferlen=1024;
				buffer = new char[bufferlen];
			}
else{
				outStream.flush();
			}
		}
		in.close();
		urlStream.close();
		outStream.flush();
		outStream.close();
		con.disconnect();
	
%>



