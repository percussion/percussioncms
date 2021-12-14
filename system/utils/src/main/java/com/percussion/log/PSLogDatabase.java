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
package com.percussion.log;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * This is a utility class providing common functionality/definitions for 
 * the rhythmyx logging databases.
 */
public class PSLogDatabase
{
   /**
    * Returns the log catalog name for the current connection.
    *
    * @param driver a valid driver type, not empty
    * @param conn a valid connection
    * @param db a valid database name, may be empty
    * @return the name of the log catalog or <code>null</code>
    * @throws SQLException for any failed sql operation
    * @throws IllegalArgumentException for any illegal arguments provided
    */
   public static String getLogCatalog(String driver, 
                                      Connection conn, 
                                      String db) throws SQLException
   {
      if (driver == null || driver.length() == 0)
         throw new IllegalArgumentException("need a valid driver type");

      if (conn == null)
         throw new IllegalArgumentException("need a valid connection");

      if (db == null)
         throw new IllegalArgumentException("need a valid database name");
      
      return conn.getCatalog();
   }

   /** the main log table. */         
   public static final String TABLE_PSLOG = "pslog";
   /** the log data table. */         
   public static final String TABLE_PSLOGDAT = "pslogdat";
   /** the log table id field. */
   public static final String COL_LOG_ID_HIGH = "log_id_high";
   /** the log table id field. */
   public static final String COL_LOG_ID_LOW = "log_id_low";
   /** the log table type field. */
   public static final String COL_LOG_TYPE = "log_type";
   /** the log table application field. */
   public static final String COL_LOG_APPL = "log_appl";
   /** the log main table primary key field. */
   public static final String COL_PSLOG_PKEY = "pslog_pkey";
   /** the log table sequence field. */
   public static final String COL_LOG_SEQ = "log_seq";
   /** the log table subtable  field. */
   public static final String COL_LOG_SUBT = "log_subt";
   /** the log table subsequence field */
   public static final String COL_LOG_SUBSEQ = "log_subseq";
   /** the log table data field */
   public static final String COL_LOG_DATA = "log_data";
   /** the log data table primary key field. */
   public static final String COL_PSLOGDAT_PKEY = "pslogdat_pkey";
}
