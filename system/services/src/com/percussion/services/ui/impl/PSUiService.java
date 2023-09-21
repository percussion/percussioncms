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
package com.percussion.services.ui.impl;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.ui.IPSUiErrors;
import com.percussion.services.ui.IPSUiService;
import com.percussion.services.ui.PSUiException;
import com.percussion.services.ui.data.PSHierarchyNode;
import com.percussion.services.ui.data.PSHierarchyNodeProperty;
import com.percussion.util.PSBaseBean;
import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementations for all ui services.
 */
@PSBaseBean("sys_uiService")
@Transactional
public class PSUiService implements IPSUiService
{
   @PersistenceContext
   private EntityManager entityManager;

   private Session getSession(){
      return entityManager.unwrap(Session.class);
   }

   /*
    * (non-Javadoc)
    * 
    * @see IPSUiService#createHierarchyNode(String, IPSGuid,
    * PSHierarchyNode.NodeType)
    */
   @Transactional
   public PSHierarchyNode createHierarchyNode(String name, IPSGuid parentId, PSHierarchyNode.NodeType type)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name cannot be null or empty");

      if (type == null)
         throw new IllegalArgumentException("type cannot be null");

      List<PSHierarchyNode> nodes = findHierarchyNodes(name, parentId, type);
      if (!nodes.isEmpty())
         throw new IllegalArgumentException("name must be unique in parent node");

      IPSGuidManager guidManager = PSGuidManagerLocator.getGuidMgr();

      PSHierarchyNode node = new PSHierarchyNode(name, guidManager.createGuid(PSTypeEnum.HIERARCHY_NODE), type);
      node.setParentId(parentId);

