function TrXMLRequestEvent(
a0,
a1
)
{
this._status=a0;
this._request=a1;
}
TrXMLRequestEvent.STATUS_QUEUED=1;
TrXMLRequestEvent.STATUS_SEND_BEFORE=2;
TrXMLRequestEvent.STATUS_SEND_AFTER=3;
TrXMLRequestEvent.STATUS_COMPLETE=4;
TrXMLRequestEvent.prototype.getStatus=function()
{
return this._status;
}
TrXMLRequestEvent.prototype.getResponseXML=function()
{
return this._request.getResponseXML();
}
TrXMLRequestEvent.prototype._isResponseValidXML=function()
{
var a2=this._request.getResponseXML();
if(!a2)
return false;
var a3=a2.documentElement;
if(!a3)
return false;
var a4=a3.nodeName;
if(!a4)
a4=a3.tagName;
if(a4=="parsererror")
return false;
return true;
}
TrXMLRequestEvent.prototype.getResponseText=function()
{
return this._request.getResponseText();
}
TrXMLRequestEvent.prototype.getResponseStatusCode=function()
{
return this._request.getStatus();
}
TrXMLRequestEvent.prototype._getAllResponseHeaders=function()
{
return this._request.getAllResponseHeaders();
}
TrXMLRequestEvent.prototype.getResponseHeader=function(
a5
)
{
var a6=this._request.getAllResponseHeaders();
return(a6.indexOf(a5)!=-1)?
this._request.getResponseHeader(a5)
:null;
}
TrXMLRequestEvent.prototype.isPprResponse=function()
{
var a7=true;
if(a7&&(!this._isResponseValidXML()))
{
TrRequestQueue._logError("Invalid PPR response."+
" The response-headers were:\n"+
this._getAllResponseHeaders()+
"\n The invalid response was:\n"+
this.getResponseText());
}
return a7;
}
TrXMLRequestEvent.prototype.getResponseContentType=function()
{
this.getResponseHeader("Content-Type");
}
