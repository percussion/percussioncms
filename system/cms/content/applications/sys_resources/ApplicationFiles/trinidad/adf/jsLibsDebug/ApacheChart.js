/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

/////////////////////////////////////////////
// Some utility functions
/////////////////////////////////////////////

/**
* Base Class that provides inheritence for charting subsystem
* @constructor 
*/
function ApacheChartObj()
{
  this.Init();
}

ApacheChartObj.prototype = new Object();
ApacheChartObj.prototype.constructor = ApacheChartObj;
ApacheChartObj._tempConstructor = function(){}

ApacheChartObj.Inherit = function(baseClass, extendingClass)
{
  var tempConstructor = ApacheChartObj._tempConstructor;

  tempConstructor.prototype = baseClass.prototype;
  extendingClass.prototype = new tempConstructor();

  extendingClass.prototype.constructor = extendingClass;
  extendingClass.superclass = baseClass.prototype;
}

ApacheChartObj.prototype.createCallback = function(func)
{
  // create a function that sets up "this" and delegates all of the parameters
  // to the passed in function
  var proxyFunction = new Function(
    "var f=arguments.callee; return f._func.apply(f._owner, arguments);");

  // attach ourselves as "this" to the created function
  proxyFunction._owner = this;

  // attach function to delegate to
  proxyFunction._func = func;

  return proxyFunction;
}

/**
* Asserts arg is true; else throws error with msg.
*/
ApacheChartObj.Assert = function(arg, msg)
{
  if (!arg)
  {
    throw new Error(msg);
  }
}


/**
* StringBuffer utility used by the charting
*/
function ApacheChartBuffer(size)
{
  this.maxStreamLength = document.all?5000:100000;	
  this.data = new Array(size?size:100);
  this.iStr = 0;
}

ApacheChartBuffer.prototype.append = function(obj)
{
  this.data[this.iStr++] = obj;
  if (this.data.length > this.maxStreamLength)
  {
    this.data = [this.data.join("")];
    this.data.length = 100;
    this.iStr = 1;
  }
  return this;  
}

ApacheChartBuffer.prototype.toString = function()
{
  return this.data.join("");
}

////////////////////////////////////////////////////////////////////
// Abstract Data Structure representing the model for drawing a chart control
////////////////////////////////////////////////////////////////////
function ApacheChartModel(seriesLabels, groupLabels, yValues, xValues, seriesColors)
{
  // An array representing series labels
  this._seriesLabels = seriesLabels;
  // An array representing Group labels
  this._groupLabels = groupLabels;
  // A 2D array representing values for Y axis
  this._yValues = yValues;
  // A 2D array representing values for X axis.
  // The x axis values are used only for scatter plots/XYline 
  this._xValues = xValues;
  // The array of strings with colors. Used for display of the series
  this._seriesColors = seriesColors;
  
  // the maximum value used to display the y-axis. 
  // Default is 120% of maximum of the yValues
  //this._maxYValue = undefined;
  
  // the minimum value used to display the y-axis. Default is 80% of minimum of values
  //this._minYValue = undefined;

  // the maximum value used to display the X-axis. 
  // Default is 120% of maximum of the xValues
  //this._maxXValue = undefined;
  
  // the minimum value used to display the y-axis. Default is 80% of minimum of values 
  //this._minXValue = undefined;
    
  // The title for the graph
  //this._title = undefined;
  
  // The sub-title for the graph
  //this._subTitle = undefined;
  
  // The foot node for the graph
  //this._footNote = undefined;
}

ApacheChartModel.prototype.getSeriesLabels = function()
{
  return this._seriesLabels; 
}

ApacheChartModel.prototype.getGroupLabels = function()
{
  return this._groupLabels;
}

ApacheChartModel.prototype.getSeriesColors = function()
{
  return this._seriesColors; 
}

ApacheChartModel.prototype.getXValues = function()
{
  return this._xValues; 
}

ApacheChartModel.prototype.getYValues = function()
{
  return this._yValues; 
}

ApacheChartModel.prototype.setMaxYValue = function(maxYValue)
{
  this._maxYValue = maxYValue; 
}

ApacheChartModel.prototype.getMaxYValue = function()
{
  return this._maxYValue; 
}

ApacheChartModel.prototype.setMinYValue = function(minYValue)
{
  this._minYValue = minYValue; 
}

ApacheChartModel.prototype.getMinYValue = function()
{
  return this._minYValue; 
}

ApacheChartModel.prototype.setMaxXValue = function(maxXValue)
{
  this._maxXValue = maxXValue; 
}

ApacheChartModel.prototype.getMaxXValue = function()
{
  return this._maxXValue; 
}

ApacheChartModel.prototype.setMinXValue = function(minXValue)
{
  this._minXValue = minXValue; 
}

ApacheChartModel.prototype.getMinXValue = function()
{
  return this._minXValue; 
}

ApacheChartModel.prototype.setTitle = function(title)
{
  this._title = title; 
}

ApacheChartModel.prototype.getTitle = function()
{
  return this._title; 
}

ApacheChartModel.prototype.setSubTitle = function(subTitle)
{
  this._subTitle = subTitle; 
}

ApacheChartModel.prototype.getSubTitle = function()
{
  return this._subTitle; 
}

ApacheChartModel.prototype.setFootNote = function(footNote)
{
  this._footNote = footNote; 
}

ApacheChartModel.prototype.getFootNote = function()
{
  return this._footNote; 
}

////////////////////////////////////////////////////////////////////
// A Chart Event that is triggered in response to a user click
// This event can be marshalled to the server if integration is done
// with a J2EE platform platform
////////////////////////////////////////////////////////////////////
function ApacheChartEvent(seriesIndices, yValueIndices, yValues, xValues)
{
  this._seriesIndices = seriesIndices;
  this._yValueIndices = yValueIndices;
  this._yValues = yValues;
  this._xValues = xValues;
}

ApacheChartEvent.prototype.getSeriesIndices = function()
{
  return this._seriesIndices;
}

ApacheChartEvent.prototype.getYValueIndices = function()
{
  return this._yValueIndices;
}

ApacheChartEvent.prototype.getYValues = function()
{
  return this._yValues;
}

ApacheChartEvent.prototype.getXValues = function()
{
  return this._xValues;
}

ApacheChartEvent.prototype.toString = function()
{
  var sb = new ApacheChartBuffer();
  if(this._seriesIndices)
    sb.append("seriesIndices = "+ this._seriesIndices.join(","));
  if(this._yValueIndices)
    sb.append("\yValueIndices = "+ this._yValueIndices.join(","));
  sb.append("\nyValues = "+ this._yValues.join(","));
  if(this._xValues)
    sb.append("\nxValues = "+ this._xValues.join(","));
  
  return sb.toString();
}

ApacheChartEvent.prototype.marshall = function()
{
  var value = new Array();
  if(this._seriesIndices)
    value.push("seriesIndices\t"+this._seriesIndices.join("\t"));
  if(this._yValueIndices)
    value.push("yValueIndices\t"+this._yValueIndices.join("\t"));
  value.push("yValues\t"+this._yValues.join("\t"));
  if(this._xValues)
    value.push("xValues\t"+this._xValues.join("\t"));
  
  return value.join("$adf$");
} 
////////////////////////////////////////////////////////////////////
// Abstract Base Class for rendering a Chart control
////////////////////////////////////////////////////////////////////
function ApacheChart(type, model, svgEmbedId, isPerspective, legendPosition)
{
  this.Init(type, model, svgEmbedId, isPerspective, legendPosition);
}

// Hack for IE. The SVG has to be loaded externally otherwise,
// it needs to be activated by clicking
ApacheChart.createSVG = function(containerId, 
  svgEmbedId, sourceUrl, inlineStyle, styleClass)
{
  var svgContainer = document.getElementById(containerId);
  var embed = document.createElement("embed");
  var agent = window._agent;
  if(agent && agent.isIE)
  {
    var semiColIndex = sourceUrl.indexOf(";");
    if(semiColIndex!=-1)
      sourceUrl = sourceUrl.substr(0,semiColIndex);
  }
  embed.setAttribute("src",sourceUrl);
  embed.setAttribute("id",svgEmbedId);
  embed.setAttribute("wmode","transparent");
  embed.setAttribute("type","image/svg+xml"); 
  
  if(agent && agent.isOpera)
  {
    // opera does not like 100% width and 100% height.
    var style = document.defaultView.getComputedStyle(svgContainer, null);
    var embedStyle = embed.style;
    embedStyle.width = style.width;
    embedStyle.height = style.height;
  }
  else
  {
    embed.style.cssText = inlineStyle;
  }
  if(styleClass)
  {
    embed.className = styleClass;
  }
  svgContainer.appendChild(embed);
}

/**
 * Method to detect if Adobe ActiveX controll is initialized on the machine
 */
ApacheChart.isASVInstalled = function()
{
  try{
      var asv = new ActiveXObject("Adobe.SVGCtl");
      return true;
  }
  catch(e){
  }
  return false;
}
ApacheChartObj.Inherit(ApacheChartObj, ApacheChart);

ApacheChart.prototype.Init = function(type, model, svgEmbedId, 
  isPerspective, legendPosition)
{  
  this._type = type;
  this._model = model;
  this._svgEmbedId = svgEmbedId;
  this._margins = {left:2,right:2,top:2,bottom:2};
  // By default the graphs are drawn with a perspective(2.5D)
  this._isPerspective = isPerspective;
  this._legendPosition = legendPosition;
  //this._rootElement = undefined;
  this._toolTip = null;
  this._toolTipVisible = false;
  //this._svgDoc = undefined;
  //this._width = undefined;
  //this._height = undefined;
  //this._vLabelContainer = undefined;
  //this._hLabelContainer = undefined;
  
  // By default the graph animates for 1.5 s
  this._animDuration = 1500; 
  this._dataElems = [];
  this._labelElems = [];
  this._gridElems = [];

  // The group labels
  this._groupLabelElems = [];
  
  // The number of Major Line sections to draw on y axis
  this._yMajorGridCount = 8;
  // The number of Minor Line section to draw on y axis
  this._yMinorGridCount = -1;

  // The number of Major Line sections to draw on x axis
  this._xMajorGridCount = -1;
  
  // Controls if the tooltips are displayed or not
  this._tooltipsVisible = true;
        
  // By default we will use gradients
  this._gradientsUsed = true;

  // The maximum precision of the numbers displayed on yaxis/tooltips
  this._maxPrecision = 0;
  
  // The decimal separator
  this._decimalSep = null;

  // The form from which we will submit
  this._formName = null;
  this._partialSubmit = true;
      
  this._svgCheckTotal = 0;
  this._errorTextNode = null;
  this._isIE = false;
  if(window._agent) // use trinidad agent
    this._isIE = _agent.isIE;
    
  if(this._isIE)
    this._errorHtml = "<H4>Unable to load SVG plugin. Please install the plugin from <a href='#' onclick=\"window.open('http://www.adobe.com/svg/viewer/install/main.html')\">Adobe</a><H4>";
  else
    this._errorHtml = "<H4>This component needs an SVG enabled browser like Internet Explorer, Firefox 1.5+ or Opera 9.0+<H4>";
  
  this._statusHtml = "<H4>Please Wait. Attempting to load SVG document...</H4>";
  this.ComputeMinMaxValues();
}

/**
 * Error text to be displayed in the container of the embed, 
 * in case the svg document fails to load. 
 * NOTE: the error text can be customized by browser for e.g. in IE it can contain
 * the link to SVG plugin download
 */
ApacheChart.prototype.setErrorHtml = function(errorHtml)
{
  this._errorHtml = errorHtml;
}

/**
 * Status text that is displayed when the SVG document is taking a while to load
 */
ApacheChart.prototype.setStatusHtml = function(statusHtml)
{
  this._statusHtml = statusHtml;
}

ApacheChart.prototype.setYMajorGridLineCount = function(count)
{
  // number of sections is 1 greater than the lines
  this._yMajorGridCount = count>0?count+1:count; 
}


ApacheChart.prototype.setYMinorGridLineCount = function(count)
{
  // number of sections is 1 greater than the lines
  this._yMinorGridCount = count>0?count+1:count; 
}

ApacheChart.prototype.setXMajorGridLineCount = function(count)
{
  // number of sections is 1 greater than the lines
  this._xMajorGridCount = count>0?count+1:count; 
}


ApacheChart.prototype.setAnimationDuration = function(dur)
{
  this._animDuration = dur;
}

ApacheChart.prototype.setGradientsUsed = function(gradient)
{
  this._gradientsUsed = gradient;
}

ApacheChart.prototype.setMaxPrecision = function(precision)
{
  this._maxPrecision = precision;
}

ApacheChart.prototype.setFormName = function(formName)
{
  this._formName = formName;
}

ApacheChart.prototype.setPartialSubmit = function(partial)
{
  this._partialSubmit = partial;
}

ApacheChart.prototype.setTooltipsVisible = function(visible)
{
  this._tooltipsVisible = visible;
}

ApacheChart.prototype.getToolTip = function()
{
  return this._toolTip;
}

ApacheChart.prototype.setToolTip = function(tt)
{
  this._toolTip = tt;
}

ApacheChart.prototype.ComputeMinMaxValues = function()
{
  var model = this._model, yValues = model.getYValues(), xValues = model.getXValues(),
      maxYValue = model.getMaxYValue(), maxXValue = model.getMaxXValue(),
      minYValue = model.getMinYValue(), minXValue = model.getMinXValue(),
      seriesLabels = model.getSeriesLabels();
  if(yValues != null && (maxYValue == null || minYValue == null))
  {
    var minMax = this._computeAxisMinMaxValues(yValues, seriesLabels.length);
    if(maxYValue == null)
      model.setMaxYValue(minMax.max);
    if(minYValue == null)
      model.setMinYValue(minMax.min);
  }
  if(xValues != null && (maxXValue == null || minXValue == null))
  {
    var minMax = this._computeAxisMinMaxValues(xValues, seriesLabels.length);
    if(maxXValue == null)
      model.setMaxXValue(minMax.max);
    if(minXValue == null)
      model.setMinXValue(minMax.min);
  }
}

ApacheChart.prototype._computeAxisMinMaxValues = function(values, seriesSize)
{
  var stackedTotal, value, maxValue = Number.NEGATIVE_INFINITY, minValue = Number.POSITIVE_INFINITY, 
      type = this._type, isStacked = false, groupsCount = values.length;
  
  if(type == ApacheChart.TYPE_VBAR_STACKED || type == ApacheChart.TYPE_HBAR_STACKED || 
     type == ApacheChart.TYPE_AREA_STACKED)
  {
    isStacked = true;
  }
  for (var i = 0; i < groupsCount; ++i)
  {
    stackedTotal = 0;
    for (var j = 0; j < seriesSize; ++j)
    {
      value = values[i][j];
      if (isStacked)
        stackedTotal += value;
      else
      {
        maxValue = Math.max(maxValue, value);
        minValue = Math.min(minValue, value);
      }
    }
    if (isStacked)
    {
      maxValue = Math.max(maxValue, stackedTotal);
      minValue = Math.min(minValue, stackedTotal);
    }
  }
  var maxMult = maxValue>0?ApacheChart._MAX_MULTIPLIER:ApacheChart._MIN_MULTIPLIER,
      minMult = minValue>0?ApacheChart._MIN_MULTIPLIER:ApacheChart._MAX_MULTIPLIER;
  return {max: maxValue*maxMult, min: minValue*minMult};
}

ApacheChart.TYPE_VBAR = 1;
ApacheChart.TYPE_HBAR = 2;
ApacheChart.TYPE_VBAR_STACKED = 3;
ApacheChart.TYPE_HBAR_STACKED = 4;
ApacheChart.TYPE_PIE = 5;
ApacheChart.TYPE_AREA = 6;
ApacheChart.TYPE_AREA_STACKED = 7;
ApacheChart.TYPE_LINE = 8;
ApacheChart.TYPE_BAR_LINE_COMBO = 9;
ApacheChart.TYPE_XYLINE = 10;
ApacheChart.TYPE_SCATTER_PLOT = 11;
ApacheChart.TYPE_RADAR = 12;
ApacheChart.TYPE_RADAR_AREA = 13;
ApacheChart.TYPE_FUNNEL = 14;
ApacheChart.CIRCULAR_GAUGE = 15;
ApacheChart.SEMI_CIRCULAR_GAUGE = 16;

ApacheChart.LEGEND_LOCATION_NONE = "none";
ApacheChart.LEGEND_LOCATION_TOP = "top";
ApacheChart.LEGEND_LOCATION_END = "end";
ApacheChart.LEGEND_LOCATION_BOTTOM = "bottom";
ApacheChart.LEGEND_LOCATION_START = "start";

ApacheChart._MAX_MULTIPLIER = 1.2;
ApacheChart._MIN_MULTIPLIER = .8;
ApacheChart._XOFFSET_PERSPECTIVE = 10;
ApacheChart._YOFFSET_PERSPECTIVE = 5;
// margin generally used around text
ApacheChart._TEXT_MARGIN = 4;
ApacheChart._DEFAULT_STOP_OPACITY = .9;
ApacheChart._BORDER_SIZE = 6;
// Animate at 15 fps
ApacheChart._ANIMATE_INTERVAL = 66;

ApacheChart._SVGCHECK_INTERVAL = 100;
ApacheChart._SVGCHECK_STATUS_LIMIT = 5000;
ApacheChart._SVGCHECK_MAX_LIMIT = 20000;

ApacheChart.createChart = function(
  type, 
  model, 
  svgEmbedId, 
  isPerspective, 
  legendPosition)
{
  var chart = null;
  if(type == this.TYPE_VBAR || type == this.TYPE_VBAR_STACKED || type == this.TYPE_BAR_LINE_COMBO)
  {
    chart = new ApacheBarChart(type, model, svgEmbedId, 
                         isPerspective, legendPosition);
  }
  else if(type == this.TYPE_HBAR || type == this.TYPE_HBAR_STACKED)
  {
    chart = new ApacheHBarChart(type, model, svgEmbedId, 
                          isPerspective, legendPosition);
  }
  else if(type == this.TYPE_PIE)
  {
    chart = new ApachePieChart(type, model, svgEmbedId, 
                         isPerspective, legendPosition);
  }
  else if(type == this.TYPE_AREA || type == this.TYPE_AREA_STACKED)
  {
    chart = new ApacheAreaChart(type, model, svgEmbedId, 
                          isPerspective, legendPosition);
  }
  else if(type == this.TYPE_LINE)
  {
    chart = new ApacheLineChart(type, model, svgEmbedId, 
                          isPerspective, legendPosition);
  }
  else if(type == this.TYPE_SCATTER_PLOT)
  {
    chart = new ApacheScatterPlotChart(type, model, svgEmbedId, 
                                 isPerspective, legendPosition);
  }
  else if(type == this.TYPE_XYLINE)
  {
    chart = new ApacheXYLineChart(type, model, svgEmbedId, 
                           isPerspective, legendPosition);
  }
  else if(type == this.TYPE_RADAR || type == this.TYPE_RADAR_AREA)
  {
    chart = new ApacheRadarChart(type, model, svgEmbedId, 
                           isPerspective, legendPosition);
  }
  else if(type == this.TYPE_FUNNEL)
  {
    chart = new ApacheFunnelChart(type, model, svgEmbedId, 
                            isPerspective, legendPosition);
  }
  else if(type == this.SEMI_CIRCULAR_GAUGE)
  {
    chart = new ApacheSemiGaugeChart(type, model, svgEmbedId, 
                            isPerspective, legendPosition);
  }
  else if(type == this.CIRCULAR_GAUGE)
  {
    chart = new ApacheGaugeChart(type, model, svgEmbedId, 
                            isPerspective, legendPosition);
  }
  return chart;
}

ApacheChart.prototype.setPerspective = function(isPerpective)
{
  this._isPerspective = isPerpective;
}

ApacheChart.prototype.clear = function()
{
  var rootElem = this._rootElement;
  var childNode = rootElem.firstChild;
  while (childNode)
  {
    rootElem.removeChild(childNode);
    childNode = rootElem.firstChild;
  }
}

ApacheChart.prototype.draw = function()
{  
  if(!this._initDocument())
    return;
  	
	// Initialize our gradients if necessary
	if (this._gradientsUsed && !this._gradientsInitialized)
  {
    this.InitializeGradients();
    this._gradientsInitialized = true;
  }
  if(this._tooltipsVisible)
  {
    this.ShowToolTipCallback = this.createCallback(this.ShowToolTip);
    this.HideToolTipCallback = this.createCallback(this.HideToolTip);
  }
  this.ClickCallback = this.createCallback(this.Click);
  
  // Note the ordering is important. The grid takes the space after the title etc.
  this.DrawBorder();
  this.DrawTitles();
  // First just draw the label elements so that we can estimate the space requirements
  this.DrawGroupLabels();
	this.DrawYValueLabels();
	// Now adjust margins based on the labels
	this.AdjustMarginsForGroupLabels();
	this.AdjustMarginsForYLabels();
  // Now start drawing the graph so that it gobbles the left over space
  this.DrawLegend();
  this.LayoutGroupLabels();
	this.LayoutYValueLabels();
	this.DrawGrid();
	this.DrawChartData();
	this.Animate();
}

ApacheChart.prototype._initDocument = function()
{
  // Get hold of the svgDocument
  var svgEmbed = document.getElementById(this._svgEmbedId);

  var isIE = this._isIE;
  if(isIE && !ApacheChart.isASVInstalled())
  {
    this._displayErrorHtml(svgEmbed);
    return false;
  }
  try
  {
	  var svgDoc = svgEmbed.getSVGDocument();
	  this._rootElement = svgDoc.getElementById("chartRoot");
	  if(!this._rootElement) // make sure that the document is loaded
	    throw "not yet loaded";
	  this._svgDoc = svgDoc;
	  this._width = svgEmbed.clientWidth;
	  this._height = svgEmbed.clientHeight;
	  if(this._errorTextNode != null)
	  {
	    svgEmbed.parentNode.removeChild(this._errorTextNode);
		  svgEmbed.style.display = "";
	  }  
  }
  catch(e)
  {
    this._svgCheckTotal += ApacheChart._SVGCHECK_INTERVAL;
    if(this._svgCheckTotal > ApacheChart._SVGCHECK_MAX_LIMIT)
    {
      // We are out of our chances
      this._displayErrorHtml(svgEmbed);
      return false;
    }
    else if(null == this._errorTextNode &&
          this._svgCheckTotal > ApacheChart._SVGCHECK_STATUS_LIMIT)
    {
      // display a status message
      this._displayStatusHtml(svgEmbed);
    }

    if(!this._drawCallback)
      this._drawCallback = this.createCallback(this.draw);
    // Lets try again
    window.setTimeout(this._drawCallback, ApacheChart._SVGCHECK_INTERVAL);
    return false;
  }
  return true;
}

ApacheChart.prototype._displayStatusHtml = function(svgEmbed)
{
  var errorTextNode = this._errorTextNode = document.createElement("span");
	errorTextNode.innerHTML = this._statusHtml;
	svgEmbed.parentNode.insertBefore(errorTextNode, svgEmbed);
	svgEmbed.style.display = "none"; 
}

ApacheChart.prototype._displayErrorHtml = function(svgEmbed)
{
  if(this._errorTextNode)
  {
    this._errorTextNode.innerHTML = this._errorHtml;
    return;
  } 
  else
  {
    var errorTextNode = this._errorTextNode = document.createElement("span");
	  errorTextNode.innerHTML = this._errorHtml;
	  svgEmbed.parentNode.insertBefore(errorTextNode, svgEmbed);
	}
	svgEmbed.style.display = "none"; 
}

ApacheChart.prototype.DrawChartData = function()
{
  // no default implementation. Subclasses have to override this
}

ApacheChart.prototype.Animate = function()
{
  var animateDuration = this._animDuration;
  if(animateDuration > 0)
  {
    if(this._animCallback == null)
       this._animCallback = this.createCallback(this.DoAnimation);
    this._startTime = (new Date()).getTime();
    this._intervalId = window.setInterval(this._animCallback, ApacheChart._ANIMATE_INTERVAL);
  }
}

ApacheChart.prototype.DoAnimation = function()
{
  var animateDuration = this._animDuration;
  var diffTime = (new Date()).getTime() -  this._startTime;
  if(diffTime >= animateDuration)
  {
    window.clearInterval(this._intervalId);
    this.SetDataAnimStep(1);
    this.SetLabelsAnimStep(1);
    this.SetGridAnimStep(1);
    // we do not need the elements any more.
    delete this._dataElems;
    delete this._labelElems;
    delete this._gridElems;
  }
  else
  {
    var ratio = (diffTime)/animateDuration;
    this.SetDataAnimStep(ratio);
    this.SetLabelsAnimStep(ratio);
    this.SetGridAnimStep(ratio);
  }
}

