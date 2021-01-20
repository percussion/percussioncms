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

import com.percussion.util.PSCollection;
import com.percussion.xml.PSXmlDocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// Test case
public class PSCommandHandlerStylesheetsTest extends TestCase
{
   public PSCommandHandlerStylesheetsTest(String name)
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
      PSCollection parameters = new PSCollection("com.percussion.design.objectstore.PSParam");
      PSUrlRequest request = new PSUrlRequest("newRequest", "http://38.227.11.8/Rhythmyx/1111.htm", parameters);
      PSUrlRequest request2 = new PSUrlRequest("newRequest", "http://38.227.11.8/Rhythmyx/2222.htm", parameters);
      PSCommandHandlerStylesheets testTo = new PSCommandHandlerStylesheets("New", new PSStylesheet(request));
      testTo.setDefaultStylesheet("Update", new PSStylesheet(request2));

      PSStylesheet condStylesheet = testTo.getDefaultStylesheet("New");
      PSCollection conditions = new PSCollection(condStylesheet.getClass());
      conditions.add(condStylesheet);
      conditions.add(condStylesheet);
      conditions.add(condStylesheet);
      testTo.addConditionalStylesheets("New", conditions);

      Element elem = testTo.toXml(doc);
      PSXmlDocumentBuilder.copyTree(doc, root, elem, true);

      // create a new object and populate it from our testTo element
      PSCommandHandlerStylesheets testFrom = new PSCommandHandlerStylesheets(elem, null, null);
      Document doc2 = PSXmlDocumentBuilder.createXmlDocument();
      Element root2 = PSXmlDocumentBuilder.createRoot(doc2, "Test");
      Element elem2 = testFrom.toXml(doc);
      PSXmlDocumentBuilder.copyTree(doc2, root2, elem2, true);
      assertTrue(testTo.equals(testFrom));
   }

   public static Test suite()
   {
      TestSuite suite = new TestSuite();

      //suite.addTest(new PSCommandHandlerStylesheetsTest("testEquals"));
      suite.addTest(new PSCommandHandlerStylesheetsTest("testXml"));

      return suite;
   }
}
