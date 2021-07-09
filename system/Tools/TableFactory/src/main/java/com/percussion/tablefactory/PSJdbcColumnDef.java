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

package com.percussion.tablefactory;

import com.percussion.util.PSSqlHelper;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.sql.Types;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class is used to represent a column definition in a table schema, and
 * the action to perform when that table schema is used to create or modify a
 * table.
 */
public class PSJdbcColumnDef extends PSJdbcTableComponent
{
   /**
    * Convenience method for calling {@link #PSJdbcColumnDef(PSJdbcDataTypeMap,
    * String, int, int, String, String, boolean, String)} with the scale
    * parameter as <code>null</code>.  See that method for parameter
    * descriptions.
    */
   public PSJdbcColumnDef(PSJdbcDataTypeMap dataTypeMap, String name,
      int action, int jdbcType, String size, boolean allowsNull,
      String defaultValue)
   {
      this(dataTypeMap, name, action, jdbcType, size, null, allowsNull,
         defaultValue);
   }


   /**
    * Initializes a newly created <code>PSJdbcColumnDef</code> object from the
    * specified parameters.
    *
    * @param dataTypeMap The object containing the jdbc to native mappings.
    * May not be <code>null</code>.
    *
    * @param name The name of the column, may not be <code>null</code> or empty.
    *
    * @param action One of the <code>PSJdbcTableComponent.ACTION_xxx</code>
    * constants.
    *
    * @param jdbcType The jdbc datatype of this column.  Must a valid jdbc data
    * type whose name is contained in the dataTypeMap.
    *
    * @param size the length of the datatype.  This value will be interpreted
    * slightly differently for each datatype:  number of characters for a
    * string value, number of bytes for a LOB value, or number of digits
    * (precision) for a numeric value.  May be <code>null</code> if datatype
    * does not support size, or to use the default size defined in the datatype
    * map.  Never empty.
    *
    * @param scale the number of digits to the right of the decimal point in
    * numeric data types.  May be <code>null</code> if datatype does not support
    * scale, or to use the default scale defined in the datatype map.  Never
    * empty.
    *
    * @param allowsNull If <code>true</code>, then column allows null values.
    *
    * @param defaultValue The default value to use if adding a column that does
    *    not allow null values to a table with existing rows.  Is not used as
    *    part of the column definition when adding the column to the table
    *    schema in the database.  If <code>null</code>, then no default is
    *    defined.  May be empty.  If the column allows null values, then the
    *    value of this parameter is ignored.
    *
    * @throws IllegalArgumentException if any param is invalid.
    */
   public PSJdbcColumnDef(PSJdbcDataTypeMap dataTypeMap, String name,
      int action, int jdbcType, String size, String scale, boolean allowsNull,
      String defaultValue)
   {
      super(name, action);

      if (dataTypeMap == null)
         throw new IllegalArgumentException("dataTypeMap may not be null");

      if (size != null && size.trim().length() == 0)
         throw new IllegalArgumentException("size may be null but not empty");

      m_dataTypeMap = dataTypeMap;

      // this will validate
      try
      {
         setType(jdbcType);
      }
      catch (PSJdbcTableFactoryException e)
      {
         throw new IllegalArgumentException("Invalid type: " + e.toString());
      }

      // this will validate, must set default value first
      m_defaultValue = defaultValue;
      setAllowsNull( allowsNull );
      if (size != null)
      {
         long sizeLong = Long.parseLong(size);
         if (sizeLong < 0)
         {
            throw new IllegalArgumentException("Field size : " + sizeLong + " is invalid ");
         }
      }

      setSize( size );
      setScale( scale );
   }


