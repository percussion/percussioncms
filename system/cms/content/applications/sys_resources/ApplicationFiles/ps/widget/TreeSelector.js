/******************************************************************************
 *
 * [ ps.widget.TreeSelector.js ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/


dojo.provide("ps.widget.TreeSelector");

dojo.require("dojo.widget.TreeSelectorV3");


dojo.widget.defineWidget(
	"ps.widget.TreeSelector",
	dojo.widget.TreeSelectorV3,
	function ()
	{
	   dojo.event.connect(this, "processNode", this, "_nodeActivated");
	},
{
   /**
    * Is called when a tree node is activated/deactivated.
    * @param node the tree node to activate. Not <code>null</code>
    */
	_nodeActivated: function (node)
	{
		dojo.lang.assert(node);
		var objId = node.modelId;
		dojo.lang.assert(objId, "widget does not have a model id");
		ps.aa.controller.activate(objId);
	}
});