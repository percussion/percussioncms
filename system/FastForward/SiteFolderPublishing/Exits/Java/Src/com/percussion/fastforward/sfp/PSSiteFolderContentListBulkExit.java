/* ****************************************************************************
 *
 * [ PSSiteFolderContentListBulkExit.java ]
 * 
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.fastforward.sfp;

import com.percussion.server.IPSRequestContext;

import java.util.Set;

/**
 * This Exit uses {@link PSSiteFolderCListBulk}to generate the content list for
 * a specified site folder.
 * 
 * @see PSSiteFolderCListBulk
 */
public class PSSiteFolderContentListBulkExit extends
      PSSiteFolderContentListBaseExit
{
   // implements the abstract method getSiteFolderCListObject()
   protected PSSiteFolderCListBase getSiteFolderCListInstance(
         IPSRequestContext request, boolean isIncremental,
         String publishableContentValidValues, String contentResourceName,
         int maxRowsPerPage, String protocol, String host, String port, 
         Set paramSetToPass)
   {
      return new PSSiteFolderCListBulk(request, isIncremental,
            publishableContentValidValues, contentResourceName, maxRowsPerPage,
            protocol, host, port, paramSetToPass);
   }
}