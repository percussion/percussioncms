function ApacheChartObj()
{
this.Init();
}
ApacheChartObj.prototype=new Object();
ApacheChartObj.prototype.constructor=ApacheChartObj;
ApacheChartObj._tempConstructor=function(){}
ApacheChartObj.Inherit=function(a0,a1)
{
var a2=ApacheChartObj._tempConstructor;
a2.prototype=a0.prototype;
a1.prototype=new a2();
a1.prototype.constructor=a1;
a1.superclass=a0.prototype;
}
ApacheChartObj.prototype.createCallback=function(a3)
{
var a4=new Function(
"var f=arguments.callee; return f._func.apply(f._owner, arguments);");
a4._owner=this;
a4._func=a3;
return a4;
}
ApacheChartObj.Assert=function(a5,a6)
{
if(!a5)
{
throw new Error(a6);
}
}
function ApacheChartBuffer(a0)
{
this.maxStreamLength=document.all?5000:100000;
this.data=new Array(a0?a0:100);
this.iStr=0;
}
ApacheChartBuffer.prototype.append=function(a1)
{
this.data[this.iStr++]=a1;
if(this.data.length>this.maxStreamLength)
{
this.data=[this.data.join("")];
this.data.length=100;
this.iStr=1;
}
return this;
}
ApacheChartBuffer.prototype.toString=function()
{
return this.data.join("");
}
function ApacheChartModel(a0,a1,a2,a3,a4)
{
this._seriesLabels=a0;
this._groupLabels=a1;
this._yValues=a2;
this._xValues=a3;
this._seriesColors=a4;
}
ApacheChartModel.prototype.getSeriesLabels=function()
{
return this._seriesLabels;
}
ApacheChartModel.prototype.getGroupLabels=function()
{
return this._groupLabels;
}
ApacheChartModel.prototype.getSeriesColors=function()
{
return this._seriesColors;
}
ApacheChartModel.prototype.getXValues=function()
{
return this._xValues;
}
ApacheChartModel.prototype.getYValues=function()
{
return this._yValues;
}
ApacheChartModel.prototype.setMaxYValue=function(a5)
{
this._maxYValue=a5;
}
ApacheChartModel.prototype.getMaxYValue=function()
{
return this._maxYValue;
}
ApacheChartModel.prototype.setMinYValue=function(a6)
{
this._minYValue=a6;
}
ApacheChartModel.prototype.getMinYValue=function()
{
return this._minYValue;
}
ApacheChartModel.prototype.setMaxXValue=function(a7)
{
this._maxXValue=a7;
}
ApacheChartModel.prototype.getMaxXValue=function()
{
return this._maxXValue;
}
ApacheChartModel.prototype.setMinXValue=function(a8)
{
this._minXValue=a8;
}
ApacheChartModel.prototype.getMinXValue=function()
{
return this._minXValue;
}
ApacheChartModel.prototype.setTitle=function(a9)
{
this._title=a9;
}
ApacheChartModel.prototype.getTitle=function()
{
return this._title;
}
ApacheChartModel.prototype.setSubTitle=function(a10)
{
this._subTitle=a10;
}
ApacheChartModel.prototype.getSubTitle=function()
{
return this._subTitle;
}
ApacheChartModel.prototype.setFootNote=function(a11)
{
this._footNote=a11;
}
ApacheChartModel.prototype.getFootNote=function()
{
return this._footNote;
}
function ApacheChartEvent(a0,a1,a2,a3)
{
this._seriesIndices=a0;
this._yValueIndices=a1;
this._yValues=a2;
this._xValues=a3;
}
ApacheChartEvent.prototype.getSeriesIndices=function()
{
return this._seriesIndices;
}
ApacheChartEvent.prototype.getYValueIndices=function()
{
return this._yValueIndices;
}
ApacheChartEvent.prototype.getYValues=function()
{
return this._yValues;
}
ApacheChartEvent.prototype.getXValues=function()
{
return this._xValues;
}
ApacheChartEvent.prototype.toString=function()
{
var a4=new ApacheChartBuffer();
if(this._seriesIndices)
a4.append("seriesIndices = "+this._seriesIndices.join(","));
if(this._yValueIndices)
a4.append("\yValueIndices = "+this._yValueIndices.join(","));
a4.append("\nyValues = "+this._yValues.join(","));
if(this._xValues)
a4.append("\nxValues = "+this._xValues.join(","));
return a4.toString();
}
ApacheChartEvent.prototype.marshall=function()
{
var a5=new Array();
if(this._seriesIndices)
a5.push("seriesIndices\t"+this._seriesIndices.join("\t"));
if(this._yValueIndices)
a5.push("yValueIndices\t"+this._yValueIndices.join("\t"));
a5.push("yValues\t"+this._yValues.join("\t"));
if(this._xValues)
a5.push("xValues\t"+this._xValues.join("\t"));
return a5.join("$adf$");
}
function ApacheChart(a0,a1,a2,a3,a4)
{
this.Init(a0,a1,a2,a3,a4);
}
ApacheChart.createSVG=function(a5,
a6,a7,a8,a9)
{
var a10=document.getElementById(a5);
var a11=document.createElement("embed");
var a12=window._agent;
if(a12&&a12.isIE)
{
var a13=a7.indexOf(";");
if(a13!=-1)
a7=a7.substr(0,a13);
}
a11.setAttribute("src",a7);
a11.setAttribute("id",a6);
a11.setAttribute("wmode","transparent");
a11.setAttribute("type","image/svg+xml");
if(a12&&a12.isOpera)
{
var a14=document.defaultView.getComputedStyle(a10,null);
var a15=a11.style;
a15.width=a14.width;
a15.height=a14.height;
}
else
{
a11.style.cssText=a8;
}
if(a9)
{
a11.className=a9;
}
a10.appendChild(a11);
}
ApacheChart.isASVInstalled=function()
{
try{
var a16=new ActiveXObject("Adobe.SVGCtl");
return true;
}
catch(e){
}
return false;
}
ApacheChartObj.Inherit(ApacheChartObj,ApacheChart);
ApacheChart.prototype.Init=function(a17,a18,a19,
a20,a21)
{
this._type=a17;
this._model=a18;
this._svgEmbedId=a19;
this._margins={left:2,right:2,top:2,bottom:2};
this._isPerspective=a20;
this._legendPosition=a21;
this._toolTip=null;
this._toolTipVisible=false;
this._animDuration=1500;
this._dataElems=[];
this._labelElems=[];
this._gridElems=[];
this._groupLabelElems=[];
this._yMajorGridCount=8;
this._yMinorGridCount=-1;
this._xMajorGridCount=-1;
this._tooltipsVisible=true;
this._gradientsUsed=true;
this._maxPrecision=0;
this._decimalSep=null;
this._formName=null;
this._partialSubmit=true;
this._svgCheckTotal=0;
this._errorTextNode=null;
this._isIE=false;
if(window._agent)
this._isIE=_agent.isIE;
if(this._isIE)
this._errorHtml="<H4>Unable to load SVG plugin. Please install the plugin from <a href='#' onclick=\"window.open('http://www.adobe.com/svg/viewer/install/main.html')\">Adobe</a><H4>";
else
this._errorHtml="<H4>This component needs an SVG enabled browser like Internet Explorer, Firefox 1.5+ or Opera 9.0+<H4>";
this._statusHtml="<H4>Please Wait. Attempting to load SVG document...</H4>";
this.ComputeMinMaxValues();
}
ApacheChart.prototype.setErrorHtml=function(a22)
{
this._errorHtml=a22;
}
ApacheChart.prototype.setStatusHtml=function(a23)
{
this._statusHtml=a23;
}
ApacheChart.prototype.setYMajorGridLineCount=function(a24)
{
this._yMajorGridCount=a24>0?a24+1:a24;
}
ApacheChart.prototype.setYMinorGridLineCount=function(a25)
{
this._yMinorGridCount=a25>0?a25+1:a25;
}
ApacheChart.prototype.setXMajorGridLineCount=function(a26)
{
this._xMajorGridCount=a26>0?a26+1:a26;
}
ApacheChart.prototype.setAnimationDuration=function(a27)
{
this._animDuration=a27;
}
ApacheChart.prototype.setGradientsUsed=function(a28)
{
this._gradientsUsed=a28;
}
ApacheChart.prototype.setMaxPrecision=function(a29)
{
this._maxPrecision=a29;
}
ApacheChart.prototype.setFormName=function(a30)
{
this._formName=a30;
}
ApacheChart.prototype.setPartialSubmit=function(a31)
{
this._partialSubmit=a31;
}
ApacheChart.prototype.setTooltipsVisible=function(a32)
{
this._tooltipsVisible=a32;
}
ApacheChart.prototype.getToolTip=function()
{
return this._toolTip;
}
ApacheChart.prototype.setToolTip=function(a33)
{
this._toolTip=a33;
}
ApacheChart.prototype.ComputeMinMaxValues=function()
{
var a34=this._model,yValues=a34.getYValues(),xValues=a34.getXValues(),
maxYValue=a34.getMaxYValue(),maxXValue=a34.getMaxXValue(),
minYValue=a34.getMinYValue(),minXValue=a34.getMinXValue(),
seriesLabels=a34.getSeriesLabels();
if(yValues!=null&&(maxYValue==null||minYValue==null))
{
var a35=this._computeAxisMinMaxValues(yValues,seriesLabels.length);
if(maxYValue==null)
a34.setMaxYValue(a35.max);
if(minYValue==null)
a34.setMinYValue(a35.min);
}
if(xValues!=null&&(maxXValue==null||minXValue==null))
{
var a35=this._computeAxisMinMaxValues(xValues,seriesLabels.length);
if(maxXValue==null)
a34.setMaxXValue(a35.max);
if(minXValue==null)
a34.setMinXValue(a35.min);
}
}
ApacheChart.prototype._computeAxisMinMaxValues=function(a36,a37)
{
var a38,value,maxValue=Number.NEGATIVE_INFINITY,minValue=Number.POSITIVE_INFINITY,
type=this._type,isStacked=false,groupsCount=a36.length;
if(type==ApacheChart.TYPE_VBAR_STACKED||type==ApacheChart.TYPE_HBAR_STACKED||
type==ApacheChart.TYPE_AREA_STACKED)
{
isStacked=true;
}
for(var a39=0;a39<groupsCount;++a39)
{
a38=0;
for(var a40=0;a40<a37;++a40)
{
value=a36[a39][a40];
if(isStacked)
a38+=value;
else
{
maxValue=Math.max(maxValue,value);
minValue=Math.min(minValue,value);
}
}
if(isStacked)
{
maxValue=Math.max(maxValue,a38);
minValue=Math.min(minValue,a38);
}
}
var a41=maxValue>0?ApacheChart._MAX_MULTIPLIER:ApacheChart._MIN_MULTIPLIER,
minMult=minValue>0?ApacheChart._MIN_MULTIPLIER:ApacheChart._MAX_MULTIPLIER;
return{max:maxValue*a41,min:minValue*minMult};
}
ApacheChart.TYPE_VBAR=1;
ApacheChart.TYPE_HBAR=2;
ApacheChart.TYPE_VBAR_STACKED=3;
ApacheChart.TYPE_HBAR_STACKED=4;
ApacheChart.TYPE_PIE=5;
ApacheChart.TYPE_AREA=6;
ApacheChart.TYPE_AREA_STACKED=7;
ApacheChart.TYPE_LINE=8;
ApacheChart.TYPE_BAR_LINE_COMBO=9;
ApacheChart.TYPE_XYLINE=10;
ApacheChart.TYPE_SCATTER_PLOT=11;
ApacheChart.TYPE_RADAR=12;
ApacheChart.TYPE_RADAR_AREA=13;
ApacheChart.TYPE_FUNNEL=14;
ApacheChart.CIRCULAR_GAUGE=15;
ApacheChart.SEMI_CIRCULAR_GAUGE=16;
ApacheChart.LEGEND_LOCATION_NONE="none";
ApacheChart.LEGEND_LOCATION_TOP="top";
ApacheChart.LEGEND_LOCATION_END="end";
ApacheChart.LEGEND_LOCATION_BOTTOM="bottom";
ApacheChart.LEGEND_LOCATION_START="start";
ApacheChart._MAX_MULTIPLIER=1.2;
ApacheChart._MIN_MULTIPLIER=.8;
ApacheChart._XOFFSET_PERSPECTIVE=10;
ApacheChart._YOFFSET_PERSPECTIVE=5;
ApacheChart._TEXT_MARGIN=4;
ApacheChart._DEFAULT_STOP_OPACITY=.9;
ApacheChart._BORDER_SIZE=6;
ApacheChart._ANIMATE_INTERVAL=66;
ApacheChart._SVGCHECK_INTERVAL=100;
ApacheChart._SVGCHECK_STATUS_LIMIT=5000;
ApacheChart._SVGCHECK_MAX_LIMIT=20000;
ApacheChart.createChart=function(
a42,
a43,
a44,
a45,
a46)
{
var a47=null;
if(a42==this.TYPE_VBAR||a42==this.TYPE_VBAR_STACKED||a42==this.TYPE_BAR_LINE_COMBO)
{
a47=new ApacheBarChart(a42,a43,a44,
a45,a46);
}
else if(a42==this.TYPE_HBAR||a42==this.TYPE_HBAR_STACKED)
{
a47=new ApacheHBarChart(a42,a43,a44,
a45,a46);
}
else if(a42==this.TYPE_PIE)
{
a47=new ApachePieChart(a42,a43,a44,
a45,a46);
}
else if(a42==this.TYPE_AREA||a42==this.TYPE_AREA_STACKED)
{
a47=new ApacheAreaChart(a42,a43,a44,
a45,a46);
}
else if(a42==this.TYPE_LINE)
{
a47=new ApacheLineChart(a42,a43,a44,
a45,a46);
}
else if(a42==this.TYPE_SCATTER_PLOT)
{
a47=new ApacheScatterPlotChart(a42,a43,a44,
a45,a46);
}
else if(a42==this.TYPE_XYLINE)
{
a47=new ApacheXYLineChart(a42,a43,a44,
a45,a46);
}
else if(a42==this.TYPE_RADAR||a42==this.TYPE_RADAR_AREA)
{
a47=new ApacheRadarChart(a42,a43,a44,
a45,a46);
}
else if(a42==this.TYPE_FUNNEL)
{
a47=new ApacheFunnelChart(a42,a43,a44,
a45,a46);
}
else if(a42==this.SEMI_CIRCULAR_GAUGE)
{
a47=new ApacheSemiGaugeChart(a42,a43,a44,
a45,a46);
}
else if(a42==this.CIRCULAR_GAUGE)
{
a47=new ApacheGaugeChart(a42,a43,a44,
a45,a46);
}
return a47;
}
ApacheChart.prototype.setPerspective=function(a48)
{
this._isPerspective=a48;
}
ApacheChart.prototype.clear=function()
{
var a49=this._rootElement;
var a50=a49.firstChild;
while(a50)
{
a49.removeChild(a50);
a50=a49.firstChild;
}
}
ApacheChart.prototype.draw=function()
{
if(!this._initDocument())
return;
if(this._gradientsUsed&&!this._gradientsInitialized)
{
this.InitializeGradients();
this._gradientsInitialized=true;
}
if(this._tooltipsVisible)
{
this.ShowToolTipCallback=this.createCallback(this.ShowToolTip);
this.HideToolTipCallback=this.createCallback(this.HideToolTip);
}
this.ClickCallback=this.createCallback(this.Click);
this.DrawBorder();
this.DrawTitles();
this.DrawGroupLabels();
this.DrawYValueLabels();
this.AdjustMarginsForGroupLabels();
this.AdjustMarginsForYLabels();
this.DrawLegend();
this.LayoutGroupLabels();
this.LayoutYValueLabels();
this.DrawGrid();
this.DrawChartData();
this.Animate();
}
ApacheChart.prototype._initDocument=function()
{
var a51=document.getElementById(this._svgEmbedId);
var a52=this._isIE;
if(a52&&!ApacheChart.isASVInstalled())
{
this._displayErrorHtml(a51);
return false;
}
try
{
var a53=a51.getSVGDocument();
this._rootElement=a53.getElementById("chartRoot");
if(!this._rootElement)
throw"not yet loaded";
this._svgDoc=a53;
this._width=a51.clientWidth;
this._height=a51.clientHeight;
if(this._errorTextNode!=null)
{
a51.parentNode.removeChild(this._errorTextNode);
a51.style.display="";
}
}
catch(e)
{
this._svgCheckTotal+=ApacheChart._SVGCHECK_INTERVAL;
if(this._svgCheckTotal>ApacheChart._SVGCHECK_MAX_LIMIT)
{
this._displayErrorHtml(a51);
return false;
}
else if(null==this._errorTextNode&&
this._svgCheckTotal>ApacheChart._SVGCHECK_STATUS_LIMIT)
{
this._displayStatusHtml(a51);
}
if(!this._drawCallback)
this._drawCallback=this.createCallback(this.draw);
window.setTimeout(this._drawCallback,ApacheChart._SVGCHECK_INTERVAL);
return false;
}
return true;
}
ApacheChart.prototype._displayStatusHtml=function(a54)
{
var a55=this._errorTextNode=document.createElement("span");
a55.innerHTML=this._statusHtml;
a54.parentNode.insertBefore(a55,a54);
a54.style.display="none";
}
ApacheChart.prototype._displayErrorHtml=function(a56)
{
if(this._errorTextNode)
{
this._errorTextNode.innerHTML=this._errorHtml;
return;
}
else
{
var a57=this._errorTextNode=document.createElement("span");
a57.innerHTML=this._errorHtml;
a56.parentNode.insertBefore(a57,a56);
}
a56.style.display="none";
}
ApacheChart.prototype.DrawChartData=function()
{
}
ApacheChart.prototype.Animate=function()
{
var a58=this._animDuration;
if(a58>0)
{
if(this._animCallback==null)
this._animCallback=this.createCallback(this.DoAnimation);
this._startTime=(new Date()).getTime();
this._intervalId=window.setInterval(this._animCallback,ApacheChart._ANIMATE_INTERVAL);
}
}
ApacheChart.prototype.DoAnimation=function()
{
var a59=this._animDuration;
var a60=(new Date()).getTime()-this._startTime;
if(a60>=a59)
{
window.clearInterval(this._intervalId);
this.SetDataAnimStep(1);
this.SetLabelsAnimStep(1);
this.SetGridAnimStep(1);
delete this._dataElems;
delete this._labelElems;
delete this._gridElems;
}
else
{
var a61=(a60)/a59;
this.SetDataAnimStep(a61);
this.SetLabelsAnimStep(a61);
this.SetGridAnimStep(a61);
}
}
ApacheChart.prototype.SetDataAnimStep=function(a62)
{
var a63=this._dataElems,animCount=a63.length;
var a64=this._margins,animHorizontal=this.AnimAlongXAxis();
if(animHorizontal)
{
var a65=a64.left;
for(var a66=0;a66<animCount;++a66)
{
var a67=(1-a62)*a65;
a63[a66].setAttribute("transform","translate("+a67+",0) scale("+a62+",1)");
}
}
else
{
var a68=a64.bottom,cy=(this._height-a68);
for(var a66=0;a66<animCount;++a66)
{
var a69=(1-a62)*cy;
a63[a66].setAttribute("transform","translate(0,"+a69+") scale(1,"+a62+")");
}
}
}
ApacheChart.prototype.AnimAlongXAxis=function(a70)
{
return false;
}
ApacheChart.prototype.SetLabelsAnimStep=function(a71)
{
var a72=this._labelElems,animCount=a72.length;
for(var a73=0;a73<animCount;++a73)
{
a72[a73].setAttribute("fill-opacity",a71);
}
}
ApacheChart.prototype.SetGridAnimStep=function(a74)
{
var a75=this._gridElems,animCount=a75.length;
var a76=this._margins,animHorizontal=this.AnimAlongXAxis();
if(animHorizontal)
{
var a77=a76.bottom,cy=(this._height-a77);
for(var a78=0;a78<animCount;++a78)
{
var a79=(1-a74)*cy;
a75[a78].setAttribute("transform","translate(0,"+a79+") scale(1,"+a74+")");
}
}
else
{
var a80=a76.left;
for(var a78=0;a78<animCount;++a78)
{
var a81=(1-a74)*a80;
a75[a78].setAttribute("transform","translate("+a81+",0) scale("+a74+",1)");
}
}
}
ApacheChart.prototype.InitializeGradients=function()
{
var a82=this._svgDoc,model=this._model,seriesColors=model.getSeriesColors(),
seriesCount=model.getSeriesLabels().length;
var a83=a82.getElementById("gradients");
ApacheChartObj.Assert(a83,"No Gradients element in the SVG document");
var a84=a83.childNodes;
ApacheChartObj.Assert(a83.childNodes.length>1,"No Gradient Template in the SVG document");
var a85=a83.childNodes[0],gradientElement;
for(var a86=0;a86<seriesCount;++a86)
{
gradientElement=a82.getElementById("gradient"+a86);
if(gradientElement==null)
{
gradientElement=a85.cloneNode(true);
a83.appendChild(gradientElement);
}
var a87=gradientElement.firstChild;
var a88=0;
while(a87)
{
if(a87.nodeName=="stop")
{
var a89=seriesColors[a86];
a89=(a88==0)?a89:this._getLighterColor(a89);
a87.setAttribute("stop-color",a89);
this.SetStopOpacity(a87);
if(a88>=1)
break;
a88++;
}
a87=a87.nextSibling;
}
}
}
ApacheChart.prototype.SetStopOpacity=function(a90)
{
a90.setAttribute("stop-opacity",ApacheChart._DEFAULT_STOP_OPACITY);
}
ApacheChart.prototype._getLighterColor=function(a91)
{
if(a91.indexOf("#")>=0)
{
a91=a91.substr(1);
var a92=a91.substr(0,2),gVal=a91.substr(2,2),bVal=a91.substr(4);
a91="#"+this._getLighterNumberStr(a92)+this._getLighterNumberStr(gVal)+
this._getLighterNumberStr(bVal);
}
else
{
a91=a91.toLowerCase().replace(" ","");
a91=a91.substring(4,a91.length-1);
var a93=a91.split(",");
a91="#"+this._getLighterNumberStr(a93[0])+this._getLighterNumberStr(a93[1])+
this._getLighterNumberStr(a93[2]);
}
return a91;
}
ApacheChart.prototype._getLighterNumberStr=function(a94)
{
var a95=Math.round(parseInt(a94,16)*1.7);
if(a95>255)
a95=255;
return this._to_hex(a95);
}
ApacheChart.prototype._to_hex=function(a96)
{
var a97=ApacheChart._digit_array;
if(a97==null)
{
a97=ApacheChart._digit_array=
['0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'];
}
var a98=''
var a99=true;
for(var a100=32;a100>0;){
a100-=4;
var a101=(a96>>a100)&0xf;
if(!a99||a101!=0){
a99=false;
a98+=a97[a101];
}
}
return''+(a98==''?'0':a98);
}
ApacheChart.prototype.DrawBorder=function()
{
var a102=this._svgDoc,rootElem=this._rootElement;
var a103=a102.getElementById("borderPrototype").cloneNode(false);
var a104=ApacheChart._BORDER_SIZE,stroke=a104/2;
a103.setAttribute("x",0);
a103.setAttribute("y",0);
a103.setAttribute("rx",stroke);
a103.setAttribute("ry",stroke);
a103.setAttribute("width",this._width-stroke);
a103.setAttribute("height",this._height-stroke);
a103.setAttribute("stroke-width",stroke);
rootElem.appendChild(a103);
var a105=this._margins;
a105.left+=a104;
a105.right+=a104;
a105.top+=a104;
a105.bottom+=a104;
}
ApacheChart.prototype.DrawTitles=function()
{
var a106=this._model,title=a106.getTitle(),
subTitle=a106.getSubTitle(),footNote=a106.getFootNote();
if(title)
this._drawTitleElem("titleTextPrototype",title,false);
if(subTitle)
this._drawTitleElem("subTitleTextPrototype",subTitle,false);
if(footNote)
this._drawTitleElem("footNoteTextPrototype",footNote,true);
}
ApacheChart.prototype._drawTitleElem=function(a107,a108,a109)
{
var a110=this._svgDoc,rootElem=this._rootElement;
var a111=this._margins,gridWidth=(this._width-a111.left-a111.right);
var a112=this._labelElems,animate=(this._animDuration>0);
var a113=a110.getElementById(a107).cloneNode(true);
if(animate)
{
a112.push(a113);
a113.setAttribute("fill-opacity","0");
}
a113.firstChild.data=a108;
rootElem.appendChild(a113);
var a114=a113.getBBox(),textWidth=a114.width,dx=a111.left;
if(a109&&this._width>textWidth+a111.right)
dx=(this._width-textWidth)-a111.right;
if(!a109&&gridWidth>textWidth)
dx=(gridWidth-textWidth)/2+a111.left;
a113.setAttribute("x",dx);
if(a109)
{
a113.setAttribute("y",this._height-a111.bottom);
a111.bottom+=a114.height+ApacheChart._TEXT_MARGIN;
}
else
{
a111.top+=a114.height;
a113.setAttribute("y",a111.top);
a111.top+=ApacheChart._TEXT_MARGIN;
}
}
ApacheChart.prototype.DrawGroupLabels=function()
{
var a115=this._svgDoc,rootElem=this._rootElement,model=this._model;
var a116=a115.createElementNS("http://www.w3.org/2000/svg","g");
this._hLabelContainer=a116;
var a117=model.getGroupLabels(),vLineCount=a117.length;
var a118,labelElems=this._labelElems,animate=(this._animDuration>0);
var a119,gLabelElems=this._groupLabelElems;
for(var a120=0;a120<vLineCount;++a120)
{
if(a120==0)
{
a118=a115.getElementById("groupLabelPrototype");
}
a119=a117[a120];
if(!a119)
continue;
a118=a118.cloneNode(true);
if(animate)
{
labelElems.push(a118);
a118.setAttribute("fill-opacity","0");
}
a118.firstChild.data=a119;
a116.appendChild(a118);
gLabelElems[a120]=a118;
}
rootElem.appendChild(a116);
}
ApacheChart.prototype.LayoutGroupLabels=function()
{
var a121=this._model,margins=this._margins,marginLeft=margins.left;
var a122=this._hLabelContainer,childNodes=a122.childNodes;
if(childNodes.length==0)
return;
if(this._isPerspective)
marginLeft+=ApacheChart._XOFFSET_PERSPECTIVE;
var a123=(this._width-marginLeft-margins.right);
var a124=this.IsGroupLabelCentered();
var a125=a121.getGroupLabels(),vLineCount=a125.length;
var a126,groupWidth=a123/(a124?vLineCount:vLineCount-1);
var a127=0,dy=this._height-margins.bottom+a122.getBBox().height+ApacheChart._TEXT_MARGIN;
var a128=this._groupLabelElems;
for(var a129=0;a129<vLineCount;++a129)
{
a126=a128[a129];
if(!a126)
continue;
a126.setAttribute("y",dy);
var a130=a126.getComputedTextLength();
if(a124)
{
if(groupWidth>a130)
a127=(groupWidth-a130)/2;
else
a127=2;
}
else
{
a127=(-a130)/2;
if(this._isPerspective)
a127-=ApacheChart._XOFFSET_PERSPECTIVE;
}
a126.setAttribute("x",marginLeft+a127+a129*groupWidth);
}
}
ApacheChart.prototype.IsGroupLabelCentered=function()
{
return true;
}
ApacheChart.prototype.AdjustMarginsForGroupLabels=function()
{
var a131=this._hLabelContainer;
if(a131&&a131.childNodes.length>0)
{
this._margins.bottom+=a131.getBBox().height+ApacheChart._TEXT_MARGIN;
var a132=this.IsGroupLabelCentered();
if(!a132)
{
var a133=a131.lastChild.getBBox().width;
if(a133/2>this._margins.right)
this._margins.right=a133/2;
}
}
}
ApacheChart.prototype.DrawLegend=function()
{
var a134=this._legendPosition;
if(a134==ApacheChart.LEGEND_LOCATION_NONE)
{
return;
}
var a135=this._svgDoc,rootElem=this._rootElement,model=this._model;
var a136=this._gradientsUsed;
var a137=model.getSeriesLabels(),seriesCount=a137.length,
seriesColors=model.getSeriesColors();
var a138,rectElem,legendRectHeight,
legendGroup=a135.createElementNS("http://www.w3.org/2000/svg","g");
var a139=this._margins,marginLeft=a139.left,marginTop=a139.top;
var a140=this._labelElems,animate=(this._animDuration>0);
rootElem.appendChild(legendGroup);
if(this._isPerspective)
{
marginLeft+=ApacheChart._XOFFSET_PERSPECTIVE;
}
var a141=(this._width-marginLeft-a139.right),
gridHeight=(this._height-marginTop-a139.bottom);
if(animate)
{
a140.push(legendGroup);
legendGroup.setAttribute("fill-opacity","0");
}
var a142=0,dy=0,tx=marginLeft,ty=this._height-a139.bottom;
var a143=(a134==ApacheChart.LEGEND_LOCATION_START||
a134==ApacheChart.LEGEND_LOCATION_END)
for(var a144=0;a144<seriesCount;++a144)
{
if(a144==0)
{
a138=a135.getElementById("legendTextPrototype");
rectElem=a135.getElementById("legendRectPrototype");
legendRectHeight=parseInt(rectElem.getAttribute("height"));
}
if(a143)
a142=0;
rectElem=rectElem.cloneNode(false);
rectElem.setAttribute("x",a142);
rectElem.setAttribute("y",dy-legendRectHeight);
if(a136)
rectElem.setAttribute("fill","url(#gradient"+a144+")");
else
rectElem.setAttribute("fill",seriesColors[a144]);
rectElem.setAttribute("stroke","#000000");
legendGroup.appendChild(rectElem);
a142+=1.5*legendRectHeight;
a138=a138.cloneNode(true);
a138.setAttribute("x",a142);
a138.setAttribute("y",dy);
a138.firstChild.data=a137[a144];
legendGroup.appendChild(a138);
if(!a143)
a142+=a138.getComputedTextLength()+legendRectHeight;
else
dy+=1.5*legendRectHeight;
if(a144==0&&!a143)
{
var a145=a138.getBBox();
if(a134==ApacheChart.LEGEND_LOCATION_TOP)
{
ty=this.SetLegendTopAdjustment(a139.top+a145.height);
a139.top+=a145.height+ApacheChart._TEXT_MARGIN;
}
else
{
ty=this.SetLegendBottomAdjustment(ty);
a139.bottom+=a145.height+ApacheChart._TEXT_MARGIN;
}
}
}
if(!a143&&a141>a142)
tx=(a141-a142)/2+marginLeft;
if(a143)
{
var a146=legendGroup.getBBox();
if(a134==ApacheChart.LEGEND_LOCATION_START)
{
tx=this.SetLegendLeftAdjustment(a139.left);
a139.left+=a146.width+ApacheChart._TEXT_MARGIN;
}
else
{
a139.right+=a146.width+ApacheChart._TEXT_MARGIN;
tx=this._width-a139.right+ApacheChart._TEXT_MARGIN;
tx=this.SetLegendRightAdjustment(tx);
}
if(gridHeight>dy)
ty=(gridHeight-a146.height)/2+marginTop;
else
ty=gridHeight+marginTop-a146.height;
}
legendGroup.setAttribute("transform","translate("+tx+","+ty+")");
}
ApacheChart.prototype.SetLegendTopAdjustment=function(a147)
{
return a147;
}
ApacheChart.prototype.SetLegendBottomAdjustment=function(a148)
{
var a149=this._hLabelContainer;
if(a149&&a149.childNodes.length>0)
{
a148+=a149.getBBox().height+ApacheChart._TEXT_MARGIN;
}
return a148;
}
ApacheChart.prototype.SetLegendLeftAdjustment=function(a150)
{
var a151=this._vLabelContainer;
if(a151)
{
a150-=a151.getBBox().width+ApacheChart._TEXT_MARGIN;
}
return a150;
}
ApacheChart.prototype.SetLegendRightAdjustment=function(a152)
{
return a152;
}
ApacheChart.prototype.DrawGrid=function()
{
if(this._isPerspective)
this.DrawPerspectiveGrid();
else
this.Draw2DGrid();
}
ApacheChart.prototype.Draw2DGrid=function()
{
var a153=this._svgDoc,model=this._model,margins=this._margins;
var a154=this._gridElems,animate=(this._animDuration>0);
var a155=margins.left,marginTop=margins.top;
var a156=(this._width-a155-margins.right);
var a157=(this._height-marginTop-margins.bottom);
var a158=this._gradientsUsed;
var a159=a153.getElementById("gridRectPrototype").cloneNode(false);
a159.setAttribute("x",margins.left);
a159.setAttribute("y",(marginTop));
a159.setAttribute("width",a156);
a159.setAttribute("height",a157);
if(a158)
a159.setAttribute("fill","url(#gridGradient)");
this._rootElement.appendChild(a159);
var a160=a153.getElementById("gridPathPrototype").cloneNode(false);
if(animate)
{
a154.push(a160);
a160.setAttribute("transform","scale(0.00001,1)");
}
var a161=new ApacheChartBuffer(),vLineCount=this.GetVLineCount(),hLineCount=this.GetHLineCount();
for(var a162=0;a162<hLineCount-1;++a162)
{
a161.append("M").append(a155).append(",").append((a162+1)*a157/hLineCount+marginTop);
a161.append("h").append(a156);
}
for(var a162=0;a162<vLineCount-1;++a162)
{
a161.append("M").append(a155+((a162+1)*a156/vLineCount)).append(",").append(marginTop);
a161.append("v").append(a157);
}
a160.setAttribute("d",a161.toString());
a160.removeAttribute("id");
this._rootElement.appendChild(a160);
}
ApacheChart.prototype.GetVLineCount=function()
{
var a163=this._xMajorGridCount;
if(a163>=0)
return a163;
else
return this._model.getGroupLabels().length;
}
ApacheChart.prototype.GetHLineCount=function()
{
return this._yMajorGridCount;
}
ApacheChart.prototype.DrawPerspectiveGrid=function()
{
var a164=this._svgDoc,model=this._model,margins=this._margins;
var a165=this._gridElems,animate=(this._animDuration>0);
var a166=ApacheChart._XOFFSET_PERSPECTIVE,yOffset=ApacheChart._YOFFSET_PERSPECTIVE;
var a167=margins.left,marginTop=margins.top;
var a168=(this._width-a167-margins.right-a166);
var a169=(this._height-marginTop-margins.bottom-yOffset);
var a170=a164.getElementById("gridRectPrototype").cloneNode(false);
var a171=this._gradientsUsed;
a170.setAttribute("x",a167+ApacheChart._XOFFSET_PERSPECTIVE);
a170.setAttribute("y",marginTop);
a170.setAttribute("width",(a168));
a170.setAttribute("height",(a169));
if(a171)
a170.setAttribute("fill","url(#gridGradient)");
a170.removeAttribute("id");
this._rootElement.appendChild(a170);
var a172=new ApacheChartBuffer();
var a173=a164.getElementById("gridPath3dRectPrototype").cloneNode(false);
a172.append("M").append(a167+a166).append(",").append(marginTop);
a172.append("l").append(-a166).append(",").append(yOffset);
a172.append("v").append(a169);
a172.append("l").append(a166).append(",").append(-yOffset);
a172.append("m").append(a168).append(",").append(0);
a172.append("l").append(-a166).append(",").append(yOffset);
a172.append("h").append(-a168);
if(a171)
a173.setAttribute("fill","url(#gridGradient)");
a173.setAttribute("d",a172.toString());
a173.removeAttribute("id");
this._rootElement.appendChild(a173);
a173=a164.getElementById("gridPathPrototype").cloneNode(false);
if(animate)
{
a173.setAttribute("transform","scale(0.00001,1)");
a165.push(a173);
}
var a174=this.GetVLineCount(),hLineCount=this.GetHLineCount();
a172=new ApacheChartBuffer();
for(var a175=0;a175<hLineCount-1;++a175)
{
a172.append("M").append(a167).append(",").append((a175+1)*a169/hLineCount+marginTop+yOffset);
a172.append("l").append(a166).append(",").append(-yOffset);
a172.append("h").append(a168);
}
for(var a175=0;a175<a174-1;++a175)
{
a172.append("M").append(a167+a166+((a175+1)*a168/a174)).append(",").append(marginTop);
a172.append("v").append(a169);
a172.append("l").append(-a166).append(",").append(yOffset);
}
a173.setAttribute("d",a172.toString());
this._rootElement.appendChild(a173);
}
ApacheChart.prototype.DrawYValueLabels=function()
{
var a176=this._svgDoc,rootElem=this._rootElement,model=this._model;
var a177=a176.createElementNS("http://www.w3.org/2000/svg","g");
this._vLabelContainer=a177;
var a178=model.getMinYValue(),maxValue=model.getMaxYValue();
var a179=this._labelElems,animate=(this._animDuration>0);
var a180=a176.getElementById("yLabelPrototype").cloneNode(true);
if(animate)
{
a179.push(a180);
a180.setAttribute("fill-opacity","0");
}
a180.firstChild.data=this._formatValue(a178);
a177.appendChild(a180);
a180=a180.cloneNode(true);
if(animate)
{
a179.push(a180);
a180.setAttribute("fill-opacity","0");
}
a180.firstChild.data=this._formatValue(maxValue);
a177.appendChild(a180);
var a181=this._yMajorGridCount;
for(var a182=0;a182<a181-1;++a182)
{
var a183=((maxValue-a178)*(a182+1)/a181)+a178;
a180=a180.cloneNode(true);
if(animate)
{
a179.push(a180);
a180.setAttribute("fill-opacity","0");
}
a180.firstChild.data=this._formatValue(a183);
a177.appendChild(a180);
}
rootElem.appendChild(a177);
}
ApacheChart.prototype._formatValue=function(a184)
{
var a185=this._decimalSep;
if(a185==null)
{
var a186=window.getLocaleSymbols?getLocaleSymbols():null;
if(a186)
{
this._decimalSep=a186.getDecimalSeparator();
}
else
this._decimalSep=".";
a185=this._decimalSep;
}
a184=a184.toFixed(this._maxPrecision);
a184=a184.toString();
if(a184.indexOf(a185)==-1)
{
a184=a184.replace(".",a185);
}
return a184;
}
ApacheChart.prototype.AdjustMarginsForYLabels=function()
{
var a187=this._vLabelContainer;
if(a187&&a187.childNodes.length>0)
this._margins.left+=a187.getBBox().width+ApacheChart._TEXT_MARGIN;
}
ApacheChart.prototype.LayoutYValueLabels=function()
{
var a188=this._model,margins=this._margins;
var a189=margins.left,marginTop=margins.top;
var a190=this._vLabelContainer,childNodes=a190.childNodes;
var a191=(this._height-marginTop-margins.bottom);
if(this._isPerspective)
a191-=ApacheChart._YOFFSET_PERSPECTIVE;
var a192=a190.getBBox(),textHeight=a192.height;
this.SetVerticalLabelAt(childNodes.item(0),a191+marginTop,
a189,textHeight);
this.SetVerticalLabelAt(childNodes.item(1),marginTop,
a189,textHeight);
var a193=this._yMajorGridCount;
for(var a194=0;a194<a193-1;++a194)
{
this.SetVerticalLabelAt(childNodes.item(a194+2),
(a193-a194-1)*a191/a193+marginTop,
a189,textHeight);
}
}
ApacheChart.prototype.SetVerticalLabelAt=function(
a195,a196,a197,a198)
{
if(this._isPerspective)
a196+=ApacheChart._YOFFSET_PERSPECTIVE;
var a199=ApacheChart._TEXT_MARGIN,
textLength=a195.getComputedTextLength(),dx=a199;
if(a197>textLength+a199)
dx=a197-textLength-a199;
a195.setAttribute("x",dx);
a195.setAttribute("y",a196+a198/2);
}
ApacheChart.prototype.DrawGroupLabelTitle=function(
a200,a201,a202,a203,a204,
a205,a206)
{
if(!a200)
return a206;
var a207=this._labelElems,animate=(this._animDuration>0);
a202.setAttribute("y",a204+a206);
a202.firstChild.data=a200;
a201.appendChild(a202);
var a208=a202.getComputedTextLength();
if(a205>a208)
a203+=(a205-a208)/2;
else
a203+=2;
a202.setAttribute("x",a203);
if(animate)
a207.push(a202);
var a209=a202.getBBox();
a206-=a209.height+ApacheChart._TEXT_MARGIN;
return a206;
}
ApacheChart.prototype.ShowToolTip=function(a210)
{
if(this._toolTipVisible)
return;
var a211=this._model,seriesColors=a211.getSeriesColors();
var a212=this.getToolTip();
if(a212==null)
{
a212=this._svgDoc.getElementById("toolTip").cloneNode(true);
this.setToolTip(a212);
this._rootElement.appendChild(a212);
}
a212.style.setProperty("visibility","visible","");
var a213=a212.firstChild.nextSibling;
var a214=a213.nextSibling.nextSibling;
this.FillToolTipData(a214,a213,a210);
var a215=a212.getBBox();
var a216=this.GetToolTipLocation(a210,a215);
var a217=a216.x,dy=a216.y;
if(a217+a215.width>this._width)
{
a217-=a215.width;
a213.setAttribute("cx",a214.getBBox().width);
}
else
{
a213.setAttribute("cx",0);
}
if(dy-a215.height<0)
{
dy+=a215.height;
a213.setAttribute("cy",0);
}
else
{
a213.setAttribute("cy",a214.getBBox().height);
}
if(this._isPerspective&&this._type!=ApacheChart.TYPE_PIE)
dy+=ApacheChart._YOFFSET_PERSPECTIVE/2
a212.setAttribute("transform","translate("+a217+","+dy+")");
this._toolTipVisible=true;
}
ApacheChart.prototype.GetToolTipLocation=function(a218,a219)
{
var a220=a218.target.getBBox();
return{x:(a220.x+a220.width/2),y:(a220.y-a219.height)};
}
ApacheChart.prototype.GetChartEvent=function(a221)
{
var a222=a221.target;
var a223=parseInt(a222.getAttribute("yValueIndex")),
j=parseInt(a222.getAttribute("seriesIndex"));
var a224=this._model,yValues=a224.getYValues();
return new ApacheChartEvent([j],[a223],[yValues[a223][j]],null);
}
ApacheChart.prototype.FillToolTipData=function(a225,a226,a227)
{
var a228=this.GetChartEvent(a227);
var a229=a228.getSeriesIndices()[0];
var a230=this._model,groupLabels=a230.getGroupLabels(),
seriesLabels=a230.getSeriesLabels(),
yValues=a228.getYValues();
var a231=a225.nextSibling.nextSibling;
a231.firstChild.data=seriesLabels[a229];
var a232=a231.getComputedTextLength();
a231=a231.nextSibling.nextSibling;
a231.firstChild.data=this._formatValue(yValues[0]);
var a233=a231.getComputedTextLength();
var a234=ApacheChart._TEXT_MARGIN,dx=a234;
if(a232>a233)
dx=(a232-a233)/2+a234;
a231.setAttribute("dx",dx);
var a235=Math.max(a232,a233)+2*a234;
a225.setAttribute("width",a235);
a225.setAttribute("stroke",seriesColors[a229]);
a226.setAttribute("stroke",seriesColors[a229]);
}
ApacheChart.prototype.HideToolTip=function(a236)
{
var a237=this.getToolTip();
if(a237)
a237.style.setProperty("visibility","hidden","");
this._toolTipVisible=false;
}
ApacheChart.prototype.Click=function(a238)
{
var a239=this.GetChartEvent(a238);
var a240=this._formName;
if(a240!=null)
{
var a241=document.getElementById(this._svgEmbedId);
var a242=a241.parentNode.id;
var a243={'event':'chartDrillDown',
'source':a242,
'value':a239.marshall()};
if(this._partialSubmit)
{
_submitPartialChange(a240,'0',a243);
}
else
{
submitForm(a240,'0',a243);
}
}
else
alert(a239);
}
function ApacheBarChart(
a0,a1,a2,
a3,a4)
{
this.Init(a0,a1,a2,a3,a4);
}
ApacheChartObj.Inherit(ApacheChart,ApacheBarChart);
ApacheBarChart.prototype.DrawChartData=function()
{
var a5=this._type==ApacheChart.TYPE_BAR_LINE_COMBO;
var a6=this._isPerspective;
if(a6)
this._drawPerspectiveBars(a5);
else
this._drawBars(a5);
if(a5)
{
if(a6)
this.__drawPerspectiveLines=ApacheLineChart.prototype.__drawPerspectiveLines;
else
this.__drawLines=ApacheLineChart.prototype.__drawLines;
ApacheLineChart.prototype.DrawChartData.call(this,a5);
}
}
ApacheBarChart.prototype._drawBars=function(a7)
{
var a8=this._svgDoc,model=this._model,margins=this._margins;
var a9=this._rootElement,dataElems=this._dataElems,animate=(this._animDuration>0);
var a10=margins.left,marginTop=margins.top;
var a11=(this._width-a10-margins.right);
var a12=(this._height-marginTop-margins.bottom);
var a13=a8.getElementById("barRectPrototype");
var a14=ApacheBarChart._BARITEM_PADDING;
var a15=(this._type==ApacheChart.TYPE_VBAR_STACKED);
var a16=model.getGroupLabels(),groupCount=a16.length,
seriesLabels=model.getSeriesLabels(),seriesCount=seriesLabels.length;
var a17=model.getSeriesColors(),yValues=model.getYValues();
var a18=model.getMinYValue(),maxValue=model.getMaxYValue();
var a19=a15?1:(a7?Math.ceil(seriesCount/2):seriesCount);
var a20=yValues.length;
var a21=(a11/Math.max(a20,groupCount)-2*a14)/a19;
var a22=a10,dy,barHeight,stackBase=a18;
var a23=this._gradientsUsed;
var a24="scale(1,0.00001)";
for(var a25=0;a25<a20;++a25)
{
a22+=a14;
dy=a12+marginTop;
for(var a26=0;a26<seriesCount;++a26)
{
if(a7&&a26%2>0)
continue;
if(a15)
stackBase=(a26==0?a18:0);
a13=a13.cloneNode(false);
if(animate)
{
dataElems.push(a13);
a13.setAttribute("transform",a24);
}
a13.setAttribute("x",a22);
barHeight=a12*(yValues[a25][a26]-stackBase)/(maxValue-a18);
if(a15)
dy-=barHeight;
else
dy=a12+marginTop-barHeight;
a13.setAttribute("y",dy);
a13.setAttribute("width",a21);
a13.setAttribute("height",barHeight);
if(a23)
a13.setAttribute("fill","url(#gradient"+a26+")");
else
a13.setAttribute("fill",a17[a26]);
a13.setAttribute("stroke",a17[a26]);
a13.setAttribute("stroke-width",1);
a13.setAttribute("yValueIndex",a25);
a13.setAttribute("seriesIndex",a26);
if(this._tooltipsVisible)
{
a13.addEventListener("mouseover",this.ShowToolTipCallback,false);
a13.addEventListener("mouseout",this.HideToolTipCallback,false);
}
a13.addEventListener("click",this.ClickCallback,false);
a9.appendChild(a13);
if(!a15)
a22+=a21;
}
if(a15)
a22+=a21;
a22+=a14;
}
}
ApacheBarChart.prototype._drawPerspectiveBars=function(a27)
{
var a28=this._svgDoc,model=this._model,margins=this._margins;
var a29=this._rootElement,dataElems=this._dataElems,animate=(this._animDuration>0);
var a30=ApacheChart._XOFFSET_PERSPECTIVE,yOffset=ApacheChart._YOFFSET_PERSPECTIVE;
var a31=margins.left,marginTop=margins.top;
var a32=(this._width-a31-margins.right-a30);
var a33=(this._height-marginTop-margins.bottom-yOffset);
var a34=a28.getElementById("barPathPrototype");
var a35=ApacheBarChart._BARITEM_PADDING;
var a36=(this._type==ApacheChart.TYPE_VBAR_STACKED);
var a37=model.getGroupLabels(),groupCount=a37.length,
seriesLabels=model.getSeriesLabels(),seriesCount=seriesLabels.length;
var a38=model.getSeriesColors(),yValues=model.getYValues();
var a39=model.getMinYValue(),maxValue=model.getMaxYValue();
var a40=a27?Math.ceil(seriesCount/2):seriesCount,barWidth;
var a41=yValues.length;
if(a36)
barWidth=a32/Math.max(a41,groupCount)-2*a35;
else
barWidth=(a32/Math.max(a41,groupCount)-2*a35-(a40)*a35)/a40;
var a42=a31,dy,barHeight,stackBase=a39;
var a43=this._gradientsUsed;
var a44="scale(1, 0.00001)";
for(var a45=0;a45<a41;++a45)
{
a42+=a35;
dy=a33+marginTop+yOffset;
for(var a46=0;a46<seriesCount;++a46)
{
if(a27&&a46%2>0)
continue;
if(a36)
stackBase=(a46==0?a39:0);
barHeight=a33*(yValues[a45][a46]-stackBase)/(maxValue-a39);
if(a36)
dy-=barHeight;
else
dy=a33+yOffset+marginTop-barHeight;
a34=a34.cloneNode(false);
if(animate)
{
dataElems.push(a34);
a34.setAttribute("transform",a44);
}
var a47=new ApacheChartBuffer();
a47.append("M").append(a42).append(",").append(dy);
a47.append("l").append(a30).append(",").append(-yOffset);
a47.append("h").append(barWidth);
a47.append("v").append(barHeight);
a47.append("l").append(-a30).append(",").append(yOffset);
a47.append("v").append(-barHeight);
a47.append("l").append(a30).append(",").append(-yOffset);
a47.append("l").append(-a30).append(",").append(yOffset);
a47.append("h").append(-barWidth);
a47.append("v").append(barHeight);
a47.append("h").append(barWidth);
a47.append("v").append(-barHeight);
a34.setAttribute("stroke",a38[a46]);
a34.setAttribute("stroke-width",1);
if(a43)
a34.setAttribute("fill","url(#gradient"+a46+")");
else
a34.setAttribute("fill",a38[a46]);
a34.setAttribute("d",a47.toString());
a34.setAttribute("yValueIndex",a45);
a34.setAttribute("seriesIndex",a46);
if(this._tooltipsVisible)
{
a34.addEventListener("mouseover",this.ShowToolTipCallback,false);
a34.addEventListener("mouseout",this.HideToolTipCallback,false);
}
a34.addEventListener("click",this.ClickCallback,false);
a29.appendChild(a34);
if(!a36)
{
a42+=barWidth;
a42+=a35;
}
}
if(a36)
a42+=barWidth;
a42+=a35;
}
}
ApacheBarChart.prototype.ShowToolTip=function(a48)
{
if(this._type==ApacheChart.TYPE_BAR_LINE_COMBO)
{
var a49=parseInt(a48.target.getAttribute("seriesIndex"));
if(a49%2>0)
{
try
{
this.GetToolTipLocation=ApacheLineChart.prototype.GetToolTipLocation;
this.FillToolTipData=ApacheLineChart.prototype.FillToolTipData;
this.GetChartEvent=ApacheLineChart.prototype.GetChartEvent;
ApacheLineChart.prototype.ShowToolTip.call(this,a48);
}
finally
{
this.GetToolTipLocation=ApacheBarChart.prototype.GetToolTipLocation;
this.FillToolTipData=ApacheBarChart.prototype.FillToolTipData;
this.GetChartEvent=ApacheBarChart.prototype.GetChartEvent;
}
return;
}
}
ApacheBarChart.superclass.ShowToolTip.call(this,a48);
}
ApacheBarChart._BARITEM_PADDING=2;
function ApacheHBarChart(
a0,a1,a2,
a3,a4)
{
this.Init(a0,a1,a2,a3,a4);
}
ApacheChartObj.Inherit(ApacheChart,ApacheHBarChart);
ApacheHBarChart.prototype.DrawChartData=function()
{
if(this._isPerspective)
this._drawPerspectiveBars();
else
this._drawBars();
}
ApacheHBarChart.prototype.AnimAlongXAxis=function()
{
return true;
}
ApacheHBarChart.prototype.DrawYValueLabels=function()
{
var a5=this._svgDoc,rootElem=this._rootElement,model=this._model;
var a6=a5.createElementNS("http://www.w3.org/2000/svg","g");
this._vLabelContainer=a6;
var a7=a5.getElementById("groupLabelPrototype");
var a8=this._labelElems,animate=(this._animDuration>0);
var a9=model.getGroupLabels(),hLineCount=a9.length;
var a10,gLabelElems=this._groupLabelElems;
for(var a11=0;a11<hLineCount;++a11)
{
a10=a9[a11];
if(!a10)
continue;
a7=a7.cloneNode(true);
a7.firstChild.data=a10;
a6.appendChild(a7);
gLabelElems[a11]=a7;
if(animate)
{
a8.push(a7);
a7.setAttribute("fill-opacity","0");
}
}
rootElem.appendChild(a6);
}
ApacheHBarChart.prototype.LayoutYValueLabels=function()
{
var a12=this._model,margins=this._margins,
marginLeft=margins.left,marginTop=margins.top;
var a13=(this._height-marginTop-margins.bottom);
if(this._isPerspective)
a13-=ApacheChart._YOFFSET_PERSPECTIVE;
var a14=this._vLabelContainer,childNodes=a14.childNodes;
if(childNodes.length==0)
return;
var a15,bBox=a14.getBBox(),textHeight=bBox.height;
var a16=a12.getGroupLabels(),hLineCount=a16.length;
var a17=this._groupLabelElems;
for(var a18=0;a18<hLineCount;++a18)
{
a15=a17[a18];
if(!a15)
continue;
this.SetVerticalLabelAt(a15,
(hLineCount-a18)*a13/hLineCount+marginTop-(a13/(2*hLineCount)),
marginLeft,textHeight);
}
}
ApacheHBarChart.prototype.IsGroupLabelCentered=function()
{
return false;
}
ApacheHBarChart.prototype.DrawGroupLabels=function()
{
var a19=this._svgDoc,rootElem=this._rootElement,model=this._model;
var a20=this._yMajorGridCount;
var a21=a19.createElementNS("http://www.w3.org/2000/svg","g");
this._hLabelContainer=a21;
var a22=model.getMinYValue(),maxValue=model.getMaxYValue();
var a23=a19.getElementById("yLabelPrototype");
var a24,labelElems=this._labelElems,animate=(this._animDuration>0);
for(var a25=0;a25<a20+1;++a25)
{
a23=a23.cloneNode(true);
if(animate)
{
labelElems.push(a23);
a23.setAttribute("fill-opacity","0");
}
if(a25==0)
a24=a22;
else if(a25==a20)
a24=maxValue;
else
a24=(((maxValue-a22)*(a25)/a20)+a22);
a23.firstChild.data=this._formatValue(a24);
a21.appendChild(a23);
}
rootElem.appendChild(a21);
}
ApacheHBarChart.prototype.LayoutGroupLabels=function()
{
var a26=this._model,margins=this._margins,marginLeft=margins.left;
var a27=(this._width-marginLeft-margins.right);
var a28=this._hLabelContainer,childNodes=a28.childNodes;
if(this._isPerspective)
a27-=ApacheChart._XOFFSET_PERSPECTIVE;
var a29=this._yMajorGridCount;
var a30,yValWidth=a27/a29;
var a31=a28.getBBox();
var a32=0,dy=this._height-margins.bottom+a31.height+ApacheChart._TEXT_MARGIN;
var a33=this._labelElems,animate=(this._animDuration>0);
for(var a34=0;a34<a29+1;++a34)
{
a30=childNodes.item(a34);
a30.setAttribute("y",dy);
var a35=a30.getComputedTextLength();
a30.setAttribute("x",marginLeft-a35/2+a34*yValWidth);
}
}
ApacheHBarChart.prototype.GetVLineCount=function()
{
return this._yMajorGridCount;
}
ApacheHBarChart.prototype.GetHLineCount=function()
{
var a36=this._xMajorGridCount;
if(a36>=0)
return a36;
else
return this._model.getGroupLabels().length;
}
ApacheHBarChart.prototype._drawBars=function()
{
var a37=this._svgDoc,model=this._model,margins=this._margins;
var a38=this._rootElement,dataElems=this._dataElems,animate=(this._animDuration>0);
var a39=margins.left,marginTop=margins.top;
var a40=(this._width-a39-margins.right);
var a41=(this._height-marginTop-margins.bottom);
var a42=a37.getElementById("barRectPrototype");
var a43=ApacheBarChart._BARITEM_PADDING;
var a44=(this._type==ApacheChart.TYPE_HBAR_STACKED);
var a45=model.getGroupLabels(),groupCount=a45.length,
seriesLabels=model.getSeriesLabels(),seriesCount=seriesLabels.length;
var a46=model.getSeriesColors(),yValues=model.getYValues();
var a47=model.getMinYValue(),maxValue=model.getMaxYValue();
var a48=a44?1:seriesCount,stackBase=a47;
var a49=yValues.length;
var a50=(a41/Math.max(a49,groupCount)-2*a43)/a48;
var a51=a39,dy=a41+marginTop,barWidth;
var a52=this._gradientsUsed;
var a53="scale(0.00001,1)";
for(var a54=0;a54<a49;++a54)
{
dy-=a43;
a51=a39;
for(var a55=0;a55<seriesCount;++a55)
{
if(a44)
stackBase=(a55==0?a47:0);
a42=a42.cloneNode(false);
if(animate)
{
dataElems.push(a42);
a42.setAttribute("transform",a53);
}
a42.setAttribute("x",a51);
barWidth=a40*(yValues[a54][a55]-stackBase)/(maxValue-a47);
if(a44)
a51+=barWidth;
a42.setAttribute("y",dy-a50);
a42.setAttribute("width",barWidth);
a42.setAttribute("height",a50);
if(a52)
a42.setAttribute("fill","url(#gradient"+a55+")");
else
a42.setAttribute("fill",a46[a55]);
a42.setAttribute("stroke",a46[a55]);
a42.setAttribute("stroke-width",1);
a42.setAttribute("yValueIndex",a54);
a42.setAttribute("seriesIndex",a55);
if(this._tooltipsVisible)
{
a42.addEventListener("mouseover",this.ShowToolTipCallback,false);
a42.addEventListener("mouseout",this.HideToolTipCallback,false);
}
a42.addEventListener("click",this.ClickCallback,false);
a38.appendChild(a42);
if(!a44)
dy-=a50;
}
if(a44)
dy-=a50;
dy-=a43;
}
}
ApacheHBarChart.prototype._drawPerspectiveBars=function()
{
var a56=this._svgDoc,model=this._model,margins=this._margins;
var a57=this._rootElement,dataElems=this._dataElems,animate=(this._animDuration>0);
var a58=ApacheChart._XOFFSET_PERSPECTIVE,yOffset=ApacheChart._YOFFSET_PERSPECTIVE;
var a59=margins.left,marginTop=margins.top;
var a60=(this._width-a59-margins.right-a58);
var a61=(this._height-marginTop-margins.bottom-yOffset);
var a62=a56.getElementById("barPathPrototype");
var a63=ApacheBarChart._BARITEM_PADDING;
var a64=(this._type==ApacheChart.TYPE_HBAR_STACKED);
var a65=model.getGroupLabels(),groupCount=a65.length,
seriesLabels=model.getSeriesLabels(),seriesCount=seriesLabels.length;
var a66=model.getSeriesColors(),yValues=model.getYValues();
var a67=model.getMinYValue(),maxValue=model.getMaxYValue();
var a68=yValues.length;
var a69,stackBase=a67;
if(a64)
a69=a61/Math.max(a68,groupCount)-2*a63;
else
a69=(a61/Math.max(a68,groupCount)-2*a63-(seriesCount)*a63)/seriesCount;
var a70=a59,dy=a61+marginTop+yOffset,barWidth;
var a71=this._gradientsUsed;
var a72="scale(0.00001,1)";
for(var a73=0;a73<a68;++a73)
{
dy-=a63;
a70=a59;
for(var a74=0;a74<seriesCount;++a74)
{
if(a64)
stackBase=(a74==0?a67:0);
barWidth=a60*(yValues[a73][a74]-stackBase)/(maxValue-a67);
a62=a62.cloneNode(false);
if(animate)
{
dataElems.push(a62);
a62.setAttribute("transform",a72);
}
var a75=new ApacheChartBuffer();
a75.append("M").append(a70).append(",").append(dy);
a75.append("h").append(barWidth);
a75.append("v").append(-a69);
a75.append("h").append(-barWidth);
a75.append("v").append(a69);
a75.append("M").append(a70).append(",").append(dy-a69);
a75.append("l").append(a58).append(",").append(-yOffset);
a75.append("h").append(barWidth);
a75.append("l").append(-a58).append(",").append(yOffset);
a75.append("z");
a75.append("M").append(a70+barWidth).append(",").append(dy);
a75.append("v").append(-a69);
a75.append("l").append(a58).append(",").append(-yOffset);
a75.append("v").append(a69);
a75.append("z");
a62.setAttribute("stroke",a66[a74]);
a62.setAttribute("stroke-width",1);
if(a71)
a62.setAttribute("fill","url(#gradient"+a74+")");
else
a62.setAttribute("fill",a66[a74]);
a62.setAttribute("d",a75.toString());
a62.setAttribute("yValueIndex",a73);
a62.setAttribute("seriesIndex",a74);
if(this._tooltipsVisible)
{
a62.addEventListener("mouseover",this.ShowToolTipCallback,false);
a62.addEventListener("mouseout",this.HideToolTipCallback,false);
}
a62.addEventListener("click",this.ClickCallback,false);
a57.appendChild(a62);
if(a64)
a70+=barWidth;
else
{
dy-=a69;
dy-=a63;
}
}
if(a64)
dy-=a69;
dy-=a63;
}
}
function ApachePieChart(
a0,a1,a2,
a3,a4)
{
this.Init(a0,a1,a2,a3,a4);
}
ApacheChartObj.Inherit(ApacheChart,ApachePieChart);
ApachePieChart.prototype.Init=function(
a5,a6,a7,
a8,a9)
{
ApachePieChart.superclass.Init.call(this,a5,a6,a7,
a8,a9);
}
ApachePieChart.prototype.DrawChartData=function()
{
var a10=this._rootElement;
var a11=this._model,yValues=a11.getYValues(),yValueCount=yValues.length;
var a12=a11.getGroupLabels(),groupCount=a12?a12.length:1;
var a13=Math.ceil(Math.sqrt(yValueCount)),nRows=Math.round(Math.sqrt(yValueCount));
var a14=this._svgDoc.getElementById("groupLabelPrototype");
var a15=this._margins,dx=a15.left,dy=a15.top;
var a16=(this._width-a15.left-a15.right)/a13;
var a17=(this._animDuration>0),isPerspective=this._isPerspective;
var a18,vGap=2*ApacheChart._TEXT_MARGIN;
var a19=(this._height-a15.top-a15.bottom-(nRows-1)*vGap)/nRows;
if(a17)
{
this._pieAnimAngles=[];
a18=this._pieAnimRadii=[];
}
for(var a20=0;a20<nRows;++a20)
{
for(var a21=0;a21<a13;++a21)
{
var a22=a12?(a20*a13+a21):(-1);
if(a22>=yValueCount)
break;
var a23=(a22==-1)?null:a12[a22];
var a24=a10.cloneNode(false);
a10.appendChild(a24);
var a25=this.DrawGroupLabelTitle(a23,a10,
a14.cloneNode(true),dx,dy,
a16,a19);
var a26=a16-2*ApacheChart._TEXT_MARGIN;
var a27=dx+a16/2+ApacheChart._TEXT_MARGIN,cy=dy+a25/2;
if(a17)
{
a18.push(Math.max(a27,cy));
}
if(isPerspective)
{
this._draw3DPies(a24,a26,a25,a22);
a24.setAttribute("transform",
"translate("+a27+","+cy+") scale(1.0,0.707)");
}
else
{
this._drawPies(a24,a26,a25,a22);
a24.setAttribute("transform",
"translate("+a27+","+cy+")");
}
dx+=a16;
}
dx=a15.left;
dy+=a19+vGap;
}
}
ApachePieChart.prototype.ComputeMinMaxValues=function()
{
}
ApachePieChart.prototype.DrawGroupLabels=function()
{
}
ApachePieChart.prototype.LayoutGroupLabels=function()
{
}
ApachePieChart.prototype.DrawGrid=function()
{
}
ApachePieChart.prototype.DrawYValueLabels=function()
{
}
ApachePieChart.prototype.LayoutYValueLabels=function()
{
}
ApachePieChart.prototype.SetDataAnimStep=function(a28)
{
var a29=this._pieAnimRadii,pieAnimAngles=this._pieAnimAngles,
isPerspective=this._isPerspective,agleIndex=0,elemIndex=0;
var a30=this._dataElems,chartCount=a29.length;
var a31=this._model,yValues=a31.getYValues();
for(var a32=0;a32<chartCount;++a32)
{
var a33=yValues[a32].length;
var a34=a29[a32]*(1-a28);
for(var a35=0;a35<a33;++a35)
{
var a36=pieAnimAngles[agleIndex++]*2*Math.PI;
var a37=a34*Math.sin(a36),ty=a34*Math.cos(a36);
if(a36<=Math.PI/2)
{
ty=-ty;
a37=a37;
}
else if(a36<=Math.PI)
{
;
}
else if(a36<=3*Math.PI/2)
{
a37=-a37;
}
else
{
ty=-ty;
a37=-a37;
}
a30[elemIndex++].setAttribute("transform","translate("+a37+","+ty+")");
if(isPerspective)
{
a30[elemIndex++].setAttribute("transform","translate("+a37+","+ty+")");
a30[elemIndex++].setAttribute("transform","translate("+a37+","+ty+")");
}
}
}
}
ApachePieChart.prototype._drawPies=function(
a38,a39,
a40,a41)
{
var a42=this._svgDoc,model=this._model,yValues=model.getYValues();
var a43=model.getGroupLabels(),seriesColors=model.getSeriesColors();
var a44=Math.min(a39/2,a40/2);
if(a41==-1)
a41=0;
var a45=yValues[a41].length;
var a46=0;
for(var a47=0;a47<a45;++a47)
{
a46+=yValues[a41][a47];
}
var a48=a42.getElementById("piePathPrototype"),pieStart=0,animAngleStart=0;
var a49=new Array(a45),dataElems=this._dataElems,animate=(this._animDuration>0);
var a50=this._gradientsUsed;
var a51="translate(-10000, -10000)",pieAnimAngles=this._pieAnimAngles;
for(var a47=0;a47<a45;++a47)
{
a48=a48.cloneNode(false);
var a52=1-(yValues[a41][a47])/(a46);
if(animate)
{
dataElems.push(a48);
a48.setAttribute("transform",a51);
var a53=(yValues[a41][a47])/(a46);
pieAnimAngles.push(animAngleStart+a53/2);
animAngleStart+=a53;
}
var a54=new ApacheChartBuffer();
a54.append("M0,0L");
a54.append(a44*Math.cos(pieStart*Math.PI*2));
a54.append(",").append(a44*Math.sin(pieStart*Math.PI*2));
if(a52>=.5)
{
a54.append("A").append(a44).append(" ").append(a44).append(" 1 0 0 ");
a54.append(a44*Math.cos((pieStart+a52)*Math.PI*2));
a54.append(",").append(a44*Math.sin((pieStart+a52)*Math.PI*2));
}
else
{
a54.append("A").append(a44).append(" ").append(a44).append(" 1 1 0 ");
a54.append(a44*Math.cos((pieStart+a52)*Math.PI*2));
a54.append(",").append(a44*Math.sin((pieStart+a52)*Math.PI*2));
}
a54.append("z");
a48.setAttribute("d",a54.toString());
if(a50)
a48.setAttribute("fill","url(#gradient"+a47+")");
else
a48.setAttribute("fill",seriesColors[a47]);
a48.setAttribute("stroke",seriesColors[a47]);
a48.setAttribute("stroke-width",1);
a48.setAttribute("yValueIndex",a41);
a48.setAttribute("seriesIndex",a47);
if(this._tooltipsVisible)
{
a48.addEventListener("mouseover",this.ShowToolTipCallback,false);
a48.addEventListener("mouseout",this.HideToolTipCallback,false);
}
a48.addEventListener("click",this.ClickCallback,false);
pieStart+=a52;
a49[a47]=a48;
}
for(var a47=0;a47<a45;++a47)
{
a38.appendChild(a49[a47]);
}
}
ApachePieChart.prototype._draw3DPies=function(
a55,a56,
a57,a58)
{
var a59=this._svgDoc,model=this._model,yValues=model.getYValues();
var a60=model.getGroupLabels(),seriesColors=model.getSeriesColors();
var a61=Math.min(a56/2,a57/2);
var a62=0;
if(a58==-1)
a58=0;
var a63=yValues[a58].length;
for(var a64=0;a64<a63;++a64)
{
a62+=yValues[a58][a64];
}
var a65=a61/4,pieElems=new Array(a63),
ringElems=new Array(a63),edgeElems=new Array(a63);
var a66=this._dataElems,animate=(this._animDuration>0);
if(a65>ApachePieChart._MAX_PERSPECTIVE_HEIGHT)
a65=ApachePieChart._MAX_PERSPECTIVE_HEIGHT;
var a67=a59.getElementById("piePathPrototype"),pieStart=0;
var a68=this._gradientsUsed;
var a69="translate(-10000, -10000)",pieAnimAngles=this._pieAnimAngles;
for(var a64=0;a64<a63;++a64)
{
a67=a67.cloneNode(false);
var a70=1-(yValues[a58][a64])/(a62);
if(animate)
{
a66.push(a67);
a67.setAttribute("transform",a69);
pieAnimAngles.push(pieStart+a70/2);
}
var a71,arcBeginY,arcEndX,arcEndY;
a71=a61*Math.cos(pieStart*Math.PI*2);
arcBeginY=a61*Math.sin(pieStart*Math.PI*2);
var a72=new ApacheChartBuffer();
a72.append("M0,0L").append(a71).append(",").append(arcBeginY);
arcEndX=a61*Math.cos((pieStart+a70)*Math.PI*2);
arcEndY=a61*Math.sin((pieStart+a70)*Math.PI*2);
if(a70>=.5)
{
a72.append("A").append(a61).append(" ").append(a61).append(" 1 0 0 ");
a72.append(arcEndX).append(",").append(arcEndY);
}
else
{
a72.append("A").append(a61).append(" ").append(a61).append(" 1 1 0 ");
a72.append(arcEndX).append(",").append(arcEndY);
}
a72.append("z");
if(a68)
a67.setAttribute("fill","url(#gradient"+a64+")");
else
a67.setAttribute("fill",seriesColors[a64]);
a67.setAttribute("stroke",seriesColors[a64]);
a67.setAttribute("stroke-width",1);
a67.setAttribute("yValueIndex",a58);
a67.setAttribute("seriesIndex",a64);
if(this._tooltipsVisible)
{
a67.addEventListener("mouseover",this.ShowToolTipCallback,false);
a67.addEventListener("mouseout",this.HideToolTipCallback,false);
}
a67.addEventListener("click",this.ClickCallback,false);
var a73=a67.cloneNode(false);
var a74=a67.cloneNode(false);
if(animate)
{
a66.push(a73);
a73.setAttribute("transform",a69);
a66.push(a74);
a74.setAttribute("transform",a69);
}
a67.setAttribute("d",a72.toString());
a72=new ApacheChartBuffer();
a72.append("M").append(a71).append(",").append(arcBeginY);
if(a70>=.5)
{
a72.append("A").append(a61).append(" ").append(a61).append(" 1 0 0 ");
a72.append(arcEndX).append(",").append(arcEndY);
}
else
{
a72.append("A").append(a61).append(" ").append(a61).append(" 1 1 0 ");
a72.append(arcEndX).append(",").append(arcEndY);
}
a72.append("v").append(a65);
if(a70>=.5)
{
a72.append("A").append(a61).append(" ").append(a61).append(" 1 0 1 ");
a72.append(a71).append(",").append(arcBeginY+a65);
}
else
{
a72.append("A").append(a61).append(" ").append(a61).append(" 1 1 1 ");
a72.append(a71).append(",").append(arcBeginY+a65);
}
a72.append("z");
a73.setAttribute("d",a72.toString());
a72=new ApacheChartBuffer();
a72.append("M0,0L");
a72.append(a71).append(",").append(arcBeginY);
a72.append("v").append(a65);
a72.append("L").append(0).append(",").append(a65);
a72.append("z");
a72.append("M0,0L");
a72.append(arcEndX).append(",").append(arcEndY);
a72.append("v").append(a65);
a72.append("L").append(0).append(",").append(a65);
a72.append("z");
a74.setAttribute("d",a72.toString());
pieStart+=a70;
pieElems[a64]=a67;
ringElems[a64]=a73;
edgeElems[a64]=a74;
}
var a75=0;
for(var a64=0;a64<a63;++a64)
{
if(a75<=.5)
a55.appendChild(ringElems[a64]);
a75+=(yValues[a58][a64])/(a62);
}
a75=0;
for(var a64=0;a64<a63;++a64)
{
if(a75<=.5)
a55.appendChild(edgeElems[a64]);
a75+=(yValues[a58][a64])/(a62);
}
a75=0;
for(var a64=0;a64<a63;++a64)
{
if(a75>.5)
a55.appendChild(edgeElems[a64]);
a75+=(yValues[a58][a64])/(a62);
}
a75=0;
for(var a64=0;a64<a63;++a64)
{
if(a75>.5)
a55.appendChild(ringElems[a64]);
a75+=(yValues[a58][a64])/(a62);
}
for(var a64=0;a64<a63;++a64)
{
a55.appendChild(pieElems[a64]);
}
}
ApachePieChart.prototype.GetToolTipLocation=function(a76,a77)
{
var a78=a76.target;
var a79=a78.getBBox();
var a80=a78.parentNode.getCTM();
return{x:(a80.e+a79.x+a79.width/2),
y:(a80.f+a79.y+a79.height/2-a77.height)};
}
ApachePieChart._MAX_PERSPECTIVE_HEIGHT=30;
function ApacheAreaChart(
a0,a1,a2,
a3,a4)
{
this.Init(a0,a1,a2,a3,a4);
}
ApacheChartObj.Inherit(ApacheChart,ApacheAreaChart);
ApacheAreaChart.prototype.Init=function(
a5,a6,a7,
a8,a9)
{
ApacheAreaChart.superclass.Init.call(this,a5,a6,a7,
a8,a9);
this._toolTips=[];
}
ApacheAreaChart.prototype.SetStopOpacity=function(a10)
{
a10.setAttribute("stop-opacity",ApacheChart._DEFAULT_STOP_OPACITY/2);
}
ApacheAreaChart.prototype.DrawChartData=function()
{
var a11=this._rootElement;
if(this._tooltipsVisible)
{
a11.addEventListener("mousemove",this.ShowToolTipCallback,false);
a11.addEventListener("mouseout",this.HideToolTipCallback,false);
}
if(this._isPerspective)
this._drawPerspectiveAreas();
else
this._drawAreas();
}
ApacheAreaChart.prototype.SetDataAnimStep=function(a12)
{
var a13=this._model,
seriesLabels=a13.getSeriesLabels(),seriesCount=seriesLabels.length;
var a14=a13.getYValues(),yValueCount=a14.length;
var a15=this._dataElems,animPathCount=(this._isPerspective)?(yValueCount-1):1;
var a16=this._margins,marginBottom=a16.bottom;
var a17=(this._height-marginBottom);
var a18=a12*seriesCount,animSeriesIndex=0;
if(a18>1)
{
animSeriesIndex=Math.floor(a18);
if(animSeriesIndex>=seriesCount)
animSeriesIndex=seriesCount-1;
a18=a18-Math.floor(a18);
}
var a19=animSeriesIndex;
for(var a20=0;a20<animPathCount;++a20)
{
var a21=(1-a18)*a17;
a15[a19*animPathCount+a20].setAttribute("transform","translate(0,"+a21+") scale(1,"+a18+")");
if(a19>0)
{
a15[(a19-1)*animPathCount+a20].setAttribute("transform","scale(1,1)");
}
}
if(a12==1)
{
for(var a19=0;a19<seriesCount;++a19)
{
for(var a20=0;a20<animPathCount;++a20)
{
a15[a19*animPathCount+a20].setAttribute("transform","scale(1,1)");
}
}
}
}
ApacheAreaChart.prototype.IsGroupLabelCentered=function()
{
return false;
}
ApacheAreaChart.prototype.GetVLineCount=function()
{
var a22=this._xMajorGridCount;
if(a22>=0)
return a22;
else
{
return this._model.getGroupLabels().length-1;
}
}
ApacheAreaChart.prototype._drawAreas=function()
{
var a23=this._svgDoc,model=this._model,margins=this._margins;
var a24=this._rootElement,dataElems=this._dataElems,animate=(this._animDuration>0);
var a25=margins.left,marginTop=margins.top;
var a26=(this._width-a25-margins.right);
var a27=(this._height-marginTop-margins.bottom);
var a28=a23.getElementById("areaPathPrototype");
var a29=model.getGroupLabels(),groupCount=a29.length,
seriesLabels=model.getSeriesLabels(),seriesCount=seriesLabels.length;
var a30=model.getSeriesColors(),yValues=model.getYValues();
var a31=model.getMinYValue(),maxValue=model.getMaxYValue();
var a32=(this._type==ApacheChart.TYPE_AREA_STACKED);
var a33=yValues.length;
var a34=(a26/(Math.max(a33,groupCount)-1));
var a35=this._gradientsUsed;
var a36="scale(1,0.00001)";
var a37,dy,cumYs=[],stackBase;
for(var a38=0;a38<seriesCount;++a38)
{
a37=a25;
dy=marginTop+a27;
var a39=new ApacheChartBuffer();
a28=a28.cloneNode(false);
if(a38==0||!a32)
a39.append("M").append(a37).append(",").append(dy);
else if(a32)
a39.append("M").append(a37).append(",").append(cumYs[0]);
stackBase=(a38==0?a31:0);
for(var a40=0;a40<a33;++a40)
{
if(a32)
{
if(null==cumYs[a40])
cumYs[a40]=a27+marginTop;
dy=(cumYs[a40]-=a27*(yValues[a40][a38]-stackBase)/(maxValue-a31));
}
else
dy=a27+marginTop-a27*(yValues[a40][a38]-a31)/(maxValue-a31);
a39.append("L").append(a37).append(",").append(dy);
if(a40!=a33-1)
a37+=a34;
}
if(animate)
{
dataElems.push(a28);
a28.setAttribute("transform",a36);
}
if(a35)
a28.setAttribute("fill","url(#gradient"+a38+")");
else
a28.setAttribute("fill",a30[a38]);
a28.setAttribute("stroke",a30[a38]);
a28.setAttribute("stroke-width",1);
a28.setAttribute("seriesIndex",a38);
a28.addEventListener("click",this.ClickCallback,false);
if(a38==0||!a32)
{
a39.append("L").append(a37).append(",").append(a27+marginTop);
a39.append("Z");
}
else
{
for(var a40=a33-1;a40>=0;--a40)
{
var a41=cumYs[a40]+a27*(yValues[a40][a38]-stackBase)/(maxValue-a31);
a39.append("L").append(a37).append(",").append(a41);
a37-=a34;
}
}
a28.setAttribute("d",a39.toString());
a24.appendChild(a28);
}
}
ApacheAreaChart.prototype._drawPerspectiveAreas=function()
{
var a42=this._svgDoc,model=this._model,margins=this._margins;
var a43=this._rootElement,dataElems=this._dataElems,animate=(this._animDuration>0);
var a44=ApacheChart._XOFFSET_PERSPECTIVE,yOffset=ApacheChart._YOFFSET_PERSPECTIVE;
var a45=margins.left,marginTop=margins.top;
var a46=(this._width-a45-margins.right-a44);
var a47=(this._height-marginTop-margins.bottom-yOffset);
var a48=a42.getElementById("areaPathPrototype");
var a49=model.getGroupLabels(),groupCount=a49.length,
seriesLabels=model.getSeriesLabels(),seriesCount=seriesLabels.length;
var a50=model.getSeriesColors(),yValues=model.getYValues();
var a51=(this._type==ApacheChart.TYPE_AREA_STACKED);
var a52=model.getMinYValue(),maxValue=model.getMaxYValue();
var a53=yValues.length;
var a54=(a46/(Math.max(a53,groupCount)-1)),stackBase;
var a55=a47+marginTop+yOffset,dx,dy,cumYs=[];
var a56=this._gradientsUsed;
var a57="scale(1,0.00001)";
for(var a58=0;a58<seriesCount;++a58)
{
dx=a45;
var a59=new ApacheChartBuffer();
a48=a48.cloneNode(false);
stackBase=(a58==0?a52:0);
for(var a60=0;a60<a53;++a60)
{
if(a51)
{
if(null==cumYs[a60])
cumYs[a60]=a55;
dy=(cumYs[a60]-=a47*(yValues[a60][a58]-stackBase)/(maxValue-a52));
}
else
dy=a55-a47*(yValues[a60][a58]-a52)/(maxValue-a52);
if(a60!=a53-1)
{
a48=a48.cloneNode(false);
a59.append("M").append(dx).append(",").append(dy);
a59.append("l").append(a44).append(",").append(-yOffset);
var a61,nextdx=dx+a54;
if(a51)
{
if(null==cumYs[a60+1])
cumYs[a60+1]=a55;
a61=(cumYs[a60+1]-a47*(yValues[a60+1][a58]-stackBase)/(maxValue-a52));
}
else
a61=a55-a47*(yValues[a60+1][a58]-a52)/(maxValue-a52)
a59.append("L").append(nextdx+a44).append(",").append(a61-yOffset);
a59.append("l").append(-a44).append(",").append(yOffset);
a59.append("L").append(dx).append(",").append(dy);
a59.append("M").append(nextdx).append(",").append(a61);
a59.append("l").append(a44).append(",").append(-yOffset);
var a62,prevSeriesY2;
if(a58==0||!a51)
{
a59.append("L").append(nextdx+a44).append(",").append(a47+marginTop);
}
else
{
a59.append("L").append(nextdx+a44).append(",").append(cumYs[a60+1]-yOffset);
}
a59.append("l").append(-a44).append(",").append(yOffset);
a59.append("L").append(nextdx).append(",").append(a61);
a59.append("M").append(dx).append(",").append(dy);
a59.append("L").append(nextdx).append(",").append(a61);
if(a58==0||!a51)
{
a59.append("L").append(nextdx).append(",").append(a55);
a59.append("L").append(dx).append(",").append(a55);
}
else
{
a59.append("L").append(nextdx).append(",").append(cumYs[a60+1]);
a59.append("L").append(dx).append(",").append(
cumYs[a60]+a47*(yValues[a60][a58]-stackBase)/(maxValue-a52));
}
a59.append("L").append(dx).append(",").append(dy);
if(a56)
a48.setAttribute("fill","url(#gradient"+a58+")");
else
a48.setAttribute("fill",a50[a58]);
a48.setAttribute("stroke",a50[a58]);
a48.setAttribute("stroke-width",1);
a48.setAttribute("yValueIndex",a60);
a48.setAttribute("seriesIndex",a58);
a48.addEventListener("click",this.ClickCallback,false);
dx+=a54;
a48.setAttribute("d",a59.toString());
a43.appendChild(a48);
if(animate)
{
dataElems.push(a48);
a48.setAttribute("transform",a57);
}
}
}
}
}
ApacheAreaChart.prototype.GetChartEvent=function(a63,a64)
{
var a65=a63.clientX,clientY=a63.clientY,evtTarget=a63.target;
var a66=(this._type==ApacheChart.TYPE_AREA_STACKED);
var a67=this._isPerspective;
var a68=ApacheChart._XOFFSET_PERSPECTIVE,yOffset=ApacheChart._YOFFSET_PERSPECTIVE;
var a69=this._model,yValues=a69.getYValues();
var a70=a69.getGroupLabels(),groupCount=a70.length;
var a71=a69.getMinYValue(),maxValue=a69.getMaxYValue();
var a72=this._margins,marginLeft=a72.left,marginTop=a72.top;
var a73=(this._width-marginLeft-a72.right);
var a74=(this._height-marginTop-a72.bottom);
var a75=yValues.length;
var a76=(a73/(Math.max(a75,groupCount)-1));
if(a67)
{
a73-=a68;
a74-=yOffset;
}
if(a65<marginLeft||
a65>(marginLeft+a73+(a67?a68:0))||
clientY<marginTop||
clientY>(marginTop+a74+(a67?yOffset:0)))
{
return null;
}
var a77,dy,dy1,cumYs=[],seriesIndices=[],seriesValues=[];
var a78=a74+marginTop+(a67?yOffset:0);
var a79=a69.getSeriesLabels().length,stackBase,insideStacked=false;
if(!a64)
a64=[];
for(var a80=0;a80<a79&&!insideStacked;++a80)
{
a77=marginLeft;
stackBase=(a80==0?a71:0);
for(var a81=0;a81<a75;++a81)
{
if(a66)
{
if(null==cumYs[a81])
cumYs[a81]=a78;
if(null==cumYs[a81+1]&&(a81!=a75-1))
cumYs[a81+1]=a78;
cumYs[a81]-=a74*(yValues[a81][a80]-stackBase)/(maxValue-a71);
}
if(a81==a75-1)
continue;
if(a65>a77&&a65<(a77+a76))
{
if(a66)
{
dy1=cumYs[a81];
dy2=(cumYs[a81+1]-a74*(yValues[a81+1][a80]-stackBase)/(maxValue-a71));
dy=dy1-(dy1-dy2)*(a65-a77)/a76;
if(clientY>=dy)
{
value=yValues[a81][a80]+(yValues[a81+1][a80]-yValues[a81][a80])*(a65-a77)/a76;
seriesValues.push(value);
seriesIndices.push(a80);
a64.push(dy);
insideStacked=true;
break;
}
}
else
{
dy1=a78-
a74*(yValues[a81][a80]-a71)/(maxValue-a71);
dy=dy1-(a74*(yValues[a81+1][a80]-yValues[a81][a80])/(maxValue-a71))*(a65-a77)/a76;
if(dy<=clientY)
{
value=yValues[a81][a80]+(yValues[a81+1][a80]-yValues[a81][a80])*(a65-a77)/a76;
seriesValues.push(value);
seriesIndices.push(a80);
a64.push(dy);
}
break;
}
}
a77+=a76;
}
}
return new ApacheChartEvent(seriesIndices,null,seriesValues,null);
}
ApacheAreaChart.prototype.ShowToolTip=function(a82)
{
this.HideToolTip();
var a83=[];
var a84=this.GetChartEvent(a82,a83);
if(a84==null||a84.getYValues().length==0)
{
return;
}
this._displayToolTips(a84.getYValues(),a84.getSeriesIndices(),a83,a82);
}
ApacheAreaChart.prototype._displayToolTips=function(
a85,a86,
a87,a88)
{
var a89=this._svgDoc,rootElem=this._rootElement;
var a90=this._model,seriesLabels=a90.getSeriesLabels(),seriesCount=seriesLabels.length,
seriesColors=a90.getSeriesColors();
var a91=a86.length,toolTips=this._toolTips;
var a92=a88.clientX;
var a93,dy;
for(var a94=0;a94<a91;++a94)
{
var a95=a86[a94];
var a96=toolTips[a95];
var a97=false;
if(a96==null)
{
a96=a89.getElementById("toolTip").cloneNode(true);
rootElem.appendChild(a96);
toolTips[a95]=a96;
a97=true;
}
a96.style.setProperty("visibility","visible","");
var a98=a96.firstChild.nextSibling;
var a99=a98.nextSibling.nextSibling;
var a100=a99.nextSibling.nextSibling;
var a101=a85.length;
var a102=a100.getComputedTextLength();
a100.firstChild.data=
seriesLabels[a95]+":  "+this._formatValue(a85[a94]);
if(a97)
{
var a103=parseInt(a99.getAttribute("height"));
var a104=parseInt(a100.getAttribute("dy"));
a103-=a104;
a99.setAttribute("height",a103);
a100=a100.nextSibling.nextSibling;
a100.firstChild.data="";
}
a102+=2*ApacheChart._TEXT_MARGIN;
a99.setAttribute("width",a102);
var a105=a88.target.getBBox();
var a106=a96.getBBox();
a93=a92,a104=a87[a94]-a106.height;
if(a93+a106.width>this._width)
{
a93-=a106.width;
a98.setAttribute("cx",a99.getBBox().width);
}
else
{
a98.setAttribute("cx",0);
}
if(a104-a106.height<0)
{
a104+=a106.height;
a98.setAttribute("cy",0);
}
else
{
a98.setAttribute("cy",a99.getBBox().height);
}
a99.setAttribute("stroke",seriesColors[a95]);
a98.setAttribute("stroke",seriesColors[a95]);
a96.setAttribute("transform","translate("+a93+","+a104+")");
}
}
ApacheAreaChart.prototype.HideToolTip=function(a107)
{
var a108=this._toolTips,tooltipCount=a108.length;
for(var a109=0;a109<tooltipCount;++a109)
{
var a110=a108[a109];
if(a110)
a110.style.setProperty("visibility","hidden","");
}
}
function ApacheLineChart(
a0,a1,a2,
a3,a4)
{
this.Init(a0,a1,a2,a3,a4);
}
ApacheChartObj.Inherit(ApacheChart,ApacheLineChart);
ApacheLineChart.prototype.DrawChartData=function(a5)
{
if(this._isPerspective)
this.__drawPerspectiveLines(a5);
else
this.__drawLines(a5);
}
ApacheLineChart.prototype.AnimAlongXAxis=function()
{
return true;
}
ApacheLineChart.prototype.__drawLines=function(a6)
{
var a7=this._svgDoc,model=this._model,margins=this._margins;
var a8=this._rootElement,dataElems=this._dataElems,
dotElems,animate=(this._animDuration>0);
var a9=margins.left,marginTop=margins.top;
var a10=(this._width-a9-margins.right);
var a11=(this._height-marginTop-margins.bottom);
var a12=a7.getElementById("lineDotPrototype"),
pathElem=a7.getElementById("linePathPrototype");
var a13=model.getGroupLabels(),groupCount=a13.length,
seriesLabels=model.getSeriesLabels(),seriesCount=seriesLabels.length;
var a14=model.getSeriesColors(),yValues=model.getYValues();
var a15=model.getMinYValue(),maxValue=model.getMaxYValue();
var a16=yValues.length;
var a17=a10/Math.max(a16,groupCount);
var a18,dy;
var a19=this._gradientsUsed;
var a20=this._isIE?"scale(0.00001,1)":"scale(0,1)";
for(var a21=0;a21<seriesCount;++a21)
{
if(a6&&a21%2==0)
continue;
a18=a9+a17/2;
var a22=new ApacheChartBuffer();
pathElem=pathElem.cloneNode(false);
for(var a23=0;a23<a16;++a23)
{
dy=a11+marginTop-a11*(yValues[a23][a21]-a15)/(maxValue-a15);
if(a23==0)
a22.append("M").append(a18).append(",").append(dy);
else
a22.append("L").append(a18).append(",").append(dy);
a12=a12.cloneNode(false);
a12.setAttribute("cx",a18);
a12.setAttribute("cy",dy);
if(a19)
a12.setAttribute("fill","url(#gradient"+a21+")");
else
a12.setAttribute("fill",a14[a21]);
a12.setAttribute("stroke",a14[a21]);
if(animate)
{
a12.setAttribute("transform",a20);
dataElems.push(a12);
}
a8.appendChild(a12);
pathElem.setAttribute("stroke",a14[a21]);
pathElem.setAttribute("seriesIndex",a21);
if(this._tooltipsVisible)
{
pathElem.addEventListener("mouseover",this.ShowToolTipCallback,false);
pathElem.addEventListener("mouseout",this.HideToolTipCallback,false);
}
pathElem.addEventListener("click",this.ClickCallback,false);
a18+=a17;
}
pathElem.setAttribute("d",a22.toString());
a8.appendChild(pathElem);
if(animate)
{
dataElems.push(pathElem);
pathElem.setAttribute("transform",a20);
}
}
}
ApacheLineChart.prototype.__drawPerspectiveLines=function(a24)
{
var a25=this._svgDoc,model=this._model,margins=this._margins;
var a26=this._rootElement,dataElems=this._dataElems,animate=(this._animDuration>0);
var a27=ApacheChart._XOFFSET_PERSPECTIVE,yOffset=ApacheChart._YOFFSET_PERSPECTIVE;
var a28=margins.left,marginTop=margins.top;
var a29=(this._width-a28-margins.right-a27);
var a30=(this._height-marginTop-margins.bottom-yOffset);
var a31=a25.getElementById("linePath3dPrototype");
var a32=model.getGroupLabels(),groupCount=a32.length,
seriesLabels=model.getSeriesLabels(),seriesCount=seriesLabels.length;
var a33=model.getSeriesColors(),yValues=model.getYValues();
var a34=model.getMinYValue(),maxValue=model.getMaxYValue();
var a35=yValues.length;
var a36=(a29/Math.max(a35,groupCount));
var a37=a30+marginTop+yOffset,dx,dy;
var a38=this._gradientsUsed;
var a39="scale(0.00001,1)";
for(var a40=0;a40<seriesCount;++a40)
{
if(a24&&a40%2==0)
continue;
dx=a28+a36/2;
var a41=new ApacheChartBuffer();
a31=a31.cloneNode(false);
for(var a42=0;a42<a35;++a42)
{
dy=a37-a30*(yValues[a42][a40]-a34)/(maxValue-a34);
if(a42!=a35-1)
{
var a41=new ApacheChartBuffer();
a31=a31.cloneNode(false);
a41.append("M").append(dx).append(",").append(dy);
a41.append("l").append(a27).append(",").append(-yOffset);
var a43=a37-a30*(yValues[a42+1][a40]-a34)/(maxValue-a34);
var a44=dx+a36;
a41.append("L").append(a44+a27).append(",").append(a43-yOffset);
a41.append("l").append(-a27).append(",").append(yOffset);
a41.append("L").append(dx).append(",").append(dy);
if(a38)
a31.setAttribute("fill","url(#gradient"+a40+")");
else
a31.setAttribute("fill",a33[a40]);
a31.setAttribute("stroke",a33[a40]);
a31.setAttribute("seriesIndex",a40);
if(this._tooltipsVisible)
{
a31.addEventListener("mousemove",this.ShowToolTipCallback,false);
a31.addEventListener("mouseout",this.HideToolTipCallback,false);
}
a31.addEventListener("click",this.ClickCallback,false);
dx+=a36;
a31.setAttribute("d",a41.toString());
a26.appendChild(a31);
if(animate)
{
dataElems.push(a31);
a31.setAttribute("transform",a39);
}
}
}
}
}
ApacheLineChart.prototype.ShowToolTip=function(a45)
{
this.HideToolTip();
ApacheLineChart.superclass.ShowToolTip.call(this,a45);
}
ApacheLineChart.prototype.GetToolTipLocation=function(a46,a47)
{
return{x:(a46.clientX+20),y:(a46.clientY+20)};
}
ApacheLineChart.prototype.GetChartEvent=function(a48)
{
var a49=a48.target;
var a50=parseInt(a49.getAttribute("seriesIndex"));
var a51=a48.clientX;
var a52=this._isPerspective;
var a53=ApacheChart._YOFFSET_PERSPECTIVE;
var a54=this._model,yValues=a54.getYValues();
var a55=a54.getGroupLabels(),groupCount=a55.length;
var a56=this._margins,marginLeft=a56.left,marginTop=a56.top;
var a57=(this._width-marginLeft-a56.right);
var a58=(this._height-marginTop-a56.bottom);
var a59=yValues.length;
var a60=(a57/Math.max(a59,groupCount));
var a61=a58+marginTop+(a52?a53:0);
var a62=marginLeft+a60/2,value=0.0;
for(var a63=0;a63<a59;++a63)
{
if(a63==a59-1)
continue;
if(a51>a62&&a51<(a62+a60))
{
value=yValues[a63][a50]+(yValues[a63+1][a50]-yValues[a63][a50])*(a51-a62)/a60;
break;
}
a62+=a60;
}
return new ApacheChartEvent([a50],null,[value],null);
}
ApacheLineChart.prototype.FillToolTipData=function(a64,a65,a66)
{
var a67=this.GetChartEvent(a66);
var a68=a67.getSeriesIndices()[0],value=a67.getYValues()[0];
var a69=this._model.getSeriesLabels();
var a70=a64.nextSibling.nextSibling;
a70.firstChild.data=a69[a68]+
": "+this._formatValue(value);
var a71=a70.getComputedTextLength();
a70=a70.nextSibling.nextSibling;
a70.firstChild.data="";
var a72=a71+2*ApacheChart._TEXT_MARGIN;
a64.setAttribute("width",a72);
a64.setAttribute("stroke",seriesColors[a68]);
a65.setAttribute("r",0);
}
function ApacheScatterPlotChart(
a0,a1,a2,
a3,a4)
{
this.Init(a0,a1,a2,a3,a4);
}
ApacheChartObj.Inherit(ApacheChart,ApacheScatterPlotChart);
ApacheScatterPlotChart.prototype.Init=function(
a5,a6,a7,
a8,a9)
{
ApacheScatterPlotChart.superclass.Init.call(this,a5,a6,a7,
a8,a9);
}
ApacheScatterPlotChart.prototype.DrawChartData=function()
{
if(this._isPerspective)
this._drawPerspectivePoints();
else
this._drawPoints();
}
ApacheScatterPlotChart.prototype.SetDataAnimStep=function(a10)
{
var a11=this._isPerspective;
var a12=this._cxs,cys=this._cys,gridCx,gridCy;
var a13=this._margins,marginLeft=a13.left,marginTop=a13.top;
var a14=(this._width-marginLeft-a13.right);
var a15=(this._height-marginTop-a13.bottom);
var a16=this._dataElems,animCount=a12.length,elemIndex=0;
if(a11)
marginLeft+=ApacheChart._XOFFSET_PERSPECTIVE;
gridCx=a14/2+marginLeft;
gridCy=a15/2+marginTop;
for(var a17=0;a17<animCount;++a17)
{
var a18=gridCx-(gridCx-a12[a17])*a10;
var a19=gridCy-(gridCy-cys[a17])*a10;
var a20=a16[elemIndex++];
a20.setAttribute("cx",a18);
a20.setAttribute("cy",a19);
if(a11)
{
a20=a16[elemIndex++];
a20.setAttribute("cx",a18);
a20.setAttribute("cy",a19);
}
}
}
ApacheScatterPlotChart.prototype.SetGridAnimStep=function(a21)
{
var a22=this._gridElems,animCount=a22.length;
for(var a23=0;a23<animCount;++a23)
{
a22[a23].setAttribute("fill-opacity",a21);
a22[a23].setAttribute("transform","scale(1,1)");
}
}
ApacheScatterPlotChart.prototype.IsGroupLabelCentered=function()
{
return false;
}
ApacheScatterPlotChart.prototype.GetVLineCount=function()
{
var a24=this._xMajorGridCount;
if(a24>=0)
return a24;
else
{
return this._model.getGroupLabels().length-1;
}
}
ApacheScatterPlotChart.prototype._drawPoints=function()
{
var a25=this._svgDoc,model=this._model,margins=this._margins;
var a26=this._rootElement,dataElems=this._dataElems,animate=(this._animDuration>0);
var a27=margins.left,marginTop=margins.top;
var a28=(this._width-a27-margins.right);
var a29=(this._height-marginTop-margins.bottom);
var a30=a25.getElementById("scatterDotPrototype");
var a31=model.getGroupLabels(),groupCount=a31.length,
seriesLabels=model.getSeriesLabels(),seriesCount=seriesLabels.length;
var a32=model.getSeriesColors(),xValues=model.getXValues(),
yValues=model.getYValues(),nValues=yValues.length;
var a33="translate(0,100000)";
var a34=model.getMinYValue(),maxYValue=model.getMaxYValue();
var a35=model.getMinXValue(),maxXValue=model.getMaxXValue();
var a36=(a28/(groupCount-1));
var a37,cys,dx,dy,gridCx,gridCY;
var a38=this._gradientsUsed;
if(animate)
{
a37=this._cxs=[];
cys=this._cys=[];
gridCx=a28/2+a27;
gridCy=a29/2+marginTop;
}
for(var a39=0;a39<seriesCount;++a39)
{
for(var a40=0;a40<nValues;++a40)
{
dy=a29+marginTop-a29*(yValues[a40][a39]-a34)/(maxYValue-a34);
dx=a27+a28*(xValues[a40][a39]-a35)/(maxXValue-a35);
a30=a30.cloneNode(false);
if(a38)
a30.setAttribute("fill","url(#gradient"+a39+")");
else
a30.setAttribute("fill",a32[a39]);
a30.setAttribute("stroke",a32[a39]);
a30.setAttribute("yValueIndex",a40);
a30.setAttribute("seriesIndex",a39);
if(this._tooltipsVisible)
{
a30.addEventListener("mouseover",this.ShowToolTipCallback,false);
a30.addEventListener("mouseout",this.HideToolTipCallback,false);
}
a30.addEventListener("click",this.ClickCallback,false);
if(animate)
{
dataElems.push(a30);
a30.setAttribute("cx",gridCx);
a30.setAttribute("cy",gridCy);
a37.push(dx);
cys.push(dy);
}
else
{
a30.setAttribute("cx",dx);
a30.setAttribute("cy",dy);
}
a26.appendChild(a30);
}
}
}
ApacheScatterPlotChart.prototype._drawPerspectivePoints=function()
{
var a41=this._svgDoc,model=this._model,margins=this._margins;
var a42=this._rootElement,dataElems=this._dataElems,animate=(this._animDuration>0);
var a43=ApacheChart._XOFFSET_PERSPECTIVE,yOffset=ApacheChart._YOFFSET_PERSPECTIVE;
var a44=margins.left,marginTop=margins.top;
var a45=(this._width-a44-margins.right-a43);
var a46=(this._height-marginTop-margins.bottom-yOffset);
var a47=a41.getElementById("scatter3dDotPrototype");
var a48=model.getGroupLabels(),groupCount=a48.length,
seriesLabels=model.getSeriesLabels(),seriesCount=seriesLabels.length;
var a49=model.getSeriesColors(),xValues=model.getXValues(),
yValues=model.getYValues(),nValues=yValues.length;
var a50=model.getMinYValue(),maxYValue=model.getMaxYValue();
var a51=model.getMinXValue(),maxXValue=model.getMaxXValue();
var a52=(a45/(groupCount-1));
var a53=a46+marginTop+yOffset,cxs,cys,dx,dy,gridCx,gridCY;
var a54=this._gradientsUsed;
if(animate)
{
cxs=this._cxs=[];
cys=this._cys=[];
gridCx=a45/2+a44+a43;
gridCy=a46/2+marginTop;
}
for(var a55=0;a55<seriesCount;++a55)
{
for(var a56=0;a56<nValues;++a56)
{
dy=a53-a46*(yValues[a56][a55]-a50)/(maxYValue-a50);
dx=a44+a45*(xValues[a56][a55]-a51)/(maxXValue-a51);
a47=a47.cloneNode(false);
if(animate)
{
dataElems.push(a47);
a47.setAttribute("cx",gridCx);
a47.setAttribute("cy",gridCy);
cxs.push(dx);
cys.push(dy);
}
else
{
a47.setAttribute("cx",dx);
a47.setAttribute("cy",dy);
}
if(a54)
a47.setAttribute("fill","url(#gradient"+a55+")");
else
a47.setAttribute("fill",a49[a55]);
a47.setAttribute("stroke",a49[a55]);
a47.setAttribute("yValueIndex",a56);
a47.setAttribute("seriesIndex",a55);
if(this._tooltipsVisible)
{
a47.addEventListener("mouseover",this.ShowToolTipCallback,false);
a47.addEventListener("mouseout",this.HideToolTipCallback,false);
}
a47.addEventListener("click",this.ClickCallback,false);
var a57=a47.cloneNode(false);
if(animate)
{
dataElems.push(a57);
}
a57.setAttribute("fill","#333333");
a57.setAttribute("opacity","0.5");
a57.setAttribute("stroke","none");
a57.setAttribute("transform","translate(3,3)");
a42.appendChild(a57);
a42.appendChild(a47);
}
}
}
ApacheScatterPlotChart.prototype.GetChartEvent=function(a58)
{
var a59=a58.target;
var a60=parseInt(a59.getAttribute("seriesIndex")),
j=parseInt(a59.getAttribute("yValueIndex"));
var a61=this._model,xValues=a61.getXValues(),
yValues=a61.getYValues();
return new ApacheChartEvent([a60],[j],[yValues[j][a60]],[xValues[j][a60]]);
}
ApacheScatterPlotChart.prototype.FillToolTipData=function(a62,a63,a64)
{
var a65=this.GetChartEvent(a64);
var a66=a65.getSeriesIndices()[0],
yValue=a65.getYValues()[0]
xValue=a65.getXValues()[0];
var a67=this._model,seriesLabels=a67.getSeriesLabels();
var a68=a62.nextSibling.nextSibling;
a68.firstChild.data=seriesLabels[a66]+
": ("+this._formatValue(xValue)+
")    ("+this._formatValue(yValue)+")";
var a69=a68.getComputedTextLength();
a68=a68.nextSibling.nextSibling;
a68.firstChild.data="";
var a70=a69+2*ApacheChart._TEXT_MARGIN;
a62.setAttribute("width",a70);
a62.setAttribute("stroke",seriesColors[a66]);
a63.setAttribute("r",0);
}
function ApacheXYLineChart(
a0,a1,a2,
a3,a4)
{
this.Init(a0,a1,a2,a3,a4);
}
ApacheChartObj.Inherit(ApacheScatterPlotChart,ApacheXYLineChart);
ApacheXYLineChart.prototype.SetDataAnimStep=function(a5)
{
ApacheChart.prototype.SetDataAnimStep.call(this,a5);
}
ApacheXYLineChart.prototype.DrawChartData=function()
{
if(this._isPerspective)
this._drawPerspectiveXYValues();
else
this._drawXYValues();
}
ApacheXYLineChart.prototype.AnimAlongXAxis=function()
{
return true;
}
ApacheXYLineChart.prototype._drawXYValues=function()
{
var a6=this._svgDoc,model=this._model,margins=this._margins;
var a7=this._rootElement,dataElems=this._dataElems,animate=(this._animDuration>0);
var a8=margins.left,marginTop=margins.top;
var a9=(this._width-a8-margins.right);
var a10=(this._height-marginTop-margins.bottom);
var a11,pathElem;
var a12=model.getGroupLabels(),groupCount=a12.length,
seriesLabels=model.getSeriesLabels(),seriesCount=seriesLabels.length;
var a13=model.getSeriesColors(),xValues=model.getXValues(),
yValues=model.getYValues(),nValues=yValues.length;
var a14="scale(0.00001,1)";
var a15=model.getMinYValue(),maxYValue=model.getMaxYValue();
var a16=model.getMinXValue(),maxXValue=model.getMaxXValue();
var a17=(a9/(groupCount-1));
var a18,dy;
pathElem=a6.getElementById("linePathPrototype");
for(var a19=0;a19<seriesCount;++a19)
{
var a20=new ApacheChartBuffer();
a18=a8;
dy=a10+marginTop;
pathElem=pathElem.cloneNode(false);
if(animate)
{
dataElems.push(pathElem);
pathElem.setAttribute("transform",a14);
}
for(var a21=0;a21<nValues;++a21)
{
dy=a10+marginTop-a10*(yValues[a21][a19]-a15)/(maxYValue-a15);
a18=a8+a9*(xValues[a21][a19]-a16)/(maxXValue-a16);
if(a21==0)
a20.append("M").append(a18).append(",").append(dy);
else
a20.append("L").append(a18).append(",").append(dy);
}
pathElem.setAttribute("seriesIndex",a19);
pathElem.setAttribute("stroke",a13[a19]);
if(this._tooltipsVisible)
{
pathElem.addEventListener("mousemove",this.ShowToolTipCallback,false);
pathElem.addEventListener("mouseout",this.HideToolTipCallback,false);
}
pathElem.addEventListener("click",this.ClickCallback,false);
pathElem.setAttribute("d",a20.toString());
a7.appendChild(pathElem);
}
}
ApacheXYLineChart.prototype._drawPerspectiveXYValues=function()
{
var a22=this._svgDoc,model=this._model,margins=this._margins;
var a23=this._rootElement,dataElems=this._dataElems,animate=(this._animDuration>0);
var a24=ApacheChart._XOFFSET_PERSPECTIVE,yOffset=ApacheChart._YOFFSET_PERSPECTIVE;
var a25=margins.left,marginTop=margins.top;
var a26=(this._width-a25-margins.right-a24);
var a27=(this._height-marginTop-margins.bottom-yOffset);
var a28,pathElem;
var a29=model.getGroupLabels(),groupCount=a29.length,
seriesLabels=model.getSeriesLabels(),seriesCount=seriesLabels.length;
var a30=model.getSeriesColors(),xValues=model.getXValues(),
yValues=model.getYValues(),nValues=yValues.length;
var a31="scale(0.00001,1)";
var a32=model.getMinYValue(),maxYValue=model.getMaxYValue();
var a33=model.getMinXValue(),maxXValue=model.getMaxXValue();
var a34=a27+marginTop+yOffset,dx,dy;
var a35=this._gradientsUsed;
pathElem=a22.getElementById("linePath3dPrototype");
for(var a36=0;a36<seriesCount;++a36)
{
var a37=new ApacheChartBuffer();
pathElem=pathElem.cloneNode(false);
if(animate)
{
dataElems.push(pathElem);
pathElem.setAttribute("transform",a31);
}
for(var a38=0;a38<nValues;++a38)
{
dy=a34-a27*(yValues[a38][a36]-a32)/(maxYValue-a32);
dx=a25+a26*(xValues[a38][a36]-a33)/(maxXValue-a33);
if(a38!=nValues-1)
{
a37.append("M").append(dx).append(",").append(dy);
a37.append("l").append(a24).append(",").append(-yOffset);
var a39,nextdx;
nextdx=a25+a26*(xValues[a38+1][a36]-a33)/(maxXValue-a33);
a39=a34-
a27*(yValues[a38+1][a36]-a32)/(maxYValue-a32);
a37.append("L").append(nextdx+a24).append(",").append(a39-yOffset);
a37.append("l").append(-a24).append(",").append(yOffset);
a37.append("L").append(dx).append(",").append(dy);
}
}
if(a35)
pathElem.setAttribute("fill","url(#gradient"+a36+")");
else
pathElem.setAttribute("fill",a30[a36]);
pathElem.setAttribute("stroke",a30[a36]);
pathElem.setAttribute("seriesIndex",a36);
if(this._tooltipsVisible)
{
pathElem.addEventListener("mousemove",this.ShowToolTipCallback,false);
pathElem.addEventListener("mouseout",this.HideToolTipCallback,false);
}
pathElem.addEventListener("click",this.ClickCallback,false);
pathElem.setAttribute("d",a37.toString());
a23.appendChild(pathElem);
}
}
ApacheXYLineChart.prototype.ShowToolTip=function(a40)
{
this.HideToolTip();
ApacheXYLineChart.superclass.ShowToolTip.call(this,a40);
}
ApacheXYLineChart.prototype.GetToolTipLocation=function(a41,a42)
{
return{x:(a41.clientX+20),y:(a41.clientY+20)};
}
ApacheXYLineChart.prototype.GetChartEvent=function(a43)
{
var a44=a43.target;
var a45=parseInt(a44.getAttribute("seriesIndex"));
var a46=a43.clientX,clientY=a43.clientY,a44=a43.target;
var a47=this._isPerspective;
var a48=ApacheChart._YOFFSET_PERSPECTIVE;
var a49=this._model,xValues=a49.getXValues(),
yValues=a49.getYValues(),nValues=yValues.length;
var a50=a49.getMinYValue(),maxYValue=a49.getMaxYValue();
var a51=a49.getMinXValue(),maxXValue=a49.getMaxXValue();
var a52=this._margins,marginLeft=a52.left,marginTop=a52.top;
var a53=(this._width-marginLeft-a52.right);
var a54=(this._height-marginTop-a52.bottom);
var a55=a54+marginTop+(a47?a48:0);
var a56,dy,xValue=0.0,yValue=0.0;
var a57,nextdx;
for(var a58=0;a58<nValues;++a58)
{
if(a58!=nValues-1)
{
a56=marginLeft+a53*(xValues[a58][a45]-a51)/(maxXValue-a51);
nextdx=marginLeft+a53*(xValues[a58+1][a45]-a51)/(maxXValue-a51);
if(a46>a56&&a46<(a56+nextdx))
{
dy=a55-a54*(yValues[a58][a45]-a50)/(maxYValue-a50);
a57=a55-
a54*(yValues[a58+1][a45]-a50)/(maxYValue-a50);
yValue=yValues[a58][a45]+(yValues[a58+1][a45]-yValues[a58][a45])*(clientY-dy)/(a57-dy);
xValue=xValues[a58][a45]+(xValues[a58+1][a45]-xValues[a58][a45])*(a46-a56)/(nextdx-a56);
break;
}
}
}
return new ApacheChartEvent([a45],null,[yValue],[xValue]);
}
ApacheXYLineChart.prototype.FillToolTipData=function(a59,a60,a61)
{
var a62=this.GetChartEvent(a61);
var a63=a62.getSeriesIndices()[0],
yValue=a62.getYValues()[0]
xValue=a62.getXValues()[0];
var a64=this._model,seriesLabels=a64.getSeriesLabels();
var a65=a59.nextSibling.nextSibling;
a65.firstChild.data=seriesLabels[a63]+
": ("+this._formatValue(xValue)+
")    ("+this._formatValue(yValue)+")";
var a66=a65.getComputedTextLength();
a65=a65.nextSibling.nextSibling;
a65.firstChild.data="";
var a67=a66+2*ApacheChart._TEXT_MARGIN;
a59.setAttribute("width",a67);
a59.setAttribute("stroke",seriesColors[a63]);
a60.setAttribute("r",0);
}
function ApacheRadarChart(
a0,a1,a2,
a3,a4)
{
this.Init(a0,a1,a2,a3,a4);
}
ApacheChartObj.Inherit(ApacheChart,ApacheRadarChart);
ApacheRadarChart.prototype.Init=function(
a5,a6,a7,
a8,a9)
{
ApacheRadarChart.superclass.Init.call(this,a5,a6,a7,
a8,a9);
this._toolTips=[];
}
ApacheRadarChart.prototype.draw=function()
{
this._yLabels=[];
ApacheRadarChart.superclass.draw.call(this);
delete this._yLabels;
}
ApacheRadarChart.prototype.SetGridAnimStep=function(a10)
{
var a11=this._gridElems,animCount=a11.length;
for(var a12=0;a12<animCount;++a12)
{
a11[a12].setAttribute("fill-opacity",a10);
}
}
ApacheRadarChart.prototype.SetDataAnimStep=function(a13)
{
var a14=this._dataElems,animCount=a14.length;
var a15=this._margins,marginLeft=a15.left,marginTop=a15.top;
var a16=(this._type==ApacheChart.TYPE_RADAR_AREA);
var a17=this._model,groupLabels=a17.getGroupLabels(),groupCount=groupLabels.length,
seriesLabels=a17.getSeriesLabels(),seriesCount=seriesLabels.length;
var a18=(this._width-marginLeft-a15.right);
var a19=(this._height-marginTop-a15.bottom);
var a20=marginLeft+a18/2,cy=marginTop+a19/2;
var a21=a13*seriesCount,animSeriesIndex=0;
if(a21>1)
{
animSeriesIndex=Math.floor(a21);
if(animSeriesIndex>=seriesCount)
animSeriesIndex=seriesCount-1;
a21=a21-Math.floor(a21);
}
var a22=(1-a21)*a20,ty=(1-a21)*cy;
var a23="translate("+a22+","+ty+") scale("+a21+","+a21+")";
var a24=animSeriesIndex;
this._setRadarSeriesAnimStep(a24,a14,a16,a23);
if(a24>0)
{
this._setRadarSeriesAnimStep(a24-1,a14,a16,"scale(1,1)");
}
if(a13==1)
{
for(var a24=0;a24<seriesCount;++a24)
{
this._setRadarSeriesAnimStep(a24,a14,a16,"scale(1,1)");
}
}
}
ApacheRadarChart.prototype._setRadarSeriesAnimStep=function(a25,a26,a27,a28)
{
a26[a25].setAttribute("transform",a28);
if(!a27)
{
var a29=a26["dots"+a25];
for(var a30=a29.length-1;a30>=0;--a30)
{
a29[a30].setAttribute("transform",a28);
}
}
}
ApacheRadarChart.prototype.DrawChartData=function()
{
this._drawRadar();
var a31=this._yLabels,rootElem=this._rootElement;
for(var a32=a31.length-1;a32>=0;a32--)
{
var a33=a31[a32];
if(a33)
{
rootElem.removeChild(a33);
rootElem.appendChild(a33);
}
}
}
ApacheRadarChart.prototype.SetStopOpacity=function(a34)
{
a34.setAttribute("stop-opacity",ApacheChart._DEFAULT_STOP_OPACITY/2);
}
ApacheRadarChart.prototype.SetLegendTopAdjustment=function(a35)
{
var a36=this._hLabelContainer;
a35-=a36.getBBox().height+ApacheChart._TEXT_MARGIN;
return a35;
}
ApacheRadarChart.prototype.SetLegendBottomAdjustment=function(a37)
{
var a38=this._hLabelContainer;
if(a38.childNodes.length>0)
{
a37+=a38.getBBox().height+ApacheChart._TEXT_MARGIN;
}
return a37;
}
ApacheRadarChart.prototype.SetLegendLeftAdjustment=function(a39)
{
var a40=this._hLabelContainer;
if(a40.childNodes.length>0)
{
a39-=a40.getBBox().width+ApacheChart._TEXT_MARGIN;
}
return a39;
}
ApacheRadarChart.prototype.SetLegendRightAdjustment=function(a41)
{
var a42=this._hLabelContainer;
if(a42.childNodes.length>0)
a41+=a42.getBBox().width+ApacheChart._TEXT_MARGIN;
return a41;
}
ApacheRadarChart.prototype.DrawGroupLabels=function()
{
var a43=this._svgDoc,rootElem=this._rootElement,model=this._model;
var a44=a43.createElementNS("http://www.w3.org/2000/svg","g");
this._hLabelContainer=a44;
var a45=this._labelElems,animate=(this._animDuration>0);
var a46=model.getGroupLabels(),vLineCount=a46.length;
var a47=a43.getElementById("groupLabelPrototype");
var a48,gLabelElems=this._groupLabelElems;
for(var a49=0;a49<vLineCount;++a49)
{
a48=a46[a49];
if(!a48)
continue;
a47=a47.cloneNode(true);
if(animate)
{
a45.push(a47);
a47.setAttribute("fill-opacity","0");
}
a47.firstChild.data=a48;
a44.appendChild(a47);
gLabelElems[a49]=a47;
}
rootElem.appendChild(a44);
}
ApacheRadarChart.prototype.AdjustMarginsForGroupLabels=function()
{
var a50=this._hLabelContainer;
if(a50.childNodes.length>0)
{
var a51=a50.getBBox();
var a52=a51.width+ApacheChart._TEXT_MARGIN,
dyVertical=a51.height+ApacheChart._TEXT_MARGIN;
this._margins.top+=dyVertical;
this._margins.bottom+=dyVertical;
this._margins.left+=a52;
this._margins.right+=a52;
}
}
ApacheRadarChart.prototype.LayoutGroupLabels=function()
{
var a53=this._model,margins=this._margins;
var a54=margins.left,marginTop=margins.top;
var a55=(this._width-a54-margins.right);
var a56,cy;
var a57=a54+a55/2,radius;
var a58=a53.getGroupLabels(),vLineCount=a58.length;
var a59,groupWidth=a55/vLineCount;
var a60=this._hLabelContainer,childNodes=a60.childNodes;
var a61=false,gLabelElems=this._groupLabelElems;
if(childNodes.length==0)
return;
for(var a62=0;a62<vLineCount;++a62)
{
a59=gLabelElems[a62];
if(!a59)
continue;
if(!a61)
{
var a63=a59.getBBox();
marginTop=margins.top;
a56=(this._height-marginTop-margins.bottom);
cy=marginTop+a56/2;
radius=Math.min(a55,a56)/2+a63.height-ApacheChart._TEXT_MARGIN/2;
a61=true;
}
var a64=(a62)*2*Math.PI/vLineCount;
var a65=a57+radius*Math.sin(a64),
dy=cy-radius*Math.cos(a64);
a59.setAttribute("y",dy);
var a66=a59.getComputedTextLength();
if(a64>Math.PI)
a65-=a66;
a59.setAttribute("x",a65);
}
}
ApacheRadarChart.prototype.DrawGrid=function()
{
this.Draw2DGrid();
}
ApacheRadarChart.prototype.Draw2DGrid=function()
{
var a67=this._svgDoc,rootElem=this._rootElement,
model=this._model,margins=this._margins;
var a68=this._gridElems,animate=(this._animDuration>0);
var a69=margins.left,marginTop=margins.top;
var a70=(this._width-a69-margins.right);
var a71=(this._height-marginTop-margins.bottom);
var a72=a69+a70/2,cy=marginTop+a71/2;
var a73=Math.min(a70,a71)/2;
var a74=this._gradientsUsed;
var a75=a67.getElementById("radarCirclePrototype").cloneNode(false);
a75.setAttribute("cx",a72);
a75.setAttribute("cy",cy);
a75.setAttribute("r",a73);
if(a74)
a75.setAttribute("fill","url(#gridGradient)");
if(animate)
{
a68.push(a75);
a75.setAttribute("fill-opacity","0");
}
rootElem.appendChild(a75);
var a76=this.GetVLineCount(),circleCount=this.GetHLineCount();
a75=a67.getElementById("radarInnerCirclePrototype");
a75.setAttribute("cx",a72);
a75.setAttribute("cy",cy);
for(var a77=0;a77<circleCount-1;++a77)
{
a75=a75.cloneNode(false);
if(animate)
{
a68.push(a75);
a75.setAttribute("fill-opacity","0");
}
var a78=a73-(a77+1)*a73/circleCount;
a75.setAttribute("r",a78);
rootElem.appendChild(a75);
}
var a79=new ApacheChartBuffer();
var a80=a67.getElementById("radarGridPathPrototype").cloneNode(false);
a79.append("M").append(a72).append(",").append(cy);
a79.append("l").append(0).append(",").append(-a73);
for(var a77=0;a77<a76-1;++a77)
{
var a81=a72+a73*Math.sin((a77+1)*2*Math.PI/a76),
dy=cy-a73*Math.cos((a77+1)*2*Math.PI/a76);
a79.append("M").append(a72).append(",").append(cy);
a79.append("L").append(a81).append(",").append(dy);
}
a80.setAttribute("d",a79.toString());
if(animate)
{
a68.push(a80);
a80.setAttribute("fill-opacity","0");
}
rootElem.appendChild(a80);
}
ApacheRadarChart.prototype.DrawYValueLabels=function()
{
}
ApacheRadarChart.prototype.LayoutYValueLabels=function()
{
var a82=this._svgDoc,rootElem=this._rootElement,
model=this._model,margins=this._margins;
var a83=margins.left,marginTop=margins.top;
var a84=(this._width-a83-margins.right);
var a85=(this._height-marginTop-margins.bottom);
var a86=this._labelElems,animate=(this._animDuration>0);
var a87=a83+a84/2,cy=marginTop+a85/2;
var a88=Math.min(a84,a85)/2;
var a89=this.GetVLineCount(),circleCount=this.GetHLineCount();
var a90=model.getMinYValue(),maxValue=model.getMaxYValue();
var a91=this._yLabels;
var a92=a82.getElementById("yLabelPrototype");
a92=a92.cloneNode(true);
var a93=this._addRadarYLabelAt(rootElem,a92,circleCount-1,
a87,marginTop,a93,this._formatValue(maxValue));
if(animate)
{
a86.push(a92);
a92.setAttribute("fill-opacity","0");
}
a92=a92.cloneNode(true);
this._addRadarYLabelAt(rootElem,a92,circleCount,
a87,cy,a93,this._formatValue(a90));
if(animate)
{
a86.push(a92);
a92.setAttribute("fill-opacity","0");
}
for(var a94=0;a94<circleCount-1;++a94)
{
var a95=(a94+1)*a88/circleCount;
var a96=((maxValue-a90)*(a94+1)/circleCount)+a90;
a92=a92.cloneNode(true);
this._addRadarYLabelAt(rootElem,a92,a94,a87,
a88-a95+marginTop,a93,this._formatValue(a96));
if(animate)
{
a86.push(a92);
a92.setAttribute("fill-opacity","0");
}
}
}
ApacheRadarChart.prototype._addRadarYLabelAt=function(
a97,a98,a99,
a100,a101,a102,a103)
{
this._yLabels[a99]=a98;
a98.firstChild.data=a103;
a97.appendChild(a98);
if(a102==null)
{
var a104=a98.getBBox();
a102=a104.height;
}
var a105=ApacheChart._TEXT_MARGIN,
textLength=a98.getComputedTextLength();
dx=a100-textLength-a105;
a98.setAttribute("x",dx);
a98.setAttribute("y",a101+a102/2);
return a102;
}
ApacheRadarChart.prototype._drawRadar=function()
{
var a106=this._svgDoc,rootElem=this._rootElement,
model=this._model,margins=this._margins;
var a107=this._dataElems,animate=(this._animDuration>0),dotElems;
var a108=margins.left,marginTop=margins.top;
var a109=(this._width-a108-margins.right);
var a110=(this._height-marginTop-margins.bottom);
var a111=a108+a109/2,cy=marginTop+a110/2;
var a112=Math.min(a109,a110)/2;
var a113=(this._type==ApacheChart.TYPE_RADAR_AREA);
var a114=a113?"areaPathPrototype":"linePathPrototype";
var a115,pathElem=a106.getElementById(a114);
var a116=model.getSeriesLabels(),seriesCount=a116.length;
var a117=model.getSeriesColors(),yValues=model.getYValues();
var a118=model.getMinYValue(),maxValue=model.getMaxYValue();
var a119="scale(0.00001,0.00001)";
var a120=this._gradientsUsed;
var a121=yValues.length;
var a122,dy;
if(!a113)
a115=a106.getElementById("lineDotPrototype");
for(var a123=0;a123<seriesCount;++a123)
{
var a124=new ApacheChartBuffer();
pathElem=pathElem.cloneNode(false);
if(animate)
{
a107.push(pathElem);
pathElem.setAttribute("transform",a119);
dotElems=a107["dots"+a123]=[];
}
for(var a125=0;a125<a121;++a125)
{
var a126=a112*(yValues[a125][a123]-a118)/(maxValue-a118);
var a122=a111+a126*Math.sin((a125)*2*Math.PI/a121),
dy=cy-a126*Math.cos((a125)*2*Math.PI/a121);
if(a125==0)
{
a124.append("M").append(a122).append(",").append(dy);
}
else
{
a124.append("L").append(a122).append(",").append(dy);
}
if(!a113)
{
a115=a115.cloneNode(false);
a115.setAttribute("cx",a122);
a115.setAttribute("cy",dy);
if(a120)
a115.setAttribute("fill","url(#gradient"+a123+")");
else
a115.setAttribute("fill",a117[a123]);
a115.setAttribute("stroke",a117[a123]);
if(animate)
{
dotElems.push(a115);
a115.setAttribute("transform",a119);
}
rootElem.appendChild(a115);
}
}
a124.append("Z");
if(a113)
{
if(a120)
pathElem.setAttribute("fill","url(#gradient"+a123+")");
else
pathElem.setAttribute("fill",a117[a123]);
}
else
{
pathElem.setAttribute("fill","none");
}
pathElem.setAttribute("stroke",a117[a123]);
pathElem.setAttribute("seriesIndex",a123);
if(this._tooltipsVisible)
{
pathElem.addEventListener("mousemove",this.ShowToolTipCallback,false);
pathElem.addEventListener("mouseout",this.HideToolTipCallback,false);
}
pathElem.addEventListener("click",this.ClickCallback,false);
pathElem.setAttribute("d",a124.toString());
rootElem.appendChild(pathElem);
}
}
ApacheRadarChart.prototype.isPointInPolygon=function(a127,a128,a129,a130)
{
var a131,j,npol=a128.length,inside=false;
for(a131=0,j=npol-1;a131<npol;j=a131++){
if((((a128[a131]<=a130)&&(a130<a128[j]))||
((a128[j]<=a130)&&(a130<a128[a131])))&&
(a129<(a127[j]-a127[a131])*(a130-a128[a131])/(a128[j]-a128[a131])+a127[a131]))
inside=!inside;
}
return inside;
}
ApacheRadarChart.prototype.GetChartEvent=function(a132,a133,a134)
{
var a135=a132.clientX,clientY=a132.clientY,evtTarget=a132.target;
var a136=(this._type==ApacheChart.TYPE_RADAR_AREA);
var a137=this._model,yValues=a137.getYValues();
var a138=a137.getMinYValue(),maxValue=a137.getMaxYValue();
var a139=this._margins,marginLeft=a139.left,marginTop=a139.top;
var a140=(this._width-marginLeft-a139.right);
var a141=(this._height-marginTop-a139.bottom);
var a142=marginLeft+a140/2,cy=marginTop+a141/2;
var a143=Math.min(a140,a141)/2;
var a144=a137.getSeriesLabels().length;
var a145=a137.getYValues(),seriesIndex;
var a146=a145.length;
if(a135<marginLeft||
a135>(marginLeft+a140)||
clientY<marginTop||
clientY>(marginTop+a141))
{
return null;
}
if(!a136)
seriesIndex=parseInt(evtTarget.getAttribute("seriesIndex"));
if(!a133)
a133=[];
if(!a134)
a134=[];
var a147,dy1,dx2,dy2,seriesIndices=[],seriesValues=[];
for(var a148=0;a148<a144;++a148)
{
if(!a136&&(seriesIndex!=a148))
continue;
for(var a149=0;a149<a146;++a149)
{
var a150=(a149!=a146-1)?a145[a149+1][a148]:a145[0][a148];
var a151=a143*(a145[a149][a148]-a138)/(maxValue-a138);
var a152=a143*(a150-a138)/(maxValue-a138);
var a153=a149*2*Math.PI/a146,
nextAngle=(a149+1)*2*Math.PI/a146;
var a147=a142+a151*Math.sin(a153),
dy1=cy-a151*Math.cos(a153);
dx2=a142+a152*Math.sin(nextAngle);
dy2=cy-a152*Math.cos(nextAngle);
if(this.isPointInPolygon([a142,a147,dx2],[cy,dy1,dy2],a135,clientY))
{
var a154=Math.atan2(cy-clientY,a135-a142);
if(a154<=Math.PI/2)
a154=Math.PI/2-a154;
else
a154=3*Math.PI/2+(Math.PI-a154);
var a155=(a154-a153)/(nextAngle-a153);
value=a145[a149][a148]+(a150-a145[a149][a148])*a155;
seriesValues.push(value);
seriesIndices.push(a148);
a134.push(dy1+(dy2-dy1)*a155);
a133.push(a147+(dx2-a147)*a155);
break;
}
}
}
return new ApacheChartEvent(seriesIndices,null,seriesValues,null);
}
ApacheRadarChart.prototype.ShowToolTip=function(a156)
{
this.HideToolTip();
var a157=[],seriesYs=[];
var a158=this.GetChartEvent(a156,a157,seriesYs);
if(a158==null||a158.getYValues().length==0)
{
return;
}
this._displayToolTips(a158.getYValues(),a158.getSeriesIndices(),
seriesYs,a157,a156);
}
ApacheRadarChart.prototype._displayToolTips=function(
a159,a160,
a161,a162,a163)
{
var a164=this._svgDoc,rootElem=this._rootElement;
var a165=this._model,seriesLabels=a165.getSeriesLabels(),
seriesCount=seriesLabels.length,seriesColors=a165.getSeriesColors();
var a166=a160.length,toolTips=this._toolTips;
var a167,dy;
for(var a168=0;a168<a166;++a168)
{
var a169=a160[a168];
var a170=toolTips[a169];
var a171=false;
if(a170==null)
{
a170=a164.getElementById("toolTip").cloneNode(true);
rootElem.appendChild(a170);
toolTips[a169]=a170;
a171=true;
}
a170.style.setProperty("visibility","visible","");
var a172=a170.firstChild.nextSibling;
var a173=a172.nextSibling.nextSibling;
var a174=a173.nextSibling.nextSibling;
var a175=a159.length;
var a176=a174.getComputedTextLength();
a174.firstChild.data=
seriesLabels[a169]+":  "+this._formatValue(a159[a168]);
if(a171)
{
var a177=parseInt(a173.getAttribute("height"));
var a178=parseInt(a174.getAttribute("dy"));
a177-=a178;
a173.setAttribute("height",a177);
a174=a174.nextSibling.nextSibling;
a174.firstChild.data="";
}
a176+=2*ApacheChart._TEXT_MARGIN;
a173.setAttribute("width",a176);
var a179=a163.target.getBBox();
var a180=a170.getBBox();
a167=a162[a168],a178=a161[a168]-a180.height;
if(a167+a180.width>this._width)
{
a167-=a180.width;
a172.setAttribute("cx",a173.getBBox().width);
}
else
{
a172.setAttribute("cx",0);
}
if(a178-a180.height<0)
{
a178+=a180.height;
a172.setAttribute("cy",0);
}
else
{
a172.setAttribute("cy",a173.getBBox().height);
}
a173.setAttribute("stroke",seriesColors[a169]);
a172.setAttribute("stroke",seriesColors[a169]);
a170.setAttribute("transform","translate("+a167+","+a178+")");
}
}
ApacheRadarChart.prototype.HideToolTip=function(a181)
{
var a182=this._toolTips,tooltipCount=a182.length;
for(var a183=0;a183<tooltipCount;++a183)
{
var a184=a182[a183];
if(a184)
a184.style.setProperty("visibility","hidden","");
}
}
function ApacheFunnelChart(
a0,a1,a2,
a3,a4)
{
this.Init(a0,a1,a2,a3,a4);
}
ApacheChartObj.Inherit(ApacheChart,ApacheFunnelChart);
ApacheFunnelChart.prototype.SetDataAnimStep=function(a5)
{
var a6=this._dataElems,animCount=a6.length;
var a7="scale("+a5+","+a5+")";
for(var a8=0;a8<animCount;++a8)
{
a6[a8].setAttribute("transform",a7);
}
}
ApacheFunnelChart.prototype.DrawChartData=function()
{
var a9=this._rootElement;
var a10=this._model,yValues=a10.getYValues(),yValueCount=yValues.length;
var a11=a10.getGroupLabels(),groupCount=a11?a11.length:1;
var a12=Math.ceil(Math.sqrt(yValueCount)),nRows=Math.round(Math.sqrt(yValueCount));
var a13=this._margins,dx=a13.left,dy=a13.top;
var a14=(this._width-a13.left-a13.right)/a12;
var a15=this._isPerspective?0:2*ApacheChart._TEXT_MARGIN;
var a16=(this._height-a13.top-a13.bottom-(nRows-1)*a15)/nRows;
var a17=this._svgDoc.getElementById("groupLabelPrototype");
for(var a18=0;a18<nRows;++a18)
{
for(var a19=0;a19<a12;++a19)
{
var a20=a11?(a18*a12+a19):(-1);
if(a20>=yValueCount)
break;
var a21=(a20==-1)?null:a11[a20];
var a22=a9.cloneNode(false);
a9.appendChild(a22);
var a23=this.DrawGroupLabelTitle(a21,a9,
a17.cloneNode(true),dx,dy,
a14,a16);
var a24=a14-2*ApacheChart._TEXT_MARGIN;
if(this._isPerspective)
{
a23-=a24/6;
this._drawPerspectiveFunnel(a22,a24,a23,a20);
a22.setAttribute("transform",
"translate("+(dx+ApacheChart._TEXT_MARGIN)+","+(dy+a24/12)+")");
}
else
{
this._drawFunnel(a22,a24,a23,a20);
a22.setAttribute("transform",
"translate("+(dx+ApacheChart._TEXT_MARGIN)+","+dy+")");
}
dx+=a14;
}
dx=a13.left;
dy+=a16+a15;
}
}
ApacheFunnelChart.prototype.ComputeMinMaxValues=function()
{
}
ApacheFunnelChart.prototype.DrawGroupLabels=function()
{
}
ApacheFunnelChart.prototype.LayoutGroupLabels=function()
{
}
ApacheFunnelChart.prototype.DrawGrid=function()
{
}
ApacheFunnelChart.prototype.DrawYValueLabels=function()
{
}
ApacheFunnelChart.prototype.LayoutYValueLabels=function()
{
}
ApacheFunnelChart.prototype._drawFunnel=function(
a25,a26,
a27,a28)
{
var a29=this._svgDoc,model=this._model,yValues=model.getYValues();
var a30=model.getGroupLabels(),seriesColors=model.getSeriesColors();
if(a28==-1)
a28=0;
var a31=yValues[a28].length;
var a32=0;
for(var a33=0;a33<a31;++a33)
{
a32+=yValues[a28][a33];
}
var a34=this._dataElems,animate=(this._animDuration>0)
var a35=a29.getElementById("funnelPathPrototype");
var a36=this._gradientsUsed;
var a37="scale(0.00001, 0.00001)";
var a38=0,y=0,slope=(a26/2)/a27,
dx=a26,dy,nextX,nextY;
for(var a33=a31-1;a33>=0;--a33)
{
a35=a35.cloneNode(false);
if(animate)
{
a34.push(a35);
a35.setAttribute("transform",a37);
}
var a39=(yValues[a28][a33])/(a32);
var a40=new ApacheChartBuffer();
a40.append("M").append(a38).append(",").append(y);
a40.append("L").append(dx).append(",").append(y);
dy=(a27)*a39;
nextY=y+dy;
nextX=a26/2-slope*(a27-(nextY));
dx=a26-nextX;
if(a33!=0)
{
a40.append("L").append(dx).append(",").append(nextY);
a40.append("L").append(nextX).append(",").append(nextY);
a40.append("Z");
}
else
{
var a41=(dy/3<=ApacheFunnelChart._MAX_FUNNEL_TIP)?y+(dy-dy/3):
a27-ApacheFunnelChart._MAX_FUNNEL_TIP;
nextX=a26/2-slope*(a27-(a41));
dx=a26-nextX;
a40.append("L").append(dx).append(",").append(a41);
a40.append("L").append(dx).append(",").append(a27);
a40.append("L").append(nextX).append(",").append(a27);
a40.append("L").append(nextX).append(",").append(a41);
a40.append("Z");
}
a35.setAttribute("d",a40.toString());
if(a36)
a35.setAttribute("fill","url(#gradient"+a33+")");
else
a35.setAttribute("fill",seriesColors[a33]);
a35.setAttribute("stroke",seriesColors[a33]);
a35.setAttribute("stroke-width",1);
a35.setAttribute("yValueIndex",a28);
a35.setAttribute("seriesIndex",a33);
if(this._tooltipsVisible)
{
a35.addEventListener("mouseover",this.ShowToolTipCallback,false);
a35.addEventListener("mouseout",this.HideToolTipCallback,false);
}
a35.addEventListener("click",this.ClickCallback,false);
a25.appendChild(a35);
y=nextY;
a38=nextX;
}
}
ApacheFunnelChart.prototype._drawPerspectiveFunnel=function(
a42,a43,
a44,a45)
{
var a46=this._svgDoc,model=this._model,yValues=model.getYValues();
var a47=model.getGroupLabels(),seriesColors=model.getSeriesColors();
if(a45==-1)
a45=0;
var a48=yValues[a45].length;
var a49=0;
for(var a50=0;a50<a48;++a50)
{
a49+=yValues[a45][a50];
}
var a51=this._dataElems,animate=(this._animDuration>0)
var a52=a46.getElementById("funnelPathPrototype");
var a53=this._gradientsUsed;
var a54="scale(0.00001, 0.00001)";
var a55=0,y=0,slope=(a43/2)/a44,
dx=a43,dy,nextX,oldDx,nextY;
var a56=dx/2,ry=dx/24,oldRx,oldRy;
for(var a50=a48-1;a50>=0;--a50)
{
a52=a52.cloneNode(false);
if(animate)
{
a51.push(a52);
a52.setAttribute("transform",a54);
}
var a57=(yValues[a45][a50])/(a49);
var a58=new ApacheChartBuffer();
a58.append("M").append(a55).append(",").append(y);
a58.append("A").append(a56).append(",").append(ry);
a58.append(" 0 1,0 ").append(dx).append(",").append(y);
a58.append("A").append(a56).append(",").append(ry);
a58.append(" 0 1,0 ").append(a55).append(",").append(y);
oldDx=dx;
oldRx=a56;
oldRy=ry;
dy=(a44)*a57;
nextY=y+dy;
nextX=a43/2-slope*(a44-(nextY));
dx=a43-nextX;
a56=(dx-nextX)/2;
ry=a56/12;
if(a50!=0)
{
a58.append("L").append(nextX).append(",").append(nextY);
a58.append("A").append(a56).append(",").append(ry);
a58.append(" 0 1,0 ").append(dx).append(",").append(nextY);
a58.append("L").append(oldDx).append(",").append(y);
}
else
{
var a59=(dy/3<=ApacheFunnelChart._MAX_FUNNEL_TIP)?y+(dy-dy/3):
a44-ApacheFunnelChart._MAX_FUNNEL_TIP;
nextX=a43/2-slope*(a44-(a59));
dx=a43-nextX;
a56=(dx-nextX)/2;
ry=a56/12;
a58.append("L").append(nextX).append(",").append(a59);
a58.append("L").append(nextX).append(",").append(a44);
a58.append("A").append(a56).append(",").append(ry);
a58.append(" 0 1,0 ").append(dx).append(",").append(a44);
a58.append("A").append(a56).append(",").append(ry);
a58.append(" 0 1,0 ").append(nextX).append(",").append(a44);
a58.append("A").append(a56).append(",").append(ry);
a58.append(" 0 1,0 ").append(dx).append(",").append(a44);
a58.append("L").append(dx).append(",").append(a59);
a58.append("L").append(oldDx).append(",").append(y);
}
a52.setAttribute("d",a58.toString());
if(a53)
a52.setAttribute("fill","url(#gradient"+a50+")");
else
a52.setAttribute("fill",seriesColors[a50]);
a52.setAttribute("stroke",seriesColors[a50]);
a52.setAttribute("stroke-width",1);
a52.setAttribute("yValueIndex",a45);
a52.setAttribute("seriesIndex",a50);
if(this._tooltipsVisible)
{
a52.addEventListener("mouseover",this.ShowToolTipCallback,false);
a52.addEventListener("mouseout",this.HideToolTipCallback,false);
}
a52.addEventListener("click",this.ClickCallback,false);
a42.appendChild(a52);
y=nextY;
a55=nextX;
}
}
ApacheFunnelChart.prototype.GetToolTipLocation=function(a60,a61)
{
var a62=a60.target;
var a63=a62.getBBox();
var a64=a62.parentNode.getCTM();
return{x:(a64.e+a63.x+a63.width/2),
y:(a64.f+a63.y+a63.height/2-a61.height)};
}
ApacheFunnelChart._MAX_FUNNEL_TIP=16;
function ApacheGaugeChart(
a0,a1,a2,
a3,a4)
{
this.Init(a0,a1,a2,a3,a4);
}
ApacheChartObj.Inherit(ApacheChart,ApacheGaugeChart);
ApacheGaugeChart.prototype.Init=function(
a5,a6,a7,
a8,a9)
{
ApacheGaugeChart.superclass.Init.call(this,a5,a6,a7,
a8,a9);
}
ApacheGaugeChart.prototype.SetDataAnimStep=function(a10)
{
var a11=this._dataElems,animCount=a11.length;
var a12=this._model,yValues=a12.getYValues();
for(var a13=0;a13<animCount;++a13)
{
var a14=yValues[a13][0];
this.SetIndicatorPosition(a14,a11[a13],a10);
}
}
ApacheGaugeChart.prototype.SetIndicatorPosition=function(a15,a16,a17)
{
var a18,cx=this._animCx,cy=this._animCy;
var a19=this._model,minValue=a19.getMinYValue(),maxValue=a19.getMaxYValue();
var a20=a17*(a15-minValue)/(maxValue-minValue);
a18=Math.PI/6+a20*(5*Math.PI/3);
if(a18<Math.PI/2)
a18+=3*Math.PI/2;
else
a18-=Math.PI/2;
a18*=180/Math.PI;
a16.setAttribute("transform","rotate("+a18+" "+cx+" "+cy+")");
}
ApacheGaugeChart.prototype.DrawChartData=function()
{
if(this._yMinorGridCount<0)
this._yMinorGridCount=4;
var a21=this._rootElement;
var a22=this._model,yValues=a22.getYValues(),yValueCount=yValues.length
var a23=a22.getGroupLabels(),
groupCount=a23?a23.length:1;
var a24=Math.ceil(Math.sqrt(yValueCount)),nRows=Math.round(Math.sqrt(yValueCount));
var a25=this._margins,dx=a25.left,dy=a25.top;
var a26=(this._width-a25.left-a25.right)/a24;
var a27=2*ApacheChart._TEXT_MARGIN;
var a28=(this._height-a25.top-a25.bottom-(nRows-1)*a27)/nRows;
var a29=this._svgDoc.getElementById("groupLabelPrototype");
for(var a30=0;a30<nRows;++a30)
{
for(var a31=0;a31<a24;++a31)
{
var a32=a23?(a30*a24+a31):(-1);
if(a32>=yValueCount)
break;
var a33=(a32==-1)?null:a23[a32];
var a34=a21.cloneNode(false);
a21.appendChild(a34);
if(a33)
a29=a29.cloneNode(true);
var a35=this.DrawGroupLabelTitle(a33,a21,
a29,dx,dy,
a26,a28);
var a36=a26-2*ApacheChart._TEXT_MARGIN;
this.DrawDial(a34,a36,a35,a32);
a34.setAttribute("transform",
"translate("+(dx+ApacheChart._TEXT_MARGIN)+","+dy+")");
if(a33)
{
var a37=a34.getBBox(),gHeight=a37.height;
if(gHeight<a35-a27)
{
var a38=parseInt(a29.getAttribute("y"));
a38-=(a35-gHeight)/2-a27;
a29.setAttribute("y",a38);
}
}
dx+=a26;
}
dx=a25.left;
dy+=a28+a27;
}
}
ApacheGaugeChart.prototype.DrawLegend=function()
{
}
ApacheGaugeChart.prototype.ComputeMinMaxValues=function()
{
}
ApacheGaugeChart.prototype.DrawGroupLabels=function()
{
}
ApacheGaugeChart.prototype.LayoutGroupLabels=function()
{
}
ApacheGaugeChart.prototype.DrawGrid=function()
{
}
ApacheGaugeChart.prototype.DrawYValueLabels=function()
{
}
ApacheGaugeChart.prototype.LayoutYValueLabels=function()
{
}
ApacheGaugeChart.prototype.DrawDial=function(
a39,a40,
a41,a42)
{
var a43=this._svgDoc,model=this._model;
if(a42==-1)
a42=0;
var a44=this._dataElems,animate=(this._animDuration>0);
var a45=this.GetGaugeTemplateName();
var a46=a43.getElementById(a45).cloneNode(true);
a39.appendChild(a46);
var a47=a46.getBBox(),gaugeWidth=a47.width,
gaugeR=gaugeWidth/2;
var a48=this._markerTextGroup;
var a49=a46.getElementsByTagName("g");
var a50=a49.item(a49.length-1);
a50.setAttribute("yValueIndex",a42);
a50.setAttribute("seriesIndex",0);
if(this._tooltipsVisible)
{
a50.addEventListener("mouseover",this.ShowToolTipCallback,false);
a50.addEventListener("mouseout",this.HideToolTipCallback,false);
}
a50.addEventListener("click",this.ClickCallback,false);
if(this._animCx==null)
{
this._animCx=a50.getAttribute("_pivotCenterX");
this._animCy=a50.getAttribute("_pivotCenterY");
}
if(animate)
a44.push(a50);
else
{
this.SetIndicatorPosition(this._model.getYValues()[a42][0],a50,1);
}
if(a48!=null)
{
a48=a48.cloneNode(true);
a46.appendChild(a48);
}
else
{
this.CreateTextMarkerGroup(a46,gaugeR);
}
this.ScaleGauge(a46,a40,a41,gaugeWidth,a47.height);
}
ApacheGaugeChart.prototype.GetGaugeTemplateName=function()
{
return"circularGauge";
}
ApacheGaugeChart.prototype.CreateTextMarkerGroup=function(a51,a52)
{
var a53=this._svgDoc,model=this._model;
var a54=a53.createElementNS("http://www.w3.org/2000/svg","g");
a51.appendChild(a54);
this._markerTextGroup=a54;
var a55=a53.getElementById("gaugeMarkerMajor"),
majorMarkerCount=this._yMajorGridCount,
minorMarker=a53.getElementById("gaugeMarkerMinor"),
minorMarkerCount=this._yMinorGridCount,
textElem=a53.getElementById("gaugeTextPrototype");
var a56=a51.getElementsByTagName("path");
var a57=parseInt(a51.getAttribute("_markerRadius"));
var a58=model.getMinYValue(),maxValue=model.getMaxYValue();
var a59,y,angle,textMargin;
for(var a60=0,theta=Math.PI/6;
a60<=majorMarkerCount;++a60,theta+=(5*Math.PI/3)/majorMarkerCount)
{
var a61;
if(theta<Math.PI/2)
a61=theta+3*Math.PI/2;
else
a61=theta-Math.PI/2;
a59=a52-a57*(Math.cos(a61));
y=a52-a57*(Math.sin(a61));
var a62=a61*180/Math.PI;
var a63=a55.cloneNode(true);
a54.appendChild(a63);
a63.setAttribute("transform",
"translate("+a59.toFixed(0)+","+y.toFixed(0)+") rotate("+a62.toFixed(0)+" 0 0)");
var a64=a58+a60*(maxValue-a58)/(majorMarkerCount);
textElem=textElem.cloneNode(true);
textElem.firstChild.data=this._formatValue(a64);
a54.appendChild(textElem);
var a65=textElem.getBBox();
if(a60==0)
{
textMargin=a65.height/2;
}
a59=a52-(a57-textMargin)*(Math.cos(a61));
y=a52-(a57-textMargin)*(Math.sin(a61));
if(theta>=5*Math.PI/6&&theta<=7*Math.PI/6)
{
y+=a65.height;
a59-=a65.width/2
}
else
{
y+=a65.height/2;
if(theta<Math.PI)
a59+=2*ApacheChart._TEXT_MARGIN;
else
a59-=a65.width+2*ApacheChart._TEXT_MARGIN;
}
textElem.setAttribute("transform",
"translate("+a59.toFixed(0)+","+y.toFixed(0)+")");
}
for(var a60=(minorMarkerCount+1),
theta=Math.PI/6+(5*Math.PI/3)/(majorMarkerCount*minorMarkerCount);
a60<=(majorMarkerCount+1)*minorMarkerCount;
++a60,theta+=(5*Math.PI/3)/(majorMarkerCount*minorMarkerCount))
{
if(a60%minorMarkerCount==0)
continue;
var a61;
if(theta<Math.PI/2)
a61=theta+3*Math.PI/2;
else
a61=theta-Math.PI/2;
var a59=a52-a57*(Math.cos(a61));
var a66=a52-a57*(Math.sin(a61));
var a62=a61*180/Math.PI;
var a63=minorMarker.cloneNode(true);
a54.appendChild(a63);
a63.setAttribute("transform",
"translate("+a59.toFixed(0)+","+a66.toFixed(0)+") rotate("+a62.toFixed(0)+" 0 0)");
}
}
ApacheGaugeChart.prototype.ScaleGauge=function(a67,a68,a69,a70,a71)
{
var a72=Math.min(a68,a69);
var a73=(a72==a68)?0:(a68-a72)/2,
ty=(a72==a69)?0:(a69-a72)/2;
var a74=a72/a70;
a67.setAttribute("transform","translate("+a73+","+ty+") scale("+a74+","+a74+")");
}
ApacheGaugeChart.prototype.GetChartEvent=function(a75)
{
var a76=a75.target;
while(a76!=null&&a76.tagName!="g")
a76=a76.parentNode;
if(a76==null)
return null;
var a77=parseInt(a76.getAttribute("yValueIndex"));
var a78=this._model,yValues=a78.getYValues();
return new ApacheChartEvent(null,[a77],[yValues[a77][0]],null);
}
ApacheGaugeChart.prototype.FillToolTipData=function(a79,a80,a81)
{
var a82=this.GetChartEvent(a81);
if(a82==null)
return;
var a83=a82.getYValues()[0];
var a84=this._model.getGroupLabels();
var a85=a79.nextSibling.nextSibling;
a85.firstChild.data=""+this._formatValue(a83);
var a86=a85.getComputedTextLength();
a85=a85.nextSibling.nextSibling;
a85.firstChild.data="";
var a87=a86+2*ApacheChart._TEXT_MARGIN;
a79.setAttribute("width",a87);
a79.setAttribute("stroke",seriesColors[0]);
a80.setAttribute("r",0);
}
ApacheGaugeChart.prototype.GetToolTipLocation=function(a88,a89)
{
return{x:(a88.clientX+20),y:(a88.clientY+20)};
}
function ApacheSemiGaugeChart(
a0,a1,a2,
a3,a4)
{
this.Init(a0,a1,a2,a3,a4);
}
ApacheChartObj.Inherit(ApacheGaugeChart,ApacheSemiGaugeChart);
ApacheSemiGaugeChart.prototype.SetIndicatorPosition=function(a5,a6,a7)
{
var a8,cx=this._animCx,cy=this._animCy;
var a9=this._model,minValue=a9.getMinYValue(),maxValue=a9.getMaxYValue();
var a10=a7*(a5-minValue)/(maxValue-minValue);
a8=a10*Math.PI;
a8*=180/Math.PI;
a6.setAttribute("transform","rotate("+a8+" "+cx+" "+cy+")");
}
ApacheSemiGaugeChart.prototype.GetGaugeTemplateName=function()
{
return"semiGauge";
}
ApacheSemiGaugeChart.prototype.CreateTextMarkerGroup=function(a11,a12)
{
var a13=this._svgDoc,model=this._model;
gElem=a13.createElementNS("http://www.w3.org/2000/svg","g");
a11.appendChild(gElem);
this._markerTextGroup=gElem;
var a14=a13.getElementById("gaugeMarkerMajor"),
majorMarkerCount=this._yMajorGridCount,
minorMarker=a13.getElementById("gaugeMarkerMinor"),
minorMarkerCount=this._yMinorGridCount,
textElem=a13.getElementById("gaugeTextPrototype");
var a15=a11.getElementsByTagName("path");
var a16=parseInt(a11.getAttribute("_markerRadius"));
var a17=model.getMinYValue(),maxValue=model.getMaxYValue();
var a18,y,angle,textMargin;
for(var a19=0;a19<=majorMarkerCount;++a19)
{
var a20=a19*Math.PI/majorMarkerCount;
a18=a12-a16*(Math.cos(a20));
y=a12-a16*(Math.sin(a20));
var a21=a20*180/Math.PI;
var a22=a14.cloneNode(true);
gElem.appendChild(a22);
a22.setAttribute("transform",
"translate("+a18.toFixed(0)+","+y.toFixed(0)+") rotate("+a21.toFixed(0)+" 0 0)");
var a23=a17+a19*(maxValue-a17)/(majorMarkerCount);
textElem=textElem.cloneNode(true);
textElem.firstChild.data=this._formatValue(a23);
gElem.appendChild(textElem);
var a24=textElem.getBBox();
if(a19==0)
{
textMargin=a24.height/2;
}
a18=a12-(a16-textMargin)*(Math.cos(a20));
y=a12-(a16-textMargin)*(Math.sin(a20));
if(a20>=Math.PI/3&&a20<=2*Math.PI/3)
{
y+=a24.height;
a18-=a24.width/2
}
else
{
y+=a24.height/2;
if(a20<Math.PI/2)
a18+=2*ApacheChart._TEXT_MARGIN;
else
a18-=a24.width+2*ApacheChart._TEXT_MARGIN;
}
textElem.setAttribute("transform",
"translate("+a18.toFixed(0)+","+y.toFixed(0)+")");
}
for(var a19=1;a19<=(majorMarkerCount)*minorMarkerCount;++a19)
{
if(a19%minorMarkerCount==0)
continue;
var a20=a19*Math.PI/(majorMarkerCount*minorMarkerCount);
var a18=a12-a16*(Math.cos(a20));
var a25=a12-a16*(Math.sin(a20));
var a21=a20*180/Math.PI;
var a22=minorMarker.cloneNode(true);
gElem.appendChild(a22);
a22.setAttribute("transform",
"translate("+a18.toFixed(0)+","+a25.toFixed(0)+") rotate("+a21.toFixed(0)+" 0 0)");
}
}
ApacheSemiGaugeChart.prototype.ScaleGauge=function(
a26,
a27,
a28,
a29,
a30)
{
var a31=a27/a29,sy=a28/a30;
var a32=Math.min(a31,sy);
var a33=(a27<=a29)?0:(a27-a29)/2,
ty=(a28<=a30)?0:(a28-a30)/2;
a26.setAttribute("transform","translate("+a33+","+ty+") scale("+a32+","+a32+")");
}
