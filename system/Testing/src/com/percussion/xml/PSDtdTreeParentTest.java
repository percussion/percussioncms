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
package com.percussion.xml;

/****************************************************************************
 * This is a simple test for a DTD tree to make sure all parents of all
 * element entries (other than the root element entry) have valid parents.
 * It walks the tree and tests all element entries for parents.
 ***************************************************************************/
public class PSDtdTreeParentTest implements PSDtdTreeVisitor
{
   public PSDtdTreeParentTest(PSDtdTree tree)
   {
      tree.getRoot().acceptVisitor(this, null);
   }

   public Object visit(PSDtdNode node, Object data)
   {
      testParent(node);
      node.childrenAccept(this, data);
      return null;
   }

   public Object visit(PSDtdElementEntry node, Object data)
   {
      testParent(node);
      node.childrenAccept(this, data);
      return null;
   }

   public Object visit(PSDtdNodeList node, Object data)
   {
      testParent(node);
      node.childrenAccept(this, data);
      return null;
   }

   public Object visit(PSDtdDataElement node, Object data)
   {
      testParent(node);
      node.childrenAccept(this, data);
      return null;
   }

   public void testParent(PSDtdNode node)
   {
      if (node instanceof PSDtdElementEntry)
      {
         PSDtdElementEntry entry = (PSDtdElementEntry)node;
         if (!entry.getElement().getName().startsWith("#"))
         {
            PSDtdElementEntry parentEl = entry.getParentElement();
            if (parentEl == null)
            {
               if (m_sawRoot)
               {
                  throw new RuntimeException(entry.getElement().getName() + " has a null parent.");
               }
               m_sawRoot = true;
            }
            else
            {
               PSDtdElement el = parentEl.getElement();
               if (el == null)
               {
                  if (m_sawRoot)
                  {
                     throw new RuntimeException(entry.getElement().getName() + "'s parent has a null element.");
                  }
                  m_sawRoot = true;
               }
            }
         }
      }
   }

   boolean m_sawRoot = false;
}
