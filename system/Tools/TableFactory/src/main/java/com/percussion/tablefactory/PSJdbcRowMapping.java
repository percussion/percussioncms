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

import com.percussion.xml.PSXmlTreeWalker;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

/**
 * PSJdbcRowMapping defines a mapping from a row of source table to a row of
 * destination table. The <code>processRow()</code> method performs the actual
 * mapping. It takes a row from the source table and modifies the column names
 * based on this mapping rule. It can also add new columns to the tranformed row.
 */
public class PSJdbcRowMapping implements IPSJdbcRowMapping
{
   /**
    * Constructor
    *
    * @param dbmsDef the database where the tables are located,
    * may not be <code> null</code>.
    * @param destTableSchema the schema for destination table for which this mapper
    * object is creating table data, may not be <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>dbmsDef</code> or
    * <code>destTableSchema</code> is <code>null</code>
    */
   public PSJdbcRowMapping(PSJdbcDbmsDef dbmsDef,
      PSJdbcTableSchema destTableSchema)
   {
      if (dbmsDef == null)
         throw new IllegalArgumentException("dbmsDef may not be null");
      if (destTableSchema == null)
         throw new IllegalArgumentException("destTableSchema may not be null");

      m_dbmsDef = dbmsDef;
   }

   /**
    * @see com.percussion.tablefactory.IPSJdbcRowMapping
    */
   public void addColumnMapping(String srcTblColName, String destTblColName)
   {
      if ((srcTblColName == null) || (srcTblColName.trim().length() < 1))
         throw new IllegalArgumentException("srcTblColName may not be null or empty");

      if ((destTblColName == null) || (destTblColName.trim().length() < 1))
         throw new IllegalArgumentException("destTblColName may not be null or empty");

      m_colMapping.put(srcTblColName, destTblColName);
   }

   /**
    * @see com.percussion.tablefactory.IPSJdbcRowMapping
    */
   public void addColumn(PSJdbcColumnData destTblColData)
   {
      if (destTblColData == null)
         throw new IllegalArgumentException("destTblColData may not be null");
      m_defColValues.add(destTblColData);
   }

   /**
    * @see com.percussion.tablefactory.IPSJdbcRowMapping
    */
   public void setTableData(PSJdbcTableData tblData)
   {
      if (tblData == null)
         throw new IllegalArgumentException("tblData may not be null");
      m_tblData = tblData;
   }

   /**
    * @see com.percussion.tablefactory.IPSJdbcRowMapping
    */
   public void setRowAction(int rowAction)
   {
      m_rowAction = rowAction;
   }

   /**
    * @see com.percussion.tablefactory.IPSJdbcRowMapping
    */
   public PSJdbcRowData processRow(Connection conn, PSJdbcRowData srcRow)
      throws PSJdbcTableFactoryException
   {
      if (conn == null)
         throw new IllegalArgumentException("conn may not be null");
      if (srcRow == null)
         throw new IllegalArgumentException("srcRow may not be null");

      verifyColumnMapping(srcRow);

      List colDataList = new ArrayList();
      Iterator rowIt = srcRow.getColumns();
      while (rowIt.hasNext())
      {
         PSJdbcColumnData srcColData = (PSJdbcColumnData)rowIt.next();
         PSJdbcColumnData destColData = processColumn(srcColData);
         if (destColData != null)
            colDataList.add(destColData);
      }
      if (m_defColValues.size() > 0)
      {
         // add the columns with default values
         for (int i=0; i < m_defColValues.size(); i++)
            colDataList.add((PSJdbcColumnData)m_defColValues.get(i));
      }
      return new PSJdbcRowData(colDataList.iterator(), m_rowAction);
   }

   /**
    * Restore this object from an Xml representation.
    *
    * @param sourceNode The element from which to get this object's state,
    * may not be <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>sourceNode</code> is
    * <code>null</code>.
    * @throws PSJdbcTableFactoryException if there are any errors.
    */
   public void fromXml(Element sourceNode)
      throws PSJdbcTableFactoryException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode may not be null");

