function TrRequestQueue(a0)
{
this._state=TrRequestQueue.STATE_READY;
this._requestQueue=new Array();
this._stateChangeListeners=null;
this._window=a0;
}
TrRequestQueue.STATE_READY=0;
TrRequestQueue.STATE_BUSY=1;
TrRequestQueue._MULTIPART_FRAME="_trDTSFrame";
TrRequestQueue._XMLHTTP_TYPE=0;
TrRequestQueue._MULTIPART_TYPE=1;
TrRequestQueue.prototype.dispose=function()
{
this._requestQueue=null;
this._stateChangeListeners=null;
this._window=null;
}
TrRequestQueue._RequestItem=function(
a1,
a2,
a3,
a4,
a5,
a6
)
{
this._type=a1;
this._context=a2;
this._actionURL=a3;
this._headerParams=a4;
this._content=a5;
this._method=a6;
}
TrRequestQueue.prototype._broadcastRequestStatusChanged=function(
a7,a8,a9)
{
if(a8)
{
try
{
a8.call(a7,a9);
}
catch(e)
{
TrRequestQueue._logError(
"Error ",e," delivering XML request status changed to ",
a8);
}
}
}
TrRequestQueue.prototype._addRequestToQueue=function(
a10,
a11,
a12,
a13,
a14,
a15
)
{
var a16=new TrRequestQueue._RequestItem(
a10,a11,a13,a15,a14,a12);
this._requestQueue.push(a16);
try
{
var a17=new TrXMLRequestEvent(
TrXMLRequestEvent.STATUS_QUEUED,
null);
this._broadcastRequestStatusChanged(a11,a12,a17);
}
catch(e)
{
TrRequestQueue._logError("Error on listener callback invocation - STATUS_QUEUED",e);
}
if(this._state==TrRequestQueue.STATE_READY)
{
this._state=TrRequestQueue.STATE_BUSY;
this._broadcastStateChangeEvent(TrRequestQueue.STATE_BUSY);
this._doRequest();
}
}
TrRequestQueue.prototype.sendFormPost=function(
a18,
a19,
a20,
a21,
a22
)
{
if(this._isMultipartForm(a20))
{
this.sendMultipartRequest(a18,a19,a20.action,a20,a21);
}
else
{
var a23=this._getPostbackContent(a20,a21);
this.sendRequest(a18,a19,a20.action,a23,a22);
}
}
TrRequestQueue.prototype._isMultipartForm=function(a24)
{
if(a24.enctype.toLowerCase()!="multipart/form-data")
return false;
var a25=a24.getElementsByTagName("input"),
inputCount=a25.length,multiPartForm=null;
for(var a26=0;a26<inputCount;++a26)
{
var a27=a25[a26];
if(a27.type=="file"&&a27.value)
{
return true;
}
}
return false;
}
TrRequestQueue.prototype._getPostbackContent=function(a28,a29)
{
var a30=a28.elements;
var a31={};
if(a30)
{
for(var a32=0;a32<a30.length;a32++)
{
var a33=a30[a32];
if(a33.name&&!a33.disabled)
{
if(a33.options)
{
a31[a33.name]=new Array();
for(var a34=0;a34<a33.options.length;a34++)
{
var a35=a33.options[a34];
if(a35.selected)
{
var a36=(a35.value===null)?
a35.text:a35.value;
a31[a33.name].push(a36);
}
}
}
else if(!((a33.type=="checkbox"||
a33.type=="radio")&&
!a33.checked))
{
var a37=a31[a33.name];
if(a37)
{
if(!a37.join)
{
var a38=new Array();
a38.push(a37);
a31[a33.name]=a38;
a37=a38;
}
a37.push(a33.value);
}
else
{
a31[a33.name]=a33.value;
}
}
}
}
}
for(var a39 in a29)
{
var a40=a29[a39];
a31[a39]=a29[a39];
}
var a41="";
for(var a39 in a31)
{
var a42=a31[a39];
if(a42!=null)
{
if(a42.join)
{
var a43=a42;
for(var a44=0;a44<a43.length;a44++)
{
a41=TrRequestQueue._appendUrlFormEncoded(a41,a39,a43[a44]);
}
}
else
{
a41=TrRequestQueue._appendUrlFormEncoded(a41,a39,a42);
}
}
}
return a41;
}
TrRequestQueue._appendUrlFormEncoded=function(
a45,
a46,
a47)
{
if(a45.length>0)
{
a45=a45+"&";
}
return a45+a46+"="+a47.toString().replace(/\%/g,'%25')
.replace(/\+/g,'%2B')
.replace(/\//g,'%2F')
.replace(/\&/g,'%26')
.replace(/\"/g,'%22')
.replace(/\'/g,'%27');
}
TrRequestQueue.prototype.sendRequest=function(
a48,
a49,
a50,
a51,
a52
)
{
this._addRequestToQueue(TrRequestQueue._XMLHTTP_TYPE,a48,a49,a50,a51,a52);
}
TrRequestQueue.prototype.sendMultipartRequest=function(
a53,
a54,
a55,
a56,
a57
)
{
var a58=
{"htmlForm":a56,"params":a57,"context":a53,"method":a54};
this._addRequestToQueue(TrRequestQueue._MULTIPART_TYPE,a58,null,a55);
}
TrRequestQueue.prototype._doRequest=function()
{
var a59=this._requestQueue.shift();
switch(a59._type)
{
case TrRequestQueue._XMLHTTP_TYPE:
this._doXmlHttpRequest(a59);
break;
case TrRequestQueue._MULTIPART_TYPE:
this._doRequestThroughIframe(a59);
break;
}
}
TrRequestQueue.prototype._doXmlHttpRequest=function(a60)
{
var a61=new TrXMLRequest();
a61.__dtsRequestContext=a60._context;
a61.__dtsRequestMethod=a60._method;
var a62=TrUIUtils.createCallback(this,this._handleRequestCallback);
a61.setCallback(a62);
a61.setRequestHeader("Content-Type","application/x-www-form-urlencoded");
var a63=a60._headerParams;
if(a63!=null)
{
for(var a64 in a63)
{
var a65=a63[a64];
if(a65["join"])
a65=a65.join(',')
a61.setRequestHeader(a64,a65);
}
}
a61.send(a60._actionURL,a60._content);
}
TrRequestQueue.prototype._doRequestThroughIframe=function(a66)
{
var a67=a66._context.htmlForm;
var a68=a66._actionURL;
var a69=a66._context.params;
var a70=TrRequestQueue._MULTIPART_FRAME;
var a71=this._getDomDocument();
var a72=a71.getElementById(a70),iframeDoc;
var a73=_agent.isIE;
if(!a72)
{
a72=a71.createElement('iframe');
a72.name=a70;
a72.id=a70;
var a74=a72.style;
a74.top=a74.left='0px';
a74.width=a74.height='1px'
a74.position='absolute';
a74.visibility="hidden";
a71.body.appendChild(a72);
}
if(a73)
{
a72=a71.frames[a70];
a72.name=a70;
iframeDoc=a72.document;
}
else if(_agent.isSafari)
{
iframeDoc=a72.document;
}
else
{
iframeDoc=a72.contentDocument;
}
if(iframeDoc&&iframeDoc.firstChild)
iframeDoc.removeChild(iframeDoc.firstChild);
this._dtsContext=a66._context.context;
this._dtsRequestMethod=a66._context.method;
this._htmlForm=a67;
this._savedActionUrl=a67.action;
this._savedTarget=a67.target;
a67.method="POST";
a67.action=a68;
a67.target=a70;
this._appendParamNode(a71,a67,"Tr-XHR-Message","true");
this._appendParamNode(a71,a67,"partial","true");
if(a69)
{
for(var a75 in a69)
{
this._appendParamNode(a71,a67,a75,a69[a75]);
}
}
if(this._iframeLoadCallback==null)
this._iframeLoadCallback=TrUIUtils.createCallback(this,this._handleIFrameLoad);
a67.submit();
this._window.setTimeout(this._iframeLoadCallback,50);
}
TrRequestQueue.prototype._appendParamNode=function(a76,a77,a78,a79)
{
var a80=this._paramNodes;
if(!a80)
{
a80=new Array();
this._paramNodes=a80;
}
var a81=a76.createElement("input");
a81.type="hidden";
a81.name=a78;
a81.value=a79;
a80.push(a81);
a77.appendChild(a81);
}
TrRequestQueue.prototype._clearParamNodes=function()
{
var a82=this._paramNodes;
if(a82)
{
var a83=a82[0].parentNode;
var a84=a82.length;
for(var a85=0;a85<a84;a85++)
{
a83.removeChild(a82[a85]);
}
delete this._paramNodes;
}
}
TrRequestQueue.prototype._handleIFrameLoad=function()
{
var a86=this._getDomDocument();
var a87=_agent.isIE;
var a88=TrRequestQueue._MULTIPART_FRAME;
var a89,iframeDoc;
if(a87)
{
a89=a86.frames[a88];
var a90=a89.document;
}
else
{
a89=a86.getElementById(a88);
a90=a89.contentDocument;
}
try
{
if(!a90.documentElement||!a90.documentElement.firstChild
||(a87&&a90.readyState!="complete"))
{
this._window.setTimeout(this._iframeLoadCallback,50);
}
else
{
this._onIFrameLoadComplete(a90,this._dtsContext,
this._dtsRequestMethod);
}
}
catch(e)
{
TrRequestQueue._alertError();
TrRequestQueue._logError("Error while performing request",e);
this._htmlForm.action=this._savedActionUrl;
this._htmlForm.target=this._savedTarget;
}
}
TrRequestQueue.prototype._onIFrameLoadComplete=function(
a91,
a92,
a93)
{
try
{
var a94=new TrIFrameXMLRequestEvent(
a91);
this._broadcastRequestStatusChanged(a92,a93,a94);
}
finally
{
if(a91.firstChild)
a91.removeChild(a91.firstChild);
this._htmlForm.action=this._savedActionUrl;
this._htmlForm.target=this._savedTarget;
this._clearParamNodes();
this._requestDone();
}
}
TrRequestQueue.prototype._handleRequestCallback=function(
a95
)
{
var a96=a95.getCompletionState();
if(a96!=TrXMLRequest.COMPLETED)
return;
var a97=0;
var a98=TrRequestQueue._getFailedConnectionText();
try
{
a97=a95.getStatus();
}
catch(e)
{
}
if((a97!=200)&&(a97!=0))
{
TrRequestQueue._alertError();
TrRequestQueue._logError("Error StatusCode(",
a97,
") while performing request\n",
a95.getResponseText());
}
try
{
if(a97!=0)
{
var a99=new TrXMLRequestEvent(
TrXMLRequestEvent.STATUS_COMPLETE,
a95);
this._broadcastRequestStatusChanged(
a95.__dtsRequestContext,
a95.__dtsRequestMethod,
a99);
}
}
finally
{
a95.cleanup();
delete a95;
this._requestDone();
}
}
TrRequestQueue.prototype._requestDone=function()
{
if(this._requestQueue.length>0)
{
this._doRequest();
}
else
{
this._state=TrRequestQueue.STATE_READY;
this._broadcastStateChangeEvent(TrRequestQueue.STATE_READY);
}
}
TrRequestQueue.prototype.addStateChangeListener=function(a100,a101)
{
var a102=this._stateChangeListeners;
if(!a102)
{
a102=new Array();
this._stateChangeListeners=a102;
}
a102.push(a100);
a102.push(a101);
}
TrRequestQueue.prototype.removeStateChangeListener=function(a103,a104)
{
var a105=this._stateChangeListeners;
var a106=a105.length;
for(var a107=0;a107<a106;a107++)
{
var a108=a105[a107];
a107++;
if(a108==a103)
{
var a109=a105[a107];
if(a109===a104)
{
a105.splice(a107-1,2);
}
}
}
if(a105.length==0)
{
this._stateChangeListeners=null;
}
}
TrRequestQueue.prototype.getDTSState=function()
{
return this._state;
}
TrRequestQueue.prototype._broadcastStateChangeEvent=function(a110)
{
var a111=this._stateChangeListeners;
if(a111)
{
var a112=a111.length;
for(var a113=0;a113<a112;a113++)
{
try
{
var a114=a111[a113];
a113++;
var a115=a111[a113];
if(a115!=null)
a114.call(a115,a110);
else
a114(a110);
}
catch(e)
{
TrRequestQueue._logError("Error on DTS State Change Listener",e);
}
}
}
}
TrRequestQueue.prototype._getDomDocument=function()
{
return this._window.document;
}
TrRequestQueue._getFailedConnectionText=function()
{
return"Connection failed";
}
TrRequestQueue._alertError=function()
{
var a116=TrRequestQueue._getFailedConnectionText();
if(a116!=null)
alert(a116);
}
TrRequestQueue._logWarning=function(a117)
{
if(console&&console.warn)
console.warn(arguments);
}
TrRequestQueue._logError=function(a118)
{
if(console&&console.error)
console.error(arguments);
}
