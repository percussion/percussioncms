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

import org.w3c.dom.DOMException;
import org.w3c.dom.Text;

public class HTMLText extends HTMLNode implements Text
{
   public HTMLText(String initialValue)
   {
      super();
      setNodeValue(initialValue);
   }

   public HTMLText()
   {
      super();
   }

   public String getData()
      throws DOMException
   {
      return getNodeValue();
   }

   public void setData(String data)
      throws DOMException
   {
      setNodeValue(data);
   }

   public int getLength()
   {
      return m_value.length();
   }

   public String substringData(int offset, int count)
      throws DOMException
   {
      return m_value.substring(offset, offset + count);
   }

   public void appendData(String arg)
      throws DOMException
   {
      m_value.append(arg);
   }

   public void insertData(int offset, String arg)
      throws DOMException
   {
      m_value.insert(offset, arg);
   }

   public void deleteData(int offset, int count)
      throws DOMException
   {
      m_value.delete(offset, offset + count);
   }

   public void replaceData(int offset, int count, String arg)
      throws DOMException
   {
      m_value.replace(offset, offset + count, arg);
   }

   public short getNodeType()
   {
      return TEXT_NODE;
   }

   public int numChildren()
   {
      return 0; // text has no children
   }

   public Text splitText(int offset)
      throws DOMException
   {
      // TODO: implement
      return null;
   }

   public boolean isElementContentWhitespace()
   {
      // TODO Auto-generated method stub
      return false;
   }

   public String getWholeText()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public Text replaceWholeText(String content) throws DOMException
   {
      // TODO Auto-generated method stub
      return null;
   }
}
