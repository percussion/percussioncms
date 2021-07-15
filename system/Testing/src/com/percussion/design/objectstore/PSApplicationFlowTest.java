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
public class PSApplicationFlowTest extends TestCase
{
   public PSApplicationFlowTest(String name)
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
      PSUrlRequest request = new PSUrlRequest("newRequest", "http://38.227.11.8/Rhythmyx/new.htm", parameters);
      PSUrlRequest request2 = new PSUrlRequest("updateRequest", "http://38.227.11.8/Rhythmyx/update.htm", parameters);
      PSApplicationFlow testTo = new PSApplicationFlow("New", request);
      testTo.setDefaultRedirect("Update", request2);
      PSRule rule = new PSRule(new PSExtensionCallSet());
      PSCollection conditions = new PSCollection(rule.getClass());
      conditions.add(rule);
      conditions.add(rule);
      conditions.add(rule);
      PSConditionalRequest conditionalRequest = new PSConditionalRequest(
         new PSUrlRequest("conditionalRequest", "http://38.227.11.8/Rhythmyx/conditional.htm", parameters),
         conditions);
      PSCollection conditionalRequests = new PSCollection(conditionalRequest.getClass());
      conditionalRequests.add(conditionalRequest);
      conditionalRequests.add(conditionalRequest);
      testTo.addConditionalRedirects("New", conditionalRequests);

      Element elem = testTo.toXml(doc);
      PSXmlDocumentBuilder.copyTree(doc, root, elem, true);

      // create a new object and populate it from our testTo element
      PSApplicationFlow testFrom = new PSApplicationFlow(elem, null, null);
      Document doc2 = PSXmlDocumentBuilder.createXmlDocument();
      Element root2 = PSXmlDocumentBuilder.createRoot(doc2, "Test");
      Element elem2 = testFrom.toXml(doc);
      PSXmlDocumentBuilder.copyTree(doc2, root2, elem2, true);
      assertTrue(testTo.equals(testFrom));
   }

   public static Test suite()
   {
      TestSuite suite = new TestSuite();

      suite.addTest(new PSApplicationFlowTest("testXml"));

      return suite;
   }
}
