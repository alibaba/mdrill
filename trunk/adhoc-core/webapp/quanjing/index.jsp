<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
 <head>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.io.*" %>
<%@ page import="java.net.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.util.regex.*" %>
<%@ page import="java.text.*" %>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>mointor</title>
    <script>
quanjing_logcheck=0;

</script>
    
        <script language="javascript" type="text/javascript" src="http://adhoc.etao.com:9999/quanjing_check.jsp"></script>

    <script language="javascript" type="text/javascript" src="../js/jquery.js"></script>
    <script language="javascript" type="text/javascript" src="../js/jquery.flot.js"></script>
    <script language="javascript" type="text/javascript" src="../js/jquery.flot.crosshair.js"></script>
    <script language="javascript" type="text/javascript" src="../js/My97DatePicker/WdatePicker.js"></script>
    <script language="javascript" type="text/javascript" src="../js/json2.js"></script>
    	<!--[if lte IE 8]><script language="javascript" type="text/javascript" src="../js/excanvas.min.js"></script><![endif]-->
<script>
	if(quanjing_logcheck!=1)
	{
		location.href='http://adhoc.etao.com:9999/quanjing_tologin.jsp';
	}
	
	</script>

<style>
	
body
{}{
scrollbar-face-color: #EDEDF3;
scrollbar-highlight-color: #ffffff;
scrollbar-shadow-color: #93949F;
scrollbar-3dlight-color: #EDEDF3;
scrollbar-arrow-color: #082468;
scrollbar-track-color: #F7F7F9;
scrollbar-darkshadow-color: #EDEDF3;

font-size: 9pt;
color: #003366;
overflow:auto;
}
TD {}{ FONT-SIZE: 12px }
TH
{}{
FONT-SIZE: 12px;
}
	
	</style>
<script>
	
	
	refreshPid_flag={};
	function refreshPid()
	{
		if($("#PID").val()==""||$("#thedate").val()=="")
		{
			alert("请输入pid和日期");
			return ;
		}
		
				refreshPid_flag={};

			$("#step1_1").hide();
	
	$("#step1_2").hide();
			$("#step2").hide();
	$("#step3").hide();
	
	resetnamemodle(toSearch);
	resetgroupName(toSearch);
	

	

}




function resetnamemodle(fn)
{
	if($("#logtype").val()=="click")
	{
		$("#namemodle").empty();
		$("#namemodle").append("<option value=''>---</option>");
		refreshPid_flag['namemodle']="ok";
		tooglePid(fn);
		return ;
	}
	
		
		var requestparams={};
		requestparams.start=0;
		requestparams.rows=200;
		requestparams.project="quanjingmointor_pid";
		requestparams.order="desc";
		requestparams.sort="sum(datanum_b)";
		requestparams.groupby="namemodle";
		requestparams.fl="namemodle,sum(datanum_b)";
		var thedateval=$("#thedate").val();
		var PIDval=$("#PID").val();
		requestparams.q=JSON.stringify([{"thedate":{"operate":1,"value":thedateval}},{"pid":{"operate":1,"value":PIDval}}]);
		requestparams.rnd=Math.random();

		$.post("/result.jsp",requestparams,
			function (data, textStatus){
				
				$("#namemodle").empty();
				if(data['code']!="1"||data['data']['docs'].length<=0)
				{
					$("#namemodle").append("<option value=''>---</option>");
				}else{
			
				for(var i=0;i<data['data']['docs'].length;i++)
				{
					var opvalue=data['data']['docs'][i]['namemodle'];
					var sumval=data['data']['docs'][i]['sum(datanum_b)'];
					
					var re = /^[A-Za-z0-9_-]+$/
					if(re.test(opvalue))
					{
					 $("#namemodle").append("<option value='"+opvalue+"'>"+opvalue+"</option>");
					}
				}
				refreshPid_flag['namemodle']="ok";

				tooglePid(fn);
			}
			}, "json");
}



