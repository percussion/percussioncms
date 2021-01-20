/******************************************************************************
 *
 * [ ps.util.js ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

/*
 * Various utilities.
 * This is a generic module.
 * While changing this file consider moving functions to separate modules.
 */

dojo.provide("ps.util");

ps.util = new function(){}


/**
 * Utility method to add a parameter to the URL.
 * If ? exists in url then adds the param appending an & otherwise
 * adds the param by appending ? to the url.
 */
ps.util.addParamToUrl = function(url,name,value)
{
   url.indexOf("?")==-1?url+="?":url+="&";
 	url+=name+"="+value;
   return url;
}


/**
 * Reports an error to the user, and logs it.
 * @param error the error to report. Optional.
 * <ul>
 * 
 * <il>If the value is skipped or can be converted to boolean <code>false</code>
 * value, the method just notifies the user that an error occured.</il>
 * 
 * <il>Otherwise, if the error is a string, it is shown as is.</il>
 * 
 * <il>Otherwise, if the error is an object of {@link ps.io.Response} type,
 * it shows an error stored in it.
 * The method fails if the object indicates a successful operation.</il>
 *
 * <il>Otherwise, if the error object has property "message",
 * the method shows content of this field.</il>
 * 
 * <il>Otherwise, the method reports that an unrecognized error has occured, and
 * logs the error object. The logged object can be later inspected with debugger
 * like Firebug</il>
 * </ul>
 *
 * @return nothing.
 */
ps.util.error = function (error)
{
   function hasProperty(o, propertyName)
   {
      for (var property in o)
      {
         if (property === propertyName)
         {
            return true;
         }
      }
      return false;
   }
   error_val = error;

   var debugObj;
   if (!error)
   {
      var message = "An Error Occured!";
   }
   else if (dojo.lang.isString(error))
   {
      var message = error;
   }
   else if (error instanceof ps.io.Response)
   {
      dojo.lang.assert(!error.isSuccess(),
            "error() was called with ps.io.Response indicating success. "
            + "It should be called on an error only.");
      ps.util.error(error.getValue());
      return;
   }
   else if (hasProperty(error, "message"))
   {
      ps.util.error(error.message);
      return;
   }
   else
   {
      debugObj = true;
      var message = "An Unrecognized Error Occured!";
   }
   dojo.debug(message);
   if (debugObj)
   {
      dojo.debug(error);
   }
   alert(message);
}

/**
 * Finds a DOM node with the specified id in the array of nodes or their
 * child nodes.
 * @param {Array} nodes the nodes to search for. Not <code>null</code>.
 * @param {String} id the node id to search for. Not <code>null</code>.
 * @return the found node or <code>null</code> if the node with the specified
 * id was not found.
 */
ps.util.findNodeById = function (nodes, id)
{
   dojo.lang.assert(dojo.lang.isArrayLike(nodes));
   dojo.lang.assertType(id, String);

   // check nodes of this level
   for (var i in nodes)
   {
      var node = nodes[i];
      if (node.id === id && node.nodeType === dojo.html.ELEMENT_NODE)
      {
         return node;
      }
   }

   // recursively check each of the children
   for (var i in nodes)
   {
      var node = nodes[i];
      if (node.nodeType === dojo.html.ELEMENT_NODE)
      {
         var foundNode = ps.util.findNodeById(node.childNodes, id);
         if (foundNode)
         {
            return foundNode;
         }
      }
   }
   return null;
}

/**
 * Swaps two nodes in place. Each node should have a parent.
 * @param node1 the first node.
 * Not <code>null</code>.
 * @param node2 the second node.
 * Not <code>null</code>.
 */
ps.util.swapNodes = function (node1, node2)
{
   dojo.lang.assert(node1);
   dojo.lang.assert(node2);

   var parent1 = node1.parentNode;
   var parent2 = node2.parentNode;
   dojo.lang.assert(parent1);
   dojo.lang.assert(parent2);

   var marker = document.createElement("div");
   parent1.insertBefore(marker, node1);
   parent2.insertBefore(node1, node2);
   parent1.insertBefore(node2, marker);

   dojo.html.destroyNode(marker);
}