      return node;
   }

   /*
    * (non-Javadoc)
    * 
    * @see IPSUiService#deleteHierarchyNode(IPSGuid)
    */
   @Transactional
   public void deleteHierarchyNode(IPSGuid id)
   {
      if (id == null)
         throw new IllegalArgumentException("id cannot be null");

      try
      {
         PSHierarchyNode node = loadHierarchyNode(id);

         // delete all children recursive
         List<PSHierarchyNode> children = findHierarchyNodes("%", node.getGUID(), null);

         for (PSHierarchyNode child : children)
            deleteHierarchyNode(child.getGUID());

         // delete all properties first
         List<PSHierarchyNodeProperty> properties = loadHierarchyNodeProperties(node.getGUID());
         for (PSHierarchyNodeProperty property : properties)
            deleteHierarchyNodeProperty(property);

         // node delete the node
         getSession().delete(node);
      }
      catch (PSUiException e)
      {
         // ignore non existing node
      }
   }

   /**
    * To get all the hierarchy nodes.
    */

   public List<PSHierarchyNode> getAllHierarchyNodes()
   {

      Session session = getSession();
      CriteriaBuilder builder = session.getCriteriaBuilder();
      CriteriaQuery<PSHierarchyNode> criteria = builder.createQuery(PSHierarchyNode.class);
      Root<PSHierarchyNode> critRoot = criteria.from(PSHierarchyNode.class);

      return (List<PSHierarchyNode>) entityManager.createQuery(criteria).getResultList();
   }

   /**
    * To get the hierarchy node properties where the nodes are type guid(non
    * folders)
    */
   public List<PSHierarchyNodeProperty> getAllHierarchyNodesGuidProperties()
   {

      Session session = getSession();


      CriteriaBuilder builder = session.getCriteriaBuilder();
      CriteriaQuery<PSHierarchyNodeProperty> criteria = builder.createQuery(PSHierarchyNodeProperty.class);
      Root<PSHierarchyNodeProperty> critRoot = criteria.from(PSHierarchyNodeProperty.class);
      criteria.where(builder.equal(critRoot.get("name"),"guid"));


      return  entityManager.createQuery(criteria).getResultList();
   }

   /*
    * (non-Javadoc)
    * 
    * @see IPSUiService#findHierarchyNodes(String, PSHierarchyNode.NodeType)
    */
   @SuppressWarnings("unchecked")
   public List<PSHierarchyNode> findHierarchyNodes(String name, PSHierarchyNode.NodeType type)
   {
      Session session = getSession();

         if (StringUtils.isBlank(name))
            name = "%";

         CriteriaBuilder builder = session.getCriteriaBuilder();
         CriteriaQuery<PSHierarchyNode> criteria = builder.createQuery(PSHierarchyNode.class);
         Root<PSHierarchyNode> critRoot = criteria.from(PSHierarchyNode.class);
         if (!name.equals("%")) criteria.where(builder.like(critRoot.get("name"), name));
         if (type != null)
            criteria.where(builder.equal(critRoot.get("type"), type.getOrdinal()));
         criteria.orderBy(builder.asc(critRoot.get("name")));

         // find nodes first
         List<PSHierarchyNode> nodes = entityManager.createQuery(criteria).getResultList();

         // then load all node properties
         for (PSHierarchyNode node : nodes)
            loadHierarchyNodeProperties(node);

         return nodes;

      }

   /*
    * (non-Javadoc)
    * 
    * @see IPSUiService#findHierarchyNodes(String, IPSGuid,
    * PSHierarchyNode.NodeType)
    */
   @SuppressWarnings("unchecked")
   public List<PSHierarchyNode> findHierarchyNodes(String name, IPSGuid parentId, PSHierarchyNode.NodeType type)
   {
      Session session = getSession();

         if (StringUtils.isBlank(name))
            name = "%";

         CriteriaBuilder builder = session.getCriteriaBuilder();
         CriteriaQuery<PSHierarchyNode> criteria = builder.createQuery(PSHierarchyNode.class);
         Root<PSHierarchyNode> critRoot = criteria.from(PSHierarchyNode.class);
         if (!name.equals("%")) criteria.where(builder.like(critRoot.get("name"), name));
         if (parentId != null)
            criteria.where(builder.equal(critRoot.get("parentId"), parentId.longValue()));
         if (type != null)
            criteria.where(builder.equal(critRoot.get("type"), type.getOrdinal()));
         criteria.orderBy(builder.asc(critRoot.get("name")));

         // find nodes first
         List<PSHierarchyNode> nodes = entityManager.createQuery(criteria).setHint("org.hibernate.cacheable", true).getResultList();

         // then filter out root nodes if requested
         List<PSHierarchyNode> resultNodes = null;
         if (parentId == null)
         {
            resultNodes = new ArrayList<>();
            for (PSHierarchyNode node : nodes)
               if (node.getParentId() == null)
                  resultNodes.add(node);
         }
         else
            resultNodes = nodes;

         // finally load all node properties
         for (PSHierarchyNode node : resultNodes)
            loadHierarchyNodeProperties(node);

         return resultNodes;

      }

   /*
    * (non-Javadoc)
    * 
    * @see IPSUiService#loadHierarchyNode(IPSGuid)
    */
   public PSHierarchyNode loadHierarchyNode(IPSGuid id) throws PSUiException
   {
      if (id == null)
         throw new IllegalArgumentException("id cannot be null");

      Session session = getSession();

         PSHierarchyNode node = session.get(PSHierarchyNode.class, id.longValue());
         if (node == null)
            throw new PSUiException(IPSUiErrors.MISSING_HIERARCHY_NODE, id);

         loadHierarchyNodeProperties(node);
         return node;

      }

   /*
    * (non-Javadoc)
    * 
    * @see IPSUiService#saveHierarchyNode(PSHierarchyNode)
    */
   @Transactional
   public void saveHierarchyNode(PSHierarchyNode node)
   {
      if (node == null)
         throw new IllegalArgumentException("node cannot be null");

      Session session = getSession();
      try
      {
         if (node.getVersion() == null)
         {
            // insert node first
            session.persist(node);

            // then insert properties
            for (String propertyName : node.getProperties().keySet())
            {
               String propertyValue = node.getProperty(propertyName);
               PSHierarchyNodeProperty property = new PSHierarchyNodeProperty(propertyName, propertyValue,
                     node.getGUID());
               saveHierarchyNodeProperty(property);
            }
         }
         else
         {
            // update node first
            session.update(node);

            // then update properties
            List<PSHierarchyNodeProperty> existingProperties = loadHierarchyNodeProperties(node.getGUID());
            for (String propertyName : node.getProperties().keySet())
            {
               String propertyValue = node.getProperty(propertyName);

               boolean exists = false;
               for (PSHierarchyNodeProperty property : existingProperties)
               {
                  if (property.getName().equals(propertyName))
                  {
                     // update existing property
                     property.setValue(propertyValue);
                     saveHierarchyNodeProperty(property);

                     existingProperties.remove(property);
                     exists = true;
                     break;
                  }
               }

               if (!exists)
               {
                  // create new propery
                  PSHierarchyNodeProperty property = new PSHierarchyNodeProperty(propertyName, propertyValue,
                        node.getGUID());
                  saveHierarchyNodeProperty(property);
               }
            }

            // remove removed properties
            for (PSHierarchyNodeProperty property : existingProperties)
               deleteHierarchyNodeProperty(property);
         }
      }
      finally
      {
         session.flush();

      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see IPSUiService#removeChildren(IPSGuid, List)
    */
   @Transactional
   public void removeChildren(IPSGuid parentId, List<IPSGuid> ids)
   {
      if (parentId == null)
         throw new IllegalArgumentException("parentId cannot be null");

      if (ids == null || ids.isEmpty())
         throw new IllegalArgumentException("ids cannot be null or empty");

      List<PSHierarchyNode> children = findHierarchyNodes("%", parentId, null);
      for (IPSGuid id : ids)
      {
         PSHierarchyNode node = findHierarchyNode(children, id);
         if (node != null)
            deleteHierarchyNode(id);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see IPSUiService#moveChildren(IPSGuid, IPSGuid, List)
    */
   @Transactional
   public void moveChildren(IPSGuid sourceId, IPSGuid targetId, List<IPSGuid> ids)
   {
      if (sourceId == null)
         throw new IllegalArgumentException("sourceId cannot be null");

      if (targetId == null)
         throw new IllegalArgumentException("targetId cannot be null");

      if (ids == null || ids.isEmpty())
         throw new IllegalArgumentException("ids cannot be null or empty");

      List<PSHierarchyNode> children = findHierarchyNodes("%", sourceId, null);
      for (IPSGuid id : ids)
      {
         PSHierarchyNode node = findHierarchyNode(children, id);
         if (node != null)
         {
            node.setParentId(targetId);
            getSession().update(node);
         }
      }
   }

   /**
    * Finds and returns the node in the supplied list for the specified id.
    * 
    * @param nodes the list of nodes to search for the node with the specified
    *           id, assumed not <code>null</code>, may be empty.
    * @param id the id of the node to find, assumed not <code>null</code>.
    * @return the foundd node, may be <code>null</code> if not found.
    */
   private PSHierarchyNode findHierarchyNode(List<PSHierarchyNode> nodes, IPSGuid id)
   {
      for (PSHierarchyNode node : nodes)
      {
         if (node.getGUID().equals(id))
            return node;
      }

      return null;
   }

   /**
    * Load all hierarchy node properties for the supplied node id.
    * 
    * @param nodeId the id of the node for which to load all hierarchy node
    *           properties, not <code>null</code>.
    * @return a list with all hierarchy node properties found for the supplied
    *         node, never <code>null</code>, may be empty.
    */
   @SuppressWarnings("unchecked")
   private List<PSHierarchyNodeProperty> loadHierarchyNodeProperties(IPSGuid nodeId)
   {
      if (nodeId == null)
         throw new IllegalArgumentException("nodeId cannot be null");

      Session session = getSession();

         CriteriaBuilder builder = session.getCriteriaBuilder();
         CriteriaQuery<PSHierarchyNodeProperty> criteria = builder.createQuery(PSHierarchyNodeProperty.class);
         Root<PSHierarchyNodeProperty> critRoot = criteria.from(PSHierarchyNodeProperty.class);
         criteria.where(builder.equal(critRoot.get("nodeId"), nodeId.longValue()));
         return entityManager.createQuery(criteria).setHint("org.hibernate.cacheable", true).getResultList();

      }

   /**
    * Load all hierarchy node properties into the supplied node.
    * 
    * @param node the node for which to load all hierarchy node properties, not
    *           <code>null</code>.
    */
   private void loadHierarchyNodeProperties(PSHierarchyNode node)
   {
      if (node == null)
         throw new IllegalArgumentException("node cannot be null");

      List<PSHierarchyNodeProperty> properties = loadHierarchyNodeProperties(node.getGUID());

      for (PSHierarchyNodeProperty property : properties)
         node.addProperty(property.getName(), property.getValue());
   }

   /**
    * Save the supplied hierarchy node property.
    * 
    * @param property the property to be saved, not <code>null</code>.
    */
   private void saveHierarchyNodeProperty(PSHierarchyNodeProperty property)
   {
      if (property == null)
         throw new IllegalArgumentException("property cannot be null");

      Session session = getSession();

         if (property.getVersion() == null)
            session.persist(property);
         else
            session.update(property);

      }

   /**
    * Delete the suppliedd hierarchy node property.
    * 
    * @param property the property to delete, not <code>null</code>.
    */
   private void deleteHierarchyNodeProperty(PSHierarchyNodeProperty property)
   {
      if (property == null)
         throw new IllegalArgumentException("property cannot be null");

      getSession().delete(property);
   }
}
