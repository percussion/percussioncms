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