/**
 * Returns the width and height of the main window.
 * @param win A top level window object or null to use 'window'.
 * @param useInner flag indicating innerWidth/innerHeight should be used to calculate
 * size for non IE browsers, else if <code>false</code> then
 * use outerWidth/outerHeight.
 * @return an object with a width and height property.
 */
ps.util.getScreenSize = function(win, useInner) 
{
   if (win == null)
      win = window;
   var doc = win.document;
   var dims = new Object();
   if( typeof( win.innerWidth ) == 'number' )
   {
      //Non-IE
      if(useInner)
      {
         dims.width = win.innerWidth;
         dims.height = win.innerHeight;
      }
      else
      {
         dims.width = win.outerWidth;
         dims.height = win.outerHeight;
      }
   }
   else if( doc.documentElement && (
      doc.documentElement.clientWidth ||
      doc.documentElement.clientHeight ) )
   {
      //IE 6+ in 'standards compliant mode'
      dims.width = doc.documentElement.clientWidth;
      dims.height = doc.documentElement.clientHeight;
   } 
   else if( doc.body && ( 
      doc.body.clientWidth ||
      doc.body.clientHeight ) )
   {
      //IE 4 compatible
      dims.width = doc.body.clientWidth;
      dims.height = doc.body.clientHeight;
   }
   return dims;
}

/**
 * Sets the size of the passed in dialog to the passed in 
 * preferred sizes. If the preferred sizes don't fit
 * in the screen then they are adjusted to fit within the
 * screen.
 * @param {dojo.widget.FloatingPane} dialog the dialog to be
 * sized.
 * @param {int} width the preferred width for the dialog.
 * @param {int} height the preferred height for the dialog.
 */
ps.util.setDialogSize = function(dialog, width, height)
{
   dojo.lang.assertType(dialog, dojo.widget.FloatingPane);
   var scrSize = ps.util.getScreenSize();
   var pHeight = height * .96;
   var pWidth = width * .96;
   if(pHeight >= scrSize.height)
      height = scrSize.height * .96;
   if(pWidth >= scrSize.width)
      width = scrSize.width * .96;
   dialog.resizeTo(width, height);      
}

/**
 * Returns the size of the passed in dialog as an object with
 * a width and height property.
 * @param {dojo.widget.FloatingPane} dialog the dialog to return
 * its size.
 */
ps.util.getDialogSize = function(dialog)
{
   dojo.lang.assertType(dialog, dojo.widget.FloatingPane);
   return dojo.html.getMarginBox(dialog.domNode);   
}

/**
 * Forces the passed in dialog to resize. This is used to 
 * get around a layout issues with browsers.
 * @param {dojo.widget.FloatingPane} dialog the dialog
 * @param {number} preferredWidth the preferred width,
 *  used if get dialog size returns zero. 
 * @param {number} preferredHeight the preferred height,
 *  used if get dialog size returns zero.
 */
ps.util.forceDialogResize = function(dialog, preferredWidth, preferredHeight)
{
   dojo.lang.assertType(dialog, dojo.widget.FloatingPane);
   var size = ps.util.getDialogSize(dialog);
   var width = size.width > 0 ? size.width : preferredWidth;
   var height = size.height > 0 ? size.height : preferredHeight;
   ps.util.setDialogSize(dialog, width - 1, height - 1);
   ps.util.setDialogSize(dialog, width, height);
}

/**
 * A helper method to get around the issue in IE 6 where
 * the CSS hover style does not work. If Firefox, Safari or
 * IE 7 then we donothing and return as the CSS hover
 * works in these browsers.
 * @param {htmlNode} div the div tag in question.
 * @param {boolean} isHovering flag indicating that the
 * mouse is currently hovering over this div tag.
 */
ps.util.handleIE6FieldDivHover = function(div, isHovering)
{
   if(!dojo.render.html.ie || dojo.render.html.ie70)
      return;
   var color = isHovering ? "#ffc" : "";   
   div.style.backgroundColor = color;   
}

