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