ApacheChart.prototype.SetDataAnimStep = function(ratio)
{
  var animElems = this._dataElems, animCount = animElems.length;
  var margins = this._margins, animHorizontal = this.AnimAlongXAxis();

  // Default implementation is to make the elements appear from x axis or y axis 
  if(animHorizontal)
  {
    var marginLeft = margins.left;
    for(var i = 0; i < animCount; ++i)
    {
      var tx = (1-ratio)*marginLeft;
      animElems[i].setAttribute("transform", "translate("+tx+",0) scale("+ratio+",1)");
    }    
  }
  else
  {
    var marginBottom = margins.bottom, cy = (this._height - marginBottom);
    
    for(var i = 0; i < animCount; ++i)
    {
      var ty = (1-ratio)*cy;
      animElems[i].setAttribute("transform", "translate(0,"+ty+") scale(1,"+ratio+")");
    }
  }
}

ApacheChart.prototype.AnimAlongXAxis = function(ratio)
{
  return false;  
}

ApacheChart.prototype.SetLabelsAnimStep = function(ratio)
{
   var animElems = this._labelElems, animCount = animElems.length;
  // Default implementation is to make the elements fade in
  for(var i = 0; i < animCount; ++i)
  {
    animElems[i].setAttribute("fill-opacity", ratio);
  }
}

ApacheChart.prototype.SetGridAnimStep = function(ratio)
{
  var animElems = this._gridElems, animCount = animElems.length;
  var margins = this._margins, animHorizontal = this.AnimAlongXAxis();
  
  // Default implementation is to make the grid appear along the x axis or y axis
  if(animHorizontal)
  {
    var marginBottom = margins.bottom, cy = (this._height - marginBottom);
    
    // reverse the animation for horizontal chart
    for(var i = 0; i < animCount; ++i)
    {
      var ty = (1-ratio)*cy;
      animElems[i].setAttribute("transform", "translate(0,"+ty+") scale(1,"+ratio+")");
    }
  }
  else 
  {
    var marginLeft = margins.left;
    for(var i = 0; i < animCount; ++i)
    {
      var tx = (1-ratio)*marginLeft;
      animElems[i].setAttribute("transform", "translate("+tx+",0) scale("+ratio+",1)");
    }
  }
}

ApacheChart.prototype.InitializeGradients = function()
{
  var svgDoc = this._svgDoc, model = this._model, seriesColors = model.getSeriesColors(),
      seriesCount = model.getSeriesLabels().length;
  var gradients = svgDoc.getElementById("gradients");
  
  ApacheChartObj.Assert(gradients, "No Gradients element in the SVG document");
  var gradientElements = gradients.childNodes;
  ApacheChartObj.Assert(gradients.childNodes.length>1, "No Gradient Template in the SVG document");
  var gradientTemplate = gradients.childNodes[0], gradientElement;
   
  for (var i = 0; i< seriesCount; ++i)
  {
    gradientElement = svgDoc.getElementById("gradient"+i);
    if(gradientElement == null)
    {
      gradientElement = gradientTemplate.cloneNode(true);
      gradients.appendChild(gradientElement);
    }
    var childNode = gradientElement.firstChild;
    var stopIndex = 0;
    while (childNode)
    {
      if (childNode.nodeName == "stop")
      {
        var color = seriesColors[i];
        
        color = (stopIndex == 0)?color:this._getLighterColor(color);
        
        childNode.setAttribute("stop-color",color);
        this.SetStopOpacity(childNode);
        
        if(stopIndex>=1)
          break;
        stopIndex++;
      }
      childNode = childNode.nextSibling;
    }
  } 
}

ApacheChart.prototype.SetStopOpacity = function(stopNode)
{
  // no default implementation
  stopNode.setAttribute("stop-opacity", ApacheChart._DEFAULT_STOP_OPACITY);
}

ApacheChart.prototype._getLighterColor = function(color)
{
  if(color.indexOf("#") >=0 )
  {
    color = color.substr(1);
    var rVal = color.substr(0,2), gVal = color.substr(2,2), bVal = color.substr(4);
    color = "#"+this._getLighterNumberStr(rVal)+this._getLighterNumberStr(gVal)+
            this._getLighterNumberStr(bVal);
  }
  else
  {
    color = color.toLowerCase().replace(" ", "");
    color = color.substring(4, color.length-1);
    var arr = color.split(",");
    color = "#"+this._getLighterNumberStr(arr[0])+this._getLighterNumberStr(arr[1])+
        this._getLighterNumberStr(arr[2]);
  }
  return color;
}

ApacheChart.prototype._getLighterNumberStr = function(valStr)
{
  var val = Math.round(parseInt(valStr, 16)*1.7);
  if(val>255)
    val = 255;
  
  return this._to_hex(val);  
}

ApacheChart.prototype._to_hex = function(n)
{
  var digit_array = ApacheChart._digit_array;
  if(digit_array == null)
  {
    digit_array = ApacheChart._digit_array = 
          ['0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'];
  }
	var hex_result=''
	var the_start=true;
	for(var i=32;i>0;){
		i-=4;
		var one_digit=(n>>i)&0xf;
		if(!the_start||one_digit!=0){
			the_start=false;
			hex_result+=digit_array[one_digit];
		}
	}
	return ''+(hex_result==''?'0':hex_result);
}

ApacheChart.prototype.DrawBorder = function()
{
  var svgDoc = this._svgDoc, rootElem = this._rootElement;
  var rectElem = svgDoc.getElementById("borderPrototype").cloneNode(false);
  var borderSize = ApacheChart._BORDER_SIZE, stroke = borderSize/2;
  rectElem.setAttribute("x", 0);
  rectElem.setAttribute("y", 0);
  rectElem.setAttribute("rx", stroke);
  rectElem.setAttribute("ry", stroke);
  rectElem.setAttribute("width", this._width-stroke);
  rectElem.setAttribute("height", this._height-stroke);
  rectElem.setAttribute("stroke-width", stroke);
  rootElem.appendChild(rectElem);
  
  var margins = this._margins;
  margins.left += borderSize;
  margins.right += borderSize;
  margins.top += borderSize;
  margins.bottom += borderSize;
}

ApacheChart.prototype.DrawTitles = function()
{
  var model = this._model, title = model.getTitle(), 
      subTitle = model.getSubTitle(), footNote = model.getFootNote();
  if(title)
    this._drawTitleElem("titleTextPrototype", title, false);
  
  if(subTitle)
    this._drawTitleElem("subTitleTextPrototype", subTitle, false);
  
  if(footNote)
    this._drawTitleElem("footNoteTextPrototype", footNote, true);
}

ApacheChart.prototype._drawTitleElem = function(template, title, isFooter)
{
  var svgDoc = this._svgDoc, rootElem = this._rootElement;
  var margins = this._margins, gridWidth = (this._width - margins.left - margins.right);
  var labelElems = this._labelElems, animate = (this._animDuration>0);
  
  var textElem = svgDoc.getElementById(template).cloneNode(true);
  if(animate)
	{
		labelElems.push(textElem);
		textElem.setAttribute("fill-opacity","0");
	}
  textElem.firstChild.data = title;
  rootElem.appendChild(textElem);
  var textBBox = textElem.getBBox(), textWidth = textBBox.width, dx=margins.left;
  
  if(isFooter && this._width > textWidth + margins.right)
    dx = (this._width-textWidth)-margins.right;
  
  if(!isFooter && gridWidth > textWidth)
		dx = (gridWidth-textWidth)/2+margins.left;
    
  textElem.setAttribute("x",dx);
  if(isFooter)
  {
    textElem.setAttribute("y",this._height-margins.bottom);
    margins.bottom += textBBox.height+ApacheChart._TEXT_MARGIN;
  }
  else
  {
    margins.top += textBBox.height;
	  textElem.setAttribute("y",margins.top);
	  margins.top += ApacheChart._TEXT_MARGIN;
	}
} 

ApacheChart.prototype.DrawGroupLabels = function()
{
  var svgDoc = this._svgDoc, rootElem = this._rootElement, model = this._model;
  var container = svgDoc.createElementNS("http://www.w3.org/2000/svg", "g");
  this._hLabelContainer = container;
  var groupLabels = model.getGroupLabels(), vLineCount = groupLabels.length;
  var labelElem, labelElems = this._labelElems, animate = (this._animDuration>0);
  var labelText, gLabelElems = this._groupLabelElems;
  
  for (var i = 0; i< vLineCount; ++i)
  {
    // draw the horizontal label
    if(i==0)
    {
      labelElem = svgDoc.getElementById("groupLabelPrototype");
    }
    labelText = groupLabels[i];
    if(!labelText)
      continue;
    
    labelElem = labelElem.cloneNode(true);
    if(animate)
		{
		  labelElems.push(labelElem);
		  labelElem.setAttribute("fill-opacity","0");
		}
    labelElem.firstChild.data = labelText;
    container.appendChild(labelElem);
    gLabelElems[i] = labelElem;
  }
  rootElem.appendChild(container);
}

ApacheChart.prototype.LayoutGroupLabels = function()
{
  var model = this._model, margins = this._margins, marginLeft = margins.left; 
  var container = this._hLabelContainer, childNodes = container.childNodes;
  
  if(childNodes.length == 0)
    return;
  if(this._isPerspective)
    marginLeft += ApacheChart._XOFFSET_PERSPECTIVE;
  var gridWidth = (this._width - marginLeft - margins.right);
  var isCenterAligned = this.IsGroupLabelCentered();
  var groupLabels = model.getGroupLabels(), vLineCount = groupLabels.length;
  var labelElem, groupWidth = gridWidth/(isCenterAligned?vLineCount:vLineCount-1);
  var dx = 0, dy = this._height - margins.bottom+container.getBBox().height+ApacheChart._TEXT_MARGIN;
  var gLabelElems = this._groupLabelElems;
  for (var i = 0; i< vLineCount; ++i)
  {
    labelElem = gLabelElems[i];
    if(!labelElem)
      continue;
      
    labelElem.setAttribute("y", dy);
    var textWidth = labelElem.getComputedTextLength();
    if(isCenterAligned)
    {
      if(groupWidth > textWidth)
			  dx = (groupWidth-textWidth)/2;
	    else
	      dx = 2;
	  }
	  else
	  {
	    dx = (-textWidth)/2;
		  if(this._isPerspective)
		    dx -= ApacheChart._XOFFSET_PERSPECTIVE;
    }    
    labelElem.setAttribute("x", marginLeft+dx+i*groupWidth);
  }
}

/**
 * Indicates if the group label should be center aligned or edge aligned
 * @return true(String) indicates center aligned, false indicates it is edge aligned
 */
ApacheChart.prototype.IsGroupLabelCentered = function()
{
  return true;
}

ApacheChart.prototype.AdjustMarginsForGroupLabels = function()
{
  var container = this._hLabelContainer;
  if(container && container.childNodes.length > 0)
  {
    this._margins.bottom += container.getBBox().height+ApacheChart._TEXT_MARGIN;
    var isCentered = this.IsGroupLabelCentered();
    if(!isCentered)
    {
      var textWidth = container.lastChild.getBBox().width;
      if(textWidth/2> this._margins.right)
        this._margins.right = textWidth/2;
    }
  }
}

ApacheChart.prototype.DrawLegend = function()
{
  var legendPosition = this._legendPosition;
  if(legendPosition == ApacheChart.LEGEND_LOCATION_NONE)
  {
    return;
  }
  
  var svgDoc = this._svgDoc, rootElem = this._rootElement, model = this._model;
  var gradientsUsed = this._gradientsUsed;
  var seriesLabels = model.getSeriesLabels(), seriesCount = seriesLabels.length,
      seriesColors = model.getSeriesColors();
  var labelElem, rectElem, legendRectHeight, 
      legendGroup = svgDoc.createElementNS("http://www.w3.org/2000/svg", "g");
  var margins = this._margins, marginLeft = margins.left, marginTop = margins.top; 
  var labelElems = this._labelElems, animate = (this._animDuration>0);
  rootElem.appendChild(legendGroup);

  if(this._isPerspective)
  {
    marginLeft += ApacheChart._XOFFSET_PERSPECTIVE;
  }
  var gridWidth = (this._width - marginLeft - margins.right),
      gridHeight = (this._height - marginTop - margins.bottom);
  
  if(animate)
	{
		labelElems.push(legendGroup);
		legendGroup.setAttribute("fill-opacity","0");
	}
	
  var dx = 0, dy = 0, tx = marginLeft, ty = this._height - margins.bottom;
  var drawSideWays = (legendPosition == ApacheChart.LEGEND_LOCATION_START || 
                      legendPosition == ApacheChart.LEGEND_LOCATION_END)
  for (var i = 0; i < seriesCount; ++i)
  {
    if(i == 0)
    {
      labelElem = svgDoc.getElementById("legendTextPrototype");
      rectElem = svgDoc.getElementById("legendRectPrototype");
      legendRectHeight = parseInt(rectElem.getAttribute("height"));
    }

    if(drawSideWays)
      dx = 0;
      
    rectElem = rectElem.cloneNode(false);
    rectElem.setAttribute("x", dx);
    rectElem.setAttribute("y", dy-legendRectHeight);
    if(gradientsUsed)
      rectElem.setAttribute("fill", "url(#gradient"+i+")");
    else
      rectElem.setAttribute("fill", seriesColors[i]);
    rectElem.setAttribute("stroke", "#000000");
    // TODO: Legend elements should fire on click event       
    //rectElem.setAttribute("onclick", "parent."+onclickStrings[i]);
    legendGroup.appendChild(rectElem);
    
    dx += 1.5*legendRectHeight;
    
    labelElem = labelElem.cloneNode(true);
    labelElem.setAttribute("x", dx);
    labelElem.setAttribute("y", dy);
    labelElem.firstChild.data = seriesLabels[i];
    legendGroup.appendChild(labelElem);
    // TODO: Legend elements should fire on click event
    //labelElem.setAttribute("onclick", "parent."+onclickStrings[i]);
    
    if(!drawSideWays)
      dx += labelElem.getComputedTextLength()+legendRectHeight;
    else
      dy += 1.5*legendRectHeight;
    
    if(i == 0 && !drawSideWays)
    {
      var rect = labelElem.getBBox();
      if(legendPosition == ApacheChart.LEGEND_LOCATION_TOP)
      {
        ty = this.SetLegendTopAdjustment(margins.top+rect.height);
        margins.top += rect.height+ApacheChart._TEXT_MARGIN;
      }
      else
      {
        ty = this.SetLegendBottomAdjustment(ty);
        margins.bottom += rect.height+ApacheChart._TEXT_MARGIN;
      }
    }
  }
  
  if(!drawSideWays && gridWidth > dx)
    tx = (gridWidth-dx)/2+marginLeft;
  
  if(drawSideWays)
  {
    var lBBox = legendGroup.getBBox();
    if(legendPosition == ApacheChart.LEGEND_LOCATION_START)
    {
      tx = this.SetLegendLeftAdjustment(margins.left);
      margins.left += lBBox.width+ApacheChart._TEXT_MARGIN;
    }
    else
    {
      margins.right += lBBox.width+ApacheChart._TEXT_MARGIN;
      tx = this._width - margins.right + ApacheChart._TEXT_MARGIN;
      tx = this.SetLegendRightAdjustment(tx);
    }
    if(gridHeight > dy)
      ty = (gridHeight-lBBox.height)/2+marginTop;
    else
      ty = gridHeight+marginTop-lBBox.height;
  }
  
  legendGroup.setAttribute("transform", "translate("+tx+","+ty+")"); 
}

/**
 * Adjusts the legend location when it is at the top
 * @param ty(int) the original y location of the legend
 */
ApacheChart.prototype.SetLegendTopAdjustment = function(ty)
{
  // By default we need not adjust anything
  return ty;
}

/**
 * Adjusts the legend location when it is at the bottom
 * @param ty(int) the original y location of the legend
 */
ApacheChart.prototype.SetLegendBottomAdjustment = function(ty)
{
  var container = this._hLabelContainer;
  if(container && container.childNodes.length > 0)
  {
    ty += container.getBBox().height+ApacheChart._TEXT_MARGIN;
  }     
  return ty;
}

/**
 * Adjusts the legend location when it is at the Left
 * @param tx(int) the original x location of the legend
 */
ApacheChart.prototype.SetLegendLeftAdjustment = function(tx)
{
  var container = this._vLabelContainer;
  if(container)
  {
    tx -= container.getBBox().width+ApacheChart._TEXT_MARGIN;
  }
  return tx;
}

/**
 * Adjusts the legend location when it is at the Right
 * @param tx{int} the original x location of the legend
 */
ApacheChart.prototype.SetLegendRightAdjustment = function(tx)
{
  // By default we need not adjust anything
  return tx;
}

ApacheChart.prototype.DrawGrid = function()
{
  if(this._isPerspective)
    this.DrawPerspectiveGrid();
  else
    this.Draw2DGrid();
}

ApacheChart.prototype.Draw2DGrid = function()
{
  var svgDoc = this._svgDoc, model = this._model, margins = this._margins;
  var gridElems = this._gridElems, animate = (this._animDuration>0);  
  var marginLeft = margins.left, marginTop = margins.top; 
  var gridWidth = (this._width - marginLeft - margins.right);
  var gridHeight = (this._height - marginTop - margins.bottom);
  var gradientsUsed = this._gradientsUsed;
  var rectElem = svgDoc.getElementById("gridRectPrototype").cloneNode(false);
  rectElem.setAttribute("x", margins.left);
  rectElem.setAttribute("y", (marginTop));
  rectElem.setAttribute("width", gridWidth);
  rectElem.setAttribute("height", gridHeight);
  if(gradientsUsed)
    rectElem.setAttribute("fill", "url(#gridGradient)");
  this._rootElement.appendChild(rectElem);
  var pathElem = svgDoc.getElementById("gridPathPrototype").cloneNode(false);
  if(animate)
  {
    gridElems.push(pathElem);
    pathElem.setAttribute("transform", "scale(0.00001,1)");
  }
  var sb = new ApacheChartBuffer(), vLineCount = this.GetVLineCount(), hLineCount = this.GetHLineCount();
  // horizontal lines
  for (var i = 0; i< hLineCount-1; ++i)
  {
    sb.append("M").append(marginLeft).append(",").append((i+1)*gridHeight/hLineCount+marginTop);
    sb.append("h").append(gridWidth);
  }
  
  // vertical lines
  for (var i = 0; i< vLineCount-1; ++i)
  {
    sb.append("M").append(marginLeft+((i+1)*gridWidth/vLineCount)).append(",").append(marginTop);
    sb.append("v").append(gridHeight);
  }
  pathElem.setAttribute("d", sb.toString());
  pathElem.removeAttribute("id");
  this._rootElement.appendChild(pathElem);
}

ApacheChart.prototype.GetVLineCount = function()
{
  var xMajorCount = this._xMajorGridCount;
  if(xMajorCount >= 0)
    return xMajorCount;
  else
    return this._model.getGroupLabels().length;
}

ApacheChart.prototype.GetHLineCount = function()
{
  return this._yMajorGridCount;  
}

ApacheChart.prototype.DrawPerspectiveGrid = function()
{
  var svgDoc = this._svgDoc, model = this._model, margins = this._margins;
  var gridElems = this._gridElems, animate = (this._animDuration>0);
  var xOffset = ApacheChart._XOFFSET_PERSPECTIVE, yOffset = ApacheChart._YOFFSET_PERSPECTIVE;
  var marginLeft = margins.left, marginTop = margins.top;
  var gridWidth = (this._width - marginLeft - margins.right - xOffset);
  var gridHeight = (this._height - marginTop - margins.bottom - yOffset);
  var rectElem = svgDoc.getElementById("gridRectPrototype").cloneNode(false);
  var gradientsUsed = this._gradientsUsed;
  
  rectElem.setAttribute("x", marginLeft+ApacheChart._XOFFSET_PERSPECTIVE);
  rectElem.setAttribute("y", marginTop);
  rectElem.setAttribute("width", (gridWidth));
  rectElem.setAttribute("height", (gridHeight));
  if(gradientsUsed)
    rectElem.setAttribute("fill", "url(#gridGradient)");
  rectElem.removeAttribute("id");
  this._rootElement.appendChild(rectElem);
  var sb = new ApacheChartBuffer();
  var pathElem = svgDoc.getElementById("gridPath3dRectPrototype").cloneNode(false);
  sb.append("M").append(marginLeft+xOffset).append(",").append(marginTop);
  sb.append("l").append(-xOffset).append(",").append(yOffset);
  sb.append("v").append(gridHeight);
  sb.append("l").append(xOffset).append(",").append(-yOffset);
  sb.append("m").append(gridWidth).append(",").append(0);
  sb.append("l").append(-xOffset).append(",").append(yOffset);
  sb.append("h").append(-gridWidth);
  if(gradientsUsed)
    pathElem.setAttribute("fill", "url(#gridGradient)");
  pathElem.setAttribute("d", sb.toString());
  pathElem.removeAttribute("id");
  this._rootElement.appendChild(pathElem);
  pathElem = svgDoc.getElementById("gridPathPrototype").cloneNode(false);
  if(animate)
  {
    pathElem.setAttribute("transform", "scale(0.00001,1)");
    gridElems.push(pathElem);
  }
  var vLineCount = this.GetVLineCount(), hLineCount = this.GetHLineCount();
  sb = new ApacheChartBuffer();
  // horizontal lines
  for (var i = 0; i< hLineCount-1; ++i)
  {
    sb.append("M").append(marginLeft).append(",").append((i+1)*gridHeight/hLineCount+marginTop+yOffset);
    sb.append("l").append(xOffset).append(",").append(-yOffset);
    sb.append("h").append(gridWidth);
  }
  
  // vertical lines
  for (var i = 0; i< vLineCount-1; ++i)
  {
    sb.append("M").append(marginLeft+xOffset+((i+1)*gridWidth/vLineCount)).append(",").append(marginTop);
    sb.append("v").append(gridHeight);
    sb.append("l").append(-xOffset).append(",").append(yOffset);
  }
  pathElem.setAttribute("d", sb.toString());
  this._rootElement.appendChild(pathElem); 
}

ApacheChart.prototype.DrawYValueLabels = function()
{
  var svgDoc = this._svgDoc, rootElem = this._rootElement, model = this._model;
  var container = svgDoc.createElementNS("http://www.w3.org/2000/svg", "g");
  this._vLabelContainer = container;
    
  var minValue = model.getMinYValue(), maxValue = model.getMaxYValue();
  var labelElems = this._labelElems, animate = (this._animDuration>0);
  
  var labelElem = svgDoc.getElementById("yLabelPrototype").cloneNode(true);
  if(animate)
	{
		labelElems.push(labelElem);
		labelElem.setAttribute("fill-opacity","0");
	}
  labelElem.firstChild.data = this._formatValue(minValue);
  container.appendChild(labelElem);
  
  labelElem = labelElem.cloneNode(true);
  if(animate)
	{
		labelElems.push(labelElem);
		labelElem.setAttribute("fill-opacity","0");
	}
  labelElem.firstChild.data = this._formatValue(maxValue);
  container.appendChild(labelElem);
      
  var hLineCount = this._yMajorGridCount;
  // horizontal lines
  for (var i = 0; i< hLineCount-1; ++i)
  {
    var value = ((maxValue-minValue)*(i+1)/hLineCount) + minValue;
    labelElem = labelElem.cloneNode(true);
    if(animate)
		{
		  labelElems.push(labelElem);
		  labelElem.setAttribute("fill-opacity","0");
		}
		labelElem.firstChild.data = this._formatValue(value);
    container.appendChild(labelElem);
  }
  
  rootElem.appendChild(container);
}

ApacheChart.prototype._formatValue = function(value)
{
  // Initialize the decimal separtor
  var decimalSep = this._decimalSep;
  if(decimalSep == null)
  {
    var symbols = window.getLocaleSymbols?getLocaleSymbols():null;
    if (symbols)
    {
      this._decimalSep =  symbols.getDecimalSeparator();
    }
    else
      this._decimalSep = ".";      
    decimalSep = this._decimalSep;
  }
  value = value.toFixed(this._maxPrecision);
  value = value.toString();
  if(value.indexOf(decimalSep) == -1)
  {
    value = value.replace(".", decimalSep);
  }
  return value;
}

ApacheChart.prototype.AdjustMarginsForYLabels = function()
{
  var container = this._vLabelContainer;
  if(container && container.childNodes.length > 0)
    this._margins.left += container.getBBox().width+ApacheChart._TEXT_MARGIN;
}

