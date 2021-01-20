/******************************************************************************
 *
 * [ RxWaitAction.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.installer.action;



import com.percussion.install.InstallUtil;
import com.percussion.installanywhere.RxIAAction;


/**
 * This action will wait or sleep for a specified number of seconds.
 */
public class RxWaitAction extends RxIAAction
{
   @Override 
   public void execute()
   {
      String strSeconds = (getInstallValue(InstallUtil.getVariableName(
            getClass().getName(), SECONDS_VAR)));
      
      try
      {
         int seconds = Integer.parseInt(strSeconds);
         Thread.sleep(seconds * 1000);
      }
      catch (Exception ie)
      {
         RxLogger.logInfo("ERROR : " + ie.getMessage());
         RxLogger.logInfo(ie);
      }
   }
   
   /**
    * The variable name for the seconds parameter passed in via the IDE.
    */
   private static final String SECONDS_VAR = "seconds";
}