      if (!sourceNode.getNodeName().equals(NODE_NAME))
      {
         Object[] args = {NODE_NAME, sourceNode.getNodeName()};
         throw new PSJdbcTableFactoryException(
            IPSTableFactoryErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      PSXmlTreeWalker walker = new PSXmlTreeWalker(sourceNode);
      int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

      // load column maps
      m_colMapping.clear();
      Element columnsMapEl = walker.getNextElement(COLUMNS_MAP_EL, firstFlags);
      if (columnsMapEl != null)
      {
         Element columnMapEl = walker.getNextElement(COLUMN_MAP_EL, firstFlags);
         while (columnMapEl != null)
         {
            //source column
            String sTemp = columnMapEl.getAttribute(SRC_COLUMN_ATTR);
            if (sTemp == null || sTemp.trim().length() == 0)
            {
               Object[] args = {NODE_NAME, SRC_COLUMN_ATTR,
                     sTemp == null ? "null" : sTemp};
               throw new PSJdbcTableFactoryException(
                  IPSTableFactoryErrors.XML_ELEMENT_INVALID_ATTR, args);
            }
            String key = sTemp;

            // destination column
            sTemp = columnMapEl.getAttribute(DEST_COLUMN_ATTR);
            if (sTemp == null || sTemp.trim().length() == 0)
            {
               Object[] args = {NODE_NAME, DEST_COLUMN_ATTR,
                     sTemp == null ? "null" : sTemp};
               throw new PSJdbcTableFactoryException(
                  IPSTableFactoryErrors.XML_ELEMENT_INVALID_ATTR, args);
            }
            String value = sTemp;
            m_colMapping.put(key, value);
            columnMapEl = walker.getNextElement(COLUMN_MAP_EL, nextFlags);
         }
      }

      // load default column values
      m_defColValues.clear();
      walker.setCurrent(sourceNode);
      Element columnsValueEl = walker.getNextElement(COLUMNS_VALUE_EL, firstFlags);
      if (columnsValueEl != null)
      {
         Element columnValueEl = walker.getNextElement(COLUMN_VALUE_EL, firstFlags);
         while (columnValueEl != null)
         {
            // destination column
            String sTemp = columnValueEl.getAttribute(DEST_COLUMN_ATTR);
            if (sTemp == null || sTemp.trim().length() == 0)
            {
               Object[] args = {NODE_NAME, DEST_COLUMN_ATTR,
                     sTemp == null ? "null" : sTemp};
               throw new PSJdbcTableFactoryException(
                  IPSTableFactoryErrors.XML_ELEMENT_INVALID_ATTR, args);
            }
            String colName = sTemp;

            // value
            sTemp = columnValueEl.getAttribute(VALUE_ATTR);
            if (sTemp == null || sTemp.trim().length() == 0)
            {
               Object[] args = {NODE_NAME, VALUE_ATTR,
                     sTemp == null ? "null" : sTemp};
               throw new PSJdbcTableFactoryException(
                  IPSTableFactoryErrors.XML_ELEMENT_INVALID_ATTR, args);
            }
            String colValue = sTemp;
            PSJdbcColumnData colData = new PSJdbcColumnData(colName, colValue);
            m_defColValues.add(colData);
            columnValueEl = walker.getNextElement(COLUMN_VALUE_EL, nextFlags);
         }
      }
   }

   /**
    * Verifies that all the columns added to the column mapping Map though
    * the <code>addColumnMapping()</code> method are present in the input row parameter.
    * If any column specified in the mapping is not present then it throws
    * <code>PSJdbcTableFactoryException</code> exception with error code
    * <code>IPSTableFactoryErrors.COLUMN_NOT_FOUND</code>
    *
    * @param row the row data against which the column mapping is
    * to be checked, may not be <code>null</code>
    *
    * @throws IllegalArgumentException if <code>row</code> is <code>null</code>
    * @throws PSJdbcTableFactoryException if any column specified in the mapping
    * is not present in <code>row</code>
    */
   private void verifyColumnMapping(PSJdbcRowData row)
      throws PSJdbcTableFactoryException
   {
      if (row == null)
         throw new IllegalArgumentException("row may not be null");

      Iterator it = m_colMapping.keySet().iterator();
      while (it.hasNext())
      {
         String srcColName = (String)it.next();
         PSJdbcColumnData srcColData = row.getColumn(srcColName);
         if (srcColData == null)
         {
            // failed to find the column specified in the mapping
            String tableName = "";
            if (m_tblData != null)
               tableName = m_tblData.getName();
            Object[] args = {tableName, srcColName};
            throw new PSJdbcTableFactoryException(
               IPSTableFactoryErrors.COLUMN_NOT_FOUND,
               args);
         }
      }
   }

   /**
    * Transforms a single column. If a mapping for this column has been added
    * through the <code>addColumnMapping()</code> method then it creates a new
    * <code>PSJdbcColumnData</code> object having the same value and encoding
    * as the <code>srcColData</code> parameter but having the transformed
    * column name and returns this newly created <code>PSJdbcColumnData</code>
    * object. If no mapping has been added for this column then it returns
    * <code>null</code>.
    *
    * @param srcColData the column to transform, may not be <code>null</code>
    *
    * @return the transformed column data, or <code>null</code> if no column
    * mapping was defined for this column
    *
    * @throws IllegalArgumentException if <code>srcColData</code> is
    * <code>null</code>
    */
   private PSJdbcColumnData processColumn(PSJdbcColumnData srcColData)
   {
      if (srcColData == null)
         throw new IllegalArgumentException("colData may not be null");

      if (m_colMapping.size() < 1)
         return null;

      String srcColName = srcColData.getName();
      if (!m_colMapping.containsKey(srcColName))
         return null;

      String destColName = (String)m_colMapping.get(srcColName);
      return new PSJdbcColumnData(destColName, srcColData.getValue(),
         srcColData.getEncoding());
   }

   /**
    * The database where the tables are located, initialized in the
    * constructor, never <code>null</code> after that.
    */
   private PSJdbcDbmsDef m_dbmsDef = null;

   /**
    * table data object containing the rows on which this mapping is performed,
    * initialized in the setTableData method,
    * never <code>null</code> after that, may not contain any row of data
    */
   private PSJdbcTableData m_tblData = null;

   /**
    * map containing source table column names as key and destination table
    * column names as value, never <code>null</code>, may be empty
    */
   private Map m_colMapping = new HashMap();

   /**
    * an array containing PSJdbcColumnData objects that should be added to
    * each row for the destination table, never <code>null</code>, may
    * be empty if no column having a default value needs to be added to
    * the row for destination table.
    */
   private List m_defColValues = new ArrayList();

   /**
    * row action that should be set for the rows for the destination
    * table, defaults to insert.
    */
   private int m_rowAction = PSJdbcRowData.ACTION_INSERT;

   /**
    * The name of this objects root Xml element.
    */
   public static final String NODE_NAME = "rowMap";

   // Xml elements and attributes
   private static final String COLUMNS_MAP_EL = "columnsMap";
   private static final String COLUMN_MAP_EL = "columnMap";
   private static final String DEST_COLUMN_ATTR = "destColumn";
   private static final String SRC_COLUMN_ATTR = "srcColumn";
   private static final String COLUMNS_VALUE_EL = "columnsValue";
   private static final String COLUMN_VALUE_EL = "columnValue";
   private static final String VALUE_ATTR = "value";
}

