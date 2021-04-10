/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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
package com.percussion.taxonomy.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;

import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.design.objectstore.IPSBackEndMapping;
import com.percussion.design.objectstore.PSBackEndColumn;
import com.percussion.design.objectstore.PSContentEditorMapper;
import com.percussion.design.objectstore.PSContentEditorPipe;
import com.percussion.design.objectstore.PSContentEditorSharedDef;
import com.percussion.design.objectstore.PSControlRef;
import com.percussion.design.objectstore.PSDisplayMapper;
import com.percussion.design.objectstore.PSDisplayMapping;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSSharedFieldGroup;
import com.percussion.design.objectstore.PSUISet;
import com.percussion.server.PSServer;
import com.percussion.services.contentmgr.impl.legacy.PSContentRepository;
import com.percussion.services.contentmgr.impl.legacy.PSTypeConfiguration;
import com.percussion.taxonomy.domain.Attribute;
import com.percussion.taxonomy.domain.Node;
import com.percussion.taxonomy.domain.Node_editor;
import com.percussion.taxonomy.domain.Related_node;
import com.percussion.taxonomy.domain.Value;
import com.percussion.taxonomy.repository.NodeDAO;
import com.percussion.taxonomy.repository.NodeServiceInf;

import java.util.Collections;


/**
 * General Service for performing CRUD operations on NODE objects
 * @author rxengineer
 *
 */
public class NodeService implements NodeServiceInf {

   protected final Log logger = LogFactory.getLog(getClass());
   
   /////////////////////////////////////////////////////////
   
   public NodeDAO nodeDAO;

   /////////////////////////////////////////////////////////

   public Collection<Node> getAllNodes(int taxID, int langID) {
      Collection<Node> nodes = null;
      try{
         nodes = (Collection<Node>) this.nodeDAO.getAllNodes(taxID, langID);
      }
      catch (HibernateException e) {
         throw new HibernateException(e);
      }
      return nodes;
   }

   public Node getNode(int nodeID, int langID) {
      Node node = null;
      try {
         node = this.nodeDAO.getNode(nodeID, langID);
      }
      catch (HibernateException e) {
         throw new HibernateException(e);
      }
      return node;
   }

   public Collection<Node> getSomeNodes(Collection<Integer> ids) {
      Collection<Node> nodes = null;
      if (ids != null) {
         try {
            nodes = this.nodeDAO.getSomeNodes(ids);
         } catch (HibernateException e) {
            throw new HibernateException(e);
         }
      }
      return nodes;
   }

   /**
    * Return nodeID, parentID, and name of all nodes for a given taxonomy and
    * search string
    */
   public Collection<Node> getNodesFromSearch(int taxID, 
                                              int langID, 
                                              String search_string, 
                                              boolean exclude_disabled) {
      Collection<Node> nodes = null;
      try {
         nodes = (Collection<Node>) this.nodeDAO.getNodesFromSearch(taxID, langID, search_string, exclude_disabled);
      } catch (HibernateException e) {
         throw new HibernateException(e);
      }
      return nodes;
   }
   
   /**
    * Return all child nodes of the given node
    */
   public Collection<Node> getChildNodes(int nodeID) {
      Collection<Node> children = null;
      try {
         children = this.nodeDAO.getChildNodes(nodeID);
      } catch (HibernateException e) {
         throw new HibernateException(e);
      }
      return children;
   }
   
   public Collection<Node> findNodesByAttribute(Attribute attribute) {
      Collection<Node> nodes = new ArrayList<Node>();
      if (attribute != null) {
         nodes = this.nodeDAO.findNodesByAttribute(attribute);
      }
      return nodes;
   }
   
   /////////////////////////////////////////////////////////

   public void removeNode(Node node) {
      if (node != null) {
         try {
            this.nodeDAO.removeNode(node);
         }
         catch (HibernateException e) {
            throw new HibernateException(e);
         }
      }
   }

   public void saveNode(Node node) {
      if (node != null) {
         try {
            this.nodeDAO.saveNode(node);
         }
         catch (HibernateException e) {
            throw new HibernateException(e);
         }
      }
   }

   /////////////////////////////////////////////////////////

   /**
    * Return nodeID, parentID, and name of all nodes for a given taxonomy
    */
   public Collection<Object[]> getAllNodeNames(int taxonomyID, int langID) {
      Collection<Object[]> names = null;
      try {
         names = (Collection<Object[]>) nodeDAO.getAllNodeNames(taxonomyID, langID);
      } catch (HibernateException e) {
         throw new HibernateException(e);
      }
      return names;
   }

   /**
    * Return nodeID, parentID, and name of all nodes for a given taxonomy
    */
   public Collection<Object[]> getSomeNodeNames(Collection<Integer> ids, int langID) {
      Collection<Object[]> names = null;
      try {
         names = (Collection<Object[]>) this.nodeDAO.getSomeNodeNames(ids, langID);
      } catch (HibernateException e) {
         throw new HibernateException(e);
      }
      return names;
   }

