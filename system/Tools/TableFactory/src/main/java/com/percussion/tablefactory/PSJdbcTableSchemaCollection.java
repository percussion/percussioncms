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

import com.percussion.util.PSCollection;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;

/**
 * This class is a container for a list of PSJdbcTableSchema objects, enabling
 * them to be serialized as a collection to and from Xml.
 */
public class PSJdbcTableSchemaCollection extends PSCollection
{
   /**
    * Constructs an empty PSJdbcTableSchemaCollection
    */
   public PSJdbcTableSchemaCollection()
   {
      super(PSJdbcTableSchema.class);
   }

   /**
    * Creates this object from its Xml representation.  See {@link #fromXml(
    * Element, PSJdbcDataTypeMap) fromXml} for more information.
    *
    * @param doc The document from which this object is to be constructed.
    *    Root element must conform to the definition for the tables element in
    *    the tabledef.dtd.  May not be <code>null</code>.
    * @param dataTypeMap The dataType map to use for the table's in this
    *    collection. May not be <code>null</code>.
    *
    * @throws IllegalArgumentException if doc or dataTypeMap is
    * <code>null</code>.
    * @throws PSJdbcTableFactoryException if the Xml definition is invalid, or
    * if there are any other errors.
    */
   public PSJdbcTableSchemaCollection(Document doc,
      PSJdbcDataTypeMap dataTypeMap)
         throws PSJdbcTableFactoryException
   {
      this();

      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      if (dataTypeMap == null)
         throw new IllegalArgumentException("dataTypeMap may not be null");

      fromXml(doc.getDocumentElement(), dataTypeMap);
   }

   /**
    * Restore this object from an Xml representation.
    *
    * @param sourceNode The element from which to get this object's state.
    *    Element must conform to the definition for the component
    *    element in the tabledef.dtd.  May not be <code>null</code>.
    * @param dataTypeMap The dataType map to use for this table's columns.
    * May not be <code>null</code>.
    *
    * @throws IllegalArgumentException if sourceNode is <code>null</code>.
    * @throws PSJdbcTableFactoryException if there are any errors.
    */
   public void fromXml(Element sourceNode, PSJdbcDataTypeMap dataTypeMap)
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

      clear();

      PSXmlTreeWalker walker = new PSXmlTreeWalker(sourceNode);
      int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

      Element table = walker.getNextElement(PSJdbcTableSchema.NODE_NAME,
         firstFlags);
      if (table == null)
      {
         Object[] args = {NODE_NAME, PSJdbcTableSchema.NODE_NAME, "null"};
         throw new PSJdbcTableFactoryException(
            IPSTableFactoryErrors.XML_ELEMENT_INVALID_CHILD, args);
      }

      while (table != null)
      {
         add(new PSJdbcTableSchema(table, dataTypeMap));
         table = walker.getNextElement(PSJdbcTableSchema.NODE_NAME, nextFlags);
      }
      fixParentChildRelationship();
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
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      // create the root element
      Element   root = doc.createElement(NODE_NAME);
      // Sort based upon foreign keys correctly.
      for (int i = 0; i < size(); i++)
      {
         PSJdbcTableSchema table = (PSJdbcTableSchema)get(i);
         root.appendChild(table.toXml(doc));
      }

