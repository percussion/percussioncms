function TrStatusIndicator()
{
}
TrStatusIndicator._register=function(a0)
{
if(!TrStatusIndicator._registered)
{
TrStatusIndicator._registered=new Object();
TrPage.getInstance().getRequestQueue().addStateChangeListener(
TrStatusIndicator._handleStateChange);
}
TrStatusIndicator._registered[a0]=true;
}
TrStatusIndicator._handleStateChange=function(a1)
{
var a2=a1==TrRequestQueue.STATE_BUSY;
for(id in TrStatusIndicator._registered)
{
var a3=document.getElementById(id+"::busy");
if(!a3)
continue;
a3.style.display=a2?"inline":"none";
var a4=document.getElementById(id+"::ready");
a4.style.display=a2?"none":"inline";
}
}
