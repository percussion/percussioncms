
var PS_SUB_MENU_IMG				= "../sys_resources/images/submenu.gif"
var PS_INVERTED_SUB_MENU_IMG	= "../sys_resources/images/submenuI.gif"
var PS_SEPERATOR_LINE_IMG		= "../sys_resources/images/line.gif"
var PS_SPACER_IMG				= "/sys_resources/images/spacer.gif"
var PS_NO_ENTRIES               = LocalizedMessage('no_entries');

var PSitemHeight  = 19;
var PSxOverlap    = 2;
var PSyOverlap    = 0;

var PSpopMenuDelay	= 400;
var PSmenuLookup	= new Array(); 
var PSmenuElement	= new Array();

var PSlastEnteredObj   = null;
// used for revision list
var PSRevisionNumber = -1;

function PSMenu(name, width, items)
{
	this.name = name;
	this.width = width;
	this.height = 0;		// set up later
	this.items = items;

	this.x = 0;
	this.y = 0;
	this.parentID = null;
	this.parentItemID = null;
}

function PSMenuItem(label, urlBuilder, img, disabled)
{
	this.label = label;
	this.urlBuilder = urlBuilder;
	this.img = img;
	this.disabled = disabled;
}

function PSCreateMenu(id, menu, parentID, parentItemID)
{
	if (id != null)
		menu.name = id;

	var menuIndex = PSmenuElement.length;

	PSmenuLookup[menu.name] = menuIndex;
	PSmenuElement[menuIndex] = menu;
	
	if (parentID != null && parentItemID != null)
	{
		menu.parentID = parentID;
		menu.parentItemID = parentItemID;
	}

	if(is_safari)
		PSCreateMenuSafari(menu, menuIndex);
	else
		PSCreateMenuIE(menu, menuIndex);	

	PSCalcMenuHeight(menu);
}

function PSTDObject(obj, id, itemId)
{
   this.obj = obj;
   this.id = id;
   this.itemId = itemId;
}

function PSCreateMenuIE(menu, menuIndex)
{
	var h = PSitemHeight;
	var spacerHTML = '<img align="absmiddle" src="' + PS_SPACER_IMG + '" width="21" height="19"/>';
	var menuBody = '';
	var noEntries = (menu.items.length == 0);
	
	for (var j = 0; j < menu.items.length; j++)
	{
		var item = menu.items[j];
		var subItems = (item.items != null);
		
		if (subItems)
			PSCreateMenu(null, item, menuIndex, j);

		if (item.img != null)
			spacerHTML = '<img align=absmiddle src="' + item.img + '"/>';

		var controlBlock = ' height="' + h 
					+ '" onmouseover="PSEnterItemIE(this,' + menuIndex + ', ' + j + ');"' 
					+ ' onmouseout="PSExitItemIE(this,' + menuIndex + ', ' + j + ');" ';

		var linkData;
		var subImg;
		if (subItems)
		{
			linkData =	'<td ' + controlBlock + ' nowrap="true">' 
						+	'<div style="float:left;width:80%;text-align:left">' + spacerHTML + item.name + '</div>'
						+	'<div style="float:left;width:20%;text-align:right">'
						+		'<img id="subIMG' + menuIndex + '-' + j + '" src="' + PS_SUB_MENU_IMG + '" align="absmiddle"/>&nbsp;'
						+	'</div>'
						+ '</td>';
		}
		else
		{
			if (item.label == "~")  // blank
			{
			  	linkData = '';
			}
			else if (item.label == "-")	// spacer
			{
				linkData = '<td><img src="' + PS_SEPERATOR_LINE_IMG + '" height="2" width="' + menu.width + '"/></td>';
			}
			else
			{
				if (!(item.disabled != null && item.disabled == true))
					controlBlock += ' onclick="' + item.urlBuilder + '; PSHideMenuTree(' + menuIndex + '); return false;"';
	
				linkData = '<td ' + controlBlock + ' nowrap="true">' + spacerHTML + item.label + '</td>';
			}
		}
		menuBody += '<tr>'
				 +		linkData
				 +	'</tr>\n';
	}
	if(noEntries)
	   menuBody += '<tr><td>' + spacerHTML + PS_NO_ENTRIES + '</td></tr>\n';

	var z = menuIndex + 1000;
	var menuHeader = '<div class="PSpopupmenu" id="PSXMenu' + menuIndex 
					+ '" onmouseover = "PSEnterMenu(' + menuIndex + ');"'
					+ ' onmouseout  = "PSExitMenu(' + menuIndex + ');"'
					+ ' style="position: absolute; z-index:' + z + '">'
					+ ' <table class="PSmenuitem" border="0" cellpadding="0" cellspacing="0" width="' + menu.width + '">';

	var menuFooter = '</table></div>';

	document.writeln(menuHeader + menuBody + menuFooter);
}


