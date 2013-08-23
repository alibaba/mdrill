<%@ page contentType="application/vnd.ms-csv; charset=utf-8" %><%@ page import="java.io.*" %><%@ page import="javax.servlet.*" %><%@ page import="java.net.*" %><%@ page import="java.util.*" %><%@ page import="java.util.regex.*" %><%@ page import="java.text.*" %><%@ page import="com.alimama.web.*" %><%
	response.setHeader("Access-Control-Allow-Credentials", "true");
	response.setHeader("Access-Control-Allow-Origin","http://localhost,http://adhoc.etao.com:9999");
	request.setCharacterEncoding("utf-8");
	
	
	String uuid = request.getParameter("uuid");
	String did=TableJoin.getDownloadId(uuid);
	response.setHeader("Content-disposition","attachment; filename="+new String( new String(did).getBytes("utf-8"), "ISO8859-1" ) +"");

	
	out.clear();
  out = pageContext.pushBody();
	OutputStreamWriter outStream = new OutputStreamWriter(response.getOutputStream(),"gbk"); 
	TableJoin.readJoinResult(uuid,outStream);
	outStream.flush();
  outStream.close();
%>
