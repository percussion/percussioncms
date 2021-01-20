// Global variables
var wifxIsDirty = false;
var wifxIsNewImage = false;
var ps_hasWifx = false;
var ps_wifxHelpWindow = null;


// Called by wifx control after each command is executed
function WebImageFXExecCommand(sEditorName, strCmdName, strTextData, lData)
{    

    if(stringStartsWith(strCmdName, "cmdsave") || strCmdName == "cmdopen"
       || strCmdName == "cmdcreatenew" || strCmdName == "cmdtwainaquire")
       wifxIsDirty = true;
               
    if("cmdmfuuploadall" == strCmdName)
    {
        wifxProcessServerResponse(sEditorName);

    }
    if("cmdcreatenew" == strCmdName)
    {
       wifxIsNewImage = true;
    }
    if("cmdhelp" == strCmdName)
    {
       wifxInvokeUserHelp();
    }

}

// Launches user help in another window
function wifxInvokeUserHelp()
{
   //First parse the rhythmyx root from the location href
   var pos = window.location.href.indexOf("/Rhythmyx");
   var rxRoot = window.location.href.substring(0, pos + 9);
   var url = rxRoot + "/" + "rx_resources/webimagefx/userguide_webimagefx.pdf";
   var opts = "menubar=0; scrollbars=0; status=0; resizable=1; width=600; height=500;";
   ps_wifxHelpWindow = window.open(url, "wifxHelpWindow", opts);
   ps_wifxHelpWindow.focus();
}

// Gets the servers redirect response header and redirects
function wifxProcessServerResponse(editorName)
{
   rxRedirect(wifxGetResponseHeader("WebImageFX-Redirect"));
}

// Gets the wifx auto upload object
function wifxGetAutoUploadObject()
{
    var objAutoUpload;
    var objEditor = WebImageFX.instances[0];

    if((null != objEditor) && ("undefined" != typeof objEditor))
    {
        objAutoUpload = objEditor.editor.AutomaticUpload();        
    }
    else
    {
        alert("ERROR:  could not get a WebImageFx editor instance object.");
    }    

    return objAutoUpload;
}

// Adds a form field to the wifx control so that it gets
// sent on upload also handles multiple values by adding a
// ";" delimiter
function wifxAddFormField(sFldName, sFldValue, isMulti)
{
    
    var objAutoUpload = wifxGetAutoUploadObject();
    if((null != objAutoUpload) && ("undefined" != typeof objAutoUpload))
    {
        var field = objAutoUpload.GetFieldValue(sFldName);
        if(field != null && field != 'undefined' && field.length > 0 && isMulti)
        {
           field += ";" + sFldValue;
        }
        else
        {
           field = sFldValue;
        }
        objAutoUpload.SetFieldValue(sFldName, field);
    }
    else
    {
        alert("Could not get an Auto-Upload object.");
    }
   
}

// Handles the redirect that comes back from the server by fixing the
// href to be an absolute path.
function rxRedirect(url)
{
   if(url == null || "undefined" == url || url.length ==  0)
      return;
   //First parse the rhythmyx root from the location href
   var pos = window.location.href.indexOf("/Rhythmyx");
   if(pos == -1)
   {
      alert("rxRedirect can only be used from a valid Rhythmyx context.");
      return;
   }
   var rxRoot = window.location.href.substring(0, pos + 9);
   var redirect = window.location.href;
   if(stringStartsWith(url.toLowerCase(), "http:"))
   {
      redirect = url;
   }
   else if(stringStartsWith(url, "../../"))
   {
      alert("rxRedirect cannot handle relative urls with a depth of 2 or more levels.");
      return;
   }
   else if(stringStartsWith(url, "../") || stringStartsWith(url, "./") || stringStartsWith(url, "/"))
   {
      redirect = rxRoot + "/" + url.substring(url.indexOf("/") + 1);
   }
   else
   {
      redirect = rxRoot + "/" + url;
   }

   window.location.href = redirect;

}

// Retrieves a response header by name
function wifxGetResponseHeader(key)
{
    var objAutoUpload = wifxGetAutoUploadObject();
    var nvpair = null;
    if((null != objAutoUpload) && ("undefined" != typeof objAutoUpload))
    {
       var lines = objAutoUpload.ReadResponseHeader().split("\n");
       for(i = 1; i < lines.length; i++)
       {
          nvpair = lines[i].split(": ");
          if(nvpair[0] == key)
             return nvpair[1];
       }
    }
    else
    {
       alert("Could not get an Auto-Upload object.");
    }
    return null;
}

// Set the transfer method(action) for the wifx auto upload object
function wifxSetTransferMethod(sTransferMethod)
{
    var objAutoUpload = wifxGetAutoUploadObject();
    if((null != objAutoUpload) && ("undefined" != typeof objAutoUpload))
    {
       objAutoUpload.setProperty("TransferMethod", sTransferMethod);
    }
    else
    {
       alert("Could not get an Auto-Upload object.");
    }
}

// Determines if a string starts with a certain text fragment
function stringStartsWith(str, fragment)
{
   return (str.substr(0, fragment.length) == fragment);
}

