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
