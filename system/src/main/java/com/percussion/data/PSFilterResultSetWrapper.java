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

package com.percussion.data;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Map;

/**
 * The PSFilterResultSetWrapper class extends the JDBC result set model
 * providing a mechanism for accepting specific rows based on filter criterion.
 */

public class PSFilterResultSetWrapper implements ResultSet
{
   /**
    * The execution data to operate on. Created when constructed,
    * never <code>null</code> after that.
    */
   private PSExecutionData m_data = null;

   /**
    * The result set which we wrap. All methods are forwarded
    * to this result set.
    * (Created when constructed, never <code>null</code>).
    */
   private ResultSet m_rs = null;

   /**
    * The filter to use during the next call. May be <code>null</code> if no
    * filter has been defined
    */
   private IPSResultSetDataFilter m_filter = null;

   /**
    * The array of columns to get the data from the current result set row. Set
    * in the ctor, never modified after that, must not be <code>null</code>.
    */
   private Object [] m_cols = null;

   /**
    * Construct a filter result set wrapper to encapsulate the result set
    * and filter based on the criterion given in the specified filter
    *
    * @param data the execution data to operate on, not <code>null</code>.
    *
    * @param rs the result set to wrap, all calls are forwarded to this
    *    object except for the next method in which special filter handling
    *    occurs
    *
    * @param filter the defined filter for accepting the specific row when
    *    retrieving the next result row from the result set, may be <code>
    *    null</code> to represent no filter
    */
   public PSFilterResultSetWrapper(PSExecutionData data, ResultSet rs,
      IPSResultSetDataFilter filter)
   {
      if (data == null)
         throw new IllegalArgumentException("Need a valid execution data!");

      if (rs == null)
         throw new IllegalArgumentException("There must be a result set!");

      if (rs.getClass().isAssignableFrom(PSFilterResultSetWrapper.class))
         throw new IllegalArgumentException("Result set already wrapped!");

      m_data = data;
      m_rs = rs;
      m_filter = filter;
      if (filter != null)
      {
         m_cols = m_filter.getColumns().toArray();
      }
   }

   /**
    * Releases this <code>ResultSet</code> object's database and
    * JDBC resources immediately instead of waiting for
    * this to happen when it is automatically closed.
    *
    * @exception SQLException if a database access error occurs
    *    or an error occurs closing down the connection or statement
    */
   public void close()
      throws SQLException
   {
      m_rs.close();
   }

   /**
    * Moves the cursor down one row from its current position.
    * A ResultSet cursor is initially positioned before the first row; the
    * first call to next makes the first row the current row; the
    * second call makes the second row the current row, and so on.
    *
    * <P>If an input stream is open for the current row, a call
    * to the method <code>next</code> will
    * implicitly close it. The ResultSet's warning chain is cleared
    * when a new row is read.
    *
    * @return true if the new current row is valid; false if there
    * are no more rows
    * @exception SQLException if a database access error occurs
    */
   public boolean next()
      throws SQLException
   {
      boolean hasNext = m_rs.next();

      // special case for handling a filter for next
      if (m_filter != null)
      {
         // only execute if we are not currently at the
         // end of the result set
         while (hasNext && !m_filter.accept(m_data, getFilterVals()))
         {
            // since we are not accepting this row
            // move the position forward and keep
            // checking, if hasNext == false, that
            // means we are at the end
            hasNext = m_rs.next();
         }
      }
      return hasNext;
   }

   /**
    * Get the values from the current result row based on the columns specified
    * in the filter.
    *
    * @return a array of string values from the current result row, will not be
    *    <code>null</code>, may be empty
    *
    * @throws SQLException if the column can not be found
    */
   private Object [] getFilterVals()
      throws java.sql.SQLException
   {
      Object [] ret = new Object[m_cols.length];

      for (int i = 0; i < m_cols.length; i++)
      {
         ret[i] = m_rs.getString((String)m_cols[i]);
      }
      return ret;
   }

   /**
    * Reports whether the last column read had a value of SQL NULL.
    * Note that you must first call getXXX on a column to try to read
    * its value and then call wasNull() to see if the value read was
    * SQL NULL.
    *
    * @return true if last column read was SQL NULL and false otherwise
    * @exception SQLException if a database access error occurs
    */
   public boolean wasNull()
      throws SQLException
   {
      return m_rs.wasNull();
   }

   //======================================================================
   // Methods for accessing results by column index
   //======================================================================

   /**
    * Gets the value of a column in the current row as a Java String.
    *
    * @param columnIndex the first column is 1, the second is 2, ...
    * @return the column value; if the value is SQL NULL, the result is null
    * @exception SQLException if a database access error occurs
    */
   public String getString(int columnIndex) throws SQLException
   {
      return m_rs.getString(columnIndex);
   }

   /**
    * Gets the value of a column in the current row as a Java boolean.
    *
    * @param columnIndex the first column is 1, the second is 2, ...
    * @return the column value; if the value is SQL NULL, the result is false
    * @exception SQLException if a database access error occurs
    */
   public boolean getBoolean(int columnIndex) throws SQLException
   {
      return m_rs.getBoolean(columnIndex);
   }

   /**
    * Gets the value of a column in the current row as a Java byte.
    *
    * @param columnIndex the first column is 1, the second is 2, ...
    * @return the column value; if the value is SQL NULL, the result is 0
    * @exception SQLException if a database access error occurs
    */
   public byte getByte(int columnIndex) throws SQLException
   {
      return m_rs.getByte(columnIndex);
   }

   /**
    * Gets the value of a column in the current row as a Java short.
    *
    * @param columnIndex the first column is 1, the second is 2, ...
    * @return the column value; if the value is SQL NULL, the result is 0
    * @exception SQLException if a database access error occurs
    */
   public short getShort(int columnIndex) throws SQLException
   {
      return m_rs.getShort(columnIndex);
   }

   /**
    * Gets the value of a column in the current row as a Java int.
    *
    * @param columnIndex the first column is 1, the second is 2, ...
    * @return the column value; if the value is SQL NULL, the result is 0
    * @exception SQLException if a database access error occurs
    */
   public int getInt(int columnIndex) throws SQLException
   {
      return m_rs.getInt(columnIndex);
   }

   /**
    * Gets the value of a column in the current row as a Java long.
    *
    * @param columnIndex the first column is 1, the second is 2, ...
    * @return the column value; if the value is SQL NULL, the result is 0
    * @exception SQLException if a database access error occurs
    */
   public long getLong(int columnIndex) throws SQLException
   {
      return m_rs.getLong(columnIndex);
   }

   /**
    * Gets the value of a column in the current row as a Java float.
    *
    * @param columnIndex the first column is 1, the second is 2, ...
    * @return the column value; if the value is SQL NULL, the result is 0
    * @exception SQLException if a database access error occurs
    */
   public float getFloat(int columnIndex) throws SQLException
   {
      return m_rs.getFloat(columnIndex);
   }

   /**
    * Gets the value of a column in the current row as a Java double.
    *
    * @param columnIndex the first column is 1, the second is 2, ...
    * @return the column value; if the value is SQL NULL, the result is 0
    * @exception SQLException if a database access error occurs
    */
   public double getDouble(int columnIndex) throws SQLException
   {
      return m_rs.getDouble(columnIndex);
   }

   /**
    * Gets the value of a column in the current row as a java.math.BigDecimal object.
    *
    * @param columnIndex the first column is 1, the second is 2, ...
    * @param scale the number of digits to the right of the decimal
    * @return the column value; if the value is SQL NULL, the result is null
    * @exception SQLException if a database access error occurs
    * @deprecated
    */
   public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException
   {
      return m_rs.getBigDecimal(columnIndex);
   }

   /**
    * Gets the value of a column in the current row as a Java byte array.
    * The bytes represent the raw values returned by the driver.
    *
    * @param columnIndex the first column is 1, the second is 2, ...
    * @return the column value; if the value is SQL NULL, the result is null
    * @exception SQLException if a database access error occurs
    */
   public byte[] getBytes(int columnIndex) throws SQLException
   {
      return m_rs.getBytes(columnIndex);
   }

   /**
    * Gets the value of a column in the current row as a java.sql.Date object.
    *
    * @param columnIndex the first column is 1, the second is 2, ...
    * @return the column value; if the value is SQL NULL, the result is null
    * @exception SQLException if a database access error occurs
    */
   public java.sql.Date getDate(int columnIndex) throws SQLException
   {
      return m_rs.getDate(columnIndex);
   }

   /**
    * Gets the value of a column in the current row as a java.sql.Time object.
    *
    * @param columnIndex the first column is 1, the second is 2, ...
    * @return the column value; if the value is SQL NULL, the result is null
    * @exception SQLException if a database access error occurs
    */
   public java.sql.Time getTime(int columnIndex) throws SQLException
   {
      return m_rs.getTime(columnIndex);
   }

   /**
    * Gets the value of a column in the current row as a java.sql.Timestamp object.
    *
    * @param columnIndex the first column is 1, the second is 2, ...
    * @return the column value; if the value is SQL NULL, the result is null
    * @exception SQLException if a database access error occurs
    */
   public java.sql.Timestamp getTimestamp(int columnIndex) throws SQLException
   {
      return m_rs.getTimestamp(columnIndex);
   }

