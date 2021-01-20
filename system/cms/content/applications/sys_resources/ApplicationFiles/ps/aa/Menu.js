/******************************************************************************
 *
 * [ ps.aa.Menu.js ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
 
dojo.provide("ps.aa.Menu");

dojo.require("dojo.lang.assert");
dojo.require("dojo.html");
dojo.require("ps.aa");
dojo.require("ps.aa.controller");
dojo.require("ps.io.Actions");

/**
 * This is used to manage the menubar and context menu for the current
 * selected object, which could be the page, a snippet or a slot.
 */
ps.aa.Menu = new function()
{
   /**
    * A list of member data used to remember various menu items (HTMLDivElement)
    */
   this.menubarIcon = null;
   this.showTreeElem = null;
   this.hideTreeElem = null;
   this.showBordersElem = null;
   this.hideBordersElem = null;
   this.showPlaceholdersElem = null;
   this.hidePlaceholdersElem = null;

   // member data for menu bar
   this.addSnippetElem = null;

   this.changeTemplateElem = null;
   this.downElem = null;
   this.upElem = null;

   //member data for 'Content' menu
   this.contentElem = null;
   this.contentNewElem = null;
   this.contentNewItemElem = null;

   //member data for 'Account' menu
   this.accountElem = null;
   this.accountLogoutElem = null;
   this.accountUserInfoElem = null;

   // member data for 'Edit' submenu
   this.editElem = null;
   this.editAllElem = null;
   this.editFieldElem = null;
   this.removeElem = null;
   
   // data for the 'View' submenu
   this.viewElem = null;
   
   // member data for 'Tools' submenu
   this.toolElem = null;
   this.workflow = null;
   
   this.compare = null;
   this.showRelationships = null;
   this.translate = null;
   this.createVersion = null
   this.showUrl = null;
   this.pubNow = null;
   
   this.separator = null;
   
   this.viewContent = null;
   this.viewProperties = null;
   this.viewRevisions = null;
   this.viewAuditTrail = null;

   // preview menu
   this.preview = null;

   // member data for context menu
   
   this.slotCtxMenu = null;
   this.itemCtxMenu = null;

   this.ctxChangeTemplate = null;
   this.ctxUp = null;
   this.ctxDown = null;
   this.ctxRemove = null;
   
   this.ctxNewFromSnippet = null;
   this.ctxInsertFromSnippet = null;
   this.ctxReplaceFromSnippet = null;
   this.ctxOpenFromSnippet = null;
	
   this.ctxItemSeparator1 = null;
   this.ctxItemSeparator2 = null;
   this.ctxEditAll = null;
   this.ctxEditField = null;   
   
   this.ctxAddSnippet = null;
   this.ctxRemoveSnippet = null;
   this.ctxNewSnippet = null;

   /**
    * Used to determine if menu entries based on PSActions are visible to the 
    * user given the context of the item as well as to get the label for an
    * action. The class hides all the server calls
    * and cache management required to provide the functionality.
    * <p>
    * The general use case for visbility is:
    * <pre>
    *    this.actionVisibilityChecker.setCurrentId(objectId);
    *    loop:
    *       this.actionVisibilityChecker.isVisible(actionName);
    * </pre>
    */
   this.actionVisibilityChecker = 
   {
      /**
       * Finds a label for a given name, case-insensitive. If the label can't
       * be retrieved for any reason, the supplied name is returned.
       * 
       * @param actionName Assumed to be the name of a PSAction registered
       * with the workbench and that is being used by this class. 
       * 
       * @return The label set on the action identified by the supplied name,
       * or the supplied name if it can't be found or loaded.
       */
      getLabel: function(actionName)
      {
         if (this.m_labels == null)
         {
            var response = ps.io.Actions.getActionLabels(this.getNames());
            if (!response.isSuccess())
            {
               dojo.debug(
                  "Failed to retrieve labels for some menu actions. Reason: " 
                  + response.getValue());
               return actionName;
            }
            this.m_labels = {};
            var resp = response.getValue()[0];
            for (var name in resp)
               this.m_labels[name.toLowerCase()] = resp[name];
         }
         
         var label = this.m_labels[actionName.toLowerCase()]
         if (label == undefined || label == null)
            return actionName;
         return label;
      },
      
      /**
       * Checks whether the item in the context supplied via the setCurrentId
       * method is visible to the current user.
       * 
       * @return true if the action identified by the supplied name, case-
       * insensitive is visible to the current user given the current
       * context, false otherwise.
       */
      isVisible: function(actionName)
      {
         if (this.m_currentId == null || actionName == undefined || actionName == null)
            return false;
         var visibilityMap = this.m_cache[this.m_currentId.toString()];
         
         if (visibilityMap == null)
         {
            var response = ps.io.Actions.getActionVisibility(this.getNames(), 
               this.m_currentId);
            if (!response.isSuccess())
            {
               dojo.debug(
                  "Failed to retrieve visibility states for some menu actions. Reason: " 
                  + response.getValue());
               return false;
            }
            visibilityMap = response.getValue();
            this.m_cache[this.m_currentId.toString()] = visibilityMap;
         }
         
         return visibilityMap[actionName.toLowerCase()];
      },
      
      /**
       * Must be called if any events occur that might change the visibility
       * of PSAction based menu entries. Causes all cached visibility maps
       * to be discarded.
       * 
       * @return none
       */
      flush: function()
      {
         this.m_cache = {};
      },
      
      /**
       * Must be called  before isVisible will work correctly.
       * 
       * @param objectId Assumed to be a valid id.
       * 
       * @return none
       */
      setCurrentId: function(objectId)
      {
         this.m_currentId = objectId;
      },
      
      /**
       * All actions that are PSAction based must be hard-coded in this method.
       * 
       * @return An array of all the names. Never null. The
       * array is owned by this class and should not be modified in any way.
       */
      getNames: function()
      {
         if (this.m_names == null)
         {
            this.m_names = [
               "Edit_PromotableVersion",
               "Publish_Now",
               "Item_ViewDependents",
               "View_Compare",
               "Translate",
            ];
         }
         return this.m_names;
      },
      
      /**
       * Lazily generated list of all known menu entries based on PSActions.
       * Filled the first time by walking the enumerable objects in the outer
       * class. If the widget has the actionName property, it is 
       * considered to be such an action. The type is Array.
       */
      m_names: null,
      
      /**
       * A map whose property name is the action name (lower-cased) and whose 
       * value is the label. There is an entry for each entry in m_names. Must 
       * be accessed via the getLabel() method.
       */
      m_labels: null,
      
      /**
       * Set by the setCurrentId method. This is the context used to 
       * determine the visibility of an action.
       */
      m_currentId: null,
      
      /**
       * Visibility results are cached by objectId for speed improvement.
       * Never null.
       */
      m_cache: {},
      
      /**
       * The outer object.
       */
      m_parent: this
   }
   
   /**
    * Initialize the properties of the menu. This should be called right after 
    * the dojo is done passing the HTML content.
    */
   this.init = function(ids)
   {
      this._initMenuBar();
   }
   
   /**
    * Non-critical initialization, which can be called later.
    */
   this.initAsynch = function (ids)
   {
      this._initContextMenu(ids);
      var menubar = dojo.widget.byId("ps.aa.Menubar");
      this._addMenubarItems2(menubar);
   }
   
   /**
    * Reset the menu for the specified object id
    * 
    * @param {ps.aa.ObjectId} objId The to be activated object id.
    */
   this.activate = function(objId, parentId)
   {
      this._resetMenubar(objId, parentId);
      this._resetContextMenu(objId, parentId);
   }
   
   /**
    * Reset the menubar for the specified object id.
    * 
    * @param {ps.aa.ObjectId} objId
    */
   this._resetMenubar = function(objId, parentId)
   {
      this._resetMenubarParams = {objId: objId, parentId: parentId};

      if (objId.isSlotNode())
      {
         this._resetSlotBar(objId.isCheckoutByMe());
      }
      else if (objId.isPageNode() || objId.isFieldNode())
      {
         this._resetPageFieldBar(objId);
      }
      else // if (objId.isSnippetNode())
      {
         this._resetSnippetBar(objId, parentId);
      }
      
      this._updateIconMenuItem(objId);
   }
   
   /**
    * Same as running {@link #resetMenubar} with the last parameters it was
    * called with.
    */
   this._resetLastMenubar = function()
   {
      var params = this._resetMenubarParams;
      if (params)
      {
         this._resetMenubar(params.objId, params.parentId);
      }
   }

   /**
    * Updates the image and the tooltip for the icon menu item on the menubar
    * 
    * @param objId the actived object id.
    */
   this._updateIconMenuItem = function(objId)
   {
      // updates the icon image
      this.menubarIcon.setImage(objId.getImagePath(
       ps.aa.controller.IMAGE_ROOT_PATH));

      // updates the title or tooltip             
      var title = "";
      if (objId.isFieldNode())
      {
         title = objId.getFieldName();
      }
      else
      {
         div = dojo.byId(objId.serialize());
         title = div.getAttribute("psAaLabel");
      }
      this.menubarIcon.setTitle(title);      
   }
   
   /**
    * Shows the widget element if it is not null and is visible, otherwise,
    * hides it.
    * 
    * @param widget The menu to show. May be null.
    * @param objId The context under which the menu is shown. This is used to
    * calculate the visibility of certain actions. If the action is not visible,
    * it will be hidden instead. If not provided, the menu will be shown.
    */
   this._maybeShow = function (widget, objId)
   {
      var visible = true;
      if (widget)
      {
         if (objId)
         {
            if (widget.rx_actionName !== undefined)
            {
               this.actionVisibilityChecker.setCurrentId(objId);
               visible = 
                  this.actionVisibilityChecker.isVisible(widget.rx_actionName);
            }
         }
         if (visible)
            dojo.html.show(widget.domNode);
         else
            dojo.html.hide(widget.domNode);
      }
   }
   
   /**
    * Hides the widget element if it is not null.
    */
   this._maybeHide = function (widget)
   {
      if (widget)
      {
         dojo.html.hide(widget.domNode);
      }
   }

   /**
   * Reset menubar for a slot
    * 
    * @param {boolean} isCheckoutByMe 'true' if the parent item is checkout by
    *    the user; 'false' otherwise.
    */
   this._resetSlotBar = function(isCheckoutByMe)
   {
      // hide menu items that do not apply to slot
      this._maybeHide(this.changeTemplateElem);
      this._maybeHide(this.upElem);
      
      this._maybeHide(this.downElem);
      
      dojo.html.hide(this.editElem.domNode);
      this._maybeHide(this.editAllElem);
      this._maybeHide(this.editFieldElem);
      this._maybeHide(this.removeElem);

      // hide Tools menu because it contains no items visible for a slot
      dojo.html.hide(this.toolElem.domNode)
      this._maybeHide(this.workflow);

      // show menu items that are specific for a slot
      this._maybeShow(this.addSnippetElem);
      
      if (this.addSnippetElem)
      {
         this.addSnippetElem.setDisabled(!isCheckoutByMe);
      }
   },
   
   /**
    * Reset the menu bar for a page or field object.
    */
   this._resetPageFieldBar = function(objId)
   {
      // hide menu items that are specific for snippet
      this._maybeHide(this.addSnippetElem);

      this._maybeHide(this.changeTemplateElem);
      this._maybeHide(this.upElem);
      this._maybeHide(this.downElem);
      
      this._maybeHide(this.removeElem);

      // show the menu items that are specific for a page/field node
      dojo.html.show(this.editElem.domNode);
      this._maybeShow(this.editAllElem);

      dojo.html.show(this.toolElem.domNode);
      this._maybeShow(this.viewContent);
      this._maybeShow(this.viewProperties);
      this._maybeShow(this.viewRevisions);
      this._maybeShow(this.viewAuditTrail);
      this._maybeShow(this.translate, objId);
      this._maybeShow(this.createVersion, objId);
      this._maybeShow(this.compare, objId);
      this._maybeShow(this.showRelationships, objId);
      
      this._maybeShow(this.workflow);

      //Show the publish now action to page only.
      if(objId.isPageNode())
      {
         this._maybeShow(this.pubNow, objId);
      }
      else
      {
         this._maybeHide(this.pubNow, objId);
      }

      
      if (objId.isFieldNode())
      {
         this._maybeShow(this.editFieldElem);
      }
      else
      {
         this._maybeHide(this.editFieldElem);
      }

      // enable and disable appropriate menu items
      var disable = !objId.isCheckoutByMe();
      this.editElem.setDisabled(disable);
      if (this.editAllElem)
      {
         this.editAllElem.setDisabled(disable);
      }
      if (objId.isFieldNode() && this.editFieldElem)
      {
         this.editFieldElem.setDisabled(disable);
      }
   },

   this._resetSnippetBar = function(objId, parentId)
   {
      // hide menu items that are not appliable to a snippet
      this._maybeHide(this.addSnippetElem);
      this._maybeHide(this.editFieldElem);

      //Hide publish now action for snippets
      this._maybeHide(this.pubNow, objId);

      // show the menu items that are specific for a snippet
      this._maybeShow(this.changeTemplateElem);
      this._maybeShow(this.upElem);
      this._maybeShow(this.downElem);
      
      dojo.html.show(this.editElem.domNode);
      this._maybeShow(this.editAllElem);
      this._maybeShow(this.removeElem);

      dojo.html.show(this.toolElem.domNode);
      this._maybeShow(this.workflow);

      var disableParent = !parentId.isCheckoutByMe();
      if (this.changeTemplateElem)
      {
         this.changeTemplateElem.setDisabled(disableParent);
      }
      if (this.downElem)
      {
         this.downElem.setDisabled(disableParent);
      }
      if (this.upElem)
      {
         this.upElem.setDisabled(disableParent);
      }
      if (this.removeElem)
      {
         this.removeElem.setDisabled(disableParent);
      }

      if (objId.isCheckoutByMe())
      {
         this.editElem.setDisabled(false);
         if (this.editAllElem)
         {
            this.editAllElem.setDisabled(false);
         }
      }
      else
      {
         if (parentId.isCheckoutByMe())
         {
            this.editElem.setDisabled(false);
            if (this.editAllElem)
            {
               this.editAllElem.setDisabled(true);
            }
         }
         else
         {
            this.editElem.setDisabled(true);
         }
      }
   },

   /**
    * Initialize the member data for menu items of the menubar.
    */
   this._initMenuBar = function()
   {
      // dynamically build the menubar
      var menubar = dojo.widget.byId("ps.aa.Menubar");
      
      this.menubarIcon = dojo.widget.createWidget(ps.aa.Menu.MENUBARICON);
      menubar.addChild(this.menubarIcon);
      this._addContentMenu(menubar);
      this._addEditMenu(menubar);
      this._addViewMenu(menubar);
      this.workflow = dojo.widget.createWidget(ps.aa.Menu.MENUBARITEM, 
           {caption: "Workflow...", 
           onClick: function(){ps.aa.controller.workflowItem();}});
      menubar.addChild(this.workflow);
      
      this._addToolsMenu(menubar);
      this._addPreviewMenu(menubar);
      this._addAccountMenu(menubar);
      this._addHelpMenu(menubar);
      this.toggleShowHidePlaceholders(true);
   }

   /**
    * Helper method to create menu bar items.
    * @param menubar the menu bar to add the menubar items to.
    * Not null.
    */
   this._addMenubarItems2 = function(menubar)
   {
      dojo.lang.assert(menubar, "Menu bar must be specified");
      
      this.addSnippetElem = dojo.widget.createWidget(ps.aa.Menu.MENUBARITEM, 
                      {caption: "Insert Snippet...", 
                      onClick: function(){
                         ps.aa.controller.addSnippet(
                         ps.aa.Menu.INSERT_FROM_SLOT);}});
      dojo.html.hide(this.addSnippetElem.domNode);

      menubar.addChild(this.addSnippetElem, null, "before", this.preview.domNode);


      this.changeTemplateElem = dojo.widget.createWidget(ps.aa.Menu.MENUBARITEM, 
                      {caption: "Template...", 
                      onClick: function(){ps.aa.controller.changeTemplate();}});
      dojo.html.hide(this.changeTemplateElem.domNode);

      this.upElem = dojo.widget.createWidget(ps.aa.Menu.MENUBARITEM, 
                      {caption: "Up", 
                      onClick: function(){ps.aa.controller.moveSnippetUp();}});
      dojo.html.hide(this.upElem.domNode);

      this.downElem = dojo.widget.createWidget(ps.aa.Menu.MENUBARITEM, 
                      {caption: "Down", 
                      onClick: function(){ps.aa.controller.moveSnippetDown();}});
      dojo.html.hide(this.downElem.domNode);

      // add them 3 in a row
      menubar.addChild(this.changeTemplateElem, null, "after", 
         this.toolElem.domNode);
      menubar.addChild(this.upElem, null, "after", 
         this.changeTemplateElem.domNode);
      menubar.addChild(this.downElem, null, "after", this.upElem.domNode);
   }
   
   this._addViewMenu = function(menubar)
   {
      var _this = this;

      /* these must be created before the menu is accessed because they are
         called early during init */
      this.showTreeElem = dojo.widget.createWidget(ps.aa.Menu.MENUITEM, 
                  {caption: "Show Outline", 
                  onClick: function(){ps.aa.controller.showTree();}});
      this.hideTreeElem = dojo.widget.createWidget(ps.aa.Menu.MENUITEM, 
                  {caption: "Hide Outline", 
                  onClick: function(){ps.aa.controller.hideTree();}});
      if(___sys_aamode == 1)
      {
         this.showBordersElem = dojo.widget.createWidget(ps.aa.Menu.MENUITEM, 
                  {caption: "Show Icons/Borders", 
                  onClick: function(){ps.aa.controller.showBorders();}});
      }
      else
      {
         this.hideBordersElem = dojo.widget.createWidget(ps.aa.Menu.MENUITEM, 
                  {caption: "Hide Icons/Borders", 
                  onClick: function(){ps.aa.controller.hideBorders();}});
      }
      this.showPlaceholdersElem = dojo.widget.createWidget(ps.aa.Menu.MENUITEM, 
                  {caption: "Show Place Holders", 
                  onClick: function(){ps.aa.controller.showPlaceholders();}});
      this.hidePlaceholdersElem = dojo.widget.createWidget(ps.aa.Menu.MENUITEM, 
                  {caption: "Hide Place Holders", 
                  onClick: function(){ps.aa.controller.hidePlaceholders();}});


      function createSubmenu()
      {
         var popmenu = dojo.widget.createWidget(ps.aa.Menu.POPUPMENU,
               {id: "ps.aa.ViewSubMenu"});

         popmenu.addChild(_this.showTreeElem);

         popmenu.addChild(_this.hideTreeElem);
          if(___sys_aamode == 1)
          {
             popmenu.addChild(_this.showBordersElem);
          }
          else
          {
             popmenu.addChild(_this.hideBordersElem);
          }
         popmenu.addChild(_this.showPlaceholdersElem);

         popmenu.addChild(_this.hidePlaceholdersElem);

         
         _this._resetLastMenubar();
      }

      this.viewElem = dojo.widget.createWidget(ps.aa.Menu.MENUBARITEMDROPDOWN, 
                      {caption: "View", submenuId: "ps.aa.ViewSubMenu"});
      this.viewElem.createSubmenu = createSubmenu;
      menubar.addChild(this.viewElem);      
   }
   
   /**
    * Helper method to create the tools menu
    */
   this._addToolsMenu = function(menubar)
   {
      var _this = this;

      function createSubmenu()
      {
         var popmenu = dojo.widget.createWidget(ps.aa.Menu.POPUPMENU,
               {id: "ps.aa.ToolSubMenu"});

         var actionName = "View_Compare";
         _this.compare = dojo.widget.createWidget(ps.aa.Menu.MENUITEM, 
               {caption: _this.actionVisibilityChecker.getLabel(actionName), 
               onClick: function(){ps.aa.controller.compareItemRevisions();}});
         _this.compare.rx_actionName = actionName;
         popmenu.addChild(_this.compare);

         actionName = "Item_ViewDependents";
         _this.showRelationships = dojo.widget.createWidget(ps.aa.Menu.MENUITEM, 
               {caption: _this.actionVisibilityChecker.getLabel(actionName), 
               onClick: function(){ps.aa.controller.showItemRelationships();}});
         _this.showRelationships.rx_actionName = actionName;
         popmenu.addChild(_this.showRelationships);

         var separator = dojo.widget.createWidget(ps.aa.Menu.MENUSEPARATOR)
         popmenu.addChild(separator);


         _this.viewContent = dojo.widget.createWidget(ps.aa.Menu.MENUITEM, 
               {caption: "View Content Item", 
               onClick: function(){ps.aa.controller.viewContent();}});
         popmenu.addChild(_this.viewContent);

         _this.viewProperties = dojo.widget.createWidget(ps.aa.Menu.MENUITEM, 
               {caption: "Properties", 
               onClick: function(){ps.aa.controller.viewProperties();}});
         popmenu.addChild(_this.viewProperties);

         _this.viewRevisions = dojo.widget.createWidget(ps.aa.Menu.MENUITEM, 
               {caption: "Revisions", 
               onClick: function(){ps.aa.controller.viewRevisions();}});
         popmenu.addChild(_this.viewRevisions);

         _this.viewAuditTrail = dojo.widget.createWidget(ps.aa.Menu.MENUITEM, 
               {caption: "Audit Trail", 
               onClick: function(){ps.aa.controller.viewAuditTrail();}});
         popmenu.addChild(_this.viewAuditTrail);

         _this.separator = dojo.widget.createWidget(ps.aa.Menu.MENUSEPARATOR)
         popmenu.addChild(_this.separator);

         if (ps.io.Actions.getLocaleCount() > 1)
         {
            actionName = "Translate";
	         _this.translate = dojo.widget.createWidget(ps.aa.Menu.MENUITEM, 
	               {caption: _this.actionVisibilityChecker.getLabel(actionName), 
	               onClick: function(){ps.aa.controller.createTranslation();}});
	         _this.translate.rx_actionName = actionName;
	         popmenu.addChild(_this.translate);
         }

         actionName = "Edit_PromotableVersion";
         _this.createVersion = dojo.widget.createWidget(ps.aa.Menu.MENUITEM, 
               {caption: _this.actionVisibilityChecker.getLabel(actionName), 
               onClick: function(){ps.aa.controller.createVersion();}});
         _this.createVersion.rx_actionName = actionName;
         popmenu.addChild(_this.createVersion);

         _this.showUrl = dojo.widget.createWidget(ps.aa.Menu.MENUITEM, 
               {caption: "Link to Page", 
               onClick: function(){ps.aa.controller.showPageUrl();}});
         popmenu.addChild(_this.showUrl);

         actionName = "Publish_Now";
         _this.pubNow = dojo.widget.createWidget(ps.aa.Menu.MENUITEM, 
               {caption: _this.actionVisibilityChecker.getLabel(actionName), 
               onClick: function(){ps.aa.controller.publishPage();}});
         _this.pubNow.rx_actionName = actionName;
         popmenu.addChild(_this.pubNow);

         //Hide all the dynamic menus initially and they will be shown as 
         //per the visibility.
         _this._maybeHide(_this.translate);
         _this._maybeHide(_this.pubNow);
         _this._maybeHide(_this.createVersion);
         _this._maybeHide(_this.compare);
         _this._maybeHide(_this.showRelationships);

         _this._resetLastMenubar();
      }

      this.toolElem = dojo.widget.createWidget(ps.aa.Menu.MENUBARITEMDROPDOWN, 
                      {caption: "Tools", submenuId: "ps.aa.ToolSubMenu"});
      this.toolElem.createSubmenu = createSubmenu;
      menubar.addChild(this.toolElem);
   }

   /**
    * Helper method to create the content menu
    */   
   this._addContentMenu = function(menubar)
   {
      var _this = this;

      function createSubmenu()
      {
         var popmenu = dojo.widget.createWidget(ps.aa.Menu.POPUPMENU,
               {id: "ps.aa.ContentSubMenu"});

         _this.contentNewElem = dojo.widget.createWidget(ps.aa.Menu.MENUITEM,
               {caption: "Copy", 
               onClick: function(){ps.aa.controller.createItem(
                   ps.aa.Menu.COPY_FROM_CONTENT);}});
         popmenu.addChild(_this.contentNewElem);

         _this.contentNewItemElem = dojo.widget.createWidget(
               ps.aa.Menu.MENUITEM, 
               {caption: "New", 
               onClick: function(){ps.aa.controller.createItem(
                  ps.aa.Menu.NEW_FROM_CONTENT);}});
         popmenu.addChild(_this.contentNewItemElem);

         _this._resetLastMenubar();
      }

      this.contentElem = dojo.widget.createWidget(ps.aa.Menu.MENUBARITEMDROPDOWN, 
            {caption: "Content", submenuId: "ps.aa.ContentSubMenu"});
      this.contentElem.createSubmenu = createSubmenu;
      menubar.addChild(this.contentElem);
   }

  
   /**
    * Helper method to create account menu.
    */
   this._addAccountMenu = function(menubar)
   {
      var _this = this;

      function createSubmenu()
      {
         var popmenu = dojo.widget.createWidget(ps.aa.Menu.POPUPMENU,
               {id: "ps.aa.AccountSubMenu"});

         _this.accountUserInfoElem = dojo.widget.createWidget(
               ps.aa.Menu.MENUITEM,
               {caption: "User Info", 
               onClick: function(){ps.UserInfo.showInfo();}});
         popmenu.addChild(_this.accountUserInfoElem);

         _this.accountLogoutElem = dojo.widget.createWidget(ps.aa.Menu.MENUITEM, 
               {caption: "Logout", 
               onClick: function(){ps.aa.controller.logout();}});
         popmenu.addChild(_this.accountLogoutElem);
      }

      this.accountElem = dojo.widget.createWidget(ps.aa.Menu.MENUBARITEMDROPDOWN, 
            {caption: "Account", submenuId: "ps.aa.AccountSubMenu"});
      this.accountElem.createSubmenu = createSubmenu;
      menubar.addChild(this.accountElem);
   }
   
   /**
    * Helper method to create the edit menu
    */   
   this._addEditMenu = function(menubar)
   {
      var _this = this;

      function createSubmenu()
      {
         var popmenu = dojo.widget.createWidget(ps.aa.Menu.POPUPMENU,
               {id: "ps.aa.EditSubMenu"});

         _this.editAllElem = dojo.widget.createWidget(ps.aa.Menu.MENUITEM,
               {caption: "Content Item", 
               onClick: function(){ps.aa.controller.editAll();}});
         popmenu.addChild(_this.editAllElem);

         _this.editFieldElem = dojo.widget.createWidget(ps.aa.Menu.MENUITEM, 
               {caption: "Field", 
               onClick: function(){ps.aa.controller.editField();}});
         dojo.html.hide(_this.editFieldElem.domNode);
         popmenu.addChild(_this.editFieldElem);

         _this.removeElem = dojo.widget.createWidget(ps.aa.Menu.MENUITEM, 
               {caption: "Remove", 
               onClick: function(){ps.aa.controller.removeSnippet();}});
         dojo.html.hide(_this.removeElem.domNode);
         popmenu.addChild(_this.removeElem);

         _this._resetLastMenubar();
      }

      this.editElem = dojo.widget.createWidget(ps.aa.Menu.MENUBARITEMDROPDOWN, 
            {caption: "Edit", submenuId: "ps.aa.EditSubMenu"});
      this.editElem.createSubmenu = createSubmenu;
      menubar.addChild(this.editElem);
   }
 
   /**
    * Helper method to create the help menu
    */   
   this._addHelpMenu = function(menubar)
   {
      var _this = this;

      function createSubmenu()
      {
         var popmenu = dojo.widget.createWidget(ps.aa.Menu.POPUPMENU,
               {id: "ps.aa.HelpSubMenu"});
         popmenu.addChild(dojo.widget.createWidget(ps.aa.Menu.MENUITEM, 
               {caption: "Active Assembly Help", 
               onClick: function()
               {
                  ps.aa.controller.openHelpWindow(ps.aa.Menu.AAHELP);
               }}));
         popmenu.addChild(
               dojo.widget.createWidget(ps.aa.Menu.MENUITEM, 
               {caption: "Active Assembly Tutorial", 
               onClick: function()
               {
                  ps.aa.controller.openHelpWindow(ps.aa.Menu.AATUTORIAL);
               }}));
         popmenu.addChild(dojo.widget.createWidget(ps.aa.Menu.MENUITEM, 
               {caption: "About Active Assembly", 
               onClick: function()
               {
                  ps.aa.controller.openHelpWindow(ps.aa.Menu.AAABOUT);
               }}));
         _this._resetLastMenubar();
      }

      var helpMenu = dojo.widget.createWidget(ps.aa.Menu.MENUBARITEMDROPDOWN, 
                      {caption: "Help", submenuId: "ps.aa.HelpSubMenu"});
      helpMenu.createSubmenu = createSubmenu;
      menubar.addChild(helpMenu);
   }
   
   /**
    * Helper method to create the preview menu
    */
   this._addPreviewMenu = function(menubar)
   {
      var _this = this;

      function createSubmenu()
      {
         var popmenu = dojo.widget.createWidget(ps.aa.Menu.POPUPMENU, 
               {id: "ps.aa.PreviewSubMenu"});
         popmenu.addChild(dojo.widget.createWidget(ps.aa.Menu.MENUITEM, 
               {caption: "Current Revisions", 
               onClick: function()
               {
                  ps.aa.controller.previewWithCurrentRevisions();
               }}));
         popmenu.addChild(dojo.widget.createWidget(ps.aa.Menu.MENUITEM, 
               {caption: "With Edits", 
               onClick: function()
               {
                  ps.aa.controller.previewWithEditRevisions();
               }}));
         _this._resetLastMenubar();
      }

      this.preview = dojo.widget.createWidget(ps.aa.Menu.MENUBARITEMDROPDOWN, 
		      {caption: "Preview", submenuId: "ps.aa.PreviewSubMenu"});
		this.preview.createSubmenu = createSubmenu;
      menubar.addChild(this.preview);
   }
   /**
    * Bind the context menus to its related href <a...> element.
    * 
    * @param {dojo.collections.ArrayList} ids The list of {ps.aa.ObjectId} ids.
    */
   this._initContextMenu = function(ids)
   {
      var _this = this;
      
      // create Slot Context Menu
      this.slotCtxMenu = dojo.widget.createWidget(ps.aa.Menu.POPUPMENU, 
		   {id: "ps.aa.SlotCtxMenu"});
		var slotMenu = this.slotCtxMenu;
      document.body.appendChild(slotMenu.domNode);
      slotMenu.createMenuItems = function()
      {
         _this.ctxNewSnippet = dojo.widget.createWidget(ps.aa.Menu.MENUITEM, 
            {caption: "New Snippet...", 
            onClick: function(){ps.aa.controller.createItem(
                ps.aa.Menu.NEW_FROM_SLOT);}});
            slotMenu.addChild(_this.ctxNewSnippet);
         _this.ctxAddSnippet = dojo.widget.createWidget(ps.aa.Menu.MENUITEM, 
            {caption: "Insert Snippet...", 
            onClick: function(){
               ps.aa.controller.addSnippet(ps.aa.Menu.INSERT_FROM_SLOT);}});
            slotMenu.addChild(_this.ctxAddSnippet);
         _this.ctxRemoveSnippet = dojo.widget.createWidget(ps.aa.Menu.MENUITEM, 
            {caption: "Remove Snippets...", 
            onClick: function(){ps.aa.controller.openRemoveSnippetsDlg();}});
            slotMenu.addChild(_this.ctxRemoveSnippet);
      };

      // create Item Context Menu
      this.itemCtxMenu = dojo.widget.createWidget(ps.aa.Menu.POPUPMENU, 
		   {id: "ps.aa.ItemCtxMenu"});
		var itemMenu = this.itemCtxMenu;
      document.body.appendChild(itemMenu.domNode);
      itemMenu.createMenuItems = function()
      {
         _this.ctxChangeTemplate = dojo.widget.createWidget(ps.aa.Menu.MENUITEM, 
               {caption: "Template...", 
               onClick: function(){ps.aa.controller.changeTemplate();}});
         itemMenu.addChild(_this.ctxChangeTemplate);

         _this.ctxUp = dojo.widget.createWidget(ps.aa.Menu.MENUITEM, 
               {caption: "Up", 
               onClick: function(){ps.aa.controller.moveSnippetUp();}});
         itemMenu.addChild(_this.ctxUp);

         _this.ctxDown = dojo.widget.createWidget(ps.aa.Menu.MENUITEM, 
               {caption: "Down", 
               onClick: function(){ps.aa.controller.moveSnippetDown();}});
         itemMenu.addChild(_this.ctxDown);

         _this.ctxRemove = dojo.widget.createWidget(ps.aa.Menu.MENUITEM, 
               {caption: "Remove", 
               onClick: function(){ps.aa.controller.removeSnippet();}});
         itemMenu.addChild(_this.ctxRemove);

         _this.ctxItemSeparator1 = dojo.widget.createWidget(
               ps.aa.Menu.MENUSEPARATOR);
         itemMenu.addChild(_this.ctxItemSeparator1);		

         _this.ctxNewFromSnippet = dojo.widget.createWidget(ps.aa.Menu.MENUITEM, 
               {caption: "New...", 
               onClick: function(){
                  ps.aa.controller.createItem(ps.aa.Menu.NEW_FROM_SNIPPET);}});
         itemMenu.addChild(_this.ctxNewFromSnippet);

         _this.ctxInsertFromSnippet = dojo.widget.createWidget(
               ps.aa.Menu.MENUITEM, 
               {caption: "Insert...", 
               onClick: function(){
                  ps.aa.controller.addSnippet(ps.aa.Menu.INSERT_FROM_SNIPPET);}});
         itemMenu.addChild(_this.ctxInsertFromSnippet);

         _this.ctxReplaceFromSnippet = dojo.widget.createWidget(
            ps.aa.Menu.MENUITEM, {caption: "Replace...", 
            onClick: function(){
               ps.aa.controller.createItem(ps.aa.Menu.REPLACE_FROM_SNIPPET);}});
         itemMenu.addChild(_this.ctxReplaceFromSnippet);

         _this.ctxItemSeparator2 = dojo.widget.createWidget(
               ps.aa.Menu.MENUSEPARATOR);
         itemMenu.addChild(_this.ctxItemSeparator2);		

         _this.ctxOpenFromSnippet = dojo.widget.createWidget(ps.aa.Menu.MENUITEM, 
               {caption: "Open", 
               onClick: function(){
                  ps.aa.controller.openSnippet();}});
         itemMenu.addChild(_this.ctxOpenFromSnippet);

         _this.ctxWorkflow = dojo.widget.createWidget(ps.aa.Menu.MENUITEM, 
               {caption: "Workflow...", 
               onClick: function(){ps.aa.controller.workflowItem();}})
         itemMenu.addChild(_this.ctxWorkflow);
          
         _this.ctxEditField = dojo.widget.createWidget(ps.aa.Menu.MENUITEM, 
               {caption: "Edit Field", 
               onClick: function(){ps.aa.controller.editField();}});
         itemMenu.addChild(_this.ctxEditField); 
         
         _this.ctxEditAll = dojo.widget.createWidget(ps.aa.Menu.MENUITEM, 
               {caption: "Edit Content Item", 
               onClick: function(){ps.aa.controller.editAll();}});
         itemMenu.addChild(_this.ctxEditAll);

         itemMenu.addChild(dojo.widget.createWidget(ps.aa.Menu.MENUITEM, 
               {caption: "View Content Item", 
               onClick: function(){ps.aa.controller.viewContent();}}));

         itemMenu.addChild(dojo.widget.createWidget(ps.aa.Menu.MENUITEM, 
               {caption: "Properties", 
               onClick: function(){ps.aa.controller.viewProperties();}}));

         itemMenu.addChild(dojo.widget.createWidget(ps.aa.Menu.MENUITEM, 
               {caption: "Revisions", 
               onClick: function(){ps.aa.controller.viewRevisions();}}));

         itemMenu.addChild(dojo.widget.createWidget(ps.aa.Menu.MENUITEM, 
               {caption: "Audit Trail", 
               onClick: function(){ps.aa.controller.viewAuditTrail();}}));
         _this._resetLastContextMenu();
      }

        if(___sys_aamode != 1)
           this.bindContextMenu(ids);
   },

   /**
    * Binds the specified image elements to ids related context menu.
    * 
    * @param {dojo.collections.ArrayList} ids The list of {ps.aa.ObjectId} ids
    *    of the specified image elements.
    */
   this.bindContextMenu = function(ids, special)
   {
      var slotNodes = new Array();
      var itemNodes = new Array();
      
      var slotCount = 0;
      var itemCount = 0;
      var objId = null;
      var prefix = ___sys_aamode == 1 && !special ? "" : "img.";
      for(var i=0;i<ids.count;i++)
      {
         var objId = ids.item(i);
         if (objId.isSlotNode())
            slotNodes[slotCount++] = prefix + objId.serialize();
         else
            itemNodes[itemCount++] = prefix + objId.serialize();
      }
      this.slotCtxMenu.bindTargetNodes(slotNodes);
      this.itemCtxMenu.bindTargetNodes(itemNodes);      
   }

   /**
    * Unbinds the specified image elements from ids related context menu.
    * 
    * @param {dojo.collections.ArrayList} ids The list of {ps.aa.ObjectId} ids
    *    of the specified image elements.
    */
   this.unBindContextMenu = function(ids, special)
   {
      var slotNodes = new Array();
      var itemNodes = new Array();
      
      var slotCount = 0;
      var itemCount = 0;
      var objId = null;
      var prefix = ___sys_aamode == 1 && !special ? "" : "img.";
      for(var i=0;i<ids.count;i++)
      {
         var objId = ids.item(i);
         if (objId.isSlotNode())
            slotNodes[slotCount++] = prefix + objId.serialize();
         else
            itemNodes[itemCount++] = prefix + objId.serialize();
      }
      this.slotCtxMenu.unBindTargetNodes(slotNodes);
      this.itemCtxMenu.unBindTargetNodes(itemNodes);      
   }
   
   
   /**
    * Bind the context menus for its related div element.
    */
   this._resetContextMenu = function(objId, parentId)
   {
      this._resetContextMenuParams = {objId: objId, parentId: parentId};

      if (objId.isSlotNode())
      {
         this._resetCtxSlotMenu(objId);
      }
      else if (objId.isSnippetNode())
      {
         this._resetCtxSnippetMenu(objId, parentId);         
      }
      else
      {
         this._resetCtxPageFieldMenu(objId);
      }
   }

   /**
    * Same as running {@link #resetContextMenu} with the last parameters it was
    * called with.
    */
   this._resetLastContextMenu = function()
   {
      var params = this._resetContextMenuParams;
      if (params)
      {
         this._resetContextMenu(params.objId, params.parentId);
      }
   }

   /**
    * Reset the context menu for a page slot object.
    */
   this._resetCtxSlotMenu = function(objId)
   {
      if (this.ctxAddSnippet)
      {
         this.ctxAddSnippet.setDisabled(!objId.isCheckoutByMe());
      }
      if (this.ctxNewSnippet)
      {
         this.ctxNewSnippet.setDisabled(!objId.isCheckoutByMe());
      }
      if (this.ctxRemoveSnippet)
      {
         this.ctxRemoveSnippet.setDisabled(!objId.isCheckoutByMe());
      }
   }

   /**
    * Reset the context menu for a snippet object.
    */
   this._resetCtxSnippetMenu = function(objId, parentId)
   {
      if (!this.ctxChangeTemplate)
      {
         // menu items are not created yet
         return;
      }
      // hide and show snippet specific menu items
      dojo.html.hide(this.ctxEditField.domNode);
      
      dojo.html.show(this.ctxChangeTemplate.domNode);
      dojo.html.show(this.ctxUp.domNode);
      dojo.html.show(this.ctxDown.domNode);
      dojo.html.show(this.ctxRemove.domNode);
      
      dojo.html.show(this.ctxItemSeparator1.domNode);
      dojo.html.show(this.ctxNewFromSnippet.domNode);
      dojo.html.show(this.ctxInsertFromSnippet.domNode);
      dojo.html.show(this.ctxReplaceFromSnippet.domNode);
      dojo.html.show(this.ctxOpenFromSnippet.domNode);
      dojo.html.show(this.ctxItemSeparator2.domNode);
      
      dojo.html.show(this.ctxWorkflow.domNode);
      dojo.html.show(this.ctxEditAll.domNode);

      // enable and disable menu items according to 'objId' and 'parentId'
      var disable = !parentId.isCheckoutByMe();
      this.ctxChangeTemplate.setDisabled(disable);
      this.ctxUp.setDisabled(disable);
      this.ctxDown.setDisabled(disable);
      this.ctxRemove.setDisabled(disable);
      this.ctxNewFromSnippet.setDisabled(disable);
      this.ctxInsertFromSnippet.setDisabled(disable);
      this.ctxReplaceFromSnippet.setDisabled(disable);
      this.ctxOpenFromSnippet.setDisabled(disable);
      
      this.ctxEditAll.setDisabled(!objId.isCheckoutByMe());
   }

   /**
    * Reset the context menu for a snippet object.
    */
   this._resetCtxPageFieldMenu = function(objId)
   {
      if (!this.ctxChangeTemplate)
      {
         // menu items are not created yet
         return;
      }
      // hide menu items which are not appliable to snippet
      dojo.html.hide(this.ctxChangeTemplate.domNode);
      dojo.html.hide(this.ctxUp.domNode);
      dojo.html.hide(this.ctxDown.domNode);
      dojo.html.hide(this.ctxRemove.domNode);
      dojo.html.hide(this.ctxItemSeparator1.domNode);
      dojo.html.hide(this.ctxNewFromSnippet.domNode);
      dojo.html.hide(this.ctxInsertFromSnippet.domNode);
      dojo.html.hide(this.ctxReplaceFromSnippet.domNode);
      dojo.html.hide(this.ctxOpenFromSnippet.domNode);
      dojo.html.hide(this.ctxItemSeparator2.domNode);
      
      // show snippet specific menu items
      dojo.html.show(this.ctxEditAll.domNode);
      if (objId.isPageNode())
      {
        dojo.html.hide(this.ctxEditField.domNode);
      }
      else
      {
        dojo.html.show(this.ctxEditField.domNode);
      }
      dojo.html.show(this.ctxWorkflow.domNode);

      // enable and disable menu items according to 'objId' and 'parentId'
      var disable = !objId.isCheckoutByMe();
      this.ctxEditAll.setDisabled(disable);
      this.ctxEditField.setDisabled(disable);
   }

   this.toggleShowHideTree = function(isShowTree)
   {
      if (isShowTree)
      {
         dojo.html.show(this.hideTreeElem.domNode);
         dojo.html.hide(this.showTreeElem.domNode);
      }
      else
      {
         dojo.html.hide(this.hideTreeElem.domNode);
         dojo.html.show(this.showTreeElem.domNode);
      }
   }

   this.toggleShowHidePlaceholders = function(isShow)
   {
      if (isShow)
      {
         dojo.html.show(this.hidePlaceholdersElem.domNode);
         dojo.html.hide(this.showPlaceholdersElem.domNode);
      }
      else
      {
         dojo.html.hide(this.hidePlaceholdersElem.domNode);
         dojo.html.show(this.showPlaceholdersElem.domNode);
      }
   }
   
   /**
    * Returns an array of menu items managed by this controller.
    */
   this.getMenuItems = function ()
   {
      var items = [];
      for (name in this)
      {
         var item = this[name];
         if (item instanceof dojo.widget.MenuItem2)
         {
            items.push(item);
         }
      }
      dojo.lang.assert(items.length > 0);
      return items;
   }
};

