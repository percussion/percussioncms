TrPanelPopup.showPopup=function(a0,a1,a2,a3)
{
var a4=TrPanelPopup.POPUP;
if(!a4)
a4=TrPanelPopup.POPUP=new TrPanelPopup();
else
a4.hide();
var a5=document.getElementById(a0);
if(!a5)
return;
var a6=document.getElementById(a1);
a4.setContent(a5);
a4.setTrigger(a6);
a4.setModal(false);
a4.setCentered(false);
a4.callback=TrPanelPopup._popupCallback;
if(a2)
{
if(a2['modal'])
a4.setModal(true);
if(a2['center'])
a4.setCentered(true);
if(a2['width']&&a2['height'])
a4.setSize(a2['width'],a2['height']);
}
a4.show(a3);
}
TrPanelPopup.hidePopup=function()
{
var a7=TrPanelPopup.POPUP;
if(a7)
a7.hide();
}
TrPanelPopup._popupCallback=function()
{
TrPanelPopup.POPUP=undefined;
}
function TrPanelPopup()
{
this._content=false;
this._trigger=false;
this._centered=false;
this._modal=false;
this._visible=false;
this._eventCallbackFunction=TrUIUtils.createCallback(this,this._handleEvent);
}
TrPanelPopup.prototype.getContent=function()
{
return this._content;
}
TrPanelPopup.prototype.setContent=function(a0)
{
this._content=a0;
if(this._content)
{
this._content.style.cssText="position: absolute; z-index: 201; top: 0px; left: 0px; visibility:hidden; padding: 0px; overflow:auto;";
}
}
TrPanelPopup.prototype.getTrigger=function()
{
return this._trigger;
}
TrPanelPopup.prototype.setTrigger=function(a1)
{
this._trigger=a1;
}
TrPanelPopup.prototype.setCentered=function(a2)
{
this._centered=a2;
}
TrPanelPopup.prototype.isModal=function()
{
return this._modal;
}
TrPanelPopup.prototype.setModal=function(a3)
{
this._modal=a3;
}
TrPanelPopup.prototype.isVisible=function()
{
return this._visible;
}
TrPanelPopup.prototype.returnValue=undefined;
TrPanelPopup.prototype.callback=undefined;
TrPanelPopup.prototype.callbackProps=undefined;
TrPanelPopup.prototype.show=function(a4)
{
if(!this.getContent())
return;
if(_pprBlocking)
return;
if(this.isVisible())
return;
this._calcPosition(a4);
if(this.isModal())
{
TrPanelPopup._showMask();
}
else
{
TrPanelPopup._addEvent(document,"click",this._eventCallbackFunction);
}
TrPanelPopup._showIeIframe();
this.getContent().style.visibility="visible";
this._visible=true;
}
TrPanelPopup.prototype.hide=function()
{
if(!this.getContent())
return;
if(this.isModal())
{
TrPanelPopup._hideMask();
}
else
{
TrPanelPopup._removeEvent(document,"click",this._eventCallbackFunction);
}
TrPanelPopup._hideIeIframe();
this.getContent().style.visibility="hidden";
this.getContent().style.left="0px";
this.getContent().style.top="0px";
if(this.callback)
{
this.callback(this.callbackProps,this.returnValue);
}
this._visible=false;
}
TrPanelPopup.prototype.setSize=function(a5,a6)
{
if(a5)
{
this.getContent().style.width=a5+"px";
}
if(a6)
{
this.getContent().style.height=a6+"px";
}
}
TrPanelPopup.prototype._handleEvent=function(a7)
{
if(!this.isVisible()||this.isModal())
return;
var a8=false;
if(_agent.isIE)
{
a8=a7.srcElement;
}
else
{
a8=a7.target;
}
while(a8)
{
if(a8==this.getContent()||
a8==this.getTrigger())
{
break;
}
a8=a8.parentNode;
}
if(!a8)
{
this.hide();
}
}
TrPanelPopup._mask=undefined;
TrPanelPopup._showMask=function()
{
if(!TrPanelPopup._mask)
{
TrPanelPopup._mask=document.createElement('div');
TrPanelPopup._mask.name="TrPanelPopup._BlockingModalDiv";
TrPanelPopup._mask.style.cssText="display:none;position: absolute; z-index: 200;top: 0px;left: 0px;cursor: not-allowed; background-color: transparent;";
TrPanelPopup._mask.innerHTML="&nbsp;";
TrPanelPopup._addEvent(TrPanelPopup._mask,"click",TrPanelPopup._consumeMaskEvent);
TrPanelPopup._addEvent(window,"resize",TrPanelPopup._setMaskSize);
TrPanelPopup._setMaskSize();
document.body.appendChild(TrPanelPopup._mask);
}
TrPanelPopup._mask.style.display="block";
}
TrPanelPopup._hideMask=function()
{
TrPanelPopup._removeEvent(TrPanelPopup._mask,"click",TrPanelPopup._consumeMaskEvent);
TrPanelPopup._removeEvent(window,"resize",TrPanelPopup._setMaskSize);
TrPanelPopup._mask.style.display="none";
}
TrPanelPopup.prototype._calcPosition=function(a9)
{
var a10=0;
var a11=0;
var a12=_agent.isIE;
var a13=a12?document.body.scrollLeft:window.pageXOffset;
var a14=a12?document.body.scrollTop:window.pageYOffset;
var a15=document.body.scrollWidth;
var a16=document.body.scrollHeight;
var a17=a12?document.body.clientWidth:window.innerWidth;
var a18=a12?document.body.clientHeight:window.innerHeight;
var a19=this.getContent().clientWidth;
var a20=this.getContent().clientHeight;
if(this._centered)
{
a10=a13+((a17-a19)/2);
a11=a14+((a18-a20)/2);
}
else
{
var a21=a12?window.event.clientX:a9.clientX;
var a22=a12?window.event.clientY:a9.clientY;
if(a13+a21+a19>document.body.scrollWidth)
a10=document.body.scrollWidth-a19;
else
a10=a13+a21;
if(a14+a22+a20>document.body.scrollHeight)
a11=document.body.scrollHeight-a20;
else
a11=a14+a22;
}
this.getContent().style.left=a10+"px";
this.getContent().style.top=a11+"px";
if(!this.isModal())
TrPanelPopup._resizeIeIframe(a10,a11,a19,a20);
}
TrPanelPopup._consumeMaskEvent=function(a23)
{
return false;
}
TrPanelPopup._addEvent=function(a24,a25,a26)
{
if(a24.addEventListener)
{
a24.addEventListener(a25,a26,false);
return true;
}
else if(a24.attachEvent)
{
var a27=a24.attachEvent("on"+a25,a26);
return a27;
}
else
{
return false;
}
}
TrPanelPopup._removeEvent=function(a28,a29,a30)
{
if(a28.removeEventListener)
{
a28.removeEventListener(a29,a30,false);
return true;
}
else if(a28.detachEvent)
{
var a31=a28.detachEvent("on"+a29,a30);
return a31;
}
else
{
return false;
}
}
TrPanelPopup._setMaskSize=function()
{
if(!TrPanelPopup._mask)
return;
if(window.innerHeight!=window.undefined)
fullHeight=window.innerHeight;
else if(document.compatMode=='CSS1Compat')
fullHeight=document.documentElement.clientHeight;
else if(document.body)
fullHeight=document.body.clientHeight;
if(window.innerWidth!=window.undefined)
fullWidth=window.innerWidth;
else if(document.compatMode=='CSS1Compat')
fullWidth=document.documentElement.clientWidth;
else if(document.body)
fullWidth=document.body.clientWidth;
if(fullHeight>document.body.scrollHeight)
{
popHeight=fullHeight;
}
else
{
popHeight=document.body.scrollHeight
}
TrPanelPopup._mask.style.height=popHeight+"px";
TrPanelPopup._mask.style.width=document.body.scrollWidth+"px";
TrPanelPopup._resizeIeIframe(0,0,document.body.scrollWidth,popHeight);
}
TrPanelPopup._showIeIframe=function()
{
if(_agent.isIE)
{
TrPanelPopup._initIeIframe();
TrPanelPopup._maskIframe.style.display="block";
}
}
TrPanelPopup._hideIeIframe=function()
{
if(_agent.isIE)
{
TrPanelPopup._initIeIframe();
TrPanelPopup._maskIframe.style.display="none";
}
}
TrPanelPopup._resizeIeIframe=function(a32,a33,a34,a35)
{
if(_agent.isIE)
{
TrPanelPopup._initIeIframe();
TrPanelPopup._maskIframe.style.left=a32;
TrPanelPopup._maskIframe.style.top=a33;
TrPanelPopup._maskIframe.style.width=a34;
TrPanelPopup._maskIframe.style.height=a35;
}
}
TrPanelPopup._initIeIframe=function()
{
if(!TrPanelPopup._maskIframe)
{
TrPanelPopup._maskIframe=document.createElement('iframe');
TrPanelPopup._maskIframe.name="TrPanelPopup._ieOnlyZIndexIframe";
TrPanelPopup._maskIframe.style.cssText="display: none; left: 0px; position: absolute; top: 0px; z-index: 199;";
TrPanelPopup._maskIframe.style.filter="progid:DXImageTransform.Microsoft.Alpha(style=0,opacity=0)";
document.body.appendChild(TrPanelPopup._maskIframe);
}
}
