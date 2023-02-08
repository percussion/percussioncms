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
import com.percussion.tablefactory.PSJdbcDataTypeMap;
import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.tablefactory.PSJdbcTableFactory;
import com.percussion.tablefactory.PSJdbcTableSchema;
import com.percussion.tablefactory.install.RxLogTables;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.tools.ant.taskdefs.condition.Condition;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;


/**
 * PSViewsExistWizCondition is a condition which will return <code>true</code>
 * when <code>eval</code> is invoked if the Rhythmyx views, see
 * {@link #RX_VIEWS}, already exist in the database, else returns
 * <code>false</code>.
 *
 * <br>
 * Example Usage:
 * <br>
 * <pre>
 *
 * First set the typedef:
 *
 *  <code>
 *  &lt;typedef name="viewsExistWizCondition"
 *              class="com.percussion.ant.install.PSViewsExistWizCondition"
 *              classpathref="INSTALL.CLASSPATH"/&gt;
 *  </code>
 *
 * Now use the task to determine if all of the Rhythmyx views exist.
 *
 *  <code>
 *  &lt;condition property="VIEWS_EXIST"&gt;
 *     &lt;viewsExistWizCondition/&gt;
 *  &lt;/condition&gt;
 *  </code>
 *
 * </pre>
 *
 */
public class PSViewsExistWizCondition extends PSAction implements Condition
{
   /* (non-Javadoc)
    * @see org.apache.tools.ant.taskdefs.condition.Condition#eval()
    */
   public boolean eval()
   {
      return checkExists();
   }

  /**************************************************************************
  * private functions
  **************************************************************************/

   /**
    * Checks if the Rhythmyx views specified by <code>RX_VIEWS</code>
    * already exist in the database.
    *
    * @return <code>true</code> if the views specified by
    * <code>RX_VIEWS</code> already exist in the database,
    * <code>false</code> otherwise.
    */
   @SuppressFBWarnings("HARD_CODE_PASSWORD")
   private boolean checkExists()
   {
      FileInputStream in = null;
      Connection conn = null;
      boolean exists = false;

      try
      {
         // get the Rhythmyx root directory
         String strInstallDir = getRootDir();

         if (strInstallDir == null)
            return false;

         if (!strInstallDir.endsWith(File.separator))
            strInstallDir += File.separator;

         // check if the "rxrepository.properties" file exists under the Rhythmyx
         // root directory
         File propFile = new File(strInstallDir +
            "rxconfig/Installer/rxrepository.properties");

         if (!(propFile.exists() && propFile.isFile()))
            return false;

         in = new FileInputStream(propFile);
         Properties props = new Properties();
         props.load(in);
         props.setProperty(PSJdbcDbmsDef.PWD_ENCRYPTED_PROPERTY, "Y");
         PSJdbcDbmsDef dbmsDef = new PSJdbcDbmsDef(props);
         PSJdbcDataTypeMap dataTypeMap = new PSJdbcDataTypeMap(
            props.getProperty("DB_BACKEND"),
            props.getProperty("DB_DRIVER_NAME"), null);
         conn = RxLogTables.createConnection(props);

         // check each view to see if it exists
         for (int i = 0; i < RX_VIEWS.length; i++)
         {
            PSJdbcTableSchema objectSchema = PSJdbcTableFactory.catalogTable(
                  conn, dbmsDef, dataTypeMap, RX_VIEWS[i], false);

            exists = (objectSchema != null);
            if (exists)
            {
               // check if the object type matches
               if (!objectSchema.isView())
                  exists = false;
            }

            if (!exists)
               break;
         }
      }
      catch (Exception ex)
      {
         PSLogger.logInfo("ERROR : " + ex.getMessage());
         PSLogger.logInfo(ex);
      }
      finally
      {
         try
         {
            if (in != null)
               in.close();
         }
         catch(Exception e)
         {
         }
         if (conn != null)
         {
            try
            {
               conn.close();
            }
            catch (SQLException e)
            {
            }
         }
      }
      return exists;
   }

  /**************************************************************************
  * member variables
  **************************************************************************/

   /**
    * Names of the Rhythmyx views whose existence in the database is to be
    * verified
    **/
    private static final String[] RX_VIEWS = {
          "CONTENTVARIANTS",
          "PSX_COMMUNITY_PERMISSION_VIEW",
          "PSX_DISPLAYFORMATPROPERTY_VIEW",
          "PSX_MENUVISIBILITY_VIEW",
          "PSX_SEARCHPROPERTIES_VIEW",
          "RXCONTENTTYPECOMMUNITY",
          "RXSITECOMMUNITY",
          "RXVARIANTCOMMUNITY",
          "RXWORKFLOWCOMMUNITY"
    };
}


