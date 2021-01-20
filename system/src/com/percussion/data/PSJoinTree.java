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

package com.percussion.data;

import com.percussion.design.objectstore.PSBackEndJoin;
import com.percussion.design.objectstore.PSBackEndTable;

import java.util.HashMap;


/**
 * The PSJoinTree class is used to determine what the connections are
 * between the tables being joined.
 *
 * @author      Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSJoinTree
{
   public PSJoinTree(java.util.List joins)
   {
      super();

      int joinCount = (joins == null) ? 0 : joins.size();
      for (int i = 0; i < joinCount; i++) {
         PSBackEndJoin join = (PSBackEndJoin)joins.get(i);

         // store the tree node for each table in the map
         PSBackEndTable ltab = join.getLeftColumn().getTable();
         PSBackEndTable rtab = join.getRightColumn().getTable();

         PSJoinTreeTableNode lnode = (PSJoinTreeTableNode)m_nodeMap.get(ltab);
         if (lnode == null) {
            lnode = new PSJoinTreeTableNode(ltab);
            m_nodeMap.put(ltab, lnode);
         }

         PSJoinTreeTableNode rnode = (PSJoinTreeTableNode)m_nodeMap.get(rtab);
         if (rnode == null) {
            rnode = new PSJoinTreeTableNode(rtab);
            m_nodeMap.put(rtab, rnode);
         }

         lnode.addJoin(join, rnode);
         rnode.addJoin(join, lnode);

         // if we don't have a root yet, make the first left node the root
         if (m_root == null)
            m_root = lnode;
      }
   }

   public PSJoinTreeTableNode getRoot()
   {
      return m_root;
   }

   public PSJoinTreeTableNode getTableNode(PSBackEndTable tab)
   {
      return (PSJoinTreeTableNode)m_nodeMap.get(tab);
   }

   public boolean hasRoute(PSBackEndTable tab1, PSBackEndTable tab2)
   {
      /* keeping a full list of visited nodes to avoid recursion problems
       * which lead to stack overflows (bug id DBEA-4BWQT4)
       */
      java.util.List visited = new java.util.ArrayList();
      PSJoinTreeTableNode node = (PSJoinTreeTableNode)m_nodeMap.get(tab1);
      return node.hasRoute(tab2, visited);
   }

   class PSJoinTreeTableNode
   {
      PSJoinTreeTableNode(PSBackEndTable nodeTable)
      {
         super();
         m_table = nodeTable;
      }

      PSBackEndTable getTable()
      {
         return m_table;
      }

      void addJoin(PSBackEndJoin join, PSJoinTreeTableNode joinNode)
      {
         PSJoinTreeTableNode l, r;
         if (join.getLeftColumn().getTable().equals(m_table))
         {
            l = this;
            r = joinNode;
         }
         else
         {
            l = joinNode;
            r = this;
         }

         m_joins.add(new PSJoinTreeJoinNode(join, l, r));
      }

      PSJoinTreeJoinNode getJoin(PSBackEndJoin join)
      {
         int size = m_joins.size();
         for (int i = 0; i < size; i++)
         {
            PSJoinTreeJoinNode node = (PSJoinTreeJoinNode)m_joins.get(i);
            if (join.equals(node.getJoin()))
               return node;
         }
            
         return null;
      }

      java.util.List getJoins()
      {
         return m_joins;
      }

      boolean hasRoute(PSBackEndTable tab, java.util.List visited)
      {
         /* keeping a full list of visited nodes to avoid recursion problems
          * which lead to stack overflows (bug id DBEA-4BWQT4)
          */
         if (visited.contains(this))
            return false;

         try {
            visited.add(this);

            // recursively check for a link
            for (int i = 0; i < m_joins.size(); i++) {
               PSJoinTreeJoinNode j = (PSJoinTreeJoinNode)m_joins.get(i);

               PSJoinTreeTableNode t = j.getLeftTableNode();
               if (t.m_table.equals(tab))
                  return true;

               if (t.hasRoute(tab, visited))
                  return true;

               t = j.getRightTableNode();
               if (t.m_table.equals(tab))
                  return true;

               if (t.hasRoute(tab, visited))
                  return true;
            }

            return false;   // no luck if we've gotten here
         } finally {
            visited.remove(this);
         }
      }


      private PSBackEndTable m_table;
      private java.util.List m_joins = new java.util.ArrayList();
   }

   class PSJoinTreeJoinNode
   {
      PSJoinTreeJoinNode(
         PSBackEndJoin join,
         PSJoinTreeTableNode leftNode,
         PSJoinTreeTableNode rightNode)
      {
         super();
         m_join = join;
         m_leftNode = leftNode;
         m_rightNode = rightNode;
      }

      PSJoinTreeTableNode getLeftTableNode()
      {
         return m_leftNode;
      }

      PSJoinTreeTableNode getRightTableNode()
      {
         return m_rightNode;
      }

      PSBackEndJoin getJoin()
      {
         return m_join;
      }


      private PSBackEndJoin         m_join;
      private PSJoinTreeTableNode   m_leftNode;
      private PSJoinTreeTableNode   m_rightNode;
   }

   java.util.Map         m_nodeMap = new HashMap();
   PSJoinTreeTableNode   m_root = null;
}

