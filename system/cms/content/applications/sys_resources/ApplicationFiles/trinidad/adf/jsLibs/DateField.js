function _dfsv(
a0,
a1
)
{
if((a0==(void 0))||(a1==(void 0)))
return;
a1+=_getDayLightSavOffset(a1);
a1+=_getTimePortion(a0);
a1+=_getLocaleTimeZoneDifference();
var a2=new Date(a1);
var a3=_getDateFieldFormat(a0);
var a4=a0.value;
var a1=a3.getAsString(a2);
if(a0.value!=a1)
{
if(a0.onchange!=(void 0))
{
if(_agent.isIE)
{
a0.onpropertychange=function()
{
var a5=window.event;
if(a5.propertyName=='value')
{
a0.onpropertychange=function(){};
a0.onchange(a5);
}
}
a0.value=a1;
}
else
{
a0.value=a1;
var a5=new Object();
a5.type='change';
a5.target=a0;
a0.onchange(a5);
}
}
else
{
a0.value=a1;
}
}
a0.select();
a0.focus();
}
function _getDayLightSavOffset(a0)
{
var a1=new Date();
var a2=new Date(a0);
var a3=a2.getTimezoneOffset()-a1.getTimezoneOffset();
return(a3*60*1000);
}
function _returnCalendarValue(
a0,
a1
)
{
var a2=a0.returnValue;
if(a2!=(void 0))
{
var a3=a0._dateField;
if(a3==(void 0))
{
a3=_savedField1879034;
}
_dfsv(a3,a2);
}
}
function _returnPopupCalendarValue(
a0,
a1
)
{
if(a1!=(void 0))
{
var a2=a0['formName'];
var a3=a0['fieldName'];
var a4=document.forms[a2][a3];
_dfsv(a4,a1);
}
}
function _ldp(
a0,
a1,
a2,
a3,
a4,
a5
)
{
var a6=document.forms[a0][a1];
var a7=_dfgv(a6);
if(!a7)
{
a7=new Date();
}
if(!a5)
{
a5=_jspDir+_getQuerySeparator(_jspDir);
if(a2)
a5+="_t=cd";
else
a5+="_t=fred&_red=cd";
}
else
{
var a8=a5.lastIndexOf('?');
var a9="";
if(a8==-1)
{
a8=a5.length;
}
else
{
a9=a5.substr(a8+1);
}
var a10=a5.lastIndexOf('/',a8);
var a11=a5.substring(0,a10+1);
a11+=_jspDir+_getQuerySeparator(_jspDir);
a11+=a9;
a11+=_getQuerySeparator(a11);
a11+="_t=fred";
var a12=a5.substring(a10+1,a8);
a5=a11;
a5+="&redirect="+escape(a12);
}
a5+="&value="+a7.getTime();
a5+="&loc="+_locale;
if(window["_enc"])
{
a5+="&enc="+_enc;
}
if(a3!=(void 0))
{
a5+="&minValue="+a3;
}
if(a4!=(void 0))
{
a5+="&maxValue="+a4;
}
if(a2)
{
TrPopupDialog._launchDialog(
a5,
{},
_returnPopupCalendarValue,
{'formName':a0,'fieldName':a1});
}
else
{
var a13=openWindow(self,
a5,
'uix_2807778',
{width:350,height:370},
true,
void 0,
_returnCalendarValue);
a13._dateField=a6;
_savedField1879034=a6;
}
}
function _dfgv(a0)
{
if(a0.value!="")
{
try{
var a1=_getDateFieldFormat(a0).getAsObject(a0.value);
return a1;
}
catch(e)
{
}
}
return null;
}
function _getTimePortion(a0)
{
var a1=_dfgv(a0);
if(!a1)
a1=new Date();
var a2=new Date(a1.getFullYear(),
a1.getMonth(),
a1.getDate());
return a1-a2;
}
function _getLocaleTimeZoneDifference()
{
var a0=new Date();
var a1=a0.getTimezoneOffset()*-1;
var a2=0;
if(_uixLocaleTZ)
a2=(_uixLocaleTZ-a1)*60*1000;
return a2;
}
function _dfb(a0,a1)
{
_fixDFF(a0);
}
function _dff(a0,a1)
{
_dfa(a0,a1);
}
function _dfa(a0,a1)
{
if(a1!=(void 0))
{
if(window._calActiveDateFields==(void 0))
window._calActiveDateFields=new Object();
if(typeof(a0)=="string")
{
a0=_getElementById(document,a0);
}
window._calActiveDateFields[a1]=a0;
}
}
function _calsd(a0,a1)
{
if(window._calActiveDateFields!=(void 0))
{
var a2=window._calActiveDateFields[a0];
if(a2)
_dfsv(a2,a1);
}
return false;
}
function _updateCal(a0,a1,a2)
{
a1+=('&scrolledValue='+a0.options[a0.selectedIndex].value);
if(a2)
_firePartialChange(a1);
else
document.location.href=a1;
}
function _doCancel()
{
var a0=parent.TrPopupDialog.getInstance();
if(a0)
{
a0.returnValue=(void 0);
parent.TrPopupDialog._returnFromDialog();
}
else
{
top.returnValue=(void 0);
top.close();
}
return false;
}
function _selectDate(a0)
{
var a1=parent.TrPopupDialog.getInstance();
if(a1)
{
a1.returnValue=a0;
parent.TrPopupDialog._returnFromDialog();
}
else
{
top.returnValue=a0;
top._unloadADFDialog(window.event);
top.close();
}
return false;
}
var _DATE_DIALOG;
var _savedField1879034;
