<%@ page contentType="text/html; charset=GB2312" %>
<%@ page import="java.util.*" %>
<%@ page import="com.alimama.web.*" %>
<html>
<head>
    <title>tablelist</title>
</head>

<body>
	<h1>海狗数据表明细</h1>
<hr>
<%
	String tablename = request.getParameter("tablename");
		String[] list=TableList.getTableShards(tablename);
		for(String shard:list)
		{
				%><%=shard%><%
		}
%>

</body>
</html>
