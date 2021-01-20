dojo.provide("ps.content.SearchTabPanel");

dojo.require("ps.content.History");
dojo.require("ps.content.BrowseTabPanel");
dojo.require("ps.content.SelectTemplates");
dojo.require("ps.widget.ContentPaneProgress");
dojo.require("ps.util");
dojo.require("dojo.collections.Dictionary");
dojo.require("dojo.lang.declare");
dojo.require("dojo.json");
dojo.require("dojo.string");

dojo.declare("ps.content.SearchTabPanel", ps.content.BrowseTabPanel,
	function(_parent){
	   dojo.lang.assert(_parent, "Parent must be specified");

      this.prefix = ps.util.BROWSETAB_SEARCH_PANEL_PREF;
      this.parent = _parent; 		
      this.searchScriptElem = null;		
      this.isSearchSimple = false;        
	},
{
   init: function()
   {
      this.tabId = this.prefix + ".tab";
      this.tab = dojo.widget.byId(this.tabId);
      dojo.lang.assert(this.tab, "Tab for " + this.prefix + " should exist");
      this.url = this.parent.rxroot + "/ui/content/searchpanel.jsp";
      ps.content.SearchTabPanel.superclass.init.apply(this);
   },

   /**
    * Handles the various things needed for each search mode view
    */
   setSearchMode: function(isForm)
   {
      // Return if not the search tab
      if (!this.isSearchTab())
         return;
      this.isSearchForm = isForm;

     if (this.isSearchForm)
      {
         this._setSplitPaneChildVisible(this.contentSplitPane, this.searchformPanel, true);
         // We first remove the filter panel then add it
         // so it appears in the correct order. 
         this._setSplitPaneChildVisible(this.contentSplitPane, this.filterPanel, false);
         this._setSplitPaneChildVisible(this.contentSplitPane, this.filterPanel, true);
         this._setSplitPaneChildVisible(this.contentSplitPane, this.filteringTablePanel, false);

         dojo.html.hide(this._getQId("nameAndCtypeFilterDiv"));
         dojo.html.show(this._getQId("siteAndFolderFilterDiv"));
         this.searchBackButton.hide();
         this._maybeSetButton(this.okButton, 
            psxGetLocalMessage("javascript.ps.content.browse@Search"), false);
      }
      else
      {
         this._setSplitPaneChildVisible(this.contentSplitPane, this.filteringTablePanel, true);
         // We first remove the filter panel then add it
         // so it appears in the correct order. 
         this._setSplitPaneChildVisible(this.contentSplitPane, this.filterPanel, false);
         this._setSplitPaneChildVisible(this.contentSplitPane, this.filterPanel, true);
         this._setSplitPaneChildVisible(this.contentSplitPane, this.searchformPanel, false);
         dojo.html.show(this._getQId("nameAndCtypeFilterDiv"));
         dojo.html.hide(this._getQId("siteAndFolderFilterDiv"));
         this.searchBackButton.show();
         this._maybeSetButton(this.okButton, 
            psxGetLocalMessage("javascript.ps.content.browse@Open"), false);
      }
   },  

   /**
    * Shows or hides the advanced search fields based on the search mode.
    */
   _showSearchFields: function()
   {
      if(this.isSearchSimple)
      {
         dojo.html.show(dojo.byId("advancedfields"));
         this.advancedButton.hide();
         this.simpleButton.show();
      }
      else
      {
         dojo.html.hide(dojo.byId("advancedfields"));
         this.advancedButton.show();
         this.simpleButton.hide();
      }
      this.isSearchSimple = !this.isSearchSimple;
      this._enableDisableSearch();
   },

   _onOk: function()
   {
      if (!this.isSearchForm || this.isSelectTemplateMode)
      {
         return ps.content.SearchTabPanel.superclass._onOk.apply(this);
      }
      else 
      var inclsites = this.includeSitesCheckbox.checked ? "yes" : "no";
      var inclfolders = this.includeFoldersCheckbox.checked ? "yes" : "no";
      var sf = document.searchQuery;
      sf.includeSites.value = inclsites;
      sf.includeFolders.value = inclfolders;
      sf.sys_searchMode.value = "simple";
      if(!this.isSearchSimple)
         sf.sys_searchMode.value = "advanced";

      if (!this._validateQueryForSynonymExp())
         return;
      if (!this._validateQuery())
         return;

      var response = ps.io.Actions.submitForm(document.searchQuery);
      if(response == null)
         return;
      ps.io.Actions.maybeReportActionError(response);
      if(!response.isSuccess())
      {
         return false;
      }

      // reset table columns
      this.contentTable.reset();
      var head = this.contentTable.domNode.getElementsByTagName("thead")[0];
      dojo.dom.removeChildren(head);
      this.contentTable.columns = this._cloneColumns(this.contentTableColumns);

      if (this.includeSitesCheckbox.checked)
      {
         this._addContentTableColumn("Site", "Site");
      }
      if (this.includeFoldersCheckbox.checked)
      {
         this._addContentTableColumn("Folder", "Folder");
      }
      this.setContent(response.getValue());
      this.setSearchMode(false);
      dojo.event.connect(
         this.contentTable, "onSelect", this, "_onContentTableSelect");
      this._setButtonRememberState("okButton", this.okButton, null, true);
      return true;
   },

   /**
    * Utility function to handle the search cleanup code on close.
    */
   _onCancel: function()
   {
      if(this.isSelectTemplateMode)
         return this._handleTemplateCancel();
      var _this = this;
      dojo.lang.setTimeout(function ()
      {
         _this.setSearchMode(true);
      }, 500);

      if(typeof psSearch != "undefined")
         psSearch = null;
      ps.content.SearchTabPanel.superclass._onCancel.apply(this);
   },

   /**
    * Validates that the full text search query
    * does not begin with '*' or '?' as this is
    * not allowed by the lucene search engine.
    */
   _validateQuery: function()
   {
      if (!document.searchQuery.sys_fulltextquery)
         return true;

      var searchForValue = document.searchQuery.sys_fulltextquery.value;
      var firstChar = searchForValue.charAt(0);
      if (firstChar == '*' || firstChar == '?')
      {
         var msg = psxGetLocalMessage("javascript.ps.content.browse@Invalid_First_Char");
         alert(msg);
         return false;
      }
      else
         return true;
   },

   /**
    * Validates that the full text search query
    * does not include any characters defined as
    * "special" by lucene.
    */
   _validateQueryForSynonymExp: function()
   {
      if (!document.searchQuery.sys_synonymexpansion)
         return true;

      var synonymExp = document.searchQuery.sys_synonymexpansion.checked;
      if (synonymExp)
      {                   
         var searchForValue = document.searchQuery.sys_fulltextquery.value;
         var spChars = "";
         var specialChars = new Array("+", "-", "&&", "||", "!", "(", ")", "{", "}", "[", "]", "^", "\"", "~", "*", "?", ":", "\\");
         for (var i = 0; i < specialChars.length; i++)
         {
            var spChar = specialChars[i];
            if (searchForValue.indexOf(spChar) != -1)
            {
               if (spChars.length == 0)
                  spChars = spChar;
               else
                  spChars += ", " + spChar;
            }
         }
               
         if (spChars.length > 0)
         {
            var msg = psxGetLocalMessage("javascript.ps.content.browse@Invalid_Chars_Synonym_Exp");
            alert(msg + " " + spChars);
            return false;
         }
      }
    
      return true;
   },

   /**
    * Load the search form from the server.
    * todo: deal with the inline javascript.
    */
   loadSearchForm: function()
   {
      if (!this.isSearchTab())
      {
         // not the search tab
         return;
      }

      if (!this._isTabLoaded())
      {
         // tab is not loaded yet
         // repeat the call to _onTabSelected on load
         this.selectOnLoad = true;
         return;
      }

      var slotId = this.parent.slotId;
      if (this.searchformPanel.slotId
            && this.searchformPanel.slotId.serialize() === slotId.serialize())
      {
         // already loaded search form for this slot id
         return;
      }
      this.searchformPanel.slotId = slotId;

      // clean up script
      if (this.searchScriptElem)
      {
         this.searchformPanel.setContent("");
         dojo.dom.removeNode(this.searchScriptElem, true);
         this.searchScriptElem = null;
      }

      this.isSearchSimple = false;

      //Get the rc search url
      var response = ps.io.Actions.getUrl(this.parent.slotId, "RC_SEARCH");
      // handle failure
      if (!response.isSuccess())
      {
         ps.io.Actions.maybeReportActionError(response);
         return;
      }
      this.searchFormUrl = response.getValue().url;
      this.searchformPanel.setUrl(this.searchFormUrl + "&genMode=aaHTML");
   },
   
   /**
    * Is called when the search form panel is loaded.
    */
   _onSearchFormPanelLoad: function ()
   {
      if (!dojo.byId("advancedfields"))
      {
         // this method was called on form cleanup
         return;
      }

      this.advancedButton = dojo.widget.byId("ps.search.advanced");
      this.simpleButton = dojo.widget.byId("ps.search.simple");
      this.ftquery = dojo.byId("searchfor");     
     
      this._loadSearchScript();
      
      ps.io.Actions.initFormBind(ps.io.Actions.getRcSearchUrl(), "searchQuery",
            ps.io.Actions.MIMETYPE_JSON);
      if (this.advancedButton)
      {
         dojo.event.connect(
               this.advancedButton, "onClick", this, "_showSearchFields");
      }
      if (this.simpleButton)
      {
         dojo.event.connect(
               this.simpleButton, "onClick", this, "_showSearchFields");
      }
      if (this.advancedButton)
      {
         this._showSearchFields();
      }

      if (this.ftquery)
      {
         dojo.event.connect(
               this.ftquery, "onkeyup", this, "_enableDisableSearch");
      }
      this._enableDisableSearch();
   },

   /**
    * Load the search javascript into the document head
	*/
   _loadSearchScript: function()
	{
      // The search script must be loaded each time as the reference
	  // to javascript variables get lost.
	  var head = document.getElementsByTagName("head")[0];
	  var scr = document.getElementById("ps.content.search.searchScript");
	  if(scr)
	  {
		  head.removeChild(scr);
	  }
      this.searchScriptElem = document.createElement('script');
      this.searchScriptElem.id = 'ps.content.search.searchScript';
      this.searchScriptElem.type = 'text/javascript';
      this.searchScriptElem.src = this.searchFormUrl + "&genMode=aaJS";
      head.appendChild(this.searchScriptElem);
	},

   /**
    * Disable search button if the search is in simple mode and the query field id not empty.
    */
   _enableDisableSearch: function()
   {
      var q = this.ftquery;
      this._maybeSetButton (this.okButton, null,
            this.isSearchSimple && q && dojo.string.isBlank(q.value));
   },

   /**
    * Is called when a tab in the tab container is selected.
    */
   _onTabSelected: function (tab)
   {
      this.loadSearchForm();
      ps.content.SearchTabPanel.superclass._onTabSelected.apply(this, [tab]);
   },
   /**
    * Is called during initialization after the UI is loaded.
    */
   _initOnLoad: function ()
   {
      ps.content.SearchTabPanel.superclass._initOnLoad.apply(this);
      this.setSearchMode(true);
   },
   /**
    * Set the searchmode to true
    */
   _onSearchAgain: function()
   {
      this.setSearchMode(true);
   },
	
   /**
    *  Parses all the expected controls for this tab.
    */
   parseControls: function()
   {
      this._parseCommonControls();
      this._parseFilterPanelControls();
      this.contentTable = this._getWidgetById("FilteringTable");
      // initial conent table columns
      this.contentTableColumns = this._cloneColumns(this.contentTable.columns);
      this._parseSearchPanelOnlyControls();      
   },
	
	/**
    * Parses the filter panel controls and add appropriate events.
    */
   _parseFilterPanelControls: function()
   {
      this.includeSitesCheckbox = this._getElemById("includeSitesCheckbox");
      this.includeFoldersCheckbox = this._getElemById("includeFoldersCheckbox");
      this.filterPanel = this._getWidgetById("filterpanel");
      this.filteringTablePanel = this._getWidgetById("tablepanel");
      this.filteringTable = this._getWidgetById("FilteringTable");
      this.filterText = this._getElemById("filterText");

      dojo.event.connect(this.filterText, "onkeyup", this, "_onFilterTyped");
      //Set the default values for include site id and folder id check boxes
      var inclSitesFlag = ps.util.getServerProperty("slotContentIncludeSiteDefaultValue","");
      var inclFoldersFlag = ps.util.getServerProperty("slotContentIncludeFolderDefaultValue","");
      if(inclSitesFlag == "true")
      {
         this.includeSitesCheckbox.checked = true;
      }
      if(inclFoldersFlag == "true")
      {
         this.includeFoldersCheckbox.checked = true;
      }

      this._ctypeList = this._getElemById("ctypeList");
      dojo.html.hide(this._ctypeList.parentNode);      
   },
	
	/**
    * Parses controls that only exist in the search panel.
    */
   _parseSearchPanelOnlyControls: function()
   {
      this.searchformPanel = this._getWidgetById("searchformpanel");
      new ps.widget.ContentPaneProgress(this.searchformPanel);
      dojo.event.connect(
            this.searchformPanel, "onLoad", this, "_onSearchFormPanelLoad");
      

      this.searchBackButton = this._getWidgetById("searchBackButton");
      dojo.event.connect(this.searchBackButton, "onClick", this, "_onSearchAgain");
   },
	
   /**
    * Is called when templates selection panel is loaded.
    */
   _onTemplatesSiteFolderParamLoaded: function ()
   {
      
         var includeSites = this._mustById(
               "ps.select.templates.includeSitesCheckbox");
         includeSites.checked = this.includeSitesChecked;
         includeSites.disabled = true;

         var includeFolders = this._mustById(
               "ps.select.templates.includeFoldersCheckbox");
         includeFolders.checked = this.includeFoldersChecked;
         includeFolders.disabled = true;
      
   },
	
	/**
    * Returns site name selected in UI, if available and requested by the user.
    * Otherwise returns <code>null</code>.
    */
   _getUISiteName: function ()
   {      
      if (this.templatesPanelObj.siteName)
      {
         return this.templatesPanelObj.siteName;
      }
      else
      {
         return null;
      }      
   },
   
   /**
    * Returns folder path selected in UI, if available and requested by the user.
    * Otherwise returns <code>null</code>.
    */
   _getUIFolderPath: function ()
   {
      if (this.templatesPanelObj.folderPath)
      {
         return this.templatesPanelObj.folderPath;
      }
      else
      {
         return null;
      }      
   },
	
	/**
    * Reloads data from the server for the current path.
    * Overriden to do nothing in the search tab
    */
   _refresh: function()
	{
      // no-op	
	}	      
});