   /**
    * Gets the value of a column in the current row as a stream of
    * ASCII characters. The value can then be read in chunks from the
    * stream. This method is particularly
    * suitable for retrieving large LONGVARCHAR values.  The JDBC driver will
    * do any necessary conversion from the database format into ASCII.
    *
    * <P><B>Note:</B> All the data in the returned stream must be
    * read prior to getting the value of any other column. The next
    * call to a get method implicitly closes the stream.  Also, a
    * stream may return 0 when the method <code>available</code>
    * is called whether there is data
    * available or not.
    *
    * @param columnIndex the first column is 1, the second is 2, ...
    * @return a Java input stream that delivers the database column value
    * as a stream of one byte ASCII characters.  If the value is SQL NULL
    * then the result is null.
    * @exception SQLException if a database access error occurs
    */
   public java.io.InputStream getAsciiStream(int columnIndex) throws SQLException
   {
      return m_rs.getAsciiStream(columnIndex);
   }

   /**
    * Gets the value of a column in the current row as a stream of
    * Unicode characters. The value can then be read in chunks from the
    * stream. This method is particularly
    * suitable for retrieving large LONGVARCHAR values.  The JDBC driver will
    * do any necessary conversion from the database format into Unicode.
    * The byte format of the Unicode stream must Java UTF-8,
    * as specified in the Java Virtual Machine Specification.
    *
    * <P><B>Note:</B> All the data in the returned stream must be
    * read prior to getting the value of any other column. The next
    * call to a get method implicitly closes the stream. Also, a
    * stream may return 0 when the method <code>available</code>
    * is called whether there is data
    * available or not.
    *
    * @param columnIndex the first column is 1, the second is 2, ...
    * @return a Java input stream that delivers the database column value
    * as a stream of two-byte Unicode characters.  If the value is SQL NULL
    * then the result is null.
    * @exception SQLException if a database access error occurs
    * @deprecated
    */
   public java.io.InputStream getUnicodeStream(int columnIndex) throws SQLException
   {
      return m_rs.getUnicodeStream(columnIndex);
   }


   /**
    * Gets the value of a column in the current row as a stream of
    * uninterpreted bytes. The value can then be read in chunks from the
    * stream. This method is particularly
    * suitable for retrieving large LONGVARBINARY values.
    *
    * <P><B>Note:</B> All the data in the returned stream must be
    * read prior to getting the value of any other column. The next
    * call to a get method implicitly closes the stream. Also, a
    * stream may return 0 when the method <code>available</code>
    * is called whether there is data
    * available or not.
    *
    * @param columnIndex the first column is 1, the second is 2, ...
    * @return a Java input stream that delivers the database column value
    * as a stream of uninterpreted bytes.  If the value is SQL NULL
    * then the result is null.
    * @exception SQLException if a database access error occurs
    */
   public java.io.InputStream getBinaryStream(int columnIndex)
       throws SQLException
   {
      return m_rs.getBinaryStream(columnIndex);
   }


   //======================================================================
   // Methods for accessing results by column name
   //======================================================================

   /**
    * Gets the value of a column in the current row as a Java String.
    *
    * @param columnName the SQL name of the column
    * @return the column value; if the value is SQL NULL, the result is null
    * @exception SQLException if a database access error occurs
    */
   public String getString(String columnName) throws SQLException
   {
      return m_rs.getString(columnName);
   }

   /**
    * Gets the value of a column in the current row as a Java boolean.
    *
    * @param columnName the SQL name of the column
    * @return the column value; if the value is SQL NULL, the result is false
    * @exception SQLException if a database access error occurs
    */
   public boolean getBoolean(String columnName) throws SQLException
   {
      return m_rs.getBoolean(columnName);
   }

   /**
    * Gets the value of a column in the current row as a Java byte.
    *
    * @param columnName the SQL name of the column
    * @return the column value; if the value is SQL NULL, the result is 0
    * @exception SQLException if a database access error occurs
    */
   public byte getByte(String columnName) throws SQLException
   {
      return m_rs.getByte(columnName);
   }

   /**
    * Gets the value of a column in the current row as a Java short.
    *
    * @param columnName the SQL name of the column
    * @return the column value; if the value is SQL NULL, the result is 0
    * @exception SQLException if a database access error occurs
    */
   public short getShort(String columnName) throws SQLException
   {
      return m_rs.getShort(columnName);
   }

   /**
    * Gets the value of a column in the current row as a Java int.
    *
    * @param columnName the SQL name of the column
    * @return the column value; if the value is SQL NULL, the result is 0
    * @exception SQLException if a database access error occurs
    */
   public int getInt(String columnName) throws SQLException
   {
      return m_rs.getInt(columnName);
   }

   /**
    * Gets the value of a column in the current row as a Java long.
    *
    * @param columnName the SQL name of the column
    * @return the column value; if the value is SQL NULL, the result is 0
    * @exception SQLException if a database access error occurs
    */
   public long getLong(String columnName) throws SQLException
   {
      return m_rs.getLong(columnName);
   }

   /**
    * Gets the value of a column in the current row as a Java float.
    *
    * @param columnName the SQL name of the column
    * @return the column value; if the value is SQL NULL, the result is 0
    * @exception SQLException if a database access error occurs
    */
   public float getFloat(String columnName) throws SQLException
   {
      return m_rs.getFloat(columnName);
   }

   /**
    * Gets the value of a column in the current row as a Java double.
    *
    * @param columnName the SQL name of the column
    * @return the column value; if the value is SQL NULL, the result is 0
    * @exception SQLException if a database access error occurs
    */
   public double getDouble(String columnName) throws SQLException
   {
      return m_rs.getDouble(columnName);
   }

   /**
    * Gets the value of a column in the current row as a java.math.BigDecimal
    * object.
    *
    * @param columnName the SQL name of the column
    * @param scale the number of digits to the right of the decimal
    * @return the column value; if the value is SQL NULL, the result is null
    * @exception SQLException if a database access error occurs
    * @deprecated
    */
   public BigDecimal getBigDecimal(String columnName, int scale) throws SQLException
   {
      return m_rs.getBigDecimal(columnName);
   }

   /**
    * Gets the value of a column in the current row as a Java byte array.
    * The bytes represent the raw values returned by the driver.
    *
    * @param columnName the SQL name of the column
    * @return the column value; if the value is SQL NULL, the result is null
    * @exception SQLException if a database access error occurs
    */
   public byte[] getBytes(String columnName) throws SQLException
   {
      return m_rs.getBytes(columnName);
   }

   /**
    * Gets the value of a column in the current row as a java.sql.Date object.
    *
    * @param columnName the SQL name of the column
    * @return the column value; if the value is SQL NULL, the result is null
    * @exception SQLException if a database access error occurs
    */
   public java.sql.Date getDate(String columnName) throws SQLException
   {
      return m_rs.getDate(columnName);
   }

   /**
    * Gets the value of a column in the current row as a java.sql.Time object.
    *
    * @param columnName the SQL name of the column
    * @return the column value; if the value is SQL NULL, the result is null
    * @exception SQLException if a database access error occurs
    */
   public java.sql.Time getTime(String columnName) throws SQLException
   {
      return m_rs.getTime(columnName);
   }

   /**
    * Gets the value of a column in the current row as a java.sql.Timestamp object.
    *
    * @param columnName the SQL name of the column
    * @return the column value; if the value is SQL NULL, the result is null
    * @exception SQLException if a database access error occurs
    */
   public java.sql.Timestamp getTimestamp(String columnName) throws SQLException
   {
      return m_rs.getTimestamp(columnName);
   }

   /**
    * Gets the value of a column in the current row as a stream of
    * ASCII characters. The value can then be read in chunks from the
    * stream. This method is particularly
    * suitable for retrieving large LONGVARCHAR values.  The JDBC driver will
    * do any necessary conversion from the database format into ASCII.
    *
    * <P><B>Note:</B> All the data in the returned stream must be
    * read prior to getting the value of any other column. The next
    * call to a get method implicitly closes the stream. Also, a
    * stream may return 0 when the method <code>available</code>
    * is called whether there is data
    * available or not.
    *
    * @param columnName the SQL name of the column
    * @return a Java input stream that delivers the database column value
    * as a stream of one byte ASCII characters.  If the value is SQL NULL
    * then the result is null.
    * @exception SQLException if a database access error occurs
    */
   public java.io.InputStream getAsciiStream(String columnName) throws SQLException
   {
      return m_rs.getAsciiStream(columnName);
   }

   /**
    * Gets the value of a column in the current row as a stream of
    * Unicode characters. The value can then be read in chunks from the
    * stream. This method is particularly
    * suitable for retrieving large LONGVARCHAR values.  The JDBC driver will
    * do any necessary conversion from the database format into Unicode.
    * The byte format of the Unicode stream must be Java UTF-8,
    * as defined in the Java Virtual Machine Specification.
    *
    * <P><B>Note:</B> All the data in the returned stream must be
    * read prior to getting the value of any other column. The next
    * call to a get method implicitly closes the stream. Also, a
    * stream may return 0 when the method <code>available</code>
    * is called whether there is data
    * available or not.
    *
    * @param columnName the SQL name of the column
    * @return a Java input stream that delivers the database column value
    * as a stream of two-byte Unicode characters.  If the value is SQL NULL
    * then the result is null.
    * @exception SQLException if a database access error occurs
    * @deprecated
    */
   public java.io.InputStream getUnicodeStream(String columnName) throws SQLException
   {
      return m_rs.getUnicodeStream(columnName);
   }

