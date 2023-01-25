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
