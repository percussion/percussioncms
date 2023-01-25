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

package com.percussion.workflow;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

public class State
{
   public State(int nIndex, Element elem, int width, int height)
   {
      m_nIndex = nIndex;
      m_ElemState = elem;
      m_nWidth = width;
      m_nHeight = height;
   }

   public Element makeElement(Element elemParent, int yLoc)
   {
      Document doc = elemParent.getOwnerDocument();
      Element elemState = doc.createElement("state");
      NamedNodeMap attrs = m_ElemState.getAttributes();
      for(int i=0; i<attrs.getLength(); i++)
      {
         Attr importNode = (Attr)doc.importNode(attrs.item(i), true);
         elemState.setAttributeNode(importNode);
      }
      elemState.setAttribute("xloc", Integer.toString(m_nIndex*m_nWidth));
      elemState.setAttribute("yloc", Integer.toString(yLoc));
      elemState.setAttribute("width", Integer.toString(m_nWidth));

      return elemState;
   }

   public String getID()
   {
      return m_ElemState.getAttribute("id");
   }

   public String getLink()
   {
      return m_ElemState.getAttribute("link");
   }

   public String getLinkNewTransition()
   {
      return m_ElemState.getAttribute("linknewtransition");
   }

   public String getLinkNewAgingTransition()
   {
      return m_ElemState.getAttribute("linknewagingtransition");
   }

   public int getIndex()
   {
      return m_nIndex;
   }

   public String getName()
   {
      return m_ElemState.getAttribute("name");
   }

   public int getMidX()
   {
      return m_nIndex*m_nWidth + m_nWidth/2;
   }

   public int getWidth()
   {
      return m_nWidth;
   }

   int m_nIndex;
   Element m_ElemState;
   int m_nWidth;
   int m_nHeight;
}
