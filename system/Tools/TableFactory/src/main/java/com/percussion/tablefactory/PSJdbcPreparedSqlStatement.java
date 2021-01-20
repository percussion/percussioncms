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
package com.percussion.tablefactory;

import com.percussion.util.PSCollection;
import com.percussion.util.PSPreparedStatement;
import org.apache.commons.io.IOUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;


/**
 * This is a SqlStatement that can be prepared and contains bound parameters.
 */
public class PSJdbcPreparedSqlStatement extends PSJdbcSqlStatement
{
   /**
    * Constructs Sql statement to execute.
    *
    * @param statement The statement to execute, may have bound parameter
    * tokens ("?").  May not be <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if statement is <code>null</code> or
    * empty.
    */
   public PSJdbcPreparedSqlStatement(String statement)
   {
      super(statement);
   }

   /**
    * Constructs Sql statement to execute, including the parameter values.
    *
    * @param statement The statement to execute, may have bound parameter
    * tokens ("?").  May not be <code>null</code> or empty.
    * @param values A list of {@link PSJdbcStatementColumn} objects, may be
    * <code>null</code> but not empty.
    *
    * @throws IllegalArgumentException if statement is <code>null</code> or
    * empty, or  if values does not contain at least one object, or does not
    * contain object of the correct type.
    */
   public PSJdbcPreparedSqlStatement(String statement, PSCollection values)
   {
      super(statement);
      setValues(values);
   }

   /**
    * A list of values to bind when the statement is executed.
    *
    * @param values A list of {@link PSJdbcStatementColumn} objects, may be
    * <code>null</code> but not empty.
    *
    * @throws IllegalArgumentException if values does not contain at least one
    * object, or does not contain object of the correct type.
    */
   public void setValues(PSCollection values)
   {
      if (values != null)
      {
         if (values.isEmpty())
            throw new IllegalArgumentException(
               "values must contain at least one object");

         if (!values.getMemberClassType().getName().equals(
            PSJdbcStatementColumn.class.getName()))
         {
            throw new IllegalArgumentException(
               "values must contain instances of PSJdbcStatementColumn");
         }
      }
      m_values = values;
   }

   // see base class
   public int execute(Connection conn) throws SQLException
   {
      PSJdbcTableFactory.logDebugMessage(m_statement);
      int updateCount = 0;
      PreparedStatement stmt = null;

      PSJdbcExecutionStepLog stepLogData = getStepLogData();
      stepLogData.setConnectionString(conn);
      stepLogData.setSqlQuery(m_statement);

      try
      {
         stmt = PSPreparedStatement.getPreparedStatement(conn, m_statement);
         if (m_values != null)
            bindValues(stmt, m_values);
         boolean result = stmt.execute();

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
                  "error closing prepared statement: " +
                  PSJdbcTableFactoryException
                     .formatSqlException(m_statement, e));
            }
         }
         closeColumnStreams(m_values);
       
      }
      return updateCount;
   }

   protected void closeColumnStreams(PSCollection values) {
      try {
         if (values != null) {
            Iterator valueIt = values.iterator();
            while (valueIt.hasNext()) {
               PSJdbcStatementColumn value = (PSJdbcStatementColumn) valueIt.next();
               if (value.getBinaryValue() != null && value.getBinaryValue().getStream() != null) {
                  IOUtils.closeQuietly(value.getBinaryValue().getStream());
               }
            }
         }
      } catch (Exception e)
      {
         PSJdbcTableFactory.logDebugMessage(
                 "error closing column streams: " + e.getMessage() +":" + 
                         PSJdbcTableFactoryException.getStackTraceAsString(e));
      }
   }

   /**
    * Binds the list of values to the statement.
    *
    * @param stmt The PreparedStatement, assumed not <code>null</code>.
    * @param value A list of one or more PSJdbcStatementColumn objects,
    * assumed not <code>null</code> or empty.
    *
    * @throws SQLException if any errors occur
    */
   protected void bindValues(PreparedStatement stmt, PSCollection values)
      throws SQLException
   {
      for (int i=0; i < values.size(); i++)
      {
         PSJdbcStatementColumn value = (PSJdbcStatementColumn)values.get(i);
         try
         {
          
            PSJdbcTableFactory.logDebugMessage("bind value " + i + ": " +
               value.getValue());
          
            value.setDataFromString(stmt, i+1);
         }
         catch(Exception e)
         {
            // convert to SQLException
            Object args[] = {value.getValue(), new Integer(value.getType()),
               e.toString()};
            PSJdbcTableFactoryException fe =  new PSJdbcTableFactoryException(
               IPSTableFactoryErrors.SQL_BIND_PARAMETER, args, e);
            throw new SQLException(fe.getLocalizedMessage(),e);
         }
      }
   }

   /**
    * List of {@link PSJdbcStatementColumn} objects, used to bind values into
    * the statement at runtime.  May be <code>null</code> if the statement does
    * not require any parameters, but never emtpy if not <code>null</code>.
    */
   protected PSCollection m_values = null;

}