   /**
    * Return a nodeName for the given node
    */
   public Collection<String> getNodeName(int nodeID, int langID) {
      Collection<String> names = null;
      try {
         names = (Collection<String>) this.nodeDAO.getNodeName(nodeID, langID);
      } catch (HibernateException e) {
         throw new HibernateException(e);
      } 
      return names;
   }

   ///////////////////////////////////////////////////////////////////////////////////////////////
   
   /**
    * Return all values associated with a given node
    */
   public Collection<Value> getValuesForNode(int nodeID, int langID) {
      Collection<Value> values = null;
      try {
         values = this.nodeDAO.getValuesForNode(nodeID, langID);
      }
      catch (HibernateException e) {
         throw new HibernateException(e);
      }
      return values;
   }

   /**
    * Return all values associated with a given node and attribute combo
    */
   public Collection<Value> getSpecificValuesForNode(int nodeID, int attrID, int langID) {
      Collection<Value> values = null;
      try {
         values = this.nodeDAO.getSpecificValuesForNode(nodeID, attrID, langID);
      } catch (HibernateException e) {
         throw new HibernateException(e);
      }
      return values;
   }

   //////////////////////////////////////////////////////////////////////////////////////

   /**
    * Return all nodes 'related to' the given node
    */
   public Collection<Related_node> getRelatedNodes(int nodeID) {
      Collection<Related_node> nodes = null;
      try {
         nodes = this.nodeDAO.getRelatedNodes(nodeID);
      }
      catch (HibernateException e) {
         throw new HibernateException(e);
      }
      return nodes;
   }
   
   /**
    * Return all related nodes 'that reference' the given node
    */
   public Collection<Related_node> getRelatedNodeReferences(int nodeID) {
      Collection<Related_node> relatedNodes = null;
      try {
         relatedNodes = this.nodeDAO.getRelatedNodeReferences(nodeID);
      } catch (HibernateException e) {
         throw new HibernateException(e);
      }
      return relatedNodes;
   }

   /**
    * Return all nodes 'similar to' the given node
    */
   public Collection<Related_node> getSimilarNodes(int nodeID) {
      Collection<Related_node> relatedNodes = null;
      try {
         relatedNodes = this.nodeDAO.getSimilarNodes(nodeID);
      } catch (HibernateException e) {
         throw new HibernateException(e);
      }
      return relatedNodes;
   }

   ///////////////////////////////////////////////////////////////////////////////////////

   /**
    * Return all NodeEditors for the given node
    */
   public Collection<Node_editor> getNodeEditors(int nodeID) {
      Collection<Node_editor> editors = null;
      try {
         editors = this.nodeDAO.getNodeEditors(nodeID);
      } catch (HibernateException e) {
         throw new HibernateException(e);
      }
      return editors;
   }

   ///////////////////////////////////////////////////////////////////////////////////////

   /**
    * Change the parent of a node
    */
   public void changeParent(int nodeID, int newParentID) {
      try {
         this.nodeDAO.changeParent(nodeID, newParentID);
      } catch (HibernateException e) {
         throw new HibernateException(e);
      }
   }

   /**
    * Return all titles for all nodes
    */
   public Collection<Object[]> getTitlesForNodes(int taxonomyID, int languageID) {
      Collection<Object[]> titles = null;
      try {
         titles = this.nodeDAO.getTitlesForNodes(taxonomyID, languageID);
      } catch (HibernateException e) {
         throw new HibernateException(e);
      }
      return titles;
   }

   /**
    * If the node doesn't have children, delet it and its relations
    * 
    * @param nodeID
    * @param taxonomyID
    * @return errors if any, null otherwise
    */
   public Map<String, String> deleteNodeAndFriends(int nodeID, int taxonomyID) {
      Map<String, String> results = null;
      try {
         results = this.nodeDAO.deleteNodeAndFriends(nodeID, taxonomyID);
      } catch (HibernateException e) {
         throw new HibernateException(e);
      }
      return results;
   }

   public void setNodeDAO(NodeDAO nodeDAO) {
      this.nodeDAO = nodeDAO;
   }

   public List<PSLocator> findItemsUsingNode(String table, String column, Node node, int maxItems, boolean remove)
   {
      return this.nodeDAO.findItemsUsingNode(table, column, node, maxItems, remove);
   }

   public List<PSLocator> getDbInUse(Node node)
   {
      return findItemsForAllTables(node, -1, false);
   }
   
 
  
   public boolean checkDbInUse(Node node)
   {
      
      return findItemsForAllTables(node, 1, false).size() > 0;
   }
   
   public void deleteNodeFromContent(Node node)
   {
      findItemsForAllTables(node, -1, true);
   }

  
   private List<PSLocator> findItemsForAllTables(Node node, int maxItems, boolean remove)
   {
      HashMap<String,HashSet<String>> fieldCols = getColumnsForControls(Collections.singletonList("sys_TaxonomyAccordion"));
      List<PSLocator> locators = new ArrayList<>();
      for(Entry<String, HashSet<String>> entry : fieldCols.entrySet())
      {
         String table = entry.getKey();
         
         HashSet<String> columns = entry.getValue();
         
         for (String column : columns)
         {
            logger.debug("found Taxonomy column "+table+"."+column);
            locators.addAll(findItemsUsingNode(table,column,node,maxItems-locators.size(), remove));
            if (locators.size()>=maxItems)
               return locators;
         }
      }
      return locators;
   }
   
