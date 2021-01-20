/******************************************************************************
 *
 * [ PSSiteFolderContentListExit.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.fastforward.sfp;

import com.percussion.server.IPSRequestContext;

import java.util.Set;

/**
 * This Exit is used to generate the content list for a specified site folder.
 * 
 * @deprecated This Exit may have poor performance with large amount of items. 
 *             Use {@link PSSiteFolderContentListBulkExit} instead.
 */
public class PSSiteFolderContentListExit extends
      PSSiteFolderContentListBaseExit
{
   // implements the abstract method getSiteFolderCListObject()
   protected PSSiteFolderCListBase getSiteFolderCListInstance(
         IPSRequestContext request, boolean isIncremental,
         String publishableContentValidValues, String contentResourceName,
         int maxRowsPerPage, String protocol, String host, String port,
         Set paramSetToPass)
   {
      return new PSSiteFolderContentList(request, isIncremental,
            publishableContentValidValues, contentResourceName, maxRowsPerPage,
            protocol, host, port,
            paramSetToPass);
   }
}
