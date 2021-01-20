/******************************************************************************
 *
 * [ ps.DivActionHelper.js ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

/*
 * Various utilities.
 * This is a generic module.
 * While changing this file consider moving functions to separate modules.
 */

ps.DivActionHelper = new function(){}

ps.DivActionHelper.mouseOverInfo = new Object();
ps.DivActionHelper.mouseOverInfo.barDelay = 1000;
ps.DivActionHelper.mouseOverInfo.barOffDelay = 200;
ps.DivActionHelper.mouseOverInfo.inTimeoutId = -1;
ps.DivActionHelper.mouseOverInfo.outTimeoutId = -1;
ps.DivActionHelper.mouseOverInfo.mouseStopTimeoutId = -1;
ps.DivActionHelper.mouseOverInfo.barOffTimeoutId = -1;
ps.DivActionHelper.mouseOverInfo.x = 0;
ps.DivActionHelper.mouseOverInfo.y = 0;
ps.DivActionHelper.mouseOverInfo.stack = new Array();
ps.DivActionHelper.mouseOverInfo.boundIds = null;
ps.DivActionHelper.mouseOverInfo.fIconsActive = false;
ps.DivActionHelper.mouseOverInfo.mouseOnFloat = false;
ps.DivActionHelper.mouseOverInfo.dndOccurring = false;
if (!dojo.render.html.ie ) 
{
  document.captureEvents(Event.MOUSEMOVE)
}

ps.DivActionHelper._onDNDStart = function()
{
   ps.DivActionHelper.reset();
   ps.DivActionHelper.mouseOverInfo.dndOccurring = true;
   ps.DivActionHelper.mouseOverInfo.stack.length = 0;  
}

ps.DivActionHelper._onDNDStop = function()
{
   ps.DivActionHelper.mouseOverInfo.dndOccurring = false;
   ps.DivActionHelper.mouseOverInfo.stack.length = 0;
}

/**
 * Handle mouse hovering over the various page objects
 */
ps.DivActionHelper.handleDivHover = function(div, isHovering)
{
  
   var isActive = (ps.aa.controller.activeId == div.id);
   var objId = new ps.aa.ObjectId(div.id);
   if(objId.isFieldNode())
      ps.util.handleIE6FieldDivHover(div, isHovering);
   if(ps.DivActionHelper.mouseOverInfo.dndOccurring)
      return;
   if(___sys_aamode == 1)
   {
      div.style.border = "1px dotted";
     
      if(isHovering && !objId.isPageNode())
      {
          if(ps.DivActionHelper.mouseOverInfo.inTimeoutId != -1)
          {
             window.clearTimeout(ps.DivActionHelper.mouseOverInfo.inTimeoutId);
             ps.DivActionHelper.mouseOverInfo.inTimeoutId = -1;
          }
          
          div.style.borderColor = "gray";
          ps.DivActionHelper.mouseOverInfo.stack.push(div);
          ps.DivActionHelper.mouseOverInfo.inTimeoutId = 
             window.setTimeout("ps.DivActionHelper._divHoverAction()", 
                ps.DivActionHelper.mouseOverInfo.barDelay);
          
      }
      else
      {
          if(ps.DivActionHelper.mouseOverInfo.barOffTimeoutId != -1)
          {
             window.clearTimeout(ps.DivActionHelper.mouseOverInfo.barOffTimeoutId);
             ps.DivActionHelper.mouseOverInfo.barOffTimeoutId = -1;
          }
          
          div.style.borderColor = "transparent";
          ps.DivActionHelper.mouseOverInfo.stack.pop();
          if(ps.DivActionHelper.mouseOverInfo.inTimeoutId != -1)
          {
             window.clearTimeout(ps.DivActionHelper.mouseOverInfo.inTimeoutId);
             ps.DivActionHelper.mouseOverInfo.inTimeoutId = -1;
          }
          if(ps.DivActionHelper.mouseOverInfo.fIconsActive)
             ps.DivActionHelper.delayedActionBarOff();
      }     
   }
   
}

ps.DivActionHelper.fBarOnmouseover = function()
{
   ps.DivActionHelper.mouseOverInfo.mouseOnFloat = true;
}

ps.DivActionHelper.fBarOnmouseout = function()
{
   ps.DivActionHelper.mouseOverInfo.mouseOnFloat = false;
}

ps.DivActionHelper._divGetParentContainer = function(div)
{
    var parentNode = div.parentNode;
    if(parentNode != null)
    {
       if(parentNode.tagName == "div")
       {
          var cname = parentNode.className;
          if(cname == "PsAaPage" || cname == "PsAaSlot" || cname == "PsAaSnippet")
          {
             return parentNode;
          }
          return ps.DivActionHelper._divGetParentContainer(parentNode);
       }
    }
    return null;
}

