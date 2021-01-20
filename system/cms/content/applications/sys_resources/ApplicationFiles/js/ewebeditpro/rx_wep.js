/**[ rx_wep.js ]****************************************************************
 *
 * COPYRIGHT (c) 1999 - 2004 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/


/**
 * This file holds the functions for the CMS buttons on the eWebEditPro control.
 * The functions herein are compliant with Netscape 4.7, 6.23, as well as IE 5+.
 * 
 * Note to modifiers of this file.  Be careful refactoring the methods.  NS 4.7 had
 * a problem calling nested functions (i.e. the isStringValid) from
 * a child window caller. 
 *
 * Date: 2002.05.22
 * Author: Scott M. Morales
 *
 */

/**
 * Constant: Holds url for a image helper app that find all the 'img' tags 
 * calculates image sizes and fixes up width and height attributes.
 */
 var FIXUP_IMAGE_PAGE = "../sys_wepSupport/fixupImages.html";


/**
 * Constant: Holds url for search page.  Called by child popup pages.
 */
 var INLINE_SEARCH_PAGE = "../sys_searchSupport/getQuery.html?";

/**
 * Constant: Holds url for return page.  Called by child popup pages.
 */
 var INLINE_RETURN_PAGE =  "../sys_searchSupport/getResults.html?";

/**
 * Constant: Holds url for Ektrons help.
 */
 var HELP_PAGE =  "../sys_resources/ewebeditpro/ug/index.htm";


/**
 * A flag indicating, whether or not the current browser type is Netscape. 
 * <code>true</code> if Netscape, false otherwise.
 */
var isNav = false;

/**
 * A flag indicating, whether or not the current browser type is Internet Explorer. 
 * <code>true</code> if Internet Explorer, false otherwise.
 */
var isIE  = false;

/**
 * A flag indicating, whether or not the OS is Macintosh. 
 * <code>true</code> if Macintosh, false otherwise.
 */
var isMac = false;

/**
 * Check the browser and set appropriate member field.  
 * Assume Netscape or IE only.
 */
if (navigator.appName == "Netscape") 
{
   isNav = true;
}
else
{
   isIE = true;
}

/**
 * Check the OS and set appropriate member field.  Note: We will need this 
 * in next upgrade.
 */
if (navigator.platform == "MacPPC")
{
   isMac = true;
}

/**
 * Data Object.  Holds property values written to maintain state.
 */
var dataObject = new Object();
dataObject.returnedValue = "";
dataObject.sEditorName = "";
dataObject.wepSelectedText = "";
dataObject.searchType = "";
dataObject.windowRef = "";

function rxLaunchWord(sEditorName, strTextData, lData)
{
   dataObject.sEditorName = sEditorName;
   
   var bodyHtml = eWebEditPro[sEditorName].getBodyHTML();
   
   if (!hasImages(bodyHtml))
   {
      //no images - simply launch word
      WepLaunchWord();
      return;	
   }
   
   //there are images in the html, need to send the whole thing
   //to the server, so that we can add width and height to each 'img'
   
   //Open an empty window.
   dataObject.windowRef = window.open("", "wordlauncherwindow",  "toolbar=0,location=0,directories=0,"+
      "status=0,menubar=0,scrollbars=0,resizable=1,width=300,height=100");
      
   //set action on the form rxwordlauncher, see ActiveEdit.xsl
   document.rxwordlauncher.action = FIXUP_IMAGE_PAGE;
   document.rxwordlauncher.wepbodyhtml.value = bodyHtml;
   
   document.rxwordlauncher.submit();
}

/**
 * Does str pass our test for valid.  Returns <code>true</code> if 
 * it does.
 * @param str - string to be tested. May be <code>null</code>.
 */
function hasImages(str)
{      
   if(str == null || str.length == 0)
   {
      return false;
   }
   
   return str.toLowerCase().indexOf("<img ") != -1;
}

/**
 * Creates CMS search box for inline links and CMS Image creation:
 */
function createSearchBox(inlineslotid, sEditorName, type) 
{
   var selectedText = eWebEditPro[sEditorName].getSelectedText();
   var selectedHtml = eWebEditPro[sEditorName].getSelectedHTML();
   //We allow nested links for only images.
   if(	type=="rxhyperlink" && isStringValidForLink(selectedHtml))
   {
       selectedText = selectedHtml;
   }

   //Update the inlinelinksearch form elements
   document.inlinelinkssearch.action = INLINE_SEARCH_PAGE;
   document.inlinelinkssearch.inlinetext.value = selectedText;
   document.inlinelinkssearch.inlineslotid.value = inlineslotid;
   document.inlinelinkssearch.inlinetype.value = type;
   //Open an empty window.
   var w = "";
   if(isNav)
   {
      dataObject.windowRef = window.open("", "searchitems", 
      "toolbar=0,location=0,directories=0,status=0,menubar=0,scrollbars=1," + 
      "resizable=1,width=720,height=400,screenX=220,screenY=220");
   } 
   else
   {
      dataObject.windowRef =  window.open("", "searchitems", 
      "toolbar=0,location=0,directories=0,status=0,menubar=0,scrollbars=1," + 
      "resizable=1,width=720,height=400, left=220, top=220");
   }   
   //Submit the inlinelinksearch form to this window
   document.inlinelinkssearch.submit();

   dataObject.windowRef.focus();
   
}

