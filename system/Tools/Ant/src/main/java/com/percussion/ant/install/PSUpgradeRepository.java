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
import com.percussion.tablefactory.PSJdbcDbmsDef;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import static com.percussion.install.InstallUtil.restoreRepositoryPropertyFile;


/**
 * PSUpgradeRepository is a task that upgrades the repository file
 * in case of upgrade
 *
 * <br>
 * Example Usage:
 * <br>
 * <pre>
 *
 * First set the taskdef:
 *
 *  <code>
 *  &lt;taskdef name="upgradeRepository"
 *              class="com.percussion.ant.install.PSUpgradeRepository"
 *              classpathref="INSTALL.CLASSPATH"/&gt;
 *  </code>
 *
 * Now use the task to load the repository properties with the correct values.
 *
 *  <code>
 *  &lt;upgradeRepository/&gt;
 *  </code>
 *
 * </pre>
 *
 */
public class PSUpgradeRepository extends PSAction
{
   // see base class
   @Override
   public void execute()
   {
      upgradeRepository(getRootDir());
   }


   /**
    * Updates the rxrepository.properties file used by the installer with the
    * current Rhythmyx repository information. The original file will be backed
    * up and restored if an error occurs.
    *
    * @param strRootDir the Rhythmyx root directory, may not be
    *           <code>null</code> or empty.
    */
   public static void upgradeRepository(String strRootDir)
   {
      if (strRootDir == null || strRootDir.trim().length() == 0)
      {
         throw new IllegalArgumentException("strRootDir may not be null or " + "empty");
      }

      Properties repProps = new Properties();
      Properties serverProps = new Properties();
      String strDriver;
      String strClass;
      String strServer;
      String strId;
      String strPw;
      String strDb;
      String strSchema;

      try
      {
         File repPropFile = new File(strRootDir, InstallUtil.REPOSITORY_PROPS_FILE);
         if (!repPropFile.exists())
            return;

         // Backup original file
         InstallUtil.backupRepositoryPropertyFile(strRootDir);

         try(FileInputStream repPropsIn = new FileInputStream(repPropFile)){
            repProps.load(repPropsIn);
         }


         // This is an upgrade of 6.X and up, load repository info from
         // the new file structure
         Properties dbProps = PSJdbcDbmsDef.loadRxRepositoryProperties(strRootDir);

         strDriver = dbProps.getProperty(PSJdbcDbmsDef.DB_DRIVER_NAME_PROPERTY, "");
         strClass = dbProps.getProperty(PSJdbcDbmsDef.DB_DRIVER_CLASS_NAME_PROPERTY, "");
         strServer = dbProps.getProperty(PSJdbcDbmsDef.DB_SERVER_PROPERTY, "");
         strId = dbProps.getProperty(PSJdbcDbmsDef.UID_PROPERTY, "");
         strPw = dbProps.getProperty(PSJdbcDbmsDef.PWD_PROPERTY, "");
         strDb = dbProps.getProperty(PSJdbcDbmsDef.DB_NAME_PROPERTY, "");
         strSchema = dbProps.getProperty(PSJdbcDbmsDef.DB_SCHEMA_PROPERTY, "");


         repProps.setProperty(PSJdbcDbmsDef.DB_DRIVER_NAME_PROPERTY, strDriver);
         repProps.setProperty(PSJdbcDbmsDef.DB_DRIVER_CLASS_NAME_PROPERTY, strClass);
         repProps.setProperty(PSJdbcDbmsDef.DB_SERVER_PROPERTY, strServer);
         repProps.setProperty(PSJdbcDbmsDef.UID_PROPERTY, strId);
         repProps.setProperty(PSJdbcDbmsDef.PWD_PROPERTY, strPw);
         repProps.setProperty(PSJdbcDbmsDef.DB_NAME_PROPERTY, strDb);
         repProps.setProperty(PSJdbcDbmsDef.DB_SCHEMA_PROPERTY, strSchema);

         try(FileOutputStream repPropsOut = new FileOutputStream(repPropFile)) {
            repProps.store(repPropsOut, null);
         }
      }
      catch (Exception e)
      {
         PSLogger.logError("Exception : " + e.getMessage());

         try
         {
            // Restore original file
            restoreRepositoryPropertyFile(strRootDir);
         }
         catch (Exception e2)
         {
         }
      }
   }
}