ApacheChart.prototype.LayoutYValueLabels = function()
{
  var model = this._model, margins = this._margins; 
  var marginLeft = margins.left, marginTop = margins.top; 
  var container = this._vLabelContainer, childNodes = container.childNodes;
  var gridHeight = (this._height - marginTop - margins.bottom);
  if(this._isPerspective)
    gridHeight -= ApacheChart._YOFFSET_PERSPECTIVE;
    
  var bBox = container.getBBox(), textHeight = bBox.height;
  
  this.SetVerticalLabelAt(childNodes.item(0), gridHeight+marginTop, 
                           marginLeft, textHeight);
  
  this.SetVerticalLabelAt(childNodes.item(1), marginTop, 
                           marginLeft, textHeight);
   
  var hLineCount = this._yMajorGridCount;
  // horizontal lines
  for (var i = 0; i< hLineCount-1; ++i)
  {
    this.SetVerticalLabelAt(childNodes.item(i+2),
          (hLineCount -i -1)*gridHeight/hLineCount+marginTop, 
          marginLeft, textHeight);

  }
}

ApacheChart.prototype.SetVerticalLabelAt = function(
  labelElem, y, marginLeft, textHeight)
{
  if(this._isPerspective)
    y += ApacheChart._YOFFSET_PERSPECTIVE;
  // readjust to right align
  var labelMargin = ApacheChart._TEXT_MARGIN, 
      textLength = labelElem.getComputedTextLength(), dx = labelMargin;
  if(marginLeft>textLength+labelMargin)
    dx = marginLeft-textLength-labelMargin;
  labelElem.setAttribute("x", dx);
  labelElem.setAttribute("y", y+textHeight/2);
}

ApacheChart.prototype.DrawGroupLabelTitle = function(
  label, container, labelElem, dx, dy,
  quadWidth, quadHeight)
{
  if(!label)
    return quadHeight;
  var labelElems = this._labelElems, animate = (this._animDuration>0);
  labelElem.setAttribute("y", dy+quadHeight);
  labelElem.firstChild.data = label;
  container.appendChild(labelElem);
  var textWidth = labelElem.getComputedTextLength();
  if(quadWidth > textWidth)
		dx += (quadWidth-textWidth)/2;
	else
	  dx += 2;
  labelElem.setAttribute("x", dx);
	
	if(animate)
	  labelElems.push(labelElem);
	
  var rect = labelElem.getBBox();
  quadHeight -= rect.height+ApacheChart._TEXT_MARGIN;
  return quadHeight;
}

ApacheChart.prototype.ShowToolTip = function(e)
{
  if (this._toolTipVisible)
    return;
  
  var model = this._model, seriesColors = model.getSeriesColors();    
  var toolTip = this.getToolTip();
  if (toolTip == null)
  {
    toolTip = this._svgDoc.getElementById("toolTip").cloneNode(true);
    this.setToolTip(toolTip);
    this._rootElement.appendChild(toolTip);
  }
  toolTip.style.setProperty("visibility","visible","");
  
  var circleElem = toolTip.firstChild.nextSibling;
  var boundingRectElem = circleElem.nextSibling.nextSibling;  
  this.FillToolTipData(boundingRectElem, circleElem, e);

  var ttBBox = toolTip.getBBox();
  var pt = this.GetToolTipLocation(e, ttBBox);
  var dx = pt.x, dy = pt.y;
  
  if(dx + ttBBox.width > this._width)
  {
    dx -= ttBBox.width;
    circleElem.setAttribute("cx",boundingRectElem.getBBox().width);
  }
  else
  {
    circleElem.setAttribute("cx",0);
  }
  if(dy - ttBBox.height < 0)
  {
    dy += ttBBox.height;
    circleElem.setAttribute("cy",0);
  }
  else
  {
    circleElem.setAttribute("cy",boundingRectElem.getBBox().height);
  }
  
  if(this._isPerspective && this._type != ApacheChart.TYPE_PIE)
    dy += ApacheChart._YOFFSET_PERSPECTIVE/2
  toolTip.setAttribute("transform","translate("+dx+","+dy+")");
  this._toolTipVisible = true;
}

ApacheChart.prototype.GetToolTipLocation = function(e, ttBBox)
{
  var targetBBox = e.target.getBBox();
  return {x:(targetBBox.x+targetBBox.width/2), y:(targetBBox.y - ttBBox.height)};
}

ApacheChart.prototype.GetChartEvent = function(e)
{
  var evtTarget = e.target;
  
  var i = parseInt(evtTarget.getAttribute("yValueIndex")), 
      j = parseInt(evtTarget.getAttribute("seriesIndex"));

  var model = this._model, yValues = model.getYValues();
  return new ApacheChartEvent([j],[i], [yValues[i][j]],null);
}

ApacheChart.prototype.FillToolTipData = function(boundingRectElem, circleElem, e)
{
  var chartEvent = this.GetChartEvent(e);
  
  var j = chartEvent.getSeriesIndices()[0];

  var model = this._model, groupLabels = model.getGroupLabels(), 
      seriesLabels = model.getSeriesLabels(),
      yValues = chartEvent.getYValues();
  
  //top label
  var textElem = boundingRectElem.nextSibling.nextSibling;
  textElem.firstChild.data = seriesLabels[j];
                
  var labelWidth = textElem.getComputedTextLength();      
  //actual value
  textElem = textElem.nextSibling.nextSibling;
  textElem.firstChild.data = this._formatValue(yValues[0]);
  var dataWidth = textElem.getComputedTextLength();
  // leave a  clearance on either end of the text
  var xMargin = ApacheChart._TEXT_MARGIN, dx = xMargin;
  if (labelWidth > dataWidth)
    dx = (labelWidth-dataWidth)/2+xMargin;
  textElem.setAttribute("dx",dx);
  var rectWidth = Math.max(labelWidth,dataWidth)+2*xMargin;
  boundingRectElem.setAttribute("width",rectWidth);
  boundingRectElem.setAttribute("stroke", seriesColors[j]);
  circleElem.setAttribute("stroke",seriesColors[j]);
}

ApacheChart.prototype.HideToolTip = function(e)
{
  var toolTip = this.getToolTip();
  if(toolTip)
    toolTip.style.setProperty("visibility","hidden","");
  this._toolTipVisible = false;
}

ApacheChart.prototype.Click = function(e)
{
  var chartEvent = this.GetChartEvent(e);
  var formName = this._formName;
  
  if(formName !=null)
  {
    var svgEmbed = document.getElementById(this._svgEmbedId);
    var sourceId = svgEmbed.parentNode.id;
    var chartValue ={ 'event': 'chartDrillDown',
                      'source':sourceId,
                      'value':chartEvent.marshall()};
    if(this._partialSubmit)
    {
      _submitPartialChange(formName,'0',chartValue);
    }
    else
    {
      submitForm(formName,'0',chartValue);
    }
  }
  else
    alert(chartEvent);
}

////////////////////////////////////////////////////////////////////
// Bar Chart subclass
////////////////////////////////////////////////////////////////////
function ApacheBarChart(
  type, model, svgEmbedId,
  isPerspective, legendPosition)
{
  this.Init(type, model, svgEmbedId, isPerspective, legendPosition);
}

ApacheChartObj.Inherit(ApacheChart, ApacheBarChart);

ApacheBarChart.prototype.DrawChartData = function()
{
  var isCombo = this._type == ApacheChart.TYPE_BAR_LINE_COMBO;
  var isPerspective = this._isPerspective;
  if(isPerspective)
    this._drawPerspectiveBars(isCombo);
  else
    this._drawBars(isCombo);
    
  // delegate to the line chart for combos
  if(isCombo)
  {
    if(isPerspective)
      this.__drawPerspectiveLines = ApacheLineChart.prototype.__drawPerspectiveLines;
    else
      this.__drawLines = ApacheLineChart.prototype.__drawLines;
    
    ApacheLineChart.prototype.DrawChartData.call(this, isCombo);
  }
}

ApacheBarChart.prototype._drawBars = function(isCombo)
{
  var svgDoc = this._svgDoc, model = this._model, margins = this._margins;
  var rootElem = this._rootElement, dataElems = this._dataElems, animate = (this._animDuration>0);
  var marginLeft = margins.left, marginTop = margins.top; 
  var gridWidth = (this._width - marginLeft - margins.right);
  var gridHeight = (this._height - marginTop - margins.bottom);
  var rectElem = svgDoc.getElementById("barRectPrototype");
  var barItemPadding = ApacheBarChart._BARITEM_PADDING;
  var isStacked = (this._type == ApacheChart.TYPE_VBAR_STACKED);
  var groupLabels = model.getGroupLabels(), groupCount = groupLabels.length, 
      seriesLabels = model.getSeriesLabels(), seriesCount = seriesLabels.length;
  var seriesColors = model.getSeriesColors(), yValues = model.getYValues();    
  var minValue = model.getMinYValue(), maxValue = model.getMaxYValue();    
  var barDivider = isStacked?1:(isCombo?Math.ceil(seriesCount/2): seriesCount);
  var yValueCount = yValues.length;
  var barWidth = (gridWidth/Math.max(yValueCount,groupCount)-2*barItemPadding)/barDivider;
  var dx = marginLeft, dy, barHeight, stackBase = minValue;
  var gradientsUsed = this._gradientsUsed;
  var defaultTransform = "scale(1,0.00001)";
  for (var i = 0; i< yValueCount; ++i)
  {
    dx += barItemPadding;
    dy = gridHeight + marginTop;
   
    for (var j = 0; j < seriesCount; ++j)
    {
      // for combo charts we draw every alternate(even) bar.
      if(isCombo && j%2>0)
        continue;

      // If we use non zero min and it is a stacked graph, we need to remove the min for only
      // the first series.
      if(isStacked)
        stackBase = (j==0?minValue:0);
      
      rectElem = rectElem.cloneNode(false);
      if(animate)
      {
        dataElems.push(rectElem);
        rectElem.setAttribute("transform",defaultTransform); 
      }
      rectElem.setAttribute("x", dx);
      barHeight = gridHeight*(yValues[i][j]-stackBase)/(maxValue-minValue);
      if(isStacked)
        dy -= barHeight;
      else
        dy = gridHeight + marginTop - barHeight;
      rectElem.setAttribute("y", dy);
      rectElem.setAttribute("width", barWidth);
      rectElem.setAttribute("height", barHeight);
      
      if(gradientsUsed)
        rectElem.setAttribute("fill", "url(#gradient"+j+")");
      else
        rectElem.setAttribute("fill", seriesColors[j]);
        
      rectElem.setAttribute("stroke", seriesColors[j]);
      rectElem.setAttribute("stroke-width", 1);
      rectElem.setAttribute("yValueIndex", i);
      rectElem.setAttribute("seriesIndex", j);
      if(this._tooltipsVisible)
      {
        rectElem.addEventListener("mouseover",this.ShowToolTipCallback,false);
        rectElem.addEventListener("mouseout",this.HideToolTipCallback,false);
      }
      rectElem.addEventListener("click",this.ClickCallback,false); 
      rootElem.appendChild(rectElem);
      if(!isStacked)
        dx += barWidth;
    }
    if(isStacked)
      dx += barWidth;
    dx += barItemPadding;
  }  
}

ApacheBarChart.prototype._drawPerspectiveBars = function(isCombo)
{
  var svgDoc = this._svgDoc, model = this._model, margins = this._margins;
  var rootElem = this._rootElement, dataElems = this._dataElems, animate = (this._animDuration>0);
  var xOffset = ApacheChart._XOFFSET_PERSPECTIVE, yOffset = ApacheChart._YOFFSET_PERSPECTIVE;
  var marginLeft = margins.left, marginTop = margins.top; 
  var gridWidth = (this._width - marginLeft - margins.right - xOffset);
  var gridHeight = (this._height - marginTop - margins.bottom - yOffset);
  var pathElem = svgDoc.getElementById("barPathPrototype");
  var barItemPadding = ApacheBarChart._BARITEM_PADDING;
  var isStacked = (this._type == ApacheChart.TYPE_VBAR_STACKED);
  var groupLabels = model.getGroupLabels(), groupCount = groupLabels.length, 
      seriesLabels = model.getSeriesLabels(), seriesCount = seriesLabels.length;
  var seriesColors = model.getSeriesColors(), yValues = model.getYValues();    
  var minValue = model.getMinYValue(), maxValue = model.getMaxYValue();    
  var seriesBars = isCombo?Math.ceil(seriesCount/2): seriesCount, barWidth;
  var yValueCount = yValues.length;
  if(isStacked)
    barWidth = gridWidth/Math.max(yValueCount,groupCount)-2*barItemPadding;
  else
    barWidth = (gridWidth/Math.max(yValueCount,groupCount) -2*barItemPadding - (seriesBars)*barItemPadding)/seriesBars;
  var dx = marginLeft, dy, barHeight, stackBase = minValue;
  var gradientsUsed = this._gradientsUsed;
  var defaultTransform = "scale(1, 0.00001)";
  for (var i = 0; i< yValueCount; ++i)
  {
    dx += barItemPadding;
    dy = gridHeight + marginTop+yOffset;
    for (var j = 0; j < seriesCount; ++j)
    {
      // for combo charts we draw every alternate(even) bar.
      if(isCombo && j%2>0)
        continue;
      // If we use non zero min and it is a stacked graph, we need to remove the min for only
      // the first series.
      if(isStacked)
        stackBase = (j==0?minValue:0);

      barHeight = gridHeight*(yValues[i][j]-stackBase)/(maxValue-minValue);
      if(isStacked)
        dy -= barHeight;
      else
        dy = gridHeight + yOffset + marginTop - barHeight;      
      pathElem = pathElem.cloneNode(false);
      if(animate)
      {
        dataElems.push(pathElem);
        pathElem.setAttribute("transform",defaultTransform); 
      }
      var sb = new ApacheChartBuffer();
      sb.append("M").append(dx).append(",").append(dy);
      sb.append("l").append(xOffset).append(",").append(-yOffset);
      sb.append("h").append(barWidth);
      sb.append("v").append(barHeight);
      sb.append("l").append(-xOffset).append(",").append(yOffset);
      sb.append("v").append(-barHeight);
      sb.append("l").append(xOffset).append(",").append(-yOffset);
      sb.append("l").append(-xOffset).append(",").append(yOffset);
      sb.append("h").append(-barWidth);
      sb.append("v").append(barHeight);
      sb.append("h").append(barWidth);
      sb.append("v").append(-barHeight);
      pathElem.setAttribute("stroke", seriesColors[j]);
      pathElem.setAttribute("stroke-width", 1);
      if(gradientsUsed)
        pathElem.setAttribute("fill", "url(#gradient"+j+")");
      else
        pathElem.setAttribute("fill", seriesColors[j]);
      pathElem.setAttribute("d", sb.toString());
      
      pathElem.setAttribute("yValueIndex", i);
      pathElem.setAttribute("seriesIndex", j);
      if(this._tooltipsVisible)
      {
        pathElem.addEventListener("mouseover",this.ShowToolTipCallback,false);
        pathElem.addEventListener("mouseout",this.HideToolTipCallback,false);      
      }
      pathElem.addEventListener("click",this.ClickCallback,false); 
      rootElem.appendChild(pathElem);
      if(!isStacked)
      {
        dx += barWidth;
        dx += barItemPadding;
      }
    }
    if(isStacked)
       dx += barWidth;
    dx += barItemPadding;
  }
}

ApacheBarChart.prototype.ShowToolTip = function(e)
{
  if(this._type == ApacheChart.TYPE_BAR_LINE_COMBO)
  {
    var i = parseInt(e.target.getAttribute("seriesIndex"));
    if(i%2>0)
    {
      try
      {
        // Maybe we need a generic framework for combos so that we can delegate.
        // Till that time...
        this.GetToolTipLocation = ApacheLineChart.prototype.GetToolTipLocation;
        this.FillToolTipData = ApacheLineChart.prototype.FillToolTipData;
        this.GetChartEvent = ApacheLineChart.prototype.GetChartEvent;
        ApacheLineChart.prototype.ShowToolTip.call(this, e);
      }
      finally
      {
        // restore
        this.GetToolTipLocation = ApacheBarChart.prototype.GetToolTipLocation;
        this.FillToolTipData = ApacheBarChart.prototype.FillToolTipData;
        this.GetChartEvent = ApacheBarChart.prototype.GetChartEvent;
      }
      return;
    }
  }
  ApacheBarChart.superclass.ShowToolTip.call(this, e);
}

// number of pixels on either side of the bar item
ApacheBarChart._BARITEM_PADDING = 2;

////////////////////////////////////////////////////////////////////
// Bar chart subclass
////////////////////////////////////////////////////////////////////
function ApacheHBarChart(
  type, model, svgEmbedId, 
  isPerspective, legendPosition)
{
  this.Init(type, model, svgEmbedId, isPerspective, legendPosition);
}

ApacheChartObj.Inherit(ApacheChart, ApacheHBarChart);

ApacheHBarChart.prototype.DrawChartData = function()
{
  if(this._isPerspective)
    this._drawPerspectiveBars();
  else
    this._drawBars();
}

ApacheHBarChart.prototype.AnimAlongXAxis = function()
{
  // horizontal bar animates around x axis
  return true;  
}

ApacheHBarChart.prototype.DrawYValueLabels = function()
{
  var svgDoc = this._svgDoc, rootElem = this._rootElement, model = this._model;
  var container = svgDoc.createElementNS("http://www.w3.org/2000/svg", "g");
  // Since the horizontal bar chart is flipped Y labels are horizontal
  this._vLabelContainer = container;  
  var labelElem = svgDoc.getElementById("groupLabelPrototype");
  var labelElems = this._labelElems, animate = (this._animDuration>0);  
  var groupLabels = model.getGroupLabels(), hLineCount = groupLabels.length;
  var labelText, gLabelElems = this._groupLabelElems;

  // horizontal lines
  for (var i = 0; i< hLineCount; ++i)
  {
    labelText = groupLabels[i];
    if(!labelText)
      continue;
    labelElem = labelElem.cloneNode(true);
    labelElem.firstChild.data = labelText;
    container.appendChild(labelElem);
    gLabelElems[i] = labelElem;
    if(animate)
		{
		  labelElems.push(labelElem);
		  labelElem.setAttribute("fill-opacity","0");
		}
  }
  rootElem.appendChild(container);
}

ApacheHBarChart.prototype.LayoutYValueLabels = function()
{
  var model = this._model, margins = this._margins, 
      marginLeft = margins.left, marginTop = margins.top; 
  var gridHeight = (this._height - marginTop - margins.bottom);
  if(this._isPerspective)
    gridHeight -= ApacheChart._YOFFSET_PERSPECTIVE;
  
  var container = this._vLabelContainer, childNodes = container.childNodes;
  
  if(childNodes.length == 0)
    return;
      
  var labelElem, bBox = container.getBBox(), textHeight = bBox.height;
  var groupLabels = model.getGroupLabels(), hLineCount = groupLabels.length;
  var gLabelElems = this._groupLabelElems;
  // horizontal lines
  for (var i = 0; i< hLineCount; ++i)
  {
    labelElem = gLabelElems[i];
    if(!labelElem)
      continue;
      
    this.SetVerticalLabelAt(labelElem, 
          (hLineCount -i)*gridHeight/hLineCount+marginTop-(gridHeight/(2*hLineCount)), 
          marginLeft, textHeight);
  }
}

ApacheHBarChart.prototype.IsGroupLabelCentered = function()
{
  return false;
}

ApacheHBarChart.prototype.DrawGroupLabels = function()
{
  var svgDoc = this._svgDoc, rootElem = this._rootElement, model = this._model;

  var vLineCount = this._yMajorGridCount;
  var container = svgDoc.createElementNS("http://www.w3.org/2000/svg", "g");
  // Since the horizontal bar chart is flipped group labels are vertical
  this._hLabelContainer = container;
  
  var minValue = model.getMinYValue(), maxValue = model.getMaxYValue();
  var labelElem = svgDoc.getElementById("yLabelPrototype");
  
  var value, labelElems = this._labelElems, animate = (this._animDuration>0);
  for (var i = 0; i< vLineCount+1; ++i)
  {
    // draw the horizontal label
    labelElem = labelElem.cloneNode(true);
    if(animate)
		{
		  labelElems.push(labelElem);
		  labelElem.setAttribute("fill-opacity","0");
		}
		if(i==0)
      value = minValue;
    else if(i==vLineCount)
      value = maxValue;
    else
      value = (((maxValue-minValue)*(i)/vLineCount) + minValue);
    labelElem.firstChild.data = this._formatValue(value);
    container.appendChild(labelElem);
  }
  rootElem.appendChild(container);
}

ApacheHBarChart.prototype.LayoutGroupLabels = function()
{
  var model = this._model, margins = this._margins, marginLeft = margins.left; 
  var gridWidth = (this._width - marginLeft - margins.right);
  var container = this._hLabelContainer, childNodes = container.childNodes;
    
  if(this._isPerspective)
    gridWidth -= ApacheChart._XOFFSET_PERSPECTIVE;

  var vLineCount = this._yMajorGridCount;
  var labelElem, yValWidth = gridWidth/vLineCount;

  var bBox = container.getBBox();      
  var dx = 0, dy = this._height - margins.bottom + bBox.height+ApacheChart._TEXT_MARGIN;
  var labelElems = this._labelElems, animate = (this._animDuration>0);
  for (var i = 0; i< vLineCount+1; ++i)
  {
    // draw the horizontal label
    labelElem = childNodes.item(i);
    labelElem.setAttribute("y", dy);
    var textWidth = labelElem.getComputedTextLength();
    labelElem.setAttribute("x", marginLeft-textWidth/2+i*yValWidth);	    
  }
}

ApacheHBarChart.prototype.GetVLineCount = function()
{
  return this._yMajorGridCount;
}

ApacheHBarChart.prototype.GetHLineCount = function()
{
  var xMajorCount = this._xMajorGridCount;
  if(xMajorCount >= 0)
    return xMajorCount;
  else
    return this._model.getGroupLabels().length;
}

ApacheHBarChart.prototype._drawBars = function()
{
  var svgDoc = this._svgDoc, model = this._model, margins = this._margins;
  var rootElem = this._rootElement, dataElems = this._dataElems, animate = (this._animDuration>0);
  var marginLeft = margins.left, marginTop = margins.top; 
  var gridWidth = (this._width - marginLeft - margins.right);
  var gridHeight = (this._height - marginTop - margins.bottom);
  var rectElem = svgDoc.getElementById("barRectPrototype");
  var barItemPadding = ApacheBarChart._BARITEM_PADDING;
  var isStacked = (this._type == ApacheChart.TYPE_HBAR_STACKED);
  var groupLabels = model.getGroupLabels(), groupCount = groupLabels.length, 
      seriesLabels = model.getSeriesLabels(), seriesCount = seriesLabels.length;
  var seriesColors = model.getSeriesColors(), yValues = model.getYValues();    
  var minValue = model.getMinYValue(), maxValue = model.getMaxYValue();    
  var barDivider = isStacked?1:seriesCount, stackBase = minValue;
  var yValueCount = yValues.length;
  var barHeight = (gridHeight/Math.max(yValueCount,groupCount)-2*barItemPadding)/barDivider;
  var dx = marginLeft, dy=gridHeight+marginTop, barWidth;
  var gradientsUsed = this._gradientsUsed;
  var defaultTransform = "scale(0.00001,1)";
  for (var i = 0; i< yValueCount; ++i)
  {
    dy -= barItemPadding;
    dx = marginLeft;
    for (var j = 0; j < seriesCount; ++j)
    {
      // If we use non zero min and it is a stacked graph, we need to remove the min for only
      // the first series.
      if(isStacked)
        stackBase = (j==0?minValue:0);
        
      rectElem = rectElem.cloneNode(false);
      if(animate)
      {
        dataElems.push(rectElem);
        rectElem.setAttribute("transform",defaultTransform); 
      }
      rectElem.setAttribute("x", dx);
      barWidth = gridWidth*(yValues[i][j]-stackBase)/(maxValue-minValue);
      if(isStacked)
        dx += barWidth;
      rectElem.setAttribute("y", dy-barHeight);
      rectElem.setAttribute("width", barWidth);
      
      rectElem.setAttribute("height", barHeight);
      if(gradientsUsed)
        rectElem.setAttribute("fill", "url(#gradient"+j+")");
      else
        rectElem.setAttribute("fill", seriesColors[j]);
      rectElem.setAttribute("stroke", seriesColors[j]);
      rectElem.setAttribute("stroke-width", 1);
      rectElem.setAttribute("yValueIndex", i);
      rectElem.setAttribute("seriesIndex", j);
      if(this._tooltipsVisible)
      {
        rectElem.addEventListener("mouseover",this.ShowToolTipCallback,false);
        rectElem.addEventListener("mouseout",this.HideToolTipCallback,false);
      }
      rectElem.addEventListener("click",this.ClickCallback,false); 
      rootElem.appendChild(rectElem);   
      if(!isStacked)
        dy -= barHeight;
    }
    if(isStacked)
      dy -= barHeight;
    dy -= barItemPadding;
  }  
}

