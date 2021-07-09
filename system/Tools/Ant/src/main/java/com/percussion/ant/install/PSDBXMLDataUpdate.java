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
import com.percussion.tablefactory.install.RxLogTables;
import com.percussion.tablefactory.*;
import com.percussion.tablefactory.tools.DbUtils;
import com.percussion.util.IOTools;

import java.io.*;
import java.sql.Connection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;


/**
 * PSDBXMLDataUpdate is a task that retrieves XML data
 * from a column in a table, creates an XML document from data, uses the
 * ISMP XSLclass to apply an XSL file to this XML document at install time,
 * then updates the data in the table with the modified XML document.
 * The XSL file is bundled with the installer and it can contain string
 * resolvers. This class uses the ISMP XSL processor that is used for command
 * builds.
 *
 *  <br>
 * Example Usage:
 * <br>
 * <pre>
 *
 * First set the taskdef:
 *
 *  <code>
 *  &lt;taskdef name="dbXMLFileUpdate"
 *              class="com.percussion.ant.install.PSDBXMLFileUpdate"
 *              classpathref="INSTALL.CLASSPATH"/&gt;
 *  </code>
 *
 * Now use the task to update the xml file.
 *
 *  <code>
 *  &lt;dbXMLFileUpdate backupTableName="TABLE_BAK" columnName="XMLDAT"
 *      selectFilter="NAME = 'relationships'" tableName="TABLE"
 *      token="$P(absoluteInstallLocation)" value="${install.dir}"
 *      xmlFile="${install.dir}/htmlconverter/config/serverPageTags.xml"
 *      xslFile="${install.dir}/installerTemp/source_tree/file.xsl"/&gt;
 *  </code>
 *
 * </pre>
 *
 */
