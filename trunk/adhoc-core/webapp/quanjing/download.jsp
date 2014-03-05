<%@ page contentType="application/vnd.ms-csv; charset=gbk" %><%@ page import="java.io.*" %><%@ page import="javax.servlet.*" %><%@ page import="java.net.*" %><%@ page import="java.util.*" %><%@ page import="java.util.regex.*" %><%@ page import="java.text.*" %><%@ page import="com.alimama.web.*" %><%
	response.setHeader("Access-Control-Allow-Credentials", "true");
	response.setHeader("Access-Control-Allow-Origin","http://localhost,http://adhoc.etao.com:9999");
	request.setCharacterEncoding("utf-8");
	%>
	
	<%	

	String ddddd = request.getParameter("data");

		response.setHeader("Content-disposition","attachment; filename=134.csv");



    	
	
	out.clear();
  out = pageContext.pushBody();
	OutputStreamWriter outStream = new OutputStreamWriter(response.getOutputStream(),"gbk"); 
	outStream.write(ddddd);
	outStream.flush();
  outStream.close();
  
  
%>
