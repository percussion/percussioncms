/* 
 * @author  Copyright 2007 Shane O Sullivan (shaneosullivan1@gmail.com)
 * @license Licensed under the Academic Free License 3.0 http://www.opensource.org/licenses/afl-3.0.php
*/
dojo.provide("sos.charting.events");
dojo.require("dojo.event.browser");
dojo.require("dojo.event.topic");
dojo.require("dojo.charting.*");

sos.charting.events = {
  addMouseOverListener: function(chart, /*Optional*/plots) {
  	if(!dojo.render.svg.capable){
		return null;
	}
  	
	var topicId = chart.node.getAttribute("id") +"/mouseover";
	
	sos.charting.events._addListener("onmouseover", chart, topicId, plots);
	return topicId;
  },
  
  addMouseOutListener: function(chart, /*Optional*/plots) {
  	if(!dojo.render.svg.capable){
		return null;
	}
	var topicId = chart.node.getAttribute("id") +"/mouseout";
	
	sos.charting.events._addListener("onmouseout", chart, topicId, plots);
	
	return topicId;
  },
  
  _addListener: function(type, chart, topicId, plots){  	
  	
	plots = plots ? plots : sos.charting.events._getPlotAreas(chart);
	var yConvert = sos.charting.events._getChartAxisConversion(chart);
  	
  	for(var i = 0; i< plots.length; i++)
	{
		if(typeof(plots[i].hasListener) == "undefined"){
			plots[i].hasListener = {};
		}
		if(!plots[i].hasListener[type]){
			plots[i].hasListener[type] = true;
		
			dojo.event.browser.addListener(plots[i], type,function(evt){
				var packet = {};
				if(evt.target.tagName == "path")
				{
				  packet['title'] = evt.target.getAttribute("title");
				}
				if(evt.target.tagName == "circle")
				{
					packet['title'] = evt.target.parentNode.firstChild.getAttribute("title");
					packet.value = yConvert.baseDisplay + (yConvert.base - evt.target.cy.baseVal.value)/yConvert.ratio;
				}
				packet.event = evt;
				dojo.event.topic.publish(topicId,packet);				
			});
		}
	}
  },
  
  _getPlotAreas: function(chart){
  	if(!dojo.render.svg.capable){
		return null;
	}
  	if(!chart || !chart.node)
	{
		dojo.debug("No chart specified, or chart with no node set");
		return null;
	}
	
	var gfce = dojo.html.getFirstChildElement;
	
	var svg = gfce(gfce(chart.node,"div"),"svg");
	var plots = [];
	
	var child = svg.firstChild;
	while(child)
	{
		if(child.tagName == "g" && child.id && child.id.indexOf("-plots") == child.id.length - 6)
		{
			plots[plots.length] = child;
		}
		child = dojo.html.getNextSiblingElement(child);
	}
	return plots;
  },
  
  _getChartAxisConversion: function(chart){
  	if(chart['YCONVERT']){
		return chart['YCONVERT'];
	}
	var nodes = chart.plotAreas[0].plotArea.plots[0].axisY.nodes.labels.childNodes;
	
	var prevDisp, prevYVal;
	var display = 0, yVal = 0;
	var totalDisp = 0, totalYVal = 0;
	
	for(var i = 0; i < 10 && i < nodes.length; i++)
	{
		display = Number(nodes[i].firstChild.nodeValue);
		yVal = Number(nodes[i].getAttribute("y"));
		
		if(prevDisp && prevYVal)
		{
			totalDisp += display - prevDisp;
			totalYVal += prevYVal - yVal;
		}
		prevDisp = display;
		prevYVal = yVal;
	}	
	
	var lowest = Number(nodes[0].getAttribute("y"));
	var lowestDisplay = Number(nodes[0].firstChild.nodeValue);
	
	chart['YCONVERT'] = {ratio: totalYVal / totalDisp,base:lowest, baseDisplay: lowestDisplay};
	
	return chart['YCONVERT'];
  }
  
  
};
