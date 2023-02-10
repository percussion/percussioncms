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
import org.w3c.dom.Element;
import org.w3c.dom.TypeInfo;

public class HTMLAttr extends HTMLNode implements Attr
{
   public HTMLAttr(String name, String value)
   {
      super();
      m_name = name;
      setValue(value);
   }

   public int numChildren()
   {
      return 0; // attributes have no children
   }

   public String getName()
   {
      return getNodeName();
   }

   // always return true as there are no default attributes in HTML
   public boolean getSpecified()
   {
      return true;
   }

   public String getValue()
   {
      return getNodeValue();
   }

   public void setValue(String value)
   {
      setNodeValue(value);
   }

   public short getNodeType()
   {
      return ATTRIBUTE_NODE;
   }

   public String toString()
   {
      return getName();
   }

   /**
    * Method introduced later in DOM level 2. Not implemented.
    */
   public Element getOwnerElement()
   {
      // TODO: implement
      throw new RuntimeException("Method getOwnerElement not supported");
   }

   public TypeInfo getSchemaTypeInfo()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public boolean isId()
   {
      // TODO Auto-generated method stub
      return false;
   }
}
