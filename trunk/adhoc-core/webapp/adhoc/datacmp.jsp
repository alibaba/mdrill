<!-- 把adhoc-metric和query-analyser的jsp页面合并到一起  -->
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page info="ad浩克关键指标报表和访问情况" %>
<%@ page import="java.util.*" %>
<%@ page import="java.util.Map.Entry" %>
<%@ page import="java.text.*" %>
<%@ page import="com.etao.adhoc.metric.*" %>
<%@ page import="com.etao.adhoc.analyse.vo.*" %>
<%@ page import="com.etao.adhoc.analyse.dao.*" %>
<html>
    <head>
        <title>ad浩克关键指标报表和访问情况</title>
        <script language="JavaScript" src="js/date.js">
        </script>
        <style>
			body {font-size:12px}
			td {text-align:center}
			h1 {font-size:26px;}
			h4 {font-size:16px;}
			em {color:#999; margin:0 10px; font-size:11px; display:block}
  		</style>
    </head>
    <body>
    <h1>最近5天ad浩克关键指标</h1>
    <%! 
    	public String getSourceName(long type) {
			if(type == 0)
				return "HIVE";
			else
				return "adhoc";
		}
		public String printTableTitle() {
			StringBuilder sb = new StringBuilder();
			sb.append("<table bordercolor=\"#000000\">");
    		sb.append("<tr>");
    		sb.append("<th bgcolor=\"B4E3ED\" bordercolor=\"#000000\">日期</th>"); 
            sb.append("<th bgcolor=\"B4E3ED\" bordercolor=\"#000000\">数据集</th>");     
            sb.append("<th bgcolor=\"B4E3ED\" bordercolor=\"#000000\">来源</th>");
            sb.append("<th bgcolor=\"B4E3ED\" bordercolor=\"#000000\">记录数</th>");     
            sb.append("<th bgcolor=\"B4E3ED\" bordercolor=\"#000000\">展现量</th>"); 
            sb.append("<th bgcolor=\"B4E3ED\" bordercolor=\"#000000\">财务点击</th>");
            sb.append("<th bgcolor=\"B4E3ED\" bordercolor=\"#000000\">财务消耗</th>");
            sb.append("<th bgcolor=\"B4E3ED\" bordercolor=\"#000000\">直通车成交笔数</th>");
            sb.append("<th bgcolor=\"B4E3ED\" bordercolor=\"#000000\">直通车成交金额</th>");
            sb.append("<th bgcolor=\"B4E3ED\" bordercolor=\"#000000\">直通车直接成交笔数</th>");
            sb.append("<th bgcolor=\"B4E3ED\" bordercolor=\"#000000\">直通车直接成交金额</th>");
            sb.append("<th bgcolor=\"B4E3ED\" bordercolor=\"#000000\">直通车间接成交笔数</th>");
            sb.append("<th bgcolor=\"B4E3ED\" bordercolor=\"#000000\">直通车间接成交金额</th>");     
            sb.append("</tr>");
            return sb.toString();
       }
		public String printMetric(Metric metric) {
			StringBuilder sb = new StringBuilder();
			sb.append("<tr>");
			sb.append("<td bordercolor=\"#000000\">" + metric.getThedate() + "</td>");
			sb.append("<td bordercolor=\"#000000\">" + metric.getTablename() + "</td>");
			sb.append("<td bordercolor=\"#000000\">" + getSourceName(metric.getType()) + "</td>");
			sb.append("<td bordercolor=\"#000000\">" + metric.getLineCnt() + "</td>");
			sb.append("<td bordercolor=\"#000000\">" + metric.getImpression() + "</td>");
			sb.append("<td bordercolor=\"#000000\">" + metric.getFinClick() + "</td>");
			sb.append("<td bordercolor=\"#000000\">" + String.format("%.2f",metric.getFinPrice()) + "</td>");
			sb.append("<td bordercolor=\"#000000\">" + (metric.getAlipayDirectNum() + metric.getAlipayIndirectNum()) + "</td>");
			sb.append("<td bordercolor=\"#000000\">" + String.format("%.2f",metric.getAlipayDirectAmt() + metric.getAlipayIndirectAmt()) + "</td>");
			sb.append("<td bordercolor=\"#000000\">" + metric.getAlipayDirectNum() + "</td>");
			sb.append("<td bordercolor=\"#000000\">" + String.format("%.2f",metric.getAlipayDirectAmt()) + "</td>");
			sb.append("<td bordercolor=\"#000000\">" + metric.getAlipayIndirectNum() + "</td>");
			sb.append("<td bordercolor=\"#000000\">" + String.format("%.2f",metric.getAlipayIndirectAmt()) + "</td>");
			sb.append("</tr>");
			return sb.toString();
		}
		
		public String printMetricsDifference(Metric metric1, Metric metric2) {
			StringBuilder sb = new StringBuilder();
			sb.append("<tr>");
			sb.append("<td bordercolor=\"#000000\" colspan=\"3\">差值</td>");
			sb.append("<td bordercolor=\"#000000\">" + (metric1.getLineCnt() - metric2.getLineCnt()) + "</td>");
			sb.append("<td bordercolor=\"#000000\">" + (metric1.getImpression() - metric2.getImpression()) + "</td>");
			sb.append("<td bordercolor=\"#000000\">" + (metric1.getFinClick() - metric2.getFinClick()) + "</td>");
			sb.append("<td bordercolor=\"#000000\">" + String.format("%.2f",metric1.getFinPrice() - metric2.getFinPrice()) + "</td>");
			sb.append("<td bordercolor=\"#000000\">" + (metric1.getAlipayDirectNum() - metric2.getAlipayDirectNum()) + "</td>");
			sb.append("<td bordercolor=\"#000000\">" + String.format("%.2f",metric1.getAlipayDirectAmt() - metric2.getAlipayDirectAmt()) + "</td>");
			sb.append("<td bordercolor=\"#000000\">" + (metric1.getAlipayIndirectNum() - metric2.getAlipayIndirectNum()) + "</td>");
			sb.append("<td bordercolor=\"#000000\">" + String.format("%.2f",metric1.getAlipayIndirectAmt() - metric2.getAlipayIndirectAmt()) + "</td>");
			sb.append("</tr>");
			return sb.toString();
		}

	%>
    <% 
	MetricService metricService = new MetricService();
	List<String> days = metricService.getRecentDays("adhoc_metric", "thedate", 10);
    	for(String thedate : days) {
    		boolean shownTitles = false;
    		if(! shownTitles){
						out.println(thedate + " 关键指标：");
						out.println(printTableTitle());
						shownTitles = true;
					}
					
					HashMap<String,ArrayList<Metric>> mmmm= metricService.getMetric(thedate) ;
					for(Entry<String,ArrayList<Metric>> e:mmmm.entrySet())
			{
				ArrayList<Metric> list=e.getValue();
				for(Metric metri : list){
						out.println(printMetric(metri));
				}
				
				if(list.size()>1)
				{
						out.println(printMetricsDifference(list.get(0),list.get(1)));
				}
				
				
			}
    		
			if(shownTitles) {
				out.println("</table>");
			}
		}	
		
		metricService.close();			
    %>
        
    </body>
</html>
