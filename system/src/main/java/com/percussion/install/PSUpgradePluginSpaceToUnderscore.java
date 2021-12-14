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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


/**
 * This plugin modifies the values in a column by replacing space with "_"
 * according to the algorithm described by
 * {@link InstallUtil#modifyName(String)}.
 */
public class PSUpgradePluginSpaceToUnderscore extends
PSUpgradePluginModifyColumnBase
{
   /**
    * Default Constructor.
    */
   public PSUpgradePluginSpaceToUnderscore()
   {
   }
   
   /**
    * Replaces whitespace with "_" in the values from the given column of the
    * given table.
    * 
    * See base class for additional details.
    */
   public boolean modifyColumnValues(final Connection conn,
         final PSJdbcDbmsDef dbmsDef, final String table, final String column,
         final String idcolumn)
   {
      if (conn == null)
      {
         throw new IllegalArgumentException("conn may not be null");
      }
      
      if (dbmsDef == null)
      {
         throw new IllegalArgumentException("dbmsDef may not be null");
      }
      
      if (table == null || table.trim().length() == 0)
      {
         throw new IllegalArgumentException("table may not be null or empty");
      }
      
      if (column == null || column.trim().length() == 0)
      {
         throw new IllegalArgumentException("column may not be null or empty");
      }
      
      if (idcolumn == null || idcolumn.trim().length() == 0)
      {
         throw new IllegalArgumentException("idcolumn may not be null or empty");
      }
      
      log("Attempting to replace space with '_' in " + column + " column from "
            + table + " table.");

      final String valuesQuery = constructValuesQuery(dbmsDef, table, column,
            idcolumn);
      
      boolean modifications = false;
      int rowsModified = 0;
      
      try
      {
         final Statement statement = conn.createStatement(
               ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
         try
         {
            final ResultSet rs = statement.executeQuery(
                  constructUpdateValuesQuery(conn, dbmsDef, valuesQuery));
            while (rs.next())
            {
               final String value = rs.getString(column);
               final String newValue = InstallUtil.convertSpaceToUnderscore(
                     value);
               if (newValue.equals(value))
                  continue;
               
               rs.updateString(column, newValue);
               rs.updateRow();
               rowsModified++;
               modifications = true;
               log("In " + table + " table, column " + column + ", changed "
                     + "value: {" + value + "} to {" + newValue + "} "
                     + "where: " + idcolumn + " = " + rs.getInt(idcolumn));
            }
            if (!conn.getAutoCommit())
            {
               conn.commit();
            }
         }
         finally
         {
            statement.close();
         }
      }
      catch (SQLException e)
      {
            e.printStackTrace(m_config.getLogStream());
      }
      finally
      {
         try
         {
            conn.setAutoCommit(true);
         }
         catch (SQLException e)
         {
            e.printStackTrace(m_config.getLogStream());
         }
      }
      
      logNumberOfRowsModified(table, rowsModified);
      return modifications;
   }
}
