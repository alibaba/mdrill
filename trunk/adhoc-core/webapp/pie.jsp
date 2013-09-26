<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.io.*" %>
<%@ page import="java.net.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.util.regex.*" %>
<%@ page import="java.text.*" %>
<%@ page import="com.alimama.mdrill.ui.service.*" %>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	<title>pie</title>
	<style type="text/css">

	.demo-container {
		position: relative;
		height: 400px;
	}



	#menu {
		position: absolute;
		top: 20px;
		left: 625px;
		bottom: 20px;
		right: 20px;
		width: 200px;
	}

	#menu button {
		display: inline-block;
		width: 200px;
		padding: 3px 0 2px 0;
		margin-bottom: 4px;
		background: #eee;
		border: 1px solid #999;
		border-radius: 2px;
		font-size: 16px;
		-o-box-shadow: 0 1px 2px rgba(0,0,0,0.15);
		-ms-box-shadow: 0 1px 2px rgba(0,0,0,0.15);
		-moz-box-shadow: 0 1px 2px rgba(0,0,0,0.15);
		-webkit-box-shadow: 0 1px 2px rgba(0,0,0,0.15);
		box-shadow: 0 1px 2px rgba(0,0,0,0.15);
		cursor: pointer;
	}


	#code {
		display: block;
		width: 870px;
		padding: 15px;
		margin: 10px auto;
		border: 1px dashed #999;
		background-color: #f8f8f8;
		font-size: 16px;
		line-height: 20px;
		color: #666;
	}

	ul {
		font-size: 10pt;
	}

	ul li {
		margin-bottom: 0.5em;
	}

	ul.options li {
		list-style: none;
		margin-bottom: 1em;
	}

	ul li i {
		color: #999;
	}

	</style>
	<!--[if lte IE 8]><script language="javascript" type="text/javascript" src="../../excanvas.min.js"></script><![endif]-->
	<script language="javascript" type="text/javascript" src="./js/jquery.js"></script>
	<script language="javascript" type="text/javascript" src="./js/jquery.flot.js"></script>
	<script language="javascript" type="text/javascript" src="./js/jquery.flot.pie.js"></script>
		<script language="javascript" type="text/javascript" src="./js/json2.js"></script>

	
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

			
function labelFormatter(label, series) {
		return "<div style='font-size:8pt; text-align:center; padding:2px; color:white;' title="+JSON.stringify(series.data[0][1])+">" + label + "<br/>" + Math.round(series.percent) + "%"+"</div>";
	}

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
	requestparams.rows=10;
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
									var placeholder = $("#placeholder_"+pos);
									placeholder.show();
									placeholder.unbind();
								$.plot('#placeholder_'+pos, data.data, {
								    series: {
								        pie: {
								            show: true,
								            radius: 1,
								            label: {
								                show: true,
								                radius: 3/4,
								                formatter: labelFormatter,
								                background: {
								                    opacity: 0.5
								                }
								            }
								        }
								    },
								    legend: {
								        show: false
								    },
								    grid: {
								        hoverable: true,
								        clickable: true
								    }
								});
							
							
										placeholder.bind("plothover", function(event, pos, obj) {
							
											if (!obj) {
												return;
											}
							
											var percent = parseFloat(obj.series.percent).toFixed(2);
											$("#hover").html("<span style='font-weight:bold; color:" + obj.series.color + "'>" + obj.series.label + " (" + percent + "%)</span>");
										});
							
										placeholder.bind("plotclick", function(event, pos2, obj) {
							
											if (!obj) {
												return;
											}
							
											percent = parseFloat(obj.series.percent).toFixed(2);
											labelfilter["pos_"+pos]=obj.series.label;
											
											if(data[""+obj.series.label])
											{
												labelfilter_other["pos_"+pos]=data[""+obj.series.label];
											}else{
												labelfilter_other["pos_"+pos]=null;
											}
											
										  $("#placeholder_col_"+pos).text(params.fieldToName.namelist[pos]+":"+obj.series.label+":"+percent+"%"+":"+JSON.stringify(obj.series.data[0][1])+"");

											if((pos+1)<len)
											{
												showIndex(pos+1,len);
											}
										});
		
		}, "json");
	
	
	
	
};


showIndex(0,<%=groupBy.size()%>,null);


</script>