<%@ page contentType="text/html; charset=GB2312" %>
<%@ page import="java.util.*" %>
<%@ page import="com.alimama.web.*" %>
<html>
<head>
    <title>supervisorlist</title>
</head>

<body>
		<h1>»úÆ÷ÁÐ±í</h1>
<hr>
<%
		String[] list=SupervisorList.list();
		for(String tbl:list)
		{
				%>
				
				<%=tbl%><hr>
					
					<%
		}
%>
</body>
</html>
