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
   public PSJdbcTableSchema(String name, Iterator<PSJdbcColumnDef> columns)
      throws PSJdbcTableFactoryException
   {
      this(name);
      if (columns == null || !columns.hasNext())
         throw new IllegalArgumentException("columns may not be null or empty");

      while (columns.hasNext())
      {
         PSJdbcColumnDef column = columns.next();
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
      m_parentTables.addAll(source.m_parentTables);
      m_childTables.addAll(source.m_childTables);
      if (source.m_schemaHandlerCollection != null)
      {
         m_schemaHandlerCollection = new PSJdbcTableSchemaHandlerCollection();
         m_schemaHandlerCollection.addAll(source.m_schemaHandlerCollection);
      }

      try {
         PSJdbcPrimaryKey pk = source.getPrimaryKey();
         if (pk != null)
            m_primaryKey = new PSJdbcPrimaryKey(pk.getColumnNames(),
                    pk.getAction());
         List<PSJdbcForeignKey> fks = source.getForeignKeys();
         if (fks != null) {
            List<PSJdbcForeignKey> newKeys = new ArrayList<>();
            for (PSJdbcForeignKey fk : fks) {
               newKeys.add(new PSJdbcForeignKey(fk.getColumns(),
                       fk.getAction()));
            }
            m_foreignKeys = newKeys;
         }


         PSJdbcUpdateKey uk = source.getUpdateKey();
         if (uk != null)
            m_updateKey = new PSJdbcUpdateKey(uk.getColumnNames());

         Iterator<PSJdbcColumnDef> cols = source.getColumns();
         while (cols.hasNext())
            m_columns.add(new PSJdbcColumnDef(cols.next()));

         Iterator<PSJdbcIndex> indexes = source.getIndexes(
                 PSJdbcIndex.TYPE_UNIQUE | PSJdbcIndex.TYPE_NON_UNIQUE);

         while (indexes.hasNext()) {
            PSJdbcIndex index = indexes.next();
            m_indexes.add(new PSJdbcIndex(index.getName(),
                    index.getColumnNames(), index.getAction(), index.getType()));
         }

      }
      catch (PSJdbcTableFactoryException e)
      {
         // cant' really happen!
         throw new IllegalArgumentException("invalid source schema: "
            + e);
      }
   }

   /**
    * Returns an Iterator over one or more valid PSJdbcColumnDef objects.
    *
    * @return The Iterator, never <code>null</code> or empty.
    */
   public Iterator<PSJdbcColumnDef> getColumns()
   {
      return m_columns.iterator();
   }

   /**
    * Returns an iterator over the <code>String</code> objects representing the
    * name of child tables of this table.
    *
    * @return the Iterator, may be empty.
    */
   public Iterator<String> getChildTables()
   {
      return m_childTables.iterator();
   }

   /**
    * Returns an iterator over the <code>String</code> objects representing the
    * name of parent tables of this table.
    *
    * @return the Iterator, may be empty.
    */
   public Iterator<String> getParentTables()
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
         column = m_columns.get(index);

      return column;
   }

   /**
    * Appends the column to this table definition.  If a column with the same
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
         oldCol = m_columns.get(index);
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

         oldCol = m_columns.get(index);
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
    * @throws PSJdbcTableFactoryException if the primary key references columns
    * not defined in this schema.
    */
   public void setPrimaryKey(PSJdbcPrimaryKey primaryKey)
      throws PSJdbcTableFactoryException
   {
      m_primaryKey = primaryKey;
      validateSchema();
   }

   /**
    * Returns the primary key definition if one has been set.
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
    * @throws PSJdbcTableFactoryException if the foreign key references
    * internal columns not defined in this schema (external columns are not
    * validated until the schema is processed).
    */
   public void setForeignKeys(List<PSJdbcForeignKey> foreignKeys,boolean createIndexes)
      throws PSJdbcTableFactoryException
   {
      if (foreignKeys == null)
      {
         m_foreignKeys.clear();
      }
      else
      {
         m_foreignKeys.clear();
         m_foreignKeys.addAll(foreignKeys);
         if(createIndexes) {
            //Add indexes for foreign keys
            this.createIndexesForForeignKey(this.getDataTypeMap());
         }
      }
      validateSchema();
   }

   /**
    * Returns the foreign key definitions if one has been set.
    *
    * @return The foreign key, or <code>null</code> if one has not been set.
    */
   public List<PSJdbcForeignKey> getForeignKeys()
   {
      return m_foreignKeys;
   }

   /**
    * Adds the index to this table definition.  If an index with the
    * same name already exists, it is replaced.  All columns referenced by the
    * index must be defined in this schema (may have an action of {@link
    * PSJdbcTableComponent#ACTION_NONE}).
    *
    * @param index The index to set on this table, may not be <code>null</code>.
    *
    * @return <code>null</code> if there is not already an index with the same
    * name that is replaced, or the old index object if it is replaced.
    *
    * @throws IllegalArgumentException if index is <code>null</code>.
    * @throws PSJdbcTableFactoryException if the index references columns
    * not defined in this schema.
    */
   public PSJdbcIndex setIndex(PSJdbcIndex index)
      throws PSJdbcTableFactoryException
   {
      if (index == null)
         throw new IllegalArgumentException("index may not be null");

      int oldIdx = m_indexes.indexOf(index);
      PSJdbcIndex oldIndex = null;

      if(oldIdx == -1){
         m_indexes.add(index);
      }else{
         oldIndex = m_indexes.get(oldIdx);
         m_indexes.remove(oldIndex);
         m_indexes.add(index);
      }
      validateSchema();
      return oldIndex;
   }

   public void resetIndexes(List<PSJdbcIndex> indexes)
           throws PSJdbcTableFactoryException
   {
      if (indexes == null)
         throw new IllegalArgumentException("indexes may not be null");
      m_indexes.clear();
      //build Unique Name list of indexes before renaming new Indexes.
      List<String> uniqueNames = new ArrayList<>();
      for (PSJdbcIndex idx1 : indexes){
         if(idx1.getAction() == PSJdbcTableComponent.ACTION_NONE ) {
            uniqueNames.add(idx1.getName());
         }
      }
      for (PSJdbcIndex idx : indexes){
         //if Action is Create, then make sure index name is unique
         if(idx.getAction() == PSJdbcTableComponent.ACTION_CREATE) {
            String idxName = idx.getName();
            String newName = idxName;
            int no = 1;
            while (uniqueNames.contains(newName)) {
               newName = idxName +"_" + no;
               no++;
            }
            idx.setName(newName);
            uniqueNames.add(idx.getName());
         }
         m_indexes.add(idx);
      }
      validateSchema();
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

      for(PSJdbcIndex idx : m_indexes){
         if(idx.getName().equalsIgnoreCase(name)){
            return idx;
         }
      }
      return null;

   }

   public PSJdbcIndex getIndex(PSJdbcIndex newIndex)
   {
      if (newIndex == null )
         throw new IllegalArgumentException("index may not be null");

     int idx = m_indexes.indexOf(newIndex);
     if(idx == -1){
        return null;
     }else{
        return m_indexes.get(idx);
     }

   }

   /**
    * Convenience method that calls
    * {@link #getIndexes(int) getIndexes(PSJdbcIndex.TYPE_UNIQUE)}
    */
   public Iterator<PSJdbcIndex> getIndexes()
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
   public Iterator<PSJdbcIndex> getIndexes(int type)
   {
      List<PSJdbcIndex> indexList = new ArrayList<>();
      for (PSJdbcIndex index : m_indexes) {
         if (index.isOfType(type))
            indexList.add(index);
      }
      return indexList.iterator();
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
      PSJdbcIndex removeIdx = null;
      for (PSJdbcIndex idx : m_indexes){
         if(idx.getName().equalsIgnoreCase(name)){
            removeIdx = idx;
            break;
         }
      }
      if(removeIdx != null){
         m_indexes.remove(removeIdx);
      }
      return removeIdx;

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
    * @throws IllegalArgumentException if updateKey is <code>null</code>.
    * @throws PSJdbcTableFactoryException if the update key references columns
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
    * Returns the update key definition if one has been set.
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
      for (PSJdbcColumnDef m_column : m_columns) row.appendChild(m_column.toXml(doc));

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
         for (int i = 0; i < m_indexes.size(); i++) {
            PSJdbcIndex idx = m_indexes.get(i);
            indexdefs.appendChild(idx.toXml(doc));
         }
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
      if (sTemp.trim().length() == 0)
      {
         Object[] args = {NODE_NAME, NAME_ATTR, sTemp};
         throw new PSJdbcTableFactoryException(
            IPSTableFactoryErrors.XML_ELEMENT_INVALID_ATTR, args);
      }
      m_name = sTemp;

      sTemp = sourceNode.getAttribute(ALLOW_SCHEMA_CHANGES_ATTR);
      if (sTemp.trim().length() != 0)
         m_allowSchemaChanges = sTemp.equalsIgnoreCase(XML_TRUE);

      sTemp = sourceNode.getAttribute(IS_VIEW_ATTR);
      if (sTemp.trim().length() != 0)
         m_isView = sTemp.equalsIgnoreCase(XML_TRUE);

      sTemp = sourceNode.getAttribute(CREATE_ATTR);
      if (sTemp.trim().length() != 0)
         m_create = sTemp.equalsIgnoreCase(XML_TRUE);

      sTemp = sourceNode.getAttribute(DELOLDDATA_ATTR);
      if (sTemp.trim().length() != 0)
         m_delOldData = sTemp.equalsIgnoreCase(XML_TRUE);

      sTemp = sourceNode.getAttribute(ALTER_ATTR);
      if (sTemp.trim().length() != 0)
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
         Iterator<String> parentTables = fk.getTables();
         
         // Convert old structure where there could only be one foreign key
         // into multiple if there are separate tables.
         m_foreignKeys.addAll(fk.normalizeForeignKeys());
         
         
         PSJdbcTableSchemaCollection tableSchemaColl =
            PSJdbcTableFactory.getTableSchemaCollection();

         while(parentTables.hasNext())
         {
            String parentTableName = parentTables.next();
            addParentTable(parentTableName);

            Object parentTableSchema = tableSchemaColl.getTableSchema(parentTableName);
            if (parentTableSchema != null)
            {
               tableSchemaColl.getTableSchema(parentTableName).addChildTable(m_name);
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
      if (m_foreignKeys == null || m_foreignKeys.isEmpty())
          return;
      if (doForeignKeyColumnsMatch(m_foreignKeys))
             return;
      processCreationOfIndexes(m_foreignKeys);
   }

   /**
    * Checks if there is an index with a set of columns that are same or a
    * subset of foreign key columns
    * 
    * @param foreignKeys The foreign key definition. Cannot be <code>null</code>
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
      if (!found) return false;
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
      PSJdbcIndex index = this.getIndex(indexName);
      return index != null;
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

   private void processIndex(String indexName, Iterator<String> foreignKeyColumnIt) throws PSJdbcTableFactoryException
   {
      int i = 1;

      PSJdbcIndex indexDef = new PSJdbcIndex(indexName, foreignKeyColumnIt, PSJdbcTableComponent.ACTION_CREATE,
              PSJdbcIndex.TYPE_NON_UNIQUE);

      if( getIndex(indexDef) == null) {
         if (!doesIndexNameExists(indexName)) {
            setIndex(indexDef);
         } else {
            while ((doesIndexNameExists(indexName))) {
               indexName = indexName + i;
               i++;
            }
            indexDef.setName(indexName  );
               setIndex(indexDef);
            }
      }
   }

   /**
    * Constructs a new index name and facilitates the creation of indexes.
    * 
    * @param foreignKeys The foreign key. Assume not <code>null<code>
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
    * already exist, and data is inserted if supplied.
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

      for (PSJdbcIndex index : m_indexes) {
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
      Iterator<String> columns = tableData.getColumnNames();
      while (columns.hasNext())
      {
         String columnName = columns.next();
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
         List<String> keyCols = getKeyColumns();
         Iterator<PSJdbcRowData> updateRows = tableData.getUpdateRows();
         while (updateRows.hasNext())
         {
            PSJdbcRowData row = updateRows.next();

            for (String keyCol : keyCols) {
               PSJdbcColumnData colData = row.getColumn(keyCol);
               if (colData == null || colData.getValue() == null) {
                  Object[] args = {getName(), keyCol};
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
    * Overridden to fulfill the contract that if t1 and t2 are 2 different
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
      PSJdbcColumnDef col = m_columns.get(0);
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
         PSJdbcColumnDef col = m_columns.get(i);
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
      for (PSJdbcColumnDef m_column : m_columns) {
         if (!m_column.canAlter()) {
            return false;
         }else if(m_column.isChanged()){
            if(m_primaryKey != null && m_primaryKey.hasColumn(m_column.getName())) {
               return false;
            }
         }
      }

      if (m_primaryKey != null && !m_primaryKey.canAlter())
         return false;

      for (PSJdbcForeignKey foreignKey : m_foreignKeys)
      {
         if (!foreignKey.canAlter())
            return false;
      }

      for (PSJdbcIndex index : m_indexes) {
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
   public List<String> getKeyColumns()
   {
      List<String> keyCols = new ArrayList<>();

      // use update key if defined, else use primary key
      Iterator<String> colNames = null;
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
         m_schemaChangeListeners = new ArrayList<>();
      m_schemaChangeListeners.add(listener);
   }

   /**
    * Gets the list of {@link IPSJdbcTableChangeListener} objects to be informed
    * of changes to this table's schema.
    *
    * @return The list, may be <code>null</code>, never empty.
    */
   public List<IPSJdbcTableChangeListener> getSchemaChangeListeners()
   {
      return m_schemaChangeListeners;
   }

   /**
    * Returns the index into the internal column list of the specified column.
    *
    * @param name The name of the column to find.  Assumed not <code>null</code>
    * or empty.
    *
    * @return The index, or <code>-1</code> if not found in the list.  Match
    * is case insensitive by name.
    */
   private int getColumnIndex(String name)
   {
      PSJdbcColumnDef column;
      int index = -1;

      for (int i = 0; i < m_columns.size(); i++)
      {
         column = m_columns.get(i);
         if (column.getName().equalsIgnoreCase(name))
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
   private String checkColumns(Iterator<String> names)
   {
      List<String> badNameList = new ArrayList<>();
      while (names.hasNext())
      {
         String name = names.next();
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
   public Iterator<PSJdbcTableSchema> getTablesWithForeignKey()
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
   private List<PSJdbcColumnDef> m_columns = new ArrayList<>();

   /**
    * List for storing the name of this table's parent tables.
    * May be empty.
    */
   private List<String> m_parentTables = new ArrayList<>();

   /**
    * List for storing the name of this table's child tables.
    * May be empty.
    */
   private List<String> m_childTables = new ArrayList<>();

   /**
    * This table's primary key definition.  May be <code>null</code>.
    */
   private PSJdbcPrimaryKey m_primaryKey = null;

   /**
    * This table's foreign key definition.  May be <code>null</code>.
    */
   private List<PSJdbcForeignKey> m_foreignKeys = new ArrayList<>();

   /**
    * This table's index definitions.  Never <code>null</code>, may be empty.
    */
   private List<PSJdbcIndex> m_indexes = new ArrayList<>();

   /**
    * This table's update key definition.  Used when updating rows as the key
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
   private List<IPSJdbcTableChangeListener> m_schemaChangeListeners = null;
   
   /**
    * This map holds a set of PSJdbcTableSchema objects that have a foreign key
    * relationship with this table. never <code>null</code>,
    * may be <code>empty</code>.
    */
   private Map<String,PSJdbcTableSchema>  m_mapTablesWithForeignKey = new HashMap<>();

   /**
    * Collection of table schema handler objects, may be <code>null</code>.
    */
   private PSJdbcTableSchemaHandlerCollection m_schemaHandlerCollection = null;
   
    /**
     * Prefix to be added to the new index name of a new index that gets created
     * when createForeignkeyIndexes mapping in is set to "yes" in
     * PSJdbcDataTypeMap.xml
     */
   public static final String INDEX_PREFIX = "IX_";

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
       for (PSJdbcForeignKey fk : fks) {
          Iterator<String> fkItt = fk.getTables();
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

