/******************************************************************************
 *
 * [ RxUpgradePluginsModel.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.installer.model;

import com.percussion.install.RxInstallerProperties;
import com.percussion.install.RxUpgrade;
import com.percussion.installanywhere.IPSProxyLocator;
import com.percussion.installanywhere.RxIAModel;
import com.percussion.installer.action.RxLogger;
import com.percussion.util.IOTools;

import java.io.File;
import java.io.IOException;


/**
 * This model represents a panel/console that displays an information
 * message if any errors were generated while executing the post-upgrade
 * plugins.
 */
public class RxUpgradePluginsModel extends RxIAModel
{
   /**
    * Constructs an {@link RxUpgradePluginsModel} object.
    *  
    * @param locator the proxy locator which will retrieve the proxy used to
    * interact with the InstallAnywhere runtime platform.
    */
   public RxUpgradePluginsModel(IPSProxyLocator locator)
   {
      super(locator);
      setHideOnUpgrade(false);
   }
   
   @Override
   public boolean queryEnter()
   {  
      if (!initDisplayMessage())
         return false;
      else
         return super.queryEnter();
   }
      
   @Override
   public boolean queryExit()
   {
      return super.queryExit();
   }

   @Override
   public String getTitle()
   {
      return "";
   }
   
   /**
    * Searches the post-upgrade plugin log files for exceptions and initializes
    * the display message accordingly.
    * 
    * @return <code>true</code> if errors were found, <code>false</code>
    * otherwise.
    */
   private boolean initDisplayMessage()
   {
      boolean errors = false;
      String errMsg = RxInstallerProperties.getString("pluginErrMsg") + "\n\n";
      File postLogFileDir = new File(RxUpgrade.getPostLogFileDir());
      if (postLogFileDir.exists())
      {
         File[] logFiles = postLogFileDir.listFiles();
         for (int i = 0; i < logFiles.length; i++)
         {
            File logFile = logFiles[i];
            try
            {
               String logFileContent = IOTools.getFileContent(logFile);
               if (logFileContent.indexOf("Exception") != -1)
               {
                  errMsg += logFile.getName() + "\n";
                  errors = true;
               }
               
            }
            catch (IOException e)
            {
               e.printStackTrace();
               RxLogger.logError("RxUpgradePluginsModel#initDisplayMessage : " +
                     e.getMessage());
            }
         }
      }
      
      if (errors)
      {
         m_displayMessage += errMsg;
         m_displayMessage += "\n" + "These files can be found at the "
            + "following location: " + postLogFileDir.getAbsolutePath();
         m_displayMessage += "\n\n"
            + RxInstallerProperties.getString("docHelp");
         m_displayMessage += "\n\n"
            + RxInstallerProperties.getString("northAmericaSupport");
         m_displayMessage += "\n\n"
            + RxInstallerProperties.getString("europeSupport");
      }
      return errors;
   }
   
   /*************************************************************************
   * Accessors/mutators for private members   
   *************************************************************************/
      
   /**
    * Retrieves the value for a given persisted property.
    * 
    * @param s property name.
    * 
    * @return object value associated with the given property.
    */
   public Object getPropertyValue(String s)
   {
      return getValue(s);
   }
   
   /**
    * Getter that returns display message.
    * @return plugin files display message.
    */ 
   public String getDisplayMessage()
   {
       return m_displayMessage;
   }

   /*************************************************************************
   * Static Variables
   *************************************************************************/
               
   /**************************************************************************
    * Variables
    **************************************************************************/
   
   /**
    * Display message
    */
    private String m_displayMessage = "";
}
