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

package com.percussion.data;

import com.percussion.server.PSConsole;

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
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The PSResultSet class extends the JDBC result set model providing a
 * persistent storage mechanism. This allows the data to be fetched
 * from the original result set, manipulated then stored in this result set
 * for later use. Unlike JDBC result sets, once a column is read, we do not
 * set it to <code>null</code>. This allows getting column data more than
 * once using the result set methods.
 * 
 * TODO: Support fetch directions other than forward.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSResultSet implements ResultSet
{

   /**
    * Construct an empty result set.
    */
   public PSResultSet()
   {
      super();
      m_data = new ArrayList[1];
      m_data[0] = new ArrayList();
      m_nameToIndexMap = new java.util.HashMap();
      m_isOpen = true;
   }

   /**
    * Construct an empty result set with meta data.
    */
   public PSResultSet(PSResultSetMetaData meta)
   {
      this();
      m_metaData = meta;
   }

   /**
    * Construct a result set with data
    */
   public PSResultSet(
      List[] data, java.util.HashMap nameToIndexMap, PSResultSetMetaData meta)
   {
      setResultData(data, nameToIndexMap);
      m_isOpen = true;
      m_metaData = meta;
   }

   /**
    * A ResultSet is initially positioned before its first row; the first
    * call to next makes the first row the current row; the second call
    * makes the second row the current row, etc.
    * <p>
    * If an input stream from the previous row is open, it is implicitly
    * closed. The ResultSet's warning chain is cleared when a new row is
    * read.
    *
    * @return        <code>true</code> if the new current row is valid;
    *              <code>false</code> if there are no more rows 
    *
    * @exception     SQLException    if a database-access error occurs.
    */
   public boolean next()
      throws java.sql.SQLException
   {
      if (isBeforeFirst())
      {
         m_rowIndex = -1; // will be added to below to form row index 0
      }
      
      return (++m_rowIndex < m_numRows);
   }
   
   /**
    * In some cases, it is desirable to immediately release a ResultSet's
    * database and JDBC resources instead of waiting for this to happen
    * when it is automatically closed; the close method provides this
    * immediate release. 
    * <p>
    * Note: A ResultSet is automatically closed by the Statement that
    * generated it when that Statement is closed, re-executed, or is used
    * to retrieve the next result from a sequence of multiple results. A
    * ResultSet is also automatically closed when it is garbage collected.
    *
    * @exception      SQLException   if a database-access error occurs.
    */
   public void close()
      throws java.sql.SQLException
   {
      if (m_data != null)
      {
         // null each column so that it can get garbage collected
         for (int i = 0; i < m_data.length; i++)
         {
            m_data[i] = null;
         }
         m_data = null;
      }
      m_numRows = 0;
      m_rowIndex = 0;
      m_isOpen = false;
   }

   /**
    * A column may have the value of SQL NULL; wasNull reports whether the
    * last column read had this special value. Note that you must first
    * call getXXX on a column to try to read its value and then call
    * wasNull() to find if the value was the SQL NULL.
    *
    * @return        <code>true</code> if last column read was SQL NULL
    *
    * @exception     SQLException    if a database-access error occurs.
    */
   public boolean wasNull()
      throws java.sql.SQLException
   {
      return m_wasNull;
   }
   
   /**
    * Get the value of a column in the current row as a Java String.
    *
    * @param   columnIndex    the first column is 1, the second is 2, ...
    *
    * @return   the column value; if the value is SQL NULL, the result is null
    *
    * @exception     SQLException    if a database-access error occurs.
    */
   public java.lang.String getString(int columnIndex)
      throws java.sql.SQLException
   {
      doCheck(columnIndex);
      Object s = m_data[columnIndex - 1].get(m_rowIndex);
      if (s == null)
         m_wasNull = true;
      else
      {
         m_wasNull = false;
         return s.toString();
      }
      return null;
   }
   
   /**
    * Get the value of a column in the current row as a Java boolean.
    *
    * @param   columnIndex    the first column is 1, the second is 2, ...
    *
    * @return   the column value; if the value is SQL NULL, the result is
    *         <code>false</code>
    *
    * @exception     SQLException    if a database-access error occurs.
    */
   public boolean getBoolean(int columnIndex)
      throws java.sql.SQLException
   {
      doCheck(columnIndex);
      Boolean b = (Boolean)m_data[columnIndex - 1].get(m_rowIndex);
      if (b == null)
      {
         m_wasNull = true;
         return false;
      }
      else
         m_wasNull = false;
      return b.booleanValue();
   }
   
   /**
    * Get the value of a column in the current row as a Java byte.
    *
    * @param   columnIndex    the first column is 1, the second is 2, ...
    *
    * @return   the column value; if the value is SQL NULL, the result is 0
    *
    * @exception     SQLException    if a database-access error occurs.
    */
   public byte getByte(int columnIndex)
      throws java.sql.SQLException
   {
      doCheck(columnIndex);
      Byte b = (Byte)m_data[columnIndex - 1].get(m_rowIndex);
      if (b == null)
      {
         m_wasNull = true;
         return 0;
      }
      else
         m_wasNull = false;
      return b.byteValue();
   }
   
   /**
    * Get the value of a column in the current row as a Java short.
    *
    * @param   columnIndex    the first column is 1, the second is 2, ...
    *
    * @return   the column value; if the value is SQL NULL, the result is 0
    *
    * @exception     SQLException    if a database-access error occurs.
    */
   public short getShort(int columnIndex)
      throws java.sql.SQLException
   {
      doCheck(columnIndex);
      Short s = (Short)m_data[columnIndex - 1].get(m_rowIndex);
      if (s == null)
      {
         m_wasNull = true;
         return 0;
      }
      else
         m_wasNull = false;
      return s.shortValue();
   }
   
   /**
    * Get the value of a column in the current row as a Java int.
    *
    * @param   columnIndex    the first column is 1, the second is 2, ...
    *
    * @return   the column value; if the value is SQL NULL, the result is 0
    *
    * @exception     SQLException    if a database-access error occurs.
    */
   public int getInt(int columnIndex)
      throws java.sql.SQLException
   {
      doCheck(columnIndex);
      Integer i = (Integer)m_data[columnIndex - 1].get(m_rowIndex);
      if (i == null)
      {
         m_wasNull = true;
         return 0;
      }
      else
         m_wasNull = false;
      return i.intValue();
   }
   
   /**
    * Get the value of a column in the current row as a Java long.
    *
    * @param   columnIndex    the first column is 1, the second is 2, ...
    *
    * @return   the column value; if the value is SQL NULL, the result is 0
    *
    * @exception     SQLException    if a database-access error occurs.
    */
   public long getLong(int columnIndex)
      throws java.sql.SQLException
   {
      doCheck(columnIndex);
      Long l = (Long)m_data[columnIndex - 1].get(m_rowIndex);
      if (l == null)
      {
         m_wasNull = true;
         return 0L;
      }
      else
         m_wasNull = false;
      return l.longValue();
   }
   
   /**
    * Get the value of a column in the current row as a Java float.
    *
    * @param   columnIndex    the first column is 1, the second is 2, ...
    *
    * @return   the column value; if the value is SQL NULL, the result is 0
    *
    * @exception     SQLException    if a database-access error occurs.
    */
   public float getFloat(int columnIndex)
      throws java.sql.SQLException
   {
      doCheck(columnIndex);
      Float f = (Float)m_data[columnIndex - 1].get(m_rowIndex);
      if (f == null)
      {
         m_wasNull = true;
         return 0.0F;
      }
      else
         m_wasNull = false;
      return f.floatValue();
   }
   
   /**
    * Get the value of a column in the current row as a Java double.
    *
    * @param   columnIndex    the first column is 1, the second is 2, ...
    *
    * @return   the column value; if the value is SQL NULL, the result is 0
    *
    * @exception     SQLException    if a database-access error occurs.
    */
   public double getDouble(int columnIndex)
      throws java.sql.SQLException
   {
      doCheck(columnIndex);
      Double d = (Double)m_data[columnIndex - 1].get(m_rowIndex);
      if (d == null)
      {
         m_wasNull = true;
         return 0.0;
      }
      else
         m_wasNull = false;
      return d.doubleValue();
   }
   
   /**
    * Get the value of a column in the current row as a
    * java.math.BigDecimal object.
    *
    * @param   columnIndex    the first column is 1, the second is 2, ...
    *
    * @param   scale         the number of digits to the right of the
    *                     decimal 
    *
    * @return   the column value; if the value is SQL NULL, the result is
    *         <code>null</code>
    *
    * @exception     SQLException    if a database-access error occurs.
    */
   public java.math.BigDecimal getBigDecimal(int columnIndex, int scale)
      throws java.sql.SQLException
   {
      doCheck(columnIndex);
      BigDecimal b = (BigDecimal)m_data[columnIndex - 1].get(m_rowIndex);
      b = new BigDecimal(b.toBigInteger(), scale);
      if (b == null)
      {
         m_wasNull = true;
      }
      else
         m_wasNull = false;
      return b;
   }
   
   /**
    * Get the value of a column in the current row as a Java byte
    * array. The bytes represent the raw values returned by the driver.
    *
    * @param   columnIndex    the first column is 1, the second is 2, ...
    *
    * @return   the column value; if the value is SQL NULL, the result is
    *         <code>null</code>
    *
    * @exception     SQLException    if a database-access error occurs.
    */
   public byte[] getBytes(int columnIndex)
      throws java.sql.SQLException
   {
      doCheck(columnIndex);
      Object o = m_data[columnIndex - 1].get(m_rowIndex);
      m_wasNull = (o == null);
      if (!m_wasNull)
      {
         if (o instanceof byte[])
            return (byte[])o;
         if (o instanceof PSBinaryData)
            return ((PSBinaryData)o).getByteArray();
         return o.toString().getBytes();
      }

      return null;
   }
   
   /**
    * Get the value of a column in the current row as a 
    * java.sql.Date object.
    *
    * @param   columnIndex    the first column is 1, the second is 2, ...
    *
    * @return   the column value; if the value is SQL NULL, the result is
    *         <code>null</code>
    *
    * @exception     SQLException    if a database-access error occurs.
    */
   public java.sql.Date getDate(int columnIndex)
      throws java.sql.SQLException
   {
      doCheck(columnIndex);
      java.sql.Date d = (java.sql.Date)m_data[columnIndex - 1].get(m_rowIndex);
      if (d == null)
      {
         m_wasNull = true;
      }
      else
         m_wasNull = false;
      return d;
   }
   
   /**
    * Get the value of a column in the current row as a 
    * java.sql.Time object.
    *
    * @param   columnIndex    the first column is 1, the second is 2, ...
    *
    * @return   the column value; if the value is SQL NULL, the result is
    *         <code>null</code>
    *
    * @exception     SQLException    if a database-access error occurs.
    */
   public java.sql.Time getTime(int columnIndex)
      throws java.sql.SQLException
   {
      doCheck(columnIndex);
      java.sql.Time t = (java.sql.Time)m_data[columnIndex - 1].get(m_rowIndex);
      if (t == null)
      {
         m_wasNull = true;
      }
      else
         m_wasNull = false;
      return t;
   }
   
   /**
    * Get the value of a column in the current row as a 
    * java.sql.Timestamp object.
    *
    * @param   columnIndex    the first column is 1, the second is 2, ...
    *
    * @return   the column value; if the value is SQL NULL, the result is
    *         <code>null</code>
    *
    * @exception     SQLException    if a database-access error occurs.
    */
   public java.sql.Timestamp getTimestamp(int columnIndex)
      throws java.sql.SQLException
   {
      doCheck(columnIndex);
      java.sql.Timestamp t = (java.sql.Timestamp)m_data[columnIndex - 1].get(m_rowIndex);
      if (t == null)
      {
         m_wasNull = true;
      }
      else
         m_wasNull = false;
      return t;
   }
   
   /**
    * A column value can be retrieved as a stream of ASCII characters and
    * then read in chunks from the stream. This method is particularly
    * suitable for retrieving large LONGVARCHAR values. The JDBC driver
    * will do any necessary conversion from the database format into ASCII.
    * <p>
    * Note: All the data in the returned stream must be read prior to
    * getting the value of any other column. The next call to a get method
    * implicitly closes the stream. Also, a stream may return 0 for
    * available() whether there is data available or not.
    *
    * @param   columnIndex    the first column is 1, the second is 2, ...
    *
    * @return   a Java input stream that delivers the database column value
    *         as a stream of one byte ASCII characters. If the value is
    *         SQL NULL then the result is <code>null</code>.
    *
    * @exception     SQLException    if a database-access error occurs.
    */
   public java.io.InputStream getAsciiStream(int columnIndex)
      throws java.sql.SQLException
   {
      doCheck(columnIndex);
      return null;
   }
   
   /**
    * A column value can be retrieved as a stream of Unicode characters and
    * then read in chunks from the stream. This method is particularly
    * suitable for retrieving large LONGVARCHAR values. The JDBC driver
    * will do any necessary conversion from the database format into Unicode.
    * <p>
    * Note: All the data in the returned stream must be read prior to
    * getting the value of any other column. The next call to a get method
    * implicitly closes the stream. Also, a stream may return 0 for
    * available() whether there is data available or not.
    *
    * @param   columnIndex    the first column is 1, the second is 2, ...
    *
    * @return   a Java input stream that delivers the database column value
    *         as a stream of two byte Unicode characters. If the value is
    *         SQL NULL then the result is <code>null</code>.
    *
    * @exception     SQLException    if a database-access error occurs.
    */
   public java.io.InputStream getUnicodeStream(int columnIndex)
      throws java.sql.SQLException
   {
      return null;
   }
   
   /**
    * A column value can be retrieved as a stream of uninterpreted bytes
    * and then read in chunks from the stream. This method is particularly
    * suitable for retrieving large LONGVARBINARY values.
    * <p>
    * Note: All the data in the returned stream must be read prior to
    * getting the value of any other column. The next call to a get method
    * implicitly closes the stream. Also, a stream may return 0 for
    * available() whether there is data available or not.
    *
    * @param   columnIndex    the first column is 1, the second is 2, ...
    *
    * @return   a Java input stream that delivers the database column value
    *         as a stream of uniterpreted bytes. If the value is
    *         SQL NULL then the result is <code>null</code>.
    *
    * @exception     SQLException    if a database-access error occurs.
    */
   public java.io.InputStream getBinaryStream(int columnIndex)
      throws java.sql.SQLException
   {
      byte[] ba = getBytes(columnIndex);
      return new java.io.ByteArrayInputStream(ba);
   }
   
   /**
    * Get the value of a column in the current row as a Java String.
    *
    * @param   columnName      is the SQL name of the column 
    *
    * @return   the column value; if the value is SQL NULL, the result is null
    *
    * @exception     SQLException    if a database-access error occurs.
    */
   public java.lang.String getString(java.lang.String columnName)
      throws java.sql.SQLException
   {
      return getString(findColumn(columnName));
   }
   
   /**
    * Get the value of a column in the current row as a Java boolean.
    *
    * @param   columnName      is the SQL name of the column 
    *
    * @return   the column value; if the value is SQL NULL, the result is
    *         <code>false</code>
    *
    * @exception     SQLException    if a database-access error occurs.
    */
   public boolean getBoolean(java.lang.String columnName)
      throws java.sql.SQLException
   {
      return getBoolean(findColumn(columnName));
   }
   
   /**
    * Get the value of a column in the current row as a Java byte.
    *
    * @param   columnName      is the SQL name of the column 
    *
    * @return   the column value; if the value is SQL NULL, the result is 0
    *
    * @exception     SQLException    if a database-access error occurs.
    */
   public byte getByte(java.lang.String columnName)
      throws java.sql.SQLException
   {
      return getByte(findColumn(columnName));
   }
   
   /**
    * Get the value of a column in the current row as a Java short.
    *
    * @param   columnName      is the SQL name of the column 
    *
    * @return   the column value; if the value is SQL NULL, the result is 0
    *
    * @exception     SQLException    if a database-access error occurs.
    */
   public short getShort(java.lang.String columnName)
      throws java.sql.SQLException
   {
      return getShort(findColumn(columnName));
   }
   
   /**
    * Get the value of a column in the current row as a Java int.
    *
    * @param   columnName      is the SQL name of the column 
    *
    * @return   the column value; if the value is SQL NULL, the result is 0
    *
    * @exception     SQLException    if a database-access error occurs.
    */
   public int getInt(java.lang.String columnName)
      throws java.sql.SQLException
   {
      return getInt(findColumn(columnName));
   }
   
   /**
    * Get the value of a column in the current row as a Java long.
    *
    * @param   columnName      is the SQL name of the column 
    *
    * @return   the column value; if the value is SQL NULL, the result is 0
    *
    * @exception     SQLException    if a database-access error occurs.
    */
   public long getLong(java.lang.String columnName)
      throws java.sql.SQLException
   {
      return getLong(findColumn(columnName));
   }
   
   /**
    * Get the value of a column in the current row as a Java float.
    *
    * @param   columnName      is the SQL name of the column 
    *
    * @return   the column value; if the value is SQL NULL, the result is 0
    *
    * @exception     SQLException    if a database-access error occurs.
    */
   public float getFloat(java.lang.String columnName)
      throws java.sql.SQLException
   {
      return getFloat(findColumn(columnName));
   }
   
   /**
    * Get the value of a column in the current row as a Java double.
    *
    * @param   columnName      is the SQL name of the column 
    *
    * @return   the column value; if the value is SQL NULL, the result is 0
    *
    * @exception     SQLException    if a database-access error occurs.
    */
   public double getDouble(java.lang.String columnName)
      throws java.sql.SQLException
   {
      return getDouble(findColumn(columnName));
   }
   
   /**
    * Get the value of a column in the current row as a
    * java.math.BigDecimal object.
    *
    * @param   columnName      is the SQL name of the column 
    *
    * @param   scale         the number of digits to the right of the
    *                     decimal 
    *
    * @return   the column value; if the value is SQL NULL, the result is
    *         <code>null</code>
    *
    * @exception     SQLException    if a database-access error occurs.
    */
   public java.math.BigDecimal getBigDecimal(java.lang.String columnName, int scale)
      throws java.sql.SQLException
   {
      return getBigDecimal(findColumn(columnName), scale);
   }
   
   /**
    * Get the value of a column in the current row as a Java byte
    * array. The bytes represent the raw values returned by the driver.
    *
    * @param   columnName      is the SQL name of the column 
    *
    * @return   the column value; if the value is SQL NULL, the result is
    *         <code>null</code>
    *
    * @exception     SQLException    if a database-access error occurs.
    */
   public byte[] getBytes(java.lang.String columnName)
      throws java.sql.SQLException
   {
      return getBytes(findColumn(columnName));
   }
   
   /**
    * Get the value of a column in the current row as a 
    * java.sql.Date object.
    *
    * @param   columnName      is the SQL name of the column 
    *
    * @return   the column value; if the value is SQL NULL, the result is
    *         <code>null</code>
    *
    * @exception     SQLException    if a database-access error occurs.
    */
   public java.sql.Date getDate(java.lang.String columnName)
      throws java.sql.SQLException
   {
      return getDate(findColumn(columnName));
   }
   
   /**
    * Get the value of a column in the current row as a 
    * java.sql.Time object.
    *
    * @param   columnName      is the SQL name of the column 
    *
    * @return   the column value; if the value is SQL NULL, the result is
    *         <code>null</code>
    *
    * @exception     SQLException    if a database-access error occurs.
    */
   public java.sql.Time getTime(java.lang.String columnName)
      throws java.sql.SQLException
   {
      return getTime(findColumn(columnName));
   }
   
   /**
    * Get the value of a column in the current row as a 
    * java.sql.Timestamp object.
    *
    * @param   columnName      is the SQL name of the column 
    *
    * @return   the column value; if the value is SQL NULL, the result is
    *         <code>null</code>
    *
    * @exception     SQLException    if a database-access error occurs.
    */
   public java.sql.Timestamp getTimestamp(java.lang.String columnName)
      throws java.sql.SQLException
   {
      return getTimestamp(findColumn(columnName));
   }
   
   /**
    * A column value can be retrieved as a stream of ASCII characters and
    * then read in chunks from the stream. This method is particularly
    * suitable for retrieving large LONGVARCHAR values. The JDBC driver
    * will do any necessary conversion from the database format into ASCII.
    * <p>
    * Note: All the data in the returned stream must be read prior to
    * getting the value of any other column. The next call to a get method
    * implicitly closes the stream. Also, a stream may return 0 for
    * available() whether there is data available or not.
    *
    * @param   columnName      is the SQL name of the column 
    *
    * @return   a Java input stream that delivers the database column value
    *         as a stream of one byte ASCII characters. If the value is
    *         SQL NULL then the result is <code>null</code>.
    *
    * @exception     SQLException    if a database-access error occurs.
    */
   public java.io.InputStream getAsciiStream(java.lang.String columnName)
      throws java.sql.SQLException
   {
      return getAsciiStream(findColumn(columnName));
   }
   
   /**
    * A column value can be retrieved as a stream of Unicode characters and
    * then read in chunks from the stream. This method is particularly
    * suitable for retrieving large LONGVARCHAR values. The JDBC driver
    * will do any necessary conversion from the database format into Unicode.
    * <p>
    * Note: All the data in the returned stream must be read prior to
    * getting the value of any other column. The next call to a get method
    * implicitly closes the stream. Also, a stream may return 0 for
    * available() whether there is data available or not.
    *
    * @param   columnName      is the SQL name of the column 
    *
    * @return   a Java input stream that delivers the database column value
    *         as a stream of two byte Unicode characters. If the value is
    *         SQL NULL then the result is <code>null</code>.
    *
    * @exception     SQLException    if a database-access error occurs.
    */
   public java.io.InputStream getUnicodeStream(java.lang.String columnName)
      throws java.sql.SQLException
   {
      return getUnicodeStream(findColumn(columnName));
   }
   
   /**
    * A column value can be retrieved as a stream of uninterpreted bytes
    * and then read in chunks from the stream. This method is particularly
    * suitable for retrieving large LONGVARBINARY values.
    * <p>
    * Note: All the data in the returned stream must be read prior to
    * getting the value of any other column. The next call to a get method
    * implicitly closes the stream. Also, a stream may return 0 for
    * available() whether there is data available or not.
    *
    * @param   columnName      is the SQL name of the column 
    *
    * @return   a Java input stream that delivers the database column value
    *         as a stream of uniterpreted bytes. If the value is
    *         SQL NULL then the result is <code>null</code>.
    *
    * @exception     SQLException    if a database-access error occurs.
    */
   public java.io.InputStream getBinaryStream(java.lang.String columnName)
      throws java.sql.SQLException
   {
      return getBinaryStream(findColumn(columnName));
   }
   
   /**
    * The first warning reported by calls on this ResultSet is returned.
    * Subsequent ResultSet warnings will be chained to this SQLWarning.
    * <p>
    * The warning chain is automatically cleared each time a new row is read.
    * <p>
    * Note: This warning chain only covers warnings caused by ResultSet
    * methods. Any warning caused by statement methods (such as reading
    * OUT parameters) will be chained on the Statement object.
    *
    * @return        the first SQLWarning or <code>null</code>
    *
    * @exception     SQLException    if a database-access error occurs.
    */
   public java.sql.SQLWarning getWarnings()
      throws java.sql.SQLException
   {
      return null;
   }
   
   /**
    * After this call getWarnings returns null until a new warning is
    * reported for this ResultSet.
    *
    * @exception     SQLException    if a database-access error occurs.
    */
   public void clearWarnings()
      throws java.sql.SQLException
   {
   }
   
   /**
    * Get the name of the SQL cursor used by this ResultSet.
    * <p>
    * In SQL, a result table is retrieved through a cursor that is named.
    * The current row of a result can be updated or deleted using a
    * positioned update/delete statement that references the cursor name.
    * <p>
    * JDBC supports this SQL feature by providing the name of the SQL
    * cursor used by a ResultSet. The current row of a ResultSet is also
    * the current row of this SQL cursor.
    * <p>
    * Note: If positioned update is not supported a SQLException is thrown
    *
    * @return        the ResultSet's SQL cursor name 
    *
    * @exception     SQLException    if a database-access error occurs.
    */
   public java.lang.String getCursorName()
      throws java.sql.SQLException
   {
      return null;
   }
   
   /**
    * The number, types and properties of a ResultSet's columns are
    * provided by the getMetaData method.
    *
    * @return        the description of a ResultSet's columns
    *
    * @exception     SQLException    if a database-access error occurs.
    */
   public java.sql.ResultSetMetaData getMetaData()
      throws java.sql.SQLException
   {
      return m_metaData;
   }
   
   /**
    * Get the value of a column in the current row as a Java object.
    * <p>
    * This method will return the value of the given column as a Java
    * object. The type of the Java object will be the default Java Object
    * type corresponding to the column's SQL type, following the mapping
    * specified in the JDBC spec.
    * <p>
    * This method may also be used to read datatabase specific abstract
    * data types.
    *
    * @param   columnIndex    the first column is 1, the second is 2, ...
    *
    * @return   A java.lang.Object holding the column value.
    *
    * @exception     SQLException    if a database-access error occurs.
    */
   public java.lang.Object getObject(int columnIndex)
      throws java.sql.SQLException
   {
      Object o = (Object)m_data[columnIndex - 1].get(m_rowIndex);
      if (o == null)
         m_wasNull = true;
      else
         m_wasNull = false;
      return o;
   }

   /**
    * Get the value of a column in the current row as a Java object.
    * <p>
    * This method will return the value of the given column as a Java
    * object. The type of the Java object will be the default Java Object
    * type corresponding to the column's SQL type, following the mapping
    * specified in the JDBC spec.
    * <p>
    * This method may also be used to read datatabase specific abstract
    * data types.
    *
    * @param   columnName      is the SQL name of the column 
    *
    * @return   A java.lang.Object holding the column value.
    *
    * @exception     SQLException    if a database-access error occurs.
    */
   public java.lang.Object getObject(java.lang.String columnName)
      throws java.sql.SQLException
   {
      return getObject(findColumn(columnName));
   }
   
   /**
    * Map a Resultset column name to a ResultSet column index.
    *
    * @param   columnName      is the SQL name of the column 
    *
    * @return   the column index
    *
    * @exception     SQLException    if a database-access error occurs.
    */
   public int findColumn(String columnName)
      throws java.sql.SQLException
   {
      try
      {
         Integer i = (Integer)(m_nameToIndexMap.get(columnName));
         if (i == null)
            throw new SQLException("No column named " + columnName);
         return i.intValue();
      }
      catch (Exception e)
      {
         throw new SQLException(e.toString());
      }
   }
   
   /**
    * JDBC 2.0
    * <p>
    * Gets the value of a column in the current row as a java.io.Reader.
    *
    * @param   columnIndex    the first column is 1, the second is 2, ...
    *
    * @return   the value in the specified column as a java.io.Reader
    *
    * @exception     SQLException    if a database-access error occurs.
    */
   public java.io.Reader getCharacterStream(int columnIndex)
      throws SQLException
   {
      return null;
   }
   
   /**
    * JDBC 2.0
    * <p>
    * Gets the value of a column in the current row as a java.io.Reader.
    *
    * @param   columnName      is the SQL name of the column 
    *
    * @return   the value in the specified column as a java.io.Reader
    *
    * @exception     SQLException    if a database-access error occurs.
    */
   public java.io.Reader getCharacterStream(java.lang.String columnName)
      throws SQLException
   {
      return getCharacterStream(findColumn(columnName));
   }
   
   /**
    * JDBC 2.0 Gets the value of a column in the current row as a
    * java.math.BigDecimal object with full precision.
    *
    * @param   columnIndex    the first column is 1, the second is 2, ...
    *
    * @return   the column value (full precision); if the value is
    *         SQL NULL, the result is <code>null</code>
    *
    * @exception     SQLException    if a database-access error occurs.
    */
   public java.math.BigDecimal getBigDecimal(int columnIndex)
      throws SQLException
   {
      BigDecimal b = (BigDecimal)m_data[columnIndex - 1].get(m_rowIndex);
      if (b == null)
         m_wasNull = true;
      else
         m_wasNull = false;
      return b;
   }
   
   /**
    * JDBC 2.0 Gets the value of a column in the current row as a
    * java.math.BigDecimal object with full precision.
    *
    * @param   columnName      is the SQL name of the column 
    *
    * @return   the column value (full precision); if the value is
    *         SQL NULL, the result is <code>null</code>
    *
    * @exception     SQLException    if a database-access error occurs
    */
   public java.math.BigDecimal getBigDecimal(java.lang.String columnName)
      throws SQLException
   {
      return getBigDecimal(findColumn(columnName));
   }
   
   /**
    * JDBC 2.0 
    * <p>
    * Indicates whether the cursor is before the first row in the result set.
    *
    * @return   <code>true</code> if the cursor is before the first row,
    *         <code>false</code> otherwise. Returns <code>false</code>
    *         when the result set contains no rows.
    *
    * @exception     SQLException    if a database-access error occurs
    */
   public boolean isBeforeFirst()
      throws SQLException
   {
      return (m_rowIndex < 0);
   }
   
   /**
    * JDBC 2.0 
    * <p>
    * Indicates whether the cursor is after the last row in the result set.
    *
    * @return   <code>true</code> if the cursor is after the last row,
    *         <code>false</code> otherwise. Returns <code>false</code>
    *         when the result set contains no rows.
    *
    * @exception     SQLException    if a database-access error occurs
    */
   public boolean isAfterLast()
      throws SQLException
   {
      return (m_rowIndex >= m_numRows);
   }
   
   /**
    * JDBC 2.0 
    * <p>
    * Indicates whether the cursor is on the first row of the result set.
    *
    * @return   <code>true</code> if the cursor is on the first row,
    *         <code>false</code> otherwise.
    *
    * @exception     SQLException    if a database-access error occurs
    */
   public boolean isFirst()
      throws SQLException
   {
      return (m_rowIndex == 0);
   }
   
   /**
    * JDBC 2.0 
    * <p>
    * Indicates whether the cursor is on the last row of the result set.
    * Note: Calling the method isLast may be expensive because the JDBC
    * driver might need to fetch ahead one row in order to determine
    * whether the current row is the last row in the result set.
    *
    * @return   <code>true</code> if the cursor is on the last row,
    *         <code>false</code> otherwise.
    *
    * @exception     SQLException    if a database-access error occurs
    */
   public boolean isLast()
      throws SQLException
   {
      return (m_rowIndex == (m_numRows - 1));
   }
   
   /**
    * JDBC 2.0 
    * <p>
    * Moves the cursor to the front of the result set, just before the
    * first row. Has no effect if the result set contains no rows.
    *
    * @exception     SQLException    if a database-access error occurs or
    *                         the result set type is TYPE_FORWARD_ONLY
    */
   public void beforeFirst()
      throws SQLException
   {
      m_rowIndex = -1;
   }
   
   /**
    * JDBC 2.0 
    * <p>
    * Moves the cursor to the end of the result set, just after the last row.
    * Has no effect if the result set contains no rows.
    *
    * @exception     SQLException    if a database-access error occurs or
    *                         the result set type is TYPE_FORWARD_ONLY
    */
   public void afterLast()
      throws SQLException
   {
      m_rowIndex = m_numRows;
   }
   
   /**
    * JDBC 2.0 
    * <p>
    * Moves the cursor to the first row in the result set.
    *
    * @return   <code>true</code> if the cursor is on a valid row;
    *         <code>false</code> if there are no rows in the result set
    *
    * @exception     SQLException    if a database-access error occurs or
    *                         the result set type is TYPE_FORWARD_ONLY
    */
   public boolean first()
      throws SQLException
   {
      if (m_numRows == 0)
         return false;
      m_rowIndex = 0;
      return true;
   }
   
   /**
    * JDBC 2.0 
    * <p>
    * Moves the cursor to the last row in the result set.
    *
    * @return   <code>true</code> if the cursor is on a valid row;
    *         <code>false</code> if there are no rows in the result set
    *
    * @exception     SQLException    if a database-access error occurs or
    *                         the result set type is TYPE_FORWARD_ONLY
    */
   public boolean last()
      throws SQLException
   {
      m_rowIndex = m_numRows - 1;
      return (m_numRows > 0);
   }
   
   /**
    * JDBC 2.0 
    * <p>
    * Retrieves the current row number. The first row is number 1, the
    * second number 2, and so on.
    *
    * @return   the current row number; 0 if there is no current row
    *
    * @exception     SQLException    if a database-access error occurs
    */
   public int getRow()
      throws SQLException
   {
      if (isBeforeFirst() || isAfterLast())
         return 0;
      return m_rowIndex + 1;
   }
   
   /**
    * JDBC 2.0 
    * <p>
    * Moves the cursor to the given row number in the result set. 
    * <p>
    * If the row number is positive, the cursor moves to the given row
    * number with respect to the beginning of the result set. The first
    * row is row 1, the second is row 2, and so on. 
    * <p>
    * If the given row number is negative, the cursor moves to an absolute
    * row position with respect to the end of the result set. For example,
    * calling absolute(-1) positions the cursor on the last row,
    * absolute(-2) indicates the next-to-last row, and so on. 
    * <p>
    * An attempt to position the cursor beyond the first/last row in the
    * result set leaves the cursor before/after the first/last row,
    * respectively. 
    * <p>
    * Note: Calling absolute(1) is the same as calling first().
    * Calling absolute(-1) is the same as calling last().
    *
    * @return   <code>true</code> if the cursor is on the result set;
    *         <code>false</code> otherwise
    *
    * @exception     SQLException    if a database-access error occurs or
    *                         row is 0, or
    *                         the result set type is TYPE_FORWARD_ONLY
    */
   public boolean absolute(int row)
      throws SQLException
   {
      if (row == 0)
         throw new SQLException();

      if (row > 0)
      {
         m_rowIndex = row - 1;
         if (m_rowIndex >= m_numRows)
            m_rowIndex = m_numRows - 1;
      }
      else
      {
         m_rowIndex = m_numRows - row;
         if (m_rowIndex < 0)
            m_rowIndex = 0;
      }
      return (m_numRows != 0);
   }
   
   /**
    * JDBC 2.0 
    * <p>
    * Moves the cursor a relative number of rows, either positive or
    * negative. Attempting to move beyond the first/last row in the
    * result set positions the cursor before/after the the first/last row.
    * Calling relative(0) is valid, but does not change the cursor position. 
    * <p>
    * Note: Calling relative(1) is different from calling next() because is
    * makes sense to call next() when there is no current row, for example,
    * when the cursor is positioned before the first row or after the last
    * row of the result set.
    *
    * @return   <code>true</code> if the cursor is on a row;
    *         <code>false</code> otherwise
    *
    * @exception     SQLException    if a database-access error occurs or
    *                         there is no current row, or
    *                         the result set type is TYPE_FORWARD_ONLY
    */
   public boolean relative(int rows)
      throws SQLException
   {
      if (isBeforeFirst() || isAfterLast())
         throw new SQLException();
      return absolute(rows + m_rowIndex);
   }
   
   /**
    * JDBC 2.0 
    * <p>
    * Moves the cursor to the previous row in the result set. 
    * <p>
    * Note: previous() is not the same as relative(-1) because it makes
    * sense to call previous() when there is no current row.
    *
    * @return   <code>true</code> if the cursor is on a valid row;
    *         <code>false</code> if it is off the result set
    *
    * @exception     SQLException    if a database-access error occurs or
    *                         the result set type is TYPE_FORWARD_ONLY
    */
   public boolean previous()
      throws SQLException
   {
      return (--m_rowIndex >= 0);
   }
   
   /**
    * JDBC 2.0 Gives a hint as to the direction in which the rows in this
    * result set will be processed. The initial value is determined by the
    * statement that produced the result set. The fetch direction may be
    * changed at any time.
    * 
    * @exception     SQLException    if a database access error occurs or
    *                         the result set type is TYPE_FORWARD_ONLY
    *                         and the fetch direction is not
    *                         FETCH_FORWARD.
    */
   public void setFetchDirection(int direction)
      throws SQLException
   {
      if (m_resultSetType == TYPE_FORWARD_ONLY)
         if (direction != FETCH_FORWARD)
            throw new SQLException("Result set is forward only");
      
      m_fetchDirection = direction;
   }
   
   /**
    * JDBC 2.0 Returns the fetch direction for this result set.
    *
    * @return   the current fetch direction for this result set
    *
    * @exception     SQLException    if a database-access error occurs
    */
   public int getFetchDirection()
      throws SQLException
   {
      return m_fetchDirection;
   }
   
   /**
    * JDBC 2.0 Gives the JDBC driver a hint as to the number of rows that
    * should be fetched from the database when more rows are needed for
    * this result set. If the fetch size specified is zero, the JDBC
    * driver ignores the value and is free to make its own best guess as
    * to what the fetch size should be. The default value is set by the
    * statement that created the result set. The fetch size may be changed
    * at any time.
    *
    * @param   rows    the number of rows to fetch
    *
    * @exception     SQLException    if a database access error occurs or
    *                         the condition 0 <= rows <=
    *                         this.getMaxRows() is not satisfied.
    */
   public void setFetchSize(int rows)
      throws SQLException
   {
      // ignore hint
   }
   
   /**
    * JDBC 2.0 Returns the fetch size for this result set.
    *
    * @return   the current fetch size for this result set
    *
    * @exception     SQLException    if a database-access error occurs
    */
   public int getFetchSize()
      throws SQLException
   {
      return 0;
   }
   
   /**
    * JDBC 2.0 Returns the type of this result set. The type is determined
    * by the statement that created the result set.
    *
    * @return   TYPE_FORWARD_ONLY, TYPE_SCROLL_INSENSITIVE, or
    *         TYPE_SCROLL_SENSITIVE
    *
    * @exception     SQLException    if a database-access error occurs
    */
   public int getType()
      throws SQLException
   {
      return m_resultSetType;
   }
   
   /**
    * JDBC 2.0 Returns the concurrency mode of this result set. The
    * concurrency used is determined by the statement that created the
    * result set.
    *
    * @return   the concurrency type, CONCUR_READ_ONLY or CONCUR_UPDATABLE
    *
    * @exception     SQLException    if a database-access error occurs
    */
   public int getConcurrency()
      throws SQLException
   {
      return CONCUR_READ_ONLY;
   }
   
   /**
    * JDBC 2.0 Indicates whether the current row has been updated.
    * The value returned depends on whether or not the result set can
    * detect updates.
    *
    * @return   <code>true</code> if the row has been visibly updated by
    *         the owner or another, and updates are detected
    *
    * @exception     SQLException    if a database-access error occurs
    */
   public boolean rowUpdated()
      throws SQLException
   {
      return false;
   }
   
   /**
    * JDBC 2.0 Indicates whether the current row has had an insertion.
    * The value returned depends on whether or not the result set can
    * detect visible inserts.
    *
    * @retrn   <code>true</code> if a row has had an insertion and
    *         insertions are detected
    *
    * @exception     SQLException    if a database-access error occurs
    */
   public boolean rowInserted()
      throws SQLException
   {
      return false;
   }
   
   /**
    * JDBC 2.0 Indicates whether a row has been deleted. A deleted row
    * may leave a visible "hole" in a result set. This method can be
    * used to detect holes in a result set. The value returned depends
    * on whether or not the result set can detect deletions.
    *
    * @return   <code>true</code> if a row was deleted and deletions
    *         are detected
    *
    * @exception     SQLException    if a database-access error occurs
    */
   public boolean rowDeleted()
      throws SQLException
   {
      return false;
   }
   
   /**
    * JDBC 2.0 Give a nullable column a null value. The updateXXX methods
    * are used to update column values in the current row, or the insert
    * row. The updateXXX methods do not update the underlying database;
    * instead the updateRow or insertRow methods are called to update
    * the database.
    *
    * @param   columnIndex    the first column is 1, the second is 2, ...
    *
    * @exception     SQLException    if a database-access error occurs
    */
   public void updateNull(int columnIndex)
      throws SQLException
   {
      doCheck(columnIndex);
      m_data[columnIndex - 1].set(m_rowIndex, null);
   }
   
   /**
    * JDBC 2.0 Updates a column with a boolean value. The updateXXX methods
    * are used to update column values in the current row, or the insert
    * row. The updateXXX methods do not update the underlying database;
    * instead the updateRow or insertRow methods are called to update
    * the database.
    * 
    * @param   columnIndex    the first column is 1, the second is 2, ...
    *
    * @param   x            the new column value
    *
    * @exception     SQLException    if a database-access error occurs
    */
   public void updateBoolean(int columnIndex,
      boolean x)
      throws SQLException
   {
      doCheck(columnIndex);
      m_data[columnIndex - 1].set(m_rowIndex, x);
   }
   
   /**
    * JDBC 2.0 Updates a column with a byte value. The updateXXX methods
    * are used to update column values in the current row, or the insert
    * row. The updateXXX methods do not update the underlying database;
    * instead the updateRow or insertRow methods are called to update
    * the database.
    *
    * @param   columnIndex    the first column is 1, the second is 2, ...
    *
    * @param   x            the new column value
    *
    * @exception     SQLException    if a database-access error occurs
    */
   public void updateByte(int columnIndex,
      byte x)
      throws SQLException
   {
      doCheck(columnIndex);
      m_data[columnIndex - 1].set(m_rowIndex, new Byte(x));
   }
   
   /**
    * JDBC 2.0 Updates a column with a short value. The updateXXX methods
    * are used to update column values in the current row, or the insert
    * row. The updateXXX methods do not update the underlying database;
    * instead the updateRow or insertRow methods are called to update
    * the database.
    *
    * @param   columnIndex    the first column is 1, the second is 2, ...
    * 
    * @param   x            the new column value
    *
    * @exception     SQLException    if a database-access error occurs
    */
   public void updateShort(int columnIndex,
      short x)
      throws SQLException
   {
      doCheck(columnIndex);
      m_data[columnIndex - 1].set(m_rowIndex, new Short(x));
   }
   
   /**
    * JDBC 2.0 Updates a column with an integer value. The updateXXX methods
    * are used to update column values in the current row, or the insert
    * row. The updateXXX methods do not update the underlying database;
    * instead the updateRow or insertRow methods are called to update
    * the database.
    * 
    * @param   columnIndex    the first column is 1, the second is 2, ...
    *
    * @param   x            the new column value
    *
    * @exception     SQLException    if a database-access error occurs
    */
   public void updateInt(int columnIndex,
      int x)
      throws SQLException
   {
      doCheck(columnIndex);
      m_data[columnIndex - 1].set(m_rowIndex, new Integer(x));
   }
   
   /**
    * JDBC 2.0 Updates a column with a long value. The updateXXX methods
    * are used to update column values in the current row, or the insert
    * row. The updateXXX methods do not update the underlying database;
    * instead the updateRow or insertRow methods are called to update
    * the database.
    *
    * @param   columnIndex    the first column is 1, the second is 2, ...
    *
    * @param   x            the new column value
    *
    * @exception     SQLException    if a database-access error occurs
    */
   public void updateLong(int columnIndex,
      long x)
      throws SQLException
   {
      doCheck(columnIndex);
      m_data[columnIndex - 1].set(m_rowIndex, new Long(x));
   }
   
   /**
    * JDBC 2.0 Updates a column with a float value. The updateXXX methods
    * are used to update column values in the current row, or the insert
    * row. The updateXXX methods do not update the underlying database;
    * instead the updateRow or insertRow methods are called to update
    * the database.
    *
    * @param   columnIndex    the first column is 1, the second is 2, ...
    *
    * @param   x            the new column value
    *
    * @exception     SQLException    if a database-access error occurs
    */
   public void updateFloat(int columnIndex,
      float x)
      throws SQLException
   {
      doCheck(columnIndex);
      m_data[columnIndex - 1].set(m_rowIndex, new Float(x));
   }
   
   /**
    * JDBC 2.0 Updates a column with a Double value. The updateXXX methods
    * are used to update column values in the current row, or the insert
    * row. The updateXXX methods do not update the underlying database;
    * instead the updateRow or insertRow methods are called to update
    * the database.
    *
    * @param   columnIndex    the first column is 1, the second is 2, ...
    * 
    * @param   x            the new column value
    *
    * @exception     SQLException    if a database-access error occurs
    */
   public void updateDouble(int columnIndex,
      double x)
      throws SQLException
   {
      doCheck(columnIndex);
      m_data[columnIndex - 1].set(m_rowIndex, new Double(x));
   }
   
   /**
    * JDBC 2.0 Updates a column with a BigDecimal value. The updateXXX
    * methods are used to update column values in the current row, or the
    * insert row. The updateXXX methods do not update the underlying
    * database; instead the updateRow or insertRow methods are called
    * to update the database.
    * 
    * @param   columnIndex    the first column is 1, the second is 2, ...
    *
    * @param   x            the new column value
    *
    * @exception     SQLException    if a database-access error occurs
    */
   public void updateBigDecimal(int columnIndex,
      java.math.BigDecimal x)
      throws SQLException
   {
      doCheck(columnIndex);
      m_data[columnIndex - 1].set(m_rowIndex, x);
   }
   
   /**
    * JDBC 2.0 Updates a column with a String value. The updateXXX
    * methods are used to update column values in the current row,
    * or the insert row. The updateXXX methods do not update the
    * underlying database; instead the updateRow or insertRow methods
    * are called to update the database.
    *
    * @param   columnIndex    the first column is 1, the second is 2, ...
    *
    * @param   x            the new column value
    *
    * @exception     SQLException    if a database-access error occurs
    */
   public void updateString(int columnIndex,
      java.lang.String x)
      throws SQLException
   {
      doCheck(columnIndex);
      m_data[columnIndex - 1].set(m_rowIndex, x);
   }
   
   /**
    * JDBC 2.0 Updates a column with a byte array value. The updateXXX
    * methods are used to update column values in the current row, or
    * the insert row. The updateXXX methods do not update the underlying
    * database; instead the updateRow or insertRow methods are called to
    * update the database.
    *
    * @param   columnIndex    the first column is 1, the second is 2, ...
    *
    * @param   x            the new column value
    *
    * @exception     SQLException    if a database-access error occurs
    */
   public void updateBytes(int columnIndex,
      byte[] x)
      throws SQLException
   {
      if (true)
         throw new SQLException("updateBytes not supported");
   }
   
   /**
    * JDBC 2.0 Updates a column with a Date value. The updateXXX methods
    * are used to update column values in the current row, or the insert
    * row. The updateXXX methods do not update the underlying database;
    * instead the updateRow or insertRow methods are called to update the
    * database.
    *
    * @param   columnIndex    the first column is 1, the second is 2, ...
    *
    * @param   x            the new column value
    *
    * @exception     SQLException    if a database-access error occurs
    */
   public void updateDate(int columnIndex,
      java.sql.Date x)
      throws SQLException
   {
      doCheck(columnIndex);
      m_data[columnIndex - 1].set(m_rowIndex, x);
   }
   
   /**
    * JDBC 2.0 Updates a column with a Time value. The updateXXX methods
    * are used to update column values in the current row, or the insert
    * row. The updateXXX methods do not update the underlying database;
    * instead the updateRow or insertRow methods are called to update
    * the database.
    *
    * @param   columnIndex    the first column is 1, the second is 2, ...
    *
    * @param   x            the new column value
    *
    * @exception     SQLException    if a database-access error occurs
    */
   public void updateTime(int columnIndex,
      java.sql.Time x)
      throws SQLException
   {
      doCheck(columnIndex);
      m_data[columnIndex - 1].set(m_rowIndex, x);
   }
   
   /**
    * JDBC 2.0 Updates a column with a Timestamp value. The updateXXX
    * methods are used to update column values in the current row, or
    * the insert row. The updateXXX methods do not update the underlying
    * database; instead the updateRow or insertRow methods are called to
    * update the database.
    *
    * @param   columnIndex    the first column is 1, the second is 2, ...
    *
    * @param   x            the new column value
    *
    * @exception     SQLException    if a database-access error occurs
    */
   public void updateTimestamp(int columnIndex,
      java.sql.Timestamp x)
      throws SQLException
   {
      doCheck(columnIndex);
      m_data[columnIndex - 1].set(m_rowIndex, x);
   }
   
   /**
    * JDBC 2.0 Updates a column with an ascii stream value. The updateXXX
    * methods are used to update column values in the current row, or the
    * insert row. The updateXXX methods do not update the underlying
    * database; instead the updateRow or insertRow methods are called to
    * update the database.
    *
    * @param   columnIndex    the first column is 1, the second is 2, ...
    *
    * @param   x            the new column value
    *
    * @param   length         the length of the stream
    *
    * @exception     SQLException    if a database-access error occurs
    */
   public void updateAsciiStream(int columnIndex,
      java.io.InputStream x,
      int length)
      throws SQLException
   {
      if (true)
         throw new SQLException("updateAsciiStream not supported");
   }
   
   /**
    * JDBC 2.0 Updates a column with a binary stream value. The updateXXX
    * methods are used to update column values in the current row, or the
    * insert row. The updateXXX methods do not update the underlying
    * database; instead the updateRow or insertRow methods are called to
    * update the database.
    *
    * @param   columnIndex    the first column is 1, the second is 2, ...
    *
    * @param   x            the new column value
    *
    * @param   length         the length of the stream
    *
    * @exception     SQLException    if a database-access error occurs
    */
   public void updateBinaryStream(int columnIndex,
      java.io.InputStream x,
      int length)
      throws SQLException
   {
      if (true)
         throw new SQLException("updates not supported");
   }
   
   /**
    * JDBC 2.0 Updates a column with a character stream value. The
    * updateXXX methods are used to update column values in the current
    * row, or the insert row. The updateXXX methods do not update the
    * underlying database; instead the updateRow or insertRow methods
    * are called to update the database.
    *
    * @param   columnIndex    the first column is 1, the second is 2, ...
    *
    * @param   x            the new column value
    *
    * @param   length         the length of the stream
    *
    * @exception     SQLException    if a database-access error occurs
    */
   public void updateCharacterStream(int columnIndex,
      java.io.Reader x,
      int length)
      throws SQLException
   {
      if (true)
         throw new SQLException("updates not supported");
   }
   
   /**
    * JDBC 2.0 Updates a column with an Object value. The updateXXX
    * methods are used to update column values in the current row, or
    * the insert row. The updateXXX methods do not update the underlying
    * database; instead the updateRow or insertRow methods are called to
    * update the database.
    *
    * @param   columnIndex    the first column is 1, the second is 2, ...
    *
    * @param   x            the new column value
    * 
    * @param   scale         For java.sql.Types.DECIMAL or
    *                     java.sql.Types.NUMERIC types this is the
    *                     number of digits after the decimal. For all
    *                     other types this value will be ignored.
    *
    * @exception     SQLException    if a database-access error occurs
    */
   public void updateObject(int columnIndex,
      Object x,
      int scale)
      throws SQLException
   {
      // TODO: check if column type is DECIMAL or numeric, and
      // use the scale parameter accordingly
      doCheck(columnIndex);
      m_data[columnIndex - 1].set(m_rowIndex, x);
   }
   
   /**
    * JDBC 2.0 Updates a column with an Object value. The updateXXX
    * methods are used to update column values in the current row, or
    * the insert row. The updateXXX methods do not update the underlying
    * database; instead the updateRow or insertRow methods are called to
    * update the database.
    *
    * @param   columnIndex    the first column is 1, the second is 2, ...
    *
    * @param   x            the new column value
    *
    * @exception     SQLException    if a database-access error occurs
    */
   public void updateObject(int columnIndex,
      Object x)
      throws SQLException
   {
      doCheck(columnIndex);
      m_data[columnIndex - 1].set(m_rowIndex, x);
   }
   
   /**
    * JDBC 2.0 Updates a column with a null value. The updateXXX methods
    * are used to update column values in the current row, or the insert
    * row. The updateXXX methods do not update the underlying database;
    * instead the updateRow or insertRow methods are called to update
    * the database.
    *
    * @param   columnName      the name of the column
    *
    * @exception     SQLException    if a database-access error occurs
    */
   public void updateNull(java.lang.String columnName)
      throws SQLException
   {
      updateNull(findColumn(columnName));
   }
   
   /**
    * JDBC 2.0 Updates a column with a boolean value. The updateXXX methods
    * are used to update column values in the current row, or the insert
    * row. The updateXXX methods do not update the underlying database;
    * instead the updateRow or insertRow methods are called to update
    * the database.
    * 
    * @param   columnName      the name of the column
    *
    * @param   x            the new column value
    *
    * @exception     SQLException    if a database-access error occurs
    */
   public void updateBoolean(java.lang.String columnName,
      boolean x)
      throws SQLException
   {
      updateBoolean(findColumn(columnName), x);
   }
   
   /**
    * JDBC 2.0 Updates a column with a byte value. The updateXXX methods
    * are used to update column values in the current row, or the insert
    * row. The updateXXX methods do not update the underlying database;
    * instead the updateRow or insertRow methods are called to update
    * the database.
    *
    * @param   columnName      the name of the column
    *
    * @param   x            the new column value
    *
    * @exception     SQLException    if a database-access error occurs
    */
   public void updateByte(java.lang.String columnName,
      byte x)
      throws SQLException
   {
      updateByte(findColumn(columnName), x);
   }
   
   /**
    * JDBC 2.0 Updates a column with a short value. The updateXXX methods
    * are used to update column values in the current row, or the insert
    * row. The updateXXX methods do not update the underlying database;
    * instead the updateRow or insertRow methods are called to update
    * the database.
    *
    * @param   columnName      the name of the column
    * 
    * @param   x            the new column value
    *
    * @exception     SQLException    if a database-access error occurs
    */
   public void updateShort(java.lang.String columnName,
      short x)
      throws SQLException
   {
      updateShort(findColumn(columnName), x);
   }
   
   /**
    * JDBC 2.0 Updates a column with an integer value. The updateXXX methods
    * are used to update column values in the current row, or the insert
    * row. The updateXXX methods do not update the underlying database;
    * instead the updateRow or insertRow methods are called to update
    * the database.
    * 
    * @param   columnName      the name of the column
    *
    * @param   x            the new column value
    *
    * @exception     SQLException    if a database-access error occurs
    */
   public void updateInt(java.lang.String columnName,
      int x)
      throws SQLException
   {
      updateInt(findColumn(columnName), x);
   }
   
   /**
    * JDBC 2.0 Updates a column with a long value. The updateXXX methods
    * are used to update column values in the current row, or the insert
    * row. The updateXXX methods do not update the underlying database;
    * instead the updateRow or insertRow methods are called to update
    * the database.
    *
    * @param   columnName      the name of the column
    *
    * @param   x            the new column value
    *
    * @exception     SQLException    if a database-access error occurs
    */
   public void updateLong(java.lang.String columnName,
      long x)
      throws SQLException
   {
      updateLong(findColumn(columnName), x);
   }
   
   /**
    * JDBC 2.0 Updates a column with a float value. The updateXXX methods
    * are used to update column values in the current row, or the insert
    * row. The updateXXX methods do not update the underlying database;
    * instead the updateRow or insertRow methods are called to update
    * the database.
    *
    * @param   columnName      the name of the column
    *
    * @param   x            the new column value
    *
    * @exception     SQLException    if a database-access error occurs
    */
   public void updateFloat(java.lang.String columnName,
      float x)
      throws SQLException
   {
      updateFloat(findColumn(columnName), x);
   }
   
   /**
    * JDBC 2.0 Updates a column with a Double value. The updateXXX methods
    * are used to update column values in the current row, or the insert
    * row. The updateXXX methods do not update the underlying database;
    * instead the updateRow or insertRow methods are called to update
    * the database.
    *
    * @param   columnName      the name of the column
    * 
    * @param   x            the new column value
    *
    * @exception     SQLException    if a database-access error occurs
    */
   public void updateDouble(java.lang.String columnName,
      double x)
      throws SQLException
   {
      updateDouble(findColumn(columnName), x);
   }
   
   /**
    * JDBC 2.0 Updates a column with a BigDecimal value. The updateXXX
    * methods are used to update column values in the current row, or the
    * insert row. The updateXXX methods do not update the underlying
    * database; instead the updateRow or insertRow methods are called
    * to update the database.
    * 
    * @param   columnName      the name of the column
    *
    * @param   x            the new column value
    *
    * @exception     SQLException    if a database-access error occurs
    */
   public void updateBigDecimal(java.lang.String columnName,
      java.math.BigDecimal x)
      throws SQLException
   {
      updateBigDecimal(findColumn(columnName), x);
   }
   
   /**
    * JDBC 2.0 Updates a column with a String value. The updateXXX
    * methods are used to update column values in the current row,
    * or the insert row. The updateXXX methods do not update the
    * underlying database; instead the updateRow or insertRow methods
    * are called to update the database.
    *
    * @param   columnName      the name of the column
    *
    * @param   x            the new column value
    *
    * @exception     SQLException    if a database-access error occurs
    */
   public void updateString(java.lang.String columnName,
      java.lang.String x)
      throws SQLException
   {
      updateString(findColumn(columnName), x);
   }
   
   /**
    * JDBC 2.0 Updates a column with a byte array value. The updateXXX
    * methods are used to update column values in the current row, or
    * the insert row. The updateXXX methods do not update the underlying
    * database; instead the updateRow or insertRow methods are called to
    * update the database.
    *
    * @param   columnName      the name of the column
    *
    * @param   x            the new column value
    *
    * @exception     SQLException    if a database-access error occurs
    */
   public void updateBytes(java.lang.String columnName,
      byte[] x)
      throws SQLException
   {
      updateBytes(findColumn(columnName), x);
   }
   
   /**
    * JDBC 2.0 Updates a column with a Date value. The updateXXX methods
    * are used to update column values in the current row, or the insert
    * row. The updateXXX methods do not update the underlying database;
    * instead the updateRow or insertRow methods are called to update the
    * database.
    *
    * @param   columnName      the name of the column
    *
    * @param   x            the new column value
    *
    * @exception     SQLException    if a database-access error occurs
    */
   public void updateDate(java.lang.String columnName,
      java.sql.Date x)
      throws SQLException
   {
      updateDate(findColumn(columnName), x);
   }
   
   /**
    * JDBC 2.0 Updates a column with a Time value. The updateXXX methods
    * are used to update column values in the current row, or the insert
    * row. The updateXXX methods do not update the underlying database;
    * instead the updateRow or insertRow methods are called to update
    * the database.
    *
    * @param   columnName      the name of the column
    *
    * @param   x            the new column value
    *
    * @exception     SQLException    if a database-access error occurs
    */
   public void updateTime(java.lang.String columnName,
      java.sql.Time x)
      throws SQLException
   {
      updateTime(findColumn(columnName), x);
   }
   
   /**
    * JDBC 2.0 Updates a column with a Timestamp value. The updateXXX
    * methods are used to update column values in the current row, or
    * the insert row. The updateXXX methods do not update the underlying
    * database; instead the updateRow or insertRow methods are called to
    * update the database.
    *
    * @param   columnName      the name of the column
    *
    * @param   x            the new column value
    *
    * @exception     SQLException    if a database-access error occurs
    */
   public void updateTimestamp(java.lang.String columnName,
      java.sql.Timestamp x)
      throws SQLException
   {
      updateTimestamp(findColumn(columnName), x);
   }
   
   /**
    * JDBC 2.0 Updates a column with an ascii stream value. The updateXXX
    * methods are used to update column values in the current row, or the
    * insert row. The updateXXX methods do not update the underlying
    * database; instead the updateRow or insertRow methods are called to
    * update the database.
    *
    * @param   columnName      the name of the column
    *
    * @param   x            the new column value
    *
    * @param   length         the length of the stream
    *
    * @exception     SQLException    if a database-access error occurs
    */
   public void updateAsciiStream(java.lang.String columnName,
      java.io.InputStream x,
      int length)
      throws SQLException
   {
      updateAsciiStream(findColumn(columnName), x, length);
   }
   
   /**
    * JDBC 2.0 Updates a column with a binary stream value. The updateXXX
    * methods are used to update column values in the current row, or the
    * insert row. The updateXXX methods do not update the underlying
    * database; instead the updateRow or insertRow methods are called to
    * update the database.
    *
    * @param   columnName      the name of the column
    *
    * @param   x            the new column value
    *
    * @param   length         the length of the stream
    *
    * @exception     SQLException    if a database-access error occurs
    */
   public void updateBinaryStream(java.lang.String columnName,
      java.io.InputStream x,
      int length)
      throws SQLException
   {
      updateBinaryStream(findColumn(columnName), x, length);
   }
   
   /**
    * JDBC 2.0 Updates a column with a character stream value. The
    * updateXXX methods are used to update column values in the current
    * row, or the insert row. The updateXXX methods do not update the
    * underlying database; instead the updateRow or insertRow methods
    * are called to update the database.
    *
    * @param   columnName      the name of the column
    *
    * @param   x            the new column value
    *
    * @param   length         the length of the stream
    *
    * @exception     SQLException    if a database-access error occurs
    */
   public void updateCharacterStream(java.lang.String columnName,
      java.io.Reader x,
      int length)
      throws SQLException
   {
      updateCharacterStream(findColumn(columnName), x, length);
   }
   
   /**
    * JDBC 2.0 Updates a column with an Object value. The updateXXX
    * methods are used to update column values in the current row, or
    * the insert row. The updateXXX methods do not update the underlying
    * database; instead the updateRow or insertRow methods are called to
    * update the database.
    *
    * @param   columnName      the name of the column
    *
    * @param   x            the new column value
    * 
    * @param   scale         For java.sql.Types.DECIMAL or
    *                     java.sql.Types.NUMERIC types this is the
    *                     number of digits after the decimal. For all
    *                     other types this value will be ignored.
    *
    * @exception     SQLException    if a database-access error occurs
    */
   public void updateObject(java.lang.String columnName,
      Object x,
      int scale)
      throws SQLException
   {
      updateObject(findColumn(columnName), x, scale);
   }
   
   /**
    * JDBC 2.0 Updates a column with an Object value. The updateXXX
    * methods are used to update column values in the current row, or
    * the insert row. The updateXXX methods do not update the underlying
    * database; instead the updateRow or insertRow methods are called to
    * update the database.
    *
    * @param   columnName      the name of the column
    *
    * @param   x            the new column value
    *
    * @exception     SQLException    if a database-access error occurs
    */
   public void updateObject(java.lang.String columnName,
      Object x)
      throws SQLException
   {
      updateObject(findColumn(columnName), x);
   }
   
   /**
    * JDBC 2.0 Inserts the contents of the insert row into the result set
    * and the database. Must be on the insert row when this method is
    * called.
    *
    * @exception     SQLException    if a database access error occurs,
    *                         if called when not on the insert row,
    *                         or if not all of non-nullable columns in
    *                         the insert row have been given a value
    */
   public void insertRow()
      throws SQLException
   {
      if (true)
         throw new SQLException("insertion not supported");
   }
   
   /**
    * JDBC 2.0 Updates the underlying database with the new contents of the
    * current row. Cannot be called when on the insert row.
    *
    * @exception     SQLException    if a database access error occurs or
    *                         if called when on the insert row
    */
   public void updateRow()
      throws SQLException
   {
      if (true)
         throw new SQLException("updates not supported");
   }
   
   /**
    * JDBC 2.0 Deletes the current row from the result set and the
    * underlying database. Cannot be called when on the insert row.
    *
    * @exception     SQLException    if a database access error occurs or
    *                         if called when on the insert row.
    */
   public void deleteRow()
      throws SQLException
   {
      if (true)
         throw new SQLException("delete not supported");
   }
   
   /**
    * JDBC 2.0 Refreshes the current row with its most recent value in
    * the database. Cannot be called when on the insert row. The refreshRow
    * method provides a way for an application to explicitly tell the JDBC
    * driver to refetch a row(s) from the database. An application may want
    * to call refreshRow when caching or prefetching is being done by the
    * JDBC driver to fetch the latest value of a row from the database. The
    * JDBC driver may actually refresh multiple rows at once if the fetch
    * size is greater than one. All values are refetched subject to the
    * transaction isolation level and cursor sensitivity. If refreshRow
    * is called after calling updateXXX, but before calling updateRow,
    * then the updates made to the row are lost. Calling the method
    * refreshRow frequently will likely slow performance.
    *
    * @exception     SQLException    if a database access error occurs or
    *                         if called when on the insert row
    */
   public void refreshRow()
      throws SQLException
   {
   }
   
   /**
    * JDBC 2.0 Cancels the updates made to a row. This method may be called
    * after calling an updateXXX method(s) and before calling updateRow to
    * rollback the updates made to a row. If no updates have been made or
    * updateRow has already been called, then this method has no effect.
    *
    * @exception     SQLException    if a database access error occurs or
    *                         if called when on the insert row
    */
   public void cancelRowUpdates()
      throws SQLException
   {
   }

   /**
    * JDBC 2.0 Moves the cursor to the insert row. The current cursor
    * position is remembered while the cursor is positioned on the insert
    * row. The insert row is a special row associated with an updatable
    * result set. It is essentially a buffer where a new row may be
    * constructed by calling the updateXXX methods prior to inserting the
    * row into the result set. Only the updateXXX, getXXX, and insertRow
    * methods may be called when the cursor is on the insert row. All of
    * the columns in a result set must be given a value each time this
    * method is called before calling insertRow. The method updateXXX
    * must be called before a getXXX method can be called on a column value.
    *
    * @exception     SQLException    if a database access error occurs or
    *                         the result set is not updatable
    */
   public void moveToInsertRow()
      throws SQLException
   {
   }
   
   /**
    * JDBC 2.0 Moves the cursor to the remembered cursor position,
    * usually the current row. This method has no effect if the cursor
    * is not on the insert row.
    *
    * @exception     SQLException    if a database access error occurs or
    *                         the result set is not updatable
    */
   public void moveToCurrentRow()
      throws SQLException
   {
   }
   
   /**
    * JDBC 2.0 Returns the Statement that produced this ResultSet object.
    * If the result set was generated some other way, such as by a
    * DatabaseMetaData method, this method returns null.
    *
    * @return   the Statment that produced the result set or <code>null</code>
    *         if the result set was produced some other way
    *
    * @exception     SQLException    if a database access error occurs
    */
   public java.sql.Statement getStatement()
      throws SQLException
   {
      return null;
   }
   
   /**
    * JDBC 2.0 Returns the value of a column in the current row as a
    * Java object. This method uses the given Map object for the custom
    * mapping of the SQL structured or distinct type that is being retrieved.
    *
    * @param   i            the first column is 1, the second is 2, ...
    *
    * @param   map          the mapping from SQL type names to Java classes
    *
    * @return   an object representing the SQL value
    *
    * @exception     SQLException    if a database access error occurs
    */
   public Object getObject(int i,
         Map<String, Class<?>> map)
      throws SQLException
   {
      if (true)
         throw new SQLException("custom mapping not supported");
      return null;
   }
   
   /**
    * JDBC 2.0 Gets a REF(<structured-type>) column value from the current
    * row.
    *
    * @param   i            the first column is 1, the second is 2, ...
    *
    * @return   a Ref object representing an SQL REF value
    *
    * @exception     SQLException    if a database access error occurs
    */
   public java.sql.Ref getRef(int i)
      throws SQLException
   {
      if (true)
         throw new SQLException("SQL REF not supported");
      return null;
   }
   
   /**
    * JDBC 2.0 Gets a BLOB value in the current row of this ResultSet object.
    *
    * @param   i            the first column is 1, the second is 2, ...
    *
    * @return   a Blob object representing the SQL BLOB value in the
    *         specified column
    *
    * @exception     SQLException    if a database access error occurs
    */
   public java.sql.Blob getBlob(int i)
      throws SQLException
   {
      if (true)
         throw new SQLException("SQL Blob not supported");
      return null;
   }
   
   /**
    * JDBC 2.0 Gets a CLOB value in the current row of this ResultSet object.
    *
    * @param   i            the first column is 1, the second is 2, ...
    *
    * @return   a Clob object representing the SQL CLOB value in the
    *         specified column
    *
    * @exception     SQLException    if a database access error occurs
    */
   public java.sql.Clob getClob(int i)
      throws SQLException
   {
      if (true)
         throw new SQLException("SQL Clob not supported");
      return null;
   }
   
   /**
    * JDBC 2.0 Gets an SQL ARRAY value from the current row of this
    * ResultSet object.
    *
    * @param   i            the first column is 1, the second is 2, ...
    *
    * @return   an Array object representing the SQL ARRAY value in the
    *         specified column
    *
    * @exception     SQLException    if a database access error occurs
    */
   public java.sql.Array getArray(int i)
      throws SQLException
   {
      if (true)
         throw new SQLException("SQL Array not supported");
      return null;
   }
   
   /**
    * JDBC 2.0 Returns the value in the specified column as a Java object.
    * This method uses the specified Map object for custom mapping if
    * appropriate.
    *
    * @param   colName       the name of the column from which to retrieve
    *                     the value
    *
    * @param   map          the mapping from SQL type names to Java classes
    *
    * @return   an object representing the SQL value in the specified column
    *
    * @exception     SQLException    if a database access error occurs
    */
   public Object getObject(java.lang.String colName,
         Map<String, Class<?>> map)
      throws SQLException
   {
      return getObject(findColumn(colName), map);
   }
   
   /**
    * JDBC 2.0 Gets a REF(<structured-type>) column value from the current
    * row.
    *
    * @param   colName       the column name
    *
    * @return   a Ref object representing the SQL REF value in the
    *         specified column
    *
    * @exception     SQLException    if a database access error occurs
    */
   public java.sql.Ref getRef(java.lang.String colName)
      throws SQLException
   {
      return getRef(findColumn(colName));
   }
   
   /**
    * JDBC 2.0 Gets a BLOB value in the current row of this ResultSet object.
    *
    * @param   colName       the name of the column from which to retrieve
    *                     the value
    *
    * @return   a Blob object representing the SQL BLOB value in the
    *         specified column
    *
    * @exception     SQLException    if a database access error occurs
    */
   public java.sql.Blob getBlob(java.lang.String colName)
      throws SQLException
   {
      return getBlob(findColumn(colName));
   }
   
   /**
    * JDBC 2.0 Gets a CLOB value in the current row of this ResultSet object.
    *
    * @param   colName       the name of the column from which to retrieve
    *                     the value
    *
    * @return   a Clob object representing the SQL CLOB value in the
    *         specified column
    *
    * @exception     SQLException    if a database access error occurs
    */
   public java.sql.Clob getClob(java.lang.String colName)
      throws SQLException
   {
      return getClob(findColumn(colName));
   }
   
   /**
    * JDBC 2.0 Gets an SQL ARRAY value in the current row of this ResultSet
    * object.
    *
    * @param   colName       the name of the column from which to retrieve
    *                     the value
    *
    * @return   an Array object representing the SQL ARRAY value in the
    *         specified column
    *
    * @exception     SQLException    if a database access error occurs
    */
   public java.sql.Array getArray(java.lang.String colName)
      throws SQLException
   {
      return getArray(findColumn(colName));
   }
   
   /**
    * JDBC 2.0 Gets the value of a column in the current row as a
    * java.sql.Date object. This method uses the given calendar to
    * construct an appropriate millisecond value for the Date if the
    * underlying database does not store timezone information.
    *
    * @param   columnIndex    the first column is 1, the second is 2, ...
    *
    * @param   cal          the calendar to use in constructing the date
    *
    * @return   the column value; if the value is SQL NULL, the result is
    *         <code>null</code>
    *
    * @exception     SQLException    if a database access error occurs
    */
   public java.sql.Date getDate( int columnIndex,
      java.util.Calendar cal)
      throws SQLException
   {
      return null;
   }
   
   /**
    * JDBC 2.0 Gets the value of a column in the current row as a
    * java.sql.Date object. This method uses the given calendar to
    * construct an appropriate millisecond value for the Date, if the
    * underlying database does not store timezone information.
    *
    * @param   columnName   the SQL name of the column from which to retrieve
    *                  the value
    *
    * @param   cal       the calendar to use in constructing the date
    *
    * @return   the column value; if the value is SQL NULL, the result is
    *         <code>null</code>
    *
    * @exception     SQLException    if a database access error occurs
    */
   public java.sql.Date getDate( java.lang.String columnName,
      java.util.Calendar cal)
      throws SQLException
   {
      return null;
   }
   
   /**
    * JDBC 2.0 Gets the value of a column in the current row as a
    * java.sql.Time object. This method uses the given calendar to
    * construct an appropriate millisecond value for the Time, if the
    * underlying database does not store timezone information.
    *
    * @param   columnIndex    the first column is 1, the second is 2, ...
    *
    * @param   cal          the calendar to use in constructing the time
    *
    * @return   the column value; if the value is SQL NULL, the result is
    *         <code>null</code>
    *
    * @exception     SQLException    if a database access error occurs
    */
   public java.sql.Time getTime( int columnIndex,
      java.util.Calendar cal)
      throws SQLException
   {
      return null;
   }
   
   /**
    * JDBC 2.0 Gets the value of a column in the current row as a
    * java.sql.Time object. This method uses the given calendar to
    * construct an appropriate millisecond value for the Time, if the
    * underlying database does not store timezone information.
    *
    * @param   columnName   the SQL name of the column from which to retrieve
    *                  the value
    *
    * @param   cal       the calendar to use in constructing the time
    *
    * @return   the column value; if the value is SQL NULL, the result is
    *         <code>null</code>
    *
    * @exception     SQLException    if a database access error occurs
    */
   public java.sql.Time getTime( java.lang.String columnName,
      java.util.Calendar cal)
      throws SQLException
   {
      return null;
   }
   
   /**
    * JDBC 2.0 Gets the value of a column in the current row as a
    * java.sql.Timestamp object. This method uses the given calendar to
    * construct an appropriate millisecond value for the Timestamp, if the
    * underlying database does not store timezone information.
    *
    * @param   columnIndex    the first column is 1, the second is 2, ...
    *
    * @param   cal          the calendar to use in constructing the
    *                     timestamp
    *
    * @return   the column value; if the value is SQL NULL, the result is
    *         <code>null</code>
    *
    * @exception     SQLException    if a database access error occurs
    */
   public java.sql.Timestamp getTimestamp(int columnIndex,
      java.util.Calendar cal)
      throws SQLException
   {
      return null;
   }
   
   /**
    * JDBC 2.0 Gets the value of a column in the current row as a
    * java.sql.Timestamp object. This method uses the given calendar to
    * construct an appropriate millisecond value for the Timestamp, if the
    * underlying database does not store timezone information.
    *
    * @param   columnName   the SQL name of the column from which to retrieve
    *                  the value
    *
    * @param   cal       the calendar to use in constructing the timestamp
    *
    * @return   the column value; if the value is SQL NULL, the result is
    *         <code>null</code>
    *
    * @exception     SQLException    if a database access error occurs
    */
   public java.sql.Timestamp getTimestamp(java.lang.String columnName,
      java.util.Calendar cal)
      throws SQLException
   {
      return null;
   }

   
   /**
    * @author   chadloder
    * 
    * @version 1.9 1999/05/24
    * 
    * Appends a null row at the end of the result set.
    * This row's value can then be set by calling setRow(int) with the
    * return value from this method.
    *
    * @throws SQLException If no data has been set
    *
    * @return int The index of the newly added row.
    */
   public int appendNullRow()
      throws SQLException
   {
      if (m_data == null || m_data.length < 1)
      {
         throw new SQLException("no data available");
      }

      for (int i = 0; i < m_data.length; i++)
      {
         m_data[i].add(null);
      }

      m_numRows++;

      return m_data[0].size() - 1; // 0-based
   }

   public void setBeforeFirst() throws SQLException
   {
      m_rowIndex = -1;
   }

   public void setAfterLast() throws SQLException
   {
      m_rowIndex = m_numRows;
   }

   /**
    * @author   chadloder
    * 
    * @version 1.9 1999/05/24
    * 
    * Sets the row cursor to point to a given valid row. This method
    * may NOT be used to set the row cursor to before the first row
    * or after the last row.
    * 
    * @param   rowIdx   The row index, 0-based
    * 
    * @throws   SQLException If setting the row index would cause the row
    * cursor to be before the first row or after the last row. If an
    * exception is thrown, the row index will retain the value it had
    * before this method was called.
    * 
    */
   public void setRow(int rowIdx) throws SQLException
   {
      int oldRowIndex = m_rowIndex;
      m_rowIndex = rowIdx;

      if (isBeforeFirst())
      {
         m_rowIndex = oldRowIndex;
         throw new SQLException("row cursor is positioned before first row");
      }
      if (isAfterLast())
      {
         m_rowIndex = oldRowIndex;
         throw new SQLException("row cursor is positioned after last row");
      }
   }

   /**
    * Ensure that close() is called during garbage collection
    */
   protected void finalize() throws Throwable
   {
      try
      {
         close();
      }
      catch (SQLException e)
      {
         // EXCEPTION IGNORED : our implementation of close() does not throw
         // any exceptions, and we wouldn't be able to do anything about it
         // anyways
         PSConsole.printMsg("Data", e);
      }
      super.finalize();
   }
   
   /**
    * Sets the result data. Note that if two different columns have the same
    * exact data (as with "SELECT name N, name M from MyTable"), instead of
    * having two names map to the same column index, it is better to set two
    * entries in data[] to refer to the same List object.
    *
    * Note that SQL column numbers are 1-based, which means that to map the name
    * "foo" to the List in data[0], you should put an entry in the map which
    * maps "foo" to Integer(1)
    */
   void setResultData(List[] data, java.util.HashMap nameToIndexMap)
      throws IllegalArgumentException
   {
      if (data == null)
         throw new IllegalArgumentException("result data == null");
      if (nameToIndexMap == null)
         throw new IllegalArgumentException("name to index map == null");
      m_numRows = data[0].size();
      m_data = data;
      m_rowIndex = -1;
      m_nameToIndexMap = nameToIndexMap ;
   }

   /**
    * @author   chadloder
    * 
    * @version 1.9 1999/05/24
    * 
    * Renames a column.
    * 
    * @param   oldName   The old name of the column.
    * @param   newName   The new name of the column.
    * 
    * @throws   SQLException if there is no column with the
    * name <CODE>oldName</CODE>, or if there is already a
    * column with the name <CODE>newName</CODE>. If an
    * exception is thrown, the result set remains unchanged.
    */
   void renameColumn(String oldName, String newName)
      throws SQLException
   {
      Integer ord = (Integer)m_nameToIndexMap.remove(oldName);
      if (ord == null)
         throw new SQLException("No column named " + oldName);

      // make sure we don't obliterate an existing column
      if (null != m_nameToIndexMap.get(newName))
      {
         m_nameToIndexMap.put(oldName, ord);
         throw new SQLException("A column named " + newName
            + " already exists.");
      }

      m_nameToIndexMap.put(newName, ord);
   }

   public Map getColumnNames()
   {
      return Collections.unmodifiableMap(m_nameToIndexMap);
   }

   // checks to see if it's ok to read from the given column
   private void doCheck(int columnIndex) throws SQLException
   {
      if (!m_isOpen)
         throw new SQLException("attempt to get data from a closed result set");
      if (columnIndex > m_data.length)
         throw new SQLException("column index out of range: " + columnIndex +
            "; number of columns is " + m_data.length);
      if (isBeforeFirst())
         throw new SQLException("row cursor is positioned before first row");
      if (isAfterLast())
         throw new SQLException("row cursor is positioned after last row");
   }

   public List getColumnData(int ordinal) throws SQLException
   {
      if (!m_isOpen)
         throw new SQLException("attempt to get data from a closed result set");
      if (ordinal > m_data.length)
         throw new SQLException("column index out of range: " + ordinal +
            "; number of columns is " + m_data.length);
      return m_data[ordinal - 1];
   }

   public List getColumnData(String name) throws SQLException
   {
      int ord = findColumn(name);
      return getColumnData(ord);
   }

   public void setMetaData(PSResultSetMetaData meta) throws SQLException
   {
      m_metaData = meta;
      int colCount = meta.getColumnCount();
      List[] data = new List[colCount];
      java.util.HashMap nameMap = new java.util.HashMap();
      for (int colNo = 1; colNo <= colCount; colNo++)
      {
         data[colNo-1] = new java.util.ArrayList();
         nameMap.put(meta.getColumnName(colNo), new Integer(colNo));
      }

      setResultData(data, nameMap);
   }

   public void addRow(Object[] rowData) throws SQLException
   {
      int rowIdx = appendNullRow();
      setRow(rowIdx);
      for (int i = 0; i < rowData.length; i++)
      {
         updateObject(i + 1, rowData[i]);
      }
   }

   public Object[] getRowBuffer() throws SQLException
   {
      Object[] o = new Object[m_data.length];
      for (int i = 0; i < o.length; i++)
      {
         o[i] = getObject(i+1);
      }
      return o;
   }

   int getNumRows()
   {
      return m_numRows;
   }

   /** the current row index (element index into the Lists, 0-based) */
   private int m_rowIndex = -1;
   
   /** the number of rows (the number of elements in each List) */
   private int m_numRows = 0;
   
   /** true if the last object returned from a getXXX method was SQL null */
   private boolean m_wasNull = false;
   
   /** the fetch direction (forward only for now) */
   private int m_fetchDirection = FETCH_FORWARD;
   
   /** the result set type */
   private int m_resultSetType = TYPE_SCROLL_INSENSITIVE ;
   
   /** true if this result set is open */
   private boolean m_isOpen;
   
   /** the column name to column number map */
   private java.util.HashMap m_nameToIndexMap;
   
   /** an array of Lists, one List for each column, one List element
    * for each row. Each List will have the same number of elements
    * (equal to the number of rows in this result set)
    */
   private List[] m_data;

   /** the meta data for this result set */
   private PSResultSetMetaData m_metaData;
   
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
