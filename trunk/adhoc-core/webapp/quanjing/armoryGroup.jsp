<%@ page contentType="text/javascript; charset=utf-8" %>
<%@ page import="java.lang.*" %>
<%@ page import="java.io.*" %>
<%@ page import="java.net.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.util.regex.*" %>
<%@ page import="java.text.*" %>
<%@ page import="java.util.Map.*" %>
<%@ page import="javax.servlet.*" %>
<%@ page import="java.text.*" %>
<%@ page import="com.alimama.quanjingmonitor.parser.ArmoryGroup" %>
<%
	String g = request.getParameter("g");
	String info=ArmoryGroup.getJson(g);
%><%=info%>