function PSCreateMenuSafari(menu, menuIndex)
{
	var h = PSitemHeight;
	var spacerHTML = '<img align=absmiddle src="' + PS_SPACER_IMG + '" width="21" height="19"/>';
	var menuBody = '';
	var noEntries = (menu.items.length == 0);
	
	for (var j = 0; j < menu.items.length; j++)
	{
		var item = menu.items[j];
		var subItems = (item.items != null);
		
		if (subItems)
			PSCreateMenu(null, item, menuIndex, j);

		if (item.img != null)
			spacerHTML = '<img align=absmiddle src="' + item.img + '">';

		var controlBlock = ' height="' + h 
					+ '" onmouseover="PSEnterItemIE(this,' + menuIndex + ', ' + j + ');"' 
					+ ' onmouseout="PSExitItemIE(this,' + menuIndex + ', ' + j + ');" ';

		var linkData;
		var subImg;
		if (subItems)
		{
			linkData =	'<td ' + controlBlock + ' nowrap="true">' 
						+	'<span style="width:80%;text-align:left">' + spacerHTML + item.name + '</span></td>'
						+	'<td><span style="width:20%;text-align:right">'
						+		'<img id="subIMG' + menuIndex + '-' + j + '" src="' + PS_SUB_MENU_IMG + '" align="absmiddle"/>'
						+	'</span>'
						+ '</td>';
		}
		else
		{
			if (item.label == "~")  // blank
			{
			  	linkData = '';
			}
			else if (item.label == "-")	// spacer
			{
				linkData = '<td><img src="' + PS_SEPERATOR_LINE_IMG + '" height="2" width="' + menu.width + '"/></td>';
			}
			else
			{
				if (!(item.disabled != null && item.disabled == true))
					controlBlock += ' ondblclick="' + item.urlBuilder + '; PSHideMenuTree(' + menuIndex + '); return false;"';
	
				linkData = '<td ' + controlBlock + ' nowrap="true">' + spacerHTML + item.label + '</td>';
			}
		}
		menuBody += '<tr>'
				 +		linkData
				 +	'</tr>\n';
	}
	if(noEntries)
	   menuBody += '<tr><td>' + spacerHTML + PS_NO_ENTRIES + '</td></tr>\n';

	var z = menuIndex + 1000;
	var menuHeader = '<div class="PSpopupmenu" id="PSXMenu' + menuIndex 
					+ '" onmouseover = "PSEnterMenu(' + menuIndex + ');"'
					+ ' onmouseout  = "PSExitMenu(' + menuIndex + ');"'
					+ ' style="position: absolute; z-index:' + z + '">'
					+ ' <table class="PSmenuitem" border="0" cellpadding="0" cellspaing="0" width="' + menu.width + '">';

	var menuFooter = '</table></div>';

	document.writeln(menuHeader + menuBody + menuFooter);
}

function PSRealScrollTop()
{
	var s = 0;
	if (document.all && document.compatMode && document.compatMode == "CSS1Compat")
		s = document.documentElement.scrollTop;
	else if (document.all && document.compatMode && document.compatMode == "BackCompat")
		s = document.body.scrollTop;
	else if (window.pageYOffset)
		s = window.pageYOffset;
	else 
		s = document.body.scrollTop;

	return s;
}

function PSRealScrollLeft()
{
	var s = 0;
	if (document.all && document.compatMode && document.compatMode == "CSS1Compat")
		s = document.documentElement.scrollLeft;
	else if (document.all && document.compatMode && document.compatMode == "BackCompat")
		s = document.body.scrollLeft;
	else if (window.pageXOffset)
		s = window.pageXOffset;
	else 
		s = document.body.scrollLeft;

	return s;
}

