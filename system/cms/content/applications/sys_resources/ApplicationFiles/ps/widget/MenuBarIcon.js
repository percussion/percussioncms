/******************************************************************************
 *
 * [ ps.widget.MenuBarIcon.js ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
 
dojo.provide("ps.widget.MenuBarIcon");

dojo.require("dojo.widget.Menu2");
dojo.require("dojo.dom");


/**
 * This widget is used to display an icon at the menubar. It supports the onClick
 * event.
 */
dojo.widget.defineWidget(
	"ps.widget.MenuBarIcon",
	dojo.widget.MenuBarItem2,
{
	templateString:
      '<span class="dojoMenuItem2" dojoAttachEvent="onClick: _onClick;">'
		+ '<img src="../sys_resources/images/aa/page_1.gif" alt="Icon" title="" verticalAlign="middle"/>'
      + '</span>',

	imgDomNode : null,
	
   setImage : function(imgUrl)
   {
      if (this.imgDomNode == null)
      {
         //var nodes = this.domNode.childNodes;
         //this.imgDomNode = nodes[0];
         this.imgDomNode = dojo.dom.getFirstChildElement(this.domNode, "img");
      }
      this.imgDomNode.setAttribute("src", imgUrl);
   },

   setTitle : function(title)
   {
      if (this.imgDomNode == null)
      {
         //var nodes = this.domNode.childNodes;
         //this.imgDomNode = nodes[0];
         this.imgDomNode = dojo.dom.getFirstChildElement(this.domNode, "img");
      }
      this.imgDomNode.setAttribute("title", title);
   }

});


