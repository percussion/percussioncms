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