function PSEnterTopItem(name, event)
{
	if (event.button > 1) return false;
	
	var ID = PSmenuLookup[name];

	if (ID == null)
	{
		alert("Error, menu '" + name + "' was not loaded.");
		return;
	}
	PSReleaseMenuTree(ID);
	
	//Find the offset sizes if any
	var obj = PSgetObj('PSXMenu' + ID);
	var totalOffSetX = 0;
	var totalOffSetY = 0;
	while(obj!= null && obj.tagName.toLowerCase() != 'body')
	{
		obj = obj.offsetParent;
		if (obj != null)
		{
			totalOffSetX += obj.offsetLeft;
			totalOffSetY += obj.offsetTop;
		}
	}

	var x, y;
	if (is_nav4)
	{
		x = event.pageX - 5;
		y = event.pageY - 5;
	}
	else
	{
	   x = event.clientX + PSRealScrollLeft() - 5;
	   y = event.clientY + PSRealScrollTop() - 5;
	}

	//Adjust the offsets before moving
	x -= totalOffSetX;
	y -= totalOffSetY;
	
	PSMovePopMenu(ID, x, y);
	PSShowPopMenu(ID);
}

function PSExitTopItem(name)
{
	var ID = PSmenuLookup[name];
	if (ID == null)
		return;
	PSDelayHidePopMenu(ID);
}

function PSEnterItemIE(obj, ID, itemID)
{
    var highlighttextcolor = "highlighttext";
    var highlightbkgcolor = "highlight";
    if(is_safari)
    {
       highlighttextcolor = "white";
       highlightbkgcolor = "blue";
    }
           
    // Clean up menu items that did not properly exit
    PSCleanUpLeftOverObj();
    
    obj.style.old_color = obj.style.color;
    obj.style.color = highlighttextcolor;
    obj.style.old_backgroundColor = obj.style.backgroundColor;
    obj.style.backgroundColor = highlightbkgcolor;

	var img = PSgetImg("subIMG" + ID + "-" + itemID);
	if (img != null)
	{
		img.old_src = img.src;
		img.src = PS_INVERTED_SUB_MENU_IMG;	
	}
	PSlastEnteredObj = new PSTDObject(obj, ID, itemID);
	PSEnterItem(ID, itemID);
    	
}

// Safari had some issues with the mouseovers and was not
// cleaning up after itself. This function does a better cleanup.
function PSCleanUpLeftOverObj()
{
   if(PSlastEnteredObj == null)
      return;
   var obj = PSlastEnteredObj.obj;
   obj.style.color = obj.style.old_color;
   obj.style.backgroundColor = obj.style.old_backgroundColor;

   var img = PSgetImg("subIMG" + PSlastEnteredObj.id + "-" + PSlastEnteredObj.itemId);
   if (img != null)
      img.src = img.old_src;
   PSlastEnteredObj = null;   
}

function PSEnterItem(ID, itemID)
{
	for (var i = 0; i < PSmenuElement.length; i++)
	{
		if (PSmenuElement[i].parentID == ID &&
		    PSmenuElement[i].parentItemID == itemID)
		{
			PSClearHideTimer(i);
			PSMovePopMenu(i, null, null);
			PSDelayShowPopMenu(i);
			return 0;
		}
	}
	return -1;
}

function PSExitItemIE(obj, ID, itemID)
{
    obj.style.color = obj.style.old_color;
    obj.style.backgroundColor = obj.style.old_backgroundColor;

	var img = PSgetImg("subIMG" + ID + "-" + itemID);
	if (img != null)
		img.src = img.old_src;
	PSlastEnteredObj = null;

	PSExitItem(ID, itemID);
}

function PSExitItem(ID, itemID)
{
	for (var i = 0; i < PSmenuElement.length; i++)
	{
		if (PSmenuElement[i].parentID == ID &&
		    PSmenuElement[i].parentItemID == itemID)
		{
			PSClearShowTimer(i);
			PSDelayHidePopMenu(i);
			return 0;
		}
	}
	return -1;
}

function PSEnterMenu(ID)
{
	
	var parentID = PSmenuElement[ID].parentID;
	if (parentID == null)
		PSClearHideTimer(ID);
	else
		PSReleaseMenuTree(ID);
}

function PSExitMenu(ID)
{
	PSTimeoutMenuTree(ID);
}

function PSHideMenuTree(ID)
{
	PSHidePopMenu(ID);
	for (var i = 0; i < PSmenuElement.length; i++)
	{
		if (PSmenuElement[i].parentID == ID && PSmenuElement[i].isOn)
			PSHideMenuTree(i);
	}
}

function PSReleaseMenuTree(ID)
{
	PSClearHideTimer(ID);
	var parentID = PSmenuElement[ID].parentID;
	if (parentID != null)
		PSReleaseMenuTree(parentID);
}

