<!-- 把adhoc-metric和query-analyser的jsp页面合并到一起  -->
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page info="ad浩克关键指标报表和访问情况" %>
<%@ page import="java.util.*" %>
<%@ page import="java.text.*" %>
<%@ page import="com.etao.adhoc.metric.*" %>
<%@ page import="com.etao.adhoc.analyse.vo.*" %>
<%@ page import="com.etao.adhoc.analyse.dao.*" %>
<html>
    <head>
        <title>ad浩克关键指标报表和访问情况</title>
        <style>
			body {font-size:12px}
			td {text-align:center}
			h1 {font-size:26px;}
			h4 {font-size:16px;}
			em {color:#999; margin:0 10px; font-size:11px; display:block}
  		</style>
    </head>
    <body>
    
    <%! 
		MysqlService mysqlService = new MysqlService();
		boolean shownTableTitles = false;
	%>
	<h1>最近10天模块访问情况(已排除开发)</h1>
    <% 
    	List<String> days = mysqlService.getRecentDays("module_info","queryday",10);
    	shownTableTitles = false;
    	for(String day : days) {	
    		boolean thedateHasData = false;
    		ModuleInfo[] moduleInfos = mysqlService.getModuleInfos(day);

    		for(ModuleInfo moduleInfo : moduleInfos ){
    			if(moduleInfo != null) {
    				if(! shownTableTitles) {
    					out.println("<table bordercolor=\"#000000\">");
    					out.println("<th bgcolor=\"B4E3ED\" bordercolor=\"#000000\">日期</th>"); 
    					out.println("<th bgcolor=\"B4E3ED\" bordercolor=\"#000000\">数据集</th>");
    					out.println("<th bgcolor=\"B4E3ED\" bordercolor=\"#000000\">有效PV</th>");
    			    out.println("<th bgcolor=\"B4E3ED\" bordercolor=\"#000000\">有效UV</th>"); 

    					out.println("<th bgcolor=\"B4E3ED\" bordercolor=\"#000000\">访问列表</th>"); 
    				} 
    				thedateHasData = true;  
    				out.println("<tr>");
					out.println("<td bordercolor=\"#000000\">" + moduleInfo.getQueryDay() + "</td>");
					out.println("<td bordercolor=\"#000000\">" + moduleInfo.getModuleName() + "</td>");
					out.println("<td bordercolor=\"#000000\">" + moduleInfo.getQueryCnt() + "</td>");
					out.println("<td bordercolor=\"#000000\">" + moduleInfo.getUv() + "</td>");
					out.println("<td bordercolor=\"#000000\" style=\"text-align:left\">" + moduleInfo.getNicklist() + "</td>");
					
					out.println("</tr>");
					shownTableTitles = true;
				}
			}
			if(thedateHasData){
				out.println("<tr><td bordercolor=\"#000000\" colspan=\"4\">&nbsp</td></tr>");
			}
		}
		if(shownTableTitles){
			out.println("</table>");
		}			
    %>
   
	<h2>最近5天用户访问情况</h2>
	<%
		days = mysqlService.getRecentDays("day_user_pv","queryday",5);
		shownTableTitles = false;
		for(String day : days) {	
    		boolean thedateHasData = false;
    		List<DayUserPv> dayUserPvList = mysqlService.getDayTopUsers(day,100);
    		if(dayUserPvList != null) {
				for(DayUserPv dayUserPv :dayUserPvList) {
					if(! shownTableTitles) {
						out.println("<table bordercolor=\"#000000\">");
						out.println("<th bgcolor=\"B4E3ED\" bordercolor=\"#000000\">日期</th>");
						out.println("<th bgcolor=\"B4E3ED\" bordercolor=\"#000000\">用户名</th>");
						out.println("<th bgcolor=\"B4E3ED\" bordercolor=\"#000000\">有效PV</th>");
						out.println("<th bgcolor=\"B4E3ED\" bordercolor=\"#000000\">部门</th>");
					} 
					out.println("<tr>");
					out.println("<td bordercolor=\"#000000\">" + dayUserPv.getQueryDay() + "</td>");
					out.println("<td bordercolor=\"#000000\">" + dayUserPv.getNick() + "</td>");
					out.println("<td bordercolor=\"#000000\">" + dayUserPv.getQueryCnt() + "</td>");
					out.println("<td bordercolor=\"#000000\" style=\"text-align:left\">" + dayUserPv.getDepartment() + "</td>");

					
					out.println("</tr>");
					shownTableTitles = true;
					thedateHasData = true;
				}
			}
			if(thedateHasData) {
				out.println("<tr><td bordercolor=\"#000000\" colspan=\"2\">&nbsp</td></tr>");
			}
		}
		if(shownTableTitles){
			out.println("</table>");
		}	
    %>
    
     <br />
    <h1>访问量TOP100用户</h1>
   
    <%
    	List<TotalUserPv> totalUserPvList =  mysqlService.getTotalTopUsers(100);
    	shownTableTitles = false;
    	if(totalUserPvList != null) {
    		if( ! shownTableTitles) {
    			out.println("<table bordercolor=\"#000000\">");
    			out.println("<th bgcolor=\"B4E3ED\" bordercolor=\"#000000\">用户</th>");
				out.println("<th bgcolor=\"B4E3ED\" bordercolor=\"#000000\">有效PV</th>");
				out.println("<th bgcolor=\"B4E3ED\" bordercolor=\"#000000\">部门</th>");
			}
			for(TotalUserPv totalUserPv : totalUserPvList ) {
				out.println("<tr>");
				out.println("<td bordercolor=\"#000000\">" + totalUserPv.getNick() + "</td>");
				out.println("<td bordercolor=\"#000000\">" + totalUserPv.getQueryCnt() + "</td>");
				out.println("<td bordercolor=\"#000000\" style=\"text-align:left\">" + totalUserPv.getDepartment() + "</td>");
				out.println("</tr>");
				shownTableTitles = true;
			}
			if(shownTableTitles){
				out.println("</table>");
			}
		}		
	%>
    
    <%
    	mysqlService.close();
    %>
    <p>字段说明</p>
    <lable>有效PV:用户点击一次“开始查询”则产生一次有效PV</lable>
    <lable>UV:点击“开始查询”的用户数（去重）</lable>
    </body>
</html>
