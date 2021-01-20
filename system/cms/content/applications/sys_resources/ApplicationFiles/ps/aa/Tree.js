/******************************************************************************
 *
 * [ ps.aa.Tree.js ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

dojo.provide("ps.aa.Tree");

dojo.require("dojo.lang.assert");
dojo.require("dojo.lang.type");
dojo.require("dojo.dom");
dojo.require("dojo.collections.ArrayList");

dojo.require("ps.aa");


/**
 * <p>The tree model contains the id of all managed DOM nodes in the page panel. 
 * This tree model contains by a root node and multiple child nodes. 
 * Each node is a {ps.aa.TreeNode) object.
 * </p>
 * <p>If the page panel is rendered with a page template, then the root 
 * represents the local template if the page uses a global template otherwise it 
 * is the page node as a whole. The 1st level of child nodes are "Field" nodes, 
 * which are followed by "Slot" nodes. The "Field" node is the leaf node, but 
 * the "Slot" node may contains a list of "Snippet" nodes. The "Snippet" node 
 * contains a list of "Field" nodes and possibly a list of "Slot" nodes and so 
 * on.
 * </p>
 * @constructor
 */
ps.aa.Tree = function()
{
   //ps.aa.TreeNode
   this.root = null;
   
   /**
    * Populate the data model from the DOM element with the id of 'MainBody'. 
    * This must be  called before calling any other methods of this class.
    */
   this.init = function()
   {
      var startElement = dojo.byId("ps.aa.ContentPane");
      dojo.lang.assert(startElement != null, 
         "Cannot find DOM element id='ps.aa.ContentPane'");
      this._createNodes(startElement, null);
      this._sort(this.root);
   }
   
   /**
    * Sort the tree based on the rules below:
    * 1. Field nodes appear first under the parent alpha ordered.
    * 2. Then appean slot nodes and alpha ordered.
    * 3. Snippets are not affected by this means they preserve the system sort 
    *    order.
    */
   this._sort = function(treeNode)
   {
      if(treeNode != null && !treeNode.isLeafNode())
      {
         treeNode.childNodes.sort(function(o1, o2){

            if((o1.objId.isFieldNode() && o2.objId.isSlotNode()))
            {
                return -1;  
            }           
            if((o1.objId.isSlotNode() && o2.objId.isFieldNode()))
            {
                return 1;  
            }           
            if((o1.objId.isFieldNode() && o2.objId.isFieldNode()) ||            
               (o1.objId.isSlotNode() && o2.objId.isSlotNode()))
            {
               var s1 = o1.getLabel().toLowerCase();
               var s2 = o2.getLabel().toLowerCase();
               if (s1 > s2)
               {
                  return 1;
               }
               else if (s1 < s2)
               {
                  return -1;
               }
            }
            if(o1.objId.isSnippetNode() && o2.objId.isSnippetNode())
            {
               var s1 = parseInt(o1.objId.getSortRank());
               var s2 = parseInt(o2.objId.getSortRank());
               if (s1 > s2)
               {
                  return 1;
               }
               else if (s1 < s2)
               {
                  return -1;
               }
            }
            return 0;
            
         });
         dojo.lang.forEach(treeNode.childNodes.toArray(), this._sort, this);
      }
   }
   /**
    * Creates tree nodes from the specified DOM element.
    * 
    * @param {HTMLDivElement} startElement The parent DOM element which 
    *    contains all child nodes. Assumed not null.
    * @param {ps.aa.TreeNode} pnode the last parent node, which may be null.
    */
   this._createNodes = function(startElement, pnode)
   {
      // the getElementsByTagName function returns the elements in the result 
      // of a "Preorder Traversal" (or first depth-first traversal) operation.
      var divs = startElement.getElementsByTagName("div");
      var div = null;
      var lastParent = pnode;
      for(var i=0; i<divs.length; i++)
      {
         div = divs[i];
         if (div.className == ps.aa.PAGE_CLASS)
         {
            lastParent = this._addPageNode(div);
         }
         else if (div.className == ps.aa.FIELD_CLASS)
         {
            lastParent = this._addFieldNode(div, lastParent);
         }
         else if (div.className == ps.aa.SNIPPET_CLASS)
         {
            lastParent = this._addSnippetNode(div, lastParent);
         }
         else if (div.className == ps.aa.SLOT_CLASS)
         {
            lastParent = this._addSlotNode(div, lastParent, null);
         }
      }      
   }
   
   /**
    * Resets the child nodes of the node, associated with the specified 
    * DOM element. Fails, if the tree node cannot be found.
    * 
    * @param {HTMLDivElement} element The DOM element, that contains the id of
    *    an existing tree node. Not <code>null</code>.
    */
   this._resetChildNodes = function (element)
   {
      dojo.lang.assert(element);
      var id = ps.aa.Page.getObjectId(element);
      dojo.lang.assert(id, "Cannot find object id");
         
      var node = this.getNodeById(id);
      dojo.lang.assert(node, "Cannot find node");
         
      node.removeChildNodes();
      this._createNodes(element, node);
      this._sort(node);
   }

   /**
    * Should be called before any dom node is removed, or its structure is
    * changed.
    * Calls {@link #onBeforeDomChange} event.
    * @param {ps.aa.ObjectId} id the id of the node which will change.
    * Not <code>null</code>.
    * @see #onBeforeDomChange
    * @see #fireDomChanged
    */   
   this.fireBeforeDomChange = function (id)
   {
      dojo.lang.assertType(id, ps.aa.ObjectId);

      this.onBeforeDomChange(id);
   }
   
   /**
    * The event which is fired from {@link #fireBeforeDomChange}.
    * Put a listener on this event to process the dom nodes which are going
    * to be disposed.
    * @see #fireBeforeDomChange
    * @see #onDomChanged
    */
   this.onBeforeDomChange = function (id)
   {
      dojo.lang.assertType(id, ps.aa.ObjectId);
   }

   /**
    * Should be called after a page dom node is changed or removed,
    * or the dom tree structure is changed.
    * Calls {@link #onAfterDomChanged}.
    * @param {ps.aa.ObjectId} notChangedId the id node which is not changed.
    * Can be this node, or parent node if this node is changed.
    * Is used as a starting point for the tree model refresh.
    * Can be <code>null</code> if the full page is changed.
    * @param {ps.aa.ObjectId} id the id of the node which will change.
    * Can be <code>null</code>, if the root is changed.
    * @see #onDomChanged
    * @see #fireBeforeDomChange
    */
   this.fireDomChanged = function (notChangedId, id)
   {
      id && dojo.lang.assertType(id, ps.aa.ObjectId);
      notChangedId && dojo.lang.assert(notChangedId, ps.aa.ObjectId);

      if (id && id.equals(this.root.objId))
      {
         // root node can be reset
         id = null;
      }

      if (notChangedId)
      {
         var element = ps.aa.Page.getElement(notChangedId);
         this._resetChildNodes(element);
      }
      else
      {
         // reset whole model
         this.root.clear();
         this.root = null;
         this.init();
      }

      if (!id)
      {
         id = this.root.objId;
      }
      this.onDomChanged(id);
   }

   /**
    * The event which is fired from {@link #fireDomChanged}.
    * Put a listener on this event to process the newly appended dom nodes.
    * @see #fireDomChanged
    * @see #onBeforeDomChange
    */
   this.onDomChanged = function (id)
   {
      dojo.lang.assertType(id, ps.aa.ObjectId);
   }

   /**
    * Adds the specified PAGE div element to the tree as the root. It is an 
    * error attempting to add more than once.
    * 
    * @param {HTMLDivElement} div The element contains the added object id.
    * 
    * @return the added page node if successful; otherwise return null.
    */
   this._addPageNode = function (div)
   {
      if (this.root != null)
      {
         ps.util.error("Unknown page node, already got a root: " 
            + this.root.objId.toString());
         return null;
      }
      
      return this.root = this._getNodeRqd(div);
   }
   
   /**
    * Adds the specified SLOT div element to the tree.
    * 
    * @param {HTMLDivElement} div The element contains the added object id.
    * @param {ps.aa.TreeNode} lastParent The last (possible) parent node,
    *    which can be a page, a slot or a snippet node. It may be null.
    * @param {ps.aa.TreeNode} slotNode The slot node, which is used to collect
    *    all its child nodes if specified. It may be null.
    * 
    * @return the added slot node if successful; otherwise return null.
    */
   this._addSlotNode = function(div, lastParent, slotNode)
   {
      var node = slotNode;
      if (node == null)
         node = this._getNodeRqd(div);

      var parentNode = this._getParentPageSnippet(node, lastParent);
      dojo.lang.assert(parentNode != null, 
         "Cannot find parent node for slot node: " + node.toString());

      parentNode.addChildNode(node);
      return node;
   }
   
   /**
    * Gets the parent page or snippet node for the specified child node.
    * 
    * @param {ps.aa.TreeNode} childNode The specified child node.
    * @param {ps.aa.TreeNode} lastParent The last (possible) parent node,
    *    which may be null. If this is not the parent node, then it is used to 
    *    traverse the tree in reverse (or bottom up) order.
    * 
    * @return The parent page/snippet node, its content id is the same as the 
    *    childNode node. It is null if cannot find the parent page/snippet node.
    */
   this._getParentPageSnippet = function(childNode, lastParent)
   {
      var parentNode = lastParent;
      
      while (parentNode)
      {
         if ((parentNode.isPageNode() || parentNode.isSnippetNode()) && 
             parentNode.objId.getContentId() == childNode.objId.getContentId())
         {
            return parentNode;
         }
         else
         {
            parentNode = parentNode.parentNode;
         }
      }
      
      return null;
   }

   /**
    * Adds the specified SNIPPET div element to the tree.
    * 
    * @param {HTMLDivElement} div The element contains the added object id.
    * @param {ps.aa.TreeNode} lastParent The last (possible) parent node,
    *    which can be a page, a slot or a snippet node. It may be null.
    * 
    * @return the added snippet node if successful; otherwise return null.
    */
   this._addSnippetNode = function(div, lastParent)
   {
      var node = this._getNodeRqd(div);
      if (this.root == null)
      {
         this.root = node;
         return this.root;
      }
      else
      {
         var parentNode = this._getParentSlot(node, lastParent);
         if (parentNode != null)
         {
            parentNode.addChildNode(node);
            return node;
         }
      }
      
      ps.util.error("Cannot find parent node for snippet node: " + node.toString());
      return null;
   }   
   
   /**
    * Gets the parent slot for the specified snippet node.
    * 
    * @param {ps.aa.TreeNode} snippetNode The specified snippet node.
    * @param {ps.aa.TreeNode} lastParent The last (possible) parent node,
    *    which can be a page, a slot or a snippet node. It may be null.
    *    This is used to traverse the tree in reverse (or bottom up) order.
    * 
    * @return The parent slot node, its slot id is the same as the snippet node
    *    and the content id of the slot is the parent id of the snippet.
    *    It is null if cannot find the parent slot node.
    */
   this._getParentSlot = function(snippetNode, lastParent)
   {
      var parentNode = lastParent;
      
      // find the slot whose content id is the parent id of the snippet and
      // the slot id of both node are the same.
      while (parentNode)
      {
         if (parentNode.isSlotNode() && 
             parentNode.objId.getSlotId() == snippetNode.objId.getSlotId() &&
             parentNode.objId.getContentId() == snippetNode.objId.getParentId())
         {
            return parentNode;
         }
         else
         {
            parentNode = parentNode.parentNode;
         }
      }
      
      return null;
   }

   /**
    * Adds the specified FIELD div element to the tree.
    * 
    * @param {HTMLDivElement} div The element contains the added object id.
    * @param {ps.aa.TreeNode} lastParent The last (possible) parent node,
    *    which can be a page, a slot or a snippet node. It may be null.
    */
   this._addFieldNode = function(div, lastParent)
   {
      var node = this._getNodeRqd(div);
      var parentNode = this._getParentPageSnippet(node, lastParent);
      dojo.lang.assert(parentNode != null, 
         "Cannot find parent node for field node: " + node.toString());

      parentNode.addChildNode(node);
      return parentNode;
   }   

   /**
    * Gets the object id from the specified HTML div element. It prompt
    * error message if fail to get the object id.
    * 
    * @param {HTMLDivElement} div The div element.
    * 
    * @return {ps.aa.ObjectId} the object id of the element.
    */
   this._getNodeRqd = function(div)
   {
      var objId = ps.aa.Page.getObjectId(div);
      dojo.lang.assert(objId != null, 
         "Malformed objectId for a node of class=" + div.className);

      return new ps.aa.TreeNode(objId);
   }
   
   /**
    * Gets the root of the tree.
    *
    * @return {ps.aa.TreeNode} object, never null.
    */
   this.getRootNode = function()
   {
      return this.root;
   }

   /**
    * Gets a specified node.
    *
    * @param {ps.aa.ObjectId} id The id of the specified node.
    *
    * @return {ps.aa.TreeNode} object. It may be null if cannot find the node.
    */
   this.getNodeById = function(id)
   {
      dojo.lang.assertType(id, ps.aa.ObjectId);
      return this._getNodeById(id, this.root);
   }

   /**
    * Search for the specified node from the given parent node.
    *
    * @param {ps.aa.ObjectId} id The id of the specified node.
    * @param {ps.aa.TreeNode} pnode The parent node to search from.
    *
    * @return {ps.aa.TreeNode} object. It may be null if cannot find the node.
    */
   this._getNodeById = function(id, pnode)
   {
      dojo.lang.assertType(id, ps.aa.ObjectId);
      pnode && dojo.lang.assertType(pnode, ps.aa.TreeNode);

      if (pnode.equals(id))
      {
         return pnode;
      }

      if (!pnode.isLeafNode())
      {
         var node = null;
         for (var i=0; i<pnode.childNodes.count; i++)
         {
            node = this._getNodeById(id, pnode.childNodes.item(i));
            if (node != null)
               return node;
         }
      }
      
      return null;
   }
   
   /**
    * Gets all object ids of the nodes that satisfy the following:
    * 
    * If the field name is specified, then gets the object ids from the field
    * nodes, where the content id and field name of the node equal to the
    * specified parameters.
    * 
    * If the field name is not specified, then gets the nodes described below:
    * (1) The immidiate (child) field nodes of the root, where the content id 
    *     of the node equals the specified content id.
    * (2) The snippet nodes whose content id equals to the specified content id.
    *
    * @param {int} contentId The content id of the searched nodes. This is a 
    *    required parameter.
    * @param {String} fieldName The field name of the searched nodes. This is
    *    an optional parameter. 
    *
    * @return a list {dojo.collections.ArrayList} of {ps.aa.ObjectId} objects. 
    *    Never null, but may be empty.
    */
   this.getIdsFromContentId = function(contentId, fieldName)
   {
      if (this.root.isLeafNode())
         return result;

      var result = null;
      if (dojo.lang.isString(fieldName))
      {
         result = new dojo.collections.ArrayList();
         this._getFieldIdsByContentIdName(contentId, fieldName, 
            this.root, result);
      }
      else
      {
         result = this._getIdsOfTopLevelFieldNode(contentId);
         this._getSnippetIdsFromContentId(contentId, this.root, result);
      }
      
      return result;
   }

   /**
    * Gets all object ids whose content id equals the specified content id.
    * 
    * @param contentId The content id of the returned object id. It may be
    *    null if wants to get all object ids regardless the content id property.
    * 
    * @return {dojo.collections.ArrayList} a list of {ps.aa.ObjectId} ids 
    *    described above. Never null, but may be empty.
    */
   this.getAllIdsByContentId = function(contentId)
   { 
      var result = new dojo.collections.ArrayList();
      if (this.root == null)
         return result;

      this._getAllIdsByContentId(contentId, this.root, result);
      
      return result;
   }

   /**
    * Gets the object ids from the given node and its descendants.
    * 
    * @param {ps.aa.ObjectId} objId The object id of the specified node.
    * 
    * @return {dojo.collections.ArrayList} a list of ids described above. 
    *    Never null, but may be empty.
    */
   this.getIdsFromNodeId = function (objId)
   {
      dojo.lang.assertType(objId, ps.aa.ObjectId);
      var ids = new dojo.collections.ArrayList();
      var node = this.getNodeById(objId);
      if (node != null)
         this._getIdsFromNode(node, ids);
         
      return ids;
   }

   /**
    * Gets the object ids from the given node and its descendants.
    * 
    * @param {ps.aa.TreeNode} objId The object id of the specified node.
    * @param {dojo.collections.ArrayList} ids The list of ids described above,
    *    which is used to collects all ids.
    */
   this._getIdsFromNode = function(node, ids)
   {
      if (node == null)
         return;
         
      ids.add(node.objId);
      if (!node.isLeafNode())
      {
         for (var i=0; i<node.childNodes.count; i++)
           this._getIdsFromNode(node.childNodes.item(i), ids);
      }
   }

   /**
    * Gets all object ids of the nodes, whose content id equals the specified 
    * content id.
    *
    * @param {int} contentId The content id of the searched nodes. This is a 
    *    required parameter. It may be null if wants to get all object ids 
    *    regardless the content id property.
    * @param {ps.aa.TreeNode} node The node which is searched from. 
    *    This is a  required parameter.
    * @param {dojo.collections.ArrayList} result The variable that collects
    *    all qualified snippet nodes. This is a required parameter.
    */
   this._getAllIdsByContentId = function(contentId, node, result)
   {
      if (contentId == null)
        result.add(node.objId);
      if (node.objId.getContentId() == contentId)
        result.add(node.objId);
      
      if (node.isLeafNode())
         return;
      
      for (var i=0; i<node.childNodes.count; i++)
      {
         this._getAllIdsByContentId(contentId, node.childNodes.item(i), result);
      }
   }         
   
   /**
    * Gets all object ids of the field nodes, whose content id and field name
    * equals the specified parameters.
    *
    * @param {int} contentId The content id of the searched nodes. This is a 
    *    required parameter.
    * @param {String} fieldName The field name of the searched nodes. This is
    *    a required parameter. 
    * @param {ps.aa.TreeNode} pnoade The parent node that the search from. 
    *    This is a  required parameter.
    * @param {dojo.collections.ArrayList} result The variable that collects
    *    all qualified snippet nodes. This is a required parameter.
    */
   this._getFieldIdsByContentIdName = function(contentId, fieldName, pnode, 
      result)
   {
      if (pnode.isLeafNode())
        return;
        
      for (var i=0; i<pnode.childNodes.count; i++)
      {
         var node = pnode.childNodes.item(i);
         if (node.isFieldNode() 
             && node.objId.getContentId() == contentId
             && node.objId.getFieldName() == fieldName)
         {
            result.add(node.objId);
         }
         else if (!node.isLeafNode())
         {
            this._getFieldIdsByContentIdName(contentId, fieldName, node, 
               result);
         }
      }
   }   
      

   /**
    * Gets the object ids from the immidiate (child) field nodes of the root, 
    * where the content id of the node equals the specified content id.
    * 
    * @param {int} contentId The content id of the searched nodes. This is a 
    *    required parameter.
    *
    * @return a list {dojo.collections.ArrayList} of {ps.aa.ObjectId} objects. 
    *    Never null, but may be empty.
    */
   this._getIdsOfTopLevelFieldNode = function(contentId)
   {
      var result = new dojo.collections.ArrayList();
      for (var i=0; i<this.root.childNodes.count; i++)
      {
         var node = this.root.childNodes.item(i);
         if (node.isFieldNode() && node.objId.getContentId() == contentId)
         {
            result.add(node.objId);
         }
      }
      return result;      
   }   

   /**
    * Gets all object ids of the nodes that satisfy the following:
    * (1) The content id of the object id equals the specified content id,
    * (2) The snippet field nodes. 
    *
    * @param {int} contentId The content id of the searched nodes. This is a 
    *    required parameter.
    * @param {ps.aa.TreeNode} pnoade The parent node that the search from. 
    *    This is a  required parameter.
    * @param {dojo.collections.ArrayList} result The variable that collects
    *    all qualified snippet nodes. This is a required parameter.
    */
   this._getSnippetIdsFromContentId = function(contentId, pnode, result)
   {
      for (var i=0; i<pnode.childNodes.count; i++)
      {
         var node = pnode.childNodes.item(i);
         if (node.isSnippetNode() && node.objId.getContentId() == contentId)
         {
            result.add(node.objId);
         }
         else if (!node.isLeafNode())
         {
            this._getSnippetIdsFromContentId(contentId, node, result);
         }
      }
   }   
      
   /**
    * Removes the specified node and all its child nodes. Do nothing if cannot
    * find the node with the specified id.
    *
    * @param {ps.aa.ObjectId} id The id of the to be removed node.
    *
    * @return true if successfully removed the specified node; false otherwise.
    */
   this.removeNode = function(id)
   {
      var node = this.getNodeById(id);
      if (node == null)
         return false;
         
      var pnode = node.parentNode;
      if (pnode != null)
         pnode.childNodes.remove(node);
         
      node.clear();
      return true;
   }
   
   /**
    * Gets the next sibling node if there is one. If there is no next sibling
    * node, then gets the previous sibling node; otherwise, gets the parent node.
    * 
    * @return the id of the node described above. It may be null none of the 
    *    above exists.
    */
   this.getNextSiblingId = function(id)
   {
      var node = this.getNodeById(id);
      dojo.lang.assert(node, "Cannot find node id=" + id.serialize());

      var pnode = node.parentNode;
      if (pnode == null)
         return null;
         
      var len = pnode.childNodes.count;
      var index = pnode.childNodes.indexOf(node);
      var node = null;
      if (index < (len-1))
      {
         node = pnode.childNodes.item(index+1); // next sibling
      }
      else if (index > 0)
      {
         node = pnode.childNodes.item(index-1); // previous sibling
      }
      else
      {
         node = pnode; // there is no sibling, return the parent node.
      }
      return node.objId;
   }
   
   /**
    * Converts a node (if specified) or the whole tree to text.
    * 
    * @param {ps.aa.TreeNode} node The to be converted node if specified; 
    *    otherwise, converts the whole tree into text.
    * 
    * @return serialized string of the node or the whole tree.
    */   
   this.toString = function(node)
   {
      if (dojo.lang.isUndefined(node))
      {
         return this.toString(this.root);
      }
      else
      {
         var text = node.toString() + "\n";
         if (node.isLeafNode())
            return text;
            
         for (var i=0; i<node.childNodes.count; i++)
         {
            var cnode = node.childNodes.item(i);
            text += "   child[" + i + "]: " + this.toString(cnode) + "\n";
         }
         return text;
      }
   }   
};


