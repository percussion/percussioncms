function TrNumberFormat(a0)
{
if(!a0)
alert("type for TrNumberFormat not defined!");
this._type=a0;
this._pPre=getLocaleSymbols().getPositivePrefix();
this._pSuf=getLocaleSymbols().getPositiveSuffix();
this._nPre=getLocaleSymbols().getNegativePrefix();
this._nSuf=getLocaleSymbols().getNegativeSuffix();
this._maxFractionDigits=3;
this._maxIntegerDigits=40;
this._minFractionDigits=0;
this._minIntegerDigits=1;
this._groupingUsed=true;
}
TrNumberFormat.getNumberInstance=function()
{
return new TrNumberFormat("number");
}
TrNumberFormat.getCurrencyInstance=function()
{
return new TrNumberFormat("currency");
}
TrNumberFormat.getPercentInstance=function()
{
return new TrNumberFormat("percent");
}
TrNumberFormat.prototype.setGroupingUsed=function(a1)
{
this._groupingUsed=a1;
}
TrNumberFormat.prototype.isGroupingUsed=function()
{
return this._groupingUsed;
}
TrNumberFormat.prototype.setMaximumIntegerDigits=function(a2)
{
if(a2)
{
this._maxIntegerDigits=a2<0?0:a2;
if(this._minIntegerDigits>this._maxIntegerDigits)
{
this._minIntegerDigits=this._maxIntegerDigits;
}
}
}
TrNumberFormat.prototype.getMaximumIntegerDigits=function()
{
return this._maxIntegerDigits;
}
TrNumberFormat.prototype.setMaximumFractionDigits=function(a3)
{
if(a3)
{
this._maxFractionDigits=a3<0?0:a3;
if(this._maxFractionDigits<this._minFractionDigits)
{
this._minFractionDigits=this._maxFractionDigits;
}
}
}
TrNumberFormat.prototype.getMaximumFractionDigits=function()
{
return this._maxFractionDigits;
}
TrNumberFormat.prototype.setMinimumIntegerDigits=function(a4)
{
if(a4)
{
this._minIntegerDigits=a4<0?0:a4;
if(this._minIntegerDigits>this._maxIntegerDigits)
{
this._maxIntegerDigits=this._minIntegerDigits;
}
}
}
TrNumberFormat.prototype.getMinimumIntegerDigits=function()
{
return this._minIntegerDigits;
}
TrNumberFormat.prototype.setMinimumFractionDigits=function(a5)
{
if(a5)
{
this._minFractionDigits=a5<0?0:a5;
if(this._maxFractionDigits<this._minFractionDigits)
{
this._maxFractionDigits=this._minFractionDigits;
}
}
}
TrNumberFormat.prototype.getMinimumFractionDigits=function()
{
return this._minFractionDigits;
}
TrNumberFormat.prototype.format=function(a6)
{
if(this._type=="percent")
return this.percentageToString(a6);
else if(this._type=="currency")
return this.currencyToString(a6);
else if(this._type=="number")
return this.numberToString(a6);
}
TrNumberFormat.prototype.parse=function(a7)
{
if(this._type=="percent")
return this.stringToPercentage(a7);
else if(this._type=="currency")
return this.stringToCurrency(a7);
}
TrNumberFormat.prototype.stringToCurrency=function(a8)
{
var a9=a8.indexOf(this._nPre);
if(a9!=-1)
{
a8=a8.substr(this._nPre.length,a8.length);
var a10=a8.indexOf(this._nSuf);
if(a10!=-1)
{
a8=a8.substr(0,a8.length-this._nSuf.length);
return(parseFloat(a8)*-1);
}
else
{
throw new TrParseException("not able to parse number");
}
}
else
{
var a11=a8.indexOf(this._pPre);
if(a11!=-1)
{
a8=a8.substr(this._pPre.length,a8.length);
var a12=a8.indexOf(this._pSuf);
if(a12!=-1)
{
a8=a8.substr(0,a8.length-this._pSuf.length);
a8=parseFloat(a8);
}
else
{
throw new TrParseException("not able to parse number");
}
return a8;
}
else
{
throw new TrParseException("not able to parse number");
}
}
}
TrNumberFormat.prototype.stringToPercentage=function(a13)
{
a13=a13.replace(/\%/g,'');
a13=parseFloat(a13);
if(isNaN(a13))
{
throw new TrParseException("not able to parse number");
}
return a13;
}
TrNumberFormat.prototype.numberToString=function(a14)
{
var a15=a14<0;
if(a15)
a14=(a14*-1);
var a16=a14+"";
var a17=getLocaleSymbols().getDecimalSeparator();
var a18=a16.indexOf(a17);
var a19=a16.length;
var a20;
var a21;
if(a18!=-1)
{
a20=a16.substring(0,a18);
a21=a16.substring(a18+1,a19);
}
else
{
a20=a16;
a21="";
}
a20=this._formatIntegers(a20);
a21=this._formatFractions(a21)
if(a21!="")
a16=(a20+a17+a21);
else
a16=(a20);
if(a15)
a16="-"+a16;
return a16;
}
TrNumberFormat.prototype.currencyToString=function(a22)
{
if(a22<0)
{
a22=(a22*-1)+"";
a22=this.numberToString(a22);
return this._nPre+a22+this._nSuf;
}
else
{
a22=this.numberToString(a22);
return this._pPre+a22+this._pSuf;
}
}
TrNumberFormat.prototype.percentageToString=function(a23)
{
a23=a23*100;
a23=this.getRounded(a23);
if(isNaN(a23))
{
throw new TrParseException("not able to parse number");
}
a23=this.numberToString(a23);
return a23+'%';
}
TrNumberFormat.prototype.getRounded=function(a24)
{
a24=this.moveDecimalRight(a24);
a24=Math.round(a24);
a24=this.moveDecimalLeft(a24);
return a24;
}
TrNumberFormat.prototype.moveDecimalRight=function(a25)
{
var a26='';
a26=this.moveDecimal(a25,false);
return a26;
}
TrNumberFormat.prototype.moveDecimalLeft=function(a27)
{
var a28='';
a28=this.moveDecimal(a27,true);
return a28;
}
TrNumberFormat.prototype.moveDecimal=function(a29,a30)
{
var a31='';
a31=this.moveDecimalAsString(a29,a30);
return parseFloat(a31);
}
TrNumberFormat.prototype.moveDecimalAsString=function(a32,a33)
{
var a34=2;
if(a34<=0)
return a32;
var a35=a32+'';
var a36=this.getZeros(a34);
var a37=new RegExp('([0-9.]+)');
if(a33)
{
a35=a35.replace(a37,a36+'$1');
var a38=new RegExp('(-?)([0-9]*)([0-9]{'+a34+'})(\\.?)');
a35=a35.replace(a38,'$1$2.$3');
}
else
{
var a39=a37.exec(a35);
if(a39!=null)
{
a35=a35.substring(0,a39.index)+a39[1]+a36+a35.substring(a39.index+a39[0].length);
}
var a38=new RegExp('(-?)([0-9]*)(\\.?)([0-9]{'+a34+'})');
a35=a35.replace(a38,'$1$2$4.');
}
a35=a35.replace(/\.$/,'');
return a35;
}
TrNumberFormat.prototype.getZeros=function(a40)
{
var a41='';
var a42;
for(a42=0;a42<a40;a42++){
a41+='0';
}
return a41;
}
TrNumberFormat.prototype._formatIntegers=function(a43)
{
var a44=a43.length;
var a45=this.getMaximumIntegerDigits();
var a46=this.getMinimumIntegerDigits();
var a47;
if(a44>a45)
{
a47=a44-a45;
a43=a43.substring(a47,a44);
}
else if(a44<a46)
{
a47=a46-a44;
var a48="";
while(a47>0)
{
a48="0"+a48;
--a47;
}
a43=a48+a43;
}
if(this.isGroupingUsed())
{
a43=this._addGroupingSeparators(a43);
}
return a43;
}
TrNumberFormat.prototype._formatFractions=function(a49)
{
var a50=a49.length;
var a51=this.getMaximumFractionDigits();
var a52=this.getMinimumFractionDigits();
if(a50>a51)
{
a49=a49.substring(0,a51-1);
}
else if(a50<a52)
{
var a53=a52-a50;
while(a53>0)
{
a49=a49+"0";
--a53;
}
}
return a49;
}
TrNumberFormat.prototype._addGroupingSeparators=function(a54)
{
var a55=a54.length;
if(a55<=3)
return a54;
var a56=a55%3;
var a57;
var a58;
var a59="";
if(a56>0)
{
a57=a54.substring(0,a56);
a58=a54.substring(a56,a55);
}
else
{
a57="";
a58=a54;
}
var a60=getLocaleSymbols().getGroupingSeparator();
for(i=0;i<a58.length;i++)
{
if(i%3==0)
{
a59+=a60;
}
a59+=a58.charAt(i);
}
a54=a57+a59;
return a54;
}
function TrParseException(
a0
)
{
this._message=a0;
}
TrParseException.prototype.getMessage=function()
{
return this._message;
}
