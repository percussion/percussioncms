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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.services.assembly;

import com.percussion.services.contentmgr.IPSNode;

import java.util.List;

import javax.jcr.Node;

/**
 * The interface implemented by the node returned from the navigation
 * (slot or widget) content finder.
 *
 * @author YuBingChen
 */
public interface IPSProxyNode extends IPSNode
{
   /**
    * Gets all ancestor nodes of the current node.
    *  
    * @return the ancestor nodes, where the 1st element is the root node, 2nd
    * element is the direct child node of the root, ... etc. The last element
    * is the direct parent of the current node. It never be <code>null</code>,
    * but may be empty.
    */
    List<Node> getAncestors();
    
    /**
     * Gets the root node.
     * 
     * @return the root node, never <code>null</code>.
     */
    Node getRoot();
}