ApacheHBarChart.prototype._drawPerspectiveBars = function()
{
  var svgDoc = this._svgDoc, model = this._model, margins = this._margins;
  var rootElem = this._rootElement, dataElems = this._dataElems, animate = (this._animDuration>0);
  var xOffset = ApacheChart._XOFFSET_PERSPECTIVE, yOffset = ApacheChart._YOFFSET_PERSPECTIVE;
  var marginLeft = margins.left, marginTop = margins.top; 
  var gridWidth = (this._width - marginLeft - margins.right - xOffset);
  var gridHeight = (this._height - marginTop - margins.bottom - yOffset);
  var pathElem = svgDoc.getElementById("barPathPrototype");
  var barItemPadding = ApacheBarChart._BARITEM_PADDING;
  var isStacked = (this._type == ApacheChart.TYPE_HBAR_STACKED);
  var groupLabels = model.getGroupLabels(), groupCount = groupLabels.length, 
      seriesLabels = model.getSeriesLabels(), seriesCount = seriesLabels.length;
  var seriesColors = model.getSeriesColors(), yValues = model.getYValues();    
  var minValue = model.getMinYValue(), maxValue = model.getMaxYValue();    
  var yValueCount = yValues.length;
  var barHeight, stackBase = minValue;
  if(isStacked)
    barHeight = gridHeight/Math.max(yValueCount,groupCount)-2*barItemPadding;
  else
    barHeight = (gridHeight/Math.max(yValueCount,groupCount)-2*barItemPadding - (seriesCount)*barItemPadding)/seriesCount;
  var dx = marginLeft, dy=gridHeight+marginTop+yOffset, barWidth;
  var gradientsUsed = this._gradientsUsed;
  var defaultTransform = "scale(0.00001,1)";
  
  for (var i = 0; i< yValueCount; ++i)
  {
    dy -= barItemPadding;
    dx = marginLeft;
    for (var j = 0; j < seriesCount; ++j)
    {      
      // If we use non zero min and it is a stacked graph, we need to remove the min for only
      // the first series.
      if(isStacked)
        stackBase = (j==0?minValue:0);
              
      barWidth = gridWidth*(yValues[i][j]-stackBase)/(maxValue-minValue);      
      pathElem = pathElem.cloneNode(false);
      if(animate)
      {
        dataElems.push(pathElem);
        pathElem.setAttribute("transform",defaultTransform); 
      }
      var sb = new ApacheChartBuffer();
      sb.append("M").append(dx).append(",").append(dy);
      sb.append("h").append(barWidth);
      sb.append("v").append(-barHeight);
      sb.append("h").append(-barWidth);
      sb.append("v").append(barHeight);
      
      sb.append("M").append(dx).append(",").append(dy-barHeight);
      sb.append("l").append(xOffset).append(",").append(-yOffset);
      sb.append("h").append(barWidth);
      sb.append("l").append(-xOffset).append(",").append(yOffset);
      sb.append("z");
      sb.append("M").append(dx+barWidth).append(",").append(dy);
      sb.append("v").append(-barHeight);
      sb.append("l").append(xOffset).append(",").append(-yOffset);
      sb.append("v").append(barHeight);
      sb.append("z");
      pathElem.setAttribute("stroke", seriesColors[j]);
      pathElem.setAttribute("stroke-width", 1);
      if(gradientsUsed)
        pathElem.setAttribute("fill", "url(#gradient"+j+")");
      else
        pathElem.setAttribute("fill", seriesColors[j]);
        
      pathElem.setAttribute("d", sb.toString());
      
      pathElem.setAttribute("yValueIndex", i);
      pathElem.setAttribute("seriesIndex", j);
      if(this._tooltipsVisible)
      {
        pathElem.addEventListener("mouseover",this.ShowToolTipCallback,false);
        pathElem.addEventListener("mouseout",this.HideToolTipCallback,false);      
      }
      pathElem.addEventListener("click",this.ClickCallback,false);
      rootElem.appendChild(pathElem);
      if(isStacked)
        dx += barWidth;
      else
      {
        dy -= barHeight;
        dy -= barItemPadding;
      }
    }
    if(isStacked)
       dy -= barHeight;
    dy -= barItemPadding;
  }
}

////////////////////////////////////////////////////////////////////
// Pie chart subclass
////////////////////////////////////////////////////////////////////
function ApachePieChart(
  type, model, svgEmbedId, 
  isPerspective, legendPosition)
{
  this.Init(type, model, svgEmbedId, isPerspective, legendPosition);
}

ApacheChartObj.Inherit(ApacheChart, ApachePieChart);

ApachePieChart.prototype.Init = function(
  type, model, svgEmbedId, 
  isPerspective, legendPosition)
{
  ApachePieChart.superclass.Init.call(this, type, model, svgEmbedId, 
                      isPerspective, legendPosition);
  //this._pieAnimAngles = undefined;
  //this._pieAnimRadii = undefined;
}

ApachePieChart.prototype.DrawChartData = function()
{  
  var rootElem = this._rootElement;
    
  // calculate the number of rows and columns
  var model = this._model, yValues = model.getYValues(), yValueCount = yValues.length;
  var groupLabels = model.getGroupLabels(), groupCount = groupLabels?groupLabels.length:1;
      
  var nCols = Math.ceil(Math.sqrt(yValueCount)), nRows = Math.round(Math.sqrt(yValueCount));
  var labelElem = this._svgDoc.getElementById("groupLabelPrototype");
  var margins = this._margins, dx=margins.left, dy=margins.top;
  var quadWidth = (this._width - margins.left - margins.right)/nCols;
  var animate = (this._animDuration>0), isPerspective = this._isPerspective;
  var pieAnimRadii, vGap = 2*ApacheChart._TEXT_MARGIN;
  var quadHeight = (this._height - margins.top - margins.bottom - (nRows-1)*vGap)/nRows;
  if(animate)
  {
    this._pieAnimAngles = [];
    pieAnimRadii = this._pieAnimRadii = []; 
  }
  for(var i = 0; i<nRows; ++i)
  {
    for(var j = 0; j<nCols; ++j)
    {  
      var iGroup = groupLabels?(i*nCols + j):(-1);
      if(iGroup >= yValueCount)
        break;
      
      var groupLabel = (iGroup == -1)?null:groupLabels[iGroup];
      var pieContainer = rootElem.cloneNode(false);
      rootElem.appendChild(pieContainer);
      var newHeight = this.DrawGroupLabelTitle(groupLabel, rootElem, 
                                            labelElem.cloneNode(true), dx, dy, 
                                            quadWidth, quadHeight);
      var newWidth = quadWidth - 2*ApacheChart._TEXT_MARGIN;
      var cx= dx+quadWidth/2+ApacheChart._TEXT_MARGIN, cy = dy+newHeight/2;
      
      if(animate)
      {
        pieAnimRadii.push(Math.max(cx, cy));
      }
      
      if(isPerspective)
      {
        this._draw3DPies(pieContainer, newWidth, newHeight, iGroup);
        // The chart is draw with the center at 0 so we need to compensate for it.
        pieContainer.setAttribute("transform", 
          "translate("+cx+","+cy+") scale(1.0,0.707)");
      }
      else
      {
        this._drawPies(pieContainer, newWidth, newHeight, iGroup);
        pieContainer.setAttribute("transform", 
          "translate("+cx+","+cy+")");
      }
      dx +=quadWidth;
    }
    dx=margins.left;
    dy +=quadHeight+vGap;
  }  
}

ApachePieChart.prototype.ComputeMinMaxValues = function()
{

}

ApachePieChart.prototype.DrawGroupLabels = function()
{

}

ApachePieChart.prototype.LayoutGroupLabels = function()
{

}

ApachePieChart.prototype.DrawGrid = function()
{

}

ApachePieChart.prototype.DrawYValueLabels = function()
{

}

ApachePieChart.prototype.LayoutYValueLabels = function()
{

}

ApachePieChart.prototype.SetDataAnimStep = function(ratio)
{
  var pieAnimRadii = this._pieAnimRadii, pieAnimAngles = this._pieAnimAngles,
      isPerspective = this._isPerspective, agleIndex = 0, elemIndex = 0;
  var animElems = this._dataElems, chartCount = pieAnimRadii.length;
  var model = this._model, yValues = model.getYValues();
  
  // We are animating parependiculat to the tangent to the middle of the pie
  for(var i = 0; i < chartCount; ++i)
  {
    var nPies = yValues[i].length;
    var radius = pieAnimRadii[i]*(1-ratio);
    for (var j = 0; j<nPies; ++j)
    {
      var angle = pieAnimAngles[agleIndex++]*2*Math.PI;
      var tx = radius*Math.sin(angle), ty = radius*Math.cos(angle);
      if(angle <= Math.PI/2)
      {
        ty = -ty;
        tx = tx;
      }
      else if(angle <= Math.PI)
      {
        ;
      }
      else if(angle <= 3*Math.PI/2)
      {
        tx = -tx;
      }
      else
      {
        ty = -ty;
        tx = -tx;
      }
      animElems[elemIndex++].setAttribute("transform", "translate("+tx+","+ty+")");
      if(isPerspective)
      {
        animElems[elemIndex++].setAttribute("transform", "translate("+tx+","+ty+")");
        animElems[elemIndex++].setAttribute("transform", "translate("+tx+","+ty+")");
      }
    }
  }
}

ApachePieChart.prototype._drawPies = function(
  pieContainer, quadWidth, 
  quadHeight, iGroup)
{
  var svgDoc = this._svgDoc, model = this._model, yValues = model.getYValues();
  var groupLabels = model.getGroupLabels(), seriesColors = model.getSeriesColors();
  var pieSize = Math.min(quadWidth/2, quadHeight/2);
  
  if(iGroup == -1)
    iGroup = 0;

  var nPies = yValues[iGroup].length;
  var pieTotal = 0;
    
  for (var i = 0; i < nPies; ++i)
  {
    pieTotal += yValues[iGroup][i];
  }
  
  var pathElem = svgDoc.getElementById("piePathPrototype"), pieStart = 0, animAngleStart = 0;
  var pieElems = new Array(nPies), dataElems = this._dataElems, animate = (this._animDuration>0);
  var gradientsUsed = this._gradientsUsed;
  var defaultTransform = "translate(-10000, -10000)", pieAnimAngles = this._pieAnimAngles;
  for (var i = 0; i<nPies; ++i)
  {
    pathElem = pathElem.cloneNode(false);
    var valueRatio = 1 - (yValues[iGroup][i])/(pieTotal);
    if(animate)
    {
      dataElems.push(pathElem);
      pathElem.setAttribute("transform",defaultTransform);
      var curAnimRatio = (yValues[iGroup][i])/(pieTotal);
      pieAnimAngles.push(animAngleStart+curAnimRatio/2);
      animAngleStart+=curAnimRatio;
    }
    var sb = new ApacheChartBuffer();
    sb.append("M0,0L");
    sb.append(pieSize*Math.cos(pieStart*Math.PI*2));
    sb.append(",").append(pieSize*Math.sin(pieStart*Math.PI*2));

    if (valueRatio >= .5) // major arc
    {
      sb.append("A").append(pieSize).append(" ").append(pieSize).append(" 1 0 0 ");
      sb.append(pieSize* Math.cos((pieStart+valueRatio)*Math.PI*2));
      sb.append(",").append(pieSize*Math.sin((pieStart+valueRatio)*Math.PI*2));
    }
    else
    {
      sb.append("A").append(pieSize).append(" ").append(pieSize).append(" 1 1 0 ");
      sb.append(pieSize* Math.cos((pieStart+valueRatio)*Math.PI*2));
      sb.append(",").append(pieSize*Math.sin((pieStart+valueRatio)*Math.PI*2));
    }
    sb.append("z");
    
    pathElem.setAttribute("d", sb.toString());
    if(gradientsUsed)
      pathElem.setAttribute("fill", "url(#gradient"+i+")");
    else
      pathElem.setAttribute("fill", seriesColors[i]);
    pathElem.setAttribute("stroke", seriesColors[i]);
    pathElem.setAttribute("stroke-width", 1);
    pathElem.setAttribute("yValueIndex", iGroup);
    pathElem.setAttribute("seriesIndex", i);
    if(this._tooltipsVisible)
    {
      pathElem.addEventListener("mouseover",this.ShowToolTipCallback,false);
      pathElem.addEventListener("mouseout",this.HideToolTipCallback,false);      
    }
    pathElem.addEventListener("click",this.ClickCallback,false);
    pieStart += valueRatio;
    pieElems[i] = pathElem;
  }
  
  
  for (var i = 0; i< nPies; ++i)
  {
    // calculate the pie gradient:
    pieContainer.appendChild(pieElems[i]);
  }  
}

ApachePieChart.prototype._draw3DPies = function(
  pieContainer, quadWidth, 
  quadHeight, iGroup)
{
  var svgDoc = this._svgDoc, model = this._model, yValues = model.getYValues();
  var groupLabels = model.getGroupLabels(), seriesColors = model.getSeriesColors();
  var pieSize = Math.min(quadWidth/2, quadHeight/2);
  var pieTotal = 0;
  
  if(iGroup == -1)
    iGroup = 0;

  var nPies = yValues[iGroup].length;      
  for (var i = 0; i < nPies; ++i)
  {
    pieTotal += yValues[iGroup][i];
  }

  var perspectiveHeight = pieSize/4, pieElems = new Array(nPies), 
      ringElems = new Array(nPies), edgeElems = new Array(nPies);
  var dataElems = this._dataElems, animate = (this._animDuration>0);
  if( perspectiveHeight> ApachePieChart._MAX_PERSPECTIVE_HEIGHT )
    perspectiveHeight = ApachePieChart._MAX_PERSPECTIVE_HEIGHT;
  
  var pathElem = svgDoc.getElementById("piePathPrototype"), pieStart = 0;
  var gradientsUsed = this._gradientsUsed;
  var defaultTransform = "translate(-10000, -10000)", pieAnimAngles = this._pieAnimAngles;
  for (var i = 0; i < nPies; ++i)
  {
    pathElem = pathElem.cloneNode(false);
    var valueRatio = 1 - (yValues[iGroup][i])/(pieTotal);
    if(animate)
    {
      dataElems.push(pathElem);
      pathElem.setAttribute("transform",defaultTransform);
      pieAnimAngles.push(pieStart+valueRatio/2);
    }
    var arcBeginX, arcBeginY, arcEndX, arcEndY;
    
    arcBeginX = pieSize*Math.cos(pieStart*Math.PI*2);
    arcBeginY = pieSize*Math.sin(pieStart*Math.PI*2); 
    var sb = new ApacheChartBuffer();
    sb.append("M0,0L").append(arcBeginX).append(",").append(arcBeginY);

    arcEndX = pieSize*Math.cos((pieStart+valueRatio)*Math.PI*2);
    arcEndY = pieSize*Math.sin((pieStart+valueRatio)*Math.PI*2);
    
    if (valueRatio >= .5) 
    {
      sb.append("A").append(pieSize).append(" ").append(pieSize).append(" 1 0 0 ");
      sb.append(arcEndX).append(",").append(arcEndY);
    }
    else
    {
      sb.append("A").append(pieSize).append(" ").append(pieSize).append(" 1 1 0 ");
      sb.append(arcEndX).append(",").append(arcEndY);
    }
    sb.append("z");
    if(gradientsUsed)
      pathElem.setAttribute("fill", "url(#gradient"+i+")");
    else
      pathElem.setAttribute("fill", seriesColors[i]);
    pathElem.setAttribute("stroke", seriesColors[i]);
    pathElem.setAttribute("stroke-width", 1);
    pathElem.setAttribute("yValueIndex", iGroup);
    pathElem.setAttribute("seriesIndex", i);
    if(this._tooltipsVisible)
    {
      pathElem.addEventListener("mouseover",this.ShowToolTipCallback,false);
      pathElem.addEventListener("mouseout",this.HideToolTipCallback,false);      
    }
    pathElem.addEventListener("click",this.ClickCallback,false);

    var pathRingElem = pathElem.cloneNode(false);
    var pathEdgeElem = pathElem.cloneNode(false);
    if(animate)
    {
      dataElems.push(pathRingElem);
      pathRingElem.setAttribute("transform",defaultTransform);
      dataElems.push(pathEdgeElem);
      pathEdgeElem.setAttribute("transform",defaultTransform);
    }
    pathElem.setAttribute("d", sb.toString());
    
    sb = new ApacheChartBuffer();
    sb.append("M").append(arcBeginX).append(",").append(arcBeginY);
    if (valueRatio >= .5) // major arc
    {
      sb.append("A").append(pieSize).append(" ").append(pieSize).append(" 1 0 0 ");
      sb.append(arcEndX).append(",").append(arcEndY);
    }
    else
    {
      sb.append("A").append(pieSize).append(" ").append(pieSize).append(" 1 1 0 ");
      sb.append(arcEndX).append(",").append(arcEndY);
    }
        
    sb.append("v").append(perspectiveHeight);
    if (valueRatio >= .5) // major arc
    {
      sb.append("A").append(pieSize).append(" ").append(pieSize).append(" 1 0 1 ");
      sb.append(arcBeginX).append(",").append(arcBeginY+perspectiveHeight);
    }
    else
    {
      sb.append("A").append(pieSize).append(" ").append(pieSize).append(" 1 1 1 ");
      sb.append(arcBeginX).append(",").append(arcBeginY+perspectiveHeight);
    }
    
    sb.append("z");
    pathRingElem.setAttribute("d", sb.toString());
    
    sb = new ApacheChartBuffer();
    sb.append("M0,0L");
    sb.append(arcBeginX).append(",").append(arcBeginY);
    sb.append("v").append(perspectiveHeight);
    sb.append("L").append(0).append(",").append(perspectiveHeight);
    sb.append("z");
    sb.append("M0,0L");
    sb.append(arcEndX).append(",").append(arcEndY);
    sb.append("v").append(perspectiveHeight);
    sb.append("L").append(0).append(",").append(perspectiveHeight);
    sb.append("z");
    pathEdgeElem.setAttribute("d", sb.toString());
    
    pieStart += valueRatio;
    pieElems[i] = pathElem;
    ringElems[i] = pathRingElem;
    edgeElems[i] = pathEdgeElem;
  }
  
  // For the top half, edges have preference over rings
  var totalRatio = 0;
  for (var i = 0; i< nPies; ++i)
  {
    if(totalRatio <= .5)
      pieContainer.appendChild(ringElems[i]);
    totalRatio += (yValues[iGroup][i])/(pieTotal);
  }
  totalRatio = 0;
  for (var i = 0; i< nPies; ++i)
  {
    if(totalRatio <= .5)
      pieContainer.appendChild(edgeElems[i]);
    totalRatio += (yValues[iGroup][i])/(pieTotal);
  }
  
  // For the bottom half, rings have preference over edges
  totalRatio = 0;
  for (var i = 0; i< nPies; ++i)
  {
    if(totalRatio > .5)
      pieContainer.appendChild(edgeElems[i]);
    totalRatio += (yValues[iGroup][i])/(pieTotal);
  }

  totalRatio = 0;
  for (var i = 0; i< nPies; ++i)
  {
    if(totalRatio > .5)
      pieContainer.appendChild(ringElems[i]);
    totalRatio += (yValues[iGroup][i])/(pieTotal);
  }  
  
  for (var i = 0; i< nPies; ++i)
  {
    pieContainer.appendChild(pieElems[i]);
  }  
}

ApachePieChart.prototype.GetToolTipLocation = function(e, ttBBox)
{
  var evtTarget = e.target;
  var targetBBox = evtTarget.getBBox();
  var ctm = evtTarget.parentNode.getCTM();
  return {x:(ctm.e+targetBBox.x+targetBBox.width/2), 
          y:(ctm.f+targetBBox.y+targetBBox.height/2 - ttBBox.height)};
}

ApachePieChart._MAX_PERSPECTIVE_HEIGHT = 30;	
////////////////////////////////////////////////////////////////////
// Area chart subclass
////////////////////////////////////////////////////////////////////
function ApacheAreaChart(
  type, model, svgEmbedId, 
  isPerspective, legendPosition)
{
  this.Init(type, model, svgEmbedId, isPerspective, legendPosition);
}

ApacheChartObj.Inherit(ApacheChart, ApacheAreaChart);

ApacheAreaChart.prototype.Init = function(
  type, model, svgEmbedId, 
  isPerspective, legendPosition)
{
  ApacheAreaChart.superclass.Init.call(this, type, model, svgEmbedId, 
                                 isPerspective, legendPosition);
  this._toolTips = [];
}

ApacheAreaChart.prototype.SetStopOpacity = function(stopNode)
{
  // In gecko opacity does not mix with stop-opacity, so use a lower value
  stopNode.setAttribute("stop-opacity", ApacheChart._DEFAULT_STOP_OPACITY/2);
}

ApacheAreaChart.prototype.DrawChartData = function()
{
  var rootElem = this._rootElement;
  
  if(this._tooltipsVisible)
  {
    rootElem.addEventListener("mousemove",this.ShowToolTipCallback,false);
    rootElem.addEventListener("mouseout",this.HideToolTipCallback,false);
  }
      
  if(this._isPerspective)
    this._drawPerspectiveAreas();
  else
    this._drawAreas();
}

ApacheAreaChart.prototype.SetDataAnimStep = function(ratio)
{
  var model = this._model, 
      seriesLabels = model.getSeriesLabels(), seriesCount = seriesLabels.length;
  var yValues = model.getYValues(), yValueCount = yValues.length;
  var animElems = this._dataElems, animPathCount = (this._isPerspective)? (yValueCount-1):1;
  var margins = this._margins, marginBottom = margins.bottom;
  var cy = (this._height - marginBottom);
  var newRatio = ratio*seriesCount, animSeriesIndex = 0;
  if(newRatio > 1)
  {
    animSeriesIndex = Math.floor(newRatio);
    if(animSeriesIndex >= seriesCount)
      animSeriesIndex = seriesCount - 1;
    newRatio = newRatio - Math.floor(newRatio);
  }

  // We will make each series appear separately
  var i = animSeriesIndex; 
  for (var j = 0; j < animPathCount; ++j)
  {
    var ty = (1-newRatio)*cy;
    animElems[i*animPathCount+j].setAttribute("transform", "translate(0,"+ty+") scale(1,"+newRatio+")");
    if(i>0)
    {
      animElems[(i-1)*animPathCount+j].setAttribute("transform", "scale(1,1)");
    }
  }
  // make sure that everything is scaled properly at the end
  if(ratio == 1)
  {
    for(var i = 0; i < seriesCount; ++i)
    {
      for (var j = 0; j < animPathCount; ++j)
      {
        animElems[i*animPathCount+j].setAttribute("transform", "scale(1,1)");
      }
    }
  }
}

/**
 * Overridden to indicate that the group label is edge aligned instead of center aligned
 */
ApacheAreaChart.prototype.IsGroupLabelCentered = function()
{
  return false;
}

ApacheAreaChart.prototype.GetVLineCount = function()
{
  var xMajorCount = this._xMajorGridCount;
  if(xMajorCount >= 0)
    return xMajorCount;
  else
  {
    // Area Chart has one vertical line less since the 
    // first line represents a value
    return this._model.getGroupLabels().length-1;
  }
}

