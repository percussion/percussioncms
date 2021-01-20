/*[ RxLogger.java ]**********************************************************
 *
 * COPYRIGHT (c) 2004 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.installer.action;

import com.percussion.install.PSLogger;
import com.percussion.installanywhere.RxIAAction;
import com.percussion.installer.RxVariables;


/**
 * This is a wrapper action which delegates to {@link PSLogger} for all custom
 * code logging.
 */
public class RxLogger extends RxIAAction
{
   /**
    * See this method of <code>RxIAAction</code> for detailed
    * information.
    */
   @Override
   public void execute()
   {
      PSLogger.init(getInstallValue(RxVariables.INSTALL_DIR));
   }
   
   /*************************************************************************
    * Static Public functions
    *************************************************************************/
   
   /**
    * See {@link PSLogger#logInfo(Object)} for details.
    * 
    * @param log the string to be logged later in the specified log file,
    * may be <code>null</code> or empty.
    */
   static public void logInfo(Object log)
   {
      PSLogger.logInfo(log);
   }
   
   /**
    * See {@link PSLogger#logWarn(Object)} for details.
    *
    * @param log the string to be logged later in the specified log file,
    * may be <code>null</code> or empty.
    */
   static public void logWarn(Object log)
   {
      PSLogger.logWarn(log);
   }
   
   /**
    * See {@link PSLogger#logError(Object)} for details.
    *
    * @param log the string to be logged later in the specified log file,
    * may be <code>null</code> or empty.
    */
   static public void logError(Object log)
   {
      PSLogger.logError(log);
   }   
}