function PSTimeoutMenuTree(ID)
{
	PSDelayHidePopMenu(ID);
	var parentID = PSmenuElement[ID].parentID;
	if (parentID != null)
		PSTimeoutMenuTree(parentID);
}

function PSCalcMenuHeight(menu)
{
	var h = PSitemHeight;
	menu.height = 0;
	
	for (var j = 0; j < menu.items.length; j++)
	{
		var item = menu.items[j];
		if (item.label == "-")		// spacer is less height
			menu.height += 2 + 2;
		else
			menu.height += (h + 2);
	}
}

function PSMovePopMenu(ID, x, y)
{
	var h = PSitemHeight;
	var menu = PSmenuElement[ID];

	// fix position to remain within the client area of the browser
	var scrollLeft = PSRealScrollLeft();
	var scrollTop = PSRealScrollTop();
	var clientWidth = ((is_ie) ? document.body.clientWidth : window.innerWidth) - 6;
	var clientHeight = ((is_ie) ?  document.body.clientHeight : window.innerHeight) - 6;

	// if a sub menu calc based on parent
	if (x == null && y == null)
	{
		if (menu.parentID != null && menu.parentItemID != null)
		{
			var p = PSmenuElement[menu.parentID];
			x =  p.x + p.width + PSxOverlap;
			y =  p.y + ((h + 2) * menu.parentItemID) + PSyOverlap;

			if ((x - scrollLeft) + menu.width > clientWidth)
				x = (p.x - menu.width - PSxOverlap) + scrollLeft;
		}
	}
	else
	{
		if ((x - scrollLeft) + menu.width > clientWidth)
			x = (clientWidth - menu.width) + scrollLeft;
	}
	if ((y - scrollTop) + menu.height > clientHeight)
		y = (clientHeight - menu.height) + scrollTop;

	// fix edge cases
	x = x < 0 ? 0 : x;
	y = y < 0 ? 0 : y;

	// reset internal data
	menu.x = x;
	menu.y = y;

	// now move the actual layer/div
	var obj = PSgetObj('PSXMenu' + ID);

	obj = obj.style;
	obj.left = menu.x + "px";
	obj.top = menu.y + "px";
}

function PSShowPopMenu(ID)
{
	PSmenuElement[ID].isOn = true;
	PSShowObj('PSXMenu' + ID);
}

function PSHidePopMenu(ID)
{
	PSmenuElement[ID].isOn = false;
	PSHideObj('PSXMenu' + ID);
}

function PSShowObj(name)
{
	var obj = PSgetObj(name);
	obj.style.visibility = "visible";
}

function PSHideObj(name)
{
	var obj = PSgetObj(name);
	obj.style.visibility = "hidden";
}

function PSDelayShowPopMenu(ID)
{
	PSmenuElement[ID].showTimerID = setTimeout('PSShowPopMenu (' + ID + ')', PSpopMenuDelay);
}

function PSDelayHidePopMenu(ID)
{
	PSmenuElement[ID].hideTimerID = setTimeout('PSHidePopMenu (' + ID + ')', PSpopMenuDelay);
}

function PSClearShowTimer(ID)
{
	clearTimeout(PSmenuElement[ID].showTimerID);
	PSmenuElement[ID].showTimerID = -1;
}

function PSClearHideTimer(ID)
{
	clearTimeout(PSmenuElement[ID].hideTimerID);
	PSmenuElement[ID].hideTimerID = -1;
}

function PSgetObj(name)
{
	var obj = null;

	if (document.getElementById)
		obj = document.getElementById(name);
	
	else if (document.all)
		obj = document.all[name];
	
	else
		obj = getImg(name);

	return obj;		
}

function PSgetImg(name)
{
	return document.images[name];
}

function PSMenuItemSelected(ID)
{
	PSHideMenuTree(ID);
}

function PSTogglePreview(preview)
{
	var gotoPreview = !preview;
	var h = PSHref2Hash();
	
	if (gotoPreview)
	{
		h["sys_lastid"] = h["sys_activeitemid"];
		h["sys_activeitemid"] = "";
	}
	else
	{
		h["sys_activeitemid"] = h["sys_lastid"];
		h["sys_lastid"] = null;
	}

	var x = PSHash2Href(h);
	window.location.href = x;
}

function PSBuildDummy(url)
{
	alert("url = " + url);
}

