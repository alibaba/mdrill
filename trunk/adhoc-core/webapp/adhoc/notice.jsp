<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.io.*" %>
<%@ page import="java.net.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.util.regex.*" %>
<%@ page import="java.text.*" %>
<%@ page import="java.util.Map.*" %>
<%@ page import="com.alimama.mdrill.ui.service.*" %>


<%
	String projectName = request.getParameter("project");
	String callback = request.getParameter("callback");
	String startstr = request.getParameter("start");
		String rowsstr = request.getParameter("rows");

	String resultstr=MdrillService.notice(projectName,callback, startstr, rowsstr);
%><%=resultstr%>