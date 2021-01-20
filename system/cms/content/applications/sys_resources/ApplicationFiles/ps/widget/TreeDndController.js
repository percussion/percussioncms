/******************************************************************************
 *
 * [ ps.widget.TreeDndController.js ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

dojo.provide("ps.widget.TreeDndController");

dojo.require("dojo.widget.TreeDndControllerV3");
dojo.require("dojo.dnd.TreeDragAndDropV3");
dojo.require("dojo.experimental");
dojo.require("ps.aa.controller");


dojo.widget.defineWidget(
	"ps.widget.TreeDndController",
	dojo.widget.TreeDndControllerV3,
	function() {
		this.dragSources = {};
		this.dropTargets = {};
		this.listenedTrees = {};
	},
{
   
	onBeforeTreeDestroy: function(message) {
	   //We don't want to stop listening to the tree because it will be
	   //reloaded with a new model most likely.
		//this.unlistenTree(message.source);
		dojo.debug("I would be not listening anymore but I am going to.");
	},
	
	reset : function() {
	   this.dragSources = {};
	   this.dropTargets = {};
	},
	
	/**
	 * Intercept the parent's makeDropTarget method to add listener on the drop
	 * target onDragOver call.
	 */
	makeDropTarget : function (node)
	{
	   var target = dojo.widget.TreeDndControllerV3.prototype.makeDropTarget
	         .apply(this, arguments);
	   dojo.event.connectAround(target, "onDragOver",
            this, "_onDragOver");
	   return target;
	},

   /**
    * A listener around all tree drop targets onDragOver calls.
    * Makes sure that is dropped only into allowed target.
    */
   _onDragOver : function (invocation)
   {
      var accepts = invocation.proceed();
      var targetId = invocation.object.treeNode.modelId;
      var targetSlotId = null;
      if(targetId.isSlotNode())
      {
         targetSlotId = targetId;
      }
      else if(targetId.isSnippetNode())
      {
         var snippetNode = ps.aa.controller.treeModel.getNodeById(targetId)
         targetSlotId = snippetNode.parentNode.objId;
      }
      if (accepts && targetSlotId)
      {
         var dragSource = invocation.args[0].dragObjects[0].dragSource;
         var snippetId = dragSource.treeNode.modelId;
         var snippetNode = ps.aa.controller.treeModel.getNodeById(snippetId)
         var slotId = snippetNode.parentNode.objId;
         return slotId.belongsToTheSameItem(targetSlotId);
      }
      else
      {
         return false;
      }
      dojo.lang.assert(false, "Should not reach here");
   }
});