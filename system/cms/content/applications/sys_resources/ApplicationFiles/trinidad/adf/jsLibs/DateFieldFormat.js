function _getDateFieldFormat(a0)
{
var a1=a0.name;
if(a1&&_dfs)
{
var a2=_dfs[a1];
if(a2)
return new TrDateTimeConverter(a2);
}
return new TrDateTimeConverter();
}
function _fixDFF(a0)
{
var a1=_getDateFieldFormat(a0);
if(a0.value!="")
{
try
{
var a2=a1.getAsObject(a0.value);
if(a2!=null)
a0.value=a1.getAsString(a2);
}
catch(e)
{
}
}
}
