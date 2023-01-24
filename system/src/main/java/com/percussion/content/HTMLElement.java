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
package com.percussion.content;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.TypeInfo;

public class HTMLElement extends HTMLNode implements Element
{
   public HTMLElement(String name)
   {
      super();
      m_name = name;
      m_attrs = new HTMLNamedNodeMap();
   }

   public String getTagName()
   {
      return getNodeName();
   }

   public String getAttribute(String name)
   {
      //FB: NP_NONNULL_RETURN_VIOLATION NC 1-17-16
      String ret = "";
      Attr att = getAttributeNode(name);
      if (att != null)
         ret = att.getNodeValue();
      return ret;
   }

   public void setAttribute(String name, String value)
      throws DOMException
   {
      m_attrs.setNamedItem(new HTMLAttr(name, value));
   }

   public void removeAttribute(String name)
      throws DOMException
   {
      m_attrs.removeNamedItem(name);
   }

   public Attr getAttributeNode(String name)
   {
      return (Attr)m_attrs.getNamedItem(name);
   }

   public Attr setAttributeNode(Attr newAttr)
      throws DOMException
   {
      return (Attr)m_attrs.setNamedItem(newAttr);
   }

   public Attr removeAttributeNode(Attr oldAttr)
      throws DOMException
   {
      return (Attr)m_attrs.removeNamedItem(oldAttr.getName());
   }

   public NodeList getElementsByTagName(String name)
   {
      HTMLNodeList list = new HTMLNodeList();
      for (Node kid = getFirstChild(); kid != null; kid = kid.getNextSibling() )
      {
         if (kid.getNodeType() == ELEMENT_NODE)
         {
            Element el = (Element)kid;
            if (el.getTagName().equals(name))
               list.add(kid);

            list.addAll((HTMLNodeList)el.getElementsByTagName(name));
         }
      }
      return list;
   }

   public void normalize()
   {
      // TODO: implement
   }

   // this is overridden by HTMLElement
   public NamedNodeMap getAttributes()
   {
      return m_attrs;
   }

   public short getNodeType()
   {
      return ELEMENT_NODE;
   }

   /**
    * Method introduced later in DOM level 2. Not implemented.
    */
   public String getAttributeNS(String namespaceURI, String localName)
   {
      // TODO: implement
      throw new RuntimeException("Method getAttributeNS not supported");
   }

   /**
    * Method introduced later in DOM level 2. Not implemented.
    */
   public void setAttributeNS(String namespaceURI, String localName, String value)
   {
      // TODO: implement
      throw new RuntimeException("Method setAttributeNS not supported");
   }

   /**
    * Method introduced later in DOM level 2. Not implemented.
    */
   public void removeAttributeNS(String namespaceURI, String localName)
   {
      // TODO: implement
      throw new RuntimeException("Method removeAttributeNS not supported");
   }

   /**
    * Method introduced later in DOM level 2. Not implemented.
    */
   public Attr getAttributeNodeNS(String namespaceURI, String localName)
   {
      // TODO: implement
      throw new RuntimeException("Method getAttributeNodeNS not supported");
   }

   /**
    * Method introduced later in DOM level 2. Not implemented.
    */
   public Attr setAttributeNodeNS(Attr newAttr) throws DOMException
   {
      // TODO: implement
      throw new RuntimeException("Method setAttributeNodeNS not supported");
   }

   /**
    * Method introduced later in DOM level 2. Not implemented.
    */
   public NodeList getElementsByTagNameNS(String namespaceURI, String localName)
   {
      // TODO: implement
      throw new RuntimeException("Method getElementsByTagNameNS not supported");
   }

   /**
    * Method introduced later in DOM level 2. Not implemented.
    */
   public boolean hasAttributeNS(String namespaceURI, String localName)
   {
      // TODO: implement
      throw new RuntimeException("Method hasAttributeNS not supported");
   }

   /**
    * Method introduced later in DOM level 2.
    */
   public boolean hasAttribute(String name)
   {
        return getAttributeNode(name) != null;
   }

  // see interface for description
   public boolean hasAttributes()
   {
      return (m_attrs != null && m_attrs.getLength() != 0);
   }

   private HTMLNamedNodeMap m_attrs;

   public TypeInfo getSchemaTypeInfo()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public void setIdAttribute(String name, boolean isId) throws DOMException
   {
      // TODO Auto-generated method stub
      
   }

   public void setIdAttributeNS(String namespaceURI, String localName, boolean isId) throws DOMException
   {
      // TODO Auto-generated method stub
      
   }

   public void setIdAttributeNode(Attr idAttr, boolean isId) throws DOMException
   {
      // TODO Auto-generated method stub
      
   }
}
