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
