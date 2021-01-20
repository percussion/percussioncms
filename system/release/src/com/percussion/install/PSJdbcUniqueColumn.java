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
package com.percussion.install;

// java
import com.percussion.tablefactory.IPSJdbcTableDataHandler;
import com.percussion.tablefactory.IPSTableFactoryErrors;
import com.percussion.tablefactory.PSJdbcColumnData;
import com.percussion.tablefactory.PSJdbcColumnDef;
import com.percussion.tablefactory.PSJdbcDataTypeMap;
import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.tablefactory.PSJdbcRowData;
import com.percussion.tablefactory.PSJdbcTableFactoryException;
import com.percussion.tablefactory.PSJdbcTableSchema;
import com.percussion.xml.PSXmlTreeWalker;

import java.sql.Connection;
import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Data handler object which makes all the values of the column (specified in
 * the handler's Xml) unique for adding unique constraint on this column.
 *
 * All the <code>null</code> values in this column are modified based on the
 * value specified in the handler's Xml.
 * The first <code>null</code> value is assigned a value obtained by
 * concatenation of strings <code>m_value</code> and "0". For example, if
 * <code>m_value</code> is "CONTENTTYPENAME", then first <code>null</code>
 * value is modified to "CONTENTTYPENAME0", the second <code>null</code> value
 * is modified to "CONTENTTYPENAME1" and so on.
 *
 * If any duplicate value is found, then an index is added to the value to
 * make the value unique. For example, if the column has three rows with value
 * "Article", then the three values after modification are "Article", "Article1"
 * and "Article2".
 *
 * NOTE : DB2 does not allow unique constraint on nullable columns. So all
 * columns which comprise of a unique key constraint should be non-nullable.
 */
public class PSJdbcUniqueColumn implements IPSJdbcTableDataHandler
{
   /**
    * @see com.percussion.tablefactory.IPSJdbcTableDataHandler
    *
    * @throws IllegalArgumentException if <code>dbmsDef</code> or
    * <code>destTableSchema</code> is <code>null</code>
    */
   public void init(PSJdbcDbmsDef dbmsDef, Connection conn,
      PSJdbcTableSchema srcTableSchema, PSJdbcTableSchema destTableSchema)
       throws PSJdbcTableFactoryException
   {
      if (dbmsDef == null)
         throw new IllegalArgumentException("dbmsDef may not be null");

      if (destTableSchema == null)
         throw new IllegalArgumentException("destTableSchema may not be null");

      m_dbmsDef = dbmsDef;
      m_tblSchema = destTableSchema;

      // check if the schema contains the column <code>m_column</code>
      PSJdbcColumnDef colDef = m_tblSchema.getColumn(m_column);
      if (colDef == null)
      {
         Object args[] = {m_tblSchema.getName() , m_column};
         throw new PSJdbcTableFactoryException(
            IPSTableFactoryErrors.COLUMN_NOT_FOUND, args);
      }
   }

   /**
    * @see com.percussion.tablefactory.IPSJdbcTableDataHandler
    */
   public PSJdbcRowData execute(Connection conn, PSJdbcRowData row)
      throws PSJdbcTableFactoryException
   {
      if (conn == null)
         throw new IllegalArgumentException("conn may not be null");

      if (row == null)
         return row;

      PSJdbcDataTypeMap dataTypeMap = null;
      try
      {
         dataTypeMap = new PSJdbcDataTypeMap(m_dbmsDef.getBackEndDB(),
            m_dbmsDef.getDriver(), null);
      }
      catch (Exception e)
      {
         throw new PSJdbcTableFactoryException(
            IPSTableFactoryErrors.LOAD_DEFAULT_DATATYPE_MAP, e.toString(), e);
      }

      // add the column value to the <code>m_colValues</code>
      PSJdbcColumnData colData = row.getColumn(m_column);
      if (colData == null)
      {
         colData = getColumnValue(null);
         row.addColumn(colData);
      }
      else
      {
         String colValue = colData.getValue();
         colData = getColumnValue(colValue);
         row.removeColumn(m_column);
         row.addColumn(colData);
      }
      return row;
   }

   /**
    * Returns a column data object containing the modified value for the
    * column which is being made altered.
    *
    * @param value the current value of the column in the database, may be
    * <code>null</code>
    *
    * @return the modified column value, may not be <code>null</code>
    *
    * @see getUniqueColumnValue
    */
   protected PSJdbcColumnData getColumnValue(String value)
   {
      return getUniqueColumnValue(value);
   }

   /**
    * Checks if the value specified by <code>value</code> already exists
    * in the list <code>m_colValues</code>. If the value already exists then
    * it is modified appropriately. If <code>value</code> is non-<code>null</code>
    * then an index is added to the column value to make it unique. If value
    * is <code>null</code> then a value based on <code>m_value</code> is
    * returned.
    *
    * @param value the value of the column <code>m_column</code>, may be
    * <code>null</code> or empty. It is <code>null</code> if the paramter
    * <code>row</code> to the <code>execute()</code> method does not contain
    * the column <code>m_column</code>.
    *
    * @return column data object containing the modified value of column
    * <code>m_column</code>, never <code>null</code>
    */
   private PSJdbcColumnData getUniqueColumnValue(String value)
   {
      String colValue = null;
      String baseValue = null;
      if (value == null)
      {
         colValue = m_value + "0";
         baseValue = m_value;
      }
      else
      {
         colValue = value;
         baseValue = value;
      }

      int i = 1;
      while (m_colValues.contains(colValue.toUpperCase()))
      {
         colValue = baseValue + i;
         i++;
      }
      m_colValues.add(colValue.toUpperCase());
      return new PSJdbcColumnData(m_column, colValue);
   }

   /**
    * @see com.percussion.tablefactory.IPSJdbcTableDataHandler
    */
   public void close(Connection conn)  throws PSJdbcTableFactoryException
   {
   }

   /**
    * @see com.percussion.tablefactory.IPSJdbcTableDataHandler
    * See {@link #toXml(Document)} for the DTD that <code>sourceNode</code>
    * should follow.
    */
   public void fromXml(Element sourceNode) throws PSJdbcTableFactoryException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode may not be null");

      if (!sourceNode.getNodeName().equals(IPSJdbcTableDataHandler.NODE_NAME))
      {
         Object[] args = {IPSJdbcTableDataHandler.NODE_NAME, sourceNode.getNodeName()};
         throw new PSJdbcTableFactoryException(
            IPSTableFactoryErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      PSXmlTreeWalker walker = new PSXmlTreeWalker(sourceNode);
      int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

      Element columnEl = walker.getNextElement(COLUMN_EL, firstFlags);
      if (columnEl == null)
      {
         throw new PSJdbcTableFactoryException(
            IPSTableFactoryErrors.XML_ELEMENT_NULL, COLUMN_EL);
      }

      // get the name attribute
      String sTemp = columnEl.getAttribute(COLUMN_NAME_ATTR);
      if (sTemp == null || sTemp.trim().length() == 0)
      {
         Object[] args = {COLUMN_EL, COLUMN_NAME_ATTR,
            sTemp == null ? "null" : sTemp};
         throw new PSJdbcTableFactoryException(
            IPSTableFactoryErrors.XML_ELEMENT_INVALID_ATTR, args);
      }
      m_column = sTemp;

      // get the value attribute
      sTemp = columnEl.getAttribute(COLUMN_VALUE_ATTR);
      if (sTemp == null)
      {
         Object[] args = {COLUMN_EL, COLUMN_VALUE_ATTR, "null"};
         throw new PSJdbcTableFactoryException(
            IPSTableFactoryErrors.XML_ELEMENT_INVALID_ATTR, args);
      }
      m_value = sTemp;
   }

   /**
    * @see com.percussion.tablefactory.IPSJdbcTableDataHandler
    *
    * The child element of the datahandler element
    * <code>IPSJdbcTableDataHandler.NODE_NAME</code> is based on the following
    * DTD :
    *
    * &lt;!--
    * Specifies the column whose values should be made unique.
    * Attributes:
    * name - The name of the column
    * value - Base value to be used for <code>null</code> values in the
    * specified column
    * -->
    *
    * &lt;!ELEMENT column>
    * &lt;!ATTLIST column
    *    name CDATA #REQUIRED
    *    value CDATA #REQUIRED
    * >
    *
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      // create the root element
      Element   root = doc.createElement(IPSJdbcTableDataHandler.NODE_NAME);
      String className = this.getClass().getName();
      root.setAttribute(IPSJdbcTableDataHandler.CLASS_ATTR, className);

      Element columnEl = doc.createElement(COLUMN_EL);
      columnEl.setAttribute(COLUMN_NAME_ATTR, m_column);
      columnEl.setAttribute(COLUMN_VALUE_ATTR, m_value);

      root.appendChild(columnEl);
      return root;
   }

   /**
    * provides the database/schema information for the table, initialized in the
    * <code>init()</code> method, never <code>null</code> after initialization
    */
   protected PSJdbcDbmsDef m_dbmsDef = null;

   /**
    * Schema of the table which contains the column specified by
    * <code>m_column</code>, initialized in the <code>init()</code> method,
    * never <code>null</code> after initialization. This table schema should
    * contain the column specified by <code>m_column</code>.
    */
   protected PSJdbcTableSchema m_tblSchema = null;

   /**
    * Name of the column whose values are to be made unique, initialized in the
    * <code>fromXml()</code> method, never <code>null</code> or empty after
    * initialization
    */
   protected String m_column = null;

   /**
    * Base value to be used for <code>null</code> values in the specified
    * column, initialized in the <code>fromXml()</code> method,
    * never <code>null</code> after initialization, may be empty.
    */
   protected String m_value = null;

   /**
    * List of values for the column <code>m_column</code> obtained from the
    * row of data <code>row</code> passed as argument to the
    * <code>execute()</code> method. Column values are stored as
    * <code>String</code> objects in UPPERCASE. This list is never
    * <code>null</code>, may be empty.
    */
   private Set m_colValues = new HashSet();

   // Xml elements and attributes
   protected static final String COLUMN_EL = "column";
   protected static final String COLUMN_NAME_ATTR = "name";
   protected static final String COLUMN_VALUE_ATTR = "value";

}


