function _TrPollManager()
{
this.pollIdList;
this.active=true;
}
_TrPollManager.prototype.addAndActivate=function(a0,a1,a2)
{
if(!this.pollIdList)
this.pollIdList=new Array();
this[a0]=new _TrPollCommand(a1,a2,this.active);
idIndex=-1;
for(var a3=0;a3<this.pollIdList.length;a3++)
{
if(a0==this.pollIdList[a3])
{
idIndex=a3;
break;
}
}
if(idIndex!=-1)
{
this.pollIdList[idIndex]==a0;
}
else
{
this.pollIdList.push(a0);
}
}
_TrPollManager.prototype.deactivateAll=function()
{
for(var a4=0;a4<this.pollIdList.length;a4++)
{
clearTimeout(this[this.pollIdList[a4]].commandId);
}
this.active=false;
}
_TrPollManager.prototype.reactivateAll=function()
{
for(var a5=0;a5<this.pollIdList.length;a5++)
{
this[this.pollIdList[a5]].activate();
}
this.active=true;
}
function _TrPollCommand(a0,a1,a2)
{
this.commandString=a0;
this.timeout=a1;
if(a2)
this.activate();
}
_TrPollCommand.prototype.activate=function()
{
this.commandId=setTimeout(this.commandString,this.timeout);
}