/**
 * Clean up dataObject.  Set all properties to "" for the next call. 
 */
function cleanUp()
{
   dataObject.returnedValue = "";
   dataObject.sEditorName = "";
   dataObject.wepSelectedText = "";
   dataObject.searchType = "";
   dataObject.windowRef = "";

}

/**
 * Formats the output then pastes into the control.
 * Netscape 4.7 & 6.23 compliant.  
 * @param returnedHTML is a string and can be <code>null</code>
 */
function formatOutput(returnedHTML){
   dataObject.searchType = "";
   eWebEditPro[dataObject.sEditorName].pasteHTML(returnedHTML);
}

/**
 * Sets whole HTML content on the current Ektron editor.
 */
function setWholeHtmlContent(htmlcontent) {

   eWebEditPro[dataObject.sEditorName].setContent("htmlwhole", htmlcontent, "");
}


/**
 * Tell Ektron to Launch MS Word.
 */
function WepLaunchWord()
{
   eWebEditPro[dataObject.sEditorName].ExecCommand("cmdmsword", "", 1);
}

/**
 * Called by eWebEditPro on events.  This is where to stick custom functions. 
 * May need to make this accessible at a later date if requests demand it. 
 */
function eWebEditProExecCommand(sEditorName, strCmdName, strTextData, lData)
{
   //Avoid adding links or variants if the selected html contains contenrEditable attribute
   var selectedHtml = eWebEditPro[sEditorName].getSelectedHTML();
   if(("rx_inlineweblink" == strCmdName ||
   "rx_inlinerxlink" == strCmdName ||
   "rx_inlinerxvariant" == strCmdName) &&
   selectedHtml.toLowerCase().indexOf("contenteditable=") != -1)
   {
      alert("Your selection contains an inline variant or part of it.\nYou can not insert inline links or images or variants if your selection contains inline variant or part of it.");
      return;
   }
   // rx_inlineweblink -- Creates web link:
   if("rx_inlineweblink" == strCmdName)
   {  
      createExternalReference(sEditorName);
   }

   // rx_inlinerxlink  -- Creates Rhythmyx link:
   if("rx_inlinerxlink" == strCmdName)
   {  
      createRxInlineReference('rxhyperlink', sEditorName, eval(sEditorName + "_InlineLinkSlot")); 
   }

   // rx_inlinerximage  -- Creates Rhythmyx image reference:
   if("rx_inlinerximage" == strCmdName)
   {  
      createRxInlineReference('rximage', sEditorName, eval(sEditorName + "_InlineImageSlot"));
   }

   // rx_inlinerxvariant  -- Creates Rhythmyx variant reference:
   if("rx_inlinerxvariant" == strCmdName)
   {  
      createRxInlineReference('rxvariant', sEditorName, eval(sEditorName + "_InlineVariantSlot"));
   }

   if("rx_wephelp" == strCmdName)
   {
      openHelp();
   }


   if("rx_word" == strCmdName)
   {
      rxLaunchWord(sEditorName, strTextData, lData);
   }

   // cmdviewashtml -- Ektron's view as HTML.  This disables rx_ commands:
   if("cmdviewashtml" == strCmdName)
   {  
      DisableCommand(sEditorName,"rx_inlineweblink");
      DisableCommand(sEditorName,"rx_inlinerxlink");
      DisableCommand(sEditorName,"rx_inlinerximage");
      DisableCommand(sEditorName,"rx_inlinerxvariant");
      DisableCommand(sEditorName,"rx_wephelp");
      DisableCommand(sEditorName,"rx_word");
   }
   
   // cmdviewashtml -- Ektron's view as HTML.  This enables rx_ commands:
   if("cmdviewaswysiwyg" == strCmdName && eval(sEditorName + "_ReadOnly")!='yes')
   {  
      EnableCommand(sEditorName,"rx_inlineweblink");
      EnableCommand(sEditorName,"rx_inlinerxlink");
      EnableCommand(sEditorName,"rx_inlinerximage");
      EnableCommand(sEditorName,"rx_inlinerxvariant");
      EnableCommand(sEditorName,"rx_wephelp");
      EnableCommand(sEditorName,"rx_word");
   }   
}

/**
 * Creates web link rx style.  
 * @param type - must not be <code>null</code> or empty.
 * @param sEditorName - must not be <code>null</code> or empty.
 * @param getSelectedText - boolean if true, it gets the selected text from
 * the control.  May be <code>null</code> if so, then false.
 */
function createRxInlineReference(type, sEditorName, inlineslotid)
{  
   if(type == null || type.length == 0 || 
   sEditorName == null || sEditorName.length == 0)
   {
      alert("Please notify the Rhythmyx administrator: " + 
      "ERROR: createInlineReference() - received null values");
      return;
   } 
   
   dataObject.sEditorName = sEditorName;
   dataObject.searchType = type;
	
   createSearchBox(inlineslotid, sEditorName, type);
}