/**
 * Locates and returns the page div or <code>null</code> if
 * not found.
 */
ps.DivActionHelper._getPageDiv = function()
{
   var divs = document.getElementsByTagName("div");
   for(i = 0; i < divs.length; i++)
   {
      if(divs[i].className == "PsAaPage")
         return divs[i];
   }
   return null;
}

/**
 * Resets the floating action bar by destroying it and
 * close any open context menu.
 */
ps.DivActionHelper.reset = function()
{
   if(ps.DivActionHelper.mouseOverInfo.mouseOnFloat || !ps.DivActionHelper.mouseOverInfo.fIconsActive)
      return;
   ps.DivActionHelper._destroyFloatingActionBar();
   var ctxMenu1 = dojo.widget.byId("ps.aa.ItemCtxMenu");
   var ctxMenu2 = dojo.widget.byId("ps.aa.SlotCtxMenu");
   try
   {
      ctxMenu1.close(true);
      ctxMenu2.close(true);  
   }
   catch (ignore)
   {
   }   
}

/**
 * Shuts off the floating action bar after a delayed amount
 * of time.
 */
ps.DivActionHelper.delayedActionBarOff = function()
{
   if(ps.DivActionHelper.mouseOverInfo.barOffTimeoutId != -1)
   {
      window.clearTimeout(ps.DivActionHelper.mouseOverInfo.barOffTimeoutId);
      ps.DivActionHelper.mouseOverInfo.barOffTimeoutId = -1;
   }
   ps.DivActionHelper.mouseOverInfo.barOffTimeoutId = 
      window.setTimeout("ps.DivActionHelper.reset()", 
         ps.DivActionHelper.mouseOverInfo.barOffDelay);   

}

/**
 * The action that occurs when the mouse hovers over a slot, snippet or field
 * for the predetermined delay time.
 */
ps.DivActionHelper._divHoverAction = function()
{
  ps.DivActionHelper._initFloatingActionBar();
}

/**
 * Records mouse location when the mouse moves
 */
ps.DivActionHelper._onmousemove = function(e)
{
  if(ps.DivActionHelper.mouseOverInfo.mouseStopTimeoutId != -1)
  {
     window.clearTimeout(ps.DivActionHelper.mouseOverInfo.mouseStopTimeoutId);
     ps.DivActionHelper.mouseOverInfo.mouseStopTimeoutId = -1;
  }

  if (dojo.render.html.ie) 
  { 
    try
    {
       ps.DivActionHelper.mouseOverInfo.x = event.clientX + document.body.scrollLeft;
       ps.DivActionHelper.mouseOverInfo.y = event.clientY + document.body.scrollTop;
    }
    catch (ignore)
    {
    }
    
  } 
  else
  {  
    ps.DivActionHelper.mouseOverInfo.x = e.pageX;
    ps.DivActionHelper.mouseOverInfo.y = e.pageY;
  }
  ps.DivActionHelper.mouseOverInfo.mouseStopTimeoutId = 
     window.setTimeout("ps.DivActionHelper._onmousestop()", 1000);
  
}

/**
 * Action that occurs when the user stops moving the mouse for
 * a second.
 */
ps.DivActionHelper._onmousestop = function()
{
   if(!ps.DivActionHelper.mouseOverInfo.fIconsActive)
      ps.DivActionHelper._forceMouseOver();

}

// Set the onmousemove event handler
document.onmousemove = ps.DivActionHelper._onmousemove;

/**
 * Creates the floating action bar based on where the mouse is currently 
 * over.
 */
ps.DivActionHelper._initFloatingActionBar = function()
{
   if(ps.DivActionHelper.mouseOverInfo.dndOccurring)
      return;
   var fDiv = document.getElementById("rxFloatingActionBar");
   
   var slot = null;
   var snippet = null;
   var field = null;
   
   // Destroy existing bar
   ps.DivActionHelper._destroyFloatingActionBar();

   ps.DivActionHelper.mouseOverInfo.boundIds = new dojo.collections.ArrayList();
   var stack = ps.DivActionHelper._copyArray(ps.DivActionHelper.mouseOverInfo.stack);
   var nodes = new Array();
   var objId = null;
   
   while(stack.length > 0)
   {
      var node = stack.shift();
      var coords = dojo.html.toCoordinateObject(node, false, dojo.html.boxSizing.BORDER_BOX);
      objId = new ps.aa.ObjectId(node.id);
      
      if(coords.top < 0 || coords.left < 0)
         continue;
      nodes.unshift(ps.DivActionHelper._createActionBarElement(objId, node));
      ps.DivActionHelper.mouseOverInfo.boundIds.add(objId);
      if(objId.isSlotNode())
         break;
   }
   for(i = nodes.length - 1; i >= 0; i--)
   {
      fDiv.appendChild(nodes[i]);   
   }

   // Add the page icon as it should always exist.
   var pageDiv = ps.DivActionHelper._getPageDiv();
   if(pageDiv != null)
   {
      objId = new ps.aa.ObjectId(pageDiv.id);
      fDiv.appendChild(
         ps.DivActionHelper._createActionBarElement(objId, pageDiv));
      ps.DivActionHelper.mouseOverInfo.boundIds.add(objId);
   }
      
   fDiv.style.visibility = "visible";
   fDiv.style.backgroundColor = "#ffffff";
   fDiv.style.paddingTop = "4px";
   if(dojo.render.html.ie)
   {
      fDiv.style.paddingBottom = "4px";
   }
   fDiv.style.paddingLeft = "4px";
   fDiv.style.paddingRight = "4px";
   fDiv.style.border = "solid #000000 1px";
   fDiv.style.top = (ps.DivActionHelper.mouseOverInfo.y - 20 )+ "px";
   fDiv.style.left = (ps.DivActionHelper.mouseOverInfo.x + 10) + "px";
   ps.aa.Menu.bindContextMenu(ps.DivActionHelper.mouseOverInfo.boundIds, true);
   ps.DivActionHelper.mouseOverInfo.fIconsActive = true;

}

