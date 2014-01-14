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
     

     <script src="./src/i18n/grid.locale-en.js" type="text/javascript"></script>
      <script src="./js/jquery.jqGrid.min.js" type="text/javascript"></script>

     
<script>  
	
	  function showTooltip(n,x, y, contents) {
  	    $("#axis_id_index_y"+n).css({ opacity: 0.10 });
        $('<div id="tooltip" axix=\''+n+'\'>' + contents + '</div>').css( {
            position: 'absolute',
            display: 'none',
            top: y + 5,
            left: x + 5,
            border: '1px solid #fdd',
            padding: '2px',
            'background-color': '#fee',
            opacity: 0.80
        }).appendTo("body").fadeIn(200);
    }
    
    
    
    
    
    
    
    
         
         
	 updateLegendTimeout = null;
	 latestPosition = null;
	     var previousPoint = null;

	 
function updateLegend() {

			updateLegendTimeout = null;

			var pos = latestPosition;
		

			var axes = plot.getAxes();
			if (pos.x < axes.xaxis.min || pos.x > axes.xaxis.max ||
				pos.y < axes.yaxis.min || pos.y > axes.yaxis.max) {
				return;
			}

						var legends = $("#placeholder .legendLabel");

			var i, j, dataset = plot.getData();
			for (i = 0; i < dataset.length; ++i) {

					var series = dataset[i];	
					for (j = 0; j < series.data.length; ++j) {
						if (series.data[j]!=null&&series.data[j][0] > pos.x) {
							var p2=series.data[j];

							for(var k=(j-1);k>=0;k--)
							{
								if(series.data[k]!=null)
								{
										var p1=series.data[k];
										var y=p1[1]*1 + (p2[1] - p1[1]) * (pos.x - p1[0]) / (p2[0] - p1[0]);
										
										var fixresult=y.toFixed(2);
										
										if(series.label.indexOf("pv")>=0)
										{
											fixresult=y.toFixed(0);
										}
										
										if(series.label.indexOf("=")>=0)
										{
											legends.eq(i).text(series.label.replace(/=.*/, "= " + fixresult));//.toFixed(2)
										}else{
											legends.eq(i).text(series.label+"="+fixresult);
										}
										
										break ;
								}
							}
							
					
							break;
						}
					}

					
			}
		}
		
		 var plot=null;


	function plotAccordingToChoices() {
        var data = [];

        choiceContainer.find("input:checked").each(function () {
            var key = $(this).attr("name");
            if (key && datasets[key])
                data.push(datasets[key]);
        });
        
        if(data.length<=0)
        {
        		alert("请至少选择一种数据");
        		return ;
        	
        }

        if (data.length > 0)
        {
            plot =$.plot($("#placeholder"), data, {
            	
               series: {
                   lines: { show: true },
                   points: { show: false }
               },
               legend: {
							   	  backgroundOpacity: 0.15 ,

                    position: "nw" // position of default legend container within plot
              
                },
                crosshair: {
								mode: "x"
								},
               grid: { hoverable: true, clickable: true }     ,
                yaxis: { 
                	min: 0 ,
                	alignTicksWithAxis: null,
									position: "right"
                	},
                xaxis: {  	  
                	mode: "time",
                minTickSize: [1, "minute"],
                //min: mdrilldata.min,
                //max: mdrilldata.max,
                timeformat:"%h:%M"
              }
            }
            );
            
      $.each(plot.getAxes(), function (i, axis) {
			if (!axis.show)
				return;

			var box = axis.box;

			$("<div id='axis_id_index_"+axis.direction+axis.n+"' class='axisTarget' style='position:absolute; left:" + box.left + "px; top:" + box.top + "px; width:" + box.width +  "px; height:" + box.height + "px'></div>")
				.data("axis.direction", axis.direction)
				.data("axis.n", axis.n)
				.css({ backgroundColor: "#f00", opacity: 0, cursor: "pointer" })
				.appendTo(plot.getPlaceholder())
				.hover(
					function () { $(this).css({ opacity: 0.10 }) },
					function () { $(this).css({ opacity: 0 }) }
				);
		}); 
   }
   
   
   
    var previousPoint = null;
    $("#placeholder").bind("plothover", function (event, pos, item) {
    	if(!pos||!pos.x||!pos.y)
    	{
    		return ;
    	}
    	
   

        if ($("#enableTooltip:checked").length > 0) {
            if (item) {
                if (previousPoint != item.dataIndex) {
                    previousPoint = item.dataIndex;
                                	  	    $("#axis_id_index_y"+$("#tooltip").attr("axix")).css({ opacity: 0 });

                    $("#tooltip").remove();
                    var x = item.datapoint[0].toFixed(2),
                        y = item.datapoint[1].toFixed(2);
                        
                    
                    showTooltip(item.series.yaxis.n,item.pageX, item.pageY, $.plot.formatDate(new Date(parseInt(x)),"%h:%M")+"="+y);//     item.series.label + " of " + x + " = " + y
                }
            }
            else {
            	
            	  	    $("#axis_id_index_y"+$("#tooltip").attr("axix")).css({ opacity: 0 });

            	
                $("#tooltip").remove();
                
                
                
                previousPoint = null;            
            }
        }
        
        latestPosition = pos;
			if (!updateLegendTimeout) {
				updateLegendTimeout = setTimeout(updateLegend, 50);
			}
    });
    
    
	

    $("#placeholder").bind("plotclick", function (event, pos, item) {
        if (item) {
            $("#clickdata").text("You clicked point " + item.dataIndex + " in " + item.series.label + ".");
            plot.highlight(item.series, item.datapoint);
        }
    });
            
  
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
	
			function dayToTimestampDay(s1)
	{

		return dayToTimestamp2(s1.substring(0,4)+"-"+s1.substring(4,6)+"-"+s1.substring(6));
	}
	
			function dayToTimestampDayHourMin(s1,hour)
	{

			var splitday=s1.substring(0,4)+"-"+s1.substring(4,6)+"-"+s1.substring(6)+" "+hour.substring(0,2)+":"+hour.substring(2,4)+":00";
			
		return dayToTimestamp2(splitday);
	}
	
   
		function dayToTimestamp(s1,s2)
	{

		var t1= dayToTimestamp2(s1.substring(0,4)+"-"+s1.substring(4,6)+"-"+s1.substring(6));
		var t2= dayToTimestamp2(s2.substring(0,4)+"-"+s2.substring(4,6)+"-"+s2.substring(6));
		
		return (t2-t1)/(1000*3600*24);
	}
	
	function showload()
	{
		
		      document.getElementById("loading").style.display="";  

          jQuery('#loading-one').empty().append('loading...').parent().fadeIn('slow'); 
	}
	
	
	function hideload()
	{
		
		          document.getElementById("loading").style.display="none";  

	}
	

function parseDay(date)
{
    var month = date.getMonth() < 9 ? "0" + (date.getMonth() + 1) : (date.getMonth() + 1);   
    var day = date.getDate() <= 9 ? "0" + (date.getDate()) : (date.getDate());   
    var hour = date.getHours() <= 9 ? "0" + (date.getHours()) : (date.getHours());
    var yyyymmdd= (date.getFullYear() + "" + month + "" + day);         
    return yyyymmdd;
}
     	
     	</script>
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
overflow: hidden; /* Remove scroll bars on browser window */
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

 	<h1>数据比对/<a href="./tanx_table.jsp">数据排行</a></h1>
 	<br>
 	<input type="text" name="thedateStart" id="thedateStart" onFocus="WdatePicker({dateFmt:'yyyyMMdd'})"  />
 	 	<input type="text" name="thepid" id="thepid" value="*"  />

 	<select name="producttype" id="producttype" >
 		<option value="all">全部</option>
      <option value="D">D</option>
      <option value="X">X</option>
      <option value="S">S</option>
  </select>
   	<span ><input type="button" onclick="timeseries()"  value="查看" /></span><br>
   	    <p id="choices" style=""></p>

    <div id="placeholder" style="width:1200px;height:400px;">loading......</div>
    <p id="hoverdata" style="display:none">Mouse hovers at
    (<span id="x">0</span>, <span id="y">0</span>). <span id="clickdata"></span></p>
    <input id="enableTooltip" type="checkbox" checked="checked" style="display:none">

 </body>
</html>

<script>



function showresult(tp,data,YPOS,color)
{

	
	g_result[tp]={'data':data,'label':tp,"Y":YPOS,'color':color};
	
	var count=0;
	for(var p in g_result)
	{
		count++;
	}
	
	if(count<4)
	{
		return ;
		
	}
	
		hideload();
				
		 datasets =g_result;
	    var yaxisobj={};
	    var yi=1;
	    var i = 0;
	    $.each(datasets, function(key, val) {
	    		
	          //val.color = i;
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
	    });
	    
	    
	    choiceContainer.empty();
	     $("#placeholder").unbind("plothover");
	    $.each(datasets, function(key, val) {
	     choiceContainer.append(' <input type="checkbox" name="' + key +
	                               '" checked="checked" id="id' + key + '">' +
	                               '<label for="id' + key + '">'
	                                + val.label + '</label>');
	      }
	    
	    );
	    choiceContainer.find("input").click(plotAccordingToChoices);
	    plotAccordingToChoices();

}


function searchData(strtype,pidlist)
{
 	showload();
	requestPV_AVG(strtype,pidlist);
	requestPV_today(strtype,pidlist);
	requestClick_today(strtype,pidlist);
	requestClick_AVG(strtype,pidlist);
}

function requestPV_AVG(strtype,pidlist)
{
			var strproducttype=jQuery("#producttype").val();
		var chooseday=jQuery("#thedateStart").val();
		var datts=dayToTimestampDay(chooseday);
		var daystart=parseDay(new Date(datts-8*24*60*60*1000));
		var dayend=parseDay(new Date(datts-1*24*60*60*1000));
	
		var newq=getBaseFq('pv');
	  newq.push({"thedate":{"operate":9,"value":[daystart,dayend]}});
	  	var requestparams={};
			requestparams.start=0;
			requestparams.rows=10000;
			requestparams.project="tanx_pv";
			requestparams.order="asc";
			requestparams.sort="miniute_5";
			requestparams.groupby="miniute_5";
			requestparams.fl="miniute_5,sum(records),dist(thedate)";
		 requestparams.q=JSON.stringify(newq);
		 
			$.post("/result.jsp",requestparams,
					function (data, textStatus){	
						
						if(data.code!="1")
						{
								alert("服务器异常，请稍后再试");
								hideload();
								return ;
						}
						
						if(data.total<=0)
						{
								//alert("没查到匹配的数据");
								//hideload();
								//return ;
						}
						var returnresult=[];
						var listtmp=data.data.docs;
						for(var i=0;i<listtmp.length;i++)
						{
							var item=listtmp[i];
							var records=parseFloat(item["sum(records)"]);
							var days=parseFloat(item["dist(thedate)"]);
							if(days<=1)
							{
								days=1;
							}
							returnresult.push([dayToTimestampDayHourMin(chooseday,item["miniute_5"]),records/days]);
						}
						
						showresult("pv_avg",returnresult,'pv',1);
				
				}, "json");
		 
}


function requestClick_AVG(strtype,pidlist)
{
			var strproducttype=jQuery("#producttype").val();
		var chooseday=jQuery("#thedateStart").val();
		var datts=dayToTimestampDay(chooseday);
		var daystart=parseDay(new Date(datts-8*24*60*60*1000));
		var dayend=parseDay(new Date(datts-1*24*60*60*1000));
	
		var newq=getBaseFq('click');
	  newq.push({"thedate":{"operate":9,"value":[daystart,dayend]}});
	  if(strtype==1)
	  {
	  		  newq.push({"pid":{"operate":6,"value":pidlist}});
	  }
	  	var requestparams={};
			requestparams.start=0;
			requestparams.rows=10000;
			requestparams.project="tanx_click";
			requestparams.order="asc";
			requestparams.sort="miniute_5";
			requestparams.groupby="miniute_5";
			requestparams.fl="miniute_5,sum(records),dist(thedate)";
		 requestparams.q=JSON.stringify(newq);
		 
			$.post("/result.jsp",requestparams,
					function (data, textStatus){	
						
						if(data.code!="1")
						{
								alert("服务器异常，请稍后再试");
								hideload();
								return ;
						}
						
						if(data.total<=0)
						{
								//alert("没查到匹配的数据");
								//hideload();
								//return ;
						}
						var returnresult=[];
						var listtmp=data.data.docs;
						for(var i=0;i<listtmp.length;i++)
						{
							var item=listtmp[i];
							var records=parseFloat(item["sum(records)"]);
							var days=parseFloat(item["dist(thedate)"]);
							if(days<=1)
							{
								days=1;
							}
							returnresult.push([dayToTimestampDayHourMin(chooseday,item["miniute_5"]),records/days]);
						}
						
						showresult("click_avg",returnresult,'click',3);
				}, "json"); 
}


function requestPV_today(strtype,pidlist)
{
			var strproducttype=jQuery("#producttype").val();
		var chooseday=jQuery("#thedateStart").val();
		var datts=dayToTimestampDay(chooseday);
		var daystart=parseDay(new Date(datts-8*24*60*60*1000));
		var dayend=parseDay(new Date(datts-1*24*60*60*1000));
	
		var newq=getBaseFq('pv');
	  newq.push({"thedate":{"operate":1,"value":[chooseday]}});
	  	var requestparams={};
			requestparams.start=0;
			requestparams.rows=10000;
			requestparams.project="tanx_pv";
			requestparams.order="asc";
			requestparams.sort="miniute_5";
			requestparams.groupby="miniute_5";
			requestparams.fl="miniute_5,sum(records)";
		 requestparams.q=JSON.stringify(newq);
		 
			$.post("/result.jsp",requestparams,
					function (data, textStatus){	
						
						if(data.code!="1")
						{
								alert("服务器异常，请稍后再试");
								hideload();
								return ;
						}
						
						if(data.total<=0)
						{
								//alert("没查到匹配的数据");
								//hideload();
								//return ;
						}
						var returnresult=[];
						var listtmp=data.data.docs;
						for(var i=0;i<listtmp.length;i++)
						{
							var item=listtmp[i];
							var records=parseFloat(item["sum(records)"]);
							returnresult.push([dayToTimestampDayHourMin(chooseday,item["miniute_5"]),records]);
						}
						
						showresult("pv_today",returnresult,'pv',2);
				
				}, "json");
}


function requestClick_today(strtype,pidlist)
{
			var strproducttype=jQuery("#producttype").val();
		var chooseday=jQuery("#thedateStart").val();
		var datts=dayToTimestampDay(chooseday);
		var daystart=parseDay(new Date(datts-8*24*60*60*1000));
		var dayend=parseDay(new Date(datts-1*24*60*60*1000));
	
		var newq=getBaseFq('click');
	  newq.push({"thedate":{"operate":1,"value":chooseday}});
	    if(strtype==1)
	  {
	  		  newq.push({"pid":{"operate":6,"value":pidlist}});
	  }
	  	var requestparams={};
			requestparams.start=0;
			requestparams.rows=10000;
			requestparams.project="tanx_click";
			requestparams.order="asc";
			requestparams.sort="miniute_5";
			requestparams.groupby="miniute_5";
			requestparams.fl="miniute_5,sum(records)";
		 requestparams.q=JSON.stringify(newq);
		 
		 
			$.post("/result.jsp",requestparams,
					function (data, textStatus){	
						
						if(data.code!="1")
						{
								alert("服务器异常，请稍后再试");
								hideload();
								return ;
						}
						
						if(data.total<=0)
						{
								//alert("没查到匹配的数据");
								//hideload();
								//return ;
						}
						var returnresult=[];
						var listtmp=data.data.docs;
						for(var i=0;i<listtmp.length;i++)
						{
							var item=listtmp[i];
							var records=parseFloat(item["sum(records)"]);
							returnresult.push([dayToTimestampDayHourMin(chooseday,item["miniute_5"]),records]);
						}
						
						showresult("click_today",returnresult,'click',4);
				
				}, "json");
}


function getBaseFq(tp)
{
	var strproducttype=jQuery("#producttype").val();
	var thepid=jQuery("#thepid").val();
	
	var newq=[];
	  if(strproducttype!='all'&&tp=='pv')
	  {
	  	newq.push({"producttype":{"operate":1,"value":strproducttype}});
	  }
	  if(thepid!='*'&&thepid!="")
	  {
	  	newq.push({"pid":{"operate":1,"value":thepid}});
	  }
	  return newq;
}

function timeseries()
{
	var strproducttype=jQuery("#producttype").val();
	var chooseday=jQuery("#thedateStart").val();
	var datts=dayToTimestampDay(chooseday);
	var daystart=parseDay(new Date(datts-8*24*60*60*1000));
	var dayend=parseDay(new Date(datts-1*24*60*60*1000));
	g_result={};
	if(strproducttype!='all')
	{
			var newq=getBaseFq('pv');
			newq.push({"thedate":{"operate":9,"value":[daystart,chooseday]}});
	  	var requestparams={};
			requestparams.start=0;
			requestparams.rows=10000;
			requestparams.project="tanx_pv";
			requestparams.order="desc";
			requestparams.sort="count(*)";
			requestparams.groupby="pid";
			requestparams.fl="pid,count(*)";
		 requestparams.q=JSON.stringify(newq);
		 
		 showload();
			$.post("/result.jsp",requestparams,
					function (data, textStatus){	
						
						if(data.code!="1")
						{
								alert("服务器异常，请稍后再试");
								hideload();
								return ;
						}
						
						if(data.total<=0)
						{
								alert("没查到匹配的数据");
								hideload();
								return ;
						}
						var pidlist=[];
						var listtmp=data.data.docs;
						for(var i=0;i<listtmp.length;i++)
						{
							var item=listtmp[i];
							pidlist.push(item['pid']);
						}
						
						searchData(1,pidlist);
				
				}, "json");
			
			
		
	}else{
			searchData(0,[]);
	}
	
}


var today=new Date();
jQuery("#thedateStart").val(parseDay(new Date(today.getTime())));

var choiceContainer = $("#choices");
var datasets={};
var mdrilldata={};

var g_result={};

timeseries();
	</script>	
</script>