   /**
    * Gets the value of a column in the current row as a stream of
    * uninterpreted bytes. The value can then be read in chunks from the
    * stream. This method is particularly
    * suitable for retrieving large LONGVARBINARY values.  The JDBC driver will
    * do any necessary conversion from the database format into uninterpreted
    * bytes.
    *
    * <P><B>Note:</B> All the data in the returned stream must be
    * read prior to getting the value of any other column. The next
    * call to a get method implicitly closes the stream. Also, a
    * stream may return 0 when the method <code>available</code>
    * is called whether there is data
    * available or not.
    *
    * @param columnName the SQL name of the column
    * @return a Java input stream that delivers the database column value
    * as a stream of uninterpreted bytes.  If the value is SQL NULL
    * then the result is null.
    * @exception SQLException if a database access error occurs
    */
   public java.io.InputStream getBinaryStream(String columnName)
       throws SQLException
   {
      return m_rs.getBinaryStream(columnName);
   }


   //=====================================================================
   // Advanced features:
   //=====================================================================

   /**
    * <p>The first warning reported by calls on this ResultSet is
    * returned. Subsequent ResultSet warnings will be chained to this
    * SQLWarning.
    *
    * <P>The warning chain is automatically cleared each time a new
    * row is read.
    *
    * <P><B>Note:</B> This warning chain only covers warnings caused
    * by ResultSet methods.  Any warning caused by statement methods
    * (such as reading OUT parameters) will be chained on the
    * Statement object.
    *
    * @return the first SQLWarning or null
    * @exception SQLException if a database access error occurs
    */
   public SQLWarning getWarnings() throws SQLException
   {
      return m_rs.getWarnings();
   }

   /**
    * After this call getWarnings returns null until a new warning is
    * reported for this ResultSet.
    *
    * @exception SQLException if a database access error occurs
    */
   public void clearWarnings() throws SQLException
   {
      m_rs.clearWarnings();
   }

   /**
    * Gets the name of the SQL cursor used by this ResultSet.
    *
    * <P>In SQL, a result table is retrieved through a cursor that is
    * named. The current row of a result can be updated or deleted
    * using a positioned update/delete statement that references the
    * cursor name. To insure that the cursor has the proper isolation
    * level to support update, the cursor's select statement should be
    * of the form 'select for update'. If the 'for update' clause is
    * omitted the positioned updates may fail.
    *
    * <P>JDBC supports this SQL feature by providing the name of the
    * SQL cursor used by a ResultSet. The current row of a ResultSet
    * is also the current row of this SQL cursor.
    *
    * <P><B>Note:</B> If positioned update is not supported a
    * SQLException is thrown
    *
    * @return the ResultSet's SQL cursor name
    * @exception SQLException if a database access error occurs
    */
   public String getCursorName() throws SQLException
   {
      return m_rs.getCursorName();
   }

   /**
    * Retrieves the  number, types and properties of a ResultSet's columns.
    *
    * @return the description of a ResultSet's columns
    * @exception SQLException if a database access error occurs
    */
   public ResultSetMetaData getMetaData() throws SQLException
   {
      return m_rs.getMetaData();
   }

   /**
    * <p>Gets the value of a column in the current row as a Java object.
    *
    * <p>This method will return the value of the given column as a
    * Java object.  The type of the Java object will be the default
    * Java object type corresponding to the column's SQL type,
    * following the mapping for built-in types specified in the JDBC
    * spec.
    *
    * <p>This method may also be used to read datatabase-specific
    * abstract data types.
    *
    * JDBC 2.0
    *
    *
    * In the JDBC 2.0 API, the behavior of method
    * <code>getObject</code> is extended to materialize
    * data of SQL user-defined types.  When the a column contains
    * a structured or distinct value, the behavior of this method is as
    * if it were a call to: getObject(columnIndex,
    * this.getStatement().getConnection().getTypeMap()).
    *
    * @param columnIndex the first column is 1, the second is 2, ...
    * @return a java.lang.Object holding the column value
    * @exception SQLException if a database access error occurs
    */
   public Object getObject(int columnIndex) throws SQLException
   {
      return m_rs.getObject(columnIndex);
   }

   /**
    * <p>Gets the value of a column in the current row as a Java object.
    *
    * <p>This method will return the value of the given column as a
    * Java object.  The type of the Java object will be the default
    * Java object type corresponding to the column's SQL type,
    * following the mapping for built-in types specified in the JDBC
    * spec.
    *
    * <p>This method may also be used to read datatabase-specific
    * abstract data types.
    *
    * JDBC 2.0
    *
    * In the JDBC 2.0 API, the behavior of method
    * <code>getObject</code> is extended to materialize
    * data of SQL user-defined types.  When the a column contains
    * a structured or distinct value, the behavior of this method is as
    * if it were a call to: getObject(columnIndex,
    * this.getStatement().getConnection().getTypeMap()).
    *
    * @param columnName the SQL name of the column
    * @return a java.lang.Object holding the column value.
    * @exception SQLException if a database access error occurs
    */
   public Object getObject(String columnName) throws SQLException
   {
      return m_rs.getObject(columnName);
   }

   /**
    * Maps the given Resultset column name to its ResultSet column index.
    *
    * @param columnName the name of the column
    * @return the column index
    * @exception SQLException if a database access error occurs
    */
   public int findColumn(String columnName) throws SQLException
   {
      return m_rs.findColumn(columnName);
   }


   //--------------------------JDBC 2.0-----------------------------------

   //---------------------------------------------------------------------
   // Getter's and Setter's
   //---------------------------------------------------------------------

   /**
    * JDBC 2.0
    *
    * <p>Gets the value of a column in the current row as a java.io.Reader.
    * @param columnIndex the first column is 1, the second is 2, ...
    */
   public java.io.Reader getCharacterStream(int columnIndex) throws SQLException
   {
      return m_rs.getCharacterStream(columnIndex);
   }

   /**
    * JDBC 2.0
    *
    * <p>Gets the value of a column in the current row as a java.io.Reader.
    * @param columnName the name of the column
    * @return the value in the specified column as a <code>java.io.Reader</code>
    */
   public java.io.Reader getCharacterStream(String columnName) throws SQLException
   {
      return m_rs.getCharacterStream(columnName);
   }

   /**
    * JDBC 2.0
    *
    * Gets the value of a column in the current row as a java.math.BigDecimal
    * object with full precision.
    *
    * @param columnIndex the first column is 1, the second is 2, ...
    * @return the column value (full precision); if the value is SQL NULL,
    * the result is null
    * @exception SQLException if a database access error occurs
    */
   public BigDecimal getBigDecimal(int columnIndex) throws SQLException
   {
      return m_rs.getBigDecimal(columnIndex);
   }

   /**
    * JDBC 2.0
    *
    * Gets the value of a column in the current row as a java.math.BigDecimal
    * object with full precision.
    * @param columnName the column name
    * @return the column value (full precision); if the value is SQL NULL,
    * the result is null
    * @exception SQLException if a database access error occurs
    *
    */
   public BigDecimal getBigDecimal(String columnName) throws SQLException
   {
      return m_rs.getBigDecimal(columnName);
   }

   //---------------------------------------------------------------------
   // Traversal/Positioning
   //---------------------------------------------------------------------

   /**
    * JDBC 2.0
    *
    * <p>Indicates whether the cursor is before the first row in the result
    * set.
    *
    * @return true if the cursor is before the first row, false otherwise. Returns
    * false when the result set contains no rows.
    * @exception SQLException if a database access error occurs
    */
   public boolean isBeforeFirst() throws SQLException
   {
      return m_rs.isBeforeFirst();
   }

   /**
    * JDBC 2.0
    *
    * <p>Indicates whether the cursor is after the last row in the result
    * set.
    *
    * @return true if the cursor is  after the last row, false otherwise.  Returns
    * false when the result set contains no rows.
    * @exception SQLException if a database access error occurs
    */
   public boolean isAfterLast() throws SQLException
   {
      return m_rs.isAfterLast();
   }

   /**
    * JDBC 2.0
    *
    * <p>Indicates whether the cursor is on the first row of the result set.
    *
    * @return true if the cursor is on the first row, false otherwise.
    * @exception SQLException if a database access error occurs
    */
   public boolean isFirst() throws SQLException
   {
      return m_rs.isFirst();
   }

   /**
    * JDBC 2.0
    *
    * <p>Indicates whether the cursor is on the last row of the result set.
    * Note: Calling the method <code>isLast</code> may be expensive
    * because the JDBC driver
    * might need to fetch ahead one row in order to determine
    * whether the current row is the last row in the result set.
    *
    * @return true if the cursor is on the last row, false otherwise.
    * @exception SQLException if a database access error occurs
    */
   public boolean isLast() throws SQLException
   {
      return m_rs.isLast();
   }

