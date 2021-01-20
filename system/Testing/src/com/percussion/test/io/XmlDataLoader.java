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
package com.percussion.test.io;

import java.io.OutputStream;
import java.io.IOException;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.*;

public class XmlDataLoader implements DataLoader
{
   public XmlDataLoader(Document doc, String name, Object ob)
   {
      m_elementName = name;
      m_elementType = ob.getClass().getName();
      m_doc = doc;
      m_root = PSXmlDocumentBuilder.createRoot(m_doc, name);
      m_root.setAttribute("class", m_elementType);
   }

   public XmlDataLoader(Document doc, Element root, String name, Object ob)
   {
      m_elementName = name;
      m_elementType = ob.getClass().getName();
      m_doc = doc;
      m_root = PSXmlDocumentBuilder.addEmptyElement(m_doc, root, m_elementName);
      m_root.setAttribute("class", m_elementType);
   }

   public DataLoader getChildLoader(String name, Object ob)
   {
      return new XmlDataLoader(m_doc, m_root, name, ob);
   }

   public long getLong(String name)
   {
      long ret = 0L;
      PSXmlTreeWalker walker = new PSXmlTreeWalker(m_root);
      String value = walker.getElementData(name, true);
      if (value != null && value.length() != 0)
         ret = Long.parseLong(value);
      return ret;
   }

   public void setLong(String name, long val)
   {
      PSXmlDocumentBuilder.addElement(m_doc, m_root, name, "" + val);
   }

   public double getDouble(String name)
   {
      double ret = 0.0;
      PSXmlTreeWalker walker = new PSXmlTreeWalker(m_root);
      String value = walker.getElementData(name, true);
      if (value != null && value.length() != 0)
         ret = Double.parseDouble(value);
      return ret;
   }

   public void setDouble(String name, double val)
   {
      PSXmlDocumentBuilder.addElement(m_doc, m_root, name, "" + val);
   }

   public String getString(String name)
   {
      String ret = "";
      PSXmlTreeWalker walker = new PSXmlTreeWalker(m_root);
      String value = walker.getElementData(name, true);
      if (value != null)
         ret = value;
      return ret;
   }

   public void setString(String name, String val)
   {
      PSXmlDocumentBuilder.addElement(m_doc, m_root, name, val);
   }

   public void write(OutputStream stream) throws IOException
   {
      PSXmlDocumentBuilder.write(m_root, stream);
   }

   private String m_elementName;
   private String m_elementType;
   private Document m_doc;
   private Element m_root;
}