filtername={"tbuad":["k2","mobile"],"tbtpd":["k2","mobile"],"mad":["k2","mobile"],"top":["k2","mobile"],"dpad":["k2","mobile"],"mcad":["k2","mobile"],"active":["k2","mobile"],"shopads":["k2","mobile"],"shop":["k2","mobile"],"tcmad":["k2","mobile"]
	,"tbuad":["k2","kgb"],"shop":["k2","kgb"],"shop":["k2","kgb"],"_default":["mobile","kgb"]
	};

function tooglePid(fn)
{
	
	
	if($("#logtype").val()=="click")
	{
		$("#groupName").empty();
		$("#groupName").append("<option value='kgb_af_clk_cm3'>kgb_af_clk_cm3</option>");
		$("#groupName").append("<option value='kgb_af_clk_cm6'>kgb_af_clk_cm6</option>");
		$("#groupName").append("<option value='kgb_af_mclk_cm3'>kgb_af_mclk_cm3</option>");
		$("#groupName").append("<option value='kgb_af_mclk_cm6'>kgb_af_mclk_cm6</option>");
	}else{
		
		var namenodeval=$("#namemodle").val();
		
		var filterlist=filtername["_default"];
		if(namenodeval)
		{
			filterlist=filtername[namenodeval];
			if(!filterlist)
			{
				filterlist=filtername["_default"];
			}
		}
		
		var data=groupnamelist;
		$("#groupName").empty();
		if(data['code']!="1"||data['data']['docs'].length<=0)
		{
					$("#groupName").append("<option value=''>---</option>");
		}else{
				for(var i=0;i<data['data']['docs'].length;i++)
				{
					var opvalue=data['data']['docs'][i]['groupName'];
					var isskip=false;
					for(var j=0;j<filterlist.length;j++)
					{
						if(opvalue.indexOf(filterlist[j])>=0)
						{
							isskip=true;
							break;
						}
					}
					if(!isskip)
					{
						$("#groupName").append("<option value='"+opvalue+"'>"+opvalue+"</option>"); 
					}
				}
	}
}
	
	
	if(refreshPid_flag['groupname']=="ok"&&refreshPid_flag['namemodle']=="ok")
	{
			$("#step1_1").show();
	   $("#step1_2").show();
	   if(fn)
	   {
	   fn();	
	  }
	}
	
}

groupnamelist={};
function resetgroupName(fn)
{
	
	if($("#logtype").val()=="click")
	{
		refreshPid_flag['groupname']="ok";
		tooglePid(fn);
		return ;
	}
	
		var requestparams={};
		requestparams.start=0;
		requestparams.rows=200;
		requestparams.project="quanjingmointor_pid";
		requestparams.order="desc";
		requestparams.sort="sum(datanum_b)";
		requestparams.groupby="groupName";
		requestparams.fl="groupName,sum(datanum_b)";
		var thedateval=$("#thedate").val();
		var PIDval=$("#PID").val();
		requestparams.q=JSON.stringify([{"thedate":{"operate":1,"value":thedateval}},{"pid":{"operate":1,"value":PIDval}}]);
		requestparams.rnd=Math.random();

		$.post("/result.jsp",requestparams,
			function (data, textStatus){
				groupnamelist=data;
								
				refreshPid_flag['groupname']="ok";
				tooglePid(fn);
			}, "json");
}
	

	
	function dayToTimestamp(str)
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



