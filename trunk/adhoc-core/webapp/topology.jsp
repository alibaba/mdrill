<%@ page contentType="text/html; charset=GB2312" %>
<%@ page import="java.util.*" %>
<%@ page import="com.alimama.web.*" %>
<html>
<head>
    <title>tablelist</title>
</head>

<body>
	active_storms<br>
<%
		String[] list=Topology.active_storms();
		for(String tbl:list)
		{
				%>
				
				<a href="./topologyStatus.jsp?stormName=<%=tbl%>"><%=tbl%></a><br>
					
					<%
		}
		%>
</body>
</html>
