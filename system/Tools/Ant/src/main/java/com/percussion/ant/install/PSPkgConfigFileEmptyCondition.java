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
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;

import org.apache.tools.ant.taskdefs.condition.Condition;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

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
