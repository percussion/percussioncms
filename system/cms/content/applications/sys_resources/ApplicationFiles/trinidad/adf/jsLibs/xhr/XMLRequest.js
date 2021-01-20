function TrXMLRequest()
{
this.isSynchronous=false;
this.callback=null;
this._state=TrXMLRequest.UNINITIALIZED;
this.headers=new Object();
this.xmlhttp=TrXMLRequest._createXmlHttpRequest();
}
TrXMLRequest.UNINITIALIZED=0;
TrXMLRequest.LOADING=1;
TrXMLRequest.LOADED=2;
TrXMLRequest.INTERACTIVE=3;
TrXMLRequest.COMPLETED=4;
TrXMLRequest.prototype.setSynchronous=
function(a0)
{
this.isSynchronous=a0;
};
TrXMLRequest.prototype.setCallback=
function(a1)
{
this.callback=a1;
};
TrXMLRequest.prototype.getCompletionState=
function()
{
return this._state;
};
TrXMLRequest.prototype.getStatus=
function()
{
return this.xmlhttp.status;
}
TrXMLRequest.prototype.getResponseXML=
function()
{
return this.xmlhttp.responseXML;
}
TrXMLRequest.prototype.getResponseText=
function()
{
return this.xmlhttp.responseText;
}
TrXMLRequest.prototype.send=
function(a2,a3)
{
var a4=this.xmlhttp;
if(!this.isSynchronous)
{
var a5=new Function("arguments.callee.obj.__onReadyStateChange();");
a5.obj=this;
a4.onreadystatechange=a5;
}
var a6=a3?"POST":"GET";
a4.open(a6,a2,!this.isSynchronous);
for(var a7 in this.headers)
a4.setRequestHeader(a7,this.headers[a7]);
a4.setRequestHeader("Tr-XHR-Message","true");
a4.send(a3);
if(this.isSynchronous)
{
this._state=a4.readyState;
}
}
TrXMLRequest.prototype.getResponseHeader=
function(a8)
{
return this.xmlhttp.getResponseHeader(a8);
}
TrXMLRequest.prototype.getAllResponseHeaders=
function()
{
return this.xmlhttp.getAllResponseHeaders();
}
TrXMLRequest.prototype.setRequestHeader=
function(a9,a10)
{
this.headers[a9]=a10;
}
TrXMLRequest._createXmlHttpRequest=function()
{
var a11;
if(_agent.isIE)
{
a11=new ActiveXObject("Msxml2.XMLHTTP");
}
else
{
a11=new XMLHttpRequest();
}
return a11;
}
TrXMLRequest.prototype.__onReadyStateChange=
function()
{
this._state=this.xmlhttp.readyState;
if(this.callback)
this.callback(this);
}
TrXMLRequest.prototype.cleanup=function()
{
this.callback=null
delete this.xmlhttp;
}