/**
 * Destroys the floating action bar.
 */
ps.DivActionHelper._destroyFloatingActionBar = function()
{
   var fDiv = document.getElementById("rxFloatingActionBar");
   ps.DivActionHelper.mouseOverInfo.fIconsActive = false;
   if(ps.DivActionHelper.mouseOverInfo.boundIds != null)
   {
      ps.aa.Menu.unBindContextMenu(ps.DivActionHelper.mouseOverInfo.boundIds, true);
      ps.DivActionHelper.mouseOverInfo.boundIds = null;
   }
   while(fDiv.hasChildNodes())
   {
      fDiv.removeChild(fDiv.lastChild);
   }
   fDiv.style.visibility = "hidden";
   fDiv.style.top = "-10px";
   fDiv.style.left = "-10px";
}

/**
 * Creates a single action bar icon element.
 */
ps.DivActionHelper._createActionBarElement = function(objId, div)
{
   var el = document.createElement("a");
   var imgEl = document.createElement("img");
   var imgPath = "../sys_resources/images/aa";
   
   var label = null;
   if(objId.isFieldNode())
   {
      label = dojo.string.trim(objId.getFieldLabel());
      //Filed labels will have trailing colons, remove it.
      if(label && label.charAt(label.length-1)==":")
      {
         label = label.substring(0,label.length-1);
      }
   }
   else
   {
      label = div.getAttribute("PsAaLabel");
   }
   el.setAttribute("href", "javascript:void(0)");
   el.setAttribute("id", "img." + div.id);   
   if(dojo.render.html.ie)
   {
      el.onclick = new Function("evt", "ps.aa.controller.activate(this)");
      el.style.border = "none";
      imgEl.style.paddingRight = "2px";
      imgEl.style.border = "none";
   }
   else
   {
      el.setAttribute("onclick", "ps.aa.controller.activate(this)"); 
      el.setAttribute("style", "border: none;");
      imgEl.setAttribute("style", "padding-right: 2px; border: none;");
   }
   imgEl.setAttribute("src", imgPath + objId.getImagePath());   
   if(label != null)
      imgEl.setAttribute("title", label);
   el.appendChild(imgEl);
   
   return el
}

/**
 * Force the mouse over by putting an invisible layer under the
 * the mouse and then removing it.
 */
ps.DivActionHelper._forceMouseOver = function()
{
   //window.setTimeout("ps.DivActionHelper._handleInvisibleLayer(true)", 10);

}

/**
 * Handle showing and hiding the invisible layer
 */
ps.DivActionHelper._handleInvisibleLayer = function(show)
{
   var div = document.getElementById("rxInvisibleLayer");
   if(show)
   {
      div.style.visibility = "visible";
      div.style.top = (ps.DivActionHelper.mouseOverInfo.y)+ "px";
      div.style.left = (ps.DivActionHelper.mouseOverInfo.x) + "px";
      window.setTimeout("ps.DivActionHelper._handleInvisibleLayer(false)", 10);
   }
   else
   {
      div.style.visibility = "hidden";
      div.style.top = "-10px";
      div.style.left = "-10px";
   }
}

/**
 * Helper function to copy an array to a new array object
 */
ps.DivActionHelper._copyArray = function(arr)
{
   var nArr = new Array();
   for(i = 0; i < arr.length; i++)
      nArr[i] = arr[i];
   return nArr;
} 

// Subscribe to DND topics so we know when DND starts and finishes
dojo.event.topic.subscribe("dragStart", null, ps.DivActionHelper._onDNDStart);
dojo.event.topic.subscribe("dragEnd", null, ps.DivActionHelper._onDNDStop);

