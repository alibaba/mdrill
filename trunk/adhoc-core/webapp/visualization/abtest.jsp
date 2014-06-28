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
    <title>visualization</title>
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
     <script language="javascript" type="text/javascript" src="../js/visualization.js"></script>
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


	</style>


 </head>
 <body>
 	    <div id="loading" style="position:fixed !important;position:absolute;top:0;left:0;height:100%; width:100%; z-index:999; background:#000 url(http://interjc.googlecode.com/svn/trunk/waterfall/img/load.gif) no-repeat center center; opacity:0.6; filter:alpha(opacity=60);font-size:14px;line-height:20px;display:none" >  
    <p id="loading-one" style="color:#fff;position:absolute; top:50%; left:50%; margin:20px 0 0 -50px; padding:3px 10px;" onclick="javascript:turnoff('loading')">页面载入中..</p>  
</div>  

	<%
	response.setHeader("Access-Control-Allow-Credentials", "true");
	response.setHeader("Access-Control-Allow-Origin","http://localhost,http://adhoc.etao.com:9999");
	String projectName = request.getParameter("project");
	String callback = request.getParameter("callback");
	String startStr = request.getParameter("start");
	String rowsStr = request.getParameter("rows");
	String queryStr = request.getParameter("q");
	String dist = request.getParameter("dist");
	String username = request.getParameter("username");
	
	String fl = String.valueOf(request.getParameter("fl")).replaceAll("thedate,","");
	String groupby = String.valueOf(request.getParameter("groupby")).replaceAll("thedate,","");
	String sort = request.getParameter("sort");
	String order = request.getParameter("order");
	String leftjoin = request.getParameter("leftjoin");
	String dimvalue = String.valueOf(request.getParameter("dimvalue")).replaceAll("日期,","");
	String singleY = request.getParameter("singleY");
		String jobparam = String.valueOf(request.getParameter("jobparam")).replaceAll(".*过滤条件","过滤条件").replaceAll("时间.*","").replaceAll("。","");

	
	ArrayList<String> groupBy=Pie.parseGroup(fl);

	String jsonParse=Pie.parseStatList(projectName,queryStr,fl,groupby,dimvalue);
		String jsonParseGroup=Pie.parseGroupList(projectName,queryStr,fl,groupby,dimvalue);

%>
 	(<%=jobparam%>)

<table border=2>
	<tr><td>原始数据分布</td><td>原始数据趋势</td><td>abtest</td></tr>
		<tr>
	<td>
			
			<!--##################################-->
	<div>
 	<input style="width:100px" type="text" name="thedateStart_pie" id="thedateStart_pie" onFocus="WdatePicker({dateFmt:'yyyyMMdd'})"  />
 	<input style="width:100px"  type="text" name="thedateEnd_pie" id="thedateEnd_pie" onFocus="WdatePicker({dateFmt:'yyyyMMdd'})"  />
 	<br>
 	   	<select style="width:100px"  name="group_pie" id="group_pie" >
      <option value="thedate">thedate</option>
  </select>
 	 	<select style="width:100px"  name="stat_pie" id="stat_pie" >
      <option value="count(*)">计数(*)</option>
  </select>
  
  <br>
  	<select  name="maxrows" id="maxrows"  style="display:none">
      <option value="10">显示10组</option>
       <option value="20">显示20组</option>
        <option value="5">显示5组</option>
  </select>
  	<select style="width:100px"  name="displaytype" id="displaytype" onchange="showIndex(0,<%=groupBy.size()%>,null)" >
      <option value="pie">饼图</option>
       <option value="cate">柱图</option>
  </select>
  
  <select name="displayother" id="displayother"  style="display:none" >
  	  <option value="Y">显示其他</option>
      <option value="N">不显示其他</option>
  </select>
 	 	<input style="width:100px"  type="button" onclick="showIndex(0,<%=groupBy.size()%>,null)"  value="查询" />
		</div>
	<table  border="0" cellspacing="0" cellpadding="0" id="pie_multy"><tr> 

	<%
	for(int i=0;i<1;i++)
	{
	
%>
		<td valign="middle">
			
			<table  border="0" cellspacing="0" cellpadding="0" id="pie_placeholder_tbl_<%=i%>">
  <tr>
    <td colspan="2">    	<div id="pie_placeholder_col_<%=i%>" > </div></td>
  </tr>
  <tr>
    <td>
    	<div id="pie_placeholder_tip_<%=i%>" > </div>
    	</td>
    <td>				 <div id="pie_placeholder_<%=i%>" style="width: 300px;height: 300px;">	</div>	</td>

  </tr>
</table>
			</td>
<%
	}
