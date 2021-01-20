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
