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
package com.percussion.rx.publisher.jsf.beans;


import com.percussion.rx.jsf.PSCategoryNodeBase;
import com.percussion.rx.jsf.PSEditableNode;
import com.percussion.rx.jsf.PSLockableNavigation;
import com.percussion.rx.jsf.PSNodeBase;
import com.percussion.rx.jsf.PSTreeModel;
import com.percussion.rx.publisher.jsf.nodes.PSContentListViewNode;
import com.percussion.rx.publisher.jsf.nodes.PSContextContainerNode;
import com.percussion.rx.publisher.jsf.nodes.PSDeliveryTypeContainerNode;
import com.percussion.rx.publisher.jsf.nodes.PSSiteContainerNode;

/**
 * Present the navigation for the design tab. This is a JSF managed bean used
 * for the design navigation tree, as well as the views and editors.
 * 
 * TODO: This class needs to have auto load container nodes for context and
 * delivery types added and the static data removed.
 * 
 * @author dougrand
 */
public class PSDesignNavigation extends PSLockableNavigation
{
   /**
    * The title of the root node.
    */
   public static String ROOT_TITLE = "root";
   
   /**
    * The parent node for all site nodes. Initialized by constructor, never
    * <code>null</code> after that.
    */
   private PSSiteContainerNode m_siteNodes;

   /**
    * The parent node for all context nodes. Initialized by constructor, never
    * <code>null</code> after that.
    */
   private PSContextContainerNode m_contextNodes;

   /**
    * The parent node for all delivery type nodes. Initialized by constructor, 
    * never <code>null</code> after that.
    */
   private PSDeliveryTypeContainerNode m_typeNodes;

   /**
    * The parent node for all unused Content List nodes. Initialized by 
    * constructor, never <code>null</code> after that.
    */
   private PSContentListViewNode m_unsedCListNodes;
   
   /**
    * Constructor - create the initial nodes in the tree.
    */
   public PSDesignNavigation() 
   {
      m_siteNodes = new PSSiteContainerNode("Sites", true);
      m_contextNodes = new PSContextContainerNode();
      m_typeNodes = new PSDeliveryTypeContainerNode();
      m_unsedCListNodes = new PSContentListViewNode(
            "Unused Content Lists", PSContentListViewNode.Type.UNUSED, null,
            "UnusedContentLists");

      PSCategoryNodeBase root = new PSCategoryNodeBase(ROOT_TITLE, null);
      m_tree = new PSTreeModel(root, this);

      root.addNode(m_siteNodes);
      root.addNode(m_unsedCListNodes);
      root.addNode(m_contextNodes);
      root.addNode(m_typeNodes);
      
      setStartingNode(m_siteNodes);
   }
   
   /**
    * Gets the "Unused Content List" node.
    * @return the "Unused Content List" node, never <code>null</code>.
    */
   public PSContentListViewNode getUnsedContentList()
   {
      return m_unsedCListNodes;
   }
   
   /*
    * see base class method for details
    */
   @Override
   public boolean getIsLocked()
   {
      return super.getIsLocked() && getCurrentItemGuid() != null;
   }

   /*
    * //see base class method for details
    */
   @Override
   protected void focusOnStartingNode()
   {
      super.focusOnStartingNode();
    
      // set the container node to "Sites" node for populating the site table
      // in sitelist.jsp. The site table is populated with PSNavigation.getList().
      setCurrentCategoryKey((String)getStartingNode().getKey());
      
      resetAllChildNodes();
   }

   /**
    * Reset all child nodes for all top level nodes.
    */
   private void resetAllChildNodes()
   {
      m_siteNodes.resetChildren();
      m_unsedCListNodes.resetChildren();
      m_contextNodes.resetChildren();
      m_typeNodes.resetChildren();

   }
}
