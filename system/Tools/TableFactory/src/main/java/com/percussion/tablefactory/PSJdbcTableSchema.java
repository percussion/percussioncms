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

import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class is used to describe a table schema, and is a container for
 * columns, primary keys, foreign keys, unique indexes, and optionally table
 * data as well.  May also be used to specify schema changes only.
 */
public class PSJdbcTableSchema implements Comparable<PSJdbcTableSchema>
{
   /**
    * Creates a table schema using the supplied columns.  The table action is
    * defaulted to {#isCreate()} = <code>true</code>, {@link #isDelOldData()} =
    * <code>false</code>, {@link #isAlter()} = false.
    *
    * @param name The name of this table, may not be <code>null</code> or empty.
    * @param columns An Iterator over one or more non-<code>null</code>
    * PSJdbcColumnDef objects.  May not be <code>null</code> or empty, or
    * contain columns with the same name.
    *
    * @throws IllegalArgumentException if any param is <code>null</code>, if
    * name is empty, or if columns does not contain at least one instance of a
    * PSJdbcColumnDef, or if columns contains any <code>null</code> entries, or
    * entries that are not instances of PSJdbcColumnDef objects.
    * @throws PSJdbcTableFactoryException if columns contains any duplicate
    * columns.
    */
   public PSJdbcTableSchema(String name, Iterator columns)
      throws PSJdbcTableFactoryException
   {
      this(name);
      if (columns == null || !columns.hasNext())
         throw new IllegalArgumentException("columns may not be null or empty");

      while (columns.hasNext())
      {
         Object colObj = columns.next();
         if (!(colObj instanceof PSJdbcColumnDef))
            throw new IllegalArgumentException(
               "columns may only contain PSJdbcColumnDef objects");

         PSJdbcColumnDef column = (PSJdbcColumnDef)colObj;
         PSJdbcColumnDef dupeCol = setColumn(column);
         if (dupeCol != null)
         {
            Object[] args = {m_name + " table", column.getName()};
            throw new PSJdbcTableFactoryException(
               IPSTableFactoryErrors.DUPLICATE_COLUMN, args);
         }
      }
   }
   
   /**
    * Creates a table schema using the supplied name.  The table action is
    * defaulted to {#isCreate()} = <code>true</code>, {@link #isDelOldData()} =
    * <code>false</code>, {@link #isAlter()} = false.
    *
    * @param name The name of this table, may not be <code>null</code> or empty.
    * @param columns An Iterator over one or more non-<code>null</code>
    * PSJdbcColumnDef objects.  May not be <code>null</code> or empty, or
    * contain columns with the same name.
    */
   public PSJdbcTableSchema(String name)
      throws PSJdbcTableFactoryException
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty");