// Do something reasonable for actions that are not treated
// specially in the action pages
function PSBuildAction(name, url, contentid, revisionid)
{
  if (url != null && url != "")
  {
	if (url.indexOf("?") < 0)
	{
		// Make this a query url
		url = url + "?";
	}
	else
	{
		url = url + "&";
	}
	url = url + "sys_contentid=" + contentid + "&sys_revision=" + revisionid;
	var w = window.open(url, name);
    w.focus();
  }
  else
  {
    alert("Unimplemented client action " + name);
  }
}

function PSActivateParent()
{
	var x = PSParentURL();
	if(x.indexOf("parentPage")==-1)
		x += "&parentPage=yes";
	window.location.href = x;
}

function PSParentURL()
{
	var h = PSHref2Hash();

	// clear the breadcrumb trail
	h["sys_trail"] = null;
	
	// clear the active item
	h["sys_activeitemid"] = null;

	var url = PSHash2Href(h);
	return url;
}

function PSActivateItem(id)
{
	var h = PSHref2Hash();

	// build the breadcrumb trail
	if (h["sys_trail"] == null)
		h["sys_trail"] = id;
	else
		h["sys_trail"] += ":" + id;

	h["sys_activeitemid"] = id;
	
	var y = PSHash2Href(h);
	var tmp = null;
	if(y.indexOf("?parentPage=yes&amp;")!=-1)
		tmp = y.split("parentPage=yes&amp;");
	else if(y.indexOf("?parentPage=yes&")!=-1)
		tmp = y.split("parentPage=yes&");
	else if(y.indexOf("&amp;parentPage=yes")!=-1)
		tmp = y.split("&amp;parentPage=yes");
	else if(y.indexOf("&parentPage=yes")!=-1)
		tmp = y.split("&parentPage=yes");
	var x = y;
	if(tmp != null)
	{
		x=tmp[0];
		if(tmp.length > 1)
			x += tmp[1];
	}

	window.location.href = x;
}

function PSActivateBreadCrumbItem(id, trail)
{
	var h = PSHref2Hash();

	h["sys_activeitemid"] = id;

	// update the breadcrumb trail
	h["sys_trail"] = trail.substring(0, trail.indexOf(id) + id.length);

	var x = PSHash2Href(h);

	window.location.href = x;
}

function PSActivateBreadCrumbParentItem()
{
	var x = PSBreadCrumbParentItemURL();
	if(x.indexOf("parentPage")==-1)
		x += "&parentPage=yes";
	window.location.href = x;
}

function PSBreadCrumbParentItemURL()
{
	var url;
	var h = PSHref2Hash();

	var trail = h["sys_trail"].split(":");
	if (trail.length - 1 < 0)
		url = PSParentURL();
	else
	{
		// update the breadcrumb trail
		trail = trail.slice(0,-1);		// clear out the last item
		h["sys_activeitemid"] = trail[trail.length - 1];
		
		// build the new trail
		var tmp = trail[0];
		for (i = 1; i < trail.length; i++)
		{
			if (trail[i] != null)
				tmp += ":" + trail[i];
		}
		h["sys_trail"] = tmp;
		
		url = PSHash2Href(h);
	}
	return url;
}

function PSMoveItem(command, url, sysid)
{
	var redir = window.location.href;
	if (command == "delete")
		redir = PSBreadCrumbParentItemURL();
	
	var x = appendQuestionOrAmpersandtoURL(url);
	
	x += "sys_command=" + command + "&sysid=" + sysid;
	x += "&httpcaller=" + escape(redir);

	window.location.href = x;
}

function PSAddItem(url, slotname)
{
	var x = appendQuestionOrAmpersandtoURL(url);
	var h = PSHref2Hash(window.location.href);
	var contentid = h["sys_contentid"];
	var revision = h["sys_revision"];
	var activeitemid =  h["sys_activeitemid"];
	if(activeitemid == null)
		activeitemid = 0;
	x += "sys_slotname=" + slotname + "&sys_contentid=" + contentid + "&sys_activeitemid=" + activeitemid + "&sys_revision=" + revision + "&sys_mode=Site Centric";
	var w = window.open(x, "searchitems", "toolbar=0,location=0,directories=0,status=0,menubar=0,scrollbars=1,resizable=1,width=720,height=400");
	w.focus();
}

