/******************************************************************************
 *
 * [ ps.widget.Autoscroller.js ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

dojo.provide("ps.widget.Autoscroller");

dojo.require("ps.widget.ScrollableNodes");

/**
 * Scrolls an element in the provided direction while it's
 * <code>scroll()</code> method is being called.
 *
 * @author Andriy Palamarchuk
 */
ps.widget.Autoscroller = function ()
{
   /**
    * Initializes the autoscroller.
    * @param nodes array of nodes to autoscroll.
    * Not <code>null</code>
    */
   this.init = function(nodes)
   {
      var _this = this;
      this.scrollableNodes.init(nodes)

      // listen on dojo.dnd.dragManager methods instead of concreate targets
      // and objects to provide autoscroll for all DnD
      dojo.event.connect(dojo.dnd.dragManager, "onMouseUp",
            function () {_this._stopScroll();});
      dojo.event.connectAround(dojo.dnd.dragManager, "onMouseMove",
            this, "_onMouseMove");
   }
   
   /**
    * Around advice for {@link dojo.dnd.dragManager#onMouseMove}.
    * Autoscrolls if necessary.
    */
   this._onMouseMove = function (invocation)
   {
      dojo.lang.assert(invocation, "Invocation must be defined");
      invocation.proceed();
      if (dojo.dnd.dragManager.dragObjects.length)
      {
         var e = invocation.args[0];
         var contentNode = this.scrollableNodes.getOverNode(e);
         this._maybeAutoscroll(contentNode, e);
      }
   }

   /**
    * Autoscrolls the provided element if the mouse event is in the autoscroll
    * area. If the autoscroll is not needed, the existing autoscroll,
    * managed by this object, is cancelled.
    * @param element the dom node of the element to scroll.
    * If <code>null</code>, autoscroll is cancelled.
    * @param event the mouse event. Not <code>null</code>.
    */
   this._maybeAutoscroll = function (element, event)
   {
      dojo.lang.assert(event, "Event for maybeAutoscroll should not be null.")
      this._resetTimeout();
      
      var stopScroll = true;
      if (element)
      {
         var direction = this._detectAutoscrollArea(element, event);
         if (direction)
         {
            this._scroll(element, direction);
            stopScroll = false;
         }
      }

      if (stopScroll)
      {
         this._stopScroll();
      }
   }

   /**
    * Stops currently running autoscroll.
    */
   this._stopScroll = function ()
   {
      this.element = null;
   }

   /**
    * Function to start and maintain autoscrolling. Autoscrolling happens as
    * long as the method is called for this element.
    * The method can be called as frequently as necessary.
    */
   this._scroll = function (element, direction)
   {
      this.direction = direction;
      if (this._isScrollingStopped(element))
      {
         // launch autoscrolling
         this.element = element;
         this._doScroll();
      }
   }
   
   /**
    * Actual scrolling.
    */
   this._doScroll = function ()
   {
      var scrollLeft = this.element.scrollLeft;
      var scrollTop = this.element.scrollTop;

      this.element.scrollLeft += this.direction.x * this.SCROLL_DISTANCE;
      this.element.scrollTop += this.direction.y * this.SCROLL_DISTANCE;

      dojo.lang.setTimeout(this, "_continueScroll", this.SCROLL_TIME,
           this.element);

      var moved = scrollLeft !== this.element.scrollLeft
            || scrollTop !== this.element.scrollTop;

      if (moved)
      {
         dojo.dnd.dragManager.onScroll();
      }
      return moved;
   }
   
   /**
    * Continues autoscrolling if necessary.
    * @param element the scrolled element. Not <code>null</code>.
    */
   this._continueScroll = function (element)
   {
      if (this._isScrollingStopped(element))
      {
         return;
      }
      this._doScroll();
   }
   
   /**
    * Returns <code>true</code> if a scroll should be performed right now.
    * This happens when the autoscrolling process is finished or when element
    * to scroll was changed.
    */
   this._isScrollingStopped = function (element)
   {
      return (!this.element && element !== this.element) || this._isTimeout();
   }
   
   /**
    * Resets autoscrolling timeout.
    * @see #SCROLL_TIMEOUT
    */
    this._resetTimeout = function ()
    {
       this.autoscrollTimeout = new Date().getTime() + this.SCROLL_TIMEOUT;
    }
    
    /**
     * Returns true if autoscroll was left running for more than
     * {@link @SCROLL_TIMEOUT}.
     */
    this._isTimeout = function ()
    {
       return new Date().getTime() > this.autoscrollTimeout;
    }

   /**
    * Detects whether the event happened in autoscroll area of the provided
    * element.
    * @param element the analysed element. Not <code>null</code>.
    * @param e the mouse event to check. Not <code>null</code>.
    * @return <code>null</code> if the event did not occur in the autoscroll
    * area. Otherwise returns an object with attributes 'x', 'y', containing
    * direction of the closest edge as 1, 0, or -1, where 0 means there is no
    * component edge nearby.
    */   
   this._detectAutoscrollArea = function (element, e)
   {
      dojo.lang.assert(element, "Element must be specified");
      dojo.lang.assert(e, "Event must be specified");

      var sides = ps.util.getVisibleSides(element);

      var _this = this;
      function near(i1, i2)
      {
         var d = i2 - i1;
         return d >= 0 && d <= _this.AUTOSCROLL_EDGE_DISTANCE;
      }
      
      // the checks for both directions on the same axis are independent,
      // to allow them cancel each other if the area 
      var x = 0;
      if (near(sides.left, e.clientX))
      {
         x -= 1;
      }
      if (near(e.clientX, sides.right))
      {
         x += 1;
      }
      
      var y = 0;
      if (near(sides.top, e.clientY))
      {
         y -= 1;
      }
      if (near(e.clientY, sides.bottom))
      {
         y += 1;
      }
      
      return x || y ? {x: x, y: y} : null;
   }

   /**
    * Element to be scrolled.
    */
   this.element = null;
   
   /**
    * Direction to scroll. It's an object with attributes x and y,
    * specifying direction to scroll as values -1, 0, 1.
    */
   this.direction = {x:0, y:0};
   
   /**
    * Abandoned autoscroll timeout.
    * @see #SCROLL_TIMEOUT
    */
   this.autoscrollTimeout = 0;

   /**
    * The scrollable nodes manager utility.
    */
   this.scrollableNodes = new ps.widget.ScrollableNodes();

   /**
    * Time between subsequent scrolls.
    */
   this.SCROLL_TIME = 100;
   
   /**
    * Distance a page is scrolled at once.
    */
   this.SCROLL_DISTANCE = 10;
   
   /**
    * Time after which autoscrolling ends.
    * Used as a safety precaution when autoscrolling was not stopped in time.
    */
   this.SCROLL_TIMEOUT = 5 * 1000;

   /**
    * Distance from the side of an element during drag when autoscroll is
    * triggered.
    */
   this.AUTOSCROLL_EDGE_DISTANCE = 50;
}