%>

</tr>
</table>

   	<table  border="0" cellspacing="0" cellpadding="0" id="cate_multy" style="display:none" ><tr> 
		<%

	
	for(int i=0;i<1;i++)
	{
	
%>
		<td valign="middle">
			
			<table  border="0" cellspacing="0" cellpadding="0" id="cate_placeholder_tbl_<%=i%>">
  <tr>
    <td colspan="2">    	<div id="cate_placeholder_col_<%=i%>" > </div></td>
  </tr>
  <tr>
    <td>
    	<div id="cate_placeholder_tip_<%=i%>" > </div>
    	</td>
    <td>				 <div id="cate_placeholder_<%=i%>" style="width: 300px;height: 300px;">	</div>
</td>

</tr>
</table>
</td>
<%
	}
%>
  </tr>
</table>
			
	</td>
	<td>
			
		<div>	
 	<input style="width:100px" type="text" name="thedateStart" id="thedateStart" onFocus="WdatePicker({dateFmt:'yyyyMMdd'})"  />
 	<input style="width:100px" type="text" name="thedateEnd" id="thedateEnd" onFocus="WdatePicker({dateFmt:'yyyyMMdd'})"  />
 	
 	<select name="singleY" id="singleY" style="display:none" >
      <option value="Y">单个Y轴</option>
      <option value="N">多个Y轴</option>
  </select>
  <select name="showPre" id="showPre" style="display:none" >
      <option value="N">不显示预测值</option>
            <option value="Y">显示预测值</option>

  </select>
   	<span ><input style="width:100px" type="button" onclick="timeseries()"  value="趋势分析" /></span><br>
  <span >指标:</span><span id="divlabel" > </span>
  </div>

	<div>
    <div id="placeholder" style="width:420px;height:200px;">loading......</div>
    <p id="choices" style="display:none">Show:</p>
    <p id="hoverdata" style="display:none">Mouse hovers at
    (<span id="x">0</span>, <span id="y">0</span>). <span id="clickdata"></span></p>
    <input id="enableTooltip" type="checkbox" checked="checked" style="display:none">
		</div>

			</td>
			
			
		<td>
			
			核心维度：
						<div id="cmp_str_col_important_select">
										<input type="checkbox" name="cmp_num_99999"  checked="checked"  disabled="disabled" id="cmp_num_99999"  value="main_cat_name"><label for="cmp_num_99999" id="label_cmp_num_99999">主营类目名称</label>
							</div>
						
			核心指标：
						<div id="cmp_num_col_important_select">
										<input type="checkbox" name="cmp_num_99998"  checked="checked"  disabled="disabled" id="cmp_num_99998"  value="(case when (alipay_direct_num+alipay_indirect_num)=0 then 0.00001 else ((alipay_direct_amt+alipay_indirect_amt)/(alipay_direct_num+alipay_indirect_num)) end)"><label for="cmp_num_99998" id="label_cmp_num_99998">笔单价</label>

							</div>
分组的时候需要对比的维度：
			<div id="cmp_str_col_select"></div>
		
			分组的时候需要对比的指标：
			<div id="cmp_num_col_select"></div>
			
			保存名称：<input type="text" name="abtest_save_name" id="abtest_save_name" value="abtest" />
			<input style="width:100px" type="button"  onclick="makeabtest()"  value="生成" />
			
		</td>