ApacheAreaChart.prototype._drawAreas = function()
{
  var svgDoc = this._svgDoc, model = this._model, margins = this._margins;
  var rootElem = this._rootElement, dataElems = this._dataElems, animate = (this._animDuration>0);
  var marginLeft = margins.left, marginTop = margins.top; 
  var gridWidth = (this._width - marginLeft - margins.right);
  var gridHeight = (this._height - marginTop - margins.bottom);
  var pathElem = svgDoc.getElementById("areaPathPrototype");
  var groupLabels = model.getGroupLabels(), groupCount = groupLabels.length, 
      seriesLabels = model.getSeriesLabels(), seriesCount = seriesLabels.length;
  var seriesColors = model.getSeriesColors(), yValues = model.getYValues();    
  var minValue = model.getMinYValue(), maxValue = model.getMaxYValue();    
  var isStacked = (this._type == ApacheChart.TYPE_AREA_STACKED);
  var yValueCount = yValues.length;
  var barWidth = (gridWidth/(Math.max(yValueCount,groupCount)-1));
  var gradientsUsed = this._gradientsUsed;
  var defaultTransform = "scale(1,0.00001)";
  var dx, dy, cumYs = [], stackBase;
  for (var i = 0; i< seriesCount; ++i)
  {
    dx = marginLeft;
    dy = marginTop + gridHeight;
    var sb = new ApacheChartBuffer();
    pathElem = pathElem.cloneNode(false);
    if(i == 0 || !isStacked)
      sb.append("M").append(dx).append(",").append(dy);
    else if(isStacked)
      sb.append("M").append(dx).append(",").append(cumYs[0]);
    
    // If we use non zero min and it is a stacked graph, we need to remove the min for only
    // the first series.
    stackBase = (i==0?minValue:0);
    for (var j = 0; j < yValueCount; ++j)
    {
      if(isStacked)
      {
        if(null == cumYs[j])
          cumYs[j] = gridHeight + marginTop;
          
        dy = (cumYs[j] -= gridHeight*(yValues[j][i]-stackBase)/(maxValue-minValue));
      }
      else
        dy = gridHeight + marginTop - gridHeight*(yValues[j][i]- minValue)/(maxValue-minValue);
      
      sb.append("L").append(dx).append(",").append(dy);    

      if(j != yValueCount - 1)      
        dx += barWidth;
    }
    if(animate)
    {
      dataElems.push(pathElem);
      pathElem.setAttribute("transform",defaultTransform); 
    }
    
    if(gradientsUsed)
      pathElem.setAttribute("fill", "url(#gradient"+i+")");
    else
      pathElem.setAttribute("fill", seriesColors[i]);
    pathElem.setAttribute("stroke", seriesColors[i]);
    pathElem.setAttribute("stroke-width", 1);
    pathElem.setAttribute("seriesIndex", i);
    pathElem.addEventListener("click",this.ClickCallback,false); 
    
    if(i == 0 || !isStacked)
    {
      sb.append("L").append(dx).append(",").append(gridHeight + marginTop);
      sb.append("Z");
    }
    else
    {
      for (var j = yValueCount-1; j>=0; --j)
      {
        var prevY = cumYs[j]+gridHeight*(yValues[j][i]-stackBase)/(maxValue-minValue);
        sb.append("L").append(dx).append(",").append(prevY);
        dx -= barWidth;
      }
    }
    pathElem.setAttribute("d", sb.toString());
    rootElem.appendChild(pathElem);
  }
}

ApacheAreaChart.prototype._drawPerspectiveAreas = function()
{
  var svgDoc = this._svgDoc, model = this._model, margins = this._margins;
  var rootElem = this._rootElement, dataElems = this._dataElems, animate = (this._animDuration>0);
  var xOffset = ApacheChart._XOFFSET_PERSPECTIVE, yOffset = ApacheChart._YOFFSET_PERSPECTIVE;
  var marginLeft = margins.left, marginTop = margins.top; 
  var gridWidth = (this._width - marginLeft - margins.right - xOffset);
  var gridHeight = (this._height - marginTop - margins.bottom - yOffset);
  var pathElem = svgDoc.getElementById("areaPathPrototype");
  var groupLabels = model.getGroupLabels(), groupCount = groupLabels.length, 
      seriesLabels = model.getSeriesLabels(), seriesCount = seriesLabels.length;
  var seriesColors = model.getSeriesColors(), yValues = model.getYValues();    
  var isStacked = (this._type == ApacheChart.TYPE_AREA_STACKED);
  var minValue = model.getMinYValue(), maxValue = model.getMaxYValue();    
  var yValueCount = yValues.length;
  var barWidth = (gridWidth/(Math.max(yValueCount,groupCount)-1)), stackBase;
  var gridBottom = gridHeight + marginTop + yOffset, dx, dy, cumYs = [];
  var gradientsUsed = this._gradientsUsed;
  var defaultTransform = "scale(1,0.00001)";
  for (var i = 0; i< seriesCount; ++i)
  {
    dx = marginLeft;
    var sb = new ApacheChartBuffer();
    pathElem = pathElem.cloneNode(false);
    
    // If we use non zero min and it is a stacked graph, we need to remove the min for only
    // the first series.
    stackBase = (i==0?minValue:0);    
    
    for (var j = 0; j < yValueCount; ++j)
    { 
      if(isStacked)
      {
        if(null == cumYs[j])
          cumYs[j] = gridBottom;
          
        dy = (cumYs[j] -= gridHeight*(yValues[j][i]-stackBase)/(maxValue-minValue));
      }
      else
        dy = gridBottom - gridHeight*(yValues[j][i]-minValue)/(maxValue-minValue);
        
      if(j != yValueCount - 1)
      {
        pathElem = pathElem.cloneNode(false);
        sb.append("M").append(dx).append(",").append(dy);
        sb.append("l").append(xOffset).append(",").append(-yOffset);
        var nextdy, nextdx = dx+barWidth;
        if(isStacked)
        {
          if(null == cumYs[j+1])
            cumYs[j+1] = gridBottom;
            
          nextdy = (cumYs[j+1] - gridHeight*(yValues[j+1][i]-stackBase)/(maxValue-minValue));
        }
        else
          nextdy = gridBottom - gridHeight*(yValues[j+1][i]-minValue)/(maxValue-minValue)
        sb.append("L").append(nextdx+xOffset).append(",").append(nextdy-yOffset);
        sb.append("l").append(-xOffset).append(",").append(yOffset);
        sb.append("L").append(dx).append(",").append(dy);
        sb.append("M").append(nextdx).append(",").append(nextdy);
        sb.append("l").append(xOffset).append(",").append(-yOffset);
        
        var prevSeriesY, prevSeriesY2;
        if(i == 0 || !isStacked)
        {
          sb.append("L").append(nextdx+xOffset).append(",").append(gridHeight + marginTop);  
        }
        else
        {
          
          sb.append("L").append(nextdx+xOffset).append(",").append(cumYs[j+1]-yOffset);
        }
        
        sb.append("l").append(-xOffset).append(",").append(yOffset);
        sb.append("L").append(nextdx).append(",").append(nextdy);
        
        sb.append("M").append(dx).append(",").append(dy);
        sb.append("L").append(nextdx).append(",").append(nextdy);
        
        if(i == 0 || !isStacked)
        {
          sb.append("L").append(nextdx).append(",").append(gridBottom);
          sb.append("L").append(dx).append(",").append(gridBottom);
        }
        else
        {
          sb.append("L").append(nextdx).append(",").append(cumYs[j+1]);
          sb.append("L").append(dx).append(",").append(
            cumYs[j]+gridHeight*(yValues[j][i]-stackBase)/(maxValue-minValue));
        }
        sb.append("L").append(dx).append(",").append(dy);
        if(gradientsUsed)
          pathElem.setAttribute("fill", "url(#gradient"+i+")");
        else
          pathElem.setAttribute("fill", seriesColors[i]);
        pathElem.setAttribute("stroke", seriesColors[i]);
        pathElem.setAttribute("stroke-width", 1);
        pathElem.setAttribute("yValueIndex", j);
        pathElem.setAttribute("seriesIndex", i);
        pathElem.addEventListener("click",this.ClickCallback,false); 
        dx += barWidth;
        pathElem.setAttribute("d", sb.toString());
        rootElem.appendChild(pathElem);
        if(animate)
        {
          dataElems.push(pathElem);
          pathElem.setAttribute("transform",defaultTransform); 
        }
      }       
    }
  }
}

ApacheAreaChart.prototype.GetChartEvent = function(e, seriesYs)
{
  var clientX = e.clientX, clientY = e.clientY, evtTarget = e.target;
  var isStacked = (this._type == ApacheChart.TYPE_AREA_STACKED);
  var isPerspective = this._isPerspective;
  var xOffset = ApacheChart._XOFFSET_PERSPECTIVE, yOffset = ApacheChart._YOFFSET_PERSPECTIVE;
  var model = this._model, yValues = model.getYValues();
  var groupLabels = model.getGroupLabels(), groupCount = groupLabels.length;
  var minValue = model.getMinYValue(), maxValue = model.getMaxYValue();
  var margins = this._margins, marginLeft = margins.left, marginTop = margins.top; 
  var gridWidth = (this._width - marginLeft - margins.right);
  var gridHeight = (this._height - marginTop - margins.bottom);
  var yValueCount = yValues.length;
  var barWidth = (gridWidth/(Math.max(yValueCount,groupCount)-1));
  if(isPerspective)
  {
    gridWidth -= xOffset;
    gridHeight -= yOffset;
  }
  
  if(clientX < marginLeft || 
     clientX>(marginLeft + gridWidth + (isPerspective?xOffset:0)) ||
     clientY < marginTop || 
     clientY>(marginTop + gridHeight + (isPerspective?yOffset:0)))
  {
    return null;
  }
  
  var dx, dy, dy1, cumYs = [], seriesIndices = [], seriesValues = [];
  var gridBottom = gridHeight + marginTop +(isPerspective?yOffset:0);
  var seriesCount = model.getSeriesLabels().length, stackBase, insideStacked = false;
  if(!seriesYs)
    seriesYs = [];
  for (var i = 0; i< seriesCount && !insideStacked; ++i)
  {
    dx = marginLeft;
    stackBase = (i==0?minValue:0);      
    for (var j = 0; j < yValueCount; ++j)
    {
      if(isStacked)
      {
        if(null == cumYs[j])
          cumYs[j] = gridBottom;
        if(null == cumYs[j+1] && (j != yValueCount -1))
          cumYs[j+1] = gridBottom;
        cumYs[j] -= gridHeight*(yValues[j][i]-stackBase)/(maxValue-minValue);
      }
      if(j == yValueCount - 1)
        continue;
      if(clientX > dx && clientX < (dx+barWidth))
      {
        if(isStacked)
        {
          dy1 = cumYs[j];
          dy2 = (cumYs[j+1] - gridHeight*(yValues[j+1][i]-stackBase)/(maxValue-minValue));
          dy = dy1 - (dy1-dy2)*(clientX - dx)/barWidth;
          if(clientY >= dy)
          {
            value = yValues[j][i] + (yValues[j+1][i]-yValues[j][i])*(clientX-dx)/barWidth;
            seriesValues.push(value);
            seriesIndices.push(i);
            seriesYs.push(dy);
            insideStacked = true;
            break;
          }
        }
        else 
        {
          dy1 = gridBottom - 
                gridHeight*(yValues[j][i]-minValue)/(maxValue-minValue);
          
          dy = dy1 - (gridHeight*(yValues[j+1][i]-yValues[j][i])/(maxValue-minValue))*(clientX-dx)/barWidth;

          // find all the series that the y point matches
          if(dy<=clientY)
          {
            value = yValues[j][i] + (yValues[j+1][i]-yValues[j][i])*(clientX - dx)/barWidth;
            seriesValues.push(value);
            seriesIndices.push(i);
            seriesYs.push(dy);
          }
          break;
        }
      }
      dx += barWidth; 
    }
  }
  return new ApacheChartEvent(seriesIndices,null, seriesValues,null);
}

ApacheAreaChart.prototype.ShowToolTip = function(e)
{  
  
  // Hide any existing tooltips
  this.HideToolTip();
  
  var seriesYs = [];
  var chartEvent = this.GetChartEvent(e, seriesYs);
  if(chartEvent == null || chartEvent.getYValues().length == 0)
  {
    return;
  }
  
  this._displayToolTips(chartEvent.getYValues(), chartEvent.getSeriesIndices(), seriesYs, e);
}

ApacheAreaChart.prototype._displayToolTips = function(
  seriesValues, seriesIndices, 
  seriesYs, e)
{
  var svgDoc = this._svgDoc, rootElem = this._rootElement;
  var model = this._model, seriesLabels = model.getSeriesLabels(), seriesCount = seriesLabels.length,
      seriesColors = model.getSeriesColors();
  var tooltipCount = seriesIndices.length, toolTips = this._toolTips;
  var clientX = e.clientX;
  var dx, dy;
  for(var i = 0; i<tooltipCount; ++i)
  { 
    var seriesIndex = seriesIndices[i];
    var toolTip = toolTips[seriesIndex];
    var resizeOnInit = false;
    if (toolTip == null)
    {
      toolTip = svgDoc.getElementById("toolTip").cloneNode(true);
      rootElem.appendChild(toolTip);
      toolTips[seriesIndex] = toolTip;
      resizeOnInit = true;
    }
    
    toolTip.style.setProperty("visibility","visible","");
    
    var circleElem = toolTip.firstChild.nextSibling;
    var boundingRectElem = circleElem.nextSibling.nextSibling;
    
    //append all the series values as labels
    var textElem = boundingRectElem.nextSibling.nextSibling;
    var textElemCount = seriesValues.length;
    var rectWidth = textElem.getComputedTextLength();
    
    textElem.firstChild.data = 
        seriesLabels[seriesIndex]+":  " + this._formatValue(seriesValues[i]);
    
    // Initially the template tooltip has an extra text node
    if(resizeOnInit)
    {
      var rectHeight = parseInt(boundingRectElem.getAttribute("height"));
      var dy = parseInt(textElem.getAttribute("dy"));
      rectHeight -= dy;
      boundingRectElem.setAttribute("height",rectHeight);
      textElem = textElem.nextSibling.nextSibling;
      textElem.firstChild.data = "";
    }
    
    rectWidth += 2*ApacheChart._TEXT_MARGIN;
    boundingRectElem.setAttribute("width",rectWidth);
    
    var targetBBox = e.target.getBBox();
    var ttBBox = toolTip.getBBox();
    dx = clientX, dy = seriesYs[i]- ttBBox.height;
    if(dx + ttBBox.width > this._width)
    {
      dx -= ttBBox.width;
      circleElem.setAttribute("cx",boundingRectElem.getBBox().width);
    }
    else
    {
      circleElem.setAttribute("cx",0);
    }
    if(dy - ttBBox.height < 0)
    {
      dy += ttBBox.height;
      circleElem.setAttribute("cy",0);
    }
    else
    {
      circleElem.setAttribute("cy",boundingRectElem.getBBox().height);
    }
    boundingRectElem.setAttribute("stroke", seriesColors[seriesIndex]);
    circleElem.setAttribute("stroke",seriesColors[seriesIndex]);
    toolTip.setAttribute("transform","translate("+dx+","+dy+")");
  }
}

ApacheAreaChart.prototype.HideToolTip = function(e)
{
  var tooltips = this._toolTips, tooltipCount = tooltips.length;
  for(var i = 0; i<tooltipCount; ++i)
  { 
    var toolTip = tooltips[i];
    if(toolTip)
      toolTip.style.setProperty("visibility","hidden","");
  }
}

////////////////////////////////////////////////////////////////////
// Line chart subclass
////////////////////////////////////////////////////////////////////
function ApacheLineChart(
  type, model, svgEmbedId, 
  isPerspective, legendPosition)
{
  this.Init(type, model, svgEmbedId, isPerspective, legendPosition);
}

ApacheChartObj.Inherit(ApacheChart, ApacheLineChart);

ApacheLineChart.prototype.DrawChartData = function(isCombo)
{
  if(this._isPerspective)
    this.__drawPerspectiveLines(isCombo);
  else
    this.__drawLines(isCombo);
}

ApacheLineChart.prototype.AnimAlongXAxis = function()
{
  // always around x axis
  return true;  
}

ApacheLineChart.prototype.__drawLines = function(isCombo)
{
  var svgDoc = this._svgDoc, model = this._model, margins = this._margins;
  var rootElem = this._rootElement, dataElems = this._dataElems, 
      dotElems, animate = (this._animDuration>0);
  var marginLeft = margins.left, marginTop = margins.top; 
  var gridWidth = (this._width - marginLeft - margins.right);
  var gridHeight = (this._height - marginTop - margins.bottom);
  var lineDot = svgDoc.getElementById("lineDotPrototype"), 
      pathElem = svgDoc.getElementById("linePathPrototype");
  var groupLabels = model.getGroupLabels(), groupCount = groupLabels.length, 
      seriesLabels = model.getSeriesLabels(), seriesCount = seriesLabels.length;
  var seriesColors = model.getSeriesColors(), yValues = model.getYValues();    
  var minValue = model.getMinYValue(), maxValue = model.getMaxYValue();    
  var yValueCount = yValues.length;
  var barWidth = gridWidth/Math.max(yValueCount,groupCount);
  var dx, dy;
  var gradientsUsed = this._gradientsUsed;
  // Adobe plugin does not like 0 for scale and gecko does not like a low number for circles
  var defaultTransform = this._isIE?"scale(0.00001,1)":"scale(0,1)";

  for (var i = 0; i< seriesCount; ++i)
  {
    // For combo charts we will draw every alternate(odd) series
    if(isCombo && i%2 == 0)
      continue;
    dx = marginLeft+barWidth/2;
    var sb = new ApacheChartBuffer();
    pathElem = pathElem.cloneNode(false);
    
    for (var j = 0; j < yValueCount; ++j)
    {
      dy = gridHeight + marginTop - gridHeight*(yValues[j][i]-minValue)/(maxValue-minValue);
      if(j == 0)
        sb.append("M").append(dx).append(",").append(dy);
      else
        sb.append("L").append(dx).append(",").append(dy);
      lineDot = lineDot.cloneNode(false);
      lineDot.setAttribute("cx", dx);
      lineDot.setAttribute("cy", dy);
      
      if(gradientsUsed)
        lineDot.setAttribute("fill", "url(#gradient"+i+")");
      else
        lineDot.setAttribute("fill", seriesColors[i]);
        
      lineDot.setAttribute("stroke", seriesColors[i]);
      if(animate)
      {
        lineDot.setAttribute("transform",defaultTransform); 
        dataElems.push(lineDot); 
      }
      rootElem.appendChild(lineDot);
      
      // There is no fill for lines
      pathElem.setAttribute("stroke", seriesColors[i]);
      pathElem.setAttribute("seriesIndex", i);
      if(this._tooltipsVisible)
      {
        pathElem.addEventListener("mouseover",this.ShowToolTipCallback,false);
        pathElem.addEventListener("mouseout",this.HideToolTipCallback,false);
      }
      pathElem.addEventListener("click",this.ClickCallback,false); 
      dx += barWidth;
    }
    pathElem.setAttribute("d", sb.toString());
    rootElem.appendChild(pathElem);
    if(animate)
    {
      dataElems.push(pathElem);
      pathElem.setAttribute("transform",defaultTransform); 
    }
  }  
}

ApacheLineChart.prototype.__drawPerspectiveLines = function(isCombo)
{
  var svgDoc = this._svgDoc, model = this._model, margins = this._margins;
  var rootElem = this._rootElement, dataElems = this._dataElems, animate = (this._animDuration>0);
  var xOffset = ApacheChart._XOFFSET_PERSPECTIVE, yOffset = ApacheChart._YOFFSET_PERSPECTIVE;
  var marginLeft = margins.left, marginTop = margins.top; 
  var gridWidth = (this._width - marginLeft - margins.right - xOffset);
  var gridHeight = (this._height - marginTop - margins.bottom - yOffset);
  var pathElem = svgDoc.getElementById("linePath3dPrototype");
  var groupLabels = model.getGroupLabels(), groupCount = groupLabels.length, 
      seriesLabels = model.getSeriesLabels(), seriesCount = seriesLabels.length;
  var seriesColors = model.getSeriesColors(), yValues = model.getYValues();    
  var minValue = model.getMinYValue(), maxValue = model.getMaxYValue();    
  var yValueCount = yValues.length;
  var barWidth = (gridWidth/Math.max(yValueCount,groupCount));
  var gridBottom = gridHeight + marginTop + yOffset, dx, dy;
  var gradientsUsed = this._gradientsUsed;
  var defaultTransform = "scale(0.00001,1)";
  
  for (var i = 0; i< seriesCount; ++i)
  {
    // For combo charts we will draw every alternate(odd) series
    if(isCombo && i%2 == 0)
      continue;
    dx = marginLeft+barWidth/2;
    var sb = new ApacheChartBuffer();
    pathElem = pathElem.cloneNode(false);
    for (var j = 0; j < yValueCount; ++j)
    {      
      dy = gridBottom - gridHeight*(yValues[j][i]-minValue)/(maxValue-minValue);
            
      if(j != yValueCount - 1)
      {
        var sb = new ApacheChartBuffer();        
        pathElem = pathElem.cloneNode(false);
        sb.append("M").append(dx).append(",").append(dy);
        sb.append("l").append(xOffset).append(",").append(-yOffset);
        var nextdy = gridBottom - gridHeight*(yValues[j+1][i]-minValue)/(maxValue-minValue);
        var nextdx = dx+barWidth;
        sb.append("L").append(nextdx+xOffset).append(",").append(nextdy-yOffset);
        sb.append("l").append(-xOffset).append(",").append(yOffset);
        sb.append("L").append(dx).append(",").append(dy);
        if(gradientsUsed)
          pathElem.setAttribute("fill", "url(#gradient"+i+")");
        else
          pathElem.setAttribute("fill", seriesColors[i]);
        pathElem.setAttribute("stroke", seriesColors[i]);
        pathElem.setAttribute("seriesIndex", i);
        if(this._tooltipsVisible)
        {
          pathElem.addEventListener("mousemove",this.ShowToolTipCallback,false);
          pathElem.addEventListener("mouseout",this.HideToolTipCallback,false);
        }
        pathElem.addEventListener("click",this.ClickCallback,false); 
        dx += barWidth;
        pathElem.setAttribute("d", sb.toString());
        rootElem.appendChild(pathElem);
        if(animate)
        {
          dataElems.push(pathElem);
          pathElem.setAttribute("transform",defaultTransform); 
        }
      }        
    }
  }
}

ApacheLineChart.prototype.ShowToolTip = function(e)
{
  // first hide any existing tooltip
  this.HideToolTip();
  ApacheLineChart.superclass.ShowToolTip.call(this, e);
}

ApacheLineChart.prototype.GetToolTipLocation = function(e, ttBBox)
{
  return {x:(e.clientX+20), y:(e.clientY+20)};
}

ApacheLineChart.prototype.GetChartEvent = function(e)
{
  var evtTarget = e.target;
  var i = parseInt(evtTarget.getAttribute("seriesIndex"));

  var clientX = e.clientX;
  var isPerspective = this._isPerspective;
  var yOffset = ApacheChart._YOFFSET_PERSPECTIVE;
  var model = this._model, yValues = model.getYValues();
  var groupLabels = model.getGroupLabels(), groupCount = groupLabels.length;
  var margins = this._margins, marginLeft = margins.left, marginTop = margins.top; 
  var gridWidth = (this._width - marginLeft - margins.right);
  var gridHeight = (this._height - marginTop - margins.bottom);
  var yValueCount = yValues.length;  
  var barWidth = (gridWidth/Math.max(yValueCount,groupCount));
  var gridBottom = gridHeight + marginTop +(isPerspective?yOffset:0);
  var dx = marginLeft+barWidth/2, value = 0.0;
  for (var j = 0; j < yValueCount; ++j)
  {
    if(j == yValueCount - 1)
      continue;
    if(clientX > dx && clientX < (dx+barWidth))
    {
      value = yValues[j][i] + (yValues[j+1][i]-yValues[j][i])*(clientX - dx)/barWidth;
      break;
    }
    dx += barWidth; 
  }
  
  return new ApacheChartEvent([i],null, [value],null);
}

ApacheLineChart.prototype.FillToolTipData = function(boundingRectElem, circleElem, e)
{
  var chartEvent = this.GetChartEvent(e);
  var i = chartEvent.getSeriesIndices()[0], value = chartEvent.getYValues()[0];
  var seriesLabels = this._model.getSeriesLabels();
  
  var textElem = boundingRectElem.nextSibling.nextSibling;
  textElem.firstChild.data = seriesLabels[i]+
                ": "+this._formatValue(value);
                
  var labelWidth = textElem.getComputedTextLength();      
  //We do not need the next label 
  textElem = textElem.nextSibling.nextSibling;
  textElem.firstChild.data = "";
  var rectWidth = labelWidth+2*ApacheChart._TEXT_MARGIN;
  boundingRectElem.setAttribute("width",rectWidth);
  boundingRectElem.setAttribute("stroke", seriesColors[i]);
  circleElem.setAttribute("r",0);
}

////////////////////////////////////////////////////////////////////
// ScatterPlot chart subclass
////////////////////////////////////////////////////////////////////
function ApacheScatterPlotChart(
  type, model, svgEmbedId, 
  isPerspective, legendPosition)
{
  this.Init(type, model, svgEmbedId, isPerspective, legendPosition);
}

ApacheChartObj.Inherit(ApacheChart, ApacheScatterPlotChart);

ApacheScatterPlotChart.prototype.Init = function(
  type, model, svgEmbedId, 
  isPerspective, legendPosition)
{
  ApacheScatterPlotChart.superclass.Init.call(this, type, model, svgEmbedId, 
                               isPerspective, legendPosition);
  //this._cxs = undefined;
  //this._cys = undefined;
}

