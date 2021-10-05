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
