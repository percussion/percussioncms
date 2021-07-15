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
package com.percussion.install;

// java
import com.percussion.data.PSIdGenerator;
import com.percussion.tablefactory.IPSJdbcTableDataHandler;
import com.percussion.tablefactory.IPSTableFactoryErrors;
import com.percussion.tablefactory.PSJdbcColumnData;
import com.percussion.tablefactory.PSJdbcColumnDef;
import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.tablefactory.PSJdbcRowData;
import com.percussion.tablefactory.PSJdbcTableFactoryException;
import com.percussion.tablefactory.PSJdbcTableSchema;
import com.percussion.xml.PSXmlTreeWalker;

import java.sql.Connection;
import java.sql.SQLException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Data handler object which makes all the values of the column (specified in
 * the handler's Xml) unique based on the NextNumber table.
 */
public class PSJdbcNextNumberColumn implements IPSJdbcTableDataHandler
{
   /*
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

      m_conn = conn;
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

   /*
    * @see com.percussion.tablefactory.IPSJdbcTableDataHandler
    */
   public PSJdbcRowData execute(Connection conn, PSJdbcRowData row)
      throws PSJdbcTableFactoryException
   {
      if (conn == null)
         throw new IllegalArgumentException("conn may not be null");

      if (row == null)
         return row;

      try
      {
         PSJdbcColumnData colData = row.getColumn(m_column);
         if (colData == null)
         {
            colData = getColumnValue(null);
         }
         else
         {
            String colValue = colData.getValue();
            colData = getColumnValue(colValue);
            row.removeColumn(m_column);
         }

         row.addColumn(colData);
      }
      catch (Exception e)
      {
         throw new PSJdbcTableFactoryException(0, e);
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
    * @throws PSJdbcTableFactoryException
    * @throws SQLException
    * @see getUniqueColumnValue
    */
   protected PSJdbcColumnData getColumnValue(String value)
      throws SQLException, PSJdbcTableFactoryException
   {
      return getUniqueColumnValue(value);
   }


   /* (non-Javadoc)
    * @see com.percussion.install.PSJdbcUniqueColumn#getUniqueColumnValue(java.lang.String)
    */
   protected PSJdbcColumnData getUniqueColumnValue(String value)
      throws SQLException, PSJdbcTableFactoryException
   {
      if (value==null || value.trim().length() < 1)
         value = "" + getNextNumber(m_nextNumberKey);

      return new PSJdbcColumnData(m_column, value);
   }

   /**
    * Get the next number for the supplied key.
    *
    * @param key the key to get the next number for, not <code>null</code>
    * or empty.
    * @return the next number.
    * @throws PSJdbcTableFactoryException if anything goes wrong looking
    * up next number for a given key.
    */
   private int getNextNumber(String key) throws PSJdbcTableFactoryException
   {
      if (key == null || key.trim().length() == 0)
         throw new IllegalArgumentException("key may not be null or empty");

      try
      {
         return PSIdGenerator.getNextIdBlock(key, 1)[0];
      }
      catch (SQLException e)
      {
         throw new PSJdbcTableFactoryException(0, e);
      }
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

      // get the nextNumberKey attribute
      sTemp = columnEl.getAttribute(NEXT_NUMBER_KEY_ATTR);
      if (sTemp == null || sTemp.trim().length() == 0)
      {
         Object[] args = {COLUMN_EL, NEXT_NUMBER_KEY_ATTR,
            sTemp == null ? "null" : sTemp};
         throw new PSJdbcTableFactoryException(
            IPSTableFactoryErrors.XML_ELEMENT_INVALID_ATTR, args);
      }

      m_nextNumberKey = sTemp;
   }

   /**
    * @see com.percussion.tablefactory.IPSJdbcTableDataHandler
    *
    * The child element of the datahandler element
    * <code>IPSJdbcTableDataHandler.NODE_NAME</code> is based on the following
    * DTD :
    *
    * &lt;!--
    * Specifies the column whose values should be based on NextNumber table and
    * the value of the key to lookup in the NextNumber table.
    *
    * Attributes:
    * name - The name of the column
    * value - Base value to be used for <code>null</code> values in the
    * specified column
    * -->
    *
    * &lt;!ELEMENT column>
    * &lt;!ATTLIST column
    *    name CDATA #REQUIRED
    * >
    *
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      // create the root element
      Element  root = doc.createElement(IPSJdbcTableDataHandler.NODE_NAME);
      String className = this.getClass().getName();
      root.setAttribute(IPSJdbcTableDataHandler.CLASS_ATTR, className);

      Element columnEl = doc.createElement(COLUMN_EL);
      columnEl.setAttribute(COLUMN_NAME_ATTR, m_column);
      columnEl.setAttribute(NEXT_NUMBER_KEY_ATTR, m_nextNumberKey);

      root.appendChild(columnEl);
      return root;
   }

   /* (non-Javadoc)
    * @see com.percussion.tablefactory.IPSJdbcTableDataHandler#close(java.sql.Connection)
    */
   public void close(Connection conn) throws PSJdbcTableFactoryException {
      // TODO Auto-generated method stub

   }

   /**
    * DB Connection.
    */
   private Connection m_conn = null;

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
    * NextNumber table Key name, never <code>null</code> or <code>empty</code>.
    */
   protected String m_nextNumberKey = null;

   // Xml elements and attributes
   protected static final String COLUMN_EL = "column";
   protected static final String COLUMN_NAME_ATTR = "name";
   protected static final String NEXT_NUMBER_KEY_ATTR = "nextNumberKey";
}


