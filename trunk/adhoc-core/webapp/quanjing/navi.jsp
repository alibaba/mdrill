<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.io.*" %>
<%@ page import="java.net.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.util.regex.*" %>
<%@ page import="java.text.*" %>
<%

	String pagename = request.getParameter("currpage");

	String[] namelist={
		"p4p监控"
		,"流量分析"
		,"TANX外投流量指标"
		,"TANX外投流量数据排行"
	};
	
		String[] hrefList={
		"./goldeye_realtime.jsp"
		,"./siteoverlap.jsp"
		,"./tanxpv.jsp"
		,"./tanx_table.jsp"
	};
	
	String hrefprint="";
	String joinchar="";
	
	for(int i=0;i<namelist.length&&i<hrefList.length;i++)
	{
		String namestr=namelist[i];
		String hrefstr=hrefList[i];
		
		if(hrefstr.indexOf(pagename)>=0)
		{
				hrefprint+=joinchar+namestr;
		}else{
				hrefprint+=joinchar+"<a style='color:#551890' href='"+hrefstr+"'>"+namestr+"</a>";
		}
		joinchar="/";
	
	}


%>
<div style="font-size:50px; border:double; width:210px; text-align:center; vertical-align:middle">全景监控</div>

<h1><%=hrefprint%></h1> 