function display()
{
	
									var datasets = mdrilldata.data;

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
				    	$("#choices").empty();
				    		$("#placeholder").empty();

				    var choiceContainer = $("#choices");
				    var disabled="disabled=\"disabled\"";

						var indexNum=0;
				
						var defaultSelectKey={"pv":"yes","pid_pv":"yes","pid_click":"yes"};
						var tableCheckList="<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" ><tr>"
				$.each(datasets, function(key, val) {
				    	
								var strchecked="checked=\"checked\"";
					    	if(defaultSelectKey[key]!="yes")
					    	{
					    		strchecked="";
					    	}
	
					    	if(indexNum!=0&&indexNum%8==0)
					    	{
					    		tableCheckList+="</tr><tr>"
					    	}
					       tableCheckList+='<td> <input '+disabled+' type="checkbox" name="' + key +
					                               '" '+strchecked+' id="id' + key + '">' +
					                               '<label for="id' + key + '"  >'
					                                + val.label + '</label> </td>';
					                                
					       disabled="";
					       
					       indexNum++;

				                                
				    });
				    tableCheckList+="</tr></table>"
				     choiceContainer.append(tableCheckList);
				    choiceContainer.find("input").click(plotAccordingToChoices);
				
				    plotAccordingToChoices();
}
		
 var plot=null;
 
 
 
	function plotAccordingToChoices() {
									var datasets = mdrilldata.data;

        var data = [];

				    var choiceContainer = $("#choices");

        choiceContainer.find("input:checked").each(function () {
            var key = $(this).attr("name");
            if (key && datasets[key])
                data.push(selectHour(datasets[key]));
        });


        if (data.length > 0)
        {
            plot =$.plot($("#placeholder"), data, {
            	
               series: {
                   lines: { show: true },
                   points: { show: false }
               },
              crosshair: {
								mode: "x"
								},
							grid: {
								hoverable: true,
								clickable: true 
							},
							   legend: {
							   	  backgroundOpacity: 0.15 ,

                    position: "nw" // position of default legend container within plot
              
                },
                 yaxis: {
                    autoscaleMargin: 0.02,
                    position: "right" // or "right"
                },
             
                xaxis: {  	  
                	mode: "time",
                	minTickSize: [1, "minute"],
                	timeformat:"%H:%M"
              }
            }
            );
            
            var legends = $("#placeholder .legendLabel");

		legends.each(function () {
			// fix the widths so they don't jump around
			$(this).css('width', $(this).width());
		});
		
	
            
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
            
            
         
	 updateLegendTimeout = null;
	 latestPosition = null;
	 
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
                        
                    
                    showTooltip(item.series.yaxis.n,item.pageX, item.pageY, $.plot.formatDate(new Date(parseInt(x)),"%H:%M")+"="+y);//     item.series.label + " of " + x + " = " + y
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

    
    
	

    

	

	
function fetchMachineList()
{
			var requestparams={};
			var strurl="/result.jsp";

	if($("#logtype").val()=="click")
	{
		strurl="/quanjing/armoryGroup.jsp";
		requestparams.g=$("#groupName").val();
		requestparams.rnd=Math.random();
	}else{
		strurl="/result.jsp";
		requestparams.start=0;
		requestparams.rows=2000;
		requestparams.project="quanjingmointor_host";
		requestparams.order="asc";
		requestparams.sort="sum(datanum_b)";
		requestparams.groupby="nodename,dns_ip,product_name,nodegroup,site";
		requestparams.fl="nodename,dns_ip,product_name,nodegroup,site,count(*),sum(datanum_a),sum(datanum_b)";
		var thedateval=$("#thedate").val();
		var PIDval=$("#PID").val();
		requestparams.q=JSON.stringify([
		{"thedate":{"operate":1,"value":thedateval}},
		{"groupName":{"operate":1,"value":$("#groupName").val()}}
		]);
		requestparams.rnd=Math.random();
	}
	
	

		$.post(strurl,requestparams,
			function (data, textStatus){
									$("#step2").show();
					
				
					$("#machinelist").empty();
				$("#machine").empty();
				$("#machine").append("<option value=''>全部</option>");
				if(data['code']!="1"||data['data']['docs'].length<=0)
				{
					return ;
				}
			
				
        var machinestr="<table border=\"1\" cellspacing=\"0\" cellpadding=\"0\" width=\"1000\"><tr><td>nodename</td><td>dns_ip</td><td>product_name</td><td>nodegroup</td><td>site</td></tr>"
				for(var i=0;i<data['data']['docs'].length;i++)
				{
					var strnodename=data['data']['docs'][i]['nodename'];
					var strdns_ip=data['data']['docs'][i]['dns_ip'];
					var strproduct_name=data['data']['docs'][i]['product_name'];
					var strnodegroup=data['data']['docs'][i]['nodegroup'];
					var site=data['data']['docs'][i]['site'];

					machinestr+="<tr><td>"+strnodename+"</td><td>"+strdns_ip+"</td><td>"+strproduct_name+"</td><td>"+strnodegroup+"</td><td>"+site+"</td></tr>"
					
   				$("#machine").append("<option value='"+strnodename+"'>"+strnodename+"</option>");

				}
				machinestr+="</table>"
				$("#machinelist").append(machinestr);
				
			}, "json");
	
}


 function selectHour(list)
 {


		var date = new Date();   

 	   var selecttp=$("#hourselect").val();
 	   

 	  
 	   var strthedate=$("#thedate").val();
 	   var beginhour=0;
 	   if(selecttp=="1")
 	   {
 	   		beginhour=date.getHours()<6?0:date.getHours()-5;
 	   }
 	   if(selecttp=="2")
 	   {
 	   		beginhour=0;
 	   }
 	   if(selecttp=="3")
 	   {
 	   		beginhour=6;
 	   }
 	   if(selecttp=="4")
 	   {
 	   		beginhour=12;
 	   }
 	   if(selecttp=="5")
 	   {
 	   		beginhour=18;
 	   }
 	   
 	   var strhour=beginhour<=9?"0"+beginhour:beginhour;
 	   var beginstr=strthedate.substring(0,4)+"-"+strthedate.substring(4,6)+"-"+strthedate.substring(6)+" "+strhour+":00:00";
 	   	var timestrbegin=dayToTimestamp(beginstr);
 	   	var timestrend=timestrbegin+1000*3600*6;
	
	     var returnarr=[];
	     var lastts=0;
			for(var i=0;i<list['data'].length;i++)
			{
				if(selecttp=="6"||(list['data'][i][0]>=timestrbegin&&list['data'][i][0]<=timestrend))
				{
					if(lastts<=list['data'][i][0])
					{
						lastts=list['data'][i][0];
						returnarr.push(list['data'][i]);
				}
				}
			}
			
			var returnobj={};
			for(var p in list)
			{
				returnobj[p]=list[p];
			}
			
			returnobj['data']=returnarr;
			
			
			return returnobj;


}



mdrilldata = {};
flags={};
function dataCallback(tp,data)
{
	
	
	if(tp=="pid_pv_5")
	{
		
		if(data)
		{
		var endindex=data['data']['docs'].length-1;
		for(var i=0;i<endindex;i++)
				{
					var timestr=dayToTimestamp(data['data']['docs'][i]['miniute5']);
					if(timestr>0)
					{
						var item=[timestr,data['data']['docs'][i]['sum(datanum_a)']];
						
							if(mdrilldata.min==0||mdrilldata.min<timestr)
							{
								mdrilldata.min=timestr;
							}
							
							if(mdrilldata.max==0||mdrilldata.max>timestr)
							{
								mdrilldata.max=timestr;
							}
							mdrilldata.data[tp].data.push(item);
					}
				}
			}
		flags[tp]="ok"

	}if(tp=="pid_pv")
	{
		
		if(data)
		{
		var endindex=data['data']['docs'].length-5;
		for(var i=0;i<endindex;i++)
				{
					var timestr=dayToTimestamp(data['data']['docs'][i]['miniute']);
					var item=[timestr,data['data']['docs'][i]['sum(datanum_a)']];
					
						if(mdrilldata.min==0||mdrilldata.min<timestr)
						{
							mdrilldata.min=timestr;
						}
						
						if(mdrilldata.max==0||mdrilldata.max>timestr)
						{
							mdrilldata.max=timestr;
						}
						mdrilldata.data[tp].data.push(item);
				}
			}
		flags[tp]="ok"

	}else if(tp=="pv")
	{
		
		if(data)
		{
		var endindex=data['data']['docs'].length-5;
		for(var i=0;i<endindex;i++)
				{
					var timestr=dayToTimestamp(data['data']['docs'][i]['miniute']);
					var item=[timestr,data['data']['docs'][i]['sum(datanum_b)']];
					
						if(mdrilldata.min==0||mdrilldata.min<timestr)
						{
							mdrilldata.min=timestr;
						}
						
						if(mdrilldata.max==0||mdrilldata.max>timestr)
						{
							mdrilldata.max=timestr;
						}
						mdrilldata.data.pv.data.push(item);
				}
			}
		flags['pv']="ok"

	}else if(tp=="access")
	{
		if(data)
		{
						 		mdrilldata['data']["access_avg"]={"data":[],"label":"rt_avg","Y":"access"};
						 		mdrilldata['data']["access_max"]={"data":[],"label":"rt_max","Y":"access"};
						 		mdrilldata['data']["access_min"]={"data":[],"label":"rt_min","Y":"access"};
		var endindex=data['data']['docs'].length-5;

			for(var i=0;i<endindex;i++)
				{
					var timestr=dayToTimestamp(data['data']['docs'][i]['miniute']);
					//requestparams.fl="miniute,count(*),sum(rtsum),average(rtavg),max(rtmax),min(rtmin)";
					
					var access_avg=[timestr,data['data']['docs'][i]['average(rtavg)']];
					var access_max=[timestr,data['data']['docs'][i]['max(rtmax)']];
					var access_min=[timestr,data['data']['docs'][i]['min(rtmin)']];

					
						if(mdrilldata.min==0||mdrilldata.min<timestr)
						{
							mdrilldata.min=timestr;
						}
						
						if(mdrilldata.max==0||mdrilldata.max>timestr)
						{
							mdrilldata.max=timestr;
						}
						mdrilldata['data']["access_avg"].data.push(access_avg);
						mdrilldata['data']["access_max"].data.push(access_max);
						mdrilldata['data']["access_min"].data.push(access_min);
				}
			}
		flags['access']="ok"

	}else if(tp=="pid_access")
	{
		if(data)
		{
						 		mdrilldata['data']["pid_access_avg"]={"data":[],"label":"pid_rt_avg","Y":"access"};
						 		mdrilldata['data']["pid_access_max"]={"data":[],"label":"pid_rt_max","Y":"access"};
						 		mdrilldata['data']["pid_access_min"]={"data":[],"label":"pid_rt_min","Y":"access"};
		var endindex=data['data']['docs'].length-5;

			for(var i=0;i<endindex;i++)
				{
					var timestr=dayToTimestamp(data['data']['docs'][i]['miniute']);				
					var access_avg=[timestr,data['data']['docs'][i]['average(rtavg)']];
					var access_max=[timestr,data['data']['docs'][i]['max(rtmax)']];
					var access_min=[timestr,data['data']['docs'][i]['min(rtmin)']];

					
						if(mdrilldata.min==0||mdrilldata.min<timestr)
						{
							mdrilldata.min=timestr;
						}
						
						if(mdrilldata.max==0||mdrilldata.max>timestr)
						{
							mdrilldata.max=timestr;
						}
						mdrilldata['data']["pid_access_avg"].data.push(access_avg);
						mdrilldata['data']["pid_access_max"].data.push(access_max);
						mdrilldata['data']["pid_access_min"].data.push(access_min);
				}
			}
		flags['pid_access']="ok"

	}else if(tp=="pid_click")
	{
		if(data)
		{
		var endindex=data['data']['docs'].length-5;

			for(var i=0;i<endindex;i++)
				{
					var timestr=dayToTimestamp(data['data']['docs'][i]['miniute']);				
					var access_avg=[timestr,data['data']['docs'][i]['sum(datanum)']];

					
						if(mdrilldata.min==0||mdrilldata.min<timestr)
						{
							mdrilldata.min=timestr;
						}
						
						if(mdrilldata.max==0||mdrilldata.max>timestr)
						{
							mdrilldata.max=timestr;
						}
						mdrilldata['data']["pid_click"].data.push(access_avg);
				}
			}
		flags['pid_click']="ok"

	}else{
		if(data)
		{
		for(var p in data['resultData'])
		{
			var d1=data['resultData'][p];
			for(var p2 in d1)
			{
				 var d2=d1[p2];
				 for(var ptype in d2)
				 {
				 		mdrilldata['data'][ptype]={"data":[],"label":ptype,"Y":"system"};
				 		
				 		var plistdata=d2[ptype];
				 		for(var plist in plistdata)
				 		{
				 			
				 			
				 				var timestr=dayToTimestamp(plist);
							var item=[timestr,plistdata[plist]];
							
								if(mdrilldata.min==0||mdrilldata.min<timestr)
								{
									mdrilldata.min=timestr;
								}
								
								if(mdrilldata.max==0||mdrilldata.max>timestr)
								{
									mdrilldata.max=timestr;
								}
								mdrilldata['data'][ptype].data.push(item);
				 			
				 		}
				 	
					}
			}
			
		}
	}
		
				flags['system']="ok"

	}
	
	if($("#logtype").val()=="click")
	{
		if(flags['system']=="ok"&&flags['pid_click']=="ok")
		{
			$("#step3").show();
			display();
		}
		
		return ;
	}
	
	if(flags['system']=="ok"&&flags['pv']=="ok"&&flags['access']=="ok"&&flags['pid_access']=="ok"&&flags['pid_pv']=="ok"&&flags['pid_pv_5']=="ok")
	{
		$("#step3").show();
		display();
	}
	
}



function fetchAllPv()
{
	var requestparams={};
		requestparams.start=0;
		requestparams.rows=2000;
		requestparams.project="quanjingmointor_pid";
		requestparams.order="asc";
		requestparams.sort="miniute";
		requestparams.groupby="miniute";
		requestparams.fl="miniute,sum(datanum_a)";
		var thedateval=$("#thedate").val();
		var PIDval=$("#PID").val();
		requestparams.q=JSON.stringify([
		{"thedate":{"operate":1,"value":thedateval}},
		{"pid":{"operate":1,"value":PIDval}}		]);
		requestparams.rnd=Math.random();

		$.post("/result.jsp",requestparams,
			function (data, textStatus){
				if(data['code']!="1")
				{
				dataCallback("pid_pv",false);
					return ;
				}
				
				dataCallback("pid_pv",data);
			
			}, "json");
	
}


function fetchAll5Pv()
{
	var requestparams={};
		requestparams.start=0;
		requestparams.rows=2000;
		requestparams.project="quanjingmointor_pid";
		requestparams.order="asc";
		requestparams.sort="miniute5";
		requestparams.groupby="miniute5";
		requestparams.fl="miniute5,sum(datanum_a)";
		var thedateval=$("#thedate").val();
		var PIDval=$("#PID").val();
		requestparams.q=JSON.stringify([
		{"thedate":{"operate":1,"value":thedateval}},
		{"pid":{"operate":1,"value":PIDval}}		]);
		requestparams.rnd=Math.random();

		$.post("/result.jsp",requestparams,
			function (data, textStatus){
				if(data['code']!="1")
				{
				dataCallback("pid_pv_5",false);
					return ;
				}
				
				dataCallback("pid_pv_5",data);
			
			}, "json");
}

function fetchPv()
{
	
	var requestparams={};
		requestparams.start=0;
		requestparams.rows=2000;
		requestparams.project="quanjingmointor_pid";
		requestparams.order="asc";
		requestparams.sort="miniute";
		requestparams.groupby="miniute";
		requestparams.fl="miniute,sum(datanum_b)";
		var thedateval=$("#thedate").val();
		var PIDval=$("#PID").val();
		requestparams.q=JSON.stringify([
		{"thedate":{"operate":1,"value":thedateval}},
		{"pid":{"operate":1,"value":PIDval}},
		{"groupName":{"operate":1,"value":$("#groupName").val()}},
		{"namemodle":{"operate":1,"value":$("#namemodle").val()}}
		]);
		requestparams.rnd=Math.random();

		$.post("/result.jsp",requestparams,
			function (data, textStatus){
				if(data['code']!="1")
				{
				dataCallback("pv",false);
					return ;
				}
				
				dataCallback("pv",data);
			
			}, "json");
	
}


function fetchAccess()
{
	
	var requestparams={};
		requestparams.start=0;
		requestparams.rows=2000;
		requestparams.project="quanjingmointor_access";
		requestparams.order="asc";
		requestparams.sort="miniute";
		requestparams.groupby="miniute";
		requestparams.fl="miniute,average(rtavg),max(rtmax),min(rtmin)";
		var thedateval=$("#thedate").val();
		var PIDval=$("#PID").val();
		requestparams.q=JSON.stringify([
		{"thedate":{"operate":1,"value":thedateval}},
		{"pid":{"operate":1,"value":PIDval}},
		{"namemodle":{"operate":1,"value":$("#namemodle").val()}}
		]);
		requestparams.rnd=Math.random();

		$.post("/result.jsp",requestparams,
			function (data, textStatus){
				if(data['code']!="1")
				{
				dataCallback("access",false);
					return ;
				}
				
				dataCallback("access",data);
			
			}, "json");
	
}



function fetchAllAccess()
{
	
	var requestparams={};
		requestparams.start=0;
		requestparams.rows=2000;
		requestparams.project="quanjingmointor_access";
		requestparams.order="asc";
		requestparams.sort="miniute";
		requestparams.groupby="miniute";
		requestparams.fl="miniute,average(rtavg),max(rtmax),min(rtmin)";
		var thedateval=$("#thedate").val();
		var PIDval=$("#PID").val();
		requestparams.q=JSON.stringify([
		{"thedate":{"operate":1,"value":thedateval}},
		{"pid":{"operate":1,"value":PIDval}}
		]);
		requestparams.rnd=Math.random();

		$.post("/result.jsp",requestparams,
			function (data, textStatus){
				if(data['code']!="1")
				{
				dataCallback("pid_access",false);
					return ;
				}
				
				dataCallback("pid_access",data);
			}, "json");
}




function fetchAllClick()
{
	
	var requestparams={};
		requestparams.start=0;
		requestparams.rows=2000;
		requestparams.project="quanjingmointor_click";
		requestparams.order="asc";
		requestparams.sort="miniute";
		requestparams.groupby="miniute";
		requestparams.fl="miniute,sum(datanum)";
		var thedateval=$("#thedate").val();
		var PIDval=$("#PID").val();
		requestparams.q=JSON.stringify([
		{"thedate":{"operate":1,"value":thedateval}},
		{"pid":{"operate":1,"value":PIDval}}
		]);
		requestparams.rnd=Math.random();

		$.post("/result.jsp",requestparams,
			function (data, textStatus){
				if(data['code']!="1")
				{
				dataCallback("pid_click",false);
					return ;
				}
				
				dataCallback("pid_click",data);
			}, "json");
}


function fetchLoad()
{
	
	var requestparams={};
		requestparams.thedate=$("#thedate").val();
		requestparams.scope=$("#groupName").val();
		requestparams.tag="load5_avg,load15_avg,load1_avg,cpu_avg,cpu_max,cpu_min,mem_avg";

		requestparams.rnd=Math.random();

		$.post("/quanjing/monitor.jsp",requestparams,
			function (data, textStatus){
				if(data['status']!="success")
				{
				dataCallback("system",false);
					return ;
				}
				
				dataCallback("system",data);
			
			}, "json");
	
}
	


function fetchMachineLoad()
{
	
	var requestparams={};
		requestparams.thedate=$("#thedate").val();
		requestparams.scope=$("#machine").val();
		requestparams.scopeType="1";
		requestparams.monitorType="1";
		requestparams.tag="mem,load5,load15,load1,cpu";
		requestparams.rnd=Math.random();



		$.post("/quanjing/monitor.jsp",requestparams,
			function (data, textStatus){
				if(data['status']!="success")
				{
					data['resultData']={};
					dataCallback("system",false);

					return ;
				}
				
				dataCallback("system",data);
			
			}, "json");
	
}


function remakeMdrillData()
{
	
	if($("#logtype").val()=="click")
	{
			 mdrilldata = {"min":0,"max":0,"data":{"pid_click":{"data":[],"label":"pid_click","Y":"click"}},"code":1};

	}else{
				mdrilldata = {"min":0,"max":0,"data":{"pv":{"data":[],"label":"pv","Y":"pv"},"pid_pv":{"data":[],"label":"pid_pv","Y":"pv"},"pid_pv_5":{"data":[],"label":"pid_pv_5","Y":"pv5"}},"code":1};
}
}



function toSearch()
{
	$("#step2").hide();
					$("#step3").hide();
	remakeMdrillData();
	flags={};
	if($("#logtype").val()=="click")
	{
		fetchAllClick();
		fetchLoad();
			fetchMachineList();
		return ;
	}
	
	flags={};
		
			fetchPv();
			fetchAll5Pv();
			fetchAllPv();
			
			fetchAccess();
			fetchAllAccess();
			fetchLoad();
			fetchMachineList();
}
function toSearchMachine()
{
	$("#step3").hide();
		  remakeMdrillData();
		  flags={};

	if($("#logtype").val()=="click")
	{
		fetchAllClick();
		if(""==$("#machine").val())
		{
			fetchLoad();
		}else{
			fetchMachineLoad();
		}
		return ;
	}
	
	
		
		fetchPv();
		fetchAllPv();
		fetchAll5Pv();
		
		fetchAccess();
		fetchAllAccess();
		if(""==$("#machine").val())
		{
			fetchLoad();
		}else{
			fetchMachineLoad();
		}
}





	</script>
	
	
	 </head>
 <body>
<form target="_blank" method="post" action="http://adhoc.etao.com:9999/changeFree.jsp" >
<input type="submit" value="查看系统变更" />
</form>
<table border="1" cellspacing="0" cellpadding="0" width="1000">
	
	
	 <tr>
    <td>指标</td>
    <td><select name="logtype" id="logtype">
      <option value="pv">pv</option>
      <option value="click">click</option>
    </select></td>
        <td></td>

  </tr>
  <tr>
    <td>日期</td>
    <td>
        <input type="text" name="thedate" id="thedate" onFocus="WdatePicker({dateFmt:'yyyyMMdd'})"  />
         <select name="hourselect" id="hourselect">
      			<option value="1">最近6小时</option>
      			<option value="2">0~6点</option>
      			<option value="3">6~12点</option>
      			<option value="4">12~18点</option>
      			<option value="5">18点以后</option>
      			<option value="6">全天</option>
    		</select>
    </td>
    <td></td>

  </tr>
  <tr>
    <td>PID</td>
    <td><input type="text" name="PID" id="PID"  value="*"  onblur="refreshPid()" /></td>
    <td><input type="button" value="查询接口与链路"  onclick="refreshPid()" /></td>

  </tr>
  
 
  <tr id="step1_1" style="display:none">
    <td>接口</td>
    <td><select name="namemodle" id="namemodle" onchange="tooglePid()">
      <option value="">---</option>
    </select></td>
        <td></td>

  </tr>
  
   <tr id="step1_2"  style="display:none">
    <td>请求链路</td>
    <td><select name="groupName" id="groupName">
      <option value="">---</option>
    </select></td>
    <td><input type="button" value="查看负载"  onclick="toSearch()" /></td>

  </tr>
  
   

  
   <tr id="step2"  style="display:none">
    <td>机器列表</td>
    <td><select name="machine" id="machine" >
      <option value="">全部</option>
    </select></td>
        <td><input type="button" value="查看当前机器负载"  onclick="toSearchMachine()" /></td>

  </tr>
  <tr  style="display:none">
    <td>指标过滤</td>
    <td colspan="2">  <div ></div>
</td>

  </tr>
  
   
</table>
<br>
<div id="step3" >
<div id="choices"></div>
<table border="0" cellspacing="0" cellpadding="0">
	<tr><td> <div id="placeholder" style="width:1100px;height:500px;" valign="top"></div> </td></tr>
</table>
   

    <p id="hoverdata" style="display:none">Mouse hovers at
    (<span id="x">0</span>, <span id="y">0</span>). <span id="clickdata"></span></p>

    <input id="enableTooltip" type="checkbox" checked="checked" style="display:none">


 <div id="machinelist" ></div>

</div>
 </body>
</html>

<script>
	
			var date = new Date();   
        var month = date.getMonth() < 9 ? "0" + (date.getMonth() + 1) : (date.getMonth() + 1);   
        var day = date.getDate() <= 9 ? "0" + (date.getDate()) : (date.getDate());   
        var hour = date.getHours() <= 9 ? "0" + (date.getHours()) : (date.getHours());
        var yyyymmdd= (date.getFullYear() + "" + month + "" + day);   
   	jQuery("#thedate").val(yyyymmdd);
   	
   	
   	
   	refreshPid();
	</script>