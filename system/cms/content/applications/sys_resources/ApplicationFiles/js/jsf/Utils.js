/******************************************************************************
 *
 * [Utils.js ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

/*
 * A file to keep java script utility functions for jsf files.
 */
/**
 * Namespace for this file. All functions should use this name space.
 */
psJsfUtil = new function(){};

/**
 * Utility function to detect ie6 browser. 
 * Right now we just need IE6 detection, instead of adding a heavy weigth
 * browser detection file I just added this method. 
 * If we need more browser detection methods, we can replace it with a 
 * separate file.
 */
psJsfUtil.isExplorer6 = function() 
{
	var appVer = navigator.appVersion;
	appVer = appVer.split(';');
	if(appVer[1] == ' MSIE 6.0') {
		return true;
	}				
}

/**
 * This is a workaround for IE6 hover issue, The menus under publishing
 * design and runtime were not showing up as the menus are rendered using
 * the css with hover styles on LI and UL. IE6 does not supports hover on
 * only anchor links. (Rx bug - RX-14196)
 */
psJsfUtil.menuHoverIE6Fix = function() 
{     
   var sfEls = document.getElementById("psPubMenu").getElementsByTagName("LI");     
   for (var i=0; i<sfEls.length; i++) 
   {         
      sfEls[i].onmouseover=function() 
      {             
         this.className+=" elemhover";         
      }         
      sfEls[i].onmouseout=function() 
      {             
         this.className=this.className.replace(new RegExp(" elemhover\\b"), "");         
      }     
   } 
} 

/**
 * Workaround to fix the Trinidad navagationTree nodes
 * not rendering on IE6. The unicode chars will be replaced by an image.
 */
psJsfUtil.navTreeNodeIE6Fix = function()
{
   var imageDir = "/Rhythmyx/sys_resources/trinidad/adf/images/";
   var expandedImg = "expanded_IE6.gif";
   var collapsedImg = "collapsed_IE6.gif";
   var imgElPrefix = "<img align=\"middle\" height=\"16\" width=\"16\" src=\"";
   var nList = document.getElementsByTagName("a");
   for(i = 0; i < nList.length; i++)
   {
      var val = nList[i].innerHTML;
      if(val == "\u25bc")
      {
         nList[i].innerHTML =  imgElPrefix + imageDir + expandedImg  +"\">";
      }
      else if(val == "\u25ba")
      {
         nList[i].innerHTML = imgElPrefix + imageDir + collapsedImg  +"\">";
      }
    }
 }

/**
 * Attch the function psJsfUtil.menuHoverIE6Fix if it is IE6 to window onload.
 */
if (window.attachEvent && psJsfUtil.isExplorer6()) 
{
   window.attachEvent("onload",psJsfUtil.menuHoverIE6Fix); 
   window.attachEvent("onload",psJsfUtil.navTreeNodeIE6Fix); 
}
