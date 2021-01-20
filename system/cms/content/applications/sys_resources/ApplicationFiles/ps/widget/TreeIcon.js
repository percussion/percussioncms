/******************************************************************************
 *
 * [ ps.widget.TreeIcon.js ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

dojo.provide("ps.widget.TreeIcon");

dojo.require("dojo.lang.assert");
dojo.require("dojo.widget.HtmlWidget");
dojo.require("dojo.widget.TreeDocIconExtension");


dojo.widget.defineWidget(
   "ps.widget.TreeIcon",
   dojo.widget.TreeDocIconExtension,
{
   templateCssPath: dojo.uri.moduleUri("ps", "widget/TreeIcon.css"),
   
   
   /**
    * Gets the nominal node type to be used as part of 
    * CSS class selector. The CSS class is used to display the icon.
    * @Override
    *
    * @param {dojo.widget.TreeNodeV3} node
    * @return {String}
    * @see {#_getNodeType}
    */
   getnodeDocType: function(node) 
   {
      //dojo.debug("getnodeDocType called with Node: " + node);
      dojo.lang.assert(node);
      dojo.lang.assertType(node,dojo.widget.TreeNodeV3);
      var oid = node.modelId;
      dojo.lang.assert(oid, "node does not have model id attached to it.");
      var nodeDocType = this._getNodeType(oid);
      //dojo.debug("TreeIcon - doc type is: " + nodeDocType);
      return nodeDocType;
    },
    

   /**
    * Gets the node type for css class selector based
    * on the ObjectId.
    * 
    * @param {ps.aa.ObjectId} objId
    * @return {String} node type.
    */
   _getNodeType : function(objId)
   {   
      dojo.lang.assertType(objId,ps.aa.ObjectId);
      var objClass;
      if(objId.isPageNode())
      {
         objClass = ps.aa.PAGE_CLASS;
      }
      else if(objId.isSnippetNode())
      {
         objClass = ps.aa.SNIPPET_CLASS;
      }
      else if(objId.isSlotNode())
      {
         objClass = ps.aa.SLOT_CLASS;
      }
      else if(objId.isFieldNode())
      {
         objClass = ps.aa.FIELD_CLASS;
      }
      var myName = ps.aa.ObjectId.ImageNames[objClass] + objId.getCheckoutStatus();
      var initial = myName.substring(0,1);
      var rest = myName.substring(1,myName.length);
      var klass = initial.toUpperCase() + rest;
      return klass;
   }

});
