<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="java.io.*" %>
<%@ page import="java.net.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.util.regex.*" %>
<%@ page import="java.text.*" %>
<%@ page import="com.alimama.web.*" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>sql</title>
</head>

<body>
<form action="./sql.jsp" method="get" >
<%
	String connstr1 = request.getParameter("connstr");
	String strsql1 = request.getParameter("sql");
	if(connstr1==null)
	{
		connstr1="jdbc:mdrill://127.0.0.1:9999";
	}
	if(strsql1==null)
	{
		strsql1="select thedate,user_id,count(*) from rpt_hitfake_auctionall_d where thedate >'20130625' and  thedate <'20130705' and (thedate ='20130704' or thedate ='20130705' or thedate<='20130702')   and ((custid='1104981405' and user_id='136018175') or user_id='932280506' or user_id like '%9999%') group by thedate,user_id limit 0,100";
	}
	
	%>
<table>
	<tr><td><input name="connstr" style="width:1000px"  value="<%=connstr1%>" /></td><td></td></tr>
<tr><td><textarea name="sql" cols="1024" style="width:1000px" rows="5"><%=strsql1%></textarea></td><td><input name="go" type="submit" /></td></tr>
</table>
<input name="go" type="submit" />
<hr>
<%
	String connstr = request.getParameter("connstr");
	String strsql = request.getParameter("sql");
	
	String resultstr="";
	if(strsql!=null&&strsql!=null)
	{
		resultstr=Sql.execute(strsql,connstr);
		}
%><%=resultstr%>
</form>
</body>
</html>