   /**
    * JDBC 2.0
    *
    * <p>Moves the cursor to the front of the result set, just before the
    * first row. Has no effect if the result set contains no rows.
    *
    * @exception SQLException if a database access error occurs or the
    * result set type is TYPE_FORWARD_ONLY
    */
   public void beforeFirst() throws SQLException
   {
      m_rs.beforeFirst();
   }

   /**
    * JDBC 2.0
    *
    * <p>Moves the cursor to the end of the result set, just after the last
    * row.  Has no effect if the result set contains no rows.
    *
    * @exception SQLException if a database access error occurs or the
    * result set type is TYPE_FORWARD_ONLY
    */
   public void afterLast() throws SQLException
   {
      m_rs.afterLast();
   }

   /**
    * JDBC 2.0
    *
    * <p>Moves the cursor to the first row in the result set.
    *
    * @return true if the cursor is on a valid row; false if
    *         there are no rows in the result set
    * @exception SQLException if a database access error occurs or the
    * result set type is TYPE_FORWARD_ONLY
    */
   public boolean first() throws SQLException
   {
      return m_rs.first();
   }

   /**
    * JDBC 2.0
    *
    * <p>Moves the cursor to the last row in the result set.
    *
    * @return true if the cursor is on a valid row;
    * false if there are no rows in the result set
    * @exception SQLException if a database access error occurs or the
    * result set type is TYPE_FORWARD_ONLY.
    */
   public boolean last() throws SQLException
   {
      return m_rs.last();
   }

   /**
    * JDBC 2.0
    *
    * <p>Retrieves the current row number.  The first row is number 1, the
    * second number 2, and so on.
    *
    * @return the current row number; 0 if there is no current row
    * @exception SQLException if a database access error occurs
    */
   public int getRow() throws SQLException
   {
      return m_rs.getRow();
   }

   /**
    * JDBC 2.0
    *
    * <p>Moves the cursor to the given row number in the result set.
    *
    * <p>If the row number is positive, the cursor moves to
    * the given row number with respect to the
    * beginning of the result set.  The first row is row 1, the second
    * is row 2, and so on.
    *
    * <p>If the given row number is negative, the cursor moves to
    * an absolute row position with respect to
    * the end of the result set.  For example, calling
    * <code>absolute(-1)</code> positions the
    * cursor on the last row, <code>absolute(-2)</code> indicates the next-to-last
    * row, and so on.
    *
    * <p>An attempt to position the cursor beyond the first/last row in
    * the result set leaves the cursor before/after the first/last
    * row, respectively.
    *
    * <p>Note: Calling <code>absolute(1)</code> is the same
    * as calling <code>first()</code>.
    * Calling <code>absolute(-1)</code> is the same as calling <code>last()</code>.
    *
    * @return true if the cursor is on the result set; false otherwise
    * @exception SQLException if a database access error occurs or
    * row is 0, or result set type is TYPE_FORWARD_ONLY.
    */
   public boolean absolute( int row ) throws SQLException
   {
      return m_rs.absolute(row);
   }

   /**
    * JDBC 2.0
    *
    * <p>Moves the cursor a relative number of rows, either positive or negative.
    * Attempting to move beyond the first/last row in the
    * result set positions the cursor before/after the
    * the first/last row. Calling <code>relative(0)</code> is valid, but does
    * not change the cursor position.
    *
    * <p>Note: Calling <code>relative(1)</code>
    * is different from calling <code>next()</code>
    * because is makes sense to call <code>next()</code> when there is no current row,
    * for example, when the cursor is positioned before the first row
    * or after the last row of the result set.
    *
    * @return true if the cursor is on a row; false otherwise
    * @exception SQLException if a database access error occurs, there
    * is no current row, or the result set type is TYPE_FORWARD_ONLY
    */
   public boolean relative( int rows ) throws SQLException
   {
      return m_rs.relative(rows);
   }

   /**
    * JDBC 2.0
    *
    * <p>Moves the cursor to the previous row in the result set.
    *
    * <p>Note: <code>previous()</code> is not the same as
    * <code>relative(-1)</code> because it
    * makes sense to call</code>previous()</code> when there is no current row.
    *
    * @return true if the cursor is on a valid row; false if it is off the result set
    * @exception SQLException if a database access error occurs or the
    * result set type is TYPE_FORWARD_ONLY
    */
   public boolean previous() throws SQLException
   {
      return m_rs.previous();
   }

   /**
    * JDBC 2.0
    *
    * Gives a hint as to the direction in which the rows in this result set
    * will be processed.  The initial value is determined by the statement
    * that produced the result set.  The fetch direction may be changed
    * at any time.
    *
    * @exception SQLException if a database access error occurs or
    * the result set type is TYPE_FORWARD_ONLY and the fetch direction is not
    * FETCH_FORWARD.
    */
   public void setFetchDirection(int direction) throws SQLException
   {
      m_rs.setFetchDirection(direction);
   }

   /**
    * JDBC 2.0
    *
    * Returns the fetch direction for this result set.
    *
    * @return the current fetch direction for this result set
    * @exception SQLException if a database access error occurs
    */
   public int getFetchDirection() throws SQLException
   {
      return m_rs.getFetchDirection();
   }

   /**
    * JDBC 2.0
    *
    * Gives the JDBC driver a hint as to the number of rows that should
    * be fetched from the database when more rows are needed for this result
    * set.  If the fetch size specified is zero, the JDBC driver
    * ignores the value and is free to make its own best guess as to what
    * the fetch size should be.  The default value is set by the statement
    * that created the result set.  The fetch size may be changed at any
    * time.
    *
    * @param rows the number of rows to fetch
    * @exception SQLException if a database access error occurs or the
    * condition 0 <= rows <= this.getMaxRows() is not satisfied.
    */
   public void setFetchSize(int rows) throws SQLException
   {
      m_rs.setFetchSize(rows);
   }

   /**
    * JDBC 2.0
    *
    * Returns the fetch size for this result set.
    *
    * @return the current fetch size for this result set
    * @exception SQLException if a database access error occurs
    */
   public int getFetchSize() throws SQLException
   {
      return m_rs.getFetchSize();
   }

   /**
    * JDBC 2.0
    *
    * Returns the type of this result set.  The type is determined by
    * the statement that created the result set.
    *
    * @return TYPE_FORWARD_ONLY, TYPE_SCROLL_INSENSITIVE, or
    * TYPE_SCROLL_SENSITIVE
    * @exception SQLException if a database access error occurs
    */
   public int getType() throws SQLException
   {
      return m_rs.getType();
   }

   /**
    * JDBC 2.0
    *
    * Returns the concurrency mode of this result set.  The concurrency
    * used is determined by the statement that created the result set.
    *
    * @return the concurrency type, CONCUR_READ_ONLY or CONCUR_UPDATABLE
    * @exception SQLException if a database access error occurs
    */
   public int getConcurrency() throws SQLException
   {
      return m_rs.getConcurrency();
   }

   //---------------------------------------------------------------------
   // Updates
   //---------------------------------------------------------------------

   /**
    * JDBC 2.0
    *
    * Indicates whether the current row has been updated.  The value returned
    * depends on whether or not the result set can detect updates.
    *
    * @return true if the row has been visibly updated by the owner or
    * another, and updates are detected
    * @exception SQLException if a database access error occurs
    */
   public boolean rowUpdated() throws SQLException
   {
      return m_rs.rowUpdated();
   }

   /**
    * JDBC 2.0
    *
    * Indicates whether the current row has had an insertion.  The value returned
    * depends on whether or not the result set can detect visible inserts.
    *
    * @return true if a row has had an insertion and insertions are detected
    * @exception SQLException if a database access error occurs
    */
   public boolean rowInserted() throws SQLException
   {
      return m_rs.rowInserted();
   }

   /**
    * JDBC 2.0
    *
    * Indicates whether a row has been deleted.  A deleted row may leave
    * a visible "hole" in a result set.  This method can be used to
    * detect holes in a result set.  The value returned depends on whether
    * or not the result set can detect deletions.
    *
    * @return true if a row was deleted and deletions are detected
    * @exception SQLException if a database access error occurs
    */
   public boolean rowDeleted() throws SQLException
   {
      return m_rs.rowDeleted();
   }

   /**
    * JDBC 2.0
    *
    * Give a nullable column a null value.
    *
    * The <code>updateXXX</code> methods are used to update column values in the
    * current row, or the insert row.  The <code>updateXXX</code> methods do not
    * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
    * methods are called to update the database.
    *
    * @param columnIndex the first column is 1, the second is 2, ...
    * @exception SQLException if a database access error occurs
    */
   public void updateNull(int columnIndex) throws SQLException
   {
      m_rs.updateNull(columnIndex);
   }

   /**
    * JDBC 2.0
    *
    * Updates a column with a boolean value.
    *
    * The <code>updateXXX</code> methods are used to update column values in the
    * current row, or the insert row.  The <code>updateXXX</code> methods do not
    * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
    * methods are called to update the database.
    *
    * @param columnIndex the first column is 1, the second is 2, ...
    * @param x the new column value
    * @exception SQLException if a database access error occurs
    */
   public void updateBoolean(int columnIndex, boolean x) throws SQLException
   {
      m_rs.updateBoolean(columnIndex, x);
   }

