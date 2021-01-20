function TrPage()
{
this._loadedLibraries=TrPage._collectLoadedLibraries();
this._requestQueue=new TrRequestQueue(window);
}
TrPage.getInstance=function()
{
if(TrPage._INSTANCE==null)
TrPage._INSTANCE=new TrPage();
return TrPage._INSTANCE;
}
TrPage.prototype.getRequestQueue=function()
{
return this._requestQueue;
}
TrPage.prototype.sendPartialFormPost=function(
a0,
a1,
a2)
{
this.getRequestQueue().sendFormPost(
this,this._requestStatusChanged,
a0,a1,a2);
}
TrPage.prototype._requestStatusChanged=function(a3)
{
if(a3.getStatus()==TrXMLRequestEvent.STATUS_COMPLETE)
{
var a4=a3.getResponseStatusCode();
if(a4==200)
{
if(!_agent.isPIE)
{
_pprStopBlocking(window);
}
if(a3.isPprResponse())
{
var a5=a3.getResponseXML();
this._handlePprResponse(a5.documentElement);
}
else
{
}
}
else if(a4>=400)
{
if(!_agent.isPIE)
{
_pprStopBlocking(window);
}
}
}
}
TrPage.prototype._handlePprResponse=function(a6)
{
var a7=TrPage._getNodeName(a6);
if(a7=="content")
{
this._handlePprResponseAction(a6);
var a8=a6.childNodes;
var a9=a8.length;
for(var a10=0;a10<a9;a10++)
{
var a11=a8[a10];
var a12=TrPage._getNodeName(a11);
if(a12=="fragment")
{
this._handlePprResponseFragment(a11);
}
else if(a12=="script")
{
this._handlePprResponseScript(a11);
}
else if(a12=="script-library")
{
this._handlePprResponseLibrary(a11);
}
}
}
else if(a7=="redirect")
{
var a13=TrPage._getTextContent(a6);
window.location.href=a13;
}
else if(a7=="error")
{
var a14=TrPage._getTextContent(a6);
if(a14==null)
a14="Unknown error during PPR";
alert(a14);
}
else if(a7=="noop")
{
}
else
{
}
}
TrPage._getNodeName=function(a15)
{
var a16=a15.nodeName;
if(!a16)
a16=a15.tagName;
return a16;
}
TrPage.prototype._handlePprResponseAction=function(a17)
{
var a18=a17.getAttribute("action");
if(a18)
{
var a19=window.document;
a19.forms[0].action=a18;
}
}
TrPage.prototype._handlePprResponseFragment=function(a20)
{
var a21=this._getFirstElementFromFragment(a20);
if(!a21)
return;
var a22=a21.getAttribute("id");
if(!a22)
return;
var a23=window.document;
var a24=a23.getElementById(a22);
var a25=_getActiveElement();
var a26=null;
if(a25&&TrPage._isDomAncestorOf(a25,a24))
a26=a25.id;
if(a24==null)
{
}
else
{
a24.parentNode.replaceChild(a21,a24);
}
if(a26)
{
a25=a23.getElementById(a26);
if(a25&&a25.focus)
{
a25.focus();
window._trActiveElement=a25;
}
}
}
TrPage._isDomAncestorOf=function(a27,a28)
{
while(a27)
{
if(a27==a28)
return true;
var a29=a27.parentNode;
if(a29==a27)
break;
a27=a29;
}
return false;
}
TrPage.prototype.__replaceDomElement=function(a30,a31)
{
a31.parentNode.replaceChild(a30,a31);
}
TrPage.prototype._getFirstElementFromFragment=function(a32)
{
var a33=a32.childNodes;
var a34=a32.childNodes[0];
var a35=a34.data;
var a36=window.document;
var a37=a36.createElement("div");
a37.innerHTML=a35;
return TrPage._getFirstElementWithId(a37);
}
TrPage._getFirstElementWithId=function(a38)
{
var a39=a38.childNodes;
var a40=a39.length;
for(var a41=0;a41<a40;a41++)
{
var a42=a39[a41];
if(a42.nodeType==1)
{
if(a42.id)
return a42;
return TrPage._getFirstElementWithId(a42);
}
}
return null;
}
TrPage.prototype._loadScript=function(source)
{
var loadedLibraries=this._loadedLibraries;
if(loadedLibraries[source])
return;
loadedLibraries[source]=true;
var xmlHttp=new TrXMLRequest();
xmlHttp.setSynchronous(true);
xmlHttp.send(source,null);
if(xmlHttp.getStatus()==200)
{
var responseText=xmlHttp.getResponseText();
if(responseText)
{
if(_agent.isIE)
window.execScript(responseText);
else
window.eval(responseText);
}
}
xmlHttp.cleanup();
}
TrPage.prototype._handlePprResponseScript=function(scriptNode)
{
var source=scriptNode.getAttribute("src");
if(source)
{
this._loadScript(source);
}
else
{
var nodeText=TrPage._getTextContent(scriptNode);
if(nodeText)
{
if(_agent.isIE)
window.execScript(nodeText);
else
window.eval(nodeText);
}
}
}
TrPage.prototype._handlePprResponseLibrary=function(a43)
{
var a44=TrPage._getTextContent(a43);
this._loadScript(a44);
}
TrPage._getTextContent=function(a45)
{
if(_agent.isIE)
{
var a46=a45.innerText;
if(a46==undefined)
a46=a45.text;
return a46;
}
if(_agent.isSafari)
{
var a47="";
var a48=a45.firstChild;
while(a48)
{
var a49=a48.nodeType;
if((a49==3)||(a49==4))
a47=a47+a48.data;
a48=a48.nextSibling;
}
return a47;
}
return a45.textContent;
}
TrPage._collectLoadedLibraries=function()
{
var a50=new Object();
var a51=window.document;
var a52=a51.getElementsByTagName("script");
for(var a53=0;a53<a52.length;a53++)
{
var a54=a52[a53].getAttribute("src");
if(a54)
a50[a54]=true;
}
return a50;
}
TrPage.prototype.addStyleClassMap=function(a55)
{
if(!a55)
return;
if(!this._styleClassMap)
this._styleClassMap=new Object();
for(var a56 in a55)
this._styleClassMap[a56]=a55[a56];
}
TrPage.prototype.getStyleClass=function(a57)
{
if(a57&&this._styleClassMap)
{
var a58=this._styleClassMap[a57];
if(a58)
return a58;
}
return a57;
}
