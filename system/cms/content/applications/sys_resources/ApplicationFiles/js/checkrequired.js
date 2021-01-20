function checkrequired(which)
{
 var pass=true;
 if (document.images)
 {
  for(i=0;i<which.length;i++)
  {
   var tempobj=which.elements[i];
   if (tempobj.name.substring(0,8)=="required")
   {
    if(((tempobj.type=="text"||
         tempobj.type=="textarea")&&tempobj.value=='')||
        (tempobj.type.toString().charAt(0)=="s"&&tempobj.selectedIndex==-1))
    {
      pass=false;
      break;
    }
   }
  }
 }
 
 if(!pass)
 {
  alert("One or more of the required elements are not completed. Please complete them, then submit again!");
  return false;
 }
 else
  return true;
}
