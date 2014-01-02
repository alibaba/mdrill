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
	String json = request.getParameter("json");

	String result=MdrillService.insert(projectName,json,null);
%><%=result%>
