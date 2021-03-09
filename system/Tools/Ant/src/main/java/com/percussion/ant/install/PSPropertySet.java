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
import com.percussion.util.PSProperties;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * PSPropertySet is a task to set properties on a property file.
 *
 *<br>
 * Example Usage:
 *<br>
 *<pre>
 *
 * First set the taskdef:
 *
 *  <code>
 *  &lt;taskdef name="propertySet"
 *              class="com.percussion.ant.install.PSPropertySet"
 *              classpathref="INSTALL.CLASSPATH"/&gt;
 *  </code>
 *
 * Now use the task to set the properties.
 *
 *  <code>
 *  &lt;propertySet propertyFile="C:/Rhythmyx/file.properties"
 *                  propertyName="newProperty"
 *                  propertyValue="newValue"/&gt;
 *  </code>
 *
 * </pre>
 *
 */
public class PSPropertySet extends PSAction
{
   // see base class
   @Override
   public void execute()
   {
      try
      {
         File propFile = new File(m_strPropertyFile);
         if(propFile.exists())
         {
            PSProperties props = new PSProperties(m_strPropertyFile);

            props.setProperty(m_strPropertyName,
                  m_strPropertyValue);

            //save the properties
            try(FileOutputStream out =
               new FileOutputStream(m_strPropertyFile)) {
               props.store(out, null);
            }
         }
      }
      catch(FileNotFoundException e)
      {
         PSLogger.logInfo("ERROR : " + e.getMessage());
         PSLogger.logInfo(e);
      }
      catch(IOException ioe)
      {
         PSLogger.logInfo("ERROR : " + ioe.getMessage());
         PSLogger.logInfo(ioe);
      }
   }

   /*************************************************************************
    * Property Accessors and Mutators
    *************************************************************************/
   /**
    * Accessor for the Property File
    */
   public String getPropertyFile()
   {
      return m_strPropertyFile;
   }

   /**
    * Mutator for the Property File.
    */
   public void setPropertyFile(String strPropertyFile)
   {
      m_strPropertyFile = strPropertyFile;
   }

   /**
    * Accessor for the Property Name
    */
   public String getPropertyName()
   {
      return m_strPropertyName;
   }

   /**
    * Mutator for the Property Name.
    */
   public void setPropertyName(String strPropertyName)
   {
      m_strPropertyName = strPropertyName;
   }

   /**
    * Accessor for the property value
    */
   public String getPropertyValue()
   {
      return m_strPropertyValue;
   }

   /**
    * Mutator for the property value
    */
   public void setPropertyValue(String strPropertyValue)
   {
      m_strPropertyValue = strPropertyValue;
   }

   /**************************************************************************
    * Properties
    *************************************************************************/

   /**
    *  The Property File
    */
   private String m_strPropertyFile = "";

   /**
    *  The property name
    */
   private String m_strPropertyName = "";

   /**
    * The property value
    */
   private String m_strPropertyValue = "";

}
