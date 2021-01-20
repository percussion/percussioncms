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
package com.percussion.install;

import com.percussion.tablefactory.PSJdbcDbmsDef;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;


/**
 * The main purpose of this plugin is to ensure unique string values in the
 * given column of the given table.  It does this by finding duplicate values
 * and modifying all duplicates in the following way:
 *
 * Extranet  
 * Extranet -> Extranet1
 * Extranet -> Extranet2
 */
public class PSUpgradePluginUniqueNames extends PSUpgradePluginModifyColumnBase
{
   /**
    * Default Constructor.
    */
   public PSUpgradePluginUniqueNames()
   {
   }

   /**
    * Makes unique the values from the given column of the given table.
    *
    * See base class for additional details.
    */
   public boolean modifyColumnValues(
      final Connection conn, final PSJdbcDbmsDef dbmsDef, String table, 
      String column, String idcolumn)
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
         throw new IllegalArgumentException("idcolumn may not be null");
      }
      
      log("Attempting to make unique " + column + " column from " + table +
            " table.");

      final String valuesQuery = constructValuesQuery(dbmsDef, table, column,
            idcolumn);
      
      boolean modifications = false;
      int rowsModified = 0;
      
      try
      {
         final Set allValues = loadValues(conn, column, valuesQuery);
         {
            final Set valuesSoFar = new HashSet();
            final Statement statement = conn.createStatement(
                  ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            try
            {
               final ResultSet rs = statement.executeQuery(
                     constructUpdateValuesQuery(conn, dbmsDef, valuesQuery));
               while (rs.next())
               {
                  final String value = rs.getString(column);
                  if (valuesSoFar.contains(value))
                  {
                     // duplicate detected
                     final String newValue =
                           constructNewValue(value, allValues);
                     rs.updateString(column, newValue);
                     rs.updateRow();
                     rowsModified++;
                     modifications = true;
                     log("In " + table + " table, column " + column + ", changed "
                           + "value: {" + value + "} to {" + newValue + "} "
                           + "where: " + idcolumn + " = " + rs.getInt(idcolumn));
                     valuesSoFar.add(newValue);
                     allValues.add(newValue);
                  }
                  else
                  {
                     valuesSoFar.add(value);
                  }
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
