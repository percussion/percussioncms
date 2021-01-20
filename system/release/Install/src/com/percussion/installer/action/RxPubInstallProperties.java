/******************************************************************************
 *
 * [ RxPubInstallProperties.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.installer.action;

import com.percussion.installanywhere.RxIAAction;
import com.percussion.installer.RxVariables;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;


/**
 * This action is used to load the publisher properties from pre-6.6
 * installations.
 */
public class RxPubInstallProperties extends RxIAAction
{
   @Override
   public void execute()
   {
      loadInstallProperties();
   }
   
   /*************************************************************************
    * Properties Accessors and Mutators
    *************************************************************************/
   
   /**
    * Returns the absolute path of the properties file (rxpublisher.properties)
    * @return the absolute path of the properties file (rxpublisher.properties)
    * may be <code>null</code>, never empty if not <code>null</code>
    */
   public String fetchPublisherPropertiesFile()
   {
      String strPropFile = getInstallValue(RxVariables.INSTALL_DIR) +
         File.separator + m_propertyFileName;
      return strPropFile;
   }
      
   /*************************************************************************
    * Private functions
    *************************************************************************/
   
   /**
    * Loads the publisher service name and description properties stored in the
    * rxpublisher.properties file.  Stores the values of these properties as
    * installer variables.
    */
   protected void loadInstallProperties()
   {
      // read the properties file
      String propFile = fetchPublisherPropertiesFile();
      if (propFile == null)
         return;
      
      FileInputStream fis = null;
      try
      {
         File file = new File(propFile);
         if (file.exists())
         {
            fis = new FileInputStream(file);
            m_installProps.load(fis);
         }
         else
            return;
      }
      catch (Exception e)
      {
         RxLogger.logInfo("exception : " + e.getMessage());
      }
      finally
      {
         if (fis != null)
         {
            try
            {
               fis.close();
            }
            catch (Exception e)
            {
               // no -op
            }
         }
      }
      
      String svcName = m_installProps.getProperty(PUBLISHER_SVC_NAME);
      if (!((svcName == null) || (svcName.trim().length() == 0)))
         setInstallValue(RxVariables.RX_PUB_SERVICE_NAME, svcName);
         
      String svcDesc = m_installProps.getProperty(PUBLISHER_SVC_DESC);
      if (!((svcName == null) || (svcName.trim().length() == 0)))
         setInstallValue(RxVariables.RX_PUB_SERVICE_DESC, svcDesc);
   }
     
   /**
    * The name of the file to map the properties to.  This file will be
    * relative to the root install directory like "C:\Rhythmyx"
    */
   private String m_propertyFileName = "rxconfig"
      + File.separator  + "Publisher"
      + File.separator  + "rxpublisher.properties";
     
   /**
    * Used for loading the installation properties from rxpublisher.properties
    * file and storing it back to the same file at the end of installation.
    */
   private Properties m_installProps = new Properties();
   
   /**
    * key for storing the publisher service name in rxpublisher.properties file
    */
   private static String PUBLISHER_SVC_NAME = "publisherSvcName";

   /**
    * key for storing the publisher service description in
    * rxpublisher.properties file
    */
   private static String PUBLISHER_SVC_DESC = "publisherSvcDesc";
}