function PSModifyItem(url, sysid)
{
	var x = appendQuestionOrAmpersandtoURL(url);
	
	x += "sysid=" + sysid;
	var w = window.open(x, "modifyslotitem", "toolbar=0,location=0,directories=0,status=0,menubar=0,scrollbars=1,resizable=1,width=425,height=200");
	w.focus();
}

function PSEditContent(url, sysconid, sysrevid)
{
	var x = appendQuestionOrAmpersandtoURL(url);
	
	x += "sys_view=sys_All";
	if(sysconid!="")
		x += "&sys_contentid=" + sysconid + "&sys_revision=" + sysrevid;

	var p = "location=1,directories=0,status=0,menubar=0,scrollbars=1,resizable=1";
	if (is_ie)
		p += ",toolbar=0";
	else
		p += ",toolbar=1";
	var w = window.open(x, "editcontent", p);	
	w.focus();
	return w;
}

function PSQuickEditContent(url, sysconid, sysrevid)
{
	var x = appendQuestionOrAmpersandtoURL(url);
	
	x += "sys_view=sys_All&WFAction=Quick Edit";
	if(sysconid!="")
		x += "&sys_contentid=" + sysconid + "&sys_revision=" + sysrevid;

	var p = "location=1,directories=0,status=0,menubar=0,scrollbars=1,resizable=1";
	if (is_ie)
		p += ",toolbar=0";
	else
		p += ",toolbar=1";

	var w = window.open(x, "editcontent", p);	
	w.focus();
	return w;
}

function PSViewContent(url, sysconid, sysrevid)
{
	if (url == null || url == "")
	{
		url = window.location.href;
	}
	var x = url.split("?")[0] + "?sys_contentid=" + sysconid + "&sys_revision=" + sysrevid + "&sys_pageid=0&sys_command=preview&sys_view=sys_All";

	var p = "location=1,directories=0,status=0,menubar=0,scrollbars=1,resizable=1";
	if (is_ie)
		p += ",toolbar=0";
	else
		p += ",toolbar=1";

	var w = window.open(x, "editcontent", p);	
	w.focus();
}

function PSEditMeta(url, sysconid, sysrevid)
{
	var x = appendQuestionOrAmpersandtoURL(url);
	
	x += "sys_contentid=" + sysconid + "&sys_revision=" + sysrevid + "&sys_view=sys_ItemMeta";
	var w = window.open(x, "editmeta", "toolbar=0,location=0,directories=0,status=0,menubar=0,scrollbars=1,resizable=1,width=600,height=400");
	w.focus();
}

function PSViewMeta(url, sysconid, sysrevid)
{
	if (url == null || url == "")
	{
		url = window.location.href;
	}
	var x = url.split("?")[0] + "?sys_contentid=" + sysconid + "&sys_revision=" + sysrevid + "&sys_pageid=0&sys_command=preview&sys_view=sys_ItemMeta";
	var w = window.open(x, "viewmeta", "toolbar=0,location=0,directories=0,status=0,menubar=0,scrollbars=1,resizable=1,width=600,height=400");
	w.focus();
}

function PSCompare(url, sysconid, sysrevid, sysvarid, sysfolderid, syssiteid)
{
	var x = appendQuestionOrAmpersandtoURL(url);
	x += "activeitem=1&sys_contentid1=" + sysconid + "&sys_revision1=" + sysrevid;
	if (sysvarid!="")
	{
		x += "&sys_variantid1=" + sysvarid;
	}
	if (sysfolderid!="")
	{
		x += "&sys_folderid=" + sysfolderid;
	}
	if (syssiteid!="")
	{
		x += "&sys_siteid=" + syssiteid;
	}
	
	var w = window.open(x, "compare");
	w.focus();
}

function PSEditField(url, sysconid, sysrevid, fieldname)
{
	var x = appendQuestionOrAmpersandtoURL(url);
	
	x += "sys_contentid=" + sysconid + "&sys_revision=" + sysrevid + "&sys_view=sys_SingleField:" + fieldname;
	var w = window.open(x, "editfield", "toolbar=0,location=0,directories=0,status=0,menubar=0,scrollbars=1,resizable=1,width=500,height=200");
	w.focus();
}

function PSNewVersion(url, sysconid, sysrevid)
{
	var x = url.split("?")[0] + "?sys_contentid=" + sysconid + "&sys_revision=" + sysrevid + "&sys_pageid=0&sys_command=relate&sys_relationshiptype=NewCopy&sys_view=sys_All";
	var w = window.open(x, "newversion", "toolbar=0,location=0,directories=0,status=0,menubar=0,scrollbars=1,resizable=1,width=600,height=400");
	w.focus();
}

