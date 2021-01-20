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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A single element of a PSJDbcDataTypeMap, this class defines the relation
 * between a JDBC type and a native type.  This class is immutable.
 */
public class PSJdbcDataTypeMapping
{

   /**
    * Convenience method for calling {@link #PSJdbcDataTypeMapping(String,
    * String, String, String, String, boolean)} with the
    * <code>isNative2Jdbc</code> parameter as <code>false</code>.
    * See that method for parameter descriptions.
    */
   public PSJdbcDataTypeMapping(String jdbc, String nativeStr,
      String defaultSize, String defaultScale, String suffix)
   {
      this(jdbc, nativeStr, defaultSize, defaultScale, suffix, false);
   }

   /**
    * Constructs a new <code>PSJdbcDataTypeMapping</code> object with the specified
    * parameters.
    *
    * @param jdbc The jdbctype of the column.  Not <code>null</code> or empty.
    *
    * @param nativeStr The string representation of the native data type.
    * Not <code>null</code> or empty.
    *
    * @param defaultSize provides a default size/precision to be used when the
    * table definition does not provide one.  This attribute must be defined
    * if the data type supports size/precision.  Must be <code>null</code> if
    * the data type does not support size/precision.  Never empty.
    *
    * @param defaultScale provides a default scale to be used when the table
    * defintion does not provide one.  This attribute must be defined if the
    * data type supports scale.  Must be <code>null</code> if the data type
    * does not support scale.  Never empty.
    *
    * @param suffix  an optional clause to be appended after the native string
    * and size and scale parameters.  Some databases have data types that
    * require this addition text; for example, the VARBINARY data type in DB2
    * is specifed as "VARCHAR () FOR BIT DATA".  "FOR BIT DATA" is the suffix.
    * May be <code>null</code> if not needed.  Never empty.
    *
    * @param isNative2Jdbc If two or more jdbc data types map to the same native
    * data type for a database/driver then this can be specified as
    * <code>true</code> for one of the mappings. If <code>true</code> then the
    * native data type will be mapped to the jdbc data type specified in this
    * mapping. The jdbc data type reported by the driver will not be used in
    * this case. If <code>false</code> then the native data type will be
    * mapped to the jdbc data type reported by the driver.
    * The first mapping found for each native type that returns
    * <code>true</code> from <code>isNative2Jdbc()</code> method will be used
    * and any others are ignored.
    *
    * @throws IllegalArgumentException if any parameter is invalid
    */
   public PSJdbcDataTypeMapping(String jdbc, String nativeStr,
      String defaultSize, String defaultScale, String suffix,
      boolean isNative2Jdbc)
   {
      initData(
         jdbc, nativeStr, defaultSize, defaultScale, suffix, isNative2Jdbc);
   }


   /**
    * Constructs a new <code>PSJdbcDataTypeMapping</code> object from its XML
    * representation.
    *
    * @param sourceNode The element from which this object is to be constructed.
    * This element must conform to the definition described in {@link #toXml}.
    * May not be <code>null</code>.
    *
    * @throws IllegalArgumentException if sourceNode is <code>null</code>.
    * @throws PSJdbcTableFactoryException if the XML representation is
    * invalid.
    */
   public PSJdbcDataTypeMapping(Element sourceNode) throws PSJdbcTableFactoryException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode may not be null");

