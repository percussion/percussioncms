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

package com.percussion.data.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLWarning;


/**
 * The PSXmlConnection class extends the File System driver's
 * connection handling for XML file support.
 * 
 * @author      Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSXmlConnection extends PSFileSystemConnection {


   /** 
    * @author chadloder
    * 
    * Creates a connection to the given catalog. 
    * 
    * @param   catalog   
    * 
    * @param   url   
    * 
    * @param   driver   
    * 
    * @since 1.2 1999/5/7
    *
    */
   public PSXmlConnection(java.lang.String catalog, String url,
      String sessionId, PSXmlDriver driver)
      throws SQLException
   {
      super(catalog, url, sessionId, driver);   
   }

   /**
    * Create an XML statement execution object. This is the only
    * supported method at this time. Parameters are not supported.
    *
    * @return      the new Statement object (PSXmlStatement)
    * @exception   SQLException   if an error occurs
    */
   public java.sql.Statement createStatement() throws SQLException
   {
      checkClosed();
      return new PSXmlStatement(this);
   }

   /**
    * This is not currently supported.
    *
    * @param      sql            the SQL statement, optionally containing
    *                            placeholders ('?')
    * @return                     the new PreparedStatement object
    * @exception   SQLException   always thrown as this is not supported
    */
   public java.sql.PreparedStatement prepareStatement(java.lang.String sql)
      throws SQLException
   {
      if (true)
         throw new SQLException("unsupported");
      checkClosed();
      return null;
   }

   /**
    * This is not currently supported.
    *
    * @param      sql            the SQL statement, optionally containing
    *                            placeholders ('?')
    * @return                     the new CallableStatement object
    * @exception   SQLException   always thrown as this is not supported
    */
   public java.sql.CallableStatement prepareCall(java.lang.String sql)
      throws SQLException
   {
      if (true)
         throw new SQLException("unsupported");
      checkClosed();
      return null;
   }

   /**
    * This is not currently supported.
    *
    * @param      sql            the SQL statement, optionally containing
    *                            placeholders ('?')
    * @return                     the native form of the statement
    * @exception   SQLException   always thrown as this is not supported
    */
   public java.lang.String nativeSQL(java.lang.String sql)
      throws SQLException
   {
      if (true)
         throw new SQLException("unsupported");
      checkClosed();
      return null;
   }

   /**
    * When this is enabled, all changes made to the XML file are written
    * to disk immediately. If this is disabled, changes will be cached
    * until rollback or commit is called.
    *
    * @param      autoCommit      <code>true</code> to enable auto-commit,
    *                            <code>false</code> to disable it
    * @exception   SQLException   if an error occurs
    */
   public void setAutoCommit(boolean autoCommit)
      throws SQLException
   {
      checkClosed();
   }

   /**
    * Get the current auto-commit mode. When this is enabled, all changes
    * made to the XML file are written to disk immediately. If this is
    * disabled, changes will be cached until rollback or commit is called.
    *
    * @return                     <code>true</code> if auto-commit is enabled,
    *                            <code>false</code> otherwise
    * @exception   SQLException   if an error occurs
    */
   public boolean getAutoCommit()
      throws SQLException
   {
      checkClosed();
      return false;
   }

   /**
    * Commit all changes made since the previous commit/rollback call.
    * This forces all stored changes made to the XML file to be written
    * to disk.
    *
    * @exception   SQLException   if an error occurs
    */
   public void commit()
      throws SQLException
   {
      if (true)
         throw new SQLException("unsupported");
      checkClosed();
   }

   /**
    * Rollback all changes made since the previous commit/rollback call.
    * This discards all stored changes made to the XML file, reverting it
    * to its previous state.
    *
    * @exception   SQLException   if an error occurs
    */
   public void rollback()
      throws SQLException
   {
      if (true)
         throw new SQLException("unsupported");
      checkClosed();
   }

   /**
    * Close the XML file associated with this connection. Any subsequent
    * attempts to use this connection will cause an exception. Using this
    * method is more efficient than waiting for the garbage collector to
    * close the connection.
    *
    * @exception   SQLException   if an error occurs
    */
   public void close()
      throws SQLException
   {
      super.close();
   }

   /**
    * Access information about the XML files
    * associated with this connection.
    *
    * @return                     the DatabaseMetaData object
    * @exception   SQLException   if an error occurs
    */
   public java.sql.DatabaseMetaData getMetaData()
      throws SQLException
   {
      checkClosed();
      // only construct this when meta data is needed
      if (null == m_metaData)
         m_metaData = new PSXmlDatabaseMetaData(this);
      return m_metaData;      
   }

   /**
    * Enable or disable read-only access to the XML file.
    *
    * @param      autoCommit      <code>true</code> to enable read-only
    *                            access, <code>false</code> to disable it
    * @exception   SQLException   if an error occurs
    */
   public void setReadOnly(boolean readOnly)
      throws SQLException
   {
      checkClosed();
   }

   /**
    * Is the XML file open in read-only mode?
    *
    * @return                     <code>true</code> if read-only is set,
    *                            <code>false</code> otherwise
    * @exception   SQLException   if an error occurs
    */
   public boolean isReadOnly()
      throws SQLException
   {
      checkClosed();
      return false;
   }
  
   /**
    * This is not currently supported.
    *
    * @param      level          the TRANSACTION_xxx isolation level
    * @exception   SQLException   always thrown as this is not supported
    */
   public void setTransactionIsolation(int level)
      throws SQLException
   {
      if (true)
         throw new SQLException("unsupported");
      checkClosed();
   }

   /**
    * Get the current transaction isolation level.
    *
    * @return      Connection.TRANSACTION_NONE is always returned as this
    *             drier does not support transaction isolation
    * @exception   SQLException   if an error occurs
    */
   public int getTransactionIsolation()
      throws SQLException
   {
      checkClosed();
      return Connection.TRANSACTION_NONE;
   }

   /**
    * Get the first warning reported on this connection. Warnings are
    * chained, so use the returned warning to iterate the warnings.
    *
    * @return      the first warning, or <code>null</code> of none exist
    * @exception   SQLException   if an error occurs
    */
   public SQLWarning getWarnings()
      throws SQLException
   {
      checkClosed();
      return null;
   }

   /**
    * Remove all warnings associated with this connection.
    * @exception   SQLException   if an error occurs
    */
   public void clearWarnings()
      throws SQLException
   {
      checkClosed();
   }

   /**
    * JDBC 2.0 Creates a Statement object that will generate ResultSet
    * objects with the given type and concurrency. This method is the
    * same as the createStatement method above, but it allows the default
    * result set type and result set concurrency type to be overridden.
    *
    * @param   resultSetType            a result set type; see
    *                                  ResultSet.TYPE_XXX
    *
    * @param   resultSetConcurrency    a concurrency type; see
    *                                  ResultSet.CONCUR_XXX
    *
    * @return   a new Statement object
    *
    * @exception   SQLException   if a database access error occurs
    */
   public java.sql.Statement createStatement(int resultSetType,
                                             int resultSetConcurrency)
      throws SQLException
   {
      checkClosed();
      return null;
   }

   /**
    * JDBC 2.0 Creates a PreparedStatement object that will generate
    * ResultSet objects with the given type and concurrency. This method
    * is the same as the prepareStatement method above, but it allows the
    * default result set type and result set concurrency type to be
    * overridden.
    *
    * @param   resultSetType            a result set type; see
    *                                  ResultSet.TYPE_XXX
    *
    * @param   resultSetConcurrency    a concurrency type; see
    *                                  ResultSet.CONCUR_XXX
    *
    * @return   a new PreparedStatement object containing the pre-compiled
    *          SQL statement
    *
    * @exception   SQLException   if a database access error occurs
    */
    public java.sql.PreparedStatement prepareStatement(java.lang.String sql,
                                                      int resultSetType,
                                                      int resultSetConcurrency)
      throws SQLException
   {
      checkClosed();
      return null;
   }

   /**
    * JDBC 2.0 Creates a CallableStatement object that will generate
    * ResultSet objects with the given type and concurrency. This method
    * is the same as the prepareCall method above, but it allows the
    * default result set type and result set concurrency type to be
    * overridden.
    *
    * @param   resultSetType            a result set type; see
    *                                  ResultSet.TYPE_XXX
    *
    * @param   resultSetConcurrency    a concurrency type; see
    *                                  ResultSet.CONCUR_XXX
    *
    * @return   a new CallableStatement object containing the pre-compiled
    *          SQL statement
    *
    * @exception   SQLException   if a database access error occurs
    */
   public java.sql.CallableStatement prepareCall(   java.lang.String sql,
                                                   int resultSetType,
                                                   int resultSetConcurrency)
      throws SQLException
   {
      checkClosed();
      return null;
   }

   /**
    * JDBC 2.0 Gets the type map object associated with this connection.
    * Unless the application has added an entry to the type map, the map
    * returned will be empty.
    *
    * @return   the java.util.Map object associated with this Connection
    *          object
    */
   public java.util.Map getTypeMap()
      throws SQLException
   {
      checkClosed();
      return null;
   }

   /**
    * JDBC 2.0 Installs the given type map as the type map for this
    * connection. The type map will be used for the custom mapping of
    * SQL structured types and distinct types.
    *
    * @param   map      the java.util.Map object to install as the
    *                   replacement for this Connection object's default
    *                   type map
    */
   public void setTypeMap(java.util.Map map)
      throws SQLException
   {
      checkClosed();
   }

   protected void finalize() throws Throwable
   {
      super.finalize();
   }

}

