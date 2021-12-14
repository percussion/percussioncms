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
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementations for all ui services.
 */
@Transactional
@PSBaseBean("sys_uiService")
public class PSUiService implements IPSUiService
{


   private SessionFactory sessionFactory;

   public SessionFactory getSessionFactory() {
      return sessionFactory;
   }

   @Autowired
   public void setSessionFactory(SessionFactory sessionFactory) {
      this.sessionFactory = sessionFactory;
   }


   /*
    * (non-Javadoc)
    * 
    * @see IPSUiService#createHierarchyNode(String, IPSGuid,
    * PSHierarchyNode.NodeType)
    */
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
         sessionFactory.getCurrentSession().delete(node);
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

      Session session = sessionFactory.getCurrentSession();
      Criteria criteria = session.createCriteria(PSHierarchyNode.class);

      return (List<PSHierarchyNode>) criteria.list();
   }

   /**
    * To get the hierarchy node properties where the nodes are type guid(non
    * folders)
    */
   public List<PSHierarchyNodeProperty> getAllHierarchyNodesGuidProperties()
   {

      Session session = sessionFactory.getCurrentSession();

      Criteria criteria = session.createCriteria(PSHierarchyNodeProperty.class);
      criteria.add(Restrictions.eq("name", "guid"));

      return (List<PSHierarchyNodeProperty>) criteria.list();
   }

   /*
    * (non-Javadoc)
    * 
    * @see IPSUiService#findHierarchyNodes(String, PSHierarchyNode.NodeType)
    */
   @SuppressWarnings("unchecked")
   public List<PSHierarchyNode> findHierarchyNodes(String name, PSHierarchyNode.NodeType type)
   {
      Session session = sessionFactory.getCurrentSession();

         if (StringUtils.isBlank(name))
            name = "%";

         Criteria criteria = session.createCriteria(PSHierarchyNode.class);
         if (!name.equals("%")) criteria.add(Restrictions.like("name", name));
         if (type != null)
            criteria.add(Restrictions.eq("type", new Integer(type.getOrdinal())));
         criteria.addOrder(Order.asc("name"));

         // find nodes first
         List<PSHierarchyNode> nodes = (List<PSHierarchyNode>) criteria.list();

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
      Session session = sessionFactory.getCurrentSession();

         if (StringUtils.isBlank(name))
            name = "%";

         Criteria criteria = session.createCriteria(PSHierarchyNode.class);
         if (!name.equals("%")) criteria.add(Restrictions.like("name", name));
         if (parentId != null)
            criteria.add(Restrictions.eq("parentId", parentId.longValue()));
         if (type != null)
            criteria.add(Restrictions.eq("type", type.getOrdinal()));
         criteria.addOrder(Order.asc("name"));

         // find nodes first
         List<PSHierarchyNode> nodes = (List<PSHierarchyNode>) criteria.setCacheable(true).list();

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
   @SuppressWarnings("unchecked")
   public PSHierarchyNode loadHierarchyNode(IPSGuid id) throws PSUiException
   {
      if (id == null)
         throw new IllegalArgumentException("id cannot be null");

      Session session = sessionFactory.getCurrentSession();

         PSHierarchyNode node = (PSHierarchyNode) session.get(PSHierarchyNode.class, new Long(id.longValue()));
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
   public void saveHierarchyNode(PSHierarchyNode node)
   {
      if (node == null)
         throw new IllegalArgumentException("node cannot be null");

      Session session = sessionFactory.getCurrentSession();
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
            sessionFactory.getCurrentSession().update(node);
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

      Session session = sessionFactory.getCurrentSession();

         Criteria criteria = session.createCriteria(PSHierarchyNodeProperty.class);
         criteria.add(Restrictions.eq("nodeId", nodeId.longValue())).setCacheable(true);
         List<PSHierarchyNodeProperty> properties = (List<PSHierarchyNodeProperty>) criteria.list();

         return properties;

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

      Session session = sessionFactory.getCurrentSession();

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

      sessionFactory.getCurrentSession().delete(property);
   }
}
