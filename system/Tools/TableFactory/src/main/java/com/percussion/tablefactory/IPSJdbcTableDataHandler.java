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

import java.sql.Connection;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Interface used by classes that wish to be process data when table schema
 * change occurs.
 */
public interface IPSJdbcTableDataHandler
{
   /**
    * Initialize this data handler. This method must be called before
    * <code>execute()</code> method, and <code>close()</code> method must be
    * called after the <code>execute()</code> method. This method does
    * not close the database connection <code>conn</code>.
    *
    * The value of <code>srcTableSchema</code>
    * and <code>destTableSchema</code> depend upon the schema handler
    * which contains this data handler.
    *
    * If the enclosing schema handler is of type
    * <code>PSJdbcTableSchemaHandler.TYPE_INT_ON_CREATE</code> then
    * <code>srcTableSchema</code> is the schema of the table being created
    * and <code>destTableSchema</code> is <code>null</code>.
    *
    * If the enclosing schema handler is one of the following types:
    * <code>PSJdbcTableSchemaHandler.TYPE_INT_NO_ALTER_TABLE_STMT</code> or
    * <code>PSJdbcTableSchemaHandler.TYPE_INT_TO_BACKUP</code> or
    * <code>PSJdbcTableSchemaHandler.TYPE_INT_FROM_BACKUP</code>
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
    * is being updated, may be <code>null</code> if the enclosing schema
    * handler is of type <code>PSJdbcTableSchemaHandler.TYPE_INT_ON_CREATE</code>,
    * otherwise it may not be <code>null</code>
    *
    * @throws IllegalArgumentException if <code>dbmsDef</code> or <code>conn</code>
    * or <code>srcTableSchema</code> is <code>null</code>, or
    * <code>destTableSchema</code> is <code>null</code> and enclosing schema
    * handler is one of the following types:
    * <code>PSJdbcTableSchemaHandler.TYPE_INT_NO_ALTER_TABLE_STMT</code> or
    * <code>PSJdbcTableSchemaHandler.TYPE_INT_TO_BACKUP</code> or
    * <code>PSJdbcTableSchemaHandler.TYPE_INT_FROM_BACKUP</code>
    *
    * @throws PSJdbcTableFactoryException if there are any errors.
    */
   public void init(PSJdbcDbmsDef dbmsDef, Connection conn,
      PSJdbcTableSchema srcTableSchema, PSJdbcTableSchema destTableSchema)
      throws PSJdbcTableFactoryException;

   /**
    * Modifies the row data obtained from the souce table, and returns the
    * modified row data. <code>init()</code> method must be called before
    * this method, and <code>close()</code> method must be
    * called after this method. This method does
    * not close the database connection <code>conn</code>.
    *
    * @param conn the database connection to use, may not be <code>null</code>
    * @param row the row data to modify, may be <code>null</code>
    *
    * @return the modified row data, may be <code>null</code>
    *
    * @throws IllegalStateException if <code>initHandlers</code> method has not
    * been called previously on this table schema handler.
    * @throws IllegalArgumentException if conn is <code>null</code>
    * @throws PSJdbcTableFactoryException if there are any errors.
    */
   public PSJdbcRowData execute(Connection conn, PSJdbcRowData row)
      throws PSJdbcTableFactoryException;

   /**
    * Closes this data handler. <code>init()</code> and <code>execute()</code>
    * methods must be called after this method.
    * This method should close any open database resources. This method does
    * not close the database connection <code>conn</code>. The database
    * connection parameter <code>conn</code> may be a different object from the
    * connection parameter <code>conn</code> provided in the <code>init()</code>
    * and <code>execute()</code> methods.
    *
    * @param conn the database connection to use, may not be <code>null</code>
    *
    * @throws IllegalStateException if <code>init</code> method has not been
    * called previously on this data handler.
    * @throws PSJdbcTableFactoryException if there are any errors.
    */
   public void close(Connection conn)
      throws PSJdbcTableFactoryException;

   /**
    * Restore this object from an Xml representation conforming with the
    * tabledef.dtd. The tag name of <code>sourceNode</code> must equal the
    * value of <code>NODE_NAME</code>.
    * The child elements and attributes of <code>sourceNode</code> depend upon
    * the class implementing this interface. Different implementations may have
    * different attributes and child elements of <code>sourceNode</code>.
    *
    * @param sourceNode The element from which to get this object's state.
    * May not be <code>null</code>. The tag name of this node equals the
    * value of <code>NODE_NAME</code>.
    *
    * @throws IllegalArgumentException if sourceNode is <code>null</code>.
    * @throws PSJdbcTableFactoryException if there are any errors or if
    * the tag name of <code>sourceNode</code> does not equal the value of
    * <code>NODE_NAME</code>
    */
   public void fromXml(Element sourceNode) throws PSJdbcTableFactoryException;

   /**
    * Serializes this object's state to Xml conforming with the tabledef.dtd.
    * The tag name of the returned element must equal the value of
    * <code>NODE_NAME</code>.
    *
    * @param doc The document to use when creating elements, may not be <code>
    * null</code>.
    *
    * @return The element containing this object's state, never <code>
    * null</code>. Its tag name must equal the value of <code>NODE_NAME</code>
    *
    * @throws IllegalArgumentException if doc is <code>null</code>.
    */
   public Element toXml(Document doc);

   /**
    * The name of this objects root Xml element.
    */
   public static final String NODE_NAME = "dataHandler";

   // Xml elements and attributes
   public static String CLASS_ATTR = "class";
}
