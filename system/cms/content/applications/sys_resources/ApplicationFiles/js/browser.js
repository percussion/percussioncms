//
// browser sniffer code, partially from
// http://www.mozilla.org/docs/web-developer/sniffer/browser_type.html
//
var agt = navigator.userAgent.toLowerCase();
var is_major = parseInt(navigator.appVersion);
var is_minor = parseFloat(navigator.appVersion);

var is_win = ((agt.indexOf("win") != -1) || (agt.indexOf("16bit") != -1));
var is_mac = (agt.indexOf("mac")!= -1);

var is_safari = (is_mac && (agt.indexOf("safari") !== -1));
var is_ie = ((agt.indexOf("msie") !== -1) && (agt.indexOf("opera") === -1));
var is_nav  = ((agt.indexOf('mozilla') !== -1) && (agt.indexOf('spoofer') === -1) && (agt.indexOf('compatible') === -1) && (agt.indexOf('opera') === -1) && (agt.indexOf('webtv') === -1) && (agt.indexOf('hotjava') === -1) && !is_safari);

var is_mozilla = ((agt.indexOf('mozilla') !== -1) && (agt.indexOf('gecko') !== -1));
var is_nav4 = (is_nav && (is_major === 4));
var is_firefox = agt.indexOf('firefox') !== -1;

// The following injects a script call into the DOM that fetches the user's
// session id. It is only useful for Firefox and Safari. (see RX-13675)
// It is not needed in IE and, in fact, will cause an HTML/Javascript 
// parsing error in IE 6. (see http://support.microsoft.com/default.aspx/kb/927917) 
if (is_firefox || is_safari) 
{
   var pssessionScript = document.createElement('script');
   pssessionScript.type = 'text/javascript';
   pssessionScript.src = '/ui/util/getPSSessionID.jsp';
   document.getElementsByTagName('head')[0].appendChild(pssessionScript);
}

var is_chrome = agt.indexOf('chrome') != -1;
var is_dce = navigator.userAgent.indexOf("PercussionDCE/") > -1;


// the following code is from http://www.webreference.com/dhtml/diner/resize/resize4.html
// it is used to fix netscape when you resize the screen and you are using DHTML
function reDo()
{
   if (innerWidth != origWidth || innerHeight != origHeight)
   {
      location.reload();
   }
}

if (is_nav4)
{
   origWidth = innerWidth;
    origHeight = innerHeight;
   onresize = reDo;
}

//
// fix the loaded popmenu stylesheet for ie win only
// the color names are only defined for ie win
//
if (is_win && is_ie)
{
   var ss = document.styleSheets;
   for (var i = 0; i < ss.length; i++)
   {
      var tmp = ss[i].href;
      if (tmp.indexOf("popmenu.css") != -1)
      {
         var rules = ss[i].rules;
         if (rules)
         {
            for (var j = 0; j < rules.length; j++)
            {
               var theStyle = rules[j].style;
               switch(rules[j].selectorText)
               {
                  case ".PSmenuitem":
                     theStyle.color = "menutext";
                     theStyle.cursor = "default";
                     break;

                  case ".PSnavmenuitem":
                     theStyle.color = "menutext";
                     theStyle.cursor = "hand";
                     break;

                  case ".PSpopupmenu":
                     theStyle.backgroundColor = "menu";
                     break;

                  case ".PSbreadcrumb":
                     theStyle.color = "menutext";
                     break;

                  case ".PStopnavbar":
                     theStyle.backgroundColor = "buttonface";
                     break;
               }
            }
         }
         break;
      }
   }
}

var AjaxSwingProxy = function(){
	var hostBase = "";
	var window;
	return {
		setBase : function(base){
			hostBase  = base;
		},
		setWindow : function(win){
			window  = win;
		},
		refresh : function(hint, contentids, revisionids){
		  window.document.getElementById('maincontent').src=hostBase+"?refresh=true&hint="+hint+"&contentids="+contentids+"&revisionids="+revisionids;
		}
	};
}();

var WebStartProxy = function(){
	var hostBase = "";
	var window;
	return {
		setBase : function(base){
			hostBase  = base;
		},
		setWindow : function(win){
			window  = win;
		},
		refresh : function(hint, contentids, revisionids){
		  window.document.getElementById('maincontent').src=hostBase+"?refresh=true&hint="+hint+"&contentids="+contentids+"&revisionids="+revisionids;
		}
	};
}();


