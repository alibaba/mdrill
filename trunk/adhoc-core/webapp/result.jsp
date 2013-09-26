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
	String startStr = request.getParameter("start");
	String rowsStr = request.getParameter("rows");
	String queryStr = request.getParameter("q");
	String dist = request.getParameter("dist");
	String fl = request.getParameter("fl");
	String groupby = request.getParameter("groupby");
	String sort = request.getParameter("sort");
	String order = request.getParameter("order");
	String leftjoin = request.getParameter("leftjoin");
	
	MdrillService.result(projectName, callback, startStr, rowsStr, queryStr, dist, fl, groupby, sort, order,leftjoin,out);
%>
