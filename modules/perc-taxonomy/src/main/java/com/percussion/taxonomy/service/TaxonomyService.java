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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.taxonomy.service;

import static org.apache.commons.lang.Validate.notNull;

import com.percussion.taxonomy.domain.Attribute;
import com.percussion.taxonomy.domain.Node;
import com.percussion.taxonomy.domain.Taxonomy;
import com.percussion.taxonomy.repository.AttributeServiceInf;
import com.percussion.taxonomy.repository.NodeServiceInf;
import com.percussion.taxonomy.repository.TaxonomyDAO;
import com.percussion.taxonomy.repository.TaxonomyServiceInf;
import com.percussion.taxonomy.repository.VisibilityServiceInf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;

public class TaxonomyService implements TaxonomyServiceInf
{
   /**
    * Auto wired by spring framework.
    * 
    * @param taxonomyDAO the taxonomy DAO service, not <code>null</code>.
    * @param visibilityService the visibility service, not <code>null</code>.
    * @param nodeService the node service, not <code>null</code>.
    * @param attributeService the attribute service, not <code>null</code>.
    */
   public TaxonomyService(TaxonomyDAO taxonomyDAO, VisibilityServiceInf visibilityService, NodeServiceInf nodeService, AttributeServiceInf attributeService)
   {
      this.taxonomyDAO = taxonomyDAO;
      this.visibilityService = visibilityService;
      this.nodeService = nodeService;
      this.attributeService = attributeService;
   }
   
   public Collection<Taxonomy> getAllTaxonomys()
   {
      try
      {
         return taxonomyDAO.getAllTaxonomys();
      }
      catch (HibernateException e)
      {
         throw new HibernateException(e);
      }
   }

   public Taxonomy getTaxonomy(int id)
   {
      return taxonomyDAO.getTaxonomy(id);
   }

   public boolean doesTaxonomyExists(String name)
   {
      List<Taxonomy> result = taxonomyDAO.getTaxonomy(name);
      for (Taxonomy t : result)
      {
         if (name.equalsIgnoreCase(t.getName()))
            return true;
      }
      return false;
   }

   public int getTaxonomyIdByName(String name)
   {
      List<Integer> result = taxonomyDAO.getTaxonomyIdForName(name);
      return (result.size()>0) ? result.get(0) : -1;
   }
   
   public void removeTaxonomy(Taxonomy taxonomy)
   {
      notNull(taxonomy);
      
      removeTaxonomyNodes(taxonomy);

      for (Attribute attribute : taxonomy.getAttributes())
      {
         attributeService.removeAttribute(attribute);
      }

      visibilityService.removeVisibilities(taxonomy.getVisibilities());

      taxonomyDAO.removeTaxonomy(taxonomy);
   }

   public void saveTaxonomy(Taxonomy taxonomy)
   {
      taxonomyDAO.saveTaxonomy(taxonomy);
   }

   /**
    * Removes all nodes associated to the given taxonomy.
    * 
    * @param taxonomy {@link Taxonomy} to get the nodes from. Must not be
    *           <code>null</code>.
    */
   private void removeTaxonomyNodes(Taxonomy taxonomy)
   {
      for (Node node : getNodesInDeletionOrder(taxonomy))
      {
         nodeService.deleteNodeAndFriends(node.getId(), taxonomy.getId());
      }
   }

   /**
    * Iterates over the node list and gets the nodes ordered from root parent to
    * the leaf level nodes. First node is the root, then all its childrens, and
    * so on.
    * 
    * @param taxonomy the {@link Taxonomy} object, must not be <code>null</code>
    * @return {@link List}<{@link Node}> never <code>null</code> but may be
    *         empty.
    */
   public List<Node> getNodesInDeletionOrder(Taxonomy taxonomy)
   {
      notNull(taxonomy);
      
      if (taxonomy.getNodes() == null)
      {
         return new ArrayList<Node>();
      }

      Map<Integer, List<Node>> mapLevelToNodes = buildLevelToNodesMap(taxonomy);
      return getOrderedList(mapLevelToNodes);
   }

   /**
    * Builds a map where the key is the level of a node, and the value is a list of nodes that are in that level.
    * @param taxonomy {@link Taxonomy} object to get the nodes from. Must not be <code>null</code>.
    * @return {@link Map}<{@link Integer}, {@link List}<{@link Node}>> 
    */
   private Map<Integer, List<Node>> buildLevelToNodesMap(Taxonomy taxonomy)
   {
      int nodesToOrder = taxonomy.getNodes().size();
      int level = 0;
      Map<Integer, List<Node>> mapLevelToNodes = new HashMap<Integer, List<Node>>();

      List<Node> rootNodes = getRootNodes(taxonomy.getNodes());
      mapLevelToNodes.put(level, rootNodes);
      nodesToOrder -= rootNodes.size();

      while (nodesToOrder > 0)
      {
         // get all the nodes in the following level
         List<Node> innerLevelList = new ArrayList<Node>();

         for (Node innerRoot : mapLevelToNodes.get(level))
         {
            List<Node> innerChildren = getChildrenForNode(innerRoot, taxonomy.getNodes());
            innerLevelList.addAll(innerChildren);
            nodesToOrder -= innerChildren.size();
         }

         // add them to the map
         level++;
         mapLevelToNodes.put(level, innerLevelList);
      }

      return mapLevelToNodes;
   }

   private List<Node> getOrderedList(Map<Integer, List<Node>> mapLevelToNodes)
   {
      List<Node> ordered = new ArrayList<Node>();
      List<Integer> orderedLevels = new ArrayList<Integer>(mapLevelToNodes.keySet());

      // order from lower to major and then revert it
      Collections.sort(orderedLevels);
      Collections.reverse(orderedLevels);

      for (Integer level : orderedLevels)
      {
         ordered.addAll(mapLevelToNodes.get(level));
      }
      return ordered;
   }

   private List<Node> getChildrenForNode(Node root, Collection<Node> nodes)
   {
      List<Node> children = new ArrayList<Node>();

      for (Node node : nodes)
      {
         if (node.getParent() != null && node.getParent().getId() == root.getId())
         {
            children.add(node);
         }
      }
      return children;
   }

   private List<Node> getRootNodes(Collection<Node> nodes)
   {
      List<Node> rootNodes = new ArrayList<Node>();
      for (Node node : nodes)
      {
         if (node.getParent() == null)
         {
            rootNodes.add(node);
         }
      }
      return rootNodes;
   }

   /**
    * Taxonomy DAO service, initialized by constructor, auto wired by spring framework
    */
   private TaxonomyDAO taxonomyDAO;

   /**
    * Visibility service, initialized by constructor, auto wired by spring framework
    */
   private VisibilityServiceInf visibilityService;

   /**
    * Node service, initialized by constructor, auto wired by spring framework
    */
   private NodeServiceInf nodeService;

   /**
    * Attribute service, initialized by constructor, auto wired by spring framework
    */
   private AttributeServiceInf attributeService;
}
