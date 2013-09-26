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
    <title>TimeSeries</title>
    <!--[if lte IE 8]><script language="javascript" type="text/javascript" src="http://people.iola.dk/olau/flot/excanvas.min.js"></script><![endif]-->
    <script language="javascript" type="text/javascript" src="./js/jquery.js"></script>
    <script language="javascript" type="text/javascript" src="./js/jquery.flot.js"></script>
 </head>
 <body>
    <div id="placeholder" style="width:1200px;height:600px;"></div>

    <p id="choices">Show:</p>
    
    
    
    <p id="hoverdata" style="display:none">Mouse hovers at
    (<span id="x">0</span>, <span id="y">0</span>). <span id="clickdata"></span></p>

  

    <input id="enableTooltip" type="checkbox" checked="checked" style="display:none">

<script type="text/javascript">
$(function () {
	
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
	String singleY = request.getParameter("singleY");
	String resultdatasets=TimeSeries.result(projectName, callback, startStr, rowsStr, queryStr, dist, fl, groupby, sort, order,leftjoin,out,dimvalue,singleY);
%>
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



	function plotAccordingToChoices() {
        var data = [];

        choiceContainer.find("input:checked").each(function () {
            var key = $(this).attr("name");
            if (key && datasets[key])
                data.push(datasets[key]);
        });

        if (data.length > 0)
        {
            var plot =$.plot($("#placeholder"), data, {
            	
               series: {
                   lines: { show: true },
                   points: { show: true }
               },
               grid: { hoverable: true, clickable: true }     ,
                yaxis: { 
                	min: 0 ,
                	alignTicksWithAxis: null,
									position: "left"
                	},
                xaxis: {  	  
                	mode: "time",
                minTickSize: [1, "day"],
                min: mdrilldata.min,
                max: mdrilldata.max,
                timeformat:"%y-%m-%d"
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
        $("#x").text(pos.x.toFixed(2));
        $("#y").text(pos.y.toFixed(2));

        if ($("#enableTooltip:checked").length > 0) {
            if (item) {
                if (previousPoint != item.dataIndex) {
                    previousPoint = item.dataIndex;
                                	  	    $("#axis_id_index_y"+$("#tooltip").attr("axix")).css({ opacity: 0 });

                    $("#tooltip").remove();
                    var x = item.datapoint[0].toFixed(2),
                        y = item.datapoint[1].toFixed(2);
                        
                    
                    showTooltip(item.series.yaxis.n,item.pageX, item.pageY, $.plot.formatDate(new Date(parseInt(x)),"%y-%m-%d")+"="+y);//     item.series.label + " of " + x + " = " + y
                }
            }
            else {
            	
            	  	    $("#axis_id_index_y"+$("#tooltip").attr("axix")).css({ opacity: 0 });

            	
                $("#tooltip").remove();
                
                
                
                previousPoint = null;            
            }
        }
    });
    
    
	

    $("#placeholder").bind("plotclick", function (event, pos, item) {
        if (item) {
            $("#clickdata").text("You clicked point " + item.dataIndex + " in " + item.series.label + ".");
            plot.highlight(item.series, item.datapoint);
        }
    });
    }
    
	    var mdrilldata = <%=resultdatasets%>;

if(mdrilldata.code==1)
{
    var datasets = mdrilldata.data;

    // hard-code color indices to prevent them from shifting as
    // countries are turned on/off
    var yaxisobj={};
    var yi=1;
    var i = 0;
    $.each(datasets, function(key, val) {
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
    });
    
    // insert checkboxes 
    var choiceContainer = $("#choices");
    $.each(datasets, function(key, val) {
        choiceContainer.append('<br/><input type="checkbox" name="' + key +
                               '" checked="checked" id="id' + key + '">' +
                               '<label for="id' + key + '">'
                                + val.label + '</label>');
    });
    choiceContainer.find("input").click(plotAccordingToChoices);


    plotAccordingToChoices();
    
    
    
    
    
 

    
  }else{
  	
  	alert(mdrilldata.msg);
  	window.close();
	}


});
</script>

 </body>
</html>
