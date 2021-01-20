function TrIFrameXMLRequestEvent(
a0)
{
this._iframeDoc=a0;
}
TrIFrameXMLRequestEvent.prototype.getStatus=function()
{
return TrXMLRequestEvent.STATUS_COMPLETE;
}
TrIFrameXMLRequestEvent.prototype.getResponseXML=function()
{
var a1=_agent.isIE;
var a2=this._iframeDoc;
if(a1&&a2.XMLDocument)
return a2.XMLDocument;
else
return a2;
}
TrIFrameXMLRequestEvent.prototype.getResponseText=function()
{
var a3=_agent.isIE;
var a4=this._iframeDoc,xmlDocument=null;
if(a3&&a4.XMLDocument)
xmlDocument=a4.XMLDocument;
else if(window.XMLDocument&&(a4 instanceof XMLDocument))
xmlDocument=a4;
if(xmlDocument)
return AdfAgent.AGENT.getNodeXml(xmlDocument);
else
return a4.documentElement.innerHTML;
}
TrIFrameXMLRequestEvent.prototype._isResponseValidXML=function()
{
var a5=_agent.isIE;
var a6=this._iframeDoc;
if(a5&&a6.XMLDocument)
return true;
else if(window.XMLDocument&&(a6 instanceof XMLDocument))
return true;
else
return false;
}
TrIFrameXMLRequestEvent.prototype.getResponseStatusCode=function()
{
return 200;
}
TrIFrameXMLRequestEvent.prototype.isPprResponse=function()
{
var a7=_agent.isIE;
var a8=this._iframeDoc;
var a9=false;
if(a7&&a8.XMLDocument)
{
var a10=a8.XMLDocument,childNodes=a10.childNodes;
if(childNodes.length>=2&&childNodes[1].nodeName=="Tr-XHR-Response-Type")
a9=true;
}
else
{
if(a8.firstChild&&a8.firstChild.nodeName=="Tr-XHR-Response-Type")
a9=true;
}
return a9;
}
TrIFrameXMLRequestEvent.prototype.getResponseContentType=function()
{
if(this._isResponseValidXML())
return"text/xml";
return"text/html";
}
