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
<title>3.8生活节</title>
<script>
	quanjing_logcheck=0;
</script>
<script language="javascript" type="text/javascript" src="http://adhoc.etao.com:9999/quanjing_check_416.jsp"></script>

<script>
	if(quanjing_logcheck!=1)
	{
		location.href='http://adhoc.etao.com:9999/quanjing_416.jsp';
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


<jsp:include    page="navi.jsp"    flush="true">   
             <jsp:param    name="currpage"    value="416_realtime"    />   
</jsp:include>
	<h2><a style="color:#551890" href="././416_realtime.jsp">分时数据</a>/<a style="color:#551890" href="././416_realtime_sum.jsp">累计数据</a>/活动期累计数据/<a style="color:#551890" href="././416_table.jsp">离线数据</a></h2>
 


<table  border="0" cellspacing="0" cellpadding="0"> 
<tr>
	<td>&nbsp;日期:</td>
<td><input type="text" name="thedateStart" id="thedateStart" onFocus="WdatePicker({dateFmt:'yyyyMMdd'})"  /></td>
	<td>&nbsp; 对比日期:</td>

<td><input type="text" name="thedateEnd" id="thedateEnd" onFocus="WdatePicker({dateFmt:'yyyyMMdd'})"  /></td>
	<td> &nbsp;流量分类:</td>

<td>
<select name="source" id="source"  onchange="tooglesource()"  >
      <option value="pc"  selected="selected">pc</option>
      <!--option value="wireless">wireless</option-->
  </select>
 	
  </td>
  
 <td>
 	 <table id="wireless_sub_tbl" style="display:none" border="0" cellspacing="0" cellpadding="0"> 
				    <tr>
				    		<td> &nbsp;来源:</td>

				    	<td> 	   
				    	<select name="wireless_sub" id="wireless_sub"  >
				    		<option value="all"  selected="selected">全部</option>
				      	<option value="pc">pc</option>
				      	<option value="wireless">wireless</option>
				  		</select>
				</td></tr>
				</table>	
				
		</td>


<td style="display:none">			  
				   <table id="channel_tbl" style="display:none" border="0" cellspacing="0" cellpadding="0"> 
				    <tr>
				    		<td>&nbsp; 渠道:</td>

				    	<td> 	  
				    	 <select name="channel" id="channel"  >
				      <option value="*"  selected="selected">全部</option>
				      <option value="android" >android</option>
				      <option value="ios">ios</option>
				      <option value="other">other</option>
				  </select>
				  </td></tr>
				</table>
 	
 	</td>

<td>
&nbsp; 匹配方式：
		</td>
<td>
				<select name="matchmode" id="matchmode" onchange="tooglemode()" >
			      <option value="pid"  selected="selected">广告位</option>
			      <option value="keyword">媒体关键词</option>
			  </select>
		</td>


<td>		  
			   
			  <table id="pidtbl" border="0" cellspacing="0" cellpadding="0"> 
				<tr>
						<td> &nbsp;广告位PID:</td>
			    <td> 	 	<input type="text" name="thepid" id="thepid" value="*"  /></td>
			    <td> &nbsp;模板ID:</td>
			    <td> 	 	<input type="text" name="thetid" id="thetid" value="*"  /></td>
			 </tr>
			</table>	
	</td>


<td>			
				<table id="searchtbl" style="display:none" border="0" cellspacing="0" cellpadding="0">
			<tr>
										<td>&nbsp; 关键词:</td>
   
				 <td> 	 	<input type="text" name="searchkeyword" id="searchkeyword" value="*新浪*"  />
			
			</td>
			 </tr>
			</table>
  
	</td>
	
	<td>
		
		    <input type="button" onclick="timeseries()"  value="查看" />
		</td>
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
<script  type="text/javascript" language="JavaScript" src="./416_realtime_fn.js" charset="utf-8"></script>
<script  type="text/javascript" language="JavaScript" src="./416_realtime_new.js" charset="utf-8"></script>
<script  type="text/javascript" language="JavaScript" src="./416_realtime.js" charset="utf-8"></script>



<script>
	
var typeshow=[];

function makeDataSet()
{
		
		var labelName={};
		labelName["zeroarr"]="1";

		
		labelName["apv_sum"]=0;
		labelName["auv_sum"]=0;
		labelName["aclick_sum"]="1";
		labelName["spv_sum"]=0;
		labelName["suv_sum"]=0;
		labelName["sclick_sum"]=0;
		labelName["sclickuv_sum"]=0;
		labelName["lead_click_sum"]=0;
		labelName["lead_click_uv_sum"]=0;
		labelName["call_client_pv_sum"]=0;
		labelName["call_client_uv_sum"]=0;
		labelName["download_client_pv_sum"]=0;
		labelName["download_client_uv_sum"]=0;
	

		labelName["promise_aclick_sum"]=0;
		labelName["promise_aclick_rate"]=0;
	
		return labelName;
}


var g_xformat="%h:%M";//

var g_issumByDay=true;
if(g_issumByDay)
{
	g_xformat="%m-%d";
	g_minTickSize="day";
}

var today=new Date();
jQuery("#thedateStart").val(parseDay(new Date(today.getTime())));

jQuery("#thedateEnd").val(parseDay(new Date(today.getTime()-1000*3600*24*1)));


var choiceContainer = $("#choices");
var datasets={};
var mdrilldata={};
var g_result={};
g_debug_mode=true;
g_debug_mode_realtime=false;

$("#source").val("pc");
$("#matchmode").val("pid");
tooglemode();
tooglesource();

//timeseries();
$(function(){
var logparams={};
logparams["date"]=parseDate(new Date());
logparams["bizdate"]=parseDay(new Date())+"-"+parseDay(new Date());
logparams["nick"]=log_nickname.replace(/\(.*\)/ig,"");
logparams["email"]=log_userid+"@taobao.com";
logparams["set"]="416扎堆";
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
