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
package com.percussion.tablefactory;

import com.percussion.util.PSSQLStatement;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * This class represents a sql statement that is a step in a
 * PSJdbcExecutionPlan, and contains all the info it requires to be executed at
 * run-time.
 */
public class PSJdbcSqlStatement extends PSJdbcExecutionStep
{
   /**
    * Constructs Sql statement to execute.
    *
    * @param statement The statement to execute, may not be <code>null</code> or
    * empty.
    *
    * @throws IllegalArgumentException if statement is <code>null</code> or
    * empty.
    */
   public PSJdbcSqlStatement(String statement)
   {
      if (statement == null || statement.trim().length() == 0)
         throw new IllegalArgumentException(
            "statement may not be null or empty");

      m_statement = statement;
   }

   // see base class
   public int execute(Connection conn) throws SQLException
   {
      Statement stmt = null;
      int updateCount = 0;

      PSJdbcExecutionStepLog stepLogData = getStepLogData();
      stepLogData.setConnectionString(conn);
      stepLogData.setSqlQuery(m_statement);

      PSJdbcTableFactory.logDebugMessage(m_statement);

      try
      {
         stmt = PSSQLStatement.getStatement(conn);
         boolean result = stmt.execute(m_statement);
         if(!result)
         {
            updateCount = stmt.getUpdateCount();
         }

         stepLogData.setSuccess(true);
         stepLogData.setUpdateCount(updateCount);
         if (updateCount == 0)
         {
            stepLogData.setErrorMessage(
               PSJdbcExecutionStepLog.NO_ROWS_UPDATED_MSG);
         }
         else
         {
            stepLogData.setErrorMessage(PSJdbcExecutionStepLog.SUCCESS_MSG);
         }
      }
      catch (SQLException e)
      {
         updateCount = 0;
         stepLogData.setSuccess(false);
         stepLogData.setUpdateCount(0);
         String errMsg = PSJdbcTableFactoryException
            .formatSqlException(m_statement, e);
         stepLogData.setErrorMessage(errMsg);
         handleSqlException(e);
      }
      finally
      {
         if (stmt != null)
         {
            try
            {
               stmt.close();
            }
            catch (SQLException e)
            {
               PSJdbcTableFactory.logDebugMessage(
                  "Error closing prepared statement: " +
                  PSJdbcTableFactoryException.formatSqlException(e));
            }
         }
      }
      return updateCount;
   }

   /**
    * The statment to execute, initialized in ctor, never <code>null</code> or
    * empty.
    */
   protected String m_statement = null;

}

