/******************************************************************************
 *
 * [ ps.aa.controller.js ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
 
dojo.provide("ps.aa.controller");

dojo.require("dojo.json");
dojo.require("dojo.lang.assert");
dojo.require("dojo.lang.type");
dojo.require("dojo.html");
dojo.require("dojo.widget.FloatingPane")

dojo.require("ps.aa");
dojo.require("ps.aa.dnd");
dojo.require("ps.aa.Menu");
dojo.require("ps.UserInfo");
dojo.require("ps.aa.Page");
dojo.require("ps.aa.Tree");
dojo.require("ps.aa.SnippetMove");
dojo.require("ps.content.Browse");
dojo.require("ps.util");
dojo.require("ps.widget.Autoscroller");
dojo.require("ps.widget.PSButton");
dojo.require("ps.workflow.WorkflowActions");

/**
 * The Active Assembly controler is responsible to manage all activities, which 
 * include, but not limited the following:
 * (a) communicates to the server for all server operations. 
 * (b) informs the page controller (ps.aa.Page) upon successful server 
 *     operation(s), so that the page controller will be able to update its 
 *     (and its dependent) views accordingly. 
 * (c) update menu system (menubar and context menu) for the activated object.
 * (d) keep references to reusable objects such as a content browse dialog etc.
 */
