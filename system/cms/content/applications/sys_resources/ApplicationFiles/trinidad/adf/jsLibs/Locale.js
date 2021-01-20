var _digits;
var _decimalSep;
var _groupingSep;
function isDigit(
a0
)
{
return(_getDigits()[a0]!=null);
}
function _getDigits()
{
if(_digits==null)
{
var a0=[
0x0030,
0x0660,
0x06F0,
0x0966,
0x09E6,
0x0A66,
0x0AE6,
0x0B66,
0x0BE7,
0x0C66,
0x0CE6,
0x0D66,
0x0E50,
0x0ED0,
0x0F20,
0xFF10
];
_digits=new Object();
for(var a1=0;a1<a0.length;a1++)
{
for(var a2=0;a2<10;a2++)
{
var a3=String.fromCharCode(a0[a1]+a2);
_digits[a3]=a2;
}
}
}
return _digits;
}
function parseDigit(
a0
)
{
var a1=_getDigits()[a0];
if(a1==null)
{
return NaN;
}
else
{
return a1;
}
}
function isNotLowerCase()
{
var a0=alphaChar.charCodeAt(0);
if(a0>0xFF)
{
return true;
}
else
{
return!_isLowerCaseStrict(alphaChar);
}
}
function isLowerCase(
a0
)
{
var a1=a0.charCodeAt(0);
if(a1>0xFF)
{
return!isDigit(a0);
}
else
{
return _isLowerCaseStrict(a0);
}
}
function _isLowerCaseStrict(
a0
)
{
var a1=a0.charCodeAt(0);
return(((a1>=0x61)&&(a1<=0x7A))||
((a1>=0xDF)&&(a1<=0xFF)));
}
function isUpperCase(
a0
)
{
var a1=a0.charCodeAt(0);
if(a1>0xFF)
{
return!isDigit(a0);
}
else
{
return _isUpperCaseStrict(a0);
}
}
function isNotUpperCase(
a0
)
{
var a1=a0.charCodeAt(0);
if(a1>0xFF)
{
return true;
}
else
{
return!_isUpperCaseStrict(a0);
}
}
function _isUpperCaseStrict(
a0
)
{
var a1=a0.charCodeAt(0);
return(((a1>=0x41)&&(a1<=0x5A))||
((a1>=0xC0)&&(a1<=0xDe)));
}
function isLetter(
a0
)
{
return isLowerCase(a0)|isUpperCase(a0);
}
function getUserLanguage()
{
var a0=_locale;
if(a0==null)
{
a0=window.navigator.userLanguage;
if(a0==null)
{
a0=window.navigator.language;
}
}
return a0;
}
function getJavaLanguage(
a0
)
{
if(a0==null)
{
a0=getUserLanguage();
}
var a1=a0.indexOf("-",0);
if(a1==-1)
return a0;
var a2=a0.length;
var a3=a0.substring(0,a1);
a3+="_";
a1++;
var a4=a0.indexOf("-",a1);
if(a4==-1)
{
a4=a2;
}
var a5=a0.substring(a1,
a4);
a3+=a5.toUpperCase();
if(a4!=a2)
{
a3+="_";
a3+=a0.substring(a4+1,
a2);
}
return a3;
}
function getLocaleSymbols(
a0
)
{
var a1=getJavaLanguage(a0);
while(true)
{
var a2=window["LocaleSymbols_"+a1];
if(a2!=null)
{
return a2;
}
else
{
var a3=a1.lastIndexOf("_");
if(a3!=-1)
{
a1=a1.substring(0,a3);
}
else
{
break;
}
}
}
}
function _getEras()
{
return this.getLocaleElements()["Eras"];
}
function _getMonths()
{
return this.getLocaleElements()["MonthNames"];
}
function _getShortMonths()
{
return this.getLocaleElements()["MonthAbbreviations"];
}
function _getWeekdays()
{
return this.getLocaleElements()["DayNames"];
}
function _getShortWeekdays()
{
return this.getLocaleElements()["DayAbbreviations"];
}
function _getAmPmStrings()
{
return this.getLocaleElements()["AmPmMarkers"];
}
function _getZoneStrings()
{
return this.getLocaleElements()["zoneStrings"];
}
function _getLocalPatternChars()
{
return this.getLocaleElements()["localPatternChars"];
}
function _getDecimalSeparator()
{
if(_decimalSep!=null)
return _decimalSep;
return this.getLocaleElements()["NumberElements"][0];
}
function _getGroupingSeparator()
{
if(_groupingSep!=null)
return _groupingSep;
return this.getLocaleElements()["NumberElements"][1];
}
function _getPatternSeparator()
{
return this.getLocaleElements()["NumberElements"][2];
}
function _getPercent()
{
return this.getLocaleElements()["NumberElements"][3];
}
function _getZeroDigit()
{
return this.getLocaleElements()["NumberElements"][4];
}
function _getDigit()
{
return this.getLocaleElements()["NumberElements"][5];
}
function _getMinusSign()
{
return this.getLocaleElements()["NumberElements"][6];
}
function _getExponential()
{
return this.getLocaleElements()["NumberElements"][7];
}
function _getPerMill()
{
return this.getLocaleElements()["NumberElements"][8];
}
function _getInfinity()
{
return this.getLocaleElements()["NumberElements"][9];
}
function _getNaN()
{
return this.getLocaleElements()["NumberElements"][10];
}
function _getCurrencySymbol()
{
return this.getLocaleElements()["CurrencyElements"][0];
}
function _getCurrencyCode()
{
return this.getLocaleElements()["CurrencyElements"][1];
}
function _getPositivePrefix()
{
return this.getLocaleElements()["CurrencyElements"][2];
}
function _getPositiveSuffix()
{
return this.getLocaleElements()["CurrencyElements"][3];
}
function _getNegativePrefix()
{
return this.getLocaleElements()["CurrencyElements"][4];
}
function _getNegativeSuffix()
{
return this.getLocaleElements()["CurrencyElements"][5];
}
function _getLocaleElements()
{
return this["LocaleElements"];
}
function _getFullTimePatternString()
{
return this.getLocaleElements()["DateTimePatterns"][0];
}
function _getLongTimePatternString()
{
return this.getLocaleElements()["DateTimePatterns"][1];
}
function _getMediumTimePatternString()
{
return this.getLocaleElements()["DateTimePatterns"][2];
}
function _getShortTimePatternString()
{
return this.getLocaleElements()["DateTimePatterns"][3];
}
function _getFullDatePatternString()
{
return this.getLocaleElements()["DateTimePatterns"][4];
}
function _getLongDatePatternString()
{
return this.getLocaleElements()["DateTimePatterns"][5];
}
function _getMediumDatePatternString()
{
return this.getLocaleElements()["DateTimePatterns"][6];
}
function _getShortDatePatternString()
{
return this.getLocaleElements()["DateTimePatterns"][7];
}
function _getDateTimeFormatString()
{
return this.getLocaleElements()["DateTimePatterns"][8];
}
function LocaleSymbols(
a0
)
{
this["LocaleElements"]=a0;
}
LocaleSymbols.prototype.getFullTimePatternString=_getFullTimePatternString;
LocaleSymbols.prototype.getLongTimePatternString=_getLongTimePatternString;
LocaleSymbols.prototype.getMediumTimePatternString=_getMediumTimePatternString;
LocaleSymbols.prototype.getShortTimePatternString=_getShortTimePatternString;
LocaleSymbols.prototype.getFullDatePatternString=_getFullDatePatternString;
LocaleSymbols.prototype.getLongDatePatternString=_getLongDatePatternString;
LocaleSymbols.prototype.getMediumDatePatternString=_getMediumDatePatternString;
LocaleSymbols.prototype.getShortDatePatternString=_getShortDatePatternString;
LocaleSymbols.prototype.getDateTimeFormatString=_getDateTimeFormatString;
LocaleSymbols.prototype.getEras=_getEras;
LocaleSymbols.prototype.getMonths=_getMonths;
LocaleSymbols.prototype.getShortMonths=_getShortMonths;
LocaleSymbols.prototype.getWeekdays=_getWeekdays;
LocaleSymbols.prototype.getShortWeekdays=_getShortWeekdays;
LocaleSymbols.prototype.getAmPmStrings=_getAmPmStrings;
LocaleSymbols.prototype.getZoneStrings=_getZoneStrings;
LocaleSymbols.prototype.getLocalPatternChars=_getLocalPatternChars;
LocaleSymbols.prototype.getDecimalSeparator=_getDecimalSeparator;
LocaleSymbols.prototype.getGroupingSeparator=_getGroupingSeparator;
LocaleSymbols.prototype.getPatternSeparator=_getPatternSeparator;
LocaleSymbols.prototype.getPercent=_getPercent;
LocaleSymbols.prototype.getZeroDigit=_getZeroDigit;
LocaleSymbols.prototype.getDigit=_getDigit;
LocaleSymbols.prototype.getMinusSign=_getMinusSign;
LocaleSymbols.prototype.getExponential=_getExponential;
LocaleSymbols.prototype.getPerMill=_getPerMill;
LocaleSymbols.prototype.getInfinity=_getInfinity;
LocaleSymbols.prototype.getNaN=_getNaN;
LocaleSymbols.prototype.getCurrencySymbol=_getCurrencySymbol;
LocaleSymbols.prototype.getCurrencyCode=_getCurrencyCode;
LocaleSymbols.prototype.getPositivePrefix=_getPositivePrefix;
LocaleSymbols.prototype.getPositiveSuffix=_getPositiveSuffix;
LocaleSymbols.prototype.getNegativePrefix=_getNegativePrefix;
LocaleSymbols.prototype.getNegativeSuffix=_getNegativeSuffix;
LocaleSymbols.prototype.getLocaleElements=_getLocaleElements;
function TrConverterHint()
{
this._class="TrConverterHint";
}
TrConverterHint.prototype.getFormatHint=function(){}
function TrValidatorHint()
{
this._class="TrValidatorHint";
}
TrConverterHint.prototype.getHints=function(a0){}
function TrConverter()
{
this._class="TrConverter";
}
TrConverter.prototype.getAsString=function(a0,a1){}
TrConverter.prototype.getAsObject=function(a2,a3){}
function TrValidator()
{
this._class="TrValidator";
}
TrValidator.prototype.validate=function(a0,a1,a2){}
function TrConverterException(
a0,
a1,
a2
)
{
if(a0==null)
{
this._facesMessage=new TrFacesMessage(a1,
a2,
TrFacesMessage.SEVERITY_ERROR);
}
else
{
this._facesMessage=a0;
}
}
TrConverterException.prototype.getFacesMessage=
function()
{
return this._facesMessage;
}
function TrValidatorException(
a0,
a1,
a2
)
{
if(a0==null)
{
this._facesMessage=new TrFacesMessage(a1,
a2,
TrFacesMessage.SEVERITY_ERROR);
}
else
{
this._facesMessage=a0;
}
}
TrValidatorException.prototype.getFacesMessage=
function()
{
return this._facesMessage;
}
function TrFacesMessage(
a0,
a1,
a2
)
{
this._summary=a0;
this._detail=a1;
if(a2==null)
{
this._severity=TrFacesMessage.SEVERITY_INFO;
}
else
{
this._severity=a2;
}
}
TrFacesMessage.SEVERITY_INFO=0;
TrFacesMessage.SEVERITY_WARN=1;
TrFacesMessage.SEVERITY_ERROR=2;
TrFacesMessage.SEVERITY_FATAL=3;
TrFacesMessage._SEVERITY_DEFAULT=TrFacesMessage.SEVERITY_INFO;
TrFacesMessage.prototype.getDetail=
function()
{
return this._detail;
}
TrFacesMessage.prototype.getSummary=
function()
{
return this._summary;
}
TrFacesMessage.prototype.setDetail=
function(
a3
)
{
this._detail=a3;
}
TrFacesMessage.prototype.setSummary=
function(
a4
)
{
this._summary=a4;
}
TrFacesMessage.prototype.getSeverity=
function()
{
return this._severity;
}
TrFacesMessage.prototype.setSeverity=
function(
a5
)
{
this._severity=a5;
}
var TrFastMessageFormatUtils=new Object();
TrFastMessageFormatUtils.format=function(
a6,
a7
)
{
var a8=new Array();
for(var a9=1;a9<arguments.length;a9++)
{
a8[a9-1]=arguments[a9];
}
return _formatErrorString(a6,a8);
}
var TrMessageFactory=new Object();
TrMessageFactory.createFacesMessage=function(
a10,
a11,
a12,
a13
)
{
var a14=TrMessageFactory.getSummaryString(a10);
var a15=a11;
var a16=a13;
if(a16==null)
{
a16=TrFacesMessage.SEVERITY_ERROR
}
if(a15==null)
{
a15=TrMessageFactory.getDetailString(a10);
}
if(a15!=null)
{
if(a12!=null)
{
a15=TrFastMessageFormatUtils.format(a15,a12);
}
}
return new TrFacesMessage(a14,a15,a16);
}
TrMessageFactory.getSummaryString=function(
a17
)
{
if(a17==null)
return null;
return TrMessageFactory._TRANSLATIONS[a17];
}
TrMessageFactory.getDetailString=function(
a18
)
{
if(a18==null)
return null;
return TrMessageFactory._TRANSLATIONS[a18+"_detail"];
}
TrMessageFactory.getString=function(
a19
)
{
return TrMessageFactory.getSummaryString(a19);
}
TrMessageFactory.createMessage=function(
a20,
a21,
a22
)
{
var a23=TrMessageFactory.getSummaryString(a20);
if(a23!=null)
{
a23=TrFastMessageFormatUtils.format(a23,a21,a22);
}
return a23;
}
TrMessageFactory.createCustomMessage=function(
a24,
a25,
a26
)
{
var a27;
if(a24!=null)
{
a27=TrFastMessageFormatUtils.format(a24,a25,a26);
}
return a27;
}