/**
 * Returns an object with attributes 'top', 'left', 'bottom', 'right',
 * indicating distances of the element in pixels from left and top page side.
 * @param element the dom node of the element to find visible sides for.
 * Not <code>null</code>
 */
ps.util.getVisibleSides = function (element)
{
   dojo.lang.assert(element, "Element must be specified");

   var box = dojo.html.getBorderBox(element);
   var visible = ps.util.getVisiblePosition(element);

   var top = visible.y;
   var left = visible.x;
   var bottom = top + box.height;
   var right = left + box.width;

   return {top: top, left: left, bottom: bottom, right: right};
}

/**
 * Returns visible position of the node on the page as an object with x, y
 * attributes.
 * @param node the node to find visible position for. Not <code>null</code>.
 */
ps.util.getVisiblePosition = function (node)
{
   function visibleSize (node, styleAttr)
   {
      var result = 0;
      var n = node;
      while (n)
      {
         var dpixels = dojo.html.getPixelValue(n, styleAttr);
         if (dpixels)
         {
            result += dpixels;
         }
         n = n.parentNode;
      }
      return result;
   }

   return {x: visibleSize(node, "left"), y: visibleSize(node, "top")};
}

/**
 * Creates a dialog with the provided parameters and default parameters.
 * @param params the parameters to create the dialog.
 * Not null.
 * @param {String} width the dialog width as specified in HTML.
 * Can be null if not specified.
 * @param {String} height the dialog height.
 * Can be null if not specified.
 * @param (boolean) resizable option, if specified and false then creates the
 * dialog as not resizable otherwise resizable.
 */
ps.util.createDialog = function (params, width, height, isResizable)
{
   dojo.lang.assert(params, "Parameters should be specifed");
   var isRes = true;
   if(isResizable == false)
   {
      isRes = false;
   }
   // programmatic creation of a modal floating pane only works
   // when its element is created first.
   var div = document.createElement('div');
   if (width)
   {
      div.style.width = width;
   }
   if (height)
   {
      div.style.height = height;
   }
   div.style.position = "absolute";
   document.body.appendChild(div);

   var p = {
      bgColor: ps.util.DIALOG_BACKGROUND,
      bgOpacity: ps.util.DIALOG_BACKGROUND_OPACITY,
      toggle: "explode",
      toggleDuration: 10,
      constrainToContainer: true,
      hasShadow: true,
      resizable: isRes,
      executeScripts: true,
      cacheContent: false
   };
   dojo.lang.mixin(p, params);

   return dojo.widget.createWidget("ModalFloatingPane", p, div);
}

/**
 * Gets the target from the supplied event and executes the select() method on
 * it. This is meant for text controls such as INPUT type="text".
 */
ps.util.selectAll = function(e)
{
   var targ;
   if (!e)
   {
      var e=window.event;
   }
   if (e.target)
   {
      targ=e.target;
   }
   else if (e.srcElement)
   {
      targ=e.srcElement;
   }
   if (targ.nodeType==3) // defeat Safari bug
   {
      targ = targ.parentNode;
   }
   targ.select();
}


/**
 * Creates a small dialog with a 1 line text control to show the provided text.
 * The control is readonly and the content is selected when focus is gained.
 * The control is given focus initially.
 * Selection still has quirks on FF.
 */
ps.util.ShowPageLinkDialog = function(text)
{
   var div = document.createElement('div');
   div.style.position = "absolute";
   div.style.border = "0px";
   document.body.appendChild(div);

   var dlg = dojo.widget.createWidget("ModalFloatingPane",
   {
       id: "ps.pageLinkDiv",
       title: 'Paste link into email or IM ',
       titleBarDisplay: true,
       displayCloseAction: true,
       bgColor: ps.DIALOG_BACKGROUND,
       bgOpacity: ps.DIALOG_BACKGROUND_OPACITY,
       executeScripts: true,
       resizable: false
   }, div);

   //couldn't use Dojo's dojo.event.connect, it wouldn't work for onfocus (??)
   dlg.setContent(
      '<input onfocus="ps.util.selectAll(event)" id="ps.util.wgtShowPageLink" type="text" size="60" readonly="true" value="' + text + '" />'
   );
   ps.util.setDialogSize(dlg, 440, 70);
   dlg.show();

   var foo = dojo.byId("ps.util.wgtShowPageLink");
   foo.focus();
}

