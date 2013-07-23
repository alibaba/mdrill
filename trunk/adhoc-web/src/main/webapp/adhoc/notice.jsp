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
	String groupby = request.getParameter("groupby");
	String resultstr=MdrillService.notice(projectName, callback, groupby);
%><%=resultstr%>