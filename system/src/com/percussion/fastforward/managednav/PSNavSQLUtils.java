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
package com.percussion.fastforward.managednav;

import com.percussion.extension.services.PSDatabasePool;
import com.percussion.util.PSSqlHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Utility class for SQL database access.
 * 
 * @author DavidBenua
 *  
 */
public class PSNavSQLUtils
{
   /**
    * Static Methods only.
    */
   private PSNavSQLUtils()
   {

   }

   /**
    * Connects to the backend database. The server's storage pool is used, and
    * the default credentials are supplied.
    * 
    * @return the SQL connection.
    * @throws PSNavException
    */
   public static Connection connect() throws PSNavException
   {
      Connection conn = null;
      try
      {
         conn = PSDatabasePool.getDatabasePool().getConnection();
      }
      catch (Exception e)
      {
         log.error("SQL Error {}", e.getMessage());
         log.debug(e.getMessage(),e);
         throw new PSNavException(e);
      }
      return conn;
   }

   /**
    * Releases the connnection to the database.
    * 
    * @param conn the connection
    * @throws PSNavException
    */
   public static void release(Connection conn) throws PSNavException
   {
      try
      {
         PSDatabasePool.getDatabasePool().releaseConnection(conn);
      }
      catch (Exception e)
      {
         log.error("SQL Error {}", e.getMessage());
         log.debug(e.getMessage(),e);
         throw new PSNavException(e);
      }
   }

   /**
    * Close the connection and handle any errors recognized as part of the
    * closing process.
    * 
    * @param conn the connection to close
    * @param stmt the statement. May be <code>null</code>.
    * @param rs the resutl set. May be <code>null</code>
    * @throws PSNavException
    */
   public static void closeout(Connection conn, Statement stmt, ResultSet rs)
         throws PSNavException
   {
      if (rs != null)
      {
         try
         {
            rs.close();
         }
         catch (SQLException e)
         {
            log.error("Closing result set {}", e.getMessage());
            log.debug(e.getMessage(),e);
         }
      }
      if (stmt != null)
      {
         try
         {
            stmt.close();
         }
         catch (SQLException e2)
         {
            log.error("Closing SQL statement {}", e2.getMessage());
            log.debug(e2.getMessage(),e2);
         }
      }
      if (conn != null)
      {
         release(conn);
      }

   }

   /**
    * Qualifies the supplied table name using the default connection info.
    * 
    * @param tableName The name to qualify, may not be <code>null</code> or
    *           empty.
    * 
    * @return The qualified name, never <code>null</code> or empty.
    */
   public static String qualifyTableName(String tableName)
   {
      if (tableName == null || tableName.trim().length() == 0)
         throw new IllegalArgumentException(
               "tableName may not be null or empty");

      PSDatabasePool dbPool = PSDatabasePool.getDatabasePool();
      String table = PSSqlHelper.qualifyTableName(tableName, dbPool
            .getDefaultDatabase(), dbPool.getDefaultSchema(), dbPool
            .getDefaultDriver());

      return table;
   }

   /**
    * Writes the log.
    */
   private static final Logger log = LogManager.getLogger(PSNavSQLUtils.class);

}
