/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.percussion.install;

import com.percussion.tablefactory.IPSJdbcTableDataHandler;
import com.percussion.tablefactory.IPSTableFactoryErrors;
import com.percussion.tablefactory.PSJdbcDataTypeMap;
import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.tablefactory.PSJdbcExecutionStep;
import com.percussion.tablefactory.PSJdbcResultSetIteratorStep;
import com.percussion.tablefactory.PSJdbcRowData;
import com.percussion.tablefactory.PSJdbcStatementFactory;
import com.percussion.tablefactory.PSJdbcTableFactory;
import com.percussion.tablefactory.PSJdbcTableFactoryException;
import com.percussion.tablefactory.PSJdbcTableMapping;
import com.percussion.tablefactory.PSJdbcTableSchema;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

/**
 * Data handler object which wraps the functionality of
 * <code>PSJdbcTableMapping</code> object.
 */
public class PSJdbcTableMapper implements IPSJdbcTableDataHandler
{
   /**
    * @see com.percussion.tablefactory.IPSJdbcTableDataHandler
    * @throws IllegalArgumentException if dbmsDef is <code>null</code>
    */
   public void init(PSJdbcDbmsDef dbmsDef, Connection conn,
      PSJdbcTableSchema srcTableSchema, PSJdbcTableSchema destTableSchema)
       throws PSJdbcTableFactoryException
   {
      if (dbmsDef == null)
         throw new IllegalArgumentException("dbmsDef may not be null");
      m_dbmsDef = dbmsDef;
   }

   /**
    * @see com.percussion.tablefactory.IPSJdbcTableDataHandler
    *
    * <code>fromXml</code> method must be called before calling this method
    * so that the row mappings are defined.
    *
    * @throws IllegalStateException if <code>fromXml</code> method has not
    * been called before calling this method
    */
   public PSJdbcRowData execute(Connection conn, PSJdbcRowData row)
      throws PSJdbcTableFactoryException
   {
      if (conn == null)
         throw new IllegalArgumentException("conn may not be null");

      if (m_tableMapEl == null)
         throw new IllegalStateException("row mappings not defined");

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

      // check if the source table exists
      PSJdbcTableSchema srcSchema = PSJdbcTableFactory.catalogTable(conn,
         m_dbmsDef, dataTypeMap, m_srcTable, false);
      boolean srcTableExists = (srcSchema != null);
      if (!srcTableExists)
         return null;

      // check if the destination table exists
      PSJdbcTableSchema destSchema = PSJdbcTableFactory.catalogTable(conn,
         m_dbmsDef, dataTypeMap, m_destTable, false);
      boolean destTableExists = (destSchema != null);
      if (!destTableExists)
         return null;

      PSJdbcTableMapping tableMapping = new PSJdbcTableMapping(
         m_dbmsDef, destSchema);
      tableMapping.addRowMapping(m_tableMapEl);
      applyTableMapping(conn, tableMapping, srcSchema, destSchema);
      return null;
   }

   /**
    * Applies the row mappings defined in the table mapping to all the rows
    * in the source table.
    *
    * @param conn the database connection to use, assumed not <code>null</code>
    * @param tableMapping contains all the row mappings, assumed not <code>null</code>
    * @param srcSchema schema of the source table from which data will be
    * obtained, assumed not <code>null</code>
    * @param destSchema schema of the destination table into which data will be
    * updated, assumed not <code>null</code>
    *
    * @throws SQLException if any error occurs reading data from source table
    * or updating data in the destination table
    * @throws IOException if any error occurs reading or writing LOB data
    * @throws PSJdbcTableFactoryException if any error occurs
    */
   private void applyTableMapping(Connection conn, PSJdbcTableMapping tableMapping,
      PSJdbcTableSchema srcSchema, PSJdbcTableSchema destSchema)
      throws PSJdbcTableFactoryException
   {
      PSJdbcResultSetIteratorStep step =
         PSJdbcStatementFactory.getResultSetIteratorStatement(
            m_dbmsDef, srcSchema, null, null,
            PSJdbcRowData.ACTION_INSERT);

      try
      {
         step.execute(conn);
         PSJdbcRowData srcRow = step.next();
         while (srcRow != null)
         {
            // process the row mappings
            List rowList = tableMapping.processRow(conn, srcRow);
            if (rowList != null)
            {
               Iterator it = rowList.iterator();
               while (it.hasNext())
               {
                  // create the insert statement
                  PSJdbcRowData destRow = (PSJdbcRowData)it.next();
                  PSJdbcExecutionStep insertStep =
                     PSJdbcStatementFactory.getInsertStatement(
                        m_dbmsDef, destSchema, destRow);
                  insertStep.execute(conn);
               }
            }
            srcRow = step.next();
         }
      }
      catch (SQLException e)
      {
         Object[] args = {m_destTable,
            PSJdbcTableFactoryException.formatSqlException(e)};
         throw new PSJdbcTableFactoryException(
            IPSTableFactoryErrors.DATA_PROCESS_ERROR, args, e);
      }
      finally
      {
         if (step != null)
            step.close();
      }
   }

