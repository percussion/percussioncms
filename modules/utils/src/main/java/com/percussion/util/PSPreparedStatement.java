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
package com.percussion.util;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.util.Calendar;

/**
 * Just like {@link PSSQLStatement}, This is a proxy (or wrapper) class to a 
 * <code>PreparedStatement</code> object. It delegates all methods to the 
 * <code>PreparedStatement</code> object and logs the SQL statement and elapse 
 * time for each SQL execution (query or update). 
 * <P>
 * This class can only take effect after the log4j is properly configured
 * and the debug mode is enabled for {@link PSSQLStatement}. 
 * <P>
 * Must use {@link #getPreparedStatement(Connection, String)} to create a 
 * <code>PreparedStatement</code> object.
 */
public class PSPreparedStatement
   extends PSSQLStatement
   implements PreparedStatement
{
   /**
    * Creates a <code>PreparedStatement<code> object from the supplied 
    * connection and SQL statement.
    * 
    * @param conn the connection, may not be <code>null</code>.
    * @param sql the SQL statement, may not be <code>null</code> or empty.
    * 
    * @return an created <code>PSPreparedStatement<code> object if the
    *    log4j debug mode is on for this class; otherwise return 
    *    <code>PreparedStatement</code> object, never <code>null</code>.
    * 
    * @throws SQLException if error occurs.
    */
   static public PreparedStatement getPreparedStatement(
      Connection conn,
      String sql)
      throws SQLException
   {
      if (conn == null)
         throw new IllegalArgumentException("conn may not be null");
      if (sql == null || sql.trim().length() == 0)
         throw new IllegalArgumentException("sql may not be null or empty");

      PreparedStatement stmt = conn.prepareStatement(sql);
      
      if (isLogEnabled())
         return new PSPreparedStatement(stmt, sql);
      else
         return stmt;
   }
   
   /**
    * Just like {@link #getPreparedStatement(Connection, String)}, but
    * with additional parameters, result set type and concurrency type.
    * 
    * @param resultSetType a result set type; see ResultSet.TYPE_XXX
    * @param resultSetConcurrency a concurrency type; see ResultSet.CONCUR_XXX
    * 
    * @see Connection.prepareStatement(String,int,int)
    */
   static public PreparedStatement getPreparedStatement(
      Connection conn,
      String sql,
      int resultSetType,
      int resultSetConcurrency)
      throws SQLException
   {
      if (conn == null)
         throw new IllegalArgumentException("conn may not be null");
      if (sql == null || sql.trim().length() == 0)
         throw new IllegalArgumentException("sql may not be null or empty");

      PreparedStatement stmt =
         conn.prepareStatement(sql, resultSetType, resultSetConcurrency);
      
      if (isLogEnabled())
         return new PSPreparedStatement(stmt, sql);
      else
         return stmt;
   }

   // Get the prepared Statement class name, used currently by 
   // PSSqlHelper to resolve the driver class type
   public String getOriginalPreparedStmtClassName()
   {
      return m_stmt.getClass().getName();
   }
   
   /**
    * Gets the original prepared statement object.
    * 
    * @return the original prepared statement, never <code>null</code>.
    */
   public PreparedStatement getOriginalPreparedStatement()
   {
      return m_stmt;
   }
   
   // Implements PreparedStatement.executeQuery()
   public ResultSet executeQuery() throws SQLException
   {
      startTimer();
      ResultSet rs = m_stmt.executeQuery();
      logElapsedTime();
      
      return rs;
   }

   // Implements PreparedStatement.executeUpdate()
   public int executeUpdate() throws SQLException
   {
      startTimer();
      int rs = m_stmt.executeUpdate();
      logElapsedTime();
      
      return rs;
   }

   // Implements PreparedStatement.setURL(int, URL)
   public void setURL(int parameterIndex, URL x)
      throws SQLException
   {
      m_stmt.setURL(parameterIndex, x);
      logBindingValue(parameterIndex, x);
   }

   // Implements PreparedStatement.getParameterMetaData()
   public ParameterMetaData getParameterMetaData()
      throws SQLException
   {
      return m_stmt.getParameterMetaData();
   }
   
   // Implements PreparedStatement.setNull(int, int)
   public void setNull(int parameterIndex, int sqlType) throws SQLException
   {
      m_stmt.setNull(parameterIndex, sqlType);

      logBindingValue(parameterIndex, "null");
   }

   // Implements PreparedStatement.setBoolean(int, boolean)
   public void setBoolean(int parameterIndex, boolean x) throws SQLException
   {
      m_stmt.setBoolean(parameterIndex, x);
      
      logBindingValue(parameterIndex, "" + x);
   }

   // Implements PreparedStatement.setByte(int, byte)
   public void setByte(int parameterIndex, byte x) throws SQLException
   {
      m_stmt.setByte(parameterIndex, x);

      logBindingValue(parameterIndex, "" + x);
   }

   // Implements PreparedStatement.setShort(int, short)
   public void setShort(int parameterIndex, short x) throws SQLException
   {
      m_stmt.setShort(parameterIndex, x);
      
      logBindingValue(parameterIndex, "" + x);
   }

   // Implements PreparedStatement.setShort(int, int)  
   public void setInt(int parameterIndex, int x) throws SQLException
   {
      m_stmt.setInt(parameterIndex, x);
      
      logBindingValue(parameterIndex, "" + x);
   }

   // Implements PreparedStatement.setLong(int, long)
   public void setLong(int parameterIndex, long x) throws SQLException
   {
      m_stmt.setLong(parameterIndex, x);       
      
      logBindingValue(parameterIndex, "" + x);
   }

   // Implements PreparedStatement.setFloat(int, float)
   public void setFloat(int parameterIndex, float x) throws SQLException
   {
      m_stmt.setFloat(parameterIndex, x);       
      
      logBindingValue(parameterIndex, "" + x);
   }

   // Implements PreparedStatement.setFloat(int, double)
   public void setDouble(int parameterIndex, double x) throws SQLException
   {
      m_stmt.setDouble(parameterIndex, x);
      
      logBindingValue(parameterIndex, "" + x);
   }

   // Implements PreparedStatement.setFloat(int, double)
   public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException
   {
      m_stmt.setBigDecimal(parameterIndex, x);
      
      logBindingValue(parameterIndex, x);
   } 

   // Implements PreparedStatement.setString(int, String)
   public void setString(int parameterIndex, String x) throws SQLException
   {
      m_stmt.setString(parameterIndex, x);
      
      logBindingValue(parameterIndex, x);
   }

   // Implements PreparedStatement.setBytes(int, byte[])
   public void setBytes(int parameterIndex, byte x[]) throws SQLException
   {
      m_stmt.setBytes(parameterIndex, x);
      
      logBindingValue(parameterIndex, x);
   }

   // Implements PreparedStatement.setDate(int, Date)
   public void setDate(int parameterIndex, java.sql.Date x) throws SQLException
   {
      m_stmt.setDate(parameterIndex, x);
      
      logBindingValue(parameterIndex, x);
   }

   // Implements PreparedStatement.setTime(int, Time)
   public void setTime(int parameterIndex, java.sql.Time x) throws SQLException
   {
      m_stmt.setTime(parameterIndex, x);
      
      logBindingValue(parameterIndex, x);
   }

   // Implements PreparedStatement.setTimestamp(int, Timestamp)
   public void setTimestamp(int parameterIndex, java.sql.Timestamp x)
      throws SQLException
   {
      m_stmt.setTimestamp(parameterIndex, x);
      
      logBindingValue(parameterIndex, x);
   }

   // Implements PreparedStatement.setAsciiStream(int, InputStream, int)
   public void setAsciiStream(
      int parameterIndex,
      java.io.InputStream x,
      int length)
      throws SQLException
   {
      m_stmt.setAsciiStream(parameterIndex, x, length);
      
      logBindingValue(parameterIndex, x);
   }

   // Implements PreparedStatement.setUnicodeStream(int, InputStream, int)
   public void setUnicodeStream(
      int parameterIndex,
      java.io.InputStream x,
      int length)
      throws SQLException
   {
      m_stmt.setUnicodeStream(parameterIndex, x, length);
      
      logBindingValue(parameterIndex, x);
   }

   // Implements PreparedStatement.setBinaryStream(int, InputStream, int)
   public void setBinaryStream(
      int parameterIndex,
      java.io.InputStream x,
      int length)
      throws SQLException
   {
      m_stmt.setBinaryStream(parameterIndex, x, length);
      
      logBindingValue(parameterIndex, x);
   }

   // Implements PreparedStatement.clearParameters()
   public void clearParameters() throws SQLException
   {
      m_stmt.clearParameters();
   }

    //----------------------------------------------------------------------
    // Advanced features:

   // Implements PreparedStatement.setObject(int, Object, int, int)
   public void setObject(
      int parameterIndex,
      Object x,
      int targetSqlType,
      int scale)
      throws SQLException
   {
      m_stmt.setObject(parameterIndex, x, targetSqlType, scale);
      
      logBindingValue(parameterIndex, x);
   }

   // Implements PreparedStatement.setObject(int, Object, int)
   public void setObject(int parameterIndex, Object x, int targetSqlType)
      throws SQLException
   {
      m_stmt.setObject(parameterIndex, x, targetSqlType);
      
      logBindingValue(parameterIndex, x);
   }

   // Implements PreparedStatement.setObject(int, Object)
   public void setObject(int parameterIndex, Object x) throws SQLException
   {
      m_stmt.setObject(parameterIndex, x);
      
      logBindingValue(parameterIndex, x);
   }

   // Implements PreparedStatement.execute()
   public boolean execute() throws SQLException
   {
      startTimer();
      boolean rs = m_stmt.execute();
      logElapsedTime();
            
      return rs;
   }

    //--------------------------JDBC 2.0-----------------------------

   // Implements PreparedStatement.setBlob(int, reader, int)
   public void addBatch() throws SQLException
   {
      m_stmt.addBatch();
   }

   // Implements PreparedStatement.setCharacterStream(int, reader, int)
   public void setCharacterStream(
      int parameterIndex,
      java.io.Reader reader,
      int length)
      throws SQLException
   {
      m_stmt.setCharacterStream(parameterIndex, reader, length);
      
      logBindingValue(parameterIndex, reader.toString());
   }

   // Implements PreparedStatement.setRef(int, Ref)
   public void setRef (int i, Ref x) throws SQLException
   {
      m_stmt.setRef(i, x);
      
      logBindingValue(i, x);
   }

   // Implements PreparedStatement.setBlob(int, Blob)
   public void setBlob (int i, Blob x) throws SQLException
   {
      m_stmt.setBlob(i, x);
      
      logBindingValue(i, x);
   }

   // Implements PreparedStatement.setClob(int, Clob)
   public void setClob (int i, Clob x) throws SQLException
   {
      m_stmt.setClob(i, x);
      
      logBindingValue(i, x);
   }

   // Implements PreparedStatement.setArray(int, Array)
   public void setArray (int i, Array x) throws SQLException
   {
      m_stmt.setArray(i, x);
      
      logBindingValue(i, x);
   }

   // Implements PreparedStatement.getMetaData()
   public ResultSetMetaData getMetaData() throws SQLException
   {
      return m_stmt.getMetaData();
   }

   // Implements PreparedStatement.setDate(int, Date, Calendar)
   public void setDate(int parameterIndex, java.sql.Date x, Calendar cal)
       throws SQLException
   {
      m_stmt.setDate(parameterIndex, x, cal);
      
      logBindingValue(parameterIndex, x,  cal);
   }

   // Implements PreparedStatement.setTime(int, Time, Calendar)
   public void setTime(int parameterIndex, java.sql.Time x, Calendar cal)
      throws SQLException
   {
      m_stmt.setTime(parameterIndex, x, cal);          
      
      logBindingValue(parameterIndex, x,  cal);
   }

   // Implements PreparedStatement.setTimestamp(int, Timestamp, Calendar)
   public void setTimestamp(
      int parameterIndex,
      java.sql.Timestamp x,
      Calendar cal)
      throws SQLException
   {
      m_stmt.setTimestamp(parameterIndex, x, cal);
      
      logBindingValue(parameterIndex, x,  cal);
   }

   // Implements PreparedStatement.setNull(int, int, String)
   public void setNull (int paramIndex, int sqlType, String typeName) 
    throws SQLException
   {
      m_stmt.setNull(paramIndex, sqlType, typeName);
      
      logBindingValue(paramIndex, "null");
   }  
   
   /**
    * Creates an object from the given parameters.
    *
    * @param stmt the preparedstatement object, assume not <code>null</code>.
    * @param sql the SQL statement, assume not <code>null</code>.
    */
   private PSPreparedStatement(PreparedStatement stmt, String sql)
   {
      super(stmt);
      m_stmt = stmt;
      m_sqlStatement = sql;
   }
   
   /**
    * Logs a parameter value.
    *
    * @param parameterIndex the index of the parameter.
    *
    * @param x the value of the parameter, assume not <code>null</code>.
    */
   private void logBindingValue(int parameterIndex, Object x)
   {
      String value = x == null ? "null" : x.toString();
      m_additionalLogInfo.append(
         "Binding value [" + parameterIndex + "]: " + value + "\n");
   }

   /**
    * Just like {@link #logBindingValue(int, Object)} with additional value
    *
    * @param parameterIndex the index of the parameter.
    *
    * @param x the 1st value of the parameter, assume not <code>null</code>.
    * 
    * @param y the 2nd value of the parameter, assume not <code>null</code>.
    */
   private void logBindingValue(int parameterIndex, Object x, Object y)
   {
      String valueX = x == null ? "null" : x.toString();
      String valueY = y == null ? "null" : y.toString();
      String value = valueX + ", " + valueY;
      m_additionalLogInfo.append(
         "Binding value [" + parameterIndex + "]: " + value + "\n");
   }

   /**
    * All methods will be delegated to this object. Initialized by ctor,
    * never <code>null</code> after that.
    */
   private PreparedStatement m_stmt;

   public void setAsciiStream(int parameterIndex, InputStream x)
         throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public void setAsciiStream(int parameterIndex, InputStream x, long length)
         throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public void setBinaryStream(int parameterIndex, InputStream x)
         throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public void setBinaryStream(int parameterIndex, InputStream x, long length)
         throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public void setBlob(int parameterIndex, InputStream inputStream)
         throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public void setBlob(int parameterIndex, InputStream inputStream, long length)
         throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public void setCharacterStream(int parameterIndex, Reader reader)
         throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public void setCharacterStream(int parameterIndex, Reader reader, long length)
         throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public void setClob(int parameterIndex, Reader reader) throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public void setClob(int parameterIndex, Reader reader, long length)
         throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public void setNCharacterStream(int parameterIndex, Reader value)
         throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public void setNCharacterStream(int parameterIndex, Reader value, long length)
         throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public void setNClob(int parameterIndex, NClob value) throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public void setNClob(int parameterIndex, Reader reader) throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public void setNClob(int parameterIndex, Reader reader, long length)
         throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public void setNString(int parameterIndex, String value) throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public void setRowId(int parameterIndex, RowId x) throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public void setSQLXML(int parameterIndex, SQLXML xmlObject)
         throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

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

}











