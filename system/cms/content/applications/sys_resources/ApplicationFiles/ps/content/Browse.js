

/******************************************************************************
 *
 * [ ps.content.Browse.js ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
dojo.provide("ps.content.Browse");

dojo.require("ps.content.History");
dojo.require("ps.content.SelectTemplates");
dojo.require("ps.content.BrowseTabPanel");
dojo.require("ps.content.SitesTabPanel");
dojo.require("ps.content.FoldersTabPanel");
dojo.require("ps.content.SearchTabPanel");
dojo.require("ps.widget.ContentPaneProgress");
dojo.require("ps.util");
dojo.require("dojo.collections.Dictionary");
dojo.require("dojo.lang.declare");
dojo.require("dojo.json");
dojo.require("dojo.string");


/**
 * Ctor for the Content Browser Dialog. Content Browser Dialog is a generic 
 * content browsing interface whose behavior can vary depending on the mode. The
 * dialog widget is created in the fly and added as a child of the body element
 * of the document. The contents of the dialog is loaded from the server while
 * creating the widget itself.
 * 
 * Note: Creating on the fly is not setting the dialog size. We assume the 
 * dialog  widget tag is already available in the html doc and acquire from the
 * document for now.
 */
ps.content.Browse = function(mode)
{
   //todo validate the mode to check if the mode is supported
   this.mode = mode;

   this.preferredHeight = 500;
   this.preferredWidth = 750;

   /**
    * Indicates that this dialog will be in standalone mode which means it
	* will be rendered within a browser window and will "stand alone".
	* In this mode it will always resize to the parents window size and
	* will not display a title bar or shadow.
	*/
   this.isStandAlone = this.mode != ps.util.BROWSE_MODE_ACTIVE_ASSEMBLY;

   /**
    * This must be called after construction of the dialog. This actually parses 
    * all controls/widgets within the dialog and connects appropriate events.
    */
   this.init = function(rxroot)
   {
      this.rxroot = rxroot;
      this.currentTab = null;
   }
   
   /**
    * Creates the Browse/Search dialog if it is not created yet.
    * Stores the dialog in the {@link #wgtDlg} field.
    */
   this.maybeCreateBrowseDialog = function ()
   {
      if (this.wgtDlg)
      {
         this.searchtab._loadSearchScript();
         return;
      }

      var scrSize = ps.util.getScreenSize();
	  var theWidth = this.isStandAlone ? scrSize.width : this.preferredWidth;
	  var theHeight = this.isStandAlone ? scrSize.height : this.preferredHeight;
	  this.wgtDlg = ps.createDialog(
            {
               id: "ps.content.BrowseDlg",
               title:psxGetLocalMessage("javascript.ps.content.browse@Active_Assembly_Browse_Content"),
               titleBarDisplay: !this.isStandAlone,
			   hasShadow: !this.isStandAlone,
			   resizable: !this.isStandAlone
            }, theWidth + "px", theHeight + "px");

      function tab(prefix, tabLabel)
      {
         // <tag ...></tag> form is used because for some reason 
         // the <tag.../> form fails
         return '<div dojoType="ContentPane" id="' + prefix + '.tab" '
               + 'label="' + tabLabel + '" preload="true"></div>\n';
      }

      var content =
            '<div id="ps.content.mainTabContainer" dojoType="TabContainer" '
            +       'style="width: 100%; height: 100%">\n'
            +    tab(ps.util.BROWSETAB_SITES_PANEL_PREF, psxGetLocalMessage("javascript.ps.content.browse@Sites"))
            +    tab(ps.util.BROWSETAB_FOLDERS_PANEL_PREF, psxGetLocalMessage("javascript.ps.content.browse@Folders"))
            +    tab(ps.util.BROWSETAB_SEARCH_PANEL_PREF, psxGetLocalMessage("javascript.ps.content.browse@Search"))
            + '</div>';
      this.wgtDlg.setContent(content);
      var _this = this;

      //override the dialog close function
      // to not destroy the dialog
      this.wgtDlg.closeWindow = function()
      {
         ps.aa.controller.enableConflictStyleSheets(true);
         _this.currentTab._onCancel();
         _this.close()
      }
      this.parseTabControls();		
   }

   /**
    * Method called to parse the tab controls and connect events.
    */
   this.parseTabControls = function()
   {
      var sitestab = new ps.content.SitesTabPanel(this);
      var folderstab = new ps.content.FoldersTabPanel(this);
      var searchtab =  new ps.content.SearchTabPanel(this);
      this.searchtab = searchtab;
		
      // these take long time, so interlieve them with the main flow
      // to make the browser more responsive
      var rxroot = this.rxroot;
      // load the first tab ASAP
      sitestab.init();
      dojo.lang.delayThese([
            function() {folderstab.init();},
            function() {searchtab.init();}],
            500);
		var tabContainer = dojo.widget.byId("ps.content.mainTabContainer");
			
   }

   /**
    * This function does all required steps for closing the dialog
    * window.
    */
   this.close = function()
   {
      ps.aa.controller.enableConflictStyleSheets(true);
      if(this.isStandAlone)
	  {
             self.close();
	  }
	  else
	  {
	     this.wgtDlg.hide();
	  }
   }

   /**
    * Open the dialog box connecting the callers Ok and Cancel methods to the 
    * buttons in the dialog.
    * After adding the snippet it is repositioned as per the value of position, 
    * with respect to the snippet represented by the supplied relationship id.
    */
   this.open = function(okCallback, slotId, refRelId, position)
   {
      dojo.lang.assert(slotId, "slotId parameter must be specified");
      dojo.lang.assert(slotId.isSlotNode(), "Must pass a slot id");
      ps.aa.controller.enableConflictStyleSheets(false);
      if(position == null)
          position = "before";
      this.slotId = slotId;
      this.okCallback = okCallback;
      this.refRelId = refRelId;
      this.position = position;
      
      this.maybeCreateBrowseDialog();
	  if(!this.isStandAlone)
	  {
         ps.util.setDialogSize(this.wgtDlg, this.preferredWidth, this.preferredHeight);
	  }
	  else
	  {
	     var _this = this;
		 dojo.lang.setTimeout(function ()
		  {
			 _this.fillInParentWindow()
		  }, 500);
	  }

      // simulate selection
      var tabContainer = dojo.widget.byId("ps.content.mainTabContainer");
      var tab0 = tabContainer.children[0];
      tabContainer.selectChild(tab0, tabContainer);	

      this.wgtDlg.show();
      if (dojo.render.html.ie55 || dojo.render.html.ie60)
      {
         // otherwise it does not displayed when the dialog is shown second time
         dojo.html.hide(tabContainer.domNode);
         dojo.html.show(tabContainer.domNode);
      }
      
      if (dojo.render.html.safari)
      {
         // Force this tab to be selected to get around bug
         // with Safari not displaying or laying out the tab
         // correctly on intial tab load.
         dojo.lang.setTimeout(function ()
         {
            tabContainer.selectChild(tab0, tabContainer);
         }, 500);
      }

      if (dojo.render.html.ie55 || dojo.render.html.ie60)
      {
         dojo.lang.setTimeout(function ()
         {
            // workaroundof of an IE6 bug, when the dialog container
            // does not show up when the dialog is open second time
            dojo.html.hide(tabContainer.domNode);
            dojo.html.show(tabContainer.domNode);
         }, 1000 * 5);
      }
   }
   /**
    * Causes the dialog to fill in the entire parent window and moves it
	* to Top: 0 Left: 0
	*/
   this.fillInParentWindow = function()
	{
      var scrSize = ps.util.getScreenSize(null, true);
		this.wgtDlg.domNode.style.top = 0;
		this.wgtDlg.domNode.style.left = 0;
		this.wgtDlg.resizeTo(scrSize.width, scrSize.height);		
	}
}