</tr>
</table>




 
 
 </body>
</html>

<script>
	
	function getChooseStr(strid)
	{
		
		var cmp_str_col_select_obj = $("#"+strid);
	  var json=[];
	  cmp_str_col_select_obj.find("input:checked").each(function () {
	  	json.push($(this).val()) ;//[]=$("#label_"+$(this).attr("name")).text();
	  });
	  
	  return json;
	}
	function makeabtest()
	{


	
			var cmp_str_col_important_select=getChooseStr("cmp_str_col_important_select");
  	  var cmp_num_col_important_select=getChooseStr("cmp_num_col_important_select"); 	  
  	  var json_str_cols=getChooseStr("cmp_str_col_select");
  	  var json_num_cols=getChooseStr("cmp_num_col_select");
 
 
  	 
  	 var saveparams={};
  	 saveparams.project="<%=projectName%>";
  	 saveparams.callback="<%=callback%>";
  	 saveparams.start="<%=startStr%>";
  	 saveparams.rows="<%=rowsStr%>";
  	 saveparams.q=JSON.stringify(<%=queryStr%>);
  	 saveparams.dist="<%=dist%>";
  	 saveparams.username="<%=username%>";
  	 saveparams.fl="<%=fl%>";
  	 saveparams.groupby="<%=groupby%>";
  	 
  	 saveparams.sort="<%=sort%>";
  	 saveparams.order="<%=order%>";
  	 saveparams.leftjoin="<%=leftjoin%>";
  	 saveparams.dimvalue="<%=dimvalue%>";
  	 saveparams.jobparam="<%=jobparam%>";
  	 

  	 
  	 var requestparams={};
  	 requestparams.project="<%=projectName%>";
  	 requestparams.callback="";
  	 requestparams.q=JSON.stringify(<%=queryStr%>);
		requestparams.colls_important=JSON.stringify(cmp_str_col_important_select);
		requestparams.number_important=JSON.stringify(cmp_num_col_important_select);
		requestparams.colls=JSON.stringify(json_str_cols);
		requestparams.numbers=JSON.stringify(json_num_cols);
		requestparams.mailto="<%=username%>@alibaba-inc.com";
		requestparams.username="<%=username%>";
		requestparams.jobname=$("#abtest_save_name").val();
		requestparams.memo="beiyong";
		
 		 requestparams.params=JSON.stringify(saveparams);
 
  	requestparams.rnd=Math.random();






	$.post("./kmeans.jsp",requestparams,

		function (data, textStatus){
			
			alert(JSON.stringify(data));
			
		
		}, "json");
  	 
	
		
	}
		var g_jsonparse=<%=jsonParse%>;
		var g_jsonparse_group=<%=jsonParseGroup%>;
		
		var g_queryStr=<%=queryStr%>;
		var g_dimvalue="<%=dimvalue%>";
		var g_projectName="<%=projectName%>";
		var g_fl="<%=fl%>";
		var g_groupby="<%=groupby%>";
		var g_leftjoin="<%=(leftjoin==null||leftjoin.isEmpty()?"yes":"no")%>";
		if("yes"!=g_leftjoin)
		{
			alert("abtest，目前不支持个人表，请取消选择的个人表");
			window.close();
		}
	
	   var divlabelDiv = $("#divlabel");
	   var stat_pie_select = $("#stat_pie");

	   	   		   var cmp_num_col_select = $("#cmp_num_col_select");

	   
	   divlabelDiv.empty();
	   stat_pie_select.empty();
	   var joinchar="checked=\"checked\"";
	   
	   var repeat_filter={};
	   for(var i=0;i<g_jsonparse.stats.length&&g_jsonparse.statsShow.length;i++)
	   {
	   		var statval=g_jsonparse.stats[i];
	   		var statvalshow=g_jsonparse.statsShow[i];
	   		divlabelDiv.append(' <input type="checkbox" name="divlabel_' + i +
                               '" '+joinchar+' id="divlabel_' + i + '"  value="'
                                + i + '">' +
                               '<label for="divlabel_' + i + '">'
                                + statvalshow + '</label>');
                                                      joinchar="";
          
            if(statval!="count(*)")
            {                
            	
            	var showstr=statvalshow.replace(/.*\((.*)\).*/ig,"$1");
            	 var statvalstr=statval.replace(/.*\((.*)\).*/ig,"$1");
            	 if(!repeat_filter[statvalstr])
            	 {
repeat_filter[statvalstr]=true;
				cmp_num_col_select.append(' <input type="checkbox" name="cmp_num_' + i +
                               '"  id="cmp_num_' + i + '"  value="'
                                + statvalstr + '">' +
                               '<label for="cmp_num_' + i + '" id="label_cmp_num_' + i + '">'
                                + showstr + '</label>');
                              };
                              
                            }
                                
				stat_pie_select.append("<option value='"+statval+"'>"+statvalshow+"</option>");
	   }
	   
	   	   var group_pie_select = $("#group_pie");
	   	   	    var cmp_str_col_select = $("#cmp_str_col_select");

	   	   group_pie_select.empty();

	   for(var i=0;i<g_jsonparse_group.group.length&&g_jsonparse_group.groupShow.length;i++)
	   {
	   		var statval=g_jsonparse_group.group[i];
	   		if(statval!="thedate")
	   		{
	   		var statvalshow=g_jsonparse_group.groupShow[i];                    
				group_pie_select.append("<option value='"+statval+"'>"+statvalshow+"</option>");
				
				cmp_str_col_select.append(' <input type="checkbox"  checked="checked" name="cmp_str_' + i +
                               '"  id="cmp_str_' + i + '"  value="'
                                + statval + '">' +
                               '<label for="cmp_str_' + i + '"  id="label_cmp_str_' + i + '">'
                                + statvalshow + '</label>');
				}
	   }
	   
	 
	   
	function dayToTimestamp2(str)
	{
		if(str.length<=10)
		{
			str+=" 00:00:00";
		}
	var new_str = str.replace(/:/g,'-');
		new_str = new_str.replace(/ /g,'-');
	var arr = new_str.split("-");
	if(arr.length<=5)
	{
		return 0;
	}
	
		var datum = new Date(Date.UTC(arr[0],arr[1]-1,arr[2],arr[3]-8,arr[4],arr[5]));
	
		return datum.getTime();
	}
   
		function dayToTimestamp(s1,s2)
	{

		var t1= dayToTimestamp2(s1.substring(0,4)+"-"+s1.substring(4,6)+"-"+s1.substring(6));
		var t2= dayToTimestamp2(s2.substring(0,4)+"-"+s2.substring(4,6)+"-"+s2.substring(6));
		
		return (t2-t1)/(1000*3600*24);
	}
	
	function validate(s1,s2,table)
	{
		
		  var days=10;
			var tableDays={"rpt_p4padhoc_cust":366,"rpt_b2bad_hoc_memb_sum_d":366,"r_rpt_tanx_amif_adhoc_adzone":366,"r_rpt_tanx_amif_zx_adzone":366,"r_rpt_tanx_amif_zxbd_adzone":366};
			if(tableDays[table]>0)
			{
				days=tableDays[table];
			}
			
			var l=dayToTimestamp(s1,s2);
			if(l>days)
			{
				alert("该表查询时间间隔不能超过"+days+"天");
				return false;
			}
			return true;

		
	}
	
