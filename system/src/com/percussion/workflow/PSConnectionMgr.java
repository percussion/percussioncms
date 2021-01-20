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
package com.percussion.workflow;

import com.percussion.extension.services.PSDatabasePool;
import com.percussion.utils.jdbc.PSConnectionDetail;
import com.percussion.utils.jdbc.PSConnectionHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.naming.NamingException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * Simple utility class that gets and releases a JDBC connection from the
 * server's Database pool.
 */


public class PSConnectionMgr
{
   /**
    * Standard constructor used to create a <CODE>PSConnectionMgr</CODE> that
    * can be used to get a server database pool connect for workflow use via
    * {@link #getConnection()}
    */
   public PSConnectionMgr() throws SQLException
   {
   }

   /**
    * Returns the JDBC connection object created by the server database pool
    * which is a private member of the <CODE>PSConnectionMgr</CODE>; subsequent
    * calls will return the same connection. When the user is done with the
    * connection, it should be freed via a call to{@link #releaseConnection()}
    * @return  the JDBC connection object from  server database pool associated
    * with this connection manager.
    * @throws  SQLException if an SQL error occurs
    * @throws  NamingException if the datasource cannot be resolved
    */
   public synchronized Connection getConnection()
      throws SQLException, NamingException
   {
      Connection connection =  getWithOptionsConnection();
      return connection;
   }

   /**
    * Releases the stored JDBC connection object
    * 
    * @throws SQLException if there are any errors. 
    */
   @SuppressWarnings("deprecation")
   public synchronized void releaseConnection()
      throws SQLException
   {
      if(m_Connection != null)
      {
         if (m_bUseDatabasePool)
         {
            PSDatabasePool.getDatabasePool().releaseConnection(m_Connection);
             m_Connection= null;
         }
         else
         {
            m_Connection.close();
         }
      }
      // do a try and make sure to set m_connection = null
   }

   /**
    * Returns a new JDBC connection object created by the DriverManager for
    * which the maximum transaction isolation level has already been set.
    * The connection is obtained using the driver, database and user
    * information contained in the workflow properties file, which are the same
    * as those used to create the standard workflow database connection via
    * {@link #getConnection()}
    * This method is intended for debugging and test use only, not as part
    * of production workflow software. The connection created by this method
    * should be freed by a call to {@link #releaseDebugConnection(Connection)}
    *
    * @return  a JDBC connection object for debug use.
    * @throws  SQLException if an SQL error occurs
    * @throws  NamingException if the datasource cannot be resolved
    */
   public static Connection getDebugConnection()
       throws SQLException, NamingException
   {
      Connection connection = null;
      PSConnectionMgr connectionMgr =
            new PSConnectionMgr(true,  // Get a new connection.
                                false  // Do not use the server database pool.
                                ); // Set transaction isolation level.
      connection = connectionMgr.getWithOptionsConnection();
      connectionMgr = null;
      return connection;
   }

   /**
    * Releases a debug JDBC connection object.
    * This is needed for because {@link #releaseConnection(Connection)}
    * is not static.
    * @param connection  connection to be released (closed)
    * @throws            SQLException if an SQL error occurs
    */
   public static void releaseDebugConnection(Connection connection)
      throws SQLException
   {
      if(connection != null)
      {
            connection.close();
      }
   }

   /**
    * Returns a new JDBC connection object created by the server database pool;
    * Since the PSConnectionMgr does not keep track of this connection, the
    * user is responsible for freeing it via a call to
    * {@link #releaseConnection(Connection)}
    * @return  a JDBC connection object from  server database pool.
    * @throws  SQLException if an SQL error occurs
    * @throws  NamingException if the datasource cannot be resolved
    */
   public static Connection getNewConnection()
       throws SQLException, NamingException
   {
      Connection connection = null;
      PSConnectionMgr connectionMgr =
            new PSConnectionMgr(true,  // Get a new connection.
                                true   // Use the server database pool.
                                ); //Don't set transaction isolation level
      connection = connectionMgr.getWithOptionsConnection();
      connectionMgr = null;
      return connection;
   }