function PSGetApplet(win, name)
{

	 if (typeof(win.contentexplorer) != "undefined")
	 {

	     return win.contentexplorer;
	 }

   var doc = win.document;
   var obj = null;

   if (doc.getElementById)
   {
      obj = doc.getElementById(name);
      if (obj != null)
         return obj;
   }

   if (doc.all)
   {
      obj = doc.all[name];
      if (obj != null)
         return obj;
   }

   if (is_nav4)
   {
      obj = PSNetscapeGetObj(doc, name);
      if (obj != null)
         return obj;
   }
   
   if(is_firefox || is_chrome)
   {
      obj = doc.applets[name];
      if (obj != null)
         return obj;
   
      obj = doc.embeds[name];
      if (obj != null)
      return obj;
   }

   if (doc.frames != null)
   {
      for (var i = 0; i < doc.frames.length; i++)
      {
         obj = PSGetApplet(doc.frames[i], name);
         if (obj != null)
            return obj;
      }
   }

   if (win.frames != null)
   {
      for (var j = 0; j < win.frames.length; j++)
      {
         obj = PSGetApplet(win.frames[j], name);
         if (obj != null)
            return obj;
      }
   }

   return obj;
}

function PSNetscapeGetObj(doc, name)
{
   var obj = null;

   obj = doc.applets[name];
   if (obj != null)
      return obj;

   obj = doc.embeds[name];
   if (obj != null)
      return obj;

   obj = doc.layers[name];
   if (obj != null)
      return obj;

   obj = doc.forms[name];
   if (obj != null)
      return obj;

   for (var i = 0; i < doc.forms.length; i++)
   {
      obj = doc.forms[i].elements[name];
      if (obj != null)
         return obj;
   }

    for (var k = 0; k < doc.layers.length; k++)
    {
      obj = PSNetscapeGetObj(doc.layers[k].document, name);
      if (obj != null)
         return obj;
   }
   return obj;
}

// Find an ancestor window with the variable actionPageRefresh
// set and reload it.
function reloadOpener(win)
{
  var parent = win.opener;
  try
  {
     if (parent==null || parent.closed) 
     {
       return null;
     }
  }
  catch (e)
  {
     // An exception may be thrown due to a bug in IE in which
     // closed parent windows remain as objects.  In FireFox,
     // the window objects are set to null when closed.
     return null;
  }

  /*if (parent['actionPageRefresh'] != null)
  {
    parent.location.reload();
  }
  else
  {
    return reloadOpener(parent);
  }*/
}

function refreshCxApplet(win, hint, contentids, revisionids)
{

      if (typeof(contentexplorer) != "undefined")
      {

         contentexplorer.refresh(hint,contentids,revisionids);
         javaInject();
         return;
      }

		 // As written, this code assumes that the CX is in the second
		 // frame of two frames. If this assumption changes, this code
		 // should be updated.
		 if (win != null && win.frames != null && win.frames[1] != null)
		 {
	     win.frames[1].refreshCxApplet(hint, contentids, revisionids); 
		 } else if (ajaxSwingEnabled) {
		 	AjaxSwingProxy.refresh(hint,contentids,revisionids);
		 }
}

// Display java help
function _showHelp()
{
		
	if(is_safari || (is_ie == true && is_mac == true))
	   if(arguments.length == 1) 
	      document.applets.help.showHelp(arguments[0]);
	   else
	      document.applets.help.showHelp();
	else
	   if(arguments.length == 1) 
	      document.help.showHelp(arguments[0]);
	   else
	      document.help.showHelp();
}

// Display workflow tab help
function _showWorkflowTabHelp(helpPage)
{
	if (window != null)
	{
	   var hwin = window.open("../Docs/Rhythmyx/Rhythmyx_Workflow_Tab_Help/index.htm?toc.htm?" + helpPage,"HelpWindow");
	   hwin.focus();
	}
}

// An object that represents a name/value pair
function NVPair(name, value)
{
   this.name = name;
   this.value = value;
}   

// Applet caller object is used to appropriately call an applet
// for the browser that is automatically detected
function AppletCaller()
{
   this.attribs = [];
   this.params = [];
}

// Adds a parameter/attribute for the Applet
function AppletCaller_addParam(name, value)
{
   var type = "embed";
   if(is_safari || (is_ie == true && is_mac == true))
      type = "applet";
   else if(is_ie == true)
      type = "object";
   if(isTagAttrib(type, name))
   {
      if(type == "object")
      {
         if(name.toLowerCase() == "codebaseattr")
            name = "codebase";
         if(name.toLowerCase() == "typeattr")
            name = "type";
      }
      if(type == "embed")
      {
         if(name.toLowerCase() === "codebaseattr" || name.toLowerCase() === "typeattr" || name.toLowerCase() === "classid" || name.toLowerCase() === "id")
            return;
        if(is_mozilla == true && name.toLowerCase() == "type")
           value = "application/x-java-applet";
      }
      var idx = this.attribs.length;
      for(i = 0; i < this.attribs.length; i++)
      {
         if(this.attribs[i].name.toLowerCase() == name.toLowerCase())
            idx = i;
      }
      this.attribs[idx] = new NVPair(name, value);
   }  
   else
   {
      if(name.toLowerCase() == "classid" || name.toLowerCase() == "codebaseattr" || name.toLowerCase() == "typeattr")
         return;      
      var idx1 = this.params.length;
      for(i = 0; i < this.params.length; i++)
      {
         if(this.params[i].name.toLowerCase() == name.toLowerCase())
            idx1 = i;
      }
      this.params[idx1] = new NVPair(name, value);
   }
}
AppletCaller.prototype.addParam = AppletCaller_addParam;

