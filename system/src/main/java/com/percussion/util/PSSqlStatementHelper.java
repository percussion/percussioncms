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
package com.percussion.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This is a utility class providing methods to execute JDBC SQL statements
 * without worry about closing statements, result-set and connection.
 * 
 */
public abstract class PSSqlStatementHelper
{
   
   /**
    * Logger for this class
    */
    private static final Logger ms_log = LogManager.getLogger(PSSqlStatementHelper.class);

   /**
    * Prepare a statement for execution.
    * 
    * @param conn the connection used to create the statement, never 
    * <code>null</code>.
    * 
    * @return the prepared statement, never <code>null</code>.
    * 
    * @throws SQLException if failed to create the statement.
    */
   public abstract PreparedStatement prepareStatement(Connection conn)
      throws SQLException;
   
   /**
    * Parse the result set that is returned from executing the statement
    * of {@link #prepareStatement()}.
    * 
    * @param rs the result set, never <code>null</code>.
    * 
    * @throws SQLException if failed to create the statement.
    */
   public abstract void parseResultSet(ResultSet rs) throws SQLException;
   
   /**
    * Gets the SQL in text format. This is used for log in case of error.
    * 
    * @return the SQL text, never <code>null</code> or empty.
    */
   public abstract String getSQLString();
   
   /**
    * Executes the statement that is created by
    * {@link #prepareStatement(Connection)}. The {@link Connection#close()}
    * will be called within this method.
    * 
    * @param conn the connection used for the query, never <code>null</code>.
    */
   public void executeQuery(Connection conn)
   {
      PreparedStatement stmt = null;
      ResultSet rs = null;

      try
      {
         stmt = prepareStatement(conn);
         rs = stmt.executeQuery();
         parseResultSet(rs);
      }
      catch (SQLException e)
      {
         String errorMsg = "Failed to execute SQL statement " + getSQLString();
         ms_log.error(errorMsg, e);
         throw new RuntimeException(e);
      }
      finally
      {
         if (null != rs)
            try
            {
               rs.close();
            }
            catch (Exception e)
            {
               ms_log.error("Failed to close Result Set", e);
            }
         if (null != stmt)
            try
            {
               stmt.close();
            }
            catch (Exception e)
            {
               ms_log.error("Failed to close Statement", e);
            }
         if (conn != null)
         {
            try
            {
               conn.close();
            }
            catch (SQLException e)
            {
               ms_log.error("Failed to close the Connection", e);
            }
         }
      }      
   }
}





