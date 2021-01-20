function TrNumberConverter(
a0,
a1,
a2,
a3,
a4,
a5,
a6,
a7,
a8,
a9,
a10,
a11)
{
this._pattern=a0;
this._type=a1;
this._locale=a2;
this._messages=a3;
this._currencyCode=a6;
this._currencySymbol=a7;
this._maxFractionDigits=a8;
this._maxIntegerDigits=a9;
this._minFractionDigits=a10;
this._minIntegerDigits=a11;
if(a4!==undefined)
this._integerOnly=a4;
else
this._integerOnly=false;
if(a5!==undefined)
this._groupingUsed=a5;
else
this._groupingUsed=true;
this._initNumberFormat();
this._class="TrNumberConverter";
}
TrNumberConverter.prototype=new TrConverter();
TrNumberConverter.prototype.setCurrencyCode=function(a12)
{
this._currencyCode=a12;
}
TrNumberConverter.prototype.getCurrencyCode=function()
{
return this._currencyCode;
}
TrNumberConverter.prototype.setCurrencySymbol=function(a13)
{
this._currencySymbol=a13;
}
TrNumberConverter.prototype.getCurrencySymbol=function()
{
return this._currencySymbol;
}
TrNumberConverter.prototype.setMaxFractionDigits=function(a14)
{
this._maxFractionDigits=a14;
}
TrNumberConverter.prototype.getMaxFractionDigits=function()
{
return this._maxFractionDigits;
}
TrNumberConverter.prototype.setMaxIntegerDigits=function(a15)
{
this._maxIntegerDigits=a15;
}
TrNumberConverter.prototype.getMaxIntegerDigits=function()
{
return this._maxIntegerDigits;
}
TrNumberConverter.prototype.setMinFractionDigits=function(a16)
{
this._minFractionDigits=a16;
}
TrNumberConverter.prototype.getMinFractionDigits=function()
{
return this._minFractionDigits;
}
TrNumberConverter.prototype.setMinIntegerDigits=function(a17)
{
this._minIntegerDigits=a17;
}
TrNumberConverter.prototype.getMinIntegerDigits=function()
{
return this._minIntegerDigits;
}
TrNumberConverter.prototype.setGroupingUsed=function(a18)
{
this._groupingUsed=a18;
}
TrNumberConverter.prototype.isGroupingUsed=function()
{
return this._groupingUsed;
}
TrNumberConverter.prototype.setIntegerOnly=function(a19)
{
this._integerOnly=a19;
}
TrNumberConverter.prototype.isIntegerOnly=function()
{
return this._integerOnly;
}
TrNumberConverter.prototype.getFormatHint=function()
{
if(this._messages&&this._messages["hintPattern"])
{
return TrMessageFactory.createCustomMessage(
this._messages["hintPattern"],
this._pattern);
}
else
{
if(this._pattern)
{
return TrMessageFactory.createMessage(
"org.apache.myfaces.trinidad.convert.NumberConverter.FORMAT_HINT",
this._pattern);
}
else
{
return null;
}
}
}
TrNumberConverter.prototype.getAsString=function(
a20,
a21
)
{
if(this._isConvertible())
{
if(this._type=="percent"||this._type=="currency")
{
var a22=this._numberFormat.format(a20);
if(this._type=="currency")
{
if(this._currencyCode)
{
a22=a22.replace(getLocaleSymbols().getCurrencyCode(),this._currencyCode);
}
else if(this._currencySymbol)
{
a22=a22.replace(getLocaleSymbols().getCurrencySymbol(),this._currencySymbol);
}
}
return a22;
}
else
{
return this._numberFormat.format(a20);
}
}
else
{
return undefined;
}
}
TrNumberConverter.prototype.getAsObject=function(
a23,
a24
)
{
if(this._isConvertible())
{
var a25;
if(this._type=="percent"||this._type=="currency")
{
try
{
a23=this._numberFormat.parse(a23)+"";
}
catch(e)
{
var a26;
var a27=this._numberFormat.format(this._example);
var a28="org.apache.myfaces.trinidad.convert.NumberConverter.CONVERT_"+this._type.toUpperCase();
if(this._messages&&this._messages[this._type])
{
a26=_createCustomFacesMessage(TrMessageFactory.getSummaryString(a28),
this._messages[this._type],
a24,
a23,
a27);
}
else
{
a26=_createFacesMessage(a28,
a24,
a23,
a27);
}
throw new TrConverterException(a26);
}
}
a25=_decimalParse(a23,
this._messages,
"org.apache.myfaces.trinidad.convert.NumberConverter",
null,
null,
null,
null,
a24,
!this.isIntegerOnly());
if(this._type=="percent")
{
a25=a25/100;
}
return a25;
}
else
{
return undefined;
}
}
TrNumberConverter.prototype._isConvertible=function()
{
if((this._pattern==null)&&(this._locale==null))
{
return true;
}
else
{
return false;
}
}
TrNumberConverter.prototype._initNumberFormat=function()
{
if(this._type=="percent")
{
this._example=0.3423;
this._numberFormat=TrNumberFormat.getPercentInstance();
}
else if(this._type=="currency")
{
this._example=10250;
this._numberFormat=TrNumberFormat.getCurrencyInstance();
}
else if(this._type=="number")
{
this._numberFormat=TrNumberFormat.getNumberInstance();
}
this._numberFormat.setGroupingUsed(this.isGroupingUsed());
this._numberFormat.setMaximumFractionDigits(this.getMaxFractionDigits());
this._numberFormat.setMaximumIntegerDigits(this.getMaxIntegerDigits());
this._numberFormat.setMinimumFractionDigits(this.getMinFractionDigits());
this._numberFormat.setMinimumIntegerDigits(this.getMinIntegerDigits());
}
