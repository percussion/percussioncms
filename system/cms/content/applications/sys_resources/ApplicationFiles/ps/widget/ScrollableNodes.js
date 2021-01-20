/******************************************************************************
 *
 * [ ps.widget.ScrollableNodes.js ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

dojo.provide("ps.widget.ScrollableNodes");

/**
 * Manages list of scrollable elements to support autoscroll.
 * 
 * @author Andriy Palamarchuk
 */
ps.widget.ScrollableNodes = function ()
{
   /**
    * Stores dom nodes which should be scrolled during dragging operation.
    */
   this.init = function (nodes)
   {
      this.nodes = nodes;

      dojo.lang.forEach(this.nodes, function(node)
      {
         dojo.lang.assert(node, "Expected all scrollable nodes to be defined.");
      });
   }

   /**
    * Finds the scrollable node, over which the provided mouse event occurs.
    * @param e the mouse event. Not <code>null</code>.
    */
   this.getOverNode = function (e)
   {
      dojo.lang.assert(e, "Event must be specified");

      for (var i = 0; i < this.nodes.length; i++)
      {
         var n = this.nodes[i];
         if (this._overElement(n, e))
         {
            return n;
         }
      }
      return null;
   }

   /**
    * Returns <code>true</code> if the mouse event happened over visible part
    * of the provided element.
    * @param element the dom node of the element to process.
    * Not <code>null</code>.
    * @param e the mouse event.
    * Not <code>null</code>.
    */
   this._overElement = function (element, e)
   {
      dojo.lang.assert(element, "Element must be specified");
      dojo.lang.assert(e, "Event must be specified");

      var sides = ps.util.getVisibleSides(element);
      return e.clientX >= sides.left
            && e.clientX <= sides.right
            && e.clientY >= sides.top
            && e.clientY <= sides.bottom;
   }
}
