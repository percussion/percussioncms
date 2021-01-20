/******************************************************************************
 *
 * [ RxDeleteCPFiles.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.installer.action;

import com.percussion.installanywhere.RxIAAction;
import com.percussion.installer.rule.RxCPFilesFoundRule;

import java.io.File;
import java.util.List;


/**
 * This action deletes any .cp files found by {@link RxCPFilesFoundRule}.
 */
public class RxDeleteCPFiles extends RxIAAction
{
   @Override   
   public void execute()
   {
      try
      {
         List<String> cpFiles = RxCPFilesFoundRule.getCpFiles();

         for (String filePath : cpFiles)
         {
            File f = new File(filePath);
            
            RxLogger.logInfo("Deleting " + filePath);
            f.delete();
         }
      }
      catch (Exception e)
      {
         RxLogger.logError("RxDeleteCPFiles#execute : " + e.getMessage());
      }
   }
}






