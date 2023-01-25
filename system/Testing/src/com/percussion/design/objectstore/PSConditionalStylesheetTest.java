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
public class PSConditionalStylesheetTest extends TestCase
{
   public PSConditionalStylesheetTest(String name)
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
      PSRule rule = new PSRule(new PSExtensionCallSet());
      PSCollection ruleCol = new PSCollection(rule.getClass());
      ruleCol.add(rule);
      ruleCol.add(rule);
      ruleCol.add(rule);
      PSUrlRequest request = new PSUrlRequest("newRequest", "http://38.227.11.8/Rhythmyx/1111.htm", parameters);
      PSConditionalStylesheet testTo = new PSConditionalStylesheet(request, ruleCol);
      Element elem = testTo.toXml(doc);
      PSXmlDocumentBuilder.copyTree(doc, root, elem, true);

      // create a new object and populate it from our testTo element
      PSConditionalStylesheet testFrom = new PSConditionalStylesheet(elem, null, null);
      Document doc2 = PSXmlDocumentBuilder.createXmlDocument();
      Element root2 = PSXmlDocumentBuilder.createRoot(doc2, "Test");
      Element elem2 = testFrom.toXml(doc);
      PSXmlDocumentBuilder.copyTree(doc2, root2, elem2, true);
      assertTrue(testTo.equals(testFrom));
   }

   public static Test suite()
   {
      TestSuite suite = new TestSuite();

      suite.addTest(new PSConditionalStylesheetTest("testXml"));

      return suite;
   }
}