function PSPromotableVersion(url, sysconid, sysrevid)
{
	var x = url.split("?")[0] + "?sys_contentid=" + sysconid + "&sys_revision=" + sysrevid + "&sys_pageid=0&sys_command=relate&sys_relationshiptype=PromotableVersion&sys_view=sys_All";
	var w = window.open(x, "promotableversion", "toolbar=0,location=0,directories=0,status=0,menubar=0,scrollbars=1,resizable=1,width=600,height=400");
	w.focus();
}

function PSEditSimpleItem(url)
{
	var w = window.open(url, "editsimpleitem", "toolbar=0,location=0,directories=0,status=0,menubar=0,scrollbars=1,resizable=1,width=500,height=200");
	w.focus();
}

function PSPreviewItem(url,sysconid,sysvarid,sysrevid,siteid,folderid,sessionid)
{
	var x = appendQuestionOrAmpersandtoURL(url);
	
	x += "pssessionid=" + sessionid + "&sys_folderid=" + folderid + "&sys_siteid=" + siteid + "&sys_contentid=" + sysconid + "&sys_variantid=" + sysvarid + "&sys_revision=" + sysrevid + "&sys_authtype=0&sys_context=0";
	var w = window.open(x, "previewitem");
	w.focus();
}

/**
 * @param {String} assemblyUrl the value to pass to url in sys_assemblyurl parameter.
 * Points to the assembly url to forward to after the current action is done.
 */
function PSActiveAssemblyItem(url,sysconid,sysvarid,sysrevid,siteid,folderid,sessionid,assemblyUrl)
{
	var x = appendQuestionOrAmpersandtoURL(url);
	
	x += "pssessionid=" + sessionid + "&sys_folderid=" + folderid + "&sys_siteid=" + siteid + "&sys_contentid=" + sysconid + "&sys_variantid=" + sysvarid + "&sys_revision=" + sysrevid + "&sys_authtype=0&sys_context=0&sys_command=editrc&parentPage=yes";
	if (assemblyUrl)
	{
	   x += "&sys_assemblyurl=" + encodeURIComponent(assemblyUrl);
	}
	var w = window.open(x, "activeassembly");
	w.focus();
}

function PSAuditTrail(url, sysconid, sysrevid)
{
	var x = url.split("?")[0] + "?sys_contentid=" + sysconid + "&sys_revision=" + sysrevid + "&sys_command=preview&sys_userview=sys_audittrail";
	var w = window.open(x, "audittrail", "toolbar=0,location=0,directories=0,status=0,menubar=0,scrollbars=1,resizable=1,width=600,height=200");
	w.focus();
}

function PSShowRevisions(url, sysconid, sysrevid)
{
	var x = url.split("?")[0] + "?sys_contentid=" + sysconid + "&sys_revision=" + sysrevid + "&sys_command=preview&sys_userview=sys_Revisions";
	var w = window.open(x, "showrevisions", "toolbar=0,location=0,directories=0,status=0,menubar=0,scrollbars=1,resizable=1,width=600,height=200");
	w.focus();
}

function PSBuildWFAction(url, checkintransitionurl, transitionname, commentrequired, showadhoc, sysconid, sysrevid, systransid, syswfaction, syscommand, fromRoles)
{
	//While generating the PSBuildWFAction function call single quotes in transitionname and syswfaction will be replaced by _psxapos_, these need to be replaced by back by single quotes
	var re = new RegExp("_psxapos_","g");
	transitionname = transitionname.replace(re,"'");
	syswfaction = syswfaction.replace(re,"'");
	var x = appendQuestionOrAmpersandtoURL(url);
	x += "transitionName=" + transitionname + "&commentRequired=" + commentrequired + "&sys_contentid=" + sysconid + "&sys_transitionid=" + systransid + "&fromaa=yes";
	var w = window.open(x, "workflowtransition", "toolbar=0,location=0,directories=0,status=0,menubar=0,scrollbars=0,resizable=1,width=260,height=345");
	w.focus();
}

