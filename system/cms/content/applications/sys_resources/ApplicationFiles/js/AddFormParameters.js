
// Adds httpcaller and psredirect strings to the empty
// hidden fields if needed. These hidden fields must exist in
// the form or the function will fail.
function addFormRedirect(myform)
{
        
	
	if(myform.sys_currentview)
	{
		
		if((myform.sys_currentview.value=='sys_All' || myform.sys_currentview.value=='sys_Content' || myform.sys_currentview.value=='sys_ItemMeta' || myform.sys_currentview.value.indexOf('sys_SingleField')!=-1) && window.opener && myform.sys_pageid.value == 0)
		{
			var currenturl = window.location.href;
			
			myform.httpcaller.value = "../sys_uiSupport/redirect.html";
						
			if(myform.psredirect.value == "" && 
				(currenturl.indexOf("rc_createnew=yes")!=-1 || currenturl.indexOf("sys_folderid=")!=-1 ))
			{
				var sysContentId = "";
				var sysRevision = "";
				if(myform.sys_contentid != null)
				   sysContentId = myform.sys_contentid.value;
				if(myform.sys_revision != null)
				   sysRevision = myform.sys_revision.value;				   
				   
				myform.psredirect.value = buildRedirectURL(sysContentId, sysRevision);
			}

		}
	} 
	
	
	
}

/*
 * Modifies the sys page id
 */
function modifyPageId(myform, id)
{
   myform.sys_pageid.value = id;   
}

/*
 * Modifies the sys page id for Add New Item button.
 */
function addNewChildItem(id)
{
   	var h = PSHref2Hash();
	h["sys_pageid"] = id;
	window.location.href = PSHash2Href(h);
}

/*
 * Modifies params when onclick is
 * hit. Tries to find hidden fields to modify first.
 * If a hidden field does not exist it will tack on to
 * the action. This cannot be used on onsubmit
 * as "action" is immutable at that time
 */
function modifyFormParams(myform)
{

	// ARRAY OF ARGUMENT VALUES
    var argv = modifyFormParams.arguments;

    // THE NUMBER OF ARGUMENTS PASSED TO THIS FUNCTION
    var argc = argv.length;

    // start at 1, the first param is the form itself
    var startIndex = 1;
    	
    var name = "";
    var value = "";
    var del = "?";
    
    // LOOP THROUGH THE ARGUMENTS
    for (var i = startIndex; i < argc; i++)
    {
       name = argv[i];
       value = argv[i + 1];
       
       if(myform.elements[name] != null & false)
       {
          // Hidden field exists, change it
          myform.elements[name].value = value;
       }
       else
       {
         // Tack on to action
         if(myform.action.indexOf("?") != -1)
         {
            del = "&";
         }
            myform.action += del + name + "=" + value;
       }
       ++i;
    }


}

/*
 * This donothing version is required to avoid javaScript errors and may be 
 * modified later depending on the requirement.
 */
function setbodycontent()
{
	return true;
}

/*
 * This donothing version is required to avoid javaScript errors and may be 
 * modified later depending on the requirement.
 */
function getbodycontent()
{
}

/* Function to build the redirect url it will be do nothing redirect.html if the
 * request comes from a edit item. If request comes from a create new item, then
 * the item need to be inserted into the related content table.
 */
function buildRedirectURL(conid, rev)
{
	var redirecturl;
	var currenturl = window.location.href;

	if (currenturl.indexOf("rc_createnew=yes")!=-1)
	{
		var sys_activeitemid = parseParam("rcnew_activeitemid", currenturl); 
		var sys_contentid = parseParam("rcnew_contentid", currenturl); 
		var sys_revision = parseParam("rcnew_revision", currenturl); 
		var sys_slotid = parseParam("rcnew_slotid", currenturl); 
		var itemvariantid = parseParam("rcnew_itemvariantid", currenturl); 
		var folderid = parseParam("rcnew_folderid", currenturl); 
		redirecturl = "../sys_rcSupport/updaterelateditems.html?" + "sys_activeitemid=" + sys_activeitemid + "&sys_contentid=" + sys_contentid + "&sys_revision=" + sys_revision + "&sys_slotid=" + sys_slotid + "&itemvariantid=" + itemvariantid; 
		if (folderid != "")
		{
			redirecturl += "&sys_folderid=" + folderid
		}
	}
	else if (currenturl.indexOf("sys_folderid=")!=-1)
	{
		var sys_folderid = parseParam("sys_folderid", currenturl); 
		redirecturl = "../sys_cxSupport/redirectrefresh.html?" + "sys_folderid=" + sys_folderid + "&closeWindow=no&refreshHint=Selected"; 
	}
	else
	{
		redirecturl = "../sys_uiSupport/redirect.html?sys_revision=" + rev + "&sys_contentid=" + conid;
	}
	return redirecturl;
}
/*
 * Generic helper function te get a value of a given parameter
 * from the given href.
 */

function parseParam(param, href)
{
  var value = "";
  if(param == null || param=="")
	 return value;
  index = href.indexOf(param);
	if(index==-1){
		return value;
	}
	value = href.substring(index+param.length+1);
  index = value.indexOf("&");
  if(index == -1)
	 return value;
  value = value.substring(0, index);
  return value;
}
