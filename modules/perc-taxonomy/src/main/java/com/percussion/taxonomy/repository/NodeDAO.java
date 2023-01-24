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
package com.percussion.taxonomy.repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.percussion.design.objectstore.PSLocator;
import com.percussion.taxonomy.domain.Attribute;
import com.percussion.taxonomy.domain.Node;
import com.percussion.taxonomy.domain.Node_editor;
import com.percussion.taxonomy.domain.Related_node;
import com.percussion.taxonomy.domain.Value;

public interface NodeDAO {

   //////////////////////////////////////////////////////////////////////////////////////////////////////
   
   public Collection<Node> getAllNodes(int taxID, int langID);

   public Collection<Node> getNodesFromSearch(int taxID, 
                                              int langID, 
                                              String search_string, 
                                              boolean exclude_disabled);

   public Node getNode(int nodeID, int langID);

   public Collection<Node> getSomeNodes(Collection<Integer> ids);

   public Collection<Node> getChildNodes(int nodeID);
   
   public Collection<Node> findNodesByAttribute(Attribute attribute);
   
   //////////////////////////////////////////////////////////////////////////////////////////////////////

   public void removeNode(Node node);

   public void saveNode(Node node);

   /**
    * Change the parent of a node
    */
   public void changeParent(int nodeID, int newParentID);
   
   /**
    * If the node doesn't have children, delete it and its relations
    * 
    * @param nodeID
    * @param taxonomyID
    * @return errors if any, null otherwise
    */
   public Map<String, String> deleteNodeAndFriends(int nodeID, int taxonomyID);
   
   //////////////////////////////////////////////////////////////////////////////////////////////////////

   public Collection<Object[]> getAllNodeNames(int taxonomyID, int langID);

   public Collection<Object[]> getSomeNodeNames(Collection<Integer> ids, int langID);

   public Collection<String> getNodeName(int nodeID, int langID);
   
   //////////////////////////////////////////////////////////////////////////////////////////////////////

   public Collection<Value> getValuesForNode(int nodeID, int langID);

   public Collection<Value> getSpecificValuesForNode(int nodeID, int attrID, int langID);

   //////////////////////////////////////////////////////////////////////////////////////////////////////

   /**
    * Return all NodeEditors for the given node
    */
   public Collection<Node_editor> getNodeEditors(int nodeID);

   //////////////////////////////////////////////////////////////////////////////////////////////////////

   public Collection<Related_node> getRelatedNodes(int nodeID);

   public Collection<Related_node> getSimilarNodes(int nodeID);
   
   public Collection<Related_node> getRelatedNodeReferences(int nodeID);

   //////////////////////////////////////////////////////////////////////////////////////////////////////

   /**
    * Return all titles for all nodes
    */
   public Collection<Object[]> getTitlesForNodes(int taxonomyID, int languageID);

   //////////////////////////////////////////////////////////////////////////////////////////////////////
   
   public List<PSLocator> findItemsUsingNode(String table, String column, Node node, int maxItems, boolean remove);
   
}
