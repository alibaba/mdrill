<%@ page contentType="text/html; charset=GB2312" %>
<%@ page import="java.util.*" %>
<%@ page import="com.alimama.web.*" %>
<html>
<head>
    <title>tablelist</title>
</head>

<body>
	<h1>海狗数据表列表</h1>
<hr>
<table border=1>
	<tr><td>数据表名</td><td>监控</td><td>表schema</td></tr>
<%
		String[] list=TableList.getTablelist();
		for(String tbl:list)
		{
				%>
					<tr><td><%=tbl%></td><td><a href="./tableshards.jsp?tablename=<%=tbl%>">查看</a></td><td><a href="./tableSchema.jsp?tablename=<%=tbl%>">查看</a></td></tr>
					
					<%
		}
%>

</table>
</body>
</html>