   /**
    * @see com.percussion.tablefactory.IPSJdbcTableDataHandler
    */
   public void close(Connection conn)  throws PSJdbcTableFactoryException
   {
   }

   /**
    * @see com.percussion.tablefactory.IPSJdbcTableDataHandler
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

      Element tableMapEl = walker.getNextElement(
         PSJdbcTableMapping.NODE_NAME, firstFlags);

      if (tableMapEl == null)
      {
         throw new PSJdbcTableFactoryException(
            IPSTableFactoryErrors.XML_ELEMENT_NULL,
            PSJdbcTableMapping.NODE_NAME);
      }
      m_tableMapEl = tableMapEl;

      // get the destTable attribute
      String sTemp = tableMapEl.getAttribute(DEST_TABLE_ATTR);
      if (sTemp == null || sTemp.trim().length() == 0)
      {
         Object[] args = {PSJdbcTableMapping.NODE_NAME, DEST_TABLE_ATTR,
            sTemp == null ? "null" : sTemp};
         throw new PSJdbcTableFactoryException(
            IPSTableFactoryErrors.XML_ELEMENT_INVALID_ATTR, args);
      }
      m_destTable = sTemp;

      // get the srcTable attribute
      sTemp = tableMapEl.getAttribute(SRC_TABLE_ATTR);
      if (sTemp == null || sTemp.trim().length() == 0)
      {
         Object[] args = {PSJdbcTableMapping.NODE_NAME, SRC_TABLE_ATTR,
            sTemp == null ? "null" : sTemp};
         throw new PSJdbcTableFactoryException(
            IPSTableFactoryErrors.XML_ELEMENT_INVALID_ATTR, args);
      }
      m_srcTable = sTemp;
   }

   /**
    * @see com.percussion.tablefactory.IPSJdbcTableDataHandler
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      // create the root element
      Element   root = doc.createElement(IPSJdbcTableDataHandler.NODE_NAME);
      String className = this.getClass().getName();
      root.setAttribute(IPSJdbcTableDataHandler.CLASS_ATTR, className);
      root.appendChild(m_tableMapEl);
      return root;
   }

   /**
    * provides the database/schema information for the table, initialized in the
    * constructor, never <code>null</code> after initialization
    */
   private PSJdbcDbmsDef m_dbmsDef = null;

   /**
    * name of the destination table into which data needs to be inserted,
    * initialized in the <code>fromXml</code> method,
    * never <code>null</code> after initialization
    */
   private String m_destTable = null;

   /**
    * name of the source table from which data will be copied,
    * initialized in the <code>fromXml</code> method,
    * never <code>null</code> after initialization
    */
   private String m_srcTable = null;

   /**
    * Element from which to obtain the table mapping,
    * initialized in the <code>fromXml</code> method, never <code>null</code>
    * after initialization.
    */
   private Element m_tableMapEl = null;

   // Xml elements and attributes
   private static final String DEST_TABLE_ATTR = "destTable";
   private static final String SRC_TABLE_ATTR = "srcTable";

}