/**
 * Does str pass our test for valid.  Returns <code>true</code> if 
 * it does.
 * @param str - string to be tested. May be <code>null</code>.
 */
function isStringValid(str)
{      
   if(str == null || str.length == 0 || str == "http://")
   {
      return false;
   }

   return true;
}

/**
 * Creates link to non Rhythmyx managed element.
 * @param sEditorName - must not be <code>null</code> or empty.
 */
function createExternalReference(sEditorName)
{
   if(sEditorName == null || sEditorName.length == 0)
   {
      alert("Please notify the Rhythmyx administrator: " + 
      "ERROR: createExternalReference - received null values");
      return;
   } 
   dataObject.sEditorName = sEditorName;
   
   if(isNav)
      {
         dataObject.windowRef = window.open("../sys_resources/html/externalLink.html?ewep=" + sEditorName, "extLink", 
         "toolbar=0,location=0,directories=0,status=0,menubar=0,scrollbars=0," + 
         "resizable=0,width=530,height=160,screenX=220,screenY=220");
      } 
      else
      {
         dataObject.windowRef =  window.open("../sys_resources/html/externalLink.html?ewep=" + sEditorName, "extLink", 
         "toolbar=0,location=0,directories=0,status=0,menubar=0,scrollbars=0," + 
         "resizable=0,width=530,height=160, left=220, top=220");
      } 
      dataObject.windowRef.focus();
}

// A list of allowed elements, these are defined from HTML 4.01
var allowed = new Array("sub","sup","small","big","em","b","i","tt","strong","dfn","code",
"samp","kbd","var","cite","abbr","acronym","img","object","br","script","map","q","span","bdo","font");

var skipFirst = new Array("p","div");

// Anchor tags only allow certain elements to be nested inside them. This function
// determines if the selectedHtml only has allowable elements. The list of allowable
// elements is listed in the "allowed" variable above.
//
// There are some exceptions. If the first element found is one of the "skipFirst"
// elements listed, those are allowed for that special case. This allows a user to 
// do what looks like a valid selection that really includes (incorrectly) the div
// or paragraph tags. In this circumstance, ektron will fix the results.
function isStringValidForLink(selectedHtml)
{
	var i;
	var len = selectedHtml.length;

	// Handle some special cases.. notably Ektron can return <p>content</p> or <div>content</div> when
	// the user believes they've just selected regular text or an image. Skip these starting cases
	var first = true;

	for(i = 0; i < len; i++)
	{
		// If we find an element that is not in the allowed array, then return false
		if (selectedHtml.charAt(i) == '<')
		{
			// Find the end delimiter or space
			var e, j;
			for(e = i + 1; e < len; e++)
			{
				var ch = selectedHtml.charAt(e);
				if (ch == '>' || ch == ' ')
				{
					break;
				}
			}
			var el = selectedHtml.substring(i + 1, e).toLowerCase();
			var inarray = false;
			if (first)
			{
				first = false;
				for(j = 0; j < skipFirst.length; j++)
				{
					if (skipFirst[j] == el)
					{
						inarray = true;
					}
				}
			}
			if (el.length > 0 && el.charAt(0) == '/')
			{
				inarray = true; // Closing tag, ignore
			}
			for(j = 0; j < allowed.length && inarray == false; j++)
			{
				if (allowed[j] == el)
				{
					inarray = true;
				}
			}
			if (! inarray)
			{
				return false;
			}
			i = e;
		}
	}

	return true;
}
/**
 * Open Ektrons help
 */
function openHelp()
{
   var theUrl = "../sys_resources/ewebeditpro/ug/index.htm";
   window.open(HELP_PAGE, "searchitems", "toolbar=0,location=0,directories=0,"+
   "status=0,menubar=0,scrollbars=1,resizable=1,width=900,height=600");
}

/**
 * Ektron functions from developer guide -- disable commands
 */
function DisableCommand(sEditorName,sCommandName)
{ 
   if(sEditorName == null || sEditorName.length == 0 ||
   sCommandName == null || sCommandName.length == 0)
   {
      alert("Please notify the Rhythmyx administrator: " + 
      "ERROR: DisableCommand - received null values");
      return;
   } 
   var objInstance = eWebEditPro.instances[sEditorName];
   var objMenu = objInstance.editor.Toolbars();
   objMenu.CommandItem(sCommandName).setProperty("CmdGray", true);
}

/**
 * Ektron functions from developer guide -- enable commands
 */
function EnableCommand(sEditorName,sCommandName)
{
   if(sEditorName == null || sEditorName.length == 0 ||
   sCommandName == null || sCommandName.length == 0)
   {
      alert("Please notify the Rhythmyx administrator: " + 
      "ERROR: EnableCommand - received null values");
      return;
   } 
   var objInstance = eWebEditPro.instances[sEditorName];
   var objMenu = objInstance.editor.Toolbars();
   objMenu.CommandItem(sCommandName).setProperty("CmdGray", false);
}