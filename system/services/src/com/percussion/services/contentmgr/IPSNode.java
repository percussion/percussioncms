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

import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.jsr170.IPSJcrCacheItem;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

/**
 * Extended JCR node with added functionality
 * @author dougrand
 */
public interface IPSNode extends Node, IPSJcrCacheItem
{
   /**
    * Get the global unique id for this node
    * @return the guid, never <code>null</code>
    */
   IPSGuid getGuid();
   
   /**
    * Set the depth on a node
    * @param depth
    */
   void setDepth(int depth);
   
   /**
    * Set the parent of a node
    * @param parent the parent node, never <code>null</code>
    * @throws RepositoryException 
    */
   void setParent(Node parent) throws RepositoryException;
   
   /**
    * Gets the multi string values for the specified property.
    * 
    * @param name the name of the specified property.
    * 
    * @return a list of (unique) string values of the property, never <code>null</code>, may be empty.
    * The list is sorted in ascending order if it is not empty.
    * 
    * @throws PathNotFoundException if the property does not exist.
    * @throws RepositoryException if failed to retrieve the value of the properties.
    */
   List<String> getPropertyStringValues(String name) throws PathNotFoundException, RepositoryException;
}
