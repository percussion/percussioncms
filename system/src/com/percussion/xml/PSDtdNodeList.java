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

package com.percussion.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The PSDtdNodeList class is used to represent sequences and
 * option lists in our internal DTD tree.
 *
 * This list will represent either a sequence of nodes or a list
 * of optional nodes.
 *
 * @see        PSDtdNode
 * @see        PSDtdElement
 * @see        PSDtdTree
 *
 * @author     David Gennaco
 * @version    1.0
 * @since      1.0
 */
public class PSDtdNodeList extends PSDtdNode {
   /**
    *                        Base constructor, initialize array list
    *                           default - type set to Sequence
    *
    */
   PSDtdNodeList(int type) {
      super();
      m_nodes = new ArrayList();
      /* Translate invalid types to sequence list */
      if (type != OPTIONLIST)
         type = SEQUENCELIST;

      m_type = type;
   }

   /**
    *                        Construct with occurrences
    *
    *                        @param  type         SEQUENCELIST (,) or OPTIONLIST (|)
    *
    *                        @param  occurrences   the occurrence setting
    *
    */
   PSDtdNodeList(int type, int occurrences)
   {
      super(occurrences);

      /* Translate invalid types to sequence list */
      if (type != OPTIONLIST)
         type = SEQUENCELIST;

      m_type  = type;
      m_nodes = new ArrayList();
   }

   /**
    *   Adds a <code>PSDtdElementEntry</code> object to this list. If node is not
    *   an instance of <code>PSDtdElementEntry</code> then does not add it to
    *   the list.
    *   @param      node      The node to add (append).
    */
   public void add(PSDtdNode node)
   {
      if (node instanceof PSDtdElementEntry)
         m_nodes.add(node);
   }

   /**
    *   Get the number of nodes in this list
    *
    *   @return            Number of nodes
    */
   public int getNumberOfNodes()
   {
      return m_nodes.size();
   }

   /**
    *   Get the node at the specified index
    *
    *   @return            the node at the specified index or
    *                                             <code>null</code> if index is out of range
    */
   public PSDtdNode getNode(int index)
   {
      if ((index < 0) || (index >= m_nodes.size())) {
         return null;
      } else {
         return (PSDtdNode) m_nodes.get(index);
      }
   }


   /**
    *   Return the type associated with this list
    *                        <p>
    *                possible values:
    *       <code>SEQUENCELIST</code>
    *       <code>OPTIONLIST</code>
    */
   public int getType()
   {
      return m_type;
   }

   /**
    *  print is used for debugging/checking DTD structure manually
    */
   public void print(String tab)
   {
      System.out.println(tab + m_type + " " + m_occurrenceType);

      for (int i = 0; i < m_nodes.size(); i++)
      {
         ((PSDtdNode) m_nodes.get(i)).print(tab + "   ");
      }
   }

   /**
    * Add the items in this list to the catalog list.
    * This function should be overridden for all extended classes.
    *   @param   stack         the recursion detection stack
    *   @param   catalogList   the catalog list being built
    * @param   cur         the current name to expand on
    * @param   sep         the element separator string
    * @param   attribId      the string used to identify an attribute entry
    */
   public void catalog(HashMap stack, List catalogList, String cur,
                     String sep, String attribId)
   {
      if (m_nodes != null)
      {
         for (int i = 0; i < m_nodes.size(); i++)
         {
            if (catalogList.size() > PSDtdTree.MAX_CATALOG_SIZE)
               break;

            ((PSDtdNode) m_nodes.get(i)).catalog(stack, catalogList, cur, sep, attribId);
         }
      }

      return;
   }

   public Object acceptVisitor(PSDtdTreeVisitor visitor, Object data)
   {
      return visitor.visit(this, data);
   }

   public Object childrenAccept(PSDtdTreeVisitor visitor, Object data)
   {
      for (int i = 0; i < m_nodes.size(); i++)
      {
         PSDtdNode n = (PSDtdNode)m_nodes.get(i);
         n.acceptVisitor(visitor, data);
      }
      return null;
   }

   List m_nodes;  /* use ArrayList: fastest (non-synchronized) way to
                     maintain insert/append elements */

   int m_type;

   static public final int SEQUENCELIST = ',';
   static public final int OPTIONLIST = '|';
}
