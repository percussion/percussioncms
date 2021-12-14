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
package com.percussion.install;

import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.util.PSSqlHelper;
import com.percussion.utils.jdbc.PSJdbcUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

/**
 * This is a generic plugin base class that implements IPSUpgradePlugin.  This
 * plugin takes a table name, column name, and id column name as config data.
 * The string values in the column are modified in the
 * {@link #modifyColumnValues(Connection, PSJdbcDbmsDef, String, String, String)}
 * method.  This method must be overridden by subclasses to perform various 
 * types of modifications on the column values.
 */
public abstract class PSUpgradePluginModifyColumnBase implements IPSUpgradePlugin
{
   /**
    * Implements process method of IPSUpgradePlugin.
    * @param config IPSUpgradeModule object.
    *    may not be <code>null<code>.
    * @param elemData data element of plugin.  Includes one or more tableset
    * elements, which can be described as follows:
    * <br>
    * <pre>
    * <code>
    * &lt;tableset&gt;
    *    &lt;table&gt;RXSITES&lt/table&gt;
    *    &lt;column&gt;SITENAME&lt;/column&gt;
    *    &lt;id-column&gt;SITEID&lt;/id-column&gt;
    * &lt;/tableset&gt;
    * </code>
    * </pre>
    * 
    * @return <code>PSPluginResponse</code> object with appropriate status and
    * message.
    */
   public PSPluginResponse process(IPSUpgradeModule config, Element elemData)
   {
      m_config = config;
      
      NodeList nl = elemData.getElementsByTagName("tableset");
      FileInputStream in = null;
      Connection conn = null;
      PSJdbcDbmsDef dbmsDef = null;
      
      try
      {
         conn = RxUpgrade.getJdbcConnection();
         dbmsDef = new PSJdbcDbmsDef(RxUpgrade.getRxRepositoryProps());
         String table = "";
         String column = "";
         String idcolumn = "";
         
         for(int j=0; nl!=null && j<nl.getLength(); j++)
         {
            table = InstallUtil.getElemValue(nl.item(j), "table");
            column = InstallUtil.getElemValue(nl.item(j), "column");
            idcolumn = InstallUtil.getElemValue(nl.item(j), "id-column");
            
            if (!modifyColumnValues(conn, dbmsDef, table, column, idcolumn))
            {
               log("No modifications were made.");
            }
         }
      }
      catch(Exception e)
      {
         e.printStackTrace(config.getLogStream());
      }
      finally
      {
         try
         {
            if(in != null)
            {
               in.close();
               in =null;
            }
         }
         catch(Throwable t)
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
            conn = null;
         }
         config.getLogStream().println(
            "leaving the process() of the plugin...");
      }
      return null;
   }

   /**
    * Modifies the values of a given column from the specified table.  Must be
    * overridden by all subclasses.
    *
    * @param conn the database connection object, cannot be <code>null</code>.
    * @param dbmsDef the database definition, cannot be <code>null</code>.
    * @param table the name of the table to check, cannot be <code>null</code>
    * or empty.
    * @param column the name of the column to check, cannot be <code>null</code>
    * or empty.
    * @param idcolumn the name of the id column for this table, cannot be
    * <code>null</code> or empty.
    * 
    * @return <code>true</code> if modifications were made.
    */
   protected abstract boolean modifyColumnValues(
      final Connection conn, final PSJdbcDbmsDef dbmsDef, String table, 
      String column, String idcolumn);
   
   /**
    * Write a log of the number of rows modified
    */
   protected void logNumberOfRowsModified(String table, final int rowsModified)
   {
      if (rowsModified > 0)
      {
         String results = rowsModified + " row(s) modified in table " + table;
         log(results);
      }
   }

   /**
    * Constructs new value from existing value which does not match any of the
    * existing values.
    * @param value current value.
    * @param allValues all the existing values. 
    */
   protected String constructNewValue(final String value, final Set allValues)
   {
      String newValue = value;
      int idx = 1;
      while (allValues.contains(newValue))
      {
         newValue = value + idx;
         idx++;
      }
      return newValue;
   }

   /**
    * Creates updatable query to load all the values of the column.
    */
   protected String constructUpdateValuesQuery(final Connection conn,
         final PSJdbcDbmsDef dbmsDef, final String valuesQuery)
         throws SQLException
   {
      if (PSSqlHelper.isOracle(dbmsDef.getDriver()) ||
            dbmsDef.getDriver().equals(PSJdbcUtils.DB2))
      {
         conn.setAutoCommit(false);
         return valuesQuery + " FOR UPDATE";
      }
      else
      {
         return valuesQuery;
      }
   }

   /**
    * Creates a query to load all the values of the column.
    *    
    * @param conn the database connection object, cannot be <code>null</code>.
    * @param dbmsDef the database definition, cannot be <code>null</code>.
    * @param table the name of the table to check
    * @param column the name of the column to check
    * @param idcolumn the name of the id column for this table
    */
   protected String constructValuesQuery(final PSJdbcDbmsDef dbmsDef, String table,
         String column, String idcolumn)
   {
      final String qualTableName = PSSqlHelper.qualifyTableName(table,
            dbmsDef.getDataBase(), dbmsDef.getSchema(), 
            dbmsDef.getDriver());       
      
      return "SELECT " + qualTableName + "." + column + ", " +
                         qualTableName + "." + idcolumn +
                         " FROM " + qualTableName;
   }

   /**
    * Loads all the names from the database.
    */
   protected Set loadValues(final Connection conn, String column,
         final String valuesQuery) throws SQLException
   {
      final Set allValues = new HashSet();
      final Statement statement = conn.createStatement();
      try
      {
         final ResultSet resultSet = statement.executeQuery(valuesQuery);
         while (resultSet.next())
         {
            allValues.add(resultSet.getString(column));
         }
      }
      finally
      {
         statement.close();
      }
      return allValues;
   }
   
   /**
    * Prints message to the log printstream if it exists
    * or just sends it to System.out
    *
    * @param msg the message to be logged, can be <code>null</code>.
    */
   protected void log(String msg)
   {
      if (msg == null)
      {
         return;
      }

      if (m_config != null)
      {
         m_config.getLogStream().println(msg);
      }
      else
      {
         System.out.println(msg);
      }
   }
   
   protected IPSUpgradeModule m_config;
}
