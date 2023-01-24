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
import com.percussion.install.PSOraConvertLONG2LOBTool;
import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.tablefactory.install.RxLogTables;
import com.percussion.utils.jdbc.PSJdbcUtils;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * PSConvertOraLongToLOB is a task which uses the
 * <code>com.percussion.install.PSOraConvertLONG2LOBTool</code> class to
 * convert the LONG column in the specified tables to LOB column. The tables
 * which need to be converted are specified in the <code>tableNames</code>
 * member variable using <code>setTableNames()</code>.
 *
 *<br>
 * Example Usage:
 *<br>
 *<pre>
 *
 * First set the taskdef:
 *
 *  <code>
 *  &lt;taskdef name="convertOraLongToLOB"
 *              class="com.percussion.ant.install.PSConvertOraLongToLOB"
 *              classpathref="INSTALL.CLASSPATH"/&gt;
 *  </code>
 *
 * Now use the task to perform the conversion.
 *
 *  <code>
 *  &lt;convertOraLongToLOB tableNames="TABLE1,TABLE2"/&gt;
 *  </code>
 *
 * </pre>
 *
 */
public class PSConvertOraLongToLOB extends PSAction
{
   // see base class
   @SuppressFBWarnings("HARD_CODE_PASSWORD")
   @Override
   public void execute()
   {
      FileInputStream in = null;
      Connection conn = null;
      ByteArrayOutputStream baos = null;
      PrintStream ps = null;

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

         String driver = dbmsDef.getDriver();
         if (!driver.startsWith(PSJdbcUtils.ORACLE_PRIMARY))
            return;

         conn = RxLogTables.createConnection(props);
         
         PSLogger.logInfo("Converting LONG column to LOB column for tables : ");
         for (int i = 0; i < tableNames.length; i++)
            PSLogger.logInfo(tableNames[i]);

         baos = new ByteArrayOutputStream();
         ps = new PrintStream(baos);

         PSOraConvertLONG2LOBTool oraConvert =
            new PSOraConvertLONG2LOBTool(conn, dbmsDef, ps);

         String[] convertTables =
            oraConvert.filterTablesToConvert(tableNames);

         if ((convertTables == null) || (convertTables.length == 0))
         {
            PSLogger.logInfo(
            "Tables already converted from LONG column to LOB column");
            return;
         }
         else
         {
            PSLogger.logInfo(
            "Tables which need conversion from LONG column to LOB column : ");
            for (int i = 0; i < convertTables.length; i++)
               PSLogger.logInfo(convertTables[i]);
         }

         oraConvert.executeConversion(convertTables);

         PSLogger.logInfo("Finished conversion of LONG column to LOB column.");
         PSLogger.logInfo(baos.toString());
      }
      catch (Exception ex)
      {
         PSLogger.logInfo("ERROR : " + ex.getMessage());
         PSLogger.logInfo(ex);
      }
      finally
      {
         if (in != null)
         {
            try
            {
               in.close();
            }
            catch(Exception e)
            {
            }
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
         if (baos != null)
         {
            try
            {
               baos.close();
            }
            catch(Exception e)
            {
            }
         }
         if (ps != null)
         {
            try
            {
               ps.close();
            }
            catch(Exception e)
            {
            }
         }
      }
   }

   /*******************************************************************
    * Private functions.
    *******************************************************************/


   /*******************************************************************
    * Property accessors and mutators.
    *******************************************************************/

   /**
    * Returns the names of tables which have a LONG column and the column needs
    * to be converted into LOB column.
    *
    * @return names of tables which have a LONG column and the column needs to
    * be converted into LOB column, never <code>null</code>, may be empty
    */
   public String[] getTableNames()
   {
      return tableNames;
   }

   /**
    * Sets the names of tables which have a LONG column and the column needs to
    * be converted into LOB column.
    *
    * @param tableNames names of tables which have a LONG column and the column
    * needs to be converted into LOB column, may be <code>null</code> or empty,
    * if <code>null</code> then set to empty array.
    */
   public void setTableNames(String tableNames)
   {
      this.tableNames = convertToArray(tableNames);
   }

   /*******************************************************************
    * Properties
    *******************************************************************/

   /**
    * names of tables which have a LONG column and the column needs to be
    * converted into LOB column, never <code>null</code>, may be empty.
    */
   private String[] tableNames = new String[0];

   /*******************************************************************
    * Member variables
    *******************************************************************/

}


