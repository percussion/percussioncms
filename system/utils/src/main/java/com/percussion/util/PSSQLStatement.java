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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This is a proxy (or wrapper) class to a <code>Statement</code> object. It 
 * delegates all methods to the <code>Statement</code> object and logs the 
 * SQL statement and elapse time for each SQL execution (query or update). 
 * <P>
 * This class can only take effect after the log4j is properly configured
 * and the debug mode is enabled for this class. 
 * <P>
 * Must use {@link #getStatement(Connection)} to create a <code>Statement</code>
 * object.
 */
public class PSSQLStatement implements Statement
{
   /**
    * Creates a <code>Statement<code> object from the supplied connection.
    *
    * @param conn the connection, may not be <code>null</code>.
    *
    * @return the created <code>PSSQLStatement<code> object if the log4j is
    *    configured and debug mode is on for this class; otherwise return 
    *    <code>Statement</code> object, never <code>null</code>.
    *
    * @throws SQLException if error occurs.
    */
   static public Statement getStatement(Connection conn) throws SQLException
   {
      if (conn == null)
         throw new IllegalArgumentException("conn may not be null");

      Statement stmt = conn.createStatement();

      if (isLogEnabled())
        stmt = new PSSQLStatement(stmt);
      
      return stmt;
   }

   /**
    * Just like {@link #getStatement(Connection)} with additional parameters.
    *  
    * Creates a <code>Statement<code> object from the supplied connection.
    *
    * @param conn the connection, may not be <code>null</code>.
    * @param resultSetType a result set type; see ResultSet.TYPE_XXX
    * @param resultSetConcurrency a concurrency type; see ResultSet.CONCUR_XXX
    *
    * @return the created <code>PSSQLStatement<code> object if the log4j is
    *    configured and debug mode is on for this class; otherwise return 
    *    <code>Statement</code> object, never <code>null</code>.
    *
    * @see Connection.createStatement(int, int)
    * 
    * @throws SQLException if error occurs.
    */
   static public Statement getStatement(
      Connection conn,
      int resultSetType,
      int resultSetConcurrency)
      throws SQLException
   {
      if (conn == null)
         throw new IllegalArgumentException("conn may not be null");

      Statement stmt =
         conn.createStatement(resultSetType, resultSetConcurrency);

      if (isLogEnabled())
        stmt = new PSSQLStatement(stmt);
      
      return stmt;
   }
   
   // Implements Statement.executeQuery(String)
   public ResultSet executeQuery(String sql) throws SQLException
   {
      startTimer(sql);
      ResultSet rs = m_stmt.executeQuery(sql);
      logElapsedTime();

      return rs;
   }

   // Implements Statement.executeUpdate(String)
   public int executeUpdate(String sql) throws SQLException
   {
      startTimer(sql);
      int rs = m_stmt.executeUpdate(sql);
      logElapsedTime();

      return rs;
   }

   // Implements Statement.close(String)
   public void close() throws SQLException
   {
      m_stmt.close();
   }

   //----------------------------------------------------------------------

   // Implements Statement.getMaxFieldSize(String)
   public int getMaxFieldSize() throws SQLException
   {
      return m_stmt.getMaxFieldSize();
   }


   // Implements Statement.setMaxFieldSize(int)
   public void setMaxFieldSize(int max) throws SQLException
   {
      m_stmt.setMaxFieldSize(max);
   }

   // Implements Statement.getMaxRows()
   public int getMaxRows() throws SQLException
   {
      return m_stmt.getMaxRows();
   }

   // Implements Statement.setMaxRows(int)
   public void setMaxRows(int max) throws SQLException
   {
      m_stmt.setMaxRows(max);
   }

   // Implements Statement.setMaxRows(int)
   public void setEscapeProcessing(boolean enable) throws SQLException
   {
      m_stmt.setEscapeProcessing(enable);
   }

   // Implements Statement.getQueryTimeout()
   public int getQueryTimeout() throws SQLException
   {
      return m_stmt.getQueryTimeout();
   }

   // Implements Statement.setQueryTimeout(int)
   public void setQueryTimeout(int seconds) throws SQLException
   {
      m_stmt.setQueryTimeout(seconds);
   }

   // Implements Statement.cancel()
   public void cancel() throws SQLException
   {
      m_stmt.cancel();
   }

   // Implements Statement.getWarnings()
   public SQLWarning getWarnings() throws SQLException
   {
      return m_stmt.getWarnings();
   }

   // Implements Statement.clearWarnings()
   public void clearWarnings() throws SQLException
   {
      m_stmt.clearWarnings();
   }

   // Implements Statement.setCursorName(String)
   public void setCursorName(String name) throws SQLException
   {
      m_stmt.setCursorName(name);
   }

   //----------------------- Multiple Results --------------------------

   // Implements Statement.execute(String)
   public boolean execute(String sql) throws SQLException
   {
      startTimer(sql);
      boolean rs = m_stmt.execute(sql);
      logElapsedTime();

      return rs;
   }

   // Implements Statement.getResultSet()
   public ResultSet getResultSet() throws SQLException
   {
      return m_stmt.getResultSet();
   }

   // Implements Statement.getUpdateCount()
   public int getUpdateCount() throws SQLException
   {
      return m_stmt.getUpdateCount();
   }

   // Implements Statement.getMoreResults()
   public boolean getMoreResults() throws SQLException
   {
      return m_stmt.getMoreResults();
   }


   //--------------------------JDBC 2.0-----------------------------


   // Implements Statement.setFetchDirection()
   public void setFetchDirection(int direction) throws SQLException
   {
      m_stmt.setFetchDirection(direction);
   }

   // Implements Statement.getFetchDirection()
   public int getFetchDirection() throws SQLException
   {
      return m_stmt.getFetchDirection();
   }

   // Implements Statement.setFetchSize(int)
   public void setFetchSize(int rows) throws SQLException
   {
      m_stmt.setFetchSize(rows);
   }

   // Implements Statement.getFetchSize()
   public int getFetchSize() throws SQLException
   {
      return m_stmt.getFetchSize();
   }

   // Implements Statement.getResultSetConcurrency()
   public int getResultSetConcurrency() throws SQLException
   {
      return m_stmt.getResultSetConcurrency();
   }

   // Implements Statement.getResultSetType()
   public int getResultSetType()  throws SQLException
   {
      return m_stmt.getResultSetType();
   }


   // Implements Statement.addBatch(String)
   public void addBatch( String sql ) throws SQLException
   {
      m_stmt.addBatch(sql);
   }

   // Implements Statement.clearBatch()
   public void clearBatch() throws SQLException
   {
      m_stmt.clearBatch();
   }

   // Implements Statement.executeBatch()
   public int[] executeBatch() throws SQLException
   {
      startTimer();
      int[] rs = m_stmt.executeBatch();
      logElapsedTime();

      return rs;

   }

   // Implements Statement.getConnection()
   public Connection getConnection()  throws SQLException
   {
      return m_stmt.getConnection();
   }

   // Implements Statement.getGeneratedKeys()
   public ResultSet getGeneratedKeys() throws SQLException 
   {
      return m_stmt.getGeneratedKeys();
   }

   // Implements Statement.getResultSetHoldability()
   public int getResultSetHoldability() throws SQLException 
   {
      return m_stmt.getResultSetHoldability();
   }
   
   // Implements Statement.getMoreResults(int)
   public boolean getMoreResults(int current) throws SQLException
   {
      return m_stmt.getMoreResults(current);
   }

   // Implements Statement.execute(String, String[])
   public boolean execute(String sql, String columnNames[]) throws SQLException 
   {
      startTimer(sql);
      boolean rs = m_stmt.execute(sql, columnNames);
      logElapsedTime();
      return rs;
   }

   // Implements Statement.execute(String, int[])
   public boolean execute(String sql, int columnIndexes[]) throws SQLException 
   {
      startTimer(sql);
      boolean rs = m_stmt.execute(sql, columnIndexes);
      logElapsedTime();
      return rs;
   }

   // Implements Statement.execute(String, int)
   public boolean execute(String sql, int autoGeneratedKeys)
            throws SQLException 
   {
      startTimer(sql);
      boolean rs = m_stmt.execute(sql, autoGeneratedKeys);
      logElapsedTime();
      return rs;
   }

   // Implements Statement.executeUpdate(String, int)
   public int executeUpdate(String sql, int autoGeneratedKeys)
            throws SQLException 
   {
      startTimer(sql);
      int rs = m_stmt.executeUpdate(sql, autoGeneratedKeys);
      logElapsedTime();
      return rs;
   }

   // Implements Statement.executeUpdate(String, int[])
   public int executeUpdate(String sql, int columnIndexes[])
            throws SQLException 
   {
      startTimer(sql);
      int rs = m_stmt.executeUpdate(sql, columnIndexes);
      logElapsedTime();
      return rs;
   }

   // Implements Statement.executeUpdate(String, String[])
   public int executeUpdate(String sql, String columnNames[])
            throws SQLException 
   {
      startTimer(sql);
      int rs = m_stmt.executeUpdate(sql, columnNames);
      logElapsedTime();
      return rs;
   }
  
   /**
    * Creates an object from the given parameters.
    *
    * @param stmt the preparedstatement object, assume not <code>null</code>.
    */
   protected PSSQLStatement(Statement stmt)
   {
      m_stmt = stmt;
   }   
   
   /**
    * Logs the execution elapse time. Must call {@link #startTimer()} or 
    * {@link #startTimer(String)} previously.  
    */
   protected void logElapsedTime()
   {
      m_timer.stop();
      ms_log.debug("SQL[" + m_currSQLExeCount + "]: " + m_timer.toString());
   }
   
   /**
    * Starts the stop watch for recording the elapse time of a SQL execution.
    * Must be called before each SQL execution, call {@link #logElapsedTime()}
    * after the execution.
    * 
    * @see #logElapsedTime()
    */
   protected void startTimer()
   {
      m_timer.start();
      m_currSQLExeCount = ++ms_sqlExeCount; 
      
      ms_log.debug("SQL[" + m_currSQLExeCount + "]: \n" + m_sqlStatement);
      ms_log.debug("\n" + m_additionalLogInfo.toString());
      m_additionalLogInfo = new StringBuffer(); // flush the buffer
   }
   
   /**
    * Just like {@link #startTimer()}, it will also set the SQL statement.
    * 
    * @param sql the SQL statement, assume not <code>null</code> or empty.
    */
   protected void startTimer(String sql)
   {
      m_sqlStatement = sql;
      startTimer();
   }
   
   /**
    * Determines if the log is enabled for this class.
    * 
    * @return <code>true</code> if the log is enabled; <code>false</code>  
    *    otherwise.
    */
   protected static boolean isLogEnabled()
   {
      return (ms_log != null && ms_log.isDebugEnabled());
   }

   /**
    * The SQL statement, it may be <code>null</code> or empty;
    */
   protected String m_sqlStatement;
   
   /**
    * It contains additional log info, such as the binding values from the 
    * derived class <code>PSPreparedStatement<code>. Never <code>null</code>, 
    * may be empty.
    */
   protected StringBuffer m_additionalLogInfo = new StringBuffer();
   
   /**
    * The logger used for this class, may be <code>null</code> if the log4j
    * has not been configured.
    */
   private static final Logger ms_log = LogManager.getLogger(PSSQLStatement.class);

   /**
    * All methods will be delegated to this object. Initialized by ctor,
    * never <code>null</code> after that.
    */
   private Statement m_stmt;

   /**
    * It is used to record the execution time of a SQL statement.
    * Never <code>null</code>, started by {@link #startTimer()}, 
    * stopped by {@link #logElapsedTime()}.
    */
   private PSStopwatch m_timer = new PSStopwatch();
   
   /**
    * Total SQL execution count. It is increased by {@link #startTimer()}
    */
   private static long ms_sqlExeCount = 0;
   
   /**
    * The SQL execution count for the object itself. It is set by
    * {@link #startTimer()}and used by {@link #logElapsedTime()}to match up
    * the logged SQL statement and its elapse time.
    */
   private long m_currSQLExeCount;

   public boolean isClosed() throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public boolean isPoolable() throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public void setPoolable(boolean poolable) throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public boolean isWrapperFor(Class<?> iface) throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public <T> T unwrap(Class<T> iface) throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   @Override
   public void closeOnCompletion() throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   @Override
   public boolean isCloseOnCompletion() throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }
}