   /**
    * Create this object from its Xml representation.  See {@link #fromXml(
    * Element) fromXml} for more information.
    *
    * @param dataTypeMap The object containing the jdbc to native mappings.
    *    May not be <code>null</code>.
    * @param sourceNode The element from which this object is to be constructed.
    *    Element must conform to the definition for the column element in the
    *    tabledef.dtd.  May not be <code>null</code>.
    *
    * @throws IllegalArgumentException if either param is <code>null</code>.
    * @throws PSJdbcTableFactoryException if there are any errors.
    */
   public PSJdbcColumnDef(PSJdbcDataTypeMap dataTypeMap, Element sourceNode)
      throws PSJdbcTableFactoryException
   {
      if (dataTypeMap == null)
         throw new IllegalArgumentException("dataTypeMap may not be null");

      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode may not be null");

      m_dataTypeMap = dataTypeMap;
      fromXml(sourceNode);
   }

   /**
    * Construct this column by copying data from the supplied column.
    *
    * @param srcCol The column to copy from.  May not be <code>null</code>.
    *
    * @throws IllegalArgumentException if srcCol is <code>null</code>.
    * @throws IllegalStateException if srcCol's type is invalid.
    */
   public PSJdbcColumnDef(PSJdbcColumnDef srcCol)
   {
      if (srcCol == null)
         throw new IllegalArgumentException("srcCol may not be null");

      setName(srcCol.getName());
      setAction(srcCol.getAction());
      setAllowsNull(srcCol.allowsNull());
      setDefaultValue(srcCol.getDefaultValue());
      m_dataTypeMap = srcCol.m_dataTypeMap;
      m_size = srcCol.m_size;
      m_scale = srcCol.m_scale;
      m_limitSizeForIndex = srcCol.m_limitSizeForIndex;

      try
      {
         setType(srcCol.getType());
      }
      catch (PSJdbcTableFactoryException e)
      {
         // this can never happen, but what the heck...
         throw new IllegalStateException("Invalid type: " + e.toString());
      }
   }


   /**
    * Restore this object from an Xml representation.
    *
    * @param sourceNode The element from which to get this object's state.
    *    Element must conform to the definition for the column element in the
    *    tabledef.dtd.  May not be <code>null</code>.
    *
    * @throws IllegalArgumentException if sourceNode is <code>null</code>.
    * @throws PSJdbcTableFactoryException if there are any errors.
    */
   @Override
   public void fromXml(Element sourceNode) throws PSJdbcTableFactoryException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode may not be null");

