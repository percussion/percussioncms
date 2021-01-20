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

import com.percussion.install.PSLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;


/**
 * This is used to update an existing property in a properties file or to add a
 * new property to a properties file.
 *
 *<br>
 * Example Usage:
 *<br>
 *<pre>
 *
 * First set the taskdef:
 *
 *  <code>
 *  &lt;taskdef name="updatePropFileWizAction"
 *              class="com.percussion.ant.install.PSUpdatePropFileWizAction"
 *              classpathref="INSTALL.CLASSPATH"/&gt;
 *  </code>
 *
 * Now use the task to set the properties.
 *
 *  <code>
 *  &lt;updatePropFileWizAction onlyIfExists="true"
 *                  propertyFile="C:/Rhythmyx/file.properties"
 *                  propertyName="newProperty"
 *                  propertyValue="newValue"/&gt;
 *  </code>
 *
 * </pre>
 *
 */
public class PSUpdatePropFileWizAction extends PSAction
{
   // see base class
   @Override
   public void execute()
   {
      String propFile = propertyFile;
      String propName = propertyName;
      String propValue = propertyValue;

      propFile = propFile.trim();
      File f = new File(propFile);
      if (!f.isFile())
      {
         PSLogger.logInfo("Properties file does not exist : " + propFile);
         return;
      }
      Properties p = new Properties();
      FileInputStream fis = null;
      FileOutputStream fos = null;
      try
      {
         fis = new FileInputStream(propFile);
         if (fis == null)
         {
            PSLogger.logInfo("Failed to open input stream for file : "
                  + propFile);
            return;
         }
         p.load(fis);
         if (onlyIfExists)
         {
            String value = p.getProperty(propName);
            if (value == null)
            {
               PSLogger.logInfo("Property does not exist : " + propName +
               " onlyIfExists = true, not updating.");
               return;
            }
         }
         if (propValue == null)
            propValue = "";
         p.setProperty(propName, propValue);
         fos = new FileOutputStream(propFile);
         if (fos == null)
         {
            PSLogger.logInfo("Failed to open output stream for file : "
                  + propFile);
            return;
         }
         p.store(fos, null);
         return;
      }
      catch (Exception e)
      {
         PSLogger.logInfo("ERROR : " + e.getMessage());
         PSLogger.logInfo(e);
      }
      finally
      {
         try
         {
            if (fis != null)
               fis.close();
         }
         catch (Exception e)
         {
         }
         try
         {
            if (fos != null)
               fos.close();
         }
         catch (Exception e)
         {
         }
      }
   }

   /*************************************************************************
    * Properties Accessors and Mutators
    *************************************************************************/

   /**
    * Returns the absolute property file path.
    * @return the absolute property file path, never <code>null</code>
    * or empty.
    */
   public String getPropertyFile()
   {
      return propertyFile;
   }

   /**
    * Sets the absolute property file path.
    * @param propertyFile the absolute property file path,
    * may not be <code>null</code> or empty
    */
   public void setPropertyFile(String propertyFile)
   {
      if ((propertyFile == null) ||
            (propertyFile.trim().length() < 1))
         throw new IllegalArgumentException(
         "propertyFile may not be null or empty");
      this.propertyFile = propertyFile;
   }

   /**
    * Returns the <code>boolean</code> indicating if the property should be
    * updated only if already exists.
    * @return <code>true</code> if the specified property must be updated only if
    * it already exists but should not be added if it does not exist.
    * <code>false</code> if the specified property must be updated if it
    * already exists else should be added if it does not exist.
    */
   public boolean getOnlyIfExists()
   {
      return onlyIfExists;
   }

   /**
    * Sets the the <code>boolean</code> indicating if the property should be
    * updated only if already exists.
    * @param onlyIfExists <code>true</code> if the specified property must be
    * updated only if it already exists but should not be added if it does not
    * exist. <code>false</code> if the specified property must be updated if it
    * already exists else should be added if it does not exist.
    */
   public void setOnlyIfExists(boolean onlyIfExists)
   {
      this.onlyIfExists = onlyIfExists;
   }

   /**
    * Returns the key for the property to be updated.
    * @return the key for the property to be updated, never <code>null</code>
    * or empty.
    */
   public String getPropertyName()
   {
      return propertyName;
   }

   /**
    * Sets the key for the property to be updated.
    * @param propertyName the key for the property to be updated,
    * may not be <code>null</code> or empty
    */
   public void setPropertyName(String propertyName)
   {
      if ((propertyName == null) ||
            (propertyName.trim().length() < 1))
         throw new IllegalArgumentException(
         "propertyName may not be null or empty");
      this.propertyName = propertyName;
   }

   /**
    * Returns the value for the property to be updated.
    * @return the value for the property to be updated, may be <code>null</code>
    * or empty.
    */
   public String getPropertyValue()
   {
      return propertyValue;
   }

   /**
    * Sets the value for the property to be updated.
    * @param propertyValue the value for the property to be updated,
    * may be <code>null</code> or empty. If <code>null</code> then set to empty.
    */
   public void setPropertyValue(String propertyValue)
   {
      if (propertyValue == null)
         propertyValue = "";
      this.propertyValue = propertyValue;
   }

   /*************************************************************************
    * Properties
    *************************************************************************/

   /**
    * absolute file path of the properties file,
    * never <code>null</code>, empty until setter is called.
    */
   private String propertyFile = "";

   /**
    * if <code>true</code> then updates the property if the specified property
    * already exists but does not add the property if it does not exist.
    * if <code>false</code> then updates the property if the specified property
    * already exists else adds the property.
    */
   private boolean onlyIfExists = false;

   /**
    * key for the property to be updated, never <code>null</code> or empty
    * The value "CE_TemplateDirectory" is only a sample value used to show the usage,
    * actual value will be set using the Installshield UI.
    */
   private String propertyName = "CE_TemplateDirectory";

   /**
    * value for the property to be updated, may be <code>null</code> or empty
    * The value "rx_ceTemplates" is only a sample value used to show the usage,
    * actual value will be set using the Installshield UI.
    */
   private String propertyValue = "rx_ceTemplates";

}