      m_name = name;
      
   }

   /**
    * Creates this object from its Xml representation.  See {@link #fromXml(
    * Element, PSJdbcDataTypeMap) fromXml} for more information.
    *
    * @param sourceNode The element from which this object is to be constructed.
    *    Element must conform to the definition for the table element in
    *    the tabledef.dtd.  May not be <code>null</code>.
    * @param dataTypeMap The dataType map to use for this table's columns.
    * May not be <code>null</code>.
    *
    * @throws IllegalArgumentException if sourceNode or dataTypeMap is
    * <code>null</code>.
    * @throws PSJdbcTableFactoryException if the Xml definition is invalid, or
    * if there are any other errors.
    */
   public PSJdbcTableSchema(Element sourceNode, PSJdbcDataTypeMap dataTypeMap)
      throws PSJdbcTableFactoryException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode may not be null");

      if (dataTypeMap == null)
         throw new IllegalArgumentException("dataTypeMap may not be null");

      fromXml(sourceNode, dataTypeMap);
   }

   /**
    * Copy constructor.  Performs a deep copy of the source schema to create
    * this schema.
    *
    * @param source The table to copy from, may not be <code>null</code>.
    *
    * @throws IllegalArgumentException if source is <code>null</code>
    */
   public PSJdbcTableSchema(PSJdbcTableSchema source)
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      m_name = source.m_name;
      m_alter = source.m_alter;
      m_allowSchemaChanges = source.m_allowSchemaChanges;
      m_isView = source.m_isView;
      m_create = source.m_create;
      m_delOldData = source.m_delOldData;
      m_parentTables.clear();
      m_parentTables.addAll(source.m_parentTables);
      m_childTables.clear();
      m_childTables.addAll(source.m_childTables);
      if (m_schemaHandlerCollection != null)
         m_schemaHandlerCollection.clear();
      if (source.m_schemaHandlerCollection != null)
      {
         m_schemaHandlerCollection = new PSJdbcTableSchemaHandlerCollection();
         m_schemaHandlerCollection.addAll(source.m_schemaHandlerCollection);
      }

      try
      {
         PSJdbcPrimaryKey pk = source.getPrimaryKey();
         if (pk != null)
            m_primaryKey = new PSJdbcPrimaryKey(pk.getColumnNames(),
               pk.getAction());
         List<PSJdbcForeignKey> fks = source.getForeignKeys();
         if (fks != null) {
            List<PSJdbcForeignKey> newKeys = new ArrayList<PSJdbcForeignKey>();
            for (PSJdbcForeignKey fk : fks) {
               newKeys.add(new PSJdbcForeignKey(fk.getColumns(),
                     fk.getAction()));
            }
            m_foreignKeys = newKeys;
         }


         PSJdbcUpdateKey uk = source.getUpdateKey();
         if (uk != null)
            m_updateKey = new PSJdbcUpdateKey(uk.getColumnNames());

         Iterator cols = source.getColumns();
         while (cols.hasNext())
            m_columns.add(new PSJdbcColumnDef((PSJdbcColumnDef)cols.next()));

         Iterator indexes = source.getIndexes(
            PSJdbcIndex.TYPE_UNIQUE | PSJdbcIndex.TYPE_NON_UNIQUE);
         while (indexes.hasNext())
         {
            PSJdbcIndex index = (PSJdbcIndex)indexes.next();
            m_indexes.add(new PSJdbcIndex(index.getName(),
               index.getColumnNames(), index.getAction(), index.getType()));
         }
      }
      catch (PSJdbcTableFactoryException e)
      {
         // cant' really happen!
         throw new IllegalArgumentException("invalid source schema: "
            + e.toString());
      }
   }

   /**
    * Returns an Iterator over one or more valid PSJdbcColumnDef objects.
    *
    * @return The Iterator, never <code>null</code> or empty.
    */
   public Iterator getColumns()
   {
      return m_columns.iterator();
   }

   /**
    * Returns an iterator over the <code>String</code> objects representing the
    * name of child tables of this table.
    *
    * @return the Iterator, may be empty.
    */
   public Iterator getChildTables()
   {
      return m_childTables.iterator();
   }

   /**
    * Returns an iterator over the <code>String</code> objects representing the
    * name of parent tables of this table.
    *
    * @return the Iterator, may be empty.
    */
   public Iterator getParentTables()
   {
      return m_parentTables.iterator();
   }

   /**
    * Returns the column with the specified name.  Name is compared case
    * insensitive.
    *
    * @param name The name of the column to return, may not be <code>null</code>
    * or empty.
    *
    * @return The column object, or <code>null</code> if a match is not found.
    */
   public PSJdbcColumnDef getColumn(String name)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty");

      PSJdbcColumnDef column = null;

      int index = getColumnIndex(name);
      if (index > -1)
         column = (PSJdbcColumnDef)m_columns.get(index);

      return column;
   }

   /**
    * Append's the column to this table definition.  If a column with the same
    * name already exists, it is replaced.
    *
    * @param column The column to set on this table, may not be <code>null
    * </code>.
    *
    * @return <code>null</code> if there is not already a column with the same
    * name that is replaced, or the old column object if it is replaced.
    */
   public PSJdbcColumnDef setColumn(PSJdbcColumnDef column)
   {
      if (column == null)
         throw new IllegalArgumentException("column may not be null");

      PSJdbcColumnDef oldCol = null;
      int index = getColumnIndex(column.getName());
      if (index > -1)
      {
         oldCol = (PSJdbcColumnDef)m_columns.get(index);
         m_columns.set(index, column);
      }
      else
         m_columns.add(column);

      return oldCol;
   }

   /**
    * Remove's the column with the specified name.
    *
    * @param name The name of the column to remove, may not be <code>null</code>
    * or empty.
    *
    * @return The column removed, or <code>null</code> if a match is not found.
    *
    * @throws PSJdbcTableFactoryException if removing the last column in the
    * table, or if removing a column that is referenced by another schema object
    * e.g. primary key, or foreign key.
    */
   public PSJdbcColumnDef removeColumn(String name)
      throws PSJdbcTableFactoryException
   {
      if (name == null)
         throw new IllegalArgumentException("name may not be null");

      PSJdbcColumnDef oldCol = null;
      int index = getColumnIndex(name);
      if (index > -1)
      {
         if (m_columns.size() == 1)
            throw new PSJdbcTableFactoryException(
               IPSTableFactoryErrors.REMOVE_LAST_COLUMN);

         oldCol = (PSJdbcColumnDef)m_columns.get(index);
         m_columns.remove(index);
      }

      validateSchema();

      return oldCol;
   }

   /**
    * Sets the primary key object on this table schema.  If there is an existing
    * primary key defined, it is replaced.  All columns referenced by the
    * primary key must be defined in this schema (may have an action of {@link
    * PSJdbcTableComponent#ACTION_NONE}).
    *
    * @param primaryKey The primary key definition, may be <code>null</code>.
    *
    * @throws PSJdbcTableFactoryException if the primary key refereneces columns
    * not defined in this schema.
    */
   public void setPrimaryKey(PSJdbcPrimaryKey primaryKey)
      throws PSJdbcTableFactoryException
   {
      m_primaryKey = primaryKey;
      validateSchema();
   }

   /**
    * Returns the primary key defintion if one has been set.
    *
    * @return The primary key, or <code>null</code> if one has not been set.
    */
   public PSJdbcPrimaryKey getPrimaryKey()
   {
      return m_primaryKey;
   }

   /**
    * Sets the foreign key objects on this table schema.  If there is an existing
    * foreign key defined, it is replaced.   All columns referenced by the
    * foreign key must be defined in this schema (may have an action of {@link
    * PSJdbcTableComponent#ACTION_NONE}).
    *
    * @param foreignKeys The List of foreign key definition, may be <code>null</code>.
    *
    * @throws PSJdbcTableFactoryException if the foreign key refereneces
    * internal columns not defined in this schema (external columns are not
    * validated until the schema is processed).
    */
   public void setForeignKeys(List<PSJdbcForeignKey> foreignKeys)
      throws PSJdbcTableFactoryException
   {
      if (foreignKeys == null)
      {
         m_foreignKeys.clear();
      }
      else
      {
         m_foreignKeys = foreignKeys;
      }
      validateSchema();
   }

   /**
    * Returns the foreign key defintions if one has been set.
    *
    * @return The foreign key, or <code>null</code> if one has not been set.
    */
   public List<PSJdbcForeignKey> getForeignKeys()
   {
      return m_foreignKeys;
   }

   /**
    * Add's the index to this table definition.  If an index with the
    * same name already exists, it is replaced.  All columns referenced by the
    * index must be defined in this schema (may have an action of {@link
    * PSJdbcTableComponent#ACTION_NONE}).
    *
    * @param index The index to set on this table, may not be <code>null</code>.
    *
    * @return <code>null</code> if there is not already an index with the same
    * name that is replaced, or the old index object if it is replaced.
    *
    * @throws IllegalArgumentExcpetion if index is <code>null</code>.
    * @throws PSJdbcTableFactoryException if the index refereneces columns
    * not defined in this schema.
    */
   public PSJdbcIndex setIndex(PSJdbcIndex index)
      throws PSJdbcTableFactoryException
   {
      if (index == null)
         throw new IllegalArgumentException("index may not be null");

      PSJdbcIndex oldIndex = null;
      int i = getIndexIndex(index.getName());
      if (i > -1)
      {
         oldIndex = (PSJdbcIndex)m_indexes.get(i);
         m_indexes.set(i, index);
      }
      else
         m_indexes.add(index);

      validateSchema();
      return oldIndex;
   }

   /**
    * Returns the index with the specified name.  Name is compared case
    * insensitive.
    *
    * @param name The name of the index to return, may not be <code>null</code>
    * or empty.
    *
    * @return The index object, or <code>null</code> if a match is not found.
    */
   public PSJdbcIndex getIndex(String name)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty");

      PSJdbcIndex index = null;

      int i = getIndexIndex(name);
      if (i > -1)
         index = (PSJdbcIndex)m_indexes.get(i);

      return index;
   }

   /**
    * Convenience method that calls
    * {@link #getIndexes(int) getIndexes(PSJdbcIndex.TYPE_UNIQUE)}
    */
   public Iterator getIndexes()
   {
      return getIndexes(PSJdbcIndex.TYPE_UNIQUE);
   }

   /**
    * Returns an iterator over zero or more valid <code>PSJdbcIndex</code>
    * objects.
    *
    * @param type the type of indexes to include in the list whose iterator
    * is returned, should be one of <code>PSJdbcIndex.TYPE_XXX</code> or
    * multiple <code>PSJdbcIndex.TYPE_XXX</code> values OR'ed together.
    *
    * @return the Iterator over a list of <code>PSJdbcIndex</code> objects,
    * never <code>null</code>, may be empty.
    */
   public Iterator getIndexes(int type)
   {
      List indexes = new ArrayList();
      Iterator it = m_indexes.iterator();
      while (it.hasNext())
      {
         PSJdbcIndex index = (PSJdbcIndex)it.next();
         if (index.isOfType(type))
            indexes.add(index);
      }
      return indexes.iterator();
   }

   /**
    * Remove's the index with the specified name.
    *
    * @param name The name of the index to remove, may not be <code>null</code>
    * or empty.
    *
    * @return The index removed, or <code>null</code> if a match is not found.
    */
   public PSJdbcIndex removeIndex(String name)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty");

      PSJdbcIndex oldIndex = null;
      int index = getIndexIndex(name);
      if (index > -1)
         oldIndex = (PSJdbcIndex)m_indexes.remove(index);

      return oldIndex;
   }

   /**
    * Removes all indexes from this table schema.
    *
    */
   public void clearIndexes()
   {
      m_indexes.clear();
   }

   /**
    * Removes all foreign keys from this table schema.
    *
    */
   public void clearForeignKeys()
   {
      m_foreignKeys.clear();
   }

   /**
    * Sets the update key object on this table schema.  If there is an existing
    * update key defined, it is replaced.  All columns referenced by the
    * update key must be defined in this schema (may have an action of {@link
    * PSJdbcTableComponent#ACTION_NONE}).
    *
    * @param updateKey The update key definition, may not be
    * <code>null</code>.
    *
    * @throws IllegalArgumentExcpetion if updateKey is <code>null</code>.
    * @throws PSJdbcTableFactoryException if the update key refereneces columns
    * not defined in this schema.
    */
   public void setUpdateKey(PSJdbcUpdateKey updateKey)
      throws PSJdbcTableFactoryException
   {
      if (updateKey == null)
         throw new IllegalArgumentException("updateKey may not be null");

      m_updateKey = updateKey;
      validateSchema();
   }

   /**
    * Returns the update key defintion if one has been set.
    *
    * @return The update key, or <code>null</code> if one has not been set.
    */
   public PSJdbcUpdateKey getUpdateKey()
   {
      return m_updateKey;
   }


   /**
    * Serializes this object's state to Xml conforming with the tabledef.dtd.
    *
    * @param doc The document to use when creating elements.  May not be <code>
    *    null</code>.
    *
    * @return The element containing this object's state, never <code>
    *    null</code>. This does not include the Xml for any tableData that has
    *    been set on this object.  That must be retrieved and serialized
    *    separately.
    *
    * @throws IllegalArgumentException if doc is <code>null</code>.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      // create the root element
      Element   root = doc.createElement(NODE_NAME);

      // set flags
      root.setAttribute(NAME_ATTR, m_name);
      root.setAttribute(CREATE_ATTR, m_create ? XML_TRUE : XML_FALSE);
      root.setAttribute(DELOLDDATA_ATTR, m_delOldData ? XML_TRUE : XML_FALSE);
      root.setAttribute(ALTER_ATTR, m_alter ? XML_TRUE : XML_FALSE);
      root.setAttribute(ALLOW_SCHEMA_CHANGES_ATTR, m_allowSchemaChanges ? XML_TRUE : XML_FALSE);
      root.setAttribute(IS_VIEW_ATTR, m_isView ? XML_TRUE : XML_FALSE);

      // add a row for the columns
      Element row = PSXmlDocumentBuilder.addEmptyElement(doc, root, ROW_EL);

      // add each column to the row
      for (int i = 0; i < m_columns.size(); i++)
         row.appendChild(((PSJdbcColumnDef)m_columns.get(i)).toXml(doc));

      // add primary key if defined
      if (m_primaryKey != null)
         root.appendChild(m_primaryKey.toXml(doc));

      // add foreign key if defined
      if (m_foreignKeys != null) {
         for (PSJdbcForeignKey fk : m_foreignKeys) {
            root.appendChild(fk.toXml(doc));
         }
      }
       

      // add update key if defined
      if (m_updateKey != null)
         root.appendChild(m_updateKey.toXml(doc));

      // add indexes if any defined
      if (!m_indexes.isEmpty())
      {
         Element indexdefs = PSXmlDocumentBuilder.addEmptyElement(doc, root,
            INDEX_DEF_EL);
         for (int i = 0; i < m_indexes.size(); i++)
            indexdefs.appendChild(((PSJdbcIndex)m_indexes.get(i)).toXml(doc));
      }

      return root;
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

      PSXmlTreeWalker walker = new PSXmlTreeWalker(sourceNode);
      int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

      // get flags
      String sTemp = sourceNode.getAttribute(NAME_ATTR);
      if (sTemp == null || sTemp.trim().length() == 0)
      {
         Object[] args = {NODE_NAME, NAME_ATTR, sTemp == null ? "null" : sTemp};
         throw new PSJdbcTableFactoryException(
            IPSTableFactoryErrors.XML_ELEMENT_INVALID_ATTR, args);
      }
      m_name = sTemp;

      sTemp = sourceNode.getAttribute(ALLOW_SCHEMA_CHANGES_ATTR);
      if (sTemp != null && sTemp.trim().length() != 0)
         m_allowSchemaChanges = sTemp.equalsIgnoreCase(XML_TRUE);

      sTemp = sourceNode.getAttribute(IS_VIEW_ATTR);
      if (sTemp != null && sTemp.trim().length() != 0)
         m_isView = sTemp.equalsIgnoreCase(XML_TRUE);

      sTemp = sourceNode.getAttribute(CREATE_ATTR);
      if (sTemp != null && sTemp.trim().length() != 0)
         m_create = sTemp.equalsIgnoreCase(XML_TRUE);

      sTemp = sourceNode.getAttribute(DELOLDDATA_ATTR);
      if (sTemp != null && sTemp.trim().length() != 0)
         m_delOldData = sTemp.equalsIgnoreCase(XML_TRUE);

      sTemp = sourceNode.getAttribute(ALTER_ATTR);
      if (sTemp != null && sTemp.trim().length() != 0)
         m_alter = sTemp.equalsIgnoreCase(XML_TRUE);

      // load schema handlers
      walker.setCurrent(sourceNode);
      Element schemaHandlersEl = walker.getNextElement(
         PSJdbcTableSchemaHandlerCollection.NODE_NAME, firstFlags);
      if (schemaHandlersEl != null)
      {
         m_schemaHandlerCollection = new PSJdbcTableSchemaHandlerCollection(
            schemaHandlersEl);
      }

      // load columns
      m_columns.clear();
      walker.setCurrent(sourceNode);
      Element row = walker.getNextElement(ROW_EL, firstFlags);
      if (row == null)
         throw new PSJdbcTableFactoryException(
            IPSTableFactoryErrors.XML_ELEMENT_NULL, ROW_EL);

      Element column = walker.getNextElement(PSJdbcColumnDef.NODE_NAME,
         firstFlags);
      if (column == null)
      {
         throw new PSJdbcTableFactoryException(
            IPSTableFactoryErrors.XML_ELEMENT_NULL, PSJdbcColumnDef.NODE_NAME);
      }

      while (column != null)
      {
         PSJdbcColumnDef colDef = new PSJdbcColumnDef(dataTypeMap, column);
         m_columns.add(colDef);
         column = walker.getNextElement(PSJdbcColumnDef.NODE_NAME, nextFlags);
      }

      // load primary key if defined
      walker.setCurrent(sourceNode);
      Element primaryKey = walker.getNextElement(PSJdbcPrimaryKey.NODE_NAME,
         firstFlags);
      if (primaryKey != null)
      {
         m_primaryKey = new PSJdbcPrimaryKey(primaryKey);

         /* since the PSJdbcPlanBuilder may compare this primary key's name to
            the database metadata, make sure we assign the default name here
            if one wasn't provided in the table def
          */
         if (StringUtils.isEmpty(m_primaryKey.getName()))
         {
            m_primaryKey.setName( "PK_" + m_name);
         }
      }
      else
         m_primaryKey = null;

      // load foreign key if defined
      walker.setCurrent(sourceNode);
      Element foreignKey = walker.getNextElement(PSJdbcForeignKey.NODE_NAME,
         firstFlags);
      m_foreignKeys.clear();
      while (foreignKey != null)
      {
         PSJdbcForeignKey fk = new PSJdbcForeignKey(foreignKey);
         
         
         // populate the parent and child table maps based on this foreign key
         Iterator parentTables = fk.getTables();
         
         // Convert old structure where there could only be one foreign key
         // into multiple if there are separate tables.
         m_foreignKeys.addAll(fk.normalizeForiegnKeys());
         
         
         PSJdbcTableSchemaCollection tableSchemaColl =
            PSJdbcTableFactory.getTableSchemaCollection();

         while(parentTables.hasNext())
         {
            String parentTableName = (String)parentTables.next();
            addParentTable(parentTableName);

            Object parentTableSchema = tableSchemaColl.getTableSchema(parentTableName);
            if (parentTableSchema != null)
            {
               tableSchemaColl.getTableSchema(parentTableName).addChildTable(m_name);
            }
            else
            {
               // this will happen if the child table is defined before the
               // parent table in the table schema definition Xml.
            }
         }
         foreignKey = walker.getNextElement(PSJdbcForeignKey.NODE_NAME,
               firstFlags);
      }
      


      // load update key if defined
      walker.setCurrent(sourceNode);
      Element updateKey = walker.getNextElement(PSJdbcUpdateKey.NODE_NAME,
         firstFlags);
      if (updateKey != null)
      {
         m_updateKey = new PSJdbcUpdateKey(updateKey);
      }
      else
         m_updateKey = null;

      // load indexes if defined
      m_indexes.clear();
      walker.setCurrent(sourceNode);
      Element indexes = walker.getNextElement(INDEX_DEF_EL, firstFlags);
      if (indexes != null)
      {
         Element index = walker.getNextElement(PSJdbcIndex.NODE_NAME,
            firstFlags);
         if (index == null)
         {
            throw new PSJdbcTableFactoryException(
               IPSTableFactoryErrors.XML_ELEMENT_NULL, PSJdbcIndex.NODE_NAME);
         }
         while (index != null)
         {
            PSJdbcIndex indexDef = new PSJdbcIndex(index);
            m_indexes.add(indexDef);
            index = walker.getNextElement(PSJdbcIndex.NODE_NAME, nextFlags);
         }
      }
      createIndexesForForeignKey(dataTypeMap);
   }

   /**
    * Creates indexes for foreign key columns if they do not exist and does
    * nothing if there are no foreign keys.
    * 
    * @param dataTypeMap data type map, assume not <code>null</code>.
    * 
    * @throws PSJdbcTableFactoryException if error occurs
    */

   private void createIndexesForForeignKey(PSJdbcDataTypeMap dataTypeMap) throws PSJdbcTableFactoryException
   {
      if (!(dataTypeMap.isCreateForeignKeyIndexes()))
          return;
      if (m_foreignKeys == null || m_foreignKeys.size()==0)
          return;
      if (doForeignKeyColumnsMatch(m_foreignKeys))
             return;
      processCreationOfIndexes(m_foreignKeys);
   }

   /**
    * Checks if there is an index with a set of columns that are same or a
    * subset of foreign key columns
    * 
    * @param foreignKey The foreign key definition. Cannot be <code>null</code>
    * 
    * @return <code>true<code> if there is an index with same set of columns as foreign
    *         key
    */
    
   private boolean doForeignKeyColumnsMatch(List<PSJdbcForeignKey> foreignKeys)
   {        
      for (PSJdbcForeignKey foreignKey : foreignKeys ) {
      List<String> foreignKeyColumnNames = foreignKey.getForeignKeyColumnNames();
      Iterator<?> indexIterator = m_indexes.iterator();
      boolean found=false;
      while(indexIterator.hasNext())
      {
         PSJdbcIndex index = (PSJdbcIndex) indexIterator.next();
         List<String> indexColumnNames = index.getIndexColumnNames();
         if((indexColumnNames.containsAll(foreignKeyColumnNames)))
         {
            found=true;
            break;
         }
         }
      if (found==false) return false;
      }
      return true;
   }

   /**
    * Checks uniqueness of a given index name
    * 
    * @param indexName name of the index to be checked if its unique
    * 
    * @return <code>true<code> if index name is unique
    */
   boolean doesIndexNameExists(String indexName)
   {
      Iterator<?> indexit1 = getIndexes(PSJdbcIndex.TYPE_UNIQUE);
      Iterator<?> indexit2 = getIndexes(PSJdbcIndex.TYPE_NON_UNIQUE);
      List<String> indexNameList = new ArrayList<String>();
      while (indexit1.hasNext())
      {
         PSJdbcIndex index1 = (PSJdbcIndex) indexit1.next();
         indexNameList.add(index1.getName());
      }
      while (indexit2.hasNext())
      {
         PSJdbcIndex index2 = (PSJdbcIndex) indexit2.next();
         indexNameList.add(index2.getName());
      }

      if (indexNameList.contains(indexName))
      {
         return false;
      }
      return true;
   }

   /**
    * Creates and loads the index to the table definition. Makes sure the index
    * name is unique.
    * 
    * @param indexName name of the index
    * @param foreignKeyColumnIt An iterator over one or more foreign key column
    *            names
    *   
    * @throws PSJdbcTableFactoryException if error occurs
    */

   private void processIndex(String indexName, Iterator<?> foreignKeyColumnIt) throws PSJdbcTableFactoryException
   {
      int i = 1;
      if (doesIndexNameExists(indexName))
      {
         PSJdbcIndex indexDef = new PSJdbcIndex(indexName, foreignKeyColumnIt, PSJdbcTableComponent.ACTION_CREATE,
                 PSJdbcIndex.TYPE_NON_UNIQUE);
         setIndex(indexDef);
      }
      else
      {
         while (!(doesIndexNameExists(indexName)))
         {
            indexName = indexName + i;
            i++;
         }
         PSJdbcIndex indexDef = new PSJdbcIndex(indexName, foreignKeyColumnIt, PSJdbcTableComponent.ACTION_CREATE,
                 PSJdbcIndex.TYPE_NON_UNIQUE);
         setIndex(indexDef);
      }
   }

   /**
    * Constructs a new index name and facilitates the creation of indexes.
    * 
    * @param foreignKey The foreign key. Assume not <code>null<code>
    * 
    * @throws PSJdbcTableFactoryException if error occurs
    */
   private void processCreationOfIndexes(List<PSJdbcForeignKey> foreignKeys) throws PSJdbcTableFactoryException
   {
      for (PSJdbcForeignKey foreignKey : foreignKeys)
      {
      if (StringUtils.isEmpty(foreignKey.getName()) || foreignKey.getName().length() < 3)
      {
         Iterator<?> itcol = foreignKey.getColumns();
         String[] fkcolnames = (String[]) itcol.next();
         String fkcolname = fkcolnames[0];
         String IxName = INDEX_PREFIX + fkcolname;
         processIndex(IxName, foreignKey.getForeignKeyColumnNames().iterator());
      }
      else
      {
         String IxName = INDEX_PREFIX + foreignKey.getName().substring(3);
         processIndex(IxName, foreignKey.getForeignKeyColumnNames().iterator());
      }
   }
   }

   /**
    * Returns <code>true</code> if the table represented by this schema is a
    * "VIEW", <code>false</code> if not.
    * @return <code>true</code> if the table represented by this schema is a
    * "VIEW", <code>false</code> if not.
    */
   public boolean isView()
   {
      return m_isView;
   }

   /**
    * Set <code>true</code> if the table represented by this schema
    * is a "VIEW", <code>false</code> if not.
    * @param isView <code>true</code> if the table represented by this schema
    * is a "VIEW", <code>false</code> if not.
    */
   public void setIsView(boolean isView)
   {
      m_isView = isView;
   }

   /**
    * Changes this table's name.
    *
    * @param name The new name, may not be <code>null</code> or empty.
    */
   public void setName(String name)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty");

      m_name = name;
   }

   /**
    * @return The name of this table, never <code>null</code> or empty.
    */
   public String getName()
   {
      return m_name;
   }

   /**
    * Sets the create flag value for this table, used when table is
    * processed by the {@link PSJdbcTableFactory}.
    *
    * @param isCreate If <code>true</code>, table is created if it does not
    * aleady exist, and data is inserted if supplied.
    *
    * If <code>false</code>, table is only altered and/or data modified if it
    * already exists.
    *
    * @throws IllegalArgumentException if isCreate is <code>true</code> and the
    * value of {@link #isAlter()} is already <code>true</code>.
    */
   public void setCreate(boolean isCreate)
   {
      m_create = isCreate;
   }

   /**
    * @return The create flag value.  See {@link #setCreate(boolean) setCreate}
    * for more info.
    */
   public boolean isCreate()
   {
      return m_create;
   }

   /**
    * Sets the delolddata flag value for this table, used when table is
    * processed by the {@link PSJdbcTableFactory}.
    *
    * @param isDelOldData If the table exists and {@link #isCreate()} is <code>
    * true</code>, this value is used to determine the action taken.  If the
    * table does not exist, exists but is empty, or if {@link #isCreate()} is
    * <code>false</code>, then this value is ignored. Otherwise:
    *
    * If <code>true</code>, old data is discarded.
    *
    * If <code>false</code>, then an attempt is made to preserve any existing
    * data while altering the table, after which any data modifications supplied
    * are processed.
    */
   public void setDelOldData(boolean isDelOldData)
   {
      m_delOldData = isDelOldData;
   }

   /**
    * @return The delolddata flag value.  See {@link #setDelOldData(boolean)
    * setDelOldData} for more info.
    */
   public boolean isDelOldData()
   {
      return m_delOldData;
   }

   /**
    * Sets the alter flag value for this table, used when table is
    * processed by the {@link PSJdbcTableFactory}.
    *
    * @param isAlter Determines if the child elements of the table are to be
    * treated as the complete schema definition, or only the changes to be made.
    * This value is ignored if {@link #isCreate()} is <code>true</code>.

    * If <code>true</code>, then child objects defined in this table schema are
    * treated as changes.  Each object (column, primary key, foreign key, index)
    * is handled based on its action attribute.  Data modifications may not also
    * be specified.
    *
    * If <code>false</code>, then a the full table schema must be provided, and
    * the action attribute of each child object is ignored.
    *
    * @throws PSJdbcTableFactoryException if isAlter() is <code>true</code> and
    * {@link #getTableData()} does not return <code>null</code>.
    */
   public void setAlter(boolean isAlter) throws PSJdbcTableFactoryException
   {
      // can't alter and have data
      if (isAlter && m_tableData != null)
         throw new PSJdbcTableFactoryException(
            IPSTableFactoryErrors.ALTER_TABLE_SET_DATA, getName());

      m_alter = isAlter;
   }

   /**
    * @return The alter flag value.  See {@link #setAlter(boolean) setAlter} for
    * more info.
    */
   public boolean isAlter()
   {
      return m_alter && !m_create;
   }

   /**
    * Method to set the flag for allowing/disallowing schema changes
    * for this table.
    *
    * @param bAllow if true then allows schema changes for this table
    */
   public void setAllowSchemaChanges(boolean bAllow)
   {
      m_allowSchemaChanges = bAllow;
   }

   /**
    * Returns true if schema changes are allowed for this table else returns
    * false.
    *
    * @return allow schema changes flag.
    */
   public boolean isAllowSchemaChanges()
   {
      return m_allowSchemaChanges;
   }

   /**
    * Returns type of action that will be taken when processing this schema.
    *
    * @return type of action that will be taken when processing this schema,
    * should be one of the following values:
    * <code>PSJdbcPlanBuilder.SCHEMA_ACTION_NONE</code> or
    * <code>PSJdbcPlanBuilder.SCHEMA_ACTION_CREATE</code> or
    * <code>PSJdbcPlanBuilder.SCHEMA_ACTION_ALTER</code> or
    * <code>PSJdbcPlanBuilder.SCHEMA_ACTION_RECREATE</code>
    */
   public int getSchemaAction()
   {
      return m_schemaAction;
   }

   /**
    * Sets the type of action that will be taken when processing this schema.
    *
    * @param schemaAction the type of action that will be taken when processing
    * this schema, should be one of the following values:
    * <code>PSJdbcPlanBuilder.SCHEMA_ACTION_NONE</code> or
    * <code>PSJdbcPlanBuilder.SCHEMA_ACTION_CREATE</code> or
    * <code>PSJdbcPlanBuilder.SCHEMA_ACTION_ALTER</code> or
    * <code>PSJdbcPlanBuilder.SCHEMA_ACTION_RECREATE</code>
    *
    * @throws IllegalArgumentException if <code>schemaAction</code> is invalid
    */
   public void setSchemaAction(int schemaAction)
   {
      if ((schemaAction == PSJdbcPlanBuilder.SCHEMA_ACTION_NONE) ||
         (schemaAction == PSJdbcPlanBuilder.SCHEMA_ACTION_CREATE) ||
         (schemaAction == PSJdbcPlanBuilder.SCHEMA_ACTION_ALTER) ||
         (schemaAction == PSJdbcPlanBuilder.SCHEMA_ACTION_RECREATE))
      {
         m_schemaAction = schemaAction;
         return;
      }
      throw new IllegalArgumentException(
         "invalid schema action : " + schemaAction);
   }

   /**
    * Sets table data on this table, used to specify the table's data when this
    * table is processed by the {@link PSJdbcTableFactory}.
    *
    * @param tableData The table data, may be <code>null</code>.
    *
    * @throws IllegalArgumentException if tableData is <code>null</code>.
    * @throws PSJdbcTableFactoryException if a column defined in the table data
    * is not defined in the table schema, or if an update has been specified and
    * no primary or update keys are defined or values for those key columns are
    * not provided, or if {@link #isAlter()} is <code>true</code>.
    */
   public void setTableData(PSJdbcTableData tableData)
      throws PSJdbcTableFactoryException
   {
      if (tableData != null)
      {
         // can't alter and have data
         if (isAlter())
            throw new PSJdbcTableFactoryException(
               IPSTableFactoryErrors.ALTER_TABLE_SET_DATA, getName());

         validateTableData(tableData);
      }

      m_tableData = tableData;
   }

   /**
    * Validates that the child components of this schema are valid.
    *
    * @throws PSJdbcTableFactoryException if a column defined in a primary
    * key, or foreign key is not defined as a column in this schema.
    */
   public void validateSchema() throws PSJdbcTableFactoryException
   {
      if (m_primaryKey != null)
      {
         String badCols = checkColumns(m_primaryKey.getColumnNames());
         if (badCols != null)
         {
            Object[] args = {getName(), PSJdbcPrimaryKey.CONTAINER_NAME,
               badCols};
            throw new PSJdbcTableFactoryException(
               IPSTableFactoryErrors.MISSING_COLUMN, args);
         }
      }

      if (m_foreignKeys != null)
      {
         for (PSJdbcForeignKey foreignKey : m_foreignKeys) {
            String badCols = checkColumns(foreignKey.getInternalColumns());
         if (badCols != null)
         {
            Object[] args = {getName(), PSJdbcForeignKey.CONTAINER_NAME,
               badCols};
            throw new PSJdbcTableFactoryException(
               IPSTableFactoryErrors.MISSING_COLUMN, args);
         }
      }
      }

      if (m_updateKey != null)
      {
         String badCols = checkColumns(m_updateKey.getColumnNames());
         if (badCols != null)
         {
            Object[] args = {getName(), PSJdbcUpdateKey.CONTAINER_NAME,
               badCols};
            throw new PSJdbcTableFactoryException(
               IPSTableFactoryErrors.MISSING_COLUMN, args);
         }
      }

      Iterator indexes = m_indexes.iterator();
      while (indexes.hasNext())
      {
         PSJdbcIndex index = (PSJdbcIndex)indexes.next();
         String badCols = checkColumns(index.getColumnNames());
         if (badCols != null)
         {
            Object[] args = {getName(), PSJdbcIndex.CONTAINER_NAME + ": " +
               index.getName(), badCols};
            throw new PSJdbcTableFactoryException(
               IPSTableFactoryErrors.MISSING_COLUMN, args);
         }
      }
   }

   /**
    * Validates that the supplied tableData is valid for this schema.
    *
    * @param tableData The table data, never <code>null</code>.
    *
    * @throws IllegalArgumentException if tableData is <code>null</code>.
    * @throws PSJdbcTableFactoryException if a column defined in the table data
    * is not defined in this schema, or if an update has been specified and
    * no primary or update keys are defined or values for those key columns are
    * not provided.
    */
   public void validateTableData(PSJdbcTableData tableData)
      throws PSJdbcTableFactoryException
   {
      if (tableData == null)
         throw new IllegalArgumentException("tableData may not be null");

      // validate has no columns not defined in schema, required keys defined
      Iterator columns = tableData.getColumnNames();
      while (columns.hasNext())
      {
         String columnName = (String)columns.next();
         PSJdbcColumnDef columnDef = getColumn(columnName);
         if (columnDef == null || (columnDef.getAction() ==
            PSJdbcTableComponent.ACTION_DELETE && isAlter()))
         {
            Object[] args = {tableData.getName(), columnName};
            throw new PSJdbcTableFactoryException(
               IPSTableFactoryErrors.COLUMN_NOT_FOUND,
               args);
         }
      }

      /* if data has any updates, replaces or deletes, make sure that the
       * schema has update or primary keys defined, and values are provided for
       * those columns.
       */
      if (tableData.hasUpdates())
      {
         if (m_primaryKey == null && m_updateKey == null)
            throw new PSJdbcTableFactoryException(
               IPSTableFactoryErrors.UPDATE_DATA_NO_KEYS, getName());

         // walk each update row and be sure there's a value for each key column
         List keyCols = getKeyColumns();
         Iterator updateRows = tableData.getUpdateRows();
         while (updateRows.hasNext())
         {
            PSJdbcRowData row = (PSJdbcRowData)updateRows.next();

            Iterator iKeys = keyCols.iterator();
            while (iKeys.hasNext())
            {
               String columnName = (String)iKeys.next();
               PSJdbcColumnData colData = row.getColumn(columnName);
               if (colData == null || colData.getValue() == null)
               {
                 Object[] args = {getName(), columnName};
                 throw new PSJdbcTableFactoryException(
                    IPSTableFactoryErrors.UPDATE_DATA_NO_KEY_VALUE, args);
               }
            }
         }
      }
   }

   /**
    * Returns the table data object if one has been set.  See {@link
    * #setTableData(PSJdbcTableData) setTableData} for more info.
    *
    * @return The table data, may be <code>null</code>
    */
   public PSJdbcTableData getTableData()
   {
      return m_tableData;
   }

   /**
    * Compares this schema to another object.
    *
    * @param obj the object to compare
    * @return <code>true</code> if the object is a PSJdbcTableSchema with
    *    identical values, excluding the dataTypeMap. Otherwise returns
    *    <code>false</code>.
    */
   public boolean equals(Object obj)
   {
      boolean isMatch = true;
      if (!(obj instanceof PSJdbcTableSchema))
         isMatch = false;
      else
      {
         PSJdbcTableSchema other = (PSJdbcTableSchema)obj;
         if (!this.m_columns.equals(other.m_columns))
            isMatch = false;
         else if (this.m_primaryKey != null ^ other.m_primaryKey != null)
            isMatch = false;
         else if (this.m_primaryKey != null && !this.m_primaryKey.equals(
            other.m_primaryKey))
            isMatch = false;
         else if (this.m_updateKey != null ^ other.m_updateKey != null)
            isMatch = false;
         else if (this.m_updateKey != null && !this.m_updateKey.equals(
            other.m_updateKey))
            isMatch = false;
         else if (this.m_create != other.m_create)
            isMatch = false;
         else if (this.m_delOldData != other.m_delOldData)
            isMatch = false;
         else if (this.m_alter != other.m_alter)
            isMatch = false;
         else if (this.m_allowSchemaChanges != other.m_allowSchemaChanges)
            isMatch = false;
         else if (this.m_isView != other.m_isView)
            isMatch = false;
         else if (this.m_foreignKeys != null ^ other.m_foreignKeys != null)
            isMatch = false;
         else if (this.m_foreignKeys != null && !this.m_foreignKeys.equals(
            other.m_foreignKeys))
            isMatch = false;
         else if (!this.m_indexes.equals(other.m_indexes))
            isMatch = false;
         else if (this.m_tableData != null ^ other.m_tableData != null)
            isMatch = false;
         else if (this.m_tableData != null && !this.m_tableData.equals(
            other.m_tableData))
            isMatch = false;
      }

      return isMatch;

   }

   /**
    * Overridden to fullfill the contract that if t1 and t2 are 2 different
    * instances of this class and t1.equals(t2), t1.hashCode() ==
    * t2.hashCode().
    *
    * @return The sum of all the hash codes of the composite objects.
    */
   public int hashCode()
   {
      int hash = 0;
      hash += m_name.hashCode();
      hash += m_columns.hashCode();
      hash += Boolean.valueOf(m_alter).hashCode();
      hash += Boolean.valueOf(m_allowSchemaChanges).hashCode();
      hash += Boolean.valueOf(m_isView).hashCode();
      hash += Boolean.valueOf(m_create).hashCode();
      hash += Boolean.valueOf(m_delOldData).hashCode();
      if (m_foreignKeys != null)
         hash += m_foreignKeys.hashCode();
      if (m_primaryKey != null)
         hash += m_primaryKey.hashCode();
      if (m_updateKey != null)
         hash += m_updateKey.hashCode();
      hash += m_indexes.hashCode();
      if (m_tableData != null)
         hash += m_tableData.hashCode();

      return hash;
   }


   /**
    * Get the dataTypeMap from the first column in this table.
    *
    * @return The map, never <code>null</code>.
    */
   public PSJdbcDataTypeMap getDataTypeMap()
   {
      // get map from first column
      PSJdbcColumnDef col = (PSJdbcColumnDef)m_columns.get(0);
      return col.getDataTypeMap();
   }

   /**
    * @return <code>false</code> if all table components have an action of
    * PSJdbcTableComponent#ACTION_NONE, <code>true</code> if not.
    */
   public boolean hasChanges()
   {
      boolean hasChanges = false;
      for (int i=0; i < m_columns.size() && !hasChanges; i++)
      {
         PSJdbcColumnDef col = (PSJdbcColumnDef)m_columns.get(i);
            hasChanges = col.hasChanges();
      }

      if (!hasChanges && m_primaryKey != null)
            hasChanges = m_primaryKey.hasChanges();

      for (int i=0; i < m_foreignKeys.size() && !hasChanges; i++)
      {
         PSJdbcForeignKey foreignKey = m_foreignKeys.get(i);
            hasChanges = foreignKey.hasChanges();
      }

      return hasChanges;
   }

   /**
    * Returns a schema handler object of the specified type if it exists
    * in schema handler collection associated with this table schema,
    * otherwise returns <code>null</code>.
    *
    * @param schemaHandlerType The type of the schema handler to locate.
    *
    * @return a schema handler object of the specified type if it exists
    * in schema handler collection associated with this table schema,
    * otherwise returns <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>schemaHandlerType</code>
    * is not one of the following values:
    * <code>TYPE_INT_NO_ALTER_TABLE_STMT</code>
    * <code>TYPE_INT_TO_BACKUP</code>
    * <code>TYPE_INT_FROM_BACKUP</code>
    * <code>TYPE_INT_NO_ALTER_TABLE_STMT</code>
    */
   public PSJdbcTableSchemaHandler getTableSchemaHandler(int schemaHandlerType)
   {
      if (m_schemaHandlerCollection == null)
         return null;
      return m_schemaHandlerCollection.getTableSchemaHandler(schemaHandlerType);
   }

   /**
    * Determines if this table can be processed using ALTER TABLE statements
    * if it exists.
    * @return <code>true</code> if the only changes are adding columns, and
    * keys.
    */
   public boolean canBeAltered()
   {
      Iterator columns = m_columns.iterator();
      while (columns.hasNext())
      {
         PSJdbcColumnDef col = (PSJdbcColumnDef)columns.next();
         if (!col.canAlter())
            return false;
      }

      if (m_primaryKey != null && !m_primaryKey.canAlter())
         return false;

      for (PSJdbcForeignKey foreignKey : m_foreignKeys)
      {
         if (!foreignKey.canAlter())
         return false;
      }

      Iterator indexes = m_indexes.iterator();
      while (indexes.hasNext())
      {
         PSJdbcIndex index = (PSJdbcIndex)indexes.next();
         if (!index.canAlter())
            return false;
      }

      return true;
   }


   /**
    * Builds list of column names that comprise the key to use when updating
    * a row of data.
    *
    * @return A list of column names as Strings.  If this tableSchema contains
    * an update key, those columns will be used, otherwise the columns from the
    * primary key will be used.  If neither are defined, an empty list is
    * returned.
    */
   public List getKeyColumns()
   {
      List keyCols = new ArrayList();

      // use update key if defined, else use primary key
      Iterator colNames = null;
      if (m_updateKey != null)
      {
         colNames = m_updateKey.getColumnNames();
      }
      else if (m_primaryKey != null)
      {
         colNames = m_primaryKey.getColumnNames();
      }

      while (colNames != null && colNames.hasNext())
         keyCols.add(colNames.next());

      return keyCols;
   }

   /**
    * Adds the name of the parent table into the list containing the
    * name of this table's parent tables.
    *
    * @param parentTableName name of the this table's parent table, may not be
    * <code>null</code> or empty
    *
    * @throws IllegalArgumentException if parentTableName is
    * <code>null</code> or empty
    */
   public void addParentTable(String parentTableName)
   {
      if ((parentTableName == null) || (parentTableName.trim().length() == 0))
         throw new IllegalArgumentException(
            "parentTableName may not be null or empty");

      if (!(m_parentTables.contains(parentTableName)))
         m_parentTables.add(parentTableName);
   }

   /**
    * Adds the name of the child table into the list containing the
    * name of this table's child tables.
    *
    * @param childTableName name of the child table, may not be
    * <code>null</code> or empty
    *
    * @throws IllegalArgumentException if childTableName is
    * <code>null</code> or empty
    */
   public void addChildTable(String childTableName)
   {
      if ((childTableName == null) || (childTableName.trim().length() == 0))
         throw new IllegalArgumentException(
            "childTableName may not be null or empty");

      if (!(m_childTables.contains(childTableName)))
         m_childTables.add(childTableName);
   }

   /**
    * Adds a listener to be informed of table schema change events.
    *
    * @param listener The listener, may not be <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>listener</code> is
    * <code>null</code>.
    */
   public void addSchemaChangeListener(IPSJdbcTableChangeListener listener)
   {
      if (listener == null)
         throw new IllegalArgumentException("listener may not be null");

      if (m_schemaChangeListeners == null)
         m_schemaChangeListeners = new ArrayList();
      m_schemaChangeListeners.add(listener);
   }

   /**
    * Gets the list of {@link IPSJdbcTableChangeListener} objects to be informed
    * of changes to this table's schema.
    *
    * @return The list, may be <code>null</code>, never empty.
    */
   public List getSchemaChangeListeners()
   {
      return m_schemaChangeListeners;
   }

   /**
    * Returns the index into the internal column list of the specified column.
    *
    * @param name The name of the column to find.  Assumed not <code>null</code>
    * or emtpy.
    *
    * @return The index, or <code>-1</code> if not found in the list.  Match
    * is case insensitive by name.
    */
   private int getColumnIndex(String name)
   {
      PSJdbcColumnDef column = null;
      int index = -1;

      for (int i = 0; i < m_columns.size(); i++)
      {
         column = (PSJdbcColumnDef)m_columns.get(i);
         if (column.getName().equalsIgnoreCase(name))
         {
            index = i;
            break;
         }
      }

      return index;
   }

   /**
    * Returns the ordinal position in the internal list of indexes for the
    * specified index object.
    *
    * @param name The name of the index to find.  Assumed not <code>null</code>
    * or emtpy.
    *
    * @return The ordinal position, or <code>-1</code> if not found in the list.
    * Match is case insensitive by name.
    */
   private int getIndexIndex(String name)
   {
      PSJdbcIndex indexObj = null;
      int index = -1;

      for (int i = 0; i < m_indexes.size(); i++)
      {
         indexObj = (PSJdbcIndex)m_indexes.get(i);
         if (indexObj.getName().equalsIgnoreCase(name))
         {
            index = i;
            break;
         }
      }

      return index;
   }

   /**
    * Checks each name in the supplied iterator and validates that a column
    * with that name is defined in this schema.
    *
    * @param names An iterator over zero or more names, assumed not <code>null
    * </code> and to contain only String objects.
    *
    * @return <code>null</code> if all names in the iterator have corresponding
    * column definitions in this schema, a comma delimited list of invalid names
    * if not.
    */
   private String checkColumns(Iterator names)
   {
      List badNameList = new ArrayList();
      while (names.hasNext())
      {
         String name = (String)names.next();
         if (getColumnIndex(name) == -1)
            badNameList.add(name);
      }

      return badNameList.isEmpty() ? null : badNameList.toString();
   }
   
   /**
    * Puts/associates TableWithForeignKey relationship to this schema.
    * @param schema never <code>null</code>.
    */
   public void addTableWithForeignKey(PSJdbcTableSchema schema)
   { 
      if (schema == null)
         throw new IllegalArgumentException("schema must not be null");
      
      m_mapTablesWithForeignKey.put(schema.getName(), schema);
   }
   
   /**
    * Returns an iterator to collection of PSJdbcTableSchema that 
    * have Foreign Keys.
    * @return never <code>null</code>, may be <code>empty</code>.
    */
   public Iterator getTablesWithForeignKey()
   {
      return m_mapTablesWithForeignKey.values().iterator();
   }

  
   /**
    * The name of this objects root Xml element.
    */
   public static String NODE_NAME = "table";

   /**
    * The name of this table.  Initialized in the ctor, never <code>null</code>
    * after that.
    */
   private String m_name = null;

   /**
    * The create flag for this table.  See {@link #setCreate(boolean)} for more
    * info.
    */
   private boolean m_create = true;

   /**
    * The create flag for this table.  See {@link #setDelOldData(boolean)} for
    * more info.
    */
   private boolean m_delOldData = false;

   /**
    * The create flag for this table.  See {@link #setAlter(boolean)} for more
    * info.
    */
   private boolean m_alter = false;

   /**
    * The create flag for this table.  See
    * {@link #setAllowSchemaChanges(boolean)} for more info.
    */
   private boolean m_allowSchemaChanges = true;

   /**
    * <code>true</code> if the table represented by this schema is a
    * "VIEW", <code>false</code> if not.
    */
   private boolean m_isView = false;

   /**
    * This table's column definitions.  Never <code>null</code> or empty after
    * construction, contains only valid PSJdbcColumnDef objects.
    */
   private List m_columns = new ArrayList();

   /**
    * List for storing the name of this table's parent tables.
    * May be empty.
    */
   private List m_parentTables = new ArrayList();

   /**
    * List for storing the name of this table's child tables.
    * May be empty.
    */
   private List m_childTables = new ArrayList();

   /**
    * This table's primary key defintion.  May be <code>null</code>.
    */
   private PSJdbcPrimaryKey m_primaryKey = null;

   /**
    * This table's foreign key defintion.  May be <code>null</code>.
    */
   private List<PSJdbcForeignKey> m_foreignKeys = new ArrayList<PSJdbcForeignKey>();

   /**
    * This table's index definitions.  Never <code>null</code>, may be empty.
    */
   private List m_indexes = new ArrayList();

   /**
    * This table's update key defintion.  Used when updating rows as the key
    * columns if it has been defined.  May be <code>null</code>.
    */
   private PSJdbcUpdateKey m_updateKey = null;

   /**
    * This table's data.  May be <code>null</code> if none has been specified.
    * Used to specify the table's data when processed by the {@link
    * PSJdbcTableFactory}.
    */
   private PSJdbcTableData m_tableData = null;

   /**
    * List of {@link IPSJdbcTableChangeListener} objects to be notified of
    * schema changes.  May be <code>null</code>, never empty, modified by calls
    * to {@link #addSchemaChangeListener(IPSJdbcTableChangeListener)
    * addTableChangeListener}.
    */
   private List m_schemaChangeListeners = null;
   
   /**
    * This map holds a set of PSJdbcTableSchema objects that have a foreign key
    * relationship with this table. never <code>null</code>,
    * may be <code>empty</code>.
    */
   private Map  m_mapTablesWithForeignKey = new HashMap();

   /**
    * Collection of table schema handler objects, may be <code>null</code>.
    */
   private PSJdbcTableSchemaHandlerCollection m_schemaHandlerCollection = null;
   
    /**
     * Prefix to be added to the new index name of a new index that gets created
     * when createForeignkeyIndexes mapping in is set to "yes" in
     * PSJdbcDataTypeMap.xml
     */
   private static final String INDEX_PREFIX = "IX_";

   /**
    * The type of action that will be taken when processing this schema.
    * Default is <code>PSJdbcPlanBuilder.SCHEMA_ACTION_NONE</code>, which
    * indicates that no action will be taken when processing the schema.
    * Valid values for this variable are:
    * <code>PSJdbcPlanBuilder.SCHEMA_ACTION_NONE</code> or
    * <code>PSJdbcPlanBuilder.SCHEMA_ACTION_CREATE</code> or
    * <code>PSJdbcPlanBuilder.SCHEMA_ACTION_ALTER</code> or
    * <code>PSJdbcPlanBuilder.SCHEMA_ACTION_RECREATE</code>
    */
   private int m_schemaAction = PSJdbcPlanBuilder.SCHEMA_ACTION_NONE;

   // Xml elements and attributes
   private static final String ROW_EL = "row";
   private static final String NAME_ATTR = "name";
   private static final String CREATE_ATTR = "create";
   private static final String DELOLDDATA_ATTR = "delolddata";
   private static final String ALTER_ATTR = "alter";
   private static final String ALLOW_SCHEMA_CHANGES_ATTR = "allowSchemaChanges";
   private static final String IS_VIEW_ATTR = "isView";
   private static final String XML_TRUE = "y";
   private static final String XML_FALSE = "n";
   private static final String INDEX_DEF_EL = "indexdefinitions";
   
    @Override
    public int compareTo(PSJdbcTableSchema o) {
        // Order alphabetically but put tables with foreign keys after those without and
        // order tables that reference this after

        int value = foreignKeyCompare(o.getName(),this.getForeignKeys()) - foreignKeyCompare(this.getName(),o.getForeignKeys());
        return value==0 ? this.getName().compareTo(o.getName()) : value;
    }

    private int foreignKeyCompare(String tableName, List<PSJdbcForeignKey> fks)
    {
        int value = 0;
        if (fks==null) return value;
        Iterator<PSJdbcForeignKey> fksItt = fks.iterator();
        while (fksItt.hasNext()) {
            Iterator<String> fkItt = fksItt.next().getTables();
            if (fkItt.hasNext())
                value++;
            while (fkItt.hasNext()) {
                String fkTable = fkItt.next();
                if (fkTable.equals(tableName)) {
                    value++;
                    break;
                }

            }
        }
        return value;
    }
   }