      if (!sourceNode.getNodeName().equals(NODE_NAME))
      {
         Object[] args = {NODE_NAME, sourceNode.getNodeName()};
         throw new PSJdbcTableFactoryException(
            IPSTableFactoryErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      // allow base class to set its memebers
      getComponentState(sourceNode);

      // get index size limit, default to false (this default in the DTD)      
      m_limitSizeForIndex = "y".equalsIgnoreCase(sourceNode.getAttribute(
         LIMIT_SIZE_ATTR));

      PSXmlTreeWalker walker = new PSXmlTreeWalker(sourceNode);

      // get the data type and set it
      String strJdbcType = walker.getElementData(JDBC_EL);
      if (strJdbcType == null)
         throw new PSJdbcTableFactoryException(
            IPSTableFactoryErrors.XML_ELEMENT_NULL, JDBC_EL);
      // this will validate for us
      setType(strJdbcType);

      // get the allows null value
      String strAllowsNull = walker.getElementData(ALLOWS_NULL_EL);
      if (strAllowsNull == null)
         throw new PSJdbcTableFactoryException(
            IPSTableFactoryErrors.XML_ELEMENT_NULL, ALLOWS_NULL_EL);

      if (strAllowsNull.equalsIgnoreCase("y")) m_allowsNull = true;
      else if (strAllowsNull.equalsIgnoreCase("yes")) m_allowsNull = true;
      else m_allowsNull = false;

      // get the size param, validate, and set it
      String size = walker.getElementData( SIZE_EL );
      if (size != null && size.trim().length() == 0)
         size = null;
      else if (!validateParams( size ))
      {
         Object[] args = {NODE_NAME, SIZE_EL, size};
         throw new PSJdbcTableFactoryException(
            IPSTableFactoryErrors.XML_ELEMENT_INVALID_CHILD, args);
      }
      setSize( size );

      // get the scale param, validate, and set it
      String scale = walker.getElementData( SCALE_EL );
      if (scale != null && scale.trim().length() == 0)
         scale = null;
      else if (!validateParams( scale ))
      {
         Object[] args = {NODE_NAME, SIZE_EL, size};
         throw new PSJdbcTableFactoryException(
            IPSTableFactoryErrors.XML_ELEMENT_INVALID_CHILD, args);
      }
      setScale( scale );

      // get the default value and set it
      m_defaultValue = walker.getElementData(DEFAULT_VALUE_EL);
      m_sequence = walker.getElementData(SEQUENCE_EL);
   }

   /**
    * Serializes this object's state to Xml conforming with the tabledef.dtd.
    *
    * @param doc The document to use when creating elements.  May not be <code>
    *    null</code>.
    *
    * @return The element containing this object's state, never <code>
    *    null</code>.
    *
    * @throws IllegalArgumentException if doc is <code>null</code>.
    */
   @Override
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      // create the root element
      Element   root = doc.createElement(NODE_NAME);

      // first add base class state
      setComponentState(root);

      // set index size limit
      root.setAttribute(LIMIT_SIZE_ATTR, m_limitSizeForIndex ? "y" : "n");
      
      // add jdbc type as a String
      PSXmlDocumentBuilder.addElement(doc, root, JDBC_EL, m_jdbcTypeString);

      // add size parameter
      if (m_size != null)
         PSXmlDocumentBuilder.addElement(doc, root, SIZE_EL, m_size);

      // add allows null
      PSXmlDocumentBuilder.addElement(doc, root, ALLOWS_NULL_EL, m_allowsNull ?
         "yes" : "no");

      // add scale parameter
      if (m_scale != null)
         PSXmlDocumentBuilder.addElement(doc, root, SCALE_EL, m_scale);

      // add default if not null
      if (m_defaultValue != null)
         PSXmlDocumentBuilder.addElement(doc, root, DEFAULT_VALUE_EL,
            m_defaultValue);

      if (m_sequence != null)
         PSXmlDocumentBuilder.addElement(doc, root, SEQUENCE_EL, m_sequence);

      return root;
   }

