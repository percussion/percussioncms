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
package com.percussion.rx.admin.jsf.beans;

import com.percussion.rx.admin.jsf.nodes.PSTaskContainerNode;
import com.percussion.rx.admin.jsf.nodes.PSTaskLogNode;
import com.percussion.rx.admin.jsf.nodes.PSTaskNotificationContainerNode;
import com.percussion.rx.jsf.PSCategoryNodeBase;
import com.percussion.rx.jsf.PSLockableNavigation;
import com.percussion.rx.jsf.PSLockableNode;
import com.percussion.rx.jsf.PSNodeBase;
import com.percussion.rx.jsf.PSTreeModel;

/**
 * The navigation for the Admin tab. This is a JSF managed bean used
 * for the Admin navigation tree, as well as the views and editors.
 *
 * @author Andriy Palamarchuk
 */
public class PSAdminNavigation extends PSLockableNavigation
{
   /**
    * The parent node for all task nodes. Initialized by constructor, never
    * <code>null</code> after that.
    */
   private PSTaskContainerNode m_taskNodes;

   /**
    * The parent node for all notification nodes. Initialized by constructor, 
    * never <code>null</code> after that.
    */
   private PSTaskNotificationContainerNode m_notifyNodes;
   
   /**
    * The constructor. Creates the initial nodes in the tree.
    */
   public PSAdminNavigation()
   {
      PSCategoryNodeBase root = new PSCategoryNodeBase("root", null);
      m_tree = new PSTreeModel(root, this);
      
      PSNodeBase consoleNode = new PSAdminConsoleBean(
            "Input console commands to Rhythmyx.", "Console");
      root.addNode(consoleNode);
      addToolsNodes(root);
      
      m_taskNodes = new PSTaskContainerNode(); 
      root.addNode(m_taskNodes);
      m_notifyNodes = new PSTaskNotificationContainerNode();
      root.addNode(m_notifyNodes);
      root.addNode(new PSTaskLogNode(
            "The log of the scheduled task execution.",
            "admin-scheduled-task-log", "Task Log"));
      
      setStartingNode(consoleNode);
   }

   @Override
   protected void focusOnStartingNode()
   {
      super.focusOnStartingNode();

      // force to reload all child nodes
      m_taskNodes.resetChildren();
      m_notifyNodes.resetChildren();
   }
   
   /**
    * Attaches the "Tools" category node with sub-nodes to the provided node.
    * @param root the node to attach the category node to.
    * Assumed not <code>null</code>.
    */
   private void addToolsNodes(PSCategoryNodeBase root)
   {
      final PSCategoryNodeBase tools = new PSCategoryNodeBase("Tools",
            PSAdminConsoleBean.CONSOLE_VIEW);
      root.addNode(tools);

      tools.addNode(new PSLockableNode(
            "Convert legacy XSL variants into Velocity based templates.",
            "admin-convert-variants", "Convert Variants to Templates"));
      tools.addNode(new PSLockableNode(
            "Searches and fixes Rhythmyx installation problems",
            "admin-rxfix", "Run RxFix"));
      tools.addNode(new PSLockableNode(
            "Finds and fixes the content items lacking records "
               + "in one of the tables.",
            "admin-checker",
            "Check Repository Consistency"));
   }  
}
