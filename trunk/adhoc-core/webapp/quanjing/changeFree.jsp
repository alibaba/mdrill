<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
 <head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>mointor</title>
   <%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.io.*" %>
<%@ page import="java.net.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.util.regex.*" %>
<%@ page import="java.text.*" %>
<link rel="stylesheet" type="text/css" href="http://changefree.alibaba-inc.com/v2_changefree/app/global/css/main.css?rnd=<%=java.lang.System.currentTimeMillis()%>"/>
<script type="text/javascript" src="http://changefree.alibaba-inc.com/v2_changefree/app/global/js/jquery.js?rnd=<%=java.lang.System.currentTimeMillis()%>"></script>
<script type="text/javascript" src="http://changefree.alibaba-inc.com/v2_changefree/app/global/js/jquery.dataTables.js?rnd=<%=java.lang.System.currentTimeMillis()%>"></script>


<script>
function submit_auto()
{
	
	   document.getElementById('autosubmit_form').submit();
}


</script>
	 </head>
 <body onload="submit_auto()">
<form target="_self" method="post" id="autosubmit_form" action="http://changefree.alibaba-inc.com/v2_changefree/app/index.php/get/changeList" >
<input type="hidden" name="radio_s" value="5" />
<input type="hidden" name="category" value="2" />
<input type="hidden" name="deptId" value="8" />
<input type="submit" id="autosubmit" name="autosubmit" value="如果没有跳转，请点击这里继续......" />
</form>

</body>

</html>


