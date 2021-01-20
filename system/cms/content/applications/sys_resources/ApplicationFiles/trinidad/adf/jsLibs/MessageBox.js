TrMessageBox._registerMessageBox=function(a0)
{
if(!TrMessageBox._MESSAGE_BOX)
TrMessageBox._MESSAGE_BOX=new TrMessageBox(a0);
}
TrMessageBox.addMessage=function(a1,a2,a3)
{
var a4=TrMessageBox._MESSAGE_BOX;
if(!a4)
return;
a4.addMessage(a1,a2,a3);
}
TrMessageBox.removeMessages=function(a5)
{
var a6=TrMessageBox._MESSAGE_BOX;
if(!a6)
return;
a6.removeMessages(a5);
}
TrMessageBox.isPresent=function()
{
return(TrMessageBox._MESSAGE_BOX)?true:false;
}
function TrMessageBox(a0)
{
if(a0==undefined)
return;
this._messageBoxId=a0;
TrMessageBox._LINK_STYLE=TrPage.getInstance().getStyleClass("OraLink");
TrMessageBox._LIST_STYLE=TrPage.getInstance().getStyleClass("af|messages::list");
TrMessageBox._LIST_SINGLE_STYLE=TrPage.getInstance().getStyleClass("af|messages::list-single");
}
TrMessageBox.prototype.addMessage=function(a1,a2,a3)
{
var a4=this._getMessageList();
var a5=document.createElement("li");
if(a1)
{
if(!a2)
a2=a3.getSummary();
var a6=document.createElement("a");
a6.className=TrMessageBox._LINK_STYLE;
a6.href="#"+a1;
a6.innerHTML=a2;
a5.appendChild(a6);
a5.name=this._getMessageNameForInput(a1);
var a7=document.createTextNode(" - "+a3.getSummary());
a5.appendChild(a7);
}
else
{
var a7=document.createTextNode(a3.getSummary()+" - "+a3.getDetail());
a5.appendChild(a7);
}
a4.appendChild(a5);
if(a4.hasChildNodes())
{
var a8=a4.getElementsByTagName("li");
if(a8.length==1)
a4.className=TrMessageBox._LIST_SINGLE_STYLE;
else
a4.className=TrMessageBox._LIST_STYLE;
}
this._showMessageBox();
}
TrMessageBox.prototype.removeMessages=function(a9)
{
var a10=this._getMessageList();
if(!a10.hasChildNodes())
return;
var a11=this._getMessageNameForInput(a9);
var a12=a10.getElementsByTagName("li");
for(var a13=0;a13<a12.length;)
{
var a14=a12[a13];
if(a14.name&&a14.name==a11)
{
a10.removeChild(a14);
continue;
}
a13++;
}
if(a12.length==0)
this._hideMessageBox();
else if(a12.length==1)
a10.className=TrMessageBox._LIST_SINGLE_STYLE;
else
a10.className=TrMessageBox._LIST_STYLE;
}
TrMessageBox.prototype._getMessageBox=function()
{
if(this._messageBoxId==null)
return null;
return _getElementById(document,this._messageBoxId);
}
TrMessageBox.prototype._getMessageList=function()
{
if(this._messageBoxId==null)
return null;
return _getElementById(document,this._messageBoxId+"__LIST__");
}
TrMessageBox.prototype._showMessageBox=function()
{
var a15=this._getMessageBox();
if(!a15)
return;
a15.style.display="";
}
TrMessageBox.prototype._hideMessageBox=function()
{
var a16=this._getMessageBox();
if(!a16)
return;
a16.style.display="none";
}
TrMessageBox.prototype._getMessageNameForInput=function(a17)
{
if(!this._messageBoxId||!a17)
return null;
return this._messageBoxId+"__"+a17+"__";
}
