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

import com.percussion.utils.jdbc.IPSConnectionInfo;

/**
 * Encapsulates information about a change to a table.
 */
public class PSJdbcTableChangeEvent
{
   /**
    * Constructs an event with the information about the table that has changed
    * and the type of change that has occurred using jdbc connection info.
    *
    * @param driver The name of the driver used to access the table that has
    * changed, may not be <code>null</code> or empty.
    * @param server The name of the server on which the database containing the
    * table resides, may be <code>null</code> or empty.
    * @param database The name of the database in which the table is defined,
    * may be <code>null</code> or empty.
    * @param schema The origin or schema of the table, may be <code>null</code>,
    * never empty.
    * @param table The name of the table, may not be <code>null</code> or empty.
    * @param action The type of change that has occurred, must be one of the
    * <code>ACTION_xxx</code> types.
    *
    * @throws IllegalArgumentException if any param is invalid.
    */
   public PSJdbcTableChangeEvent(String driver, String server, String database,
      String schema, String table, int action)
   {
      if (driver == null || driver.trim().length() == 0)
         throw new IllegalArgumentException("driver may not be null or empty");

      if (schema == null || schema.trim().length() == 0)
         throw new IllegalArgumentException("schema may not be null or empty");

      if (table == null || table.trim().length() == 0)
         throw new IllegalArgumentException("table may not be null or empty");

      if (action != ACTION_SCHEMA_CHANGED)
         throw new IllegalArgumentException("invalid action type");         
         
      m_driver = driver;
      m_server = server;
      m_database = database;
      m_schema = schema;
      m_table = table;
      m_action = action;
   }

   /**
    * Constructs an event with the information about the table that has changed
    * and the type of change that has occurred using a datasource.
    *
    * @param connInfo The connection info used to access the table that 
    * has changed, may be <code>null</code> or empty.  
    * @param table The name of the table, may not be <code>null</code> or empty.
    * @param action The type of change that has occurred, must be one of the
    * <code>ACTION_xxx</code> types.
    *
    * @throws IllegalArgumentException if any param is invalid.
    */
   public PSJdbcTableChangeEvent(IPSConnectionInfo connInfo, String table, 
      int action)
   {
      if (table == null || table.trim().length() == 0)
         throw new IllegalArgumentException("table may not be null or empty");

      if (action != ACTION_SCHEMA_CHANGED)
         throw new IllegalArgumentException("invalid action type");         
         
      m_connInfo = connInfo;
      m_table = table;
      m_action = action;
   }
   
   /**
    * Determine if connection info was supplied during construction, rather than
    * the jdbc connection data.
    * 
    * @return <code>true</code> if conn info was supplied, <code>false</code>
    * if driver, server and database were supplied.
    */
   public boolean usedConnInfo()
   {
      return m_driver == null;
   }
   
   /**
    * Get the driver name.
    * 
    * @return The driver name, , may <code>null</code> or empty if the database
    * was located through a JNDI lookup.
    */
   public String getDriver()
   {
      return m_driver;
   }

   /**
    * Gets the server name.
    * 
    * @return The server name, may <code>null</code> or empty if the database
    * was located through a JNDI lookup.
    */
   public String getServer()
   {
      return m_server;
   }

   /**
    * Gets the database name.
    * 
    * @return The database name, may be <code>null</code> or empty.
    */
   public String getDatabase()
   {
      return m_database;
   }

   /**
    * Gets the schema name.
    * 
    * @return The schema or origin, may be <code>null</code> or empty.
    */
   public String getSchema()
   {
      return m_schema;
   }

   /**
    * Gets the table name.
    * 
    * @return The table name, never <code>null</code> or empty.
    */
   public String getTable()
   {
      return m_table;
   }
   
   /**
    * Get the type of action which has occurred.
    * 
    * @return One of the ACTION_xxx types.
    */
   public int getAction()
   {
      return m_action;
   }
   
   /**
    * Get connection info supplied during construction.  Use 
    * {@link #usedConnInfo()} to determine if the event supplied connection
    * info, as it may be supplied as <code>null</code>.
    * 
    * @return The connection info, may be <code>null</code> if not supplied
    * or if <code>null</code> was supplied.
    */
   public IPSConnectionInfo getConnectionInfo()
   {
      return m_connInfo;
   }

   /**
    * Constant to indicate that a schema change has occurred.
    */
   public static final int ACTION_SCHEMA_CHANGED = 1;

   /**
    * Info used to access the database in which the table change has 
    * occurred. May be <code>null</code> or empty, never modified after 
    * construction.
    */
   private IPSConnectionInfo m_connInfo = null;   
   
   /**
    * Driver used to access the database in which the table change has occurred.
    * May be <code>null</code> or empty only if constructed with a datasource,
    * never modified after construction.
    */
   private String m_driver = null;

   /**
    * Server of the database in which the table change has occurred.
    * Initialized in the ctor, may be <code>null</code> or empty, never modified
    * after that.
    */
   private String m_server = null;

   /**
    * Name of the database in which the table change has occurred.
    * Initialized in the ctor, may be <code>null</code> or empty, never modified
    * after that.
    */
   private String m_database = null;

   /**
    * Name of the table that has changed.  Initialized in the
    * ctor, never <code>null</code>, empty, or modified after that.
    */
   private String m_table = null;

   /**
    * Schema or origin of the table that has changed.  Initialized in the
    * ctor, may be <code>null</code> or empty, never modified after that.
    */
   private String m_schema = null;

   /**
    * The type of change that has occurred, set in the ctor, one of the
    * <code>ACTION_xxx</code> types.
    */
   private int m_action;

}
