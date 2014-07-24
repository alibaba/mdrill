<%@ page contentType="application/vnd.ms-csv; charset=gbk" %><%@ page import="java.io.*" %><%@ page import="javax.servlet.*" %><%@ page import="java.net.*" %><%@ page import="java.util.*" %><%@ page import="java.util.regex.*" %><%@ page import="java.text.*" %><%@ page import="com.alimama.mdrill.ui.service.*" %><%
	response.setHeader("Access-Control-Allow-Credentials", "true");
	response.setHeader("Access-Control-Allow-Origin","http://localhost,http://adhoc.etao.com:9999");
	request.setCharacterEncoding("utf-8");
	%>

	<%	
	String uuid = request.getParameter("uuid");
		String isall = request.getParameter("isall");
String matchallstre="y";

	String did=AdhocOfflineService.readHiveResultID(uuid);

	
	response.setHeader("Content-disposition","attachment; filename="+new String( new String(did).getBytes("utf-8"), "ISO8859-1" ) +"");


	out.clear();
  out = pageContext.pushBody();
	OutputStreamWriter outStream = new OutputStreamWriter(response.getOutputStream(),"gbk"); 
	AdhocOfflineService.readAbtestResult(uuid,outStream,"",!matchallstre.equals(isall));
	outStream.flush();
  outStream.close();

%>
