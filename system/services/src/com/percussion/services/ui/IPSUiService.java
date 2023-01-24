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
package com.percussion.services.ui;

import com.percussion.services.ui.data.PSHierarchyNode;
import com.percussion.services.ui.data.PSHierarchyNodeProperty;
import com.percussion.utils.guid.IPSGuid;

import java.util.HashMap;
import java.util.List;

/**
 * Provides all functionality used to create, update and delete ui objects
 * to the repository.
 */
public interface IPSUiService
{
   /**
    * Create a new hierarchy node for the supplied name, parent and type.
    * 
    * @param name the name for the new node, not <code>null</code> or empty,
    *    must be unique for the specified parent.
    * @param parentId the id of the parent node, may be <code>null</code> if
    *    this is a new root node.
    * @param type the node type, not <code>null</code>.
    * @return the new created node, never <code>null</code>. The node is
    *    complete including the correct id, but not persisted to the 
    *    repository yet.
    */
   public PSHierarchyNode createHierarchyNode(String name, IPSGuid parentId, 
      PSHierarchyNode.NodeType type);
   
   /**
    * Get all the hierarchy nodes.
    * 
    * @return a list of all the hierarchy nodes.
    */
   public List<PSHierarchyNode> getAllHierarchyNodes();
   
   /**
    * Get all the node properties for the nodes that have name as guid.
    * 
    * 
    * @return a list of hierarchy node properties.
    */
   public List<PSHierarchyNodeProperty> getAllHierarchyNodesGuidProperties();
   
   /**
    * Find all hierarchy nodes for the specified name.
    * 
    * @param name the name of the node to find, may be <code>null</code> or 
    *    empty. Finds all nodes if <code>null</code> or empty, sql type (%) 
    *    wildcards are supported.
    * @param type the node type by which to filter the returned results,
    *    may be <code>null</code> to ignore this filter.
    * @return a list with all found nodes for the specified name, 
    *    never <code>null</code>, may be empty.
    */
   public List<PSHierarchyNode> findHierarchyNodes(String name, 
      PSHierarchyNode.NodeType type);
   
   /**
    * Find all hierarchy nodes for the specified name and parent.
    * 
    * @param name the name of the node to find, may be <code>null</code> or 
    *    empty. Finds all nodes for the specified parent if <code>null</code>
    *    or empty, sql type (%) wildcards are supported.
    * @param parentId the parent for which to find all nodes, may be 
    *    <code>null</code> to find all root nodes.
    * @param type the node type by which to filter the returned results,
    *    may be <code>null</code> to ignore this filter.
    * @return a list with all found nodes for the specified name and parent, 
    *    never <code>null</code>, may be empty.
    */
   public List<PSHierarchyNode> findHierarchyNodes(String name, 
      IPSGuid parentId, PSHierarchyNode.NodeType type);
   
   /**
    * Load the hierarchy node for the specified id.
    * 
    * @param id the id of hierarchy node to load, not <code>null</code>.
    * @return the loaded hierarchy node, never <code>null</code>.
    * @throws PSUiException if no hierarchy node was found for the specified id.
    */
   public PSHierarchyNode loadHierarchyNode(IPSGuid id) throws PSUiException;
   
   /**
    * Save the supplied hierarchy node to the repository.
    * 
    * @param node the hierarchy node to be saved, not <code>null</code>.
    */
   public void saveHierarchyNode(PSHierarchyNode node);
   
   /**
    * Delete the referenced hierarchy node, cases where the node for the 
    * supplied id does not exist anymore are ignored.
    * 
    * @param id the id of the hierarchy node to be deleted, not 
    *    <code>null</code>.
    */
   public void deleteHierarchyNode(IPSGuid id);
   
   /**
    * Remove the specified children from the provided parent. Children which
    * are not found in the parent will be ignored.
    * 
    * @param parentId the id of the parent from which to remove the specified 
    *    children, not <code>null</code>.
    * @param ids the ids of all children to remove from the specified parent,
    *    not <code>null</code> or empty.
    */
   public void removeChildren(IPSGuid parentId, List<IPSGuid> ids);
   
   /**
    * Move all specified children from the source to the target. Children
    * which are not found in the source will be ignored.
    * 
    * @param sourceId the id of the source node from which to move the 
    *    specified children, not <code>null</code>.
    * @param targetId the id of the target to which to move the specified
    *    children, not <code>null</code>.
    * @param ids the ids of all children to move from the source to the target,
    *    not <code>null</code> or empty.
    */
   public void moveChildren(IPSGuid sourceId, IPSGuid targetId, 
      List<IPSGuid> ids);
}