/**
 * Creates a prompt dialog, consisting of a test box with two buttons.
 * Expects options as an array of parameters to use while creating the dialog.
 * The button clicks will call the callback functions.
 * options
 * dlgTitle : (String)Dialog title, Default: Prompt Dialog
 * promptTitle : (String)Text box title, Default: Text
 * promptText: (String)Text box value, Default: ""
 * textRequired: (boolean) alerts user for non empty text on OK click., Default:false
 * okBtnText: (String)Text to show on OK button, Default : OK
 * cancelBtnText: (String)Text to show on Cancel button, Default: Cancel
 * okBtnCallBack: (function)Callback function for Ok button pressed, 
 *    Default:does nothing
 * cancelBtnCallBack: (function)Callback function for Cancel button pressed, 
 * if not 
 * It is callers responsibility to close the dialog by calling hide method. 
 * @param options array of options, if provided must be an object of array.

 * @return dojo dialog object.
 */
ps.util.CreatePromptDialog = function(options)
{
   var _this = this;

   this.dlgTitle = "Prompt Dialog";
   this.promptTitle = "Text";
   this.promptText = "";
   this.textRequired = false;
   this.okBtnText = "OK";
   this.cancelBtnText = "Cancel";
   this.okBtnCallBack = null;
   this.cancelBtnCallBack = null;
   if(options != null)
   {
      this.dlgTitle = options.dlgTitle;      
      this.promptTitle = options.promptTitle;      
      this.promptText = options.promptText;      
      this.textRequired = options.textRequired;      
      this.okBtnText = options.okBtnText;      
      this.cancelBtnText = options.cancelBtnText;      
      this.okBtnCallBack = options.okBtnCallBack;      
      this.cancelBtnCallBack = options.cancelBtnCallBack;      
   }

   var div = document.createElement('div');
   div.style.position = "absolute";
   div.style.border = "0px";
   document.body.appendChild(div);

   this.wgtDlg = dojo.widget.createWidget("ModalFloatingPane",
   {
       id: "ps.promptDiv",
       title: this.dlgTitle,
       titleBarDisplay: true,
       displayCloseAction: true,
       bgColor: ps.DIALOG_BACKGROUND,
       bgOpacity: ps.DIALOG_BACKGROUND_OPACITY,
       executeScripts: true,
       resizable: false
   }, div);

   dojo.event.connect(this.wgtDlg, "onLoad", function()
   {
      _this.wgtButtonOk = dojo.widget.byId("ps.util.promptButtonSelect");
      _this.wgtButtonCancel = dojo.widget.byId("ps.util.promptButtonCancel");
      _this.wgtPromptText = dojo.byId("ps.util.promptInput");
      _this.wgtButtonCancel.onClick = function()
      {
         if(_this.cancelBtnCallBack)
         {
             _this.cancelBtnCallBack(_this.wgtPromptText.value);
         }
         else
         {
            _this.wgtDlg.hide();
         }
      };
      _this.wgtButtonOk.onClick = function()
      {
         if(_this.textRequired && ps.util.trim(_this.wgtPromptText.value).length<1)
         {
            alert(_this.promptTitle + " is required.");
            _this.wgtDlg.focusTitle();
            return;
         }
         if(_this.okBtnCallBack)
         {
            _this.okBtnCallBack(_this.wgtPromptText.value);
         }
      };
      
   });
   
   /**
    * Sets the focus on the prompt text box.
    */
   this.wgtDlg.focusTitle = function()
   {
      _this.wgtPromptText.focus();
   }
   var dlgContent = "<div>" + this.promptTitle + "</div>" +
          "<div>" +
          "<input id='ps.util.promptInput' type='text' size='60' value='" + this.promptText + "' /></div>" +
          "<br />" +
          "<div class='PsAaButtonBox'>" +
          "<button dojoType='ps:PSButton' id='ps.util.promptButtonCancel'>" + this.cancelBtnText + "</button>" +
          "<button dojoType='ps:PSButton' id='ps.util.promptButtonSelect'>" + this.okBtnText + "</button>"+
			"</div>";
   
   this.wgtDlg.setContent(dlgContent);
   
   ps.util.setDialogSize(this.wgtDlg, 440, 120);
   return this.wgtDlg;
}