   /**
    * JDBC 2.0
    *
    * Updates a column with a byte value.
    *
    * The <code>updateXXX</code> methods are used to update column values in the
    * current row, or the insert row.  The <code>updateXXX</code> methods do not
    * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
    * methods are called to update the database.
    *
    * @param columnIndex the first column is 1, the second is 2, ...
    * @param x the new column value
    * @exception SQLException if a database access error occurs
    */
   public void updateByte(int columnIndex, byte x) throws SQLException
   {
      m_rs.updateByte(columnIndex, x);
   }

   /**
    * JDBC 2.0
    *
    * Updates a column with a short value.
    *
    * The <code>updateXXX</code> methods are used to update column values in the
    * current row, or the insert row.  The <code>updateXXX</code> methods do not
    * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
    * methods are called to update the database.
    *
    * @param columnIndex the first column is 1, the second is 2, ...
    * @param x the new column value
    * @exception SQLException if a database access error occurs
    */
   public void updateShort(int columnIndex, short x) throws SQLException
   {
      m_rs.updateShort(columnIndex, x);
   }

   /**
    * JDBC 2.0
    *
    * Updates a column with an integer value.
    *
    * The <code>updateXXX</code> methods are used to update column values in the
    * current row, or the insert row.  The <code>updateXXX</code> methods do not
    * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
    * methods are called to update the database.
    *
    * @param columnIndex the first column is 1, the second is 2, ...
    * @param x the new column value
    * @exception SQLException if a database access error occurs
    */
   public void updateInt(int columnIndex, int x) throws SQLException
   {
      m_rs.updateInt(columnIndex, x);
   }

   /**
    * JDBC 2.0
    *
    * Updates a column with a long value.
    *
    * The <code>updateXXX</code> methods are used to update column values in the
    * current row, or the insert row.  The <code>updateXXX</code> methods do not
    * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
    * methods are called to update the database.
    *
    * @param columnIndex the first column is 1, the second is 2, ...
    * @param x the new column value
    * @exception SQLException if a database access error occurs
    */
   public void updateLong(int columnIndex, long x) throws SQLException
   {
      m_rs.updateLong(columnIndex, x);
   }

   /**
    * JDBC 2.0
    *
    * Updates a column with a float value.
    *
    * The <code>updateXXX</code> methods are used to update column values in the
    * current row, or the insert row.  The <code>updateXXX</code> methods do not
    * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
    * methods are called to update the database.
    *
    * @param columnIndex the first column is 1, the second is 2, ...
    * @param x the new column value
    * @exception SQLException if a database access error occurs
    */
   public void updateFloat(int columnIndex, float x) throws SQLException
   {
      m_rs.updateFloat(columnIndex, x);
   }

   /**
    * JDBC 2.0
    *
    * Updates a column with a Double value.
    *
    * The <code>updateXXX</code> methods are used to update column values in the
    * current row, or the insert row.  The <code>updateXXX</code> methods do not
    * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
    * methods are called to update the database.
    *
    * @param columnIndex the first column is 1, the second is 2, ...
    * @param x the new column value
    * @exception SQLException if a database access error occurs
    */
   public void updateDouble(int columnIndex, double x) throws SQLException
   {
      m_rs.updateDouble(columnIndex, x);
   }

   /**
    * JDBC 2.0
    *
    * Updates a column with a BigDecimal value.
    *
    * The <code>updateXXX</code> methods are used to update column values in the
    * current row, or the insert row.  The <code>updateXXX</code> methods do not
    * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
    * methods are called to update the database.
    *
    * @param columnIndex the first column is 1, the second is 2, ...
    * @param x the new column value
    * @exception SQLException if a database access error occurs
    */
   public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException
   {
      m_rs.updateBigDecimal(columnIndex, x);
   }

   /**
    * JDBC 2.0
    *
    * Updates a column with a String value.
    *
    * The <code>updateXXX</code> methods are used to update column values in the
    * current row, or the insert row.  The <code>updateXXX</code> methods do not
    * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
    * methods are called to update the database.
    *
    * @param columnIndex the first column is 1, the second is 2, ...
    * @param x the new column value
    * @exception SQLException if a database access error occurs
    */
   public void updateString(int columnIndex, String x) throws SQLException
   {
      m_rs.updateString(columnIndex, x);
   }

   /**
    * JDBC 2.0
    *
    * Updates a column with a byte array value.
    *
    * The <code>updateXXX</code> methods are used to update column values in the
    * current row, or the insert row.  The <code>updateXXX</code> methods do not
    * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
    * methods are called to update the database.
    *
    * @param columnIndex the first column is 1, the second is 2, ...
    * @param x the new column value
    * @exception SQLException if a database access error occurs
    */
   public void updateBytes(int columnIndex, byte x[]) throws SQLException
   {
      m_rs.updateBytes(columnIndex, x);
   }

   /**
    * JDBC 2.0
    *
    * Updates a column with a Date value.
    *
    * The <code>updateXXX</code> methods are used to update column values in the
    * current row, or the insert row.  The <code>updateXXX</code> methods do not
    * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
    * methods are called to update the database.
    *
    * @param columnIndex the first column is 1, the second is 2, ...
    * @param x the new column value
    * @exception SQLException if a database access error occurs
    */
   public void updateDate(int columnIndex, java.sql.Date x) throws SQLException
   {
      m_rs.updateDate(columnIndex, x);
   }

   /**
    * JDBC 2.0
    *
    * Updates a column with a Time value.
    *
    * The <code>updateXXX</code> methods are used to update column values in the
    * current row, or the insert row.  The <code>updateXXX</code> methods do not
    * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
    * methods are called to update the database.
    *
    * @param columnIndex the first column is 1, the second is 2, ...
    * @param x the new column value
    * @exception SQLException if a database access error occurs
    */
   public void updateTime(int columnIndex, java.sql.Time x) throws SQLException
   {
      m_rs.updateTime(columnIndex, x);
   }

   /**
    * JDBC 2.0
    *
    * Updates a column with a Timestamp value.
    *
    * The <code>updateXXX</code> methods are used to update column values in the
    * current row, or the insert row.  The <code>updateXXX</code> methods do not
    * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
    * methods are called to update the database.
    *
    * @param columnIndex the first column is 1, the second is 2, ...
    * @param x the new column value
    * @exception SQLException if a database access error occurs
    */
   public void updateTimestamp(int columnIndex, java.sql.Timestamp x)
     throws SQLException
   {
      m_rs.updateTimestamp(columnIndex, x);
   }

   /**
    * JDBC 2.0
    *
    * Updates a column with an ascii stream value.
    *
    * The <code>updateXXX</code> methods are used to update column values in the
    * current row, or the insert row.  The <code>updateXXX</code> methods do not
    * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
    * methods are called to update the database.
    *
    * @param columnIndex the first column is 1, the second is 2, ...
    * @param x the new column value
    * @param length the length of the stream
    * @exception SQLException if a database access error occurs
    */
   public void updateAsciiStream(int columnIndex,
           java.io.InputStream x,
           int length) throws SQLException
   {
      m_rs.updateAsciiStream(columnIndex, x, length);
   }

   /**
    * JDBC 2.0
    *
    * Updates a column with a binary stream value.
    *
    * The <code>updateXXX</code> methods are used to update column values in the
    * current row, or the insert row.  The <code>updateXXX</code> methods do not
    * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
    * methods are called to update the database.
    *
    * @param columnIndex the first column is 1, the second is 2, ...
    * @param x the new column value
    * @param length the length of the stream
    * @exception SQLException if a database access error occurs
    */
   public void updateBinaryStream(int columnIndex,
            java.io.InputStream x,
            int length) throws SQLException
   {
      m_rs.updateBinaryStream(columnIndex, x, length);
   }

   /**
    * JDBC 2.0
    *
    * Updates a column with a character stream value.
    *
    * The <code>updateXXX</code> methods are used to update column values in the
    * current row, or the insert row.  The <code>updateXXX</code> methods do not
    * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
    * methods are called to update the database.
    *
    * @param columnIndex the first column is 1, the second is 2, ...
    * @param x the new column value
    * @param length the length of the stream
    * @exception SQLException if a database access error occurs
    */
   public void updateCharacterStream(int columnIndex,
             java.io.Reader x,
             int length) throws SQLException
   {
      m_rs.updateCharacterStream(columnIndex, x, length);
   }

   /**
    * JDBC 2.0
    *
    * Updates a column with an Object value.
    *
    * The <code>updateXXX</code> methods are used to update column values in the
    * current row, or the insert row.  The <code>updateXXX</code> methods do not
    * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
    * methods are called to update the database.
    *
    * @param columnIndex the first column is 1, the second is 2, ...
    * @param x the new column value
    * @param scale For java.sql.Types.DECIMAL or java.sql.Types.NUMERIC types
    *  this is the number of digits after the decimal.  For all other
    *  types this value will be ignored.
    * @exception SQLException if a database access error occurs
    */
   public void updateObject(int columnIndex, Object x, int scale)
     throws SQLException
   {
      m_rs.updateObject(columnIndex, x, scale);
   }

