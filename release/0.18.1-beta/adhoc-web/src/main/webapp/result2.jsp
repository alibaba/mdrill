<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.io.*" %>
<%@ page import="java.net.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.util.regex.*" %>
<%@ page import="java.text.*" %>
<%@ page import="com.alimama.mdrill.ui.service.*" %>
<%

 String projectName = request.getParameter("project");
		String callback = request.getParameter("callback");
		String groupby = request.getParameter("groupby");
		String startStr = request.getParameter("start");
		String rowsStr = request.getParameter("rows");
		String queryStr = request.getParameter("q");
	
	String resultstr=MdrillService.fieldValueList(projectName, callback, groupby, startStr, rowsStr, queryStr);
%><%=resultstr%>
