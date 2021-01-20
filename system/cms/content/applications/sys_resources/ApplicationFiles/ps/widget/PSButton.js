
/******************************************************************************
 *
 * [ ps.widget.PSButton.js ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

dojo.provide("ps.widget.PSButton");

dojo.require("dojo.widget.Button");

/**
 * The Button class overriden to customize style sheet
 */
dojo.widget.defineWidget("ps.widget.PSButton", dojo.widget.Button,
{    
    templateCssPath: dojo.uri.moduleUri("ps", "widget/PSButton.css")    
});


