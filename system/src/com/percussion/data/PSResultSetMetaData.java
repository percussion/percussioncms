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

import java.lang.reflect.Method;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;


/**
 * The PSResultSetMetaData class extends the JDBC ResultSetMetaData class,
 * providing meta data support for the PSResultSet class.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSResultSetMetaData implements java.sql.ResultSetMetaData {
   /**
    * Construct an empty result set meta data object. Be sure to add
    * columns to theis object with addResultSetMetaData or addColumnMetaData.
    *
    * @see         #addResultSetMetaData
    * @see         #addColumnMetaData
    */
   public PSResultSetMetaData()
   {
      super();
      m_columnCount = 0;
      m_columns = new ArrayList<>();
   }

   /**
    * Construct a result set meta data from the specified meta data.
    *
    * @param      meta            the ResultSetMetaData object to copy
    *
    * @exception   SQLException   if a SQL error occurs
    */
   public PSResultSetMetaData(ResultSetMetaData meta)
      throws SQLException
   {
      this();
      addResultSetMetaData(meta);
   }

   /**
    * Copy the result set's meta data definitions into this object.
    *
    * @param      meta            the ResultSetMetaData object to copy
    *
    * @exception   SQLException   if a SQL error occurs
    */
   public void addResultSetMetaData(ResultSetMetaData meta)
      throws SQLException
   {
      /* Only JDBC 2.0+ drivers support getColumnClassName so we must see
       * if it's supported and use it only then. Attempting to use it on
       * any meta data object may cause NullPointerException,
       * AbstractMethodError or even hard system crashes
       * (bug id Rx-99-11-0043)
       */
      Method getColumnClassNameMethod = null;
      try {
         Class<? extends ResultSetMetaData> cl = meta.getClass();
         getColumnClassNameMethod = cl.getDeclaredMethod(
            "getColumnClassName", new Class[] { Integer.TYPE } );
      } catch (Throwable t) {
         // we will simply ignore this (it's not that useful anyway)
      }

      int colCount = meta.getColumnCount();

      PSResultSetColumnMetaData col;
      String name;
      int type, size;
      for (int colNo = 1; colNo <= colCount; colNo++) {
         name = meta.getColumnName(colNo);
         type = meta.getColumnType(colNo);
         size = meta.getColumnDisplaySize(colNo);

         col = new PSResultSetColumnMetaData(name, type, size);
         m_columns.add(col);

         col.setAutoIncrement(meta.isAutoIncrement(colNo));

         col.setCaseSensitive(meta.isCaseSensitive(colNo));
         col.setSearchable(meta.isSearchable(colNo));
         col.setCurrency(meta.isCurrency(colNo));
         col.setNullable(meta.isNullable(colNo));
         col.setSigned(meta.isSigned(colNo));
         col.setReadOnly(meta.isReadOnly(colNo));
         col.setWritable(meta.isWritable(colNo));
         col.setDefinitelyWritable(meta.isDefinitelyWritable(colNo));
         if(type != Types.CLOB && type != Types.BLOB)
            col.setPrecision(meta.getPrecision(colNo));
         col.setScale(meta.getScale(colNo));
         col.setColumnTypeName(meta.getColumnTypeName(colNo));
         col.setColumnLabel(meta.getColumnLabel(colNo));

         /* some (rather stupid) drivers (like Sybase) throw an exception
          * as they do not support various calls rather than a nice simple
          * null like most drivers
          * See bug id Rx-99-11-0046
          */
         try {
            col.setCatalogName(meta.getCatalogName(colNo));
         } catch (SQLException e) {
            // guess we're ignoring these due to above reasoning
            col.setCatalogName("");
         }
         catch (UnsupportedOperationException e) {
            // ignore these due to above reasoning
            col.setCatalogName("");
         }

         try {
            col.setSchemaName(meta.getSchemaName(colNo));
         } catch (SQLException e) {
            // guess we're ignoring these due to above reasoning
            col.setSchemaName("");
         }
         catch (UnsupportedOperationException e) {
            // ignore these due to above reasoning
            col.setSchemaName("");
         }

         try {
            col.setTableName(meta.getTableName(colNo));
         } catch (SQLException e) {
            // guess we're ignoring these due to above reasoning
            col.setTableName("");
         }
         catch (UnsupportedOperationException e) {
            // ignore these due to above reasoning
            col.setTableName("");
         }

         // only get class name if supported
         if (getColumnClassNameMethod != null)
         {
            try {
               col.setColumnClassName(meta.getColumnClassName(colNo));
            } catch (Throwable t) {
               /* we are ignoring this as most drivers do not support it and
                * give back a wide range of errors including
                * NullPointerException and UnsupportedOperationException.
                * We will simply ignore this and disable further attempts
                * to make the call.
                */
               col.setColumnClassName("");
               getColumnClassNameMethod = null;
            }
         }
         else
         {
            col.setColumnClassName("");
         }
      }

      m_columnCount += colCount;
   }

   /**
    * Add the column meta data definition to this object.
    *
    * @param      col            the PSResultSetColumnMetaData object to add
    *                                          (not copied)
    */
   public void addColumnMetaData(PSResultSetColumnMetaData col)
   {
      m_columns.add(col);
      m_columnCount++;
   }

   /**
    * Remove a column from the meta data.
    *
    * @param pos the 1 based position of the column
    */
   void removeColumnMetaData(int pos)
   {
      m_columns.remove(pos-1);
      m_columnCount--;
   }

   /**
    * What's the number of columns in the ResultSet?
    *
    * @return                    the number
    *
    * @exception SQLException    if a database-access error occurs.
    */
   public int getColumnCount()
      throws SQLException
   {
      return m_columnCount;
   }

   /**
    * Is the column automatically numbered, thus read-only?
    *
    * @param   column            the first column is 1, the second is 2, ...
    *
    * @return                    <code>true</code> if so
    *
    * @exception SQLException    if a database-access error occurs.
    */
   public boolean isAutoIncrement(int column)
      throws SQLException
   {
      if (column > m_columnCount)
         throw new SQLException("Specified column number (" + column + ") is out of range (" + m_columnCount + ").");

      return (m_columns.get(column-1)).isAutoIncrement();
   }

   /**
    * Does a column's case matter?
    *
    * @param   column            the first column is 1, the second is 2, ...
    *
    * @return                    <code>true</code> if so
    *
    * @exception SQLException    if a database-access error occurs.
    */
   public boolean isCaseSensitive(int column)
      throws SQLException
   {
      if (column > m_columnCount)
         throw new SQLException("Specified column number (" + column + ") is out of range (" + m_columnCount + ").");

      return (m_columns.get(column-1)).isCaseSensitive();
   }

   /**
    * Can the column be used in a where clause?
    *
    * @param   column            the first column is 1, the second is 2, ...
    *
    * @return                    <code>true</code> if so
    *
    * @exception SQLException    if a database-access error occurs.
    */
   public boolean isSearchable(int column)
      throws SQLException
   {
      if (column > m_columnCount)
         throw new SQLException("Specified column number (" + column + ") is out of range (" + m_columnCount + ").");

      return (m_columns.get(column-1)).isSearchable();
   }

   /**
    * Is the column a cash value?
    *
    * @param   column            the first column is 1, the second is 2, ...
    *
    * @return                    <code>true</code> if so
    *
    * @exception SQLException    if a database-access error occurs.
    */
   public boolean isCurrency(int column)
      throws SQLException
   {
      if (column > m_columnCount)
         throw new SQLException("Specified column number (" + column + ") is out of range (" + m_columnCount + ").");

      return (m_columns.get(column-1)).isCurrency();
   }

   /**
    * Can you put a NULL in this column?
    *
    * @param   column            the first column is 1, the second is 2, ...
    *
    * @return                    columnNoNulls, columnNullable or
    *                            columnNullableUnknown
    *
    * @exception SQLException    if a database-access error occurs.
    */
   public int isNullable(int column)
      throws SQLException
   {
      if (column > m_columnCount)
         throw new SQLException("Specified column number (" + column + ") is out of range (" + m_columnCount + ").");

      return (m_columns.get(column-1)).isNullable();
   }

   /**
    * Is the column a signed number?
    *
    * @param   column            the first column is 1, the second is 2, ...
    *
    * @return                    <code>true</code> if so
    *
    * @exception SQLException    if a database-access error occurs.
    */
   public boolean isSigned(int column)
      throws SQLException
   {
      if (column > m_columnCount)
         throw new SQLException("Specified column number (" + column + ") is out of range (" + m_columnCount + ").");

      return (m_columns.get(column-1)).isSigned();
   }

   /**
    * What's the column's normal max width in chars?
    *
    * @param   column            the first column is 1, the second is 2, ...
    *
    * @return                    max width
    *
    * @exception SQLException    if a database-access error occurs.
    */
   public int getColumnDisplaySize(int column)
      throws SQLException
   {
      if (column > m_columnCount)
         throw new SQLException("Specified column number (" + column + ") is out of range (" + m_columnCount + ").");

      return (m_columns.get(column-1)).getColumnDisplaySize();
   }

   /**
    * What's the suggested column title for use in printouts and displays?
    *
    * @param   column            the first column is 1, the second is 2, ...
    *
    * @return                    suggested column title
    *
    * @exception SQLException    if a database-access error occurs.
    */
   public String getColumnLabel(int column)
      throws SQLException
   {
      if (column > m_columnCount)
         throw new SQLException("Specified column number (" + column + ") is out of range (" + m_columnCount + ").");

      return (m_columns.get(column-1)).getColumnLabel();
   }

   /**
    * What's a column's name?
    *
    * @param   column            the first column is 1, the second is 2, ...
    *
    * @return                    column name
    *
    * @exception SQLException    if a database-access error occurs.
    */
   public String getColumnName(int column)
      throws SQLException
   {
      if (column > m_columnCount)
         throw new SQLException("Specified column number (" + column + ") is out of range (" + m_columnCount + ").");

      return (m_columns.get(column-1)).getColumnName();
   }

   /**
    * What's a column's table's schema?
    *
    * @param   column            the first column is 1, the second is 2, ...
    *
    * @return                    schema name or "" if not applicable
    *
    * @exception SQLException    if a database-access error occurs.
    */
   public String getSchemaName(int column)
      throws SQLException
   {
      if (column > m_columnCount)
         throw new SQLException("Specified column number (" + column + ") is out of range (" + m_columnCount + ").");

      return (m_columns.get(column-1)).getSchemaName();
   }

   /**
    * What's a column's number of decimal digits?
    *
    * @param   column            the first column is 1, the second is 2, ...
    *
    * @return                    precision
    *
    * @exception SQLException    if a database-access error occurs.
    */
   public int getPrecision(int column)
      throws SQLException
   {
      if (column > m_columnCount)
         throw new SQLException("Specified column number (" + column + ") is out of range (" + m_columnCount + ").");

      return (m_columns.get(column-1)).getPrecision();
   }

   /**
    * What's a column's number of digits to right of the decimal point?
    *
    * @param   column            the first column is 1, the second is 2, ...
    *
    * @return                    scale
    *
    * @exception SQLException    if a database-access error occurs.
    */
   public int getScale(int column)
      throws SQLException
   {
      if (column > m_columnCount)
         throw new SQLException("Specified column number (" + column + ") is out of range (" + m_columnCount + ").");

      return (m_columns.get(column-1)).getScale();
   }

   /**
    * What's a column's table name?
    *
    * @param   column            the first column is 1, the second is 2, ...
    *
    * @return                    table name or "" if not applicable
    *
    * @exception SQLException    if a database-access error occurs.
    */
   public String getTableName(int column)
      throws SQLException
   {
      if (column > m_columnCount)
         throw new SQLException("Specified column number (" + column + ") is out of range (" + m_columnCount + ").");

      return (m_columns.get(column-1)).getTableName();
   }

   /**
    * What's a column's table's catalog name?
    *
    * @param   column            the first column is 1, the second is 2, ...
    *
    * @return                    column name or "" if not applicable.
    *
    * @exception SQLException    if a database-access error occurs.
    */
   public String getCatalogName(int column)
      throws SQLException
   {
      if (column > m_columnCount)
         throw new SQLException("Specified column number (" + column + ") is out of range (" + m_columnCount + ").");

      return (m_columns.get(column-1)).getCatalogName();
   }

   /**
    * What's a column's SQL type? See java.sql.Types for possible types.
    *
    * @param   column            the first column is 1, the second is 2, ...
    *
    * @return                    SQL type
    *
    * @exception SQLException    if a database-access error occurs.
    */
   public int getColumnType(int column)
      throws SQLException
   {
      if (column > m_columnCount)
         throw new SQLException("Specified column number (" + column + ") is out of range (" + m_columnCount + ").");

      return (m_columns.get(column-1)).getColumnType();
   }

   /**
    * What's a column's data source specific type name?
    *
    * @param   column            the first column is 1, the second is 2, ...
    *
    * @return                    type name
    *
    * @exception SQLException    if a database-access error occurs.
    */
   public String getColumnTypeName(int column)
      throws SQLException
   {
      if (column > m_columnCount)
         throw new SQLException("Specified column number (" + column + ") is out of range (" + m_columnCount + ").");

      return (m_columns.get(column-1)).getColumnTypeName();
   }

   /**
    * Is a column definitely not writable?
    *
    * @param   column            the first column is 1, the second is 2, ...
    *
    * @return                    <code>true</code> if so
    *
    * @exception SQLException    if a database-access error occurs.
    */
   public boolean isReadOnly(int column)
      throws SQLException
   {
      if (column > m_columnCount)
         throw new SQLException("Specified column number (" + column + ") is out of range (" + m_columnCount + ").");

      return (m_columns.get(column-1)).isReadOnly();
   }

   /**
    * Is it possible for a write on the column to succeed?
    *
    * @param   column            the first column is 1, the second is 2, ...
    *
    * @return                    <code>true</code> if so
    *
    * @exception SQLException    if a database-access error occurs.
    */
   public boolean isWritable(int column)
      throws SQLException
   {
      if (column > m_columnCount)
         throw new SQLException("Specified column number (" + column + ") is out of range (" + m_columnCount + ").");

      return (m_columns.get(column-1)).isWritable();
   }

   /**
    * Will a write on the column definitely succeed?
    *
    * @param   column            the first column is 1, the second is 2, ...
    *
    * @return                    <code>true</code> if so
    *
    * @exception SQLException    if a database-access error occurs.
    */
   public boolean isDefinitelyWritable(int column)
      throws SQLException
   {
      if (column > m_columnCount)
         throw new SQLException("Specified column number (" + column + ") is out of range (" + m_columnCount + ").");

      return (m_columns.get(column-1)).isDefinitelyWritable();
   }

   /**
    * JDBC 2.0
    * <p>
    * Returns the fully-qualified name of the Java class whose instances
    * are manufactured if the method ResultSet.getObject is called to
    * retrieve a value from the column. ResultSet.getObject may return a
    * subclass of the class returned by this method.
    *
    * @return                    the fully-qualified name of the class in
    *                            the Java programming language that would
    *                            be used by the method ResultSet.getObject
    *                            to retrieve the value in the specified
    *                            column. This is the class name used for
    *                            custom mapping.
    *
    * @exception SQLException    if a database-access error occurs.
    */
   public String getColumnClassName(int column)
      throws SQLException
   {
      if (column > m_columnCount)
         throw new SQLException("Specified column number (" + column + ") is out of range (" + m_columnCount + ").");

      return (m_columns.get(column-1)).getColumnClassName();
   }


   private int            m_columnCount;
   private List<PSResultSetColumnMetaData>      m_columns;
   public boolean isWrapperFor(Class<?> iface) throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public <T> T unwrap(Class<T> iface) throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }
}

