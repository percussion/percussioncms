/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

(function($){

    $.PercEffectivenessGadget = {
        renderGraph : renderGraph
    };
    
    // grab necessary Perc APIs
    var PercActivityService  = percJQuery.PercActivityService;
    var PercServiceUtils = percJQuery.PercServiceUtils;
    var perc_utils       = percJQuery.perc_utils;
    var perc_paths       = percJQuery.perc_paths;
    var isLargeColumn = true;
    var path;
    var durType;
    var durValue;
    var usg;
    var thresh;
    var busy = false;

    function renderGraph(site, durationType, durationValue, usage, threshold) {
        if(site == null)
            site = "";
	
        path = site;
        durType = durationType;
        durValue = durationValue;
        usg = usage;
        thresh = threshold;

        // get the data and then pass it to createDataTable to create the table
        PercActivityService
            .getContentEffectiveness(
                perc_paths.SITES_ROOT+'/'+site,
                durationType,
                durationValue,
				usage,
				threshold,
                function (status, data) 
                {
                    if(status == PercServiceUtils.STATUS_SUCCESS) 
                    {
                        $("#perc-chart").html('');
                        generateBreadcrumbs (path); 
                        createGraph(data, threshold);
                    } 
                    else 
                    {
                        $("#perc-chart-wrapper").hide();
                        $("#perc-yaxis-label").hide();
                        $("#perc-chart-breadcrumb").hide();
                        $("#perc-chart-error").append('<div  class = "effectiveness-error-message" >' + data + '</div>');               
                    }
                    miniMsg.dismissMessage(loadingMsg);
                    busy = false;
                }
            );
    }        
    function createGraph(data, threshold) {

		//Check if returned array is empty or not.
		var graphData = data.Effectiveness.length > 0;	
		var threshold = threshold;
		if(graphData){
		$("#perc-yaxis-label").show();
		$("#perc-chart-error").hide();
		$("#perc-chart-breadcrumb").show();
		$("#perc-chart-breadcrumb").show();
		$("#perc-chart-wrapper").show();	
		
		//If the gadget is in first column then we have to render it as large 
		isLargeColumn = gadgets.window.getDashboardColumn() == 1;
		var effectiveness = data.Effectiveness;
		var itemCount = effectiveness.length;

		// Calculate if we need scroll bar
		
		if(itemCount > 5){
			wrapperHeight = (itemCount-5) * 20 + 100;
			$('#perc-chart').css('height', wrapperHeight + '%');
			
		}
		else{
			wrapperHeight = 98;
			$('#perc-chart').css('height', wrapperHeight + '%');
		}
		
		var highestValue = (effectiveness[0].effectiveness);
		
		if(threshold > highestValue){
			highestValue = threshold;
		}
		var lowestValue =  (effectiveness[itemCount-1].effectiveness);
		
		if (lowestValue > threshold){
			lowestValue = threshold;
		}
		if( lowestValue <= 0){
			var totalValue = highestValue-lowestValue;	
		}
		else if(lowestValue == highestValue){
			var totalValue = highestValue;	
		}
		else {
			var totalValue = highestValue+lowestValue;	
		}	
		// Round the step value to uper integer. 
			totalValue = Math.ceil(totalValue/10 + totalValue%10);
			
		//	Make the xaxis value to be multiple of 5 or 10
		
		if( totalValue >= 100){
			totalValue = totalValue + 10 - totalValue%10;				
		}
		else if(totalValue < 100){

		totalValue = totalValue + 5 - totalValue%5;	
		}	
				
		var negativeValue = [];
			negativeValue[0] = 0;
		var positiveValue = [];
			positiveValue[0] = 0;
		if(lowestValue < 0){
		
			for (i=1; negativeValue[i-1] >= lowestValue; i++)
			{
				negativeValue[i] = negativeValue[i-1] - totalValue;
			}
			 negativeValue = negativeValue.reverse();
			for (i=1; positiveValue[i-1] < highestValue; i++)
			{
				positiveValue[i] = positiveValue[i-1] + totalValue;
			}
			var dataRange = negativeValue.concat(positiveValue);
		}
		
		else {
			for (i=1; positiveValue[i-1] < highestValue; i++)
			{
				positiveValue[i] = positiveValue[i-1] + totalValue;
				dataRange = positiveValue;
			}
		}
		key = [];
		value = [];
		line1 = [];
		color = [];
		line2 = [[threshold, 0],[threshold, 1]];
		var plotIdx = 0;
		for(i = itemCount - 1; i >= 0; i--) 
		{
			var item = effectiveness[i];
			key[plotIdx] = item.name;
			value[plotIdx] = item.effectiveness;
			line1[plotIdx] = [value[plotIdx], key[plotIdx]];
			color[plotIdx] = value[plotIdx] >= threshold? "#0e7922" : "#982b23";
			plotIdx++;
		}

		// Plot the graph
			$.jqplot.config.enablePlugins = true;
			$.jqplot.eventListenerHooks.push(['jqplotClick', drillDownClickHandler]);
			plot1 = $.jqplot('perc-chart', [line1,line2],{
			seriesColors: color, 
			legend: {show:false, location: 'nw'},
			seriesDefaults:{showMarker:false, fill:false},
			series:[	
				    {yaxis:'yaxis', showHighlight:false, showMarker:false, renderer:$.jqplot.BarRenderer, fillToZero:true, rendererOptions:{ varyBarColor: true, highlightMouseOver:false, barWidth: 25, shadowDepth: 3, shadowOffset: 2, barDirection: 'horizontal', barPadding:10, barMargin:10} },							
				    {yaxis:'y2axis',  color: '#616D7E', showMarker:true}
					],
			
			axes:{
				  yaxis:{
						show:false,
						renderer:$.jqplot.CategoryAxisRenderer,
						showTickMarks: false,
						showHighlight:false,
						highlighter:{show: false}
						 },
				  xaxis: {
						 show:false,
						 showTickMarks: false,
						 tickOptions:{formatString:'%d'},
						 label:'Gains per page',
						 pad:0,
						 ticks: dataRange
						 },					 
				  y2axis:{
						show:false,
						showTickMarks: false,
						showMarker:true,
						tickOptions:{showGridline : false,showMark:false},
						min:0,
						max:1,
						label:'Threshold'
					}					
				},				
				highlighter: {tooltipLocation:'sw', 
							  tooltipAxes: 'x', 
							  formatString:'<table class="jqplot-highlighter"><tr><td>Goal:</td><td>%s</td></tr></table>'
							},

				 grid:{
					  drawGridLines: false,        
					  gridLineColor: '#cccccc',    
					  background: '#FFFDF6',      
					  borderColor: '#999999',     
					  borderWidth: 0,           
					  shadow: false,
					  renderer: $.jqplot.CanvasGridRenderer,
					  rendererOptions: {drawGridLines: false}  		
					}
				});
		}
		else{
			$("#perc-chart-wrapper").hide();
			$("#perc-chart-error").show();
			$("#perc-yaxis-label").hide();
			breadcrumbItems = [];
			breadcrumbItems = path.split("/");
			if(breadcrumbItems[0]==""){
				$("#perc-chart-breadcrumb").hide();
			}
			var errorMessagePresence = $(".effectiveness-error-message").length;
			if(!errorMessagePresence){
				$("#perc-chart-error").append('<div class = "effectiveness-error-message">No Data Found</div>');
			}	
		}
		
		
	// Fix the position for X-axis label	
		var xaxisPos = $(".jqplot-yaxis").width() + 11;
		$(".jqplot-xaxis-label").css('left', xaxisPos).css('font-size', '12px');
	//Referesh the xais and yaxis values to have it display properly in IE.	
		$(".jqplot-yaxis-tick").css('display', 'none');
		$(".jqplot-yaxis-tick").css('display', 'block');
		$(".jqplot-xaxis").css('display', 'none');
		$(".jqplot-xaxis").css('display', 'block');			
	}

	
	
	/**
	 * Generate the breadcrumbs based on Path  value
	 */

	function generateBreadcrumbs (path){
		$("#perc-chart-breadcrumb-list").html("");
		breadcrumbItems = [];
		breadcrumbItems = path.split("/");
		var siteName = prefs.getString(SITE_NAME);
		if (siteName == "@all"){
             $("#perc-chart-breadcrumb-list").append(' <li atitle = "" ><span id = "perc-breadcrumb-items" atitle = "">All Sites</span></li><span class = "perc-breadcrumb-separator" >&nbsp;>&nbsp;</span>');			 	
		}
		var pathValue = breadcrumbItems[0];
		
		$("#perc-yaxis-label").html("");
		if(breadcrumbItems[0] == ""){
			$(".perc-breadcrumb-separator").hide();
			$("#perc-yaxis-label").append("Sites");
		}
		else{
			$("#perc-yaxis-label").append("Sections");
		}


		$("#perc-chart-breadcrumb-list").append('<li atitle = "'+ breadcrumbItems[0] + '" ><span id = "perc-breadcrumb-items" atitle = "'+ breadcrumbItems[0] +'">' +breadcrumbItems[0]+'</span></li>');
		
		for (i=1; i< breadcrumbItems.length; i++)
		{
			 pathValue += '/' + breadcrumbItems[i];
			$("#perc-chart-breadcrumb-list").append(' <span>>&nbsp;</span><li atitle = "'+ pathValue + '" ><span id = "perc-breadcrumb-items" atitle = "'+ pathValue +'">' +breadcrumbItems[i]+'</span></li>');	
		}
	}
    /**
     * Click handler for drill down operations.
     */
    function drillDownClickHandler(ev, gridpos, datapos, neighbor, plot) {
		var pathService		 = percJQuery.PercPathService;
        var items = plot.data;
        var itemLength = items.length;
        var numberOfBars = 0;
        if (itemLength > 0) {
            numberOfBars = items[0].length;
        }
        if (numberOfBars == 0)
            return;
        var columnLoc = getColumnLocation(plot,gridpos,numberOfBars);
        var parentFolder = (path.indexOf("/") == (path.length - 1)) ? path : path + '/';
        var relativePath = parentFolder + items[0][columnLoc][1];
        renderGraphAndPersist(relativePath);
    }

	 /**
     * Returns the column based on the location of the current click.
     */
    function getColumnLocation(plot, gridpos, numberOfBars)   
    {   
       var insideheight = plot.grid._height;   
       var colheight = insideheight / numberOfBars;   
       var column = parseInt(gridpos.y / colheight);   
       return (numberOfBars - 1) - column;
    }

    /**
     * Renders the graph for the given relative path.  If the path does
     * not exist, the graph for the next existing path will be rendered.
     * The path will be persisted using the drill down key.
     */
    function renderGraphAndPersist(relativePath){
        if (busy) return;
        var pathService = percJQuery.PercPathService;
        loadingMsg = miniMsg.createStaticMessage("Loading...");
        busy = true;
        pathService.getLastExistingPath("/Sites/"+relativePath, function(status, result){	
            if(result == ""){
               path = "";
            }
            else{
               path = result;
            }
            renderGraph(path,durType,durValue,usg,thresh);
            PercMetadataService.save(drillDownKey, path, function(){});
        });
    }
		
	/**
     *Associate click event with breadcrumb item/s
     */
	$('#perc-breadcrumb-items').live('click', function()
    {
		var parentFolder = (path.indexOf("/") == (path.length - 1)) ? path : path + '/';	
		var relativePath  = $(this).attr("atitle");
		$("li").each(function(){
		
			var itemLength = $(this).attr("atitle").length;
			if(itemLength  > relativePath.length){
				$(this).hide();
			}
		});
		renderGraphAndPersist(relativePath);
	});
	
})(jQuery);
