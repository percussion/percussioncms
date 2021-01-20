function TrIntegerConverter(
a0,
a1,
a2,
a3,
a4)
{
this._message=a0;
this._maxPrecision=a1;
this._maxScale=a2;
this._maxValue=a3;
this._minValue=a4;
this._class="TrIntegerConverter";
}
TrIntegerConverter.prototype=new TrConverter();
TrIntegerConverter.prototype.getFormatHint=function()
{
return null;
}
TrIntegerConverter.prototype.getAsString=function(
a5,
a6
)
{
return""+a5;
}
TrIntegerConverter.prototype.getAsObject=function(
a7,
a8
)
{
return _decimalParse(a7,
this._message,
"org.apache.myfaces.trinidad.convert.IntegerConverter",
this._maxPrecision,
this._maxScale,
this._maxValue,
this._minValue,
a8,
null);
}
function TrLongConverter(
a0,
a1,
a2,
a3,
a4)
{
this._message=a0;
this._maxPrecision=a1;
this._maxScale=a2;
this._maxValue=a3;
this._minValue=a4;
this._class="TrLongConverter";
}
TrLongConverter.prototype=new TrConverter();
TrLongConverter.prototype.getFormatHint=function()
{
return null;
}
TrLongConverter.prototype.getAsString=function(
a5,
a6
)
{
return""+a5;
}
TrLongConverter.prototype.getAsObject=function(
a7,
a8
)
{
return _decimalParse(a7,
this._message,
"org.apache.myfaces.trinidad.convert.LongConverter",
this._maxPrecision,
this._maxScale,
this._maxValue,
this._minValue,
a8,
null);
}
function TrShortConverter(
a0,
a1,
a2,
a3,
a4)
{
this._message=a0;
this._maxPrecision=a1;
this._maxScale=a2;
this._maxValue=a3;
this._minValue=a4;
this._class="TrShortConverter";
}
TrShortConverter.prototype=new TrConverter();
TrShortConverter.prototype.getFormatHint=function()
{
return null;
}
TrShortConverter.prototype.getAsString=function(
a5,
a6
)
{
return""+a5;
}
TrShortConverter.prototype.getAsObject=function(
a7,
a8
)
{
return _decimalParse(a7,
this._message,
"org.apache.myfaces.trinidad.convert.ShortConverter",
this._maxPrecision,
this._maxScale,
this._maxValue,
this._minValue,
a8,
null);
}
function TrByteConverter(
a0,
a1,
a2,
a3,
a4)
{
this._message=a0;
this._maxPrecision=a1;
this._maxScale=a2;
this._maxValue=a3;
this._minValue=a4;
this._class="TrByteConverter";
}
TrByteConverter.prototype=new TrConverter();
TrByteConverter.prototype.getFormatHint=function()
{
return null;
}
TrByteConverter.prototype.getAsString=function(
a5,
a6
)
{
return""+a5;
}
TrByteConverter.prototype.getAsObject=function(
a7,
a8
)
{
return _decimalParse(a7,
this._message,
"org.apache.myfaces.trinidad.convert.ByteConverter",
this._maxPrecision,
this._maxScale,
this._maxValue,
this._minValue,
a8,
null);
}
function TrDoubleConverter(
a0,
a1,
a2,
a3,
a4)
{
this._message=a0;
this._maxPrecision=a1;
this._maxScale=a2;
this._maxValue=a3;
this._minValue=a4;
this._class="TrDoubleConverter";
}
TrDoubleConverter.prototype=new TrConverter();
TrDoubleConverter.prototype.getFormatHint=function()
{
return null;
}
TrDoubleConverter.prototype.getAsString=function(
a5,
a6
)
{
return""+a5;
}
TrDoubleConverter.prototype.getAsObject=function(
a7,
a8
)
{
return _decimalParse(a7,
this._message,
"org.apache.myfaces.trinidad.convert.DoubleConverter",
this._maxPrecision,
this._maxScale,
this._maxValue,
this._minValue,
a8,
true);
}
function TrFloatConverter(
a0,
a1,
a2,
a3,
a4)
{
this._message=a0;
this._maxPrecision=a1;
this._maxScale=a2;
this._maxValue=a3;
this._minValue=a4;
this._class="TrFloatConverter";
}
TrFloatConverter.prototype=new TrConverter();
TrFloatConverter.prototype.getFormatHint=function()
{
return null;
}
TrFloatConverter.prototype.getAsString=function(
a5,
a6
)
{
return""+a5;
}
TrFloatConverter.prototype.getAsObject=function(
a7,
a8
)
{
return _decimalParse(a7,
this._message,
"org.apache.myfaces.trinidad.convert.FloatConverter",
this._maxPrecision,
this._maxScale,
this._maxValue,
this._minValue,
a8,
true);
}
function TrRangeValidator(
a0,
a1,
a2)
{
this._maxValue=a0;
this._minValue=a1;
this._messages=a2;
this._class="TrRangeValidator";
}
TrRangeValidator.prototype=new TrValidator();
TrRangeValidator.prototype.getHints=function(
a3
)
{
return _returnRangeHints(
this._messages,
this._maxValue,
this._minValue,
"org.apache.myfaces.trinidad.validator.RangeValidator.MAXIMUM_HINT",
"org.apache.myfaces.trinidad.validator.RangeValidator.MINIMUM_HINT",
"org.apache.myfaces.trinidad.validator.RangeValidator.RANGE_HINT",
"hintMax",
"hintMin",
"hintRange"
);
}
TrRangeValidator.prototype.validate=function(
a4,
a5,
a6
)
{
string=""+a4;
numberValue=parseFloat(string);
var a7;
if(this._minValue&&this._maxValue)
{
if(numberValue>=this._minValue&&numberValue<=this._maxValue)
{
return string;
}
else
{
var a8="org.apache.myfaces.trinidad.validator.LongRangeValidator.NOT_IN_RANGE";
if(this._messages&&this._messages["range"])
{
a7=_createCustomFacesMessage(TrMessageFactory.getSummaryString(a8),
this._messages["range"],
a5,
string,
""+this._minValue,
""+this._maxValue);
}
else
{
a7=_createFacesMessage(a8,
a5,
string,
""+this._minValue,
""+this._maxValue);
}
}
}
else
{
if(this._minValue!=null)
{
if(numberValue>=this._minValue)
{
return string;
}
else
{
var a8="org.apache.myfaces.trinidad.validator.LongRangeValidator.MINIMUM";
if(this._messages&&this._messages["min"])
{
a7=_createCustomFacesMessage(TrMessageFactory.getSummaryString(a8),
this._messages["min"],
a5,
string,
""+this._minValue);
}
else
{
a7=_createFacesMessage(a8,
a5,
string,
""+this._minValue);
}
}
}
else
{
if(this._maxValue==null||numberValue<=this._maxValue)
{
return string;
}
else
{
var a8="org.apache.myfaces.trinidad.validator.LongRangeValidator.MAXIMUM";
if(this._messages&&this._messages["max"])
{
a7=_createCustomFacesMessage(TrMessageFactory.getSummaryString(a8),
this._messages["max"],
a5,
string,
""+this._maxValue);
}
else
{
a7=_createFacesMessage(a8,
a5,
string,
""+this._maxValue);
}
}
}
}
throw new TrConverterException(a7);
}
function TrLengthValidator(
a0,
a1,
a2)
{
this._maxValue=a0;
this._minValue=a1;
this._messages=a2;
this._class="TrLengthValidator";
}
TrLengthValidator.prototype=new TrValidator();
TrLengthValidator.prototype.getHints=function(
a3
)
{
return _returnRangeHints(
this._messages,
this._maxValue,
this._minValue,
"org.apache.myfaces.trinidad.validator.LengthValidator.MAXIMUM_HINT",
"org.apache.myfaces.trinidad.validator.LengthValidator.MINIMUM_HINT",
(this._minValue==this._maxValue)
?"org.apache.myfaces.trinidad.validator.LengthValidator.EXACT_HINT"
:"org.apache.myfaces.trinidad.validator.LengthValidator.RANGE_HINT",
"hintMax",
"hintMin",
"hintRange"
);
}
TrLengthValidator.prototype.validate=function(
a4,
a5,
a6
)
{
var a7=""+a4;
var a8=a7.length;
if(a8>=this._minValue&&
((this._maxValue==null)||(a8<=this._maxValue)))
{
return a7;
}
else
{
if((this._minValue>0)&&(this._maxValue!=null))
{
var a9=(this._minValue==this._maxValue);
var a10=a9
?"org.apache.myfaces.trinidad.validator.LengthValidator.EXACT"
:"org.apache.myfaces.trinidad.validator.LengthValidator.NOT_IN_RANGE";
var a11;
var a12="range";
if(this._messages&&this._messages[a12])
{
a11=_createCustomFacesMessage(TrMessageFactory.getSummaryString(a10),
this._messages[a12],
a5,
a7,
""+this._minValue,
""+this._maxValue);
}
else
{
a11=_createFacesMessage(a10,
a5,
a7,
""+this._minValue,
""+this._maxValue);
}
throw new TrConverterException(a11);
}
else if(a8<this._minValue)
{
var a10="org.apache.myfaces.trinidad.validator.LengthValidator.MINIMUM";
var a11;
if(this._messages&&this._messages["min"])
{
a11=_createCustomFacesMessage(TrMessageFactory.getSummaryString(a10),
this._messages["min"],
a5,
a7,
""+this._minValue);
}
else
{
a11=_createFacesMessage(a10,
a5,
a7,
""+this._minValue);
}
throw new TrConverterException(a11);
}
else
{
var a10="org.apache.myfaces.trinidad.validator.LengthValidator.MAXIMUM";
var a11;
if(this._messages&&this._messages["max"])
{
a11=_createCustomFacesMessage(TrMessageFactory.getSummaryString(a10),
this._messages["max"],
a5,
a7,
""+this._maxValue);
}
else
{
a11=_createFacesMessage(a10,
a5,
a7,
""+this._maxValue);
}
throw new TrConverterException(a11);
}
}
}
function TrDateTimeRangeValidator(
a0,
a1,
a2)
{
this._maxValue=a0;
this._minValue=a1;
this._messages=a2;
this._class="TrDateTimeRangeValidator";
}
TrDateTimeRangeValidator.prototype=new TrValidator();
TrDateTimeRangeValidator.prototype.getHints=function(
a3
)
{
var a4=null;
var a5=null;
if(this._maxValue)
a4=a3.getAsString(new Date(this._maxValue));
if(this._minValue)
a5=a3.getAsString(new Date(this._minValue));
return _returnRangeHints(
this._messages,
a4,
a5,
"org.apache.myfaces.trinidad.validator.DateTimeRangeValidator.MAXIMUM_HINT",
"org.apache.myfaces.trinidad.validator.DateTimeRangeValidator.MINIMUM_HINT",
"org.apache.myfaces.trinidad.validator.DateTimeRangeValidator.RANGE_HINT",
"hintMax",
"hintMin",
"hintRange"
);
}
TrDateTimeRangeValidator.prototype.validate=function(
a6,
a7,
a8
)
{
dateTime=a6.getTime();
var a9;
if(this._minValue&&this._maxValue)
{
minDate=parseInt(this._minValue);
maxDate=parseInt(this._maxValue);
if(dateTime>=minDate&&dateTime<=maxDate)
{
return a6;
}
else
{
var a10="org.apache.myfaces.trinidad.validator.DateTimeRangeValidator.NOT_IN_RANGE";
if(this._messages&&this._messages["range"])
{
a9=_createCustomFacesMessage(TrMessageFactory.getSummaryString(a10),
this._messages["range"],
a7,
""+a8.getAsString(a6),
""+a8.getAsString(new Date(this._minValue)),
""+a8.getAsString(new Date(this._maxValue)));
}
else
{
a9=_createFacesMessage(a10,
a7,
""+a8.getAsString(a6),
""+a8.getAsString(new Date(this._minValue)),
""+a8.getAsString(new Date(this._maxValue)));
}
}
}
else
{
if(this._minValue)
{
minDate=parseInt(this._minValue);
if(dateTime>=minDate)
{
return a6;
}
else
{
var a10="org.apache.myfaces.trinidad.validator.DateTimeRangeValidator.MINIMUM";
if(this._messages&&this._messages["min"])
{
a9=_createCustomFacesMessage(TrMessageFactory.getSummaryString(a10),
this._messages["min"],
a7,
""+a8.getAsString(a6),
""+a8.getAsString(new Date(this._minValue)));
}
else
{
a9=_createFacesMessage(a10,
a7,
""+a8.getAsString(a6),
""+a8.getAsString(new Date(this._minValue)));
}
}
}
else if(this._maxValue)
{
maxDate=parseInt(this._maxValue);
if(dateTime<=maxDate)
{
return a6;
}
else
{
var a10="org.apache.myfaces.trinidad.validator.DateTimeRangeValidator.MAXIMUM";
if(this._messages&&this._messages["max"])
{
a9=_createCustomFacesMessage(TrMessageFactory.getSummaryString(a10),
this._messages["max"],
a7,
""+a8.getAsString(a6),
""+a8.getAsString(new Date(this._maxValue)));
}
else
{
a9=_createFacesMessage(a10,
a7,
""+a8.getAsString(a6),
""+a8.getAsString(new Date(this._maxValue)));
}
}
}
else
{
return a6;
}
}
throw new TrConverterException(a9);
}
function TrDateRestrictionValidator(
a0,
a1,
a2)
{
this._weekdaysValue=a0;
this._monthValue=a1;
this._messages=a2;
this._weekdaysMap={'2':'tue','4':'thu','6':'sat','1':'mon','3':'wed','5':'fri','0':'sun'};
this._translatedWeekdaysMap={'sun':'0','mon':'1','tue':'2','wed':'3','thu':'4','fri':'5','sat':'6'};
this._monthMap={'2':'mar','4':'may','9':'oct','8':'sep','11':'dec','6':'jul','1':'feb','3':'apr','10':'nov','7':'aug','5':'jun','0':'jan'};
this._translatedMonthMap={'jan':'0','feb':'1','mar':'2','apr':'3','may':'4','jun':'5','jul':'6','aug':'7','sep':'8','oct':'9','nov':'10','dec':'11'};
this._class="TrDateRestrictionValidator";
}
TrDateRestrictionValidator.prototype=new TrValidator();
TrDateRestrictionValidator.prototype.getHints=function(
a3
)
{
var a4=['mon','tue','wed','thu','fri','sat','sun'];
var a5=['jan','feb','mar','apr','may','jun','jul','aug','sep','oct','nov','dec'];
if(this._weekdaysValue)
TrCollections.removeValuesFromArray(this._weekdaysValue,a4);
if(this._monthValue)
TrCollections.removeValuesFromArray(this._monthValue,a5);
return _returnHints(
this._messages,
!this._weekdaysValue?this._weekdaysValue:this._translate(a4,this._translatedWeekdaysMap,a3.getLocaleSymbols().getWeekdays()),
!this._monthValue?this._monthValue:this._translate(a5,this._translatedMonthMap,a3.getLocaleSymbols().getMonths()),
"org.apache.myfaces.trinidad.validator.DateRestrictionValidator.WEEKDAY_HINT",
"org.apache.myfaces.trinidad.validator.DateRestrictionValidator.MONTH_HINT",
"hintWeek",
"hintMonth"
);
}
TrDateRestrictionValidator.prototype._translate=function(
values,
map,
valueArray
)
{
if(values)
{
var translatedValues=new Array();
var valuesAsArray=eval(values);
for(i=0;i<valuesAsArray.length;i++)
{
translatedValues.push(valueArray[map[valuesAsArray[i].toLowerCase()]]);
}
return eval(translatedValues);
}
else
{
return values;
}
}
TrDateRestrictionValidator.prototype.validate=function(
value,
label,
converter
)
{
submittedDay=value.getDay();
weekDaysArray=eval(this._weekdaysValue);
if(weekDaysArray)
{
var dayString=this._weekdaysMap[submittedDay];
for(var i=0;i<weekDaysArray.length;++i)
{
if(weekDaysArray[i].toLowerCase()==dayString)
{
var allWeekdays=['mon','tue','wed','thu','fri','sat','sun'];
TrCollections.removeValuesFromArray(this._weekdaysValue,allWeekdays);
var days=this._translate(allWeekdays,this._translatedWeekdaysMap,converter.getLocaleSymbols().getWeekdays());
var facesMessage;
var key="org.apache.myfaces.trinidad.validator.DateRestrictionValidator.WEEKDAY";
if(this._messages&&this._messages["days"])
{
facesMessage=_createCustomFacesMessage(TrMessageFactory.getSummaryString(key),
this._messages["days"],
label,
""+converter.getAsString(value),
days);
}
else
{
facesMessage=_createFacesMessage(key,
label,
""+converter.getAsString(value),
days);
}
throw new TrConverterException(facesMessage);
}
}
}
submittedMonth=value.getMonth();
monthArray=eval(this._monthValue);
if(monthArray)
{
var monthString=this._monthMap[submittedMonth];
for(var i=0;i<monthArray.length;++i)
{
if(monthArray[i].toLowerCase()==monthString)
{
var allMonth=['jan','feb','mar','apr','may','jun','jul','aug','sep','oct','nov','dec'];
TrCollections.removeValuesFromArray(this._monthValue,allMonth);
var month=this._translate(allMonth,this._translatedMonthMap,converter.getLocaleSymbols().getMonths());
var facesMessage;
var key="org.apache.myfaces.trinidad.validator.DateRestrictionValidator.MONTH";
if(this._messages&&this._messages["month"])
{
facesMessage=_createCustomFacesMessage(TrMessageFactory.getSummaryString(key),
this._messages["month"],
label,
""+converter.getAsString(value),
month);
}
else
{
facesMessage=_createFacesMessage(key,
label,
""+converter.getAsString(value),
month);
}
throw new TrConverterException(facesMessage);
}
}
}
return value;
}
function _decimalParse(
a0,
a1,
a2,
a3,
a4,
a5,
a6,
a7,
a8
)
{
if(a0==null)
return null;
a0=TrUIUtils.trim(a0);
if(a0.length==0)
return null
var a9=null;
var a10=getLocaleSymbols();
if(a10)
{
var a11=a10.getGroupingSeparator();
if((a0.indexOf(a11)==0)||
(a0.lastIndexOf(a11)==(a0.length-1)))
{
a9=_createFacesMessage(a2+".CONVERT",
a7,
a0);
throw new TrConverterException(a9);
}
if(a11=="\xa0"){
var a12=new RegExp("\\ ","g");
a0=a0.replace(a12,"\xa0");
}
var a13=new RegExp("\\"+a11,"g");
a0=a0.replace(a13,"");
var a14=new RegExp("\\"+a10.getDecimalSeparator(),"g");
a0=a0.replace(a14,".");
}
if((a0.indexOf('e')<0)&&
(a0.indexOf('E')<0)&&
(((a0*a0)==0)||
((a0/a0)==1)))
{
var a15=null;
var a16=false;
if(a8!=null)
{
a15=parseFloat(a0);
}
else
{
a15=parseInt(a0);
if(a15<parseFloat(a0))
{
a16=true;
}
}
if(!a16&&!isNaN(a15))
{var a17=a0.length;
var a18=0;
var a19=a0.lastIndexOf('.');
if(a19!=-1)
{
a17=a19;
a18=parseInt(a0.length-parseInt(a19-1));
}
var a20;
if((a5!=null)&&
(a15>a5))
{
a20=a2+".MAXIMUM";
}
else if((a6!=null)&&
(a15<a6))
{
a20=a2+".MINIMUM";
}
if(a20!=null)
{
var a21=a21;
if((a21==null)||
(a21[a20]==null))
throw new TrConverterException(null,null,"Conversion failed, but no appropriate message found");
else
{
a9=_createFacesMessage(a20,
a7,
a0);
throw new TrConverterException(a9);
}
}
return a15;
}
}
var a22=null;
var a23=false;
if(a2.indexOf("NumberConverter")==-1)
{
a22=a2+".CONVERT";
}
else
{
a22=a2+".CONVERT_NUMBER";
if(a1&&a1["number"])
{
a9=_createCustomFacesMessage(TrMessageFactory.getSummaryString(a22),
a1["number"],
a7,
a0);
a23=true;
}
}
if(!a23)
{
a9=_createFacesMessage(a22,
a7,
a0);
}
throw new TrConverterException(a9);
}
function TrRegExpValidator(
a0,
a1
)
{
this._pattern=a0;
this._messages=a1;
this._class="TrRegExpValidator";
}
TrRegExpValidator.prototype=new TrValidator();
TrRegExpValidator.prototype.getHints=function(
a2
)
{
var a3=null;
if(this._messages["hint"])
{
a3=new Array();
a3.push(TrMessageFactory.createCustomMessage(
this._messages["hint"],
""+this._pattern)
);
}
return a3;
}
TrRegExpValidator.prototype.validate=function(
a4,
a5,
a6
)
{
a4=a4+'';
var a7=a4.match(this._pattern);
if((a7!=(void 0))&&(a7[0]==a4))
{
return a4;
}
else
{
var a8="org.apache.myfaces.trinidad.validator.RegExpValidator.NO_MATCH";
var a9;
if(this._messages&&this._messages["detail"])
{
a9=_createCustomFacesMessage(
TrMessageFactory.getSummaryString(a8),
this._messages["detail"],
a5,
a4,
this._pattern);
}
else
{
a9=_createFacesMessage(a8,
a5,
a4,
this._pattern);
}
throw new TrValidatorException(a9);
}
}
function _returnRangeHints(
a0,
a1,
a2,
a3,
a4,
a5,
a6,
a7,
a8
)
{
if(a1&&a2)
{
var a9=new Array();
if(a0&&a0[a8])
{
a9.push(
TrMessageFactory.createCustomMessage(
a0[a8],
""+a2,
""+a1)
);
}
else
{
a9.push(
TrMessageFactory.createMessage(
a5,
""+a2,
""+a1)
);
}
return a9;
}
return _returnHints(
a0,
a1,
a2,
a3,
a4,
a6,
a7
);
}
function _returnHints(
a0,
a1,
a2,
a3,
a4,
a5,
a6
)
{
var a7;
if(a1)
{
a7=new Array();
if(a0&&a0[a5])
{
a7.push(
TrMessageFactory.createCustomMessage(
a0[a5],
""+a1)
);
}
else
{
a7.push(
TrMessageFactory.createMessage(
a3,
""+a1)
);
}
}
if(a2)
{
if(!a7)
{
a7=new Array();
}
if(a0&&a0[a6])
{
a7.push(
TrMessageFactory.createCustomMessage(
a0[a6],
""+a2)
);
}
else
{
a7.push(
TrMessageFactory.createMessage(
a4,
""+a2)
);
}
}
return a7;
}
