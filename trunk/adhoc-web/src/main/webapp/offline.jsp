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
	String fl = request.getParameter("fl");
	String groupby = request.getParameter("groupby");
	String mailto= request.getParameter("mailto");
	String username= request.getParameter("username");
	String jobname= request.getParameter("jobname");
	String jobparam= request.getParameter("jobparam");
	
	
	String leftjoin= request.getParameter("leftjoin");
	String fq2= request.getParameter("fq2");
	String limit2= request.getParameter("limit2");
	String orderby2= request.getParameter("orderby2");
	String desc2= request.getParameter("desc2");
		String memo= request.getParameter("memo");

	String resultstr=AdhocOfflineService.offline(projectName, callback, queryStr, fl, groupby,mailto,username,jobname,jobparam,leftjoin,fq2,limit2,orderby2,desc2,memo);
%><%=resultstr%>
