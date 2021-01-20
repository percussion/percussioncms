function TrPopupDialog()
{
var a0=document.createElement("div");
a0.style.cssText="visibility:hidden; position: absolute; z-index: 201;";
var a1=document.createElement("div");
a0.appendChild(a1);
this._titleDiv=a1;
var a2=document.createElement("iframe");
a2.name="_blank";
a2.frameBorder="0";
this._iframe=a2;
a0.appendChild(a2);
document.body.appendChild(a0);
TrPanelPopup.call(this)
this.setModal(true);
this.setCentered(true);
this.setContent(a0);
var a3=TrPage.getInstance();
a0.className=a3.getStyleClass("af|panelPopup::container");
a2.className=a3.getStyleClass("af|panelPopup::content");
a1.className=a3.getStyleClass("af|panelPopup::title-text")+
' '+
a3.getStyleClass("af|panelPopup::title-bar");
}
TrPopupDialog.prototype=new TrPanelPopup();
TrPopupDialog.prototype.setTitle=function(a4)
{
if(a4)
{
this._titleDiv.innerHTML=a4;
this._titleDiv.style.display="block";
}
else
{
this._titleDiv.innerHTML="";
this._titleDiv.style.display="none";
}
}
TrPopupDialog.prototype.setDestination=function(a5)
{
this._iframe.src=a5;
}
TrPopupDialog.prototype.setSize=function(a6,a7)
{
if(a6&&a7)
{
this._resizeIFrame(a6,a7);
}
}
TrPopupDialog.getInstance=function()
{
return TrPopupDialog.DIALOG;
}
TrPopupDialog.prototype._resizeIFrame=function(a8,a9)
{
this._iframe.height=a9;
this._iframe.width=a8;
this._calcPosition(false);
}
TrPopupDialog._initDialogPage=function()
{
var a10;
try
{
a10=parent.TrPopupDialog.DIALOG;
}
catch(err)
{
}
if(!a10)
return;
a10.setTitle(document.title);
if(_agent.isIE)
{
a10._resizeIFrame(
a10._iframe.Document.body.scrollWidth+40,
a10._iframe.Document.body.scrollHeight+40);
}
else
{
a10._resizeIFrame(
a10._iframe.contentDocument.body.offsetWidth+40,
a10._iframe.contentDocument.body.offsetHeight+40);
}
a10.show();
}
TrPopupDialog._returnFromDialog=function()
{
var a11=TrPopupDialog.DIALOG;
if(a11)
{
a11.hide();
}
else
{
alert("returnFromDialog(): Error - Current popup is not a dialog");
}
TrPopupDialog.DIALOG=undefined;
}
TrPopupDialog._returnFromDialogAndSubmit=function(a12,a13)
{
if(a12)
{
var a14=a12['formName'];
var a15=a12['postback'];
_submitPartialChange(a14,0,{rtrn:a15});
}
}
TrPopupDialog._launchDialog=function(
a16,
a17,
a18,
a19)
{
var a20=TrPopupDialog.DIALOG;
if(!a20)
{
a20=TrPopupDialog.DIALOG=new TrPopupDialog();
}
a20.callback=a18;
a20.callbackProps=a19;
if(a17&&a17['width']&&a17['height'])
a20.setSize(a17['width'],a17['height']);
a20.setDestination(a16);
}
