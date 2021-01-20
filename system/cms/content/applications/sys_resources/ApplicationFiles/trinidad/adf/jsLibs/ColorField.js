var _cfBus=new Object();
var _cfTransIconURL;
var _cfOpaqueIconURL;
var _cfBgColor;
function _cfsw(
a0)
{
var a1=_getColorFieldFormat(a0);
var a2=a0.name+"$sw";
var a3=null;
var a4=_getElementById(document,a2);
if(a4!=null)
{
var a5=false;
if(a0.value!="")
{
try
{
a3=a1.getAsObject(a0.value);
}
catch(e)
{
}
}
if(a3!=null)
{
if(a3.alpha==0)
{
a4.style.backgroundColor=null;
a4.src=_cfTransIconURL;
a4.alt=_cfTrans;
}
else
{
a4.style.backgroundColor=
new TrColorConverter("#RRGGBB").getAsString(a3);
a4.src=_cfOpaqueIconURL;
a4.alt=a1.getAsString(a3);
}
}
else
{
a4.style.backgroundColor=_cfBgColor;
a4.src=_cfOpaqueIconURL;
a4.alt=null;
}
if(_agent.isGecko)
a4.title=a4.alt;
}
}
function _returnColorPickerValue(
a0,
a1
)
{
var a2=a0.returnValue;
var a3=a0._colorField;
if(a3==null)
{
a3=_savedColorField1879034;
}
if(a0.isApplicable)
_cfUpdate(a3,a2);
}
function _cfbs(
a0)
{
_cfUpdate(_cfBus[a0.source],a0.params.value);
}
function _cfUpdate(
a0,
a1)
{
if(a0!=null)
{
var a2=_getColorFieldFormat(a0);
var a3=(a0.type!='hidden');
var a4=a0.value;
var a5=a2.getAsString(a1);
if(a5==_cfTrans&&!a2._allowsTransparent)
return;
if(a5==null)
a5="";
if(a5!=a0.value)
{
if(a0.onchange!=null)
{
if(_agent.isIE)
{
a0.onpropertychange=function()
{
var a6=window.event;
if(a6.propertyName=='value')
{
a0.onpropertychange=function(){};
_cfsw(a0);
a0.onchange(a6);
}
}
a0.value=a5;
}
else
{
a0.value=a5;
if(!_agent.isNav)
_cfsw(a0);
var a6=new Object();
a6.type='change';
a6.target=a0;
a0.onchange(a6);
}
}
else
{
a0.value=a5;
if(!_agent.isNav)
_cfsw(a0);
}
}
if(a3)
{
a0.select();
a0.focus();
}
}
}
function _lcp(
a0,
a1,
a2
)
{
var a3=document.forms[a0][a1];
if(!a2)
{
a2=_jspDir+_getQuerySeparator(_jspDir)+"_t=fred&_red=cp";
}
else
{
var a4=a2.lastIndexOf('?');
var a5="";
if(a4==-1)
{
a4=a2.length;
}
else
{
a5=a2.substr(a4+1);
}
var a6=_jspDir+_getQuerySeparator(_jspDir);
a6+=a5;
a6+=_getQuerySeparator(a6);
a6+="_t=fred";
var a7=a2.substring(0,a4);
a2=a6;
a2+="&redirect="+escape(a7);
}
var a8=_cfs[a1];
var a9="#RRGGBB"
if(a8!=null)
{
a2+="&pattern=";
if(typeof a8=="string")
{
a9=a8;
a2+=escape(a9);
}
else
{
a9=a8[0];
a2+=escape(a8.join(" "));
}
}
if(a3.value!="")
{
var a10=_getColorFieldFormat(a3);
try
{
var a11=a10.getAsObject(a3.value);
if(a11!=null)
{
a2+="&value=";
if(a11.alpha==0)
a2+=escape(_cfTrans);
else
a2+=escape(new TrColorConverter(a9).getAsString(a11));
}
}
catch(e)
{
}
}
var a12=_cfts[a1];
if(a12!=null)
{
a2+="&allowsTransparent="+a12;
}
a2+="&loc="+_locale;
if(window["_enc"])
{
a2+="&enc="+_enc;
}
var a13=openWindow(self,
a2,
'colorDialog',
{width:430,height:230},
true,
null,
_returnColorPickerValue);
a13._colorField=a3;
_savedColorField1879034=a3;
}
var _savedColorField1879034;