public class PSDBXMLDataUpdate extends PSXMLFileUpdate
{
   // see base class
   @Override
   public void execute()
   {
      String installLoc = getRootDir();

      FileInputStream in = null;
      Connection conn = null;
      try
      {
         String tempDir = installLoc;
         if (!tempDir.endsWith(File.separator))
            tempDir += File.separator;

         in = new FileInputStream(new File(tempDir +
         "rxconfig/Installer/rxrepository.properties"));

         tempDir += TEMP_DIR;
         File fTempDir = new File(tempDir);
         fTempDir.mkdirs();
         fTempDir.deleteOnExit();

         // get database connection
         Properties props = new Properties();
         props.load(in);
         props.setProperty(PSJdbcDbmsDef.PWD_ENCRYPTED_PROPERTY, "Y");
         PSJdbcDbmsDef dbmsDef = new PSJdbcDbmsDef(props);
         PSJdbcDataTypeMap dataTypeMap = new PSJdbcDataTypeMap(
               props.getProperty("DB_BACKEND"),
               props.getProperty("DB_DRIVER_NAME"), null);
         conn = RxLogTables.createConnection(props);

         // catalog the table schema
         PSJdbcTableSchema tableSchema =
            PSJdbcTableFactory.catalogTable(conn, dbmsDef, dataTypeMap,
                  tableName, false);

         if (tableSchema == null)
         {
            // table does not exist
            return;
         }

         // create a backup of existing table
         boolean success = true;
         success = this.backupTable(conn, dbmsDef, dataTypeMap);
         if (!success)
         {
            // failed to create backup of the table
            return;
         }

         // catalog table data
         // we will catalog the update key columns and the column whose
         // data we need to update
         List keyColumns = tableSchema.getKeyColumns();
         if (keyColumns.isEmpty())
         {
            PSLogger.logInfo(
                  "ERROR : No key columns defined for table - " + tableName);
            PSLogger.logInfo(
                  "Data cannot be updated in table - " + tableName);
            return;
         }

         String[] columns = new String[keyColumns.size() + 1];
         Iterator it = keyColumns.iterator();
         int index = 0;
         while (it.hasNext())
         {
            columns[index] = (String)it.next();
            index++;
         }
         columns[index] = columnName;

         // use the filter if specified
         PSJdbcSelectFilter filter = null;
         if (selectFilter.trim().length() > 0)
            filter = new PSJdbcSelectFilter(selectFilter);

         PSJdbcTableData tableData = PSJdbcTableFactory.catalogTableData(
               conn, dbmsDef, tableSchema, columns, filter,
               PSJdbcRowData.ACTION_UPDATE);

         if (tableData == null)
         {
            // table has no data
            return;
         }

         // create a plan containing all the update statements
         PSJdbcExecutionPlan plan = new PSJdbcExecutionPlan();
         PSJdbcExecutionStep step = null;
         Iterator rowIt = tableData.getRows();
         while (rowIt.hasNext())
         {
            PSJdbcRowData row = (PSJdbcRowData)rowIt.next();
            PSJdbcColumnData colData = row.getColumn(columnName);
            if (colData != null)
            {
               String colValue = colData.getValue();
               if (!((colValue == null) || (colValue.trim().length() < 1)))
               {
                  File tempFile = File.createTempFile("rxtemp", ".xml", fTempDir);
                  tempFile.deleteOnExit();
                  PSJdbcColumnData newColData = updateColumnData(colValue,
                        tempFile);
                  row.removeColumn(columnName);
                  row.addColumn(newColData);
                  step = PSJdbcStatementFactory.getUpdateStatement(
                        dbmsDef, tableSchema, row);
                  plan.addStep(step);
               }
            }
         }
         // execute the plan
         plan.execute(conn);
      }
      catch(Exception ex)
      {
         PSLogger.logInfo("ERROR : " + ex.getLocalizedMessage());
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
            catch (Exception ex)
            {
            }
         }
         if (conn != null)
         {
            try
            {
               conn.close();
            }
            catch (Exception ex)
            {
            }
         }
      }
   }

   /***************************************************************************
    * Private Functions
    ***************************************************************************/

   /**
    * directory to use for creating temporary XML files.
    */
   private static final String TEMP_DIR = "rxconfig/Installer/tempXml";

   /**
    * Creates a backup of the existing table.
    *
    * @param conn the database connection to use, assumed not <code>null</code>
    * @param dbmsDef the database where the table is located, assumed not
    * <code>null</code>
    * @param dataTypeMap used for creating the schema of the table, assumed
    * not <code>null</code>
    *
    * @return <code>true</code> if the table exists and then backup was
    * successfully created, <code>false</code> otherwise
    */
   private boolean backupTable(Connection conn, PSJdbcDbmsDef dbmsDef,
         PSJdbcDataTypeMap dataTypeMap)
   {
      boolean success = true;
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      PrintStream ps = new PrintStream(baos);

      try
      {
         success = DbUtils.backupTable(conn, dbmsDef,
               dataTypeMap, tableName, backupTableName, ps, true);

         if (success)
            PSLogger.logInfo(
                  "Successfully created backup of table : " + tableName);
         else
            PSLogger.logInfo(
                  "Failed to create backup of table : " + tableName);

         PSLogger.logInfo(baos.toString());
      }
      catch (Exception ex)
      {
         success = false;
         PSLogger.logInfo("ERROR : " + ex.getLocalizedMessage());
         PSLogger.logInfo(ex);
      }
      finally
      {
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
      return success;
   }

   /**
    * Writes the current column data to the file specified by
    * <code>tempFile</code>, calls the <code>execute()</code> of
    * <code>RxISXMLFileUpdate</code> to apply the stylesheet to the
    * <code>tempFile</code>, reads the updated file, and creates a column data
    * from the contents of the file.
    *
    * @param colValue the current data in the column which will be written to
    * the <code>tempFile</code> for applying the stylesheet, assumed not
    * <code>null</code> and non-empty
    * @param tempFile the temporary file to use for writing the current
    * XML data specified by <code>colData</code>, assumed not <code>null</code>
    *
    * @return the updated column data, never <code>null</code>
    *
    * @throws IOException if any error occurs writing to the temporary file,
    * or reading from the temporary file after applying the stylesheet
    */
   private PSJdbcColumnData updateColumnData(String colValue,
         File tempFile) throws IOException
         {
      PSJdbcColumnData columnData = new PSJdbcColumnData(columnName, colValue);
      FileWriter fw = new FileWriter(tempFile);
      try
      {
         fw.write(colValue);
      }
      finally
      {
         if (fw != null)
         {
            try
            {
               fw.close();
            }
            catch (Exception e)
            {
            }
         }
      }

      // set the XML file on which the stylesheet should be applied
      // the path of the XML file is relative to the Rhythmyx root directory
      String tempFileName = tempFile.getName();
      setXmlFile(TEMP_DIR + File.separator + tempFileName);
      setBackupXMLFile(false);

      FileReader fr = null;
      StringWriter sw = null;
      try
      {
         super.execute();
         fr = new FileReader(tempFile);
         sw = new StringWriter();
         IOTools.writeStream(fr, sw);
         String newColValue = sw.toString();
         columnData = new PSJdbcColumnData(columnName, newColValue);
      }
      catch (Exception e)
      {
         PSLogger.logError("ERROR : " + e.getMessage());
      }
      finally
      {
         if (fr != null)
         {
            try
            {
               fr.close();
            }
            catch (Exception e)
            {
            }
         }
         if (sw != null)
         {
            try
            {
               sw.close();
            }
            catch (Exception e)
            {
            }
         }
      }

      return columnData;
         }

   /***************************************************************************
    * Bean Properties
    ***************************************************************************/

   /**
    * Returns the name of the table which contains a column with XML data which
    * needs to be updated by applying a stylesheet.
    *
    * @return the name of the table, never <code>null</code> or empty
    */
   public String getTableName()
   {
      return tableName;
   }

   /**
    * Sets the name of the table which contains a column with XML data which
    * needs to be updated by applying a stylesheet.
    *
    * @param tableName the name of the table, may not be <code>null</code>
    * or empty
    *
    * @throws IllegalArgumentException if <code>tableName</code> is invalid
    */
   public void setTableName(String tableName)
   {
      if ((tableName == null) || (tableName.trim().length() < 1))
         throw new IllegalArgumentException(
         "tableName may not be null or empty");
      this.tableName = tableName.trim();
   }

   /**
    * Returns the name of the column which contains the XML data which needs to
    * be updated by applying a stylesheet.
    *
    * @return the name of the column, never <code>null</code> or empty
    */
   public String getColumnName()
   {
      return columnName;
   }

   /**
    * Sets the name of the column which contains the XML data which needs to
    * be updated by applying a stylesheet.
    *
    * @param columnName the name of the column, may not be <code>null</code> or
    * empty
    *
    * @throws IllegalArgumentException if <code>columnName</code> is invalid
    */
   public void setColumnName(String columnName)
   {
      if ((columnName == null) || (columnName.trim().length() < 1))
         throw new IllegalArgumentException(
         "columnName may not be null or empty");
      this.columnName = columnName.trim();
   }

   /**
    * Returns the "WHERE" clause to use when catalogging the table data. This
    * can be used to restrict the rows whose column value will be updated by
    * applying the stylesheet on the existing data in the column.
    *
    * @return the "WHERE" clause to use when catalogging the table data, never
    * <code>null</code>, may be empty, does not include the "WHERE" keyword
    */
   public String getSelectFilter()
   {
      return selectFilter;
   }

   /**
    * Sets the "WHERE" clause to use when catalogging the table data.
    *
    * @param selectFilter the "WHERE" clause to use when catalogging the table
    * data, may be <code>null</code> or empty, if <code>null</code> then set
    * to empty, should not include the "WHERE" keyword
    */
   public void setSelectFilter(String selectFilter)
   {
      if ((selectFilter == null) || (selectFilter.trim().length() < 1))
         selectFilter = "";
      this.selectFilter = selectFilter.trim();
   }

   /**
    * Returns the name of the backup table which will store the data prior to
    * modifications. A backup of the existing table is created before performing
    * any data changes.
    *
    * @return the name of the backup table, never <code>null</code> or empty
    */
   public String getBackupTableName()
   {
      return backupTableName;
   }

   /**
    * Sets the the name of the backup table which will store the data prior to
    * modifications. A backup of the existing table is created before performing
    * any data changes.
    *
    * @param backupTableName name of the backup table, may not be
    * <code>null</code> or empty
    *
    * @throws IllegalArgumentException if <code>backupTableName</code> is invalid
    */
   public void setBackupTableName(String backupTableName)
   {
      if ((backupTableName == null) || (backupTableName.trim().length() < 1))
         throw new IllegalArgumentException(
         "backupTableName may not be null or empty");
      this.backupTableName = backupTableName.trim();
   }


   /***************************************************************************
    * Properties
    ***************************************************************************/

   /**
    * The name of the table which contains a column with XML data which needs
    * to be updated by applying a stylesheet. Never <code>null</code> or
    * empty. The actual value for this property will be set through the
    * Installshield UI.
    */
   private String tableName = "PSX_RXCONFIGURATIONS";

   /**
    * The name of the column which contains the XML data which needs to be
    * updated by applying a stylesheet. Never <code>null</code> or empty.
    * The actual value for this property will be set through the
    * Installshield UI.
    */
   private String columnName = "CONFIGURATION";

   /**
    * The "WHERE" clause to use when catalogging the table data, may be
    * <code>null</code> or empty. This can be used to restrict the rows
    * whose column value will be updated by applying the stylesheet on the
    * existing data in the column. Should not include the "WHERE" keyword.
    */
   private String selectFilter = "NAME = 'relationships'";

   /**
    * Before modifying the existing data in the table, a backup of the table
    * will be created. This variable specifies the name of the backup table.
    * Never <code>null</code> or empty. Actual value for this property
    * will be set through the Installshield UI. The name of the backup table
    * must be less than 30 characters (limitation on Oracle).
    */
   private String backupTableName = "PSX_RXCONFIG_UP_BAKUP";
}
