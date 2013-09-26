<%@ page contentType="text/html; charset=GB2312" %>
<%@ page import="java.util.*" %>
<%@ page import="com.alimama.web.*" %>
<html>
<head>
    <title>tablelist</title>
</head>

<body>
<%
	String stormName = request.getParameter("stormName");
		List<String> list=Topology.getStatus(stormName);
		for(String shard:list)
		{
				%><%=shard%><hr><%
		}
%>

</body>
</html>
