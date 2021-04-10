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
