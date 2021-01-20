/******************************************************************************
 *
 * [ ps.widget.MenuBarItemDropDown.js ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
 
dojo.provide("ps.widget.MenuBarItemDropDown");

dojo.require("dojo.widget.Menu2");

/**
 * Extends the dojo MenuBarItem2 to add the the decoration to indicate it is a 
 * drop down menu. Intended to be used only if the item is a drop down menu. 
 * When rendered, it puts a little down arrow at the end of the caption.
 * If method createSubmenu is specified, calls it to create a submenu on demand.
 */
dojo.widget.defineWidget(
	"ps.widget.MenuBarItemDropDown",
	dojo.widget.MenuBarItem2,
	function ()
	{
	   var _this = this;
	   
	   // listener, creating submenu on demand
	   function onCreateSubmenu()
	   {
	      if (_this.submenuCreated)
	      {
	         return;
	      }
	      _this.submenuCreated = true;
	      if (_this.createSubmenu)
	      {
	         _this.createSubmenu();
	      }
	   }

	   dojo.event.connectBefore(this, "_onClick", onCreateSubmenu);
	   dojo.event.connectBefore(this, "_openSubmenu", onCreateSubmenu);
	},
{
   // it would be nice to modify the super class template string to add 
   // the decoration instead of repeating that from the superclass.
   templateString:
         '<span class="dojoMenuItem2" dojoAttachEvent="onMouseOver: onHover; '
         + 'onMouseOut: onUnhover; onClick: _onClick;">'
         + '${this.caption} <span><img src="'
         + dojo.uri.dojoUri("../ps/widget/images/dropdownButtonsArrow.gif")
         + '" verticalAlign="middle"/></span></span>',

    // a function, which creates submenu.
    createSubmenu: null
});