// Corrects the form action by turning into an absolute href
function fixFormAction(theAction)
{
   if(stringStartsWith(theAction, "http:"))
      return theAction;
   var path = window.location.href;
   var root = path.substring(0, path.lastIndexOf("/"));
   return root + "/" + theAction;
}

// Handles submiting the form via the wifx component
function wifxHandleSubmit(bool)
{   
   
   // If false is passed in then no form processing
   // should happen 
   if(!bool || !ps_hasWifx)
      return bool;
      
   var wifx =  WebImageFX.instances[0].editor;
   // Upload as usual if the wifx component is not dirty
   if(!wifx.IsDirty && !wifxIsDirty)
      return true;
         
   // First we need to save the image
   wifx.Save();
   if(wifxIsNewImage)
   {
      // This is a hack to get around a bug in the wifx control,
      // we need to re-load new images or the component thinks there
      // is no image to upload.
      wifx.EditFile(wifx.SavedFileName());
      wifxIsNewImage = false;
   
   }
   var theForm = document.forms[0];
   // Copy all of the form fields into the wifx component
   wifxCopyAllFormFields(theForm);
   wifxAddImageInfo();
   wifxAddFormField("webimagefxupload", "true", false); // This field must be set to get the required response 
   
   // Set the appropriate action
   wifxSetTransferMethod(fixFormAction(theForm.action));
   
   // invoke the upload
   wifx.ExecCommand('cmdmfuuploadall', '', 1);
   return false;
}

// Adds image information to the request as form fields
function wifxAddImageInfo()
{
   var wifx = WebImageFX.instances[0].editor;
   var name = "uploadfilephoto";
   wifxAddFormField(name + "_height", wifx.GetImageInformation("height"), false);
   wifxAddFormField(name + "_width", wifx.GetImageInformation("width"), false);
}

// Loops through all form fields and adds then to the wifx component
// for upload.
function wifxCopyAllFormFields(theForm)
{
   var delimit = ";";
   if(theForm != null)
     {
       elementCount = theForm.elements.length;
       for(i=0; i<elementCount; i++)
       {
      	currentEl = theForm.elements[i];   
   	      	
      	   if(currentEl.type == "checkbox" || currentEl.type == "radio")
      	   {
      	      if(currentEl.length == null || currentEl.length == 'undefined')
      	      {
      	         //Handle single checkbox and radio buttons
      	         if(currentEl.checked == true)
      	            wifxAddFormField(currentEl.name, currentEl.value, true); 
      	      }
      	      else
      	      {
      	         //Handle checkbox and radio button groups
      	              	         
      	         for(y=0; y < currentEl.length; y++)
      	         {      	            
      	            if(currentEl[y].checked == true)
      	            {
      	              wifxAddFormField(currentEl.name, currentEl[y].value, true);
      	            }
      	         }
      	         
      	      }
      	}
      	else if(currentEl.type == "select-one" || currentEl.type == "select-multiple")
      	{
           //Handle select boxes
      	   selections = "";
      	   for(y=0; y<currentEl.options.length; y++)
      	   {
      	      if(currentEl.options[y].selected == true)
      	      {
      	         selections += currentEl[y].value;
      	         if((y + 1) < currentEl.length)
      	         selections += delimit;     	      
      	      }
      	   }
      	   wifxAddFormField(currentEl.name, selections, currentEl.type == "select-multiple");
      	
      	}
      	else if(currentEl.type.length == 0 || currentEl.type == "submit" || currentEl.type == "reset")
      	{
      	   //Do nothing
      	}
      	else if(currentEl.name != "uploadfilephoto")
      	{
      	   wifxAddFormField(currentEl.name, currentEl.value, false);
      	}
      
    }
         
  }

}

// Loads the wifx component with the image as specified by the
// contentid and revision number. It handles building the url to 
// retrieve the binary from rhythmyx.
function wifxLoadImage()
{

   var theForm = document.forms[0];
   if(theForm != null && ps_hasWifx)
   {
      var theHref = window.location.href;
      var filetitle = theForm.uploadfilephoto_filename.value;
      var filetype = theForm.uploadfilephoto_type.value;
      var params = PSHref2Hash(theHref);
      var contentid = params["sys_contentid"];
      var revision = params["sys_revision"];

      if(filetitle != null && filetitle.length > 0 && contentid != null && contentid.length > 0)
      {
         // Create the url to retrieve the binary
         var tempimg = "/temp." + wifxGetTypeExt(filetype);
         var url = theHref.substring(0, theHref.indexOf("?"));
         url = url.substring(0, url.lastIndexOf("."));
         url += "?sys_command=binary";
         url += "&sys_contentid=" + contentid;
         url += "&sys_revision=" + revision;
         url += "&sys_submitname=uploadfilephoto";
         url += "&tmpimg=" + tempimg;
                  
         WebImageFX.instances[0].editor.EditFile(url);
         

      }
   }   

}

// Returns the appropriate extension for the mime type passed in
function wifxGetTypeExt(type)
{

   var map = new Array();
   map["image/gif"] = "gif";
   map["image/jpeg"] = "jpg";
   map["image/pjpeg"] = "jpg";
   map["image/jpe"] = "jpg";
   map["image/jpg"] = "jpg";
   map["image/png"] = "png";
   
   return map[type.toLowerCase()];
}


