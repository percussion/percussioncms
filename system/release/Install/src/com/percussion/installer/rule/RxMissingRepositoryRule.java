/******************************************************************************
 *
 * [ RxMissingRepositoryRule.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.installer.rule;

import com.percussion.install.RxFileManager;
import com.percussion.installanywhere.RxIARule;
import com.percussion.installer.RxVariables;

import java.io.File;


/**
 * This rule will evaluate to <code>true</code> if the
 * rxrepository.properties is missing.
 */
public class RxMissingRepositoryRule extends RxIARule
{
   @Override
   protected boolean evaluate()
   {
      //get the root dir
      String strRootDir = getInstallValue(RxVariables.INSTALL_DIR);
            
      if(strRootDir.trim().length() > 0)
      {
         RxFileManager fileManager = new RxFileManager(strRootDir);
         File repFile = new File(fileManager.getRepositoryFile());
         if(!repFile.exists())
            return true;
      }
      
      return false;
   }
}
