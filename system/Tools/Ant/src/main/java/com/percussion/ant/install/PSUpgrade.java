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

import com.percussion.install.RxUpgrade;
import com.percussion.install.PSLogger;

/**
 * The execute method of this called when it is an upgrade.
 * Execute method inturn calls RxUpgrade class process method to
 * process the upgrade.
 *
 *<br>
 * Example Usage:
 *<br>
 *<pre>
 *
 * First set the taskdef:
 *
 *  <code>
 *  &lt;taskdef name="upgrade"
 *              class="com.percussion.ant.install.PSUpgrade"
 *              classpathref="INSTALL.CLASSPATH"/&gt;
 *  </code>
 *
 * Now use the task to run the upgrade plugins.
 *
 *  <code>
 *  &lt;upgrade upgradeFileNames="rxupgrade.xml, rxupgrade2.xml"/&gt;
 *  </code>
 *
 * </pre>
 *
 */
public class PSUpgrade extends PSAction
{
   // see base class
   @Override
   public void execute()
   {
      m_strRootDir = getRootDir();

      RxUpgrade upgrade = new RxUpgrade();
      String[] pluginFiles = getUpgradeFileNames();
      for (int i = 0; i < pluginFiles.length; i++)
      {
         String fileName = pluginFiles[i];

         try
         {
            upgrade.process(m_strRootDir, fileName);
         }
         catch(Exception e)
         {
            e.printStackTrace(System.out);
            PSLogger.logError("file: " + fileName + " " + e.getMessage());
            PSLogger.logError(e);
         }
      }
   }

   /**
    * Getter that returns UpgradeFileName.
    * @return Upgrade File Name that was set on this bean.
    */
   public String[] getUpgradeFileNames()
   {
      return m_strCfgFiles;
   }

   /**
    * Setter for UpgradeFileName.
    * @param fileName, never <code>null</code> or <code>empty</code>.
    */
   public void setUpgradeFileNames(String fileNames)
   {
      if (fileNames==null || fileNames.trim().length() < 1)
         throw new IllegalArgumentException("fileName may not be null or empty");

      m_strCfgFiles = convertToArray(fileNames);
   }

   /**************************************************************************
    * Variables
    **************************************************************************/
   /**
    * Root dir of this installation.
    */
   private String m_strRootDir = "";

   /**
    * config file names for upgradeaction
    */
   private String[] m_strCfgFiles = {"rxupgrade.xml",
   "rxOrphanedDataCleanupPlugins.xml"};

}
