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