   /**
    * Releases a specific JDBC connection object from server pool
    * 
    * @param connection The connection to release, ignored if <code>null</code>.
    *  
    * @throws SQLException If there are any errors.
    */
   @SuppressWarnings("deprecation")
   public static void releaseConnection(Connection connection)
      throws SQLException
   {
      if(connection != null)
      {
         PSDatabasePool.getDatabasePool().releaseConnection(connection);
      }
   }

   /**
    * General constructor specifying characteristics of the desired connection:
    * new/single connection, server/standalone connection, set/don't
    * transaction isolation level. Private, for use only by other constructors,
    * e.g.  {@link #getDebugConnection},  {@link #getNewConnection}
    *
    * @param getNewConnection         <CODE>true</CODE> if connection request
    *                                 will always produce new connections that
    *                                 must be managed by the user,
    *                                 <CODE>false</CODE> if a connection
    *                                 should be created only if the
    *                                 <CODE>PSConnectionMgr</CODE> has not yet
    *                                 created and stored a connection.
    * @param useDatabasePool          <CODE>true</CODE> if the connection
    *                                 should come from the e2 server db pool
    *                                 <CODE>false</CODE> to get a connection
    *                                 via DriverManager.getConnection.
    * @throws SQLException if an error occurs
    */
   @SuppressWarnings("unused")
   private PSConnectionMgr(boolean getNewConnection,
                           boolean useDatabasePool)
      throws SQLException
   {
      m_bGetNewConnection = getNewConnection;
      m_bUseDatabasePool = useDatabasePool;

      if (m_bGetNewConnection)
      {
         m_Connection = null;
      }
   }

   /**
    * Returns a JDBC connection object which that may be created by the
    * database pool or by the DriverManager, and may be new or stored, and may
    * have its transaction isolation level set to maximum, depending on options
    * specified by the constructor {@link #PSConnectionMgr(boolean, boolean)}.
    * 
    * @return  a JDBC connection object with the desired characteristics
    * @throws  SQLException if an SQL error occurs
    * @throws  NamingException if the datasource cannot be found
    */
   private synchronized Connection getWithOptionsConnection()
      throws SQLException, NamingException
   {
      Connection theConnection = null;

      if (m_Connection != null && !m_bGetNewConnection )
         // Use existing connection if it exits and is wanted
      {
         theConnection = m_Connection;
      }
      else // Get a new connection
      {
         theConnection = PSConnectionHelper.getDbConnection(null);

         if (!m_bGetNewConnection) // Save the connection if desired
         {
            m_Connection = theConnection;
         }

         if (!ms_initDBMDInfo)
         {
            DatabaseMetaData dbmd = theConnection.getMetaData();
            m_bStoresLowerCaseIdentifiers = dbmd.storesLowerCaseIdentifiers();
            m_bStoresUpperCaseIdentifiers = dbmd.storesUpperCaseIdentifiers();
            m_bSupportsCatalogsInDataManipulation =
                  dbmd.supportsCatalogsInDataManipulation();
            m_bIsCatalogAtStart = dbmd.isCatalogAtStart();
            m_sCatalogSeparator = dbmd.getCatalogSeparator();
            m_bSupportsSchemasInDataManipulation =
                  dbmd.supportsSchemasInDataManipulation();

            ms_initDBMDInfo = true;
         }
      }
      return theConnection;
   }


   /**
    * This method takes care of the case issues in the select statements. We
    * use this for table names only.
    *
    * @param identifier (Table name)
    *
    * @return casef ixed identifier
    *
    */
   static private String fixIdentifierCase(String identifier)
   {
      if(identifier == null)
         return null;

      if (m_bStoresLowerCaseIdentifiers)
         identifier = identifier.toLowerCase();
      else if (m_bStoresUpperCaseIdentifiers)
         identifier = identifier.toUpperCase();

      return identifier;
   }