function parseDay(date)
{
    var month = date.getMonth() < 9 ? "0" + (date.getMonth() + 1) : (date.getMonth() + 1);   
    var day = date.getDate() <= 9 ? "0" + (date.getDate()) : (date.getDate());   
    var hour = date.getHours() <= 9 ? "0" + (date.getHours()) : (date.getHours());
    var yyyymmdd= (date.getFullYear() + "" + month + "" + day);         
    return yyyymmdd;
}

var today=new Date();
	
   	jQuery("#thedateStart").val(parseDay(new Date(today.getTime()-7*24*60*60*1000)));
   	
   	jQuery("#thedateEnd").val(parseDay(new Date(today.getTime()-1*24*60*60*1000)));

   	 	jQuery("#thedateStart_pie").val(parseDay(new Date(today.getTime()-7*24*60*60*1000)));
   	
   	jQuery("#thedateEnd_pie").val(parseDay(new Date(today.getTime()-1*24*60*60*1000)));
   	
function isthedate(obj)
{
	if("thedate"==obj['key'])
	{
		return true;
	}
	
	for(var p in obj)
	{
		if("thedate"==p)
		{
			
			return true;
		}	
		
	}
	
	
	return false;
}


var choiceContainer = $("#choices");
var datasets={};
var mdrilldata={};
function timeseries()
{
	
	var newq=[];
	for(var i=0;i<g_queryStr.length;i++)
	{
		var item=g_queryStr[i];
		if(!isthedate(item))
		{
			newq.push(item);
		}
	}
	var startday=jQuery("#thedateStart").val();
	var endday=jQuery("#thedateEnd").val();
	newq.push({"thedate":{"operate":9,"value":[startday,endday]}});
	
	
	if(!validate(startday,endday,g_projectName))
	{
		return ;
	}
	
	var requestparams={};
	requestparams.q=JSON.stringify(newq);
  requestparams.project=g_projectName;
  requestparams.start=0;
  requestparams.rows=1000;
  requestparams.groupby="thedate";
  requestparams.sort="thedate";
  requestparams.order="asc";
  requestparams.singleY=$("#singleY").val();
  requestparams.rnd=Math.random();
  var divlabelDiv = $("#divlabel");
  requestparams.fl="thedate";
  requestparams.dimvalue="";

  var ischoose=false;
  divlabelDiv.find("input:checked").each(function () {
        var key = $(this).attr("name");
        var val=parseInt($(this).val());
        if (key&&val>=0)
        {
        		requestparams.fl+=","+g_jsonparse.stats[val];
        	  requestparams.dimvalue+=","+g_jsonparse.statsShow[val];
						ischoose=true;
        }  
  });
  
  if(!ischoose)
  {
  	alert("请至少选择一个指标");
  	return ;
  }
  
  var showPrev=jQuery("#showPre").val();
      document.getElementById("loading").style.display="";  

          jQuery('#loading-one').empty().append('loading...').parent().fadeIn('slow');  
  $.post("/timeseries_json.jsp",requestparams,
			function (data, textStatus){
	    mdrilldata = data;
    document.getElementById("loading").style.display="none";  

	if(mdrilldata.code==1)
	{
	    datasets = mdrilldata.data;
	    var yaxisobj={};
	    var yi=1;
	    var i = 0;
	    $.each(datasets, function(key, val) {
	    		if(key.indexOf("_pre")>0&&"Y"!=showPrev)
	    		{
	    			
	    		}else{
	    	
	          val.color = i;
	        	var kkkk=val.Y;
	       	 	var yindex=yaxisobj[kkkk];
	       	 	if(!yindex)
	       	 	{
	       	 		yindex=yi;
	       	 		yaxisobj[kkkk]=yindex;
	       	 		yi++;
	       	 	}
	       	 	
	          val.yaxis = yindex;
	
	        ++i;
	      }
	    });
	    
	    
	    choiceContainer.empty();
	     $("#placeholder").unbind("plothover");
	    $.each(datasets, function(key, val) {
	    	if(key.indexOf("_pre")>0&&"Y"!=showPrev)
	    		{
	    			
	    		}else{
	        choiceContainer.append('<br/><input type="checkbox" name="' + key +
	                               '" checked="checked" id="id' + key + '">' +
	                               '<label for="id' + key + '">'
	                                + val.label + '</label>');
	                              }
	    });
	    choiceContainer.find("input").click(plotAccordingToChoices);
	    plotAccordingToChoices();
	  }else{
	  	alert(mdrilldata.msg);
		}		
	}, "json");
  

	
}


	
 var params=<%=Pie.parseStat(projectName,queryStr,fl,groupby,dimvalue)%>;

			
