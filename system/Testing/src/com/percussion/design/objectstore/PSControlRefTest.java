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
package com.percussion.design.objectstore;

import com.percussion.util.PSCollection;
import com.percussion.xml.PSXmlDocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// Test case
public class PSControlRefTest extends TestCase
{
   public PSControlRefTest(String name)
   {
      super(name);
   }

   public void testEquals() throws Exception
   {
   }

   public void testXml() throws Exception
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot(doc, "Test");

      // create test object
      PSParam param1 = new PSParam("p1", new PSTextLiteral("param1"));
      PSParam param2 = new PSParam("p2", new PSTextLiteral("param2"));
      PSParam param3 = new PSParam("p3", new PSTextLiteral("param3"));
      PSCollection parameters = new PSCollection(param1.getClass());
      parameters.add(param1);
      parameters.add(param2);
      parameters.add(param3);
      PSControlRef testTo = new PSControlRef("control1", parameters);
      Element elem = testTo.toXml(doc);
      root.appendChild(elem);

      // create a new object and populate it from our testTo element
      PSControlRef testFrom = new PSControlRef(elem, null, null);
      assertTrue(testTo.equals(testFrom));
   }

   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      
      suite.addTest(new PSControlRefTest("testXml"));
      
      return suite;
   }
}
