/******************************************************************************
 *
 * [ ps.widget.PopupMenu.js ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
 
dojo.provide("ps.widget.PopupMenu");

dojo.require("dojo.widget.Menu2");


/**
 * Added additional features based on dojo.widget.PopupMenu2.
 * If method createMenuItems is specified, calls it to create the menu items
 * on demand.
 */
dojo.widget.defineWidget(
   "ps.widget.PopupMenu",
   dojo.widget.PopupMenu2,
{
   /**
    * Binds all specified DOM nodes to this popup menu. 
    * 
    * @param {Array} targetNodes The array of ids in {string} or {DomNode} 
    *    that need to bind to this popup menu.
    */
   bindTargetNodes: function(targetNodes)
   {
      for (var i=0; i<targetNodes.length; i++)
      {
         this.bindDomNode(targetNodes[i]);
      }
   },

   /**
    * Unbinds all specified DOM nodes to this popup menu. 
    * 
    * @param {Array} targetNodes The array of ids in {string} or {DomNode} 
    *    that need to unbind to this popup menu.
    */
   unBindTargetNodes: function(targetNodes)
   {
      for (var i=0; i<targetNodes.length; i++)
      {
         this.unBindDomNode(targetNodes[i]);
      }
   },

   /**
    * Overwrite super, so that we have a chance to activate the target object
    * before open the context menu.
    * Lazily create menu items.
    */   
   onOpen: function(/*Event*/ e)
   {
      if (this.createMenuItems && !this.itemsCreated)
      {
         this.createMenuItems();
      }
      this.itemsCreated = true;

      if (e.currentTarget != null)
      {
         ps.aa.controller.activate(e.currentTarget);
      }
      ps.widget.PopupMenu.superclass.onOpen.apply(this, arguments);
   }
});
