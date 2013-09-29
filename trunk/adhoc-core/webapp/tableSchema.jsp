<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.io.*" %>
<%@ page import="java.net.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.util.regex.*" %>
<%@ page import="java.text.*" %>
<%@ page import="com.alimama.mdrill.ui.service.*" %>
<html>
<head>
    <title>tablelist</title>
</head>

<body>
	<h1>table schema</h1>
<hr>

<table border=1>
	<tr><td>字段名</td><td>字段类型</td></tr>
<%
		String tablename = request.getParameter("tablename");
		
		LinkedHashMap<String, String> fieldColumntypeMap=MdrillService.readFieldsFromSchemaXml(tablename);
		for(java.util.Map.Entry<String, String> e:fieldColumntypeMap.entrySet())
			{
				%><tr><td><%=e.getKey()%></td><td><%=e.getValue()%></td></tr><%
			}
%>
</table>
</body>
</html>