   /**
    * This method qualifies the identifier with DBMS supproted way. for
    * Example, the DBMS may or may not suport schemas, databases etc. We use
    * this for table names only.
    *
    * @param sIdentifier (Table name)
    *
    * @return fully qualified identifier (Table name).
    *
    */
   static public String getQualifiedIdentifier(String sIdentifier)
   {
      PSConnectionDetail detail = null;
      if (!ms_initConnInfo)
      {
         
         try
         {
            detail = PSConnectionHelper.getConnectionDetail(
               null);
            ms_database = detail.getDatabase();
            ms_schema = detail.getOrigin();
            ms_initConnInfo = true;
         }
         catch (Exception e)
         {
            throw new RuntimeException("Failed to obtain connection detail: " 
               + e.getLocalizedMessage(), e);
         }
      }
      
      sIdentifier = fixIdentifierCase(sIdentifier);
      StringBuffer buf = new StringBuffer();

      boolean bAddedCatalog = false;
      String sCatalog = null;

      if((ms_database != null) && (ms_database.length() != 0)
         && m_bSupportsCatalogsInDataManipulation)
      {
         if (m_bIsCatalogAtStart)
         {
            buf.append(ms_database);
            buf.append(m_sCatalogSeparator);
            bAddedCatalog = true;
         }
         else
            sCatalog = m_sCatalogSeparator + ms_database;
      }


      /* if we have an origin, see if it's permitted
       * if we've already written the catalog info to the front,
       * we then need to add the schema, even if it's empty,
       * to avoid catalog.table from being treated as
       * schema.table.
       */
      String sOrigin = ms_schema;
      if(null == sOrigin)
         sOrigin = "";

      if(((sOrigin.length() > 0) || bAddedCatalog) &&
         m_bSupportsSchemasInDataManipulation)
      {
         buf.append(sOrigin);
         buf.append('.');
      }

      buf.append(sIdentifier);   // this has to be there

      // if catalog belongs on the end, take care of it now
      if((false == bAddedCatalog) && (null != sCatalog))
         buf.append(sCatalog);

      return buf.toString();
   }

   /**
    * Name of the database.  Initialized by first call to 
    * {@link #getQualifiedIdentifier(String)}, may be <code>null</code> or 
    * empty.
    */
   static private String ms_database;

   /**
    * Name of the schema/origin.  Initialized by first call to 
    * {@link #getQualifiedIdentifier(String)}, may be <code>null</code> or 
    * empty.
    */   
   static private String ms_schema;
   
   /**
    * Indicates if connection info used by 
    * {@link #getQualifiedIdentifier(String)} has been initialized yet.
    */
   static private boolean ms_initConnInfo = false;

   /**
    * <CODE>true</CODE> if a new connection should be returned, else
    * <CODE>false</CODE>.
    */
   static public boolean m_bGetNewConnection = false;

   /**
    * <CODE>true</CODE> if the e2 server db pool should be used, else
    * <CODE>false</CODE> to get a connection via DriverManager.getConnection.
    */
   static public boolean m_bUseDatabasePool = true;

   /**
    * Connection string for <CODE>DriverManager.getConnection</CODE> if
    * e2 server db pool is not being used, else "".
    */
   static public String m_sConnStr = "";

   /**
    * Indicates if dbmd info used by 
    * {@link #getWithOptionsConnection()} has been initialized yet.
    */
   static private boolean ms_initDBMDInfo = false;   
   
   static public final String DB_BACKEND = "DB_BACKEND";
   static public final String DB_DRIVER_CLASS_NAME = "DB_DRIVER_CLASS_NAME";
   static public final String DB_DRIVER_NAME = "DB_DRIVER_NAME";
   static public final String DB_SERVER_NAME = "DB_SERVER";
   static public final String DB_DATABASE_NAME = "DB_NAME";
   static public final String DB_SCHEMA_NAME = "DB_SCHEMA";
   static public final String DB_USERID = "UID";
   static public final String DB_PASSWORD = "PWD";

   private Connection   m_Connection = null;

   static public boolean m_bStoresLowerCaseIdentifiers = false;
   static public boolean m_bStoresUpperCaseIdentifiers = true;
   static public boolean m_bSupportsCatalogsInDataManipulation = false;
   static public boolean m_bIsCatalogAtStart = false;
   static public String  m_sCatalogSeparator = ".";
   static public boolean m_bSupportsSchemasInDataManipulation = false;
   
   /**
    * Logger
    */
   private static Log ms_log = LogFactory.getLog(PSConnectionMgr.class);

}
