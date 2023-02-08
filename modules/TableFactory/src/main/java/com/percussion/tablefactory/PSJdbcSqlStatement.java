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
         updateCount=0;
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
    * The statement to execute, initialized in ctor, never <code>null</code> or
    * empty.
    */
   protected String m_statement;

}