ApacheScatterPlotChart.prototype.DrawChartData = function()
{
  if(this._isPerspective)
    this._drawPerspectivePoints();
  else
    this._drawPoints();
}

ApacheScatterPlotChart.prototype.SetDataAnimStep = function(ratio)
{
  var isPerspective = this._isPerspective;
  var cxs = this._cxs, cys = this._cys, gridCx, gridCy;
  var margins = this._margins, marginLeft = margins.left, marginTop = margins.top;
  var gridWidth = (this._width - marginLeft - margins.right);
  var gridHeight = (this._height - marginTop - margins.bottom);
  var animElems = this._dataElems, animCount = cxs.length, elemIndex = 0;
  
  if(isPerspective)
    marginLeft += ApacheChart._XOFFSET_PERSPECTIVE;
  gridCx = gridWidth/2 + marginLeft;
  gridCy = gridHeight/2 + marginTop;
  // we are going to animate by starting the dot at the middle and work towards its destination
  for(var i = 0; i < animCount; ++i)
  {
    var cx = gridCx - (gridCx - cxs[i])*ratio;
    var cy = gridCy - (gridCy - cys[i])*ratio;
    var elem = animElems[elemIndex++];
    elem.setAttribute("cx", cx); 
    elem.setAttribute("cy", cy);
    if(isPerspective)
    {
      elem = animElems[elemIndex++];
      elem.setAttribute("cx", cx); 
      elem.setAttribute("cy", cy);
    }
  }
}

ApacheScatterPlotChart.prototype.SetGridAnimStep = function(ratio)
{
  var animElems = this._gridElems, animCount = animElems.length;
  for(var i = 0; i < animCount; ++i)
  {
    animElems[i].setAttribute("fill-opacity", ratio);
    animElems[i].setAttribute("transform", "scale(1,1)");
  }
}

/**
 * Overridden to indicate that the group label is edge aligned instead of center aligned
 */
ApacheScatterPlotChart.prototype.IsGroupLabelCentered = function()
{
  return false;
}


ApacheScatterPlotChart.prototype.GetVLineCount = function()
{
  var xMajorCount = this._xMajorGridCount;
  if(xMajorCount >= 0)
    return xMajorCount;
  else
  {
    // Area Chart has one vertical line less since the 
    // first line represents a value
    return this._model.getGroupLabels().length-1;
  }  
}

ApacheScatterPlotChart.prototype._drawPoints = function()
{
  var svgDoc = this._svgDoc, model = this._model, margins = this._margins;
  var rootElem = this._rootElement, dataElems = this._dataElems, animate = (this._animDuration>0);
  var marginLeft = margins.left, marginTop = margins.top; 
  var gridWidth = (this._width - marginLeft - margins.right);
  var gridHeight = (this._height - marginTop - margins.bottom);
  var dotElem = svgDoc.getElementById("scatterDotPrototype");
  var groupLabels = model.getGroupLabels(), groupCount = groupLabels.length, 
      seriesLabels = model.getSeriesLabels(), seriesCount = seriesLabels.length;
  var seriesColors = model.getSeriesColors(), xValues = model.getXValues(), 
      yValues = model.getYValues(), nValues = yValues.length;    
  var defaultTransform = "translate(0,100000)";
  var minYValue = model.getMinYValue(), maxYValue = model.getMaxYValue();
  var minXValue = model.getMinXValue(), maxXValue = model.getMaxXValue();
  var barWidth = (gridWidth/(groupCount-1));
  var cxs, cys, dx, dy, gridCx, gridCY;
  var gradientsUsed = this._gradientsUsed;
  
  if(animate)
  {
    cxs = this._cxs = [];
    cys = this._cys = [];
    gridCx = gridWidth/2 + marginLeft;
    gridCy = gridHeight/2 + marginTop;
  }
  
  for (var i = 0; i< seriesCount; ++i)
  {
    for (var j = 0; j < nValues; ++j)
    {
      dy = gridHeight + marginTop - gridHeight*(yValues[j][i]-minYValue)/(maxYValue-minYValue);
      dx = marginLeft + gridWidth*(xValues[j][i]-minXValue)/(maxXValue-minXValue);
      dotElem = dotElem.cloneNode(false);
      if(gradientsUsed)
        dotElem.setAttribute("fill", "url(#gradient"+i+")");
      else
        dotElem.setAttribute("fill", seriesColors[i]);
      dotElem.setAttribute("stroke", seriesColors[i]);
      dotElem.setAttribute("yValueIndex", j);
      dotElem.setAttribute("seriesIndex", i);
      if(this._tooltipsVisible)
      {
        dotElem.addEventListener("mouseover",this.ShowToolTipCallback,false);
        dotElem.addEventListener("mouseout",this.HideToolTipCallback,false);
      }
      dotElem.addEventListener("click",this.ClickCallback,false);    
      if(animate)
      {
        dataElems.push(dotElem);
        dotElem.setAttribute("cx", gridCx);
        dotElem.setAttribute("cy", gridCy);
        // we will use it during animation
        cxs.push(dx);
        cys.push(dy);
      }
      else
      {
        dotElem.setAttribute("cx", dx);
        dotElem.setAttribute("cy", dy);
      }
      rootElem.appendChild(dotElem);
    }    
  }
}

ApacheScatterPlotChart.prototype._drawPerspectivePoints = function()
{
  var svgDoc = this._svgDoc, model = this._model, margins = this._margins;
  var rootElem = this._rootElement, dataElems = this._dataElems, animate = (this._animDuration>0);
  var xOffset = ApacheChart._XOFFSET_PERSPECTIVE, yOffset = ApacheChart._YOFFSET_PERSPECTIVE;
  var marginLeft = margins.left, marginTop = margins.top; 
  var gridWidth = (this._width - marginLeft - margins.right - xOffset);
  var gridHeight = (this._height - marginTop - margins.bottom - yOffset);
  var dotElem = svgDoc.getElementById("scatter3dDotPrototype");
  var groupLabels = model.getGroupLabels(), groupCount = groupLabels.length, 
      seriesLabels = model.getSeriesLabels(), seriesCount = seriesLabels.length;
  var seriesColors = model.getSeriesColors(), xValues = model.getXValues(), 
      yValues = model.getYValues(), nValues = yValues.length;    
  var minYValue = model.getMinYValue(), maxYValue = model.getMaxYValue();
  var minXValue = model.getMinXValue(), maxXValue = model.getMaxXValue();
  var barWidth = (gridWidth/(groupCount-1));
  var gridBottom = gridHeight + marginTop + yOffset, cxs, cys, dx, dy, gridCx, gridCY;
  var gradientsUsed = this._gradientsUsed;
  if(animate)
  {
    cxs = this._cxs = [];
    cys = this._cys = [];
    gridCx = gridWidth/2 + marginLeft +xOffset;
    gridCy = gridHeight/2 + marginTop;
  }
  
  for (var i = 0; i< seriesCount; ++i)
  {
    for (var j = 0; j < nValues; ++j)
    { 
      dy = gridBottom - gridHeight*(yValues[j][i]-minYValue)/(maxYValue-minYValue);
      dx = marginLeft + gridWidth*(xValues[j][i]-minXValue)/(maxXValue-minXValue);
      dotElem = dotElem.cloneNode(false);
      if(animate)
      {
        dataElems.push(dotElem);
        dotElem.setAttribute("cx", gridCx);
        dotElem.setAttribute("cy", gridCy);
        // we will use it during animation
        cxs.push(dx);
        cys.push(dy);
      }
      else
      {
        dotElem.setAttribute("cx", dx);
        dotElem.setAttribute("cy", dy);
      }
      if(gradientsUsed)
        dotElem.setAttribute("fill", "url(#gradient"+i+")");
      else
        dotElem.setAttribute("fill", seriesColors[i]);
      dotElem.setAttribute("stroke", seriesColors[i]);
      dotElem.setAttribute("yValueIndex", j);
      dotElem.setAttribute("seriesIndex", i);
      if(this._tooltipsVisible)
      {
        dotElem.addEventListener("mouseover",this.ShowToolTipCallback,false);
        dotElem.addEventListener("mouseout",this.HideToolTipCallback,false);
      }
      dotElem.addEventListener("click",this.ClickCallback,false);
      var shadowElem = dotElem.cloneNode(false);
      if(animate)
      {
        dataElems.push(shadowElem);
      }
      shadowElem.setAttribute("fill","#333333");
      shadowElem.setAttribute("opacity","0.5");
      shadowElem.setAttribute("stroke","none");
      shadowElem.setAttribute("transform", "translate(3,3)");
      rootElem.appendChild(shadowElem);
      rootElem.appendChild(dotElem);
    }
  }
}

ApacheScatterPlotChart.prototype.GetChartEvent = function(e)
{
  var evtTarget = e.target;
  var i = parseInt(evtTarget.getAttribute("seriesIndex")), 
      j = parseInt(evtTarget.getAttribute("yValueIndex"));

  var model = this._model, xValues = model.getXValues(), 
      yValues = model.getYValues();
  
  
  return new ApacheChartEvent([i],[j], [yValues[j][i]],[xValues[j][i]]);
}

ApacheScatterPlotChart.prototype.FillToolTipData = function(boundingRectElem, circleElem, e)
{
  var chartEvent = this.GetChartEvent(e);
  var i = chartEvent.getSeriesIndices()[0],
      yValue = chartEvent.getYValues()[0]
      xValue = chartEvent.getXValues()[0];
  var model = this._model, seriesLabels = model.getSeriesLabels();
      
  var textElem = boundingRectElem.nextSibling.nextSibling;
  textElem.firstChild.data = seriesLabels[i]+
                ": ("+ this._formatValue(xValue) + 
                ")    (" + this._formatValue(yValue) +")";
                
  var labelWidth = textElem.getComputedTextLength();      
  //We do not need the next label 
  textElem = textElem.nextSibling.nextSibling;
  textElem.firstChild.data = "";
  var rectWidth = labelWidth+2*ApacheChart._TEXT_MARGIN;
  boundingRectElem.setAttribute("width",rectWidth);
  boundingRectElem.setAttribute("stroke", seriesColors[i]);
  circleElem.setAttribute("r",0);
}


////////////////////////////////////////////////////////////////////
// XYLine Chart subclass
// Note: chart x values should be in the ascending order other wise 
//       the chart does not make sense
////////////////////////////////////////////////////////////////////
function ApacheXYLineChart(
  type, model, svgEmbedId, 
  isPerspective, legendPosition)
{
  this.Init(type, model, svgEmbedId, isPerspective, legendPosition);
}

ApacheChartObj.Inherit(ApacheScatterPlotChart, ApacheXYLineChart);

ApacheXYLineChart.prototype.SetDataAnimStep = function(ratio)
{
  // bypass the parent and go directly into Chart
  ApacheChart.prototype.SetDataAnimStep.call(this, ratio);
}

ApacheXYLineChart.prototype.DrawChartData = function()
{
  if(this._isPerspective)
    this._drawPerspectiveXYValues();
  else
    this._drawXYValues();
}

ApacheXYLineChart.prototype.AnimAlongXAxis = function()
{
  // always around x axis
  return true;  
}

ApacheXYLineChart.prototype._drawXYValues = function()
{
  var svgDoc = this._svgDoc, model = this._model, margins = this._margins;
  var rootElem = this._rootElement, dataElems = this._dataElems, animate = (this._animDuration>0);
  var marginLeft = margins.left, marginTop = margins.top; 
  var gridWidth = (this._width - marginLeft - margins.right);
  var gridHeight = (this._height - marginTop - margins.bottom);
  var dotElem, pathElem;
  var groupLabels = model.getGroupLabels(), groupCount = groupLabels.length, 
      seriesLabels = model.getSeriesLabels(), seriesCount = seriesLabels.length;
  var seriesColors = model.getSeriesColors(), xValues = model.getXValues(), 
      yValues = model.getYValues(), nValues = yValues.length;    
  var defaultTransform = "scale(0.00001,1)";
  var minYValue = model.getMinYValue(), maxYValue = model.getMaxYValue();
  var minXValue = model.getMinXValue(), maxXValue = model.getMaxXValue();
  var barWidth = (gridWidth/(groupCount-1));
  var dx, dy;
  
  pathElem = svgDoc.getElementById("linePathPrototype");
    
  for (var i = 0; i< seriesCount; ++i)
  {
    var sb = new ApacheChartBuffer();
    dx = marginLeft;
    dy = gridHeight + marginTop;
    pathElem = pathElem.cloneNode(false);
    if(animate)
    {
      dataElems.push(pathElem);
      pathElem.setAttribute("transform",defaultTransform);
    }
    for (var j = 0; j < nValues; ++j)
    {
      dy = gridHeight + marginTop - gridHeight*(yValues[j][i]-minYValue)/(maxYValue-minYValue);
      dx = marginLeft + gridWidth*(xValues[j][i]-minXValue)/(maxXValue-minXValue);
      if(j==0)
        sb.append("M").append(dx).append(",").append(dy);
      else
        sb.append("L").append(dx).append(",").append(dy);
    }
    
    pathElem.setAttribute("seriesIndex", i);
    pathElem.setAttribute("stroke", seriesColors[i]);
    if(this._tooltipsVisible)
    {
      pathElem.addEventListener("mousemove",this.ShowToolTipCallback,false);
      pathElem.addEventListener("mouseout",this.HideToolTipCallback,false);
    }
    pathElem.addEventListener("click",this.ClickCallback,false);    
    pathElem.setAttribute("d", sb.toString());
    rootElem.appendChild(pathElem);
  }
}

ApacheXYLineChart.prototype._drawPerspectiveXYValues = function()
{
  var svgDoc = this._svgDoc, model = this._model, margins = this._margins;
  var rootElem = this._rootElement, dataElems = this._dataElems, animate = (this._animDuration>0);
  var xOffset = ApacheChart._XOFFSET_PERSPECTIVE, yOffset = ApacheChart._YOFFSET_PERSPECTIVE;
  var marginLeft = margins.left, marginTop = margins.top; 
  var gridWidth = (this._width - marginLeft - margins.right - xOffset);
  var gridHeight = (this._height - marginTop - margins.bottom - yOffset);
  var dotElem, pathElem;
  var groupLabels = model.getGroupLabels(), groupCount = groupLabels.length, 
      seriesLabels = model.getSeriesLabels(), seriesCount = seriesLabels.length;
  var seriesColors = model.getSeriesColors(), xValues = model.getXValues(), 
      yValues = model.getYValues(), nValues = yValues.length;    
  var defaultTransform = "scale(0.00001,1)";
  var minYValue = model.getMinYValue(), maxYValue = model.getMaxYValue();
  var minXValue = model.getMinXValue(), maxXValue = model.getMaxXValue();
  var gridBottom = gridHeight + marginTop+yOffset, dx, dy;
  var gradientsUsed = this._gradientsUsed;
  
  pathElem = svgDoc.getElementById("linePath3dPrototype");
  
  for (var i = 0; i< seriesCount; ++i)
  {
    var sb = new ApacheChartBuffer();
    pathElem = pathElem.cloneNode(false);
    if(animate)
    {
      dataElems.push(pathElem);
      pathElem.setAttribute("transform",defaultTransform);
    }    
    for (var j = 0; j < nValues; ++j)
    { 
      dy = gridBottom - gridHeight*(yValues[j][i]-minYValue)/(maxYValue-minYValue);
      dx = marginLeft + gridWidth*(xValues[j][i]-minXValue)/(maxXValue-minXValue);
      if(j != nValues - 1)
      {
        sb.append("M").append(dx).append(",").append(dy);
        sb.append("l").append(xOffset).append(",").append(-yOffset);
        var nextdy, nextdx;
        nextdx = marginLeft + gridWidth*(xValues[j+1][i]-minXValue)/(maxXValue-minXValue);
        nextdy = gridBottom - 
                  gridHeight*(yValues[j+1][i]-minYValue)/(maxYValue-minYValue);
        sb.append("L").append(nextdx+xOffset).append(",").append(nextdy-yOffset);
        sb.append("l").append(-xOffset).append(",").append(yOffset);
        sb.append("L").append(dx).append(",").append(dy);
      }
    }
    if(gradientsUsed)
      pathElem.setAttribute("fill", "url(#gradient"+i+")");
    else
      pathElem.setAttribute("fill", seriesColors[i]);
    pathElem.setAttribute("stroke", seriesColors[i]);
    pathElem.setAttribute("seriesIndex", i);
    if(this._tooltipsVisible)
    {
      pathElem.addEventListener("mousemove",this.ShowToolTipCallback,false);
      pathElem.addEventListener("mouseout",this.HideToolTipCallback,false);
    }
    pathElem.addEventListener("click",this.ClickCallback,false); 
    pathElem.setAttribute("d", sb.toString());
    rootElem.appendChild(pathElem);
  }
}

ApacheXYLineChart.prototype.ShowToolTip = function(e)
{
  // first hide any existing tooltip
  this.HideToolTip();
  ApacheXYLineChart.superclass.ShowToolTip.call(this, e);
}

ApacheXYLineChart.prototype.GetToolTipLocation = function(e, ttBBox)
{
  return {x:(e.clientX+20), y:(e.clientY+20)};
}

ApacheXYLineChart.prototype.GetChartEvent = function(e)
{
  var evtTarget = e.target;
  
  var i = parseInt(evtTarget.getAttribute("seriesIndex"));

  var clientX = e.clientX, clientY = e.clientY, evtTarget = e.target;
  var isPerspective = this._isPerspective;
  var yOffset = ApacheChart._YOFFSET_PERSPECTIVE;
  var model = this._model, xValues = model.getXValues(), 
      yValues = model.getYValues(), nValues = yValues.length;
  var minYValue = model.getMinYValue(), maxYValue = model.getMaxYValue();
  var minXValue = model.getMinXValue(), maxXValue = model.getMaxXValue();
  var margins = this._margins, marginLeft = margins.left, marginTop = margins.top; 
  var gridWidth = (this._width - marginLeft - margins.right);
  var gridHeight = (this._height - marginTop - margins.bottom);
      
  var gridBottom = gridHeight + marginTop +(isPerspective?yOffset:0);
  var dx, dy, xValue = 0.0, yValue = 0.0;
  var nextdy, nextdx;
  for (var j = 0; j < nValues; ++j)
  { 
    if(j != nValues - 1)
    {
      dx = marginLeft + gridWidth*(xValues[j][i]-minXValue)/(maxXValue-minXValue);
      nextdx = marginLeft + gridWidth*(xValues[j+1][i]-minXValue)/(maxXValue-minXValue);
      
      if(clientX > dx && clientX < (dx+nextdx))
      {
        dy = gridBottom - gridHeight*(yValues[j][i]-minYValue)/(maxYValue-minYValue);
        nextdy = gridBottom - 
                gridHeight*(yValues[j+1][i]-minYValue)/(maxYValue-minYValue);

        yValue = yValues[j][i] + (yValues[j+1][i]-yValues[j][i])*(clientY - dy)/(nextdy-dy);
        xValue = xValues[j][i] + (xValues[j+1][i]-xValues[j][i])*(clientX - dx)/(nextdx-dx);
        break;
      }
    }
  }
  
  return new ApacheChartEvent([i],null, [yValue],[xValue]);
}

ApacheXYLineChart.prototype.FillToolTipData = function(boundingRectElem, circleElem, e)
{
  var chartEvent = this.GetChartEvent(e);
  var i = chartEvent.getSeriesIndices()[0],
      yValue = chartEvent.getYValues()[0]
      xValue = chartEvent.getXValues()[0];
  var model = this._model, seriesLabels = model.getSeriesLabels();
    
  var textElem = boundingRectElem.nextSibling.nextSibling;
  textElem.firstChild.data = seriesLabels[i]+
                ": ("+ this._formatValue(xValue) + 
                ")    (" + this._formatValue(yValue) +")";
                
  var labelWidth = textElem.getComputedTextLength();      
  //We do not need the next label 
  textElem = textElem.nextSibling.nextSibling;
  textElem.firstChild.data = "";
  var rectWidth = labelWidth+2*ApacheChart._TEXT_MARGIN;
  boundingRectElem.setAttribute("width",rectWidth);
  boundingRectElem.setAttribute("stroke", seriesColors[i]);
  circleElem.setAttribute("r",0);
}

////////////////////////////////////////////////////////////////////
// Radar chart subclass
////////////////////////////////////////////////////////////////////
function ApacheRadarChart(
  type, model, svgEmbedId,
  isPerspective, legendPosition)
{
  this.Init(type, model, svgEmbedId, isPerspective, legendPosition);
}

ApacheChartObj.Inherit(ApacheChart, ApacheRadarChart);

ApacheRadarChart.prototype.Init = function(
  type, model, svgEmbedId, 
  isPerspective, legendPosition)
{
  ApacheRadarChart.superclass.Init.call(this, type, model, svgEmbedId, 
                          isPerspective, legendPosition);
  this._toolTips = [];
}

ApacheRadarChart.prototype.draw = function()
{
  this._yLabels = [];
  ApacheRadarChart.superclass.draw.call(this);  
  delete this._yLabels;
}

ApacheRadarChart.prototype.SetGridAnimStep = function(ratio)
{
  var animElems = this._gridElems, animCount = animElems.length;
  for(var i = 0; i < animCount; ++i)
  {    
    animElems[i].setAttribute("fill-opacity",ratio);
  }
}

ApacheRadarChart.prototype.SetDataAnimStep = function(ratio)
{
  var animElems = this._dataElems, animCount = animElems.length;
  var margins = this._margins, marginLeft = margins.left, marginTop = margins.top; 
  var isRadarArea =(this._type == ApacheChart.TYPE_RADAR_AREA);
  var model = this._model, groupLabels = model.getGroupLabels(), groupCount = groupLabels.length, 
      seriesLabels = model.getSeriesLabels(), seriesCount = seriesLabels.length;
  var gridWidth = (this._width - marginLeft - margins.right);
  var gridHeight = (this._height - marginTop - margins.bottom);
  var cx = marginLeft+gridWidth/2, cy = marginTop+gridHeight/2;
  var newRatio = ratio*seriesCount, animSeriesIndex = 0;
  if(newRatio > 1)
  {
    animSeriesIndex = Math.floor(newRatio);
    if(animSeriesIndex >= seriesCount)
      animSeriesIndex = seriesCount - 1;
    newRatio = newRatio - Math.floor(newRatio);
  }

  var tx = (1-newRatio)*cx, ty = (1-newRatio)*cy;
  var transform = "translate("+tx+","+ ty+") scale("+newRatio+","+newRatio+")";

  // We will make each series appear separately
  var i = animSeriesIndex;
  this._setRadarSeriesAnimStep(i, animElems, isRadarArea, transform); 
  if(i>0)
  {
    this._setRadarSeriesAnimStep(i-1, animElems, isRadarArea, "scale(1,1)");
  }
  
  // make sure that everything is scaled properly at the end
  if(ratio == 1)
  {
    for(var i = 0; i < seriesCount; ++i)
    {
      this._setRadarSeriesAnimStep(i, animElems, isRadarArea, "scale(1,1)");
    }
  }  
}

ApacheRadarChart.prototype._setRadarSeriesAnimStep = function(i, animElems, isRadarArea, transform)
{
  animElems[i].setAttribute("transform", transform);
  if(!isRadarArea)
  {
    var dots = animElems["dots"+i];
    for(var j = dots.length-1; j>=0; --j)
    {
      dots[j].setAttribute("transform", transform);
    }
  }
}

ApacheRadarChart.prototype.DrawChartData = function()
{
  this._drawRadar();
  
  // Move the y-value labels to the top since currently there is not concept of z-index in svg
  var yLabels = this._yLabels, rootElem = this._rootElement;
  for(var i = yLabels.length-1; i >=0; i--)
  {
    var label = yLabels[i];
    if(label)
    {
      rootElem.removeChild(label);
      rootElem.appendChild(label);
    }
  }
}

ApacheRadarChart.prototype.SetStopOpacity = function(stopNode)
{
  // In gecko opacity does not mix with stop-opacity, so use a lower value
  stopNode.setAttribute("stop-opacity", ApacheChart._DEFAULT_STOP_OPACITY/2);
}

/**
 * Adjusts the legend location when it is at the top
 * @param ty(int) the original y location of the legend
 */
ApacheRadarChart.prototype.SetLegendTopAdjustment = function(ty)
{
  var container = this._hLabelContainer;
  ty -= container.getBBox().height+ApacheChart._TEXT_MARGIN;
  return ty;
}

/**
 * Adjusts the legend location when it is at the bottom
 * @param ty(int) the original y location of the legend
 */
ApacheRadarChart.prototype.SetLegendBottomAdjustment = function(ty)
{
  var container = this._hLabelContainer;
  if(container.childNodes.length > 0)
  {
    ty += container.getBBox().height+ApacheChart._TEXT_MARGIN;
  }
  return ty;
}

