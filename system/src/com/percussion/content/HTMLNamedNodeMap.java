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
package com.percussion.content;

import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class HTMLNamedNodeMap extends HashMap implements NamedNodeMap
{
   public HTMLNamedNodeMap()
   {
      super();
      m_keys = new ArrayList();
   }

   public Node getNamedItem(String name)
   {
      return (Node)get(name);
   }

   public Node setNamedItem(Node arg)
      throws DOMException
   {
      m_keys.add(arg.getNodeName());
      return (Node)put(arg.getNodeName(), arg);
   }

   public Node removeNamedItem(String name)
   {
      m_keys.remove(name);
      Node ret = (Node)remove(name);
      if (ret == null)
         throw new HTMLException(DOMException.NOT_FOUND_ERR, "Node not found");
      return ret;
   }

   public Node item(int index)
   {
      String key = (String)m_keys.get(index);
      return (Node)get(key);
   }

   public int getLength()
   {
      return size();
   }

   /**
    * Method introduced later in DOM level 2. Not implemented.
    */
   public Node getNamedItemNS(String namespaceURI, String localName)
   {
      // TODO: implement
      throw new RuntimeException("Method getNamedItemNS not supported");
   }

   /**
    * Method introduced later in DOM level 2. Not implemented.
    */
   public Node setNamedItemNS(Node arg) throws DOMException
   {
      // TODO: implement
      throw new RuntimeException("Method setNamedItemNS not supported");
   }

   /**
    * Method introduced later in DOM level 2. Not implemented.
    */
   public Node removeNamedItemNS(String namespaceURI, String localName)
      throws DOMException
   {
      // TODO: implement
      throw new RuntimeException("Method removeNamedItemNS not supported");
   }

   private ArrayList m_keys;
}
