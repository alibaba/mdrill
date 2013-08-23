<%@ page contentType="text/html; charset=utf-8" %><%@ page import="java.io.*" %><%@ page import="javax.servlet.*" %><%@ page import="java.net.*" %><%@ page import="java.util.*" %><%@ page import="java.util.regex.*" %><%@ page import="java.text.*" %><%@ page import="com.alimama.web.*" %><%
		String tableShowName = request.getParameter("tableShowName");
		String colsShowName = request.getParameter("colsShowName");
		String splitString = request.getParameter("splitString");
		String username = request.getParameter("username");
		String memo = request.getParameter("memo");
		String joins = request.getParameter("joins");
		String callback = request.getParameter("callback");
String resultstr=TableJoin.create(tableShowName,colsShowName,splitString,username,joins,callback,memo);
%><%=resultstr%>
