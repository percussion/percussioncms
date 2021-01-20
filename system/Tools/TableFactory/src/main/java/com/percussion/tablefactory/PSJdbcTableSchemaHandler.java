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

import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class is used to specify the steps that need to be performed when
 * table schema change occurs.
 */
public class PSJdbcTableSchemaHandler
{
   /**
    * Creates this object from its Xml representation.  See {@link #fromXml(
    * Element) fromXml} for more information.
    *
    * @param sourceNode The element from which this object is to be constructed.
    * May not be <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>sourceNode</code> is
    * <code>null</code>.
    * @throws PSJdbcTableFactoryException if the Xml definition is invalid, or
    * if there are any other errors.
    */
   public PSJdbcTableSchemaHandler(Element sourceNode)
      throws PSJdbcTableFactoryException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode may not be null");

      fromXml(sourceNode);
   }

   /**
    * Serializes this object's state to Xml conforming with the tabledef.dtd.
    *
    * @param doc The document to use when creating elements.  May not be
    * <code>null</code>.
    *
    * @return The element containing this object's state, never
    * <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>doc</code> is <code>null</code>.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      // create the root element
      Element root = doc.createElement(NODE_NAME);

      // set the type atrribute
      root.setAttribute(TYPE_ATTR, getType(m_type));

      // add the data handlers
      Element dataHandlersEl = PSXmlDocumentBuilder.addEmptyElement(
         doc, root, DATA_HANDLERS_EL);
      root.appendChild(dataHandlersEl);

      // add data handlers
      Iterator it = m_dataHandlers.iterator();
      {
         IPSJdbcTableDataHandler handler = (IPSJdbcTableDataHandler)it.next();
         Element dataHandlerEl = handler.toXml(doc);
         dataHandlersEl.appendChild(dataHandlerEl);
      }

      return root;
   }

   /**
    * Restore this object from an Xml representation conforming with the
    * tabledef.dtd.
    *
    * @param sourceNode The element from which to get this object's state.
    * May not be <code>null</code>.
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

      // get the type attribute
      String sTemp = sourceNode.getAttribute(TYPE_ATTR);
      if (sTemp == null || sTemp.trim().length() == 0)
      {
         Object[] args = {NODE_NAME, TYPE_ATTR,
            sTemp == null ? "null" : sTemp};
         throw new PSJdbcTableFactoryException(
            IPSTableFactoryErrors.XML_ELEMENT_INVALID_ATTR, args);
      }
      m_type = getSchemaHandlerType(sTemp);

      // load data handlers
      m_dataHandlers.clear();

      Element dataHandlersEl = walker.getNextElement(
         DATA_HANDLERS_EL, firstFlags);

      if (dataHandlersEl != null)
      {
         walker = new PSXmlTreeWalker(dataHandlersEl);
         Element dataHandlerEl = walker.getNextElement(
            IPSJdbcTableDataHandler.NODE_NAME, firstFlags);
         while (dataHandlerEl != null)
         {
            // data handler class
            sTemp = dataHandlerEl.getAttribute(IPSJdbcTableDataHandler.CLASS_ATTR);
            if (sTemp == null || sTemp.trim().length() == 0)
            {
               Object[] args = {NODE_NAME,
                     IPSJdbcTableDataHandler.CLASS_ATTR,
                     sTemp == null ? "null" : sTemp};
               throw new PSJdbcTableFactoryException(
                  IPSTableFactoryErrors.XML_ELEMENT_INVALID_ATTR, args);
            }
            String className = sTemp;

            // load the data handler class
            IPSJdbcTableDataHandler dataHandler = null;
            try
            {
               Class cls = Class.forName(className);
               dataHandler = (IPSJdbcTableDataHandler)cls.newInstance();
               dataHandler.fromXml(dataHandlerEl);
            }
            catch (ClassNotFoundException e)
            {
               throw new PSJdbcTableFactoryException(
                  IPSTableFactoryErrors.DATA_HANDLER_CLASS_NOT_FOUND, className);
            }
            catch (PSJdbcTableFactoryException e)
            {
               throw e;
            }
            catch (Exception e)
            {
               Object[] args = {NODE_NAME,
                     IPSJdbcTableDataHandler.CLASS_ATTR,
                     e.getLocalizedMessage()};
               throw new PSJdbcTableFactoryException(
                  IPSTableFactoryErrors.XML_ELEMENT_INVALID_ATTR, args);
            }

            m_dataHandlers.add(dataHandler);
            dataHandlerEl = walker.getNextElement(
               IPSJdbcTableDataHandler.NODE_NAME, nextFlags);
         }
      }
   }

   /**
    * Initializes the data handlers encapsulated by this schema handler. The
    * value of <code>srcTableSchema</code> and <code>destTableSchema</code> depend
    * upon the type of schema handler this object is.
    *
    * If this object is a schema handler of type <code>TYPE_INT_ON_CREATE</code>
    * then <code>srcTableSchema</code> is the schema of the table being created
    * and <code>destTableSchema</code> is <code>null</code>.
    *
    * If this object is a schema handler of one of the following types:
    * <code>TYPE_INT_NO_ALTER_TABLE_STMT</code> or
    * <code>TYPE_INT_TO_BACKUP</code> or
    * <code>TYPE_INT_FROM_BACKUP</code>
    * then <code>srcTableSchema</code> is the schema of the source table from
    * which the data is being obtained and <code>destTableSchema</code> is
    * the schema of the destination table into which data is being updated.
    *
    * @param dbmsDef provides the database/schema information for the table,
    * may not be <code>null</code>
    * @param conn the database connection to use, may not be <code>null</code>
    * @param srcTableSchema schema of the table being created or schema of the
    * source table from which data is being obtained, may not be <code>null</code>
    * @param destTableSchema schema of the destination table into which data
    * is being updated, may be <code>null</code> if this object is of type
    * <code>PSJdbcTableSchemaHandler.TYPE_INT_ON_CREATE</code>,
    * otherwise it may not be <code>null</code>
    *
    * @throws IllegalArgumentException if <code>dbmsDef</code> or
    * <code>conn</code> or <code>srcTableSchema</code> is <code>null</code>,
    * or <code>destTableSchema</code> is <code>null</code> and this object is
    * one of the following types:
    * <code>TYPE_INT_NO_ALTER_TABLE_STMT</code> or
    * <code>TYPE_INT_TO_BACKUP</code> or
    * <code>TYPE_INT_FROM_BACKUP</code>
    * @throws PSJdbcTableFactoryException if there are any errors.
    */
   public void initHandlers(PSJdbcDbmsDef dbmsDef, Connection conn,
      PSJdbcTableSchema srcTableSchema, PSJdbcTableSchema destTableSchema)
      throws PSJdbcTableFactoryException
   {
      if (dbmsDef == null)
         throw new IllegalArgumentException("dbmsDef may not be null");
      if (conn == null)
         throw new IllegalArgumentException("conn may not be null");
      if (srcTableSchema == null)
         throw new IllegalArgumentException("srcTableSchema may not be null");
      if ((m_type == PSJdbcTableSchemaHandler.TYPE_INT_FROM_BACKUP) ||
         (m_type == PSJdbcTableSchemaHandler.TYPE_INT_TO_BACKUP) ||
         (m_type == PSJdbcTableSchemaHandler.TYPE_INT_NO_ALTER_TABLE_STMT))
      {
         if (destTableSchema == null)
            throw new IllegalArgumentException("destTableSchema may not be null");
      }

      // initialize the data handlers
      Iterator it = m_dataHandlers.iterator();
      while (it.hasNext())
      {
         IPSJdbcTableDataHandler handler = (IPSJdbcTableDataHandler)it.next();
         handler.init(dbmsDef, conn, srcTableSchema, destTableSchema);
      }
      m_bHandlersInitialized = true;
   }

   /**
    * Closes the data handlers.
    *
    * @param conn the database connection to use, may not be <code>null</code>
    *
    * @throws IllegalStateException if <code>initHandlers()</code> method has not
    * been called previously on this table schema handler.
    * @throws IllegalArgumentException if <code>conn</code> is <code>null</code>
    * @throws PSJdbcTableFactoryException if there are any errors.
    */
   public void closeHandlers(Connection conn)
      throws PSJdbcTableFactoryException
   {
      if (!m_bHandlersInitialized)
         throw new IllegalStateException("data handlers not initialized");
      if (conn == null)
            throw new IllegalArgumentException("conn may not be null");

      // close the data handlers
      Iterator it = m_dataHandlers.iterator();
      while (it.hasNext())
      {
         IPSJdbcTableDataHandler handler = (IPSJdbcTableDataHandler)it.next();
         handler.close(conn);
      }
      m_bHandlersInitialized = false;
   }

   /**
    * Modifies the row data.
    *
    * @param conn the database connection to use, may not be <code>null</code>
    * @param row the row data to modify, may be <code>null</code>
    *
    * @return the modified row data, may be <code>null</code>
    *
    * @throws IllegalStateException if <code>initHandlers()</code> method has not
    * been called previously on this table schema change handler.
    * @throws IllegalArgumentException if <code>conn</code> is <code>null</code>
    * @throws PSJdbcTableFactoryException if there are any errors.
    */
   public PSJdbcRowData execute(Connection conn, PSJdbcRowData row)
      throws PSJdbcTableFactoryException
   {
      if (!m_bHandlersInitialized)
         throw new IllegalStateException("data handlers not initialized");
      if (conn == null)
         throw new IllegalArgumentException("conn may not be null");
      // execute the data handlers
      Iterator it = m_dataHandlers.iterator();
      while (it.hasNext())
      {
         IPSJdbcTableDataHandler handler = (IPSJdbcTableDataHandler)it.next();
         row = handler.execute(conn, row);
      }
      return row;
   }

   /**
    * Throws IllegalArgumentException if <code>type</code> is a
    * not a valid schema handler type.
    *
    * @param type the schema handler type value to check for
    * validity
    *
    * @throws IllegalArgumentException if <code>type</code>
    * is not one of the following values:
    * <code>TYPE_INT_NO_ALTER_TABLE_STMT</code>
    * <code>TYPE_INT_TO_BACKUP</code>
    * <code>TYPE_INT_FROM_BACKUP</code>
    * <code>TYPE_INT_NO_ALTER_TABLE_STMT</code>
    */
   public static void checkValidSchemaHandlerType(int type)
   {
      switch (type)
      {
         case TYPE_INT_NO_ALTER_TABLE_STMT:
         case TYPE_INT_TO_BACKUP:
         case TYPE_INT_FROM_BACKUP:
         case TYPE_INT_ON_CREATE:
            return;
      }
      throw new IllegalArgumentException("invalid schema handler type");
   }

   /**
    * Returns the integer constant correponding to the schema handler type
    * specified by <code>type</code>
    *
    * @param type the schema handler type, may not be
    * <code>null</code> or empty
    *
    * @return the integer constant correponding to the schema handler type
    * specified by <code>type</code>
    *
    * @throws IllegalArgumentException if <code>type</code> is
    * <code>null</code> or empty, or is not a valid schema handler type.
    */
   public static int getSchemaHandlerType(String type)
   {
      if ((type == null) || (type.trim().length() < 1))
         throw new IllegalArgumentException(
            "schema handler type may not be null or empty");

      type = type.trim();
      if (type.equalsIgnoreCase(TYPE_STR_NO_ALTER_TABLE_STMT))
         return TYPE_INT_NO_ALTER_TABLE_STMT;
      else if (type.equalsIgnoreCase(TYPE_STR_TO_BACKUP))
         return TYPE_INT_TO_BACKUP;
      else if (type.equalsIgnoreCase(TYPE_STR_FROM_BACKUP))
         return TYPE_INT_FROM_BACKUP;
      else if (type.equalsIgnoreCase(TYPE_STR_ON_CREATE))
         return TYPE_INT_ON_CREATE;
      else
         throw new IllegalArgumentException("invalid schema handler type");
   }

   /**
    * Returns the <code>String</code> constant correponding to the schema handler
    * type specified by <code>type</code>
    *
    * @param type the schema handler type, should be a valid
    * schema handler type
    *
    * @return the <code>String</code> constant correponding to the schema handler
    * type specified by <code>type</code>
    *
    * @throws IllegalArgumentException if <code>type</code> is
    * not a valid schema handler type.
    */
   public static String getType(int type)
   {
      switch (type)
      {
         case TYPE_INT_NO_ALTER_TABLE_STMT:
            return TYPE_STR_NO_ALTER_TABLE_STMT;

         case TYPE_INT_TO_BACKUP:
            return TYPE_STR_TO_BACKUP;

         case TYPE_INT_FROM_BACKUP:
            return TYPE_STR_FROM_BACKUP;

         case TYPE_INT_ON_CREATE:
            return TYPE_STR_ON_CREATE;
      }
      throw new IllegalArgumentException("invalid schema handler type");
   }

   /**
    * Returns the type of schema handler.
    * @return the type of schema handler.
    */
   public int getType()
   {
      return m_type;
   }

   /**
    * List of {@link IPSJdbcTableDataHandler} objects to process data when
    * schema change occurs. These objects process data when the schema plan
    * is being executed. Never <code>null</code>, may be empty.
    */
   private List m_dataHandlers = new ArrayList();

   /**
    * stores the initialization state of the data handlers. If
    * <code>execute()</code> method is called before the <code>initHandlers()</code>
    * then <code>IllegalStateException</code> is thrown.
    */
   private boolean m_bHandlersInitialized = false;

   /**
    * stores the type of schema handler.
    */
   private int m_type = 0;

   /**
    * The name of this objects root Xml element.
    */
   public static final String NODE_NAME = "schemaHandler";

   // Xml elements and attributes
   private static final String DATA_HANDLERS_EL = "dataHandlers";
   private static final String TYPE_ATTR = "type";

   public static final String TYPE_STR_NO_ALTER_TABLE_STMT = "noAlterTableStmt";
   public static final String TYPE_STR_TO_BACKUP = "toBackup";
   public static final String TYPE_STR_FROM_BACKUP = "fromBackup";
   public static final String TYPE_STR_ON_CREATE = "onCreate";

   public static final int TYPE_INT_NO_ALTER_TABLE_STMT = 1;
   public static final int TYPE_INT_TO_BACKUP = 2;
   public static final int TYPE_INT_FROM_BACKUP = 3;
   public static final int TYPE_INT_ON_CREATE = 4;

}


