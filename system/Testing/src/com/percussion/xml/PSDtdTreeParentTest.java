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