   /**
    * JDBC 2.0
    *
    * Updates a column with an Object value.
    *
    * The <code>updateXXX</code> methods are used to update column values in the
    * current row, or the insert row.  The <code>updateXXX</code> methods do not
    * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
    * methods are called to update the database.
    *
    * @param columnIndex the first column is 1, the second is 2, ...
    * @param x the new column value
    * @exception SQLException if a database access error occurs
    */
   public void updateObject(int columnIndex, Object x) throws SQLException
   {
      m_rs.updateObject(columnIndex, x);
   }

   /**
    * JDBC 2.0
    *
    * Updates a column with a null value.
    *
    * The <code>updateXXX</code> methods are used to update column values in the
    * current row, or the insert row.  The <code>updateXXX</code> methods do not
    * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
    * methods are called to update the database.
    *
    * @param columnName the name of the column
    * @exception SQLException if a database access error occurs
    */
   public void updateNull(String columnName) throws SQLException
   {
      m_rs.updateNull(columnName);
   }

   /**
    * JDBC 2.0
    *
    * Updates a column with a boolean value.
    *
    * The <code>updateXXX</code> methods are used to update column values in the
    * current row, or the insert row.  The <code>updateXXX</code> methods do not
    * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
    * methods are called to update the database.
    *
    * @param columnName the name of the column
    * @param x the new column value
    * @exception SQLException if a database access error occurs
    */
   public void updateBoolean(String columnName, boolean x) throws SQLException
   {
      m_rs.updateBoolean(columnName, x);
   }

   /**
    * JDBC 2.0
    *
    * Updates a column with a byte value.
    *
    * The <code>updateXXX</code> methods are used to update column values in the
    * current row, or the insert row.  The <code>updateXXX</code> methods do not
    * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
    * methods are called to update the database.
    *
    * @param columnName the name of the column
    * @param x the new column value
    * @exception SQLException if a database access error occurs
    */
   public void updateByte(String columnName, byte x) throws SQLException
   {
      m_rs.updateByte(columnName, x);
   }

   /**
    * JDBC 2.0
    *
    * Updates a column with a short value.
    *
    * The <code>updateXXX</code> methods are used to update column values in the
    * current row, or the insert row.  The <code>updateXXX</code> methods do not
    * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
    * methods are called to update the database.
    *
    * @param columnName the name of the column
    * @param x the new column value
    * @exception SQLException if a database access error occurs
    */
   public void updateShort(String columnName, short x) throws SQLException
   {
      m_rs.updateShort(columnName, x);
   }

   /**
    * JDBC 2.0
    *
    * Updates a column with an integer value.
    *
    * The <code>updateXXX</code> methods are used to update column values in the
    * current row, or the insert row.  The <code>updateXXX</code> methods do not
    * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
    * methods are called to update the database.
    *
    * @param columnName the name of the column
    * @param x the new column value
    * @exception SQLException if a database access error occurs
    */
   public void updateInt(String columnName, int x) throws SQLException
   {
      m_rs.updateInt(columnName, x);
   }

   /**
    * JDBC 2.0
    *
    * Updates a column with a long value.
    *
    * The <code>updateXXX</code> methods are used to update column values in the
    * current row, or the insert row.  The <code>updateXXX</code> methods do not
    * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
    * methods are called to update the database.
    *
    * @param columnName the name of the column
    * @param x the new column value
    * @exception SQLException if a database access error occurs
    */
   public void updateLong(String columnName, long x) throws SQLException
   {
      m_rs.updateLong(columnName, x);
   }

   /**
    * JDBC 2.0
    *
    * Updates a column with a float value.
    *
    * The <code>updateXXX</code> methods are used to update column values in the
    * current row, or the insert row.  The <code>updateXXX</code> methods do not
    * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
    * methods are called to update the database.
    *
    * @param columnName the name of the column
    * @param x the new column value
    * @exception SQLException if a database access error occurs
    */
   public void updateFloat(String columnName, float x) throws SQLException
   {
      m_rs.updateFloat(columnName, x);
   }

   /**
    * JDBC 2.0
    *
    * Updates a column with a double value.
    *
    * The <code>updateXXX</code> methods are used to update column values in the
    * current row, or the insert row.  The <code>updateXXX</code> methods do not
    * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
    * methods are called to update the database.
    *
    * @param columnName the name of the column
    * @param x the new column value
    * @exception SQLException if a database access error occurs
    */
   public void updateDouble(String columnName, double x) throws SQLException
   {
      m_rs.updateDouble(columnName, x);
   }

   /**
    * JDBC 2.0
    *
    * Updates a column with a BigDecimal value.
    *
    * The <code>updateXXX</code> methods are used to update column values in the
    * current row, or the insert row.  The <code>updateXXX</code> methods do not
    * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
    * methods are called to update the database.
    *
    * @param columnName the name of the column
    * @param x the new column value
    * @exception SQLException if a database access error occurs
    */
   public void updateBigDecimal(String columnName, BigDecimal x) throws SQLException
   {
      m_rs.updateBigDecimal(columnName, x);
   }

   /**
    * JDBC 2.0
    *
    * Updates a column with a String value.
    *
    * The <code>updateXXX</code> methods are used to update column values in the
    * current row, or the insert row.  The <code>updateXXX</code> methods do not
    * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
    * methods are called to update the database.
    *
    * @param columnName the name of the column
    * @param x the new column value
    * @exception SQLException if a database access error occurs
    */
   public void updateString(String columnName, String x) throws SQLException
   {
      m_rs.updateString(columnName, x);
   }

   /**
    * JDBC 2.0
    *
    * Updates a column with a byte array value.
    *
    * The <code>updateXXX</code> methods are used to update column values in the
    * current row, or the insert row.  The <code>updateXXX</code> methods do not
    * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
    * methods are called to update the database.
    *
    * @param columnName the name of the column
    * @param x the new column value
    * @exception SQLException if a database access error occurs
    */
   public void updateBytes(String columnName, byte x[]) throws SQLException
   {
      m_rs.updateBytes(columnName, x);
   }

   /**
    * JDBC 2.0
    *
    * Updates a column with a Date value.
    *
    * The <code>updateXXX</code> methods are used to update column values in the
    * current row, or the insert row.  The <code>updateXXX</code> methods do not
    * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
    * methods are called to update the database.
    *
    * @param columnName the name of the column
    * @param x the new column value
    * @exception SQLException if a database access error occurs
    */
   public void updateDate(String columnName, java.sql.Date x) throws SQLException
   {
      m_rs.updateDate(columnName, x);
   }

   /**
    * JDBC 2.0
    *
    * Updates a column with a Time value.
    *
    * The <code>updateXXX</code> methods are used to update column values in the
    * current row, or the insert row.  The <code>updateXXX</code> methods do not
    * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
    * methods are called to update the database.
    *
    * @param columnName the name of the column
    * @param x the new column value
    * @exception SQLException if a database access error occurs
    */
   public void updateTime(String columnName, java.sql.Time x) throws SQLException
   {
      m_rs.updateTime(columnName, x);
   }

   /**
    * JDBC 2.0
    *
    * Updates a column with a Timestamp value.
    *
    * The <code>updateXXX</code> methods are used to update column values in the
    * current row, or the insert row.  The <code>updateXXX</code> methods do not
    * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
    * methods are called to update the database.
    *
    * @param columnName the name of the column
    * @param x the new column value
    * @exception SQLException if a database access error occurs
    */
   public void updateTimestamp(String columnName, java.sql.Timestamp x)
     throws SQLException
   {
      m_rs.updateTimestamp(columnName, x);
   }

   /**
    * JDBC 2.0
    *
    * Updates a column with an ascii stream value.
    *
    * The <code>updateXXX</code> methods are used to update column values in the
    * current row, or the insert row.  The <code>updateXXX</code> methods do not
    * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
    * methods are called to update the database.
    *
    * @param columnName the name of the column
    * @param x the new column value
    * @param length of the stream
    * @exception SQLException if a database access error occurs
    */
   public void updateAsciiStream(String columnName,
           java.io.InputStream x,
           int length) throws SQLException
   {
      m_rs.updateAsciiStream(columnName, x, length);
   }

   /**
    * JDBC 2.0
    *
    * Updates a column with a binary stream value.
    *
    * The <code>updateXXX</code> methods are used to update column values in the
    * current row, or the insert row.  The <code>updateXXX</code> methods do not
    * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
    * methods are called to update the database.
    *
    * @param columnName the name of the column
    * @param x the new column value
    * @param length of the stream
    * @exception SQLException if a database access error occurs
    */
   public void updateBinaryStream(String columnName,
            java.io.InputStream x,
            int length) throws SQLException
   {
      m_rs.updateBinaryStream(columnName, x, length);
   }

   /**
    * JDBC 2.0
    *
    * Updates a column with a character stream value.
    *
    * The <code>updateXXX</code> methods are used to update column values in the
    * current row, or the insert row.  The <code>updateXXX</code> methods do not
    * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
    * methods are called to update the database.
    *
    * @param columnName the name of the column
    * @param x the new column value
    * @param length of the stream
    * @exception SQLException if a database access error occurs
    */
   public void updateCharacterStream(String columnName,
             java.io.Reader reader,
             int length) throws SQLException
   {
      m_rs.updateCharacterStream(columnName, reader, length);
   }