   private HashMap<String,HashSet<String>> getColumnsForControls(List<String> controlNames)
   {
      PSItemDefManager defMgr = PSItemDefManager.getInstance();
      HashSet<PSField> fields = new HashSet<>();
      HashMap<String,HashSet<String>> tableColMap = new HashMap<>();
   // load system and shared def
      //PSContentEditorSystemDef m_systemDef = PSServer.getContentEditorSystemDef();
      PSContentEditorSharedDef m_sharedDef = PSServer.getContentEditorSharedDef();
      
      Iterator groupsItt = m_sharedDef.getFieldGroups();
      
      
      while (groupsItt.hasNext())
      {
         PSSharedFieldGroup group = (PSSharedFieldGroup)groupsItt.next();
         PSFieldSet fieldSet = group.getFieldSet();
         PSDisplayMapper displayMapper = group.getUIDefinition().getDisplayMapper();
         fields.addAll(getControlFields(fieldSet,displayMapper,controlNames));
      }
      
      
      long[] typeIds = defMgr.getAllContentTypeIds(-1);
      
      List<PSBackEndColumn> retCols = new ArrayList<PSBackEndColumn>();
      
    
      for (int i = 0; i < typeIds.length; i++)
      {
          PSItemDefinition itemDef;
         
          try
          {
              itemDef = defMgr.getItemDef(typeIds[i], -1);

              PSContentEditorPipe pipe = (PSContentEditorPipe)itemDef.getContentEditor().getPipe();
              PSContentEditorMapper mapper = pipe.getMapper();
              PSFieldSet fieldSet = mapper.getFieldSet();
              
              PSDisplayMapper dispMapper = mapper.getUIDefinition().getDisplayMapper();
              
              fields.addAll(getControlFields(fieldSet, dispMapper,controlNames));
              
          }
          catch (PSInvalidContentTypeException e)
          {
              logger.debug("Skipping invalid content type ",e);
          }
      
      }
      
      for (PSField field:fields)
      {
         IPSBackEndMapping locator = field.getLocator();
         if (locator instanceof PSBackEndColumn)
         {
            PSBackEndColumn column = (PSBackEndColumn)locator;
            String table = column.getTable().getTable();
            String columnStr = column.getColumn();
            HashSet<String> tableEntry = tableColMap.get(table);
            if (tableEntry==null)
            {
               tableEntry = new HashSet<>();
               tableColMap.put(table, tableEntry);
            }
            tableEntry.add(columnStr);
            
         }  
            
      }
      return tableColMap;
   }
   
   private List<PSField> getControlFields(PSFieldSet fieldSet, PSDisplayMapper mapper, List<String> controlNames)
   {
      List<PSField> fields = new ArrayList<PSField>();
      Iterator mappings = mapper.iterator();
      while(mappings.hasNext())
      {
         PSDisplayMapping mapping = (PSDisplayMapping) mappings.next();
         
         String fieldName = mapping.getFieldRef();
         
         PSUISet uiSet = mapping.getUISet();
         PSControlRef control = uiSet.getControl();
         if (control!=null)
         {
            String controlName = control.getName();
            if (controlNames.contains(controlName))
            {
               
               Object o = fieldSet.get(fieldName);  
               /**
                * If the field reference is not found in this fieldset, then check
                * whether it is multiproperty simple child field
                */
               if(o == null)
               {
                  o = fieldSet.getChildField(fieldName,
                     PSFieldSet.TYPE_MULTI_PROPERTY_SIMPLE_CHILD);
               }
               
               /* If field reference is field set, then it might be simplechild or
                * complexchild. In case of simple child, we have to show the mapping
                * in parent mapper only, so get the field reference from it's mapper
                * and get the field.
                */
               if(o instanceof PSFieldSet)
               {
                  PSFieldSet childFs = (PSFieldSet)o;
                  if(childFs.getType() == PSFieldSet.TYPE_SIMPLE_CHILD)
                  {
                     PSDisplayMapper childMapper = mapping.getDisplayMapper();
                     Iterator childMappings = childMapper.iterator();
                     while(childMappings.hasNext())
                     {
                        PSDisplayMapping childMapping =
                           (PSDisplayMapping) childMappings.next();
                        fieldName = childMapping.getFieldRef();
                        o = fieldSet.getChildField(fieldName,
                           PSFieldSet.TYPE_SIMPLE_CHILD);
                     }
                  }
                  else
                     //don't recurse into complex child field sets
                     continue;
               } 
               
               
               if (o instanceof PSField)
               {
                  fields.add((PSField) o);
               }
            }
         }
       
      }
      return fields;
   }
   
   ///////////////////////////////////////////////////////////////////////////////
   
}
