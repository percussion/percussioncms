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
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.tools.ant.taskdefs.condition.Condition;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import java.io.File;

/**
 * This class is used to determine if a particular package configuration file
 * is empty.  A package configuration file is empty if it does not specify
 * configuration for any package elements.
 *
 * Returns <code>true</code> if the package configuration file specified by
 * <code>m_relativeFilePath</code> member variable is empty, <code>false</code>
 * otherwise.
 *
 * <br>
 * Example Usage:
 * <br>
 * <pre>
 *
 * First set the typedef:
 *
 *  <code>
 *  &lt;typedef name="PSPkgConfigFileEmptyCondition"
 *              class="com.percussion.ant.install.PSPkgConfigFileEmptyCondition"
 *              classpathref="INSTALL.CLASSPATH"/&gt;
 *  </code>
 *
 * Now use the task.
 *
 *  <code>
 *  &lt;condition property="IS_CONFIG_EMPTY"&gt;
 *     &lt;PSPkgConfigFileEmptyCondition
 *        relativeFilePath="rxconfig/Packages/DefaultConfigs/perc.SystemObjects_defaultConfig.xml"
 *  &lt;/condition&gt;
 *  </code>
 *
 * </pre>
 *
 */
public class PSPkgConfigFileEmptyCondition extends PSAction implements Condition
{
   /* (non-Javadoc)
    * @see org.apache.tools.ant.taskdefs.condition.Condition#eval()
    */
   public boolean eval()
   {
      String installDir = getRootDir();
      if ((installDir == null) || (installDir.trim().length() == 0))
      {
         return false;
      }

      if (!installDir.endsWith(File.separator))
      {
         installDir += File.separator;
      }
      
      String strCfgFile = installDir + m_relativeFilePath;
      File cfgFile = new File(strCfgFile);
      if (!cfgFile.exists())
      {
         PSLogger.logInfo("file does not exist : " + strCfgFile);
         return false;
      }
      
      try
      {
         DocumentBuilder db = PSXmlDocumentBuilder.getDocumentBuilder(false);
         Document doc = db.parse(cfgFile);
         if (doc == null)
         {
            return false;
         }
         
         Element root = doc.getDocumentElement();
         if (root == null)
         {
            return false;
         }
         
         NodeList nl = root.getElementsByTagName("SolutionConfig");
         if (nl == null)
         {
            return false;
         }
         
         int nodeListLen = nl.getLength();
         Element el = null;
         for (int i = 0; i < nodeListLen; i++)
         {
            el = (Element) nl.item(i);
            NodeList propertyElems = el.getElementsByTagName("property");
            NodeList propertySetElems = el.getElementsByTagName("propertySet");
            if (propertyElems.getLength() > 0 ||
                  propertySetElems.getLength() > 0)
            {
               return false;
            }
         }
      }
      catch (Exception e)
      {
         PSLogger.logError("Exception in PSPkgConfigFileEmptyCondition : "
            + e.getMessage());
         PSLogger.logError(e);
      }
   
      return true;
   }

  /***************************************************************
  * Mutators and Accessors
  ***************************************************************/

  /**
   * Returns the relative path of the package configuration file from the
   * installation directory.
   * @return the relative path of the package configuration file from the
   * installation directory, may be <code>null</code> or empty
   */
   public String getRelativeFilePath()
   {
      return m_relativeFilePath;
   }

   /**
    * Sets the relative path of the xml file from the installation directory.
    * @param relativeFilePath the relative path of the xml file from the
    * installation directory, never <code>null</code> or empty
    * @throw IllegalArgumentException if relativeFilePath is <code>null</code>
    * or empty
    */
   public void setRelativeFilePath(String relativeFilePath)
   {
      if ((relativeFilePath == null) || (relativeFilePath.trim().length() == 0))
         throw new IllegalArgumentException(
            "relativeFilePath may not be null or empty");
      this.m_relativeFilePath = relativeFilePath;
   }

   /**
    * Stores the relative path of the package configuration file.
    */
   private String m_relativeFilePath = 
      "rxconfig/Packages/DefaultConfigs/perc.SystemObjects_defaultConfig.xml";
 
}
