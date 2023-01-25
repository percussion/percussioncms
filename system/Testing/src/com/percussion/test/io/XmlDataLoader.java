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
