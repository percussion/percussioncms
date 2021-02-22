/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.ant.install;

import com.percussion.install.InstallUtil;
import com.percussion.install.PSLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * PSServerInstallEndPanel is a task that cleans up the installation
 * process.
 *
 * <br>
 * Example Usage:
 * <br>
 * <pre>
 *
 * First set the taskdef:
 *
 *  <code>
 *  &lt;taskdef name="serverInstallEndPanel"
 *              class="com.percussion.ant.PSServerInstallEndPanel"
 *              classpathref="INSTALL.CLASSPATH"/&gt;
 *  </code>
 *
 * Now use the task to clean up the installation process.
 *
 *  <code>
 *  &lt;serverInstallEndPanel
 *       propertyFileName="rxconfig/Installer/installation.properties"
 *       serviceDesc="Percussion Rhythmyx Server"
 *       serviceName="Rhythmyx Server"/&gt;
 *  </code>
 *
 * </pre>
 *
 */
public class PSServerInstallEndPanel extends PSAction
{
   // see base class
   @Override
   public void execute()
   {
      String svcName = serviceName;
      String installDir = getRootDir();

      if ((installDir == null) || (installDir.trim().length() == 0))
         return;

      if (!installDir.endsWith(File.separator))
         installDir += File.separator;

      String strPropFile = installDir + propertyFileName;
      File propFile = new File(strPropFile);
      if (!propFile.exists())
      {
         try
         {
            if(!propFile.createNewFile())
               throw new IOException();
         }
         catch(IOException ioe)
         {
            PSLogger.logInfo(
            "Failed to create installation.properties file");
            return;
         }
      }

      String svcDesc = serviceDesc;

      try
      {
         Properties prop = new Properties();

         try(FileInputStream fis = new FileInputStream(strPropFile)) {
            prop.load(fis);
         }

         prop.setProperty(InstallUtil.RHYTHMYX_SVC_NAME, svcName);
         if (!((svcDesc == null) || (svcDesc.trim().length() == 0)))
            prop.setProperty(InstallUtil.RHYTHMYX_SVC_DESC, svcDesc);

         try(FileOutputStream fos = new FileOutputStream(strPropFile)) {
            prop.store(fos, null);
         }
      }
      catch(Exception excp)
      {
         PSLogger.logInfo(excp.getMessage());
         PSLogger.logInfo(excp);
         return;
      }

   }

   /*************************************************************************
    * Properties Accessors and Mutators
    *************************************************************************/

   /**
    * Accessor for the Property File Name property.
    *
    * @return the property file name relative to the install root,
    * never <code>null</code> or empty
    */
   public String getPropertyFileName()
   {
      return propertyFileName;
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
      this.propertyFileName = propertyFileName;
   }

   /**
    * Returns the Rhythmyx Service Name
    * @return the Rhythmyx Service Name, never <code>null</code> or empty
    */
   public String getServiceName()
   {
      return serviceName;
   }

   /**
    * Sets the Rhythmyx Service Name
    * @param serviceName the Rhythmyx Service Name,
    * may not be <code>null</code> or empty
    * @throw IllegalArgumentException if serviceName is <code>null</code>
    * or empty
    */
   public void setServiceName(String serviceName)
   {
      if ((serviceName == null) || (serviceName.trim().length() == 0))
         throw new IllegalArgumentException(
         "serviceName may not be null or empty");
      this.serviceName = serviceName;
   }

   /**
    * Returns the Rhythmyx Service Description
    * @return the Rhythmyx Service Description, never <code>null</code> or empty
    */
   public String getServiceDesc()
   {
      return serviceDesc;
   }

   /**
    * Sets the Rhythmyx Service Description
    * @param serviceDesc the Rhythmyx Service Description,
    * may not be <code>null</code> or empty
    * @throw IllegalArgumentException if serviceDesc is <code>null</code>
    * or empty
    */
   public void setServiceDesc(String serviceDesc)
   {
      if ((serviceDesc == null) || (serviceDesc.trim().length() == 0))
         throw new IllegalArgumentException(
         "serviceDesc may not be null or empty");
      this.serviceDesc = serviceDesc;
   }

   /*************************************************************************
    * Static Public functions
    *************************************************************************/

   /*************************************************************************
    * Private functions
    *************************************************************************/

   /*************************************************************************
    * Properties
    *************************************************************************/

   /**
    * The path of the installation.properties file relative to the root
    * installation directory. This properties file contains the Rhythmyx
    * Service Name and Description.
    */
   private String propertyFileName =
      "rxconfig/Installer/installation.properties";

   /**
    * stores the Rhythmyx Service Name
    */
   private String serviceName = "Rhythmyx40";

   /**
    * stores the Rhythmyx Service Description
    */
   private String serviceDesc = "";

   /*************************************************************************
    * Static Variables
    *************************************************************************/

   /*************************************************************************
    * Private Variables
    *************************************************************************/

}