/**
 * Adjusts the legend location when it is at the Left
 * @param tx(int) the original x location of the legend
 */
ApacheRadarChart.prototype.SetLegendLeftAdjustment = function(tx)
{
  var container = this._hLabelContainer;
  if(container.childNodes.length > 0)
  {
    tx -= container.getBBox().width+ApacheChart._TEXT_MARGIN;
  }
  return tx;
}

/**
 * Adjusts the legend location when it is at the Right
 * @param tx(int) the original x location of the legend
 */
ApacheRadarChart.prototype.SetLegendRightAdjustment = function(tx)
{
  var container = this._hLabelContainer;
  if(container.childNodes.length > 0)
    tx += container.getBBox().width+ApacheChart._TEXT_MARGIN;
  return tx;
}

ApacheRadarChart.prototype.DrawGroupLabels = function()
{
  var svgDoc = this._svgDoc, rootElem = this._rootElement, model = this._model;
  var container = svgDoc.createElementNS("http://www.w3.org/2000/svg", "g");
  this._hLabelContainer = container;
  
  var labelElems = this._labelElems, animate = (this._animDuration>0);
  var groupLabels = model.getGroupLabels(), vLineCount = groupLabels.length;
  var labelElem = svgDoc.getElementById("groupLabelPrototype");
  var labelText, gLabelElems = this._groupLabelElems;
  for(var i = 0; i<vLineCount; ++i)
  {
    labelText = groupLabels[i];
    if(!labelText)
      continue;
    labelElem = labelElem.cloneNode(true);
    if(animate)
		{
		  labelElems.push(labelElem);
		  labelElem.setAttribute("fill-opacity","0");
		}
    labelElem.firstChild.data = labelText;
    container.appendChild(labelElem);
    gLabelElems[i] = labelElem;
  }
  rootElem.appendChild(container);
}

ApacheRadarChart.prototype.AdjustMarginsForGroupLabels = function()
{
  var container = this._hLabelContainer;
  if(container.childNodes.length > 0)
  {
    var bBox = container.getBBox();
    var dxVertical = bBox.width+ApacheChart._TEXT_MARGIN,
        dyVertical = bBox.height+ApacheChart._TEXT_MARGIN;
    this._margins.top += dyVertical;
    this._margins.bottom += dyVertical;
    this._margins.left += dxVertical;
    this._margins.right += dxVertical;
  }
}

ApacheRadarChart.prototype.LayoutGroupLabels = function()
{
  var model = this._model, margins = this._margins;
  var marginLeft = margins.left, marginTop = margins.top; 
  var gridWidth = (this._width - marginLeft - margins.right);
  var gridHeight, cy;
  var cx = marginLeft+gridWidth/2, radius;
  var groupLabels = model.getGroupLabels(), vLineCount = groupLabels.length;
  var labelElem, groupWidth = gridWidth/vLineCount;
  var container = this._hLabelContainer, childNodes = container.childNodes;
  var firstLabel = false, gLabelElems = this._groupLabelElems;
  
  if(childNodes.length == 0)
    return;
    
  for(var i = 0; i<vLineCount; ++i)
  {
    labelElem = gLabelElems[i];
    if(!labelElem)
      continue;
      
    if(!firstLabel)
    {
      var rect = labelElem.getBBox();
      marginTop = margins.top; 
      gridHeight = (this._height - marginTop - margins.bottom);
      cy = marginTop+gridHeight/2;
      radius = Math.min(gridWidth, gridHeight)/2+rect.height-ApacheChart._TEXT_MARGIN/2;
      firstLabel = true;
    }
    
    var theta = (i)*2*Math.PI/vLineCount;
    var dx= cx + radius*Math.sin(theta),
        dy = cy - radius*Math.cos(theta);
    
    labelElem.setAttribute("y", dy);
    var textWidth = labelElem.getComputedTextLength();
    if(theta > Math.PI)
			dx -= textWidth;
    labelElem.setAttribute("x", dx);
  }
}

ApacheRadarChart.prototype.DrawGrid = function()
{
  // No perspective support for radar
  this.Draw2DGrid();
}

ApacheRadarChart.prototype.Draw2DGrid = function()
{
  var svgDoc = this._svgDoc, rootElem = this._rootElement,
      model = this._model, margins = this._margins;
  var gridElems = this._gridElems, animate = (this._animDuration>0);
  var marginLeft = margins.left, marginTop = margins.top; 
  var gridWidth = (this._width - marginLeft - margins.right);
  var gridHeight = (this._height - marginTop - margins.bottom);
  var cx = marginLeft+gridWidth/2, cy = marginTop+gridHeight/2;
  var radius = Math.min(gridWidth, gridHeight)/2;
  var gradientsUsed = this._gradientsUsed;
  var circle = svgDoc.getElementById("radarCirclePrototype").cloneNode(false);
  circle.setAttribute("cx", cx);
  circle.setAttribute("cy", cy);
  circle.setAttribute("r", radius);
  
  if(gradientsUsed)
    circle.setAttribute("fill", "url(#gridGradient)");
  if(animate)
  {
    gridElems.push(circle);
    circle.setAttribute("fill-opacity","0");
  }
  rootElem.appendChild(circle);
  
  var vLineCount = this.GetVLineCount(), circleCount = this.GetHLineCount();
  // inner circles
  circle = svgDoc.getElementById("radarInnerCirclePrototype");
  circle.setAttribute("cx", cx);
  circle.setAttribute("cy", cy);
  for (var i = 0; i< circleCount-1; ++i)
  {
    circle = circle.cloneNode(false);
    if(animate)
    {
      gridElems.push(circle);
      circle.setAttribute("fill-opacity","0");
    }
    var newRadius = radius - (i+1)*radius/circleCount;
    circle.setAttribute("r", newRadius);
    rootElem.appendChild(circle);
  }
  
  var sb = new ApacheChartBuffer();
  var pathElem = svgDoc.getElementById("radarGridPathPrototype").cloneNode(false);
  sb.append("M").append(cx).append(",").append(cy);
  sb.append("l").append(0).append(",").append(-radius);

  for(var i = 0; i<vLineCount-1; ++i)
  {
    var dx= cx + radius*Math.sin((i+1)*2*Math.PI/vLineCount),
        dy = cy - radius*Math.cos((i+1)*2*Math.PI/vLineCount);
    sb.append("M").append(cx).append(",").append(cy);
    sb.append("L").append(dx).append(",").append(dy);    
  }
  pathElem.setAttribute("d", sb.toString());
  if(animate)
  {
    gridElems.push(pathElem);
    pathElem.setAttribute("fill-opacity","0");
  }
  rootElem.appendChild(pathElem);
}

ApacheRadarChart.prototype.DrawYValueLabels = function()
{
  // For radar the y values do not effect margins 
  // so we do our drawing and layout in shot.
}

ApacheRadarChart.prototype.LayoutYValueLabels = function()
{
  var svgDoc = this._svgDoc, rootElem = this._rootElement,
      model = this._model, margins = this._margins;
  var marginLeft = margins.left, marginTop = margins.top; 
  var gridWidth = (this._width - marginLeft - margins.right);
  var gridHeight = (this._height - marginTop - margins.bottom);
  var labelElems = this._labelElems, animate = (this._animDuration>0);
  var cx = marginLeft+gridWidth/2, cy = marginTop+gridHeight/2;
  var radius = Math.min(gridWidth, gridHeight)/2;  
  var vLineCount = this.GetVLineCount(), circleCount = this.GetHLineCount();
      
  var minValue = model.getMinYValue(), maxValue = model.getMaxYValue();  
  var yLabels = this._yLabels;
  var labelElem = svgDoc.getElementById("yLabelPrototype");
  labelElem = labelElem.cloneNode(true);
  var textHeight = this._addRadarYLabelAt(rootElem, labelElem, circleCount-1, 
                         cx,marginTop, textHeight, this._formatValue(maxValue));
  if(animate)
	{
		labelElems.push(labelElem);
		labelElem.setAttribute("fill-opacity","0");
	}                     
  labelElem = labelElem.cloneNode(true);
  this._addRadarYLabelAt(rootElem, labelElem, circleCount, 
                           cx, cy, textHeight, this._formatValue(minValue));
  if(animate)
	{
		labelElems.push(labelElem);
		labelElem.setAttribute("fill-opacity","0");
	}
                           
  // horizontal lines
  for (var i = 0; i< circleCount-1; ++i)
  {
    var newRadius = (i+1)*radius/circleCount;
    var value = ((maxValue-minValue)*(i+1)/circleCount) + minValue;
    labelElem = labelElem.cloneNode(true);
    this._addRadarYLabelAt(rootElem, labelElem, i, cx,
          radius-newRadius+marginTop, textHeight, this._formatValue(value));
    if(animate)
	  {
		  labelElems.push(labelElem);
		  labelElem.setAttribute("fill-opacity","0");
	  }
  }
}

ApacheRadarChart.prototype._addRadarYLabelAt = function(
  rootElem, labelElem, index, 
  x, y, textHeight, value)
{
  this._yLabels[index] = labelElem;
  labelElem.firstChild.data = value;
  rootElem.appendChild(labelElem);
  if(textHeight == null)
  {
    var rect = labelElem.getBBox();
    textHeight = rect.height;
  }
  // readjust to right align
  var labelMargin = ApacheChart._TEXT_MARGIN, 
      textLength = labelElem.getComputedTextLength();
  dx = x-textLength-labelMargin;
  labelElem.setAttribute("x", dx);
  labelElem.setAttribute("y", y+textHeight/2);

  return textHeight;
}

ApacheRadarChart.prototype._drawRadar = function()
{
  var svgDoc = this._svgDoc, rootElem = this._rootElement,
      model = this._model, margins = this._margins;
  var dataElems = this._dataElems, animate = (this._animDuration>0), dotElems;
  var marginLeft = margins.left, marginTop = margins.top; 
  var gridWidth = (this._width - marginLeft - margins.right);
  var gridHeight = (this._height - marginTop - margins.bottom);
  var cx = marginLeft+gridWidth/2, cy = marginTop+gridHeight/2;
  var radius = Math.min(gridWidth, gridHeight)/2;
  var isRadarArea =(this._type == ApacheChart.TYPE_RADAR_AREA);
  var protoName = isRadarArea?"areaPathPrototype":"linePathPrototype";
  var lineDot,pathElem = svgDoc.getElementById(protoName);
  var seriesLabels = model.getSeriesLabels(), seriesCount = seriesLabels.length;
  var seriesColors = model.getSeriesColors(), yValues = model.getYValues();    
  var minValue = model.getMinYValue(), maxValue = model.getMaxYValue();    
  var defaultTransform = "scale(0.00001,0.00001)";
  var gradientsUsed = this._gradientsUsed;
  var yValueCount = yValues.length;
  var dx, dy;
  
  if(!isRadarArea)
    lineDot = svgDoc.getElementById("lineDotPrototype");
  
  for (var i = 0; i< seriesCount; ++i)
  {
    var sb = new ApacheChartBuffer();
    pathElem = pathElem.cloneNode(false);
    if(animate)
    {
      dataElems.push(pathElem);
      pathElem.setAttribute("transform",defaultTransform);
      dotElems = dataElems["dots"+i] = [];
    }
    for (var j = 0; j < yValueCount; ++j)
    {
      var yPoint = radius*(yValues[j][i]-minValue)/(maxValue-minValue);
      
      var dx= cx + yPoint*Math.sin((j)*2*Math.PI/yValueCount),
        dy = cy - yPoint*Math.cos((j)*2*Math.PI/yValueCount);
      
      if(j == 0)
      {
        sb.append("M").append(dx).append(",").append(dy);
      }
      else
      {
        sb.append("L").append(dx).append(",").append(dy);    
      }
      
      if(!isRadarArea)
      {
        lineDot = lineDot.cloneNode(false);
        lineDot.setAttribute("cx", dx);
        lineDot.setAttribute("cy", dy);
        if(gradientsUsed)
          lineDot.setAttribute("fill", "url(#gradient"+i+")");
        else
          lineDot.setAttribute("fill", seriesColors[i]);
        lineDot.setAttribute("stroke", seriesColors[i]);
        if(animate)
        {
          dotElems.push(lineDot);
          lineDot.setAttribute("transform",defaultTransform);
        }
        rootElem.appendChild(lineDot);
      }
    }
    sb.append("Z");
    if(isRadarArea)
    {
      if(gradientsUsed)
        pathElem.setAttribute("fill", "url(#gradient"+i+")");
      else
        pathElem.setAttribute("fill", seriesColors[i]);
    }
    else
    {
      pathElem.setAttribute("fill", "none");
    }
    pathElem.setAttribute("stroke", seriesColors[i]);
    
    pathElem.setAttribute("seriesIndex", i);
    if(this._tooltipsVisible)
    {
      pathElem.addEventListener("mousemove",this.ShowToolTipCallback,false);
      pathElem.addEventListener("mouseout",this.HideToolTipCallback,false);
    }
    pathElem.addEventListener("click",this.ClickCallback,false);          
    pathElem.setAttribute("d", sb.toString());
    rootElem.appendChild(pathElem);
  }
}

ApacheRadarChart.prototype.isPointInPolygon = function(xs, ys, x, y)
{
  var i, j, npol = ys.length, inside = false;
  for (i = 0, j = npol-1; i < npol; j = i++) {
    if ((((ys[i]<=y) && (y<ys[j])) ||
         ((ys[j]<=y) && (y<ys[i]))) &&
        (x < (xs[j] - xs[i]) * (y - ys[i]) / (ys[j] - ys[i]) + xs[i]))

      inside = !inside;
  }
  return inside;
}

ApacheRadarChart.prototype.GetChartEvent = function(e, seriesXs, seriesYs)
{
  var clientX = e.clientX, clientY = e.clientY, evtTarget = e.target;
  var isRadarArea =(this._type == ApacheChart.TYPE_RADAR_AREA);
  var model = this._model, yValues = model.getYValues();
  var minValue = model.getMinYValue(), maxValue = model.getMaxYValue();
  var margins = this._margins, marginLeft = margins.left, marginTop = margins.top; 
  var gridWidth = (this._width - marginLeft - margins.right);
  var gridHeight = (this._height - marginTop - margins.bottom);
  var cx = marginLeft+gridWidth/2, cy = marginTop+gridHeight/2;
  var radius = Math.min(gridWidth, gridHeight)/2;
  var seriesCount = model.getSeriesLabels().length;
  var yValues = model.getYValues(), seriesIndex;
  var yValueCount = yValues.length;    
  
  if(clientX < marginLeft || 
     clientX>(marginLeft + gridWidth) ||
     clientY < marginTop || 
     clientY>(marginTop + gridHeight))
  {
    return null;
  }
  
  if(!isRadarArea)
    seriesIndex = parseInt(evtTarget.getAttribute("seriesIndex"));

  if(!seriesXs)
    seriesXs = [];
    
  if(!seriesYs)
    seriesYs = [];
    
  var dx1, dy1, dx2, dy2, seriesIndices = [], seriesValues = [];
  for (var i = 0; i< seriesCount; ++i)
  {
    if(!isRadarArea && (seriesIndex != i))
      continue;
    
    for (var j = 0; j < yValueCount; ++j)
    {
      var nextYVal = (j != yValueCount -1)? yValues[j+1][i]: yValues[0][i];
      var yPoint = radius*(yValues[j][i]-minValue)/(maxValue-minValue);
      var yPoint2 = radius*(nextYVal-minValue)/(maxValue-minValue);
      var angle = j*2*Math.PI/yValueCount, 
          nextAngle = (j+1)*2*Math.PI/yValueCount;
      var dx1 = cx + yPoint*Math.sin(angle),
        dy1 = cy - yPoint*Math.cos(angle);
      
        dx2 = cx + yPoint2*Math.sin(nextAngle);
        dy2 = cy - yPoint2*Math.cos(nextAngle);
      
      if(this.isPointInPolygon([cx, dx1, dx2], [cy, dy1, dy2], clientX, clientY))
      {
        // find the point on the radar that matches the current mouse 
        // using the angle of the current mouse point
        var mousePtAngle = Math.atan2(cy - clientY, clientX - cx);
        if(mousePtAngle <= Math.PI/2)
          mousePtAngle = Math.PI/2 - mousePtAngle;
        else
          mousePtAngle = 3*Math.PI/2 + (Math.PI - mousePtAngle);
        var ratio = (mousePtAngle - angle)/(nextAngle - angle);
        value = yValues[j][i] + (nextYVal-yValues[j][i])*ratio;
        seriesValues.push(value);
        seriesIndices.push(i);
        seriesYs.push(dy1+(dy2-dy1)*ratio);
        seriesXs.push(dx1+(dx2-dx1)*ratio);
        break;
      }
    }
  }
  return new ApacheChartEvent(seriesIndices,null, seriesValues,null);
}

ApacheRadarChart.prototype.ShowToolTip = function(e)
{    
  // Hide any existing tooltips
  this.HideToolTip();
  
  var seriesXs = [], seriesYs = [];
  var chartEvent = this.GetChartEvent(e, seriesXs, seriesYs);
  if(chartEvent == null || chartEvent.getYValues().length == 0)
  {
    return;
  }
  this._displayToolTips(chartEvent.getYValues(), chartEvent.getSeriesIndices(), 
                        seriesYs, seriesXs, e);
}

ApacheRadarChart.prototype._displayToolTips = function(
  seriesValues, seriesIndices, 
  seriesYs, seriesXs, e)
{
  var svgDoc = this._svgDoc, rootElem = this._rootElement;
  var model = this._model, seriesLabels = model.getSeriesLabels(), 
      seriesCount = seriesLabels.length, seriesColors = model.getSeriesColors();
  var tooltipCount = seriesIndices.length, toolTips = this._toolTips;
  var dx, dy;
  for(var i = 0; i<tooltipCount; ++i)
  { 
    var seriesIndex = seriesIndices[i];
    var toolTip = toolTips[seriesIndex];
    var resizeOnInit = false;
    if (toolTip == null)
    {
      toolTip = svgDoc.getElementById("toolTip").cloneNode(true);
      rootElem.appendChild(toolTip);
      toolTips[seriesIndex] = toolTip;
      resizeOnInit = true;
    }
    
    toolTip.style.setProperty("visibility","visible","");
    
    var circleElem = toolTip.firstChild.nextSibling;
    var boundingRectElem = circleElem.nextSibling.nextSibling;
    
    //append all the series values as labels
    var textElem = boundingRectElem.nextSibling.nextSibling;
    var textElemCount = seriesValues.length;
    var rectWidth = textElem.getComputedTextLength();
    
    textElem.firstChild.data = 
        seriesLabels[seriesIndex]+":  " +this._formatValue(seriesValues[i]);
    
    // Initially the template tooltip has an extra text node
    if(resizeOnInit)
    {
      var rectHeight = parseInt(boundingRectElem.getAttribute("height"));
      var dy = parseInt(textElem.getAttribute("dy"));
      rectHeight -= dy;
      boundingRectElem.setAttribute("height",rectHeight);
      textElem = textElem.nextSibling.nextSibling;
      textElem.firstChild.data = "";
    }
    
    rectWidth += 2*ApacheChart._TEXT_MARGIN;
    boundingRectElem.setAttribute("width",rectWidth);
    
    var targetBBox = e.target.getBBox();
    var ttBBox = toolTip.getBBox();
    dx = seriesXs[i], dy = seriesYs[i]- ttBBox.height;
    if(dx + ttBBox.width > this._width)
    {
      dx -= ttBBox.width;
      circleElem.setAttribute("cx",boundingRectElem.getBBox().width);
    }
    else
    {
      circleElem.setAttribute("cx",0);
    }
    if(dy - ttBBox.height < 0)
    {
      dy += ttBBox.height;
      circleElem.setAttribute("cy",0);
    }
    else
    {
      circleElem.setAttribute("cy",boundingRectElem.getBBox().height);
    }
    boundingRectElem.setAttribute("stroke", seriesColors[seriesIndex]);
    circleElem.setAttribute("stroke",seriesColors[seriesIndex]);
    toolTip.setAttribute("transform","translate("+dx+","+dy+")");
  }
}

ApacheRadarChart.prototype.HideToolTip = function(e)
{
  var tooltips = this._toolTips, tooltipCount = tooltips.length;
  for(var i = 0; i<tooltipCount; ++i)
  { 
    var toolTip = tooltips[i];
    if(toolTip)
      toolTip.style.setProperty("visibility","hidden","");
  }
}

////////////////////////////////////////////////////////////////////
// Funnel chart subclass
////////////////////////////////////////////////////////////////////
function ApacheFunnelChart(
  type, model, svgEmbedId, 
  isPerspective, legendPosition)
{
  this.Init(type, model, svgEmbedId, isPerspective, legendPosition);
}

ApacheChartObj.Inherit(ApacheChart, ApacheFunnelChart);

ApacheFunnelChart.prototype.SetDataAnimStep = function(ratio)
{
  var animElems = this._dataElems, animCount = animElems.length;
  var transform = "scale("+ratio+","+ratio+")";
  for(var i = 0; i < animCount; ++i)
  {
    animElems[i].setAttribute("transform", transform);
  }
}

ApacheFunnelChart.prototype.DrawChartData = function()
{  
  var rootElem = this._rootElement;
    
  // calculate the number of rows and columns
  var model = this._model, yValues = model.getYValues(), yValueCount = yValues.length;
  var groupLabels = model.getGroupLabels(), groupCount = groupLabels?groupLabels.length:1;
      
  var nCols = Math.ceil(Math.sqrt(yValueCount)), nRows = Math.round(Math.sqrt(yValueCount));
  
  var margins = this._margins, dx=margins.left, dy=margins.top;
  var quadWidth = (this._width - margins.left - margins.right)/nCols;
  // We do not need any gap for 3D pie chart because of the vertical scaling
  var vGap = this._isPerspective?0:2*ApacheChart._TEXT_MARGIN;
  var quadHeight = (this._height - margins.top - margins.bottom - (nRows-1)*vGap)/nRows;
  var labelElem = this._svgDoc.getElementById("groupLabelPrototype");
  for(var i = 0; i<nRows; ++i)
  {
    for(var j = 0; j<nCols; ++j)
    {  
      var iGroup = groupLabels?(i*nCols + j):(-1);
      if(iGroup >= yValueCount)
        break;
      
      var groupLabel = (iGroup == -1)?null:groupLabels[iGroup];
        
      var fnlContainer = rootElem.cloneNode(false);
      rootElem.appendChild(fnlContainer);
      var newHeight = this.DrawGroupLabelTitle(groupLabel, rootElem, 
                                      labelElem.cloneNode(true), dx, dy, 
                                      quadWidth, quadHeight);
      var newWidth = quadWidth - 2*ApacheChart._TEXT_MARGIN;
      if(this._isPerspective)
      {

        // Top ring has a height of 1/6 that of the width. So we need to compensate for half of it.
        newHeight -= newWidth/6;
        this._drawPerspectiveFunnel(fnlContainer, newWidth, newHeight, iGroup);
        fnlContainer.setAttribute("transform", 
          "translate("+(dx+ApacheChart._TEXT_MARGIN)+","+(dy+newWidth/12)+")");
      }
      else
      {
        this._drawFunnel(fnlContainer, newWidth, newHeight, iGroup);
        fnlContainer.setAttribute("transform", 
          "translate("+(dx+ApacheChart._TEXT_MARGIN)+","+dy+")");
      }
      dx +=quadWidth;
    }
    dx=margins.left;
    dy +=quadHeight+vGap;
  }  
}

ApacheFunnelChart.prototype.ComputeMinMaxValues = function()
{

}

ApacheFunnelChart.prototype.DrawGroupLabels = function()
{

}

ApacheFunnelChart.prototype.LayoutGroupLabels = function()
{

}

ApacheFunnelChart.prototype.DrawGrid = function()
{

}

ApacheFunnelChart.prototype.DrawYValueLabels = function()
{

}

ApacheFunnelChart.prototype.LayoutYValueLabels = function()
{

}

