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

import com.percussion.xml.PSXmlTreeWalker;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;

/**
 * PSJdbcTableMapping is used to manipulate rows obtained from one table so
 * that it can be inserted/updated into another table. The
 * <code>processTable()</code> method iterates through all the rows and
 * modifies the column names based on the mappings that have been added through
 * the <code>addRowMapping()</code> method. Each row that is iterated
 * in the <code>processTable()</code> method results in one or more rows for the
 * destination table based on the number of mappings added. For each row from
 * the source table, the number of rows generated for the destination table is
 * equal to the number of row mappings (<code>IPSJdbcRowMapping</code>) added
 * through the <code>addRowMapping()</code> method. This is because each row
 * mapping (<code>IPSJdbcRowMapping</code> object) results in one row for the
 * destination table.
 */
public class PSJdbcTableMapping
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
   public PSJdbcTableMapping(PSJdbcDbmsDef dbmsDef, PSJdbcTableSchema destTableSchema)
   {
      if (dbmsDef == null)
         throw new IllegalArgumentException("dbmsDef may not be null");
      if (destTableSchema == null)
         throw new IllegalArgumentException("destTableSchema may not be null");

      m_dbmsDef = dbmsDef;
      m_tableSchema = destTableSchema;
   }

   /**
    * Processes the rows obtained from the source table, applies all the
    * mappings added through the <code>addRowMapping()</code> method and returns
    * table data object which can be used to insert/update data
    * into the destination table specified by the <code>destTableSchema</code>
    * parameter in the constructor.
    *
    * @param conn the database connection to use, may not be <code>null</code>
    * @param tblData object containing all the rows of data
    * obtained from the source table, may not be <code>null</code>, may not
    * contain any row of data
    *
    * @return the transformed table data which can be used to insert/update
    * data into the destination table specified by the
    * <code>destTableSchema</code> parameter in the constructor,
    * never <code>null</code>
    *
    * @throws IllegalArgumentException if <code>conn</code> or
    * <code>tblData</code> is <code>null</code>
    * @throws PSJdbcTableFactoryException if any column specified in row mapping
    * is not found in any row of this table data.
    */
   public PSJdbcTableData processTable(Connection conn, PSJdbcTableData tblData)
      throws PSJdbcTableFactoryException
   {
      if (conn == null)
         throw new IllegalArgumentException("conn may not be null");
      if (tblData == null)
         throw new IllegalArgumentException("tblData may not be null");

      Iterator it = m_rowMapList.iterator();
      while (it.hasNext())
      {
         IPSJdbcRowMapping rowMapping = (IPSJdbcRowMapping)it.next();
         rowMapping.setTableData(tblData);
      }

      List rowList = new ArrayList();
      Iterator rows = tblData.getRows();
      while (rows.hasNext())
      {
         PSJdbcRowData row = (PSJdbcRowData)rows.next();
         Iterator mapIt = m_rowMapList.iterator();
         while (mapIt.hasNext())
         {
            IPSJdbcRowMapping rowMapping = (IPSJdbcRowMapping)mapIt.next();
            rowList.add(rowMapping.processRow(conn, row));
         }
      }
      return new PSJdbcTableData(m_tableSchema.getName(), rowList.iterator());
   }

   /**
    * Processes the row obtained from the source table, applies all the
    * mappings added through the <code>addRowMapping</code> method and returns
    * a list of <code>PSJdbcRowData</code> objects which can be used to
    * insert/update data into the destination table specified by the
    * <code>destTableSchema</code> parameter in the constructor.
    *
    * @param conn the database connection to use, may not be <code>null</code>
    * @param row a row data object from the source table,
    * may not be <code>null</code>

    * @return a list of <code>PSJdbcRowData<code> objects containing the
    * transformed row data which can be used to insert/update data into the
    * destination table specified by the <code>destTableSchema</code>
    * parameter in the constructor.
    *
    * @throws IllegalArgumentException if <code>conn</code> or
    * <code>srcRow</code> is <code>null</code>
    * @throws PSJdbcTableFactoryException if any column specified in row mapping
    * is not found in srcRow.
    */
   public List processRow(Connection conn, PSJdbcRowData srcRow)
      throws PSJdbcTableFactoryException
   {
      if (conn == null)
         throw new IllegalArgumentException("conn may not be null");
      if (srcRow == null)
         throw new IllegalArgumentException("srcRow may not be null");

      List rowList = new ArrayList();
      Iterator it = m_rowMapList.iterator();
      while (it.hasNext())
      {
         IPSJdbcRowMapping rowMapping = (IPSJdbcRowMapping)it.next();
         rowList.add(rowMapping.processRow(conn, srcRow));
      }
      return rowList;
   }

   /**
    * Adds a row mapping rule to apply to each row during processing of rows
    * in the <code>processTable()</code> method.
    *
    * @param rowMapping row mapping rule, never <code>null</code>
    *
    * @throws IllegalArgumentException if mapping is <code>null</code>
    */
   public void addRowMapping(IPSJdbcRowMapping rowMapping)
   {
      if (rowMapping == null)
         throw new IllegalArgumentException("rowMapping may not be null");
      m_rowMapList.add(rowMapping);
   }

   /**
    * Restores row mapping rules from Xml representation and then adds them
    * to its internal list. These row mapping rules are applied to each row
    * during processing of rows in the <code>processTable()</code> method.
    *
    * @param sourceNode The element from which to get row mapping rules.
    * May not be <code>null</code>. The tag name of this node must equal the
    * value of <code>NODE_NAME</code>.
    *
    * @throws IllegalArgumentException if <code>sourceNode</code> is
    * <code>null</code>.
    * @throws PSJdbcTableFactoryException if there are any errors.
    */
   public void addRowMapping(Element sourceNode)
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

      // load row maps
      m_rowMapList.clear();
      Element rowMapEl = walker.getNextElement(PSJdbcRowMapping.NODE_NAME, firstFlags);
      while (rowMapEl != null)
      {
         PSJdbcRowMapping rowMapping = new PSJdbcRowMapping(
            m_dbmsDef, m_tableSchema);
         rowMapping.fromXml(rowMapEl);
         m_rowMapList.add(rowMapping);
         rowMapEl = walker.getNextElement(PSJdbcRowMapping.NODE_NAME, nextFlags);
      }
   }

   /**
    * The database where the tables are located, initialized in the
    * constructor, never <code>null</code> after that.
    */
   private PSJdbcDbmsDef m_dbmsDef = null;

   /**
    * The schema for destination table for which this mapper object is creating
    * table data, initialized in the constructor, never <code>null</code> after
    * that.
    */
   private PSJdbcTableSchema m_tableSchema = null;

   /**
    * List of row mapping rules specified by IPSJdbcRowMapping objects,
    * never <code>null</code>, may be empty
    */
   private List m_rowMapList = new ArrayList();

   /**
    * The name of this objects root Xml element.
    */
   public static final String NODE_NAME = "tableMap";

}