// Renders the HTML to display the Applet
function AppletCaller_show()
{
   var type = "embed";
   if(is_safari || (is_ie == true && is_mac == true))
      type = "applet";
   else if(is_ie == true)
      type = "object";
   
   var buffer = "";
   document.open();
   buffer += writeAppletStartTag(type, this.attribs);
   buffer += writeAppletParams(this.params);
   // Uncomment for debug
   // alert(buffer);
   if(type == "object" || type == "embed")
   {
      document.writeln("<NOEMBED>");
      document.writeln("No Java 2 SDK support for Applet!!");
      document.writeln("</NOEMBED>");
   }
   document.writeln("</" + type + ">");
   document.close(); 
   
   
}
AppletCaller.prototype.show = AppletCaller_show;

// Returns true if the name specified is an attribute for the
// specified type of tag (Applet, Object, Embed).
function isTagAttrib(type, name)
{
   var attribs = new Array("width", "height", "align", "id", "vspace", "hspace", "name");
   if(type.toLowerCase() == "applet")
   {
      var temp1 = ["codebase", "archive", "code", "mayscript"];
      attribs = attribs.concat(temp1);
      for(i = 0; i < attribs.length; i++)
      {
         if(name.toLowerCase() == attribs[i])
            return true;
      }      
   
   }
   else if(type.toLowerCase() == "object")
   {
      var temp2 = ["classid", "border", "data", "usemap", "codebaseattr", "typeattr"];
      attribs = attribs.concat(temp2);
      for(i = 0; i < attribs.length; i++)
      {
         if(name.toLowerCase() == attribs[i])
            return true;
      }
   }
   else if(type.toLowerCase() == "embed")
   {
      return true;
   }
   return false;
}

// Writes the applet start tag to the document including
// attributes
function writeAppletStartTag(name, attribs_raw)
{
   var buffer = "";
   var attribs = is_firefox ? fixArchiveValues(attribs_raw) : attribs_raw;
   buffer += "<" + name;
   for(i = 0; i < attribs.length; i++)
   {
      buffer += " " + attribs[i].name + "=\"" + attribs[i].value + "\"";
   }
   buffer += ">";
   
   document.writeln(buffer);
   return buffer;
}

// Writes the applet params to the document
function writeAppletParams(params_raw)
{
   var buffer = "";
   var params = is_firefox ? fixArchiveValues(params_raw) : params_raw;
   for(i = 0; i < params.length; i++)
   {
      buffer += "<param name=\"" + params[i].name + "\" value=\"" + encodeQuotes(params[i].value) + "\"/>\n";
   }
   document.write(buffer);
   return buffer;
} 

/**
 * Modifies the value of any Archive param or attribute to add the
 * pssessionid. Also adds a param named "pssessionid" to the applet tag so
 * the pssessionid is accessible via applets java code. 
 * This is to get around a firefox bug
 * where the session is getting "lost".
 */
function fixArchiveValues(list)
{
   if(pssessionid == undefined || pssessionid.length == 0)
      return list;
   var results = [];
   var pssessionidKey = "pssessionid";
   var pssessionidParamExists = false;

   for(i = 0; i < list.length; i++)
   {
      var nm = list[i].name.toLowerCase();
      if(nm == pssessionidKey)
         pssessionidParamExists = true;
      results[i] = new NVPair(list[i].name, list[i].value);
      if(nm == "archive" || nm == "cache_archive" || nm == "cache_archive_ex")
      {
         results[i].value = list[i].value + "?pssessionid=" + pssessionid;
      }
   }
   if(!pssessionidParamExists)
   {
      results[results.length] = new NVPair(pssessionidKey, pssessionid);
   }
   return results;
}

// Set focus to the Banner frame, which always the 1st (or top) frame. This may be called from applet
function focusBannerFrame()
{
   if (parent.frames.length > 0)
      parent.frames[0].focus();
}