/**
 * This is a node that contains in the {ps.aa.Tree} object.
 *
 * @param (ps.aa.ObjectId} objectId  The id of this node, never null.
 * @param (ps.aa.TreeNode} parentNode  The id of the parent node, it may be null 
 *    for a root node; otherwise it may not be null.
 * @param {dojo.collections.ArrayList} childNodes  The child nodes, never null, 
 *    may be empty.
 * @constructor
 */
ps.aa.TreeNode = function (objectId, pNode, childNodes)
{
   dojo.lang.assertType(objectId, ps.aa.ObjectId);
   pNode && dojo.lang.assertType(pNode, ps.aa.TreeNode);
   
   /**
    * The object id of this node (ps.aa.ObjectId}.
    */
   this.objId = objectId;

   /**
    * The object id of this node (ps.aa.TreeNode}.
    */
   if (dojo.lang.isUndefined(pNode))
      this.parentNode = null;
   else
      this.parentNode = pNode;

   /**
    * The immidiate child nodes. {dojo.collections.ArrayList}.
    */
   if (dojo.lang.isUndefined(childNodes))
      this.childNodes = null;
   else
      this.childNodes = childNodes;
   
   /**
    * The label of the node.
    */
   this.nodeLabel = null;
   
   /**
    * @return true if this node does not have any child nodes; otherwise false.
    */
   this.isLeafNode = function()
   {
      return this.childNodes == null || this.childNodes.count == 0;
   }

   /**
    * @return the label of the node.
    */
   this.getLabel = function()
   {
      if (this.nodeLabel == null)
      {
         if (this.isFieldNode())
         {
            // Get field label but remove any trailing colon (:)
            this.nodeLabel = this.objId.getFieldLabel().replace(/\:$/g, "");
         }
         else
         {
            var id = this.objId.serialize();
            var divElem = dojo.byId(id);
            dojo.lang.assert(divElem, "Cannot find DIV element with id=" + id);
            this.nodeLabel = dojo.html.getAttribute(divElem, "psAaLabel");
         }
      }
      return this.nodeLabel;
   }
   
   /**
    * Determines if the specified object id equals the object id of the node.
    * 
    * @param {ps.aa.ObjectId} id The object id in question.
    * Not <code>null</code>
    * 
    * @return true if the object ids are equal; false otherwise.
    */
   this.equals = function(id)
   {
      dojo.lang.assertType(id, ps.aa.ObjectId);
      return this.objId.serialize() == id.serialize();
   }
   
   /**
    * Clears all properties of the node and removes its child nodes if there is
    * any. This is used before remove/destroy the node from the tree.
    */
   this.clear = function()
   {
      this.removeChildNodes();
      
      this.objId = null;
      this.parentNode = null;  
      this.nodeLabel = null;
   }
   
   /**
    * Clears the label property.
    */
   this.clearLabel = function()
   {
      this.nodeLabel = null;
   }
   
   /**
    * Removes all child nodes if exists.
    */
   this.removeChildNodes = function()
   {
      if (this.childNodes == null)
         return;
         
      for (var i=0; i<this.childNodes.count; i++)
      {
         var node = this.childNodes.item(i);
         node.clear();
      }
      this.childNodes.clear(); 
   }
   
   /**
    * @return this.objId.isSnippetNode();
    */
   this.isSnippetNode = function()
   {
      return this.objId.isSnippetNode();
   }
   
   /**
    * Returns the id of the data object associated with this node.
    * @return ps.aa.ObjectId
    */
   this.getObjectId = function()
   {
      return this.objId;
   }
   
   /**
    * @return this.objId.isPageNode();
    */
   this.isPageNode = function()
   {
      return this.objId.isPageNode();
   }
   
   /**
    * @return this.objId.isFieldNode();
    */
   this.isFieldNode = function()
   {
      return this.objId.isFieldNode();
   }
   
   /**
    * @return this.objId.isSlotNode();
    */
   this.isSlotNode = function()
   {
      return this.objId.isSlotNode();
   }
   
   /**
    * Adds the specified child node.
    * 
    * @param {ps.aa.TreeNode} node The new child node.
    */
   this.addChildNode = function(node)
   {
      if (this.childNodes == null)
         this.childNodes = new dojo.collections.ArrayList();
      
      node.parentNode = this;
      this.childNodes.add(node);   
   }
   
   /**
    * Returns the node index in the parent node or throws an exception
    * if this is a root node, which does not have a parent,
    * or if it can't find the node.
    */
   this.getIndex = function ()
   {
      dojo.lang.assert(this.parentNode);
      var siblings = this.parentNode.childNodes.toArray();
      for (var i in siblings)
      {
         var node = siblings[i];
         if (node === this)
         {
            return parseInt(i);
         }
     }
     dojo.lang.assert(false,
         "Inconsistent tree structure, could not find this node in the parent");
   }
   
   /**
    * @return serialized string of this object.
    */   
   this.toString = function()
   {
      if (this.parentNode == null && this.childNodes == null)
      {
         return "id = " + this.objId.serialize();
      }
      else if (this.parentNode == null)
      {
         return "id = " + this.objId.serialize() + "\n" +
                "childNodes.count = " + this.childNodes.count;
      }
      else if (this.childNodes == null)
      {
         return "id = " + this.objId.serialize() + "\n" +
                "parent = " + this.parentNode.objId.serialize();
      }
      else
      {
         return "id = " + this.objId.serialize() + "\n" +
                "parent = " + this.parentNode.objId.serialize() + "\n" +
                "childNodes.count = " + this.childNodes.count;         
      }
         
   }
};
