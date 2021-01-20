var TrCollections=new Object();
TrCollections.removeValuesFromArray=function(
a0,
a1
)
{
if(a0&&a1)
{
for(i=0;i<a0.length;i++)
{
var a2=a0[i];
for(j=0;j<a1.length;j++)
{
if(a1[j].toLowerCase()==a2.toLowerCase())
{
a1.splice(j,1);
}
}
}
}
}
