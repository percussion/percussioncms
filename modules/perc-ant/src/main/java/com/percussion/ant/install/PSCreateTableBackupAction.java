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
import com.percussion.tablefactory.PSJdbcExecutionStep;
import com.percussion.tablefactory.PSJdbcStatementFactory;
import com.percussion.tablefactory.install.RxLogTables;
import com.percussion.tablefactory.tools.DbUtils;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * This class creates backup of specified tables. If backup of the table is
 * successfully created, then it may drop the original table if
 * <code>dropTables</code> is <code>true</code>.  Create backup table name by
 * appending <code>suffix</code> to table name.
 *
 *<br>
 * Example Usage:
 *<br>
 *<pre>
 *
 * First set the taskdef:
 *
 *  <code>
 *  &lt;taskdef name="createTableBackupAction"
 *              class="com.percussion.ant.install.PSCreateTableBackupAction"
 *              classpathref="INSTALL.CLASSPATH"/&gt;
 *  </code>
 *
 * Now use the task to create a backup of the specified table(s).
 *
 *  <code>
 *  &lt;createTableBackupAction dropTables="false"
 *                  suffix="_BAK"
 *                  tables="TABLE1,TABLE2"/&gt;
 *  </code>
 *
 * </pre>
 *
 */
public class PSCreateTableBackupAction extends PSAction
{
   // see base class
   @SuppressFBWarnings("HARD_CODE_PASSWORD")
   @Override
   public void execute()
   {
      FileInputStream in = null;
      Connection conn = null;

      try
      {
         String propFile = getRootDir() + File.separator
         + "rxconfig/Installer/rxrepository.properties";

         File f = new File(propFile);
         if (!(f.exists() && f.isFile()))
            return;

         in = new FileInputStream(f);
         Properties props = new Properties();
         props.load(in);
         props.setProperty(PSJdbcDbmsDef.PWD_ENCRYPTED_PROPERTY, "Y");
         PSJdbcDbmsDef dbmsDef = new PSJdbcDbmsDef(props);
         PSJdbcDataTypeMap dataTypeMap = new PSJdbcDataTypeMap(
               props.getProperty("DB_BACKEND"),
               props.getProperty("DB_DRIVER_NAME"), null);
         conn = RxLogTables.createConnection(props);

         int maxTblNameLen = MAX_TABLE_NAME_LENGTH - (getSuffix().length() + 1);
         for (int i=0; i <tables.length; i++)
         {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);

            try
            {
               String tblName = tables[i].trim();
               String backupTblName = tblName;
               if ((tblName.length() > maxTblNameLen))
                  backupTblName = tblName.substring(0, maxTblNameLen-1);
               backupTblName += getSuffix();

               PSLogger.logInfo("Creating backup of table : " + tblName +
                     " into table : " + backupTblName);

               boolean success = DbUtils.backupTable(
                     conn, dbmsDef, dataTypeMap,
                     tblName, backupTblName, ps, true);

               if (success)
                  PSLogger.logInfo(
                        "Successfully created backup of table : " + tblName);
               else
                  PSLogger.logInfo(
                        "Failed to create backup of table : " + tblName);

               PSLogger.logInfo(baos.toString());

               if (success && dropTables)
               {
                  PSLogger.logInfo("Dropping table : " + tblName);
                  PSJdbcExecutionStep step =
                     PSJdbcStatementFactory.getDropTableStatement(
                           dbmsDef, tblName);
                  step.execute(conn);
                  PSLogger.logInfo("Successfully dropped table : " + tblName);
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
                  if (baos != null)
                     baos.close();
               }
               catch(Exception e)
               {
               }
               try
               {
                  if (ps != null)
                     ps.close();
               }
               catch(Exception e)
               {
               }
            }
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
   }


   /***************************************************************************
    * Bean properties
    ***************************************************************************/

   /**
    * Returns the name of tables whose backup is to be created.
    *
    * @return the name of tables whose backup is to be created,
    * never <code>null</code>, may be empty array
    */
   public String[] getTables()
   {
      return tables;
   }

   /**
    * Sets the name of tables whose backup is to be created.
    *
    * @param tables name of tables whose backup is to be created,
    * never <code>null</code>, may be empty array
    */
   public void setTables(String tables)
   {
      this.tables = convertToArray(tables);
   }

   /**
    * Returns a boolean indicating whether the tables should be deleted if a
    * successful backup is created.
    *
    * @return <code>true</code> if the tables specified in <code>tables</code>
    * will be dropped if a successful backup of tables has been created,
    * <code>false</code> if the tables are not to be dropped even after a
    * successful backup is created.
    */
   public boolean getDropTables()
   {
      return dropTables;
   }

   /**
    * Sets whether the tables should be deleted if a successful backup is created.
    *
    * @param dropTables <code>true</code> if the tables specified in <code>tables</code>
    * should be dropped if a successful backup of tables has been created,
    * <code>false</code> if the tables are not to be dropped even after a
    * successful backup is created.
    */
   public void setDropTables(boolean dropTables)
   {
      this.dropTables = dropTables;
   }

   /**
    * Returns the suffix to be added to the table to obtain the name of the
    * backup table.
    */
   public String getSuffix()
   {
      return suffix;
   }

   /**
    * Sets the value of the suffix to be used in creating the backup table name.
    * The set will only occur for non-empty input values.
    */
   public void setSuffix(String newSuffix)
   {
      if (newSuffix != null && newSuffix.trim().length() > 0)
         suffix = newSuffix;
   }

   /**************************************************************************
    * properties
    **************************************************************************/

   /**
    * Name of tables whose backup is to be created, never <code>null</code>,
    * may be empty
    */
   private String[] tables = new String[]{};

   /**
    * if <code>true</code> then drops the tables specified in <code>tables</code>
    * if a successful backup of tables has been created. If <code>false</code>,
    * tables are not dropped even after a successful backup is created.
    */
   private boolean dropTables = false;

   /**
    * Suffix to be added to the table to obtain the name of backup tables.
    */
   private String suffix = "_UPG";

   /**************************************************************************
    * member variables
    **************************************************************************/

   /**
    * maximum number of characters that the table name can contain. Oracle
    * does not permit table names with more than 30 characters.
    */
   private static final int MAX_TABLE_NAME_LENGTH = 30;
}


