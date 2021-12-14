var noUnsavedChangesMade = 'You have unsaved changes to the images. ' + '\n' + 
' If you continue you will lose your changes.  Do you want to Continue?';


function makeDirty()
            {
				       dirtyFlag = true; 
            	       $("#dirty").val("true");
            }

function checkDirtyOnExit()
{   
    var go = true; 
	if(dirtyFlag)
	{
		go = confirm(noUnsavedChangesMade); 
	    return go;
	}
	return true; 
}	

function close_dirty()
{
	var closeMe = checkDirtyOnExit();
	if(closeMe)
	{
		window.close(); 
	}
}

function newimage_dirty()
{
   var newImage = checkDirtyOnExit();
   if(newImage)
   {
   	   cancelOperation(); 
   }	
}

function saveAndContinue()
{
    document.getElementById("operation").name = "_finish"; 
    document.forms[0].submit();  
}
            
function saveAndExit()
{
	document.getElementById("action").value = "close"; 
	saveAndContinue();   
    
}
 
function cancelOperation()
{
	document.getElementById("sizedImages").value=""; 
	document.getElementById("operation").name = "_cancel"; 
    document.forms[0].submit();  
} 
            
function cancelAndExit()
{
	cancelOperation();
	var t1=setTimeout("window.close();",2000); 
}

function parentRefresh()
{
	if (window.opener != null)
	{
	   refreshCxApplet(window.opener, "Selected", "", "");
  	}
}