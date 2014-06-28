<%@ page contentType="text/html; charset=utf-8" %><%@ page import="java.io.*" %><%@ page import="javax.servlet.*" %><%@ page import="java.net.*" %><%@ page import="java.util.*" %><%@ page import="java.util.regex.*" %><%@ page import="java.text.*" %><%@ page import="com.alimama.mdrill.ui.service.*" %><%
	response.setHeader("Access-Control-Allow-Credentials", "true");
	response.setHeader("Access-Control-Allow-Origin","http://localhost,http://adhoc.etao.com:9999");
	request.setCharacterEncoding("utf-8");
	%>

	<%	
	String uuid = request.getParameter("uuid");
		String varstr = request.getParameter("callback");


	out.clear();
  out = pageContext.pushBody();
	OutputStreamWriter outStream = new OutputStreamWriter(response.getOutputStream(),"utf-8"); 
	AdhocOfflineService.readAbtestInfo(varstr,uuid,outStream);
	outStream.flush();
  outStream.close();

%>
