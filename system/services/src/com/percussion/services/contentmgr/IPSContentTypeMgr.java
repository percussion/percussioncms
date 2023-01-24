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
package com.percussion.services.contentmgr;

import com.percussion.services.contentmgr.data.PSContentTemplateDesc;
import com.percussion.services.contentmgr.data.PSContentTypeWorkflow;
import com.percussion.utils.guid.IPSGuid;

import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeTypeManager;

/**
 * This interface allows the interrogation and modification of the information
 * about content types in the system. Content types are broadly represented here
 * using the JSR-170 interface {@link javax.jcr.nodetype.NodeDefinition}.
 * 
 * @author dougrand
 */
public interface IPSContentTypeMgr extends NodeTypeManager
{
   /**
    * Create a new concrete node definition object. The new object will have an
    * assigned guid
    * 
    * @return a new node, never <code>null</code>
    */
   IPSNodeDefinition createNodeDefinition();

   /**
    * Load a node definition from the database
    * 
    * @param typeids guids that reference existing nodes, for Rhino these are
    *           legacy guids, may not be <code>null</code> or empty
    * @return a list of definitions, never <code>null</code> or empty, may not
    *         match the size of <code>typeids</code> if some are not present
    *         in the database
    * @throws RepositoryException on errors, but specifically
    *            NoSuchNodeTypeException if none of the definitions are not
    *            found
    */
   List<IPSNodeDefinition> loadNodeDefinitions(List<IPSGuid> typeids)
         throws RepositoryException;

   /**
    * Finds zero or more node definitions that reference the supplied template
    * guid.
    * 
    * @param templateid the template guid, never <code>null</code>
    * @return a non-<code>null</code> list of zero or more node definitions
    * @throws RepositoryException
    */
   List<IPSNodeDefinition> findNodeDefinitionsByTemplate(IPSGuid templateid)
         throws RepositoryException;
   
   /**
    * Find a single node definition that is an exact match to the name. 
    * @param name the name to match, never <code>null</code> or empty
    * @return a single node definition
    * @throws javax.jcr.nodetype.NoSuchNodeTypeException if the definition
    * does not exist
    * @throws RepositoryException if the name is not unique
    */
   IPSNodeDefinition findNodeDefinitionByName(String name) throws RepositoryException;

   /**
    * Save a set of node definitions to the database.
    * 
    * @param defs the node definitions, never <code>null</code> or empty
    * @throws RepositoryException
    */
   void saveNodeDefinitions(List<IPSNodeDefinition> defs)
         throws RepositoryException;

   /**
    * Delete a set of node definitions
    * 
    * @param defs the node definitions to delete, never <code>null</code> or
    *           empty
    * @throws RepositoryException
    */
   void deleteNodeDefinitions(List<IPSNodeDefinition> defs)
         throws RepositoryException;

   /**
    * Find node definitions by name
    * 
    * @param name the name or pattern of the definitions to find, never
    *           <code>null</code> or empty
    * @return the definitions, never <code>null</code> but could be empty
    * @throws RepositoryException if there is a loading problem
    */
   List<IPSNodeDefinition> findNodeDefinitionsByName(String name)
         throws RepositoryException;

   /**
    * Find all node definitions for object type 1.
    * 
    * @return the definitions, never <code>null</code>, may be empty.
    * @throws RepositoryException If there is a loading problem.
    */
   List<IPSNodeDefinition> findAllItemNodeDefinitions()
         throws RepositoryException;
   
   /**
    * Return the content type template associations if any..
    * @param tmpId the template guid, never <code>null</code>
    * @param ctId the content type guid, never <code>null</code>
    * @return List of such associations, may return <code>null</code>
    * @throws RepositoryException
    */
   PSContentTemplateDesc findContentTypeTemplateAssociation(
         IPSGuid tmpId, IPSGuid ctId) throws RepositoryException;

   /**
    * Return the content type workflow associations if any..
    * @param ctId the content type guid, never <code>null</code>
    * @return List of such associations, may return <code>null</code>
    * @throws RepositoryException
    */
   List<PSContentTypeWorkflow> findContentTypeWorkflowAssociations(
         IPSGuid ctId) throws RepositoryException;

   /**
    * Finds zero or more node definitions that reference the supplied workflow
    * guid.
    * 
    * @param workflowid the workflow guid, never <code>null</code>
    * @return a non-<code>null</code> list of zero or more node definitions
    * @throws RepositoryException
    */
   List<IPSNodeDefinition> findNodeDefinitionsByWorkflow(IPSGuid workflowid)
         throws RepositoryException;
}
