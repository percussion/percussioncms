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

import java.sql.DatabaseMetaData;


/**
 * The PSDataTypeInfo class offers a more tractable interface to
 * database meta data than does java.sql.DataTypeInfo. Results
 * are cached and put into a readily useable form.
 *
 * A PSDataTypeInfo object may use JDBC resources during construction
 * or method invocation, but it does not keep any JDBC resources open
 * after the method returns, and caches meta data wherever possible.
 */
public class PSDataTypeInfo
{
   /**
    * Create a data type info object.
    *
    * @param   typeName         the native data type name
    *
    * @param   jdbcType         the JDBC data type (Types.xxx)
    *
    * @param   maxPrecision   the max precision
    *
    * @param   literalPrefix   the prefix to use when quoting literals
    *
    * @param   literalSuffix   the suffix to use when quoting literals
    *
    * @param   nullable         are null values permitted (see the
    *                           java.sql.DatabaseMetaData typeXXX settings)
    *
    * @param   searchable      what type of searching is supported
    *
    * @param   autoIncrement   is the value automatically incremented?
    *
    * @param   minScale         the minimum scale supported
    *
    * @param   maxScale         the maximum scale supported
    *
    *   @see      java.sql.DatabaseMetaData
    */
   PSDataTypeInfo(String typeName, short jdbcType, int maxPrecision,
      String literalPrefix, String literalSuffix, short nullable,
      short searchable, boolean autoIncrement, short minScale,
      short maxScale)
   {
      super();
      m_typeName = typeName;
      m_jdbcType = jdbcType;
      m_maxPrecision = maxPrecision;
      m_minScale = minScale;
      m_maxScale = maxScale;
      m_literalPrefix = literalPrefix;
      m_literalSuffix = literalSuffix;
      m_flags = 0;

      if (nullable == DatabaseMetaData.typeNoNulls)
         m_flags |= FLAG_NO_NULLS;
      else if (nullable == DatabaseMetaData.typeNullable)
         m_flags |= FLAG_NULLABLE;
      else
         m_flags |= FLAG_NULL_UNKNOWN;

      if (autoIncrement)
         m_flags |= FLAG_AUTO_INCREMENT;

      if (searchable == DatabaseMetaData.typeSearchable)
         m_flags |= FLAG_SEARCH_ALL;
      else if (searchable == DatabaseMetaData.typePredChar)
         m_flags |= FLAG_SEARCH_CHAR;
      else if (searchable == DatabaseMetaData.typePredBasic)
         m_flags |= FLAG_SEARCH_BASIC;
      else
         m_flags |= FLAG_SEARCH_NONE;
   }

   /**
    * Get the native data type name.
    *
    * @return         the native data type name
    */
   public String getTypeName()
   {
      return m_typeName;
   }

   /**
    * Get the JDBC data type.
    *
    * @return         the jdbc data type
    */
   public short getJdbcType()
   {
      return m_jdbcType;
   }

   /**
    * Get the max precision (size).
    *
    * @return         the max precision
    */
   public int getMaxPrecision()
   {
      return m_maxPrecision;
   }

   /**
    * Get the prefix to use when quoting literals.
    *
    * @return      the prefix to use when quoting literals
    */
   public String getLiteralPrefix()
   {
      return m_literalPrefix;
   }

   /**
    * Get the suffix to use when quoting literals.
    *
    * @return      the suffix to use when quoting literals
    */
   public String getLiteralSuffix()
   {
      return m_literalSuffix;
   }

   /**
    * Are null values permitted?
    *
    * @return      the java.sql.DatabaseMetaData typeXXX null setting
    */
   public short getNullableSetting()
   {
      if ((m_flags & FLAG_NO_NULLS) == FLAG_NO_NULLS)
         return DatabaseMetaData.typeNoNulls;

      if ((m_flags & FLAG_NULLABLE) == FLAG_NULLABLE)
         return DatabaseMetaData.typeNullable;

      return DatabaseMetaData.typeNullableUnknown;
   }

   /**
    * What type of searching is supported?
    *
    * @return      java.sql.DatabaseMetaData typeXXX search setting
    */
   public short getSearchableSetting()
   {
      if ((m_flags & FLAG_SEARCH_ALL) == FLAG_SEARCH_ALL)
         return DatabaseMetaData.typeSearchable;

      if ((m_flags & FLAG_SEARCH_ALL) == FLAG_SEARCH_CHAR)
         return DatabaseMetaData.typePredChar;

      if ((m_flags & FLAG_SEARCH_ALL) == FLAG_SEARCH_BASIC)
         return DatabaseMetaData.typePredBasic;

      return DatabaseMetaData.typePredNone;
   }

   /**
    * is the value automatically incremented?
    *
    * @return      <code>true</code> if it is
    */
   public boolean isAutoIncrement()
   {
      return ((m_flags & FLAG_AUTO_INCREMENT) == FLAG_AUTO_INCREMENT);
   }

   /**
    * Get the minimum scale supported
    *
    * @return      the minimum scale supported
    */
   public short getMinimumScale()
   {
      return m_minScale;
   }

   /**
    * Get the maximum scale supported
    *
    * @return      the maximum scale supported
    */
   public short getmaximumScale()
   {
      return m_maxScale;
   }


   // the native data type name
   private String      m_typeName;

   // the JDBC data type (Types.xxx)
   private short      m_jdbcType;

   // max precision
   private int         m_maxPrecision;

   // the minimum scale supported
   private short      m_minScale;
   
   // the maximum scale supported
   private short      m_maxScale;

   // the prefix to use when quoting literals
   private String      m_literalPrefix;

   // the suffix to use when quoting literals
   private String      m_literalSuffix;

   /* this contains the flags for:
    *
    * null values: DatabaseMetaData.typeNoNulls, typeNullable or
    *              typeNullableUnknown
    *
    * searchable: DatabaseMetaData.typePredNone, typePredChar, typePredBasic
    *             or typeSearchable
    *
    * autoIncrement
    */
   private int         m_flags;

   private static final int      FLAG_NO_NULLS         = 0x0001;
   private static final int      FLAG_NULLABLE         = 0x0002;
   private static final int      FLAG_NULL_UNKNOWN     = 0x0004;
   private static final int      FLAG_AUTO_INCREMENT   = 0x0008;
   private static final int      FLAG_SEARCH_NONE      = 0x0010;
   private static final int      FLAG_SEARCH_CHAR      = 0x0020;
   private static final int      FLAG_SEARCH_BASIC     = 0x0040;
   private static final int      FLAG_SEARCH_ALL       = 0x0080;
}
