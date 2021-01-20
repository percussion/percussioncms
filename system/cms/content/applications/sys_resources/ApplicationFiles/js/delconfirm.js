function delConfirm(link) 
{
 if(confirm( "Delete actions cannot be undone. Are you sure you want to continue?")) 
 {
  location=link+"&rxorigin="+rxorigin;
 }
}

function delConfirmWf(link) 
{
 if(confirm( "Delete actions cannot be undone. Are you sure you want to continue?")) 
 {
  location=link+"&rxorigin=wfhome";
 }
}