      return root;
   }

   /**
    * For each table schema in this collection, locates the first table
    * data in the supplied collection with the same name and sets that table
    * data object on the table schema. Skips table schema objects for which
    * {@link PSJdbcTableSchema#isAlter()} returns <code>true</code>, and a
    * message is logged to that effect.
    *
    * @param tableDataCollection The table data collection.  Never <code>null
    * </code>.
    *
    * @throws IllegalArgumentException if tableDataCollection is <code>null
    * </code> or empty.
    * @throws PSJdbcTableFactoryException if a table in the data collection
    * cannot be found in this schema collection, or if a table data object
    * defines a column not found in the corresponding table schema object.
    */
   public void setTableData(PSJdbcTableDataCollection tableDataCollection)
      throws PSJdbcTableFactoryException
   {
      if (tableDataCollection == null)
         throw new IllegalArgumentException(
            "tableDataCollection may not be null");

      Set matchedTables = new HashSet();

         Iterator tables = this.iterator();
         while (tables.hasNext()) {
            PSJdbcTableSchema tableSchema = (PSJdbcTableSchema) tables.next();
            PSJdbcTableData tableData = tableDataCollection.getTableData(
                    tableSchema.getName());
            if (tableData != null) {
               if (tableSchema.isAlter()) {
                  PSJdbcTableFactory.logMessage(
                          "Skipping table data for table schema (" +
                                  tableSchema.getName() + ") with alter=y.");
                  continue;
               }

               matchedTables.add(tableData.getName());
               tableSchema.setTableData(tableData);
            }
         }

         // now walk tabledata collection to see if there are any we didn't match
         tables = tableDataCollection.iterator();
         while (tables.hasNext())
         {
            PSJdbcTableData tableData = (PSJdbcTableData)tables.next();
            if (!matchedTables.contains(tableData.getName()))
            {
               throw new PSJdbcTableFactoryException(
                  IPSTableFactoryErrors.TABLE_SCHEMA_NOT_FOUND,
                  tableData.getName());
            }
         }
      }

   /**
    * Returns the table schema object with the specified name.
    *
    * @param name The name of the table schema to locate, match is case
    * insensitive.  May not be <code>null </code> or empty.
    *
    * @return The matching schema object, or <code>null</code> if it is not
    * found.
    *
    * @throws IllegalArgumentException if name is <code>null</code> or emtpy.
    */
   public PSJdbcTableSchema getTableSchema(String name)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException(
            "name may not be null or empty");

      PSJdbcTableSchema tableSchema = null;

      for (int i = 0; i < size(); i++)
      {
         PSJdbcTableSchema tempTable = (PSJdbcTableSchema)get(i);
         if (tempTable.getName().equalsIgnoreCase(name))
         {
            tableSchema = tempTable;
            break;
         }
      }

      return tableSchema;
   }

   /**
    * This function fixes the parent child relation between the table schema
    * objects contained in this collection. If a child table has a parent table
    * then it makes sure that the parent table also has the child table as its
    * child.
    * We need this function because the table schema defintion Xml may contain
    * the schema definition of the child table before the schema definition of
    * the parent table. In such cases the child table knows about the parent
    * table but the parent table does not know about the child table.
    */
   protected void fixParentChildRelationship()
   {
      Iterator it = null;
      String childTableName = null;
      String parentTableName = null;
      PSJdbcTableSchema childTableSchema = null;
      PSJdbcTableSchema parentTableSchema = null;
      int size = size();
      for (int i = 0; i < size; i++)
      {
         childTableSchema = (PSJdbcTableSchema)get(i);
         childTableName = childTableSchema.getName();
         it = childTableSchema.getParentTables();
         while (it.hasNext())
         {
            parentTableName = (String)it.next();
            parentTableSchema = getTableSchema(parentTableName);
            if (parentTableSchema != null)
               parentTableSchema.addChildTable(childTableName);
         }
      }
   }

   // testing only
   public static void main(String[] args)
   {
      if (args.length != 1)
      {
         System.out.println("test usage: ");
         System.out.println("java PSJdbcTableSchemaCollection <tables>");
         System.out.println(
            "where <tables> is an xml file containing the expected xml.");
         System.exit(1);
      }

      try
      {
         PSJdbcDataTypeMap map = new PSJdbcDataTypeMap("MSSQL", "inetdae7",
            null);
         try(FileInputStream in = new java.io.FileInputStream(args[0])){
         Document doc = PSXmlDocumentBuilder.createXmlDocument(in, false);
         PSJdbcTableSchemaCollection coll = new PSJdbcTableSchemaCollection(
                 doc, map);
         try(FileOutputStream out = new java.io.FileOutputStream(args[0] + ".tst")) {
            PSXmlDocumentBuilder.write(coll.toXml(doc), out);
         }
      }
      }
      catch (Throwable t)
      {
         t.printStackTrace(System.out);
      }
   }

   @Override
   public Iterator<PSJdbcTableSchema> iterator()
   {
      return super.iterator();
   }

   /**
    * The name of this objects root Xml element.
    */
   public static String NODE_NAME = "tables";

   // Xml elements
   private static final String TABLE_EL = "table";

}

