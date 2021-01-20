/******************************************************************************
 *
 * [ ps.widget.Tree.js ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

/**
 * Comments from Dojo (dojo.widget.TreeV3):
 * Tree model does all the drawing, visual node management etc.
 * Throws events about clicks on it, so someone may catch them and process
 */

dojo.provide("ps.widget.Tree");

dojo.require("dojo.lang.assert");
dojo.require("dojo.widget.Manager");
dojo.require("dojo.widget.HtmlWidget");
dojo.require("dojo.widget.TreeNodeV3");
dojo.require("dojo.widget.TreeV3");

dojo.widget.defineWidget(
   "ps.widget.Tree",
   dojo.widget.TreeV3,
{
   /**
    * Selector helps activate the nodes (selects) them.
    */
   selector:"",
   
   /**
    * Tree Model.
    * @see ps.aa.Tree
    */
   model : null,
   
   /**
    * Flag to see if the tree is already loaded or not.
    */
   loaded : false,
   
   /**
    * Tree controller.
    */
   treeController : null,
   
   /**
    * Load custom css
    */
   templateCssPath: dojo.uri.moduleUri("ps", "widget/Tree.css"),

   /**   
    * Slot nodes, for which initialization is not completed yet.
    */
   delayedInitSlotNodes: [],
   
   /**
    * Item nodes, for which initialization is not completed yet.
    */
   delayedInitItemNodes: [],
   
   /**
    * Indicates whether browser supports Array.indexOf.
    */
   indexOfSupported: Array.indexOf,
   
   /** Creates a tree node from a model node.
    * 
    * @param {ps.aa.TreeNode} modelNode
    * 
    * @return {dojo.widget.TreeNodeV3}
    */
   createWidgetFromModelNode : function (modelNode) 
   {
      // dojo.debug("Creating widget node from model " + modelNode.toString());
      dojo.lang.assertType(modelNode, ps.aa.TreeNode);
      var title = modelNode.getLabel();
      var widgetId = modelNode.objId.getTreeNodeWidgetId();
      var widgetNode = dojo.widget.createWidget("TreeNodeV3", 
         {
            title: title, 
            tree: this.widgetId, 
            id: widgetId,
            modelId: modelNode.objId,
            tryLazyInit: true,
            isFolder: !modelNode.isLeafNode()
         });
      var _this = this;
      widgetNode.setChildren = function ()
      {
         // get model node again, because it could be changed already
         var n = _this.model.getNodeById(widgetNode.modelId);
         if (!n.isLeafNode())
         {
            for (var i = 0; i < n.childNodes.count; i++) {
               var childModel = n.childNodes.item(i);
               widgetNode.addChild(_this.createWidgetFromModelNode(childModel));
            }
         }
      }

      var oid = modelNode.objId;
      var noMove = !oid.isSnippetNode();
      var hasParentCanCheckout = modelNode.parentNode && 
            modelNode.parentNode.objId.isCheckoutByMe();
      var noAddChild = !(oid.isSlotNode() && hasParentCanCheckout) 
         || oid.isPageNode();
      var actionsDisabled = [];
      if (noAddChild) actionsDisabled.push("ADDCHILD");
      if (noMove) actionsDisabled.push("MOVE");
      widgetNode.actionsDisabled = actionsDisabled;

      //For some reason dojo does not set html id's to tree nodes
      widgetNode.domNode.setAttribute("id",widgetId);
      this._bindContextMenu(widgetNode);
      return widgetNode;
   },

   /**
    * Binds the context menu to a node widget.
    * 
    * @param {dojo.widget.TreeNodeV3}
    */
   _bindContextMenu : function(widget)
   {        
      var objId = widget.modelId;
      // it's possible the menus are not created yet during initialization
      if (objId.isSlotNode()) {
         if (ps.aa.Menu.slotCtxMenu)
         {
            ps.aa.Menu.slotCtxMenu.bindTargetNodes([widget.domNode]);
         }
         else
         {
            dojo.lang.assert(!this.indexOfSupported
                  || this.delayedInitSlotNodes.indexOf(widget.domNode) === -1,
                  "Slot node is registered more than once: " + widget.domNode);
            this.delayedInitSlotNodes.push(widget.domNode);
         }
      }
      else
      {
         if (ps.aa.Menu.itemCtxMenu)
         {
            ps.aa.Menu.itemCtxMenu.bindTargetNodes([widget.domNode]);
         }
         else
         {
            dojo.lang.assert(!this.indexOfSupported
                  || this.delayedInitItemNodes.indexOf(widget.domNode) === -1,
                  "Snippet node is registered more than once: " + widget.domNode);
            this.delayedInitItemNodes.push(widget.domNode);
         }
      }
   },

   /**
    * Unbinds the context menu from the node widget.
    * 
    * @param {dojo.widget.TreeNodeV3}
    */
   _unBindContextMenu : function(widget)
   {        
      var objId = widget.modelId;
      if (objId.isSlotNode()) {
         ps.aa.Menu.slotCtxMenu.unBindTargetNodes([widget.domNode]);
      }
      else {
         ps.aa.Menu.itemCtxMenu.unBindTargetNodes([widget.domNode]);  
      }     
   },
   
   /**
    * Loads the tree by creating node widgets based on the model.
    * 
    * @param {ps.aa.Tree} model
    */
   loadFromModel : function (model) 
   {
      this.treeController = dojo.widget.manager.getWidgetById("treeController");
      this.actionsDisabled.push('ADDCHILD');
      this.model = model;
      
      this._loadModel();
   },
   
   /**
    * Finishes functionality of {@link loadModel}, which can be executed later
    * and requires other UI subsystems to be initialized.
    * @param {ps.aa.Tree} model the tree model to use. Not null.
    */
   loadFromModelAsynch : function (model)
   {
      dojo.lang.assertType(model, ps.aa.Tree);
      if (this.delayedInitSlotNodes.length)
      {
         ps.aa.Menu.slotCtxMenu.bindTargetNodes(this.delayedInitSlotNodes);
      }
      if (this.delayedInitItemNodes.length)
      {
         ps.aa.Menu.itemCtxMenu.bindTargetNodes(this.delayedInitItemNodes);
      }
   },
   
   /**
    * Reloads the model.
    */
   _loadModel: function () 
   {
      if (this.loaded)
      {
         dojo.lang.assert(this.children);
         dojo.lang.assertType(this.children,Array);
         if (this.children.length > 0)
         {
            var child = this.children[0];
            dojo.lang.assertType(child, dojo.widget.TreeNodeV3);
            this.removeChild(child);
            child.destroy();
         }
         var treeDnd = dojo.widget.manager.getWidgetById("treeDndController");
         if (treeDnd)
         {
            treeDnd.reset();
         }
      }

      var rootModelNode = this.model.getRootNode();
      var rootWidgetNode = this.createWidgetFromModelNode(rootModelNode);
      this.addChild(rootWidgetNode);

      this.treeController.expandToLevel(this, this.expandLevel);
      this.loaded = true;
   },
   
   /**
    * Initializes the tree Drag-and-Drop functionality.
    */
   dndInit: function ()
   {
      if (dojo.widget.manager.getWidgetById("treeDndController"))
      {
         return;
      }

      var dndController = dojo.widget.createWidget("ps:TreeDndController", {
         id: "treeDndController",
         controller: "treeController"
      });
      dndController.listenTree(this);
   },
 
    /** 
    * Updates a tree node from a model node.
    * 
    * @param {ps.aa.TreeNode} modelNode
    * @param {dojo.widget.TreeNodeV3} treeNodeWidget
    * 
    * @return {dojo.widget.TreeNodeV3}
    */
   _updateWidgetFromModelNode : function (modelNode,parentWidget) 
   {

      //dojo.debug("Trying to update an existing tree " +
      //      "node widget with modelNode: " + modelNode.toString());
      
      var childWidget = this.getWidgetFromModelNode(modelNode);
      if (childWidget) {
         //dojo.debug("sync: Found an existing widget that " +
         //      "matchs this modelNode: " + modelNode.toString());
         if (!parentWidget) 
         {
            //dojo.debug("parentWidget is null so this must be the root node.");
         }
         else if (parentWidget == childWidget.parent) 
         {
            //dojo.debug("This child node had the same parent as before (OK).");
            childWidget.doDetach();
         }
         else 
         {
            /*
             * detach the child from the old parent and attach it to the
             * new parent.
             */
            //dojo.debug("Detaching child widget from old parent.");
            childWidget.doDetach();
            // The caller of this method will add the node to the parent.
         }
         
         if (childWidget.title != modelNode.getLabel()) 
         {
            //dojo.debug("Title changed from " + childWidget.title 
            // + " to " + modelNode.getLabel());
            childWidget.setTitle(modelNode.getLabel());
         }
         childWidget.modelId = modelNode.objId;
      }
      else /* did not find widget for model */ 
      {
         //dojo.debug("Did not find widget corresponding to model node");
         childWidget = this.createWidgetFromModelNode(modelNode);
      }
      return childWidget;
   },  

   /**
    * Removes the tree nodes that are no longer exist in the tree model.
    */   
   _cleanTree : function () 
   {
      //dojo.debug("Tree - Cleaning Tree - Start");

      var deadNodes = [];
      var root = this.children[0];
      var stack = [root];
      while (wNode = stack.pop()) {
         var mNode = this.model.getNodeById(wNode.modelId);
         if (mNode) 
         {
            //dojo.debug("Tree has node " + mNode.toString());
            for (var i = 0; i < wNode.children.length; i++) 
            {
               stack.push(wNode.children[i]);
            }
         }
         else 
         {
            //dojo.debug("Node is dead: " + wNode.toString());
            deadNodes.push(wNode);
         }
      }
      
      // removes all invalid nodes and its decendents
      for (var i = 0; i < deadNodes.length; i++) 
      {
         this._removeNodes(deadNodes[i]);
      }
      //dojo.debug("Tree - Cleaning Tree - End");
   },

   /**
    * Removes a node and its decendent nodes from the tree.
    * 
    * @param {dojo.widget.TreeNodeV3} node The to be removed node, which may 
    *    contain child node.
    */
   _removeNodes : function (node) 
   {
      //dojo.debug("Tree - Removing NODES: " + node.modelId.toString() + ", len=" + node.children.length);
      dojo.lang.assert(node, "Can't remove null node.");   

      // removes the child nodes first if any      
      while (node.children.length > 0) 
      {
         this._removeNodes(node.children[0]);
      }

      this._removeNode(node);
   },
      
   
   /**
    * Removes a node from the tree.
    * 
    * @param {ojo.widget.TreeNodeV3} node The to be removed node, which may not
    *    contain any child node.
    */
   _removeNode : function (node) 
   {
      //dojo.debug("Tree - Removing node: " + node.modelId.toString());
      
      dojo.lang.assert(node, "Can't remove null node.");   
      dojo.lang.assert(!node.children.length,
            "Can't remove a node with children.");   

      this._unBindContextMenu(node);
      node.destroy();
   },
      
   /**
    * Reloads the model.
    */
   _synchModel: function () 
   {
      //dojo.debug("Tree - Synchronizing Tree to Model - Start");
      
      // Clean Widgets that are no longer in the tree.
      this._cleanTree();
      if (this.children.length == 0)
      {
         this._loadModel();
         return;
      }
      
      var rootModelNode = this.model.getRootNode();
      var rootWidgetNode = this._updateWidgetFromModelNode(rootModelNode,null);
      var child = this.children[0];
      
      //dojo.debug("original root = " + child);
      //dojo.debug("new root = " + rootWidgetNode);
      
      dojo.lang.assert(child == rootWidgetNode, 
            "The root widget node should not have changed.");

      var modelAndWidget = {model:rootModelNode, widget:rootWidgetNode};
      var stack = [modelAndWidget];
      while (mw = stack.pop()) 
      {
         var w = mw.widget;
         var m = mw.model;
         if (!m.isLeafNode() && !w.tryLazyInit) 
         {            
            //dojo.debug("Number of children: " + m.childNodes.count);
            for (var i = 0; i < m.childNodes.count; i++) {
               var childModel = m.childNodes.item(i);
               var childWidget = this._updateWidgetFromModelNode(childModel,w);
               stack.push({model:childModel,widget:childWidget});
               w.addChild(childWidget,i,false);
            }
         }
         else 
         {
            //dojo.debug("Tree - leaf node, no children");
         }
         this._updateIsFolderFromModel(w, m);
      }
      this.loaded = true;
      var treeDnd = dojo.widget.manager.getWidgetById("treeDndController");
      if (treeDnd)
      {
         treeDnd.reset();
      }

      //dojo.debug("Tree - Synchronizing Tree to Model - End");
   },

    /** 
     * Insures that tree node widget folder indicator value corresponds to
     * the model.
     * 
     * @param {dojo.widget.TreeNodeV3} nodeWidget the node widget to set folder
     * status value for.
     * Assumed not null.
     * @param {ps.aa.TreeNode} modelNode the corresponding model.
     * Assumed not null.
     */
   _updateIsFolderFromModel : function (nodeWidget, modelNode) 
   {
      if (nodeWidget.tryLazyInit)
      {
         if (!modelNode.isLeafNode() !== nodeWidget.isFolder)
         {
            // model and lazy node are out of sync
            if (modelNode.isLeafNode())
            {
               nodeWidget.unsetFolder();
            }
            else
            {
               nodeWidget.setFolder();
            }
         }
      }
      else
      {
         // ignore non-lazy nodes
      }
   },

   /**
    * Get tree node widget from a tree node model id.
    * 
    * @param {ps.aa.ObjectId} objId
    */
   getWidgetFromModelId : function (objId) 
   {
       var widgetId = objId.getTreeNodeWidgetId();
       var widget = dojo.widget.manager.getWidgetById(widgetId);
       return widget;
   },
    
   /**
    * Get widget from model node.
    * @param {ps.aa.TreeNode} treeNode
    */
   getWidgetFromModelNode : function (treeNode) 
   {
       return this.getWidgetFromModelId(treeNode.objId);
   },
    
   /**
    * Selects the widget node give the corresponding model node.
    * @param {ps.aa.ObjectId} treeNodeId
    */
   activate : function (treeNodeId) 
   {
      var _this = this;
      // expand from top to trigger lazy node loading
      function expandTo(n)
      {
         dojo.lang.assert(n, "Tree model node is expected to be not null.");
         if (n.parentNode)
         {
            expandTo(n.parentNode);

            var w = _this.getWidgetFromModelId(n.parentNode.objId);
            dojo.lang.assert(w, "Can't find a widget for  " + n.parentNode);
            w.expand();
         }
      }

      var treeNode = this.model.getNodeById(treeNodeId);
      expandTo(treeNode);
      var widget = this.getWidgetFromModelId(treeNode.objId);
      var selector = dojo.widget.manager.getWidgetById(this.selector);
      selector.deselectAll();
      selector.select(widget);
   },

   /**
    * doMove is used to move snippets within a slot or to a new slot.
    * @see {dojo.widget.TreeV3#doMove}
    * @Override dojo.widget.TreeV3#doMove
    */
	doMove: function(child, newParent, index) 
	{
		//dojo.debug("Tree move: "+child+" to "+newParent+" at "+index);
      //ps.aa.SnippetMove = function (snippetId, slotId, targetSlotId, targetIndex,
      // dontUpdatePage)
      
      dojo.lang.assert(child.modelId, "Node being moved does not have a model");
      dojo.lang.assert(newParent.modelId, "Node being moved does not have a model");
      var pid = newParent.modelId;
      var cid = child.modelId;
      dojo.lang.assert(cid.isSnippetNode(), "child is not a snippet.");
      var childModelNode = this.model.getNodeById(child.modelId);
      var parentModelNode = childModelNode.parentNode;
      dojo.lang.assert(parentModelNode, "Unable to get the parent model node.");
      var sid = parentModelNode.objId;
      dojo.lang.assert(sid.isSlotNode(), "Original parent of child is not a slot.");
      dojo.lang.assert(pid.isSlotNode(), "New parent is not a slot.");
      var targetSlotId = pid;
      var snippetId = cid;
      var slotId = sid;
      
      var move = new ps.aa.SnippetMove(snippetId, slotId, targetSlotId, 
         (index + 1), false);
      var success = ps.aa.controller.moveToSlot(move);
      
      
		//var parent = child.parent;
		if (success == true) 
		{
         //dojo.debug("Tree - successful move.");
		   var snipid = move.getTargetSnippetId();
		   try
		   {
		      // could fail if the target slot requires a template change
		      // and the template selection dialog is called
            ps.aa.controller.activate(snipid);
		   }
		   catch (e) {
		      dojo.debug("Ignore on a template change request")
		      dojo.debug(e);
		   }
		   //We don't need resync the tree because the controller will.
		}
		else
		{
         dojo.debug("Tree - move failed.");
		}
	},
	
   /**
    * A listener for {@link ps.aa.Tree#onModelChanged}.
    * @see ps.aa.Tree
    */
   onModelChanged : function ()
   {
      //dojo.debug("tree on model change called.");

      if (this.loaded) {
         this._synchModel();
      }
      else {
         this._loadModel();
      }

      //dojo.debug("tree on model change SUCCESSFUL");
	 }
});
