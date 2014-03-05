<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
 <head>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.io.*" %>
<%@ page import="java.net.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.util.regex.*" %>
<%@ page import="java.text.*" %>
<%@ page import="com.alimama.mdrill.ui.service.*" %>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>tanx</title>
   
  <script>
quanjing_logcheck=0;
</script>
 <script language="javascript" type="text/javascript" src="http://adhoc.etao.com:9999/quanjing_check.jsp"></script>

<script>
	if(quanjing_logcheck!=1)
	{
		location.href='http://adhoc.etao.com:9999/quanjing_tanx.jsp';
	}
	</script>
	
     <script language="javascript" type="text/javascript" src="../js/jquery.js"></script>
    <script language="javascript" type="text/javascript" src="../js/jquery.flot.js"></script>
    <script language="javascript" type="text/javascript" src="../js/jquery.flot.crosshair.js"></script>
    <script language="javascript" type="text/javascript" src="../js/My97DatePicker/WdatePicker.js"></script>
    <script language="javascript" type="text/javascript" src="../js/jquery.flot.pie.js"></script>
    	
    <link rel="stylesheet" type="text/css" href="../css/jquery.jqChart.css" />
    <link rel="stylesheet" type="text/css" href="../css/jquery.jqRangeSlider.css" />
    <link rel="stylesheet" type="text/css" href="../css/jquery-ui-1.8.20.css" />
    
    <link  type="text/css" href="../css/a.tbcdn.cn.css" />
    <link  type="text/css" href="../css/adhoc.css" />

    <script src="../js/jquery.mousewheel.js" type="text/javascript"></script>
    <script src="../js/jquery.jqChart.min.js" type="text/javascript"></script>
    <script src="../js/jquery.jqRangeSlider.min.js" type="text/javascript"></script>

    <script language="javascript" type="text/javascript" src="../js/json2.js"></script>
    <!--[if lte IE 8]><script language="javascript" type="text/javascript" src="../js/excanvas.min.js"></script><![endif]-->
     
    <script  type="text/javascript" language="JavaScript" src="./tanx_2.js" charset="utf-8"></script>


 
<style type="text/css">

	.demo-container {
		position: relative;
		height: 400px;
	}
