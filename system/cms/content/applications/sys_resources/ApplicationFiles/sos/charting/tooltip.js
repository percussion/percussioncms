/* 
 * @author  Copyright 2007 Shane O Sullivan (shaneosullivan1@gmail.com)
 * @license Licensed under the Academic Free License 3.0 http://www.opensource.org/licenses/afl-3.0.php
*/
dojo.provide("sos.charting.tooltip");
dojo.require("sos.charting.events");
dojo.require("dojo.event.topic");
dojo.require("dojo.html.display");

sos.charting.tooltip = {
	addTooltip: function(chart, /*?optional*/node, /*?optional*/formatter){
		if(dojo.render.svg.capable){
			return sos.charting.tooltip._addTooltipSVG(chart, node, formatter);
		} else if(dojo.render.vml.capable) {
			return sos.charting.tooltip._addTooltipVML(chart, node, formatter);
		}		
	},
	
	_addTooltipSVG: function(chart, /*?optional*/node, /*?optional*/formatter){
		var circles = chart.node.getElementsByTagName("circle");
		var yConvert = sos.charting.events._getChartAxisConversion(chart);
		
		var val, title;
		for(var i = 0; i < circles.length; i++)
		{
			val = yConvert.baseDisplay + (yConvert.base - circles[i].getAttribute("cy"))/yConvert.ratio;
			title = circles[i].parentNode.firstChild.getAttribute("title");
			circles[i].setAttribute("title",title + ": " + Math.round(val));			
		}
	},
	
	_addTooltipVML: function(chart, /*?optional*/node, /*?optional*/formatter){
		dojo.debug("sos.charting.tooltip.VML: not implemented")
		return null;
	}
};