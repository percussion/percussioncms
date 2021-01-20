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

import com.percussion.install.PSLogger;
import com.percussion.tablefactory.PSJdbcDataTypeMap;
import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.tablefactory.PSJdbcTableData;
import com.percussion.tablefactory.PSJdbcTableDataCollection;
import com.percussion.tablefactory.PSJdbcTableFactory;
import com.percussion.tablefactory.PSJdbcTableSchema;
import com.percussion.tablefactory.PSJdbcTableSchemaCollection;
import com.percussion.tablefactory.install.RxLogTables;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

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
public class PSExportDatabase extends PSAction
{
   // see base class
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

          List<String> tableNames = getTableNames(conn, dbmsDef);
          tableNames=filterTableNames(tableNames);
          Document schemaDoc = PSXmlDocumentBuilder.createXmlDocument();
          Document dataDoc = PSXmlDocumentBuilder.createXmlDocument();

          PSJdbcTableDataCollection collData = new PSJdbcTableDataCollection();
          PSJdbcTableSchemaCollection collSchema =
                  new PSJdbcTableSchemaCollection();
          PSJdbcTableSchema tableSchema = null;
          PSJdbcTableData tableData = null;

          List<PSJdbcTableSchema> schemasToSort = new ArrayList<>();
          for (int i=0; i <tableNames.size(); i++)
         {


          try
          {
              String tblName = tableNames.get(i).trim();

              tableSchema = PSJdbcTableFactory.catalogTable(conn, dbmsDef,
                      dataTypeMap, tblName, true);
              if(tableSchema == null)
                  continue;
              schemasToSort.add(tableSchema);

              //collSchema.add(tableSchema);

              //tableData = tableSchema.getTableData();
              //if(tableData == null)
               //   continue;
              //collData.add(tableData);

          }
          catch (Exception ex)
          {
              PSLogger.logInfo("ERROR : " + ex.getMessage());
              PSLogger.logInfo(ex);
          }




      }

          Collections.sort(schemasToSort);

          for (PSJdbcTableSchema sortedSchema : schemasToSort)
          {
              collSchema.add(sortedSchema);

              tableData = sortedSchema.getTableData();
              if(tableData == null)
                  continue;
              collData.add(tableData);

          }


          schemaDoc.appendChild(collSchema.toXml(schemaDoc));
          dataDoc.appendChild(collData.toXml(dataDoc));
          File defFile = new File(tableDefFile);
          defFile.getParentFile().mkdirs();

          File dataFile = new File(tableDataFile);
          dataFile.getParentFile().mkdirs();

          try (OutputStream os = new FileOutputStream(new File(tableDefFile))) {
              PSXmlDocumentBuilder.write(schemaDoc, os);
          }

          try (OutputStream os = new FileOutputStream(new File(tableDataFile))) {
              PSXmlDocumentBuilder.write(dataDoc, os);
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

    private List<String> filterTableNames(List<String> tableNames) {
       return tableNames.stream().filter(name -> !name.endsWith("_BAK")).collect(Collectors.toList());
    }

    private List<String> getTableNames(Connection conn, PSJdbcDbmsDef dbmsDef) throws SQLException {
        DatabaseMetaData dmd = conn.getMetaData();
        List<String> tableNames = new ArrayList<>();
        try {
            String[] types = {"TABLE"};
            ResultSet rs = dmd.getTables(dbmsDef.getDataBase(), dbmsDef.getSchema(), "%", types);

            while (rs.next()) {
                tableNames.add(rs.getString("TABLE_NAME"));
            }
        }
        catch (SQLException ex) {
            PSLogger.logInfo("ERROR : " + ex.getMessage());
            PSLogger.logInfo(ex);
            throw ex;
        }

        return tableNames;
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
   public String[] getTableIncludes()
   {
      return tableIncludes;
   }

   /**
    * Sets the name of tables whose backup is to be created.
    *
    * @param tables name of tables whose backup is to be created,
    * never <code>null</code>, may be empty array
    */
   public void setTableIncludes(String tableIncludes)
   {
      this.tableIncludes = convertToArray(tableIncludes);
   }

    /**
     * Returns the name of tables whose backup is to be created.
     *
     * @return the name of tables whose backup is to be created,
     * never <code>null</code>, may be empty array
     */
    public String[] getTableExcludes()
    {
        return tableIncludes;
    }

    /**
     * Sets the name of tables whose backup is to be created.
     *
     * @param tables name of tables whose backup is to be created,
     * never <code>null</code>, may be empty array
     */
    public void setTableExcludes(String tableExcludes)
    {
        this.tableIncludes = convertToArray(tableExcludes);
    }


   public String getTableDefFile() { return tableDefFile; }

    public String getTableDataFile() { return tableDataFile; }


    public void setTableDefFile(String tableDefFile) { this.tableDefFile = tableDefFile;}
    public void setTableDataFile(String tableDataFile) { this.tableDataFile = tableDataFile;}

    /**************************************************************************
    * properties
    **************************************************************************/

    /**
     * Name of tables whose backup is to be created, never <code>null</code>,
     * may be empty
     */
    private String tableDefFile = null;

    private String tableDataFile = null;

   /**
    * Name of tables whose backup is to be created, never <code>null</code>,
    * may be empty
    */
   private String[] tableIncludes= null;
    /**
     * Name of tables whose backup is to be created, never <code>null</code>,
     * may be empty
     */
    private String[] tableExcludes= null;

}


