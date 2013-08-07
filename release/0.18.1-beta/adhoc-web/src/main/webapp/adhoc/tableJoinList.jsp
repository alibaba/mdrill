<%@ page contentType="text/html; charset=utf-8" %><%@ page import="java.io.*" %><%@ page import="javax.servlet.*" %><%@ page import="java.net.*" %><%@ page import="java.util.*" %><%@ page import="java.util.regex.*" %><%@ page import="java.text.*" %><%@ page import="com.alimama.web.*" %><%
		String username = request.getParameter("userName");
		String start = request.getParameter("start");
		String rows = request.getParameter("rows");
		String type = request.getParameter("type");
		String callback = request.getParameter("callback");
String resultstr=TableJoin.getUserTables(username,Integer.parseInt(start),Integer.parseInt(rows),Integer.parseInt(type),callback);
%><%=resultstr%>
