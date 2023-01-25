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
