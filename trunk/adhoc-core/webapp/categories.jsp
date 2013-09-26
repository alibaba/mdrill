<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head><%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.io.*" %>
<%@ page import="java.net.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.util.regex.*" %>
<%@ page import="java.text.*" %>
<%@ page import="com.alimama.mdrill.ui.service.*" %>
    <title>
    Hyperlinks Example - HTML5 jQuery Chart Plugin by jqChart
</title>
    <link rel="stylesheet" type="text/css" href="./css/jquery.jqChart.css" />
    <link rel="stylesheet" type="text/css" href="./css/jquery.jqRangeSlider.css" />
    <link rel="stylesheet" type="text/css" href="./css/jquery-ui-1.8.20.css" />
    <script src="./js/jquery.js" type="text/javascript"></script>
    <script src="./js/jquery.mousewheel.js" type="text/javascript"></script>
    <script src="./js/jquery.jqChart.min.js" type="text/javascript"></script>
    <script src="./js/jquery.jqRangeSlider.min.js" type="text/javascript"></script>
    <!--[if IE]><script lang="javascript" type="text/javascript" src="../js/excanvas.js"></script><![endif]-->
    


</head>
<body>
    	<table  border="0" cellspacing="0" cellpadding="0"><tr> 
		<%
	response.setHeader("Access-Control-Allow-Credentials", "true");
	response.setHeader("Access-Control-Allow-Origin","http://localhost,http://adhoc.etao.com:9999");
	String projectName = request.getParameter("project");
	String callback = request.getParameter("callback");
	String startStr = request.getParameter("start");
	String rowsStr = request.getParameter("rows");
	String queryStr = request.getParameter("q");
	String dist = request.getParameter("dist");
	String fl = request.getParameter("fl");
	String groupby = request.getParameter("groupby");
	String sort = request.getParameter("sort");
	String order = request.getParameter("order");
	String leftjoin = request.getParameter("leftjoin");
	String dimvalue = request.getParameter("dimvalue");
	ArrayList<String> groupBy=Pie.parseGroup(fl);
	
	for(int i=0;i<groupBy.size();i++)
	{
	
%>
		<td valign="middle">
			
			<table  border="0" cellspacing="0" cellpadding="0" id="placeholder_tbl_<%=i%>">
  <tr>
    <td colspan="2">    	<div id="placeholder_col_<%=i%>" > </div></td>
  </tr>
  <tr>
    <td>
    	<div id="placeholder_tip_<%=i%>" > </div>
    	</td>
    <td>				 <div id="placeholder_<%=i%>" style="width: 400px;height: 400px;">	</div>	</td>
</td>
  </tr>
</table>
			
<%
	}
%>
</tr>
</table>
</body>
</html>

<script type="text/javascript">
		
 var params=<%=Pie.parseStat(projectName,queryStr,fl,groupby,dimvalue)%>;



var labelfilter={};
var labelfilter_other={};

function showIndex(pos,len,fq)
{
	for(var i=(pos+1);i<len;i++)
	{
		$("#placeholder_"+i).hide();
			$("#placeholder_tip_"+i).hide();
			$("#placeholder_tbl_"+i).hide();
	}
	
	
	
	var requestparams={};
	requestparams.start=0;
	requestparams.rows=6;
	requestparams.project=params.project;
	requestparams.order=params.order;
	requestparams.sort=params.sort;
	requestparams.groupby=params.pie_groupby[pos];
	requestparams.fl=params.pie_groupby[pos]+","+params.pie_stat;
	
	var sfilter=JSON.parse(params.q);
	for(var i=0;i<pos;i++)
	{
		var tips=labelfilter["pos_"+i];
		if(tips&&i>=0&&params.pie_groupby[i])
		{
					var tips2=labelfilter_other["pos_"+i];
			if(tips2)
			{
					for(var j=0;j<tips2.length;j++)
					{
							sfilter.push(tips2[j]);
						}
			}else{
				var item={"key":params.pie_groupby[i],"operate":"1","value":tips}
				sfilter.push(item);
			}
		}
	}
	
	requestparams.q=JSON.stringify(sfilter);
	
	$.post("./pie_data.jsp",requestparams,

		function (data, textStatus){
			
								$("#placeholder_col_"+pos).text(params.fieldToName.namelist[pos]);

								$("#placeholder_tip_"+pos).show();
								var tips=labelfilter["pos_"+(pos-1)];
							
								$("#placeholder_tip_"+pos).show();
								$("#placeholder_tbl_"+pos).show();
								if(tips)
								{
									$("#placeholder_tip_"+pos).html("<strong>&gt;&gt;&gt;</strong>");
							}else{
																$("#placeholder_tip_"+pos).html("");

							}
							
							
							var newchardata=[];
							var newcharlabel=[];
							for(var ii=0;ii<data.data.length;ii++)
							{
								newchardata.push([data.data[ii].label,data.data[ii].data]);
								newcharlabel.push(data.data[ii].label);
							}
									var placeholder = $("#placeholder_"+pos);
									placeholder.show();
							//	

									$("#placeholder_"+pos).jqChart({
                title: params.fieldToName.namelist[pos],
                animation: { duration: 1 },
                series: [
                            {
                                title: '',
                                type: 'column',
                                data: newchardata,
                                cursor: 'pointer',
                                hyperlinks: newcharlabel
                            }
                        ]
            });
			placeholder.unbind("dataPointMouseDown");
            $("#placeholder_"+pos).bind('dataPointMouseDown', function (event, data2) {
                var label = data2.series.hyperlinks[data2.index];
                labelfilter["pos_"+pos]=label;
											
											if(data[""+label])
											{
												labelfilter_other["pos_"+pos]=data[""+label];
											}else{
												labelfilter_other["pos_"+pos]=null;
											}
											
										  $("#placeholder_col_"+pos).text(params.fieldToName.namelist[pos]+":"+label+"");

											if((pos+1)<len)
											{
												showIndex(pos+1,len);
											}
            });
		
		}, "json");
	
	
	
	
};


showIndex(0,<%=groupBy.size()%>,null);


</script>
