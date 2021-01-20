var _byteLenKey="org.apache.myfaces.trinidad.validator.ByteLengthValidator.MAXIMUM";
function TrByteLengthValidator(
a0,
a1
)
{
this._length=a0;
this._messages=a1;
this._class="TrByteLengthValidator";
}
TrByteLengthValidator.prototype=new TrValidator();
function CjkFormat(
a0,
a1
)
{
this._base=TrByteLengthValidator;
this._base(a0,a1);
this._class="CjkFormat";
}
CjkFormat.prototype=new TrByteLengthValidator();
CjkFormat.prototype.getHints=function(
a2
)
{
var a3=null;
if(this._messages["hint"])
{
a3=new Array();
a3.push(TrMessageFactory.createCustomMessage(
this._messages["hint"],
this._length)
);
}
return a3;
}
CjkFormat.prototype.validate=function(
a4,
a5,
a6
)
{
var a7=0;
var a8=this._length;
while(a7<a4.length)
{
var a9=a4.charCodeAt(a7);
if((a9<0x80)||((0xFF60<a9)&&(a9<0xFFA0)))a8--;
else a8-=2;
if(a8<0)
{
var a10;
if(!this._messages["detail"])
{
a10=_createFacesMessage(_byteLenKey,
a5,
a4,
this._length);
}
else
{
a10=_createCustomFacesMessage(
TrMessageFactory.getSummaryString(_byteLenKey),
this._messages["detail"],
a5,
a4,
this._length);
}
throw new TrValidatorException(a10);
}
a7++;
}
return a4;
}
function Utf8Format(
a0,
a1
)
{
this._base=TrByteLengthValidator;
this._base(a0,a1);
this._class="Utf8Format";
}
Utf8Format.prototype=new TrByteLengthValidator();
Utf8Format.prototype.getHints=function(
a2
)
{
var a3=null;
if(this._messages["hint"])
{
a3=new Array();
a3.push(TrMessageFactory.createCustomMessage(
this._messages["hint"],
this._length)
);
}
return a3;
}
Utf8Format.prototype.validate=function(
a4,
a5,
a6
)
{
var a7=0;
var a8=this._length;
while(a7<a4.length)
{
var a9=a4.charCodeAt(a7);
if(a9<0x80)a8--;
else if(a9<0x800)a8-=2;
else
{
if((a9&0xF800)==0xD800)
a8-=2;
else
a8-=3;
}
if(a8<0)
{
var a10;
if(!this._messages["detail"])
{
a10=_createFacesMessage(_byteLenKey,
a5,
a4,
this._length);
}
else
{
a10=_createCustomFacesMessage(
TrMessageFactory.getSummaryString(_byteLenKey),
this._messages["detail"],
a5,
a4,
this._length);
}
throw new TrValidatorException(a10);
}
a7++;
}
return a4;
}
function SBFormat(
a0,
a1
)
{
this._base=TrByteLengthValidator;
this._base(a0,a1);
this._class="SBFormat";
}
SBFormat.prototype=new TrByteLengthValidator();
SBFormat.prototype.getHints=function(
a2
)
{
var a3=null;
if(this._messages["hint"])
{
a3=new Array();
a3.push(TrMessageFactory.createCustomMessage(
this._messages["hint"],
this._length)
);
}
return a3;
}
SBFormat.prototype.validate=function(
a4,
a5,
a6
)
{
if(this._length<a4.length)
{
var a7;
if(!this._messages["detail"])
{
a7=_createFacesMessage(_byteLenKey,
a5,
a4,
this._length);
}
else
{
a7=_createCustomFacesMessage(
TrMessageFactory.getSummaryString(_byteLenKey),
this._messages["detail"],
a5,
a4,
this._length);
}
throw new TrValidatorException(a7);
}
return a4;
}
