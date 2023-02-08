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

import com.percussion.error.PSExceptionUtils;
import com.percussion.install.PSLogger;
import com.percussion.install.RxUpgrade;

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
      for (String fileName : pluginFiles) {
         try {
            upgrade.process(m_strRootDir, fileName);
         } catch (Exception e) {
            PSLogger.logError("file: " + fileName + " " +
                    PSExceptionUtils.getMessageForLog(e));
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
    * @param fileNames, never <code>null</code> or <code>empty</code>.
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
