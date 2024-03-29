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
package com.percussion.deployer.server;

import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.design.objectstore.PSContentEditorMapper;
import com.percussion.design.objectstore.PSContentEditorPipe;
import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.design.objectstore.PSTableRef;
import com.percussion.design.objectstore.PSTableSet;
import com.percussion.error.IPSDeploymentErrors;
import com.percussion.error.PSDeployException;
import com.percussion.tablefactory.PSJdbcTableData;
import com.percussion.tablefactory.PSJdbcTableFactoryException;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.util.Iterator;

/**
 * Encapsulates a item defintion and Jdbc table data for a single table that is
 * part of a content item's full data for use in making modifications to the
 * table data based on information in the item def.
 */
public class PSItemData
{
   /**
    * Construct from member data.
    * 
    * @param itemDef The item definition to which the data applies, may not be
    * <code>null</code>.
    * @param itemData The item data to check, may not be <code>null</code>.
    * 
    * @throws PSDeployException if there is an error cloning the itemData
    */
   public PSItemData(PSItemDefinition itemDef,  PSJdbcTableData itemData) 
      throws PSDeployException
   {
      if (itemDef == null)
         throw new IllegalArgumentException("itemDef may not be null");
      
      if (itemData == null)
         throw new IllegalArgumentException("itemData may not be null");
      
      m_itemDef = itemDef;
      m_srcItemData = itemData;
      
      try
      {
         // quick deep clone - not efficient, but it's okay here
         m_tgtItemData = new PSJdbcTableData(itemData.toXml(
            PSXmlDocumentBuilder.createXmlDocument()));
      }
      catch (PSJdbcTableFactoryException e)
      {
         // this would be a bug of some kind
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
            e.getLocalizedMessage());
      }
      
   }
   
   /**
    * Get the item defintion supplied during construction
    * 
    * @return The item def, never <code>null</code>.
    */
   public PSItemDefinition getItemDef()
   {
      return m_itemDef;
   }
   
   /**
    * Get the table data supplied during construction.  Should be treated as
    * immutable.  Use {@link #getTgtTableData()} to make modifications.
    * 
    * @return The table data, never <code>null</code>.
    */
   public PSJdbcTableData getSrcTableData()
   {
      return m_srcItemData;
   }
   
   /**
    * Get the table data cloned from the source table data during construction.
    * Use the data returned by this method to make modifications.  See 
    * {@link #getSrcTableData()} for more info.
    * 
    * @return The table data, never <code>null</code>.
    */
   public PSJdbcTableData getTgtTableData()
   {
      return m_tgtItemData;
   }
   
   /**
    * Get the table alias matching this object's table data to use when 
    * referencing tables in the item def.
    * 
    * @return The table alias, never <code>null</code> or emtpy after 
    * construction.
    */
   public String getTableAlias()
   {
      if (m_tableAlias == null)
      {
         String tableName = m_srcItemData.getName();
         PSContentEditorPipe pipe = 
            (PSContentEditorPipe) m_itemDef.getContentEditor().getPipe();
         if (pipe != null)
         {
            Iterator tableSets = pipe.getLocator().getTableSets();
            while (tableSets.hasNext() && m_tableAlias == null)
            {
               PSTableSet tableSet = (PSTableSet)tableSets.next();
               Iterator tableRefs = tableSet.getTableRefs();
               while (tableRefs.hasNext())
               {
                  PSTableRef tableRef = (PSTableRef)tableRefs.next();
                  if (tableRef.getName().equalsIgnoreCase(tableName))
                     m_tableAlias = tableRef.getAlias();
               }
            }            
         }   
      }
      
      return m_tableAlias;
   }   

   /**
    * Get the parent field set from the item def supplied during construction.
    * 
    * @return The parent fieldset, never <code>null</code> after construction.
    */
   public PSFieldSet getParentFieldSet()
   {
      PSFieldSet fs = null;
      PSContentEditorPipe pipe = 
         (PSContentEditorPipe) m_itemDef.getContentEditor().getPipe();
      if (pipe != null) // won't be if getTableAlias was called during ctor
      {
         PSContentEditorMapper mapper = pipe.getMapper();
         fs = mapper.getFieldSet();
      }
      
      return fs;
   }
   
   /**
    * The item def supplied to the ctor, never <code>null</code> or modified
    * after that.
    */
   PSItemDefinition m_itemDef;
   
   /**
    * The table data supplied during construction, never <code>null</code> or 
    * modified after that.
    */
   PSJdbcTableData m_srcItemData;
   
   /**
    * A clone of the table data supplied during construction, created at
    * construction time, never modified or <code>null</code> after that.
    */
   PSJdbcTableData m_tgtItemData;
   
   /**
    * The table alias determined during construction, never <code>null</code>,
    * empty, or modified after that.  See {@link #getTableAlias()} for details.
    */
   String m_tableAlias = null;
}

