
/******************************************************************************
 *
 * [ ps.widget.MenuBarItem2.js ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
 
dojo.provide("ps.widget.MenuBarItem2");

dojo.require("dojo.widget.Menu2");

/**
 * Extends the dojo MenuBarItem2 to do nothing to fix a weird menubar item 
 * renderng order issue. Whenver we mix custome menubar items and dojo menubar 
 * items the order is messed up. However, if we use all cutome menubar items it 
 * works fine.
 */
dojo.widget.defineWidget(
	"ps.widget.MenuBarItem2",
	dojo.widget.MenuBarItem2,
{
});