ApacheFunnelChart.prototype._drawFunnel = function(
  fnlContainer, quadWidth, 
  quadHeight, iGroup)
{
  var svgDoc = this._svgDoc, model = this._model, yValues = model.getYValues();
  var groupLabels = model.getGroupLabels(), seriesColors = model.getSeriesColors();
  if(iGroup == -1)
    iGroup = 0;
  
  // Number of segments
  var nSeg = yValues[iGroup].length;
  var total = 0;
  for (var i = 0; i < nSeg; ++i)
  {
    total += yValues[iGroup][i];
  }
  
  var dataElems = this._dataElems, animate = (this._animDuration>0)
  var pathElem = svgDoc.getElementById("funnelPathPrototype");
  var gradientsUsed = this._gradientsUsed;
  var defaultTransform = "scale(0.00001, 0.00001)";
  var x = 0, y = 0, slope = (quadWidth/2)/quadHeight,
      dx = quadWidth, dy, nextX, nextY;
  for (var i = nSeg-1; i >= 0; --i)
  {
    pathElem = pathElem.cloneNode(false);
    if(animate)
    {
      dataElems.push(pathElem);
      pathElem.setAttribute("transform",defaultTransform);
    }
    var valueRatio = (yValues[iGroup][i])/(total);
    var sb = new ApacheChartBuffer();
    sb.append("M").append(x).append(",").append(y);
    sb.append("L").append(dx).append(",").append(y);
    dy = (quadHeight)*valueRatio;
    nextY   = y + dy;
    nextX = quadWidth/2 - slope*(quadHeight-(nextY) );
    dx = quadWidth - nextX;
    if(i != 0)
    {
      sb.append("L").append(dx).append(",").append(nextY);
      sb.append("L").append(nextX).append(",").append(nextY);
      sb.append("Z");
    }
    else
    {
      var startTipY = (dy/3<=ApacheFunnelChart._MAX_FUNNEL_TIP)?y+(dy - dy/3):
                       quadHeight-ApacheFunnelChart._MAX_FUNNEL_TIP;
      nextX = quadWidth/2 - slope*(quadHeight-(startTipY) );
      dx = quadWidth - nextX;
      sb.append("L").append(dx).append(",").append(startTipY);
      sb.append("L").append(dx).append(",").append(quadHeight);
      sb.append("L").append(nextX).append(",").append(quadHeight);
      sb.append("L").append(nextX).append(",").append(startTipY);
      sb.append("Z");
    }
    pathElem.setAttribute("d", sb.toString());
    if(gradientsUsed)
      pathElem.setAttribute("fill", "url(#gradient"+i+")");
    else
      pathElem.setAttribute("fill", seriesColors[i]);
    pathElem.setAttribute("stroke", seriesColors[i]);
    pathElem.setAttribute("stroke-width", 1);
    pathElem.setAttribute("yValueIndex", iGroup);
    pathElem.setAttribute("seriesIndex", i);
    if(this._tooltipsVisible)
    {
      pathElem.addEventListener("mouseover",this.ShowToolTipCallback,false);
      pathElem.addEventListener("mouseout",this.HideToolTipCallback,false);      
    }
    pathElem.addEventListener("click",this.ClickCallback,false);
    fnlContainer.appendChild(pathElem);
    y = nextY;
    x = nextX;
  }  
}

ApacheFunnelChart.prototype._drawPerspectiveFunnel = function(
  fnlContainer, quadWidth, 
  quadHeight, iGroup)
{
  var svgDoc = this._svgDoc, model = this._model, yValues = model.getYValues();
  var groupLabels = model.getGroupLabels(), seriesColors = model.getSeriesColors();
  if(iGroup == -1)
    iGroup = 0;
  
  // Number of segments
  var nSeg = yValues[iGroup].length;
  var total = 0;
  for (var i = 0; i < nSeg; ++i)
  {
    total += yValues[iGroup][i];
  }
  
  var dataElems = this._dataElems, animate = (this._animDuration>0)
  var pathElem = svgDoc.getElementById("funnelPathPrototype");
  var gradientsUsed = this._gradientsUsed;
  var defaultTransform = "scale(0.00001, 0.00001)";
  var x = 0, y = 0, slope = (quadWidth/2)/quadHeight,
      dx = quadWidth, dy, nextX, oldDx, nextY;
  // the ring height is 1/12 of the width
  var rx = dx/2, ry = dx/24, oldRx, oldRy;
  for (var i = nSeg-1; i >= 0; --i)
  {
    pathElem = pathElem.cloneNode(false);
    if(animate)
    {
      dataElems.push(pathElem);
      pathElem.setAttribute("transform",defaultTransform);
    }    
    var valueRatio = (yValues[iGroup][i])/(total);
    var sb = new ApacheChartBuffer();
    sb.append("M").append(x).append(",").append(y);
    sb.append("A").append(rx).append(",").append(ry);
    sb.append(" 0 1,0 ").append(dx).append(",").append(y);
    sb.append("A").append(rx).append(",").append(ry);
    sb.append(" 0 1,0 ").append(x).append(",").append(y);

    oldDx = dx;
    oldRx = rx;
    oldRy = ry;    
    dy = (quadHeight)*valueRatio;
    nextY  = y + dy;
    nextX = quadWidth/2 - slope*(quadHeight-(nextY) );
    dx = quadWidth - nextX;
    rx = (dx-nextX)/2;
    ry = rx/12;
    if(i != 0)
    {
      sb.append("L").append(nextX).append(",").append(nextY);
      sb.append("A").append(rx).append(",").append(ry);
      sb.append(" 0 1,0 ").append(dx).append(",").append(nextY);
      sb.append("L").append(oldDx).append(",").append(y);
    }
    else
    {
      var startTipY = (dy/3<=ApacheFunnelChart._MAX_FUNNEL_TIP)?y+(dy - dy/3):
                       quadHeight-ApacheFunnelChart._MAX_FUNNEL_TIP;
      nextX = quadWidth/2 - slope*(quadHeight-(startTipY) );
      dx = quadWidth - nextX;
      
      rx = (dx-nextX)/2;
      ry = rx/12;
      sb.append("L").append(nextX).append(",").append(startTipY);
      sb.append("L").append(nextX).append(",").append(quadHeight);
      sb.append("A").append(rx).append(",").append(ry);
      sb.append(" 0 1,0 ").append(dx).append(",").append(quadHeight);
      sb.append("A").append(rx).append(",").append(ry);
      sb.append(" 0 1,0 ").append(nextX).append(",").append(quadHeight);
      sb.append("A").append(rx).append(",").append(ry);
      sb.append(" 0 1,0 ").append(dx).append(",").append(quadHeight);
      sb.append("L").append(dx).append(",").append(startTipY);
      sb.append("L").append(oldDx).append(",").append(y);
    }
    pathElem.setAttribute("d", sb.toString());
    if(gradientsUsed)
      pathElem.setAttribute("fill", "url(#gradient"+i+")");
    else
      pathElem.setAttribute("fill", seriesColors[i]);
    pathElem.setAttribute("stroke", seriesColors[i]);
    pathElem.setAttribute("stroke-width", 1);
    pathElem.setAttribute("yValueIndex", iGroup);
    pathElem.setAttribute("seriesIndex", i);
    if(this._tooltipsVisible)
    {
      pathElem.addEventListener("mouseover",this.ShowToolTipCallback,false);
      pathElem.addEventListener("mouseout",this.HideToolTipCallback,false);      
    }
    pathElem.addEventListener("click",this.ClickCallback,false);
    fnlContainer.appendChild(pathElem);
    y = nextY;
    x = nextX;
  }  
}

ApacheFunnelChart.prototype.GetToolTipLocation = function(e, ttBBox)
{
  var evtTarget = e.target;
  var targetBBox = evtTarget.getBBox();
  var ctm = evtTarget.parentNode.getCTM();
  return {x:(ctm.e+targetBBox.x+targetBBox.width/2), 
          y:(ctm.f+targetBBox.y+targetBBox.height/2 - ttBBox.height)};
}

ApacheFunnelChart._MAX_FUNNEL_TIP = 16;

////////////////////////////////////////////////////////////////////
// Circular gauge chart subclass
////////////////////////////////////////////////////////////////////
function ApacheGaugeChart(
  type, model, svgEmbedId, 
  isPerspective, legendPosition)
{
  this.Init(type, model, svgEmbedId, isPerspective, legendPosition);
}

ApacheChartObj.Inherit(ApacheChart, ApacheGaugeChart);

ApacheGaugeChart.prototype.Init = function(
  type, model, svgEmbedId, 
  isPerspective, legendPosition)
{
  ApacheGaugeChart.superclass.Init.call(this, type, model, svgEmbedId, 
                                   isPerspective, legendPosition);
  //this._markerTextGroup = undefined;
  //this._animCx;
  //this._animCy;
}

ApacheGaugeChart.prototype.SetDataAnimStep = function(ratio)
{
  var animElems = this._dataElems, animCount = animElems.length;
  var model = this._model, yValues = model.getYValues();
  for(var i = 0; i < animCount; ++i)
  {
    // For Dial Chart only one value is applicable
    var yValue = yValues[i][0];
    this.SetIndicatorPosition(yValue, animElems[i], ratio);
  }
}

ApacheGaugeChart.prototype.SetIndicatorPosition = function(yValue, indicator, ratio)
{
  var theta, cx = this._animCx, cy = this._animCy; 
  var model = this._model, minValue = model.getMinYValue(), maxValue = model.getMaxYValue();
  var valueRatio = ratio*(yValue - minValue)/(maxValue-minValue);
    
  theta = Math.PI/6 + valueRatio*(5*Math.PI/3);
  if(theta < Math.PI/2)
    theta += 3*Math.PI/2;
  else
    theta -= Math.PI/2;
  theta *= 180/Math.PI;
  indicator.setAttribute("transform", "rotate("+theta+" "+cx+" "+cy+")");
}

ApacheGaugeChart.prototype.DrawChartData = function()
{
  if(this._yMinorGridCount<0)
    this._yMinorGridCount = 4;
  var rootElem = this._rootElement;
    
  // calculate the number of rows and columns
  var model = this._model, yValues = model.getYValues(), yValueCount = yValues.length
  var groupLabels = model.getGroupLabels(), 
      groupCount = groupLabels?groupLabels.length:1;
      
  var nCols = Math.ceil(Math.sqrt(yValueCount)), nRows = Math.round(Math.sqrt(yValueCount));
  
  var margins = this._margins, dx=margins.left, dy=margins.top;
  var quadWidth = (this._width - margins.left - margins.right)/nCols;
  var vGap = 2*ApacheChart._TEXT_MARGIN;
  var quadHeight = (this._height - margins.top - margins.bottom - (nRows-1)*vGap)/nRows;
  var labelElem = this._svgDoc.getElementById("groupLabelPrototype");
  for(var i = 0; i<nRows; ++i)
  {
    for(var j = 0; j<nCols; ++j)
    {  
      var iGroup = groupLabels?(i*nCols + j):(-1);
      if(iGroup >= yValueCount)
        break;
      
      var groupLabel = (iGroup == -1)?null:groupLabels[iGroup];
        
      var gaugeContainer = rootElem.cloneNode(false);
      rootElem.appendChild(gaugeContainer);
      if(groupLabel)
        labelElem = labelElem.cloneNode(true);
      var newHeight = this.DrawGroupLabelTitle(groupLabel, rootElem, 
                                      labelElem, dx, dy, 
                                      quadWidth, quadHeight);
      var newWidth = quadWidth - 2*ApacheChart._TEXT_MARGIN;
      this.DrawDial(gaugeContainer, newWidth, newHeight, iGroup);
      gaugeContainer.setAttribute("transform", 
        "translate("+(dx+ApacheChart._TEXT_MARGIN)+","+dy+")");
      
      if(groupLabel)
      {
        var gBBox = gaugeContainer.getBBox(), gHeight = gBBox.height;
        if(gHeight < newHeight-vGap)
        {
          var newY = parseInt(labelElem.getAttribute("y"));
          newY -= (newHeight-gHeight)/2-vGap;
          labelElem.setAttribute("y", newY);
        }
      }
      dx +=quadWidth;
    }
    dx=margins.left;
    dy +=quadHeight+vGap;
  }
}

ApacheGaugeChart.prototype.DrawLegend = function()
{
  // Legend does not make sense for a gauge
}

ApacheGaugeChart.prototype.ComputeMinMaxValues = function()
{

}

ApacheGaugeChart.prototype.DrawGroupLabels = function()
{

}

ApacheGaugeChart.prototype.LayoutGroupLabels = function()
{

}

ApacheGaugeChart.prototype.DrawGrid = function()
{

}

ApacheGaugeChart.prototype.DrawYValueLabels = function()
{

}

ApacheGaugeChart.prototype.LayoutYValueLabels = function()
{

}

ApacheGaugeChart.prototype.DrawDial = function(
  gaugeContainer, quadWidth, 
  quadHeight, iGroup)
{
  var svgDoc = this._svgDoc, model = this._model;
  if(iGroup == -1)
    iGroup = 0;
  
  var dataElems = this._dataElems, animate = (this._animDuration>0);
  var templateName = this.GetGaugeTemplateName();
  var gauge = svgDoc.getElementById(templateName).cloneNode(true);
  gaugeContainer.appendChild(gauge);

  var gaugeBBox = gauge.getBBox(), gaugeWidth = gaugeBBox.width, 
      gaugeR = gaugeWidth/2;  
  var gElem = this._markerTextGroup;
  var groups = gauge.getElementsByTagName("g");
  var indicator = groups.item(groups.length-1); 
  
  indicator.setAttribute("yValueIndex", iGroup);
  indicator.setAttribute("seriesIndex", 0);
  if(this._tooltipsVisible)
  {
    indicator.addEventListener("mouseover",this.ShowToolTipCallback,false);
    indicator.addEventListener("mouseout",this.HideToolTipCallback,false);
  }
  indicator.addEventListener("click",this.ClickCallback,false);
  if(this._animCx == null)
  {
    this._animCx = indicator.getAttribute("_pivotCenterX");
    this._animCy = indicator.getAttribute("_pivotCenterY");
  }
      
  if(animate)
    dataElems.push(indicator);
  else
  {
    // If there is no animation lets move the indicators to the last position
    this.SetIndicatorPosition(this._model.getYValues()[iGroup][0], indicator, 1);
  }
  
  if(gElem!=null)
  {
    gElem = gElem.cloneNode(true);
    gauge.appendChild(gElem);
  }
  else
  {
    this.CreateTextMarkerGroup(gauge, gaugeR);
  }
  this.ScaleGauge(gauge, quadWidth, quadHeight, gaugeWidth, gaugeBBox.height);
}

ApacheGaugeChart.prototype.GetGaugeTemplateName = function()
{
  return "circularGauge";
}

ApacheGaugeChart.prototype.CreateTextMarkerGroup = function(gauge, gaugeR)
{
  var svgDoc = this._svgDoc, model = this._model;
  var gElem = svgDoc.createElementNS("http://www.w3.org/2000/svg", "g");
  gauge.appendChild(gElem);
  this._markerTextGroup = gElem;
  
  var majorMarker = svgDoc.getElementById("gaugeMarkerMajor"),
      majorMarkerCount = this._yMajorGridCount,
      minorMarker = svgDoc.getElementById("gaugeMarkerMinor"),
      minorMarkerCount = this._yMinorGridCount,
      textElem = svgDoc.getElementById("gaugeTextPrototype");
  
  var paths = gauge.getElementsByTagName("path");
  var markerContainerR = parseInt(gauge.getAttribute("_markerRadius"));
  var minValue = model.getMinYValue(), maxValue = model.getMaxYValue();
  var x, y, angle, textMargin;

  for(var i=0, theta = Math.PI/6; 
      i<=majorMarkerCount; ++i, theta += (5*Math.PI/3)/majorMarkerCount)
  {
    var adjustedTheta;
    if(theta < Math.PI/2)
      adjustedTheta = theta +3*Math.PI/2;
    else
      adjustedTheta = theta - Math.PI/2;
    x = gaugeR-markerContainerR*(Math.cos(adjustedTheta));
    y = gaugeR-markerContainerR*(Math.sin(adjustedTheta));
    var angle = adjustedTheta*180/Math.PI;
    var marker = majorMarker.cloneNode(true);
    gElem.appendChild(marker);
    marker.setAttribute("transform",
          "translate("+x.toFixed(0)+","+y.toFixed(0)+") rotate("+angle.toFixed(0)+" 0 0)");
    var value = minValue + i*(maxValue-minValue)/(majorMarkerCount);
    textElem = textElem.cloneNode(true);
    textElem.firstChild.data = this._formatValue(value);
    gElem.appendChild(textElem);
    var textBBox = textElem.getBBox();
    if(i == 0)
    {
      textMargin = textBBox.height/2;
    }
    x = gaugeR-(markerContainerR-textMargin)*(Math.cos(adjustedTheta));
    y = gaugeR-(markerContainerR-textMargin)*(Math.sin(adjustedTheta));

    if(theta >= 5*Math.PI/6 && theta <= 7*Math.PI/6)
    {
      y += textBBox.height;
      x -= textBBox.width/2
    }
    else
    {
      y += textBBox.height/2;
      if(theta < Math.PI)
        x += 2*ApacheChart._TEXT_MARGIN;
      else
        x -= textBBox.width + 2*ApacheChart._TEXT_MARGIN;
    }

    textElem.setAttribute("transform",
          "translate("+x.toFixed(0)+","+y.toFixed(0)+")");
  }

  for(var i=(minorMarkerCount+1), 
      theta = Math.PI/6+(5*Math.PI/3)/(majorMarkerCount*minorMarkerCount); 
      i<=(majorMarkerCount+1)*minorMarkerCount; 
      ++i, theta += (5*Math.PI/3)/(majorMarkerCount*minorMarkerCount))
  {
    if(i%minorMarkerCount == 0)
      continue;
    var adjustedTheta;
    if(theta < Math.PI/2)
      adjustedTheta = theta +3*Math.PI/2;
    else
      adjustedTheta = theta - Math.PI/2;
    var x = gaugeR-markerContainerR*(Math.cos(adjustedTheta));
    var y = gaugeR-markerContainerR*(Math.sin(adjustedTheta));
    var angle = adjustedTheta*180/Math.PI;
    var marker = minorMarker.cloneNode(true);
    gElem.appendChild(marker);
    marker.setAttribute("transform",
          "translate("+x.toFixed(0)+","+y.toFixed(0)+") rotate("+angle.toFixed(0)+" 0 0)");
  }
}

ApacheGaugeChart.prototype.ScaleGauge = function(gauge, quadWidth, quadHeight, gaugeWidth, gaugeHeight)
{
  var minSide = Math.min(quadWidth, quadHeight);
  var tx = (minSide == quadWidth)?0: (quadWidth-minSide)/2,
      ty = (minSide == quadHeight)?0: (quadHeight-minSide)/2;
  var scale = minSide/gaugeWidth;
  gauge.setAttribute("transform","translate("+tx+","+ty+") scale("+scale+","+scale+")");
}

ApacheGaugeChart.prototype.GetChartEvent = function(e)
{
  var evtTarget = e.target;
  
  while(evtTarget != null && evtTarget.tagName != "g")
    evtTarget = evtTarget.parentNode;
  
  if(evtTarget == null)
    return null;
      
  var i = parseInt(evtTarget.getAttribute("yValueIndex"));

  var model = this._model, yValues = model.getYValues();
  return new ApacheChartEvent(null,[i], [yValues[i][0]], null);
}

ApacheGaugeChart.prototype.FillToolTipData = function(boundingRectElem, circleElem, e)
{
  var chartEvent = this.GetChartEvent(e);
  if(chartEvent == null)
    return;
    
  var value = chartEvent.getYValues()[0];
  var groupLabels = this._model.getGroupLabels();
  
  var textElem = boundingRectElem.nextSibling.nextSibling;
  textElem.firstChild.data = ""+this._formatValue(value);
                
  var labelWidth = textElem.getComputedTextLength();      
  //We do not need the next label 
  textElem = textElem.nextSibling.nextSibling;
  textElem.firstChild.data = "";
  var rectWidth = labelWidth+2*ApacheChart._TEXT_MARGIN;
  boundingRectElem.setAttribute("width",rectWidth);
  boundingRectElem.setAttribute("stroke", seriesColors[0]);
  circleElem.setAttribute("r",0);
}

ApacheGaugeChart.prototype.GetToolTipLocation = function(e, ttBBox)
{
  return {x:(e.clientX+20), y:(e.clientY+20)};
}

////////////////////////////////////////////////////////////////////
// Semi-Circular gauge chart subclass
////////////////////////////////////////////////////////////////////
function ApacheSemiGaugeChart(
  type, model, svgEmbedId, 
  isPerspective, legendPosition)
{
  this.Init(type, model, svgEmbedId, isPerspective, legendPosition);
}

ApacheChartObj.Inherit(ApacheGaugeChart, ApacheSemiGaugeChart);


ApacheSemiGaugeChart.prototype.SetIndicatorPosition = function(yValue, indicator, ratio)
{
  var theta, cx = this._animCx, cy = this._animCy; 
  var model = this._model, minValue = model.getMinYValue(), maxValue = model.getMaxYValue();
  var valueRatio = ratio*(yValue - minValue)/(maxValue-minValue);

  theta = valueRatio*Math.PI;
  theta *= 180/Math.PI;
  indicator.setAttribute("transform", "rotate("+theta+" "+cx+" "+cy+")");
}

ApacheSemiGaugeChart.prototype.GetGaugeTemplateName = function()
{
  return "semiGauge";
}

ApacheSemiGaugeChart.prototype.CreateTextMarkerGroup = function(gauge, gaugeR)
{
  var svgDoc = this._svgDoc, model = this._model;
  gElem = svgDoc.createElementNS("http://www.w3.org/2000/svg", "g");
  gauge.appendChild(gElem);
  this._markerTextGroup = gElem;
  
  var majorMarker = svgDoc.getElementById("gaugeMarkerMajor"),
      majorMarkerCount = this._yMajorGridCount,
      minorMarker = svgDoc.getElementById("gaugeMarkerMinor"),
      minorMarkerCount = this._yMinorGridCount,
      textElem = svgDoc.getElementById("gaugeTextPrototype");
  
  var paths = gauge.getElementsByTagName("path");
  var markerContainerR = parseInt(gauge.getAttribute("_markerRadius"));
  var minValue = model.getMinYValue(), maxValue = model.getMaxYValue();
  var x, y, angle, textMargin;
  
  for(var i=0; i<=majorMarkerCount; ++i)
  {
    var theta = i*Math.PI/majorMarkerCount;
    x = gaugeR-markerContainerR*(Math.cos(theta));
    y = gaugeR-markerContainerR*(Math.sin(theta));
    var angle = theta*180/Math.PI;
    var marker = majorMarker.cloneNode(true);
    gElem.appendChild(marker);
    marker.setAttribute("transform",
          "translate("+x.toFixed(0)+","+y.toFixed(0)+") rotate("+angle.toFixed(0)+" 0 0)");
    var value = minValue + i*(maxValue-minValue)/(majorMarkerCount);
    textElem = textElem.cloneNode(true);
    textElem.firstChild.data = this._formatValue(value);
    gElem.appendChild(textElem);
    var textBBox = textElem.getBBox();
    if(i == 0)
    {
      textMargin = textBBox.height/2;
    }
    x = gaugeR-(markerContainerR-textMargin)*(Math.cos(theta));
    y = gaugeR-(markerContainerR-textMargin)*(Math.sin(theta));

    if(theta >= Math.PI/3 && theta <= 2*Math.PI/3)
    {
      y += textBBox.height;
      x -= textBBox.width/2
    }
    else
    {
      y += textBBox.height/2;
      if(theta < Math.PI/2)
        x += 2*ApacheChart._TEXT_MARGIN;
      else
        x -= textBBox.width + 2*ApacheChart._TEXT_MARGIN;
    }

    textElem.setAttribute("transform",
          "translate("+x.toFixed(0)+","+y.toFixed(0)+")");
  }

  for(var i=1; i<=(majorMarkerCount)*minorMarkerCount; ++i)
  {
    if(i%minorMarkerCount == 0)
      continue;
      
    var theta = i*Math.PI/(majorMarkerCount*minorMarkerCount);
    var x = gaugeR-markerContainerR*(Math.cos(theta));
    var y = gaugeR-markerContainerR*(Math.sin(theta));
    var angle = theta*180/Math.PI;
    var marker = minorMarker.cloneNode(true);
    gElem.appendChild(marker);
    marker.setAttribute("transform",
          "translate("+x.toFixed(0)+","+y.toFixed(0)+") rotate("+angle.toFixed(0)+" 0 0)");
  }
}

ApacheSemiGaugeChart.prototype.ScaleGauge = function(
  gauge, 
  quadWidth, 
  quadHeight, 
  gaugeWidth, 
  gaugeHeight)
{
  var sx = quadWidth/gaugeWidth, sy = quadHeight/gaugeHeight;
  var scale = Math.min(sx, sy);
  var tx = (quadWidth<=gaugeWidth)?0:(quadWidth-gaugeWidth)/2,
      ty = (quadHeight<=gaugeHeight)?0:(quadHeight-gaugeHeight)/2;
  gauge.setAttribute("transform","translate("+tx+","+ty+") scale("+scale+","+scale+")");
}
