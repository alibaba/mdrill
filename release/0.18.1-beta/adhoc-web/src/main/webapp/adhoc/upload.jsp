<%@ page contentType="text/html; charset=utf-8" %><%@ page import="java.io.*" %><%@ page import="javax.servlet.*" %><%@ page import="java.net.*" %><%@ page import="java.util.*" %><%@ page import="java.util.regex.*" %><%@ page import="java.text.*" %><%@ page import="com.alimama.web.*" %><%
	
	out.clear();
  out = pageContext.pushBody();
	OutputStreamWriter outStream = new OutputStreamWriter(response.getOutputStream(),"utf-8"); 
	TableJoin.addTxt(request,response,outStream);
	outStream.flush();
  outStream.close();
%>
