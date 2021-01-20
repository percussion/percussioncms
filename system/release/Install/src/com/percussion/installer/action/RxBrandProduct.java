/******************************************************************************
 *
 * [ RxBrandProduct.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.installer.action;

import com.percussion.install.Code;
import com.percussion.install.CodeException;
import com.percussion.installanywhere.RxIAAction;
import com.percussion.installer.RxVariables;
import com.percussion.installer.model.RxBrandModel;


/**
 * This action will brand the product with the brand code in the installer's
 * property file.
 */
public class RxBrandProduct extends RxIAAction
{
   /**
    * Constructs an {@link RxBrandProduct}
    */
   public RxBrandProduct()
   {
      super();
   }
   
   @Override
   public void execute()
   {
      Code code = RxBrandModel.fetchBrandCode();
      if (code == null)
      {
         RxLogger.logInfo("RxBrandProduct : Brand code is null");
         return;
      }
      try
      {
         //get the root dir
         String strRootDir = getInstallValue(RxVariables.INSTALL_DIR);
         code.brand(strRootDir);
      }
      catch (CodeException ex)
      {
         RxLogger.logInfo("ERROR : " + ex.getMessage());
         RxLogger.logInfo(ex);
      }
   }
}
