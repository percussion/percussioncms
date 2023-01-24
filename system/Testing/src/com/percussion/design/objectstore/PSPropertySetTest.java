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
package com.percussion.design.objectstore;

import com.percussion.xml.PSXmlDocumentBuilder;

import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Unit test class for <code>PSPropertySet</code>
 */
public class PSPropertySetTest extends TestCase
{
   /**
    * Tests that the XML serialization has a predictable ordering (ascending
    * alphabetical property name).
    */
   public void testXmlOrdering()
   {
      PSPropertySet pset = new PSPropertySet();     
      pset.setProperty("p1", "v1");
      pset.setProperty("p3", "v3");
      pset.setProperty("p2", "v2");
      
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = pset.toXml(doc);
      NodeList fields = root.getElementsByTagName(PSProperty.XML_NODE_NAME);
      assertEquals(3, fields.getLength());
      assertEquals("p1", ((Element)fields.item(0)).getAttribute("name"));
      assertEquals("p2", ((Element)fields.item(1)).getAttribute("name"));
      assertEquals("p3", ((Element)fields.item(2)).getAttribute("name"));
   }
}
