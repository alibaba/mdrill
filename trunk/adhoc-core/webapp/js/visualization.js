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

        if (data.length > 0)
        {
            plot =$.plot($("#placeholder"), data, {
            	
               series: {
                   lines: { show: true },
                   points: { show: true }
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
                minTickSize: [1, "day"],
                //min: mdrilldata.min,
                //max: mdrilldata.max,
                timeformat:"%m-%d"
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
                        
                    
                    showTooltip(item.series.yaxis.n,item.pageX, item.pageY, $.plot.formatDate(new Date(parseInt(x)),"%m-%d")+"="+y);//     item.series.label + " of " + x + " = " + y
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
  
  
        
   
    


