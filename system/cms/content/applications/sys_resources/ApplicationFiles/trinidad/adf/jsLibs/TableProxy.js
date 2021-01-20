function _tableSort(
a0,
a1,
a2,
a3,
a4)
{
_submitPartialChange(a0,a1,
{event:'sort',
source:a2,
value:a3,
state:a4});
return false;
}
function CollectionComponent(
a0,
a1
)
{
this._formName=a0;
this._name=a1;
}
CollectionComponent.prototype.getFormName=function()
{
return this._formName;
};
CollectionComponent.prototype.getName=function()
{
return this._name;
};
CollectionComponent.prototype.getFormElement=function(a2)
{
var a3=document.forms[this.getFormName()];
var a4=this.getName()+":"+a2;
var a5=a3[a4];
return a5;
};
CollectionComponent.defineSubmit=function(a6,a7)
{
if(this._eventParam!=(void 0))
return;
CollectionComponent.prototype._eventParam=a6;
CollectionComponent.prototype._sourceParam=a7;
CollectionComponent.prototype._pTargetsParam="partialTargets";
CollectionComponent.prototype.addParam=function(paramName,paramValue){
if(this._params==(void 0))
{
this._params=new Object();
}
this._params[paramName]=paramValue;
}
CollectionComponent.prototype.submit=function(event,link){
this.addParam(this._eventParam,event);
this.addParam(this._sourceParam,this.getName());
var a8=this._params;
var a9=a8[this._pTargetsParam];
if(link!=(void 0))
{
var a10=link.id;
if(a10!=(void 0))
{
_setRequestedFocusNode(document,a10,false,window);
}
if(a9==(void 0))
{
a9=this.getName();
a8[this._pTargetsParam]=a9;
}
}
var a11=this._validate;
if(a11==(void 0))
a11=1;
var a12=submitForm;
if(a9!=(void 0))
{
a12=_submitPartialChange;
}
a12(this.getFormName(),a11,a8);
return false;
};
};
CollectionComponent.defineMultiSelect=function(a13,a14,a15)
{
if(this._selectedKey!=(void 0))
return;
CollectionComponent.prototype._selectedKey=a13;
CollectionComponent.prototype._selectedModeKey=a14;
CollectionComponent.prototype.getLength=function(){
var a16=this._getBoxes();
return a16.length;
};
CollectionComponent.prototype.multiSelect=function(selectAll){
var a16=this._getBoxes();
for(var a17=0;a17<a16.length;a17++)
{
var a18=a16[a17];
a18.checked=selectAll;
}
var a19=this.getFormElement(this._selectedModeKey);
if(selectAll)
{
a19.value="all";
}
else
{
a19.value="none";
}
if(a15)
{
_submitPartialChange(this.getFormName(),1,null);
}
};
CollectionComponent.prototype._getBoxes=function(){
var a16=this.getFormElement(this._selectedKey);
if(a16.length==(void 0))
{
var a20=new Array(1);
a20[0]=a16;
a16=a20;
}
return a16;
};
};
CollectionComponent.defineTree=
function(a21,
a22,
a23,
a24,
a25,
a26,
a27)
{
if(this._pathParam!=(void 0))
return;
CollectionComponent.defineSubmit(a21,a22);
CollectionComponent.prototype._pathParam=a23;
CollectionComponent.prototype._startParam=a24;
CollectionComponent.prototype._gotoEvent=a25;
CollectionComponent.prototype._focusEvent=a26;
CollectionComponent.prototype._validate=a27;
CollectionComponent.prototype.action=function(event,path,link)
{
this.addParam(this._pathParam,path);
return this.submit(event,link);
};
CollectionComponent.prototype.range=function(path,start)
{
this.addParam(this._startParam,start);
return this.action(this._gotoEvent,path);
};
CollectionComponent.prototype.focus=function(path,link)
{
return this.action(this._focusEvent,path,link);
};
};
