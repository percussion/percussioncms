function TrColorConverter(
a0,
a1,
a2,
a3)
{
this._class="TrColorConverter";
this._allowsTransparent=a1;
this._patternsString=a2;
this._messages=a3;
if(a0!=null)
{
if(typeof(a0)=="string")
a0=[a0];
}
this._pattern=a0;
}
TrColorConverter.prototype=new TrConverter();
TrColorConverter.prototype.getFormatHint=function()
{
if(this._messages&&this._messages["hint"])
{
return TrMessageFactory.createCustomMessage(
this._messages["hint"],
this._pattern);
}
else
{
return TrMessageFactory.createMessage(
"org.apache.myfaces.trinidad.convert.ColorConverter.FORMAT_HINT",
this._pattern);
}
}
TrColorConverter.prototype.getAsString=function(
a4)
{
if(a4==null)
return null;
if(a4.alpha==0)
return _cfTrans;
var a5=new Object();
a5.value="";
var a6=this._pattern;
if(typeof a6!="string")
a6=a6[0];
_cfoDoClumping(a6,
_cfoSubformat,
a4,
a5);
return a5.value;
}
TrColorConverter.prototype.getAsObject=function(
a7,
a8)
{
if(a7==null)
return null;
a7=TrUIUtils.trim(a7);
if(a7.length==0)
return null
if(this._allowsTransparent&&_cfTrans==a7)
return new TrColor(0,0,0,0);
var a9;
var a10="org.apache.myfaces.trinidad.convert.ColorConverter.CONVERT";
if(this._messages&&this._messages["detail"])
{
a9=_createCustomFacesMessage(
TrMessageFactory.getSummaryString(a10),
this._messages["detail"],
a8,
a7,
this._patternsString);
}
else
{
a9=_createFacesMessage(a10,
a8,
a7,
this._patternsString);
}
var a11=this._pattern;
if(typeof a11=="string")
{
return this._rgbColorParseImpl(a7,
a11,
a9);
}
else
{
var a12;
for(a12=0;a12<a11.length;a12++)
{
try{
var a13=this._rgbColorParseImpl(a7,
a11[a12],
a9);
return a13;
}
catch(e)
{
if(a12==a11.length-1)
throw e;
}
}
}
}
TrColorConverter.prototype._rgbColorParseImpl=function(
a14,
a15,
a16)
{
var a17=new Object();
a17.currIndex=0;
a17.parseString=a14;
a17.parseException=new TrConverterException(a16);
var a18=new TrColor(0x00,0x00,0x00);
if(_cfoDoClumping(a15,
_cfoSubParse,
a17,
a18))
{
if(a14.length!=a17.currIndex)
{
throw a17.parseException;
}
return a18;
}
else
{
throw a17.parseException;
}
}
function TrColor(
a0,
a1,
a2,
a3)
{
this._class="TrColor";
if(a3==null)
a3=0xff;
this.red=(a0&0xff);
this.green=(a1&0xff);
this.blue=(a2&0xff);
this.alpha=(a3&0xff);
}
TrColor.prototype.toString=function()
{
return"rgba("+this.red+
","+this.green+
","+this.blue+
","+this.alpha+")";
}
var _cfTrans;
function _cfoDoClumping(
a0,
a1,
a2,
a3
)
{
var a4=a0.length;
var a5=false;
var a6=0;
var a7=null;
var a8=0;
for(var a9=0;a9<a4;a9++)
{
var a10=a0.charAt(a9);
if(a5)
{
if(a10=="\'")
{
a5=false;
if(a6!=1)
{
a8++;
a6--;
}
if(!a1(a0,
"\'",
a8,
a6,
a2,
a3))
{
return false;
}
a6=0;
a7=null;
}
else
{
a6++;
}
}
else
{
if(a10!=a7)
{
if(a6!=0)
{
if(!a1(a0,
a7,
a8,
a6,
a2,
a3))
{
return false;
}
a6=0;
a7=null;
}
if(a10=='\'')
{
a5=true;
}
a8=a9;
a7=a10;
}
a6++;
}
}
if(a6!=0)
{
if(!a1(a0,
a7,
a8,
a6,
a2,
a3))
{
return false;
}
}
return true;
}
function _cfoSubformat(
a0,
a1,
a2,
a3,
a4,
a5
)
{
var a6=null;
if((a1>='A')&&(a1<='Z')||
(a1>='a')&&(a1<='z'))
{
switch(a1)
{
case'r':
a6=_cfoGetPaddedNumber(a4.red,a3,3,10);
break;
case'g':
a6=_cfoGetPaddedNumber(a4.green,a3,3,10);
break;
case'b':
a6=_cfoGetPaddedNumber(a4.blue,a3,3,10);
break;
case'a':
a6=_cfoGetPaddedNumber(a4.alpha,a3,3,10);
break;
case'R':
a6=
_cfoGetPaddedNumber(a4.red,a3,2,16).toUpperCase();
break;
case'G':
a6=
_cfoGetPaddedNumber(a4.green,a3,2,16).toUpperCase();
break;
case'B':
a6=
_cfoGetPaddedNumber(a4.blue,a3,2,16).toUpperCase();
break;
case'A':
a6=
_cfoGetPaddedNumber(a4.alpha,a3,2,16).toUpperCase();
break;
default:
a6="";
}
}
else
{
a6=a0.substring(a2,a2+a3);
}
a5.value+=a6;
return true;
}
function _cfoSubParse(
a0,
a1,
a2,
a3,
a4,
a5
)
{
var a6=a4.currIndex;
if((a1>='A')&&(a1<='Z')||
(a1>='a')&&(a1<='z'))
{
switch(a1)
{
case'r':
a5.red=_cfoAccumulateNumber(a4,a3,3,10);
if(a5.red==null)
{
return false;
}
break;
case'g':
a5.green=_cfoAccumulateNumber(a4,a3,3,10);
if(a5.green==null)
{
return false;
}
break;
case'b':
a5.blue=_cfoAccumulateNumber(a4,a3,3,10);
if(a5.blue==null)
{
return false;
}
break;
case'a':
a5.alpha=_cfoAccumulateNumber(a4,a3,3,10);
if(a5.alpha==null)
{
return false;
}
break;
case'R':
a5.red=_cfoAccumulateNumber(a4,a3,2,16);
if(a5.red==null)
{
return false;
}
break;
case'G':
a5.green=_cfoAccumulateNumber(a4,a3,2,16);
if(a5.green==null)
{
return false;
}
break;
case'B':
a5.blue=_cfoAccumulateNumber(a4,a3,2,16);
if(a5.blue==null)
{
return false;
}
break;
case'A':
a5.alpha=_cfoAccumulateNumber(a4,a3,2,16);
if(a5.alpha==null)
{
return false;
}
break;
default:
}
}
else
{
return _cfoMatchText(a4,
a0.substring(a2,a2+a3));
}
return true;
}
function _cfoMatchText(
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
if(a5!=a1)
return false;
a0.currIndex+=a2;
return true;
}
function _cfoAccumulateNumber(
a0,
a1,
a2,
a3)
{
var a4=a0.currIndex;
var a5=a4;
var a6=a0.parseString;
var a7=a6.length;
if(a7>a5+a2)
a7=a5+a2;
var a8=0;
while(a5<a7)
{
var a9=parseInt(a6.charAt(a5),a3);
if(!isNaN(a9))
{
a8*=a3;
a8+=a9;
a5++;
}
else
{
break;
}
}
if(a4!=a5&&
(a5-a4)>=a1)
{
a0.currIndex=a5;
return a8;
}
else
{
return null;
}
}
function _cfoGetPaddedNumber(
a0,
a1,
a2,
a3)
{
var a4=a0.toString(a3);
if(a1!=null)
{
var a5=a1-a4.length;
while(a5>0)
{
a4="0"+a4;
a5--;
}
}
if(a2!=null)
{
var a6=a4.length-a2;
if(a6>0)
{
a4=a4.substring(a6,
a6+a2);
}
}
return a4;
}
