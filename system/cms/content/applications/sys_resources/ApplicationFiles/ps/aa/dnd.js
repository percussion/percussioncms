/******************************************************************************
 *
 * [ ps.aa.dnd.js ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

dojo.provide("ps.aa.dnd");

dojo.require("ps.aa");

/**
 * The Drag and Drop (view) controller.
 * Provides DnD functionality to Assembly Page.
 */
ps.aa.dnd = new function ()
{
   /**
    * Configured page drag and drop.
    */
   this.init = function ()
   {
      dojo.dnd.dragManager.nestedTargets = true;
      dojo.event.connect(ps.aa.controller.treeModel, "onBeforeDomChange",
            this, "_onBeforeDomChange");
      dojo.event.connect(ps.aa.controller.treeModel, "onDomChanged",
            this, "_onDomChanged");

      this._onDomChanged(ps.aa.controller.pageId);
   }

   /**
    * Unregisters the dom nodes which are about to change from DnD.
    * A listener on {@link ps.aa.Tree#onBeforeDomChange}.
    */
   this._onBeforeDomChange = function (id)
   {
      dojo.lang.assertType(id, ps.aa.ObjectId);

      // unregisters old sources and targets
      function unregisterAll(ids, objects)
      {
         dojo.lang.forEach(ids, function (id)
         {
            dojo.lang.assert(id.serialize() in objects,
                 "Following id is not registered: " + id.serialize());
            objects[id.serialize()].unregister();
            delete objects[id.serialize()];
         });
      }

      unregisterAll(this._getSnippetIds(id), this.dragSources);
      unregisterAll(this._getSlotIds(id), this.dropTargets);
   }

   /**
    * Registers the changed dom nodes for DnD.
    * A listener on {@link ps.aa.Tree#onDomChanged}.
    */
   this._onDomChanged = function (id)
   {
      dojo.lang.assertType(id, ps.aa.ObjectId);
      var _this = this;

      // drag sources
      dojo.lang.forEach(this._getSnippetIds(id), function (id)
      {
         dojo.lang.assert(!(id.serialize() in _this.dragSources));
         var node = ps.aa.Page.getElement(id);
         var source = new dojo.dnd.HtmlDragSource(node, ps.aa.SNIPPET_CLASS);
         _this.dragSources[id.serialize()] = source;
      });

      // drop targets
      dojo.lang.forEach(this._getSlotIds(id), function (id)
      {
         dojo.lang.assert(!(id.serialize() in _this.dropTargets));
         var node = ps.aa.Page.getElement(id);
         var target = new dojo.dnd.HtmlDropTarget(node, ps.aa.SNIPPET_CLASS);
         _this.dropTargets[id.serialize()] = target;

         dojo.event.connectAround(target, "onDragMove",
               _this, "_resetDropTargetVertical");

         dojo.event.connectAround(target, "onDragOver", _this, "_onDragOver");
         dojo.lang.assert(target.insert);
         dojo.event.connectAround(target, "insert", _this, "_dropTargetInsert");
         dojo.event.connectAround(target, "createDropIndicator",
               _this, "_createDropIndicator");

         dojo.event.connect(target, "onDropEnd", _this, "_onDropEnd");
      });
   }

   /**
    * An around advice for the "onDragOver" method of a drop target handler.
    * @param invocation the original "onDragOver" event handler.
    * @return <code>true</code> if the drop operation was successful.
    */
   this._onDragOver = function (invocation)
   {
      dojo.lang.assert(invocation, "Invocation must be defined");

      if (!invocation.proceed())
      {
         return false;
      }

      var event = invocation.args[0];
      dojo.lang.assert(event, "Event must be specified.");
      
      var dragObject = event.dragObjects[0];
      var slotId = this._getParentSlotId(dragObject.domNode);
      var dropTarget = invocation.object;

      var targetSlotId = this._getDropTargetId(dropTarget);
      dojo.lang.assert(slotId);
      dojo.lang.assert(targetSlotId);
      
      if (!slotId.belongsToTheSameItem(targetSlotId))
      {
         return false;
      }
      
      this._resetTargetChildBoxes(dropTarget);
      dropTarget.vertical = this._isDropIndicatorVertical(dropTarget);
      return true;
   }
   
   /**
    * An around advice for the drop target methods, setting correct slot
    * layout direction.
    * @param invocation the original call. Not null.
    * @return <code>true</code> if the drop operation was successful.
    */
   this._resetDropTargetVertical = function (invocation)
   {
      var dropTarget = invocation.object;
      var slotId = this._getDropTargetId(dropTarget);
      dojo.lang.assert(slotId,
            "Object id should be specified on the drop target");
      dropTarget.vertical = this._isDropIndicatorVertical(dropTarget);
      return invocation.proceed();
   }

   /**
    * Drop target maintains a array of child boxes. The target "onDragOver"
    * method fills up this array with data for the drop target direct children.
    * This method instead fills up the array with the snippet nodes data,
    * because snippet nodes could be nested deeper than the next level.
    * @param dropTarget the current drop target. Not null.
    */
   this._resetTargetChildBoxes = function (dropTarget)
   {
      dojo.lang.assert(dropTarget, "Drop target expected");

      var boxes = dropTarget.childBoxes;
      
      // do nothing if there is no child nodes - the slot is empty
      if (!boxes || boxes.length === 0)
      {
         return;
      }

      var targetSlotId = this._getDropTargetId(dropTarget);

      // exclude snippets inside of snippets
      var snippetIds = dojo.lang.filter(
            this._getSnippetIds(targetSlotId),
            function (id)
            {
               return id.getSlotId() === targetSlotId.getSlotId();
            });
      var snippetIdStrings = dojo.lang.map(snippetIds,
            function (id) {return id.serialize();});

      var divs = dropTarget.domNode.getElementsByTagName("div");
      var snippetNodes = dojo.lang.filter(divs, function (div)
      {
         return div.id && div.className == ps.aa.SNIPPET_CLASS
               && dojo.lang.inArray(snippetIdStrings, div.id);
      });

      // sort the snippet nodes by the snippet order
      snippetNodes.sort(function (n1, n2)
      {
         var id1 = new ps.aa.ObjectId(n1.id);
         var id2 = new ps.aa.ObjectId(n2.id);
         return id1.getSortRank() - id2.getSortRank();
      });

      boxes.length = 0;
      var _this = this;
      dojo.lang.forEach(snippetNodes, function(child)
      {
         boxes.push(_this._getDropTargetChildBox(child));
      });
      this._fillDropTargetChildBoxesGaps(boxes);
   }

   /**
    * Because we manually select drop target children, we skip the elements
    * we are not interested in. As result the generated boxes do not cover the
    * whole drop target.
    * This method resizes the boxes to the right and down, so their borders
    * touch and all the gaps are closed.
    */
   this._fillDropTargetChildBoxesGaps = function (boxes)
   {
      dojo.lang.forEach(boxes, function(box)
      {
         var leftmost = null;
         var topmost = null;
         dojo.lang.forEach(boxes, function(box2)
         {
            // skip itself
            if (box === box2)
            {
               return;
            }

            // if box and box2 overlap vertically, box2 is to the right of box
            if (Math.max(box.top, box2.top) < Math.min(box.bottom, box2.bottom)
                 && box2.right > box.right)
            {
               if (!leftmost || leftmost > box2.left)
               {
                  leftmost = box2.left;
               }
            }

            // if box and box2 overlap horizontally, box2 is under box
            if (Math.max(box.left, box2.left) < Math.min(box.right, box2.right)
                 && box2.bottom > box.bottom)
            {
               if (!topmost || topmost > box2.top)
               {
                  topmost = box2.top;
               }
            }
         });
         if (leftmost && leftmost > box.right + 1)
         {
            box.right = leftmost - 1;
         }
         if (topmost && topmost > box.bottom + 1)
         {
            box.bottom = topmost - 1;
         }
      });
   }

   /**
    * An around advice for the "insert" method of a drop target handler.
    * Parameters for the advised invocation:
    * @param {dojo.dnd.DragEvent} e the drag event.
    * Not <code>null</code>.
    * @param refNode the node relative to which the insert is done.
    * Not <code>null</code>.
    * @param position where relative to refNode the new node should be inserted.
    * One of {@link #POS_BEFORE}, {@link #POS_AFTER}, and {@link #POS_APPEND}.
    * Not <code>null</code>.
    * @return <code>true</code> if the drop operation was successful.
    */
   this._dropTargetInsert = function (invocation)
   {
      dojo.lang.assert(invocation);

      var event = invocation.args[0];
      var refNode = invocation.args[1];
      var position = invocation.args[2];

      dojo.lang.assert(event, "Event must be specified.");
      dojo.lang.assert(refNode, "Reference node must be specified.");
      dojo.lang.assert(position, "Position must be specified.");
      this._assertValidPosition(position);

      var snippetNode = event.dragObject.domNode;
      var snippetId = new ps.aa.ObjectId(snippetNode.id);
      var slotId = this._getParentSlotId(snippetNode);
      var dropTarget = invocation.object;
      var targetSlotId = this._getDropTargetId(dropTarget);
      
      var index = this._getDropIndex(dropTarget, snippetId, refNode, position);

      if (snippetId.getSlotId() !== targetSlotId.getSlotId())
      {
         dojo.lang.assert(!this._m_move);
         this._m_move = new ps.aa.SnippetMove(
             snippetId, slotId, targetSlotId, index, true);
         var result = ps.aa.controller.moveToSlot(this._m_move);
      }
      // source and target slots are the same
      else if (ps.aa.controller.reorderSnippetInSlot(
            snippetId, index))
      {
         dojo.lang.assert(!this._m_move);
         // used later to refresh the slot
         this._m_move = new ps.aa.SnippetMove(
              snippetId, slotId, targetSlotId, index, true);
         this._m_move.setUiUpdateNeeded(true);
         this._m_move.setSuccess(true);
         var result = true;
      }
      else
      {
         var result = false;
      }
      
      // clear drop indicator again, in case it was created by mistake,
      // e.g. during error handling user interactions
      if (dropTarget.dropIndicator)
      {
         dojo.html.removeNode(dropTarget.dropIndicator);
         delete dropTarget.dropIndicator;
      }
      
      return result && invocation.proceed();
   }
   
   /**
    * An around advice for the <code>onDropEnd</code> method of a drop target
    * handler.
    */
   this._onDropEnd = function ()
   {
      if (this._m_move)
      {
         dojo.lang.assertType(this._m_move.isSuccess(), Boolean);
         // page still can be updated, e.g. in template selection
         this._m_move.setDontUpdatePage(false);
         if (this._m_move.isUiUpdateNeeded() && this._m_move.isSuccess())
         {
            ps.aa.controller.maybeRefreshMovedSnippetNode(this._m_move, true);

            // activate the target snippet
            var snippetId = this._m_move.getTargetSnippetId();
            ps.aa.controller.activate(ps.aa.Page.getElement(snippetId));
         }
         this._m_move = null;
      }
   }
   
   /**
    * Index to drop for the dragged snippet component. Counts snippet nodes
    * in the container before the provided node.
    * @param dropTarget the drop target to calculate the position for.
    * Not null.
    * @param {ps.aa.ObjectId} snippetId the snippet id to drop.
    * Not <code>null</code>.
    * @param {String} position a valid position relative by refNode
    * as defined by {@link #_assertValidPosition}.
    * @return a 1-based index where the snippet component is dropped.
    * Not <code>null</code>.
    * @see #_dropTargetInsert
    */
   this._getDropIndex = function (dropTarget, snippetId, refNode, position)
   {
      dojo.lang.assert(dropTarget, "Target must be specified");
      dojo.lang.assertType(snippetId, ps.aa.ObjectId);
      dojo.lang.assert(refNode, "Reference node must be specified");
      this._assertValidPosition(position);

      var childrenLen = dropTarget.childBoxes.length;
      for (var i = 0, refNodeIdx = 1; i < childrenLen; i++)
      {
         var childBox = dropTarget.childBoxes[i];
         if (childBox.node === refNode && position !== this.POS_APPEND)
         {
            if (position != this.POS_BEFORE)
            {
               refNodeIdx++;
            }
            return Math.min(refNodeIdx, childrenLen);
         }

         // skip the snippet being moved
         if (childBox.node.id !== snippetId.serialize())
         {
            refNodeIdx++;
         }
      }

      if (position === this.POS_APPEND)
      {
         return refNodeIdx;
      }

      dojo.debug(refNode);
      dojo.lang.assert(false,
            "Could not find reference node in the list of nodes");
   }
   
   /**
    * Determines whether drop indicator should be vertical.
    * If the first 2 snippets of the slot are stacked side-by-side,
    * it returns true, otherwise it returns false.
    * Note, we handle only the simplest cases, because a slot can have any
    * layout.
    * Even with scary layouts when our check guesses wrong,
    * users still can drag and drop a snippet from another slot,
    * and move it inside a slot to the desired position using Up and Down
    * menu items.
    * @param dropTarget the drop target to check whether it's vertical.
    * Not null.
    * @return true if the slot is considered vertical. False otherwise.
    */
   this._isDropIndicatorVertical = function (dropTarget)
   {
      dojo.lang.assert(dropTarget, "Drop target must be specified");

      if (dropTarget.childBoxes.length < 2)
      {
         return false;
      }

      var box0 = dropTarget.childBoxes[0];
      var box1 = dropTarget.childBoxes[1];

      return Math.abs(box0.left - box1.left) > Math.abs(box0.top - box1.top);
   }

   /**
    * Makes drop indicator more visible.
    * @param invocation call to
    * {@link dojo.dnd.HtmlDropTarget#createDropIndicator}.
    * Not <code>null</code>.
    */
   this._createDropIndicator = function (invocation)
   {
      dojo.lang.assert(invocation);
      invocation.proceed();
      var target = invocation.object;
      dojo.lang.assert(target.dropIndicator);
      var style = target.dropIndicator.style;
      var color = "gray";
      var borderWidth = "3px";
      if (target.vertical)
      {
         style.borderLeftWidth = borderWidth;
         style.borderLeftColor = color;
      }
      else
      {
         style.borderTopWidth = borderWidth;
			style.borderTopColor = color;
      }
   }

   /**
    * Finds a slot node, which is a parent of the provided node.
    * @param domNode the node to search from.
    * Defined.
    * @return the id of the first node from domNode or its parents,
    * corresponding to a slot.
    * Returns <code>null</code> if a slot node can't be found.
    */
   this._getParentSlotId = function(domNode)
   {
      dojo.lang.assert(domNode, "Dom node is not specified");
      var node = domNode;
      while (node)
      {
         if (node.nodeType === dojo.dom.ELEMENT_NODE &&
             node.className === ps.aa.SLOT_CLASS)
         {
            return new ps.aa.ObjectId(node.id);
         }
         else
         {
            node = node.parentNode;
         }
      }
      return null;
   }

   /**
    * Checks that the provided position value is a valid value as defined
    * in {@link dojo.dnd.HtmlDropTarget#insert}.
    */
   this._assertValidPosition = function (position)
   {
      dojo.lang.assert(
            position === this.POS_BEFORE
            || position === this.POS_AFTER
            || position === this.POS_APPEND,
            "Unrecognized position: " + position);
   }

   /**
    * Generates a box object for placing it into
    * dojo.dnd.HtmlDropTarget.boxes list.
    * Borrowed from dojo.dnd.HtmlDropTarget.onDragOver
    * @param child the dom element to generate the box for.
    * @see _resetTargetChildBoxes
    */
   this._getDropTargetChildBox = function (child)
   {
         var pos = dojo.html.getAbsolutePosition(child, true);
         var inner = dojo.html.getBorderBox(child);
         return {top: pos.y, bottom: pos.y + inner.height,
               left: pos.x, right: pos.x + inner.width,
               height: inner.height,  width: inner.width,
               node: child};
   }

   /**
    * Returns an array of slot ids under ther provided id.
    */
   this._getSlotIds = function (id)
   {
      dojo.lang.assertType(id, ps.aa.ObjectId);
      var ids = ps.aa.controller.treeModel.getIdsFromNodeId(id).toArray();
      return dojo.lang.filter(ids, function (id) {return id.isSlotNode()});
   }
   
   /**
    * Returns an array of snippet ids under the provided id.
    */
   this._getSnippetIds = function (id)
   {
      dojo.lang.assertType(id, ps.aa.ObjectId);
      var ids = ps.aa.controller.treeModel.getIdsFromNodeId(id).toArray();
      return dojo.lang.filter(ids, function (id) {return id.isSnippetNode()});
   }
   
   /**
    * Extracts id from the drop target.
    * @param dropTarget the drop target to extract id from. Not null.
    * @return the object id of the target.
    */
   this._getDropTargetId = function (dropTarget)
   {
      dojo.lang.assert(dropTarget, "Expected dropTarget");
      return new ps.aa.ObjectId(dropTarget.domNode.id);
   }

   /**
    * Constant to indicate that a node should be inserted before the specified
    * node in {@link dojo.dnd.HtmlDropTarget#insert}.
    */
   this.POS_BEFORE = "before";

   /**
    * Constant to indicate that a node should be inserted after the specified
    * node in {@link dojo.dnd.HtmlDropTarget#insert}.
    */
   this.POS_AFTER = "after";

   /**
    * Constant to indicate that a node should be appended to the specified
    * node in {@link dojo.dnd.HtmlDropTarget#insert}.
    */
   this.POS_APPEND = "append";

   /**
    * Drag sources of type dojo.dnd.HtmlDragSource.
    * Keys - string representation of ids, values -
    * {@link dojo.dnd.HtmlDragSource} objects.
    * Can be empty if not defined yet or the page does not have any sources.
    */
   this.dragSources = {};

   /**
    * Keys - string representation of ids, values -
    * {@link dojo.dnd.HtmlDropTarget} objects.
    * Can be empty if not defined yet or the page does not have any targets.
    */
   this.dropTargets = {};
}