   /**
    * JDBC 2.0
    *
    * Updates a column with an Object value.
    *
    * The <code>updateXXX</code> methods are used to update column values in the
    * current row, or the insert row.  The <code>updateXXX</code> methods do not
    * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
    * methods are called to update the database.
    *
    * @param columnName the name of the column
    * @param x the new column value
    * @param scale For java.sql.Types.DECIMAL or java.sql.Types.NUMERIC types
    *  this is the number of digits after the decimal.  For all other
    *  types this value will be ignored.
    * @exception SQLException if a database access error occurs
    */
   public void updateObject(String columnName, Object x, int scale)
     throws SQLException
   {
      m_rs.updateObject(columnName, x, scale);
   }

   /**
    * JDBC 2.0
    *
    * Updates a column with an Object value.
    *
    * The <code>updateXXX</code> methods are used to update column values in the
    * current row, or the insert row.  The <code>updateXXX</code> methods do not
    * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
    * methods are called to update the database.
    *
    * @param columnName the name of the column
    * @param x the new column value
    * @exception SQLException if a database access error occurs
    */
   public void updateObject(String columnName, Object x) throws SQLException
   {
      m_rs.updateObject(columnName, x);
   }

   /**
    * JDBC 2.0
    *
    * Inserts the contents of the insert row into the result set and
    * the database.  Must be on the insert row when this method is called.
    *
    * @exception SQLException if a database access error occurs,
    * if called when not on the insert row, or if not all of non-nullable columns in
    * the insert row have been given a value
    */
   public void insertRow() throws SQLException
   {
      m_rs.insertRow();
   }

   /**
    * JDBC 2.0
    *
    * Updates the underlying database with the new contents of the
    * current row.  Cannot be called when on the insert row.
    *
    * @exception SQLException if a database access error occurs or
    * if called when on the insert row
    */
   public void updateRow() throws SQLException
   {
      m_rs.updateRow();
   }

   /**
    * JDBC 2.0
    *
    * Deletes the current row from the result set and the underlying
    * database.  Cannot be called when on the insert row.
    *
    * @exception SQLException if a database access error occurs or if
    * called when on the insert row.
    */
   public void deleteRow() throws SQLException
   {
      m_rs.deleteRow();
   }

   /**
    * JDBC 2.0
    *
    * Refreshes the current row with its most recent value in
    * the database.  Cannot be called when on the insert row.
    *
    * The <code>refreshRow</code> method provides a way for an application to
    * explicitly tell the JDBC driver to refetch a row(s) from the
    * database.  An application may want to call <code>refreshRow</code> when
    * caching or prefetching is being done by the JDBC driver to
    * fetch the latest value of a row from the database.  The JDBC driver
    * may actually refresh multiple rows at once if the fetch size is
    * greater than one.
    *
    * All values are refetched subject to the transaction isolation
    * level and cursor sensitivity.  If <code>refreshRow</code> is called after
    * calling <code>updateXXX</code>, but before calling <code>updateRow</code>, then the
    * updates made to the row are lost.  Calling the method <code>refreshRow</code> frequently
    * will likely slow performance.
    *
    * @exception SQLException if a database access error occurs or if
    * called when on the insert row
    */
   public void refreshRow() throws SQLException
   {
      m_rs.refreshRow();
   }

   /**
    * JDBC 2.0
    *
    * Cancels the updates made to a row.
    * This method may be called after calling an
    * <code>updateXXX</code> method(s) and before calling <code>updateRow</code> to rollback
    * the updates made to a row.  If no updates have been made or
    * <code>updateRow</code> has already been called, then this method has no
    * effect.
    *
    * @exception SQLException if a database access error occurs or if
    * called when on the insert row
    *
    */
   public void cancelRowUpdates() throws SQLException
   {
      m_rs.cancelRowUpdates();
   }

   /**
    * JDBC 2.0
    *
    * Moves the cursor to the insert row.  The current cursor position is
    * remembered while the cursor is positioned on the insert row.
    *
    * The insert row is a special row associated with an updatable
    * result set.  It is essentially a buffer where a new row may
    * be constructed by calling the <code>updateXXX</code> methods prior to
    * inserting the row into the result set.
    *
    * Only the <code>updateXXX</code>, <code>getXXX</code>,
    * and <code>insertRow</code> methods may be
    * called when the cursor is on the insert row.  All of the columns in
    * a result set must be given a value each time this method is
    * called before calling <code>insertRow</code>.
    * The method <code>updateXXX</code> must be called before a
    * <code>getXXX</code> method can be called on a column value.
    *
    * @exception SQLException if a database access error occurs
    * or the result set is not updatable
    */
   public void moveToInsertRow() throws SQLException
   {
      m_rs.moveToInsertRow();
   }

   /**
    * JDBC 2.0
    *
    * Moves the cursor to the remembered cursor position, usually the
    * current row.  This method has no effect if the cursor is not on the insert
    * row.
    *
    * @exception SQLException if a database access error occurs
    * or the result set is not updatable
    */
   public void moveToCurrentRow() throws SQLException
   {
      m_rs.moveToCurrentRow();
   }

   /**
    * JDBC 2.0
    *
    * Returns the Statement that produced this <code>ResultSet</code> object.
    * If the result set was generated some other way, such as by a
    * <code>DatabaseMetaData</code> method, this method returns <code>null</code>.
    *
    * @return the Statment that produced the result set or
    * null if the result set was produced some other way
    * @exception SQLException if a database access error occurs
    */
   public Statement getStatement() throws SQLException
   {
      return m_rs.getStatement();
   }

   /**
    * JDBC 2.0
    *
    * Returns the value of a column in the current row as a Java object.
    * This method uses the given <code>Map</code> object
    * for the custom mapping of the
    * SQL structured or distinct type that is being retrieved.
    *
    * @param i the first column is 1, the second is 2, ...
    * @param map the mapping from SQL type names to Java classes
    * @return an object representing the SQL value
    */
   public Object getObject(int i, Map<String, Class<?>> map) throws SQLException
   {
      return m_rs.getObject(i, map);
   }

   /**
    * JDBC 2.0
    *
    * Gets a REF(&lt;structured-type&gt;) column value from the current row.
    *
    * @param i the first column is 1, the second is 2, ...
    * @return a <code>Ref</code> object representing an SQL REF value
    */
   public Ref getRef(int i) throws SQLException
   {
      return m_rs.getRef(i);
   }

   /**
    * JDBC 2.0
    *
    * Gets a BLOB value in the current row of this <code>ResultSet</code> object.
    *
    * @param i the first column is 1, the second is 2, ...
    * @return a <code>Blob</code> object representing the SQL BLOB value in
    *         the specified column
    */
   public Blob getBlob(int i) throws SQLException
   {
      return m_rs.getBlob(i);
   }

   /**
    * JDBC 2.0
    *
    * Gets a CLOB value in the current row of this <code>ResultSet</code> object.
    *
    * @param i the first column is 1, the second is 2, ...
    * @return a <code>Clob</code> object representing the SQL CLOB value in
    *         the specified column
    */
   public Clob getClob(int i) throws SQLException
   {
      return m_rs.getClob(i);
   }

   /**
    * JDBC 2.0
    *
    * Gets an SQL ARRAY value from the current row of this <code>ResultSet</code> object.
    *
    * @param i the first column is 1, the second is 2, ...
    * @return an <code>Array</code> object representing the SQL ARRAY value in
    *         the specified column
    */
   public java.sql.Array getArray(int i) throws SQLException
   {
      return m_rs.getArray(i);
   }

   /**
    * JDBC 2.0
    *
    * Returns the value in the specified column as a Java object.
    * This method uses the specified <code>Map</code> object for
    * custom mapping if appropriate.
    *
    * @param colName the name of the column from which to retrieve the value
    * @param map the mapping from SQL type names to Java classes
    * @return an object representing the SQL value in the specified column
    */
   public Object getObject(String colName, Map<String, Class<?>> map) throws SQLException
   {
      return m_rs.getObject(colName, map);
   }

   /**
    * JDBC 2.0
    *
    * Gets a REF(&lt;structured-type&gt;) column value from the current row.
    *
    * @param colName the column name
    * @return a <code>Ref</code> object representing the SQL REF value in
    *         the specified column
    */
   public Ref getRef(String colName) throws SQLException
   {
      return m_rs.getRef(colName);
   }

   /**
    * JDBC 2.0
    *
    * Gets a BLOB value in the current row of this <code>ResultSet</code> object.
    *
    * @param colName the name of the column from which to retrieve the value
    * @return a <code>Blob</code> object representing the SQL BLOB value in
    *         the specified column
    */
   public Blob getBlob(String colName) throws SQLException
   {
      return m_rs.getBlob(colName);
   }

   /**
    * JDBC 2.0
    *
    * Gets a CLOB value in the current row of this <code>ResultSet</code> object.
    *
    * @param colName the name of the column from which to retrieve the value
    * @return a <code>Clob</code> object representing the SQL CLOB value in
    *         the specified column
    */
   public Clob getClob(String colName) throws SQLException
   {
      return m_rs.getClob(colName);
   }

