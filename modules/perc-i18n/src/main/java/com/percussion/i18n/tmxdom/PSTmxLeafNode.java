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
package com.percussion.i18n.tmxdom;

import com.percussion.xml.PSXmlDocumentBuilder;

import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * Implementation of the interface {@link IPSTmxLeafNode}
 */

public abstract class PSTmxLeafNode
   extends PSTmxNode
   implements IPSTmxLeafNode
{
   /**
    * Value associated with this node. A leaf node will normally have a value 
    * which is a string.
    */
   protected String m_Value = null;


   /*
    * Implementation of the method defined in the interface
    */
   public String getValue()
   {
      if(m_Value==null)
         return "";
      return m_Value;
   }

   /*
    * Implementation of the method defined in the interface
    */
   public void setValue(String value)
   {
      if(value == null)
         value = "";
      Node node = m_DOMElement.getFirstChild();
      if(node instanceof Text)
      {
         ((Text)node).setData(value);
      }
      else
      {
         Text text = m_DOMElement.getOwnerDocument().createTextNode(value);
         PSXmlDocumentBuilder.copyTree(m_DOMElement.getOwnerDocument(),
            m_DOMElement, text, false);
      }
      m_Value = value;
   }
}