/**
 * Constants for the paths of all icon images
 */
ps.aa.Menu.PAGE_IMG_PATH = "../sys_resources/images/page.gif";
ps.aa.Menu.SNIPPET_IMG_PATH = "../sys_resources/images/item.gif";
ps.aa.Menu.SLOT_IMG_PATH = "../sys_resources/images/relatedcontent/slot.gif";
ps.aa.Menu.FIELD_IMG_PATH = "../sys_resources/images/pen.gif";

/**
 * Constants for menu components
 */
ps.aa.Menu.MENUBAR = "ps:MenuBar2";
ps.aa.Menu.MENUBARICON = "ps:MenuBarIcon";
ps.aa.Menu.MENUBARITEM = "MenuBarItem2";
ps.aa.Menu.MENUBARITEMDROPDOWN = "ps:MenuBarItemDropDown";
ps.aa.Menu.MENUITEM = "MenuItem2";
ps.aa.Menu.MENUSEPARATOR = "MenuSeparator2";
ps.aa.Menu.POPUPMENU = "ps:PopupMenu";

/**
 * Constant for Active Assembly Help action types
 */
ps.aa.Menu.AAHELP = "AAHelp";
ps.aa.Menu.AATUTORIAL = "AATutorial";
ps.aa.Menu.AAABOUT = "AAAbout";
 
/**
 * Constants for create item menus
 */
ps.aa.Menu.NEW_FROM_SLOT = 0;
ps.aa.Menu.NEW_FROM_SNIPPET = 1;
ps.aa.Menu.REPLACE_FROM_SNIPPET = 2;
ps.aa.Menu.COPY_FROM_CONTENT = 3;
ps.aa.Menu.NEW_FROM_CONTENT = 4;

/**
 * Constants for insert snippets
 */
ps.aa.Menu.INSERT_FROM_SLOT = 0;
ps.aa.Menu.INSERT_FROM_SNIPPET = 1;

