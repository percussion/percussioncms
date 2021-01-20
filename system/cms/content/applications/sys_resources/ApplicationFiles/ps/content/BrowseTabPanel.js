dojo.provide("ps.content.BrowseTabPanel");

dojo.require("ps.content.History");
dojo.require("ps.content.SelectTemplates");
dojo.require("ps.widget.ContentPaneProgress");
dojo.require("ps.util");
dojo.require("dojo.collections.Dictionary");
dojo.require("dojo.lang.declare");
dojo.require("dojo.json");
dojo.require("dojo.string");


/**
 * <p>An individual tab in the browser dialog</p>.
 * <p>Stores current path as a concatenation of site, folder names, separated
 * by '/' character. For an item this class stores path to its folder with
 * appended '|' character, and numeric item id.
 * </p>
 */
dojo.declare("ps.content.BrowseTabPanel", null,
	function(_parent){
	   dojo.lang.assert(_parent, "Parent must be specified");

      this.parent = _parent;
      this.isSearchForm = true;
      this.isSelectTemplateMode = false;
      this.lastButtonState = new dojo.collections.Dictionary();
      this.templatesPanelObj = null;     
	},
{
   /**
    * Initializes the tab.
    */
   init: function ()
   {
      dojo.event.topic.subscribe(
            this.tab.parent.domNode.id + "-selectChild", this, "_onTabSelected");
      this.tab.setUrl(this.url);

      if (this._isTabLoaded())
      {
         this._initOnLoad();
      }
      else
      {
         var _this = this;
         dojo.event.connect(this.tab, "onLoad", function()
         {
            try
            {
               _this._initOnLoad();
            }
            catch (e)
            {
               dojo.debug(e);
            }
         });
      }
   },   
   /**
    * Forces tab to relayout itself to work around broken layout when user
    * switches to a different tab not waiting when the tab is loaded completely.
    * Andriy: I saw this problem on a slow IE6.
    */
   _redoLayout: function()
   {
      var _this = this;
      dojo.lang.setTimeout(function ()
      {
         var p = _this.tab.parent;
         var w = p.width;
         w = w % 2 ? w + 1 : w - 1;
         var styleWidth = p.domNode.style.width;
         var styleHeight = p.domNode.style.height;
         p.resizeTo(w, p.height);

         // restore style, so the size won't be forced
         // and layout during resizing would work
         p.domNode.style.width = styleWidth;
         p.domNode.style.height = styleHeight;
      }, 1000);
   },
   
   /**
    * Returns <code>true</code> if the tab is already loaded.
    */
   _isTabLoaded: function()
   {
      return !!this._getWidgetById("okButton");
   },

   /**
    * Is called during initialization after the UI is loaded.
    */
   _initOnLoad: function ()
   {
      this.rootContentActions = this._defineRootContentActions();
      dojo.lang.assert(this._isTabLoaded(),
            "Tab " + this.prefix + " should be loaded already");

      this.parseControls();

      //this.setSearchMode(true);
      
      if (this.selectOnLoad)
      {
         // tab is already selected
         this.selectOnLoad = false;
         this._onTabSelected(this.tab);
      }
      this._redoLayout();
   },

   /**
    * Is called when a tab in the tab container is selected.
    */
   _onTabSelected: function (tab)
   {
     
      if (tab !== this.tab)
      {
         return;
      }
      this.parent.currentTab = this;

      if (!this.slotId
            || this.slotId.serialize() !== this.parent.slotId.serialize())
      {
         if (!this._isTabLoaded())
         {
            // tab is selected, but not loaded yet
            // repeat the call to this method on load
            this.selectOnLoad = true;
            return;
         }

         this.slotId = this.parent.slotId;
         if (!this.pathHistory)
         {
            this.pathHistory = this._createInitialHistory();
         }

         this._refresh();
      }
      //Force resize the dialog to avoid painting issues on some browsers (cheaper and easier!)
      ps.util.forceDialogResize(this.parent.wgtDlg, 
         this.parent.preferredWidth, 
         this.parent.preferredHeight);
      //			
   },

   /**
    * Returns <code>true</code> if the tab is already loaded.
    */
   _isTabLoaded: function()
   {
      return !!this._getWidgetById("okButton");
   },



   /**
    *  Parses all the expected controls for this tab.
    */
   parseControls: function()
   {
      //Abstract function implemented by subclass
   },
   
   /**
    * Parse the controls common to all tabs
    */ 
   _parseCommonControls: function()
   {
      this.okButton = this._getWidgetById("okButton");
      this.cancelButton = this._getWidgetById("cancelButton");
      this.mainSplitPane = this._getWidgetById("mainsplitpane");
      this.contentSplitPane = this._getWidgetById("contentsplitpane");
      this.commandPanel = this._getWidgetById("commandpanel");

      dojo.event.connect(this.okButton, "onClick", this, "_onOk");
      dojo.event.connect(this.cancelButton, "onClick", this, "_onCancel");
   },   

   /**
    * Callback function called when the OK button is clicked.
    * This will also call the parents okCallback function.
    */
   _onOk: function()
   {
      if(this.isSelectTemplateMode)
      {
         this._handleTemplateOk();
         return true;
      }
      var row = this.contentTable.getSelectedData();
      var a = this._getRowA(row);
      a.onclick();
   },

   
   /**
    * Adds a column to the content table.
    * @param {String} field the column field name.
    * Not <code>null</code>.
    * @param {String} label the column label.
    * Not <code>null</code>.
    */
   _addContentTableColumn: function (field, label)
   {
      dojo.lang.assertType(field, String);
      dojo.lang.assertType(label, String);
      
      var col = this._cloneColumn(this.contentTableColumns[0]);
      col.field = field;
      col.label = label;
      this.contentTable.columns.push(col);
   },
   
   /**
    * Returns site name selected in UI, if available and requested by the user.
    * Otherwise returns <code>null</code>.
    */
   _getUISiteName: function ()
   {
      return null;
   },
   
   /**
    * Returns folder path selected in UI, if available and requested by the user.
    * Otherwise returns <code>null</code>.
    */
   _getUIFolderPath: function ()
   {
      return null;
   },
   
   /**
    * Same as <code>dojo.byId</code>, but throws an assertion if the node
    * does not exist.
    */
   _mustById: function (id)
   {
      var node = dojo.byId(id);
      dojo.lang.assert(node, "Could not find node " + id);
      return node;
   },
   
   _handleTemplateOk: function()
   {
      var snippetId = this.templatesPanelObj.getSelectedId();
      var response = ps.io.Actions.resolveSiteFolders(
	      this._getUISiteName(), this._getUIFolderPath());
	   if(response.isSuccess())
	   {
	       var temp = response.getValue();
		    if(temp.sys_folderid != undefined)
		    {
             snippetId.setFolderId(temp.sys_folderid);
		    }
		    if(temp.sys_siteid != undefined)
		    {
             snippetId.setSiteId(temp.sys_siteid);
		    }		 
	  }
	  else
	  {
         ps.io.Actions.maybeReportActionError(response);
	  }

      var slotId = this.parent.slotId;
      if(this.parent.mode == ps.util.BROWSE_MODE_ACTIVE_ASSEMBLY ||
		     this.parent.mode == ps.util.BROWSE_MODE_ACTIVE_ASSEMBLY_TABLE_EDITOR)
	   { 
         var response = ps.io.Actions.addSnippet(
               snippetId, slotId, this._getUIFolderPath(), this._getUISiteName());
         if(response.isSuccess())
         {
            this.parent.okCallback(slotId) ;
            if(this.parent.mode == ps.util.BROWSE_MODE_ACTIVE_ASSEMBLY_TABLE_EDITOR)
            {
               //no-op
            }
            else
            {
               var newRelId = response.getValue();
               var refRelId = this.parent.refRelId;
               var position = this.parent.position;
               ps.aa.controller.repositionSnippet(slotId,refRelId,newRelId,position);
               //Reset the reference to new rel id and position to "after"
               this.parent.refRelId = newRelId;
               this.parent.position = "after";
            }
         }
         else
         {
            ps.io.Actions.maybeReportActionError(response);
         }
      }
      else
      {
         this.parent.okCallback(snippetId);
      }
      this._maybeRestoreButtonState("okButton");
      this.cancelButton.setCaption(this.CLOSE_LABEL);
      this.setSelectTemplateMode(false);
   },
   
   _handleTemplateCancel: function()
   {
      if(!this.isSelectTemplateMode)
         return true;
      this._maybeRestoreButtonState("okButton");
      this.cancelButton.setCaption(this.CLOSE_LABEL);
      this.setSelectTemplateMode(false);
      return false;
   },
   
   /**
    * Callback function called when the Cancel button is clicked.
    * This will also call the parents cancelCallback function and
    * close the dialog.
    */
   _onCancel: function()
   {      
      var shouldClose = this._handleTemplateCancel();
      if(shouldClose)
         this.parent.close();
   },
   
   /**
    * Sets the button state and records current state values.
    * @param {string} name the name key for this button as will be stored
    * in the last button state variable.
    * @param {dojo.widgt.Button} button the button to be modified.
    * @param {string} caption the button caption to be set.
    * @param {boolean} disabled the flag indicating if the button
    *  should be disabled.
    */
   _setButtonRememberState: function(name, button, caption, disabled)
   {
      var obj = new Object();
      obj.disabled = button.disabled;
      obj.ref = button;
      obj.caption = button.caption;
      this._maybeSetButton(button, caption, disabled);
      this.lastButtonState.add(name, obj);      
   },
   
   /**
    * Sets the button's caption or disabled state only if different
    * from what it is currently set to.
    * @param {dojo.widget.Button} button the button to be modified.
    * @param {string} caption the button caption to be set.
    * @param {boolean} disabled the flag indicating if the button
    *  should be disabled.
    */
   _maybeSetButton: function(button, caption, disabled)
   {
      if(disabled != null && button.disabled != disabled)
      {
         button.setDisabled(disabled);

         // a workaround for a problem when a button is disabled multiple times,
         // it should be enabled multiple times,
         // to completely switch to the "enabled" appearance
         if (!disabled)
         {
            button.setDisabled(disabled);
         }
      }
      if(caption != null && button.caption != caption)
         button.setCaption(caption);   
   },
   
   /**
    * Restores the button state if needed.
    */
   _maybeRestoreButtonState: function(name)
   {
      if(this.lastButtonState.containsKey(name))
      {
         var obj = this.lastButtonState.item(name);
         this._maybeSetButton(obj.ref, obj.caption, obj.disabled);
         this.lastButtonState.remove(name);
      }   
   
   },

   /**
    * Callback function called when the filter text changes.
    */
   _onFilterTyped: function ()
   {
      var _this = this;
      
      // to eliminate too frequent refresh
      if (this.lastFilterValue === this.filterText.value)
      {
         return;
      }
      this.lastFilterValue = this.filterText.value;
      
      // delay the execution until this.filterText.value is updated
      dojo.lang.setTimeout(function ()
      {
         _this._filterContentTable();
      }, 10);
   },
   
   /**
    * Filters contentTable rows using the value of the filterText field
    * as a filter.
    */
   _filterContentTable: function ()
   {
      var _this = this;
      this.contentTable.setFilter(psxGetLocalMessage("javascript.ps.content.browse@Name"), function (value)
      {
         var str = value.toLowerCase();
         var filter = _this.filterText.value.toLowerCase();
         return dojo.string.isBlank(filter)
               || _this._getCellText(str).indexOf(filter) !== -1;
      });
   },
   
   /**
    * Callback function called when the content table selection changes.
    */
   _onContentTableSelect: function (obj)
   {
      this._maybeRestoreButtonState("okButton");
      this._maybeSetButton(this.okButton, this.okButton.caption, 
         !this.contentTable.getSelectedData());
   },

   /**
    * Callback function called when a selection changes on the content type list.
    */
   _onCTypeChanged: function ()
   {
      this._refresh();
   },
   
   /**
    * Callback function called when the content type dropdown gets focus.
    */
   _onCTypeFocused: function ()
   {
      var list = this.getContentTypeList();
      if (!list.slotId
            || list.slotId.serialize() != this.slotId.serialize())
      {
         list.slotId = this.slotId;

         var response = ps.io.Actions.getAllowedContentTypeForSlot(this.slotId);
         ps.io.Actions.maybeReportActionError(response);
         var typesNum = 0;
         if (response.isSuccess())
         {
            var marker = list.options[0].nextSibling;
            var types = response.getValue();
            typesNum = types.length;

            // add the 
            for (i = 0; i < typesNum; i++)
            {
               var option = document.createElement('option');
               option.appendChild(document.createTextNode(types[i].name));
               option.value = types[i].contenttypeid;
               list.insertBefore(option, marker);
            }

            // delete the previous options, leave the first "All" entry.
            // adding/removing is done in this complex way to prevent the dropdown
            // from resizing all the time
            list.options.length = 1 + typesNum;
         }
      }
   },

   /**
    * Is called when the path of the tab is changed.
    * Updates state of UI conrols based on the current data.
    */
   _onPathChanged: function ()
   {
      this._setLastStoredPath(this.getFolder());
      this.pathText.value = this.getFolder();

      this._setButtonDisabledSpecial(this.backButton, !this.pathHistory.canGoBack());
      this._setButtonDisabledSpecial(this.upButton, this.getPath() === this.ROOT);

      this._maybeRestoreButtonState("okButton");
      this._maybeSetButton(this.okButton, 
         psxGetLocalMessage("javascript.ps.content.browse@Open"), true);
      
      this._scrollPathText();
      this.filterText.value = "";
   },
   
   /**
    * Special function to switch the disabled image for the address
    * bar buttons.
    * @param {dojo.widget.Button} button the button to be disabled.
    * @param {boolean} disabled <code>true</code> if disabled.
    */
   _setButtonDisabledSpecial: function(button, disabled)
   {
      if (button.disabled == disabled)
         return;
      button.setDisabled(disabled);

      // replace button image
      var node = button.containerNode ? button.containerNode : button.domNode;
      var img = node.getElementsByTagName('img')[0];
      dojo.lang.assert(img, "Expected to find an image in a button content.");
      if (disabled)
      {
         var from = /16\.gif/;
         var to = "_disabled16.gif";
      }
      else
      {
         var from = /\_disabled16\.gif/;
         var to = "16.gif";
      }
      img.src = img.src.replace(from, to);
   },
   
   /**
    * Scrolls path field text, so it shows the last portion of the text.
    * There is no cross-browser way to do that.
    * Had to resort to browser-specific hacks
    * Looks like DOM 3 will provide common key event handling API.
    */
   _scrollPathText: function ()
   {
      // move cursor to the end of the field
      try
      {
         this._setCaretToEnd(this.pathText);
      }
      catch (ignore)
      {
         // for some reason switching back to a tab on Firefox 2.0
         // causes an exception here
      }
      
      if (this.pathText.dispatchEvent)
      {
         try
         {
            // add a dummy space character to make the field scroll
            var e = document.createEvent("KeyboardEvent");
            e.initKeyEvent("keypress", true, true, window,
                  false, false, false, false, 32, 32);
            this.pathText.dispatchEvent(e);

            // remove it with backspace
            var e = document.createEvent("KeyboardEvent");
            e.initKeyEvent("keypress", true, true, window,
                  false, false, false, false, 8, 0);
           this.pathText.dispatchEvent(e);
         }
         catch(ignore){}
      }
      else
      {
         // For some browsers (e.g.) IE just setting caret scrolls the field.
         // Just ignore for other browsers, such as Safari, Konqueror,
         // because we can't do anything for them.
      }
   },
   
  /**
    * Switches the tab to the new path.
    * Refreshes the tab content for the new path.
    * @param {String} path the path to switch to.
    * Not <code>null</code>. 
    */
   _goTo: function (path, row)
   {
      dojo.lang.assertType(path, String);
      var _this = this;

      function maybeSetContent(response)
      {
         if (response.isSuccess())
         {
            _this.pathHistory.add(path);
            _this.setContent(response.getValue());
         }
      }

      // strip last "/"
      var last = path.length - 1;
      if (last !== 0 && path.charAt(last) === "/")
      {
         path = path.substring(0, path.length - 1);
      }

      if (path === this.ROOT)
      {
         var action = this.rootContentActions[this.prefix];
         dojo.lang.assert(action,
              "Root content action for " + this.prefix + " should be specified");
         var response = action();
         maybeSetContent(response);
      }
      else if (this.isItemPath(path))
      {
         // generate a snippet id from the slot id
         var snippetId = this.parent.slotId.clone();
         var cid = this.parseContentIdFromPath(path);
         snippetId.setContentId(cid);
         snippetId.setSnippetNode();

         this.templatesPanelObj = this._loadTemplatesPanel(snippetId);
         if (row)
         {
            this.templatesPanelObj.siteName = row.Site;
            this.templatesPanelObj.folderPath = row.Folder;
         }
         this.setSelectTemplateMode(true);
         this._setButtonRememberState("okButton", this.okButton, 
            psxGetLocalMessage("javascript.ps.content.browse@Select"), false);
         this.cancelButton.setCaption(psxGetLocalMessage("javascript.ps.content.browse@Back"));
        			
			// See _handleTemplateOk as this is the callback that
         // will handle the work after template selection.
         return;
      }
      else // a folder path
      {
         var response = this._getFolderChildren(path);
         maybeSetContent(response);
      }
      
      dojo.lang.assert(response, "Response should be defined");
      ps.io.Actions.maybeReportActionError(response);
      response.isSuccess() && this._onPathChanged();
   },
   
   /**
    * Creates a copy of a FilteringTable.columns data.
    * @param {Array} columns the array to copy.
    */
   _cloneColumns: function (columns)
   {
      dojo.lang.assertType(columns, Array);
      var newColumns = [];
      dojo.lang.forEach(columns, function(column)
      {
         newColumns.push(this._cloneColumn(column));
      }, this)
      return newColumns;
   },
   
   /**
    * Creates a copy of one FilteringTable.columns data element.
    * @param {Array} columns the array to copy.
    */
   _cloneColumn: function (column)
   {
      return dojo.lang.shallowCopy(column, false)
   },

   /**
    * Helper method to parse the content id from the
    * path string.
    * @param {string} the path string.
    */
   parseContentIdFromPath: function(path)
   {
      var temp = path.split("|");
      if(temp.length < 2)
         return "";
      return dojo.string.trim(temp[1]);   
   },

   /**
    * Refreshes the tab.
    */
   refreshBrowser: function()
   {
      this._refresh();
   },
   
   /**
    * Reloads data from the server for the current path.
    */
   _refresh: function ()
   {
      this._goTo(this.getPath());
   },

   /**
    * Makes sure the provided object is valid not-null content table row data.
    * @param row the data to check.
    */
   _assertValidRow: function (row)
   {
      // Do not validate the ID column since it can be a number (simple id for 
      // a site/folder child) or a string (complex id in case of search results)
      dojo.lang.assertType(row.Name, String);
      dojo.lang.assertType(row.Description, String);
      dojo.lang.assertType(row.Type, Number);
   },
   
   /**
    * Actions to get root content.
    */
   _defineRootContentActions: function ()
   {
      var actions = {};
      actions[ps.util.BROWSETAB_SITES_PANEL_PREF] =
            function () {return ps.io.Actions.getSites();};
      actions[ps.util.BROWSETAB_FOLDERS_PANEL_PREF] =
            function () {return ps.io.Actions.getRootFolders();};
      return actions;
   },

   /**
    * Creates history with initial record. Creates empty history if it is search
    *  tab.
    */
   _createInitialHistory: function()
   {
      if(this.isSearchTab())
      {
         return new ps.content.History("");
      }
      if (this._getLastStoredPath())
      {
         var path = this._getLastStoredPath();
      }
      else if (this.isSiteTab())
      {
         var path = this._getCurrentSitePath();
      }
      else
      {
         var path = this.ROOT;
      }

      dojo.lang.assert(path);

      // make sure the path is valid
      if (!this._isValidPath(path))
      {
         path = this.ROOT;
      }
      return new ps.content.History(path);
   },
   
   /**
    * Returns last path successfully accessed by the user, which is stored in a
    * cookie.
    */
   _getLastStoredPath: function ()
   {
      return dojo.io.cookie.getCookie(this.LAST_PATH_COOKIE);
   },

   /**
    * Stores the last visited path as a cookie.
    * @param {String} path the last successfully visited path to store.
    * Not <code>null</code>.
    */
   _setLastStoredPath: function (path)
   {
      dojo.lang.assertType(path, String);
      var daysStoreCookie = 100;
      dojo.io.cookie.setCookie(this.LAST_PATH_COOKIE, path, daysStoreCookie);
   },
   
   /**
    * Returns <code>true</code> if the provided path is valid.
    */
   _isValidPath: function (path)
   {
      return path === this.ROOT || this._getFolderChildren(path).isSuccess();
   },

   /**
    * Returns path for the current site if in Active Assembly mode or the
    * root in any other mode.
    * If has problems to determine the result reports the problems to the user
    * and returns the root directory. 
    */
   _getCurrentSitePath: function ()
   {
      if(this.mode != ps.util.BROWSE_MODE_ACTIVE_ASSEMBLY)
   	  return this.ROOT;

      var siteId = ps.aa.controller.pageId.getSiteId();
      dojo.lang.assert(dojo.lang.isNumeric(siteId), "Can't get site id");

      var response = ps.io.Actions.getSites();
      ps.io.Actions.maybeReportActionError(response);
      if (response.isSuccess())
      {
         var sites = response.getValue();
         var siteName;
         dojo.lang.forEach(sites, function (row)
         {
            if (row.Id == siteId)
            {
               siteName = row.Name;
            }
         }, this);
         dojo.lang.assert(siteName, "Site name was not found");
         var path = this.ROOT + this._getCellText(siteName);
      }
      else
      {
         var path = this.ROOT;
      }
      dojo.lang.assert(path, "path must be defined at this point");
      return path;
   },

   /**
    * Returns root children.
    * @param {String} path the folder to get children for.
    * Not <code>null</code>.
    * @return a response from {@link ps.io.Actions#getFolderChildren}.
    */
   _getFolderChildren: function (path)
   {
      dojo.lang.assertType(path, String);
      return ps.io.Actions.getFolderChildren(
            path, this._getContentType(),
            this.parent.slotId.getSlotId(),
            this.isSiteTab());
   },
   
   /**
    * Sets the caret to different positions and to set the selection should.
    * Uses browser-specific code.
    * Borrowed from http://www.faqts.com/knowledge_base/view.phtml/aid/13562.
    */
   _setSelectionRange: function (input, selectionStart, selectionEnd)
   {
      if (input.setSelectionRange)        // Mozilla
      {
         input.setSelectionRange(selectionStart, selectionEnd);
      }
      else if (input.createTextRange)     // IE
      {
         var range = input.createTextRange();
         range.collapse(true);
         range.moveEnd('character', selectionEnd);
         range.moveStart('character', selectionStart);
         range.select();
      }
   },

   /**
    * Sets the caret to beginning of the input.
    * Borrowed from http://www.faqts.com/knowledge_base/view.phtml/aid/13562.
    */
   _setCaretToEnd: function (input)
   {
      this._setSelectionRange(input, input.value.length, input.value.length);
   },
   
   /**
    * Replaces content of the content area.
    * @param {Array} rows an array of JSON objects to initializes the content
    * table with.
    * Not <code>null</code>, can be empty.
    */
   setContent: function (rows)
   {
      dojo.lang.assertType(rows, Array);
      rows.length > 0 && this._assertValidRow(rows[0]);
      
      this._setNameImages(rows);
      this.contentTable.store.setData(rows);
      this._addContentClickHandlers(rows);
   },

   /**
    * Adds image to the name HTML to indicate kind of table content entry.
    * @param {Array} rows an array of JSON objects the content table
    * will be initialized with.
    * Not <code>null</code>, can be empty.
    */
   _setNameImages: function (rows)
   {
      dojo.lang.assertType(rows, Array);
      rows.length > 0 && this._assertValidRow(rows[0]);

      var imgPref = '<img style="vertical-align: middle" src="';
      var folderImg = imgPref + this.parent.rxroot + '/sys_resources/images/folder.gif"/>&nbsp;';
      var itemImg = imgPref + this.parent.rxroot + '/sys_resources/images/item.gif"/>&nbsp;';
      var _this = this;
      dojo.lang.forEach(rows, function (row)
      {
         var img = row.Type === _this.ITEM_TYPE
               ? itemImg : folderImg;
         if(row.IconPath && row.IconPath.length>0)
         {
           var iPath = row.IconPath;
           if(iPath.substring(0, 3) == "../")
              iPath = "/Rhythmyx" + iPath.substring(2);
           img = imgPref + iPath + '"/>&nbsp;';
         }
         row.Name = img + row.Name;
      });
   },

   /**
    * Adds onclick handles on the names hyperlinks,
    * which open the content table entry.
    * @param {Array} rows an array of JSON objects the content table is
    * initialized table with.
    * Not <code>null</code>, can be empty.
    */
   _addContentClickHandlers: function (rows)
   {
      dojo.lang.assertType(rows, Array);
      rows.length > 0 && this._assertValidRow(rows[0]);

      var path = this.getPath();
      if (path !== this.ROOT)
      {
         path += "/";
      }
      
      // remember "checked" state now, because in IE the checkboxes checked
      // state is lost and it always reports them as unchecked later
      this.includeSitesChecked =
            this.includeSitesCheckbox && this.includeSitesCheckbox.checked;
      this.includeFoldersChecked =
            this.includeFoldersCheckbox && this.includeFoldersCheckbox.checked;

      dojo.lang.forEach(rows, function (row)
      {
         var a = this._getRowA(row);
         if (row.Type === this.ITEM_TYPE)
         {
            var idComponents = (row.Id + "").split(":")
            var newPath = path + this.ITEM_SEPARATOR + idComponents[0];
         }
         else
         {
            var newPath = path + dojo.html.renderedTextContent(a);
         }
         dojo.lang.assert(newPath);
         var _this = this;
         dojo.event.connect(a, "onclick",
               function() {_this._goTo(newPath, row);});
      }, this);
   },

   /**
    * Returns an <a /> dom node from the table for the provided data row
    * Throws an assertion error if such node does not exist.
    */
   _getRowA: function (row)
   {
      this._assertValidRow(row);

      var tr = this.contentTable.getRow(row);
      dojo.lang.assert(tr, "Table row should exist for this data");
      var nameTd = tr.getElementsByTagName("td")[0];
      dojo.lang.assert(nameTd, "Name cell should exist");
      var a = nameTd.getElementsByTagName("a")[0];
      dojo.lang.assert(a, "Name should be a hyperlink to open it");
      return a;
   },
   
   /**
    * Handles setting the appropriate panels visible for
    * select template mode.
    * @param {boolean} isTemplateMode whether the panel should be shown in a
    * template mode.
    */
   setSelectTemplateMode: function(isTemplateMode)
   {
      dojo.lang.assertType(isTemplateMode, Boolean);

      this.isSelectTemplateMode = isTemplateMode;
      if (isTemplateMode)
      {
         this._maybeCreateTemplatesPanel();
         this._setSplitPaneChildVisible(
            this.mainSplitPane, this.templatesPanel, true);
         if (!this.isFolderTab())
         {
            this._maybeCreateTemplatesSiteFolderParam();
            this._setSplitPaneChildVisible(
                  this.mainSplitPane, this.templatesSiteFolderParam, true);
         }
         // We first remove the command panel then add it
         // so it appears in the correct order.   
         this._setSplitPaneChildVisible(
            this.mainSplitPane, this.commandPanel, false);
         this._setSplitPaneChildVisible(
            this.mainSplitPane, this.commandPanel, true); 
         if(this.isSearchTab())
         {
            this._setSplitPaneChildVisible(
               this.mainSplitPane, this.contentSplitPane, false);
            this.searchBackButton.hide();   
         }
         else
         {
            this._setSplitPaneChildVisible(
               this.mainSplitPane, this.addressbarPanel, false);
            this._setSplitPaneChildVisible(
               this.mainSplitPane, this.clientPanel, false);
         }
      }
      else // !isTemplateMode
      {
         if(this.isSearchTab())
         {
            this._setSplitPaneChildVisible(
               this.mainSplitPane, this.contentSplitPane, true);
            if(!this.isSearchForm)
               this.searchBackButton.show();
         }
         else
         {
            this._setSplitPaneChildVisible(
               this.mainSplitPane, this.addressbarPanel, true);
            this._setSplitPaneChildVisible(
               this.mainSplitPane, this.clientPanel, true);
         }         
         // We first remove the command panel then add it
         // so it appears in the correct order.   
         this._setSplitPaneChildVisible(
            this.mainSplitPane, this.commandPanel, false);
         this._setSplitPaneChildVisible(
            this.mainSplitPane, this.commandPanel, true);    
         if (this.templatesPanel)
         {
            this._setSplitPaneChildVisible(
               this.mainSplitPane, this.templatesPanel, false);
         }
         if (this.templatesSiteFolderParam)
         {
            this._setSplitPaneChildVisible(
                  this.mainSplitPane, this.templatesSiteFolderParam, false);
         }
      }
      ps.util.forceDialogResize(this.parent.wgtDlg,
       this.parent.preferredWidth, this.parent.preferredHeight);      
   },
	
	 /**
    * Creates templatesPanel if it is not created yet.
    */
   _maybeCreateTemplatesPanel: function ()
   {
      if (this.templatesPanel)
      {
         // already created
         return;
      }
      var div = document.createElement('div');
      div.style.position = "absolute";
      div.style.padding = "10px";
      document.body.appendChild(div);

      var params = {
            id: this.prefix + ".templatespanel",
            sizeMin: 200,
            sizeShare: 95,
            executeScripts: true,
            cacheContent: false
      };

      this.templatesPanel =
            dojo.widget.createWidget("ContentPane", params, div);
      new ps.widget.ContentPaneProgress(this.templatesPanel);

   },

   /**
    * Creates templatesSiteFolderParam panel if it is not created yet.
    */
   _maybeCreateTemplatesSiteFolderParam: function ()
   {
      if (this.templatesSiteFolderParam)
      {
         // already created
         return;
      }
      var div = document.createElement('div');
      div.style.position = "absolute";
      div.style.padding = "10px";
      document.body.appendChild(div);

      var params = {
            id: this.prefix + ".templatessitefolderparam",
            sizeMin: 30,
            sizeShare: 10,
            executeScripts: true,
            cacheContent: false
      };

      this.templatesSiteFolderParam =
            dojo.widget.createWidget("ContentPane", params, div);
      new ps.widget.ContentPaneProgress(this.templatesSiteFolderParam);
   },
	
	/**
    * Loads the template select panel based on the passed in
    * slot object id.
    * @param {ps.aa.ObjectId} snippetId the snippet id to select a template for.
    * @return the template panel, never <code>null</code>.
    */
   _loadTemplatesPanel: function (snippetId)
   {
      dojo.lang.assertType(snippetId, ps.aa.ObjectId);

      if (!this.isFolderTab())
      {
         this._maybeCreateTemplatesSiteFolderParam();
         var newUrl = __rxroot + "/ui/content/sitefolderparam.jsp"
               + "?idPrefix=" + escape("ps.select.templates.")
               + "&includeSitesLabel=" + escape(
                  psxGetLocalMessage("javascript.ps.content.browse@Include_Site"))
               + "&includeFoldersLabel=" + escape(
                  psxGetLocalMessage("javascript.ps.content.browse@Include_Folder"));
         this.templatesSiteFolderParam.setUrl(newUrl);
         dojo.event.connect(this.templatesSiteFolderParam, "onLoad", function()
         {
            _this._onTemplatesSiteFolderParamLoaded();
         });
      }

      this._maybeCreateTemplatesPanel();
      var newUrl = __rxroot + "/ui/content/selecttemplate.jsp" + 
         "?noButtons=false&objectId=" + escape(snippetId.serialize());
      this.templatesPanel.setUrl(newUrl);
      var panel = new ps.content.SelectTemplates();
      var _this = this;

      dojo.event.connect(this.templatesPanel, "onLoad", function()
      {
         panel.initAsPanel(_this.parent.mode);
      });
      return panel;
   },

   /**
    * Sets the child of a split pane visible or invisible.
    * @param {dojo.widget} the split pane whose child will be
    * set.
    * @param {dojo.widget} the child to be made visible or not.
    * @param {boolean} isVisible flag indicating that the
    * filter panel should or should not be visible.
    */    
   _setSplitPaneChildVisible: function (splitpane, child, isVisible)
   {
      if(!child)
         return;

      if(isVisible)
      {
         var alreadyHasChild = false;
         
         for(var x = 0; x < splitpane.children.length; x++)
         {
            if(splitpane.children[x] === child)
            {
               alreadyHasChild = true;
               break;
            }
         }
         if(!alreadyHasChild)
            splitpane.addChild(child);
      }
      else
      {
         splitpane.removeChild(child);
      }
   },
      
   /**
    * Is called when templates selection panel is loaded.
    */
   _onTemplatesSiteFolderParamLoaded: function ()
   {
         //Set the default values for include site id and folder id check boxes
         var inclSitesFlag = ps.util.getServerProperty("slotContentIncludeSiteDefaultValue","");
         var inclFoldersFlag = ps.util.getServerProperty("slotContentIncludeFolderDefaultValue","");
         var includeSites = this._mustById(
               "ps.select.templates.includeSitesCheckbox");
         includeSites.checked = inclSitesFlag=="true"?true:false;
         includeSites.disabled = false;

         var includeFolders = this._mustById(
               "ps.select.templates.includeFoldersCheckbox");
         includeFolders.checked = inclFoldersFlag=="true"?true:false;
         includeFolders.disabled = false;

   },

   /**
    * Finds DOM element by id.
    * Returns <code>null</null> if the element can't be found.
    * @param {String} id the part of id, unqualified by the tab prefix,
    * to search for. The method fully qualifies it.
    * Not <code>null</code>.
    */
   _getElemById: function (id)
   {
      dojo.lang.assertType(id, String);
      return dojo.byId(this._getQId(id));
   },

   /**
    * Finds DOJO widget by id.
    * Returns <code>null</null> if the widget can't be found.
    * @param {String} id the part of id, unqualified by the tab prefix,
    * to search for. The method fully qualifies it.
    * Not <code>null</code>.
    */
   _getWidgetById: function (id)
   {
      dojo.lang.assertType(id, String);
      return dojo.widget.byId(this._getQId(id));
   },

   /**
    * Helper method to return the fully qualified id for
    * this tab.
    * @param {string} name for the control.
    */
   _getQId: function (s)
   {
      return this.prefix + "." + s;
   },
   
   /**
    * Returns currently selected content type.
    * If no content type is selected by user, returns "-1".
    */
   _getContentType: function ()
   {
      var ctype = this.getContentTypeList().value;
      if (ctype)
      {
         return dojo.string.trim(ctype);
      }
      else
      {
         return "-1";
      }
   },
   
   /**
    * The content type list. Should be used instead of direct access to the
    * content list field, because the getter provides additional assertions.
    */
   getContentTypeList: function ()
   {
      dojo.lang.assert(!this.isSearchTab());
      return this._ctypeList;
   },
   
   /**
    * Returns <code>true</code> if this is a site tab.
    */
   isSiteTab: function ()
   {
      return this.prefix === ps.util.BROWSETAB_SITES_PANEL_PREF;
   },

   /**
    * Returns <code>true</code> if this is a search tab.
    */
   isSearchTab: function ()
   {
      return this.prefix === ps.util.BROWSETAB_SEARCH_PANEL_PREF;
   },
   
   /**
    * Returns <code>true</code> if this is a search tab.
    */
   isFolderTab: function ()
   {
      return this.prefix === ps.util.BROWSETAB_FOLDERS_PANEL_PREF;
   },
   /**
    * Provides complete current path.
    */
   getPath: function ()
   {
      return this.pathHistory.getCurrent();
   },
   
   /**
    * Returns </code>true</code> if the current path points to an item.
    * @param {String} path the path to check.
    * If </code>null</code>, checks the current path.
    */
   isItemPath:  function (path)
   {
      path && dojo.lang.assertType(path, String);
      if (!path)
      {
         path = this.getPath();
      }
      return /\|\d+$/.test(path);
   },
   
   /**
    * Returns a parent folder.
    * @param {String} path the path to get current folder for.
    * If </code>null</code>, returns a folder for processes the current path.
    */
   getParentFolder: function (path)
   {
      path && dojo.lang.assertType(path, String);
      if (!path)
      {
         path = this.getPath();
      }
      dojo.lang.assert(path !== this.ROOT,
            "Tried to get parent folder for a root directory");
      var separator = this.isItemPath(path) ? this.ITEM_SEPARATOR : "/";
      var lastIdx = path.lastIndexOf(separator);
      var path = path.substring(0, lastIdx);
      if (path === "")
      {
         path = this.ROOT;
      }
      return path;
   },
   
   /**
    * Provides folder part of the current path.
    * @param {String} path the path to get folder for.
    * If </code>null</code>, returns a folder for processes the current path.
    */
   getFolder: function (path)
   {
      path && dojo.lang.assertType(path, String);
      if (!path)
      {
         path = this.getPath();
      }
      return this.isItemPath(path) ? this.getParentFolder(path) : path;
   },

   /**
    * Returns a text of a content table cell.
    * @param {String} html the cell html.
    */
   _getCellText: function (html)
   {
      var beforeStr = '<a href="#">';
      var from = html.lastIndexOf(beforeStr) + beforeStr.length;
      var to = html.lastIndexOf("</a>")
      dojo.lang.assert(from > 0 && to > 0 && from <= to,
            "Unexpected html of the string: " + html);
      return html.substring(from, to);
   },

   ROOT: "/",

   /**
    * The item id separator in a path.
    */
   ITEM_SEPARATOR: "|",

   /**
    * Cookie name to store last path successfully accessed by the user.
    */
   LAST_PATH_COOKIE: this.parent.mode + "." + this.prefix + ".lastPath",  

   /**
    * A type used for row data indicating an item.
    */
   ITEM_TYPE: 1,

   /**
    * A type used for row data indicating a folder.
    */
   FOLDER_TYPE: 2,

   /**
    * A type used for row data indicating a site.
    */
   SITE_TYPE: 9,

   /**
    * Constant for the Cancel button.
    */
   CANCEL_LABEL: psxGetLocalMessage("javascript.ps.content.browse@Cancel"),

   /**
    * Constant for the Close button.
    */
   CLOSE_LABEL: psxGetLocalMessage("javascript.ps.content.browse@Close"),

   /**
    * Constant for the Ok button.
    */
   OK_LABEL: psxGetLocalMessage("javascript.ps.content.browse@Ok")
});