/**
 * Compares 2 strings ignoring case. Is used for sorting filtering table.
 */
ps.util.compareIgnoreCase = function (s1, s2)
{
   s1 = s1.toLowerCase();
   s2 = s2.toLowerCase()
   if (s1 > s2)
   {
      return 1;
   }
   else if (s1 < s2)
   {
      return -1;
   }
   else
   {
      return 0;
   }
}

/**
 * Helper class to trim the supplied string.
 * @param str, if <code>null</code> returns empty string otherwise returns 
 * trimmed string. Asserts if str is not of type String.
 */
ps.util.trim = function(str)
{
   if(!str)
     return "";
   dojo.lang.assertType(str,String);
   return str.replace(/^\s\s*/, '').replace(/\s\s*$/, '');
}

/**
 * Helper method to get the the css text of the supplied element from the
 * Style Sheets (including the builtin style sheet of 0).
 * @param elemName, Name of the element whose styles needs to be returned, 
 *    if defined must be a String.
 * @param clearStyle, flag to indicate whether to clear the style after 
 *    extracting.
 * @return, element style or empty string. Never <code>null</code>
 */
ps.util.getElementStyleSheetCss = function(elemName, clearStyle)
{
   if(!elemName || ps.util.trim(elemName).length < 1)
      return "";
   dojo.lang.assertType(elemName,String);
   var clrStyle = clearStyle && clearStyle == true;
   var elemStyles = "";
   elemName = ps.util.trim(elemName).toLowerCase();
   var sheets = document.styleSheets;
   for(var i = 0; i < sheets.length; i++)
   {
      var rules = sheets[i].cssRules?sheets[i].cssRules:sheets[i].rules;
      if(rules.length > 0)
      {
         for(var j = 0; j < rules.length; j++)
         {
            var s = rules[j].style;
            if(rules[j].selectorText && 
               ps.util.trim(rules[j].selectorText).toLowerCase()==elemName)
            {
               elemStyles = elemStyles==""?s.cssText:elemStyles+";"
                              +s.cssText;
               if(clrStyle)
                  s.cssText = "";
            }
         }
      }
   }
   return elemStyles;
}

/**
 * Enables or disables style sheets. Disables or enables all the stylesheets 
 * whose href ends case insensitively with the given fileName.
 * @param fileName name of the style sheet file, if blank does nothing.<b>
 * @param enabled flag to indicate whether to enable or disable. 
 */
ps.util.enableStyleSheet = function(fileName, enabled)
{
   if(!fileName || ps.util.trim(fileName).length < 1)
      return;
   dojo.lang.assertType(fileName,String);
   var sheets = document.styleSheets;
   fileName = fileName.toLowerCase();
   for(var i = 0; i < sheets.length; i++)
   {
      var sheet = sheets[i];
      var href = ps.util.trim(sheet.href).toLowerCase();
      if(dojo.string.endsWith(href,fileName))
      {
         sheet.disabled = !enabled;
      }
   }
}

/**
 * Helper class to find all empty Active Assembly containers and
 * add a place holder.
 */
