/******************************************************************************
 *
 * [ ps.content.ContentPaneProgress.js ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
dojo.provide("ps.widget.ContentPaneProgress");
 
/**
 * Shows "wait" cursor while {@link dojo.widget.ContentPane} loads.
 *
 * @author Andriy Palamarchuk
 * @param contentPane the content pane widget to show content loading for.
 * Not null.
 * @constructor
 */
ps.widget.ContentPaneProgress = function (contentPane)
{
   dojo.lang.assert(contentPane
         && contentPane.onDownloadStart
         && contentPane.onDownloadEnd, "Content pane must be defined");

   // the content pane element
   var element = contentPane.domNode;

   // the cursor 
   var originalCursor = element.style.cursor;
   var WAIT_CURSOR = "wait";

   // whether indication of progress is already shown
   function isIndicating()
   {
      return element.style.cursor === WAIT_CURSOR;
   }

   // show progress indicator
   function start()
   {
      if (isIndicating())
      {
         return;
      }
      element.style.cursor = WAIT_CURSOR;
   }
   
   // in case loading is already in progress ...
   if (!contentPane.isLoaded)
   {
      start();
   }
   dojo.event.connect(contentPane, "onDownloadStart", function() {start()});
   dojo.event.connect(contentPane, "onDownloadEnd", function()
   {
      element.style.cursor =
            originalCursor === WAIT_CURSOR ? "" : originalCursor;
   });
}
