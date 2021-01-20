
/******************************************************************************
 *
 * [ ps.widget.MenuBar2.js ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
 
dojo.provide("ps.widget.MenuBar2");

dojo.require("dojo.widget.Menu2");

/**
 * Override dojo.widget.PopupMenu2.closeSubmenu() function to avoid gain
 * focus on the original window instead of the popup window.
 */
dojo.widget.defineWidget(
	"ps.widget.MenuBar2",
	dojo.widget.MenuBar2,
{
   /**
    * Override dojo.widget.PopupMenu2.closeSubmenu() function, so that 
    * we can avoid the following statement of, this.parent.domNode.focus(), 
    * which is called inside dojo.widget.PopupContainerBase.close() function.
    * The above statement will always make the current Window gain focus and
    * push any popup Window behind (the popup window is invoked by a menu item
    * from the menubar). 
    * For some reason, the above problem works for FireFox, but not IE, and it
    * is not an issue with the context menu.
    * 
    * @param {boolean} force true if force to close the sub popup.
    */
   closeSubmenu: function(force){
      // summary: close the currently displayed submenu
      if (this.currentSubmenu == null){ return; }

      // set the parent property to null to avoid a statement of
      // this.parent.domNode.focus() in the dojo.widget.PopupContainerBase.close()
      if (this.currentSubmenu.parent)
         this.currentSubmenu.parent = null;

      this.currentSubmenu.close(force);
      this.currentSubmenu = null;

      this.currentSubmenuTrigger.is_open = false;
      this.currentSubmenuTrigger._closedSubmenu(force);
      this.currentSubmenuTrigger = null;
   }

});