body, button, input, select, textarea {
    font: 12px/1.5 tahoma,"microsoft yahei","微软雅黑";
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




html,body {
margin: 10; /* Remove body margin/padding */
padding: 0;
font-size: 75%;
}
 
.ui-tabs-nav li {
position: relative;
}
 
.ui-tabs-selected a span {
padding-right: 10px;
}
 
.ui-tabs-close {
display: none;
position: absolute;
top: 3px;
right: 0px;
z-index: 800;
width: 16px;
height: 14px;
font-size: 10px;
font-style: normal;
cursor: pointer;
}
 
.ui-tabs-selected .ui-tabs-close {
display: block;
}
 
.ui-layout-west .ui-jqgrid tr.jqgrow td {
border-bottom: 0px none;
}
 
.ui-datepicker {
z-index: 1200;
}

	</style>


 </head>
 <body>
 	    <div id="loading" style="position:fixed !important;position:absolute;top:0;left:0;height:100%; width:100%; z-index:999; background:#000 url(http://interjc.googlecode.com/svn/trunk/waterfall/img/load.gif) no-repeat center center; opacity:0.6; filter:alpha(opacity=60);font-size:14px;line-height:20px;display:none" >  
    <p id="loading-one" style="color:#fff;position:absolute; top:50%; left:50%; margin:20px 0 0 -50px; padding:3px 10px;" onclick="javascript:turnoff('loading')">页面载入中..</p>  
</div>  

<div style="font-size:50px; border:double; width:210px; text-align:center; vertical-align:middle">全景监控</div>
 	<h1><a style="color:#551890" href="./3_8_realtime.jsp">3.8生活节</a>/TANX外投流量指标/<a style="color:#551890" href="./tanx_table.jsp">TANX外投流量数据排行</a></h1>
 	 	<h2><a style="color:#551890" href="./tanxpv.jsp">pv</a>/<a style="color:#551890" href="./tanxclick.jsp">点击</a>/<a style="color:#551890" href="./tanxrate.jsp">点击率</a>/pv累积/<a style="color:#551890" href="./tanxclicksum.jsp">点击累积</a>/<a style="color:#551890" href="./tanxratesum.jsp">累积点击率</a>/<a style="color:#551890" href="./tanxdiff.jsp">波动</a></h2>

 <table  border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td>日期：</td>
    <td><input type="text" name="thedateStart" id="thedateStart" onFocus="WdatePicker({dateFmt:'yyyyMMdd'})"  />
</td>
<td>
	<select name="matchmode" id="matchmode" onchange="tooglemode()" >
      <option value="pid"  selected="selected">按照PID</option>
      <option value="keyword">关键词匹配</option>
  </select>
</td>
<td>
	
	
<table id="pidtbl" border="0" cellspacing="0" cellpadding="0"> 
	<td>pid:</td>
    <td> 	 	<input type="text" name="thepid" id="thepid" value="mm_13087935_2078460_8374317"  />
</td>
    <td>产品类型：</td>
    <td> 	<select name="producttype" id="producttype" >
 		<option value="all">全部</option>
      <option value="D">直投</option>
      <option value="X">交换</option>
      <option value="S">系统抄底</option>
  </select></td>
</table>	
	<table id="searchtbl" style="display:none" border="0" cellspacing="0" cellpadding="0">
	<td>关键词:</td>
    <td> 	 	<input type="text" name="searchkeyword" id="searchkeyword" value="*新浪*"  />
</td>

</table>
	
</td>

  
      <td colspan="2" align="right"><span ><input type="button" onclick="timeseries()"  value="查看" /></span></td>

  </tr>
 
</table>

   	    <select id="showpidlist" style="display:none"  onchange="tooglepidlist()"></select>

   	    <p id="choices" style=""></p>

    <div id="placeholder" style="width:1200px;height:600px;">loading......</div>
    <p id="hoverdata" style="display:none">Mouse hovers at
    (<span id="x">0</span>, <span id="y">0</span>). <span id="clickdata"></span></p>
    <input id="enableTooltip" type="checkbox" checked="checked" style="display:none">

 </body>
</html>

<script>
	
var typeshow=[];



function makeDataSet()
{
				g_result['zeroarr']['ischoose']="1";
				g_result['pv_today_sum']['ischoose']="1";
				g_result['pvbefore1_sum']['ischoose']="1";
				g_result['pvbefore7_sum']['ischoose']="1";
				g_result['pv_avg_sum']['ischoose']="1";
		 datasets ={
		 	"zeroarr":g_result['zeroarr'],
		 	"pv_today_sum":g_result['pv_today_sum'],
		 	"pvbefore1_sum":g_result['pvbefore1_sum'],
		 	"pvbefore7_sum":g_result['pvbefore7_sum'],
		 	"pv_avg_sum":g_result['pv_avg_sum']
		 	};
	
}


//typeshow=[
// 		"pv_today","pvbefore1","pvbefore7","pv_avg"
// 		,"click_today","clickbefore1","clickbefore7","click_avg"
// 		pv_today_sum,pvbefore1_sum,pvbefore7_sum,pv_avg_sum
// 		
// 		click_today_sum,clickbefore1_sum,clickbefore7_sum,click_avg_sum
// 		today_clickrate,pvbefore1_clickrate,pvbefore7_clickrate，avg_clickrate，
// 		pv_diff_rate
// 		click_diff_rate
// 	];


function searchData(strtype,pidlist)
{
 	showload();
 	
 	typeshow=["pv_today","pvbefore1","pvbefore7","pv_avg"];
 	
	requestPV_AVG(strtype,pidlist,"pv_avg","七日平均PV","pv",0);
	requestPV(strtype,pidlist,0,"pv_today","当日PV","pv",0);
	requestPV(strtype,pidlist,1,"pvbefore1","昨日PV","pv",0);
	requestPV(strtype,pidlist,7,"pvbefore7","上周同日PV","pv",0);

}





var today=new Date();
jQuery("#thedateStart").val(parseDay(new Date(today.getTime())));
var choiceContainer = $("#choices");
var datasets={};
var mdrilldata={};
var g_result={};


g_isrequestpid=false;

	$("#matchmode").val("pid");
	tooglemode();
timeseries();












function parseDate(date)
{
    var month = date.getMonth() < 9 ? "0" + (date.getMonth() + 1) : (date.getMonth() + 1);   
    var day = date.getDate() <= 9 ? "0" + (date.getDate()) : (date.getDate());   
    var hour = date.getHours() <= 9 ? "0" + (date.getHours()) : (date.getHours());
    var miniute = date.getMinutes() <= 9 ? "0" + (date.getMinutes()) : (date.getMinutes());
        var secs = date.getSeconds() <= 9 ? "0" + (date.getSeconds()) : (date.getSeconds());

    var yyyymmdd= (date.getFullYear() + "" + month + "" + day+hour+miniute+secs);         
    return yyyymmdd;
}

function jsonpcall()
{
	
}


$(function(){
var logparams={};
logparams["date"]=parseDate(new Date());
logparams["bizdate"]=parseDay(new Date())+"-"+parseDay(new Date());
logparams["nick"]=log_nickname.replace(/\(.*\)/ig,"");
logparams["email"]=log_userid+"@taobao.com";
logparams["set"]="全景监控tanxpvsum";
logparams["dimvalue"]=location.href;
logparams["filter"]="";
logparams["r"]=new Date().getTime();
logparams["callback"]="jsonpcall";


$.ajax({
url:"http://adhoc.etao.com:9999/querylog.jsp",
data:logparams,
dataType:"jsonp",
jsonp:"jsonpcall",
success:jsonpcall});

});


	</script>	
</script>