ps.aa.controller = new function()
{
   /**
    * Whether the tree pane is showing.
    */
   this.isShowTree = null;
   
   /**
    * The id of the current page.
    */
   this.pageId = null;
   /**
    * The id of the activated object.
    */
   this.activeId = null;
   /**
    * Reference to the content browser dialog box. Created and initialized in 
    * {@link #init} method. See ps.content.Browse class for actual usage.
    */
   this.contentBrowser = null;
   
   /**
    * Reference to the snippet picker dialog box. Created and initialized in 
    * {@link #init} method. See ps.content.SnippetPicker class for actual usage.
    */
   this.snippetPickerDlg = null;

   /**
    * Reference to the Create New Item dialog box. Created and initialized in 
    * {@link #init} method. See ps.content.CreateItem class for actual usage.
    */
   this.createItemDlg = null;

   /**
    * The object id that represents the item being edited in full content editor
    */
   this.editObjectId = null;

   /**
    * The window object of content editor.
    */
   this.psCeWindow = null;
     
   /**
    * This tree model contains all managed nodes in the page panel.
    */
   this.treeModel = null;
   
   /**
    * The AA tree widget
    */
    this.treeWidget = null;
   
   /**
    * The workflow actions object
    */
    this.wfActions = null;
    
    /**
     * The Field editor object
     */
    this.fieldEdit = null;
    
    /**
     * The width of the tree pane just before being
     * hidden. 
     */
    this.lastShowingTreePaneWidth = 0;
    
    /**
     * Minimal tree width.
     */
    this.MIN_TREE_WIDTH = 20;
    
   /**
    * Menubar style initialized in _handleMenuBarBackGround method.
    */
    this.menuBarStyle = null;

    /**
     * Flag indicating that placeholders are showing
     */
     this.isShowPlaceholders = true;
   
   /**
    * Initialize the controller and other AA objects.
    * Should be called right after the dojo is done passing the HTML content.
    */
   this.init = function()
   {
      // Be careful when adding any new code to initialization, especially
      // dojo widget creation.
      // New code can visibly affect performance old systems (especially IE6).
      var _this = this;

      // init individual objects
      ps.aa.Page.init();

      // init the tree model
      this.treeModel = new ps.aa.Tree();
      this.treeModel.init();
      this.pageId = this.treeModel.getRootNode().objId;
      dojo.event.connect(this.treeModel, "onBeforeDomChange",
            this, "_onBeforeDomChange");
      dojo.event.connect(this.treeModel, "onDomChanged",
            this, "_onDomChanged");
      
      var ids = new dojo.collections.ArrayList();
      ids.add(this.pageId);
      if(___sys_aamode != 1)
         ids = this.treeModel.getIdsFromNodeId(this.pageId);
      
      this._resetObjectIcons(ids);
      
      this.isShowTree = this._loadTreePaneShowing();
      ps.aa.Menu.init(ids);
      this._maybeShowTree();

      this.bottomPane = dojo.widget.byId("ps.aa.BottomPane");
      dojo.event.connect(this.bottomPane, "endSizing", this, "_endTreeSizing");
      this.treeWidget = dojo.widget.manager.getWidgetById("pageTree");
      this.treeWidget.loadFromModel(this.treeModel);
      this.activate(ps.aa.Page.getElement(this.pageId));
      this.updateBodyStyles();
      // do these actions asynchroniously, so they won't delay page loading
      var timeout = dojo.render.html.ie55 || dojo.render.html.ie60 ? 500 : 600;
      dojo.lang.setTimeout(function() {_this.asynchInit(ids);}, timeout);
   },

   /**
    * Helper method to move and clear the body styles to the page content div 
    * element. Moves the inline styles, builtin styles and css class name and 
    * id attribute.
    */
   this.updateBodyStyles = function()
   {
      var bodyElem = document.getElementsByTagName("body")[0];
      var pageDivElem = dojo.byId("ps.aa.PageContent");
      //Get and clear the body element inline styles
      var ilStyles = ps.util.trim(bodyElem.style.cssText);
      bodyElem.style.cssText = "";
      //Get and clear the body element styles from stylesheets
      var elemStyles = ps.util.getElementStyleSheetCss("body",true);
      //Build the new style
      var newStyle = "";
      if(ilStyles.length > 0 && elemStyles.length > 0)
         newStyle = ilStyles + ";" + elemStyles;
      else if(ilStyles.length > 0)
         newStyle = ilStyles;
      else if(elemStyles.length > 0)
         newStyle = elemStyles;
      
      //Move the new style to div element
      if(newStyle.length > 0)
         pageDivElem.style.cssText = newStyle;
      
      //Move the class name
      pageDivElem.className = bodyElem.className;
      //Move the id attribute
      var idAttrib = dojo.html.getAttribute(bodyElem, "id");
      bodyElem.setAttribute("id",idAttrib);
      //Set the body class to the predefined class.
      bodyElem.className = "PsAabody";
   }
   
   /**
    * Run this method with a delay for initialization, which can be done much
    * later, so it won't delay page loading.
    * Note to implementors - try to put activity in here in order of importance.
    * Logic, which is more critical to be executed first to the beginning of the
    * method.
    */
   this.asynchInit = function(ids)
   {
      var _this = this;

      ps.aa.Menu.initAsynch(ids);
      this.treeWidget.loadFromModelAsynch(this.treeModel);

      this.wfActions = new ps.workflow.WorkflowActions();
      this.wfActions.init();
      this.fieldEdit = new ps.aa.Field();
      this.fieldEdit.init();

      ps.aa.dnd.init();
      this.treeWidget.dndInit();

      this.contentBrowser = new ps.content.Browse(ps.util.BROWSE_MODE_ACTIVE_ASSEMBLY);
      this.contentBrowser.init(__rxroot);
      
      this.snippetPickerDlg = new ps.content.SnippetPicker();
      this.snippetPickerDlg.init(__rxroot + "/ui/content/snippetpicker.jsp", 1);

      this.createItemDlg = new ps.content.CreateItem();
      this.createItemDlg.init(__rxroot + "/ui/content/CreateItem.jsp");

      this.templatesDlg = new ps.content.SelectTemplates();      
      this.templatesDlg.init(__rxroot + "/ui/content/selecttemplate.jsp");

      //Handle the menubar back ground style changes.
      this._handleMenuBarBackGround();

      this.adjustLayout();
      window.onresize = this.adjustLayout;

      // init autoscroller
      var nodes = [];
      nodes.push(dojo.byId("ps.aa.ContentPane"));
      nodes.push(dojo.widget.byId("pageTree").domNode);
      this.autoscroller.init(nodes);
      
      // these take long time, so interleave them with main flow
      // to make IE6 more responsive
      dojo.lang.delayThese([
            function() {_this.wfActions.maybeCreateWorkflowDialog();},
            function() {_this.contentBrowser.maybeCreateBrowseDialog();}], 100);
   }

   /**
    * Adjust the current window size, so that there is no scroll bar on the 
    * most out size of the window. This will keep the menubar at the top of
    * the main window.
    */
   this.adjustLayout = function()
   {
      var mainPane = dojo.widget.byId("ps.aa.mainSplitPane");
      var viewport = dojo.html.getViewport();
      mainPane.resizeTo(viewport.width, viewport.height);
   }

   /**
    * Activates the specified object.
    * @param {HTMLElement} or {ps.aa.ObjectId} htmlElem the dom node to activate
    *    or an objectId of the node to activate.
    * Not <code>null</code>.
    */
   this.activate = function (htmlElem)
   {
      dojo.lang.assert(htmlElem);
      var id = null;
      var divElem = null;
      if (dojo.lang.isOfType(htmlElem, ps.aa.ObjectId))
      {
         id = htmlElem;
         divElem = ps.aa.Page.getElement(id);
      }
      else //if (htmlElem.nodeType == Node.ELEMENT_NODE) 
      {
         id = ps.aa.Page.getObjectId(htmlElem);
         //If the id was created from a widget get the page div element.
         if (id.widget != null || dojo.html.isTag(htmlElem, 'a')) {
            divElem = ps.aa.Page.getElement(id);
         }
         else {
            divElem = htmlElem;
         }
      }
      var parentId = null;
      if (id.isSnippetNode() && !this.treeModel.root.equals(id))
      {
         parentId = ps.aa.Page.getParentId(divElem, id);
         dojo.lang.assert(parentId);
      }
      
      this.activeId = id;
      if (ps.aa.Page.activate(divElem))
      {
         ps.aa.Menu.activate(id, parentId);

      }
      if (this.treeWidget)
         this.treeWidget.activate(id);
         
      //dojo.debug("activate id: " + id.toString());
   }
   
   /**
    * Updates the Tree widget.
    * Parameters can be passed but they will be ignored.
    */
   this.updateTreeWidget = function() 
   {
      this.treeWidget.onModelChanged();
   }

   /**
    * Is called when the user resized the tree.
    */   
   this._endTreeSizing = function()
   {
      var wg = dojo.widget.byId("pageTree");
      if (wg.sizeShare > this.MIN_TREE_WIDTH && !this.isShowTree)
      {
         // became visible because of resizing
         this.showTree();
      }
      else if (wg.sizeShare <= this.MIN_TREE_WIDTH && this.isShowTree)
      {
         // was closed by resizing
         this.hideTree();
      }
   },

   this.showTree = function()
   {
      this.isShowTree = true;
      this._maybeShowTree();
   }
   
   this.hideTree = function()
   {
      this.isShowTree = false;
      this._maybeShowTree();
   }
   
   this.showBorders = function()
   {
      this._switchBorderMode(true);
   }

   this.hideBorders = function()
   {
      this._switchBorderMode(false);
   }

   this._switchBorderMode = function(isShow)
   {
      var hash = PSHref2Hash(null);
      hash["sys_aamode"] = isShow ? "0" : "1";
      window.location.href = PSHash2Href(hash, null);
   }

   this.showPlaceholders = function()
   {
      ps.aa.Menu.toggleShowHidePlaceholders(true);
      ps.util.showHidePlaceholders(document, true);
      this.isShowPlaceholders = true;
   }

   this.hidePlaceholders = function()
   {
      ps.aa.Menu.toggleShowHidePlaceholders(false);
      ps.util.showHidePlaceholders(document, false);
      this.isShowPlaceholders = false;
   }
   
   /**
    * Shows/hides a tree based on value of {@link #isShowTree}
    */
   this._maybeShowTree = function()
   {
      ps.aa.Menu.toggleShowHideTree(this.isShowTree);
      this.toggleTreePane(this.isShowTree);
   }

   /**
    * Toggles the tree pane according to the specified flag
    * 
    * @param treePaneShowing <code>true</code> if wanting to show the tree panel.
    * Not <code>null</code>.
    */
   this.toggleTreePane = function (treePaneShowing)
   {
      dojo.lang.assert(dojo.lang.isBoolean(treePaneShowing));

      var wg = dojo.widget.byId("pageTree");

      if (!treePaneShowing)
      {
         this.lastShowingTreePaneWidth = wg.sizeShare;
//         dojo.debug(wg.sizeShare);
         wg.sizeShare = 0;
      }
      else if (wg.sizeShare < 1)
      {
         wg.sizeShare = this.lastShowingTreePaneWidth > this.MIN_TREE_WIDTH
               ? this.lastShowingTreePaneWidth : 290;
      }

      this._saveTreePaneShowing(treePaneShowing);
      var mainPane = dojo.widget.byId("ps.aa.mainSplitPane");
      mainPane._layoutPanels();
   }
   
   /**
    * Persists information whether the tree pane is shown to a cookie.
    * @param {Boolean} showing whether the tree pane is showing.
    * Not <code>null</code>.
    */
   this._saveTreePaneShowing = function (showing)
   {
      dojo.lang.assert(dojo.lang.isBoolean(showing));
      dojo.io.cookie.setCookie(this._TREE_PANE_SHOWING_COOKIE, showing);
   }
   
   /**
    * Loads information whether the tree pane is shown from a cookie.
    * Returns <code>true</code> if a tree pane should be shown.
    */
   this._loadTreePaneShowing = function ()
   {
      var wg = dojo.widget.byId("pageTree");
      var ck = dojo.io.cookie.getCookie(this._TREE_PANE_SHOWING_COOKIE);
      return ck ? eval(ck) : wg.sizeShare > 0;
   }

  /**
    * Refreshes the slot with the specified id.
    * @param {ps.aa.ObjectId} id the id of the slot to refresh.
    * The slot node must exist on the page.
    * Not <code>null</code>.
    * 
    * @return the given 'id'. This is because this only affects the content of
    *    the slot, but not the id (or the slot) itself.
    */
   this.refreshSlot = function (id)
   {
      return this._refreshX(id, undefined, false, ps.io.Actions, "getSlotContent");
   }

  /**
    * Refreshes the snippet with the specified id.
    * @param {ps.aa.ObjectId} id the id of the snippet to refresh.
    * The snippet node must exist on the page.
    * Not <code>null</code>.
    * @param {ps.aa.ObjectId} newId the id of the snippet to replace this snippet
    * with. Optional. If not specified uses <code>id</code> for this.
    * The snippet node must <b>not</b> exist on the page.
    * 
    * @return the id of the unchanged node, which is the id itself if both 'id' 
    *    and 'newId' are the same; or the id of the parent node if given 'id' 
    *    is not equals to 'newId'; otherwise return null, the whole tree has 
    *    been affected.
    */
   this.refreshSnippet = function (id, newId)
   {
      return this._refreshX(id, newId, true, ps.io.Actions, "getSnippetContent");
   }

   /**
    * Refreshes the field with the specified id.
    * @param {ps.aa.ObjectId} id the id of the field to refresh.
    * The field node must exist on the page.
    * Not <code>null</code>.
    * 
    * @return the given 'id'. This is because this only affects the content of
    *    the field, but not the id (or the field) itself.
    */
   this.refreshField = function (id)
   {
      return this._refreshX(id, undefined, true, ps.io.Actions, "getFieldContent");
   }

   /**
    * A utility function for common logic of {@link #refreshSlot},
    * {@link #refreshField}.
    * @param {ps.aa.ObjectId} id the id of the existing object to refresh.
    * The object node must exist on the page.
    * Not <code>null</code>.
    * @param {ps.aa.ObjectId} newId the id used to retrieve object node
    * from the server. Optional. If not specified, then id is used for this.
    * @param {Boolean} onlyContent if <code>true</content> replaces
    * the node content with result of the action,
    * if <code>false</code>, replaces the whole node.
    * Not <code>null</code>.
    * @param actionObject the object to call <code>action</code> method on.
    * An id of the object to load will be passed to it.
    * @param {String} action the name of the method from
    * <code>actionObject</code>
    * 
    * @return the id of the unchanged node, which is the id itself if both 'id' 
    *    and 'newId' are the same; or the id of the parent node if given 'id' 
    *    is not equals to 'newId'; otherwise return null, the whole tree has 
    *    been affected.
    */
   this._refreshX = function(id, newId, onlyContent, actionObject, action)
   {
      var notChangedId = null;
      
      dojo.lang.assertType(id, ps.aa.ObjectId);
      newId && dojo.lang.assertType(newId, ps.aa.ObjectId);
      dojo.lang.assertType(onlyContent, Boolean);
      dojo.lang.assert(actionObject);
      dojo.lang.assertType(action, String);
      
      if (!newId)
      {
         var newId = id;
      }
      
      var node = ps.aa.Page.getElement(id);
      var response = dojo.lang.hitch(actionObject, action)(newId, true);
      ps.io.Actions.maybeReportActionError(response);
      if (response.isSuccess())
      {
         var treeModel = this.treeModel;
         var treeNode = treeModel.getNodeById(id);

         treeModel.fireBeforeDomChange(id);
         
         // refresh or replace the new content (from the response)
         if (onlyContent)
         {
            if (!id.equals(newId))
               this._replaceDomId(id, newId);
            
            this._refreshNodeContent(node, newId,
                  response.getValue(), newId.isSnippetNode());
         }
         else
         {
            this._refreshNode(node, newId, response.getValue());
         }
         
         // Determine the unchanged node, so that we will 
         // rebuild all the child nodes of it in the treeModel
         if (id.equals(newId))
         {
            notChangedId = id;
         }
         else if (treeNode.parentNode)
         {
            notChangedId = treeNode.parentNode.objId;
         }
         else
         {
            // whole page is being changed
            notChangedId = null;
         }
         dojo.lang.assert(!dojo.lang.isUndefined(notChangedId));
         
         treeModel.fireDomChanged(notChangedId, newId);
      }
      ps.util.addPlaceholders(document);
      if(this.isShowPlaceholders)
      {
         this.showPlaceholders();
      }
      else
      {
         this.hidePlaceholders();
      }
      if(___sys_aamode == 1)
         ps.DivActionHelper.reset();
      return notChangedId;
   }

   /**
    * Replaces DOM node with the node created from the provided html text.
    * New and old nodes can have different ids, or can be even different tags.
    * 
    * @param node the node to replace. Not <code>null</code>.
    * @param {ps.aa.ObjectId} objId the id of the node to replace this node
    *    with. Not <code>null</code>.
    * @param {String} htmlContent the html to replace the node.
    *    Not <code>null</code>.
    */
   this._refreshNode = function (node, objId, htmlContent)
   {
      dojo.lang.assert(node);
      dojo.lang.assert(node.nodeType === dojo.dom.ELEMENT_NODE);
      dojo.lang.assertType(objId, ps.aa.ObjectId);
      dojo.lang.assertType(htmlContent, String);
      
      var newNodes = dojo.html.createNodesFromText(htmlContent, true);
      var newNode = ps.util.findNodeById(newNodes, objId.serialize());
      dojo.lang.assert(newNode,
            "Expected html to contain a node with id "
            + objId.serialize() + ".\nThe html: " + htmlContent);

      var unUsedNode = dojo.dom.replaceNode(node, newNode);
      dojo.dom.destroyNode(unUsedNode);
   }

   /**
    * Fresh or replace the node content or child nodes with the specified HTML content.
    * 
    * @param {HTMLElement} elem the DOM element of the specified snippet.
    * @parm {ps.aa.ObjectId} id the id of the object corresponding to the node.
    * Not <code>null</code>.
    * @param {String} htmlContent the html to replace the node.
    *    Not <code>null</code>.
    * @param skipAnchor true if wants to skip the first anchor <a> child element
    *    which is the case of a snippet node; false if simply replace all the
    *    child nodes with the specified HTML content.
    */   
   this._refreshNodeContent = function(pnode, id, htmlContent, skipAnchor)
   {
      dojo.lang.assert(id, ps.aa.ObjectId)
      if (___sys_aamode == 0 && skipAnchor)
      {
         var anchorElem = dojo.byId(id.getAnchorId());
         dojo.lang.assert(anchorElem,
               "Could not find anchor with id " + id.getAnchorId());
         var container = anchorElem.parentNode;
      }
      else
      {
         var anchorElem = null;
         var container = pnode;
      }

      // remove all child nodes from the parent node         
      var childNodes = new Array();
      while(container.hasChildNodes())
      { 
         childNodes.push(container.firstChild);
         dojo.dom.removeNode(container.firstChild); 
      }

      // remove all child nodes, except the anchor node if there is one
      for (var i=0; i<childNodes.length; i++)
      {
         if (childNodes[i] != anchorElem)
            dojo.dom.destroyNode(childNodes[i]);
      }

      // add the anchor node if there was one
      if (anchorElem != null)
         container.appendChild(anchorElem);
         
      // append the new nodes to the parent node
      var newNodes = dojo.html.createNodesFromText(htmlContent, true);
      for (var i=0; i<newNodes.length; i++)
      {
         container.appendChild(newNodes[i]);
      }
   }

   this.addSnippet = function(source)
   {
      var _this = this;
      switch(source)
      {
         case ps.aa.Menu.INSERT_FROM_SLOT:
            var snippets = dojo.html.getElementsByClass("PsAaSnippet",
               document.getElementById(this.activeId.toString()),"div",
               dojo.html.classMatchType.IsOnly,false);
            if(snippets.length<1)
            {
               this._openBrowseDlg(this.activeId,null,"before");
            }
            else
            {
               this.snippetPickerDlg.open(
                     function (slotId,refRelId,position) {
                        _this._openBrowseDlg(slotId,refRelId,position)},
                     function () {}, this.activeId,1);
            }
         break;
         case ps.aa.Menu.INSERT_FROM_SNIPPET:
            var snNode = this.treeModel.getNodeById(this.activeId);
            var snSlotId = snNode.parentNode.objId;
            this.snippetPickerDlg.open(
                  function (slotId,relId,position) {_this._openBrowseDlg(
                  slotId,relId,position)},
                  function () {}, snSlotId,1,"before",this.activeId.getRelationshipId());
         break;
      }
   }

   /**
    * Opens the browse dialog for selecting snippets to insert into slot at a
    * specified position with respect to a snippet represented by its 
    * relationship id.
    * @param slotId (ps.aa.ObjectId) assumed not <code>null</code>.
    * @param refRelId (String) may be <code>null</code>. positioning is not done.
    * @param position (String) assumed to be one of "before", 
    *    "after" and "replace" strings.
    */
   this._openBrowseDlg = function(slotId,refRelId,position)
   {
      var _this = this;
      this.contentBrowser.open(
            function (refreshedSlotId) 
            {_this.addSnippetToSlot(refreshedSlotId)},
            slotId, refRelId, position);
      
   }

   /**
    * Opens the snippet picker dialog for multiple snippet removal if the slot 
    * is not empty.
    */
   this.openRemoveSnippetsDlg = function()
   {
      var snippets = dojo.html.getElementsByClass("PsAaSnippet",
        document.getElementById(this.activeId.toString()),"div",
        dojo.html.classMatchType.IsOnly,false);
      if(snippets.length<1)
      {
         alert("The slot is empty.");
         return;
      }
      var _this = this;
      this.snippetPickerDlg.open(
            function (slotId,relIds) {_this._handleRemoveSnippets(slotId,relIds)},
            function () {}, this.activeId,0);
   }

   /**
    * This method is a common method expected to be called from following menu items.
    * Slot --  New (0)
    * Snippet -- New (1), Replace (2)
    * Page -- New (3), New Item (4)
    * The source param indicates which menu item this method has been called from.
    * For slot New menu, it opens the snippet picker dialog and then create new item dialog.
    * if the slot is empty then directly opens the create new item dialog.
    * For Snippet New and Replace menu actions opens the create new item dialog.
    * For New calls handleCreateItem function to create a new copy of the page.
    * For New Item menu action opens the create new item dialog.
    * 
    */
   this.createItem = function(source)
   {
      var oeWin = null;
      if (parent.newItemWindow && !parent.newItemWindow.closed)
      {
         oeWin = parent.newItemWindow;
      }
      else if(this.fieldEdit.psCeFieldWindow && !this.fieldEdit.psCeFieldWindow.closed)
      {
         oeWin = this.fieldEdit.psCeFieldWindow;
      }
      else if(this.psCeWindow && !this.psCeWindow.closed)
      {
         oeWin = this.psCeWindow;
      }
      if(oeWin != null)
      {
         var oeMsg = "You have an open editor, "
               + "close that window before creating another item.";
         alert(oeMsg);
         oeWin.focus();
         return false; 
      }
      
      var _this = this;
      switch(source)
      {
         case ps.aa.Menu.NEW_FROM_SLOT: //From Slot
            var snippets = dojo.html.getElementsByClass("PsAaSnippet",
               document.getElementById(this.activeId.toString()),"div",
               dojo.html.classMatchType.IsOnly,false);
            if(snippets.length<1)
            {
               this._openNewItemDlg(this.activeId,null,"before");
            }
            else
            {
               this.snippetPickerDlg.open(
                     function (slotId,relId,position) {_this._openNewItemDlg(
                     slotId,_this._getSnippetIdFromRelId(slotId,relId),position)},
                     function () {}, this.activeId,1);
            }
         break;
         case ps.aa.Menu.NEW_FROM_SNIPPET: //New From snippet
            var itemId = this.activeId;
            var node = this.treeModel.getNodeById(this.activeId);
            var slotId = node.parentNode.objId;
            this.snippetPickerDlg.open(
                  function (slotId,relId,position) {_this._openNewItemDlg(
                  slotId,_this._getSnippetIdFromRelId(slotId,relId),position)},
                  function () {}, slotId,1,"before",this.activeId.getRelationshipId());
         break;
         case ps.aa.Menu.REPLACE_FROM_SNIPPET: //Replace from snippet
            var itemId = this.activeId;
            var node = this.treeModel.getNodeById(this.activeId);
            var slotId = node.parentNode.objId;
            this.snippetPickerDlg.open(
                  function (slotId,relId,position) {_this._openBrowseDlg(
                  slotId,relId,position)},
                  function () {}, slotId,1,"replace",this.activeId.getRelationshipId());
         break;
         case ps.aa.Menu.COPY_FROM_CONTENT: //Copy from Page
            var itemId = this.treeModel.getRootNode().getObjectId();
            var response = ps.io.Actions.getItemPath(itemId);
            // handle failure
            if (!response.isSuccess())
            {
               ps.io.Actions.maybeReportActionError(response);
               return;
            }
            var ipath = response.getValue();
            var fpath = ipath.substring(0,ipath.lastIndexOf("/"));
            var newData = {"sys_contenttypeid":itemId.getContentTypeId(),
             "sys_templateid":itemId.getTemplateId(),"folderPath":fpath,
             "itemPath":ipath,"itemTitle":""};
            _this._handleCopyItem(itemId,newData);
         break;
         case ps.aa.Menu.NEW_FROM_CONTENT: //New from Page
            var itemId = this.treeModel.getRootNode().getObjectId();
            _this._openNewItemDlg(null,itemId,null);
         break;
         default:
            alert("invalid source");
      }
   }
   
   /**
    * Prompts the user for item title and validates it. Alerts and prompts again
    * if it is invalid.
    */
   this._handleCopyItem = function(itemId,newData)
   {
      var _this = this;
      var okCallBack = function(resultText)
      {
         newData.itemTitle = resultText;
         var response = ps.io.Actions.getIdByPath(newData.folderPath+"/"+resultText);
         if (response.isSuccess())
         {
            alert("Title should be unique under the specified folder.\n" + newData.folderPath );
            _this.dlg.hide();
            _this._handleCopyItem(itemId,newData);
         }
         else
         {
            _this.handleCreateItem(null,itemId,null,newData);
         }  
      }
      var cancelCallBack = function(){
         _this.dlg.hide();
      };
      
      var options={  "dlgTitle":"Create Copy",
                     "promptTitle":"Title",
                     "promptText":newData.itemTitle,
                     "textRequired":true,
                     "okBtnText":"Create",
                     "cancelBtnText":"Cancel",
                     "okBtnCallBack":okCallBack,
                     "cancelBtnCallBack":cancelCallBack
                  };
      this.dlg = ps.util.CreatePromptDialog(options);
      this.dlg.show();
      this.dlg.focusTitle();
   }

   /**
    * Convenient method to get the snippet id associated with the relationship id
    * in a slot.
    * @param slotId objectid of slot assumed not <code>null</code>
    * @param relId int relationship id, if <code>null</code> null.
    */
   this._getSnippetIdFromRelId = function(slotId,relId)
   {
      if(!relId)
         return null;
      var snippetId = null;
      if(relId != null)
      {
         var snippets = dojo.html.getElementsByClass("PsAaSnippet",
         document.getElementById(slotId.toString()),
         "div",dojo.html.classMatchType.IsOnly,false);
         for(var i=0;i<snippets.length;i++)
         {
            var id = ps.aa.Page.getObjectId(snippets[i]);
            if(id.getRelationshipId()==relId)
            {
               snippetId = id;
               break;
            }
         }
      }
      return snippetId;      
   }
   
   /**
    * Opens the create new item dialog and .
    * If this method is called from slot or snippet the slotId corresponds to 
    * the slot or slot id of snippet. If it is called from Page then the slot id 
    * will be null.
    * The itemId will be null if this method is called from an empty slot.
    * The position will be null if it is called from page.
    * @param slotId If not null assumed to be a valid ObjectId 
    *    corresponding to a slot.
    * @param itemId may be null, if not null assumed to be a valid ObjectId 
    *    corresponding to a snippet or page.
    * @param position may be null, if not assumed to be one of "before", 
    *    "after" and "replace" strings.
    */
   this._openNewItemDlg = function(slotId,itemId,position)
   {
      var _this = this;
      this.createItemDlg.open(
            function (slotId,itemId,position,newData) {
               _this.handleCreateItem(slotId,itemId,position,newData)},
            function () {}, slotId,itemId,position);
   }
   
   
   /**
    * Handles the item creation part.
    * Calls {@see ps.io.Actions#createItem} to create a new item.
    * If item creation fails opens full editor window. 
    * Otherwise calls {@see #postCreateItem}
    * for further processing.
    * If adding the item to folder fails alerts user about it and continues 
    * with other tasks.
    * @param slotId If not null assumed to be a valid ObjectId 
    *    corresponding to a slot.
    * @param itemId may be null, if not null assumed to be a valid ObjectId 
    *    corresponding to a snippet or page.
    * @param position may be null, if not assumed to be one of "before", 
    *    "after" and "replace" strings.
    * @param newData assumed not null and consists of the following parameters
    * {sys_contenttypeid,sys_templateid,folderPath,itemPath}
    * 
    */
   this.handleCreateItem = function(slotId,itemId,position,newData)
   {
      var response = ps.io.Actions.createItem(newData.sys_contenttypeid,
         newData.folderPath,newData.itemPath,newData.itemTitle);
      if(!response.isSuccess())
      {
         ps.io.Actions.maybeReportActionError(response);
         return;
      }
      var obj = response.getValue();
      if(dojo.lang.has(obj, "validationError"))
      {
            if(!confirm("The following errors occured while creating the new item\n"+
                obj.validationError + 
               "\nClick OK to open the full editor."))
               return;
            var iuresp = ps.io.Actions.getCreateItemUrl(
                  newData.folderPath, newData.sys_contenttypeid, false);
            ps.io.Actions.maybeReportActionError(iuresp);
            if (iuresp.isSuccess())
            {
               var url = iuresp.getValue().url;
               var idresp = ps.io.Actions.getIdByPath(newData.folderPath);
               if(!idresp.isSuccess)
               {
                 alert("Failed to get the folderid for the supplied folder path." +
                       "\nSkipping adding item to folder action.");               
               }
               else
               {            
                    url+="&sys_folderid=" + idresp.getValue().id;
               }
               this.newItemData = null;
               var temp = {"slotId":slotId,"itemId":itemId,
                "position":position,"newData":newData};
               this.newItemData = temp;
               parent.newItemWindow = window.open(url, "PsAaCreateItem",
                  this.PREVIEW_WINDOW_STYLE);
               parent.newItemWindow.focus();
            }         
            return;
      }
      var cid = obj.itemId;
      var fid = obj.folderId;
      if(fid == -1)
      {
         alert("Created the new item but failed to add it to the " +
               "folder. \n See console.log for more details.");
      }
      this.postCreateItem(slotId,itemId,position,newData,cid);
   }
   
   /**
    * Handles tasks after creating the items.
    * If slotId parameter is empty then it is assumed to be called from 
    * page level and reloads the page with the newly created item.
    * Otherwise adds the new item to slot and repositions and if position
    * is replace then removes the original item from slot.
    * 
    * @param slotId If not null assumed to be a valid ObjectId 
    *    corresponding to a slot.
    * @param itemId may be null, if not null assumed to be a valid ObjectId 
    *    corresponding to a snippet or page.
    * @param position may be null, if not assumed to be one of "before", 
    *    "after" and "replace" strings.
    * @param newData assumed not null and consists of the following parameters
    * {sys_contenttypeid,sys_templateid,folderPath,itemPath}
    * @param cid content id of newly created item assumed not null.
    * Note: This method gets called from content editor (activeEdit.xsl) 
    * make sure not break it while modifying.
    */
   this.postCreateItem = function(slotId,itemId,position,newData,cid)
   {
      if(slotId == null)
      {
         var newItemId = itemId.clone();
         newItemId.setContentId(cid);
         newItemId.setTemplateId(newData.sys_templateid);
         var response = ps.io.Actions.getUrl(newItemId,"PREVIEW_MYPAGE");
         ps.io.Actions.maybeReportActionError(response);
         if (!response.isSuccess())
         {
            return;
         }
         var value = response.getValue();
         dojo.lang.assert(dojo.lang.has(value, "url"));
         var newUrl = value.url + "&sys_command=editrc";
         window.location.href = newUrl;
      }
      else
      {
         //Add the new snippet 
         var snippetId = slotId.clone();
         snippetId.setContentId(cid);
         snippetId.setSnippetNode();
         snippetId.setTemplateId(newData.sys_templateid);
         
         var response = ps.io.Actions.addSnippet(
               snippetId, slotId, null, null);
         ps.io.Actions.maybeReportActionError(response);
         if (!response.isSuccess()) 
         {
            return;
         }
         var newRelId = response.getValue();
         var oldRelId = itemId==null?null:itemId.getRelationshipId();
         this.repositionSnippet(slotId,oldRelId,newRelId,position);
      }
   }

   /**
    * Repositions the snippet respect to the reference snippet as per the 
    * supplied position string within the given slot.
    * The new and old snippets are represented by their relationship ids.
    * Position could be either before or after or replace.
    * @param slotId (ps.aa.ObjectId) must not be <code>null</code> and must be 
    *    a valid slot id.
    * @param refRelId (String) The relationship id of the reference snippet.
    *    If <code>null</code> there is nothing to reposition, does nothing.
    * @param newRelId (String) The relationship id of the new snippet. 
    *    Must not be <code>null</code>.
    * @param position (String) If not <code>null</code> must be one of "before"
    *    or "after" or "replace". Otherwise defaults to "before".
    */
   this.repositionSnippet = function(slotId, refRelId, newRelId, position)
   {
      this.refreshSlot(slotId);
      var node = this.treeModel.getNodeById(slotId);
      this.updateTreeWidget(node);
      if(refRelId == null)
      {
         return;
      }
      if(position == null)
         position = "before";
      if(!(position == "before" || position == "after" || position=="replace"))
         dojo.lang.assert(false,"position must be either before or after or relace");
      dojo.lang.assert(slotId, "Slot id must be provided for reposition of snippet");
      dojo.lang.assert(slotId.isSlotNode(), "slotId must represent a slot object id");
      dojo.lang.assert(newRelId);

      var newSnippetId = this._getSnippetIdFromRelId(slotId,newRelId);
      dojo.lang.assert(newRelId, "Invalid relationship id of new snippet");
      var oldSnippetId = this._getSnippetIdFromRelId(slotId,refRelId);
      dojo.lang.assert(refRelId, "Invalid relationship id of reference snippet");
      
      var oldItemIndex = oldSnippetId.getSortRank();
      var newItemIndex = newSnippetId.getSortRank();

      //Check whether reposition is required or not
      var newindex = -1;
      if(position == "before" && newItemIndex != oldItemIndex-1)
      {
         newindex = parseInt(oldItemIndex,10)+1;
      }
      else if(position == "after" && newItemIndex != oldItemIndex+1)
      {
         newindex = parseInt(oldItemIndex,10)+2;
      }
      else if(position == "replace" && 
         (newItemIndex != oldItemIndex-1 || newItemIndex != oldItemIndex+1))
      {
         newindex = parseInt(oldItemIndex,10)+1;
      }
      if(newindex != -1)
      {
         var response = ps.io.Actions.move(newSnippetId, "reorder", newindex);
         ps.io.Actions.maybeReportActionError(response);
      }
      //Remove the item id from slot if position is replace
      if(position == "replace")
      {
         var response = ps.io.Actions.removeSnippet(oldSnippetId.getRelationshipId());
         ps.io.Actions.maybeReportActionError(response);
      }
      this.refreshSlot(slotId);
      var node = this.treeModel.getNodeById(slotId);
      this.updateTreeWidget(node);
      this.activate(slotId);
   }

   /**
    * Opens the active snippet in aa mode in new window.
    */
   this.openSnippet = function()
   {
      if(this.snippetOpenWindow &&  !this.snippetOpenWindow.closed)
      {
         if(!confirm(this.SNIPPETOPEN_MSG))
         {
             this.snippetOpenWindow.focus();
             return false;
         }
         else
         {
            this.snippetOpenWindow.close();
         }
      }
      this.snippetOpenWindow = this.openWindow(
         "TOOL_LINK_TO_PAGE", 
         this.PREVIEW_WINDOW_STYLE,
         this.activeId,
         "PSAaSnippetWindow");
      
   }
   
   /**
    * Refreshes the browsers current tab, if the browser dialog is still open.
    */
   this.refreshBrowseWindow = function()
   {
      if(!this.contentBrowser.wgtDlg.isShowing())
      {
         dojo.debug("No need to refresh, the content browser is closed.");
         return;
      }
      this.contentBrowser.currentTab.refreshBrowser();
   }
   
   /**
    * Implementation of Preview Current Revisions action. Parses the window url 
    * for all the required parameters to generate a preview url. Creates the 
    * Object id from these parameters and requests the preview url for this  
    * action and then opens in a named window.
    */
   this.previewWithCurrentRevisions = function()
   {
      var paramMap = this._parseUrlParams();
      this.openWindow(
         "PREVIEW_PAGE", 
         this.PREVIEW_WINDOW_STYLE,
         this._previewObjIdFromParamMap(paramMap),
         this.PREVIEW_WINDOW_NAME);
   }

   /**
    * Implementation of Preview With Edits action. Parses the window url for 
    * all the required parameters to generate a preview url. Creates the Object 
    * id from these parameters and requests the preview url for this  action and 
    * then opens in a named window.
    */
   this.previewWithEditRevisions = function()
   {
    var paramMap = this._parseUrlParams();
    var additionalParams = new Array;
    var param = new Object;
    param.name = "useEditRevisions";
    param.value = "yes";
    additionalParams[0] = param;
    this.openWindow(
      "PREVIEW_MYPAGE", 
      this.PREVIEW_WINDOW_STYLE, 
      this._previewObjIdFromParamMap(paramMap), 
      this.PREVIEW_WINDOW_NAME, 
      additionalParams);
   }

   /**
    * Generate objectid applicable to generate preview URL from the supplied 
    * parameter map.
    * @param paramMap javascript object that has all the required parameters 
    * (name-value pairs) to generate a preview URL. Assumed not null.
    */   
   this._previewObjIdFromParamMap = function(paramMap)
   {
      var objId = new Array();
      objId[ps.aa.ObjectId.NODE_TYPE] = "";
      objId[ps.aa.ObjectId.CONTENT_ID] = paramMap.sys_contentid;
      objId[ps.aa.ObjectId.TEMPLATE_ID] = paramMap.sys_variantid;
      objId[ps.aa.ObjectId.SITE_ID] = paramMap.sys_siteid;
      objId[ps.aa.ObjectId.FOLDER_ID] = paramMap.sys_folderid;
      objId[ps.aa.ObjectId.CONTEXT] = paramMap.sys_context;
      objId[ps.aa.ObjectId.AUTHTYPE] = paramMap.sys_authtype;
      objId[ps.aa.ObjectId.CONTENTTYPE_ID] = "";
      objId[ps.aa.ObjectId.CHECKOUT_STATUS] = "";
      objId[ps.aa.ObjectId.SLOT_ID] = "";
      objId[ps.aa.ObjectId.RELATIONSHIP_ID] = "";
      objId[ps.aa.ObjectId.FIELD_NAME] = "";
      objId[ps.aa.ObjectId.PARENT_ID] = "";
      objId[ps.aa.ObjectId.FIELD_LABEL] = "";
      objId[ps.aa.ObjectId.SORT_RANK] = "";
      return new ps.aa.ObjectId(dojo.json.serialize(objId));
   }
   
   /**
    * Parse the supplied URL string for the parameters and return as a map 
    * (JavaScript object).
    * @param url url string to parse for parameters, may be null in which case 
    * the URL is read from the window. If empty, an empty object is returned. 
    * The anchor part (everything starting with # till end) is ignored.
    */
   this._parseUrlParams = function(url)
   {
      // supplied url is null so get it from the window location
      if(url==null)
         url = window.location.toString();
      //Remove everything starting with "#" before parsing
      loc = url.indexOf("#");
      if(loc !=-1)
         url = url.substring(0, loc);
      //get the parameters
      url.match(/\?(.+)$/);
      var params = RegExp.$1;
      // split up the query string and store in an
      // associative array
      var params = params.split("&");
      var paramMap = {};
      for(var i=0;i<params.length;i++)
      {
          var tmp = params[i].split("=");
          paramMap[tmp[0]] = unescape(tmp[1]);
      }
      return paramMap;
    }

   /**
    * Opens a window with the impact analysis applet, showing only AA type rels.
    */ 
   this.showItemRelationships = function()
   {
      this.openWindow("TOOL_SHOW_AA_RELATIONSHIPS", 
         this._getSizedStyle(null, null, 500));
   }

   /**
    * Opens a window that allows the user to pick a language to create a 
    * translation.
    */ 
   this.createTranslation = function()
   {
      this.openWindow("ACTION_Translate", this._getSizedStyle(null, 300, 225));
   }

   /**
    * Creates a promotable version of the current item and then opens it in
    * the same browser using the template of the current item. If the current
    * item's id does not have a folder id, then the folder id of the parent is
    * used.
    */ 
   this.createVersion = function()
   {
      this.openWindow("ACTION_Edit_PromotableVersion", 
         this._getSizedStyle(null, 800, 700));
   }

   /**
    * Generates a URL that would launch the user into AA for the active item and 
    * shows it in a small popup so it can be copied by the user.
    */ 
   this.showPageUrl = function()
   {
      var tmpId = this.activeId;
      var url = this._getUrl("TOOL_LINK_TO_PAGE", tmpId);
      if (url == null)
      {
         dojo.debug("Failed to get link for item id = " + this.activeId.getContentId());
         return;
      }
      var loc = window.location;
      var fullUrl = loc.protocol + "//" + loc.host + url;
     
      ps.util.ShowPageLinkDialog(fullUrl);
   }

   /**
    * Makes a server call to create a url that can be used to access the 
    * current page and returns it. Could be null if any problems occur.
    * This is always the top level page, regardless of the currently active
    * object.
    */
   this.getLinkToCurrentPage = function()
   {
      var tmpId = this.treeModel.getRootNode().getObjectId();
      var url = this._getUrl("TOOL_LINK_TO_PAGE", tmpId);
      return url;
   }

   /**
    * Sends a logout request to the server and updates the current window
    * location.
    */
   this.logout = function()
   {
      var aaUrl = ps.aa.controller.getLinkToCurrentPage();
      var loc = window.location;
      var urlPath = loc.protocol + "//" + loc.host + "/Rhythmyx/logout";
      var url = ps.util.addParamToUrl(urlPath, "sys_redirecturl", 
       	 escape(aaUrl));
      window.location = url;
   }

   /**
    * Launches an edition that will publish the current item now and show the
    * results in a new browser window.
    */ 
   this.publishPage = function()
   {
      this.openWindow("TOOL_PUBLISH_NOW", this._getSizedStyle(null, 700, 450));
      //in case the item was checked in /transitioned
      this.wfActions.handleObjectIdModifications("0");
      var ids = new Array();
      ids[0] = this.activeId;
      this._resetObjectIcons(ids);
      this.refreshImageForObject(this.activeId);
   }

   /**
    * Opens a window with the compare tool using the currently viewed (CR) and 
    * (CR-1) revisions and the same template as the current item. 
    */ 
   this.compareItemRevisions = function()
   {
      this.openWindow("ACTION_View_Compare", 
         this._getSizedStyle(null, 900, 700));
   }

   /**
    * Opens a window with contents of the active item.
    */ 
   this.viewContent = function()
   {
        this.openWindow("CE_VIEW_CONTENT", this._getSizedStyle(null, -1, 700));
   }

   /**
    * Opens a window with properties of the active item.
    */ 
   this.viewProperties = function()
   {
        this.openWindow("CE_VIEW_PROPERTIES");
   }

   /**
    * Opens a window with revisions of the active item.
    */ 
   this.viewRevisions = function()
   {
        this.openWindow("CE_VIEW_REVISIONS");
   }

   /**
    * Opens a window with audit trail of the active item.
    */ 
   this.viewAuditTrail = function()
   {
        this.openWindow("CE_VIEW_AUDIT_TRAIL");
   }
   
   /**
    * This is a helper method for opening the view menu action window. Default 
    * settings correspond to a content editor window.
    * @param windowType See same param of _getUrl().
    * @param wStyle Window style, may be null in which case, this._getSizedStyle
    * is called.
    * @param objectId See same param of _getUrl().
    * @param wName Name of the window, may be null in which case 
    * this.CE_WINDOW_NAME is assumed.
    * @param additionalParams additional parameters to append to the url, if 
    * any, must be an array of objects and each object must have obj.name and 
    * obj.value fields.
    * @return returns the object of newly opened window.
    */
   this.openWindow = function (windowType, wStyle, objectId, wName, additionalParams) 
   {
      var url = this._getUrl(windowType, objectId);
      if (url == null)
         return;
      if (additionalParams != null && additionalParams != undefined) {
          dojo.lang.forEach(additionalParams, function (param) 
          {
             url = url + "&" + param.name + "=" + param.value;
          });
      }
      var ws = wStyle;
      if(ws==null)
         ws = this._getSizedStyle();
      var wn = wName;
      if(wn==null)
         wn = this.CE_WINDOW_NAME;
      var vwin = window.open(url, wn, ws);
      var actualSize = ps.util.getScreenSize(vwin, false);
      var desiredSize = this._getSizeFromStyle(ws);
      if (actualSize.width != desiredSize.width 
         || actualSize.height != desiredSize.height)
      {  
         vwin.resizeTo(desiredSize.width, desiredSize.height);
      }
      vwin.focus();
      return vwin;
   }
   
   /**
    * Changes the template that a snippet currently uses.
    */
   this.changeTemplate = function()
   {
      this.templatesDlg.controller = this;
      var _this = this;
      this.templatesDlg.open(
         function (newSnippetId, snippetId)
         {
            _this._handleTemplateChange(newSnippetId, snippetId);
         },
         function () {}, this.activeId);
   }
   
   /**
    * Parses the supplied style string looking for a width and height property.
    * If found, that value is returned, otherwise default values of 800 for 
    * width and 400 for height are returned.
    *
    * @param style A window style string, e.g. 'location=0,width=200,height=300'
    * If null, the default values are returned.
    * @return An object with 2 properties that contain the width and height
    * as found in the supplied style (in pixels.)
    */
   this._getSizeFromStyle = function(style)
   {
      var size = new Object();
      size.width = 800;
      size.height = 400;
      if (style == null)
      {
         return size;
      }
      var winProps = style.split(",");
      var foundWidth = false;
      var foundHeight = false;
      for (var i  = 0; i < winProps.length && !(foundWidth && foundHeight); i++)
      {
         var prop = winProps[i].split("=");
         if (prop[0] == "width")
         {
            size.width = prop[1];
            foundWidth = true;
         }
         else if (prop[0] == "height")
         {
            size.height = prop[1];
            foundHeight = true;
         }
      }
      return size;
   }
   
   /**
    * Builds a window style string from the supplied params. Each param has a
    * default as noted in its description. 
    * 
    * @param style The base style, without the width and height. If undefined or 
    * null, defaults to this.BASE_WINDOW_STYLE.
    * @param width The desired window width, in pixels. If undefined or 
    * negative, defaults to 800.
    * @param height The desired window height, in pixels. If undefined or 
    * negative, defaults to 400.
    *
    * @return A String that contains the style props as noted above. 
    */
   this._getSizedStyle = function(style, width, height)
   {
      if (style == null)
         style = this.BASIC_WINDOW_STYLE;
      if (width == null || width < 0)
         width = 800;
      if (height == null || height < 0)
         height = 400;
      if (style.length > 0)
         style = style + ",";
      return style + "width=" + width + ",height=" + height;
   }
   
   /**
    * Handler for change template select action
    * @param {ps.aa.ObjectId} the objectId of the new snippet.
    * @param {ps.aa.ObjectId} the objectId of the old snippet.
    */
   this._handleTemplateChange = function(newSnippetId, snippetId)
   {
      var response = ps.io.Actions.getItemSortRank(newSnippetId.getRelationshipId());
      if(response.isSuccess())
      {
         var rank = parseInt(response.getValue());
         if(rank == 0)
            rank = 1;
         var resp = ps.io.Actions.moveToSlot(
            newSnippetId, newSnippetId.getSlotId(), newSnippetId.getTemplateId(), rank);
         if(!resp.isSuccess())
         {
            ps.io.Actions.maybeReportActionError(resp);
         }
         else
         {
            var parentId = this.refreshSnippet(snippetId, newSnippetId);
            this.updateTreeWidget(parentId);
            this.activate(newSnippetId);
         }   
      }
   }

   this._handleRemoveSnippets = function(slotId,relIds)
   {
      var response = ps.io.Actions.removeSnippet(relIds);
      ps.io.Actions.maybeReportActionError(response);
      if (response.isSuccess())
      {
         this.refreshSlot(slotId);
         var node = this.treeModel.getNodeById(slotId);
         this.updateTreeWidget(node);
         this.activate(slotId);
      }
   }

   /**
    * Removes the active snippet.
    */
   this.removeSnippet = function()
   {
      var node = this.treeModel.getNodeById(this.activeId);
      var nextActiveId = this.treeModel.getNextSiblingId(node.objId);
      
      var response = ps.io.Actions.removeSnippet(node.objId.getRelationshipId());
      ps.io.Actions.maybeReportActionError(response);
      if (response.isSuccess())
      {
         var pid = node.parentNode.objId;
         this.refreshSlot(pid);
         this.updateTreeWidget(node.parentNode);
         var div = ps.aa.Page.getElement(nextActiveId);
         this.activate(div);
      }
   }

   /**
    * Opens the content editor window for editing the item.
    * Checks whether the item is checked out or not. If not shows an alert message to
    * user to check out the item before editing.
    * If the full editor or single field editor window is open then alerts the
    * user whether he wants to continue with other field editing or open this
    * item for editing.
    * Gets the content editor url from the server and opens editor window.   
    */
   this.editAll = function(cachedUrl)
   {
        //Check whether the item is checked out or not
        if(this.activeId.isCheckoutByMe() == 0)
        {
            alert(this.CHECKOUT_MSG);
            return false;
        }
         //If a field editor window is open for editing a field, propmt user
         //whether he wants to open this item and cancel editing of the field?
        if(this.fieldEdit.psCeFieldWindow && !this.fieldEdit.psCeFieldWindow.closed)
        {
            if(!confirm(ps.aa.controller.EDITOROPEN_MSG))
            {
                this.fieldEdit.psCeFieldWindow.focus();
                return false;
            }
            else
            {
               this.fieldEdit.psCeFieldWindow.close();
            }
        }
         //If a full editor window is open for editing an item, propmt user
         //whether he wants to open this item and cancel editing of the other item?
        else if(this.psCeWindow && !this.psCeWindow.closed)
        {
            if(this.activeId.getContentId() == this.editObjectId.getContentId() || !confirm(ps.aa.controller.EDITOROPEN_MSG))
            {
                ps.aa.controller.psCeWindow.focus();
                return false;
            }
            else
            {
               ps.aa.controller.psCeWindow.close();
            }
        }
        else if(this.fieldEdit.inplaceEditing)
        {
           if(!confirm(this.INPLACE_EDITOROPEN_MSG))
              return false;
           this.fieldEdit.onInplaceCancel();
        }
        
        this.editObjectId = this.activeId;
        var ceurl = "";
        if(cachedUrl && cachedUrl.length > 0)
        {
            ceurl = cachedUrl;
        }
        else
        {
           var response = ps.io.Actions.getUrl(this.activeId,"CE_EDIT");
           ps.io.Actions.maybeReportActionError(response);
            if (!response.isSuccess())
            {
               return false;
            }
            var value = response.getValue();
            dojo.lang.assert(dojo.lang.has(value, "url"));
            ceurl = value.url;
        }
        this.psCeWindow = window.open(ceurl,this.CE_EDIT_ITEM_WINDOW,this.PREVIEW_WINDOW_STYLE);
        this.psCeWindow.focus();
   }
   
   /**
    * Updates all the fields corresponding to the active objectid.
    * 
    */
   this.updateAllFields = function(sysTitle)
   {
      this.refreshFieldsOnPage(this.editObjectId.getContentId(),null,this.psCeWindow);
      //Reset the tree label
      this._resetTreeLabel(sysTitle);
   }
   
   /**
    * Queries the server for a Url that matches the requested windowType, using
    * the supplied Id. If the Id is null, the currently active Id is used.
    *
    * @param windowType The type of the window to be opened. Makes a request
    * to the server action with the supplied type and gets the url, if the
    * response is not success, then shows error message to user. The name must
    * match one of the TYPE_xxx values in PSGetUrlAction.java.
    * @param objectId Object Id, may be null in which case it is assumed as 
    * this.activeId.
    *
    * @return Either null (meaning the request to the server failed and a 
    * message was shown to the user,) or a valid URL as a String.
    */
   this._getUrl = function(windowType, objectId)
   {
      var oid = objectId;
      if(oid==null)
         oid = this.activeId;
      var response = ps.io.Actions.getUrl(oid, windowType);
      ps.io.Actions.maybeReportActionError(response);
      if (!response.isSuccess()) 
      {
         return null;
      }
      var value = response.getValue();
      dojo.lang.assert(dojo.lang.has(value, "url"));
      return value.url;
   }   
   
   /**
    * Convenient method to reset the tree labels, meant to be called from 
    * updateAllFields method, which gets invoked when the full content editor is
    * used to edit the content.
    * @param newtitle, the new title that needs to be set, does nothing if 
    * newtitle is null or empty.
    */
   this._resetTreeLabel = function(newtitle)
   {
      if(newtitle == null || newtitle == "")
         return;
      if(this.editObjectId == null)
         return;
      /*It is assumed that the full editor is called from either page node or
      snippet node or field node. For field nodes the parent node is assumed to
      be the page node or snippet node.
      */
      var parentNd = this.treeModel.getNodeById(this.editObjectId);
      if(this.editObjectId.isFieldNode())
         parentNd = parentNd.parentNode;
      parentNd.clearLabel();
      var id = parentNd.objId.serialize();
      var divElem = dojo.byId(id);
      dojo.lang.assert(divElem, "Cannot find DIV element with id=" + id);
      var oldtitle = dojo.html.getAttribute(divElem, "psAaLabel");
      if(newtitle != oldtitle)
      {
         divElem.setAttribute("psAaLabel",newtitle);
         this.updateTreeWidget();
      }
   }
   
   /**
    * Gets the affected object ids after the specified item has been modified.
    * 
    * @param {int} contentId The content id of the item that has been modified.
    * @param {String} fieldName The modified field name. It may be null if all
    *    field may have been modified.
    * 
    * @return {dojo.collections.ArrayList} a list of affected object ids in
    *    {ps.aa.ObjectId}.
    */
   this._getAffectedObjectIds = function(contentId, fieldName)
   {
      // gets the nodes that are directly affected by modifying this item
      var results = this.treeModel.getIdsFromContentId(contentId, fieldName);
         
      // gets the nodes that are indirectly affected by modifying this item,
      
      // gets the content id of all managed nodes
      var allObjIds = this.treeModel.getAllIdsByContentId(null);
      var allContentIds = new dojo.collections.ArrayList();
      var cid;
      for (var i=0; i<allObjIds.count; i++)
      {
         cid = allObjIds.item(i).getContentId();
         if (!allContentIds.contains(cid))
            allContentIds.add(cid);
      }
      
      // gets the affected parent/owner nodes
      var response = ps.io.Actions.getInlinelinkParentIds(contentId, 
          allContentIds.toArray());
      ps.io.Actions.maybeReportActionError(response);
      if (response.isSuccess())
      {
          var parentIds = response.getValue();
          for (var i=0; i<parentIds.length; i++)
          {
             if (parentIds[i] != contentId)
             {
                var objIds = this.treeModel.getIdsFromContentId(parentIds[i], null);
                results.addRange(objIds);
             }
          }
      }
      
      return results;
   }
   
   /**
    * Refreshes the fields on page by getting the rendered content for the fields
    * 
    * @param {int} contentId The content id of the item that has been modified.
    * @param {String} fieldName The modified field name. It may be null if all
    *    field may have been modified.
    * @param windowObj The window to need to gain focus afterwards if specified.
    */
   this.refreshFieldsOnPage = function(contentId,fieldName,windowObj)
   {
      var results = this._getAffectedObjectIds(contentId, fieldName);

      // refresh the related nodes        
      for(var i=0; i<results.count; i++)
      {
         var result = results.item(i);
         dojo.lang.assertType(result, ps.aa.ObjectId);
         if(result.isFieldNode())
         {
            this.refreshField(result, null);
         }
         else if(result.isSnippetNode())
         {
            this.refreshSnippet(result,null);
         }
      }
      //If the window is not closed then focus the window
      if(windowObj && !windowObj.closed)
      {
         windowObj.focus();
      }
      this.refreshOpener(contentId);
   }
   
   /**
    * Edit the field, calls fields edit function.
    */
   this.editField = function()
   {
      if(___sys_aamode == 1)
         ps.DivActionHelper.reset();
      this.fieldEdit.editField(ps.aa.Page.activeDiv,null);
   }

   this.cutSnippet = function()
   {
      alert("FIXME: cutSnippet()");
   }

   this.pasteSnippet = function()
   {
      alert("FIXME: pasteSnippet()");
   }

   /**
    * Moves active snippet down.
    * Facade method to be called from event handlers.
    */
   this.moveSnippetDown = function()
   {
      var snippet = this._getSnippetNode(ps.aa.Page.activeDiv);
      if (snippet)
      {
         var snippetId = ps.aa.Page.getObjectId(snippet)
         dojo.lang.assert(snippetId);

         var treeNode = this.treeModel.getNodeById(snippetId);
         var nodeIndex = treeNode.getIndex();
         var siblings = treeNode.parentNode.childNodes.toArray();

         if (nodeIndex < siblings.length - 1)
         {
            var response = ps.io.Actions.move(snippetId, "down");
            ps.io.Actions.maybeReportActionError(response);
            if (response.isSuccess())
            {
               var node2Index = nodeIndex + 1;
               var node2 = siblings[node2Index];
               // swap tree model nodes
               treeNode.parentNode.childNodes.setByIndex(nodeIndex, node2);
               treeNode.parentNode.childNodes.setByIndex(node2Index, treeNode);
               var snippet2 = ps.aa.Page.getElement(node2.objId);
               ps.util.swapNodes(snippet, snippet2);
               this.updateTreeWidget(treeNode.parentNode);
               this.activate(snippet);
            }
         }
      }
   }

   /**
    * Moves active snippet up.
    * Facade method to be called from event handlers.
    */
   this.moveSnippetUp = function()
   {
      var snippet = this._getSnippetNode(ps.aa.Page.activeDiv);
      if (snippet)
      {
         var snippetId = ps.aa.Page.getObjectId(snippet)
         dojo.lang.assert(snippetId);

         var treeNode = this.treeModel.getNodeById(snippetId);
         var nodeIndex = treeNode.getIndex();
         var siblings = treeNode.parentNode.childNodes.toArray();

         if (nodeIndex > 0)
         {
            var response = ps.io.Actions.move(snippetId, "up");
            ps.io.Actions.maybeReportActionError(response);
            if (response.isSuccess())
            {
               var node2Index = nodeIndex - 1;
               var node2 = siblings[node2Index];
               // swap tree model nodes
               treeNode.parentNode.childNodes.setByIndex(nodeIndex, node2);
               treeNode.parentNode.childNodes.setByIndex(node2Index, treeNode);

               var snippet2 = ps.aa.Page.getElement(node2.objId);
               ps.util.swapNodes(snippet, snippet2);
               this.updateTreeWidget(treeNode.parentNode);
               this.activate(snippet);
            }
         }
      }
   }

   this.workflowItem = function()
   {
      this.wfActions.open();
   }

   /**
    * Moves snippet to the specified slot.
    * If necessary requests user to specify a new template.
    * @param {ps.aa.SnippetMove} move the snippet move parameters.
    * Can be changed by the method, e.g. when the method needs to change the
    * template.
    * Not <code>null</code>.
    * @return <code>true</code> if the move is successful so far.
    * Otherwise returns <code>false</code>.
    * The oparation can still fail after
    * this method returns, e.g. if user cancels template selection dialog.
    */
   this.moveToSlot = function (move)
   {
      dojo.lang.assertType(move, ps.aa.SnippetMove);

      var response = ps.io.Actions.moveToSlot(
            move.getSnippetId(),
            move.getTargetSlotId().getSlotId(),
            move.getTargetSnippetId().getTemplateId(),
            move.getTargetIndex());

      var success = this._handleMoveToSlotResponse(response, move);
      move.setSuccess(success);
      return success;

   }
   
   /**
    * Is used to process response from {@link ps.io.Actions#moveToSlot}.
    * @param {ps.io.Response} response return value of
    * {@link ps.io.Actions#moveToSlot}.
    * Not <code>null</code>.
    * @param {ps.aa.SnippetMove} move the snippet move parameters.
    * Not <code>null</code>.
    * @return <code>true</code> if the snippet move is successful so far.
    * @see #moveToSlot
    */
   this._handleMoveToSlotResponse = function (response, move)
   {
      dojo.lang.assertType(response, ps.io.Response);
      dojo.lang.assertType(move, ps.aa.SnippetMove);

      if (response.isSuccess())
      {
         this.maybeRefreshMovedSnippetNode(move, true);
         return true;
      }
      else if (response.getValue() === ps.io.Actions.NEEDS_TEMPLATE_ID)
      {
         if (move.getSnippetId().getTemplateId() !==
             move.getTargetSnippetId().getTemplateId())
         {
            ps.error("Template was already changed!");
            return false;
         }
         var response = ps.io.Actions.getAllowedSnippetTemplates(
               move.getTargetSnippetId());

         // handle failure         
         ps.io.Actions.maybeReportActionError(response);
         if (!response.isSuccess())
         {
            return false;
         }

         var value = response.getValue();
         dojo.lang.assert(dojo.lang.has(value, "count"));
         dojo.lang.assert(dojo.lang.has(value, "templateHtml"));

         if (value.count == 0)
         {
            ps.error("There are no templates, configured for the target slot!");
            return false;
         }
         else if (value.count == 1)
         {
            var nodes = dojo.html.createNodesFromText(value.templateHtml, true);
            // 1 extra node is for the "Cancel" button
            dojo.lang.assert((nodes.length - 1) === 1,
                  "Got more than 1 node from " + value.templateHtml);
            var snippetDiv = nodes[0];
            move.setTargetSnippetId(ps.aa.Page.getObjectId(snippetDiv));
            dojo.dom.destroyNode(snippetDiv);
            delete snippetDiv;

            var result = this.moveToSlot(move);
            this.maybeRefreshMovedSnippetNode(move, result);
            return result;
         }
         else
         {
            
            this.templatesDlg.snippetMove = move;
            this.templatesDlg.controller = this;
            this.templatesDlg.open(this._onMoveToSlotTemplateSelected,
               this._onSnippetTemplateSelectionDialogCancelled, move.getTargetSnippetId());            
            
            // *do not* refresh snippet from backend, because the snippet
            // has not been changed on backend, as the actual move operation
            // will be performed much later, when user selected the template
            return true;
         }
      }
      else // unexpected failure
      {
         ps.io.Actions.maybeReportActionError(response);
         return false;
      }
      dojo.lang.assert(false, "Should not reach here");
   }
   
   /**
    * Refreshes the target snippet node if it is moved and the update is allowed
    * by the <code>move</code> parameter.
    * Used to update the snippet HTML after the drop is complete.
    * {ps.aa.SnippetMove} move the snippet move parameters.
    * Not <code>null</code>.
    * {Boolean} moved whether the page was moved.
    * Not <code>null</code>.
    */
   this.maybeRefreshMovedSnippetNode = function (move, moved)
   {
      dojo.lang.assertType(move, ps.aa.SnippetMove);
      
      if (moved)
      {
         if (move.getDontUpdatePage())
         {
            move.setUiUpdateNeeded(true);
         }
         else
         {
            this.refreshSlot(move.getSlotId());
            var changedNodes = [move.getSlotId()];
            if (move.getSlotId().getSlotId() !==
                 move.getTargetSlotId().getSlotId())
            {
               this.refreshSlot(move.getTargetSlotId());
               changedNodes.push(move.getTargetSlotId());
            }
            
            this.updateTreeWidget(changedNodes);
         }
      }
   }
   
   /**
    * This method is called when a user selects a snippet
    * in the template selection dialog.
    * When called this will have the scope of SelectTemplates.
    * @param {ps.aa.ObjectId} the objectId of the new snippet.
    * @param {ps.aa.ObjectId} the objectId of the old snippet.
    */
   this._onMoveToSlotTemplateSelected = function (newSnippetId, snippetId)
   {
      dojo.lang.assert(newSnippetId, ps.aa.ObjectId);
      dojo.lang.assert(snippetId, ps.aa.ObjectId);
      
      var move = this.snippetMove;
      dojo.lang.assert(move);
      move.setTargetSnippetId(newSnippetId);
      var result = this.controller.moveToSlot(move);
      if (!result)
      {
         this.cancelCallback();
      }
      else
      {
         this.controller.maybeRefreshMovedSnippetNode(move, true);

         var snippetId = move.getTargetSnippetId();
         this.controller.activate(ps.aa.Page.getElement(snippetId));
      }
   }   

   /**
    * Is called when the snippet template selection dialog is cancelled.
    * When called this will have the scope of SelectTemplates.
    */
   this._onSnippetTemplateSelectionDialogCancelled = function ()
   {
      // notes, 'this' is in the context of 'ps.content.SelectTemplates', but
      // not in the context of 'ps.aa.controller'
      var move = this.snippetMove;
      dojo.lang.assert(move);
      
      this.controller.refreshSlot(move.getSlotId());
      this.controller.refreshSlot(move.getTargetSlotId());
      this.controller.updateTreeWidget(move.getSlotId(), move.getTargetSlotId());
   }
   
   /**
    * This method is called to dismiss the template selection dialog.
    */
   this._cancelSnippetTemplateSelectionDialog = function ()
   {
      var dialog = dojo.widget.byId(this._SELECT_TEMPLATE_DLG_ID);
      if (!dialog)
      {
         return;
      }
      this._onSnippetTemplateSelectionDialogCancelled();
      dialog.closeWindow();
   }
   
   /**
    * @param {ps.aa.ObjectId} snippetId the id of the snippet to move.
    * Not null.
    * @param {Number} targetIndex the position, starting with 1,
    * in the target slot to move the snippet to.
    * @return <code>true</code> if the operation was successful.
    * Otherwise returns <code>false</code>.
    */
   this.reorderSnippetInSlot = function (snippetId, targetIndex)
   {
      dojo.lang.assertType(snippetId, ps.aa.ObjectId);
      dojo.lang.assert(dojo.lang.isNumeric(targetIndex),
            "Can't be interpreted as number: \"" + targetIndex + "\"");
      var response = ps.io.Actions.move(snippetId, "reorder", targetIndex);
      ps.io.Actions.maybeReportActionError(response);
      return response.isSuccess();
   }
   
   /**
    * Replaces the specified old id to the new one.
    * 
    * @param {ps.aa.ObjectId} oldId The to be replaced id.
    * @param {ps.aa.ObjectId} newId The new id.
    */
   this.replaceId = function(oldId, newId)
   {
      dojo.lang.assert(dojo.lang.isOfType(oldId, ps.aa.ObjectId));
      dojo.lang.assert(dojo.lang.isOfType(newId, ps.aa.ObjectId));

      var oldIds = new Array();
      oldIds[0] = oldId;
      var newIds = new Array();
      newIds[0] = newId;
      
      this.replaceIds(oldIds, newIds);
   }

   /**
    * Replaces the specified old ids to the new ones
    * 
    * @param {Array of ps.aa.ObjectId} oldIds The to be replaced ids.
    * @param {Array of ps.aa.ObjectId} newIds The new ids.
    */
   this.replaceIds = function(oldIds, newIds)
   {
      dojo.lang.assert(dojo.lang.isArray(oldIds));
      dojo.lang.assert(dojo.lang.isArray(newIds));
      dojo.lang.assert(oldIds.length === newIds.length);

      // use function instead of a var, because it can change      
      var _this = this;
      function getRootId()
      {
         return _this.treeModel.getRootNode().objId;
      }
      
      this.treeModel.fireBeforeDomChange(getRootId());

      var activeId = this.activeId;
      var activeIdChanged = false;
      for (var i=0; i<oldIds.length; i++)
      {
         var oldId = oldIds[i];
         var newId = newIds[i];
         if (oldId.equals(activeId))
         {
            activeId = newId;
            activeIdChanged = true;
         }
         this._replaceDomId(oldId, newId);
         this.refreshImageForObject(newId);
      }
      this.treeModel.fireDomChanged(null, null);
      this.updateTreeWidget(getRootId());

      if (activeIdChanged)
      {
         // reset selection
         var otherId = getRootId();
         if (otherId.equals(activeId)
           && this.treeModel.getRootNode().childNodes.count > 0)
         {
            otherId = this.treeModel.getRootNode().childNodes.item(0).objId;
         }
         this.activate(otherId);
         this.activate(activeId);
      }
   }
   
   /**
    * Replaces the id of an DOM element.
    * 
    * @param {ps.aa.ObjectId} oldId The to be replaced id.
    * @param {ps.aa.ObjectId} newId The new id.
    */
   this._replaceDomId = function(oldId, newId)
   {
      function changeElementId(id1, id2)
      {
         var elem = dojo.byId(id1);
         dojo.lang.assert(elem != null);
         elem.id = id2;
      }
      
      changeElementId(oldId.serialize(), newId.serialize());
      if(___sys_aamode == 0)
         changeElementId(oldId.getAnchorId(), newId.getAnchorId());
      
//    The following code is needed if wants to change ids directly from 
//    tree Model, which is to avoid rebuild treeModel 
//         this._replaceDomId(oldId.getTreeNodeWidgetId(), newId.getTreeNodeWidgetId());
//         var node = this.treeModel.getNodeById(oldId);
//         dojo.lang.assert(node != null);
//         node.objId = newId;;
   }
   
   /**
    * Add snippet to slot call back that will be called by the content 
    * browse/search dialog.
    * @param snippetToAdd is the snippet to add to slot, always defined.
    * todo: implement this
    */
   this.addSnippetToSlot = function(refreshedSlotId)
   {
      this.refreshSlot(refreshedSlotId);
      this.updateTreeWidget(refreshedSlotId);
   }
   
   /**
    * Refresh the icons of the given object ids.
    * 
    * @param {dojo.collections.ArrayList} ids the list of object ids {ps.aa.ObjectId}.
    */
   this._resetObjectIcons = function(ids)
   {
      for (var i=0; i<ids.count; i++)
      {
         var id = ids.item(i);
         this.refreshImageForObject(id);
      }
   }
      
   /**
    * Given the object id, refresh the image for the object rendered to reflect 
    * the checkout status.
    * @param objId objectid of the managed object, must not be null.
    */
   this.refreshImageForObject = function(objId)
   {
      dojo.lang.assertType(objId, ps.aa.ObjectId);
      if(___sys_aamode == 1)
         return;
      var anchor = dojo.byId(objId.getAnchorId());
      var imgElem = dojo.dom.getFirstChildElement(anchor);
      if(imgElem == null)
      {
         dojo.debug("Image element for the objectid = " + objId.serialize() 
           + "is not found");
         return;
      }
      else
      {
         imgElem.src = objId.getImagePath(this.IMAGE_ROOT_PATH);
      }
   }
   
   /**
    * Returns the snippet node containing the provided node.
    * The node is returned if it is a snippet node.
    * @param node the node to search snippet node for.
    * If <code>null</code> the method returns <code>null</code>.
    * @return the snippet node containing the provided node,
    * or <code>null</code>, if such node can't be found.
    */
   this._getSnippetNode = function (node)
   {
      if (!node)
      {
         return null;
      }
      while (node)
      {
         if (node.className === ps.aa.SNIPPET_CLASS)
         {
            return node;
         }
         node = node.parentNode;
      }
      return null;
   }   
   
   /**
    * A listener on {@link ps.aa.Tree#onBeforeDomChange}.
    */
   this._onBeforeDomChange = function (id)
   {
      var ids = this.treeModel.getIdsFromNodeId(id);

      // unbind the context menu from the to be replaced DOM elements
      if(___sys_aamode != 1)
         ps.aa.Menu.unBindContextMenu(ids);
   }

   /**
    * A listener on {@link ps.aa.Tree#onDomChanged}.
    */
   this._onDomChanged = function (id)
   {
      var ids = this.treeModel.getIdsFromNodeId(id);
      
      // refresh all image icons to keep them insync with their checkout status
      for (var i=0; i<ids.count; i++)
         this.refreshImageForObject(ids.item(i));

      // bind the context menu from the to be replaced DOM elements
      if(___sys_aamode != 1)
         ps.aa.Menu.bindContextMenu(ids);
   }
   
   /**
    * Refreshes the opener window of Active Assembly window.
    * Does noting If the opener is none or closed.
    * Checks the opener url, if it has sys_cx then calls
    * refreshCxApplet method of browser.js file to refresh the selected nodes.
    * Otherwise reloads that opener window if the url has contentid equal to
    * the supplied contentId.
    * @param contentId, the content id of teh item which needs to be refreshed
    * in the window opener.   
    */
   this.refreshOpener = function(contentId)
   {
      var wopener = window.opener;
      if(wopener == null || wopener.closed)
         return;
      var wurl = wopener.location.href;
      if(wurl.indexOf("sys_cx")!=-1)
      {
         refreshCxApplet(wopener, "Selected", contentId, null);
      }
      else
      {
         var h = PSHref2Hash(wurl);
         if(h["sys_contentid"] == contentId)
            wopener.location.href = wopener.location.href;
      }
   }


   /**
    * Convenient method to handle the menu bar background change functions, while
    * opening the modal dialogs.
    * Initializes menu bar style. 
    * Changes the text color of the dialog boxes to black.
    * Overides the show and hide methods of ModalFloatingPane.
    */
   this._handleMenuBarBackGround = function()
   {
      var menubar = dojo.widget.byId("ps.aa.Menubar");
      this.menuBarStyle = menubar.domNode.style;
      var _this = this;
      //Overrides the show function of ModalFloatingPane, copies the original 
      //function and overrides it by calling disable menubar function
      //first and then calling the original show function. 
      dojo.widget.ModalFloatingPane.prototype.origShow =
            dojo.widget.ModalFloatingPane.prototype.show;
      dojo.widget.ModalFloatingPane.prototype.show = function() 
      {
         _this._toggleMenuBarBackGround(false);
         this.origShow();
      }      
      //Overrides the hide function of ModalFloatingPane, copies the original 
      //function and overrides it by calling enable menubar function
      //first and then calling the original hide function. 
      dojo.widget.ModalFloatingPane.prototype.origHide =
            dojo.widget.ModalFloatingPane.prototype.hide;
      dojo.widget.ModalFloatingPane.prototype.hide = function() 
      {
         _this._toggleMenuBarBackGround(true);
         this.origHide();
      }      
   }

   /**
    * Toggles the menu bar background from dull to bright and vice versa based 
    * on the supplied parameter.
    * @param brighten if true menubar back ground is set to bright color other
    * wise dull color.
    */
    this._toggleMenuBarBackGround = function(brighten)
    {
       if(this.menuBarStyle)
       {
          if(brighten)
             this.menuBarStyle.background = "#85aeec url(../sys_resources/images/aa/soriaBarBg.gif) repeat-x top left";
          else
             this.menuBarStyle.background = "#85aeec url(../sys_resources/images/aa/soriaBarBgDisabled.gif) repeat-x top left";
          
       }
    }

   /**
    * Opens the help window based on the supplied windowName.
    * Accepted values for windowName parameter are AAHelp, AATutorial and AAAbout.
    * For window name
    * AAHelp -- opens Active Assembly help window, if the window already exists
    * then sets the focus to that window. 
    * AATutorial -- opens Active Assembly tutorial window, if the window already exists
    * then sets the focus to that window.
    * AAAbout -- opens the Active Assembly About Dialog. 
    */
    this.openHelpWindow = function(windowName)
    {
       dojo.lang.assert((windowName === ps.aa.Menu.AAHELP
             || windowName === ps.aa.Menu.AATUTORIAL
             || windowName === ps.aa.Menu.AAABOUT),
             "Unexpected windowName " + windowName);
       if(windowName == ps.aa.Menu.AAHELP)
       {
          var hwin = window.open(this.helpUrl,windowName);
          hwin.focus();
       }
       else if(windowName == ps.aa.Menu.AATUTORIAL)
       {
          var hwin = window.open(this.helpTutorialUrl,windowName);
          hwin.focus();
       }
       else if(windowName == ps.aa.Menu.AAABOUT)
       {
          var dlg = ps.createDialog(
                {
                   id: "ps.Help.AboutDlg",
                   title: "About Rhythmyx Active Assembly",
                   href: this.helpAboutUrl
                }, "510px", "400px");
          dlg.show();
       }
       else
       {
          dojo.lang.assert(false, "Unhandled window " + windowName);
       }
    }
    
   /**
    * Enables or disables the conflict css stylesheets based on the supplied 
    * parameter. Gets the server property named AaConflictStyleSheets and
    * if exists the value is assumed to bea comma separated list of stylesheets.
    * Disables or enables all of them. 
    */
   this.enableConflictStyleSheets = function(enabled)
   {
      if(this._aaConflictStyleSheets == null)
      {
         var ss = ps.util.getServerProperty("AaConflictStyleSheets","");
         this._aaConflictStyleSheets = ss==""?[]:dojo.string.splitEscaped(ss,",");
      }
      for(var i=0;i<this._aaConflictStyleSheets.length;i++)
      {
         ps.util.enableStyleSheet(this._aaConflictStyleSheets[i],enabled);
      }
   }
   
   /**
    * The autoscrolling manager.
    */
   this.autoscroller = new ps.widget.Autoscroller();

   /**
    * Constant for alert message shown to the user when the item is not checked 
    * out by him.
    */
    this.CHECKOUT_MSG = "The item is not checked out to you, please check out the item to edit the field.";
    
    /**
    * Constant for alert message shown to the user when a field is already open 
    * for edit and user clicks on another field};
    */
    this.EDITOROPEN_MSG = "A window is already open for editing another field. Do you want to abort the changes to the open field and activate the field you have selected?";
    
    /**
    * Constant for alert message shown to the user when a snippet is already open 
    * for edit and user clicks on open menu on another snippet;
    */
    this.SNIPPETOPEN_MSG = "A window is already open for editing another " +
          "snippet. Do you want to replace it with current snippet?";
    
    /**
     * Window object for open snippet action window. Initialized in openSnippet
     * method. May be <code>null</code>
     */
    this.snippetOpenWindow = null;
     
    /**
    * Constant for alert message shown to the user when a field is already open 
    * for edit and user clicks on another field};
    */
    this.INPLACE_EDITOROPEN_MSG = "A field is already open for editing. Do you want to abort the changes to the open field and activate the field you have selected?";

    /**
     * An id for the select template dialog.
     */
    this._SELECT_TEMPLATE_DLG_ID = "selectTemplateDialog";
    
    /**
     * Constant for the target window name of the view actions
     */
    this.CE_WINDOW_NAME = "Ce_Window";
    
    /**
     * For editing and tool windows. Does not include width or height.
     */
    this.BASIC_WINDOW_STYLE = "toolbar=0,location=0,directories=0,status=0,menubar=0,scrollbars=1,resizable=1";

    /**
     * Constant for the target window name of the preview actions
     */
    this.PREVIEW_WINDOW_NAME = "Preview_Window";
    
    /**
     * Target style for preview windows
     */
    this.PREVIEW_WINDOW_STYLE = "toolbar=0,location=0,directories=0,status=1,menubar=0,scrollbars=1,resizable=1,width=800,height=700";
    
    /**
     * Image root diectory.
     */
    this.IMAGE_ROOT_PATH = "../sys_resources/images/aa";
    
    /**
     * Constant for content editor window 
     */
    this.CE_EDIT_ITEM_WINDOW = "PsAaEditItem";
    
    /**
     * Name of the cookie, indicating whether the tree pane is showing.
     */
    this._TREE_PANE_SHOWING_COOKIE = "treePaneShowing";    

   /**
    * Array variable to hold the style sheets that conflict with the AA.
    */
    this._aaConflictStyleSheets = null;
    
   /**
    * Urls for help actions
    */
    this.helpUrl = "../Docs/Rhythmyx/Active_Assembly_Interface/index.htm";
    this.helpTutorialUrl = "../Docs/Rhythmyx/Active_Assembly_Tutorial/index.htm";
    this.helpAboutUrl = "/Rhythmyx/ui/activeassembly/help/aboutaahelp.jsp";
};    

