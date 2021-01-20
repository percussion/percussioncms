/******************************************************************************
 *
 * [ PSDeliveryTierInstallDir.java ]
 * 
 * COPYRIGHT (c) 1999 - 2012 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.installer.action;

import com.percussion.install.InstallUtil;
import com.percussion.install.RxFileManager;
import com.percussion.installanywhere.RxIAAction;
import com.percussion.installer.RxVariables;

import java.util.Properties;

/**
 * @author nate
 *
 */
public class PSDeliveryTierInstallDir extends RxIAAction
{
   @Override
   public void execute()
   {
      setPropertyName(getInstallValue(InstallUtil.getVariableName(
            getClass().getName(), PROPERTY_NAME_VAR)));
      
      try
      {
         //get a real/long file path to persist so that next time
         //we upgrade we can actually see a nice list of directories. 
         String rxRoot = RxFileManager.getLongRootDir();
         
         //Insure that the system program directory is current.
         RxFileManager.setProgramDir(getInstallValue(RxVariables.PROGRAMS_DIR));
         RxFileManager.setSystemInstallationPropertiesFile(RxFileManager.DTS_SYS_INSTALL_PROP);
         
         String installPropFile = RxFileManager.getSystemInstallationFile();
         Properties props = RxFileManager.loadProperties(installPropFile);
         
         String installDirs = props.getProperty(RxFileManager.INSTALL_PROP, "");
         installDirs = installDirs.trim();
         if (!installDirs.endsWith(";"))
            installDirs += ";";
         installDirs += rxRoot;
         
         props.setProperty(RxFileManager.INSTALL_PROP, installDirs);
         RxFileManager.saveProperties(props, installPropFile);

      }
      catch (Exception ex)
      {
         RxLogger.logInfo("ERROR : " + ex.getMessage());
         RxLogger.logInfo(ex);
      }
  }

  /*************************************************************************
   * Property Accessors and Mutators
   *************************************************************************/
  
  /**
   * Accessor for the property name property.
   * 
   * @return the name of the property to use to store the install directory.
   */
   public String getPropertyName()
   {
      return m_propertyName;
   }

  /**
   * Mutator for the property name property.
   * 
   * @param propertyName the name of the property to use to store the install
   * directory.
   */
   public void setPropertyName(String propertyName)
   {
      m_propertyName = propertyName;
   } 
   
  /**************************************************************************
  * Variables
  **************************************************************************/
 
   /**
    * The variable name for the property name parameter passed in via the IDE.
    */
   private static final String PROPERTY_NAME_VAR = "propertyName";
   
  /**************************************************************************
  * Properties
  *************************************************************************/
    
  /**
   * The installation directory property name.
   */
   private String m_propertyName = RxFileManager.INSTALL_PROP;
}