   /**
    * Serializes this object's state to a string. Creates an empty document,
    * then calls <code>toXm()</code> method and then serializes the returned
    * root element to a string.
    *
    * @return The string containing this object's state, never <code>
    * null</code> or empty
    */
   @Override
   public String toString()
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element   root = toXml(doc);
      return PSXmlDocumentBuilder.toString(root);
   }

   /**
    * compares this column to another object.
    *
    * @param obj the object to compare
    * @return <code>true</code> if the object is a PSJdbcColumnDef with
    *    identical values, excluding the dataTypeMap. Otherwise returns
    *    <code>false</code>.
    */
   @Override
   public boolean equals(Object obj)
   {
      boolean isMatch = true;
      if (!(obj instanceof PSJdbcColumnDef))
         isMatch = false;
      else
      {
         PSJdbcColumnDef other = (PSJdbcColumnDef)obj;
         if (!super.equals(other))
            isMatch = false;
         else if (m_allowsNull ^ other.m_allowsNull)
            isMatch = false;
         else if (m_jdbcType != other.m_jdbcType)
            isMatch = false;
         else if (m_size != null ^ other.m_size != null)
            isMatch = false;
         else if (m_size != null && !m_size.equals(other.m_size))
            isMatch = false;
         else if (m_scale != null ^ other.m_scale != null)
            isMatch = false;
         else if (m_scale != null && !m_scale.equals(other.m_scale))
            isMatch = false;
         else if (m_defaultValue == null ^ other.m_defaultValue == null)
            isMatch = false;
         else if (m_sequence != null && !m_sequence.equals(other.m_sequence))
            isMatch = false;
         else if (m_defaultValue != null &&
            !m_defaultValue.equals(other.m_defaultValue))
         {
            isMatch = false;
         }
         else if (m_limitSizeForIndex ^ other.m_limitSizeForIndex)
            isMatch = false;
      }

      return isMatch;
   }

   /**
    * Overridden to fullfill the contract that if t1 and t2 are 2 different
    * instances of this class and t1.equals(t2), t1.hashCode() ==
    * t2.hashCode().
    *
    * @return The sum of all the hash codes of the composite objects, excluding
    * the dataTypeMap.
    */
   @Override
   public int hashCode()
   {
      int hash = 0;
      hash += super.hashCode();
      hash += Boolean.valueOf(m_allowsNull).hashCode();
      hash += new Integer(m_jdbcType).hashCode();
      hash += m_limitSizeForIndex ? 0 : 1;
      if ( null != m_size )
         hash += m_size.hashCode();
      if ( null != m_scale )
         hash += m_scale.hashCode();
      if ( null != m_defaultValue )
         hash += m_defaultValue.hashCode();
      return hash;
   }


   /**
    * Compares this column to another, testing equals of all members excluding
    * the default value and the dataTypeMap.  Used to determine if
    * this column specifies changes compared to a column that has been
    * cataloged from the database, and the default value does not become part
    * of the column's definition in the database, hence it is excluded. If the
    * jdbc types do not match, and the native type of the old column has been
    * set, it will be compared to the native type specified by this column's
    * jdbc type and datatype map.
    *
    * @param oldCol the columndef to compare, may be <code>null</code>.
    *
    * @return <code>true</code> if this column does specifies any changes
    * from the supplied oldCol, <code>false</code> otherwise.
    */
   public boolean isChanged(PSJdbcColumnDef oldCol)
   {
      boolean isChanged = false;
      if (oldCol == null)
         isChanged = true;
      else
      {
         // don't use adjusted size for oldcol  
         String thisAdjSize = getAdjustedSize(false);
         String oldSize = oldCol.getSize();
         String thisScale = getScale();
         String oldScale = oldCol.getScale();
         
         if (!super.equals(oldCol))
            isChanged = true;
         else if (m_allowsNull ^ oldCol.m_allowsNull)
            isChanged = true;
         else if (thisAdjSize != null ^ oldSize != null)
            isChanged = true;
         else if (thisAdjSize != null && !thisAdjSize.equals(oldSize))
         {
            // only consider it a change if the existing size is too small
            try
            {
               int desiredSize = Integer.parseInt( thisAdjSize );
               int existingSize = Integer.parseInt( oldSize );
               if (existingSize < desiredSize)
                  isChanged = true;
            } catch (NumberFormatException e)
            {
               // shouldn't happen, but if it does, ignore it here and let
               // the create/alter attempt report the error
               isChanged = true;
            }
         }
         else if (thisScale != null ^ oldScale != null)
            isChanged = true;
         else if (thisScale != null && !thisScale.equals(oldScale))
         {
            // only consider it a change if the existing scale is too small
            try
            {
               int desiredScale = Integer.parseInt( thisScale );
               int existingScale = Integer.parseInt( oldScale );
               if (existingScale < desiredScale)
                  isChanged = true;
            } catch (NumberFormatException e)
            {
               // shouldn't happen, but if it does, ignore it here and let
               // the create/alter attempt report the error
               isChanged = true;
            }
         }
         else if (m_jdbcType != oldCol.m_jdbcType)
         {
            if (oldCol.m_nativeType != null)
            {
               String nativeType = m_dataTypeMap.getNativeString(m_jdbcType);
               if(!oldCol.m_nativeType.equalsIgnoreCase(nativeType))
                  isChanged = true;
            }
            else
               isChanged = true;
         }
      }

      return isChanged;
   }

   /**
    * Returns this column's jdbc data type.
    *
    * @return A valid jdbc data type.
    */
   public int getType()
   {
      return m_jdbcType;
   }

   /**
    * Sets the jdbc datatype on this column.
    *
    * @param type Must be a valid jdbc data type whose name is contained in the
    * dataTypeMap supplied at construction.
    *
    * @throws PSJdbcTableFactoryException if type is not valid.
    */
   public void setType(int type) throws PSJdbcTableFactoryException
   {
      // get the string representation of this type - this will validate
      m_jdbcTypeString = m_dataTypeMap.convertJdbcType(type);
      m_jdbcType = type;

      // since the data type has changed, recompute size and scale
      setSize( m_size );
      setScale( m_scale );
   }

   /**
    * Sets the jdbc datatype on this column.
    *
    * @param type Must be a valid jdbc data type contained in the dataTypeMap
    * supplied at construction.  May not be <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if type is <code>null</code> or empty.
    * @throws PSJdbcTableFactoryException if type is not valid.
    */
   public void setType(String type) throws PSJdbcTableFactoryException
   {
      if (type == null || type.trim().length() == 0)
         throw new IllegalArgumentException("type may not be null or empty");

      // get the int representation of this type - this will validate
      m_jdbcType = m_dataTypeMap.convertJdbcString(type);
      m_jdbcTypeString = type;

      // since the data type has changed, recompute size and scale
      setSize( m_size );
      setScale( m_scale );
   }

   /**
    * Sets the native type of this column.  This value (instead of the value
    * from the data type mapping) is used by {@link #isChanged(PSJdbcColumnDef)}
    * to compare a column that has been catataloged with a new
    * definition to determine if the new column specifies any changes.
    * <p>
    * This method can be used to record the native type as reported by the
    * database driver's metadata (which may differ from the native type as
    * specified in the data type map).
    *
    * @param nativeType The type to assign, not <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if nativeType is invalid.
    */
   public void setNativeType(String nativeType)
   {
      if (nativeType == null || nativeType.trim().length() == 0)
         throw new IllegalArgumentException(
            "nativeType may not be null or empty");

      m_nativeType = nativeType;
   }

   /**
    * Returns the native type of this column. If a native type has been set
    * using <code>setNativeType()</code>, then simply returns this type,
    * otherwise uses the data type map set in the ctor to obtain the native
    * type corresponding to the jdbc type of this column.
    *
    * @return the native type of this column, never <code>null</code> or empty
    */
   public String getNativeType()
   {
      if (m_nativeType == null || m_nativeType.trim().length() < 1)
      {
         PSJdbcDataTypeMapping dataType = m_dataTypeMap.getMapping(m_jdbcType);
         m_nativeType = dataType.getNative();
      }
      return m_nativeType;
   }

   /**
    * Determines if this column allows nulls.
    *
    * @return <code>true</code> if this columns allows nulls, <code>false</code>
    * if not.
    */
   public boolean allowsNull()
   {
      return m_allowsNull;
   }

   /**
    * Sets this column to allows nulls or not.
    *
    * @param allowsNull If <code>true</code>, nulls will be allowed, if
    * <code>false</code>, they will not be allowed.
    */
   public void setAllowsNull(boolean allowsNull)
   {
      m_allowsNull = allowsNull;
   }

   /**
    * Sets the default value to use if adding this column and {@link
    * #allowsNull()} is <code>false</code>.
    *
    * @param defaultValue The default value, may be <code>null</code> to clear
    * it and may be empty.
    */
   public void setDefaultValue(String defaultValue)
   {
      m_defaultValue = defaultValue;
   }

   /**
    * Returns this default value.  See {@link #setDefaultValue(String)
    * setDefaultValue} for more info.
    *
    * @return The default value, may be <code>null</code> or empty.
    */
   public String getDefaultValue()
   {
      return m_defaultValue;
   }

   /**
    * is this column a sequence or identity column?
    *
    * @return <code>true</code> if a sequence is defined,
    * <code>false</code> otherwise.
    **/
   public boolean isSequence()
   {
     return m_sequence != null;
   }

   /**
    * return the sequence name for this column
    *
    * @return the name of the sequence used to populate this column.
    **/
   public String getSequence()
   {
      return m_sequence;
   }

   /**
    * Gets this column's scale.  If one has not been set, the default scale for
    * this datatype is returned.
    *
    * @return The scale to use, may be <code>null</code>, never empty.
    */
   public String getScale()
   {
      return m_scale == null ? 
         m_dataTypeMap.getMapping(m_jdbcType).getDefaultScale() : m_scale;
   }


   /**
    * Sets the scale parameter of this column's datatype definition.
    *
    * @param scale the number of digits to the right of the decimal point in
    * numeric data types.  If <code>null</code>, the default scale defined in
    * the datatype map will be used instead (the default could also be
    * <code>null</code>).  Will be ignored (and <code>null</code> assigned)
    * if the datatype does not support scale.  Never empty.
    *
    * @throws IllegalArgumentException if <code>scale</code> is empty.
    */
   public void setScale(String scale)
   {
      if (!validateParams( scale ))
         throw new IllegalArgumentException("scale may be null but not empty");

      String defaultScale =
         m_dataTypeMap.getMapping(m_jdbcType).getDefaultScale();

      // if the default is null, force value to null
      if (defaultScale == null)
         scale = null;
      // if the provided value is the default, store null
      else if (defaultScale.equals(scale))
         scale = null;

      m_scale = scale;
   }


   /**
    * Get the size.  If one has not been set, the default size for this datatype
    * is returned.
    * 
    * @return The size, may be <code>null</code>, never empty.
    */
   public String getSize()
   {
      return m_size == null ? 
         m_dataTypeMap.getMapping(m_jdbcType).getDefaultSize() : m_size;
   }

   /**
    * Sets the size/precision parameter of this column's datatype definition.
    * <p>
    * For example, here is a create table statement that creates a table named
    * "foo" with a column named "bar" of type varchar using a size value of
    * "255":<br>
    * <code><pre>
    * CREATE TABLE foo (bar VARCHAR(255) NULL)
    * </pre></code>
    *
    * @param size the length of the datatype.  This value will be interpreted
    * slightly differently for each datatype:  number of characters for a
    * string value, number of bytes for a LOB value, or number of digits
    * (precision) for a numeric value.  If <code>null</code>, the default size
    * defined in the datatype map will be used when building the sql def (the 
    * default could also be <code>null</code>).  Will be ignored (and 
    * <code>null</code> assigned) if the datatype does not support size.  
    * Never empty.
    *
    * @throws IllegalArgumentException if size is empty.
    */
   public void setSize(String size)
   {
      if (!validateParams(size))
         throw new IllegalArgumentException("size may be null but not empty");

      String defaultSize =
         m_dataTypeMap.getMapping(m_jdbcType).getDefaultSize();

      // if the default is null, force value to null
      if (defaultSize == null)
         size = null;
      // if the provided value is the default value, store null
      else if (defaultSize.equals(size))
         size = null;

      m_size = size;
   }

   /**
    * Returns this column's dataTypeMap.
    *
    * @return The map, never <code>null<code>.
    */
   public PSJdbcDataTypeMap getDataTypeMap()
   {
      return m_dataTypeMap;
   }
   
   /**
    * Sets the flag to indicate if this column should limit its size based on 
    * the maximum defined in the datatype map (see {@link #getAdjustedSize()} 
    * for more info.  Defaults to <code>false</code> if never set.
    * 
    * @param limitSize <code>true</code> to limit the size based on the defined
    * maximum, <code>false</code> otherwise.
    */
   public void setLimitSizeForIndex(boolean limitSize)
   {
      m_limitSizeForIndex = limitSize;
   }

   /**
    * Gets the flag to indicate if this column should limit its size based on 
    * the maximum defined in the datatype map (see 
    * {@link #setLimitSizeForIndex(boolean)} for more info.  
    * 
    * @return <code>true</code> if the size will be limited based on the defined
    * maximum, <code>false</code> otherwise.
    */
   public boolean getLimitSizeForIndex()
   {
      return m_limitSizeForIndex;
   }   

   /**
    * Constructs this column's SQL syntax for a create/alter table statement.
    *
    * @param dbmsDef Used to account for any backend specific syntax
    * requirements.  May not be <code>null</code>.
    *
    * @return The syntax, never <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if dbmsDef is <code>null</code>.
    */
   public String getSqlDef(PSJdbcDbmsDef dbmsDef)
   {
      if (dbmsDef == null)
         throw new IllegalArgumentException("dbmsDef may not be null");

      StringBuffer buf = new StringBuffer();

      buf.append(getName());
      buf.append(" ");

      PSJdbcDataTypeMapping dataType = m_dataTypeMap.getMapping( m_jdbcType );
      String nativeDataType = dataType.getNative();

      String dbmsDefDriver = dbmsDef.getDriver();
      String dataTypeMapDriver = m_dataTypeMap.getDriver();
      if (dbmsDefDriver != null && dataTypeMapDriver != null &&
         dbmsDefDriver.trim().length() > 0 &&
         dataTypeMapDriver.trim().length() > 0)
      {
         if (dbmsDefDriver.equalsIgnoreCase(dataTypeMapDriver) &&
            m_nativeType != null &&
            m_nativeType.trim().length() > 0)
         {
            nativeDataType = m_nativeType;
         }
      }
      buf.append(nativeDataType);

      String size = getAdjustedSize(true);
      if (size != null)
      {
         buf.append("(");
         buf.append( size );
         String scale = getScale();
         if (scale != null)
            buf.append(",").append( scale );
         buf.append(")");
      }

      // add the suffix, if defined in the mapping
      if (dataType.getSuffix() != null)
         buf.append(" ").append( dataType.getSuffix() );

      String nullClause;
      if (m_allowsNull)
         nullClause = PSSqlHelper.getNullColumnSpecifier(dbmsDef.getDriver());
      else
         nullClause = " NOT NULL";

      buf.append(nullClause);

      return buf.toString();
   }


   /**
    * Ensures that this component has an action that is valid for an alter
    * table statement.  Overrides base class to additionally check if {@link
    * #allowsNull()} is <code>false</code> (cannot alter table with a
    * column that is NOT NULL unless default value is provided, and we don't
    * actually specify default values in the database).
    *
    * @return <code>true</code> if the component can be altered, <code>false
    * </code> if not.
    */
   @Override
   public boolean canAlter()
   {
      boolean canAlter = super.canAlter();
      if (canAlter && getAction() == ACTION_CREATE)
      {
         if (!allowsNull())
            canAlter = false;
         else if (m_defaultValue != null &&
            m_defaultValue.trim().length() > 0)
         {
            canAlter = false;
         }
      }
      return canAlter;
   }

   /**
    * Validates params, ensuring it is either <code>null</code> or not empty.
    *
    * @param params The params value to validate.
    */
   private boolean validateParams(String params)
   {
      boolean valid = true;
      if (params != null && params.trim().length() == 0)
         valid = false;

      return valid;
   }

   /**
    * Get the size to use for this column, adjusting the size based on the
    * value of {@link PSJdbcDataTypeMap#getMaxIndexColSize()} in the map 
    * supplied during construction.  Only adjusts if this column should limit
    * its size and the jdbctype of this column is a <code>CHAR</code> or 
    * <code>VARCHAR</code> and the map specifies a max size that is not 
    * <code>-1</code>. 
    * 
    * @param doLog <code>true</code> to log if column size is adjusted, 
    * <code>false</code> to supress logging. 
    * 
    * @return The possibly adjusted size, may be <code>null</code>, never empty.
    */
   public String getAdjustedSize(boolean doLog)
   {
      String strAdjSize = getSize();
      
      try      
      {
         long maxSize = m_dataTypeMap.getMaxIndexColSize();
         if (m_limitSizeForIndex && maxSize != -1 && m_size != null && 
            (m_jdbcType == Types.CHAR || m_jdbcType == Types.VARCHAR))
         {
            long size = Long.parseLong(m_size);
            if (size > maxSize)
            {
               strAdjSize = String.valueOf(maxSize);
               if (doLog)
               {
                  PSJdbcTableFactory.logMessage("Adjusting size of column " + 
                     getName() + " from " + m_size + " to " + strAdjSize + 
                     " due to defined maximum for current driver.");
               }
            }
         }
      }
      catch (NumberFormatException e)
      {
         // non-numeric value for size specified, ignore
      }
      
      return strAdjSize;
   }
   
   /**
    * The name of this object's root Xml element.
    */
   public static String NODE_NAME = "column";


   /**
    * The jdbc type of this column.  Set in ctor, may be modified by a call to
    * {@link #setType(int)} or {@link #setType(String)}.
    */
   private int m_jdbcType;

   /**
    * The String representation of the jdbc type of this column.  Set in ctor,
    * may be modified by a call to {@link #setType(int)} or {@link
    * #setType(String)}. Never <code>null</code> after construction.
    */
   private String m_jdbcTypeString = null;

   /**
    * The parameters that are part of this column's datatype definition, usually
    * the size.  Initialized in the ctor, may be <code>null</code>, never empty.
    */
   private String m_size = null;

   /**
    * The number of digits to the right of the decimal point in numeric data
    * types.  Included inside the parameter parenthesis with the size in the
    * generated SQL definition.  Will be <code>null</code> if the data type
    * does not support scale, or if the dataTypeMap's default scale should be
    * used.  Never empty.
    */
   private String m_scale = null;

   /**
    * Determines if this column allows <code>NULL</code> values.  If <code>true
    * </code> it does, otherwise not.  Default is to allow them.
    */
   private boolean m_allowsNull = true;

   /**
    * The value to use when adding a non-nullable column to a table.  May be
    * <code>null</code> only if {@link #m_allowsNull} is <code>true</code>.
    */
   private String m_defaultValue = null;

   /**
    * The jdbc to native data type mappings, initialized at contruction, never
    * <code>null</code> or modified after that.
    */
   private PSJdbcDataTypeMap m_dataTypeMap = null;

   /**
    * Used by {@link #isChanged(PSJdbcColumnDef)} to compare a new column
    * with this column to determine if the new column defintion specifies
    * any changes.  <code>null</code> unless set by a call to
    * {@link #setNativeType(String) setNativeType}, never <code>null</code>
    * after that.
    */
   private String m_nativeType = null;

   /**
    * the sequence name, may be <code>null</code> or empty if this is not a
    * key column.
    * If this column is a sequence in the database table then
    * this variable contains the name of the sequence which
    * will be used to obtain this column's value.
    */
   private String m_sequence = null;
   
   /**
    * Flag to indicate if this column should limit its size based on the 
    * maximum defined in the datatype map 
    * (see {@link #setLimitSizeForIndex(boolean)} for more info.  Modified by 
    * {@link #fromXml(Element)} and {@link #setLimitSizeForIndex(boolean)}.
    */
   private boolean m_limitSizeForIndex = false;

   // Xml elements and attributes
   private static final String JDBC_EL = "jdbctype";
   private static final String SIZE_EL = "size";
   private static final String SCALE_EL = "scale";
   private static final String ALLOWS_NULL_EL = "allowsnull";
   private static final String DEFAULT_VALUE_EL = "defaultvalue";
   private static final String SEQUENCE_EL = "sequence";
   private static final String LIMIT_SIZE_ATTR = "limitSizeForIndex";
}