function PSCheckinCheckout(url, sysconid, sysrevid, wfaction, actionsetid, checkoutuser)
{
	var confirmAction = true;
	var x = url.split("?")[0] + "?sys_contentid=" + sysconid + "&sys_revision=" + sysrevid + "&sys_command=workflow&WFAction=" + wfaction;

	var redir = window.location.href;
	var h = PSHref2Hash();

	if (actionsetid == "Parent" && 
		(wfaction == "checkin" || wfaction == "forcecheckin"))
	{
		h["sys_revision"] = sysrevid;
		redir = PSHash2Href(h);
	}

	if (actionsetid == "Parent" && wfaction == "checkout")
	{
		h["sys_revision"] = ":PSXSingleHtmlParameter/sys_revision";
		redir = PSHash2Href(h);
	}
	x += "&psredirect=" + escape(redir);

	if(wfaction == "forcecheckin")
		confirmAction = PSConfirmOverride(checkoutuser);
	if(confirmAction)	
		window.location.href = x;
}

function PSConfirmOverride(checkoutuser)
{
  
  var msg = LocalizedMessage("override_checkout_warning_part1")+checkoutuser;
  msg += LocalizedMessage("override_checkout_warning_part2");  
  
  return window.confirm(msg);
}

function PSCreateItem(url, slotname, folderid)
{
	var x = appendQuestionOrAmpersandtoURL(url);
	
	x += "sys_slotname=" + slotname;
	if (folderid != '')
	{
		x += "&sys_folderid=" + folderid;
	}
	
	var w = window.open(x, "createitem", "toolbar=0,location=0,directories=0,status=0,menubar=0,scrollbars=1,resizable=1,width=600,height=400");
	w.focus();
}

function PSFieldEditSpan(start)
{
	if (!is_nav4)
	{
		if (start)
			document.write('<span style="position:absolute">');
		else
			document.write('</span>');
	}
}

function PSRevisionViewContent(url, sysconid)
{
	PSViewContent(url, sysconid, PSRevisionNumber);
}

function PSRevisionViewProperties(url, sysconid)
{
	PSViewMeta(url, sysconid, PSRevisionNumber);
}

function PSRevisionPromote(url, sysconid)
{
	var w = PSEditContent(url, sysconid, PSRevisionNumber);
	if (w && window.opener)
	{
		w.window.opener = window.opener;
	}
	window.close();
}

function PSPromoteVersion(url, sysconid, rev)
{
	var w = PSEditContent('?sys_command=workflow&WFAction=checkout', sysconid, rev);
	if (w && window.opener)
	{
		w.window.opener = window.opener;
	}
	window.close();
}

function PSTranslateItem(url, sysconid, revision)
{
	var x = url.split("?")[0] + "?sys_contentid=" + sysconid + "&sys_revision=" + revision + "&sys_relationshiptype=Translation";
	var w = window.open(x, "translateitem", "toolbar=0,location=0,directories=0,status=0,menubar=0,scrollbars=1,resizable=1,width=450,height=300");
	w.focus();
}

function PSFlushCache(url, sysconid, revision, variantid)
{
	var x = appendQuestionOrAmpersandtoURL(url);
	
	x += "sys_contentid=" + sysconid + "&sys_revision=" + revision + "&sys_variantid=" + variantid;
	var w = window.open(x, "FlushCache", "toolbar=0,location=2000,directories=0,status=0,menubar=0,scrollbars=1,resizable=1,width=100,height=100");
}

function OpenCMS(cmsurl)
{
	if (window.opener && !window.opener.closed)
	{
		window.opener.location.reload();
		window.opener.focus();
		self.close();
	}
	else
	{
		window.location.href =  cmsurl;
		window.focus();
	}
}
function appendQuestionOrAmpersandtoURL(url)
{
	return (url.indexOf("?") != -1)?(url + "&"):(url + "?");
}
function PSPurgeItem(url, sessionid, sysconid)
{
	var x = appendQuestionOrAmpersandtoURL(url) + "sys_contentid=" + sysconid + "&pssessionid=" + sessionid;
	var w = window.open(x, "purgitem", "toolbar=0,location=0,directories=0,status=0,menubar=0,scrollbars=1,resizable=1,width=450,height=300");
	w.focus();
}

function PSClipboardCopy(value)
{
  if (is_ie)
  {
     clipboardData.setData('Text',value);
  }
  else 
  {
    var popup = window.open("", "Show Link","toolbar=0,menubar=0,status=0,height=160");
    var doc = popup.document;
    doc.write("<H3>The following link can be copied and pasted into your application to access the current content item</H3><p>" + value); 
    doc.write("<br><br><input type='submit' onclick='window.close()' value='Close Window'>");
  }

}
