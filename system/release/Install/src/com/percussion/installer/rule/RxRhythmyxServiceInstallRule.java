/******************************************************************************
 *
 * [ RxRhythmyxServiceInstallRule.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.installer.rule;

import com.percussion.install.InstallUtil;
import com.percussion.installanywhere.RxIARule;
import com.percussion.installer.RxVariables;
import com.percussion.installer.action.RxLogger;;
import org.apache.commons.lang.NotImplementedException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


/**
 * This class is used to determine if Rhythmyx Service Name/Deciption panel
 * should be displayed to the user. The Rhythmyx Service Name/Description panel
 * is displayed if the "installation.properties" file in "rxconfig/Installer"
 * directory does not have the "rhythmyxSvcName" key or if this key is empty
 * or the service name specified by this key is not installed as a service.
 */
public class RxRhythmyxServiceInstallRule extends RxIARule
{
   @Override
   public boolean evaluate()
   {
      String installDir = getInstallValue(RxVariables.INSTALL_DIR);
      
      if ((installDir == null) || (installDir.trim().length() == 0))
         return false;
      
      if (!installDir.endsWith(File.separator))
         installDir += File.separator;
      
      String strPropFile = installDir + m_propertyFileName;
      File propFile = new File(strPropFile);
      if (!propFile.exists())
      {
         RxLogger.logInfo("file does not exist : " + strPropFile);
         return false;
      }
      
      InputStream in = null;
      
      try
      {
         in = new FileInputStream(propFile);
         Properties prop = new Properties();
         prop.load(in);
         String svcName = prop.getProperty(InstallUtil.RHYTHMYX_SVC_NAME);
         if ((svcName == null) || (svcName.trim().length() == 0))
            return false;
         
         //check if a service is installed with name same as svcName
         return checkSvcNameExists(svcName);
      }
      catch (Exception e)
      {
         RxLogger.logInfo(
               "Exception in RxRhythmyxServiceInstallRule : " + e.getMessage());
         RxLogger.logInfo(e);
         return false;
      }
      finally
      {
         if (in != null)
         {
            try
            {
               in.close();
            }
            catch (IOException e)
            {
            }
         }
      }
   }
   
   /**
    * Used to determine if a service key exists in the registry.
    * 
    * @param svcName the name of the service, may not be <code>null</code> or
    * empty.
    * 
    * @return <code>true</code> if the service name exists, <code>false</code>
    * otherwise.
    */
   public static boolean checkSvcNameExists(String svcName)
   {
       throw new NotImplementedException("No longer use JNI registry methods need to replace");

   }
   
   /***************************************************************
    * Mutators and Accessors
    ***************************************************************/
   
   /**
    * Accessor for the Property File Name property.
    *
    * @return the property file name relative to the install root,
    * never <code>null</code> or empty
    */
   public String getPropertyFileName()
   {
      return m_propertyFileName;
   }
   
   /**
    * Mutator for the Property File Name property.
    *
    * @param propertyFileName the property file name,
    * never <code>null</code> or empty
    * @throws IllegalArgumentException if propertyFileName is <code>null</code>
    * or empty
    */
   public void setPropertyFileName(String propertyFileName)
   {
      if ((propertyFileName == null) || (propertyFileName.trim().length() == 0))
         throw new IllegalArgumentException(
         "propertyFileName may not be null or empty");
      m_propertyFileName = propertyFileName;
   }
   
   /**
    * The path of the installation.properties file relative to the root
    * installation directory. This properties file contains the Rhythmyx
    * Service Name and Description.
    */
   private String m_propertyFileName =
      "rxconfig/Installer/installation.properties";
   
   /**
    * The parent key for the Rhythmyx service.
    */
   private static final String SERVICE_KEY =
      "SYSTEM\\CurrentControlSet\\Services\\";
}
