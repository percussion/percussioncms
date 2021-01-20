var _AD_ERA=null;
function _getADEra()
{
if(_AD_ERA==null)
{
_AD_ERA=new Date(0);
_AD_ERA.setFullYear(1);
}
return _AD_ERA;
}
function _isStrict(
a0,
a1)
{
var a2=["FullYear","Month","Date","Hours","Minutes",
"Seconds","Milliseconds"];
for(var a3=0;a3<a2.length;a3++)
{
var a4="parsed"+a2[a3];
if(a0[a4]!=null&&
a0[a4]!=a1["get"+a2[a3]]())
{
return false;
}
}
return true;
}
function _doClumping(
a0,
a1,
a2,
a3,
a4
)
{
var a5=a0.length;
var a6=false;
var a7=0;
var a8=void 0;
var a9=0;
for(var a10=0;a10<a5;a10++)
{
var a11=a0.charAt(a10);
if(a6)
{
if(a11=="\'")
{
a6=false;
if(a7!=1)
{
a9++;
a7--;
}
if(!a2(a0,
a1,
"\'",
a9,
a7,
a3,
a4))
{
return false;
}
a7=0;
a8=void 0;
}
else
{
a7++;
}
}
else
{
if(a11!=a8)
{
if(a7!=0)
{
if(!a2(a0,
a1,
a8,
a9,
a7,
a3,
a4))
{
return false;
}
a7=0;
a8=void 0;
}
if(a11=='\'')
{
a6=true;
}
a9=a10;
a8=a11;
}
a7++;
}
}
if(a7!=0)
{
if(!a2(a0,
a1,
a8,
a9,
a7,
a3,
a4))
{
return false;
}
}
return true;
}
function _subformat(
a0,
a1,
a2,
a3,
a4,
a5,
a6
)
{
var a7=null;
var a8=false;
if((a2>='A')&&(a2<='Z')||
(a2>='a')&&(a2<='z'))
{
switch(a2)
{
case'D':
a7="(Day in Year)";
break;
case'E':
{
var a9=a5.getDay();
a7=(a4<=3)
?a1.getShortWeekdays()[a9]
:a1.getWeekdays()[a9];
}
break;
case'F':
a7="(Day of week in month)";
break;
case'G':
{
var a10=a1.getEras();
a7=(a5.getTime()<_getADEra().getTime())
?a10[0]
:a10[1];
}
break;
case'M':
{
var a11=a5.getMonth();
if(a4<=2)
{
a7=_getPaddedNumber(a11+1,a4);
}
else if(a4==3)
{
a7=a1.getShortMonths()[a11];
}
else
{
a7=a1.getMonths()[a11];
}
}
break;
case'S':
a7=_getPaddedNumber(a5.getMilliseconds(),a4);
break;
case'W':
a7="(Week in Month)";
break;
case'a':
{
var a12=a1.getAmPmStrings();
a7=(_isPM(a5.getHours()))
?a12[1]
:a12[0];
}
break;
case'd':
a7=_getPaddedNumber(a5.getDate(),a4);
break;
case'h':
hours=a5.getHours();
if(_isPM(hours))
hours-=12;
if(hours==0)
hours=12;
a7=_getPaddedNumber(hours,a4);
break;
case'K':
hours=a5.getHours();
if(_isPM(hours))
hours-=12;
a7=_getPaddedNumber(hours,a4);
break;
case'k':
hours=a5.getHours();
if(hours==0)
hours=24;
a7=_getPaddedNumber(hours,a4);
break;
case'H':
a7=_getPaddedNumber(a5.getHours(),a4);
break;
case'm':
a7=_getPaddedNumber(a5.getMinutes(),a4);
break;
case's':
a7=_getPaddedNumber(a5.getSeconds(),a4);
break;
case'w':
a7="(Week in year)";
break;
case'y':
{
var a13=a5.getFullYear();
var a14=(a4<=2)
?a4
:null;
a7=_getPaddedNumber(a13,a4,a14);
}
break;
case'z':
{
a7="GMT";
var a15=_getTimeZoneOffsetString(a5,false);
if(a15)
{
a7+=a15[0];
a7+=":"
a7+=a15[1];
}
}
break;
case'Z':
{
var a15=_getTimeZoneOffsetString(a5,true);
if(a15)
{
a7=a15[0];
a7+=a15[1];
}
else
{
a7="";
}
}
break;
default:
a7="";
}
}
else
{
a7=a0.substring(a3,a3+a4);
}
a6.value+=a7;
return true;
}
function _getTimeZoneOffsetString(a0,a1)
{
var a2=-1*a0.getTimezoneOffset();
a2+=_getLocaleTimeZoneDifference();
if(a1||a2!=0)
{
var a3=new Array(2);
if(a2<0)
{
a3[0]="-";
a2=-a2
}
else
{
a3[0]="+";
}
a3[0]+=_getPaddedNumber(Math.floor(a2/60),2);
a3[1]=_getPaddedNumber(a2%60,2);
return a3;
}
}
function _getLocaleTimeZoneDifference()
{
var a0=new Date();
var a1=a0.getTimezoneOffset()*-1;
var a2=0;
return a2-a1;
}
function _subparse(
a0,
a1,
a2,
a3,
a4,
a5,
a6
)
{
var a7=a5.currIndex;
if((a2>='A')&&(a2<='Z')||
(a2>='a')&&(a2<='z'))
{
switch(a2)
{
case'D':
if(_accumulateNumber(a5,3)==null)
{
return false;
}
break;
case'E':
{
var a8=_matchArray(a5,
(a4<=3)
?a1.getShortWeekdays()
:a1.getWeekdays());
if(a8==null)
{
return false;
}
}
break;
case'F':
if(_accumulateNumber(a5,2)==null)
{
return false;
}
break;
case'G':
{
var a9=_matchArray(a5,a1.getEras());
if(a9!=null)
{
if(a9==0)
{
a5.isBC=true;
}
}
else
{
return false;
}
}
break;
case'M':
{
var a10;
var a11=0;
if(a4<=2)
{
a10=_accumulateNumber(a5,2);
a11=-1;
}
else
{
var a12=(a4==3)
?a1.getShortMonths()
:a1.getMonths();
a10=_matchArray(a5,a12);
}
if(a10!=null)
{
a5.parsedMonth=(a10+a11);
}
else
{
return false;
}
}
break;
case'S':
{
var a13=_accumulateNumber(a5,3);
if(a13!=null)
{
a5.parsedMilliseconds=a13;
}
else
{
return false;
}
}
break;
case'W':
if(_accumulateNumber(a5,2)==null)
{
return false;
}
break;
case'a':
{
var a14=_matchArray(a5,
a1.getAmPmStrings());
if(a14==null)
{
return false;
}
else
{
if(a14==1)
{
a5.isPM=true;
}
}
}
break;
case'd':
{
var a15=_accumulateNumber(a5,2);
if(a15!=null)
{
a5.parsedDate=a15;
}
else
{
return false;
}
}
break;
case'h':
case'k':
case'H':
case'K':
{
var a16=_accumulateNumber(a5,2);
if(a16!=null)
{
if((a2=='h')&&(a16==12))
a16=0;
if((a2=='k')&&(a16==24))
a16=0;
a5.parsedHour=a16;
}
else
{
return false;
}
}
break;
case'm':
{
var a17=_accumulateNumber(a5,2);
if(a17!=null)
{
a5.parsedMinutes=a17;
}
else
{
return false;
}
}
break;
case's':
{
var a18=_accumulateNumber(a5,2);
if(a18!=null)
{
a5.parsedSeconds=a18;
}
else
{
return false;
}
}
break;
case'w':
if(_accumulateNumber(a5,2)==null)
{
return false;
}
break;
case'y':
{
var a19=_accumulateNumber(a5,4);
var a20=a5.currIndex-a7;
if(a19!=null)
{
if((a20>2)&&
(a4<=2)&&
(a19<=999))
{
return false;
}
else if((a4<=2)&&(a19>=0)&&(a19<=100))
{
a19=_fix2DYear(a19);
}
else if(a4==4)
{
if(a20==3)
return false;
if(a20<=2)
a19=_fix2DYear(a19);
}
if(a19==0)
return false;
a5.parsedFullYear=a19;
}
else
{
return false;
}
}
break;
case'z':
{
if(!_matchText(a5,"GMT"))
{
return false;
}
if((a5.parseString.length-a5.currIndex)>0)
{
if(_matchArray(a5,["-","+"])==null)
{
return false;
}
if(_accumulateNumber(a5,2)==null)
{
return false;
}
if(!_matchText(a5,":"))
{
return false;
}
if(((a5.parseString.length-a5.currIndex)<2)||
_accumulateNumber(a5,2)==null)
{
return false;
}
}
}
break;
case'Z':
{
if((a5.parseString.length-a5.currIndex)<5)
{
return false;
}
if(_matchArray(a5,["-","+"])==null)
{
return false;
}
if(_accumulateNumber(a5,2)==null)
{
return false;
}
if(_accumulateNumber(a5,2)==null)
{
return false;
}
}
break;
default:
}
}
else
{
return _matchText(a5,
a0.substring(a3,a3+a4));
}
return true;
}
function _fix2DYear(a0)
{
var a1;
if(_df2DYS!=null)
{
var a2=_df2DYS;
a1=a2-(a2%100);
a0+=a1;
if(a0<a2)
a0+=100;
}
else
{
var a3=new Date().getFullYear();
a1=a3-(a3%100)-100;
a0+=a1;
if(a0+80<a3)
{
a0+=100;
}
}
return a0;
}
function _matchArray(
a0,
a1
)
{
for(var a2=0;a2<a1.length;a2++)
{
if(_matchText(a0,a1[a2]))
{
return a2;
}
}
return null;
}
function _matchText(
a0,
a1
)
{
if(!a1)
return false;
var a2=a1.length;
var a3=a0.currIndex;
var a4=a0.parseString;
if(a2>a4.length-a3)
{
return false;
}
var a5=a4.substring(a3,a3+a2);
var a6=a5.toLowerCase();
var a7=a1.toLowerCase();
if(a6!=a7)
return false;
a0.currIndex+=a2;
return true;
}
function _accumulateNumber(
a0,
a1
)
{
var a2=a0.currIndex;
var a3=a2;
var a4=a0.parseString;
var a5=a4.length;
if(a5>a3+a1)
a5=a3+a1;
var a6=0;
while(a3<a5)
{
var a7=parseDigit(a4.charAt(a3));
if(!isNaN(a7))
{
a6*=10;
a6+=a7;
a3++;
}
else
{
break;
}
}
if(a2!=a3)
{
a0.currIndex=a3;
return a6;
}
else
{
return null;
}
}
function _isPM(
a0
)
{
return(a0>=12);
}
function _getPaddedNumber(
a0,
a1,
a2
)
{
var a3=a0.toString();
if(a1!=null)
{
var a4=a1-a3.length;
while(a4>0)
{
a3="0"+a3;
a4--;
}
}
if(a2!=null)
{
var a5=a3.length-a2;
if(a5>0)
{
a3=a3.substring(a5,
a5+a2);
}
}
return a3;
}
function TrDateTimeConverter(
a0,
a1,
a2,
a3,
a4
)
{
this._class="TrDateTimeConverter";
this._exampleString=a2;
this._type=a3;
this._messages=a4;
this._offset=null;
this._localeSymbols=getLocaleSymbols(a1);
if(a0==null)
a0=this._localeSymbols.getShortDatePatternString();
var a5=this._initPatterns(a0);
this._pattern=a5;
}
TrDateTimeConverter.prototype=new TrConverter();
TrDateTimeConverter.prototype.getFormatHint=function()
{
if(this._messages&&this._messages["hint"])
{
return TrMessageFactory.createCustomMessage(
this._messages["hint"],
""+this._exampleString);
}
else
{
var a6="org.apache.myfaces.trinidad.convert.DateTimeConverter."+this._type+"_HINT";
return TrMessageFactory.createMessage(
a6,
""+this._exampleString);
}
}
TrDateTimeConverter.prototype.getAsString=function(
a7
)
{
if(this._offset)
{
var a8=a7.getMinutes();
a7.setMinutes((+a8)-parseInt(this._offset));
}
var a9=new Object();
a9.value="";
var a10=this._pattern;
if(typeof a10!="string")
a10=a10[0];
_doClumping(a10,
this._localeSymbols,
_subformat,
a7,
a9);
if(this._offset)
{
var a11=(((this._offset+a7.getTimezoneOffset())*-1)/60);
if(parseInt(a11)>0)
{
a9.value=a9.value+"+"
}
a9.value=a9.value+a11+":00";
}
return a9.value;
}
TrDateTimeConverter.prototype.setDiffInMins=function(
a12
)
{
this._offset=a12;
}
TrDateTimeConverter.prototype.getDiffInMins=function()
{
return this._offset;
}
TrDateTimeConverter.prototype.getLocaleSymbols=function()
{
return this._localeSymbols;
}
TrDateTimeConverter.prototype.getAsObject=function(
a13,
a14
)
{
if(a13==null)
return null;
a13=TrUIUtils.trim(a13);
if(a13.length==0)
return null
var a15=this._pattern;
var a16;
var a17="org.apache.myfaces.trinidad.convert.DateTimeConverter.CONVERT_"+this._type;
if(this._messages&&this._messages["detail"])
{
a16=_createCustomFacesMessage(TrMessageFactory.getSummaryString(a17),
this._messages["detail"],
a14,
a13,
this._exampleString);
}
else
{
a16=_createFacesMessage(a17,
a14,
a13,
this._exampleString);
}
if(typeof a15=="string")
{
return this._simpleDateParseImpl(a13,
a15,
this._localeSymbols,
a16);
}
else
{
var a18;
for(a18=0;a18<a15.length;a18++)
{
try{
var a19=this._simpleDateParseImpl(a13,
a15[a18],
this._localeSymbols,
a16);
return a19;
}
catch(e)
{
if(a18==a15.length-1)
throw e;
}
}
}
}
TrDateTimeConverter.prototype._initPatterns=function(
a20)
{
var a21=new Array();
if(a20)
a21=a21.concat(a20);
var a22=a21.length;
for(var a23=0;a23<a22;a23++)
{
if(a21[a23].indexOf('MMM')!=-1)
{
a21[a21.length]=a21[a23].replace(/MMM/g,'MM');
a21[a21.length]=a21[a23].replace(/MMM/g,'M');
}
}
var a22=a21.length;
for(var a23=0;a23<a22;a23++)
{
if(a21[a23].indexOf('/')!=-1)
{
a21[a21.length]=a21[a23].replace(/\//g,'-');
a21[a21.length]=a21[a23].replace(/\//g,'.');
}
if(a21[a23].indexOf('-')!=-1)
{
a21[a21.length]=a21[a23].replace(/-/g,'/');
a21[a21.length]=a21[a23].replace(/-/g,'.');
}
if(a21[a23].indexOf('.')!=-1)
{
a21[a21.length]=a21[a23].replace(/\./g,'-');
a21[a21.length]=a21[a23].replace(/\./g,'/');
}
}
return a21;
}
TrDateTimeConverter.prototype._simpleDateParseImpl=function(
a24,
a25,
a26,
a27)
{
var a28=new Object();
a28.currIndex=0;
a28.parseString=a24;
a28.parsedHour=null;
a28.parsedMinutes=null;
a28.parsedSeconds=null;
a28.parsedMilliseconds=null;
a28.isPM=false;
a28.parsedBC=false;
a28.parsedFullYear=null;
a28.parsedMonth=null;
a28.parsedDate=null;
a28.parseException=new TrConverterException(a27);
var a29=new Date(0);
a29.setDate(1);
if(_doClumping(a25,
a26,
_subparse,
a28,
a29))
{
if(a24.length!=a28.currIndex)
{
throw a28.parseException;
}
var a30=a28.parsedFullYear;
if(a30!=null)
{
if(a28.parsedBC)
{
a30=_getADEra().getFullYear()-a30;
}
a29.setFullYear(a30);
a28.parsedFullYear=a30;
}
var a31=a28.parsedMonth;
if(a31!=null)
a29.setMonth(a31);
var a32=a28.parsedDate;
if(a32!=null)
a29.setDate(a32);
var a33=a28.parsedHour;
if(a33!=null)
{
if(a28.isPM&&(a33<12))
{
a33+=12;
}
a29.setHours(a33);
a28.parsedHour=a33;
}
var a34=a28.parsedMinutes;
if(a34!=null)
a29.setMinutes(a34);
var a35=a28.parsedSeconds;
if(a35!=null)
a29.setSeconds(a35);
var a36=a28.parsedMilliseconds;
if(a36!=null)
a29.setMilliseconds(a36);
if(!_isStrict(a28,a29))
{
throw a28.parseException;
}
if(this._offset)
{
var a37=a29.getMinutes();
a29.setMinutes((+a37)+parseInt(this._offset));
}
return a29;
}
else
{
throw a28.parseException;
}
}