   /**
    * JDBC 2.0
    *
    * Gets an SQL ARRAY value in the current row of this <code>ResultSet</code> object.
    *
    * @param colName the name of the column from which to retrieve the value
    * @return an <code>Array</code> object representing the SQL ARRAY value in
    *         the specified column
    */
   public java.sql.Array getArray(String colName) throws SQLException
   {
      return m_rs.getArray(colName);
   }

   /**
    * JDBC 2.0
    *
    * Gets the value of a column in the current row as a java.sql.Date
    * object. This method uses the given calendar to construct an appropriate millisecond
    * value for the Date if the underlying database does not store
    * timezone information.
    *
    * @param columnIndex the first column is 1, the second is 2, ...
    * @param cal the calendar to use in constructing the date
    * @return the column value; if the value is SQL NULL, the result is null
    * @exception SQLException if a database access error occurs
    */
   public java.sql.Date getDate(int columnIndex, Calendar cal) throws SQLException
   {
      return m_rs.getDate(columnIndex, cal);
   }

   /**
    * Gets the value of a column in the current row as a java.sql.Date
    * object. This method uses the given calendar to construct an appropriate millisecond
    * value for the Date, if the underlying database does not store
    * timezone information.
    *
    * @param columnName the SQL name of the column from which to retrieve the value
    * @param cal the calendar to use in constructing the date
    * @return the column value; if the value is SQL NULL, the result is null
    * @exception SQLException if a database access error occurs
    */
   public java.sql.Date getDate(String columnName, Calendar cal) throws SQLException
   {
      return m_rs.getDate(columnName, cal);
   }

   /**
    * Gets the value of a column in the current row as a java.sql.Time
    * object. This method uses the given calendar to construct an appropriate millisecond
    * value for the Time if the underlying database does not store
    * timezone information.
    *
    * @param columnIndex the first column is 1, the second is 2, ...
    * @param cal the calendar to use in constructing the time
    * @return the column value; if the value is SQL NULL, the result is null
    * @exception SQLException if a database access error occurs
    */
   public java.sql.Time getTime(int columnIndex, Calendar cal) throws SQLException
   {
      return m_rs.getTime(columnIndex, cal);
   }

   /**
    * Gets the value of a column in the current row as a java.sql.Time
    * object. This method uses the given calendar to construct an appropriate millisecond
    * value for the Time if the underlying database does not store
    * timezone information.
    *
    * @param columnName the SQL name of the column
    * @param cal the calendar to use in constructing the time
    * @return the column value; if the value is SQL NULL, the result is null
    * @exception SQLException if a database access error occurs
    */
   public java.sql.Time getTime(String columnName, Calendar cal) throws SQLException
   {
      return m_rs.getTime(columnName, cal);
   }

   /**
    * Gets the value of a column in the current row as a java.sql.Timestamp
    * object. This method uses the given calendar to construct an appropriate millisecond
    * value for the Timestamp if the underlying database does not store
    * timezone information.
    *
    * @param columnIndex the first column is 1, the second is 2, ...
    * @param cal the calendar to use in constructing the timestamp
    * @return the column value; if the value is SQL NULL, the result is null
    * @exception SQLException if a database access error occurs
    */
   public java.sql.Timestamp getTimestamp(int columnIndex, Calendar cal)
     throws SQLException
   {
      return m_rs.getTimestamp(columnIndex, cal);
   }

   /**
    * Gets the value of a column in the current row as a java.sql.Timestamp
    * object. This method uses the given calendar to construct an appropriate millisecond
    * value for the Timestamp if the underlying database does not store
    * timezone information.
    *
    * @param columnName the SQL name of the column
    * @param cal the calendar to use in constructing the timestamp
    * @return the column value; if the value is SQL NULL, the result is null
    * @exception SQLException if a database access error occurs
    */
   public java.sql.Timestamp getTimestamp(String columnName, Calendar cal)
     throws SQLException
   {
      return m_rs.getTimestamp(columnName, cal);
   }

   /**
    * Indicates that the result set is an instance of <code>PSResultSet</code>.
    * @return <code>true</code> if the result set is an instance of
    * <code>PSResultSet</code>
    */
   public boolean isResultSetInstanceOfPSResultSet()
   {
      return (m_rs instanceof PSResultSet);
   }
   
   /* (non-Javadoc)
    * @see java.sql.ResultSet#getURL(int)
    */
   public URL getURL(int columnIndex) throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   /* (non-Javadoc)
    * @see java.sql.ResultSet#getURL(java.lang.String)
    */
   public URL getURL(String columnName) throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   /* (non-Javadoc)
    * @see java.sql.ResultSet#updateArray(int, java.sql.Array)
    */
   public void updateArray(int columnIndex, Array x) throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   /* (non-Javadoc)
    * @see java.sql.ResultSet#updateArray(java.lang.String, java.sql.Array)
    */
   public void updateArray(String columnName, Array x) throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   /* (non-Javadoc)
    * @see java.sql.ResultSet#updateBlob(int, java.sql.Blob)
    */
   public void updateBlob(int columnIndex, Blob x) throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   /* (non-Javadoc)
    * @see java.sql.ResultSet#updateBlob(java.lang.String, java.sql.Blob)
    */
   public void updateBlob(String columnName, Blob x) throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   /* (non-Javadoc)
    * @see java.sql.ResultSet#updateClob(int, java.sql.Clob)
    */
   public void updateClob(int columnIndex, Clob x) throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   /* (non-Javadoc)
    * @see java.sql.ResultSet#updateClob(java.lang.String, java.sql.Clob)
    */
   public void updateClob(String columnName, Clob x) throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   /* (non-Javadoc)
    * @see java.sql.ResultSet#updateRef(int, java.sql.Ref)
    */
   public void updateRef(int columnIndex, Ref x) throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   /* (non-Javadoc)
    * @see java.sql.ResultSet#updateRef(java.lang.String, java.sql.Ref)
    */
   public void updateRef(String columnName, Ref x) throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public int getHoldability() throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public Reader getNCharacterStream(int columnIndex) throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public Reader getNCharacterStream(String columnLabel) throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public NClob getNClob(int columnIndex) throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public NClob getNClob(String columnLabel) throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public String getNString(int columnIndex) throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public String getNString(String columnLabel) throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public RowId getRowId(int columnIndex) throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public RowId getRowId(String columnLabel) throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public SQLXML getSQLXML(int columnIndex) throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public SQLXML getSQLXML(String columnLabel) throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public boolean isClosed() throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public void updateAsciiStream(int columnIndex, InputStream x)
         throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public void updateAsciiStream(String columnLabel, InputStream x)
         throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public void updateAsciiStream(int columnIndex, InputStream x, long length)
         throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public void updateAsciiStream(String columnLabel, InputStream x, long length)
         throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public void updateBinaryStream(int columnIndex, InputStream x)
         throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public void updateBinaryStream(String columnLabel, InputStream x)
         throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public void updateBinaryStream(int columnIndex, InputStream x, long length)
         throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public void updateBinaryStream(String columnLabel, InputStream x, long length)
         throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public void updateBlob(int columnIndex, InputStream inputStream)
         throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public void updateBlob(String columnLabel, InputStream inputStream)
         throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public void updateBlob(int columnIndex, InputStream inputStream, long length)
         throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public void updateBlob(String columnLabel, InputStream inputStream,
         long length) throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public void updateCharacterStream(int columnIndex, Reader x)
         throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public void updateCharacterStream(String columnLabel, Reader reader)
         throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public void updateCharacterStream(int columnIndex, Reader x, long length)
         throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public void updateCharacterStream(String columnLabel, Reader reader,
         long length) throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public void updateClob(int columnIndex, Reader reader) throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public void updateClob(String columnLabel, Reader reader)
         throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public void updateClob(int columnIndex, Reader reader, long length)
         throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public void updateClob(String columnLabel, Reader reader, long length)
         throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public void updateNCharacterStream(int columnIndex, Reader x)
         throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public void updateNCharacterStream(String columnLabel, Reader reader)
         throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public void updateNCharacterStream(int columnIndex, Reader x, long length)
         throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public void updateNCharacterStream(String columnLabel, Reader reader,
         long length) throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public void updateNClob(int columnIndex, NClob nClob) throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public void updateNClob(String columnLabel, NClob nClob) throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public void updateNClob(int columnIndex, Reader reader) throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public void updateNClob(String columnLabel, Reader reader)
         throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public void updateNClob(int columnIndex, Reader reader, long length)
         throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public void updateNClob(String columnLabel, Reader reader, long length)
         throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public void updateNString(int columnIndex, String nString)
         throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public void updateNString(String columnLabel, String nString)
         throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public void updateRowId(int columnIndex, RowId x) throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public void updateRowId(String columnLabel, RowId x) throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public void updateSQLXML(int columnIndex, SQLXML xmlObject)
         throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public void updateSQLXML(String columnLabel, SQLXML xmlObject)
         throws SQLException
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
   public <T> T getObject(int columnIndex, Class<T> type) throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   @Override
   public <T> T getObject(String columnLabel, Class<T> type) throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

}