      String rootName = sourceNode.getNodeName();
      if (!rootName.equals(NODE_NAME))
      {
         Object[] args = {NODE_NAME, rootName};
         throw new PSJdbcTableFactoryException(
            IPSTableFactoryErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      String jdbc = sourceNode.getAttribute( JDBC_AT );
      if (nullOrEmpty( jdbc ))
         throw new PSJdbcTableFactoryException(
            IPSTableFactoryErrors.XML_ELEMENT_INVALID_ATTR,
            new Object[] {NODE_NAME, JDBC_AT, ""} );

      String nativeStr = sourceNode.getAttribute( NATIVE_AT );
      if (nullOrEmpty( nativeStr ))
         throw new PSJdbcTableFactoryException(
            IPSTableFactoryErrors.XML_ELEMENT_INVALID_ATTR,
            new Object[] {NODE_NAME, NATIVE_AT, ""} );

      String size = sourceNode.getAttribute( SIZE_AT );
      if (0 == size.trim().length()) size = null;

      String scale = sourceNode.getAttribute( SCALE_AT );
      if (0 == scale.trim().length()) scale = null;

      String suffix = sourceNode.getAttribute( SUFFIX_AT );
      if (0 == suffix.trim().length()) suffix = null;

      String native2Jdbc = sourceNode.getAttribute(IS_NATIVE_2_JDBC_AT);
      boolean isNative2Jdbc =
         XML_TRUE.equalsIgnoreCase(native2Jdbc.trim()) ?  true : false;

      initData(jdbc, nativeStr, size, scale, suffix, isNative2Jdbc);
   }


   /**
    * Assigns the supplied parameters to the fields of this object after
    * validating them.  See {@link #PSJdbcDataTypeMapping(String, String, String,
    * String, String)} for a description of the parameters.
    *
    * @param jdbc must be not <code>null</code> or empty.
    * @param nativeStr must be not <code>null</code> or empty.
    * @param defaultSize must be either <code>null</code> or not empty.
    * @param defaultScale must be either <code>null</code> or not empty.
    * @param suffix must be either <code>null</code> or not empty.
    * @param isNative2Jdbc <code>true</code> if the native data type should
    * be mapped to the jdbc data type specified in this mapping,
    * <code>false</code> otherwise.
    *
    * @throws IllegalArgumentException if any parameter is invalid
    */
   private void initData(String jdbc, String nativeStr, String defaultSize,
      String defaultScale, String suffix, boolean isNative2Jdbc)
   {
      if (nullOrEmpty(jdbc))
         throw new IllegalArgumentException("jdbc may not be null or empty");
      m_jdbc = jdbc;

      if (nullOrEmpty(nativeStr))
         throw new IllegalArgumentException("native may not be null or empty");
      m_native = nativeStr;

      if (empty(defaultSize))
         throw new IllegalArgumentException("defaultSize may not be empty");
      m_defaultSize = defaultSize;

      if (empty(defaultScale))
         throw new IllegalArgumentException("defaultScale may not be empty");
      m_defaultScale = defaultScale;

      if (empty(suffix))
         throw new IllegalArgumentException("suffix may not be empty");
      m_suffix = suffix;

      m_isNative2Jdbc = isNative2Jdbc;
   }


   /**
    * Tests a string to see if it is <code>null</code> or empty.
    * @param value the string to be tested
    * @return <code>true</code> if <code>value</code> is a <code>null</code> or
    * empty <code>String</code>; <code>false</code> otherwise.
    */
   private static boolean nullOrEmpty(String value)
   {
      return (null == value || 0 == value.trim().length());
   }

   /**
    * Tests a string to see if it is not <code>null</code> and empty.
    * @param value the string to be tested
    * @return <code>true</code> if <code>value</code> is a not <code>null</code>
    * empty <code>String</code>; <code>false</code> otherwise.
    */
   private static boolean empty(String value)
   {
      return (null != value && 0 == value.trim().length());
   }

   /**
    * Gets the JDBC representation of this data type.
    * @return the JDBC representation, never <code>null</code> or empty.
    */
   public String getJdbc()
   {
      return m_jdbc;
   }


   /**
    * Gets the native representation of this data type.
    * @return the native representation, never <code>null</code> or empty.
    */
   public String getNative()
   {
      return m_native;
   }


   /**
    * Gets the default size associated with this data type.
    * @return the default size, or <code>null</code> if this data type does
    * not support size, never empty.
    */
   public String getDefaultSize()
   {
      return m_defaultSize;
   }


   /**
    * Gets the default scale associated with this data type.
    * @return the default scale, or <code>null</code> if this data type does
    * not support scale, never empty.
    */
   public String getDefaultScale()
   {
      return m_defaultScale;
   }


   /**
    * Gets the suffix associated with this data type.
    * @return the suffix, or <code>null</code> if no suffix is defined, never
    * empty.
    */
   public String getSuffix()
   {
      return m_suffix;
   }

   /**
    * Whether this mapping should be used to obtain the jdbc data type for the
    * native data type.
    *
    * @return <code>true</code> if the native data type should be mapped to the
    * jdbc data type specified in this mapping, <code>false</code> otherwise.
    */
   public boolean isNative2Jdbc()
   {
      return m_isNative2Jdbc;
   }

   /**
    * Generates an XML representation of this object, conforming to the DTD:
    * <pre><code>
    * &lt;!ELEMENT DataType EMPTY>
    * &lt;!ATTLIST DataType
    *    jdbc CDATA #REQUIRED
    *    native CDATA #REQUIRED
    *    defaultSize CDATA #IMPLIED
    *    defaultScale CDATA #IMPLIED
    *    suffix CDATA #IMPLIED
    *    isNative2Jdbc (y | n) "n"
    * >
    * </code></pre>
    *
    * @param doc The document to use when creating elements.  Not <code>null
    * </code>.
    * @return The element containing this object's state, never <code>null
    * </code>.
    * @throws IllegalArgumentException if doc is <code>null</code>.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      // create the root element
      Element root = doc.createElement(NODE_NAME);
      root.setAttribute( JDBC_AT, m_jdbc );
      root.setAttribute( NATIVE_AT, m_native );
      if (m_defaultSize != null)
         root.setAttribute( SIZE_AT, m_defaultSize );
      if (m_defaultScale != null)
         root.setAttribute( SCALE_AT, m_defaultScale );
      if (m_suffix != null)
         root.setAttribute( SUFFIX_AT, m_suffix );
      root.setAttribute(IS_NATIVE_2_JDBC_AT,
         m_isNative2Jdbc ? XML_TRUE : XML_FALSE);
      return root;
   }

   /**
    * Name of the parent element in the XML representation of this object.
    */
   static final String NODE_NAME = "DataType";

   // Names of the attributes of each field in the XML representation
   // (package access so they can be used by the JUnit test class)
   static final String JDBC_AT = "jdbc";
   static final String NATIVE_AT = "native";
   static final String SIZE_AT = "defaultSize";
   static final String SCALE_AT = "defaultScale";
   static final String SUFFIX_AT = "suffix";
   static final String IS_NATIVE_2_JDBC_AT = "isNative2Jdbc";

   /**
    * Constants for attibute values corresponding to boolean <code>true</code>
    * and <code>false</code>
    */
   private static final String XML_TRUE = "y";
   private static final String XML_FALSE = "n";

   /**
    * Representation of the JDBC data type.  Never <code>null</code> or empty.
    */
   private String m_jdbc;

   /**
    * Representation of the native data type.  Never <code>null</code> or empty.
    */
   private String m_native;

   /**
    * Provides a default size (precision) to be used when the table definition
    * does not provide one.  Must be defined if the data type supports size
    * (precision), or <code>null</code> if it does not.  Never empty.
    */
   private String m_defaultSize;

   /**
    * Provides a default scale to be used when the table definition does not
    * provide one.  Must be defined if the data type supports scale, or
    * <code>null</code> if it does not.  Never empty.
    */
   private String m_defaultScale;

   /**
    * Optional clause to be appended after the native string and size and scale
    * parameters.  Some databases have data types that require this addition
    * text; for example, DB2's VARBINARY data type is specifed as
    * "VARCHAR () FOR BIT DATA" ("FOR BIT DATA" is the suffix).  Will be
    * <code>null</code> (never empty) if the data type has no suffix.
    */
   private String m_suffix;

   /**
    * Member variable corresponding to the <code>IS_NATIVE_2_JDBC_AT</code>
    * attribute. Indicates if this mapping should be used to get the JDBC data
    * type corresponding to the native data type. If two or more JDBC data types
    * map to the same native data type (for example, both
    * <code>java.sql.Types#CLOB</code> and
    * <code>java.sql.Types#LONGVARCHAR</code> map to native type "NTEXT" on
    * SQL Server) then this attribute can be specified on one of the mappings.
    * The JDBC data type of the mapping with this attribute specified will be
    * used if the native type of the column matches the native type of this
    * mapping.
    */
   private boolean m_isNative2Jdbc = false;
}