function labelFormatter(label, series) {
		return "<div style='font-size:8pt; text-align:center; padding:2px; color:white;' title="+JSON.stringify(series.data[0][1])+">" + label + "<br/>" + Math.round(series.percent) + "%"+"</div>";
	}

var labelfilter={};
var labelfilter_other={};




function showIndex(pos,len,fq)
{
	if(len<=0)
	{
		alert("多维透视要求必须选择一个维度，请重新选择");
		return ;
	}
	
	var dtype=$("#displaytype").val();
			$("#pie_multy").hide();
			$("#cate_multy").hide();
			$("#"+dtype+"_multy").show();
			
			var showOther=$("#displayother").val();
	
			
	for(var i=(pos+1);i<len;i++)
	{
		$("#"+dtype+"_placeholder_"+i).hide();
			$("#"+dtype+"_placeholder_tip_"+i).hide();
			$("#"+dtype+"_placeholder_tbl_"+i).hide();
	}
	
	
	
	var requestparams={};
	requestparams.start=0;
	requestparams.rows=$("#maxrows").val();
	requestparams.project=params.project;
	requestparams.order="desc";
	requestparams.sort=jQuery("#stat_pie").val();
	requestparams.groupby=jQuery("#group_pie").val();
	requestparams.fl=jQuery("#group_pie").val()+","+jQuery("#stat_pie").val();
	
	var newq=[];
	for(var i=0;i<g_queryStr.length;i++)
	{
		var item=g_queryStr[i];
		if(!isthedate(item))
		{
			newq.push(item);
		}
	}
	var startday=jQuery("#thedateStart_pie").val();
	var endday=jQuery("#thedateEnd_pie").val();
	newq.push({"thedate":{"operate":9,"value":[startday,endday]}});
	
		if(!validate(startday,endday,g_projectName))
	{
		return ;
	}
	
	var sfilter=newq;
	
	requestparams.showOther=showOther;
	requestparams.q=JSON.stringify(sfilter);
	
	      document.getElementById("loading").style.display="";  

	 jQuery('#loading-one').empty().append('loading...').parent().fadeIn('slow');  
  

	$.post("/pie_data.jsp",requestparams,

		function (data, textStatus){
    document.getElementById("loading").style.display="none";  

								$("#"+dtype+"_placeholder_col_"+pos).text(params.fieldToName.namelist[pos]);

								$("#"+dtype+"_placeholder_tip_"+pos).show();
								var tips=labelfilter["pos_"+(pos-1)];
							
								$("#"+dtype+"_placeholder_tip_"+pos).show();
								$("#"+dtype+"_placeholder_tbl_"+pos).show();
								if(tips)
								{
									$("#"+dtype+"_placeholder_tip_"+pos).html("<strong>&gt;&gt;&gt;</strong>");
							}else{
									$("#"+dtype+"_placeholder_tip_"+pos).html("");
							}
							
							
							if("cate"==dtype)
							{
							
							var newchardata=[];
							var newcharlabel=[];
							for(var ii=0;ii<data.data.length;ii++)
							{
								newchardata.push([data.data[ii].label,data.data[ii].data]);
								newcharlabel.push(data.data[ii].label);
							}
									var placeholder = $("#"+dtype+"_placeholder_"+pos);
									placeholder.show();
							//	

									$("#"+dtype+"_placeholder_"+pos).jqChart({
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

							
						}else{
							
									var placeholder = $("#"+dtype+"_placeholder_"+pos);
									placeholder.show();
									placeholder.unbind();
								$.plot('#'+dtype+'_placeholder_'+pos, data.data, {
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
							
									}
		
		}, "json");
	
	
	
	
};




timeseries();
showIndex(0,<%=groupBy.size()%>,null);

	</script>	
</script>
