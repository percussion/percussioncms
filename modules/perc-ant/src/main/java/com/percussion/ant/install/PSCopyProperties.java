/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.percussion.ant.install;

import com.percussion.install.PSLogger;
import com.percussion.util.PSProperties;

import java.io.File;
import java.io.FileOutputStream;

/**
 * PSCopyProperties copies the provided list of properties from the source
 * properties file to the destination properties file of the current
 * installation.  The associated values are also copied.
 *
 * <br>
 * Example Usage:
 * <br>
 * <pre>
 *
 * First set the taskdef:
 *
 *  <code>
 *  &lt;taskdef name="copyProperties"
 *              class="com.percussion.ant.install.PSCopyProperties"
 *              classpathref="INSTALL.CLASSPATH"/&gt;
 *  </code>
 *
 * Now use the task to copy the properties.
 *
 *  <code>
 *  &lt;copyFileAction destPropertiesFile="C:/Rhythmyx/file1.properties"
 *                     properties="property1,property2"
 *                     srcPropertiesFile="C:/Rhythmyx/file2.properties"/&gt;
 *  </code>
 *
 * </pre>
 *
 * */
public class PSCopyProperties extends PSAction
{
   // see base class
   @Override
   public void execute()
   {
      FileOutputStream out = null;

      try
      {
         PSLogger.logInfo("Copying properties from : " + srcPropertiesFile
               + " to : " + destPropertiesFile);

         String root = getRootDir();
         File srcPropertyFile = new File(root + File.separator
               + srcPropertiesFile);
         String srcPropertyPath = srcPropertyFile.getAbsolutePath();

         if (!srcPropertyFile.exists())
         {
            PSLogger.logInfo(srcPropertiesFile + " does not exist");
            return;
         }

         File destPropertyFile = new File(root + File.separator
               + destPropertiesFile);
         String destPropertyPath = destPropertyFile.getAbsolutePath();

         if (!destPropertyFile.exists())
         {
            PSLogger.logInfo(destPropertiesFile + " does not exist");
            return;
         }

         PSProperties srcProps = new PSProperties(srcPropertyPath);
         PSProperties destProps = new PSProperties(destPropertyPath);

         copyProperties(srcProps, destProps);

         out = new FileOutputStream(destPropertyPath);

         destProps.store(out, null);
      }
      catch (Exception e)
      {
         PSLogger.logError("Failed to copy properties from : "
               + srcPropertiesFile + " to : " + destPropertiesFile);
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
    * Copies specified properties from source to destination PSProperties
    * object.  Properties with empty values will be copied.
    *
    * @param srcProps source PSProperties object, assumed not <code>null</code>.
    * @param destProps destination PSProperties object, assumed not
    * <code>null</code>.
    */
   private void copyProperties(PSProperties srcProps, PSProperties destProps)
   {
      for (int i = 0; i < properties.length; i++)
      {
         String prop = properties[i];
         String propValue = srcProps.getProperty(prop);

         if (propValue != null)
            destProps.setProperty(prop, propValue);
         else
            PSLogger.logInfo("Could not find property " + prop);
      }
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
    *  Returns the source properties file
    *
    *  @return the relative location of the properties file, never
    *  <code>null</code>, may empty.
    */
   public String getSrcPropertiesFile()
   {
      return srcPropertiesFile;
   }

   /**
    *  Sets the source properties file
    *
    *  @param propsFile the relative location of the properties file,
    *  never <code>null</code>, may be empty.
    */
   public void setSrcPropertiesFile(String propsFile)
   {
      srcPropertiesFile = propsFile;
   }

   /**
    *  Returns the destination properties file
    *
    *  @return the relative location of the properties file, never
    *  <code>null</code>, may empty.
    */
   public String getDestPropertiesFile()
   {
      return destPropertiesFile;
   }

   /**
    *  Sets the destination properties file
    *
    *  @param propsFile the relative location of the properties file,
    *  never <code>null</code>, may be empty.
    */
   public void setDestPropertiesFile(String propsFile)
   {
      destPropertiesFile = propsFile;
   }

   /**************************************************************************
    * Bean properties
    **************************************************************************/

   /**
    * Properties which should be copied, never <code>null</code>, may be empty
    */
   private String[] properties = new String[0];

   /**
    * Location of the source properties file relative to the Rhythmyx root, never
    * <code>null</code>, may be empty
    */
   private String srcPropertiesFile = "";

   /**
    * Location of the destination properties file relative to the Rhythmyx root,
    * never <code>null</code>, may be empty
    */
   private String destPropertiesFile = "";

}






