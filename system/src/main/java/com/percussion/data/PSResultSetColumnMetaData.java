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

import java.sql.ResultSetMetaData;


/**
 * The PSResultSetColunMetaData is used to defined a column's meta data
 * for use in a PSResultSetMetaData object.
 * 
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSResultSetColumnMetaData
{
   /**
    * Construct a column meta data object with some basic info.
    *
    * @param   name      the name of the column
    *
    * @param   type      the java.sql.Type data type of the column
    *
    * @param   size      the max column size
    */
   public PSResultSetColumnMetaData(String name, int type, int size) {
      super();
      m_name = name;
      m_label = name;
      m_type = type;
      m_displaySize = size;
      m_precision = size;

      // provide reasonable defaults for the rest
      m_flags = 0;
      m_scale = 0;
      m_schemaName = "";
      m_tableName = "";
      m_catalogName = "";
      m_typeName = "";
      m_className = "";
   }

   /**
    * Is the column automatically numbered, thus read-only?
    *
    * @return                    <code>true</code> if so 
    */
   public boolean isAutoIncrement()
   {
      return (m_flags & COL_AUTO_INCREMENT) == COL_AUTO_INCREMENT;
   }
 
   /**
    * Does a column's case matter?
    *
    * @return                    <code>true</code> if so 
    */
   public boolean isCaseSensitive()
   {
      return (m_flags & COL_CASE_SENSITIVE) == COL_CASE_SENSITIVE;
   }
 
   /**
    * Can the column be used in a where clause?
    *
    * @return                    <code>true</code> if so 
    */
   public boolean isSearchable()
   {
      return (m_flags & COL_SEARCHABLE) == COL_SEARCHABLE;
   }

   /**
    * Is the column a cash value?
    *
    * @return                    <code>true</code> if so 
    */
   public boolean isCurrency()
   {
      return (m_flags & COL_CURRENCY) == COL_CURRENCY;
   }
 
   /**
    * Can you put a NULL in this column?
    *
    * @return                    columnNoNulls, columnNullable or
    *                            columnNullableUnknown
    */
   public int isNullable()
   {
      if ((m_flags & COL_NO_NULL) == COL_NO_NULL)
         return ResultSetMetaData.columnNoNulls;
      else if ((m_flags & COL_NULLABLE) == COL_NULLABLE)
         return ResultSetMetaData.columnNullable;

      return ResultSetMetaData.columnNullableUnknown;
   }

   /**
    * Is the column a signed number?
    *
    * @return                    <code>true</code> if so 
    */
   public boolean isSigned()
   {
      return (m_flags & COL_SIGNED) == COL_SIGNED;
   }

   /**
    * What's the column's normal max width in chars?
    *
    * @return                    max width
    */
   public int getColumnDisplaySize()
   {
      return m_displaySize;
   }

   /**
    * What's the suggested column title for use in printouts and displays?
    *
    * @return                    suggested column title
    */
   public String getColumnLabel()
   {
      return m_label;
   }

   /**
    * What's a column's name?
    *
    * @return                    column name
    */
   public String getColumnName()
   {
      return m_name;
   }

   /**
    * What's a column's table's schema?
    *
    * @return                    schema name or "" if not applicable
    */
   public String getSchemaName()
   {
      return m_schemaName;
   }

   /**
    * What's a column's number of decimal digits?
    *
    * @return                    precision
    */
   public int getPrecision()
   {
      return m_precision;
   }

   /**
    * What's a column's number of digits to right of the decimal point?
    *
    * @return                    scale
    */
   public int getScale()
   {
      return m_scale;
   }

   /**
    * What's a column's table name?
    *
    * @return                    table name or "" if not applicable
    */
   public String getTableName()
   {
      return m_tableName;
   }

   /**
    * What's a column's table's catalog name?
    *
    * @return                    column name or "" if not applicable.
    */
   public String getCatalogName()
   {
      return m_catalogName;
   }

   /**
    * What's a column's SQL type? See java.sql.Types for possible types.
    *
    * @return                    SQL type
    */
   public int getColumnType()
   {
      return m_type;
   }

   /**
    * What's a column's data source specific type name?
    *
    * @return                    type name
    */
   public String getColumnTypeName()
   {
      return m_typeName;
   }

   /**
    * Is a column definitely not writable?
    *
    * @return                    <code>true</code> if so 
    */
   public boolean isReadOnly()
   {
      return (m_flags & COL_READ_ONLY) == COL_READ_ONLY;
   }

   /**
    * Is it possible for a write on the column to succeed?
    *
    * @return                    <code>true</code> if so 
    */
   public boolean isWritable()
   {
      return (m_flags & COL_WRITE_POSSIBLE) == COL_WRITE_POSSIBLE;
   }

   /**
    * Will a write on the column definitely succeed?
    *
    * @return                    <code>true</code> if so 
    */
   public boolean isDefinitelyWritable()
   {
      return (m_flags & COL_WRITE_DEFINITE) == COL_WRITE_DEFINITE;
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
    */
   public String getColumnClassName()
   {
      return m_className;
   }



   /**
    * Is the column automatically numbered, thus read-only?
    *
    * @param enable              <code>true</code> if so 
    */
   public void setAutoIncrement(boolean enable)
   {
      if (enable)
         m_flags |= COL_AUTO_INCREMENT;
      else
         m_flags &= ~COL_AUTO_INCREMENT;
   }
 
   /**
    * Does a column's case matter?
    *
    * @param enable              <code>true</code> if so 
    */
   public void setCaseSensitive(boolean enable)
   {
      if (enable)
         m_flags |= COL_CASE_SENSITIVE;
      else
         m_flags &= ~COL_CASE_SENSITIVE;
   }
 
   /**
    * Can the column be used in a where clause?
    *
    * @param enable              <code>true</code> if so 
    */
   public void setSearchable(boolean enable)
   {
      if (enable)
         m_flags |= COL_SEARCHABLE;
      else
         m_flags &= ~COL_SEARCHABLE;
   }

   /**
    * Is the column a cash value?
    *
    * @param enable              <code>true</code> if so 
    */
   public void setCurrency(boolean enable)
   {
      if (enable)
         m_flags |= COL_CURRENCY;
      else
         m_flags &= ~COL_CURRENCY;
   }
 
   /**
    * Can you put a NULL in this column?
    *
    * @param flag                columnNoNulls, columnNullable or
    *                            columnNullableUnknown
    */
   public void setNullable(int flag)
   {
      m_flags &= ~(COL_NO_NULL | COL_NULLABLE | COL_NULL_UNKNOWN);
      if (flag == ResultSetMetaData.columnNoNulls)
         m_flags |= COL_NO_NULL;
      else if (flag == ResultSetMetaData.columnNullable)
         m_flags |= COL_NULLABLE;
      else
         m_flags |= COL_NULL_UNKNOWN;
   }

   /**
    * Is the column a signed number?
    *
    * @param enable              <code>true</code> if so 
    */
   public void setSigned(boolean enable)
   {
      if (enable)
         m_flags |= COL_SIGNED;
      else
         m_flags &= ~COL_SIGNED;
   }

   /**
    * What's the column's normal max width in chars?
    *
    * @param size                max width
    */
   public void setColumnDisplaySize(int size)
   {
      m_displaySize = size;
   }

   /**
    * What's the suggested column title for use in printouts and displays?
    *
    * @param label               suggested column title
    */
   public void setColumnLabel(String label)
   {
      m_label = label;
   }

   /**
    * What's a column's name?
    *
    * @param name                column name
    */
   public void setColumnName(String name)
   {
      m_name = name;
   }

   /**
    * What's a column's table's schema?
    *
    * @param schema              schema name or "" if not applicable
    */
   public void setSchemaName(String schema)
   {
      m_schemaName = schema;
   }

   /**
    * What's a column's number of decimal digits?
    *
    * @param precision           precision
    */
   public void setPrecision(int precision)
   {
      m_precision = precision;
   }

   /**
    * What's a column's number of digits to right of the decimal point?
    *
    * @param scale               scale
    */
   public void setScale(int scale)
   {
      m_scale = scale;
   }

   /**
    * What's a column's table name?
    *
    * @return                    table name or "" if not applicable
    */
   public void setTableName(String table)
   {
      m_tableName = table;
   }

   /**
    * What's a column's table's catalog name?
    *
    * @param catalog             catalog name or "" if not applicable.
    */
   public void setCatalogName(String catalog)
   {
      m_catalogName = catalog;
   }

   /**
    * What's a column's SQL type? See java.sql.Types for possible types.
    *
    * @param type                SQL type
    */
   public void setColumnType(int type)
   {
      m_type = type;
   }

   /**
    * What's a column's data source specific type name?
    *
    * @param typeName            type name
    */
   public void setColumnTypeName(String typeName)
   {
      m_typeName = typeName;
   }

   /**
    * Is a column definitely not writable?
    *
    * @param enable              <code>true</code> if so 
    */
   public void setReadOnly(boolean enable)
   {
      if (enable)
         m_flags |= COL_READ_ONLY;
      else
         m_flags &= ~COL_READ_ONLY;
   }

   /**
    * Is it possible for a write on the column to succeed?
    *
    * @param enable              <code>true</code> if so 
    */
   public void setWritable(boolean enable)
   {
      if (enable)
         m_flags |= COL_WRITE_POSSIBLE;
      else
         m_flags &= ~COL_WRITE_POSSIBLE;
   }

   /**
    * Will a write on the column definitely succeed?
    *
    * @param enable              <code>true</code> if so 
    */
   public void setDefinitelyWritable(boolean enable)
   {
      if (enable)
         m_flags |= COL_WRITE_DEFINITE;
      else
         m_flags &= ~COL_WRITE_DEFINITE;
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
    */
   public void setColumnClassName(String className)
   {
      m_className = className;
   }


   // these are our flags
   private static final int COL_AUTO_INCREMENT      = 0x00000001;
   private static final int COL_CASE_SENSITIVE      = 0x00000002;
   private static final int COL_SEARCHABLE         = 0x00000004;
   private static final int COL_CURRENCY            = 0x00000008;
   private static final int COL_NO_NULL            = 0x00000010;
   private static final int COL_NULLABLE            = 0x00000020;
   private static final int COL_NULL_UNKNOWN         = 0x00000040;
   private static final int COL_SIGNED               = 0x00000080;
   private static final int COL_READ_ONLY            = 0x00000100;
   private static final int COL_WRITE_POSSIBLE       = 0x00000200;
   private static final int COL_WRITE_DEFINITE      = 0x00000400;

   private int       m_flags;
   private int       m_displaySize;
   private String      m_label;
   private String      m_name;
   private String      m_schemaName;
   private int         m_precision;
   private int         m_scale;
   private String      m_tableName;
   private String      m_catalogName;
   private int         m_type;
   private String      m_typeName;
   private String      m_className;
}