// Encode quotes into html entities
function encodeQuotes(str)
{
   return str.replace(/\"/g,"&quot;");
}

// This function calls stop on all applets within
// a document. This is used to get around a bug in Firefox
// where it does not always call stop on Applets
function stopAllApplets()
{
   if(is_firefox)
   {   
      var embeds = document.getElementsByTagName("embed");
      var len = embeds.length;
      for(i = 0; i < len; i++)
      {
         if(embeds[i] != null)
            embeds[i].stop();
      }
   }
}

// This function just refreshes the selected folder of the navigator pane 
// of the content explorer applet in the caller window and mainly intended 
// to refresh the applet when an item is opened in active assembly to reflect 
// the item's new checkout status.
function refreshCxApplet()
{
  if (typeof(contentexplorer) != "undefined")
  {
         contentexplorer.refresh('Selected');
         javaInject();
  }


	if(window.opener != null)
	{
	   var rxApplet = PSGetApplet(window.opener, 'ContentExplorerApplet');
	   if(rxApplet == undefined)
	   	return;
	   if (rxApplet != null)
	   {
		rxApplet.refresh('Selected');
	   }	   
	}
	
}

function clickInsertButton(){
    if(submitButton != null && submitButton != undefined)
    {
        submitButton.trigger('click');
    }
}

// Inhibits multiple submits from occuring and disables submit
// changing the value to 'Please Wait...'. If close button is
//pressed then simply returns true.
var _formAlreadySubmitted = false;
function _ignoreMultipleSubmit()
{
   if(window.opener !== null && window.opener.ps_CloseMe)
      return true;
   if(_formAlreadySubmitted == true)
      return false;
   _formAlreadySubmitted = true;
   var submitButton = document.getElementById("rxCESubmit");
   if(submitButton != null && submitButton != undefined)
   {
	   submitButton.value = LocalizedMessage("pleaseWait");
	   submitButton.disabled = true;
   }
   return true;
}

// cross browser event handling from http://dustindiaz.com/rock-solid-addevent
function addEvent( obj, type, fn ) {
	if (obj.addEventListener) {
		obj.addEventListener( type, fn, false );
		EventCache.add(obj, type, fn);
	}

	else if (obj.attachEvent) {
		obj["e"+type+fn] = fn;
		obj[type+fn] = function() { obj["e"+type+fn]( window.event ); };
		obj.attachEvent( "on"+type, obj[type+fn] );
		EventCache.add(obj, type, fn);
	}
	else {
		obj["on"+type] = obj["e"+type+fn];
	}
}

var EventCache = function(){
	var listEvents = [];
	return {
		listEvents : listEvents,
		add : function(node, sEventName, fHandler){
			listEvents.push(arguments);
		},
		flush : function(){
			var i, item;
			for(i = listEvents.length - 1; i >= 0; i = i - 1){
				item = listEvents[i];
				if(item[0].removeEventListener){
					item[0].removeEventListener(item[1], item[2], item[3]);
				}
				if(item[1].substring(0, 2) != "on"){
					item[1] = "on" + item[1];
				}
				if(item[0].detachEvent){
					item[0].detachEvent(item[1], item[2]);
				}
				item[0][item[1]] = null;
			}
		}
	};
}();

/**
* This method can be used to save a file to the user's file system.  This ignores
* images and metadata files as those both work fine in DCE and Firefox.
* @param binaryURL the URL of the binary i.e. "/Rhythmyx/assembly/aa?widget=hf&hash=5f10f334f7babf37a042881149035b67b8cbc92a"
* @param fileType -- the mimeType of the file i.e. "application/pdf"
* @param fileName -- the full name of the file
*/
function saveFile(binaryURL, fileType, fileName) {

	if(is_dce) {
		if(!binaryURL.startsWith('http')) {
			var prefix = window.location.protocol + '//' + window.location.hostname + ':' + window.location.port;
			if(binaryURL.includes("/Rhythmyx")) {
				binaryURL = prefix + binaryURL;
			}
			else {
				var url = window.location.href;
				var index = url.indexOf("/Rhythmyx");
				// grabs everything after and including /Rhythmyx
				var newSuffix = url.substring(index);
				// used to grab /Rhythmyx/content_type for url
				var index2 = getPosition(url.substring(index), "/", 3);
				var finalSuffix = newSuffix.substring(0, index2 + 1);
				binaryURL = prefix + finalSuffix + binaryURL;
			}
		}

		if(!fileType.includes("image") && !binaryURL.includes("MetaData"))
			java.saveFile(binaryURL, fileName);
		else
			window.open(binaryURL, '_blank', 'width=700,height=500,resizable=yes');
	}
	else
		window.open(binaryURL, '_blank', 'width=700,height=500,resizable=yes');
}

function getPosition(string, subString, index) {
   return string.split(subString, index).join(subString).length;
}

function javaInject()
{
 // If calling from desktop explorer then throw alert that will be caught by
 // desktop explorer java code to inject java object.
if (is_dce)
 {
   alert("PercussionDCE");
  }

}

javaInject();

addEvent(window,'unload',EventCache.flush);