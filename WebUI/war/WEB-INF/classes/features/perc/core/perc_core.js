/******************************************************************************
 *
 * [ perc_core.js ]
 *
 * COPYRIGHT (c) 1999 - 2010 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 * A shindig feature that maps thne jQuery variable in the gadget iframe context
 * to the top level jQuery instance which allows access to all the client code
 * in cm1.
 *
 ******************************************************************************/
 
// Ensure gadgets.perc namespace exists
var gadgets = gadgets || {};
gadgets.perc = gadgets.perc || {};

// Reference top level jQuery object instance
var percJQuery = null;
var parent = window.parent;
while(parent)
{
    if(parent && parent.jQuery && parent.jQuery.PercNavigationManager)
    {
        percJQuery = parent.jQuery;
        break;
    }
    parent = parent.parent;
}

//fix for IE where window.parent is becoming undefined for some reason.
if(percJQuery == null)
{
    var ourwin = window.top;
    if(ourwin && ourwin.jQuery && ourwin.jQuery.PercNavigationManager)
    {
        percJQuery = ourwin.jQuery;
        window.parent = ourwin;
    }
    else
    {
        for(var cwin in ourwin.frames)
        {
            if(cwin && cwin.jQuery && cwin.jQuery.PercNavigationManager)
            {
                percJQuery = cwin.jQuery;
                window.parent = cwin;
                break;
            }
        }
    }
}
//fix for IE ends

// Throw alert if top level jQuery does not exist, should not happen.
if(percJQuery == null)
{
   alert("Error!! perc jQuery does not exist.");
}
