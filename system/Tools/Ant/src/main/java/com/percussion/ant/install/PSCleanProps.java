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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.ant.install;

import com.percussion.install.PSLogger;
import com.percussion.util.PSProperties;

import java.io.File;
import java.io.FileOutputStream;

/**
 * PSCleanProps removes the provided list of properties from the given
 * properties file of the current installation.
 *
 *<br>
 * Example Usage:
 *<br>
 *<pre>
 *
 * First set the taskdef:
 *
 *  <code>
 *  &lt;taskdef name="cleanProps"
 *              class="com.percussion.ant.install.PSCleanProps"
 *              classpathref="INSTALL.CLASSPATH"/&gt;
 *  </code>
 *
 * Now use the task to remove the specified properties from the given file.
 *
 *  <code>
 *  &lt;cleanProps properties="prop1,prop2,prop3"
 *                 propertiesFile="file.properties"/&gt;
 *  </code>
 *
 * </pre>
 *
 */
public class PSCleanProps extends PSAction
{
   // see base class
   @Override
   public void execute()
   {
      FileOutputStream out = null;

      try
      {
         PSLogger.logInfo("Cleaning " + propertiesFile);

         File propertyFile = new File(getRootDir() + File.separator
               + propertiesFile);
         String propertyPath = propertyFile.getAbsolutePath();

         if (!propertyFile.exists())
         {
            PSLogger.logInfo(propertyPath + " does not exist");
            return;
         }

         PSProperties props = new PSProperties(propertyPath);

         removeProperties(props);

         out = new FileOutputStream(propertyPath);

         props.store(out, null);
      }
      catch (Exception e)
      {
         PSLogger.logError("Failed to remove properties from "
               + propertiesFile);
         PSLogger.logError("Exception : " + e.getMessage());
      }
      finally
      {
         try
         {
            if (out != null)
               out.close();
         }
         catch (Exception e)
         {
         }
      }

   }

   /**
    * Removes specified properties from supplied PSProperties object.
    *
    * @param props PSProperties object, assumed not <code>null</code>.
    */
   private void removeProperties(PSProperties props)
   {
      for (int i = 0; i < properties.length; i++)
         props.remove(properties[i]);
   }

   /**************************************************************************
    * Bean property Accessors and Mutators
    **************************************************************************/

   /**
    *  Returns the properties to remove
    *
    *  @return the names of the properties to be removed, never <code>null</code>,
    *  may be an empty array.
    */
   public String[] getProperties()
   {
      return properties;
   }

   /**
    *  Sets the properties to remove
    *
    *  @param properties the names of the properties to be removed,
    *  never <code>null</code>, may be empty.
    */
   public void setProperties(String properties)
   {
      this.properties = convertToArray(properties);
   }

   /**
    *  Returns the properties file
    *
    *  @return the relative location of the properties file, never
    *  <code>null</code>, may empty.
    */
   public String getPropertiesFile()
   {
      return propertiesFile;
   }

   /**
    *  Sets the properties file
    *
    *  @param propsFile the relative location of the properties file,
    *  never <code>null</code>, may be empty.
    */
   public void setPropertiesFile(String propsFile)
   {
      propertiesFile = propsFile;
   }

   /**************************************************************************
    * Bean properties
    **************************************************************************/

   /**
    * Properties which should be removed from server.properties,
    * never <code>null</code>, may be empty
    */
   private String[] properties = new String[0];

   /**
    * Location of the properties file relative to the Rhythmyx root, never
    * <code>null</code>, may be empty
    */
   private String propertiesFile = "";

}






