<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.io.*" %>
<%@ page import="java.net.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.util.regex.*" %>
<%@ page import="java.text.*" %>
<%@ page import="com.alimama.mdrill.ui.service.*" %>
<%
	response.setHeader("Access-Control-Allow-Credentials", "true");
	response.setHeader("Access-Control-Allow-Origin","http://localhost,http://adhoc.etao.com:9999");
	String projectName = request.getParameter("project");
	String callback = request.getParameter("callback");
	
	String queryStr = request.getParameter("q");

	String colls_important = request.getParameter("colls_important");
	String number_important = request.getParameter("number_important");
	String colls = request.getParameter("colls");
	String numbers = request.getParameter("numbers");
	
	String mailto = request.getParameter("mailto");
	String username = request.getParameter("username");
	String jobname = request.getParameter("jobname");
	String params = request.getParameter("params");
	String memo = request.getParameter("memo");
		
	String strresult=AdhocOfflineService.kmeans(projectName, callback, queryStr, 1500, 2, "custid", colls_important, number_important, colls, numbers,mailto,username,jobname,params,memo);
%><%=strresult%>
