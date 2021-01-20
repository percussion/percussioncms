var ADFDialogReturn=new Array();
function _launchDialog(
a0,
a1,
a2,
a3,
a4,
a5)
{
var a6=self;
var a7;
if(a5)
{
a7=function()
{
a6._submitPartialChange(a3,0,{rtrn:a4});
};
}
else
{
a7=function()
{
a6.submitForm(a3,0,{rtrn:a4});
};
}
var a8=ADFDialogReturn.length;
ADFDialogReturn[a8]=a7;
a0=a0+"&_rtrnId="+a8;
openWindow(window,a0,a1,a2,1);
}
function openWindow(
a0,
a1,
a2,
a3,
a4,
a5,
a6
)
{
if(a0)
{
if(a4==(void 0))
a4=false;
if(!a5)
{
a5=(a4)?"dialog":"document";
}
if(!a2)
a2="_blank";
var a7=_featureDefaults[a5];
if(a7==(void 0))
{
a5="document";
a7=_featureDefaults[a5];
}
var a8=(a4)
?_modalFeatureOverrides
:_modelessFeatureOverrides;
var a9=(_agent.isIE)
?_ieFeatures
:_nnFeatures;
var a10=null;
if(a3)
{
a10=new Object();
for(var a11 in a3)
{
a10[a11]=a3[a11];
}
}
var a12="";
for(var a13 in a9)
{
var a14=a8[a13];
if(a14==(void 0))
{
if(a10)
{
a14=a10[a13];
delete a10[a13];
}
if(a14==(void 0))
a14=a7[a13];
}
if(a14!=(void 0))
{
var a15=_booleanFeatures[a13]!=(void 0);
if(a14||!a15)
{
a12+=a13;
if(!a15)
{
a12+="="+a14;
}
a12+=",";
}
}
}
for(var a11 in a10)
{
a12+=a11;
if(a10[a11])
a12+="="+a10[a11];
a12+=",";
}
if(a12.length!=0)
{
a12=a12.substring(0,a12.length-1);
}
if(a6)
{
_setDependent(a0,a2,a6);
}
var a16=a0.open(a1,a2,a12);
var a17=false;
if(a16!=null)
{
var a18=0;
try
{
for(p in a16)
{
a18++;
break;
}
if(a18>0)
a17=true;
}
catch(e)
{
}
}
if(!a17)
{
_setDependent(a0,a2,(void 0));
if(_AdfWindowOpenError!=null)
alert(_AdfWindowOpenError);
return;
}
var a19=_agent.atMost("ie",4.99);
var a20=false;
var a21=a0.document.body;
if(a4&&!a19)
{
if(_agent.atLeast("ie",4))
{
a21.style.filter="alpha(opacity=50)";
a20=true;
}
if(_agent.isGecko)
{
if(a21!=(void 0))
_addModalCaptureGecko(a21);
}
a0.onfocus=_onModalFocus;
}
if(a4&&(_agent.atLeast("ie",5)&&_agent.isWindows))
{
_addModalCaptureIE(a21);
a21.onlosecapture=_onModalLoseCapture;
var a22=(a1!=null&&a1.indexOf(':')!=-1);
if(!a22)
{
var a23=new Function("e","_removeModalCaptureIE(window.document.body)");
a16.attachEvent("onunload",a23);
}
}
if(a4&&!a19)
{
_setDependent(a0,"modalWindow",a16);
}
if(a4&&self._pollManager)
{
_pollManager.deactivateAll();
_pollWhenModalDependentCloses();
}
a16.focus();
if(a20)
{
a0.setTimeout("_clearBodyModalEffects('alpha')",1000);
}
return a16;
}
else
{
return null;
}
}
function _pollWhenModalDependentCloses()
{
if(!_getValidModalDependent(self))
{
_pollManager.reactivateAll();
}
else
{
self.setTimeout("_pollWhenModalDependentCloses()",1000);
}
}
function _onModalLoseCapture()
{
var a0=_getValidModalDependent(self);
if(a0)
{
window.setTimeout("_onModalFocus()",1);
}
}
function _onModalFocus()
{
var a0=self.document.body;
var a1=_getModalDependent(self);
var a2=_agent.atLeast("ie",5)&&_agent.isWindows;
if(a1&&!a1.closed)
{
a1.focus();
if(a2)
{
a0.setCapture();
}
}
else
{
if(a2)
{
a0.onlosecapture=null;
_removeModalCaptureIE(a0);
}
}
}
function _clearBodyModalEffects(a0)
{
if(_getValidModalDependent(self)!=null)
{
self.setTimeout("_clearBodyModalEffects('"+a0+"')",1000);
}
else
{
if(a0=='alpha')
{
self.document.body.style.filter=null;
}
}
}
function _getValidModalDependent(
a0
)
{
var a1=_getModalDependent(a0);
if(a1)
{
if(a1.closed)
{
_setDependent(a0,"modalWindow",(void 0));
a1=(void 0);
}
}
return a1;
}
function _sizeWin(
a0,
a1,
a2,
a3
)
{
var a4=_agent.isGecko;
var a5=_agent.isIE;
var a6=_agent.isSafari;
var a7=(a4||a6);
if(!(a7||(a5&&_agent.isWindows)))
return;
var a8=a0.document.body;
if(a8)
{
var a9=(!a5&&(a8.scrollWidth>a8.clientWidth))
?a8.scrollWidth
:_getBodyWidth(a8,a8.offsetWidth,a8.offsetLeft);
var a10=0;
if(a7)
{
a10=a8.offsetHeight+(window.outerHeight-window.innerHeight);
a10+=30;
if(window.outerWidth>a8.offsetWidth)
a9+=(window.outerWidth-a8.offsetWidth);
}
else
{
a10=a8.scrollHeight+(a8.offsetHeight-a8.clientHeight);
a10+=21;
a9+=a8.offsetWidth-a8.clientWidth+16;
a10+=parseInt(a8.topMargin)+parseInt(a8.bottomMargin);
a9+=parseInt(a8.leftMargin)+parseInt(a8.rightMargin);
}
if(a1)
a9+=a1;
if(a2)
a10+=a2;
if(a3!=(void 0))
{
if(a3['W'])
{
var a11=0+a3['W'];
if(a9<a11)
a9=a11;
}
if(a3['H'])
{
var a12=0+a3['H'];
if(a10<a12)
a10=a12;
}
}
var a13=_getTop(a0);
var a14=a5?0:a13.screen.availLeft;
var a15=a5?0:a13.screen.availTop;
var a16=a13.screen.availHeight*0.95;
var a17=a13.screen.availWidth*0.95;
if(a10>a16)
a10=a16;
if(a9>a17)
a9=a17;
a13.resizeTo(a9,a10);
var a18=a5?a13.screenLeft:a13.screenX;
var a19=a5?a13.screenTop:a13.screenY;
var a20=false;
if((a18+a9)>(a14+a17))
{
a18=(a13.screen.availWidth-a9)/2;
a20=true;
}
if((a19+a10)>(a15+a16))
{
a19=(a13.screen.availHeight-a10)/2;
a20=true;
}
if(a20)
{
a13.moveTo(a18,a19);
}
}
}