ps.util.addPlaceholders = function(doc)
{
   var allDivs = doc.getElementsByTagName("div");
   for(i = 0; i < allDivs.length; i++)
   {
      var className = allDivs[i].className;
      var isEmpty = allDivs[i].childNodes.length == 0;
      var prefix = null;
      if(className == "PsAaPage")
      {
         prefix = "Page";
      }
      if(className == "PsAaSlot")
      {
         prefix = "Slot";
      }
      else if(className == "PsAaSnippet")
      {
         prefix = "Snippet";
      }
      else if(className == "PsAaField")
      {
         prefix = "Field";
      }

      if(prefix != null && isEmpty)
      {
         var placeholder = document.createElement('div');
         placeholder.className = "PsAaPlaceholder";
         var pText = document.createTextNode("Empty " + prefix);
         placeholder.appendChild(pText);
         allDivs[i].appendChild(placeholder);
      }
   }
   
}

/**
 * Returns the value of the server property for the given property name. If 
 * not found returns the default value if supplied or null.
 * @param propertyName, name of the property must not be null and must be a 
 * string object.
 * @param defaultValue, the value that needs to be returned if the property 
 * does not exist. If this is not defined then returns null.
 * @return the property value or default value. May be empty.
 */
ps.util.getServerProperty = function(propertyName,defaultValue)
{
   dojo.lang.assertType(propertyName,String);
   if(!defaultValue)
      defaultValue = "";
   if(ps.util._serverProperties == null)
   {
      var response = ps.io.Actions.getServerProperties();
      if(!response.isSuccess())
      {
         ps.io.Actions.maybeReportActionError(response);
         return "";
      }
      ps.util._serverProperties = response.getValue();
   }
   if(ps.util._serverProperties[propertyName])
      return ps.util._serverProperties[propertyName];
   else
      return defaultValue;
}
   

/**
 * Helper class to show or hide all place holders.
 */
ps.util.showHidePlaceholders = function(doc, isShow)
{
   var allDivs = doc.getElementsByTagName("div");
   for(i = 0; i < allDivs.length; i++)
   {
      var className = allDivs[i].className;
      if(className == "PsAaPlaceholder")
      {
         if(isShow)
         {
            allDivs[i].style.display = "block";
         }
         else
         {
            allDivs[i].style.display = "none";
         }
      }
   }
}

/**
 * Array object that holds the server properties. 
 * Initialized in {@link #getServerProperty(String)} method.
 */
ps.util._serverProperties = null;
   

/**
 * The color rest of the page is painted with when a modal dialog is displayed.
 */
ps.util.DIALOG_BACKGROUND = "white";

/**
 * The opacity rest of the page is painted with when a modal dialog is displayed.
 */
ps.util.DIALOG_BACKGROUND_OPACITY = 0.1;

/**
 * Controls' id prefix for the sites panel.
 */
ps.util.BROWSETAB_SITES_PANEL_PREF = "ps.content.sitespanel";

/**
 * Controls' id prefix for the folders panel.
 */
ps.util.BROWSETAB_FOLDERS_PANEL_PREF = "ps.content.folderspanel";

/**
 * Controls' id prefix for the search panel.
 */
ps.util.BROWSETAB_SEARCH_PANEL_PREF = "ps.content.searchpanel";

/**
 * Constant: ContentBrowser Dialog page URL
 */
ps.util.CONTENT_BROWSE_URL = "../ui/content/ContentBrowserDialog.jsp";

/**
 * Constant for active assembly browse mode.
 */
ps.util.BROWSE_MODE_ACTIVE_ASSEMBLY = "activeAssembly";

/**
 * Constant for active assembly browse mode.
 */
ps.util.BROWSE_MODE_ACTIVE_ASSEMBLY_TABLE_EDITOR = "activeAssemblyTable";

/**
 * Constant for rich text editor inline browse mode for templates.
 */
ps.util.BROWSE_MODE_RTE_INLINE = "rteInline";

/**
 * Constant for rich text editor inline browse mode for inline links.
 */
ps.util.BROWSE_MODE_RTE_INLINE_LINK = "rteInlineLink";

/**
 * Constant for rich text editor inline image browse mode.
 */
ps.util.BROWSE_MODE_RTE_INLINE_IMAGE = "rteInlineImage";



dojo.lang.mixin(ps, ps.util);