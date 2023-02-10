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
