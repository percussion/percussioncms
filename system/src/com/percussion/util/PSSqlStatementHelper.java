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
package com.percussion.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
   static Log ms_log = LogFactory.getLog(PSSqlStatementHelper.class);

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





